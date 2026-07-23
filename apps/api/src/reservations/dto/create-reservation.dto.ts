import {
  ArrayMaxSize,
  IsArray,
  IsDateString,
  IsNotEmpty,
  IsOptional,
  IsString,
} from 'class-validator';

export class CreateReservationDto {
  @IsString()
  @IsNotEmpty()
  identification: string;

  @IsString()
  @IsNotEmpty()
  spaceName: string;

  @IsDateString()
  reservationDate: string;

  @IsString()
  @IsNotEmpty()
  startTime: string;

  @IsString()
  @IsNotEmpty()
  endTime: string;

  @IsArray()
  @IsOptional()
  @ArrayMaxSize(40)
  @IsString({ each: true })
  guestsIdentifications?: string[];
}
