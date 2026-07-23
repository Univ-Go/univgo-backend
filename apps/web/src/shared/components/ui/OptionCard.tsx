"use client";
import { Button } from "@/src/shared/components/ui/Button";
import Image from "next/image";
import React, { useState, useRef, useEffect } from "react";

interface OptionCardProps {
  title: string;
  description: string;
  imageSrc: string;
  buttonText: string;
  buttonPosition: "left" | "right";
  buttonOnClick?: () => void;
  buttonhref?: string;
}

const OptionCard: React.FC<OptionCardProps> = ({
  title,
  description,
  imageSrc,
  buttonText,
  buttonPosition,
  buttonOnClick,
  buttonhref,
}) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [showReadMore, setShowReadMore] = useState(false);
  const [collapsedHeight, setCollapsedHeight] = useState(0);
  const textRef = useRef<HTMLParagraphElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const isRight = buttonPosition === "right";

  useEffect(() => {
    const checkTextOverflow = () => {
      if (textRef.current) {
        const lineHeight = parseFloat(getComputedStyle(textRef.current).lineHeight);
        const fullHeight = textRef.current.scrollHeight;
        const threeLineHeight = lineHeight * 3;
        
        // Establecer la altura de 3 líneas para el estado colapsado
        setCollapsedHeight(threeLineHeight);
        
        // Si el contenido es más alto que 3 líneas, mostrar el botón
        setShowReadMore(fullHeight > threeLineHeight);
      }
    };

    checkTextOverflow();
    window.addEventListener('resize', checkTextOverflow);
    
    return () => window.removeEventListener('resize', checkTextOverflow);
  }, [description]);

  // Forzar re-render del contenedor cuando cambie el estado de expansión
  useEffect(() => {
    if (containerRef.current && textRef.current) {
      // Trigger reflow para asegurar que la animación funcione
      containerRef.current.style.height = containerRef.current.style.height;
    }
  }, [isExpanded]);

  const toggleExpanded = () => {
    setIsExpanded(!isExpanded);
  };
  return (
    <div className="p-2 sm:p-4 lg:p-6">
      <div className="relative w-full overflow-hidden rounded-lg shadow-lg group p-4 sm:p-6 min-h-[250px] sm:min-h-[300px] lg:min-h-[350px] flex items-stretch">
        <div className="absolute inset-0 z-0 transition-transform duration-300 group-hover:scale-105">
          <Image
            src={imageSrc}
            alt={title}
            fill
            style={{ objectFit: "cover" }}
            priority
          />
          <div className="absolute inset-0 bg-black/50 transition-trasform duration-300 group-hover:bg-black/40" />
        </div>
        <div
          className={`relative z-10 flex ${isRight ? "flex-col lg:flex-row" : "flex-col lg:flex-row-reverse"} items-stretch w-full gap-4`}
        >
          <div className="flex flex-col justify-center flex-1">
            <div
              className={`
              bg-black/60 rounded-lg p-3 sm:p-4 lg:p-6 backdrop-blur-sm h-fit inline-block w-full lg:max-w-[80%]
              ${!isRight ? "text-left lg:self-end" : ""}
              `}
              style={{ minWidth: "200px" }}
            >
              <h2 className="text-lg sm:text-xl lg:text-2xl font-bold text-white mb-2 break-words">
                {title}
              </h2>
              <div className="relative">
                <div 
                  ref={containerRef}
                  className="transition-all duration-500 ease-in-out overflow-hidden"
                  style={{
                    height: isExpanded 
                      ? textRef.current?.scrollHeight || 'auto'
                      : showReadMore 
                        ? `${collapsedHeight}px` 
                        : 'auto'
                  }}
                >
                  <p 
                    ref={textRef}
                    className="text-sm sm:text-base text-white/90 break-words"
                  >
                    {description}
                  </p>
                </div>
                {showReadMore && (
                  <div className="flex justify-center mt-3">
                    <button
                      onClick={toggleExpanded}
                      className="flex items-center gap-1 text-xs sm:text-sm text-white hover:text-white/80 transition-all duration-300 ease-in-out underline focus:outline-none focus:ring-2 focus:ring-white/50 focus:ring-opacity-50 rounded px-2 py-1 hover:bg-white/10 backdrop-blur-sm"
                    >
                      <span>{isExpanded ? 'Mostrar menos' : 'Mostrar más'}</span>
                      <svg 
                        className={`w-3 h-3 sm:w-4 sm:h-4 transition-transform duration-300 ease-in-out ${
                          isExpanded ? 'rotate-180' : 'rotate-0'
                        }`} 
                        fill="none" 
                        stroke="currentColor" 
                        viewBox="0 0 24 24"
                      >
                        <path 
                          strokeLinecap="round" 
                          strokeLinejoin="round" 
                          strokeWidth={2} 
                          d="M19 9l-7 7-7-7" 
                        />
                      </svg>
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
          <div className="flex items-center justify-center px-2 sm:px-4">
            <Button
              text={buttonText}
              onClick={buttonOnClick}
              href={buttonhref}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default OptionCard;
