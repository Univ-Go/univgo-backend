package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SetSpaceMaintenanceServiceTest {

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @InjectMocks
    private SetSpaceMaintenanceService service;

    @Test
    void flagsAnExistingSpaceUnderMaintenance() {
        UUID spaceId = UUID.randomUUID();
        Space space = new Space(spaceId, "Gimnasio", 30, UUID.randomUUID(), false);
        when(spaceRepositoryPort.findById(spaceId)).thenReturn(Optional.of(space));

        service.execute(spaceId, true);

        ArgumentCaptor<Space> captor = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().isUnderMaintenance()).isTrue();
        assertThat(captor.getValue().getId()).isEqualTo(spaceId);
    }

    @Test
    void throwsWhenSpaceDoesNotExist() {
        UUID spaceId = UUID.randomUUID();
        when(spaceRepositoryPort.findById(spaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(spaceId, true)).isInstanceOf(SpaceNotFoundException.class);
    }
}
