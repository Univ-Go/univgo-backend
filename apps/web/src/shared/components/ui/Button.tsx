type ButtonProps = {
  text: string;
  onClick?: () => void;
  href?: string;
  disabled?: boolean;
  loading?: boolean;
};

export const Button = ({ text, onClick, href, disabled = false, loading = false }: ButtonProps) => {
  const isDisabled = disabled || loading;
  
  const buttonClasses = `
    text-white 
    ${isDisabled 
      ? 'bg-gray-400 cursor-not-allowed' 
      : 'bg-[#F12229] hover:bg-[#D32229] cursor-pointer hover:scale-103'
    }
    font-medium 
    rounded-xl 
    text-sm sm:text-md lg:text-lg
    px-6 sm:px-12 lg:px-16 
    py-2 sm:py-2.5 lg:py-3
    text-center 
    me-2 
    mb-2 
    transition-all
    duration-200 
    ease-in-out 
    w-full sm:w-auto
    inline-block
    ${isDisabled ? '' : 'hover:scale-103'}
  `;

  const content = (
    <div className="flex items-center justify-center">
      {loading && (
        <svg 
          className="animate-spin mr-2 h-4 w-4 text-white" 
          xmlns="http://www.w3.org/2000/svg" 
          fill="none" 
          viewBox="0 0 24 24"
        >
          <circle 
            className="opacity-25" 
            cx="12" 
            cy="12" 
            r="10" 
            stroke="currentColor" 
            strokeWidth="4"
          ></circle>
          <path 
            className="opacity-75" 
            fill="currentColor" 
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
      )}
      <span>{loading ? 'Cargando...' : text}</span>
    </div>
  );

  if (href && !isDisabled) {
    return (
      <a href={href} className={buttonClasses}>
        {content}
      </a>
    );
  }
  
  return (
    <button 
      onClick={isDisabled ? undefined : onClick} 
      className={buttonClasses}
      disabled={isDisabled}
    >
      {content}
    </button>
  );
};
