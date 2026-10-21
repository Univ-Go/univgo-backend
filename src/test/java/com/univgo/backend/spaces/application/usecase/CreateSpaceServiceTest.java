package com.univgo.backend.spaces.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase.CreateSpaceCommand;
import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase.ScheduleWindow;
import com.univgo.backend.spaces.application.port.in.GetAdminSpacesUseCase;
import com.univgo.backend.spaces.application.port.out.ImageUpload;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceTypeRepositoryPort;
import com.univgo.backend.spaces.domain.OverlappingScheduleException;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import com.univgo.backend.spaces.domain.SpaceType;
import com.univgo.backend.spaces.domain.SpaceTypeNotFoundException;
import com.univgo.backend.spaces.domain.TooManyImagesException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSpaceServiceTest {

    private static final UUID TYPE_ID = UUID.randomUUID();
    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final SpaceType TYPE = new SpaceType(TYPE_ID, "Cancha", SpaceCategory.SPORTS);

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceTypeRepositoryPort spaceTypeRepositoryPort;

    @Mock
    private SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;

    @Mock
    private SpaceImageWriter spaceImageWriter;

    @Mock
    private GetAdminSpacesUseCase getAdminSpacesUseCase;

    @InjectMocks
    private CreateSpaceService service;

    private static ImageUpload photo(String filename) {
        return new ImageUpload(filename, "image/jpeg", new byte[] {1, 2, 3});
    }

    private static CreateSpaceCommand command(List<ImageUpload> photos, List<ScheduleWindow> schedules) {
        return new CreateSpaceCommand(
                "Cancha de básquetbol",
                "Complejo Deportivo Central",
                TYPE_ID,
                30,
                "Cancha techada con tableros regulables.",
                List.of("Usa calzado deportivo de suela limpia."),
                schedules,
                photos,
                ADMIN_ID);
    }

    private static List<ScheduleWindow> oneWindow() {
        return List.of(new ScheduleWindow(1, LocalTime.of(6, 0), LocalTime.of(22, 0)));
    }

    @Test
    void refusesASpaceWithNoPhotographs() {
        assertThatThrownBy(() -> service.execute(command(List.of(), oneWindow())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("photograph");

        verify(spaceRepositoryPort, never()).save(any());
    }

    @Test
    void refusesASpaceWithNoOpeningWindow() {
        when(spaceTypeRepositoryPort.findById(TYPE_ID)).thenReturn(Optional.of(TYPE));

        assertThatThrownBy(() -> service.execute(command(List.of(photo("a.jpg")), List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("opening window");

        verify(spaceRepositoryPort, never()).save(any());
    }

    @Test
    void refusesMorePhotographsThanTheSpaceMayHold() {
        doThrow(new TooManyImagesException(9, 8)).when(spaceImageWriter).guardTotal(anyInt());

        assertThatThrownBy(() -> service.execute(command(List.of(photo("a.jpg")), oneWindow())))
                .isInstanceOf(TooManyImagesException.class);

        verify(spaceRepositoryPort, never()).save(any());
    }

    @Test
    void refusesAnUnknownSpaceType() {
        when(spaceTypeRepositoryPort.findById(TYPE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command(List.of(photo("a.jpg")), oneWindow())))
                .isInstanceOf(SpaceTypeNotFoundException.class);

        verify(spaceRepositoryPort, never()).save(any());
    }

    @Test
    void refusesAnOverlappingWeekWithoutWritingAnything() {
        when(spaceTypeRepositoryPort.findById(TYPE_ID)).thenReturn(Optional.of(TYPE));
        List<ScheduleWindow> overlapping = List.of(
                new ScheduleWindow(1, LocalTime.of(6, 0), LocalTime.of(12, 0)),
                new ScheduleWindow(1, LocalTime.of(10, 0), LocalTime.of(22, 0)));

        assertThatThrownBy(() -> service.execute(command(List.of(photo("a.jpg")), overlapping)))
                .isInstanceOf(OverlappingScheduleException.class);

        verify(spaceRepositoryPort, never()).save(any());
        verify(spaceScheduleRepositoryPort, never()).replaceAll(any(), anyList());
    }

    @Test
    void takesTheCategoryFromTheTypeRatherThanFromTheRequest() {
        when(spaceTypeRepositoryPort.findById(TYPE_ID)).thenReturn(Optional.of(TYPE));
        lenient().when(getAdminSpacesUseCase.detail(any())).thenReturn(null);

        service.execute(command(List.of(photo("a.jpg")), oneWindow()));

        ArgumentCaptor<Space> saved = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getCategory()).isEqualTo(SpaceCategory.SPORTS);
        assertThat(saved.getValue().getSpaceTypeId()).isEqualTo(TYPE_ID);
    }

    @Test
    void writesTheSpaceBeforeItsWindowsAndItsPhotographs() {
        when(spaceTypeRepositoryPort.findById(TYPE_ID)).thenReturn(Optional.of(TYPE));
        lenient().when(getAdminSpacesUseCase.detail(any())).thenReturn(null);

        service.execute(command(List.of(photo("a.jpg")), oneWindow()));

        InOrder order = inOrder(spaceRepositoryPort, spaceScheduleRepositoryPort, spaceImageWriter);
        order.verify(spaceRepositoryPort).save(any());
        order.verify(spaceScheduleRepositoryPort).replaceAll(any(), anyList());
        order.verify(spaceImageWriter).store(any(), anyList(), anyInt(), any());
    }

    @Test
    void storesThePhotographsInThePartOrderStartingAtTheCover() {
        when(spaceTypeRepositoryPort.findById(TYPE_ID)).thenReturn(Optional.of(TYPE));
        lenient().when(getAdminSpacesUseCase.detail(any())).thenReturn(null);
        List<ImageUpload> photos = List.of(photo("cover.jpg"), photo("second.jpg"));

        service.execute(command(photos, oneWindow()));

        verify(spaceImageWriter).store(any(), eq(photos), eq(0), eq(ADMIN_ID));
    }

    @Test
    void givesEveryWindowTheNewSpacesId() {
        when(spaceTypeRepositoryPort.findById(TYPE_ID)).thenReturn(Optional.of(TYPE));
        lenient().when(getAdminSpacesUseCase.detail(any())).thenReturn(null);

        service.execute(command(List.of(photo("a.jpg")), oneWindow()));

        ArgumentCaptor<Space> savedSpace = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepositoryPort).save(savedSpace.capture());

        ArgumentCaptor<List<SpaceSchedule>> savedWindows = ArgumentCaptor.captor();
        verify(spaceScheduleRepositoryPort).replaceAll(any(), savedWindows.capture());
        assertThat(savedWindows.getValue())
                .allMatch(window -> window.getSpaceId().equals(savedSpace.getValue().getId()));
    }
}
