import { BadRequestException, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import * as crypto from 'crypto';
import { User } from 'src/users/entities/user.entity';
import { Repository } from 'typeorm';
import { SpacesService } from '../spaces/spaces.service';
import { UsersService } from '../users/users.service';
import { CreateReservationDto } from './dto/create-reservation.dto';
import { UpdateReservationDto } from './dto/update-reservation.dto';
import { ReservationGuest } from './entities/reservation-guest.entity';
import { Reservation } from './entities/reservation.entity';
import { ReservationStatus } from './entities/reservation.entity';
import { LessThan } from 'typeorm';

@Injectable()
export class ReservationsService {
  constructor(
    @InjectRepository(Reservation)
    private readonly reservationRepository: Repository<Reservation>,
    @InjectRepository(ReservationGuest)
    private readonly reservationGuestRepository: Repository<ReservationGuest>,
    private readonly userService: UsersService,
    private readonly spaceService: SpacesService,
  ) {}

  async create(createReservationDto: CreateReservationDto) {
    const user = await this.userService.findOneByIdentification(
      createReservationDto.identification,
    );

    if (!user) {
      throw new BadRequestException(
        `User with identification ${createReservationDto.identification} does not exist.`,
      );
    }

    const space = await this.spaceService.findOneByName(
      createReservationDto.spaceName.trim(),
    );

    if (!space) {
      throw new BadRequestException(
        `Space with name ${createReservationDto.spaceName.trim()} does not exist.`,
      );
    }

    const guests: User[] = [];

    if (
      createReservationDto.guestsIdentifications &&
      createReservationDto.guestsIdentifications.length > 0
    ) {
      await this.userService.validateGuestsExist(
        createReservationDto.guestsIdentifications,
      );

      for (const identification of createReservationDto.guestsIdentifications) {
        const guest =
          await this.userService.findOneByIdentification(identification);
        if (guest) {
          guests.push(guest);
        }
      }
    }

    const totalAttendees =
      1 + (createReservationDto.guestsIdentifications?.length || 0);

    if (totalAttendees > space.capacity) {
      throw new BadRequestException(
        `The space "${space.name}" has not enough capacity for ${totalAttendees} attendees.`,
      );
    }

    const { identification, reservationDate, startTime, endTime } =
      createReservationDto;

    const rawData = `${identification}-${space.id}-${reservationDate}-${startTime}-${endTime}`;

    const qrCodeData = crypto
      .createHash('sha256')
      .update(rawData)
      .digest('hex')
      .slice(0, 16);

    const reservation = this.reservationRepository.create({
      userId: user.id,
      spaceId: space.id,
      reservationDate: createReservationDto.reservationDate,
      startTime: createReservationDto.startTime,
      endTime: createReservationDto.endTime,
      qrCodeData,
    });

    const savedReservation = await this.reservationRepository.save(reservation);

    if (guests.length > 0) {
      const reservationGuests = guests.map((guest) =>
        this.reservationGuestRepository.create({
          reservationId: savedReservation.id,
          guestIdentification: guest.identification,
          guestName: guest.name,
        }),
      );

      await this.reservationGuestRepository.save(reservationGuests);
    }

    return savedReservation;
  }

  async findOne(id: number): Promise<Reservation | null> {
    return await this.reservationRepository.findOne({
      where: { id },
      relations: ['user', 'space', 'guests'],
    });
  }

  async findAll(): Promise<Reservation[]> {
    return await this.reservationRepository.find({
      relations: ['user', 'space', 'guests'],
    });
  }

  async findByUserIdentification(
    identification: string,
  ): Promise<Reservation[]> {
    const user = await this.userService.findOneByIdentification(identification);

    if (!user) {
      throw new BadRequestException(
        `User with identification ${identification} does not exist.`,
      );
    }

    return await this.reservationRepository.find({
      where: { userId: user.id },
      relations: ['user', 'space', 'guests'],
      order: { createdAt: 'DESC' },
    });
  }

  async update(
    id: number,
    updateReservationDto: UpdateReservationDto,
  ): Promise<Reservation> {
    await this.reservationRepository.update(id, updateReservationDto);
    const updatedReservation = await this.findOne(id);
    if (!updatedReservation) {
      throw new BadRequestException(`Reservation with ID ${id} not found`);
    }
    return updatedReservation;
  }

  async remove(id: number): Promise<void> {
    await this.reservationRepository.delete(id);
  }

  // Cancela automáticamente las reservaciones cuya fecha ya pasó.
  async autoCancelExpiredReservations(): Promise<number> {
    const today = new Date();
    const expiredReservations = await this.reservationRepository.find({
      where: {
        reservationDate: LessThan(today),
        status: ReservationStatus.PENDING,
      },
    });
    for (const reservation of expiredReservations) {
      await this.reservationRepository.update(reservation.id, {
        status: ReservationStatus.CANCELLED_BY_ADMIN,
      });
    }
    return expiredReservations.length;
  }
}
