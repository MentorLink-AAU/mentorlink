/** Join group: student or faculty (mentor) join via token. */
import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { joinGroup, mentorJoinGroup, getProfile } from '../lib/api';
import { useAuth } from '../context/AuthContext';

export function JoinGroup() {
  const [token, setToken] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isMentor, setIsMentor] = useState(false);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { isFaculty, isStudent } = useAuth();

  const initialToken = searchParams.get('token') || '';

  useEffect(() => {
    if (!isStudent) return;
    getProfile()
      .then((res) => {
        if (res.data?.data?.group) navigate('/student/groups', { replace: true });
      })
      .catch(() => {});
  }, [isStudent, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const t = token || initialToken;
    if (!t) {
      setError('Please enter a token');
      return;
    }
    setLoading(true);
    setError('');
    try {
      if (isMentor && isFaculty) {
        const res = await mentorJoinGroup(t);
        navigate(`/groups/${res.data?.data?.id}`);
      } else {
        const res = await joinGroup(t);
        navigate(`/groups/${res.data?.data?.id}`);
      }
    } catch (err) {
      setError(err.response?.data?.error?.message || 'Invalid or expired token');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto">
      <h1 className="text-2xl font-bold text-blue-900 mb-6">Join Group</h1>
      <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6 space-y-4">
        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
            {error}
          </div>
        )}
        {isFaculty && (
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={isMentor}
              onChange={(e) => setIsMentor(e.target.checked)}
            />
            <span className="text-sm">Join as mentor</span>
          </label>
        )}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Group Token</label>
          <input
            type="text"
            value={token || initialToken}
            onChange={(e) => setToken(e.target.value)}
            placeholder="Paste the token shared by your group"
            className="w-full px-4 py-3 rounded-lg border border-blue-200 focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          className="w-full py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? 'Joining...' : 'Join Group'}
        </button>
      </form>
    </div>
  );
}
