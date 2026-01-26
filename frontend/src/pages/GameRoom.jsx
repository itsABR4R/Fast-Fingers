import React, { useState, useEffect } from 'react';
import MultiplayerArea from '../components/game/MultiplayerArea';
import ProgressBar from '../components/game/ProgressBar';
import useGameSocket from '../hooks/useGameSocket';
import authService from '../services/authService';

const GameRoom = () => {
  const [roomId] = useState("room_1");
  const [username, setUsername] = useState("");

  useEffect(() => {
    // Get username from auth service
    const currentUser = authService.getCurrentUser();
    if (currentUser && !currentUser.isGuest) {
      // Use actual username for logged-in users
      setUsername(currentUser.username);
    } else {
      // Generate random name for guests
      setUsername("Guest_" + Math.floor(Math.random() * 1000));
    }
  }, []);

  // Removed messages/sendChat from destructuring
  const { isConnected, players, startText, winner, sendProgress, sendFinish } = useGameSocket(roomId, username);

  return (
    <main className="flex-1 flex flex-col items-center p-6 h-[calc(100vh-80px)] overflow-hidden">

      <div className="w-full max-w-[1800px] h-full flex flex-col gap-4">

        {/* TOP SECTION: Status & Progress */}
        <div className="flex flex-col gap-2 shrink-0">
          <div className="flex items-center justify-between px-4">
            <div className={`text-[10px] font-bold flex items-center gap-2 ${isConnected ? "text-green-400" : "text-red-400"}`}>
              <div className={`w-1.5 h-1.5 rounded-full ${isConnected ? "bg-green-400 animate-pulse" : "bg-red-400"}`}></div>
              {isConnected ? "CONNECTED" : "OFFLINE"}
            </div>
            <div className="text-gray-600 text-[10px] font-mono">ROOM: {roomId}</div>
          </div>
          <ProgressBar players={players} currentPlayer={username} />
        </div>

        {/* BOTTOM SECTION: Full Width Arena */}
        <div className="flex-1 grid grid-cols-12 gap-0 min-h-0 mt-2">

          {/* CENTER: Typing Area - Now takes full 12 columns */}
          <div className="col-span-12 flex items-center justify-center relative p-8 border border-white/5 bg-black/20 rounded-2xl">
            <div className="w-full">
              <MultiplayerArea
                username={username}
                roomId={roomId}
                players={players}
                startText={startText}
                winner={winner}
                sendProgress={sendProgress}
                sendFinish={sendFinish}
              />
            </div>
          </div>

        </div>
      </div>
    </main>
  );
};

export default GameRoom;