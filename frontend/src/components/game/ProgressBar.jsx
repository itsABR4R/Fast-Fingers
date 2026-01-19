import React from 'react';
import { clsx } from 'clsx';

const ProgressBar = ({ players, currentPlayer }) => {
  const sortedPlayers = [...players].sort((a, b) => b.progress - a.progress);

  return (
    // Changed: bg-black/20 (Subtle dark fill) + rounded-xl + border
    <div className="w-full p-4 flex flex-col bg-black/20 rounded-xl border border-white/5 shadow-sm">
      
      {/* Header Labels */}
      <div className="flex justify-between text-[11px] font-bold text-gray-500 uppercase tracking-widest mb-3 px-2 border-b border-white/5 pb-2">
        <span>Race Track</span>
        <span>Finish Line</span>
      </div>

      <div className="flex-1 overflow-y-auto max-h-[220px] space-y-3 pr-2 scrollbar-thin scrollbar-thumb-gray-700">
        {sortedPlayers.map((player) => {
          const isMe = player.username === currentPlayer;
          
          return (
            <div key={player.username} className="relative w-full h-8 flex items-center group">
              
              {/* Name */}
              <div className={clsx("w-32 text-xs font-mono truncate mr-4 text-right", isMe ? "text-yellow-400 font-bold" : "text-gray-500")}>
                {player.username}
              </div>

              {/* Track */}
              <div className="flex-1 relative h-2 bg-gray-800/50 rounded-full overflow-hidden">
                <div 
                  className={clsx(
                    "absolute top-0 left-0 h-full rounded-full transition-all duration-300 ease-out",
                    isMe ? "bg-yellow-400 shadow-[0_0_15px_rgba(250,204,21,0.6)]" : "bg-blue-500/50"
                  )}
                  style={{ width: `${player.progress}%` }}
                >
                  <div className={clsx("absolute -right-1 top-0 h-full w-2 blur-[2px]", isMe ? "bg-white" : "bg-blue-300 hidden")}></div>
                </div>
              </div>

              {/* WPM */}
              <div className="w-16 text-right font-mono text-xs text-gray-600 ml-4">
                {player.wpm} <span className="text-[10px] text-gray-700">WPM</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default ProgressBar;