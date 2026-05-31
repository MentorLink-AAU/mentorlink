/** Hook for notifications: fetch, refresh, mark-as-read, add (for WS). */
import { useState, useEffect, useCallback } from 'react';
import { getNotifications, getUnreadCount, markNotificationRead } from '../lib/api';

export function useNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      const [notifRes, countRes] = await Promise.all([
        getNotifications(50),
        getUnreadCount(),
      ]);
      const data = notifRes.data?.data;
      const count = countRes.data?.data;
      if (Array.isArray(data)) setNotifications(data);
      if (typeof count === 'number') setUnreadCount(count);
    } catch {
      // ignore - user may not be logged in
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

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

  const addNotification = useCallback((n) => {
    setNotifications((prev) => [n, ...prev]);
    if (!n.read) setUnreadCount((c) => c + 1);
  }, []);

  return {
    notifications,
    unreadCount,
    loading,
    refresh,
    markAsRead,
    addNotification,
  };
}
