/**
 * Notification context: fetches notifications, WebSocket updates, mark-as-read.
 */
import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { getNotifications, getUnreadCount, markNotificationRead } from '../lib/api';
import { useWebSocket } from '../hooks/useWebSocket';
import { useAuth } from './AuthContext';

const NotificationContext = createContext(null);

export function NotificationProvider({ children }) {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const refresh = useCallback(async () => {
    try {
      const [notifRes, countRes] = await Promise.all([
        getNotifications(50),
        getUnreadCount(),
      ]);
      const data = notifRes.data?.data;
      const count = countRes.data?.data;
      if (Array.isArray(data)) setNotifications(data);
      if (typeof count === 'number') setUnreadCount(count);
    } catch {
      void 0;
    }
  }, []);

  const addNotification = useCallback((n) => {
    setNotifications((prev) => [n, ...prev]);
    if (!n.read) setUnreadCount((c) => c + 1);
  }, []);

  useWebSocket(user?.id, addNotification);

  useEffect(() => {
    if (!user) return undefined;
    let cancelled = false;
    const id = requestAnimationFrame(() => {
      if (!cancelled) void refresh();
    });
    return () => {
      cancelled = true;
      cancelAnimationFrame(id);
    };
  }, [user, refresh]);

  const markAsRead = async (id) => {
    try {
      await markNotificationRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnreadCount((c) => Math.max(0, c - 1));
    } catch {
      void 0;
    }
  };

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        refresh,
        markAsRead,
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotificationContext() {
  const ctx = useContext(NotificationContext);
  return (
    ctx || {
      notifications: [],
      unreadCount: 0,
      refresh: async () => {},
      markAsRead: async () => {},
    }
  );
}
