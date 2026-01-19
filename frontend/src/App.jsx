import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Navbar from "./components/layout/Navbar";

// Pages
import HomePage from "./pages/HomePage";
import GameRoom from "./pages/GameRoom";
import BattleRoyale from "./pages/BattleRoyale";
import SoloPractice from "./pages/SoloPractice";
import Login from "./pages/Login";
import ProfilePage from "./pages/ProfilePage"; // <--- IMPORT THIS

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen flex flex-col bg-[#323437] text-white font-mono overflow-x-hidden">
        
        <Navbar />

        <div className="flex-1 flex flex-col relative">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/practice" element={<SoloPractice />} />
            <Route path="/room" element={<GameRoom />} />
            <Route path="/battle-royale" element={<BattleRoyale />} />
            <Route path="/login" element={<Login />} />
            <Route path="/profile" element={<ProfilePage />} /> {/* <--- ADD THIS */}
          </Routes>
        </div>

      </div>
    </BrowserRouter>
  );
}

export default App;