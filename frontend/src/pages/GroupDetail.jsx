/** Group detail: members, join tokens, submissions, request faculty mentorship. */
import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getGroup, getSubmissionsByGroup, getFacultyList, requestFacultyMentorship } from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { Users, FileText, Copy, Check, Crown, User, GraduationCap, Send } from 'lucide-react';

export function GroupDetail() {
  const { groupId } = useParams();
  const { isStudent, isFaculty, isAdmin } = useAuth();
  const [group, setGroup] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [facultyList, setFacultyList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);
  const [requesting, setRequesting] = useState(false);
  const [requestError, setRequestError] = useState('');
  const [selectedFaculty, setSelectedFaculty] = useState(null);
  const [requestSent, setRequestSent] = useState(false);

  useEffect(() => {
    if (!groupId) return;
    Promise.all([getGroup(groupId), getSubmissionsByGroup(groupId)])
      .then(([g, s]) => {
        setGroup(g.data?.data);
        setSubmissions(s.data?.data || []);
      })
      .catch(() => setGroup(null))
      .finally(() => setLoading(false));
  }, [groupId]);

  useEffect(() => {
    if (isStudent && group && !group.mentorName) {
      getFacultyList(true)
        .then((res) => setFacultyList(res.data?.data || []))
        .catch(() => setFacultyList([]));
    }
  }, [isStudent, group]);

  const copyToken = (token) => {
    navigator.clipboard.writeText(token);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleRequestMentor = async () => {
    if (!selectedFaculty || !groupId) return;
    setRequesting(true);
    setRequestError('');
    try {
      await requestFacultyMentorship(groupId, {
        facultyId: selectedFaculty,
        projectTopic: group?.projectTitle || undefined,
        projectDescription: group?.projectDescription || undefined,
      });
      setRequestSent(true);
    } catch (e) {
      setRequestError(e.response?.data?.error?.message || 'Request failed');
    } finally {
      setRequesting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <div className="animate-spin w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!group) {
    return (
      <div className="bg-red-50 rounded-xl p-6 text-red-700">
        Group not found
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-blue-900">{group.name}</h1>
          <p className="text-gray-600 mt-1">{group.projectTitle}</p>
        </div>
        {group.projectId && (
          <Link
            to={`/projects/${group.projectId}`}
            className="px-4 py-2 bg-blue-100 text-blue-800 rounded-lg hover:bg-blue-200"
          >
            View Project
          </Link>
        )}
      </div>

      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6">
        <h2 className="font-semibold text-blue-900 mb-4 flex items-center gap-2">
          <Users className="w-5 h-5" />
          Members
        </h2>
        <p className="text-blue-600 mb-4">{group.memberCount} member{group.memberCount !== 1 ? 's' : ''} in this group</p>
        {group.members && group.members.length > 0 ? (
          <ul className="space-y-2">
            {group.members.map((m) => (
              <li
                key={m.userId}
                className="flex items-center gap-3 p-3 rounded-lg bg-blue-50/80 border border-blue-100"
              >
                <span className="font-medium text-blue-900">{m.fullName}</span>
                <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-700">
                  {m.isLeader ? (
                    <>
                      <Crown className="w-3.5 h-3.5" />
                      Leader
                    </>
                  ) : (
                    <>
                      <User className="w-3.5 h-3.5" />
                      Member
                    </>
                  )}
                </span>
              </li>
            ))}
          </ul>
        ) : null}
        {isStudent && !group.mentorName && facultyList.length > 0 && !requestSent && (
          <div className="mt-4 pt-4 border-t border-blue-200">
            <p className="text-sm font-medium text-blue-900 mb-2">Request a mentor</p>
            <div className="flex flex-wrap gap-2 items-center">
              <select
                value={selectedFaculty || ''}
                onChange={(e) => setSelectedFaculty(e.target.value ? Number(e.target.value) : null)}
                className="px-3 py-2 rounded-lg border border-blue-200 text-blue-900"
              >
                <option value="">Select faculty</option>
                {facultyList.map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.name} ({f.expertise || f.department}) {!f.available && '(full)'}
                  </option>
                ))}
              </select>
              <button
                onClick={handleRequestMentor}
                disabled={!selectedFaculty || requesting}
                className="inline-flex items-center gap-2 px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg disabled:opacity-50"
              >
                {requesting ? 'Sending...' : (
                  <>
                    <Send className="w-4 h-4" />
                    Request
                  </>
                )}
              </button>
            </div>
            {requestError && <p className="mt-2 text-sm text-red-600">{requestError}</p>}
          </div>
        )}
        {requestSent && (
          <div className="mt-4 p-3 rounded-lg bg-blue-50 text-blue-800 text-sm">
            Mentorship request sent. The faculty will be notified.
          </div>
        )}
        {group.mentorName && (
          <div className="mt-4 pt-4 border-t border-blue-200">
            <p className="text-sm text-gray-600 mb-2">Mentor</p>
            <div className="flex items-center gap-3 p-3 rounded-lg bg-amber-50/80 border border-amber-100">
              <GraduationCap className="w-5 h-5 text-amber-600" />
              <span className="font-medium text-blue-900">{group.mentorName}</span>
              <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">
                Mentor
              </span>
            </div>
          </div>
        )}
        {group.projectDescription && (
          <p className="mt-4 text-sm text-gray-600">{group.projectDescription}</p>
        )}
      </div>

      {(group.joinToken || group.mentorJoinToken) && (
        <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6">
          <h2 className="font-semibold text-blue-900 mb-4">Invite Tokens</h2>
          <div className="space-y-3">
            {group.joinToken && (
              <div className="flex items-center gap-2">
                <code className="flex-1 p-2 bg-gray-100 rounded text-sm break-all">
                  {group.joinToken}
                </code>
                <button
                  onClick={() => copyToken(group.joinToken)}
                  className="p-2 bg-blue-100 rounded-lg hover:bg-blue-200"
                >
                  {copied ? <Check className="w-4 h-4 text-blue-700" /> : <Copy className="w-4 h-4 text-blue-700" />}
                </button>
                <span className="text-sm text-gray-600">Students</span>
              </div>
            )}
            {group.mentorJoinToken && (isFaculty || isAdmin) && (
              <div className="flex items-center gap-2">
                <code className="flex-1 p-2 bg-gray-100 rounded text-sm break-all">
                  {group.mentorJoinToken}
                </code>
                <button
                  onClick={() => copyToken(group.mentorJoinToken)}
                  className="p-2 bg-blue-100 rounded-lg hover:bg-blue-200"
                >
                  {copied ? <Check className="w-4 h-4 text-blue-700" /> : <Copy className="w-4 h-4 text-blue-700" />}
                </button>
                <span className="text-sm text-gray-600">Mentor</span>
              </div>
            )}
          </div>
        </div>
      )}

      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden">
        <div className="p-4 bg-blue-50 border-b flex items-center gap-2">
          <FileText className="w-5 h-5 text-blue-600" />
          <span className="font-semibold text-blue-900">Submissions</span>
        </div>
        <div className="p-6">
          {submissions.length === 0 ? (
            <p className="text-gray-500">No submissions</p>
          ) : (
            <ul className="space-y-2">
              {submissions.map((s) => (
                <li key={s.id} className="flex justify-between p-3 rounded-lg border border-blue-100">
                  <span>{s.originalFilename} ({s.category})</span>
                  {group.projectId && (
                    <Link
                      to={`/projects/${group.projectId}`}
                      className="text-blue-600 text-sm hover:underline"
                    >
                      View project
                    </Link>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
