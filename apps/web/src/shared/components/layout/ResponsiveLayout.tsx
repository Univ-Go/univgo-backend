"use client";

import { ReactNode } from "react";
import { Navbar } from "./navbar/Navbar";
import Footer from "./footer/Footer";

interface ResponsiveLayoutProps {
  children: ReactNode;
  showNavbar?: boolean;
  showFooter?: boolean;
  className?: string;
}

export const ResponsiveLayout = ({ 
  children, 
  showNavbar = true, 
  showFooter = true,
  className = ""
}: ResponsiveLayoutProps) => {
  return (
    <div className="min-h-screen flex flex-col">
      {showNavbar && <Navbar />}
      
      <main className={`flex-1 ${className}`}>
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          {children}
        </div>
      </main>
      
      {showFooter && <Footer />}
    </div>
  );
};
