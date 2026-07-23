import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Reservation } from 'src/reservations/entities/reservation.entity';
import { SpaceSchedule } from './entities/space-schedule.entity';
import { SpaceType } from './entities/space-type.entity';
import { Space } from './entities/space.entity';
import { SpacesService } from './spaces.service';
import { SpacesController } from './spaces.controller';

@Module({
  imports: [
    TypeOrmModule.forFeature([Space, SpaceType, SpaceSchedule, Reservation]),
  ],
  exports: [SpacesService],
  controllers: [SpacesController],
  providers: [SpacesService],
})
export class SpacesModule {}
