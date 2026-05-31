import { cn } from '../../lib/utils';

const variants = {
  default: 'bg-mentor-surface text-mentor-text border border-mentor-border',
  primary: 'bg-mentor-primary/10 text-mentor-primary-dark border border-mentor-primary/20',
  success: 'bg-mentor-success/10 text-mentor-success border border-mentor-success/20',
  warning: 'bg-mentor-warning/10 text-mentor-warning border border-mentor-warning/25',
  danger: 'bg-mentor-danger/10 text-mentor-danger border border-mentor-danger/20',
};

export function Badge({ className, variant = 'default', ...props }) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium',
        variants[variant] || variants.default,
        className
      )}
      {...props}
    />
  );
}
