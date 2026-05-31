/** Project detail: submissions, NLP summary, meetings, schedule, mentor recommendations. */
import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  getProject,
  updateProgress,
  updateProjectTitle,
  getSubmissionsByProject,
  summarizeReport,
  getSummaries,
  uploadSubmission,
  getRecommendations,
  deleteSubmission,
  downloadSubmission,
  getMeetings,
  addMeeting,
  verifyMeeting,
  getMeetingSchedule,
  proposeMeetingSchedule,
  approveMeetingSchedule,
  counterProposeMeetingSchedule,
  acceptMeetingSchedule,
  proposeNewMeetingSchedule,
} from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { FacultyRecommendationCards } from '../components/recommendations/FacultyRecommendationCards';
import { Button } from '../components/ui/Button';
import { Card, CardContent, CardHeader } from '../components/ui/Card';
import {
  FileText,
  Upload,
  Sparkles,
  Loader2,
  Download,
  Trash2,
  Calendar,
  CheckCircle2,
  Clock,
  ChevronDown,
  ChevronUp,
  CalendarClock,
  ThumbsUp,
  ThumbsDown,
} from 'lucide-react';

const CATEGORIES = ['REPORT', 'RESEARCH_PAPER', 'PPT'];

export function ProjectDetail() {
  const { projectId } = useParams();
  const { isFaculty, isAdmin, isStudent } = useAuth();
  const [project, setProject] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [summaries, setSummaries] = useState([]);
  const [recommendations, setRecommendations] = useState(null);
  const [loading, setLoading] = useState(true);
  const [progressVal, setProgressVal] = useState(0);
  const [uploading, setUploading] = useState(false);
  const [uploadCategory, setUploadCategory] = useState('REPORT');
  const [summarizing, setSummarizing] = useState(false);
  const [error, setError] = useState('');
  const [meetings, setMeetings] = useState([]);
  const [meetingDate, setMeetingDate] = useState('');
  const [meetingDetails, setMeetingDetails] = useState('');
  const [addingMeeting, setAddingMeeting] = useState(false);
  const [showMeetingForm, setShowMeetingForm] = useState(false);
  const [editingTitle, setEditingTitle] = useState(false);
  const [titleInput, setTitleInput] = useState('');
  const [scheduleRequests, setScheduleRequests] = useState([]);
  const [showScheduleForm, setShowScheduleForm] = useState(false);
  const [scheduleProposedDate, setScheduleProposedDate] = useState('');
  const [scheduleNotes, setScheduleNotes] = useState('');
  const [scheduleActionLoading, setScheduleActionLoading] = useState(null);
  const [showCounterForm, setShowCounterForm] = useState(null);
  const [counterDate, setCounterDate] = useState('');
  const [counterNotes, setCounterNotes] = useState('');
  const [proposeNewForId, setProposeNewForId] = useState(null);

  const load = () => {
    if (!projectId) return;
    Promise.all([
      getProject(projectId),
      getSubmissionsByProject(projectId),
      getSummaries(projectId),
      getMeetings(projectId),
      getMeetingSchedule(projectId).catch(() => ({ data: { data: [] } })),
    ])
      .then(([p, s, sum, m, sch]) => {
        setProject(p.data?.data);
        setSubmissions(s.data?.data || []);
        setSummaries(sum.data?.data || []);
        setMeetings(m.data?.data || []);
        setScheduleRequests(sch.data?.data || []);
        setProgressVal(p.data?.data?.progress ?? 0);
      })
      .catch((e) => setError(e.response?.data?.error?.message || 'Failed to load'))
      .finally(() => setLoading(false));
  };

  useEffect(load, [projectId]);

  // Poll summaries when any are PENDING or PROCESSING so user sees when they complete
  const hasPendingSummaries = summaries.some(
    (s) => s.status === 'PENDING' || s.status === 'PROCESSING'
  );
  useEffect(() => {
    if (!projectId || !hasPendingSummaries) return;
    const interval = setInterval(() => {
      getSummaries(projectId)
        .then((res) => setSummaries(res.data?.data || []))
        .catch(() => {
          void 0;
        });
    }, 8000); // every 8 seconds
    return () => clearInterval(interval);
  }, [projectId, hasPendingSummaries]);

  const handleUpdateProgress = async () => {
    try {
      await updateProgress(projectId, progressVal);
      setProject((p) => (p ? { ...p, progress: progressVal } : null));
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Update failed');
    }
  };

  const handleUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      await uploadSubmission(projectId, file, uploadCategory);
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleSummarize = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setSummarizing(true);
    try {
      await summarizeReport(projectId, file);
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Summarization failed');
    } finally {
      setSummarizing(false);
    }
  };

  const handleDownload = async (sub) => {
    try {
      const res = await downloadSubmission(sub.id);
      const blob = new Blob([res.data]);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = sub.originalFilename || 'download';
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      void 0;
    }
  };

  const fetchRecommendations = async () => {
    try {
      const res = await getRecommendations(projectId);
      setRecommendations(res.data?.data);
    } catch {
      void 0;
    }
  };

  const handleAddMeeting = async (e) => {
    e.preventDefault();
    if (!meetingDate) {
      setError('Please select a meeting date');
      return;
    }
    setAddingMeeting(true);
    setError('');
    try {
      await addMeeting(projectId, {
        meetingDate,
        details: meetingDetails.trim() || undefined,
      });
      setMeetingDate('');
      setMeetingDetails('');
      setShowMeetingForm(false);
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Failed to log meeting');
    } finally {
      setAddingMeeting(false);
    }
  };

  const handleUpdateTitle = async () => {
    const t = titleInput?.trim();
    if (!t) return;
    try {
      await updateProjectTitle(projectId, t);
      setProject((p) => (p ? { ...p, title: t } : null));
      setEditingTitle(false);
      setTitleInput('');
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Failed to update title');
    }
  };

  const handleVerifyMeeting = async (meetingId) => {
    try {
      await verifyMeeting(projectId, meetingId);
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Verification failed');
    }
  };

  const handleProposeSchedule = async (e) => {
    e.preventDefault();
    if (!scheduleProposedDate) return;
    setScheduleActionLoading('propose');
    setError('');
    try {
      await proposeMeetingSchedule(projectId, {
        proposedDate: scheduleProposedDate,
        notes: scheduleNotes.trim() || undefined,
      });
      setScheduleProposedDate('');
      setScheduleNotes('');
      setShowScheduleForm(false);
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Failed to propose meeting');
    } finally {
      setScheduleActionLoading(null);
    }
  };

  const handleApproveSchedule = async (id) => {
    setScheduleActionLoading(id);
    setError('');
    try {
      await approveMeetingSchedule(projectId, id);
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Failed to approve');
    } finally {
      setScheduleActionLoading(null);
    }
  };

  const handleCounterPropose = async (id) => {
    if (!counterDate) return;
    setScheduleActionLoading(id);
    setError('');
    try {
      await counterProposeMeetingSchedule(projectId, id, {
        facultyCounterDate: counterDate,
        facultyCounterNotes: counterNotes.trim() || undefined,
      });
      setShowCounterForm(null);
      setCounterDate('');
      setCounterNotes('');
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Failed to suggest alternative');
    } finally {
      setScheduleActionLoading(null);
    }
  };

  const handleAcceptSchedule = async (id) => {
    setScheduleActionLoading(id);
    setError('');
    try {
      await acceptMeetingSchedule(projectId, id);
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Failed to accept');
    } finally {
      setScheduleActionLoading(null);
    }
  };

  const handleProposeNewSchedule = async (e, id) => {
    e.preventDefault();
    if (!scheduleProposedDate) return;
    setScheduleActionLoading(id);
    setError('');
    try {
      await proposeNewMeetingSchedule(projectId, id, {
        proposedDate: scheduleProposedDate,
        notes: scheduleNotes.trim() || undefined,
      });
      setScheduleProposedDate('');
      setScheduleNotes('');
      setProposeNewForId(null);
      load();
    } catch (e) {
      setError(e.response?.data?.error?.message || 'Failed to propose new date');
    } finally {
      setScheduleActionLoading(null);
    }
  };

  if (loading) {
    return (
      <div className="space-y-8 animate-fade-in">
        <div className="h-8 w-3/4 max-w-md rounded-lg bg-blue-200/50 animate-pulse" />
        <div className="h-24 rounded-2xl bg-white/80 border border-blue-100 animate-pulse" />
        <div className="grid gap-6">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-40 rounded-2xl bg-white/80 border border-blue-100 animate-pulse" />
          ))}
        </div>
        <div className="flex justify-center py-12">
          <div className="animate-spin w-10 h-10 border-2 border-blue-500 border-t-transparent rounded-full" />
        </div>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="bg-red-50 rounded-xl p-6 text-red-700">
        {error || 'Project not found'}
      </div>
    );
  }

  const canEditTitle = isStudent || isAdmin;

  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 animate-fade-in-up">
        <div className="flex-1">
          {editingTitle ? (
            <div className="flex flex-wrap items-center gap-2">
              <input
                type="text"
                value={titleInput}
                onChange={(e) => setTitleInput(e.target.value)}
                placeholder="Project title"
                className="px-4 py-2 rounded-lg border border-blue-200 text-blue-900 text-xl font-bold w-full max-w-md"
                autoFocus
              />
              <button
                onClick={handleUpdateTitle}
                disabled={!titleInput?.trim()}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                Save
              </button>
              <button
                onClick={() => { setEditingTitle(false); setTitleInput(''); }}
                className="px-4 py-2 text-gray-600 hover:underline"
              >
                Cancel
              </button>
            </div>
          ) : (
            <>
              <h1 className="text-2xl font-bold text-blue-900">{project.title}</h1>
              {canEditTitle && (
                <button
                  type="button"
                  onClick={() => { setTitleInput(project.title); setEditingTitle(true); }}
                  className="text-sm text-blue-600 hover:text-blue-700 hover:underline mt-1"
                >
                  Change project name
                </button>
              )}
            </>
          )}
          <p className="text-gray-600 mt-1">{project.domain}</p>
        </div>
        {project.groupId && (
          <Link
            to={`/groups/${project.groupId}`}
            className="px-4 py-2 bg-blue-100 text-blue-800 rounded-lg hover:bg-blue-200 transition-transform hover:scale-105"
          >
            View Group
          </Link>
        )}
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 animate-fade-in">{error}</div>
      )}

      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6 animate-fade-in-up hover-lift" style={{ animationDelay: '0.05s', opacity: 0 }}>
        <h2 className="font-semibold text-blue-900 mb-4">Description</h2>
        <p className="text-gray-700">{project.description || '—'}</p>
        {project.techStack && (
          <p className="mt-2 text-sm text-blue-600">Tech: {project.techStack}</p>
        )}
      </div>

      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden animate-fade-in-up hover-lift" style={{ animationDelay: '0.1s', opacity: 0 }}>
        <div className="p-4 bg-blue-50 border-b flex items-center justify-between flex-wrap gap-4">
          <span className="font-semibold text-blue-900 flex items-center gap-2">
            <Calendar className="w-5 h-5" />
            Meeting Log
          </span>
          {isStudent && project.hasMentor && (
            <button
              type="button"
              onClick={() => setShowMeetingForm(!showMeetingForm)}
              className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
            >
              {showMeetingForm ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              {showMeetingForm ? 'Cancel' : 'Log Meeting'}
            </button>
          )}
        </div>
        <div className="p-6">
          {isStudent && showMeetingForm && (
            <form onSubmit={handleAddMeeting} className="mb-6 p-4 rounded-xl bg-blue-50/80 border border-blue-100">
              <p className="text-sm text-blue-700 mb-4">Record a meeting you had with your mentor.</p>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-blue-900 mb-1">Meeting Date *</label>
                  <input
                    type="date"
                    value={meetingDate}
                    onChange={(e) => setMeetingDate(e.target.value)}
                    required
                    className="w-full px-3 py-2 rounded-lg border border-blue-200 text-blue-900"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-blue-900 mb-1">Details / Notes</label>
                  <textarea
                    value={meetingDetails}
                    onChange={(e) => setMeetingDetails(e.target.value)}
                    placeholder="e.g. Discussed implementation approach, next steps..."
                    rows={3}
                    className="w-full px-3 py-2 rounded-lg border border-blue-200 text-blue-900"
                  />
                </div>
                <button
                  type="submit"
                  disabled={addingMeeting || !meetingDate}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 inline-flex items-center gap-2"
                >
                  {addingMeeting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Calendar className="w-4 h-4" />}
                  Log Meeting
                </button>
              </div>
            </form>
          )}
          {meetings.length === 0 ? (
            <p className="text-gray-500">
              No meetings logged yet.
              {isStudent && project.hasMentor ? ' Click "Log Meeting" to record your first meeting.' : ''}
            </p>
          ) : (
            <ul className="space-y-4">
              {meetings.map((m, i) => (
                <li
                  key={m.id}
                  className="p-4 rounded-xl border border-blue-100 bg-white flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 animate-slide-in-right transition-transform hover:border-blue-200"
                  style={{ animationDelay: `${0.02 * i}s`, opacity: 0 }}
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-3 flex-wrap">
                      <span className="font-medium text-blue-900">
                        {m.meetingDate ? new Date(m.meetingDate + 'T00:00:00').toLocaleDateString(undefined, {
                          weekday: 'short',
                          year: 'numeric',
                          month: 'short',
                          day: 'numeric',
                        }) : '—'}
                      </span>
                      {m.verified ? (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          Verified
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">
                          <Clock className="w-3.5 h-3.5" />
                          Pending verification
                        </span>
                      )}
                    </div>
                    {m.details && <p className="mt-2 text-sm text-gray-600">{m.details}</p>}
                    <p className="text-xs text-gray-500 mt-2">
                      Logged by {m.loggedByName || '—'}
                      {m.verifiedAt && (
                        <> • Verified {new Date(m.verifiedAt).toLocaleString()}</>
                      )}
                    </p>
                  </div>
                  {(isFaculty || isAdmin) && !m.verified && (
                    <button
                      onClick={() => handleVerifyMeeting(m.id)}
                      className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 text-sm shrink-0"
                    >
                      <CheckCircle2 className="w-4 h-4" />
                      Verify
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {project.hasMentor && !isAdmin && (
        <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden animate-fade-in-up hover-lift" style={{ animationDelay: '0.12s', opacity: 0 }}>
          <div className="p-4 bg-indigo-50 border-b flex items-center justify-between flex-wrap gap-4">
            <span className="font-semibold text-blue-900 flex items-center gap-2">
              <CalendarClock className="w-5 h-5" />
              Schedule Future Meeting
            </span>
            {isStudent && (
              <button
                type="button"
                onClick={() => setShowScheduleForm(!showScheduleForm)}
                className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 text-sm"
              >
                {showScheduleForm ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                {showScheduleForm ? 'Cancel' : 'Propose Date'}
              </button>
            )}
          </div>
          <div className="p-6">
            <p className="text-sm text-gray-600 mb-4">
              Propose a date for an upcoming meeting. Your mentor can approve or suggest when they&apos;re free. Both must agree before the meeting is scheduled.
            </p>
            {isStudent && showScheduleForm && (
              <form onSubmit={handleProposeSchedule} className="mb-6 p-4 rounded-xl bg-indigo-50/80 border border-indigo-100">
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-blue-900 mb-1">Proposed Date *</label>
                    <input
                      type="date"
                      value={scheduleProposedDate}
                      onChange={(e) => setScheduleProposedDate(e.target.value)}
                      required
                      min={new Date().toISOString().slice(0, 10)}
                      className="w-full px-3 py-2 rounded-lg border border-indigo-200 text-blue-900"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-blue-900 mb-1">Notes (optional)</label>
                    <input
                      type="text"
                      value={scheduleNotes}
                      onChange={(e) => setScheduleNotes(e.target.value)}
                      placeholder="e.g. Discuss phase 2 implementation"
                      className="w-full px-3 py-2 rounded-lg border border-indigo-200 text-blue-900"
                    />
                  </div>
                  <button
                    type="submit"
                    disabled={scheduleActionLoading === 'propose' || !scheduleProposedDate}
                    className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 inline-flex items-center gap-2"
                  >
                    {scheduleActionLoading === 'propose' ? <Loader2 className="w-4 h-4 animate-spin" /> : <Calendar className="w-4 h-4" />}
                    Propose Meeting
                  </button>
                </div>
              </form>
            )}
            {scheduleRequests.length === 0 ? (
              <p className="text-gray-500">No meeting schedule requests yet. Propose a date to get started.</p>
            ) : (
              <ul className="space-y-4">
                {scheduleRequests.map((sr) => (
                  <li
                    key={sr.id}
                    className="p-4 rounded-xl border border-indigo-100 bg-white flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4"
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-3 flex-wrap">
                        <span className="font-medium text-blue-900">
                          Proposed: {sr.proposedDate ? new Date(sr.proposedDate + 'T00:00:00').toLocaleDateString(undefined, { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' }) : '—'}
                        </span>
                        <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${
                          sr.status === 'APPROVED' ? 'bg-emerald-100 text-emerald-800' :
                          sr.status === 'PENDING_FACULTY' ? 'bg-amber-100 text-amber-800' :
                          'bg-blue-100 text-blue-800'
                        }`}>
                          {sr.status === 'APPROVED' ? '✓ Scheduled' : sr.status === 'PENDING_FACULTY' ? 'Awaiting mentor' : 'Your response needed'}
                        </span>
                      </div>
                      {sr.facultyCounterDate && (
                        <p className="mt-2 text-sm text-indigo-700">
                          Mentor suggests: <strong>{new Date(sr.facultyCounterDate + 'T00:00:00').toLocaleDateString()}</strong>
                          {sr.facultyCounterNotes && ` — ${sr.facultyCounterNotes}`}
                        </p>
                      )}
                      {sr.agreedDate && (
                        <p className="mt-2 text-sm font-medium text-emerald-700">
                          Scheduled: {new Date(sr.agreedDate + 'T00:00:00').toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' })}
                        </p>
                      )}
                      {sr.notes && <p className="mt-1 text-sm text-gray-600">{sr.notes}</p>}
                      <p className="text-xs text-gray-500 mt-2">Proposed by {sr.proposedByName || '—'}</p>
                    </div>
                    <div className="flex flex-wrap gap-2 shrink-0">
                      {isFaculty && sr.status === 'PENDING_FACULTY' && (
                        <>
                          <button
                            onClick={() => handleApproveSchedule(sr.id)}
                            disabled={scheduleActionLoading === sr.id}
                            className="inline-flex items-center gap-1 px-3 py-1.5 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 text-sm"
                          >
                            {scheduleActionLoading === sr.id ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <ThumbsUp className="w-3.5 h-3.5" />}
                            Approve
                          </button>
                          {showCounterForm === sr.id ? (
                            <div className="flex flex-col gap-2 p-3 rounded-lg bg-indigo-50 border border-indigo-100">
                              <input
                                type="date"
                                value={counterDate}
                                onChange={(e) => setCounterDate(e.target.value)}
                                min={new Date().toISOString().slice(0, 10)}
                                className="px-2 py-1 rounded border border-indigo-200 text-sm"
                                placeholder="Date"
                              />
                              <input
                                type="text"
                                value={counterNotes}
                                onChange={(e) => setCounterNotes(e.target.value)}
                                placeholder="I'm free on this date..."
                                className="px-2 py-1 rounded border border-indigo-200 text-sm"
                              />
                              <div className="flex gap-2">
                                <button onClick={() => handleCounterPropose(sr.id)} disabled={!counterDate || scheduleActionLoading === sr.id} className="px-2 py-1 bg-indigo-600 text-white rounded text-xs">Suggest</button>
                                <button onClick={() => { setShowCounterForm(null); setCounterDate(''); setCounterNotes(''); }} className="px-2 py-1 text-gray-600 text-xs">Cancel</button>
                              </div>
                            </div>
                          ) : (
                            <button
                              onClick={() => setShowCounterForm(sr.id)}
                              className="inline-flex items-center gap-1 px-3 py-1.5 bg-indigo-100 text-indigo-800 rounded-lg hover:bg-indigo-200 text-sm"
                            >
                              <ThumbsDown className="w-3.5 h-3.5" />
                              Suggest different date
                            </button>
                          )}
                        </>
                      )}
                      {isStudent && sr.status === 'PENDING_STUDENT' && sr.facultyCounterDate && (
                        <>
                          <button
                            onClick={() => handleAcceptSchedule(sr.id)}
                            disabled={scheduleActionLoading === sr.id}
                            className="inline-flex items-center gap-1 px-3 py-1.5 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 text-sm"
                          >
                            {scheduleActionLoading === sr.id ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <ThumbsUp className="w-3.5 h-3.5" />}
                            Accept
                          </button>
                          {proposeNewForId === sr.id ? (
                            <form onSubmit={(e) => handleProposeNewSchedule(e, sr.id)} className="flex flex-col gap-2 p-3 rounded-lg bg-indigo-50 border border-indigo-100">
                              <input
                                type="date"
                                value={scheduleProposedDate}
                                onChange={(e) => setScheduleProposedDate(e.target.value)}
                                min={new Date().toISOString().slice(0, 10)}
                                className="px-2 py-1 rounded border border-indigo-200 text-sm"
                              />
                              <div className="flex gap-2">
                                <button type="submit" disabled={!scheduleProposedDate || scheduleActionLoading === sr.id} className="px-3 py-1 bg-indigo-600 text-white rounded text-sm">Propose</button>
                                <button type="button" onClick={() => { setProposeNewForId(null); setScheduleProposedDate(''); }} className="px-3 py-1 text-gray-600 text-sm">Cancel</button>
                              </div>
                            </form>
                          ) : (
                            <button
                              type="button"
                              onClick={() => setProposeNewForId(sr.id)}
                              className="inline-flex items-center gap-1 px-3 py-1.5 bg-indigo-100 text-indigo-800 rounded-lg hover:bg-indigo-200 text-sm"
                            >
                              Propose different date
                            </button>
                          )}
                        </>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}

      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6 animate-fade-in-up hover-lift" style={{ animationDelay: '0.15s', opacity: 0 }}>
        <h2 className="font-semibold text-blue-900 mb-4">Progress</h2>
        <div className="flex items-center gap-4">
          <input
            type="range"
            min={0}
            max={100}
            value={progressVal}
            onChange={(e) => setProgressVal(parseInt(e.target.value))}
            className="flex-1 accent-blue-600 transition-opacity"
          />
          <span className="text-blue-700 font-medium w-12">{progressVal}%</span>
          <button
            onClick={handleUpdateProgress}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-transform hover:scale-105 active:scale-95"
          >
            Save
          </button>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden animate-fade-in-up hover-lift" style={{ animationDelay: '0.2s', opacity: 0 }}>
        <div className="p-4 bg-blue-50 border-b flex items-center justify-between">
          <span className="font-semibold text-blue-900 flex items-center gap-2">
            <FileText className="w-5 h-5" />
            Submissions
          </span>
          <div className="flex items-center gap-2">
            <select
              value={uploadCategory}
              onChange={(e) => setUploadCategory(e.target.value)}
              className="rounded-lg border border-blue-200 text-sm"
            >
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>{c.replace('_', ' ')}</option>
              ))}
            </select>
            <label className="cursor-pointer">
              <input
                type="file"
                className="hidden"
                onChange={handleUpload}
                disabled={uploading}
              />
              <span className="inline-flex items-center gap-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm">
                {uploading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Upload className="w-4 h-4" />}
                Upload
              </span>
            </label>
          </div>
        </div>
        <div className="p-6">
          {submissions.length === 0 ? (
            <p className="text-gray-500">No submissions yet</p>
          ) : (
            <ul className="space-y-3">
              {submissions.map((s, i) => (
                <li
                  key={s.id}
                  className="flex items-center justify-between p-3 rounded-lg border border-blue-100 animate-slide-in-right transition-colors hover:border-blue-200 hover:bg-blue-50/30"
                  style={{ animationDelay: `${0.02 * i}s`, opacity: 0 }}
                >
                  <div>
                    <span className="font-medium">{s.originalFilename}</span>
                    <span className="text-sm text-gray-500 ml-2">({s.category})</span>
                    <p className="text-xs text-gray-500 mt-1">{s.submittedByName} • {s.submittedAt ? new Date(s.submittedAt).toLocaleString() : ''}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleDownload(s)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg"
                    >
                      <Download className="w-4 h-4" />
                    </button>
                    {(isFaculty || isAdmin) && (
                      <button
                        onClick={async () => {
                          try {
                            await deleteSubmission(s.id);
                            load();
                          } catch {
                            void 0;
                          }
                        }}
                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden animate-fade-in-up hover-lift" style={{ animationDelay: '0.25s', opacity: 0 }}>
        <div className="p-4 bg-blue-50 border-b flex items-center justify-between">
          <span className="font-semibold text-blue-900 flex items-center gap-2">
            <Sparkles className="w-5 h-5" />
            Report Summaries (NLP)
          </span>
          <label className="cursor-pointer">
            <input
              type="file"
              accept=".pdf"
              className="hidden"
              onChange={handleSummarize}
              disabled={summarizing}
            />
            <span className="inline-flex items-center gap-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm">
              {summarizing ? <Loader2 className="w-4 h-4 animate-spin" /> : <Sparkles className="w-4 h-4" />}
              Summarize PDF
            </span>
          </label>
        </div>
        <div className="p-6 space-y-4">
          {hasPendingSummaries && (
            <p className="text-sm text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2 flex items-center gap-2">
              <Loader2 className="w-4 h-4 animate-spin shrink-0" />
              Summarization in progress. This usually takes 2–5 minutes. The list below will update automatically.
            </p>
          )}
          {summaries.length === 0 ? (
            <p className="text-gray-500">No summaries yet. Upload a report PDF to generate a summary.</p>
          ) : (
            summaries.map((s, i) => (
              <div
                key={s.id}
                className="p-4 rounded-xl border border-blue-100 bg-blue-50/50 animate-scale-in transition-colors hover:border-blue-200"
                style={{ animationDelay: `${0.02 * i}s`, opacity: 0 }}
              >
                <p className="text-sm text-blue-600 mb-2">
                  {s.originalFilename}
                  <span className={
                    s.status === 'DONE' ? 'text-green-600' :
                    s.status === 'FAILED' ? 'text-red-600' :
                    'text-amber-600'
                  }>
                    {' • '}
                    {s.status === 'PENDING' && 'Queued'}
                    {s.status === 'PROCESSING' && 'In progress'}
                    {s.status === 'DONE' && 'Done'}
                    {s.status === 'FAILED' && 'Failed'}
                  </span>
                </p>
                {s.status === 'DONE' && s.generatedSummary && (
                  <pre className="text-sm text-gray-700 whitespace-pre-wrap font-sans">{s.generatedSummary}</pre>
                )}
                {(s.status === 'PENDING' || s.status === 'PROCESSING') && (
                  <p className="text-amber-600 flex items-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin shrink-0" />
                    {s.status === 'PENDING' ? 'Queued — summary will be ready in a few minutes.' : 'Generating summary… Usually 2–5 min. This page updates automatically.'}
                  </p>
                )}
                {s.status === 'FAILED' && (
                  <p className="text-red-600">{s.errorMessage || 'Summarization failed.'}</p>
                )}
              </div>
            ))
          )}
        </div>
      </div>

      {(isAdmin || isFaculty) && (
        <Card variant="glass" className="animate-fade-in-up hover-lift" style={{ animationDelay: '0.3s', opacity: 0 }}>
          <CardHeader>
            <h2 className="text-base font-semibold text-mentor-text">Mentor recommendations</h2>
            <p className="mt-1 text-sm text-mentor-muted">
              AI-ranked faculty matches for this project (similarity scores).
            </p>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button type="button" variant="primary" size="sm" onClick={fetchRecommendations}>
              Get recommendations
            </Button>
            {recommendations?.recommendedFaculty?.length > 0 && (
              <FacultyRecommendationCards items={recommendations.recommendedFaculty} />
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
