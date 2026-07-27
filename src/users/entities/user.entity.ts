import { Reservation } from 'src/reservations/entities/reservation.entity';
import { Column, Entity, OneToMany, PrimaryGeneratedColumn } from 'typeorm';

@Entity()
export class User {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ unique: true, nullable: false })
  identification: string;

  @Column()
  name: string;

  @Column({ nullable: false })
  password: string;

  @Column({ name: 'role_id' })
  roleId: number;

  @OneToMany(() => Reservation, (reservation) => reservation.user)
  reservations: Reservation[];
}
