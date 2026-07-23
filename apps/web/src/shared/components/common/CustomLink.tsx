import React from "react";

interface CustomLinkProps {
  href: string;
  children: React.ReactNode;
  className?: string;
  target?: string;
}

const CustomLink: React.FC<CustomLinkProps> = ({ href, children, className = "", target="_blank"}) => {
  return (
    <a
      href={href}
      className={`relative text-white underline hover:no-underline group ${className}`}
      target={target}
    >
      {children}
      <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-white group-hover:w-full transition-all duration-300 ease-in-out"></span>
    </a>
  );
};

export default CustomLink;
