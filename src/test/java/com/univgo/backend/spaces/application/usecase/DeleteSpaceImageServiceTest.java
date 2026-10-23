package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceImageStoragePort;
import com.univgo.backend.spaces.domain.LastImageException;
import com.univgo.backend.spaces.domain.SpaceImage;
import com.univgo.backend.spaces.domain.SpaceImageNotFoundException;
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
class DeleteSpaceImageServiceTest {

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID FIRST = UUID.randomUUID();
    private static final UUID SECOND = UUID.randomUUID();
    private static final UUID THIRD = UUID.randomUUID();

    @Mock
    private SpaceImageRepositoryPort spaceImageRepositoryPort;

    @Mock
    private SpaceImageStoragePort spaceImageStoragePort;

    @InjectMocks
    private DeleteSpaceImageService service;

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

    @Test
    void closesTheGapLeftByTheRemovedPhotograph() {
        when(spaceImageRepositoryPort.findBySpaceId(SPACE_ID))
                .thenReturn(List.of(image(FIRST, 0), image(SECOND, 1), image(THIRD, 2)));

        service.execute(SPACE_ID, SECOND);

        verify(spaceImageRepositoryPort).delete(SECOND);

        ArgumentCaptor<List<SpaceImage>> saved = ArgumentCaptor.captor();
        verify(spaceImageRepositoryPort).saveAll(saved.capture());
        assertThat(saved.getValue())
                .extracting(SpaceImage::id, SpaceImage::position)
                .containsExactly(Tuple.tuple(FIRST, 0), Tuple.tuple(THIRD, 1));
    }

    @Test
    void removingTheCoverPromotesTheNextPhotograph() {
        when(spaceImageRepositoryPort.findBySpaceId(SPACE_ID))
                .thenReturn(List.of(image(FIRST, 0), image(SECOND, 1)));

        service.execute(SPACE_ID, FIRST);

        ArgumentCaptor<List<SpaceImage>> saved = ArgumentCaptor.captor();
        verify(spaceImageRepositoryPort).saveAll(saved.capture());
        assertThat(saved.getValue().getFirst().id()).isEqualTo(SECOND);
        assertThat(saved.getValue().getFirst().position()).isZero();
    }

    @Test
    void deletesTheStoredDerivativesOfThatPhotographOnly() {
        when(spaceImageRepositoryPort.findBySpaceId(SPACE_ID))
                .thenReturn(List.of(image(FIRST, 0), image(SECOND, 1)));

        service.execute(SPACE_ID, SECOND);

        verify(spaceImageStoragePort).deletePrefix(SpaceImage.keyFor(SPACE_ID, SECOND));
    }

    @Test
    void refusesToRemoveTheLastPhotograph() {
        when(spaceImageRepositoryPort.findBySpaceId(SPACE_ID)).thenReturn(List.of(image(FIRST, 0)));

        assertThatThrownBy(() -> service.execute(SPACE_ID, FIRST)).isInstanceOf(LastImageException.class);

        verify(spaceImageRepositoryPort, never()).delete(any());
        verify(spaceImageStoragePort, never()).deletePrefix(anyString());
    }

    @Test
    void refusesAPhotographThatIsNotThisSpaces() {
        when(spaceImageRepositoryPort.findBySpaceId(SPACE_ID))
                .thenReturn(List.of(image(FIRST, 0), image(SECOND, 1)));

        assertThatThrownBy(() -> service.execute(SPACE_ID, UUID.randomUUID()))
                .isInstanceOf(SpaceImageNotFoundException.class);

        verify(spaceImageRepositoryPort, never()).delete(any());
        verify(spaceImageRepositoryPort, never()).saveAll(anyList());
    }
}
