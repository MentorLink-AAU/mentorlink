/** Admin: create, list, extend deadlines. */
import { useState, useEffect } from 'react';
import { getAdminDeadlines, createDeadline, extendDeadline } from '../lib/api';
import { CalendarPlus } from 'lucide-react';

const TYPES = ['GROUP_FORMATION', 'PROJECT_SUBMISSION', 'PROGRESS_UPDATE'];

export function AdminDeadlines() {
  const [deadlines, setDeadlines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ name: '', type: 'PROJECT_SUBMISSION', dueDate: '' });
  const [saving, setSaving] = useState(false);
  const [extendingId, setExtendingId] = useState(null);
  const [extendDate, setExtendDate] = useState('');

  useEffect(() => {
    getAdminDeadlines()
      .then((res) => setDeadlines(res.data?.data || []))
      .catch(() => setDeadlines([]))
      .finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.dueDate) return;
    setSaving(true);
    try {
      await createDeadline({
        ...form,
        dueDate: new Date(form.dueDate).toISOString(),
      });
      const res = await getAdminDeadlines();
      setDeadlines(res.data?.data || []);
      setForm({ name: '', type: 'PROJECT_SUBMISSION', dueDate: '' });
    } catch {
      void 0;
    }
    finally {
      setSaving(false);
    }
  };

  const handleExtend = async (id) => {
    if (!extendDate) return;
    setSaving(true);
    try {
      await extendDeadline(id, new Date(extendDate).toISOString());
      const res = await getAdminDeadlines();
      setDeadlines(res.data?.data || []);
      setExtendingId(null);
      setExtendDate('');
    } catch {
      void 0;
    }
    finally {
      setSaving(false);
    }
  };

  const toDatetimeLocal = (iso) => {
    if (!iso) return '';
    const d = new Date(iso);
    const offset = d.getTimezoneOffset() * 60000;
    return new Date(d.getTime() - offset).toISOString().slice(0, 16);
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <div className="animate-spin w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-blue-900">Deadlines</h1>
      <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6 flex flex-wrap gap-4">
        <input
          type="text"
          placeholder="Name"
          value={form.name}
          onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
          className="px-4 py-2 rounded-lg border border-blue-200"
        />
        <select
          value={form.type}
          onChange={(e) => setForm((f) => ({ ...f, type: e.target.value }))}
          className="px-4 py-2 rounded-lg border border-blue-200"
        >
          {TYPES.map((t) => (
            <option key={t} value={t}>{t.replace('_', ' ')}</option>
          ))}
        </select>
        <input
          type="datetime-local"
          value={form.dueDate}
          onChange={(e) => setForm((f) => ({ ...f, dueDate: e.target.value }))}
          required
          className="px-4 py-2 rounded-lg border border-blue-200"
        />
        <button
          type="submit"
          disabled={saving}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          Add
        </button>
      </form>
      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden">
        <ul>
          {deadlines.map((d) => (
            <li
              key={d.id}
              className="px-6 py-4 border-t border-blue-100 first:border-0"
            >
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div className="flex flex-wrap items-center gap-4">
                  <span className="font-medium">{d.name}</span>
                  <span className="text-blue-600">{d.type}</span>
                  <span className="text-gray-600">
                    {d.dueDate ? new Date(d.dueDate).toLocaleString() : '—'}
                  </span>
                </div>
                {extendingId === d.id ? (
                  <div className="flex items-center gap-2">
                    <input
                      type="datetime-local"
                      value={extendDate || toDatetimeLocal(d.dueDate)}
                      onChange={(e) => setExtendDate(e.target.value)}
                      className="px-3 py-1.5 rounded-lg border border-blue-200 text-sm"
                    />
                    <button
                      type="button"
                      onClick={() => handleExtend(d.id)}
                      disabled={saving}
                      className="px-3 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50"
                    >
                      Save
                    </button>
                    <button
                      type="button"
                      onClick={() => { setExtendingId(null); setExtendDate(''); }}
                      className="px-3 py-1.5 text-gray-600 text-sm hover:underline"
                    >
                      Cancel
                    </button>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={() => { setExtendingId(d.id); setExtendDate(toDatetimeLocal(d.dueDate)); }}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm bg-blue-100 text-blue-800 rounded-lg hover:bg-blue-200"
                  >
                    <CalendarPlus className="w-4 h-4" />
                    Extend
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
