import api from './api';

/**
 * Authentication service for user management.
 * Handles signup, login, logout, and user session management.
 */

const authService = {
    /**
     * Register a new user account.
     */
    async signup(username, email, password) {
        try {
            const response = await api.post('/auth/signup', {
                username,
                email,
                password
            });

            if (response.data.success) {
                // Store user data in localStorage
                localStorage.setItem('user', JSON.stringify(response.data.user));
                return { success: true, user: response.data.user };
            }

            return { success: false, message: response.data.message };
        } catch (error) {
            const message = error.response?.data?.message || 'Error creating account';
            return { success: false, message };
        }
    },

    /**
     * Login with username and password.
     */
    async login(username, password) {
        try {
            const response = await api.post('/auth/login', {
                username,
                password
            });

            if (response.data.success) {
                // Store user data in localStorage
                localStorage.setItem('user', JSON.stringify(response.data.user));
                return { success: true, user: response.data.user };
            }

            return { success: false, message: response.data.message };
        } catch (error) {
            const message = error.response?.data?.message || 'Error during login';
            return { success: false, message };
        }
    },

    /**
     * Logout current user.
     */
    logout() {
        localStorage.removeItem('user');
    },

    /**
     * Get current logged-in user.
     */
    getCurrentUser() {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch (e) {
                return null;
            }
        }
        return null;
    },

    /**
     * Check if user is authenticated.
     */
    isAuthenticated() {
        return this.getCurrentUser() !== null;
    },

    /**
     * Check if user is in guest mode.
     */
    isGuest() {
        return !this.isAuthenticated();
    },

    /**
     * Set guest mode (for playing without account).
     */
    setGuestMode() {
        localStorage.setItem('user', JSON.stringify({ username: 'Guest', isGuest: true }));
    }
};

export default authService;
