import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import authService from "../services/authService";
import api from "../services/api";

const ProfilePage = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [gameHistory, setGameHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchUserData = async () => {
      const currentUser = authService.getCurrentUser();

      // Check if user is logged in
      if (!currentUser || currentUser.isGuest) {
        navigate("/login");
        return;
      }

      try {
        // Fetch user profile
        const profileResponse = await api.get(`/profile/${currentUser.username}`);
        if (profileResponse.data.success) {
          setUser(profileResponse.data);
        }

        // Fetch game history
        const historyResponse = await api.get(`/profile/${currentUser.username}/history`);
        if (historyResponse.data.success) {
          setGameHistory(historyResponse.data.games || []);
        }

        setLoading(false);
      } catch (err) {
        console.error("Error fetching profile:", err);
        setError("Failed to load profile data");
        setLoading(false);
      }
    };

    fetchUserData();
  }, [navigate]);

  const handleLogout = () => {
    authService.logout();
    navigate("/");
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  if (loading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="text-gray-400 text-xl">Loading profile...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="text-red-400 text-xl">{error}</div>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="flex-1 flex flex-col items-center justify-center p-8 w-full">
      <div className="w-full max-w-4xl bg-black/20 border border-white/5 rounded-2xl p-8 shadow-2xl backdrop-blur-sm">

        {/* Header */}
        <div className="flex items-center gap-6 mb-8 border-b border-white/5 pb-8">
          <div className="w-24 h-24 bg-gray-800 rounded-full flex items-center justify-center text-4xl shadow-inner text-yellow-400">
            👤
          </div>
          <div>
            <h1 className="text-3xl font-bold text-white">{user.username}</h1>
            <p className="text-gray-500 font-mono text-sm mt-1">{user.email}</p>
            <p className="text-gray-600 text-xs mt-1">Member since {formatDate(user.createdAt)}</p>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
          <div className="bg-gray-900/40 p-4 rounded-xl text-center border border-white/5">
            <div className="text-2xl font-bold text-yellow-400">{user.avgWPM?.toFixed(1) || 0}</div>
            <div className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">Avg WPM</div>
          </div>
          <div className="bg-gray-900/40 p-4 rounded-xl text-center border border-white/5">
            <div className="text-2xl font-bold text-green-400">{user.bestWPM?.toFixed(1) || 0}</div>
            <div className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">Best WPM</div>
          </div>
          <div className="bg-gray-900/40 p-4 rounded-xl text-center border border-white/5">
            <div className="text-2xl font-bold text-blue-400">{user.totalGames || 0}</div>
            <div className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">Races</div>
          </div>
          <div className="bg-gray-900/40 p-4 rounded-xl text-center border border-white/5">
            <div className="text-2xl font-bold text-red-400">{user.totalWins || 0}</div>
            <div className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">Wins</div>
          </div>
        </div>

        {/* Game History */}
        {gameHistory.length > 0 && (
          <div className="mb-8">
            <h2 className="text-xl font-bold text-white mb-4">Recent Games</h2>
            <div className="space-y-2 max-h-64 overflow-y-auto">
              {gameHistory.map((game, index) => (
                <div key={game.id || index} className="bg-gray-900/40 p-3 rounded-lg border border-white/5 flex justify-between items-center">
                  <div className="flex gap-4 items-center">
                    <div className="text-sm text-gray-400">{formatDate(game.timestamp)}</div>
                    <div className="text-xs bg-gray-800 px-2 py-1 rounded text-gray-400">{game.gameMode}</div>
                  </div>
                  <div className="flex gap-6 items-center">
                    <div className="text-sm">
                      <span className="text-yellow-400 font-bold">{game.wpm?.toFixed(1)}</span>
                      <span className="text-gray-500 text-xs ml-1">WPM</span>
                    </div>
                    <div className="text-sm">
                      <span className="text-green-400 font-bold">{game.accuracy?.toFixed(1)}%</span>
                      <span className="text-gray-500 text-xs ml-1">ACC</span>
                    </div>
                    <div className="text-sm text-gray-500">{game.wordsTyped} words</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

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
