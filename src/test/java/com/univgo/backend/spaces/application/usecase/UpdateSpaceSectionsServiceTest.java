package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.in.UpdateSpaceIdentityUseCase.UpdateSpaceIdentityCommand;
import com.univgo.backend.spaces.application.port.in.UpdateSpaceUsageUseCase.UpdateSpaceUsageCommand;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceTypeRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.SpaceType;
import com.univgo.backend.spaces.domain.SpaceTypeNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The two section saves of the edit screen. They share a test because they share the rule that
 * matters: saving one section must not rewrite the other, which is why the edit screen has two
 * endpoints instead of one full replace.
 */
@ExtendWith(MockitoExtension.class)
class UpdateSpaceSectionsServiceTest {

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID TYPE_ID = UUID.randomUUID();
    private static final UUID OTHER_TYPE_ID = UUID.randomUUID();

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceTypeRepositoryPort spaceTypeRepositoryPort;

    private static Space existing() {
        return new Space(
                SPACE_ID,
                "Gimnasio",
                "Complejo Deportivo Central",
                30,
                TYPE_ID,
                SpaceCategory.SPORTS,
                "Sala de musculación y cardio.",
                List.of("Usa toalla sobre las máquinas."));
    }

    @Test
    void savingTheIdentityKeepsTheDescriptionAndTheRules() {
        UpdateSpaceIdentityService service = new UpdateSpaceIdentityService(spaceRepositoryPort, spaceTypeRepositoryPort);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(existing()));
        when(spaceTypeRepositoryPort.findById(TYPE_ID))
                .thenReturn(Optional.of(new SpaceType(TYPE_ID, "Cancha", SpaceCategory.SPORTS)));

        service.execute(new UpdateSpaceIdentityCommand(SPACE_ID, "Gimnasio de pesas", "Bloque B", TYPE_ID, 40));

        ArgumentCaptor<Space> saved = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Gimnasio de pesas");
        assertThat(saved.getValue().getLocation()).isEqualTo("Bloque B");
        assertThat(saved.getValue().getCapacity()).isEqualTo(40);
        assertThat(saved.getValue().getDescription()).isEqualTo("Sala de musculación y cardio.");
        assertThat(saved.getValue().getRules()).containsExactly("Usa toalla sobre las máquinas.");
    }

    @Test
    void changingTheTypeTakesTheNewTypesCategory() {
        UpdateSpaceIdentityService service = new UpdateSpaceIdentityService(spaceRepositoryPort, spaceTypeRepositoryPort);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(existing()));
        when(spaceTypeRepositoryPort.findById(OTHER_TYPE_ID))
                .thenReturn(Optional.of(new SpaceType(OTHER_TYPE_ID, "Laboratorio", SpaceCategory.LAB)));

        service.execute(new UpdateSpaceIdentityCommand(SPACE_ID, "Gimnasio", "Bloque B", OTHER_TYPE_ID, 30));

        ArgumentCaptor<Space> saved = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getSpaceTypeId()).isEqualTo(OTHER_TYPE_ID);
        assertThat(saved.getValue().getCategory()).isEqualTo(SpaceCategory.LAB);
    }

    @Test
    void refusesAnUnknownTypeWithoutSaving() {
        UpdateSpaceIdentityService service = new UpdateSpaceIdentityService(spaceRepositoryPort, spaceTypeRepositoryPort);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(existing()));
        when(spaceTypeRepositoryPort.findById(OTHER_TYPE_ID)).thenReturn(Optional.empty());

        UpdateSpaceIdentityCommand command =
                new UpdateSpaceIdentityCommand(SPACE_ID, "Gimnasio", "Bloque B", OTHER_TYPE_ID, 30);

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(SpaceTypeNotFoundException.class);

        verify(spaceRepositoryPort, never()).save(any());
    }

    @Test
    void savingTheUsageKeepsTheIdentity() {
        UpdateSpaceUsageService service = new UpdateSpaceUsageService(spaceRepositoryPort);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(existing()));

        service.execute(new UpdateSpaceUsageCommand(SPACE_ID, "Otra descripción.", List.of("Otra regla.")));

        ArgumentCaptor<Space> saved = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Gimnasio");
        assertThat(saved.getValue().getCapacity()).isEqualTo(30);
        assertThat(saved.getValue().getSpaceTypeId()).isEqualTo(TYPE_ID);
        assertThat(saved.getValue().getDescription()).isEqualTo("Otra descripción.");
        assertThat(saved.getValue().getRules()).containsExactly("Otra regla.");
    }

    @Test
    void clearingTheRulesIsAllowedBecauseASpaceMayHaveNone() {
        UpdateSpaceUsageService service = new UpdateSpaceUsageService(spaceRepositoryPort);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(existing()));

        service.execute(new UpdateSpaceUsageCommand(SPACE_ID, "Sala de musculación y cardio.", List.of()));

        ArgumentCaptor<Space> saved = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getRules()).isEmpty();
    }

    @Test
    void refusesAnUnknownSpace() {
        UpdateSpaceUsageService service = new UpdateSpaceUsageService(spaceRepositoryPort);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.empty());

        UpdateSpaceUsageCommand command = new UpdateSpaceUsageCommand(SPACE_ID, "x", List.of());

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(SpaceNotFoundException.class);

        verify(spaceRepositoryPort, never()).save(any());
    }
}
