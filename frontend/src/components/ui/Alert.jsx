import { cn } from '../../lib/utils';

const variants = {
  info: 'border-mentor-border bg-mentor-surface text-mentor-text',
  success: 'border-mentor-success/30 bg-mentor-success/5 text-mentor-text',
  warning: 'border-mentor-warning/30 bg-mentor-warning/5 text-mentor-text',
  error: 'border-mentor-danger/30 bg-mentor-danger/5 text-mentor-text',
};

const bodyTone = {
  info: 'text-mentor-muted',
  success: 'text-mentor-text',
  warning: 'text-mentor-text',
  error: 'text-mentor-danger',
};

export function Alert({ className, variant = 'info', title, children, ...props }) {
  return (
    <div
      role="alert"
      className={cn('rounded-lg border px-4 py-3 text-sm', variants[variant] || variants.info, className)}
      {...props}
    >
      {title && <p className="font-semibold text-mentor-text">{title}</p>}
      <div className={cn(title && 'mt-1', bodyTone[variant] || bodyTone.info)}>{children}</div>
    </div>
  );
}
