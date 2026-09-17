package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        when(spaceRepositoryPort.existsById(spaceId)).thenReturn(true);

        service.execute(spaceId, true);

        verify(spaceRepositoryPort).updateMaintenance(spaceId, true);
    }

    @Test
    void throwsWhenSpaceDoesNotExist() {
        UUID spaceId = UUID.randomUUID();
        when(spaceRepositoryPort.existsById(spaceId)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(spaceId, true)).isInstanceOf(SpaceNotFoundException.class);
        verify(spaceRepositoryPort, never()).updateMaintenance(spaceId, true);
    }
}
