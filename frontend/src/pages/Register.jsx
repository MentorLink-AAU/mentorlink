/** Registration: role selection (student/faculty/admin), role-specific form. */
import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { registerStudent, registerFaculty, registerAdmin } from '../lib/api';
import { GraduationCap, Eye, EyeOff } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Alert } from '../components/ui/Alert';

export function Register() {
  const location = useLocation();
  const stateRole = location.state?.role;
  const initialRole = ['student', 'faculty', 'admin'].includes(stateRole) ? stateRole : null;
  const [step, setStep] = useState(initialRole ? 'form' : 'role');
  const [role, setRole] = useState(initialRole || 'student');
  const [form, setForm] = useState({
    email: '',
    fullName: '',
    password: '',
    rollNumber: '',
    department: '',
    yearOfStudy: 1,
    expertise: '',
    maxGroups: 3,
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const update = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (role === 'student') {
        await registerStudent(form);
      } else if (role === 'faculty') {
        await registerFaculty(form);
      } else {
        await registerAdmin({ ...form, role: 'ADMIN' });
      }
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.error?.message || err.message || 'Registration failed');
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
            <CardTitle>Create account</CardTitle>
          </CardHeader>
          <CardContent>
            {step === 'role' ? (
              <div className="space-y-3">
                <p className="text-sm text-mentor-muted">I am a:</p>
                {['student', 'faculty', 'admin'].map((r) => (
                  <Button
                    key={r}
                    type="button"
                    variant="outline"
                    className="w-full capitalize"
                    onClick={() => {
                      setRole(r);
                      setStep('form');
                    }}
                  >
                    {r}
                  </Button>
                ))}
                <Link
                  to="/login"
                  className="mt-4 block text-center text-sm font-medium text-mentor-primary hover:underline"
                >
                  Already have an account? Sign in
                </Link>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <Button type="button" variant="ghost" size="sm" className="-ml-2 px-2" onClick={() => setStep('role')}>
                  ← Change role
                </Button>
                {error && (
                  <Alert variant="error" className="mb-2">
                    {error}
                  </Alert>
                )}
                <Input
                  type="email"
                  label="Email"
                  value={form.email}
                  onChange={(e) => update('email', e.target.value)}
                  required
                  placeholder="you@university.edu"
                  autoComplete="email"
                />
                <Input
                  type="text"
                  label="Full name"
                  value={form.fullName}
                  onChange={(e) => update('fullName', e.target.value)}
                  required
                  autoComplete="name"
                />
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-mentor-text">Password</label>
                  <div className="relative">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      value={form.password}
                      onChange={(e) => update('password', e.target.value)}
                      required
                      minLength={6}
                      autoComplete="new-password"
                      className="w-full rounded-lg border border-mentor-border bg-mentor-card px-3 py-2.5 pr-11 text-sm text-mentor-text focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20"
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
                {role === 'student' && (
                  <>
                    <Input
                      type="text"
                      label="Roll number"
                      value={form.rollNumber}
                      onChange={(e) => update('rollNumber', e.target.value)}
                    />
                    <Input
                      type="text"
                      label="Department"
                      value={form.department}
                      onChange={(e) => update('department', e.target.value)}
                    />
                    <Select
                      label="Year of study"
                      value={form.yearOfStudy}
                      onChange={(e) => update('yearOfStudy', parseInt(e.target.value, 10))}
                    >
                      {[1, 2, 3, 4].map((y) => (
                        <option key={y} value={y}>
                          Year {y}
                        </option>
                      ))}
                    </Select>
                  </>
                )}
                {role === 'faculty' && (
                  <>
                    <Input
                      type="text"
                      label="Department"
                      value={form.department}
                      onChange={(e) => update('department', e.target.value)}
                    />
                    <Input
                      type="text"
                      label="Expertise"
                      value={form.expertise}
                      onChange={(e) => update('expertise', e.target.value)}
                      placeholder="e.g. Machine Learning, Web Development"
                    />
                    <Input
                      type="number"
                      label="Max groups"
                      min={1}
                      max={10}
                      value={form.maxGroups}
                      onChange={(e) => update('maxGroups', parseInt(e.target.value, 10) || 3)}
                    />
                  </>
                )}
                <Button type="submit" variant="primary" className="w-full" disabled={loading}>
                  {loading ? 'Creating account…' : 'Register'}
                </Button>
              </form>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
