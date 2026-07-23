import React from "react";

interface InputFormProps {
  label: string;
  placeholder?: string; // Ahora es opcional
  typeInput: string;
  id: string;
  value?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const InputForm = ({ label, placeholder, typeInput, id , value, onChange}: InputFormProps) => {
  return (
    <div className="relative mb-5">
      <label
        htmlFor={id}
        className="
          block 
          mb-2 
          text-sm 
          font-medium 
          text-[var(--text-color)]
        "
      >
        {label}
      </label>
      <input
        type={typeInput}
        placeholder={placeholder || ""}
        id={id}
        value={value}
        onChange={onChange}
        className="
          w-full
          h-12
          px-4
          py-2
          text-[var(--text-color)]
          bg-[var(--background-color)]
          border
          border-[var(--primary-gray)]
          rounded-lg
          transition-all
          duration-200
          focus:outline-none
          focus:ring-1
          focus:ring-[var(--primary-blue)]
          focus:border-[var(--primary-blue)]
          shadow-sm
          hover:shadow-md
        "
      />
    </div>
  );
};

export default InputForm;
