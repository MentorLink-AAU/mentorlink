/** Login page: email/password, optional role, redirect after auth. */
import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { GraduationCap, Eye, EyeOff } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Alert } from '../components/ui/Alert';

export function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const { login, isStudent, isFaculty, isAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const roleKey = role || (isStudent ? 'student' : isFaculty ? 'faculty' : isAdmin ? 'admin' : null);
      const user = await login(email, password, roleKey);
      navigate(user?.requiresPasswordChange ? '/change-password' : from, { replace: true });
    } catch (err) {
      setError(err.response?.data?.error?.message || err.message || 'Login failed');
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
            <CardTitle>Sign in</CardTitle>
          </CardHeader>
          <CardContent>
            {error && (
              <Alert variant="error" className="mb-4">
                {error}
              </Alert>
            )}
            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                type="email"
                label="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                placeholder="you@university.edu"
                autoComplete="email"
              />
              <div>
                <label className="mb-1.5 block text-sm font-medium text-mentor-text">Password</label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                    className="w-full rounded-lg border border-mentor-border bg-mentor-card px-3 py-2.5 pr-11 text-sm text-mentor-text focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1.5 text-mentor-muted hover:bg-mentor-surface hover:text-mentor-text"
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                  </button>
                </div>
              </div>
              <Select label="Login as" value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="">Any role</option>
                <option value="student">Student</option>
                <option value="faculty">Faculty</option>
                <option value="admin">Admin</option>
              </Select>
              <div className="text-right">
                <Link
                  to="/forgot-password"
                  className="text-sm font-medium text-mentor-primary hover:underline"
                >
                  Forgot password?
                </Link>
              </div>
              <Button type="submit" variant="primary" className="w-full" disabled={loading}>
                {loading ? 'Signing in…' : 'Sign in'}
              </Button>
            </form>
            <p className="mt-6 text-center text-sm text-mentor-muted">
              Don&apos;t have an account?{' '}
              <Link to="/register" className="font-medium text-mentor-primary hover:underline">
                Register
              </Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
