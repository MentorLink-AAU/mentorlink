USE mentorlink_db;

-- Users (parent table for students, faculty, admin)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('STUDENT', 'FACULTY', 'ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Skills
CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Students <-> Skills (many-to-many)
CREATE TABLE student_skills (
    student_id BIGINT,
    skill_id BIGINT,
    PRIMARY KEY (student_id, skill_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (skill_id) REFERENCES skills(id)
);

-- Groups
CREATE TABLE project_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Group members
CREATE TABLE group_members (
    group_id BIGINT,
    student_id BIGINT,
    PRIMARY KEY (group_id, student_id),
    FOREIGN KEY (group_id) REFERENCES project_groups(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Projects
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    group_id BIGINT,
    faculty_id BIGINT,
    progress INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES project_groups(id),
    FOREIGN KEY (faculty_id) REFERENCES users(id)
);

-- Submissions
CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT,
    file_path VARCHAR(255),
    category VARCHAR(50), -- Assignment, Report, PPT, etc.
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- Deadlines
CREATE TABLE deadlines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    due_date TIMESTAMP,
    type ENUM('GROUP_FORMATION','PROJECT_SUBMISSION','PROGRESS_UPDATE')
);
