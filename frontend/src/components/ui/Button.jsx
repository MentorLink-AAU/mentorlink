import { forwardRef } from 'react';
import { cn } from '../../lib/utils';

const variants = {
  primary:
    'bg-mentor-primary text-white hover:bg-mentor-primary-dark shadow-sm focus-visible:ring-mentor-primary',
  secondary:
    'bg-mentor-secondary text-white hover:opacity-95 shadow-sm focus-visible:ring-mentor-secondary',
  outline:
    'border border-mentor-border bg-mentor-card text-mentor-text hover:bg-mentor-surface focus-visible:ring-mentor-primary',
  danger:
    'bg-mentor-danger text-white hover:opacity-90 shadow-sm focus-visible:ring-mentor-danger',
  ghost: 'text-mentor-text hover:bg-mentor-surface focus-visible:ring-mentor-primary',
};

const sizes = {
  sm: 'px-3 py-1.5 text-sm rounded-lg',
  md: 'px-4 py-2.5 text-sm font-medium rounded-lg',
  lg: 'px-5 py-3 text-base font-medium rounded-xl',
};

export const Button = forwardRef(function Button(
  { className, variant = 'primary', size = 'md', type = 'button', disabled, as: Comp = 'button', ...props },
  ref
) {
  const isNativeButton = Comp === 'button';
  return (
    <Comp
      ref={ref}
      type={isNativeButton ? type : undefined}
      disabled={isNativeButton ? disabled : undefined}
      aria-disabled={!isNativeButton && disabled ? true : undefined}
      className={cn(
        'inline-flex items-center justify-center gap-2 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
        !isNativeButton && disabled && 'pointer-events-none opacity-50',
        variants[variant] || variants.primary,
        sizes[size] || sizes.md,
        className
      )}
      {...props}
    />
  );
});
