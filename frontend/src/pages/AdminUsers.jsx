/** Admin: list users, delete user — modern data table. */
import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getUsers, deleteUser } from '../lib/api';
import { useToast } from '../context/ToastContext';
import { PageHeader } from '../components/ui/PageHeader';
import { DataTable } from '../components/ui/DataTable';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Alert } from '../components/ui/Alert';
import { Loader } from '../components/ui/Loader';

export function AdminUsers() {
  const { toast } = useToast();
  const [searchParams] = useSearchParams();
  const qFromUrl = searchParams.get('q') || '';
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getUsers()
      .then((res) => {
        const raw = res.data;
        const list = Array.isArray(raw?.data) ? raw.data : Array.isArray(raw) ? raw : [];
        setUsers(list);
      })
      .catch((err) => {
        setUsers([]);
        const msg =
          err.response?.data?.error?.message ||
          err.response?.data?.message ||
          err.message;
        setError(
          msg || (err.response?.status === 403 ? 'Access denied. Admin role required.' : 'Failed to load users.')
        );
      })
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id) => {
    if (!confirm('Delete this user?')) return;
    try {
      await deleteUser(id);
      setUsers((prev) => prev.filter((u) => u.id !== id));
      toast.success('User removed');
    } catch (e) {
      toast.error(e.response?.data?.error?.message || 'Delete failed');
    }
  };

  const columns = [
    { key: 'fullName', header: 'Name', sortable: true },
    { key: 'email', header: 'Email', sortable: true },
    {
      key: 'role',
      header: 'Role',
      sortable: true,
      render: (u) => (
        <Badge variant="primary">{u.role?.replace('ROLE_', '') || '—'}</Badge>
      ),
    },
    {
      key: 'actions',
      header: '',
      sortable: false,
      className: 'text-right',
      render: (u) => (
        <Button type="button" variant="danger" size="sm" onClick={() => handleDelete(u.id)}>
          Delete
        </Button>
      ),
    },
  ];

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <PageHeader title="Users" description="Manage platform accounts and roles." />
      {error && <Alert variant="error">{error}</Alert>}
      {!error && (
        <DataTable
          columns={columns}
          data={users}
          getRowId={(u) => u.id}
          searchPlaceholder="Filter by name or email…"
          searchKeys={['fullName', 'email', 'role']}
          defaultQuery={qFromUrl}
          pageSize={10}
          emptyMessage="No users found."
        />
      )}
    </div>
  );
}
