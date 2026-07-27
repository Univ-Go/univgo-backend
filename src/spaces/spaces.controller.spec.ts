import { Test, TestingModule } from '@nestjs/testing';
import { SpacesController } from './spaces.controller';

describe('SpacesController', () => {
  let controller: SpacesController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [SpacesController],
    }).compile();

    controller = module.get<SpacesController>(SpacesController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
