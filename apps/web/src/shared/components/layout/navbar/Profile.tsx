"use client";

import React, { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { FaUser } from "react-icons/fa6"; // Importing the user icon

interface DropdownItem {
  id: string;
  label: string;
  href?: string;
  onClick?: () => void;
}

interface ProfileProps {
  // Marcamos como opcionales las props que no estamos usando para evitar errores
  imageUrl?: string; // Opcional ya que no la estamos usando directamente
  altText?: string; // Opcional ya que no la estamos usando directamente
  dropdownItems: DropdownItem[];
  userName?: string; // Optional: display user name next to picture
}

export const Profile = ({
  // No necesitamos destructurar lo que no usamos
  // imageUrl,
  // altText,
  dropdownItems,
  userName,
}: ProfileProps) => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // Use a default image if none is provided

  return (
    <div className="relative inline-block text-left" ref={dropdownRef}>
      <div>
        <button
          type="button"
          className="flex items-center rounded-full bg-white/10 hover:bg-white/20 p-1
                     text-sm focus:outline-none transition-all duration-300 ease-in-out"
          id="user-menu-button"
          aria-expanded={isDropdownOpen}
          aria-haspopup="true"
          onClick={() => setIsDropdownOpen(!isDropdownOpen)}
        >
          <span className="sr-only">Open user menu</span>
          <div className="h-8 w-8 rounded-full overflow-hidden relative flex items-center justify-center transition-all duration-300 ease-in-out
                    hover:bg-[var(--primary-blue)] focus-outline-none group
                    cursor-pointer">
            <FaUser
              className="text-white transition-colors duration-300"
              size={16}
            />
          </div>
          {userName && (
            <span className="ml-2 text-white hidden sm:block">{userName}</span>
          )}
        </button>
      </div>

      {/* Dropdown menu with transition */}
      {isDropdownOpen && (
        <div
          className="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg 
                     bg-white ring-1 ring-black ring-opacity-5 focus:outline-none z-50
                     transform opacity-100 scale-100 transition ease-out duration-200"
          role="menu"
          aria-orientation="vertical"
          aria-labelledby="user-menu-button"
        >
          <div className="py-1" role="none">
            {dropdownItems.map((item) =>
              item.href ? (
                <Link
                  key={item.id}
                  href={item.href}
                  className="text-gray-700 block px-4 py-2 text-sm hover:bg-gray-100"
                  role="menuitem"
                  onClick={() => setIsDropdownOpen(false)}
                >
                  {item.label}
                </Link>
              ) : (
                <button
                  key={item.id}
                  className="text-gray-700 block w-full text-left px-4 py-2 text-sm hover:bg-gray-100"
                  role="menuitem"
                  onClick={() => {
                    if (item.onClick) {
                      item.onClick();
                    }
                    setIsDropdownOpen(false);
                  }}
                >
                  {item.label}
                </button>
              )
            )}
          </div>
        </div>
      )}
    </div>
  );
};
