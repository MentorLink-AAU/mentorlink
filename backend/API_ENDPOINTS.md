# MentorLink Backend API Endpoints

Base URL: backend root (e.g. `http://localhost:8080`).  
Auth: `Authorization: Bearer <JWT>` unless marked **Public**.

Responses use `{ "success": true, "data": ... }` or `{ "success": false, "error": { "code", "message", "path" } }`.

## Authentication — `/api/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/register/student` | Public | Register student |
| POST | `/register/faculty` | Public | Register faculty |
| POST | `/register/admin` | Public | Register admin |
| POST | `/login` | Public | Login (any role) |
| POST | `/login/student` | Public | Login (student only) |
| POST | `/login/faculty` | Public | Login (faculty only) |
| POST | `/login/admin` | Public | Login (admin only) |
| POST | `/forgot-password` | Public | Request reset email |
| POST | `/reset-password` | Public | Reset with token |
| POST | `/change-password` | JWT | Change password |
| GET | `/me` | JWT | Current user |
| PUT | `/update` | JWT | Update user fields |

## Profile — `/api/profile`

| Method | Path | Auth |
|--------|------|------|
| GET | `/me` | JWT |
| PUT | `/me` | JWT |
| POST | `/me/photo` | JWT (multipart) |

## Dashboards — `/api/dashboard`

| Method | Path | Role |
|--------|------|------|
| GET | `/student` | STUDENT |
| GET | `/faculty` | FACULTY |
| GET | `/admin` | ADMIN |

## Users — `/api/users` (ADMIN)

| Method | Path |
|--------|------|
| GET | `/` |
| GET | `/{id}` |
| DELETE | `/{id}` |

## Groups — `/api/groups`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/{groupId}` | Members, mentor, or admin only |
| POST | `/create` | Student leader |
| POST | `/join/{token}` | Student |
| POST | `/mentor/join/{token}` | Faculty |
| POST | `/{groupId}/request-faculty` | Group member |

Join tokens are only returned to authorized viewers.

## Projects — `/api/projects`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/{projectId}` | Project access required |
| POST | `/create` | Authenticated |
| PUT | `/{projectId}/title` | Members or admin |
| PUT | `/{projectId}/progress` | Assigned mentor |
| POST | `/{projectId}/summarize-report` | PDF upload (multipart) |
| GET | `/{projectId}/summaries` | Project access required |

## Submissions — `/api/submissions`

| Method | Path |
|--------|------|
| POST | `/project/{projectId}` (multipart) |
| GET | `/project/{projectId}` |
| GET | `/group/{groupId}` |
| DELETE | `/{submissionId}` |
| GET | `/{submissionId}/download` |

## Meetings — `/api/projects/{projectId}/meetings`

| Method | Path |
|--------|------|
| POST | `/` — log meeting |
| GET | `/` — list |
| POST | `/{meetingId}/verify` — faculty mentor |

| GET | `/api/admin/meetings/last-by-group` | ADMIN |

## Meeting schedule — `/api/projects/{projectId}/meeting-schedule`

| Method | Path |
|--------|------|
| POST | `/` — propose |
| GET | `/` |
| POST | `/{scheduleId}/approve` |
| POST | `/{scheduleId}/counter` |
| POST | `/{scheduleId}/accept` |
| POST | `/{scheduleId}/propose-new` |

| GET | `/api/admin/meeting-schedule` | ADMIN |

## Faculty — `/api/faculty`

| Method | Path | Role |
|--------|------|------|
| GET | `/list` | Any JWT (`?availableOnly=true`) |
| POST | `/` | ADMIN (create profile) |
| GET | `/requests/pending` | FACULTY |
| POST | `/requests/{id}/approve` | FACULTY |
| POST | `/requests/{id}/reject` | FACULTY |

## Recommender (TF-IDF) — `/api/recommend`

| Method | Path |
|--------|------|
| POST | `/mentor` |
| POST | `/mentor/top/{topN}` |
| GET | `/mentor/project/{projectId}` |

## Matrix factorization (batch) — ADMIN

| Method | Path |
|--------|------|
| POST | `/api/run/matrix_factorization` |
| GET | `/api/recommender/status/{jobId}` |
| GET | `/api/recommender/result/{jobId}` |
| GET | `/api/recommender/result/{jobId}/download` |

## Admin — `/api/admin`

| Method | Path |
|--------|------|
| POST | `/upload/students` (Excel) |
| POST | `/upload/faculty` (Excel) |
| DELETE | `/reset-yearly-data` |
| POST | `/deadlines` |
| GET | `/deadlines` |
| PUT | `/deadlines/{id}/extend` |
| GET | `/students/without-group` |
| GET | `/students/without-group/export` |
| GET | `/faculty/export` |
| POST | `/auto-group/from-leftover` |
| POST | `/auto-group/from-excel` |
| POST | `/projects/{projectId}/assign/{facultyId}` |
| POST | `/projects/{projectId}/unassign` |
| PUT | `/faculty/{facultyId}/max-groups` |
| GET | `/analytics` |
| GET | `/groups` |
| GET | `/groups/{groupId}` |

## Other

| Method | Path | Auth |
|--------|------|------|
| GET | `/api/deadlines` | JWT |
| GET | `/api/notifications` | JWT |
| GET | `/api/notifications/unread-count` | JWT |
| POST | `/api/notifications/{id}/read` | JWT |
| GET | `/api/files/{folder}/{filename}` | JWT |
| GET | `/actuator/**` | Public |
| GET | `/api/test/ping` | ADMIN |
| GET | `/api/test/error` | ADMIN |
| GET | `/api/students/profile/{userId}` | ADMIN |
| POST | `/api/students/profile` | ADMIN |

## WebSocket

- **SockJS** `/ws` — STOMP subscribe `/topic/notifications/{userId}`

## NLP service (separate process)

| Method | Path |
|--------|------|
| GET | `/health` |
| POST | `/summarize` |

Configured via `app.nlp.summarization.url` (default `http://localhost:5001`).

## Excel bulk upload columns

**Students:** Email | FullName | RollNumber | Department | YearOfStudy | Password  
**Faculty:** Email | FullName | Department | Expertise | MaxGroups | Password  

Password optional; empty generates a random password and forces change on first login.
