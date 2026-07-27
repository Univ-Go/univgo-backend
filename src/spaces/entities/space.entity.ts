import {
  Column,
  Entity,
  JoinColumn,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
} from 'typeorm';
import { SpaceType } from './space-type.entity';
import { SpaceSchedule } from './space-schedule.entity';
import { Reservation } from 'src/reservations/entities/reservation.entity';

@Entity('spaces')
export class Space {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ length: 100 })
  name: string;

  @Column()
  capacity: number;

  @Column({ name: 'space_type_id' })
  spaceTypeId: number;

  @ManyToOne(() => SpaceType, (spaceType) => spaceType.spaces)
  @JoinColumn({ name: 'space_type_id' })
  spaceType: SpaceType;

  @OneToMany(() => SpaceSchedule, (schedule) => schedule.space)
  schedules: SpaceSchedule[];

  @OneToMany(() => Reservation, (reservation) => reservation.space)
  reservations: Reservation[];
}
