package com.backend.onharu.domain.user.service;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByIdQuery;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByLoginIdQuery;
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
 * 사용자의 유스케이스인 UserQueryService 의 테스트 코드 입니다.
 */
@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserQueryService userQueryService;

    @Test
    @DisplayName("사용자 ID 로 조회시 DB 에서 사용자가 없으면 예외가 발생된다.")
    void getUser_fail_whenUserNotFound () {
        when(userRepository.getUser(any()))
                .thenThrow(new CoreException(ErrorType.User.USER_NOT_FOUND));

        assertThatThrownBy(() ->
                userQueryService.getUser(new GetUserByIdQuery(1L))
        ).isInstanceOf(CoreException.class);

        verify(userRepository, times(1)).getUser(any());
    }

    @Test
    @DisplayName("로그인 아이디로 사용자 조회시 사용자가 없으면 예외가 발생된다.")
    void getUserByLoginId_fail_whenUserNotFound() {
        when(userRepository.getUserByLoginId(any()))
                .thenThrow(new CoreException(ErrorType.User.USER_NOT_FOUND));

        assertThatThrownBy(() ->
                userQueryService.getUserByLoginId(new GetUserByLoginIdQuery("test@test.com"))
        ).isInstanceOf(CoreException.class);

        verify(userRepository, times(1)).getUserByLoginId(any());
    }
}