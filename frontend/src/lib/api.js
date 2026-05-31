/**
 * Central API client: axios instance with JWT interceptors and all MentorLink endpoints.
 */
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || '';

export const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('mentorlink_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// On 401, clear token and redirect to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('mentorlink_token');
      localStorage.removeItem('mentorlink_user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// Auth
export const login = (email, password, role) =>
  api.post(role ? `/api/auth/login/${role.toLowerCase()}` : '/api/auth/login', {
    email,
    password,
  });

export const registerStudent = (data) =>
  api.post('/api/auth/register/student', { ...data, role: 'STUDENT' });
export const registerFaculty = (data) =>
  api.post('/api/auth/register/faculty', { ...data, role: 'FACULTY' });
export const registerAdmin = (data) =>
  api.post('/api/auth/register/admin', { ...data, role: 'ADMIN' });

export const getMe = () => api.get('/api/auth/me');
export const updateUser = (data) => api.put('/api/auth/update', data);

/** Change password (first-time login or forced). Requires auth. */
export const changePassword = (currentPassword, newPassword, confirmPassword) =>
  api.post('/api/auth/change-password', {
    currentPassword,
    newPassword,
    confirmPassword,
  });

/** Request password reset email (always succeeds with generic message if OK). */
export const forgotPassword = (email) => api.post('/api/auth/forgot-password', { email });

/** Complete reset using token from email link. */
export const resetPasswordWithToken = (token, newPassword, confirmPassword) =>
  api.post('/api/auth/reset-password', { token, newPassword, confirmPassword });

// Dashboard
export const getStudentDashboard = () => api.get('/api/dashboard/student');
export const getFacultyDashboard = () => api.get('/api/dashboard/faculty');
export const getAdminDashboard = () => api.get('/api/dashboard/admin');

// Profile
export const getProfile = () => api.get('/api/profile/me');
export const updateProfile = (data) => api.put('/api/profile/me', data);
export const uploadProfilePhoto = (file) => {
  const fd = new FormData();
  fd.append('file', file);
  return api.post('/api/profile/me/photo', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

// Meetings
export const getMeetings = (projectId) => api.get(`/api/projects/${projectId}/meetings`);
export const addMeeting = (projectId, data) => api.post(`/api/projects/${projectId}/meetings`, data);
export const verifyMeeting = (projectId, meetingId) =>
  api.post(`/api/projects/${projectId}/meetings/${meetingId}/verify`);
export const getLastMeetingByGroup = () => api.get('/api/admin/meetings/last-by-group');

// Meeting schedule (future meetings - propose / approve flow)
export const getMeetingSchedule = (projectId) =>
  api.get(`/api/projects/${projectId}/meeting-schedule`);
export const proposeMeetingSchedule = (projectId, data) =>
  api.post(`/api/projects/${projectId}/meeting-schedule`, data);
export const approveMeetingSchedule = (projectId, scheduleId) =>
  api.post(`/api/projects/${projectId}/meeting-schedule/${scheduleId}/approve`);
export const counterProposeMeetingSchedule = (projectId, scheduleId, data) =>
  api.post(`/api/projects/${projectId}/meeting-schedule/${scheduleId}/counter`, data);
export const acceptMeetingSchedule = (projectId, scheduleId) =>
  api.post(`/api/projects/${projectId}/meeting-schedule/${scheduleId}/accept`);
export const proposeNewMeetingSchedule = (projectId, scheduleId, data) =>
  api.post(`/api/projects/${projectId}/meeting-schedule/${scheduleId}/propose-new`, data);
export const getAdminScheduledMeetings = () =>
  api.get('/api/admin/meeting-schedule');

// Projects
export const getProject = (id) => api.get(`/api/projects/${id}`);
export const createProject = (data) => api.post('/api/projects/create', data);
export const updateProgress = (id, progress) =>
  api.put(`/api/projects/${id}/progress`, { progress });
export const updateProjectTitle = (id, title) =>
  api.put(`/api/projects/${id}/title`, { title });
export const summarizeReport = (projectId, file) => {
  const fd = new FormData();
  fd.append('file', file);
  return api.post(`/api/projects/${projectId}/summarize-report`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const getSummaries = (projectId) =>
  api.get(`/api/projects/${projectId}/summaries`);

// Groups
export const getGroup = (id) => api.get(`/api/groups/${id}`);
export const createGroup = (data) => api.post('/api/groups/create', data);
export const joinGroup = (token) => api.post(`/api/groups/join/${token}`);
export const mentorJoinGroup = (token) =>
  api.post(`/api/groups/mentor/join/${token}`);
export const requestFacultyMentorship = (groupId, data) =>
  api.post(`/api/groups/${groupId}/request-faculty`, data);

// Submissions
export const uploadSubmission = (projectId, file, category) => {
  const fd = new FormData();
  fd.append('file', file);
  fd.append('category', category);
  return api.post(`/api/submissions/project/${projectId}`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const getSubmissionsByProject = (projectId) =>
  api.get(`/api/submissions/project/${projectId}`);
export const getSubmissionsByGroup = (groupId) =>
  api.get(`/api/submissions/group/${groupId}`);
export const deleteSubmission = (id) => api.delete(`/api/submissions/${id}`);
export const downloadSubmission = (id) =>
  api.get(`/api/submissions/${id}/download`, { responseType: 'blob' });

// Notifications
export const getNotifications = (limit = 50) =>
  api.get('/api/notifications', { params: { limit } });
export const getUnreadCount = () => api.get('/api/notifications/unread-count');
export const markNotificationRead = (id) =>
  api.post(`/api/notifications/${id}/read`);

// Deadlines
export const getDeadlines = () => api.get('/api/deadlines');

// Faculty
export const getFacultyList = (availableOnly = false) =>
  api.get('/api/faculty/list', { params: { availableOnly } });
export const getPendingMentorshipRequests = () =>
  api.get('/api/faculty/requests/pending');
export const approveMentorshipRequest = (requestId) =>
  api.post(`/api/faculty/requests/${requestId}/approve`);
export const rejectMentorshipRequest = (requestId) =>
  api.post(`/api/faculty/requests/${requestId}/reject`);

// Recommender
export const recommendMentors = (body) =>
  api.post('/api/recommend/mentor', body);
export const recommendTopMentors = (topN, body) =>
  api.post(`/api/recommend/mentor/top/${topN}`, body);
export const getRecommendations = (projectId) =>
  api.get(`/api/recommend/mentor/project/${projectId}`);

// Admin
export const uploadStudents = (file) => {
  const fd = new FormData();
  fd.append('file', file);
  return api.post('/api/admin/upload/students', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const uploadFaculty = (file) => {
  const fd = new FormData();
  fd.append('file', file);
  return api.post('/api/admin/upload/faculty', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const createDeadline = (data) =>
  api.post('/api/admin/deadlines', {
    name: data.name,
    type: data.type || 'PROJECT_SUBMISSION',
    dueDate: data.dueDate,
  });
export const getAdminDeadlines = () => api.get('/api/admin/deadlines');
export const extendDeadline = (id, dueDate) =>
  api.put(`/api/admin/deadlines/${id}/extend`, { dueDate });
export const getStudentsWithoutGroup = () =>
  api.get('/api/admin/students/without-group');
export const downloadLeftoverStudentsExcel = () =>
  api.get('/api/admin/students/without-group/export', { responseType: 'blob' });
export const downloadFacultyExcel = () =>
  api.get('/api/admin/faculty/export', { responseType: 'blob' });
export const autoGroupFromLeftover = () =>
  api.post('/api/admin/auto-group/from-leftover');
export const autoGroupFromExcel = (studentsFile, facultyFile) => {
  const fd = new FormData();
  fd.append('studentsFile', studentsFile);
  if (facultyFile) fd.append('facultyFile', facultyFile);
  return api.post('/api/admin/auto-group/from-excel', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const assignFaculty = (projectId, facultyId) =>
  api.post(`/api/admin/projects/${projectId}/assign/${facultyId}`);
export const unassignFaculty = (projectId) =>
  api.post(`/api/admin/projects/${projectId}/unassign`);
export const getAnalytics = () => api.get('/api/admin/analytics');
export const getAdminGroupsWithProgress = (search) =>
  api.get('/api/admin/groups', { params: search ? { search } : {} });
export const getAdminGroupDetail = (groupId) =>
  api.get(`/api/admin/groups/${groupId}`);
export const setFacultyMaxGroups = (facultyId, maxGroups) =>
  api.put(`/api/admin/faculty/${facultyId}/max-groups`, { maxGroups });
export const resetYearlyData = () =>
  api.delete('/api/admin/reset-yearly-data');

// Users (Admin)
export const getUsers = () => api.get('/api/users');
export const getUser = (id) => api.get(`/api/users/${id}`);
export const deleteUser = (id) => api.delete(`/api/users/${id}`);

// Files
export const fileUrl = (folder, filename) =>
  `${API_BASE}/api/files/${folder}/${filename}`;
