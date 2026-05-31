/** Admin: bulk upload students or faculty via Excel. */
import { useState, useRef } from 'react';
import { Upload, FileSpreadsheet } from 'lucide-react';
import { uploadStudents, uploadFaculty } from '../lib/api';
import { PageHeader } from '../components/ui/PageHeader';
import { Card, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Select } from '../components/ui/Select';

export function AdminUpload() {
  const [type, setType] = useState('students');
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const fileInputRef = useRef(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) return;
    setLoading(true);
    setResult(null);
    try {
      const res =
        type === 'students' ? await uploadStudents(file) : await uploadFaculty(file);
      setResult(res.data?.data);
    } catch (err) {
      setResult({ error: err.response?.data?.error?.message || 'Upload failed' });
    } finally {
      setLoading(false);
    }
  };

  const studentCols = 'Email | FullName | RollNumber | Department | YearOfStudy | Skills | Password';
  const facultyCols = 'Email | FullName | Department | Expertise | MaxGroups | Password';
  const canSubmit = !!file && !loading;

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <PageHeader
        title="Bulk upload"
        description="Import students or faculty from Excel whenever you are ready."
      />

      <Card variant="glass">
        <CardContent className="space-y-4 p-6">
          <p className="text-sm text-mentor-muted">
            Include <strong className="text-mentor-text">Password</strong> in the sheet when you want users to log in
            immediately. Students not in the list can still register manually.
          </p>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-mentor-text" htmlFor="upload-type">
                Type
              </label>
              <Select id="upload-type" value={type} onChange={(e) => setType(e.target.value)}>
                <option value="students">Students</option>
                <option value="faculty">Faculty</option>
              </Select>
            </div>
            <div className="rounded-xl border border-mentor-border/60 bg-mentor-surface/80 p-4 text-sm text-mentor-text">
              <p className="mb-2 font-medium">
                {type === 'students' ? 'Students format:' : 'Faculty format:'}
              </p>
              <code className="block break-all text-xs text-mentor-muted">
                {type === 'students' ? studentCols : facultyCols}
              </code>
              <p className="mt-2 text-mentor-muted">
                Password is optional. If empty, a random password is set (user must change on first login).
              </p>
            </div>
            <div>
              <span className="mb-2 block text-sm font-medium text-mentor-text">Excel file</span>
              <input
                ref={fileInputRef}
                id="bulk-excel-file"
                type="file"
                accept=".xlsx,.xls"
                className="sr-only"
                onChange={(e) => {
                  setFile(e.target.files?.[0] ?? null);
                  setResult(null);
                }}
              />
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                <Button
                  type="button"
                  variant="primary"
                  size="md"
                  className="w-full sm:w-auto"
                  onClick={() => fileInputRef.current?.click()}
                >
                  <FileSpreadsheet className="h-4 w-4" />
                  {file ? 'Change file' : 'Choose file to upload — click here'}
                </Button>
                <span className="truncate text-sm text-mentor-muted" title={file?.name}>
                  {file ? file.name : 'No file chosen'}
                </span>
              </div>
            </div>
            <Button type="submit" variant="primary" size="lg" className="w-full" disabled={!canSubmit}>
              <Upload className="h-4 w-4" />
              {loading ? 'Uploading…' : 'Upload'}
            </Button>
          </form>
        </CardContent>
      </Card>

      {result && !result.error && (
        <Alert variant="success" title="Upload finished">
          <p>Created: {result.created ?? result.createdCount ?? 0}</p>
          <p>Skipped: {result.skipped ?? result.skippedCount ?? 0}</p>
          {result.errors?.length > 0 && (
            <div className="mt-2 text-sm text-mentor-danger">
              {result.errors.slice(0, 5).map((e, i) => (
                <p key={i}>{e}</p>
              ))}
            </div>
          )}
        </Alert>
      )}
      {result?.error && (
        <Alert variant="error" title="Upload failed">
          {result.error}
        </Alert>
      )}
    </div>
  );
}
