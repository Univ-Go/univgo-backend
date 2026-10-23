package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.in.GetSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReorderSpaceImagesServiceTest {

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID FIRST = UUID.randomUUID();
    private static final UUID SECOND = UUID.randomUUID();
    private static final UUID THIRD = UUID.randomUUID();

    @Mock
    private SpaceImageRepositoryPort spaceImageRepositoryPort;

    @Mock
    private GetSpaceImagesUseCase getSpaceImagesUseCase;

    @InjectMocks
    private ReorderSpaceImagesService service;

    private static SpaceImage image(UUID id, int position) {
        return new SpaceImage(
                id,
                SPACE_ID,
                position,
                SpaceImage.keyFor(SPACE_ID, id),
                "image/jpeg",
                640,
                480,
                42_000,
                LocalDateTime.now(),
                UUID.randomUUID());
    }

    private void existingThree() {
        when(spaceImageRepositoryPort.findBySpaceId(SPACE_ID))
                .thenReturn(List.of(image(FIRST, 0), image(SECOND, 1), image(THIRD, 2)));
    }

    @Test
    void renumbersAFullPermutationFromZero() {
        existingThree();

        service.execute(SPACE_ID, List.of(THIRD, FIRST, SECOND));

        ArgumentCaptor<List<SpaceImage>> saved = ArgumentCaptor.captor();
        verify(spaceImageRepositoryPort).saveAll(saved.capture());
        assertThat(saved.getValue())
                .extracting(SpaceImage::id, SpaceImage::position)
                .containsExactly(Tuple.tuple(THIRD, 0), Tuple.tuple(FIRST, 1), Tuple.tuple(SECOND, 2));
    }

    @Test
    void theFirstPhotographInTheOrderBecomesTheCover() {
        existingThree();

        service.execute(SPACE_ID, List.of(SECOND, FIRST, THIRD));

        ArgumentCaptor<List<SpaceImage>> saved = ArgumentCaptor.captor();
        verify(spaceImageRepositoryPort).saveAll(saved.capture());
        assertThat(saved.getValue().getFirst().id()).isEqualTo(SECOND);
        assertThat(saved.getValue().getFirst().position()).isZero();
    }

    @Test
    void refusesASubsetBecauseItWouldLeaveTheRestUndefined() {
        existingThree();

        assertThatThrownBy(() -> service.execute(SPACE_ID, List.of(THIRD, FIRST)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(spaceImageRepositoryPort, never()).saveAll(anyList());
    }

    @Test
    void refusesAPhotographThatBelongsToAnotherSpace() {
        existingThree();

        assertThatThrownBy(() -> service.execute(SPACE_ID, List.of(FIRST, SECOND, UUID.randomUUID())))
                .isInstanceOf(IllegalArgumentException.class);

        verify(spaceImageRepositoryPort, never()).saveAll(anyList());
    }

    @Test
    void refusesADuplicatedId() {
        existingThree();

        assertThatThrownBy(() -> service.execute(SPACE_ID, List.of(FIRST, FIRST, SECOND)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(spaceImageRepositoryPort, never()).saveAll(anyList());
    }
}
