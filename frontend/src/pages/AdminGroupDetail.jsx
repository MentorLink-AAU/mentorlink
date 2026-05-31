/** Admin: view group detail, members, project, mentor. */
import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getAdminGroupDetail } from '../lib/api';
import { ArrowLeft, Users, GraduationCap, Calendar, FileText } from 'lucide-react';

export function AdminGroupDetail() {
  const { groupId } = useParams();
  const [group, setGroup] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!groupId) return;
    getAdminGroupDetail(groupId)
      .then((res) => setGroup(res.data?.data))
      .catch((e) => setError(e.response?.data?.error?.message || 'Failed to load'))
      .finally(() => setLoading(false));
  }, [groupId]);

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <div className="animate-spin w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  if (error || !group) {
    return (
      <div className="space-y-4">
        <Link to="/dashboard/admin" className="inline-flex items-center gap-2 text-blue-600 hover:underline">
          <ArrowLeft className="w-4 h-4" /> Back to Admin Dashboard
        </Link>
        <div className="bg-red-50 rounded-xl p-6 text-red-700">
          {error || 'Group not found'}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <Link
          to="/dashboard/admin"
          className="inline-flex items-center gap-2 text-blue-600 hover:underline"
        >
          <ArrowLeft className="w-4 h-4" /> Back to Admin Dashboard
        </Link>
      </div>

      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden">
        <div className="p-6 border-b border-blue-100">
          <h1 className="text-2xl font-bold text-blue-900">{group.projectTitle || 'Project'}</h1>
          <p className="text-gray-600 mt-1">Group: {group.groupName}</p>
          {group.projectDescription && (
            <p className="text-gray-600 mt-2 text-sm">{group.projectDescription}</p>
          )}
          <div className="mt-4 flex items-center gap-4">
            <span className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-100 text-blue-800 text-sm font-medium">
              Progress: {group.progress}%
            </span>
          </div>
        </div>

        <div className="grid md:grid-cols-2 gap-6 p-6">
          {/* Group Members */}
          <div className="bg-blue-50/50 rounded-xl p-6 border border-blue-100">
            <h2 className="flex items-center gap-2 font-semibold text-blue-900 mb-4">
              <Users className="w-5 h-5" />
              Group Members ({group.memberCount})
            </h2>
            <ul className="space-y-4">
              {(group.members || []).map((m) => (
                <li
                  key={m.userId}
                  className="flex items-center gap-3 p-3 bg-white rounded-lg border border-blue-100"
                >
                  <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                    <Users className="w-5 h-5 text-blue-600" />
                  </div>
                  <div>
                    <p className="font-medium text-blue-900">
                      {m.fullName || m.email} {m.isLeader && '(Leader)'}
                    </p>
                    <p className="text-sm text-gray-600">{m.email}</p>
                  </div>
                </li>
              ))}
            </ul>
          </div>

          {/* Faculty Mentor */}
          <div className="bg-blue-50/50 rounded-xl p-6 border border-blue-100">
            <h2 className="flex items-center gap-2 font-semibold text-blue-900 mb-4">
              <GraduationCap className="w-5 h-5" />
              Faculty Mentor
            </h2>
            {group.mentorName || group.mentorEmail ? (
              <div className="flex items-center gap-3 p-4 bg-white rounded-lg border border-blue-100">
                <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                  <GraduationCap className="w-5 h-5 text-blue-600" />
                </div>
                <div>
                  <p className="font-medium text-blue-900">{group.mentorName || group.mentorEmail}</p>
                  {group.mentorEmail && (
                    <p className="text-sm text-gray-600">{group.mentorEmail}</p>
                  )}
                </div>
              </div>
            ) : (
              <p className="text-gray-500 p-4">No mentor assigned yet</p>
            )}

            {/* Last Meeting */}
            {group.lastMeetingDate && (
              <div className="mt-6">
                <h3 className="flex items-center gap-2 font-medium text-blue-900 mb-2">
                  <Calendar className="w-4 h-4" />
                  Last Meeting
                </h3>
                <div className="p-4 bg-white rounded-lg border border-blue-100 space-y-1">
                  <p className="text-sm">
                    {new Date(group.lastMeetingDate + 'T00:00:00').toLocaleDateString(undefined, {
                      weekday: 'short',
                      year: 'numeric',
                      month: 'short',
                      day: 'numeric',
                    })}
                  </p>
                  <span
                    className={`inline-flex px-2 py-0.5 rounded text-xs font-medium ${
                      group.lastMeetingVerified ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {group.lastMeetingVerified ? 'Verified' : 'Pending verification'}
                  </span>
                  {group.lastMeetingDetails && (
                    <p className="text-sm text-gray-600 mt-2">{group.lastMeetingDetails}</p>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="p-6 border-t border-blue-100">
          <Link
            to={`/groups/${group.groupId}`}
            className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            <FileText className="w-4 h-4" /> View full group page
          </Link>
        </div>
      </div>
    </div>
  );
}
