import { BadRequestException, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm/repository/Repository';
import { UpdateUserDto } from './dto/update-user.dto';
import { User } from './entities/user.entity';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
  ) {}

  findAll() {
    return this.usersRepository.find();
  }

  async findOneByIdentification(identification: string): Promise<User | null> {
    try {
      return await this.usersRepository.findOneBy({ identification });
    } catch (error) {
      console.error('Error finding user by identification:', error);
      return null;
    }
  }

  update(id: number, updateUserDto: UpdateUserDto) {
    return `This action updates a #${id} user`;
  }

  remove(id: number) {
    return `This action removes a #${id} user`;
  }

  async validateGuestsExist(guestsIdentifications: string[]): Promise<void> {
    const notFoundGuests: string[] = [];

    for (const identification of guestsIdentifications) {
      const guest = await this.findOneByIdentification(identification);
      if (!guest) {
        notFoundGuests.push(identification);
      }
    }

    if (notFoundGuests.length > 0) {
      throw new BadRequestException(
        `The following guests are not part of the University community: ${notFoundGuests.join(', ')}`,
      );
    }
  }
}
