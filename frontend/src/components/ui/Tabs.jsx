import { useState } from 'react';
import { cn } from '../../lib/utils';

/**
 * Simple tabs: tabs = [{ id, label, content }]
 */
export function Tabs({ tabs, defaultId, className }) {
  const first = tabs[0]?.id;
  const [active, setActive] = useState(defaultId || first);

  return (
    <div className={cn('w-full', className)}>
      <div
        role="tablist"
        className="mb-4 flex flex-wrap gap-1 rounded-lg border border-mentor-border bg-mentor-surface/60 p-1"
      >
        {tabs.map((t) => (
          <button
            key={t.id}
            type="button"
            role="tab"
            aria-selected={active === t.id}
            onClick={() => setActive(t.id)}
            className={cn(
              'rounded-md px-4 py-2 text-sm font-medium transition',
              active === t.id
                ? 'bg-mentor-card text-mentor-primary shadow-sm'
                : 'text-mentor-muted hover:text-mentor-text'
            )}
          >
            {t.label}
          </button>
        ))}
      </div>
      <div role="tabpanel" className="min-h-[120px]">
        {tabs.find((t) => t.id === active)?.content}
      </div>
    </div>
  );
}
