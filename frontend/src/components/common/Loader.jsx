import React from 'react';

const Loader = () => (
  <div className="flex justify-center items-center space-x-2 animate-pulse">
    <div className="w-3 h-3 bg-yellow-400 rounded-full"></div>
    <div className="w-3 h-3 bg-yellow-400 rounded-full animation-delay-200"></div>
    <div className="w-3 h-3 bg-yellow-400 rounded-full animation-delay-400"></div>
  </div>
);

export default Loader;