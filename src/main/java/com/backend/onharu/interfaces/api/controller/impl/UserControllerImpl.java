package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.UserFacade;
import com.backend.onharu.domain.user.dto.UserCommand.LoginUserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.*;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.security.LocalUser;
import com.backend.onharu.infra.security.port.ISecuritySession;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IUserController;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API를 제공하는 컨트롤러 구현체입니다.
 * <p>
 * 역할별 회원가입, 프로필 조회/수정, 사용자 정보 관리 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserControllerImpl implements IUserController {

    private final ISecuritySession iSecuritySession;

    private final UserFacade userFacade;

    /**
     * 사업자 회원가입
     * <p>
     * POST /users/signup/owner
     * 사업자 회원가입을 진행합니다. 사용자 정보와 사업자 정보를 함께 받습니다.
     *
     * @param request 사업자 회원가입 요청
     * @return 회원가입 결과
     */
    @Override
    @PostMapping("/signup/owner")
    public ResponseEntity<ResponseDTO<SignUpOwnerResponse>> signUpOwner(
            @Valid @RequestBody SignUpOwnerRequest request
    ) {
        log.info("사업자 회원가입 요청: request={}", request);

        SignUpOwnerCommand command = new SignUpOwnerCommand(
                request.loginId(),
                request.password(),
                request.passwordConfirm(),
                request.name(),
                request.phone(),
                request.storeName(),
                request.businessNumber(),
                request.levelId()
        );

        User user = userFacade.signUpOwner(command);

        SignUpOwnerResponse response = new SignUpOwnerResponse(
                user.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
    }

    /**
     * 아동 회원가입
     * <p>
     * POST /users/signup/child
     * 아동 회원가입을 진행합니다. 사용자 정보와 증명서 파일 URL을 함께 받습니다.
     *
     * @param request 아동 회원가입 요청 (증명서 파일 URL 포함)
     * @return 회원가입 결과
     */
    @Override
    @PostMapping("/signup/child")
    public ResponseEntity<ResponseDTO<SignUpChildResponse>> signUpChild(
            @Valid @RequestBody SignUpChildRequest request
    ) {
        log.info("아동 회원가입 요청: request={}", request);

        SignUpChildCommand command = new SignUpChildCommand(
                request.loginId(),
                request.password(),
                request.passwordConfirm(),
                request.name(),
                request.phone(),
                request.nickname(),
                request.certificate()
        );

        User user = userFacade.signUpChild(command);

        SignUpChildResponse response = new SignUpChildResponse(
                user.getId(),
                user.getLoginId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
    }

    /**
     * 사용자 프로필 조회
     * <p>.
     * GET /users/{userId}/profile
     * Spring Security Context에서 현재 사용자의 역할을 확인하여 역할별 프로필을 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 역할별 프로필 정보
     */
    @Override
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ResponseDTO<?>> getProfile(
            @PathVariable("userId") Long userId
    ) {
        log.info("사용자 프로필 조회 요청: userId={}", userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사용자 프로필 수정
     * <p>
     * PUT /users/{userId}/profile
     * 사용자 프로필을 수정합니다.
     *
     * @param userId       사용자 ID
     * @param childRequest 아동 프로필 수정 요청
     * @param ownerRequest 사업자 프로필 수정 요청
     * @return
     */
    @Override
    @PutMapping("/{userId}/profile")
    public ResponseEntity<ResponseDTO<Void>> updateProfile(
            @PathVariable("userId") Long userId,
            @RequestBody UpdateChildProfileRequest childRequest,
            @RequestBody UpdateOwnerProfileRequest ownerRequest
    ) {
        log.info("사용자 프로필 수정 요청: userId={}, childRequest={}, ownerRequest={}", userId, childRequest, ownerRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사용자 회원 탈퇴
     * <p>
     * DELETE /users/{userId}
     * 사용자 정보를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseDTO<Void>> deleteUser(
            @PathVariable("userId") Long userId
    ) {
        log.info("사용자 정보 삭제 요청: userId={}", userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 로컬 사용자 로그인
     * <p>
     * POST /users/login
     * 사용자 아이디와 비밀번호로 로그인을 수행합니다.
     *
     * @param request 사용자 아이디, 비밀번호
     * @return 200 OK
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<Void>> login(@Valid @RequestBody LoginUserRequest request, HttpServletRequest httpRequest) {
        log.info("사용자 로그인 요청: loginUserRequest={}", request);

        User user = userFacade.loginUser(
                new LoginUserCommand(
                        request.loginId(),
                        request.password()
                )
        );

        iSecuritySession.login(user, httpRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 로컬 사용자 로그아웃
     * <p>
     * POST /users/logout
     * 로그인된 사용자의 로그아웃을 수행합니다.
     *
     * @return 200 OK
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<Void>> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("사용자 로그아웃 요청");

        iSecuritySession.logout(httpRequest, httpResponse);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    @Override
    @PostMapping("/signup/child/finish")
    public ResponseEntity<ResponseDTO<SignUpChildResponse>> finishSignUpChild(@AuthenticationPrincipal User user, @Valid @RequestBody finishSignUpChildRequest request) {
        log.info("소셜 사용자 아동 회원가입 마무리 요청");

        User childUser = userFacade.completeSignUpChildUserOAuth(
                new SignUpChildUserOAuthCommand(
                        user.getId().toString(),
                        request.nickname(),
                        request.certificate())
        );

        SignUpChildResponse response = new SignUpChildResponse(
                childUser.getId(),
                childUser.getLoginId()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    @Override
    @PostMapping("/signup/owner/finish")
    public ResponseEntity<ResponseDTO<SignUpChildResponse>> finishSignUpOwner(@AuthenticationPrincipal User user, @Valid @RequestBody finishSignUpOwnerRequest request) {
        log.info("소셜 사용자 사업자 회원가입 마무리 요청");

        User ownerUser = userFacade.completeSignUpOwnerUserOAuth(
                new SignUpOwnerUserOAuthCommand(
                        user.getId().toString(),
                        request.businessNumber(),
                        request.levelId()
                )
        );

        SignUpChildResponse response = new SignUpChildResponse(
                ownerUser.getId(),
                ownerUser.getLoginId()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }
}
