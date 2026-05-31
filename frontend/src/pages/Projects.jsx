/** Student projects: assigned project and available projects list. */
import { useState, useEffect, useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getStudentDashboard } from '../lib/api';
import { PageHeader } from '../components/ui/PageHeader';
import { Card, CardContent } from '../components/ui/Card';

export function Projects() {
  const [searchParams] = useSearchParams();
  const q = (searchParams.get('q') || '').trim().toLowerCase();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getStudentDashboard()
      .then((res) => setData(res.data?.data))
      .catch(() => setData(null))
      .finally(() => setLoading(false));
  }, []);

  const project = data?.assignedProject;
  const group = data?.group;
  const available = useMemo(() => {
    const list = data?.availableProjects;
    return Array.isArray(list) ? list : [];
  }, [data]);

  const assignedMatches = useMemo(() => {
    if (!q || !project) return true;
    const hay = `${project.title || ''} ${project.domain || ''}`.toLowerCase();
    return hay.includes(q);
  }, [q, project]);

  const filteredAvailable = useMemo(() => {
    if (!q) return available;
    return available.filter((p) =>
      `${p.title || ''} ${p.domain || ''}`.toLowerCase().includes(q)
    );
  }, [available, q]);

  if (loading) {
    return (
      <div className="space-y-8 animate-fade-in">
        <div className="h-8 w-48 rounded-lg bg-blue-200/50 animate-pulse" />
        <div className="space-y-4">
          <div className="h-4 w-32 rounded bg-blue-200/50 animate-pulse" />
          <div className="h-32 rounded-2xl bg-white/80 border border-blue-100 animate-pulse" />
        </div>
        <div className="grid sm:grid-cols-2 gap-4">
          {[1, 2].map((i) => (
            <div key={i} className="h-28 rounded-2xl bg-white/80 border border-blue-100 animate-pulse" style={{ animationDelay: `${i * 100}ms` }} />
          ))}
        </div>
        <div className="flex justify-center py-8">
          <div className="animate-spin w-10 h-10 border-2 border-blue-500 border-t-transparent rounded-full" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <PageHeader
        title="Projects"
        description={q ? `Filtered by “${searchParams.get('q')}”.` : 'Your assigned work and discoverable projects.'}
      />

      {project && assignedMatches && (
        <Card variant="glass" className="animate-fade-in-up hover-lift" style={{ animationDelay: '0.05s', opacity: 0 }}>
          <CardContent className="p-6">
            <h2 className="mb-4 font-semibold text-mentor-text">My assigned project</h2>
            <Link
              to={`/projects/${project.projectId}`}
              className="block rounded-xl border border-mentor-border/60 bg-mentor-card/40 p-4 transition hover:border-mentor-primary/40 hover:bg-mentor-primary/[0.04]"
            >
              <h3 className="font-medium text-mentor-text">{project.title}</h3>
              <p className="mt-1 text-sm text-mentor-muted">{project.domain}</p>
              <p className="mt-2 text-sm text-mentor-primary">Progress: {project.progress}%</p>
            </Link>
          </CardContent>
        </Card>
      )}

      {filteredAvailable.length > 0 && (
        <Card variant="glass" className="animate-fade-in-up hover-lift" style={{ animationDelay: '0.1s', opacity: 0 }}>
          <CardContent className="p-6">
            <h2 className="mb-4 font-semibold text-mentor-text">Available projects</h2>
            <div className="grid gap-4 sm:grid-cols-2">
            {filteredAvailable.map((p, i) => (
              <Link
                key={p.projectId}
                to={`/projects/${p.projectId}`}
                className="hover-lift block rounded-xl border border-mentor-border/60 bg-mentor-card/40 p-4 transition animate-fade-in-up hover:border-mentor-primary/40 hover:bg-mentor-primary/[0.04]"
                style={{ animationDelay: `${0.15 + i * 0.05}s`, opacity: 0 }}
              >
                <h3 className="font-medium text-mentor-text">{p.title}</h3>
                <p className="text-sm text-mentor-muted">{p.domain}</p>
              </Link>
            ))}
          </div>
          </CardContent>
        </Card>
      )}

      {q && !assignedMatches && filteredAvailable.length === 0 && (
        <p className="text-center text-sm text-mentor-muted">No projects match your search.</p>
      )}

      {!project && available.length === 0 && !group && (
        <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-8 animate-fade-in-up hover-lift" style={{ animationDelay: '0.1s', opacity: 0 }}>
          <h2 className="font-semibold text-blue-900 mb-4">Get started</h2>
          <p className="text-gray-600 text-sm mb-6">
            Create your own project and group, or join an existing group with an invite token.
          </p>
          <div className="flex flex-wrap gap-3">
            <Link
              to="/groups/create"
              className="inline-flex items-center gap-2 px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-transform hover:scale-105 active:scale-95"
            >
              Create your own group
            </Link>
            <Link
              to="/groups/join"
              className="inline-flex items-center gap-2 px-4 py-3 border border-blue-600 text-blue-700 rounded-lg hover:bg-blue-50 font-medium transition-transform hover:scale-105 active:scale-95"
            >
              Join with invite token
            </Link>
          </div>
        </div>
      )}

      {!project && available.length === 0 && group && (
        <Card variant="glass" className="animate-fade-in-up">
          <CardContent className="p-6">
            <h2 className="mb-2 font-semibold text-mentor-text">No project assigned yet</h2>
            <p className="mb-4 text-sm text-mentor-muted">
              You are in a group. When your mentor assigns a project, it will appear here.
            </p>
            <Link
              to={`/groups/${group.groupId}`}
              className="inline-flex text-sm font-medium text-mentor-primary hover:underline"
            >
              Open your group →
            </Link>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
