package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceAforoReportUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceDayBlocksUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockAforoRow;
import com.univgo.backend.reservations.domain.BlockReservations;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupantView;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.reservations.domain.ReservationStatusResolver;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The block panel for one day, plus what the panel does not show: how many reservations each block
 * got, how they ended, and who made them. Capacity and free plazas come straight from
 * {@link GetSpaceDayBlocksUseCase}, so the file says exactly what the admin was looking at on screen.
 */
@Service
public class GetSpaceAforoReportService implements GetSpaceAforoReportUseCase {

    private final GetSpaceDayBlocksUseCase getSpaceDayBlocksUseCase;
    private final SpaceRepositoryPort spaceRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public GetSpaceAforoReportService(
            GetSpaceDayBlocksUseCase getSpaceDayBlocksUseCase,
            SpaceRepositoryPort spaceRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        this.getSpaceDayBlocksUseCase = getSpaceDayBlocksUseCase;
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public AforoReport execute(UUID spaceId, LocalDate date) {
        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findInForceBySpaceId(spaceId));
        LocalDateTime now = LocalDateTime.now();
        BlockReservations day = BlockReservations.of(reservationRepositoryPort.findBySpaceAndDate(spaceId, date));

        List<BlockAforoRow> rows = getSpaceDayBlocksUseCase.execute(spaceId, date).stream()
                .map(summary -> {
                    List<OccupantView> roster = day.of(spaceId, summary.block()).stream()
                            .map(r -> toOccupantView(r, closures, now, config))
                            .toList();
                    return new BlockAforoRow(
                            summary.block(),
                            summary.capacity(),
                            roster.size(),
                            (int) roster.stream().filter(o -> o.checkedInAt() != null).count(),
                            (int) roster.stream().filter(o -> o.state() == ReservationState.EXPIRED).count(),
                            (int) roster.stream().filter(o -> o.state() == ReservationState.CANCELLED).count(),
                            summary.free(),
                            roster);
                })
                .toList();
        return new AforoReport(space.getName(), date, rows);
    }

    // ponytail: one user lookup per reservation, as GetSpaceBlockDetailService does; batch with a
    // findAllById port method if a day's roster ever gets large.
    private OccupantView toOccupantView(
            Reservation reservation, SpaceClosures closures, LocalDateTime now, InstitutionConfig config) {
        Optional<User> student = userRepositoryPort.findById(reservation.getUserId());
        return new OccupantView(
                student.map(user -> user.getFirstName() + " " + user.getLastName()).orElse("Unknown student"),
                student.map(User::getIdentification).orElse(null),
                student.map(User::getSchool).orElse(null),
                ReservationStatusResolver.resolve(reservation, closures, now, config).state(),
                reservation.getCheckedInAt());
    }
}
