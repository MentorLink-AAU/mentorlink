import { useEffect, useMemo, useState } from 'react';
import { ChevronUp, ChevronDown, Search } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Input } from './Input';
import { Button } from './Button';

/**
 * Client-side sortable, searchable, paginated table.
 * columns: { key, header, sortable?, accessor?, render?(row), className? }
 */
export function DataTable({
  columns,
  data,
  getRowId = (row, i) => row.id ?? i,
  searchPlaceholder = 'Search…',
  pageSize = 10,
  searchKeys,
  emptyMessage = 'No rows to display.',
  className,
  defaultQuery = '',
}) {
  const [query, setQuery] = useState(defaultQuery);
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState('asc');
  const [page, setPage] = useState(1);

  useEffect(() => {
    setQuery(defaultQuery || '');
    setPage(1);
  }, [defaultQuery]);

  const keys =
    searchKeys || columns.filter((c) => c.key !== 'actions').map((c) => c.key);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return data;
    return data.filter((row) =>
      keys.some((k) => {
        const v = row[k];
        return v != null && String(v).toLowerCase().includes(q);
      })
    );
  }, [data, query, keys]);

  const sorted = useMemo(() => {
    if (!sortKey) return filtered;
    const col = columns.find((c) => c.key === sortKey);
    if (!col?.sortable) return filtered;
    const dir = sortDir === 'asc' ? 1 : -1;
    return [...filtered].sort((a, b) => {
      const av = col.accessor ? col.accessor(a) : a[sortKey];
      const bv = col.accessor ? col.accessor(b) : b[sortKey];
      if (av == null && bv == null) return 0;
      if (av == null) return 1;
      if (bv == null) return -1;
      if (typeof av === 'number' && typeof bv === 'number') return (av - bv) * dir;
      return String(av).localeCompare(String(bv), undefined, { sensitivity: 'base' }) * dir;
    });
  }, [filtered, sortKey, sortDir, columns]);

  const totalPages = Math.max(1, Math.ceil(sorted.length / pageSize));
  const currentPage = Math.min(page, totalPages);
  const slice = sorted.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  const toggleSort = (key) => {
    const col = columns.find((c) => c.key === key);
    if (!col?.sortable) return;
    if (sortKey !== key) {
      setSortKey(key);
      setSortDir('asc');
    } else {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    }
    setPage(1);
  };

  return (
    <div className={cn('space-y-4', className)}>
      <div className="relative max-w-md">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-mentor-muted" />
        <Input
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            setPage(1);
          }}
          placeholder={searchPlaceholder}
          className="pl-9"
          aria-label="Filter table"
        />
      </div>

      <div className="overflow-hidden rounded-xl border border-mentor-border bg-mentor-card shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full border-collapse text-left text-sm">
            <thead className="border-b border-mentor-border bg-mentor-surface">
              <tr>
                {columns.map((col) => (
                  <th
                    key={col.key}
                    className={cn(
                      'px-4 py-3 font-semibold text-mentor-text',
                      col.sortable && 'cursor-pointer select-none hover:bg-mentor-surface/80',
                      col.className
                    )}
                    onClick={() => col.sortable && toggleSort(col.key)}
                  >
                    <span className="inline-flex items-center gap-1">
                      {col.header}
                      {col.sortable && sortKey === col.key && (
                        sortDir === 'asc' ? (
                          <ChevronUp className="h-4 w-4 text-mentor-primary" />
                        ) : (
                          <ChevronDown className="h-4 w-4 text-mentor-primary" />
                        )
                      )}
                    </span>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-mentor-border">
              {slice.length === 0 ? (
                <tr>
                  <td colSpan={columns.length} className="px-4 py-10 text-center text-mentor-muted">
                    {emptyMessage}
                  </td>
                </tr>
              ) : (
                slice.map((row, i) => (
                  <tr
                    key={getRowId(row, i)}
                    className={cn(
                      'transition hover:bg-mentor-primary/[0.04]',
                      i % 2 === 1 && 'bg-mentor-surface/40'
                    )}
                  >
                    {columns.map((col) => (
                      <td key={col.key} className={cn('px-4 py-3 text-mentor-muted', col.className)}>
                        {col.render ? col.render(row) : row[col.key]}
                      </td>
                    ))}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {sorted.length > pageSize && (
        <div className="flex flex-wrap items-center justify-between gap-3 text-sm text-mentor-muted">
          <span>
            Page {currentPage} of {totalPages} ({sorted.length} rows)
          </span>
          <div className="flex gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              disabled={currentPage <= 1}
              onClick={() => setPage((p) => Math.max(1, p - 1))}
            >
              Previous
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              disabled={currentPage >= totalPages}
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
