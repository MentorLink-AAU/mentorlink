/** Admin: analytics charts with admin/users/groups data. */
import { useEffect, useMemo, useState } from 'react';
import { getAdminGroupsWithProgress, getAnalytics, getUsers } from '../lib/api';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

const BAR_COLORS = ['#0d9488', '#2563eb', '#7c3aed', '#f97316', '#14b8a6', '#e11d48'];
const EXPERTISE_SPLIT_REGEX = /[,;/|]/;

function getFacultySkillTokens(mentor) {
  const explicitSkills = (mentor.skills || [])
    .map((s) => s?.trim())
    .filter(Boolean);
  if (explicitSkills.length > 0) return explicitSkills;

  // Fallback: parse legacy expertise text entered by admin.
  return String(mentor.expertise || '')
    .split(EXPERTISE_SPLIT_REGEX)
    .map((s) => s.trim())
    .filter(Boolean);
}

export function AdminAnalytics() {
  const [analyticsData, setAnalyticsData] = useState(null);
  const [users, setUsers] = useState([]);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getAnalytics(), getUsers(), getAdminGroupsWithProgress()])
      .then(([analyticsRes, usersRes, groupsRes]) => {
        setAnalyticsData(analyticsRes.data?.data || {});
        setUsers(usersRes.data?.data || []);
        setGroups(groupsRes.data?.data || []);
      })
      .catch(() => {
        setAnalyticsData({});
        setUsers([]);
        setGroups([]);
      })
      .finally(() => setLoading(false));
  }, []);

  const d = analyticsData || {};
  const lastMeetingByGroup = Array.isArray(d.lastMeetingByGroup) ? d.lastMeetingByGroup : [];
  const statsForCards = { ...d };
  delete statsForCards.lastMeetingByGroup;

  const students = useMemo(
    () => users.filter((u) => (u.role || '').toUpperCase() === 'STUDENT'),
    [users]
  );
  const faculty = useMemo(
    () => users.filter((u) => (u.role || '').toUpperCase() === 'FACULTY'),
    [users]
  );

  const studentById = useMemo(() => new Map(students.map((s) => [s.id, s])), [students]);
  const facultyByEmail = useMemo(
    () =>
      new Map(
        faculty
          .filter((f) => f.email)
          .map((f) => [f.email.toLowerCase(), f])
      ),
    [faculty]
  );

  const studentSkillData = useMemo(() => {
    const counts = new Map();
    students.forEach((student) => {
      (student.skills || []).forEach((skill) => {
        const normalized = skill?.trim();
        if (!normalized) return;
        counts.set(normalized, (counts.get(normalized) || 0) + 1);
      });
    });
    return Array.from(counts.entries())
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value || a.name.localeCompare(b.name));
  }, [students]);

  const facultySkillData = useMemo(() => {
    const counts = new Map();
    faculty.forEach((mentor) => {
      getFacultySkillTokens(mentor).forEach((skill) => {
        const normalized = skill?.trim();
        if (!normalized) return;
        counts.set(normalized, (counts.get(normalized) || 0) + 1);
      });
    });
    return Array.from(counts.entries())
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value || a.name.localeCompare(b.name));
  }, [faculty]);

  const groupProgressData = useMemo(
    () =>
      groups
        .map((g) => ({ name: g.groupName || `Group ${g.groupId}`, value: g.progress || 0 }))
        .sort((a, b) => b.value - a.value || a.name.localeCompare(b.name)),
    [groups]
  );

  const mentorLoadData = useMemo(() => {
    const counts = new Map();
    groups.forEach((g) => {
      const mentor = g.mentorName || g.mentorEmail;
      if (!mentor) return;
      counts.set(mentor, (counts.get(mentor) || 0) + 1);
    });
    return Array.from(counts.entries())
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value || a.name.localeCompare(b.name));
  }, [groups]);

  const departmentFormationData = useMemo(() => {
    const departmentToGroups = new Map();
    groups.forEach((g) => {
      const groupKey = g.groupId ?? g.groupName;
      (g.members || []).forEach((member) => {
        const student = studentById.get(member.userId);
        const dept = student?.department?.trim();
        if (!dept) return;
        if (!departmentToGroups.has(dept)) departmentToGroups.set(dept, new Set());
        departmentToGroups.get(dept).add(groupKey);
      });
    });
    return Array.from(departmentToGroups.entries())
      .map(([name, groupSet]) => ({ name, value: groupSet.size }))
      .sort((a, b) => b.value - a.value || a.name.localeCompare(b.name));
  }, [groups, studentById]);

  const alignmentByGroupData = useMemo(() => {
    return groups
      .map((g) => {
        const mentor = facultyByEmail.get((g.mentorEmail || '').toLowerCase());
        if (!mentor) return null;
        const mentorSkills = new Set(
          getFacultySkillTokens(mentor)
            .map((s) => s?.trim()?.toLowerCase())
            .filter(Boolean)
        );
        const studentSkills = new Set();
        (g.members || []).forEach((member) => {
          const student = studentById.get(member.userId);
          if (!student) return;
          (student.skills || []).forEach((skill) => {
            const normalized = skill?.trim()?.toLowerCase();
            if (!normalized) return;
            studentSkills.add(normalized);
          });
        });
        const union = new Set([...mentorSkills, ...studentSkills]);
        if (union.size === 0) return null;
        const intersection = [...studentSkills].filter((s) => mentorSkills.has(s)).length;
        return {
          name: g.groupName || `Group ${g.groupId}`,
          value: Number(((intersection / union.size) * 100).toFixed(2)),
        };
      })
      .filter(Boolean)
      .sort((a, b) => b.value - a.value || a.name.localeCompare(b.name));
  }, [groups, facultyByEmail, studentById]);

  const chartConfig = { margin: { top: 20, right: 20, left: 10, bottom: 80 } };
  const renderBarCard = (title, data, color, ySuffix = '') => (
    <div className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6">
      <h2 className="text-lg font-semibold text-blue-900 mb-4">{title}</h2>
      <div className="h-72">
        {data.length === 0 ? (
          <div className="h-full flex items-center justify-center text-sm text-gray-500">No data</div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data} {...chartConfig}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis dataKey="name" angle={-25} textAnchor="end" interval={0} height={90} />
              <YAxis tickFormatter={(v) => `${v}${ySuffix}`} />
              <Tooltip formatter={(value) => [`${value}${ySuffix}`, 'Value']} />
              <Bar dataKey="value" fill={color} radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold text-blue-900">Analytics</h1>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {Object.entries(statsForCards).map(([key, value]) => (
          <div
            key={key}
            className="bg-white rounded-2xl shadow-lg border border-blue-100 p-6"
          >
            <p className="text-sm text-gray-600 capitalize">
              {key.replace(/([A-Z])/g, ' $1').trim()}
            </p>
            <p className="text-2xl font-bold text-blue-900 mt-1">{String(value)}</p>
          </div>
        ))}
      </div>

      <div className="grid lg:grid-cols-2 gap-8">
        {renderBarCard('Student Skills Distribution', studentSkillData, BAR_COLORS[0])}
        {renderBarCard('Faculty Skills Distribution', facultySkillData, BAR_COLORS[1])}
        {renderBarCard('Group Progress by Group', groupProgressData, BAR_COLORS[2], '%')}
        {renderBarCard('Mentor Load', mentorLoadData, BAR_COLORS[3])}
        {renderBarCard('Department-wise Group Formation', departmentFormationData, BAR_COLORS[4])}
        {renderBarCard('Alignment Score by Group', alignmentByGroupData, BAR_COLORS[5], '%')}

        {lastMeetingByGroup.length > 0 && (
          <div className="lg:col-span-2 bg-white rounded-2xl shadow-lg border border-blue-100 p-6 overflow-x-auto">
            <h2 className="text-lg font-semibold text-blue-900 mb-4">Last Meeting by Group</h2>
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-blue-100">
                  <th className="text-left py-2 font-medium text-blue-900">Group</th>
                  <th className="text-left py-2 font-medium text-blue-900">Project</th>
                  <th className="text-left py-2 font-medium text-blue-900">Last Meeting</th>
                  <th className="text-left py-2 font-medium text-blue-900">Status</th>
                </tr>
              </thead>
              <tbody>
                {lastMeetingByGroup.map((row) => (
                  <tr key={row.groupId || row.projectId} className="border-b border-blue-50">
                    <td className="py-3 text-blue-900">{row.groupName || '—'}</td>
                    <td className="py-3 text-gray-600">{row.projectTitle || '—'}</td>
                    <td className="py-3">
                      {row.lastMeetingDate
                        ? new Date(row.lastMeetingDate + 'T00:00:00').toLocaleDateString()
                        : '—'}
                    </td>
                    <td className="py-3">
                      {row.lastMeetingDate ? (
                        row.verified ? (
                          <span className="text-emerald-600">Verified</span>
                        ) : (
                          <span className="text-amber-600">Pending</span>
                        )
                      ) : (
                        <span className="text-gray-400">No meeting</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
