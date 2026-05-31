import { forwardRef, useId } from 'react';
import { cn } from '../../lib/utils';

export const Select = forwardRef(function Select(
  { className, label, id, error, children, ...props },
  ref
) {
  const autoId = useId();
  const sid = id || autoId;
  return (
    <div className="w-full">
      {label && (
        <label htmlFor={sid} className="mb-1.5 block text-sm font-medium text-mentor-text">
          {label}
        </label>
      )}
      <select
        ref={ref}
        id={sid}
        className={cn(
          'w-full rounded-lg border border-mentor-border bg-mentor-card px-3 py-2.5 text-sm text-mentor-text',
          'focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20',
          error && 'border-mentor-danger',
          className
        )}
        {...props}
      >
        {children}
      </select>
      {error && <p className="mt-1 text-xs text-mentor-danger">{error}</p>}
    </div>
  );
});
