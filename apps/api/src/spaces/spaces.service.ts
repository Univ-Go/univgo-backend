import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { ILike } from 'typeorm';
import { Repository } from 'typeorm/repository/Repository';
import { Space } from './entities/space.entity';

@Injectable()
export class SpacesService {
  constructor(
    @InjectRepository(Space)
    private spacesRepository: Repository<Space>,
  ) {}

  async findOneByName(name: string): Promise<Space | null> {
    return await this.spacesRepository.findOne({
      where: { name: ILike(name) },
    });
  }
}
