import React, { createContext, useContext, useEffect, useState } from 'react';
import { User, AuthResponse } from '../types/auth';
import { authService } from '../services/authService';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isRider: boolean;
  isDriver: boolean;
  isAdmin: boolean;
  login: (credentials: { email: string; password: string }) => Promise<void>;
  register: (data: { fullName: string; email: string; phoneNumber: string; password: string }) => Promise<void>;
  registerDriver: (data: any) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const saveAuthSession = (authData: AuthResponse) => {
    localStorage.setItem('accessToken', authData.accessToken);
    localStorage.setItem('refreshToken', authData.refreshToken);
    localStorage.setItem('user', JSON.stringify(authData.user));
    setUser(authData.user);
  };

  const refreshUser = async () => {
    try {
      const currentUser = await authService.getCurrentUser();
      setUser(currentUser);
      localStorage.setItem('user', JSON.stringify(currentUser));
    } catch {
      setUser(null);
      localStorage.removeItem('user');
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      refreshUser().finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = async (credentials: { email: string; password: string }) => {
    const authData = await authService.login(credentials);
    saveAuthSession(authData);
  };

  const register = async (data: { fullName: string; email: string; phoneNumber: string; password: string }) => {
    const authData = await authService.register(data);
    saveAuthSession(authData);
  };

  const registerDriver = async (data: any) => {
    const authData = await authService.registerDriver(data);
    saveAuthSession(authData);
  };

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  const isRider = user?.role === 'RIDER';
  const isDriver = user?.role === 'DRIVER';
  const isAdmin = user?.role === 'ADMIN';
  const isAuthenticated = !!user;

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isLoading,
        isRider,
        isDriver,
        isAdmin,
        login,
        register,
        registerDriver,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
