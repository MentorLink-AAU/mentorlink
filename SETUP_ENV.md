# Environment Setup (Quick Start)

Use these steps once, then start the stack normally.

`backend/.env` is the official local credential storage file for this project.
Real secrets must stay only in `backend/.env` and must never be committed.
Only `backend/.env.example` is intended to be tracked in Git.

## 1) Create local backend env file

PowerShell (from repo root):

```powershell
Copy-Item backend/.env.example backend/.env
```

## 2) Fill secrets in `backend/.env`

At minimum set:

- `DB_PASSWORD`
- `JWT_SECRET`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

## 3) One-command run with env loaded (PowerShell)

From repo root:

```powershell
Get-Content backend/.env | ForEach-Object {
  if ($_ -and -not $_.StartsWith('#')) {
    $name, $value = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
  }
}; npm run start
```

This loads backend env vars into the current shell process and starts frontend + backend + NLP using the existing root script.

## 4) Super run (full project health check)

From repo root:

```powershell
npm run check:all
```

This runs:

- backend tests (`mvn test`)
- frontend tests (`vitest run`)
- frontend lint (`eslint .`)
