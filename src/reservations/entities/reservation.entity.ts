import {
  Check,
  Column,
  CreateDateColumn,
  Entity,
  JoinColumn,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { Space } from '../../spaces/entities/space.entity';
import { User } from '../../users/entities/user.entity';
import { ReservationGuest } from './reservation-guest.entity';

export enum ReservationStatus {
  PENDING = 'pending',
  CONFIRMED = 'confirmed',
  CANCELLED_BY_ADMIN = 'cancelled_by_admin',
  CANCELLED_BY_USER = 'cancelled_by_user',
  COMPLETED = 'completed',
}

@Entity('reservations')
@Check(`"end_time" > "start_time"`)
export class Reservation {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'qr_code_data', unique: true })
  qrCodeData: string;

  @Column({ name: 'user_id' })
  userId: number;

  @ManyToOne(() => User, (user) => user.reservations, { nullable: false })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ name: 'space_id' })
  spaceId: number;

  @ManyToOne(() => Space, (space) => space.reservations, { nullable: false })
  @JoinColumn({ name: 'space_id' })
  space: Space;

  @Column({ type: 'date', name: 'reservation_date' })
  reservationDate: Date;

  @Column({ type: 'time', name: 'start_time' })
  startTime: string;

  @Column({ type: 'time', name: 'end_time' })
  endTime: string;

  @Column({
    type: 'enum',
    enum: ReservationStatus,
    default: ReservationStatus.PENDING,
  })
  status: ReservationStatus;

  @CreateDateColumn({
    type: 'timestamp',
    default: () => 'CURRENT_TIMESTAMP',
    name: 'created_at',
  })
  createdAt: Date;

  @UpdateDateColumn({
    type: 'timestamp',
    default: () => 'CURRENT_TIMESTAMP',
    name: 'updated_at',
  })
  updatedAt: Date;

  @OneToMany(() => ReservationGuest, (guest) => guest.reservation)
  guests: ReservationGuest[];
}
