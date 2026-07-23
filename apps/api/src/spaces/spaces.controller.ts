import { Controller, Get, Param } from '@nestjs/common';
import { SpacesService } from './spaces.service';

@Controller('spaces')
export class SpacesController {
  constructor(private readonly spacesService: SpacesService) {}

  @Get('name/:name')
  findByName(@Param('name') name: string) {
    return this.spacesService.findOneByName(name);
  }
}
