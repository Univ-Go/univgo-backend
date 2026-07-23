"use client";

import { ReactNode } from "react";

interface ResponsiveGridProps {
  children: ReactNode;
  cols?: {
    default: number;
    sm?: number;
    md?: number;
    lg?: number;
    xl?: number;
  };
  gap?: {
    default: number;
    sm?: number;
    md?: number;
    lg?: number;
  };
  className?: string;
}

export const ResponsiveGrid = ({ 
  children, 
  cols = { default: 1, lg: 2 },
  gap = { default: 2, sm: 4, lg: 6 },
  className = ""
}: ResponsiveGridProps) => {
  const getGridCols = () => {
    const classes = [`grid-cols-${cols.default}`];
    
    if (cols.sm) classes.push(`sm:grid-cols-${cols.sm}`);
    if (cols.md) classes.push(`md:grid-cols-${cols.md}`);
    if (cols.lg) classes.push(`lg:grid-cols-${cols.lg}`);
    if (cols.xl) classes.push(`xl:grid-cols-${cols.xl}`);
    
    return classes.join(' ');
  };

  const getGap = () => {
    const classes = [`gap-${gap.default}`];
    
    if (gap.sm) classes.push(`sm:gap-${gap.sm}`);
    if (gap.md) classes.push(`md:gap-${gap.md}`);
    if (gap.lg) classes.push(`lg:gap-${gap.lg}`);
    
    return classes.join(' ');
  };

  return (
    <div className={`grid ${getGridCols()} ${getGap()} ${className}`}>
      {children}
    </div>
  );
};
