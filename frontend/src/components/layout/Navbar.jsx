import React from "react";
import { Link, useNavigate } from "react-router-dom";
import authService from "../../services/authService";

const Navbar = () => {
  const navigate = useNavigate();
  const isLoggedIn = authService.isAuthenticated() && !authService.isGuest();

  const handleAuthAction = () => {
    if (isLoggedIn) {
      navigate("/profile");
    } else {
      navigate("/login");
    }
  };

  return (
    <nav className="w-full py-8 px-12 flex items-center text-gray-500 font-mono select-none z-50 relative">
      {/* Logo */}
      <div className="flex-1">
        <Link to="/" className="text-3xl font-bold text-gray-200 hover:text-yellow-400 transition inline-flex items-center gap-2">
          <span className="text-yellow-400">Fast</span>Fingers
        </Link>
      </div>

      {/* Nav Links */}
      <div className="hidden md:flex items-center gap-8 mr-8 text-lg font-bold tracking-wide">
        <Link to="/" className="hover:text-gray-100 transition flex items-center gap-2">
          <span className="text-yellow-400">⌨</span> practice
        </Link>
        <Link to="/practice?mode=code" className="hover:text-gray-100 transition flex items-center gap-2">
          <span className="text-green-400">{`</>`}</span> code
        </Link>
        <div className="w-[1px] h-5 bg-gray-700 mx-2"></div>
        <Link to="/room" className="hover:text-gray-100 transition flex items-center gap-2 group">
          <span className="text-blue-400 group-hover:text-blue-300">👥</span> multiplayer
        </Link>

      </div>

      {/* User Icon */}
      <div
        onClick={handleAuthAction}
        className="cursor-pointer text-gray-400 hover:text-yellow-400 transition duration-300 p-2 rounded-full hover:bg-white/5"
        title={isLoggedIn ? "My Profile" : "Login"}
      >
        <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
      </div>
    </nav>
  );
};

export default Navbar;