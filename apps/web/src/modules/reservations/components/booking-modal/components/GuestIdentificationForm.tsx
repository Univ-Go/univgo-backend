"use client";

interface GuestIdentificationFormProps {
    guestCount: number;
    guestIdentifications: string[];
    onGuestIdentificationChange: (identifications: string[]) => void;
}

export const GuestIdentificationForm = ({
    guestCount,
    guestIdentifications,
    onGuestIdentificationChange
}: GuestIdentificationFormProps) => {

    if (guestCount === 0) return null;

    return (
        <div className="mt-4 border border-[var(--primary-gray)] rounded-lg p-4 bg-[var(--background-color)]">
            <h3 className="text-lg font-medium text-gray-700 mb-2">Información de invitados</h3>
            <p className="text-sm text-gray-500 mb-3">
                Ingresa las cédulas de los invitados adicionales
            </p>

            <div className="max-h-[200px] overflow-y-auto pr-2">
                {/* Campos dinámicos para invitados adicionales */}
                {Array.from({ length: guestCount }).map((_, index) => (
                    <div key={index} className="mb-3 flex items-center">
                        <div className="flex-grow">
                            <p className="text-sm font-medium text-gray-600">
                                Invitado {index + 1}
                            </p>
                            <input
                                type="text"
                                placeholder={`Cédula del invitado ${index + 1}`}
                                className="w-full p-2 border border-[var(--primary-gray)] rounded-md focus:outline-none focus:ring-1 focus:ring-[var(--primary-red)]"
                                onChange={(e) => {
                                    const newGuests = [...guestIdentifications];
                                    newGuests[index] = e.target.value;
                                    onGuestIdentificationChange(newGuests);
                                }}
                                value={guestIdentifications[index] || ''}
                            />
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};