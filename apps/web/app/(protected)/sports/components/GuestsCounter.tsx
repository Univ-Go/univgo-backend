import React from 'react';
import { FiMinusCircle, FiPlusCircle } from "react-icons/fi";
import { motion, AnimatePresence } from 'framer-motion';

interface GuestsCounterProps {
  value: number;
  onChange: (count: number) => void;
  maxValue?: number;
}

function GuestsCounter({ value, onChange, maxValue = 10 }: GuestsCounterProps) {
  const increment = () => {
    onChange(Math.min(value + 1, maxValue));
  };

  const decrement = () => {
    onChange(Math.max(value - 1, 0));
  };
  
  return (
    <div className="items-center bg-[var(--background-color)] p-3 rounded-lg shadow-sm border border-[var(--primary-gray)] inline-flex w-auto">      
      <button 
        onClick={decrement} 
        className={`text-xl hover:scale-110 transition-transform ${
          value === 0 
            ? 'text-[var(--secondary-light-red)] opacity-50 cursor-not-allowed' 
            : 'text-[var(--text)] hover:text-[var(--primary-red)] cursor-pointer'
        }`}
        disabled={value === 0}
      >
        <FiMinusCircle />
      </button>
      
      <div className="w-auto min-w-[2rem] h-8 relative mx-2 flex items-center justify-center">
        <AnimatePresence mode="wait">
          <motion.span
            key={value}
            className="absolute text-xl font-medium text-[var(--text)]"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
          >
            {value}
          </motion.span>
        </AnimatePresence>
      </div>      
      
      <button 
        onClick={increment} 
        className={`text-xl hover:scale-110 transition-transform ${
          value === maxValue
            ? 'text-[var(--secondary-light-red)] opacity-50 cursor-not-allowed' 
            : 'text-[var(--text)] hover:text-[var(--primary-red)] cursor-pointer'
        }`}
        disabled={value === maxValue}
      >
        <FiPlusCircle />
      </button>
    </div>
  );
}

export default GuestsCounter;