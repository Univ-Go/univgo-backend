"use client";

import dayjs from 'dayjs';

interface BookingConfirmationProps {

    spaceName: string;
    selectedDate: dayjs.Dayjs | null;
    guestCount: number;
    startTime: dayjs.Dayjs | null;
    endTime: dayjs.Dayjs | null;
}

export const BookingConfirmation = ({
    spaceName,
    selectedDate,
    guestCount,
    startTime,
    endTime
}: BookingConfirmationProps) => {

    return (
        <div className="mt-2 mb-6 p-4 bg-[var(--background-color)] border border-[var(--primary-gray)] rounded-lg text-center max-w-full">
            <p className="text-[var(--text)] text-sm break-words">
                Su reserva del <span className="font-medium text-[var(--primary-red)]">{spaceName}</span> para{' '}
                <span className="font-medium text-[var(--primary-red)]">
                    {guestCount + 1} {/* +1 para incluirte a ti */}
                </span>{' '}
                {guestCount + 1 === 1 ? 'persona' : 'personas'} será realizada para el día{' '}
                <span className="font-medium text-[var(--primary-blue)]">
                    {selectedDate
                        ? selectedDate.locale('es').format('dddd D [de] MMMM [de] YYYY')
                        : 'seleccionado'}
                </span>
                {(startTime && endTime) && (
                    <span>
                        {' '}desde las <span className="font-medium text-[var(--primary-blue)]">
                            {startTime.format('HH:mm')}
                        </span> hasta las <span className="font-medium text-[var(--primary-blue)]">
                            {endTime.format('HH:mm')}
                        </span>
                    </span>
                )}
            </p>
        </div>
    )
}