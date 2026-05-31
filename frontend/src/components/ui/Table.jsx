import { cn } from '../../lib/utils';

export function Table({ className, wrapperClassName, children, ...props }) {
  return (
    <div className={cn('w-full overflow-x-auto rounded-lg border border-mentor-border', wrapperClassName)}>
      <table className={cn('w-full border-collapse text-left text-sm', className)} {...props}>
        {children}
      </table>
    </div>
  );
}

export function TableHead({ className, ...props }) {
  return <thead className={cn('border-b border-mentor-border bg-mentor-surface', className)} {...props} />;
}

export function TableBody({ className, ...props }) {
  return <tbody className={cn('divide-y divide-mentor-border bg-mentor-card', className)} {...props} />;
}

export function TableRow({ className, ...props }) {
  return <tr className={cn('hover:bg-mentor-surface/80', className)} {...props} />;
}

export function TableHeaderCell({ className, ...props }) {
  return (
    <th
      className={cn('px-4 py-3 font-semibold text-mentor-text', className)}
      {...props}
    />
  );
}

export function TableCell({ className, ...props }) {
  return <td className={cn('px-4 py-3 text-mentor-muted', className)} {...props} />;
}
