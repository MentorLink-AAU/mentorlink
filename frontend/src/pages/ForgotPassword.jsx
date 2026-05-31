/** Request a password reset link by email. */
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../lib/api';
import { GraduationCap, ArrowLeft } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Alert } from '../components/ui/Alert';

export function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [doneMessage, setDoneMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setDoneMessage('');
    setLoading(true);
    try {
      const res = await forgotPassword(email);
      const msg = res.data?.data;
      setDoneMessage(
        typeof msg === 'string' ? msg : 'If an account exists for this email, you will receive instructions shortly.'
      );
    } catch (err) {
      setError(err.response?.data?.error?.message || err.message || 'Something went wrong');
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
            <CardTitle>Forgot password</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="mb-6 text-sm text-mentor-muted">
              Enter your account email. If it exists, we will send a reset link (valid 15 minutes).
            </p>
            {error && (
              <Alert variant="error" className="mb-4">
                {error}
              </Alert>
            )}
            {doneMessage && (
              <Alert variant="success" className="mb-4">
                {doneMessage}
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
              <Button type="submit" variant="primary" className="w-full" disabled={loading}>
                {loading ? 'Sending…' : 'Send reset link'}
              </Button>
            </form>
            <p className="mt-6 text-center">
              <Link
                to="/login"
                className="inline-flex items-center gap-2 text-sm font-medium text-mentor-primary hover:underline"
              >
                <ArrowLeft className="h-4 w-4" />
                Back to sign in
              </Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
