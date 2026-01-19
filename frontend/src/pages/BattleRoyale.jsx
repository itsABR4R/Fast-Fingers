import React, { useState, useEffect } from 'react';
import ProgressBar from '../components/game/ProgressBar';
import TypingArea from '../components/game/TypingArea';
import useGameSocket from '../hooks/useGameSocket';

const BattleRoyale = () => {
  const [isDead, setIsDead] = useState(false);
  // Removed chat objects
  const { players, isConnected } = useGameSocket("battle_royale_1", "Player_" + Math.floor(Math.random()*100));

  useEffect(() => {
    const myStatus = players.find(p => p.username.startsWith("Player_"))?.status;
    if (myStatus === 'ELIMINATED') setIsDead(true);
  }, [players]);

  const aliveCount = players.filter(p => p.status !== 'ELIMINATED').length;

  return (
    <main className="flex-1 flex flex-col items-center p-6 h-[calc(100vh-80px)] overflow-hidden bg-[#323437] relative">
      
      {/* Background Ambience */}
      <div className="absolute inset-0 bg-radial-gradient from-red-900/10 to-transparent pointer-events-none"></div>

      <div className="w-full max-w-[1800px] h-full flex flex-col gap-4 relative z-10">
        
        {/* TOP SECTION: Stats */}
        <div className="flex flex-col gap-2 shrink-0">
          <div className="flex items-center justify-between px-4">
            <div className={`text-[10px] font-bold flex items-center gap-2 ${isConnected ? "text-red-400" : "text-gray-500"}`}>
              <div className={`w-1.5 h-1.5 rounded-full ${isConnected ? "bg-red-500 animate-pulse" : "bg-gray-500"}`}></div>
              {isConnected ? "SUDDEN DEATH LIVE" : "CONNECTING..."}
            </div>
            <div className="text-red-500 font-bold font-mono text-sm tracking-widest">
              SURVIVORS: <span className="text-white text-lg">{aliveCount}</span> / {players.length || 20}
            </div>
          </div>
          
          <ProgressBar players={players} currentPlayer="Me" />
        </div>

        {/* BOTTOM SECTION: Full Width Arena */}
        <div className="flex-1 grid grid-cols-12 gap-0 min-h-0 mt-2">
          
          {/* CENTER: Typing Arena - Full Width, Red Borders */}
          <div className="col-span-12 flex items-center justify-center relative p-8 border border-red-500/20 bg-black/20 rounded-2xl">
            
            <div className={`w-full transition-all duration-1000 ${isDead ? "opacity-10 blur-sm grayscale pointer-events-none" : ""}`}>
              <TypingArea /> 
            </div>

            {/* WASTED OVERLAY */}
            {isDead && (
              <div className="absolute inset-0 z-50 flex flex-col items-center justify-center">
                <h1 className="text-9xl font-black text-red-600 tracking-tighter transform -rotate-6 drop-shadow-[0_0_15px_rgba(220,38,38,0.8)] animate-fade-in-up">
                  WASTED
                </h1>
                <div className="mt-8 flex gap-4 animate-fade-in">
                  <button onClick={() => window.location.reload()} className="px-6 py-2 bg-red-600 text-black font-bold uppercase hover:bg-white transition text-xs tracking-widest">
                    Retry
                  </button>
                  <button onClick={() => window.location.href='/'} className="px-6 py-2 border border-gray-600 text-gray-400 font-bold uppercase hover:text-white transition text-xs tracking-widest">
                    Exit
                  </button>
                </div>
              </div>
            )}
          </div>

        </div>
      </div>
    </main>
  );
};

export default BattleRoyale;