import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authAPI } from '../services/api';
import { toast } from 'react-hot-toast';

const useAuthStore = create(
  persist(
    (set, get) => ({
      // State
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Actions
      login: async (credentials) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await authAPI.login(credentials);
          const { user, token } = response.data;
          
          set({
            user,
            token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          
          // Store token in localStorage
          localStorage.setItem('authToken', token);
          localStorage.setItem('user', JSON.stringify(user));
          
          toast.success('Login successful!');
          return { success: true, user };
        } catch (error) {
          const errorMessage = error.response?.data?.message || 'Login failed';
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          });
          
          toast.error(errorMessage);
          return { success: false, error: errorMessage };
        }
      },

      logout: async () => {
        set({ isLoading: true });
        
        try {
          await authAPI.logout();
        } catch (error) {
          console.error('Logout error:', error);
        } finally {
          // Clear state regardless of API call success
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });
          
          // Clear localStorage
          localStorage.removeItem('authToken');
          localStorage.removeItem('user');
          
          toast.success('Logged out successfully');
        }
      },

      register: async (userData) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await authAPI.register(userData);
          const { user, token } = response.data;
          
          set({
            user,
            token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          
          // Store token in localStorage
          localStorage.setItem('authToken', token);
          localStorage.setItem('user', JSON.stringify(user));
          
          toast.success('Registration successful!');
          return { success: true, user };
        } catch (error) {
          const errorMessage = error.response?.data?.message || 'Registration failed';
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          });
          
          toast.error(errorMessage);
          return { success: false, error: errorMessage };
        }
      },

      refreshToken: async () => {
        try {
          const response = await authAPI.refreshToken();
          const { token } = response.data;
          
          set({ token });
          localStorage.setItem('authToken', token);
          
          return { success: true, token };
        } catch (error) {
          console.error('Token refresh failed:', error);
          get().logout();
          return { success: false, error: error.message };
        }
      },

      getCurrentUser: async () => {
        set({ isLoading: true });
        
        try {
          const response = await authAPI.getCurrentUser();
          const user = response.data;
          
          set({
            user,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
          
          return { success: true, user };
        } catch (error) {
          console.error('Get current user failed:', error);
          get().logout();
          return { success: false, error: error.message };
        }
      },

      changePassword: async (passwordData) => {
        set({ isLoading: true, error: null });
        
        try {
          await authAPI.changePassword(passwordData);
          set({ isLoading: false, error: null });
          
          toast.success('Password changed successfully!');
          return { success: true };
        } catch (error) {
          const errorMessage = error.response?.data?.message || 'Password change failed';
          set({ isLoading: false, error: errorMessage });
          
          toast.error(errorMessage);
          return { success: false, error: errorMessage };
        }
      },

      forgotPassword: async (email) => {
        set({ isLoading: true, error: null });
        
        try {
          await authAPI.forgotPassword(email);
          set({ isLoading: false, error: null });
          
          toast.success('Password reset email sent!');
          return { success: true };
        } catch (error) {
          const errorMessage = error.response?.data?.message || 'Failed to send reset email';
          set({ isLoading: false, error: errorMessage });
          
          toast.error(errorMessage);
          return { success: false, error: errorMessage };
        }
      },

      resetPassword: async (token, passwordData) => {
        set({ isLoading: true, error: null });
        
        try {
          await authAPI.resetPassword(token, passwordData);
          set({ isLoading: false, error: null });
          
          toast.success('Password reset successfully!');
          return { success: true };
        } catch (error) {
          const errorMessage = error.response?.data?.message || 'Password reset failed';
          set({ isLoading: false, error: errorMessage });
          
          toast.error(errorMessage);
          return { success: false, error: errorMessage };
        }
      },

      updateProfile: async (profileData) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await authAPI.updateProfile(profileData);
          const user = response.data;
          
          set({
            user,
            isLoading: false,
            error: null,
          });
          
          // Update localStorage
          localStorage.setItem('user', JSON.stringify(user));
          
          toast.success('Profile updated successfully!');
          return { success: true, user };
        } catch (error) {
          const errorMessage = error.response?.data?.message || 'Profile update failed';
          set({ isLoading: false, error: errorMessage });
          
          toast.error(errorMessage);
          return { success: false, error: errorMessage };
        }
      },

      // Initialize auth state from localStorage
      initializeAuth: () => {
        const token = localStorage.getItem('authToken');
        const userStr = localStorage.getItem('user');
        
        if (token && userStr) {
          try {
            const user = JSON.parse(userStr);
            set({
              user,
              token,
              isAuthenticated: true,
              isLoading: false,
            });
          } catch (error) {
            console.error('Error parsing user data:', error);
            localStorage.removeItem('authToken');
            localStorage.removeItem('user');
          }
        }
      },

      // Clear error
      clearError: () => set({ error: null }),

      // Check if user has permission
      hasPermission: (permission) => {
        const { user } = get();
        if (!user || !user.permissions) return false;
        
        return user.permissions.includes(permission) || user.role === 'ADMIN';
      },

      // Check if user has role
      hasRole: (role) => {
        const { user } = get();
        if (!user) return false;
        
        return user.role === role || user.role === 'ADMIN';
      },

      // Get user permissions
      getUserPermissions: () => {
        const { user } = get();
        if (!user) return [];
        
        return user.permissions || [];
      },

      // Get user role
      getUserRole: () => {
        const { user } = get();
        if (!user) return null;
        
        return user.role;
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

export default useAuthStore;
