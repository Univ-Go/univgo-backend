import { Controller, Get } from '@nestjs/common';
import { Public } from '../auth/decorators/public.decorator';

@Controller('ping')
export class PingController {
  @Public()
  @Get()
  ping(): string {
    return 'pong';
  }
}
