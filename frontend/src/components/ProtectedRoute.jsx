/** Wraps routes that require auth and optionally specific roles. */
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function ProtectedRoute({ children, roles }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center py-16">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-mentor-primary border-t-transparent" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (roles?.length) {
    const role = user.role?.replace('ROLE_', '') || user.role;
    const allowed = roles.some((r) => r === role || r === `ROLE_${role}`);
    if (!allowed) {
      return <Navigate to="/" replace />;
    }
  }

  return children;
}
