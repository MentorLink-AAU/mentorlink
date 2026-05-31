import { forwardRef, useId } from 'react';
import { cn } from '../../lib/utils';

export const Input = forwardRef(function Input(
  { className, label, id, error, hint, ...props },
  ref
) {
  const autoId = useId();
  const inputId = id || autoId;
  return (
    <div className="w-full">
      {label && (
        <label htmlFor={inputId} className="mb-1.5 block text-sm font-medium text-mentor-text">
          {label}
        </label>
      )}
      <input
        ref={ref}
        id={inputId}
        className={cn(
          'w-full rounded-lg border border-mentor-border bg-mentor-card px-3 py-2.5 text-sm text-mentor-text placeholder:text-mentor-muted',
          'focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20',
          error && 'border-mentor-danger focus:border-mentor-danger focus:ring-mentor-danger/20',
          className
        )}
        {...props}
      />
      {hint && !error && <p className="mt-1 text-xs text-mentor-muted">{hint}</p>}
      {error && <p className="mt-1 text-xs text-mentor-danger">{error}</p>}
    </div>
  );
});
