import { cn } from '../../lib/utils';

export function PageHeader({ title, description, actions, className }) {
  return (
    <div
      className={cn(
        'mb-6 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between',
        className
      )}
    >
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-mentor-text">{title}</h1>
        {description && <p className="mt-1 max-w-2xl text-sm text-mentor-muted">{description}</p>}
      </div>
      {actions && <div className="flex shrink-0 flex-wrap items-center gap-2">{actions}</div>}
    </div>
  );
}
