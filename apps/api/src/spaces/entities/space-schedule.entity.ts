import {
  Check,
  Column,
  Entity,
  JoinColumn,
  ManyToOne,
  PrimaryGeneratedColumn,
} from 'typeorm';
import { Space } from './space.entity';

export enum DayName {
  MONDAY = 'Monday',
  TUESDAY = 'Tuesday',
  WEDNESDAY = 'Wednesday',
  THURSDAY = 'Thursday',
  FRIDAY = 'Friday',
  SATURDAY = 'Saturday',
  SUNDAY = 'Sunday',
}

@Entity('space_schedules')
@Check(`"end_time" > "start_time"`)
export class SpaceSchedule {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'space_id' })
  spaceId: number;

  @ManyToOne(() => Space, (space) => space.schedules, { nullable: false })
  @JoinColumn({ name: 'space_id' })
  space: Space;

  @Column({
    type: 'enum',
    enum: DayName,
    name: 'day_name',
  })
  dayOfWeek: DayName;

  @Column({ type: 'time', name: 'start_time' })
  startTime: string;

  @Column({ type: 'time', name: 'end_time' })
  endTime: string;
}
