import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/common/Button';
import authService from '../services/authService';
import { clsx } from 'clsx';

const Login = () => {
  const [isSignup, setIsSignup] = useState(false);
  const [formData, setFormData] = useState({ username: "", password: "", email: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      if (isSignup) {
        // Signup
        if (!formData.username || !formData.email || !formData.password) {
          setError("All fields are required");
          setLoading(false);
          return;
        }

        const result = await authService.signup(formData.username, formData.email, formData.password);

        if (result.success) {
          console.log("Signup successful:", result.user);
          navigate('/');
        } else {
          setError(result.message);
        }
      } else {
        // Login
        if (!formData.username || !formData.password) {
          setError("Username and password are required");
          setLoading(false);
          return;
        }

        const result = await authService.login(formData.username, formData.password);

        if (result.success) {
          console.log("Login successful:", result.user);
          navigate('/');
        } else {
          setError(result.message);
        }
      }
    } catch (err) {
      setError("An unexpected error occurred");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError(""); // Clear error when user types
  };

  const handleGuestMode = () => {
    authService.setGuestMode();
    navigate('/');
  };

  return (
    <div className="flex-1 flex flex-col relative overflow-hidden w-full">
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-yellow-400/10 rounded-full blur-[100px] pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-blue-500/10 rounded-full blur-[100px] pointer-events-none"></div>

      <div className="flex-1 flex items-center justify-center z-10 px-4">
        <div className="w-full max-w-md bg-gray-900/60 backdrop-blur-xl border border-white/10 p-10 rounded-2xl shadow-2xl relative overflow-hidden">
          <div className="flex mb-8 bg-black/20 p-1 rounded-lg">
            <button onClick={() => { setIsSignup(false); setError(""); }} className={clsx("flex-1 py-2 text-sm font-bold rounded-md transition-all duration-300", !isSignup ? "bg-gray-700 text-yellow-400 shadow-lg" : "text-gray-500 hover:text-gray-300")}>Login</button>
            <button onClick={() => { setIsSignup(true); setError(""); }} className={clsx("flex-1 py-2 text-sm font-bold rounded-md transition-all duration-300", isSignup ? "bg-gray-700 text-blue-400 shadow-lg" : "text-gray-500 hover:text-gray-300")}>Sign Up</button>
          </div>

          <h2 className="text-3xl font-bold text-white mb-2 text-center">{isSignup ? "Create Account" : "Welcome Back"}</h2>

          {error && (
            <div className="bg-red-500/10 border border-red-500/50 text-red-400 px-4 py-2 rounded-lg mb-4 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5 mt-8">
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-1 ml-1">Username</label>
              <input
                name="username"
                type="text"
                value={formData.username}
                onChange={handleChange}
                className="w-full bg-black/30 border border-gray-700 rounded-lg p-3 text-white focus:border-yellow-400 focus:outline-none transition"
                placeholder="Username"
                disabled={loading}
              />
            </div>
            {isSignup && (
              <div className="animate-fade-in-down">
                <label className="block text-xs font-bold text-gray-500 uppercase mb-1 ml-1">Email</label>
                <input
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full bg-black/30 border border-gray-700 rounded-lg p-3 text-white focus:border-blue-400 focus:outline-none transition"
                  placeholder="Email"
                  disabled={loading}
                />
              </div>
            )}
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-1 ml-1">Password</label>
              <input
                name="password"
                type="password"
                value={formData.password}
                onChange={handleChange}
                className="w-full bg-black/30 border border-gray-700 rounded-lg p-3 text-white focus:border-yellow-400 focus:outline-none transition"
                placeholder="••••••••"
                disabled={loading}
              />
            </div>
            <Button
              className={clsx("w-full py-3 mt-4 text-gray-900 font-bold shadow-lg", isSignup ? "bg-blue-500 hover:bg-blue-400" : "bg-yellow-400 hover:bg-yellow-300")}
              disabled={loading}
            >
              {loading ? "Please wait..." : (isSignup ? "Register" : "Login")}
            </Button>
          </form>

          {/* Guest Mode Button */}
          <div className="mt-6 pt-6 border-t border-gray-700">
            <button
              onClick={handleGuestMode}
              className="w-full py-3 bg-gray-800 hover:bg-gray-700 text-gray-300 font-bold rounded-lg transition text-sm uppercase tracking-wide"
              disabled={loading}
            >
              Continue as Guest
            </button>
            <p className="text-xs text-gray-500 text-center mt-2">Play without an account (stats won't be saved)</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
