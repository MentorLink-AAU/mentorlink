import { useEffect } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from './Button';

export function Modal({ open, onClose, title, children, className, footer }) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e) => {
      if (e.key === 'Escape') onClose?.();
    };
    document.addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prev;
    };
  }, [open, onClose]);

  if (!open) return null;

  return createPortal(
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <button
        type="button"
        aria-label="Close dialog"
        className="absolute inset-0 bg-mentor-text/40 backdrop-blur-sm"
        onClick={onClose}
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? 'modal-title' : undefined}
        className={cn(
          'relative z-[101] w-full max-w-lg rounded-xl border border-mentor-border bg-mentor-card shadow-xl',
          className
        )}
      >
        <div className="flex items-start justify-between gap-4 border-b border-mentor-border px-5 py-4">
          {title ? (
            <h2 id="modal-title" className="flex-1 text-lg font-semibold text-mentor-text pr-2">
              {title}
            </h2>
          ) : (
            <span className="flex-1" />
          )}
          <Button type="button" variant="ghost" size="sm" className="-m-1 shrink-0 p-1" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>
        <div className="max-h-[min(70vh,32rem)] overflow-y-auto px-5 py-4">{children}</div>
        {footer && <div className="border-t border-mentor-border px-5 py-4">{footer}</div>}
      </div>
    </div>,
    document.body
  );
}
