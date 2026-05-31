/** Lists deadlines for authenticated users. */
import { useState, useEffect } from 'react';
import { getDeadlines } from '../lib/api';

export function Deadlines() {
  const [deadlines, setDeadlines] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDeadlines()
      .then((res) => setDeadlines(res.data?.data || []))
      .catch(() => setDeadlines([]))
      .finally(() => setLoading(false));
  }, []);

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
      <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden">
        {deadlines.length === 0 ? (
          <p className="p-8 text-center text-gray-500">No deadlines set</p>
        ) : (
          <ul>
            {deadlines.map((d) => (
              <li
                key={d.id}
                className="flex justify-between items-center px-6 py-4 border-t border-blue-100 first:border-0"
              >
                <span className="font-medium">{d.name}</span>
                <span className="px-2 py-1 rounded text-xs bg-blue-100 text-blue-800">{d.type}</span>
                <span className="text-blue-600">
                  {d.dueDate ? new Date(d.dueDate).toLocaleString() : '—'}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
