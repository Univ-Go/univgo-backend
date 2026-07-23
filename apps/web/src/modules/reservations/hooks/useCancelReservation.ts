import { useState } from "react";
import toast from "react-hot-toast";
import { cancelReservation } from "../services/reservation.service";

export const useCancelReservation = () => {
  const [isLoading, setIsLoading] = useState(false);

  const handleCancelReservation = async (reservationId: number) => {
    setIsLoading(true);

    try {
      await cancelReservation(reservationId);
      toast.success("Reserva cancelada exitosamente");
      return true;
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Error al cancelar la reserva";
      toast.error(errorMessage);
      return false;
    } finally {
      setIsLoading(false);
    }
  };
  return {
    cancelReservation: handleCancelReservation,
    isLoading,
  };
};
