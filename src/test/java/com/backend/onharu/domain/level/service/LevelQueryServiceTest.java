package com.backend.onharu.domain.level.service;

import com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByIdQuery;
import com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByNameQuery;
import com.backend.onharu.domain.level.repository.LevelRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
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
 * 등급의 유스케이스인 LevelQueryService 의 테스트 코드 입니다.
 */
@ExtendWith(MockitoExtension.class)
class LevelQueryServiceTest {

    @Mock
    private LevelRepository levelRepository;

    @InjectMocks
    private LevelQueryService levelQueryService;

    @Test
    @DisplayName("등급 ID 조회 시 해당 등급이 존재하지 않으면 예외 발생")
    void getLevel_fail_whenLevelNotFoundLevel() {
        when(levelRepository.getLevel(any()))
                .thenThrow(new CoreException(ErrorType.Level.LEVEL_NOT_FOUND));

        assertThatThrownBy(() ->
                levelQueryService.getLevel(new GetLevelByIdQuery(1L))
        ).isInstanceOf(CoreException.class);

        verify(levelRepository, times(1)).getLevel(any());
    }

    @Test
    @DisplayName("등급명으로 조회시 해당 등급이 존재하지 않으면 예외 발생")
    void getLevelByName_fail_whenLevelNotFoundLevel() {
        when(levelRepository.getLevelByName(any()))
                .thenThrow(new CoreException(ErrorType.Level.LEVEL_NOT_FOUND));

        assertThatThrownBy(() ->
                levelQueryService.getLevelByName(new GetLevelByNameQuery("테스트"))
        ).isInstanceOf(CoreException.class);

        verify(levelRepository, times(1)).getLevelByName(any());
    }
}