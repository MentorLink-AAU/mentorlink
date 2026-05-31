/** Faculty: list supervised projects with meeting/schedule status. */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { getFacultyDashboard } from '../lib/api';

const MotionDiv = motion.div;
import { Calendar, CheckCircle2, Clock, FolderKanban } from 'lucide-react';
import { PageHeader } from '../components/ui/PageHeader';
import { Card, CardContent } from '../components/ui/Card';
import { Loader } from '../components/ui/Loader';
import { EmptyState } from '../components/common/EmptyState';

export function FacultyProjects() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getFacultyDashboard()
      .then((res) => setData(res.data?.data))
      .catch(() => setData(null))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader size="lg" />
      </div>
    );
  }

  const projects = data?.supervisedProjects || [];

  return (
    <div className="space-y-8">
      <PageHeader
        title="Assigned projects"
        description="Supervised workstreams, progress, and meeting verification at a glance."
      />
      {projects.length === 0 ? (
        <EmptyState
          icon={FolderKanban}
          title="No projects assigned yet"
          description="When you are matched to a student group, their project cards will appear here."
        />
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {projects.map((p, i) => (
            <MotionDiv
              key={p.projectId}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.05, duration: 0.3 }}
              whileHover={{ y: -4, transition: { duration: 0.2 } }}
            >
              <Card variant="glass" className="h-full">
                <CardContent className="p-6">
                  <Link to={`/projects/${p.projectId}`} className="block">
                    <h3 className="font-semibold text-mentor-text hover:text-mentor-primary">{p.title}</h3>
                    <p className="mt-2 line-clamp-2 text-sm text-mentor-muted">{p.description}</p>
                    <p className="mt-4 text-sm font-medium text-mentor-primary">Progress: {p.progress}%</p>
                    {p.lastMeetingDate && (
                      <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-mentor-muted">
                        <Calendar className="h-4 w-4 shrink-0 text-mentor-primary" />
                        <span>Last meeting: {new Date(p.lastMeetingDate + 'T00:00:00').toLocaleDateString()}</span>
                        {p.lastMeetingVerified ? (
                          <span className="inline-flex items-center gap-0.5 text-mentor-success">
                            <CheckCircle2 className="h-3.5 w-3.5" /> Verified
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-0.5 text-mentor-warning">
                            <Clock className="h-3.5 w-3.5" /> Pending
                          </span>
                        )}
                      </div>
                    )}
                  </Link>
                  {p.groupId && (
                    <Link
                      to={`/groups/${p.groupId}`}
                      className="mt-3 inline-block text-sm font-medium text-mentor-primary hover:underline"
                    >
                      View group →
                    </Link>
                  )}
                </CardContent>
              </Card>
            </MotionDiv>
          ))}
        </div>
      )}
    </div>
  );
}
