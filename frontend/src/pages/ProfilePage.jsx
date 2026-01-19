import React from "react";
import { useNavigate } from "react-router-dom";

const ProfilePage = () => {
  const navigate = useNavigate();
  // Mock data - In real app, fetch this from API using localStorage.getItem('token')
  const user = {
    username: "SpeedDemon",
    matches: 42,
    avgWpm: 85,
    topWpm: 112,
    wins: 14
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };

  return (
    <div className="flex-1 flex flex-col items-center justify-center p-8 w-full">
      <div className="w-full max-w-2xl bg-black/20 border border-white/5 rounded-2xl p-8 shadow-2xl backdrop-blur-sm">
        
        {/* Header */}
        <div className="flex items-center gap-6 mb-8 border-b border-white/5 pb-8">
          <div className="w-24 h-24 bg-gray-800 rounded-full flex items-center justify-center text-4xl shadow-inner text-yellow-400">
            👤
          </div>
          <div>
            <h1 className="text-3xl font-bold text-white">{user.username}</h1>
            <p className="text-gray-500 font-mono text-sm mt-1">Member since 2024</p>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
          <div className="bg-gray-900/40 p-4 rounded-xl text-center border border-white/5">
            <div className="text-2xl font-bold text-yellow-400">{user.avgWpm}</div>
            <div className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">Avg WPM</div>
          </div>
          <div className="bg-gray-900/40 p-4 rounded-xl text-center border border-white/5">
            <div className="text-2xl font-bold text-green-400">{user.topWpm}</div>
            <div className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">Top WPM</div>
          </div>
          <div className="bg-gray-900/40 p-4 rounded-xl text-center border border-white/5">
            <div className="text-2xl font-bold text-blue-400">{user.matches}</div>
            <div className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">Races</div>
          </div>
          <div className="bg-gray-900/40 p-4 rounded-xl text-center border border-white/5">
            <div className="text-2xl font-bold text-red-400">{user.wins}</div>
            <div className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">Wins</div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex justify-end pt-4 border-t border-white/5">
          <button 
            onClick={handleLogout}
            className="px-6 py-2 bg-red-500/10 text-red-500 border border-red-500/50 hover:bg-red-500 hover:text-white rounded-lg transition text-sm font-bold uppercase tracking-wide"
          >
            Logout
          </button>
        </div>

      </div>
    </div>
  );
};

export default ProfilePage;