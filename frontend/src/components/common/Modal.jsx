import React from 'react';

const Modal = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-70 backdrop-blur-sm">
      <div className="bg-gray-800 p-8 rounded-lg shadow-xl w-[500px] border border-gray-700">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl text-yellow-400 font-bold">{title}</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-white">✕</button>
        </div>
        <div className="text-gray-300">
          {children}
        </div>
      </div>
    </div>
  );
};

export default Modal;