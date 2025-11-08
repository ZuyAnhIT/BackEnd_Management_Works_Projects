DROP DATABASE IF EXISTS QuanLyCongViecDuAn;

-- Tạo cơ sở dữ liệu
CREATE DATABASE IF NOT EXISTS QuanLyCongViecDuAn
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;
USE QuanLyCongViecDuAn;

-- Create user and grant privileges
CREATE USER IF NOT EXISTS 'admin123@'@'%' IDENTIFIED BY 'admin123@';
GRANT ALL PRIVILEGES ON QuanLyCongViecDuAn.* TO 'admin123@'@'%';
FLUSH PRIVILEGES;

-- =============================================
-- DATABASE SCHEMA - PROJECT MANAGEMENT SYSTEM
-- Supports Scrum, Kanban models
-- =============================================

-- 1. USERS TABLE
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    status ENUM('ACTIVE', 'LOCKED', 'DELETED') DEFAULT 'ACTIVE',
    is_email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. AUTH TOKENS TABLE
CREATE TABLE auth_tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    token_type ENUM('ACCESS','REFRESH','RESET_PASSWORD','EMAIL_VERIFICATION','API') NOT NULL,
    status ENUM('ACTIVE', 'REVOKED', 'EXPIRED') DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. USER SETTINGS TABLE
CREATE TABLE user_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
    display_mode ENUM('LIGHT', 'DARK', 'SYSTEM') DEFAULT 'LIGHT',
    email_notifications BOOLEAN DEFAULT TRUE,
    push_notifications BOOLEAN DEFAULT TRUE,
    default_homepage VARCHAR(50) DEFAULT 'dashboard',
    board_config JSON COMMENT 'Custom board view settings',
    list_config JSON COMMENT 'Custom list view settings',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. USER ACTIVITY LOG TABLE
CREATE TABLE activity_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100) COMMENT 'e.g., Task, Project, Comment...',
    entity_id INT,
    old_value TEXT COMMENT 'Value before change',
    new_value TEXT COMMENT 'Value after change',
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. ROLES TABLE
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(100) NOT NULL UNIQUE COMMENT 'Identifier code, e.g., SYSTEM_ADMIN, COMPANY_ADMIN, PROJECT_MEMBER',
    role_name VARCHAR(255) NOT NULL COMMENT 'Display name, e.g., System Administrator, Company Admin, Project Member',
    description TEXT,
    level ENUM('SYSTEM', 'COMPANY', 'WORKSPACE', 'PROJECT') NOT NULL COMMENT 'Scope of the role: System, Company, Workspace, Project',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. PERMISSIONS TABLE
CREATE TABLE permissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT 'Identifier code, e.g., task:create, task:delete, project:invite_member',
    permission_name VARCHAR(255) NOT NULL COMMENT 'Display name, e.g., Create Task, Delete Task, Invite Project Member',
    group_name VARCHAR(100) COMMENT 'Permission group for management, e.g., Task Management, Project Management',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. ROLE-PERMISSION MAPPING TABLE
CREATE TABLE role_permissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. USER-ROLE MAPPING TABLE (FOR SYSTEM ROLES)
CREATE TABLE user_roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    role_id INT NOT NULL COMMENT 'FK to Role with level = SYSTEM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. COMPANIES/ORGANIZATIONS TABLE
CREATE TABLE companies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    company_code VARCHAR(50) UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    address TEXT,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    website VARCHAR(255),
    created_by_id INT NOT NULL,
    status ENUM('ACTIVE', 'SUSPENDED', 'DELETED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. COMPANY MEMBERS TABLE
CREATE TABLE company_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    user_id INT NOT NULL,
    role_id INT NOT NULL COMMENT 'FK to Role (level = COMPANY)',
    job_title VARCHAR(100),
    department VARCHAR(100),
    status ENUM('ACTIVE', 'SUSPENDED', 'REMOVED') DEFAULT 'ACTIVE',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_company_user (company_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. WORKSPACES TABLE
CREATE TABLE workspaces (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    workspace_code VARCHAR(50),
    description TEXT,
    cover_image_url VARCHAR(500),
    color VARCHAR(7) DEFAULT '#3498db',
    created_by_id INT NOT NULL,
    status ENUM('ACTIVE', 'ARCHIVED', 'DELETED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. WORKSPACE MEMBERS TABLE
CREATE TABLE workspace_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    workspace_id INT NOT NULL,
    user_id INT NOT NULL,
    role_id INT NOT NULL COMMENT 'FK to Role (level = WORKSPACE)',
    status ENUM('ACTIVE', 'REMOVED') DEFAULT 'ACTIVE',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_workspace_user (workspace_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. PROJECT TYPES TABLE
CREATE TABLE project_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) UNIQUE,
    model ENUM('SCRUM', 'KANBAN', 'WATERFALL', 'HYBRID') NOT NULL,
    description TEXT,
    configuration JSON COMMENT 'Configuration for workflow, statuses, processes',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. PROJECTS TABLE
CREATE TABLE projects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    workspace_id INT NOT NULL,
    project_type_id INT,
    name VARCHAR(255) NOT NULL,
    project_code VARCHAR(50) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(500),
    goal TEXT,
    manager_id INT,
    status ENUM('NEW', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED') DEFAULT 'NEW',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    start_date DATE,
    due_date DATE,
    completed_at DATE,
    progress DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Percentage complete',
    created_by_id INT NOT NULL,
    board_config JSON COMMENT 'Column configuration, board workflow',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    FOREIGN KEY (project_type_id) REFERENCES project_types(id) ON DELETE SET NULL,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_project_code (project_code, workspace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. PROJECT MEMBERS TABLE
CREATE TABLE project_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    user_id INT NOT NULL,
    role_id INT NOT NULL COMMENT 'FK to Role (level = PROJECT)',
    status ENUM('ACTIVE', 'REMOVED') DEFAULT 'ACTIVE',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_project_user (project_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. SPRINTS TABLE
CREATE TABLE sprints (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sprint_code VARCHAR(50),
    goal TEXT,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'NOT_STARTED',
    start_date DATE,
    end_date DATE,
    duration_days INT COMMENT 'Expected duration in days',
    created_by_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. EPICS TABLE
CREATE TABLE epics (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    epic_code VARCHAR(50),
    description TEXT,
    color VARCHAR(7) DEFAULT '#8e44ad',
    status ENUM('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CLOSED') DEFAULT 'OPEN',
    start_date DATE,
    due_date DATE,
    created_by_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. TAGS TABLE
CREATE TABLE tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) DEFAULT '#95a5a6',
    description TEXT,
    created_by_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_tag_project (name, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. TASKS TABLE
CREATE TABLE tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    epic_id INT,
    sprint_id INT,
    parent_task_id INT COMMENT 'If it is a subtask',
    task_code VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    task_type ENUM('STORY', 'TASK', 'BUG', 'EPIC', 'SUBTASK') DEFAULT 'TASK',
    status VARCHAR(50) DEFAULT 'TO_DO',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    assigner_id INT,
    assignee_id INT,
    reviewer_id INT,
    story_points INT COMMENT 'Story points for Scrum',
    estimated_hours DECIMAL(10,2) COMMENT 'Estimated time (hours)',
    logged_hours DECIMAL(10,2) COMMENT 'Actual time logged (hours)',
    start_date DATE,
    due_date DATE,
    completed_at DATE,
    sort_order INT DEFAULT 0,
    created_by_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (epic_id) REFERENCES epics(id) ON DELETE SET NULL,
    FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (assigner_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_task_code (task_code, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. SUB-TASKS TABLE
CREATE TABLE sub_tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    parent_task_id INT NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status ENUM('TO_DO', 'IN_PROGRESS', 'DONE') DEFAULT 'TO_DO',
    assignee_id INT,
    estimated_hours DECIMAL(10,2) COMMENT 'Estimated time (hours)',
    sort_order INT DEFAULT 0,
    created_by_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. TASK-TAG MAPPING TABLE
CREATE TABLE task_tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tag_id INT NOT NULL,
    task_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tag_task (tag_id, task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. TASK COMMENTS TABLE
CREATE TABLE task_comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    task_id INT NOT NULL,
    commenter_id INT NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id INT COMMENT 'Reply to another comment',
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (commenter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES task_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. TASK ATTACHMENTS TABLE
CREATE TABLE task_attachments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    task_id INT NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT COMMENT 'File size in bytes',
    uploaded_by_id INT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. NOTIFICATIONS TABLE
CREATE TABLE notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    notification_type ENUM('SYSTEM', 'PROJECT', 'TASK', 'COMMENT', 'MENTION', 'DEADLINE') NOT NULL,
    link_url VARCHAR(500) COMMENT 'Link to the related content',
    project_id INT,
    task_id INT,
    created_by_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. USER NOTIFICATIONS TABLE
CREATE TABLE user_notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    notification_id INT NOT NULL,
    recipient_id INT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_notification_recipient (notification_id, recipient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 26. COMPANY INVITATIONS TABLE
CREATE TABLE company_invitations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    email VARCHAR(255) NOT NULL COMMENT 'Email of the invitee',
    role_id INT NOT NULL COMMENT 'Role to be assigned upon acceptance',
    invited_by_id INT NOT NULL COMMENT 'Admin who sent the invitation',
    
    token VARCHAR(255) NOT NULL UNIQUE COMMENT 'A unique token for this invitation',
    status ENUM('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING',
    
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    FOREIGN KEY (invited_by_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_company_email_pending (company_id, email, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- BƯỚC 2: NẠP ĐỊNH NGHĨA (QUYỀN & VAI TRÒ)
-- =============================================

-- NẠP QUYỀN (PERMISSIONS) - ĐÃ DỊCH SANG TIẾNG ANH
INSERT INTO permissions (permission_code, permission_name, group_name) VALUES
('company:create', 'Create Company', 'Company'),
('company:view', 'View Company', 'Company'),
('company:edit', 'Edit Company', 'Company'),
('company:delete', 'Delete Company', 'Company'),
('company:invite_member', 'Invite Company Member', 'Company'),
('company:remove_member', 'Remove Company Member', 'Company'),
('company:manage_roles', 'Manage Company Roles', 'Company'),
('workspace:create', 'Create Workspace', 'Workspace'),
('workspace:view', 'View Workspace', 'Workspace'),
('workspace:edit', 'Edit Workspace', 'Workspace'),
('workspace:delete', 'Delete Workspace', 'Workspace'),
('workspace:invite_member', 'Invite Workspace Member', 'Workspace'),
('workspace:remove_member', 'Remove Workspace Member', 'Workspace'),
('project:create', 'Create Project', 'Project'),
('project:view', 'View Project', 'Project'),
('project:edit', 'Edit Project', 'Project'),
('project:delete', 'Delete Project', 'Project'),
('project:invite_member', 'Invite Project Member', 'Project'),
('task:create', 'Create Task', 'Task'),
('task:view', 'View Task', 'Task'),
('task:edit', 'Edit Task', 'Task'),
('task:delete', 'Delete Task', 'Task'),
('task:assign', 'Assign Task', 'Task'),
('task:comment', 'Comment on Task', 'Task'),
('task:comment:view', 'View Task Comments', 'Task'),
('task:attach_file', 'Attach File to Task', 'Task');

-- NẠP VAI TRÒ (ROLES)
INSERT INTO roles (id, role_code, role_name, level) VALUES
(1,'SYSTEM_ADMIN', 'System Administrator', 'SYSTEM'),
(2,'USER', 'System User', 'SYSTEM'),
(3,'COMPANY_ADMIN', 'Company Administrator', 'COMPANY'),
(4,'COMPANY_MANAGER', 'Company Manager', 'COMPANY'),
(5,'COMPANY_MEMBER', 'Company Member', 'COMPANY'),
(6,'WORKSPACE_ADMIN', 'Workspace Administrator', 'WORKSPACE'),
(7,'WORKSPACE_MEMBER', 'Workspace Member', 'WORKSPACE'),
(8,'PROJECT_ADMIN', 'Project Admin', 'PROJECT'),
(9,'PROJECT_MEMBER', 'Project Member', 'PROJECT'),
(10,'GUEST_PROJECT', 'Project Guest', 'PROJECT');

-- =============================================
-- BƯỚC 3: LIÊN KẾT ROLE VÀ PERMISSION
-- =============================================

-- USER (System)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN ('company:create')
WHERE r.role_code = 'USER';

-- COMPANY_ADMIN (Company)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'company:view', 'company:edit', 'company:delete',
    'company:invite_member', 'company:remove_member',
    'company:manage_roles', 'workspace:create', 'workspace:delete'
) WHERE r.role_code = 'COMPANY_ADMIN';

-- COMPANY_MANAGER (Company)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'company:view', 'company:invite_member', 'workspace:create'
) WHERE r.role_code = 'COMPANY_MANAGER';

-- COMPANY_MEMBER (Company)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'company:view', 'workspace:create', 'project:create'
) WHERE r.role_code = 'COMPANY_MEMBER';

-- WORKSPACE_ADMIN (Workspace)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'workspace:view', 'workspace:edit', 'workspace:invite_member',
    'workspace:remove_member', 'project:create', 'project:delete'
) WHERE r.role_code = 'WORKSPACE_ADMIN';

-- WORKSPACE_MEMBER (Workspace)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'workspace:view', 'project:create'
) WHERE r.role_code = 'WORKSPACE_MEMBER';

-- PROJECT_ADMIN (Project)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'project:view', 'project:edit', 'project:invite_member',
    'task:create', 'task:view', 'task:edit', 'task:delete',
    'task:assign', 'task:comment', 'task:comment:view', 'task:attach_file'
) WHERE r.role_code = 'PROJECT_ADMIN';

-- PROJECT_MEMBER (Project)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'project:view', 'task:create', 'task:view',
    'task:edit', 'task:comment', 'task:comment:view', 'task:attach_file'
) WHERE r.role_code = 'PROJECT_MEMBER';

-- GUEST_PROJECT (Project)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'project:view', 'task:view', 'task:comment:view'
) WHERE r.role_code = 'GUEST_PROJECT';


-- =============================================
-- BƯỚC 4: TẠO DỮ LIỆU THỰC TẾ (REALISTIC DATA)
-- Mật khẩu cho tất cả user: admin123
-- =============================================

-- TẠO CÁC USER (BẮT ĐẦU TỪ ID 1)
INSERT INTO users (id, email, password, full_name, is_email_verified, status) VALUES
(1, 'anna.admin@pixelcore.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Anna Admin (C-Admin)', 1, 'ACTIVE'),
(2, 'brian.manager@pixelcore.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Brian Manager (C-Manager)', 1, 'ACTIVE'),
(3, 'charlie.member@pixelcore.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Charlie Member (C-Member)', 1, 'ACTIVE'),
(4, 'david.lead@pixelcore.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'David Lead (W-Admin)', 1, 'ACTIVE'),
(5, 'eva.dev@pixelcore.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Eva Developer (P-Member)', 1, 'ACTIVE'),
(6, 'frank.client@external.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Frank Client (P-Guest)', 1, 'ACTIVE'),
(7, 'new.user@gmail.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'New User (System-User)', 1, 'ACTIVE'),
(8, 'admin@quantum.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Quantum Admin (C-Admin 2)', 1, 'ACTIVE');

-- TẠO MÔI TRƯỜNG (COMPANIES, WORKSPACES, PROJECTS, TASKS)
INSERT INTO companies (id, name, company_code, created_by_id, status) VALUES
(1, 'PixelCore Inc.', 'PIXEL', 1, 'ACTIVE'),
(2, 'QuantumLeap Solutions', 'QUANTUM', 8, 'ACTIVE');

INSERT INTO workspaces (id, company_id, name, created_by_id, status) VALUES
(1, 1, 'Marketing', 1, 'ACTIVE'),
(2, 1, 'Engineering', 1, 'ACTIVE'),
(3, 2, 'Quantum Sales', 8, 'ACTIVE');

INSERT INTO projects (id, workspace_id, name, project_code, created_by_id, status) VALUES
(1, 2, 'Website Redesign', 'WEB', 4, 'IN_PROGRESS'),
(2, 1, 'Q4 Campaign', 'MKTG', 2, 'NEW');

INSERT INTO tasks (id, project_id, task_code, title, description, created_by_id, assignee_id, priority, due_date) VALUES
(1, 1, 'WEB-1', 'Design Homepage Mockup', 'Create mockups in Figma', 4, 5, 'HIGH', '2025-11-20'),
(2, 1, 'WEB-2', 'Develop Auth API', 'Setup JWT and endpoints', 4, 5, 'URGENT', '2025-11-15'),
(3, 2, 'MKTG-1', 'Plan Social Media', 'Draft posts for LinkedIn', 2, 2, 'MEDIUM', '2025-11-10');

-- =============================================
-- BƯỚC 5: GÁN VAI TRÒ CHO CÁC NHÂN VẬT
-- =============================================

-- GÁN VAI TRÒ CẤP HỆ THỐNG
INSERT INTO user_roles (user_id, role_id) VALUES
(7, (SELECT id FROM roles WHERE role_code = 'USER'));

-- GÁN VAI TRÒ CẤP CÔNG TY
INSERT INTO company_members (company_id, user_id, role_id, status) VALUES
-- Công ty 1: PixelCore
(1, 1, (SELECT id FROM roles WHERE role_code = 'COMPANY_ADMIN'), 'ACTIVE'),   -- Anna Admin
(1, 2, (SELECT id FROM roles WHERE role_code = 'COMPANY_MANAGER'), 'ACTIVE'), -- Brian Manager
(1, 3, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE'),  -- Charlie Member
(1, 4, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE'),  -- David Lead
(1, 5, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE'),  -- Eva Developer
(1, 6, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE'),  -- Frank Client
SELECT * FROM company_members
-- Công ty 2: QuantumLeap
(2, 8, (SELECT id FROM roles WHERE role_code = 'COMPANY_ADMIN'), 'ACTIVE');   -- Quantum Admin

-- GÁN VAI TRÒ CẤP WORKSPACE
INSERT INTO workspace_members (workspace_id, user_id, role_id, status) VALUES
-- Workspace 1: Marketing
(1, 2, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'), -- Brian Manager
-- Workspace 2: Engineering
(2, 4, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE'),  -- David Lead
(2, 5, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'), -- Eva Developer
(2, 6, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'); -- Frank Client

-- GÁN VAI TRÒ CẤP PROJECT
INSERT INTO project_members (project_id, user_id, role_id, status) VALUES
-- Project 1: Website Redesign
(1, 4, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE'),  -- David Lead
(1, 5, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE'), -- Eva Developer
(1, 6, (SELECT id FROM roles WHERE role_code = 'GUEST_PROJECT'), 'ACTIVE'); -- Frank Client

-- SAMPLE DATA TOKENS
INSERT INTO auth_tokens (id, user_id, token, token_type, status, expires_at) VALUES
(1, 1, 'token-email-user-1', 'EMAIL_VERIFICATION', 'REVOKED', '2025-01-01 00:00:00'),
(2, 2, 'token-email-user-2', 'EMAIL_VERIFICATION', 'REVOKED', '2025-01-01 00:00:00'),
(3, 3, 'token-email-user-3', 'EMAIL_VERIFICATION', 'REVOKED', '2025-01-01 00:00:00'),
(4, 4, 'token-email-user-4', 'EMAIL_VERIFICATION', 'REVOKED', '2025-01-01 00:00:00'),
(5, 5, '123456', 'EMAIL_VERIFICATION', 'ACTIVE', '2025-12-01 00:00:00');

-- =============================================
-- BƯỚC 6: THÊM DỮ LIỆU MẪU (THEO YÊU CẦU)
-- =============================================

-- SAMPLE DATA INVITATIONS
INSERT INTO company_invitations (company_id, email, role_id, invited_by_id, token, status, expires_at) VALUES
-- Anna (ID 1) mời 'user.new@example.com' làm COMPANY_MANAGER (ID 4) cho Cty 1
(1, 'user.new@example.com', 4, 1, 'token-pending-1', 'PENDING', '2025-12-01 00:00:00'),
-- Quantum Admin (ID 8) mời 'user.accepted@example.com' làm COMPANY_MANAGER (ID 4) cho Cty 2
(2, 'accepted.user@example.com', 4, 8, 'token-accepted-3', 'ACCEPTED', '2025-10-01 00:00:00'),
(2, 'expired.user@example.com', 4, 8, 'token-expired-4', 'EXPIRED', '2025-10-01 00:00:00');

-- SAMPLE DATA TOKENS
INSERT INTO auth_tokens (user_id, token, token_type, status, expires_at) VALUES
(1, 'token-anna-reset', 'RESET_PASSWORD', 'ACTIVE', '2025-12-01 00:00:00'),
(2, 'token-brian-verify', 'EMAIL_VERIFICATION', 'ACTIVE', '2025-12-01 00:00:00'),
(7, 'token-newuser-verify', 'EMAIL_VERIFICATION', 'ACTIVE', '2025-12-01 00:00:00');

-- (Chạy lại câu lệnh SELECT ở trên)


-- ALL PERMISSION
SELECT
    p.group_name AS permission_group,
    p.permission_code,
    p.permission_name AS description,
    IFNULL(
        -- Nối tất cả các role_code lại thành một chuỗi
        GROUP_CONCAT(DISTINCT r.role_code ORDER BY r.role_code SEPARATOR ', '), 
        '--- CHƯA GÁN CHO VAI TRÒ NÀO ---'
    ) AS granted_to_roles
FROM permissions p
-- Dùng LEFT JOIN để hiển thị cả các quyền chưa được gán
LEFT JOIN role_permissions rp ON p.id = rp.permission_id
LEFT JOIN roles r ON r.id = rp.role_id
GROUP BY 
    p.id  -- Nhóm theo ID của quyền
ORDER BY
    -- Sắp xếp theo nhóm cho dễ nhìn
    FIELD(p.group_name, 'Company', 'Workspace', 'Project', 'Task'), 
    p.permission_code;

-- Permission by Role
SELECT
    r.level AS role_level,
    r.role_code,
    r.role_name,
    IFNULL(
        -- Nối tất cả các permission_code lại
        GROUP_CONCAT(DISTINCT p.permission_code ORDER BY p.permission_code SEPARATOR ', '), 
        '--- KHÔNG CÓ QUYỀN NÀO ---'
    ) AS granted_permissions
FROM roles r
-- Dùng LEFT JOIN để hiển thị cả các vai trò chưa có quyền
LEFT JOIN role_permissions rp ON r.id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.id
GROUP BY 
    r.id -- Nhóm theo ID của vai trò
ORDER BY
    FIELD(r.level, 'SYSTEM', 'COMPANY', 'WORKSPACE', 'PROJECT'),
    r.role_code;

