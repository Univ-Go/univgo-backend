"use client";
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DateCalendar } from '@mui/x-date-pickers/DateCalendar';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import dayjs from 'dayjs';

type DateSelectorProps = {
    selectedDate: dayjs.Dayjs | null;
    onDateChange: (date: dayjs.Dayjs | null) => void;
    validHours: number[];
}

export const DateSelector = ({ selectedDate, onDateChange, validHours = [] }: DateSelectorProps) => {
    // Función para saber si el día tiene al menos una hora válida
    const isDayAvailable = (date: dayjs.Dayjs) => {
        const now = dayjs();
        return validHours.some(h => date.hour(h).minute(0).second(0).diff(now, 'hour') >= 24);
    };
    return (
        <div className="w-full max-w-full">
            <h3 className='text-sm sm:text-base lg:text-lg font-medium text-gray-700 mb-2'>
                Selecciona tu fecha de reserva
            </h3>
            <div className="w-full overflow-hidden flex justify-center">
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                    <DateCalendar
                        disablePast
                        shouldDisableDate={date => date.isSame(dayjs(), 'day') || !isDayAvailable(date)}
                        value={selectedDate}
                        onChange={onDateChange}
                        sx={{
                            // Responsivo y contenido
                            width: '100%',
                            maxWidth: '100%',
                            minWidth: '300px',
                            overflowX: 'hidden',
                            margin: 0,
                            
                            // Header del calendario (título del mes/año)
                            '& .MuiPickersCalendarHeader-root': {
                                paddingLeft: 1,
                                paddingRight: 1,
                                marginBottom: 1,
                                minHeight: '48px',
                            },
                            
                            '& .MuiPickersCalendarHeader-label': {
                                fontSize: { xs: '1rem', sm: '1.125rem' },
                                color: 'var(--text)',
                                fontWeight: 700,
                            },
                            
                            // Flechas de navegación
                            '& .MuiPickersArrowSwitcher-button': {
                                color: 'var(--primary-dark-blue)',
                                padding: '8px',
                                '&:hover': {
                                    backgroundColor: 'var(--gray-light)',
                                }
                            },
                            
                            // Container para los días de la semana
                            '& .MuiDayCalendar-header': {
                                display: 'flex',
                                justifyContent: 'space-around',
                                paddingLeft: 0,
                                paddingRight: 0,
                                marginBottom: '4px',
                            },
                            
                            // Container para las semanas
                            '& .MuiDayCalendar-weekContainer': {
                                display: 'flex',
                                justifyContent: 'space-around',
                                margin: 0,
                                padding: 0,
                            },
                            
                            // Días de la semana (L, M, X...)
                            '& .MuiDayCalendar-weekDayLabel': {
                                fontSize: { xs: '0.75rem', sm: '0.875rem' },
                                color: 'var(--text-light)',
                                fontWeight: 600,
                                width: { xs: '32px', md: '40px' },
                                height: { xs: '32px', md: '40px' },
                                minWidth: { xs: '32px', md: '40px' },
                                maxWidth: { xs: '32px', md: '40px' },
                                minHeight: { xs: '32px', md: '40px' },
                                maxHeight: { xs: '32px', md: '40px' },
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                margin: 0,
                                padding: 0,
                                flex: 'none',
                                boxSizing: 'border-box',
                                flexShrink: 0,
                                flexGrow: 0,
                            },
                            
                            // Días del calendario
                            '& .MuiPickersDay-root': {
                                fontSize: { xs: '0.75rem', sm: '0.875rem' },
                                width: { xs: '32px', md: '40px' },
                                height: { xs: '32px', md: '40px' },
                                minWidth: { xs: '32px', md: '40px' },
                                maxWidth: { xs: '32px', md: '40px' },
                                minHeight: { xs: '32px', md: '40px' },
                                maxHeight: { xs: '32px', md: '40px' },
                                margin: 0,
                                padding: 0,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                borderRadius: '50%',
                                flex: 'none',
                                aspectRatio: '1',
                                boxSizing: 'border-box',
                                flexShrink: 0,
                                flexGrow: 0,
                            },
                            
                            // Estilos para estados de días
                            '& .MuiPickersDay-root.Mui-selected': {
                                backgroundColor: 'var(--primary-red)',
                                color: 'white',
                                '&:hover, &:focus': {
                                    backgroundColor: 'var(--secondary-red)',
                                },
                            },
                            
                            '& .MuiPickersDay-root.Mui-selected:hover': {
                                backgroundColor: 'var(--secondary-dark-red)',
                            },
                            
                            '& .MuiPickersDay-root.MuiPickersDay-today': {
                                borderColor: 'var(--primary-blue)',
                                color: 'var(--primary-dark-blue)',
                                fontWeight: 'bold',
                            },
                            
                            '& .MuiPickersDay-root:hover': {
                                backgroundColor: 'var(--primary-blue)',
                                color: 'var(--background)',
                            },
                            
                            '& .MuiPickersDay-root.Mui-disabled': {
                                color: 'var(--primary-gray)',
                                backgroundColor: 'transparent',
                            },
                            
                            // Selector de año
                            '& .MuiYearCalendar-root': {
                                width: '100%',
                                maxHeight: '300px',
                                overflowY: 'auto',
                                padding: 1,
                            },
                            
                            '& .MuiPickersYear-root': {
                                flexBasis: { xs: '25%', sm: '33.33%' },
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                height: '40px',
                                margin: '4px',
                            },
                            
                            '& .MuiPickersYear-yearButton': {
                                fontSize: { xs: '0.875rem', sm: '1rem' },
                                minWidth: 'auto',
                                width: '100%',
                                height: '100%',
                            },
                            
                            // Fondo del calendario
                            backgroundColor: 'var(--background-color)',
                            borderRadius: 2,
                            boxShadow: 'none',
                            border: '1px solid var(--primary-gray)',
                            padding: 1,
                        }}
                    />
                </LocalizationProvider>
            </div>
        </div>
    )
}