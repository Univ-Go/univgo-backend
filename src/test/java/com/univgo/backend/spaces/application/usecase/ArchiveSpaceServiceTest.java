package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceReservationCounterPort;
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
class ArchiveSpaceServiceTest {

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    @Mock
    private SpaceReservationCounterPort spaceReservationCounterPort;

    @InjectMocks
    private ArchiveSpaceService service;

    private static SpaceClosure indefiniteClosure() {
        LocalDateTime now = LocalDateTime.now();
        return new SpaceClosure(
                UUID.randomUUID(),
                SPACE_ID,
                now.minusHours(1),
                null,
                ClosureReason.MAINTENANCE,
                null,
                ADMIN_ID,
                now.minusHours(1),
                null,
                null);
    }

    @Test
    void recordsWhoRetiredTheSpaceAndWhen() {
        when(spaceRepositoryPort.existsActiveById(SPACE_ID)).thenReturn(true);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of());
        when(spaceReservationCounterPort.countSuspendable(SPACE_ID)).thenReturn(0);

        service.execute(SPACE_ID, ADMIN_ID);

        verify(spaceRepositoryPort).archive(eq(SPACE_ID), eq(ADMIN_ID), any(LocalDateTime.class));
    }

    @Test
    void shutsTheSpaceWithAClosureThatHasNoEndDate() {
        when(spaceRepositoryPort.existsActiveById(SPACE_ID)).thenReturn(true);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of());
        when(spaceReservationCounterPort.countSuspendable(SPACE_ID)).thenReturn(0);

        service.execute(SPACE_ID, ADMIN_ID);

        ArgumentCaptor<SpaceClosure> saved = ArgumentCaptor.forClass(SpaceClosure.class);
        verify(spaceClosureRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getEndsAt()).isNull();
        assertThat(saved.getValue().getReason()).isEqualTo(ClosureReason.MAINTENANCE);
        assertThat(saved.getValue().getCreatedBy()).isEqualTo(ADMIN_ID);
    }

    @Test
    void doesNotStackASecondClosureOnASpaceAlreadyShutIndefinitely() {
        when(spaceRepositoryPort.existsActiveById(SPACE_ID)).thenReturn(true);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of(indefiniteClosure()));
        when(spaceReservationCounterPort.countSuspendable(SPACE_ID)).thenReturn(0);

        service.execute(SPACE_ID, ADMIN_ID);

        verify(spaceClosureRepositoryPort, never()).save(any());
        verify(spaceRepositoryPort).archive(eq(SPACE_ID), eq(ADMIN_ID), any(LocalDateTime.class));
    }

    @Test
    void reportsHowManyReservationsItSuspends() {
        when(spaceRepositoryPort.existsActiveById(SPACE_ID)).thenReturn(true);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of());
        when(spaceReservationCounterPort.countSuspendable(SPACE_ID)).thenReturn(4);

        assertThat(service.execute(SPACE_ID, ADMIN_ID)).isEqualTo(4);
    }

    @Test
    void refusesASpaceThatIsNotListed() {
        when(spaceRepositoryPort.existsActiveById(SPACE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(SPACE_ID, ADMIN_ID)).isInstanceOf(SpaceNotFoundException.class);

        verify(spaceRepositoryPort, never()).archive(any(), any(), any());
        verify(spaceClosureRepositoryPort, never()).save(any());
    }
}
