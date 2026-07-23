"use client";

import React, { useState } from 'react';

interface CheckboxProps {
  id: string;
  label: string;
  checked?: boolean;
  onChange?: (checked: boolean) => void;
  className?: string;
}

const Checkbox = ({ id, label, checked = false, onChange, className = '' }: CheckboxProps) => {
  const [isChecked, setIsChecked] = useState(checked);

  const toggleCheckbox = () => {
    const newState = !isChecked;
    setIsChecked(newState);
    if (onChange) onChange(newState);
  };

  return (
    <div className={`flex items-center ${className}`}>
      <div className="relative">
        <input
          id={id}
          type="checkbox"
          checked={isChecked}
          onChange={toggleCheckbox}
          className="sr-only" // Oculta el checkbox real pero mantiene la accesibilidad
        />
        <div
          onClick={toggleCheckbox}
          className={`
            w-5 h-5
            flex items-center justify-center
            border-2 rounded 
            transition-all duration-200 ease-in-out
            cursor-pointer
            ${isChecked 
              ? 'border-[var(--primary-blue)] bg-[var(--primary-blue)]' 
              : 'border-[var(--primary-gray)] bg-transparent'}
          `}
        >
          {/* Checkmark con animación - Tamaño aumentado */}
          <svg 
            className={`
              w-4 h-4 
              text-white 
              transition-all 
              duration-200 
              ${isChecked ? 'opacity-100 scale-100' : 'opacity-0 scale-0'}
            `} 
            viewBox="0 0 16 16" 
            fill="none" 
            xmlns="http://www.w3.org/2000/svg"
          >
            <path 
              d="M4 8L7 11L12 5" 
              stroke="currentColor" 
              strokeWidth="2.5" 
              strokeLinecap="round" 
              strokeLinejoin="round" 
            />
          </svg>
        </div>
      </div>
      <label 
        htmlFor={id} 
        className="ml-2 block text-sm text-[var(--text-color)] cursor-pointer"
      >
        {label}
      </label>
    </div>
  );
};

export default Checkbox;