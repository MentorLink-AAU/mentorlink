/**
 * Auth context: user state, login/logout, role helpers (isStudent, isFaculty, isAdmin).
 */
import { createContext, useContext, useState, useEffect } from 'react';
import { getMe, login as apiLogin } from '../lib/api';

const AuthContext = createContext(null);

/** Provides auth state and methods to the app. */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(() =>
    typeof window !== 'undefined' ? !!localStorage.getItem('mentorlink_token') : true
  );

  // Restore session from token on mount
  useEffect(() => {
    const token = localStorage.getItem('mentorlink_token');
    if (!token) {
      return;
    }
    getMe()
      .then((res) => {
        const data = res.data?.data;
        if (data) {
          setUser(data);
          localStorage.setItem('mentorlink_user', JSON.stringify(data));
        }
      })
      .catch(() => {
        localStorage.removeItem('mentorlink_token');
        localStorage.removeItem('mentorlink_user');
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (email, password, role = null) => {
    const res = await apiLogin(email, password, role);
    const data = res.data?.data;
    const token = typeof data === 'string' ? data : data?.token;
    if (!token) throw new Error('Login failed');
    localStorage.setItem('mentorlink_token', token);
    const meRes = await getMe();
    const u = meRes.data?.data;
    setUser(u);
    localStorage.setItem('mentorlink_user', JSON.stringify(u));
    return u;
  };

  const logout = () => {
    localStorage.removeItem('mentorlink_token');
    localStorage.removeItem('mentorlink_user');
    setUser(null);
  };

  const refreshUser = async () => {
    const res = await getMe();
    const u = res.data?.data;
    if (u) {
      setUser(u);
      localStorage.setItem('mentorlink_user', JSON.stringify(u));
    }
    return u;
  };

  const isStudent = user?.role === 'ROLE_STUDENT' || user?.role === 'STUDENT';
  const isFaculty = user?.role === 'ROLE_FACULTY' || user?.role === 'FACULTY';
  const isAdmin = user?.role === 'ROLE_ADMIN' || user?.role === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        logout,
        refreshUser,
        isStudent,
        isFaculty,
        isAdmin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

/** Hook to access auth context. Must be used within AuthProvider. */
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
