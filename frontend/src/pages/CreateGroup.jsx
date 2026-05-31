/** Create group: name, project title/description, generates join tokens. */
import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createGroup, getProfile } from '../lib/api';
import { Copy, Check, Users } from 'lucide-react';

export function CreateGroup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    projectTitle: '',
    projectDescription: '',
    projectDomain: '',
    projectTechStack: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [createdGroup, setCreatedGroup] = useState(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    getProfile()
      .then((res) => {
        if (res.data?.data?.group) navigate('/student/groups', { replace: true });
      })
      .catch(() => {});
  }, [navigate]);

  const update = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const copyToken = (token) => {
    navigator.clipboard.writeText(token);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await createGroup(form);
      setCreatedGroup(res.data?.data);
    } catch (err) {
      setError(err.response?.data?.error?.message || 'Failed to create group');
    } finally {
      setLoading(false);
    }
  };

  if (createdGroup) {
    return (
      <div className="max-w-lg mx-auto space-y-6">
        <div className="bg-blue-50 rounded-2xl border border-blue-200 p-6">
          <h2 className="text-lg font-semibold text-blue-900 mb-2 flex items-center gap-2">
            <Users className="w-5 h-5" /> Group created successfully
          </h2>
          <p className="text-sm text-gray-600 mb-4">
            Share this invite token with your teammates. Each group has a unique token saved in the database.
          </p>
          <div className="flex items-center gap-2">
            <code className="flex-1 p-3 bg-white rounded-lg border border-blue-200 text-sm break-all font-mono">
              {createdGroup.joinToken}
            </code>
            <button
              type="button"
              onClick={() => copyToken(createdGroup.joinToken)}
              className="p-3 bg-blue-100 rounded-lg hover:bg-blue-200"
            >
              {copied ? <Check className="w-5 h-5 text-blue-700" /> : <Copy className="w-5 h-5 text-blue-700" />}
            </button>
          </div>
          <Link
            to={`/groups/${createdGroup.id}`}
            className="mt-4 inline-block text-blue-600 font-medium text-sm hover:underline"
          >
            Go to my group →
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-lg mx-auto">
      <h1 className="text-2xl font-bold text-blue-900 mb-6">Create your own group & project</h1>
      <p className="text-gray-600 text-sm mb-4">
        Create a new project and group. You become the leader. Share the invite token with peers so they can join.
      </p>
      <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6 space-y-4">
        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
            {error}
          </div>
        )}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Group Name *</label>
          <input
            type="text"
            value={form.name}
            onChange={(e) => update('name', e.target.value)}
            required
            className="w-full px-4 py-3 rounded-lg border border-blue-200 focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Project Title</label>
          <input
            type="text"
            value={form.projectTitle}
            onChange={(e) => update('projectTitle', e.target.value)}
            className="w-full px-4 py-3 rounded-lg border border-blue-200 focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Project Description</label>
          <textarea
            value={form.projectDescription}
            onChange={(e) => update('projectDescription', e.target.value)}
            rows={3}
            className="w-full px-4 py-3 rounded-lg border border-blue-200 focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Domain</label>
          <input
            type="text"
            value={form.projectDomain}
            onChange={(e) => update('projectDomain', e.target.value)}
            placeholder="e.g. Machine Learning"
            className="w-full px-4 py-3 rounded-lg border border-blue-200 focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Tech Stack</label>
          <input
            type="text"
            value={form.projectTechStack}
            onChange={(e) => update('projectTechStack', e.target.value)}
            placeholder="React, Spring Boot, MySQL"
            className="w-full px-4 py-3 rounded-lg border border-blue-200 focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          className="w-full py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? 'Creating...' : 'Create Group'}
        </button>
      </form>
    </div>
  );
}
