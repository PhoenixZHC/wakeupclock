import React from 'react';

interface Props {
  onClick?: () => void;
  children: React.ReactNode;
  active?: boolean;
  className?: string;
  variant?: 'primary' | 'danger' | 'default' | 'ghost';
}

export const NeumorphicButton: React.FC<Props> = ({ onClick, children, active = false, className = '', variant = 'default' }) => {
  
  let baseClass = "relative overflow-hidden transition-all duration-200 active:scale-95 font-medium rounded-xl shadow-sm border";
  
  if (variant === 'primary') {
    baseClass += " bg-gradient-to-r from-indigo-500 to-purple-600 text-white border-transparent shadow-glow";
  } else if (variant === 'danger') {
    baseClass += " bg-gradient-to-r from-red-500 to-orange-500 text-white border-transparent shadow-lg";
  } else if (variant === 'ghost') {
    baseClass += " bg-transparent border-transparent text-text-sub hover:bg-gray-100 shadow-none";
  } else {
    // Default
    if (active) {
      baseClass += " bg-indigo-50 border-indigo-200 text-primary-brand ring-2 ring-indigo-100";
    } else {
      baseClass += " bg-white border-gray-200 text-text-main hover:border-gray-300";
    }
  }

  return (
    <button
      onClick={onClick}
      className={`
        px-6 py-4 
        ${baseClass}
        ${className}
      `}
    >
      {children}
    </button>
  );
};