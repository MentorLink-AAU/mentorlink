# MentorLink Backend API Endpoints

## Authentication (Google OAuth2)
- **GET** `/oauth2/authorization/google` - Redirect to Google login
- **GET** `/api/auth/me` - Current user (requires Bearer JWT)
- **PUT** `/api/auth/update` - Update profile (name, password, skills)

## Admin
- **POST** `/api/admin/upload/students` - Upload Excel (students)
- **POST** `/api/admin/upload/faculty` - Upload Excel (faculty)
- **POST** `/api/admin/deadlines` - Set deadline `{name, type, dueDate}`
- **GET** `/api/admin/deadlines` - List deadlines
- **PUT** `/api/admin/deadlines/{id}/extend` - Extend deadline `{dueDate}`
- **POST** `/api/admin/auto-group/from-leftover` - One-click auto-group all leftover students + assign faculty
- **POST** `/api/admin/auto-group/from-excel` - Auto-group from Excel (columns: Email, RollNumber)
- **POST** `/api/admin/auto-assign-faculty` - Auto-assign faculty to projects
- **POST** `/api/admin/projects/{projectId}/assign/{facultyId}` - Manual faculty assign
- **POST** `/api/admin/projects/{projectId}/unassign` - Unassign faculty
- **PUT** `/api/admin/faculty/{facultyId}/max-groups` - Set max groups per faculty
- **GET** `/api/admin/analytics` - Dashboard stats

## Students
- **POST** `/api/groups/create` - Create group `{name, projectId? OR projectTitle, projectDescription}`
- **POST** `/api/groups/join/{token}` - Join group by token
- **POST** `/api/groups/{groupId}/request-faculty` - Request faculty mentorship
- **GET** `/api/faculty/list` - List faculty (expertise, availability)
- **POST** `/api/projects/create` - Create project
- **POST** `/api/submissions/project/{projectId}` - Upload document
- **GET** `/api/submissions/project/{projectId}` - List submissions

## Meetings
- **POST** `/api/projects/{projectId}/meetings` - Log meeting (student, body: `{meetingDate, details}`)
- **GET** `/api/projects/{projectId}/meetings` - List meetings (student, faculty, admin)
- **POST** `/api/projects/{projectId}/meetings/{meetingId}/verify` - Verify meeting (faculty mentor)
- **GET** `/api/admin/meetings/last-by-group` - Last meeting per group (admin)

## Faculty
- **GET** `/api/faculty/requests/pending` - Pending mentorship requests
- **POST** `/api/faculty/requests/{id}/approve` - Approve request
- **POST** `/api/faculty/requests/{id}/reject` - Reject request
- **PUT** `/api/projects/{projectId}/progress` - Update progress `{progress: 0-100}`

## Deadlines
- **GET** `/api/deadlines` - List all deadlines

## Notifications
- **GET** `/api/notifications` - My notifications
- **GET** `/api/notifications/unread-count` - Unread count
- **POST** `/api/notifications/{id}/read` - Mark as read

## WebSocket
- **WS** `/ws` - STOMP over SockJS for real-time notifications/chat

## Excel Format (Bulk Upload)
**Students:** Email | FullName | RollNumber | Department | YearOfStudy | Password  
**Faculty:** Email | FullName | Department | Expertise | MaxGroups | Password  

- Password is optional. If provided, users can log in immediately. If empty, a random password is set.
- Manual registration remains available for students/faculty not in the Excel list.
