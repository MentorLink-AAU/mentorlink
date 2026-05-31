/** STOMP WebSocket hook: subscribes to /topic/notifications/{userId} and forwards messages. */
import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_BASE = import.meta.env.VITE_WS_URL || '';

export function useWebSocket(userId, onNotification) {
  const clientRef = useRef(null);
  const onNotificationRef = useRef(onNotification);

  useEffect(() => {
    onNotificationRef.current = onNotification;
  }, [onNotification]);

  const disconnect = useCallback(() => {
    if (clientRef.current?.connected) {
      clientRef.current.deactivate();
      clientRef.current = null;
    }
  }, []);

  useEffect(() => {
    if (!userId) return;
    const url = WS_BASE ? `${WS_BASE}/ws` : (typeof window !== 'undefined' ? `${window.location.origin}/ws` : '');
    const client = new Client({
      webSocketFactory: () => new SockJS(url),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        client.subscribe(`/topic/notifications/${userId}`, (msg) => {
          try {
            const body = JSON.parse(msg.body);
            onNotificationRef.current?.(body);
          } catch {
            void 0;
          }
        });
      },
    });
    client.activate();
    clientRef.current = client;
    return disconnect;
  }, [userId, disconnect]);

  return { disconnect };
}
