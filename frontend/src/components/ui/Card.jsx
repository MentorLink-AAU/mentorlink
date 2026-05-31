import { cn } from '../../lib/utils';

const variants = {
  default: 'rounded-xl border border-mentor-border bg-mentor-card text-mentor-text shadow-sm',
  glass:
    'rounded-xl border border-white/30 bg-white/50 text-mentor-text shadow-[0_8px_40px_rgba(15,23,42,0.08)] backdrop-blur-xl',
};

export function Card({ className, variant = 'default', ...props }) {
  return (
    <div
      className={cn(variants[variant] || variants.default, className)}
      {...props}
    />
  );
}

export function CardHeader({ className, ...props }) {
  return <div className={cn('border-b border-mentor-border px-5 py-4', className)} {...props} />;
}

export function CardTitle({ className, ...props }) {
  return <h3 className={cn('text-base font-semibold text-mentor-text', className)} {...props} />;
}

export function CardContent({ className, ...props }) {
  return <div className={cn('px-5 py-4', className)} {...props} />;
}

export function CardFooter({ className, ...props }) {
  return <div className={cn('border-t border-mentor-border px-5 py-4', className)} {...props} />;
}
