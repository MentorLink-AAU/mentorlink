import { forwardRef, useId } from 'react';
import { cn } from '../../lib/utils';

export const Textarea = forwardRef(function Textarea(
  { className, label, id, error, rows = 4, ...props },
  ref
) {
  const autoId = useId();
  const tid = id || autoId;
  return (
    <div className="w-full">
      {label && (
        <label htmlFor={tid} className="mb-1.5 block text-sm font-medium text-mentor-text">
          {label}
        </label>
      )}
      <textarea
        ref={ref}
        id={tid}
        rows={rows}
        className={cn(
          'w-full rounded-lg border border-mentor-border bg-mentor-card px-3 py-2.5 text-sm text-mentor-text placeholder:text-mentor-muted',
          'focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20',
          error && 'border-mentor-danger',
          className
        )}
        {...props}
      />
      {error && <p className="mt-1 text-xs text-mentor-danger">{error}</p>}
    </div>
  );
});
