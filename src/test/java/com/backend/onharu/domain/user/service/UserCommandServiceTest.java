package com.backend.onharu.domain.user.service;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * 사용자의 유스케이스인 UserCommandService 의 테스트 코드 입니다.
 */
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserCommandService userCommandService;

    @Test
    @DisplayName("아동 회원가입중 로그인 아이디가 중복될 경우 예외가 발생하고 사용자가 DB에 저장되지 않는다.")
    void signUpChild_fail_whenDuplicateLoginId() {

        SignUpChildCommand command = new SignUpChildCommand("test1234@test.com",
                "password123!",
                "password123!",
                "사업자테스트",
                "01011112222",
                "코끼리땃쥐",
                "/certification/file.pdf"
        );

        // 로그인 아이디 중복을 가정
        when(userRepository.existsByLoginId(any()))
                .thenReturn(true);

        // 예외 발생
        assertThatThrownBy(() -> userCommandService.signUpChild(command))
                .isInstanceOf(CoreException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("사업자 회원가입중 로그인 아이디가 중복될 경우 예외가 발생하고 사용자가 DB에 저장되지 않는다.")
    void signUpOwner_fail_whenDuplicateLoginId() {
        SignUpOwnerCommand command = new SignUpOwnerCommand("test1234@test.com",
                "password123!",
                "password123!",
                "사업자테스트",
                "01011112222",
                "테스트가게명",
                "1234567890",
                "1"
        );

        when(userRepository.existsByLoginId(any()))
                .thenReturn(true);

        assertThatThrownBy(() -> userCommandService.signUpOwner(command))
                .isInstanceOf(CoreException.class);

        verify(userRepository, never()).save(any());
    }

}