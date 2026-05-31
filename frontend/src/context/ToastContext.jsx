import { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';
import { X, CheckCircle, AlertCircle, Info } from 'lucide-react';
import { cn } from '../lib/utils';

const ToastContext = createContext(null);

let idSeq = 0;

function ToastItem({ toast, onDismiss }) {
  const icons = {
    success: CheckCircle,
    error: AlertCircle,
    info: Info,
  };
  const Icon = icons[toast.type] || Info;
  const styles = {
    success: 'border-mentor-success/30 bg-mentor-card text-mentor-text',
    error: 'border-mentor-danger/30 bg-mentor-card text-mentor-text',
    info: 'border-mentor-border bg-mentor-card text-mentor-text',
  };
  return (
    <div
      role="status"
      className={cn(
        'pointer-events-auto flex items-start gap-3 rounded-lg border px-4 py-3 shadow-lg',
        styles[toast.type] || styles.info
      )}
    >
      <Icon
        className={cn(
          'mt-0.5 h-5 w-5 shrink-0',
          toast.type === 'success' && 'text-mentor-success',
          toast.type === 'error' && 'text-mentor-danger',
          toast.type === 'info' && 'text-mentor-primary'
        )}
      />
      <p className="flex-1 text-sm">{toast.message}</p>
      <button
        type="button"
        onClick={() => onDismiss(toast.id)}
        className="rounded p-1 text-mentor-muted hover:bg-mentor-surface hover:text-mentor-text"
        aria-label="Dismiss"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const timers = useRef(new Map());

  const dismiss = useCallback((id) => {
    const t = timers.current.get(id);
    if (t) clearTimeout(t);
    timers.current.delete(id);
    setToasts((prev) => prev.filter((x) => x.id !== id));
  }, []);

  const show = useCallback(
    (message, type = 'info', duration = 4000) => {
      const id = ++idSeq;
      setToasts((prev) => [...prev, { id, message, type }]);
      if (duration > 0) {
        const t = setTimeout(() => dismiss(id), duration);
        timers.current.set(id, t);
      }
      return id;
    },
    [dismiss]
  );

  const value = useMemo(
    () => ({
      toast: {
        success: (msg, d) => show(msg, 'success', d),
        error: (msg, d) => show(msg, 'error', d),
        info: (msg, d) => show(msg, 'info', d),
      },
      dismiss,
    }),
    [show, dismiss]
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        className="pointer-events-none fixed bottom-4 right-4 z-[200] flex w-full max-w-sm flex-col gap-2 p-2 sm:bottom-6 sm:right-6"
        aria-live="polite"
      >
        {toasts.map((t) => (
          <ToastItem key={t.id} toast={t} onDismiss={dismiss} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    return {
      toast: {
        success: () => {},
        error: () => {},
        info: () => {},
      },
      dismiss: () => {},
    };
  }
  return ctx;
}
