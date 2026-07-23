"use client";

import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import dayjs from "dayjs";

interface TimeSelectorProps {
    startTime: dayjs.Dayjs | null;
    onStartTimeChange: (time: dayjs.Dayjs | null) => void;
    validHours: number[];
    selectedDate: dayjs.Dayjs | null;
}


export const TimeSelector = ({ startTime, onStartTimeChange, validHours, selectedDate }: TimeSelectorProps) => {
    return (
        <div className="w-full">
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <div className="flex flex-col space-y-3 sm:space-y-4">
                    {/* Selector de hora de entrada */}
                    <div>
                        <p className="text-xs sm:text-sm font-medium text-[var(--text)] mb-1">Hora de entrada</p>
                        <select
                            className="w-full p-2 border rounded text-sm"
                            value={startTime ? startTime.hour() : ''}
                            onChange={e => {
                                const hour = Number(e.target.value);
                                if (!isNaN(hour)) {
                                    onStartTimeChange(dayjs().hour(hour).minute(0).second(0));
                                } else {
                                    onStartTimeChange(null);
                                }
                            }}
                        >
                            <option value="">Selecciona una hora</option>
                                                        {validHours.filter(h => {
                                                            if (selectedDate) {
                                                                const now = dayjs();
                                                                const reservaDateTime = selectedDate.hour(h).minute(0).second(0);
                                                                return reservaDateTime.diff(now, 'hour') >= 24;
                                                            }
                                                            return true;
                                                        }).map(h => (
                                                            <option key={h} value={h}>{h}:00</option>
                                                        ))}
                        </select>
                    </div>
                </div>
            </LocalizationProvider>
        </div>
    )
}