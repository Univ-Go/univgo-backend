import { useAuth } from "@/src/modules/auth/hooks/useAuth";
import { useCallback, useEffect, useState } from "react";
import { toast } from "react-hot-toast";
import { getUserReservations } from "../services/reservation.service";

export interface UserReservation {
  id: number;
  qrCodeData: string;
  userId: number;
  spaceId: number;
  reservationDate: string;
  startTime: string;
  endTime: string;
  status:
    | "pending"
    | "confirmed"
    | "cancelled_by_admin"
    | "cancelled_by_user"
    | "completed";
  createdAt: string;
  updatedAt: string;
  user: {
    id: number;
    identification: string;
    name: string;
    email: string;
  };
  space: {
    id: number;
    name: string;
    capacity: number;
  };
  guests: {
    id: number;
    guestIdentification: string;
    guestName: string;
    reservationId: number;
  }[];
}

export const useUserReservations = () => {
  const [reservations, setReservations] = useState<UserReservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { user } = useAuth();

  const fetchReservations = useCallback(async () => {
    if (!user?.identification) {
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await getUserReservations(user.identification);
      setReservations(data);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "Error al obtener las reservas";
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [user?.identification]);

  useEffect(() => {
    fetchReservations();
  }, [fetchReservations]);

  return {
    reservations,
    loading,
    error,
    refetch: fetchReservations,
  };
};
