/** Faculty: load AI mentor recommendations per supervised project. */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getFacultyDashboard, getRecommendations } from '../lib/api';
import { Sparkles, ChevronRight, Loader2 } from 'lucide-react';
import { PageHeader } from '../components/ui/PageHeader';
import { Card, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { FacultyRecommendationCards } from '../components/recommendations/FacultyRecommendationCards';
import { Loader } from '../components/ui/Loader';
import { useToast } from '../context/ToastContext';

export function FacultyRecommendations() {
  const { toast } = useToast();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openId, setOpenId] = useState(null);
  const [recByProject, setRecByProject] = useState({});
  const [loadingId, setLoadingId] = useState(null);

  useEffect(() => {
    getFacultyDashboard()
      .then((res) => setProjects(res.data?.data?.supervisedProjects || []))
      .catch(() => setProjects([]))
      .finally(() => setLoading(false));
  }, []);

  const loadRecs = async (projectId) => {
    setLoadingId(projectId);
    try {
      const res = await getRecommendations(projectId);
      const data = res.data?.data;
      const list = data?.recommendedFaculty || [];
      setRecByProject((prev) => ({ ...prev, [projectId]: list }));
      if (!list.length) toast.info('No recommendations returned for this project yet.');
      else toast.success('Recommendations loaded');
    } catch (e) {
      toast.error(e.response?.data?.error?.message || 'Failed to load recommendations');
    } finally {
      setLoadingId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <PageHeader
        title="Recommendations"
        description="Run the mentor recommender on each supervised project. Results are ranked by similarity."
      />

      {projects.length === 0 ? (
        <Card variant="glass">
          <CardContent className="py-12 text-center text-mentor-muted">
            No supervised projects yet. Once you are assigned to a project, it will appear here.
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {projects.map((p) => {
            const open = openId === p.projectId;
            const recs = recByProject[p.projectId];
            return (
              <Card key={p.projectId} variant="glass" className="overflow-hidden">
                <CardContent className="space-y-4 py-5">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <h3 className="font-semibold text-mentor-text">{p.title}</h3>
                      <p className="text-sm text-mentor-muted line-clamp-2">{p.description}</p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => setOpenId(open ? null : p.projectId)}
                      >
                        {open ? 'Hide' : 'Show'} recommender
                      </Button>
                      <Button variant="primary" size="sm" as={Link} to={`/projects/${p.projectId}`}>
                        Open project <ChevronRight className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                  {open && (
                    <div className="border-t border-white/20 pt-4">
                      <Button
                        type="button"
                        variant="secondary"
                        size="sm"
                        className="mb-4"
                        disabled={loadingId === p.projectId}
                        onClick={() => loadRecs(p.projectId)}
                      >
                        {loadingId === p.projectId ? (
                          <>
                            <Loader2 className="h-4 w-4 animate-spin" /> Loading…
                          </>
                        ) : (
                          <>
                            <Sparkles className="h-4 w-4" /> Get recommendations
                          </>
                        )}
                      </Button>
                      {recs && <FacultyRecommendationCards items={recs} />}
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
