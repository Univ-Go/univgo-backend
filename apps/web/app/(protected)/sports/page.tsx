"use client";

import BookingModal from "@/src/modules/reservations/components/booking-modal/BookingModal";
import { ResponsiveLayout } from "@/src/shared/components/layout/ResponsiveLayout";
import { ResponsiveGrid } from "@/src/shared/components/ui/ResponsiveGrid";
import OptionCard from "@/src/shared/components/ui/OptionCard";
import { useState } from "react";

export default function SportsPage() {
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedSpaceName, setSelectedSpaceName] = useState<string>("");

  const handleOpenModal = (spaceName: string) => {
    setSelectedSpaceName(spaceName);
    setModalOpen(true);
  };

  const handleCloseModal = () => {
    setModalOpen(false);
  };

  const sportsSpaces = [
    {
      title: "Gimnasio",
      description: "¡Actívate en nuestro gimnasio universitario! Tu lugar para desconectarte, recargar y poner tu cuerpo en movimiento. ¡Te esperamos para que te sientas genial!",
      imageSrc: "/udemgym.jpg",
      buttonPosition: "left" as const,
    },
    {
      title: "Coliseo",
      description: "Tu epicentro para arte, música, teatro y eventos inolvidables. ¡Ven a vivir la cultura universitaria!",
      imageSrc: "/Coliseo_UdeM.jpg",
      buttonPosition: "right" as const,
    },
    {
      title: "Cancha de grama sintética fútbol 11",
      description: "¡Siente la pasión del fútbol en nuestra cancha de grama sintética! Tu lugar para goles, jugadas épicas y la emoción del fútbol 11. ¡A rodar el balón!",
      imageSrc: "/cancha-fut11.jpg",
      buttonPosition: "left" as const,
    },
    {
      title: "Cancha de grama sintética fútbol 7",
      description: "El lugar perfecto para partidos rápidos, goles espectaculares y momentos inolvidables con tus amigos. ¡La cancha te espera!",
      imageSrc: "/cancha-fut7.jpg",
      buttonPosition: "right" as const,
    },
  ];

  return (
    <ResponsiveLayout>
      <div className="py-4 sm:py-6 lg:py-8">
        <ResponsiveGrid 
          cols={{ default: 1, lg: 2 }} 
          gap={{ default: 2, sm: 4, lg: 6 }}
          className="mb-4 sm:mb-6 lg:mb-8"
        >
          {sportsSpaces.map((space, index) => (
            <OptionCard
              key={index}
              title={space.title}
              description={space.description}
              buttonText="Reservar"
              buttonPosition={space.buttonPosition}
              buttonOnClick={() => handleOpenModal(space.title)}
              imageSrc={space.imageSrc}
            />
          ))}
        </ResponsiveGrid>
        
        {/* Tennis courts take full width on large screens */}
        <div className="lg:col-span-2">
          <OptionCard
            title="Canchas de tenis"
            description="Perfecciona tu saque, domina tu revés y vive la emoción de cada punto. ¡Es hora de jugar!"
            buttonText="Reservar"
            buttonPosition="left"
            buttonOnClick={() => handleOpenModal("Canchas de tenis")}
            imageSrc="/campo-tenis-generico.jpg"
          />
        </div>
      </div>

      {/* Modal de reserva */}
      <BookingModal
        isOpen={modalOpen}
        onClose={handleCloseModal}
        spaceName={selectedSpaceName}
        horaLimiteInicio={6}
        horaLimiteFin={20}
        duracionReserva={2}
        tipoHora="par"
      />
    </ResponsiveLayout>
  );
}
