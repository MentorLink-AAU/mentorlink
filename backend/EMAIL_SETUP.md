# Email Configuration

MentorLink sends emails for:

1. **Registration welcome** – when a new user registers
2. **Mentorship approval/rejection** – when faculty approves or rejects a mentorship request
3. **Deadline reminders** – sent daily at 9 AM for deadlines due in 24–48 hours

## Configuration

Set these in `application.properties` or as environment variables:

| Property | Env Var | Description |
|----------|---------|-------------|
| `spring.mail.host` | `MAIL_HOST` | SMTP host (default: smtp.gmail.com) |
| `spring.mail.port` | `MAIL_PORT` | SMTP port (default: 587) |
| `spring.mail.username` | `MAIL_USERNAME` | SMTP username (email) |
| `spring.mail.password` | `MAIL_PASSWORD` | SMTP password or app password |

## Gmail

For Gmail, use an **App Password**:

1. Enable 2-Step Verification on your Google account
2. Go to Google Account → Security → App passwords
3. Generate an app password for "Mail"
4. Use that 16-character password as `MAIL_PASSWORD`

If mail is not configured, the app will log a warning and skip sending; all other features work normally.
