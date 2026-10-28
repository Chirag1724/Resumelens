import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  login as apiLogin,
  register as apiRegister,
  setAuthToken,
  setOnUnauthorized,
} from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null);
  const [userEmail, setUserEmail] = useState(null);
  const [userName, setUserName] = useState(null);
  const navigate = useNavigate();

  const clearSession = useCallback(() => {
    setToken(null);
    setUserEmail(null);
    setUserName(null);
    setAuthToken(null);
  }, []);

  useEffect(() => {
    setOnUnauthorized(() => {
      clearSession();
      navigate('/login');
    });

    return () => {
      setOnUnauthorized(null);
    };
  }, [clearSession, navigate]);

  const login = async (credentials) => {
    const data = await apiLogin(credentials);
    if (data?.token) {
      setToken(data.token);
      setUserEmail(data.email);
      setUserName(data.name || null);
      setAuthToken(data.token);
    }
    return data;
  };

  const register = async (userData) => {
    const data = await apiRegister(userData);
    if (data?.token) {
      setToken(data.token);
      setUserEmail(data.email);
      setUserName(data.name || null);
      setAuthToken(data.token);
    }
    return data;
  };

  const logout = () => {
    clearSession();
    navigate('/login');
  };

  const value = {
    token,
    userEmail,
    userName,
    isAuthenticated: Boolean(token),
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;
