import Link from 'next/link';

interface NavButtonProps {
  text: string;
  pageReference: string;
  isActive?: boolean; // Optional prop to control underline visibility
  icon?: React.ReactNode; // Optional icon to display alongside text
}

export const NavButton = ({
  text,
  pageReference,
  isActive = false,
  icon,
}: NavButtonProps) => {
  return (
    <div className="relative inline-block group">
      <Link
        href={pageReference}
        className={`
          px-4 py-2 text-sm font-light text-white 
          flex items-center gap-2 
          transition-all duration-300 ease-out
          hover:text-white/90 hover:scale-100
          ${isActive ? 'text-white' : 'text-white/80'}
        `}
      >
        {/* Icon (if provided) */}
        {icon && <span className="transition-transform duration-300 group-hover:-translate-y-0.5">{icon}</span>}
        
        {/* Text with slight upward movement on hover */}
        <span className="relative transition-transform duration-300 group-hover:-translate-y-0.5 uppercase font-semibold">
          {text}
          
          {/* Animated underline container - always present but with varying opacity/width */}
          <span className="absolute -bottom-1 left-0 w-full h-0.5 flex justify-center">
            {/* The actual underline with gradient */}
            <span 
              className={`
                h-full bg-gradient-to-r from-white/0 via-[#7bbebe] to-white/0
                transition-all duration-350 ease-out
                ${isActive 
                  ? 'w-full opacity-100' 
                  : 'w-0 opacity-0 group-hover:w-full group-hover:opacity-100'
                }
              `}
            />
          </span>
        </span>
      </Link>
    </div>
  );
}

