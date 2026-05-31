/** Student hub: single group — open current group, or create/join until you have one. */
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProfile } from '../lib/api';
import { Users, PlusCircle, LogIn, ChevronRight } from 'lucide-react';
import { PageHeader } from '../components/ui/PageHeader';
import { Card, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { EmptyState } from '../components/common/EmptyState';
import { Loader } from '../components/ui/Loader';

export function StudentGroups() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getProfile()
      .then((res) => setProfile(res.data?.data))
      .catch(() => setProfile(null))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader size="lg" />
      </div>
    );
  }

  const group = profile?.group;

  return (
    <div className="space-y-8">
      <PageHeader
        title="Groups"
        description={
          group
            ? 'You belong to one project group. Open it below — you cannot create or join another.'
            : 'Create a new group or join with an invite token. After you have a group, those options are hidden.'
        }
      />

      <div className={`grid gap-6 ${group ? 'md:grid-cols-1' : 'md:grid-cols-3'}`}>
        {group && (
          <Card variant="glass" className="max-w-lg">
            <CardContent className="flex h-full flex-col justify-between space-y-4 py-6">
              <div>
                <div className="mb-2 flex items-center gap-2 text-mentor-primary">
                  <Users className="h-5 w-5" />
                  <span className="text-xs font-semibold uppercase tracking-wide">Your group</span>
                </div>
                <h3 className="text-lg font-semibold text-mentor-text">{group.name}</h3>
                <p className="mt-1 text-sm text-mentor-muted">{group.projectTitle}</p>
                <p className="mt-2 text-sm text-mentor-primary">{group.memberCount} members</p>
              </div>
              <Button variant="primary" size="sm" as={Link} to={`/groups/${group.groupId}`}>
                Open group <ChevronRight className="h-4 w-4" />
              </Button>
            </CardContent>
          </Card>
        )}

        {!group && (
          <>
            <Card variant="glass">
              <CardContent className="flex h-full flex-col justify-between space-y-4 py-6">
                <div>
                  <div className="mb-2 flex items-center gap-2 text-mentor-primary">
                    <PlusCircle className="h-5 w-5" />
                    <span className="text-xs font-semibold uppercase tracking-wide">Start new</span>
                  </div>
                  <h3 className="text-lg font-semibold text-mentor-text">Create group</h3>
                  <p className="mt-1 text-sm text-mentor-muted">
                    Start a project group and invite teammates with a token.
                  </p>
                </div>
                <Button variant="secondary" size="sm" as={Link} to="/groups/create">
                  Create group
                </Button>
              </CardContent>
            </Card>

            <Card variant="glass">
              <CardContent className="flex h-full flex-col justify-between space-y-4 py-6">
                <div>
                  <div className="mb-2 flex items-center gap-2 text-mentor-primary">
                    <LogIn className="h-5 w-5" />
                    <span className="text-xs font-semibold uppercase tracking-wide">Join</span>
                  </div>
                  <h3 className="text-lg font-semibold text-mentor-text">Join with token</h3>
                  <p className="mt-1 text-sm text-mentor-muted">Use an invite link or token from your team lead.</p>
                </div>
                <Button variant="outline" size="sm" as={Link} to="/groups/join">
                  Join group
                </Button>
              </CardContent>
            </Card>
          </>
        )}
      </div>

      {!group && (
        <EmptyState
          icon={Users}
          title="No group yet"
          description="Create or join a group to collaborate on your capstone or research project."
        />
      )}
    </div>
  );
}
