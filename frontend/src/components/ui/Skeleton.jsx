import { cn } from '../../lib/utils';

export function Skeleton({ className, ...props }) {
  return (
    <div
      className={cn('animate-pulse rounded-md bg-mentor-border/80', className)}
      {...props}
    />
  );
}

export function DashboardSkeleton() {
  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <Skeleton className="h-9 w-64" />
        <Skeleton className="h-10 w-32" />
      </div>
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {[1, 2, 3].map((i) => (
          <div key={i} className="rounded-xl border border-mentor-border bg-mentor-card p-0 overflow-hidden">
            <Skeleton className="h-12 w-full rounded-none" />
            <div className="space-y-3 p-5">
              <Skeleton className="h-5 w-[75%]" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-5/6" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
