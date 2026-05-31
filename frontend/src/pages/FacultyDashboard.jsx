/** Faculty dashboard: supervised projects, pending mentorship requests. */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  getFacultyDashboard,
  getPendingMentorshipRequests,
  approveMentorshipRequest,
  rejectMentorshipRequest,
} from '../lib/api';
import { FolderKanban, Users, Bell, ChevronRight, UserPlus, Check, X, BarChart3 } from 'lucide-react';
import { useToast } from '../context/ToastContext';
import { PageHeader } from '../components/ui/PageHeader';
import { StatCard } from '../components/ui/StatCard';
import { Card, CardContent, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { DashboardSkeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/common/EmptyState';

export function FacultyDashboard() {
  const { toast } = useToast();
  const [data, setData] = useState(null);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getFacultyDashboard()
      .then((res) => setData(res.data?.data))
      .catch((e) => setError(e.response?.data?.error?.message || 'Failed to load'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    getPendingMentorshipRequests()
      .then((res) => setPendingRequests(res.data?.data || []))
      .catch(() => setPendingRequests([]));
  }, []);

  const handleApprove = async (requestId) => {
    try {
      await approveMentorshipRequest(requestId);
      setPendingRequests((prev) => prev.filter((r) => r.id !== requestId));
      toast.success('Mentorship request approved');
    } catch (e) {
      toast.error(e?.response?.data?.error?.message || 'Could not approve request');
    }
  };

  const handleReject = async (requestId) => {
    try {
      await rejectMentorshipRequest(requestId);
      setPendingRequests((prev) => prev.filter((r) => r.id !== requestId));
      toast.info('Request declined');
    } catch (e) {
      toast.error(e?.response?.data?.error?.message || 'Could not reject request');
    }
  };

  if (loading) return <DashboardSkeleton />;

  if (error) return <Alert variant="error">{error}</Alert>;

  const profile = data?.profile;
  const projects = data?.supervisedProjects || [];
  const groups = data?.assignedGroups || [];
  const unread = data?.unreadNotificationCount || 0;

  return (
    <div className="space-y-8">
      <PageHeader
        title={`Welcome, Prof. ${profile?.fullName || 'Faculty'}`}
        description="Oversee supervised projects, groups, and mentorship requests."
        actions={
          unread > 0 ? (
            <Button variant="outline" size="sm" as={Link} to="/notifications">
              <Bell className="h-4 w-4" />
              {unread} unread
            </Button>
          ) : null
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard icon={FolderKanban} label="Supervised projects" value={projects.length} index={0} glass />
        <StatCard icon={Users} label="Assigned groups" value={groups.length} index={1} glass />
        <StatCard icon={UserPlus} label="Pending requests" value={pendingRequests.length} index={2} glass />
        <StatCard
          icon={BarChart3}
          label="Mentorship load"
          value={profile?.currentLoad ?? 0}
          suffix={` / ${profile?.maxGroups ?? 3}`}
          index={3}
          glass
        />
      </div>

      {pendingRequests.length > 0 && (
        <Card className="overflow-hidden border-mentor-warning/30">
          <CardHeader className="flex flex-row items-center gap-2 border-mentor-warning/20 bg-mentor-warning/10 py-3">
            <UserPlus className="h-5 w-5 text-mentor-warning" />
            <span className="font-semibold text-mentor-text">
              Mentorship requests ({pendingRequests.length})
            </span>
          </CardHeader>
          <CardContent className="space-y-4">
            {pendingRequests.map((r) => (
              <div
                key={r.id}
                className="flex flex-col gap-4 rounded-xl border border-mentor-border bg-mentor-card p-4 sm:flex-row sm:items-center sm:justify-between"
              >
                <div>
                  <h3 className="font-medium text-mentor-text">{r.groupName}</h3>
                  <p className="text-sm text-mentor-muted">{r.projectTopic}</p>
                  {r.projectDescription && (
                    <p className="mt-1 line-clamp-1 text-xs text-mentor-muted">{r.projectDescription}</p>
                  )}
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button variant="ghost" size="sm" as={Link} to={`/groups/${r.groupId}`}>
                    View group
                  </Button>
                  <Button variant="primary" size="sm" type="button" onClick={() => handleApprove(r.id)}>
                    <Check className="h-4 w-4" />
                    Approve
                  </Button>
                  <Button variant="outline" size="sm" type="button" onClick={() => handleReject(r.id)}>
                    <X className="h-4 w-4" />
                    Reject
                  </Button>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent className="space-y-2">
          <p className="text-sm font-medium text-mentor-primary">
            Load: {profile?.currentLoad ?? 0} / {profile?.maxGroups ?? 3} groups
          </p>
          <p className="text-sm text-mentor-muted">Expertise: {profile?.expertise || '—'}</p>
        </CardContent>
      </Card>

      <Card className="overflow-hidden">
        <CardHeader className="flex flex-row items-center justify-between border-mentor-border bg-mentor-surface/80 py-3">
          <span className="flex items-center gap-2 font-semibold text-mentor-text">
            <FolderKanban className="h-5 w-5 text-mentor-primary" />
            Supervised projects
          </span>
          <Link
            to="/faculty/projects"
            className="text-sm font-medium text-mentor-primary hover:underline"
          >
            View all
          </Link>
        </CardHeader>
        <CardContent>
          {projects.length === 0 ? (
            <EmptyState
              title="No projects assigned"
              description="Projects appear here when you are assigned as a mentor."
            />
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {projects.map((p) => (
                <div
                  key={p.projectId}
                  className="rounded-xl border border-mentor-border p-4 transition hover:border-mentor-primary/40 hover:bg-mentor-surface/50"
                >
                  <Link to={`/projects/${p.projectId}`} className="block">
                    <h3 className="font-medium text-mentor-primary hover:underline">{p.title}</h3>
                    <p className="mt-1 text-sm text-mentor-muted">Progress: {p.progress}%</p>
                  </Link>
                  {p.groupId && (
                    <Link
                      to={`/groups/${p.groupId}`}
                      className="mt-3 inline-flex items-center gap-1 text-sm font-medium text-mentor-primary hover:underline"
                    >
                      View group <ChevronRight className="h-4 w-4" />
                    </Link>
                  )}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Card className="overflow-hidden">
        <CardHeader className="flex flex-row items-center gap-2 border-mentor-border bg-mentor-surface/80 py-3">
          <Users className="h-5 w-5 text-mentor-primary" />
          <span className="font-semibold text-mentor-text">Assigned groups</span>
        </CardHeader>
        <CardContent>
          {groups.length === 0 ? (
            <EmptyState title="No groups assigned" description="Student groups you mentor will list here." />
          ) : (
            <div className="space-y-3">
              {groups.map((g) => (
                <Link
                  key={g.groupId}
                  to={`/groups/${g.groupId}`}
                  className="flex items-center justify-between rounded-xl border border-mentor-border p-4 transition hover:border-mentor-primary/40 hover:bg-mentor-surface/50"
                >
                  <div>
                    <h3 className="font-medium text-mentor-text">{g.name}</h3>
                    <p className="text-sm text-mentor-muted">
                      {g.projectTitle} • {g.memberCount} members
                    </p>
                  </div>
                  <ChevronRight className="h-5 w-5 shrink-0 text-mentor-primary" />
                </Link>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
