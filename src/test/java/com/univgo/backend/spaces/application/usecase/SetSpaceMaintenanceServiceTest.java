package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SetSpaceMaintenanceServiceTest {

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    @InjectMocks
    private SetSpaceMaintenanceService service;

    @Test
    void takingASpaceOutOfServiceClosesItWithNoEndDate() {
        when(spaceRepositoryPort.existsById(SPACE_ID)).thenReturn(true);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of());

        service.execute(SPACE_ID, true, ADMIN_ID);

        ArgumentCaptor<SpaceClosure> saved = ArgumentCaptor.forClass(SpaceClosure.class);
        verify(spaceClosureRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getEndsAt()).isNull();
        assertThat(saved.getValue().getReason()).isEqualTo(ClosureReason.MAINTENANCE);
        assertThat(saved.getValue().getCreatedBy()).isEqualTo(ADMIN_ID);
    }

    @Test
    void switchingItOnTwiceDoesNotStackClosuresToRevertOneByOne() {
        when(spaceRepositoryPort.existsById(SPACE_ID)).thenReturn(true);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of(indefiniteClosure()));

        service.execute(SPACE_ID, true, ADMIN_ID);

        verify(spaceClosureRepositoryPort, never()).save(any());
    }

    @Test
    void handingTheSpaceBackRevertsTheClosureThatHadNoEndDate() {
        SpaceClosure closure = indefiniteClosure();
        when(spaceRepositoryPort.existsById(SPACE_ID)).thenReturn(true);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of(closure));

        service.execute(SPACE_ID, false, ADMIN_ID);

        verify(spaceClosureRepositoryPort).save(closure);
        assertThat(closure.isInForce()).isFalse();
        assertThat(closure.getRevertedBy()).isEqualTo(ADMIN_ID);
    }

    @Test
    void handingTheSpaceBackLeavesAPlannedWindowAlone() {
        SpaceClosure nextTuesday = closure(LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));
        when(spaceRepositoryPort.existsById(SPACE_ID)).thenReturn(true);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of(nextTuesday));

        service.execute(SPACE_ID, false, ADMIN_ID);

        verify(spaceClosureRepositoryPort, never()).save(any());
        assertThat(nextTuesday.isInForce()).isTrue();
    }

    @Test
    void throwsWhenSpaceDoesNotExist() {
        when(spaceRepositoryPort.existsById(SPACE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(SPACE_ID, true, ADMIN_ID)).isInstanceOf(SpaceNotFoundException.class);
        verify(spaceClosureRepositoryPort, never()).save(any());
    }

    private static SpaceClosure indefiniteClosure() {
        return closure(LocalDateTime.now().minusHours(1), null);
    }

    private static SpaceClosure closure(LocalDateTime startsAt, LocalDateTime endsAt) {
        return new SpaceClosure(
                UUID.randomUUID(),
                SPACE_ID,
                startsAt,
                endsAt,
                ClosureReason.MAINTENANCE,
                null,
                ADMIN_ID,
                LocalDateTime.now(),
                null,
                null);
    }
}
