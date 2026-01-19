import React from 'react';
import { clsx } from 'clsx';

const Button = ({ children, onClick, variant = 'primary', className }) => {
  const styles = clsx(
    "px-6 py-2 rounded font-bold transition duration-200",
    {
      "bg-yellow-400 text-gray-900 hover:bg-yellow-500": variant === 'primary',
      "bg-gray-700 text-white hover:bg-gray-600": variant === 'secondary',
      "bg-red-500 text-white hover:bg-red-600": variant === 'danger',
    },
    className
  );

  return (
    <button onClick={onClick} className={styles}>
      {children}
    </button>
  );
};

export default Button;