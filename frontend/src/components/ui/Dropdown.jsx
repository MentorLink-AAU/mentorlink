import { useEffect, useRef, useState } from 'react';
import { cn } from '../../lib/utils';

/**
 * Minimal dropdown: trigger + panel; closes on outside click and Escape.
 */
export function Dropdown({ trigger, children, align = 'right', className }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;
    const onDoc = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    const onKey = (e) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('mousedown', onDoc);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onDoc);
      document.removeEventListener('keydown', onKey);
    };
  }, [open]);

  return (
    <div ref={ref} className={cn('relative', className)}>
      <div onClick={() => setOpen((o) => !o)}>{trigger}</div>
      {open && (
        <div
          className={cn(
            'absolute z-50 mt-2 min-w-[12rem] rounded-xl border border-mentor-border bg-mentor-card py-1 shadow-xl backdrop-blur-md',
            align === 'right' && 'right-0',
            align === 'left' && 'left-0'
          )}
        >
          {typeof children === 'function' ? children(() => setOpen(false)) : children}
        </div>
      )}
    </div>
  );
}

export function DropdownItem({ className, onClick, children, ...props }) {
  return (
    <button
      type="button"
      className={cn(
        'flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-mentor-text hover:bg-mentor-surface',
        className
      )}
      onClick={onClick}
      {...props}
    >
      {children}
    </button>
  );
}
