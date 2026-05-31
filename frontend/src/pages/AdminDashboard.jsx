/** Admin dashboard: stats, groups list, search, expandable group details. */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAdminDashboard, getAdminGroupsWithProgress, resetYearlyData } from '../lib/api';
import {
  BarChart3,
  Users,
  Calendar,
  Bell,
  ChevronRight,
  Search,
  ChevronDown,
  ChevronUp,
  Upload,
  Sparkles,
} from 'lucide-react';
import { useToast } from '../context/ToastContext';
import { PageHeader } from '../components/ui/PageHeader';
import { StatCard } from '../components/ui/StatCard';
import { Card, CardContent, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Alert } from '../components/ui/Alert';
import { Loader } from '../components/ui/Loader';
import { DashboardSkeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/common/EmptyState';

export function AdminDashboard() {
  const { toast } = useToast();
  const [data, setData] = useState(null);
  const [groups, setGroups] = useState([]);
  const [groupsLoading, setGroupsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedGroupId, setExpandedGroupId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [resetting, setResetting] = useState(false);
  const [resetMessage, setResetMessage] = useState('');

  useEffect(() => {
    getAdminDashboard()
      .then((res) => setData(res.data?.data))
      .catch((e) => setError(e.response?.data?.error?.message || 'Failed to load'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      setGroupsLoading(true);
      getAdminGroupsWithProgress(searchQuery.trim() || undefined)
        .then((res) => setGroups(res.data?.data || []))
        .catch(() => setGroups([]))
        .finally(() => setGroupsLoading(false));
    }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  if (loading) return <DashboardSkeleton />;

  if (error) return <Alert variant="error">{error}</Alert>;

  const profile = data?.profile;
  const analytics = data?.analytics || {};
  const unread = data?.unreadNotificationCount || 0;

  const handleYearlyReset = async () => {
    const confirmed = window.confirm(
      'This will delete yearly academic data, uploaded users, groups, projects, deadlines, recommender outputs, and related records. Admin accounts will be kept. Do you want to continue?'
    );
    if (!confirmed) return;

    const secondConfirm = window.prompt('Type RESET to confirm yearly data reset.');
    if (secondConfirm !== 'RESET') return;

    try {
      setResetting(true);
      setResetMessage('');
      const res = await resetYearlyData();
      setResetMessage(res.data?.data || 'Yearly data reset successfully.');
      toast.success('Yearly data reset completed');
      setGroups([]);
      const dashboardRes = await getAdminDashboard();
      setData(dashboardRes.data?.data);
    } catch (e) {
      const msg = e.response?.data?.error?.message || 'Failed to reset yearly data.';
      setResetMessage(msg);
      toast.error(msg);
    } finally {
      setResetting(false);
    }
  };

  const stats = [
    { label: 'Total Users', value: analytics.totalUsers ?? 0, icon: Users },
    { label: 'Projects', value: analytics.totalProjects ?? 0, icon: BarChart3 },
    { label: 'Groups', value: analytics.totalGroups ?? 0, icon: Users },
    { label: 'Faculty', value: analytics.totalFaculty ?? 0, icon: Users },
  ];

  return (
    <div className="space-y-8">
      <PageHeader
        title="Admin dashboard"
        description={`Signed in as ${profile?.fullName || profile?.email || 'Admin'}`}
        actions={
          unread > 0 ? (
            <Button variant="outline" size="sm" as={Link} to="/notifications">
              <Bell className="h-4 w-4" />
              {unread} unread
            </Button>
          ) : null
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map(({ label, value, icon }, i) => (
          <StatCard key={label} icon={icon} label={label} value={value} index={i} glass />
        ))}
      </div>

      <Card className="overflow-hidden">
        <CardHeader className="flex flex-col gap-4 border-mentor-border py-4 sm:flex-row sm:items-center">
          <h2 className="shrink-0 font-semibold text-mentor-text">All groups & progress</h2>
          <div className="relative flex-1 max-w-xl">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-mentor-muted" />
            <Input
              placeholder="Search by group name, project, mentor, or member..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>
        </CardHeader>
        <div className="max-h-[400px] overflow-y-auto border-t border-mentor-border">
          {groupsLoading ? (
            <div className="flex justify-center py-12">
              <Loader />
            </div>
          ) : groups.length === 0 ? (
            <EmptyState title="No groups found" description="Try a different search term." className="border-0" />
          ) : (
            <div className="divide-y divide-mentor-border">
              {groups.map((g) => (
                <div key={g.groupId} className="hover:bg-mentor-surface/50">
                  <div
                    role="button"
                    tabIndex={0}
                    onClick={() => setExpandedGroupId(expandedGroupId === g.groupId ? null : g.groupId)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ')
                        setExpandedGroupId(expandedGroupId === g.groupId ? null : g.groupId);
                    }}
                    className="flex w-full cursor-pointer items-center justify-between gap-4 px-6 py-4 text-left"
                  >
                    <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2 sm:gap-4">
                      <span className="font-medium text-mentor-text">{g.groupName}</span>
                      <Link
                        to={`/admin/groups/${g.groupId}`}
                        onClick={(e) => e.stopPropagation()}
                        className="text-sm font-medium text-mentor-primary hover:underline"
                      >
                        {g.projectTitle || '—'}
                      </Link>
                      <span className="text-sm font-medium text-mentor-primary">Progress: {g.progress}%</span>
                    </div>
                    {expandedGroupId === g.groupId ? (
                      <ChevronUp className="h-5 w-5 shrink-0 text-mentor-primary" />
                    ) : (
                      <ChevronDown className="h-5 w-5 shrink-0 text-mentor-primary" />
                    )}
                  </div>
                  {expandedGroupId === g.groupId && (
                    <div className="space-y-3 border-t border-mentor-border bg-mentor-surface/40 px-6 pb-4 pt-2">
                      <div className="grid gap-4 text-sm sm:grid-cols-2">
                        <div>
                          <p className="mb-1 font-medium text-mentor-text">Members ({g.memberCount})</p>
                          <ul className="space-y-1">
                            {(g.members || []).map((m) => (
                              <li key={m.userId} className="text-mentor-muted">
                                {m.fullName || m.email} {m.isLeader && '(Leader)'}
                              </li>
                            ))}
                          </ul>
                        </div>
                        <div>
                          <p className="mb-1 font-medium text-mentor-text">Mentor</p>
                          <p className="text-mentor-muted">{g.mentorName || g.mentorEmail || '—'}</p>
                          {g.lastMeetingDate && (
                            <>
                              <p className="mb-1 mt-3 font-medium text-mentor-text">Last meeting</p>
                              <p className="text-mentor-muted">
                                {new Date(g.lastMeetingDate + 'T00:00:00').toLocaleDateString()}
                                {g.lastMeetingVerified ? ' ✓ Verified' : ' (Pending)'}
                              </p>
                              {g.lastMeetingDetails && (
                                <p className="mt-1 line-clamp-2 text-xs text-mentor-muted">{g.lastMeetingDetails}</p>
                              )}
                            </>
                          )}
                        </div>
                      </div>
                      <Link
                        to={`/groups/${g.groupId}`}
                        className="inline-flex items-center gap-1 text-sm font-medium text-mentor-primary hover:underline"
                      >
                        View full details <ChevronRight className="h-4 w-4" />
                      </Link>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        {[
          { to: '/admin/users', title: 'Manage users', desc: 'View, add, remove users', icon: Users },
          { to: '/admin/analytics', title: 'Analytics', desc: 'Platform analytics', icon: BarChart3 },
          { to: '/admin/deadlines', title: 'Deadlines', desc: 'Manage deadlines', icon: Calendar },
          { to: '/admin/upload', title: 'Bulk upload', desc: 'Upload students/faculty Excel', icon: Upload },
          { to: '/admin/auto-group', title: 'Auto-group', desc: 'Auto-group from Excel', icon: Sparkles },
        ].map(({ to, title, desc, icon }) => {
          const QuickIcon = icon;
          return (
          <Link key={to} to={to}>
            <Card className="h-full transition hover:border-mentor-primary/40 hover:shadow-md">
              <CardContent className="flex items-center justify-between gap-4 py-5">
                <div className="flex items-center gap-3">
                  <div className="rounded-xl bg-mentor-primary/10 p-3">
                    <QuickIcon className="h-6 w-6 text-mentor-primary" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-mentor-text">{title}</h3>
                    <p className="text-sm text-mentor-muted">{desc}</p>
                  </div>
                </div>
                <ChevronRight className="h-5 w-5 shrink-0 text-mentor-primary" />
              </CardContent>
            </Card>
          </Link>
          );
        })}
      </div>

      <Alert variant="error" title="Yearly database reset">
        <p>
          Use this at the end of the academic year to clear students, faculty, projects, groups, deadlines,
          recommender outputs, and uploaded files. Admin accounts are preserved.
        </p>
        {resetMessage && (
          <div className="mt-3 rounded-lg border border-mentor-border bg-mentor-card px-4 py-3 text-sm text-mentor-text">
            {resetMessage}
          </div>
        )}
        <div className="mt-4">
          <Button variant="danger" type="button" disabled={resetting} onClick={handleYearlyReset}>
            {resetting ? 'Resetting…' : 'Reset yearly data'}
          </Button>
        </div>
      </Alert>
    </div>
  );
}
