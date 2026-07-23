"use client";
import React from 'react';
import { IconType } from 'react-icons';
import { useRouter } from 'next/navigation';
import styles from './ChoiceModal.module.css';

interface OptionProps {
    label: string;
    onClick?: () => void; // Hacemos onClick opcional
    route?: string; // Agregamos la ruta como opción
    icon: IconType;
}

interface ChoiceModalProps {
    open: boolean;
    onClose: () => void;
    option1: OptionProps;
    option2: OptionProps;
}

const ChoiceModal: React.FC<ChoiceModalProps> = ({ open, onClose, option1, option2 }) => {
    const router = useRouter();

    if (!open) return null;

    const handleOptionClick = (option: OptionProps) => {
        onClose();

        if (option.route) {
            router.push(option.route);
        }
        // Si no hay ruta pero hay un onClick, lo ejecutamos
        else if (option.onClick) {
            option.onClick();
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
            onClick={onClose}
        >
            <div
                className="bg-[var(--primary-red)] rounded-lg shadow-lg flex flex-col md:flex-row w-11/12 max-w-xl mx-auto overflow-hidden"
                onClick={(e) => e.stopPropagation()}
            >
                {[option1, option2].map((option, idx) => (
                    <button
                        key={idx}
                        className={`relative cursor-pointer flex-1 flex flex-col items-center justify-center p-8 rounded-lg focus:outline-none transition hover:scale-105 overflow-hidden ${styles.optionButton}`}
                        onClick={() => handleOptionClick(option)}
                        type="button"
                    >
                        {/* Efecto de rellenado progresivo */}
                        <div className={styles.fillOverlay}></div>

                        <div className={`text-5xl mb-4 ${styles.iconContainer}`}>
                            {React.createElement(option.icon)}
                        </div>
                        <span className={`text-lg font-semibold ${styles.optionLabel}`}>
                            {option.label}
                        </span>
                    </button>
                ))}
            </div>
        </div>
    );
};

export default ChoiceModal;