"use client";
import { Button } from "@/src/shared/components/ui/Button";
import { ConfirmationModal } from "@/src/shared/components/common/ConfirmationModal";
import { useState } from "react";
import {
  FaCalendarAlt,
  FaClock,
  FaMapMarkerAlt,
  FaQrcode,
  FaUsers,
} from "react-icons/fa";
import QRCode from "react-qr-code";

export interface ReservationGuest {
  id: number;
  guestIdentification: string;
  guestName?: string;
  reservationId?: number;
  reservation?: {
    id: number;
  };
}

export interface Space {
  id: number;
  name: string;
  capacity: number;
}

export interface User {
  id: number;
  identification: string;
  name: string;
}

interface ReservationProps {
  id: number;
  qrCodeData: string;
  space?: Space; // Opcional para compatibilidad
  spaceId?: number; // Opcional para compatibilidad con estructura anterior
  userId?: number; // Opcional para compatibilidad con estructura anterior
  reservationDate: string;
  startTime: string;
  endTime: string;
  status:
    | "confirmed"
    | "pending"
    | "cancelled_by_admin"
    | "cancelled_by_user"
    | "completed";
  guests: ReservationGuest[];
  onCancel?: (id: number) => void;
  createdAt?: string; // Opcional
  updatedAt?: string; // Opcional
}

export default function Reservation({
  id,
  qrCodeData,
  space,
  spaceId,
  reservationDate,
  startTime,
  endTime,
  status,
  guests,
  onCancel,
}: ReservationProps) {
  const [showQR, setShowQR] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);

  // Mapeo temporal para compatibilidad con estructura anterior
  const SPACE_NAMES: Record<number, string> = {
    1: "Gimnasio",
    2: "Cancha de grama sintética fútbol 11",
    3: "Cancha de baloncesto",
    4: "Coliseo",
    5: "Piscina Olímpica",
    6: "Cancha de tenis",
    7: "Sala de ping pong",
    8: "Cancha múltiple",
  };

  // Determinar el nombre del espacio
  const spaceName =
    space?.name ||
    (spaceId
      ? SPACE_NAMES[spaceId] || `Espacio #${spaceId}`
      : "Espacio desconocido");

  // Formateo de fecha
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("es-ES", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const formatTime = (timeString: string) => {
    if (timeString.match(/^\d{2}:\d{2}$/)) {
      return timeString;
    }

    if (timeString.match(/^\d{2}:\d{2}:\d{2}$/)) {
      return timeString.substring(0, 5); // "14:30:00" -> "14:30"
    }

    return timeString;
  };

  // Determinar color según el estado
  const getStatusColor = () => {
    switch (status) {
      case "confirmed":
        return "bg-green-100 text-green-800 border-green-200";
      case "pending":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "cancelled_by_admin":
      case "cancelled_by_user":
        return "bg-red-100 text-red-800 border-red-200";
      case "completed":
        return "bg-blue-100 text-blue-800 border-blue-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  // Texto del estado en español
  const getStatusText = () => {
    switch (status) {
      case "confirmed":
        return "Confirmada";
      case "pending":
        return "Pendiente";
      case "cancelled_by_admin":
        return "Cancelada por admin";
      case "cancelled_by_user":
        return "Cancelada";
      case "completed":
        return "Completada";
      default:
        return status;
    }
  };

  const handleCancelClick = () => {
    setShowCancelModal(true);
  };

  const handleConfirmCancel = () => {
    if (onCancel) {
      onCancel(id);
    }
    setShowCancelModal(false);
  };

  const handleCloseCancelModal = () => {
    setShowCancelModal(false);
  };

  // Verificar si el QR debería estar disponible
  const canShowQR = status === "confirmed" || status === "pending";

  return (
    <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden mb-4">
      {/* Encabezado con estado */}
      <div className="p-4 flex justify-between items-center border-b border-gray-200">
        <div className="flex items-center">
          <span
            className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor()}`}
          >
            {getStatusText()}
          </span>
          <span className="ml-3 text-sm text-gray-500">Reserva #{id}</span>
        </div>
        <div>
          {canShowQR && (
            <button
              onClick={() => setShowQR(!showQR)}
              className="text-sm text-[var(--primary-blue)] hover:underline flex items-center"
            >
              <FaQrcode className="mr-1" />
              {showQR ? "Ocultar QR" : "Mostrar QR"}
            </button>
          )}
        </div>
      </div>

      <div className="p-4">
        {/* Información principal */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            {/* Espacio */}
            <div className="flex items-start mb-3">
              <FaMapMarkerAlt className="text-[var(--primary-red)] mt-1 mr-2" />
              <div>
                <span className="block text-sm font-medium">Espacio</span>
                <span className="text-lg">{spaceName}</span>
              </div>
            </div>

            {/* Fecha */}
            <div className="flex items-start mb-3">
              <FaCalendarAlt className="text-[var(--primary-red)] mt-1 mr-2" />
              <div>
                <span className="block text-sm font-medium">Fecha</span>
                <span>{formatDate(reservationDate)}</span>
              </div>
            </div>

            {/* Horario */}
            <div className="flex items-start mb-3">
              <FaClock className="text-[var(--primary-red)] mt-1 mr-2" />
              <div>
                <span className="block text-sm font-medium">Horario</span>
                <span>
                  {formatTime(startTime)} - {formatTime(endTime)}
                </span>
              </div>
            </div>
          </div>

          <div>
            {/* Invitados */}
            <div className="flex items-start mb-3">
              <FaUsers className="text-[var(--primary-red)] mt-1 mr-2" />
              <div>
                <span className="block text-sm font-medium">Invitados</span>
                <span>
                  {guests.length} {guests.length === 1 ? "persona" : "personas"}
                </span>
                {guests.length > 0 && (
                  <ul className="mt-1 text-sm text-gray-600">
                    {guests.map((guest) => (
                      <li key={guest.id}>
                        • ID: {guest.guestIdentification || guest.id}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Código QR */}
        {showQR && canShowQR && (
          <div className="mt-3 p-4 border-t border-gray-200">
            <div className="flex flex-col items-center">
              <div className="text-xs text-gray-500 mb-2">
                Código QR para verificación
              </div>
              <div className="border-4 border-[var(--primary-blue)] p-3 rounded-lg bg-white">
                {/* QR Code generado dinámicamente */}
                <QRCode
                  value={qrCodeData}
                  size={128}
                  style={{ height: "auto", maxWidth: "100%", width: "100%" }}
                  viewBox={`0 0 128 128`}
                />
              </div>
              <div className="mt-2 text-xs text-gray-500">ID: {qrCodeData}</div>
            </div>
          </div>
        )}

        {/* Acciones */}
        {(status === "pending" || status === "confirmed") && (
          <div className="mt-4 flex justify-end">
            <Button text="Cancelar reserva" onClick={handleCancelClick} />
          </div>
        )}
      </div>

      <ConfirmationModal
        isOpen={showCancelModal}
        onClose={handleCloseCancelModal}
        onConfirm={handleConfirmCancel}
        title="Confirmar cancelación"
        message={"¿Estás seguro de que deseas cancelar esta reserva? "}
        confirmText="Si"
        cancelText="No"
        type="danger"
      />
    </div>
  );
}
