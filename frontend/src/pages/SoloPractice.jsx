import React from 'react';
import TypingArea from '../components/game/TypingArea';

const SoloPractice = () => {
  return (
    <main className="flex-1 flex flex-col items-center justify-center w-full min-h-0">
      {/* We use w-full but let TypingArea's max-w control the constraint.
        This ensures Code Mode (which needs width) gets space.
      */}
      <div className="w-full flex justify-center px-4">
        <TypingArea />
      </div>
    </main>
  );
};

export default SoloPractice;