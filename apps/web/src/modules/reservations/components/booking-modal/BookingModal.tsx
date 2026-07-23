"use client";

import { useAuth } from '@/src/modules/auth/hooks/useAuth';
import { Button } from '@/src/shared/components/ui/Button';
import dayjs from 'dayjs';
import 'dayjs/locale/es';
import { AnimatePresence, motion } from 'framer-motion';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { useCreateReservation } from '../../hooks/useCreateReservation';
import { BookingConfirmation } from './components/BookingConfirmation';
import { DateSelector } from './components/DateSelector';
import { GuestIdentificationForm } from './components/GuestIdentificationForm';
import { GuestSelector } from './components/GuestSelector';
import { TimeSelector } from './components/TimeSelector';

interface BookingModalProps {
  isOpen: boolean;
  onClose: () => void;
  spaceName?: string;
  maxGuests?: number;
  horaLimiteInicio: number; // Ej: 6
  horaLimiteFin: number;   // Ej: 20
  duracionReserva: number; // Ej: 2 (horas)
  tipoHora: 'par' | 'impar' | 'any';
}

const BookingModal = ({ isOpen, onClose, spaceName = "espacio", maxGuests = 10, horaLimiteInicio, horaLimiteFin, duracionReserva, tipoHora }: BookingModalProps) => {
  const [selectedDate, setSelectedDate] = useState<dayjs.Dayjs | null>(null);
  const [guestCount, setGuestCount] = useState<number>(0);
  const [startTime, setStartTime] = useState<dayjs.Dayjs | null>(null);
  // endTime se calcula automáticamente
  const endTime = startTime ? startTime.add(duracionReserva, 'hour') : null;
  const [guestIdentifications, setGuestIdentifications] = useState<string[]>(Array(maxGuests).fill(''));
  const { createReservation, isLoading } = useCreateReservation();
  const { user } = useAuth();

  const handleBooking = async () => {
    // Validar reserva con 24 horas de anticipación
    if (selectedDate && startTime) {
      const now = dayjs();
      const reservaDateTime = selectedDate.hour(startTime.hour()).minute(0).second(0);
      if (reservaDateTime.diff(now, 'hour') < 24) {
        toast.error('Las reservas deben hacerse con al menos 24 horas de anticipación.', {
          duration: 4000,
          position: 'top-center',
        });
        return;
      }
    }
    const reservationData = {
      identification: user?.identification || "",
      spaceName,
      reservationDate: selectedDate?.format('YYYY-MM-DD') || "",
      startTime: startTime?.format('HH:mm:ss') || "",
      endTime: endTime?.format('HH:mm:ss') || "",
      guestsIdentifications: guestIdentifications.slice(0, guestCount).filter(id => id.trim() !== "")
    };

    const validationData = {
      selectedDate,
      startTime,
      endTime,
      user
    };

    const result = await createReservation(reservationData, validationData);

    if (result) {
      onClose();
    }
  };

  dayjs.locale('es');

  // Solo permite seleccionar horas válidas
  const getValidHours = () => {
    const hours: number[] = [];
    for (let h = horaLimiteInicio; h <= horaLimiteFin - duracionReserva; h++) {
      if (tipoHora === 'par' && h % 2 !== 0) continue;
      if (tipoHora === 'impar' && h % 2 === 0) continue;
      hours.push(h);
    }
    return hours;
  };

  const handleStartTimeChange = (newValue: dayjs.Dayjs | null) => {
    setStartTime(newValue);
  };

  const handleGuestIdentificationChange = (identifications: string[]) => {
    setGuestIdentifications(identifications);
  }

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          {/* Overlay con animación de fade */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 bg-black/30"
            onClick={onClose}
            aria-hidden="true"
          />

          <motion.div
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.95, opacity: 0 }}
            transition={{
              type: "spring",
              stiffness: 300,
              damping: 30
            }}
            className="z-50 w-full max-w-xs sm:max-w-lg md:max-w-2xl lg:max-w-4xl max-h-[90vh] mx-4 sm:mx-0 rounded-lg bg-white p-4 sm:p-6 shadow-xl overflow-hidden flex flex-col"
          >
            {/* Encabezado del modal - siempre visible */}
            <div className="mb-4 flex justify-between items-center shrink-0">
              <h2 className="text-lg sm:text-xl lg:text-2xl font-bold text-gray-800">Reserva tu espacio</h2>
              <button
                onClick={onClose}
                className="text-gray-500 hover:text-gray-700 p-1"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 sm:h-6 sm:w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {/* Contenedor con scroll para el contenido principal */}

            <div className="overflow-y-auto flex-grow">
              {/* Layout responsive */}
              <div className="flex flex-col lg:flex-row gap-4 sm:gap-6 overflow-hidden">
                {/* Columna izquierda: Calendario */}
                <div className="flex-1 min-w-0 max-w-full">
                  <DateSelector
                    selectedDate={selectedDate}
                    onDateChange={setSelectedDate}
                     validHours={getValidHours()}
                  />
                </div>

                {/* Columna derecha: Invitados y Hora */}
                <div className="flex-1 min-w-0 flex flex-col justify-start space-y-4">
                  <GuestSelector
                    guestCount={guestCount}
                    maxGuests={maxGuests}
                    onGuestCountChange={setGuestCount}
                  />

                  {/* Selector solo para hora de entrada */}
                  <TimeSelector
                    startTime={startTime}
                    onStartTimeChange={handleStartTimeChange}
                    validHours={getValidHours()}
                    selectedDate={selectedDate}
                  />
                </div>
              </div>

              <BookingConfirmation
                spaceName={spaceName}
                selectedDate={selectedDate}
                guestCount={guestCount}
                startTime={startTime}
                endTime={endTime}
              />

              <GuestIdentificationForm
                guestCount={guestCount}
                guestIdentifications={guestIdentifications}
                onGuestIdentificationChange={handleGuestIdentificationChange}
              />
            </div>

            {/* Actions */}
            <div className='flex flex-col sm:flex-row justify-center gap-2 sm:gap-3 mt-4 shrink-0'>
              <Button text='Cancelar' onClick={onClose}></Button>
              <Button
                text={isLoading ? 'Reservando...' : 'Reservar'}
                onClick={handleBooking}
                disabled={isLoading}
              />
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}

export default BookingModal;