import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm/dist/typeorm.module';
import { Space } from 'src/spaces/entities/space.entity';
import { User } from 'src/users/entities/user.entity';
import { SpacesModule } from '../spaces/spaces.module';
import { UsersModule } from '../users/users.module';
import { ReservationGuest } from './entities/reservation-guest.entity';
import { Reservation } from './entities/reservation.entity';
import { ReservationsController } from './reservations.controller';
import { ReservationsService } from './reservations.service';

@Module({
  imports: [
    UsersModule,
    SpacesModule,
    TypeOrmModule.forFeature([Reservation, ReservationGuest, Space, User]),
  ],
  controllers: [ReservationsController],
  providers: [ReservationsService],
})
export class ReservationsModule {}
