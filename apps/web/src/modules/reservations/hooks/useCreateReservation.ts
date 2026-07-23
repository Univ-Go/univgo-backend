"use client";

import dayjs from "dayjs";
import { useState } from "react";
import toast from "react-hot-toast";
import {
  createReservation,
  ReservationData,
} from "../services/reservation.service";

export const useCreateReservation = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCreateReservation = async (
    reservationData: ReservationData,
    validationData: {
      selectedDate: dayjs.Dayjs | null;
      startTime: dayjs.Dayjs | null;
      endTime: dayjs.Dayjs | null;
      user: { identification?: string } | null; // Replace with the actual user type if available
    }
  ) => {
    setIsLoading(true);
    setError(null);

    if (
      !validationData.selectedDate ||
      !validationData.startTime ||
      !validationData.endTime
    ) {
      toast.error("Por favor, completa la fecha y las horas");
      setIsLoading(false);
      return null;
    }

    if (validationData.endTime.isBefore(validationData.startTime)) {
      toast.error(
        "La hora de salida no puede ser anterior a la hora de entrada."
      );
      setIsLoading(false);
      return null;
    }

    if (!validationData.user || !validationData.user.identification) {
      toast.error("Debes estar autenticado para realizar una reserva.");
      setIsLoading(false);
      return null;
    }

    try {
      const result = await createReservation(reservationData);
      toast.success("Reserva creada exitosamente");
      return result;
    } catch (err: unknown) {
      let message = "Ocurrió un error al crear la reserva.";
      if (err instanceof Error) {
        const backendMessage = err.message.split(
          "Error creating reservation: "
        )[1];
        message = backendMessage || err.message;
      }
      setError(message);
      toast.error(message);
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  return {
    createReservation: handleCreateReservation,
    isLoading,
    error,
  };
};
