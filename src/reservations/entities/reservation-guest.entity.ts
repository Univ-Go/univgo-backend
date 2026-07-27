import {
  Column,
  Entity,
  JoinColumn,
  ManyToOne,
  PrimaryGeneratedColumn,
} from 'typeorm';
import { Reservation } from './reservation.entity';

@Entity('reservation_guests')
export class ReservationGuest {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'reservation_id' })
  reservationId: number;

  @ManyToOne(() => Reservation, (reservation) => reservation.guests, {
    nullable: false,
  })
  @JoinColumn({ name: 'reservation_id' })
  reservation: Reservation;

  @Column({ name: 'guest_identification', length: 20 })
  guestIdentification: string;

  @Column({ name: 'guest_name', length: 300, nullable: true })
  guestName: string;
}
