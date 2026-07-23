"use client";
import Reservation from "@/src/modules/reservations/components/my-reservations/Reservation";
import { useUserReservations } from "@/src/modules/reservations/hooks/useUserReservations";
import { useCancelReservation } from "@/src/modules/reservations/hooks/useCancelReservation";
import { ResponsiveLayout } from "@/src/shared/components/layout/ResponsiveLayout";
import { Button } from "@/src/shared/components/ui/Button";
import { useState } from "react";

export default function ReservationsPage() {
  const { reservations, loading, error, refetch } = useUserReservations();
  const { cancelReservation } = useCancelReservation();
  const [filter, setFilter] = useState<string>("all");

  // Función para cancelar reserva
  const handleCancelReservation = async (id: number) => {
    const success = await cancelReservation(id);
    if (success) {
      refetch(); // Refrescar la lista de reservas después de cancelar
    }
  };

  // Filtrar reservaciones según el estado seleccionado
  const filteredReservations = reservations.filter((reservation) => {
    if (filter === "all") return true;
    return reservation.status === filter;
  });

  const filterOptions = [
    { value: "all", label: "Todas" },
    { value: "pending", label: "Pendientes" },
    { value: "confirmed", label: "Confirmadas" },
    { value: "completed", label: "Completadas" },
    { value: "cancelled_by_user", label: "Canceladas" },
  ];

  return (
    <ResponsiveLayout>
      <div className="py-6 sm:py-8">
        <div className="mb-6 sm:mb-8">
          <h1 className="text-2xl sm:text-3xl lg:text-4xl font-bold mb-2">Mis Reservas</h1>
          <p className="text-sm sm:text-base text-gray-600">
            Aquí puedes ver y gestionar tus reservas de espacios universitarios.
          </p>
        </div>

        {/* Filtros */}
        <div className="mb-6 sm:mb-8">
          <span className="block mb-3 text-xs sm:text-sm font-medium text-gray-700">Filtrar por:</span>
          <div className="flex flex-wrap gap-2">
            {filterOptions.map((option) => (
              <button
                key={option.value}
                onClick={() => setFilter(option.value)}
                className={`px-3 sm:px-4 py-2 text-xs sm:text-sm rounded-full transition-all duration-200 ${
                  filter === option.value
                    ? "bg-primary-blue text-primary-dark-blue shadow-md font-medium"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200 hover:shadow-sm"
                }`}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        {/* Estado de carga */}
        {loading ? (
          <div className="flex justify-center items-center py-8 sm:py-12">
            <div className="animate-spin rounded-full h-8 w-8 sm:h-12 sm:w-12 border-t-2 border-b-2 border-primary-red"></div>
          </div>
        ) : error ? (
          // Mensaje de error
          <div className="bg-red-50 rounded-lg border border-red-200 p-6 sm:p-8 text-center animate-fade-in">
            <h3 className="text-lg sm:text-xl font-medium text-red-800 mb-2">
              Error al cargar las reservas
            </h3>
            <p className="text-sm sm:text-base text-red-600 mb-4 sm:mb-6">{error}</p>
            <Button text="Intentar de nuevo" onClick={refetch} />
          </div>
        ) : filteredReservations.length > 0 ? (
          // Lista de reservas
          <div className="space-y-4 sm:space-y-6 animate-fade-in">
            {filteredReservations.map((reservation) => (
              <Reservation
                key={reservation.id}
                id={reservation.id}
                qrCodeData={reservation.qrCodeData}
                space={reservation.space}
                reservationDate={reservation.reservationDate}
                startTime={reservation.startTime}
                endTime={reservation.endTime}
                status={reservation.status}
                guests={reservation.guests}
                onCancel={handleCancelReservation}
              />
            ))}
          </div>
        ) : (
          // Mensaje cuando no hay reservas
          <div className="bg-white rounded-lg border border-gray-200 p-6 sm:p-8 text-center shadow-sm animate-fade-in">
            <h3 className="text-lg sm:text-xl font-medium mb-2">
              No tienes reservas {filter !== "all" && "con este estado"}
            </h3>
            <p className="text-sm sm:text-base text-gray-600 mb-4 sm:mb-6">
              {filter !== "all"
                ? "Prueba seleccionando otro filtro o revisa más tarde."
                : "¿Por qué no reservas un espacio para tus actividades?"}
            </p>
            {filter === "all" && (
              <Button text="Reservar espacio" href="/sports" />
            )}
          </div>
        )}
      </div>
    </ResponsiveLayout>
  );
}
