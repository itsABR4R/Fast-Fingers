import React from "react";
import { useNavigate } from "react-router-dom";
import TypingArea from "../components/game/TypingArea";

const CompactCard = ({ title, icon, color, isLocked, onClick }) => (
  <div
    onClick={onClick}
    className={`
      relative group cursor-pointer 
      bg-[#2c2e31] hover:bg-[#323437] 
      border border-transparent hover:border-${color}-500/50 
      rounded-xl p-4 transition-all duration-300 ease-out
      flex flex-col items-center justify-center gap-2
      w-full h-24 overflow-hidden
    `}
  >
    <div className={`text-2xl text-${color}-400 group-hover:scale-110 transition duration-300`}>
      {icon}
    </div>
    <div className="text-xs font-bold text-gray-400 group-hover:text-gray-200 uppercase tracking-widest">
      {title}
    </div>
    {isLocked && (
      <div className="absolute inset-0 bg-black/60 backdrop-blur-[1px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
        <span className="text-xs font-mono text-gray-300 bg-black/80 px-2 py-1 rounded">Login Required</span>
      </div>
    )}
  </div>
);

const HomePage = () => {
  const navigate = useNavigate();
  const isLoggedIn = !!localStorage.getItem("token");

  const handleNavigation = (path, requireAuth) => {
    if (requireAuth && !isLoggedIn) navigate("/login");
    else navigate(path);
  };

  return (
    <main className="flex-1 flex flex-col items-center justify-center relative pb-12 w-full">
      {/* Typing Game */}
      <div className="w-full mb-4">
        <TypingArea />
      </div>
    </main>
  );
};

export default HomePage;