import { cn } from '../../lib/utils';

export function Loader({ className, label = 'Loading', size = 'md' }) {
  const dim =
    size === 'sm' ? 'h-6 w-6 border-2' : size === 'lg' ? 'h-14 w-14 border-4' : 'h-10 w-10 border-[3px]';
  return (
    <div className={cn('flex flex-col items-center justify-center gap-3', className)} role="status" aria-label={label}>
      <div
        className={cn(
          'animate-spin rounded-full border-mentor-primary border-t-transparent',
          dim
        )}
      />
      <span className="sr-only">{label}</span>
    </div>
  );
}
