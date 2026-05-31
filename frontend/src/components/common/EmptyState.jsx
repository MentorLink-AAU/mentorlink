import { cn } from '../../lib/utils';

export function EmptyState({ icon: Icon, title, description, action, className }) {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center rounded-xl border border-dashed border-mentor-border bg-mentor-surface/50 px-6 py-12 text-center',
        className
      )}
    >
      {Icon && <Icon className="mb-4 h-10 w-10 text-mentor-muted" strokeWidth={1.25} />}
      <h3 className="text-base font-semibold text-mentor-text">{title}</h3>
      {description && <p className="mt-2 max-w-sm text-sm text-mentor-muted">{description}</p>}
      {action && <div className="mt-6">{action}</div>}
    </div>
  );
}
