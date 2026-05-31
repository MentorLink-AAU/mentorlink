/** Admin: auto-group from Excel or leftover students, assign faculty. */
import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import {
  autoGroupFromExcel,
  autoGroupFromLeftover,
  getStudentsWithoutGroup,
  getAdminDeadlines,
  downloadLeftoverStudentsExcel,
  downloadFacultyExcel,
} from '../lib/api';
import { Users, FileSpreadsheet, Calendar, CheckCircle2, AlertCircle, Download } from 'lucide-react';
import { Button } from '../components/ui/Button';

export function AdminAutoGroup() {
  const studentsFileRef = useRef(null);
  const facultyFileRef = useRef(null);
  const [studentsFile, setStudentsFile] = useState(null);
  const [facultyFile, setFacultyFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loadingOneClick, setLoadingOneClick] = useState(false);
  const [result, setResult] = useState(null);
  const [leftoverCount, setLeftoverCount] = useState(0);
  const [deadlinePassed, setDeadlinePassed] = useState(false);
  const [deadlineDate, setDeadlineDate] = useState(null);
  const [loadingStatus, setLoadingStatus] = useState(true);

  useEffect(() => {
    Promise.all([getStudentsWithoutGroup(), getAdminDeadlines()])
      .then(([studentsRes, deadlinesRes]) => {
        const list = studentsRes.data?.data || [];
        setLeftoverCount(Array.isArray(list) ? list.length : 0);
        const deadlines = deadlinesRes.data?.data || [];
        const groupDeadline = deadlines.find((d) => d.type === 'GROUP_FORMATION');
        if (groupDeadline) {
          const due = new Date(groupDeadline.dueDate);
          setDeadlineDate(due);
          setDeadlinePassed(new Date() > due);
        } else {
          setDeadlinePassed(false);
          setDeadlineDate(null);
        }
      })
      .catch(() => {
        void 0;
      })
      .finally(() => setLoadingStatus(false));
  }, []);

  const handleOneClick = async () => {
    setLoadingOneClick(true);
    setResult(null);
    try {
      const res = await autoGroupFromLeftover();
      const data = res.data?.data;
      setResult(data);
      if (data?.groupsCreated > 0) {
        setLeftoverCount((c) => Math.max(0, c - (data.studentsGrouped || 0)));
      }
    } catch (err) {
      setResult({ error: err.response?.data?.error?.message || 'Failed to auto-group' });
    } finally {
      setLoadingOneClick(false);
    }
  };

  const handleDownloadLeftover = async () => {
    try {
      const res = await downloadLeftoverStudentsExcel();
      const blob = new Blob([res.data]);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'leftover-students.xlsx';
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      void 0;
    }
  };

  const handleDownloadFaculty = async () => {
    try {
      const res = await downloadFacultyExcel();
      const blob = new Blob([res.data]);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'faculty-list.xlsx';
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      void 0;
    }
  };

  const handleExcelSubmit = async (e) => {
    e.preventDefault();
    if (!studentsFile) return;
    setLoading(true);
    setResult(null);
    try {
      const res = await autoGroupFromExcel(studentsFile, facultyFile || undefined);
      setResult(res.data?.data);
      if (res.data?.data?.groupsCreated > 0) {
        setLeftoverCount((c) => Math.max(0, c - (res.data?.data?.studentsGrouped || 0)));
      }
    } catch (err) {
      setResult({ error: err.response?.data?.error?.message || 'Failed to auto-group' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-8 max-w-2xl">
      <div>
        <h1 className="text-2xl font-bold text-blue-900">Auto Group Formation</h1>
        <p className="text-gray-600 mt-1">
          Group leftover students using cosine similarity and assign faculty with available slots.
        </p>
      </div>

      {loadingStatus ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full" />
        </div>
      ) : (
        <>
          <div className="grid sm:grid-cols-2 gap-4">
            <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6">
              <div className="flex items-center gap-3 mb-2">
                <Users className="w-6 h-6 text-blue-600" />
                <span className="font-semibold text-blue-900">Leftover students</span>
              </div>
              <p className="text-2xl font-bold text-blue-700">{leftoverCount}</p>
              <p className="text-sm text-gray-500 mb-3">Not in any group</p>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={handleDownloadLeftover}
                  className="inline-flex items-center gap-2 px-3 py-1.5 text-sm bg-blue-100 text-blue-800 rounded-lg hover:bg-blue-200"
                >
                  <Download className="w-4 h-4" /> Leftover students
                </button>
                <button
                  onClick={handleDownloadFaculty}
                  className="inline-flex items-center gap-2 px-3 py-1.5 text-sm bg-blue-100 text-blue-800 rounded-lg hover:bg-blue-200"
                >
                  <Download className="w-4 h-4" /> Faculty list
                </button>
              </div>
            </div>
            <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6">
              <div className="flex items-center gap-3 mb-2">
                <Calendar className="w-6 h-6 text-blue-600" />
                <span className="font-semibold text-blue-900">Group formation deadline</span>
              </div>
              {deadlineDate ? (
                <>
                  <p className="text-lg font-medium text-blue-900">
                    {deadlineDate.toLocaleDateString()} {deadlineDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                  <p className={`text-sm ${deadlinePassed ? 'text-emerald-600' : 'text-amber-600'}`}>
                    {deadlinePassed ? (
                      <span className="flex items-center gap-1">
                        <CheckCircle2 className="w-4 h-4" /> Passed – auto-group available
                      </span>
                    ) : (
                      <span className="flex items-center gap-1">
                        <AlertCircle className="w-4 h-4" /> Not passed – set in Admin → Deadlines
                      </span>
                    )}
                  </p>
                </>
              ) : (
                <p className="text-amber-600 text-sm flex items-center gap-1">
                  <AlertCircle className="w-4 h-4" /> No GROUP_FORMATION deadline set
                </p>
              )}
            </div>
          </div>

          <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6 space-y-6">
            <h2 className="font-semibold text-blue-900">One-click auto-group</h2>
            <p className="text-gray-600 text-sm">
              Automatically group all leftover students (skills + department + year) and assign faculty with empty slots using cosine similarity.
            </p>
            <button
              onClick={handleOneClick}
              disabled={loadingOneClick || !deadlinePassed || leftoverCount < 2}
              className="w-full py-3 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium flex items-center justify-center gap-2"
            >
              {loadingOneClick ? (
                <>
                  <span className="animate-spin">⏳</span>
                  Processing...
                </>
              ) : (
                <>
                  <Users className="w-5 h-5" />
                  Auto-group all {leftoverCount} leftover students
                </>
              )}
            </button>
            {!deadlinePassed && leftoverCount >= 2 && (
              <p className="text-amber-600 text-sm">
                Set the GROUP_FORMATION deadline and wait for it to pass, or use Excel upload for a custom list.
              </p>
            )}
          </div>

          <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6">
            <h2 className="font-semibold text-blue-900 flex items-center gap-2 mb-4">
              <FileSpreadsheet className="w-5 h-5" />
              Upload Excel (download lists above first)
            </h2>
            <p className="text-gray-600 text-sm mb-4">
              1. Download leftover students &amp; faculty Excel above.<br />
              2. (Optional) Edit the lists if needed.<br />
              3. Upload both files to form groups and assign faculty.
            </p>
            <form onSubmit={handleExcelSubmit} className="space-y-4">
              <div>
                <span className="mb-1 block text-sm font-medium text-blue-900">Students file (required)</span>
                <input
                  ref={studentsFileRef}
                  id="autogroup-students-file"
                  type="file"
                  accept=".xlsx,.xls"
                  className="sr-only"
                  onChange={(e) => setStudentsFile(e.target.files?.[0] ?? null)}
                />
                <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                  <Button
                    type="button"
                    variant="primary"
                    size="md"
                    className="w-full sm:w-auto"
                    onClick={() => studentsFileRef.current?.click()}
                  >
                    <FileSpreadsheet className="h-4 w-4" />
                    {studentsFile ? 'Change students file' : 'Choose file to upload — click here'}
                  </Button>
                  <span className="truncate text-sm text-gray-600" title={studentsFile?.name}>
                    {studentsFile ? studentsFile.name : 'No file chosen'}
                  </span>
                </div>
                <p className="mt-1 text-xs text-gray-500">Cols: Email, RollNumber. Use downloaded file or same format.</p>
              </div>
              <div>
                <span className="mb-1 block text-sm font-medium text-blue-900">Faculty file (optional)</span>
                <input
                  ref={facultyFileRef}
                  id="autogroup-faculty-file"
                  type="file"
                  accept=".xlsx,.xls"
                  className="sr-only"
                  onChange={(e) => setFacultyFile(e.target.files?.[0] ?? null)}
                />
                <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                  <Button
                    type="button"
                    variant="primary"
                    size="md"
                    className="w-full sm:w-auto"
                    onClick={() => facultyFileRef.current?.click()}
                  >
                    <FileSpreadsheet className="h-4 w-4" />
                    {facultyFile ? 'Change faculty file' : 'Choose file to upload — click here'}
                  </Button>
                  <span className="truncate text-sm text-gray-600" title={facultyFile?.name}>
                    {facultyFile ? facultyFile.name : 'No file chosen'}
                  </span>
                </div>
                <p className="mt-1 text-xs text-gray-500">If provided, only these faculty are assigned. Otherwise all faculty with slots.</p>
              </div>
              <button
                type="submit"
                disabled={loading || !studentsFile || !deadlinePassed}
                className="w-full py-2 px-4 bg-blue-100 text-blue-800 rounded-lg hover:bg-blue-200 disabled:opacity-50 font-medium"
              >
                {loading ? 'Processing...' : 'Run auto-group from Excel'}
              </button>
            </form>
          </div>
        </>
      )}

      {result && (
        <div className={`rounded-2xl p-6 border ${result.error ? 'bg-red-50 border-red-200' : 'bg-blue-50 border-blue-200'}`}>
          {result.error ? (
            <p className="text-red-700 font-medium">{result.error}</p>
          ) : (
            <div className="space-y-3 text-blue-800">
              <h3 className="font-semibold">Result</h3>
              <ul className="space-y-1 text-sm">
                <li>Groups created: <strong>{result.groupsCreated}</strong></li>
                <li>Students grouped: <strong>{result.studentsGrouped}</strong></li>
                <li>Faculty assigned: <strong>{result.facultyAssigned}</strong></li>
                {result.studentsNotFound?.length > 0 && (
                  <li className="text-amber-700">Not found: {result.studentsNotFound.join(', ')}</li>
                )}
                {result.studentsSkipped?.length > 0 && (
                  <li className="text-gray-600">Skipped: {result.studentsSkipped.join(', ')}</li>
                )}
              </ul>
              {result.createdGroups?.length > 0 && (
                <div className="mt-4">
                  <p className="text-sm font-medium mb-2">Created groups</p>
                  <ul className="space-y-1 text-sm">
                    {result.createdGroups.map((g) => (
                      <li key={g.id}>
                        <Link to={`/groups/${g.id}`} className="text-blue-700 hover:underline">
                          {g.name} – {g.projectTitle}
                        </Link>
                        {g.mentorName && <span className="text-gray-600"> (mentor: {g.mentorName})</span>}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {!deadlineDate && (
        <p className="text-sm text-gray-500">
          <Link to="/admin/deadlines" className="text-blue-600 hover:underline">Set deadlines</Link> to enable auto-group after the group formation period.
        </p>
      )}
    </div>
  );
}
