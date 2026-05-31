/** Set a new password using the token from the email link. */
import { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { resetPasswordWithToken } from '../lib/api';
import { GraduationCap, Eye, EyeOff } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Alert } from '../components/ui/Alert';

export function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const tokenFromUrl = searchParams.get('token') || '';

  const [token, setToken] = useState(tokenFromUrl);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [done, setDone] = useState(false);

  useEffect(() => {
    if (tokenFromUrl) setToken(tokenFromUrl);
  }, [tokenFromUrl]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await resetPasswordWithToken(token.trim(), newPassword, confirmPassword);
      setDone(true);
      setTimeout(() => navigate('/login', { replace: true }), 2000);
    } catch (err) {
      setError(err.response?.data?.error?.message || err.message || 'Reset failed');
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
            <CardTitle>Reset password</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="mb-6 text-sm text-mentor-muted">
              Choose a new password (at least 8 characters, with a letter and a number).
            </p>
            {done && (
              <Alert variant="success" className="mb-4">
                Password updated. Redirecting to sign in…
              </Alert>
            )}
            {error && (
              <Alert variant="error" className="mb-4">
                {error}
              </Alert>
            )}
            {!done && (
              <form onSubmit={handleSubmit} className="space-y-4">
                <Input
                  type="text"
                  label="Reset token"
                  value={token}
                  onChange={(e) => setToken(e.target.value)}
                  required
                  readOnly={Boolean(tokenFromUrl)}
                  placeholder="Paste token from email if not in link"
                />
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-mentor-text">New password</label>
                  <div className="relative">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                      minLength={8}
                      autoComplete="new-password"
                      className="w-full rounded-lg border border-mentor-border bg-mentor-card px-3 py-2.5 pr-11 text-sm text-mentor-text focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20 read-only:opacity-70"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1.5 text-mentor-muted hover:bg-mentor-surface"
                      aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                      {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                    </button>
                  </div>
                </div>
                <Input
                  type={showPassword ? 'text' : 'password'}
                  label="Confirm password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  minLength={8}
                  autoComplete="new-password"
                />
                <Button type="submit" variant="primary" className="w-full" disabled={loading || !token.trim()}>
                  {loading ? 'Updating…' : 'Update password'}
                </Button>
              </form>
            )}
            <p className="mt-6 text-center text-sm text-mentor-muted">
              <Link to="/login" className="font-medium text-mentor-primary hover:underline">
                Sign in
              </Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
