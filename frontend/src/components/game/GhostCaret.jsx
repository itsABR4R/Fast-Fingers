import React from "react";

const GhostCaret = ({ text, progress, name }) => {
  // Calculate position based on progress index
  // Note: This is a simplified visual. In a real app, you'd calculate exact pixel coordinates.
  const isFinished = progress >= text.length;

  return (
    <div className="absolute top-0 left-0 w-full h-full pointer-events-none opacity-50 z-0">
      {/* Ghost Cursor */}
      <span 
        className="absolute border-l-2 border-gray-400 h-8 transition-all duration-100"
        style={{ 
          // This is a rough approximation. 
          // For perfect alignment, you need a monospace font width calculation.
          left: `${progress * 14.4}px`, 
          top: '4px'
        }}
      >
        <span className="absolute -top-6 -left-2 text-xs text-gray-400 bg-gray-800 px-1 rounded">
          {name}
        </span>
      </span>
    </div>
  );
};

export default GhostCaret;