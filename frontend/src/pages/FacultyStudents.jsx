/** Faculty: assigned groups / student cohorts overview. */
import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getFacultyDashboard } from '../lib/api';
import { Users } from 'lucide-react';
import { PageHeader } from '../components/ui/PageHeader';
import { DataTable } from '../components/ui/DataTable';
import { Loader } from '../components/ui/Loader';
import { Button } from '../components/ui/Button';

export function FacultyStudents() {
  const [searchParams] = useSearchParams();
  const qFromUrl = searchParams.get('q') || '';
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

  const groups = data?.assignedGroups || [];

  const columns = [
    { key: 'name', header: 'Group', sortable: true },
    { key: 'projectTitle', header: 'Project', sortable: true },
    {
      key: 'memberCount',
      header: 'Members',
      sortable: true,
      accessor: (row) => row.memberCount ?? 0,
    },
    {
      key: 'actions',
      header: '',
      sortable: false,
      render: (row) => (
        <Button variant="ghost" size="sm" as={Link} to={`/groups/${row.groupId}`}>
          View
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-8">
      <PageHeader
        title="Students & groups"
        description="Groups you currently mentor, with quick access to rosters and project context."
      />
      {groups.length === 0 ? (
        <div className="rounded-xl border border-mentor-border bg-mentor-card/80 py-14 text-center text-mentor-muted backdrop-blur">
          <Users className="mx-auto mb-3 h-10 w-10 opacity-40" />
          No assigned groups yet.
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={groups}
          getRowId={(row) => row.groupId}
          searchPlaceholder="Search groups or projects…"
          searchKeys={['name', 'projectTitle']}
          defaultQuery={qFromUrl}
          pageSize={8}
        />
      )}
    </div>
  );
}
