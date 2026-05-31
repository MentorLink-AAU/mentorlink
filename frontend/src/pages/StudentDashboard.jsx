/** Student dashboard: project, group, submissions, available projects. */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getStudentDashboard } from '../lib/api';
import {
  FolderKanban,
  Users,
  FileText,
  Bell,
  ChevronRight,
  Plus,
  LayoutList,
  TrendingUp,
} from 'lucide-react';
import { PageHeader } from '../components/ui/PageHeader';
import { StatCard } from '../components/ui/StatCard';
import { Card, CardContent, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Alert } from '../components/ui/Alert';
import { DashboardSkeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/common/EmptyState';

export function StudentDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getStudentDashboard()
      .then((res) => setData(res.data?.data))
      .catch((e) => setError(e.response?.data?.error?.message || 'Failed to load'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <DashboardSkeleton />;
  }

  if (error) {
    return <Alert variant="error">{error}</Alert>;
  }

  const profile = data?.profile;
  const project = data?.assignedProject;
  const group = data?.group;
  const submissions = data?.mySubmissions || [];
  const availableProjects = data?.availableProjects || [];
  const unread = data?.unreadNotificationCount || 0;

  return (
    <div className="space-y-8">
      <PageHeader
        title={`Welcome, ${profile?.fullName || 'Student'}`}
        description="Your project, group, and submission activity in one place."
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
        <StatCard icon={FileText} label="Submissions" value={submissions.length} index={0} glass />
        <StatCard icon={LayoutList} label="Open listings" value={availableProjects.length} index={1} glass />
        <StatCard icon={Users} label="Group members" value={group?.memberCount ?? 0} index={2} glass />
        <StatCard icon={TrendingUp} label="Project progress" value={project?.progress ?? 0} suffix="%" index={3} glass />
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <Card className="overflow-hidden">
          <CardHeader className="flex flex-row items-center gap-2 border-mentor-border bg-mentor-surface/80 py-3">
            <FolderKanban className="h-5 w-5 text-mentor-primary" />
            <span className="font-semibold text-mentor-text">My project</span>
          </CardHeader>
          <CardContent>
            {project ? (
              <>
                <h3 className="mb-2 font-semibold text-mentor-text">{project.title}</h3>
                <p className="mb-4 line-clamp-2 text-sm text-mentor-muted">{project.description}</p>
                <div className="flex items-center justify-between gap-2">
                  <Badge variant="primary">Progress: {project.progress}%</Badge>
                  <Link
                    to={`/projects/${project.projectId}`}
                    className="inline-flex items-center gap-1 text-sm font-medium text-mentor-primary hover:underline"
                  >
                    View <ChevronRight className="h-4 w-4" />
                  </Link>
                </div>
              </>
            ) : (
              <EmptyState
                title="No project yet"
                description="Your faculty mentor will assign a project when your group is ready."
                className="border-0 bg-transparent py-6"
              />
            )}
          </CardContent>
        </Card>

        <Card className="overflow-hidden">
          <CardHeader className="flex flex-row items-center gap-2 border-mentor-border bg-mentor-surface/80 py-3">
            <Users className="h-5 w-5 text-mentor-primary" />
            <span className="font-semibold text-mentor-text">My group</span>
          </CardHeader>
          <CardContent>
            {group ? (
              <>
                <h3 className="mb-2 font-semibold text-mentor-text">{group.name}</h3>
                <p className="mb-2 text-sm text-mentor-muted">{group.projectTitle}</p>
                <p className="mb-4 text-sm text-mentor-primary">{group.memberCount} members</p>
                <Link
                  to={`/groups/${group.groupId}`}
                  className="inline-flex items-center gap-1 text-sm font-medium text-mentor-primary hover:underline"
                >
                  View group <ChevronRight className="h-4 w-4" />
                </Link>
              </>
            ) : (
              <div className="space-y-3">
                <p className="text-sm text-mentor-muted">Not in a group yet. Choose an option:</p>
                <div className="flex flex-col gap-2">
                  <Button variant="primary" size="sm" as={Link} to="/groups/create">
                    <Plus className="h-4 w-4" /> Create your own group
                  </Button>
                  <Button variant="outline" size="sm" as={Link} to="/groups/join">
                    <Users className="h-4 w-4" /> Join with invite token
                  </Button>
                </div>
                <p className="text-xs text-mentor-muted">
                  Create: start a new project and get a token to invite peers. Join: use a token from your
                  teammate.
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="overflow-hidden">
          <CardHeader className="flex flex-row items-center gap-2 border-mentor-border bg-mentor-surface/80 py-3">
            <FileText className="h-5 w-5 text-mentor-primary" />
            <span className="font-semibold text-mentor-text">My submissions</span>
          </CardHeader>
          <CardContent>
            {submissions.length > 0 ? (
              <>
                <ul className="space-y-2">
                  {submissions.slice(0, 3).map((s) => (
                    <li key={s.id} className="text-sm text-mentor-text">
                      {s.originalFilename} ({s.category})
                    </li>
                  ))}
                  {submissions.length > 3 && (
                    <li className="text-sm text-mentor-primary">+{submissions.length - 3} more</li>
                  )}
                </ul>
                {project && (
                  <Link
                    to={`/projects/${project.projectId}/submissions`}
                    className="mt-4 inline-flex items-center gap-1 text-sm font-medium text-mentor-primary hover:underline"
                  >
                    Manage submissions <ChevronRight className="h-4 w-4" />
                  </Link>
                )}
              </>
            ) : (
              <EmptyState
                title="No submissions yet"
                description="Upload reports and artifacts from your project page."
                className="border-0 bg-transparent py-4"
              />
            )}
          </CardContent>
        </Card>
      </div>

      {availableProjects.length > 0 && (
        <Card className="overflow-hidden">
          <CardHeader className="border-mentor-border bg-mentor-surface/80 py-3">
            <span className="font-semibold text-mentor-text">Available projects</span>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {availableProjects.map((p) => (
                <Link
                  key={p.projectId}
                  to={`/projects/${p.projectId}`}
                  className="block rounded-xl border border-mentor-border p-4 transition hover:border-mentor-primary/40 hover:bg-mentor-surface/50"
                >
                  <h4 className="font-medium text-mentor-text">{p.title}</h4>
                  <p className="mt-1 line-clamp-1 text-sm text-mentor-muted">{p.domain}</p>
                </Link>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="font-semibold text-mentor-text">Deadlines</h2>
            <p className="text-sm text-mentor-muted">University and course milestone dates.</p>
          </div>
          <Button variant="outline" size="sm" as={Link} to="/deadlines">
            View all deadlines
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
