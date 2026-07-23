import { Entity, Column, PrimaryGeneratedColumn, OneToMany } from 'typeorm';
import { Space } from './space.entity';

@Entity('space_types')
export class SpaceType {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ length: 100 })
  name: string;

  @OneToMany(() => Space, (space) => space.spaceType)
  spaces: Space[];
}
