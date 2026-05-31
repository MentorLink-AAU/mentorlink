/** Full-page notifications list with mark-as-read. */
import { useEffect, useState } from 'react';
import { Bell } from 'lucide-react';
import { getNotifications, markNotificationRead } from '../lib/api';
import { useNotificationContext } from '../context/NotificationProvider';
import { PageHeader } from '../components/ui/PageHeader';
import { Card, CardContent } from '../components/ui/Card';
import { Loader } from '../components/ui/Loader';
import { Alert } from '../components/ui/Alert';
import { EmptyState } from '../components/common/EmptyState';
import { Badge } from '../components/ui/Badge';

export function Notifications() {
  const { refresh: refreshContext } = useNotificationContext();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    getNotifications(100)
      .then((res) => {
        if (cancelled) return;
        const data = res.data?.data;
        setItems(Array.isArray(data) ? data : []);
      })
      .catch((e) => {
        if (!cancelled) setError(e.response?.data?.error?.message || 'Failed to load notifications');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const handleClick = async (n) => {
    if (n.read) return;
    try {
      await markNotificationRead(n.id);
      setItems((prev) => prev.map((x) => (x.id === n.id ? { ...x, read: true } : x)));
      await refreshContext?.();
    } catch {
      /* keep list unchanged */
    }
  };

  return (
    <div>
      <PageHeader
        title="Notifications"
        description="Stay updated on mentorship activity, deadlines, and group events."
      />

      {loading && (
        <div className="flex justify-center py-16">
          <Loader size="lg" />
        </div>
      )}

      {!loading && error && <Alert variant="error">{error}</Alert>}

      {!loading && !error && items.length === 0 && (
        <EmptyState
          icon={Bell}
          title="You're all caught up"
          description="When something needs your attention, it will show up here."
        />
      )}

      {!loading && !error && items.length > 0 && (
        <Card>
          <CardContent className="divide-y divide-mentor-border p-0">
            {items.map((n) => (
              <button
                key={n.id}
                type="button"
                onClick={() => handleClick(n)}
                className={`flex w-full flex-col gap-1 px-5 py-4 text-left transition hover:bg-mentor-surface/80 ${
                  !n.read ? 'bg-mentor-primary/5' : ''
                }`}
              >
                <div className="flex items-center gap-2">
                  {!n.read && <Badge variant="primary">New</Badge>}
                  <span className="text-xs text-mentor-muted">
                    {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                  </span>
                </div>
                <p className="text-sm text-mentor-text">{n.message}</p>
              </button>
            ))}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
