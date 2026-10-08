package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase.ScheduleWindow;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.OverlappingScheduleException;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReplaceSpaceSchedulesServiceTest {

    private static final UUID SPACE_ID = UUID.randomUUID();

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;

    @InjectMocks
    private ReplaceSpaceSchedulesService service;

    private static Space space() {
        return new Space(
                SPACE_ID,
                "Gimnasio",
                "Complejo Deportivo Central",
                30,
                UUID.randomUUID(),
                SpaceCategory.SPORTS,
                "Sala de musculación y cardio.",
                List.of());
    }

    private static ScheduleWindow window(int dayOfWeek, int fromHour, int toHour) {
        return new ScheduleWindow(dayOfWeek, LocalTime.of(fromHour, 0), LocalTime.of(toHour, 0));
    }

    @Test
    void replacesTheWholeWeek() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space()));

        List<SpaceSchedule> result = service.execute(SPACE_ID, List.of(window(1, 6, 22), window(2, 6, 22)));

        assertThat(result).hasSize(2);
        verify(spaceScheduleRepositoryPort).replaceAll(SPACE_ID, result);
    }

    @Test
    void validatesBeforeDeletingSoARefusedWeekLeavesTheOldOneStanding() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space()));

        assertThatThrownBy(() -> service.execute(SPACE_ID, List.of(window(1, 6, 12), window(1, 10, 22))))
                .isInstanceOf(OverlappingScheduleException.class);

        verify(spaceScheduleRepositoryPort, never()).replaceAll(any(), anyList());
    }

    @Test
    void refusesAnEmptyWeekBecauseASpaceWithoutWindowsOffersNoBlocks() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space()));

        assertThatThrownBy(() -> service.execute(SPACE_ID, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("opening window");

        verify(spaceScheduleRepositoryPort, never()).replaceAll(any(), anyList());
    }

    @Test
    void refusesAnUnknownSpace() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(SPACE_ID, List.of(window(1, 6, 22))))
                .isInstanceOf(SpaceNotFoundException.class);

        verify(spaceScheduleRepositoryPort, never()).replaceAll(any(), anyList());
    }
}
