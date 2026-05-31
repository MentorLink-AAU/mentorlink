/** First-time login: user must change default password before accessing the app. */
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { changePassword as apiChangePassword } from '../lib/api';
import { GraduationCap, Eye, EyeOff, Lock } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Alert } from '../components/ui/Alert';

export function ChangePassword() {
  const { refreshUser } = useAuth();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (newPassword !== confirmPassword) {
      setError('New password and confirm password do not match.');
      return;
    }
    if (newPassword.length < 8) {
      setError('New password must be at least 8 characters.');
      return;
    }
    if (!/[A-Za-z]/.test(newPassword) || !/\d/.test(newPassword)) {
      setError('New password must contain at least one letter and one number.');
      return;
    }
    setLoading(true);
    try {
      await apiChangePassword(currentPassword, newPassword, confirmPassword);
      const u = await refreshUser();
      const role = u?.role?.replace('ROLE_', '') || u?.role;
      if (role === 'STUDENT') navigate('/dashboard/student', { replace: true });
      else if (role === 'FACULTY') navigate('/dashboard/faculty', { replace: true });
      else if (role === 'ADMIN') navigate('/dashboard/admin', { replace: true });
      else navigate('/', { replace: true });
    } catch (err) {
      setError(err.response?.data?.error?.message || err.message || 'Password change failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-mentor-secondary via-mentor-sidebar to-mentor-primary-dark px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <Link to="/" className="inline-flex items-center gap-2 text-2xl font-bold text-white">
            <GraduationCap className="h-10 w-10 text-white/80" />
            MentorLink
          </Link>
        </div>
        <Card className="border-white/10 bg-white/95 shadow-2xl backdrop-blur">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Lock className="h-6 w-6 text-mentor-warning" />
              Change your password
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="mb-6 text-sm text-mentor-muted">
              You signed in with a default password. For security, please set a new password before continuing.
            </p>
            {error && (
              <Alert variant="error" className="mb-4">
                {error}
              </Alert>
            )}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="mb-1.5 block text-sm font-medium text-mentor-text">Current password</label>
                <div className="relative">
                  <input
                    type={showCurrent ? 'text' : 'password'}
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                    placeholder="Enter current password"
                    className="w-full rounded-lg border border-mentor-border bg-mentor-card px-3 py-2.5 pr-11 text-sm text-mentor-text focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20"
                  />
                  <button
                    type="button"
                    onClick={() => setShowCurrent((s) => !s)}
                    className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1.5 text-mentor-muted hover:bg-mentor-surface"
                    aria-label={showCurrent ? 'Hide' : 'Show'}
                  >
                    {showCurrent ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                  </button>
                </div>
              </div>
              <div>
                <label className="mb-1.5 block text-sm font-medium text-mentor-text">New password</label>
                <div className="relative">
                  <input
                    type={showNew ? 'text' : 'password'}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                    placeholder="At least 8 characters, one letter and one number"
                    className="w-full rounded-lg border border-mentor-border bg-mentor-card px-3 py-2.5 pr-11 text-sm text-mentor-text focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20"
                  />
                  <button
                    type="button"
                    onClick={() => setShowNew((s) => !s)}
                    className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1.5 text-mentor-muted hover:bg-mentor-surface"
                    aria-label={showNew ? 'Hide' : 'Show'}
                  >
                    {showNew ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                  </button>
                </div>
              </div>
              <Input
                type="password"
                label="Confirm new password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                autoComplete="new-password"
                placeholder="Re-enter new password"
              />
              <Button type="submit" variant="primary" className="w-full" disabled={loading}>
                {loading ? 'Updating…' : 'Update password'}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
