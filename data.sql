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
-- BƯỚC 1: TẠO CẤU TRÚC BẢNG (SCHEMA)
-- =============================================

-- 1. Users & Roles
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

CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(100) NOT NULL UNIQUE,
    role_name VARCHAR(255) NOT NULL,
    description TEXT,
    level ENUM('SYSTEM', 'COMPANY', 'WORKSPACE', 'PROJECT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(255) NOT NULL,
    group_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. System Config
CREATE TABLE project_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) UNIQUE,
    model ENUM('SCRUM', 'KANBAN', 'WATERFALL', 'HYBRID') NOT NULL,
    description TEXT,
    configuration JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE user_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
    display_mode ENUM('LIGHT', 'DARK', 'SYSTEM') DEFAULT 'LIGHT',
    email_notifications BOOLEAN DEFAULT TRUE,
    push_notifications BOOLEAN DEFAULT TRUE,
    default_homepage VARCHAR(50) DEFAULT 'dashboard',
    board_config JSON,
    list_config JSON,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id INT,
    entity_name VARCHAR(500),
    entity_code VARCHAR(50),
    company_id INT,
    workspace_id INT,
    project_id INT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    -- TẠO INDEX (Giúp query nhanh hơn)
    KEY idx_logs_company (company_id),
    KEY idx_logs_workspace (workspace_id),
    KEY idx_logs_project (project_id),
    KEY idx_logs_user (user_id),
    KEY idx_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Organization Structure
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

CREATE TABLE company_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
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

CREATE TABLE company_invitations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    invited_by_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    status ENUM('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    FOREIGN KEY (invited_by_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_company_email_pending (company_id, email, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE workspace_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    workspace_id INT NOT NULL,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    status ENUM('ACTIVE', 'REMOVED') DEFAULT 'ACTIVE',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_workspace_user (workspace_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Project Structure
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
    progress DECIMAL(5,2) DEFAULT 0.00,
    created_by_id INT NOT NULL,
    board_config JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    FOREIGN KEY (project_type_id) REFERENCES project_types(id) ON DELETE SET NULL,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_project_code (project_code, workspace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE project_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    status ENUM('ACTIVE', 'REMOVED') DEFAULT 'ACTIVE',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_project_user (project_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE project_invitations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    invited_by_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (invited_by_id) REFERENCES users(id)
);

-- 5. Project Internals
CREATE TABLE sprints (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sprint_code VARCHAR(50),
    goal TEXT,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'NOT_STARTED',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    duration_days INT,
    created_by_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) DEFAULT '#95a5a6',
    description TEXT,
    created_by_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_tag_project (name, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE project_statuses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) DEFAULT '#CCCCCC',
    sort_order INT NOT NULL DEFAULT 0,
    is_completed_status BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE KEY uk_project_name (project_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tasks (Phụ thuộc nhiều bảng khác)
CREATE TABLE tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    epic_id INT,
    sprint_id INT,
    parent_task_id INT,
    task_code VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    task_type ENUM('STORY', 'TASK', 'BUG', 'EPIC', 'SUBTASK') DEFAULT 'TASK',
    status_id INT, 
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    is_archived BOOLEAN DEFAULT FALSE,
    assigner_id INT,
    assignee_id INT,
    reviewer_id INT,
    story_points INT,
    estimated_hours DECIMAL(10,2),
    logged_hours DECIMAL(10,2),
    start_date TIMESTAMP,
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
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
    FOREIGN KEY (status_id) REFERENCES project_statuses(id) ON DELETE SET NULL,
    UNIQUE KEY uk_task_code (task_code, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Task Sub-components
CREATE TABLE sub_tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    parent_task_id INT NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status ENUM('TO_DO', 'IN_PROGRESS', 'DONE') DEFAULT 'TO_DO',
    assignee_id INT,
    estimated_hours DECIMAL(10,2),
    sort_order INT DEFAULT 0,
    created_by_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE task_tags (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tag_id INT NOT NULL,
    task_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tag_task (tag_id, task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE task_comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    task_id INT NOT NULL,
    commenter_id INT NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id INT,
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (commenter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES task_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE task_attachments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    task_id INT NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    uploaded_by_id INT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Notifications
CREATE TABLE notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    notification_type ENUM('SYSTEM', 'PROJECT', 'TASK', 'COMMENT', 'MENTION', 'DEADLINE') NOT NULL,
    link_url VARCHAR(500),
    project_id INT,
    task_id INT,
    created_by_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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


-- =============================================
-- BƯỚC 2: NẠP ĐỊNH NGHĨA (QUYỀN & VAI TRÒ)
-- =============================================
-- LOAD PERMISSIONS (English)
INSERT INTO permissions (permission_code, permission_name, group_name) VALUES
-- Company Group
('company:create', 'Create Company', 'Company'),
('company:view', 'View Company', 'Company'),
('company:edit', 'Edit Company', 'Company'),
('company:delete', 'Delete Company', 'Company'),
('company:invite_member', 'Invite Company Member', 'Company'),
('company:remove_member', 'Remove Company Member', 'Company'),
('company:manage_roles', 'Manage Company Roles', 'Company'),

-- Workspace Group
('workspace:create', 'Create Workspace', 'Workspace'),
('workspace:view', 'View Workspace', 'Workspace'),
('workspace:edit', 'Edit Workspace', 'Workspace'),
('workspace:delete', 'Delete Workspace', 'Workspace'),
('workspace:invite_member', 'Invite Workspace Member', 'Workspace'),
('workspace:remove_member', 'Remove Workspace Member', 'Workspace'),
('workspace:manage_roles', 'Manage Workspace Roles', 'Workspace'),

-- Project Group
('project:create', 'Create Project', 'Project'),
('project:view', 'View Project', 'Project'),
('project:edit', 'Edit Project', 'Project'),
('project:delete', 'Delete Project', 'Project'),
('project:invite_member', 'Invite Project Member', 'Project'),
('project:manage_roles', 'Manage Project Roles', 'Project'),

-- Task Group
('task:create', 'Create Task', 'Task'),
('task:view', 'View Task', 'Task'),
('task:edit', 'Edit Task', 'Task'),
('task:delete', 'Delete Task', 'Task'),
('task:assign', 'Assign Task', 'Task'),
('task:comment', 'Comment on Task', 'Task'),
('task:comment:view', 'View Comments', 'Task'),
('task:attach_file', 'Attach File', 'Task'),

-- Sprint Group
('sprint:create', 'Create Sprint', 'Sprint'),
('sprint:start', 'Start Sprint', 'Sprint'),
('sprint:edit', 'Edit Sprint', 'Sprint'),
('sprint:delete', 'Delete Sprint', 'Sprint'),

-- Backlog Group
('backlog:view', 'View Backlog', 'Backlog'),
('backlog:manage', 'Manage Backlog', 'Backlog');

-- LOAD ROLES (English)
INSERT INTO roles (id, role_code, role_name, level, description) VALUES
(1, 'SYSTEM_ADMIN', 'System Administrator', 'SYSTEM', 'Full system access privileges'),
(2, 'USER', 'System User', 'SYSTEM', 'Basic user, ability to create companies'),
(3, 'COMPANY_ADMIN', 'Company Administrator', 'COMPANY', 'Full access within their company'),
(4, 'COMPANY_MEMBER', 'Company Member', 'COMPANY', 'Standard company member'),
(5, 'WORKSPACE_ADMIN', 'Workspace Administrator', 'WORKSPACE', 'Manages a specific workspace'),
(6, 'WORKSPACE_MEMBER', 'Workspace Member', 'WORKSPACE', 'Standard workspace member'),
(7, 'PROJECT_ADMIN', 'Project Administrator', 'PROJECT', 'Manages a specific project (Project Manager)'),
(8, 'PROJECT_MEMBER', 'Project Member', 'PROJECT', 'Standard project member (Dev, QA, etc.)'),
(9, 'GUEST_PROJECT', 'Project Guest', 'PROJECT', 'Read-only access to the project (Client/Stakeholder)');

-- =============================================
-- BƯỚC 3: LIÊN KẾT ROLE VÀ PERMISSION
-- =============================================
-- USER (System)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'company:create'
) WHERE r.role_code = 'USER';

-- COMPANY_ADMIN (Company)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'company:view', 'company:edit', 'company:delete', 'company:invite_member', 'company:remove_member', 'company:manage_roles',
    'workspace:create', 'workspace:delete', 'workspace:view', 'workspace:edit', 'workspace:invite_member', 'workspace:remove_member', 'workspace:manage_roles',
    'project:create', 'project:delete', 'project:view', 'project:edit', 'project:invite_member', 'project:manage_roles',
    'task:assign', 'task:attach_file', 'task:comment', 'task:comment:view', 'task:create', 'task:delete', 'task:edit', 'task:view',
    'sprint:create', 'sprint:start', 'sprint:edit', 'sprint:delete',
    'backlog:view', 'backlog:manage'
) WHERE r.role_code = 'COMPANY_ADMIN';

-- COMPANY_MEMBER (Company)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'company:view',
    'workspace:create'
) WHERE r.role_code = 'COMPANY_MEMBER';

-- WORKSPACE_ADMIN (Workspace)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'workspace:view', 'workspace:edit', 'workspace:invite_member', 'workspace:remove_member', 'workspace:manage_roles',
    'project:create', 'project:delete', 'project:view', 'project:edit', 'project:invite_member', 'project:manage_roles'
) WHERE r.role_code = 'WORKSPACE_ADMIN';

-- WORKSPACE_MEMBER (Workspace)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'workspace:view',
    'project:create',
    'project:view'
) WHERE r.role_code = 'WORKSPACE_MEMBER';

-- PROJECT_ADMIN (Project)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'project:view', 'project:edit', 'project:invite_member', 'project:manage_roles',
    'task:create', 'task:view', 'task:edit', 'task:delete', 'task:assign', 'task:comment', 'task:comment:view', 'task:attach_file',
    'sprint:create', 'sprint:start', 'sprint:edit', 'sprint:delete',
    'backlog:view', 'backlog:manage'
) WHERE r.role_code = 'PROJECT_ADMIN';

-- PROJECT_MEMBER (Project)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'project:view',
    'task:create', 'task:view', 'task:edit', 'task:comment', 'task:comment:view', 'task:attach_file',
    'backlog:view', 'backlog:manage'
) WHERE r.role_code = 'PROJECT_MEMBER';

-- GUEST_PROJECT (Project)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.permission_code IN (
    'project:view',
    'task:view',
    'task:comment',
    'task:comment:view'
) WHERE r.role_code = 'GUEST_PROJECT';


-- =============================================
-- BƯỚC 4: TẠO DỮ LIỆU MỞ RỘNG - USERS (15 users)
-- Mật khẩu: admin123 (Hash: $2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG)
-- =============================================

INSERT INTO users (id, email, password, full_name, avatar_url, phone_number, date_of_birth, gender, status, is_email_verified, created_at, last_login_at) VALUES
(1, 'super.admin@system.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'System Administrator', 'https://ui-avatars.com/api/?name=System+Admin&background=2c3e50&color=fff', '0900000001', '1985-03-15', 'MALE', 'ACTIVE', 1, '2024-01-10 08:00:00', '2025-11-19 07:30:00'),
(2, 'system.user@system.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'System User', 'https://ui-avatars.com/api/?name=System+User&background=95a5a6&color=fff', '0900000002', '1992-07-22', 'FEMALE', 'ACTIVE', 1, '2024-02-15 09:00:00', '2025-11-18 14:20:00'),
(3, 'admin@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Nguyễn Văn An', 'https://ui-avatars.com/api/?name=Nguyen+Van+An&background=3498db&color=fff', '0901234001', '1988-03-20', 'MALE', 'ACTIVE', 1, '2024-06-01 08:30:00', '2025-11-19 08:15:00'),
(4, 'manager@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Trần Thị Bình', 'https://ui-avatars.com/api/?name=Tran+Thi+Binh&background=e74c3c&color=fff', '0901234002', '1990-07-12', 'FEMALE', 'ACTIVE', 1, '2024-06-05 09:00:00', '2025-11-19 06:45:00'),
(5, 'pm1@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Lê Văn Cường', 'https://ui-avatars.com/api/?name=Le+Van+Cuong&background=f39c12&color=fff', '0901234003', '1992-11-05', 'MALE', 'ACTIVE', 1, '2024-06-10 10:00:00', '2025-11-19 09:00:00'),
(6, 'po@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Phạm Thị Dung', 'https://ui-avatars.com/api/?name=Pham+Thi+Dung&background=9b59b6&color=fff', '0901234004', '1995-02-28', 'FEMALE', 'ACTIVE', 1, '2024-06-15 11:00:00', '2025-11-18 16:30:00'),
(7, 'dev1@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Hoàng Văn Em', 'https://ui-avatars.com/api/?name=Hoang+Van+Em&background=16a085&color=fff', '0901234005', '1993-09-18', 'MALE', 'ACTIVE', 1, '2024-07-01 08:00:00', '2025-11-19 08:45:00'),
(8, 'dev2@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Võ Thị Phương', 'https://ui-avatars.com/api/?name=Vo+Thi+Phuong&background=2ecc71&color=fff', '0901234006', '1994-06-22', 'FEMALE', 'ACTIVE', 1, '2024-07-05 09:30:00', '2025-11-19 07:20:00'),
(9, 'designer@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Nguyễn Thị Giang', 'https://ui-avatars.com/api/?name=Nguyen+Thi+Giang&background=e91e63&color=fff', '0901234007', '1996-12-30', 'FEMALE', 'ACTIVE', 1, '2024-07-10 10:00:00', '2025-11-18 17:00:00'),
(10, 'tester@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Đỗ Văn Hải', 'https://ui-avatars.com/api/?name=Do+Van+Hai&background=34495e&color=fff', '0901234008', '1997-04-15', 'MALE', 'ACTIVE', 1, '2024-07-15 08:30:00', '2025-11-19 09:10:00'),
(11, 'dev3@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Bùi Văn Khánh', 'https://ui-avatars.com/api/?name=Bui+Van+Khanh&background=1abc9c&color=fff', '0901234009', '1995-08-10', 'MALE', 'ACTIVE', 1, '2024-07-20 09:00:00', '2025-11-19 08:00:00'),
(12, 'dev4@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Đặng Thị Lan', 'https://ui-avatars.com/api/?name=Dang+Thi+Lan&background=e67e22&color=fff', '0901234010', '1996-05-25', 'FEMALE', 'ACTIVE', 1, '2024-07-25 10:00:00', '2025-11-18 15:30:00'),
(13, 'ba@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Trần Văn Minh', 'https://ui-avatars.com/api/?name=Tran+Van+Minh&background=9b59b6&color=fff', '0901234011', '1994-10-08', 'MALE', 'ACTIVE', 1, '2024-08-01 11:00:00', '2025-11-19 07:45:00'),
(14, 'scrum@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Lê Thị Nga', 'https://ui-avatars.com/api/?name=Le+Thi+Nga&background=f39c12&color=fff', '0901234012', '1993-03-17', 'FEMALE', 'ACTIVE', 1, '2024-08-05 09:30:00', '2025-11-18 16:00:00'),
(15, 'pm2@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Phan Văn Oanh', 'https://ui-avatars.com/api/?name=Phan+Van+Oanh&background=7f8c8d&color=fff', '0901234013', '1991-07-21', 'OTHER', 'ACTIVE', 1, '2024-08-10 10:30:00', '2025-11-10 14:20:00'),
(16, 'admin@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Ngô Văn Phúc', 'https://ui-avatars.com/api/?name=Ngo+Van+Phuc&background=3498db&color=fff', '0902345001', '1989-11-30', 'MALE', 'ACTIVE', 1, '2024-08-15 08:00:00', '2025-11-19 06:30:00'),
(17, 'pm@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Vũ Thị Quỳnh', 'https://ui-avatars.com/api/?name=Vu+Thi+Quynh&background=e74c3c&color=fff', '0902345002', '1992-02-14', 'FEMALE', 'ACTIVE', 1, '2024-08-20 09:00:00', '2025-11-18 18:00:00'),
(18, 'dev@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Trương Văn Sơn', 'https://ui-avatars.com/api/?name=Truong+Van+Son&background=2ecc71&color=fff', '0902345003', '1996-08-19', 'MALE', 'ACTIVE', 1, '2024-08-25 10:00:00', '2025-11-19 08:30:00'),
(19, 'designer@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Mai Thị Thu', 'https://ui-avatars.com/api/?name=Mai+Thi+Thu&background=e91e63&color=fff', '0902345004', '1995-09-12', 'FEMALE', 'ACTIVE', 1, '2024-08-30 11:00:00', '2025-11-18 14:45:00'),
(20, 'tester@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Đinh Văn Toàn', 'https://ui-avatars.com/api/?name=Dinh+Van+Toan&background=34495e&color=fff', '0902345005', '1994-06-07', 'MALE', 'ACTIVE', 1, '2024-09-05 08:30:00', '2025-11-19 09:15:00'),
(21, 'admin@digitalwave.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Cao Thị Uyên', 'https://ui-avatars.com/api/?name=Cao+Thi+Uyen&background=9b59b6&color=fff', '0903456001', '1990-12-15', 'FEMALE', 'ACTIVE', 1, '2024-09-10 09:00:00', '2025-11-18 16:20:00'),
(22, 'content@digitalwave.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Hồ Văn Vinh', 'https://ui-avatars.com/api/?name=Ho+Van+Vinh&background=16a085&color=fff', '0903456002', '1993-04-28', 'MALE', 'ACTIVE', 1, '2024-09-15 10:00:00', '2025-11-19 07:50:00'),
(23, 'social@digitalwave.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Lương Thị Xuân', 'https://ui-avatars.com/api/?name=Luong+Thi+Xuan&background=f39c12&color=fff', '0903456003', '1997-01-20', 'FEMALE', 'ACTIVE', 1, '2024-09-20 11:00:00', '2025-11-18 15:10:00');

-- =============================================
-- GÁN VAI TRÒ CẤP HỆ THỐNG
-- =============================================
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, (SELECT id FROM roles WHERE role_code = 'SYSTEM_ADMIN')),
(2, (SELECT id FROM roles WHERE role_code = 'USER'));

-- =============================================
-- COMPANIES (5 công ty)
-- =============================================
INSERT INTO companies (id, name, company_code, description, logo_url, created_by_id, status, address, phone_number, email, website, created_at, updated_at) VALUES
(1, 'TechVision Solutions', 'TECHV', 'Leading software development company specializing in AI, Big Data, and Enterprise Solutions', 'https://ui-avatars.com/api/?name=TechVision&background=3498db&color=fff&size=200', 3, 'ACTIVE', '123 Võ Văn Tần, Phường 6, Quận 3, TP. Hồ Chí Minh', '02838123456', 'contact@techvision.com', 'https://techvision.com', '2024-06-01 08:30:00', '2025-10-15 14:20:00'),
(2, 'InnovaTech Group', 'INNOV', 'Innovation-driven technology company focusing on IoT, Smart City solutions and Digital Transformation', 'https://ui-avatars.com/api/?name=InnovaTech&background=1abc9c&color=fff&size=200', 16, 'ACTIVE', '456 Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', '02838765432', 'info@innovatech.com', 'https://innovatech.com', '2024-08-01 09:00:00', '2025-09-20 10:30:00'),
(3, 'DigitalWave Agency', 'DIGW', 'Creative digital marketing agency specializing in branding, social media, and content strategy', 'https://ui-avatars.com/api/?name=DigitalWave&background=e74c3c&color=fff&size=200', 21, 'ACTIVE', '789 Hai Bà Trưng, Quận Hải Châu, TP. Đà Nẵng', '02363123789', 'hello@digitalwave.com', 'https://digitalwave.com', '2024-09-01 08:00:00', '2025-11-01 09:15:00'),
(4, 'CloudSoft Solutions', 'CLOUD', 'Cloud computing and SaaS platform provider for enterprises', 'https://ui-avatars.com/api/?name=CloudSoft&background=27ae60&color=fff&size=200', 2, 'ACTIVE', '321 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', '02838999888', 'info@cloudsoft.com', 'https://cloudsoft.com', '2024-05-10 10:00:00', '2025-10-05 11:00:00'),
(5, 'DataMind Analytics', 'DATAM', 'Data analytics and business intelligence consulting firm', 'https://ui-avatars.com/api/?name=DataMind&background=8e44ad&color=fff&size=200', 2, 'ACTIVE', '555 Lý Thường Kiệt, Quận 10, TP. Hồ Chí Minh', '02838777666', 'contact@datamind.com', 'https://datamind.com', '2024-07-01 09:30:00', '2025-09-15 10:00:00');

-- =============================================
-- COMPANY MEMBERS (Đã sửa lại khớp với Users)
-- =============================================
INSERT INTO company_members (company_id, user_id, role_id, status, job_title, department, joined_at) VALUES
-- Company 1: TechVision (Users 3-15)
(1, 3, (SELECT id FROM roles WHERE role_code = 'COMPANY_ADMIN'), 'ACTIVE', 'Giám đốc Điều hành', 'Ban Giám đốc', '2024-06-01 08:30:00'),
(1, 4, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Trưởng phòng Marketing', 'Marketing', '2024-06-05 09:00:00'),
(1, 5, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Senior Project Manager', 'Engineering', '2024-06-10 10:00:00'),
(1, 6, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Product Owner', 'Product', '2024-06-15 11:00:00'),
(1, 7, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Senior Backend Developer', 'Engineering', '2024-07-01 08:00:00'),
(1, 8, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Full-stack Developer', 'Engineering', '2024-07-05 09:30:00'),
(1, 9, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'UI/UX Designer Lead', 'Design', '2024-07-10 10:00:00'),
(1, 10, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'QA Lead', 'Engineering', '2024-07-15 08:30:00'),
(1, 11, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Frontend Developer', 'Engineering', '2024-07-20 09:00:00'),
(1, 12, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Mobile Developer', 'Engineering', '2024-07-25 10:00:00'),
(1, 13, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Business Analyst', 'Analysis', '2024-08-01 11:00:00'),
(1, 14, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Scrum Master', 'Engineering', '2024-08-05 09:30:00'),
(1, 15, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Project Manager', 'Engineering', '2024-08-10 10:30:00'),

-- Company 2: InnovaTech (Users 16-20)
(2, 3, (SELECT id FROM roles WHERE role_code = 'COMPANY_ADMIN'), 'ACTIVE', 'Giám đốc Điều hành', 'Ban Giám đốc', '2024-06-01 08:30:00'),
(2, 16, (SELECT id FROM roles WHERE role_code = 'COMPANY_ADMIN'), 'ACTIVE', 'Giám đốc', 'Ban Giám đốc', '2024-08-15 08:00:00'),
(2, 17, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Project Manager', 'R&D', '2024-08-20 09:00:00'),
(2, 18, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'IoT Developer', 'R&D', '2024-08-25 10:00:00'),
(2, 19, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Lead Designer', 'Design', '2024-08-30 11:00:00'),
(2, 20, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'QA Engineer', 'Quality', '2024-09-05 08:30:00'),

-- Company 3: DigitalWave (Users 21-23)
(3, 21, (SELECT id FROM roles WHERE role_code = 'COMPANY_ADMIN'), 'ACTIVE', 'Giám đốc Sáng tạo', 'Creative', '2024-09-10 09:00:00'),
(3, 22, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Content Manager', 'Marketing', '2024-09-15 10:00:00'),
(3, 23, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Social Media Specialist', 'Marketing', '2024-09-20 11:00:00');

-- =============================================
-- WORKSPACES (12 workspaces)
-- =============================================
INSERT INTO workspaces (id, company_id, name, workspace_code, created_by_id, status, description, cover_image_url, color, created_at, updated_at) VALUES
-- Company 1: TechVision (6 workspaces)
(1, 1, 'Engineering & Development', 'ENG-DEV', 3, 'ACTIVE', 'Main workspace for software engineering and development teams', 'https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=1200&h=400&fit=crop', '#3498db', '2024-06-02 09:00:00', '2025-10-20 11:00:00'),
(2, 1, 'Design & Product', 'DESIGN-PROD', 3, 'ACTIVE', 'Product design, UX/UI and product management workspace', 'https://images.unsplash.com/photo-1557804506-669a67965ba0?w=1200&h=400&fit=crop', '#9b59b6', '2024-06-02 09:30:00', '2025-11-05 15:30:00'),
(3, 1, 'Marketing & Sales', 'MKT-SALES', 4, 'ACTIVE', 'Marketing campaigns, sales operations and customer success', 'https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=1200&h=400&fit=crop', '#e67e22', '2024-06-15 10:00:00', '2025-11-10 14:00:00'),
(4, 1, 'Quality Assurance', 'QA-TEST', 10, 'ACTIVE', 'Testing, quality assurance and automation workspace', 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=1200&h=400&fit=crop', '#27ae60', '2024-06-20 08:00:00', '2025-10-25 09:00:00'),
(5, 1, 'Mobile Development', 'MOBILE-DEV', 5, 'ACTIVE', 'iOS and Android mobile application development', 'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=1200&h=400&fit=crop', '#e74c3c', '2024-07-01 10:00:00', '2025-11-08 16:00:00'),
(6, 1, 'Research & Innovation', 'R-D', 13, 'ACTIVE', 'R&D projects, POCs and innovation initiatives', 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&h=400&fit=crop', '#16a085', '2024-07-10 11:00:00', '2025-09-30 10:00:00'),

-- Company 2: InnovaTech (3 workspaces)
(7, 2, 'IoT Innovation Lab', 'IOT-LAB', 16, 'ACTIVE', 'IoT research, smart devices and connected systems', 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&h=400&fit=crop', '#1abc9c', '2024-08-16 10:00:00', '2025-10-25 16:00:00'),
(8, 2, 'Smart City Solutions', 'SMART-CITY', 16, 'ACTIVE', 'Smart city infrastructure and urban tech projects', 'https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?w=1200&h=400&fit=crop', '#f39c12', '2024-08-20 09:00:00', '2025-11-02 14:30:00'),
(9, 2, 'Digital Transformation', 'DX', 17, 'ACTIVE', 'Enterprise digital transformation consulting projects', 'https://images.unsplash.com/photo-1551434678-e076c223a692?w=1200&h=400&fit=crop', '#8e44ad', '2024-08-25 10:30:00', '2025-10-18 11:00:00'),

-- Company 3: DigitalWave (3 workspaces)
(10, 3, 'Creative Studio', 'CREATIVE', 21, 'ACTIVE', 'Design, branding and creative content production', 'https://images.unsplash.com/photo-1496065187959-7e07b8353c55?w=1200&h=400&fit=crop', '#e74c3c', '2024-09-11 09:00:00', '2025-11-12 10:30:00'),
(11, 3, 'Social Media Management', 'SOCIAL', 21, 'ACTIVE', 'Social media campaigns and community management', 'https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=1200&h=400&fit=crop', '#2ecc71', '2024-09-15 10:00:00', '2025-11-08 15:00:00'),
(12, 3, 'Content Strategy', 'CONTENT', 22, 'ACTIVE', 'Content planning, creation and distribution', 'https://images.unsplash.com/photo-1552664730-d307ca884978?w=1200&h=400&fit=crop', '#f39c12', '2024-09-20 11:00:00', '2025-11-05 09:30:00');

-- =============================================
-- WORKSPACE MEMBERS
-- =============================================
INSERT INTO workspace_members (workspace_id, user_id, role_id, status, joined_at) VALUES
-- Workspace 1: Engineering & Development (TechVision)
(1, 3, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-06-02 09:00:00'),
(1, 5, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-06-10 10:00:00'),
(1, 7, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-01 08:00:00'),
(1, 8, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-05 09:30:00'),
(1, 10, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-15 08:30:00'),
(1, 11, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-20 09:00:00'),
(1, 12, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-25 10:00:00'),
(1, 14, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-08-05 09:30:00'),

-- Workspace 2: Design & Product (TechVision)
(2, 3, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-06-02 09:30:00'),
(2, 6, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-06-15 11:00:00'),
(2, 9, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-10 10:00:00'),
(2, 5, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-06-10 10:00:00'),

-- Workspace 3: Marketing & Sales (TechVision)
(3, 3, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-06-15 10:00:00'),
(3, 4, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-06-05 09:00:00'),

-- Workspace 4: Quality Assurance (TechVision)
(4, 10, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-06-20 08:00:00'),
(4, 3, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-06-20 08:00:00'),
(4, 14, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-08-05 09:30:00'),

-- Workspace 5: Mobile Development (TechVision)
(5, 5, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-07-01 10:00:00'),
(5, 12, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-25 10:00:00'),
(5, 8, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-05 09:30:00'),
(5, 9, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-10 10:00:00'),

-- Workspace 6: Research & Innovation (TechVision)
(6, 13, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-07-10 11:00:00'),
(6, 5, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-10 11:00:00'),
(6, 7, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-07-10 11:00:00'),

-- Workspace 7: IoT Innovation Lab (InnovaTech)
(7, 16, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-08-16 10:00:00'),
(7, 17, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-08-20 09:00:00'),
(7, 18, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-08-25 10:00:00'),
(7, 20, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-09-05 08:30:00'),

-- Workspace 8: Smart City Solutions (InnovaTech)
(8, 16, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-08-20 09:00:00'),
(8, 17, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-08-20 09:00:00'),
(8, 18, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-08-20 09:00:00'),

-- Workspace 9: Digital Transformation (InnovaTech)
(9, 17, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-08-25 10:30:00'),
(9, 16, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-08-25 10:30:00'),
(9, 19, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-08-30 11:00:00'),

-- Workspace 10: Creative Studio (DigitalWave)
(10, 21, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-09-11 09:00:00'),
(10, 22, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-09-15 10:00:00'),
(10, 23, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-09-20 11:00:00'),

-- Workspace 11: Social Media Management (DigitalWave)
(11, 21, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-09-15 10:00:00'),
(11, 23, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-09-20 11:00:00'),
(11, 22, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-09-15 10:00:00'),

-- Workspace 12: Content Strategy (DigitalWave)
(12, 22, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE', '2024-09-20 11:00:00'),
(12, 21, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE', '2024-09-20 11:00:00');

-- =============================================
-- PROJECT TYPES
-- =============================================
INSERT INTO project_types (id, type_name, type_code, model, description) VALUES
(1, 'Phát triển phần mềm (Scrum)', 'SW_SCRUM', 'SCRUM', 'Mô hình Scrum cho dự án phần mềm 2 tuần/sprint.'),
(2, 'Marketing (Kanban)', 'MKT_KANBAN', 'KANBAN', 'Mô hình Kanban liên tục cho team Marketing.'),
(3, 'Thiết kế (Kanban)', 'DESIGN_KANBAN', 'KANBAN', 'Mô hình Kanban cho dự án thiết kế sáng tạo.'),
(4, 'IoT Development (Hybrid)', 'IOT_HYBRID', 'HYBRID', 'Mô hình Hybrid kết hợp Scrum và Kanban cho IoT.');

-- =============================================
-- PROJECTS (15 dự án)
-- =============================================
INSERT INTO projects (id, workspace_id, project_type_id, name, project_code, created_by_id, manager_id, status, priority, description, goal, start_date, due_date, progress, created_at, updated_at) VALUES
-- Workspace 1: Engineering (5 projects) - TechVision
(1, 1, 1, 'E-Commerce Platform MVP', 'ECOM', 5, 5, 'IN_PROGRESS', 'HIGH', 'Building a scalable e-commerce platform with modern tech stack (Next.js, Node.js, PostgreSQL)', 'Launch MVP with core features: product catalog, cart, checkout, and payment integration by end of Q4 2025', '2025-09-01', '2025-12-31', 35.50, '2025-08-25 14:00:00', '2025-11-18 16:30:00'),
(2, 1, 1, 'API Gateway Microservices', 'API-GW', 7, 7, 'IN_PROGRESS', 'URGENT', 'Build centralized API gateway for microservices architecture', 'Implement secure, scalable API gateway with rate limiting, authentication, and monitoring', '2025-10-01', '2025-12-15', 42.00, '2025-09-20 10:00:00', '2025-11-19 08:00:00'),
(3, 1, 1, 'DevOps CI/CD Pipeline', 'DEVOPS', 5, 11, 'IN_PROGRESS', 'HIGH', 'Automate deployment pipeline with Docker, Kubernetes, and Jenkins', 'Achieve zero-downtime deployments and reduce deployment time by 70%', '2025-09-15', '2025-11-30', 55.00, '2025-09-10 09:00:00', '2025-11-19 07:30:00'),
(4, 1, 1, 'Data Analytics Dashboard', 'ANALYTICS', 13, 13, 'NEW', 'MEDIUM', 'Real-time analytics dashboard for business intelligence', 'Provide actionable insights through interactive data visualizations', '2025-11-20', '2026-02-28', 0.00, '2025-11-15 10:00:00', '2025-11-15 10:00:00'),
(5, 1, 1, 'Security Audit & Compliance', 'SECURITY', 5, 10, 'PAUSED', 'HIGH', 'Comprehensive security audit and GDPR compliance implementation', 'Achieve SOC 2 Type II certification and ensure GDPR compliance', '2025-08-01', '2025-12-31', 25.00, '2025-07-25 08:00:00', '2025-10-30 14:00:00'),

-- Workspace 2: Design & Product (2 projects) - TechVision
(6, 2, 3, 'Brand Refresh 2026', 'REBRAND', 9, 9, 'NEW', 'HIGH', 'Complete brand identity redesign including logo, color palette, typography', 'Create modern, memorable brand identity that resonates with target audience', '2025-11-15', '2026-01-31', 0.00, '2025-11-10 11:00:00', '2025-11-15 09:30:00'),
(7, 2, 3, 'Design System v2.0', 'DS-V2', 9, 9, 'IN_PROGRESS', 'MEDIUM', 'Build comprehensive design system with reusable components', 'Standardize UI/UX across all products and improve design consistency', '2025-10-01', '2025-12-31', 38.00, '2025-09-25 10:00:00', '2025-11-18 15:00:00'),

-- Workspace 3: Marketing (2 projects) - TechVision
(8, 3, 2, 'Q4 Marketing Campaign', 'MKT-Q4', 4, 4, 'IN_PROGRESS', 'HIGH', 'Multi-channel marketing campaign for Q4 including social media, email marketing', 'Generate 30% more qualified leads compared to Q3 and increase brand awareness by 25%', '2025-10-01', '2025-12-31', 45.00, '2025-09-25 13:00:00', '2025-11-18 15:00:00'),
(9, 3, 2, 'SEO Optimization Project', 'SEO-OPT', 4, 4, 'IN_PROGRESS', 'MEDIUM', 'Comprehensive SEO optimization for corporate website', 'Achieve top 3 rankings for 20 target keywords and increase organic traffic by 50%', '2025-09-01', '2025-12-15', 60.00, '2025-08-28 09:00:00', '2025-11-19 08:30:00'),

-- Workspace 5: Mobile Development (2 projects) - TechVision
(10, 5, 1, 'Phoenix Mobile App', 'PHX', 12, 12, 'IN_PROGRESS', 'HIGH', 'Cross-platform mobile application built with React Native for project management', 'Achieve 10,000+ downloads and 4.5+ star rating within first 3 months', '2025-10-01', '2026-03-31', 22.00, '2025-09-20 10:00:00', '2025-11-19 08:00:00'),
(11, 5, 1, 'Fitness Tracker App', 'FIT-TRACK', 12, 12, 'NEW', 'MEDIUM', 'Health and fitness tracking mobile application with AI coaching', 'Launch MVP with activity tracking, nutrition logging, and personalized recommendations', '2025-12-01', '2026-04-30', 0.00, '2025-11-18 11:00:00', '2025-11-18 11:00:00'),

-- Workspace 7: IoT Lab (2 projects) - InnovaTech
(12, 7, 4, 'Smart Home Hub', 'SHH', 16, 17, 'IN_PROGRESS', 'HIGH', 'Central hub device for smart home ecosystem with multiple protocol support', 'Integrate at least 5 device categories and achieve seamless user experience', '2025-09-15', '2026-02-15', 28.00, '2025-09-10 09:00:00', '2025-11-17 14:00:00'),
(13, 7, 4, 'Industrial IoT Sensors', 'IND-IOT', 16, 18, 'NEW', 'MEDIUM', 'Industrial-grade IoT sensors for predictive maintenance', 'Deploy sensor network in 3 manufacturing facilities', '2025-11-01', '2026-03-31', 0.00, '2025-10-28 10:00:00', '2025-10-28 10:00:00'),

-- Workspace 10: Creative Studio (2 projects) - DigitalWave
(14, 10, 3, 'ABC Corp Website Redesign', 'ABC-WEB', 21, 21, 'IN_PROGRESS', 'URGENT', 'Complete website redesign and development for ABC Corporation', 'Deliver fully functional, SEO-optimized website before November 30th deadline', '2025-10-10', '2025-11-30', 60.00, '2025-10-05 10:30:00', '2025-11-18 17:00:00'),
(15, 10, 3, 'Product Photography Campaign', 'PHOTO-CAMP', 21, 21, 'IN_PROGRESS', 'MEDIUM', 'Professional product photography for e-commerce catalog', 'Complete photography for 200+ products with lifestyle and studio shots', '2025-10-15', '2025-12-15', 35.00, '2025-10-10 09:00:00', '2025-11-18 14:30:00');

-- =============================================
-- PROJECT MEMBERS
-- =============================================
INSERT INTO project_members (project_id, user_id, role_id, status, joined_at) VALUES
-- Project 1: E-Commerce Platform (TechVision)
(1, 3, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-08-25 14:00:00'),
(1, 5, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-08-25 14:00:00'),
(1, 7, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-08-26 09:00:00'),
(1, 8, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-08-26 09:00:00'),
(1, 10, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-08-26 09:00:00'),
(1, 9, (SELECT id FROM roles WHERE role_code = 'GUEST_PROJECT'), 'ACTIVE', '2025-08-26 09:00:00'),

-- Project 2: API Gateway (TechVision)
(2, 5, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-20 10:00:00'),
(2, 7, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-20 10:00:00'),
(2, 8, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-20 10:00:00'),
(2, 14, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-20 10:00:00'),

-- Project 3: DevOps Pipeline (TechVision)
(3, 5, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-10 09:00:00'),
(3, 11, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-10 09:00:00'),
(3, 7, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-10 09:00:00'),
(3, 10, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-10 09:00:00'),

-- Project 4: Analytics Dashboard (TechVision)
(4, 13, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-11-15 10:00:00'),
(4, 8, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-11-15 10:00:00'),
(4, 11, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-11-15 10:00:00'),

-- Project 5: Security Audit (TechVision)
(5, 5, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-07-25 08:00:00'),
(5, 10, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-07-25 08:00:00'),
(5, 7, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-07-25 08:00:00'),

-- Project 6: Brand Refresh (TechVision)
(6, 9, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-11-10 11:00:00'),
(6, 6, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-11-10 11:00:00'),

-- Project 7: Design System (TechVision)
(7, 9, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-25 10:00:00'),
(7, 6, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-25 10:00:00'),
(7, 11, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-25 10:00:00'),

-- Project 8: Q4 Marketing (TechVision)
(8, 4, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-25 13:00:00'),
(8, 3, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-25 13:00:00'),

-- Project 9: SEO Optimization (TechVision)
(9, 4, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-08-28 09:00:00'),
(9, 3, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-08-28 09:00:00'),

-- Project 10: Phoenix Mobile (TechVision)
(10, 5, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-20 10:00:00'),
(10, 12, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-20 10:00:00'),
(10, 8, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-20 10:00:00'),
(10, 9, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-20 10:00:00'),

-- Project 11: Fitness Tracker (TechVision)
(11, 12, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-11-18 11:00:00'),
(11, 8, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-11-18 11:00:00'),
(11, 9, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-11-18 11:00:00'),

-- Project 12: Smart Home Hub (InnovaTech)
(12, 16, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-10 09:00:00'),
(12, 17, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-09-10 09:00:00'),
(12, 18, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-10 09:00:00'),
(12, 20, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-09-10 09:00:00'),

-- Project 13: Industrial IoT (InnovaTech)
(13, 16, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-10-28 10:00:00'),
(13, 18, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-10-28 10:00:00'),
(13, 17, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-10-28 10:00:00'),

-- Project 14: ABC Website (DigitalWave)
(14, 21, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-10-05 10:30:00'),
(14, 22, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-10-05 10:30:00'),
(14, 23, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-10-05 10:30:00'),
(14, 19, (SELECT id FROM roles WHERE role_code = 'GUEST_PROJECT'), 'ACTIVE', '2025-10-05 10:30:00'),

-- Project 15: Photography (DigitalWave)
(15, 21, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE', '2025-10-10 09:00:00'),
(15, 22, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE', '2025-10-10 09:00:00');

-- =============================================
-- PROJECT STATUSES
-- =============================================
-- Project 1: E-Commerce (Scrum)
INSERT INTO project_statuses (id, project_id, name, color, sort_order, is_completed_status) VALUES
(1, 1, 'To Do', '#95a5a6', 0, 0),
(2, 1, 'In Progress', '#3498db', 1, 0),
(3, 1, 'In Review', '#f39c12', 2, 0),
(4, 1, 'Done', '#27ae60', 3, 1);

-- Project 2: API Gateway (Scrum)
INSERT INTO project_statuses (project_id, name, color, sort_order, is_completed_status) VALUES
(2, 'Backlog', '#95a5a6', 0, 0),
(2, 'In Development', '#3498db', 1, 0),
(2, 'Code Review', '#f39c12', 2, 0),
(2, 'Testing', '#9b59b6', 3, 0),
(2, 'Done', '#27ae60', 4, 1);

-- Project 3: DevOps (Scrum)
INSERT INTO project_statuses (project_id, name, color, sort_order, is_completed_status) VALUES
(3, 'To Do', '#95a5a6', 0, 0),
(3, 'In Progress', '#3498db', 1, 0),
(3, 'Testing', '#9b59b6', 2, 0),
(3, 'Done', '#27ae60', 3, 1);

-- Project 8: Marketing Campaign (Kanban)
INSERT INTO project_statuses (project_id, name, color, sort_order, is_completed_status) VALUES
(8, 'Ideas', '#ecf0f1', 0, 0),
(8, 'Content Creation', '#e67e22', 1, 0),
(8, 'Review', '#f39c12', 2, 0),
(8, 'Published', '#27ae60', 3, 1);

-- Project 9: SEO (Kanban)
INSERT INTO project_statuses (project_id, name, color, sort_order, is_completed_status) VALUES
(9, 'Backlog', '#95a5a6', 0, 0),
(9, 'In Progress', '#3498db', 1, 0),
(9, 'Review', '#f39c12', 2, 0),
(9, 'Completed', '#27ae60', 3, 1);

-- Project 10: Phoenix Mobile (Scrum)
INSERT INTO project_statuses (project_id, name, color, sort_order, is_completed_status) VALUES
(10, 'Backlog', '#95a5a6', 0, 0),
(10, 'In Development', '#3498db', 1, 0),
(10, 'Testing', '#9b59b6', 2, 0),
(10, 'Done', '#27ae60', 3, 1);

-- Project 12: Smart Home Hub (Hybrid)
INSERT INTO project_statuses (project_id, name, color, sort_order, is_completed_status) VALUES
(12, 'To Do', '#95a5a6', 0, 0),
(12, 'In Progress', '#3498db', 1, 0),
(12, 'Testing', '#9b59b6', 2, 0),
(12, 'Done', '#27ae60', 3, 1);

-- Project 14: ABC Website (Kanban)
INSERT INTO project_statuses (project_id, name, color, sort_order, is_completed_status) VALUES
(14, 'Requirements', '#95a5a6', 0, 0),
(14, 'Design', '#9b59b6', 1, 0),
(14, 'Development', '#3498db', 2, 0),
(14, 'Done', '#27ae60', 3, 1);

-- =============================================
-- BƯỚC 5: SPRINTS (Cho các dự án SCRUM)
-- =============================================
INSERT INTO sprints (id, project_id, name, sprint_code, goal, status, start_date, end_date, duration_days, created_by_id, created_at, updated_at) VALUES
-- Project 1: E-Commerce Platform (6 sprints)
(1, 1, 'Sprint 1 - Foundation', 'ECOM-S1', 'Setup project infrastructure and database schema', 'COMPLETED', '2025-09-01', '2025-09-14', 14, 5, '2025-08-28 10:00:00', '2025-09-14 17:00:00'),
(2, 1, 'Sprint 2 - Authentication', 'ECOM-S2', 'Implement user authentication and authorization', 'COMPLETED', '2025-09-15', '2025-09-28', 14, 5, '2025-09-12 10:00:00', '2025-09-28 17:00:00'),
(3, 1, 'Sprint 3 - Product Catalog', 'ECOM-S3', 'Build product listing and search functionality', 'COMPLETED', '2025-09-29', '2025-10-12', 14, 5, '2025-09-26 10:00:00', '2025-10-12 17:00:00'),
(4, 1, 'Sprint 4 - Shopping Cart', 'ECOM-S4', 'Implement cart and checkout flow', 'IN_PROGRESS', '2025-10-13', '2025-10-26', 14, 5, '2025-10-10 10:00:00', '2025-11-19 09:00:00'),
(5, 1, 'Sprint 5 - Payment Integration', 'ECOM-S5', 'Integrate payment gateway and order management', 'NOT_STARTED', '2025-10-27', '2025-11-09', 14, 5, '2025-10-24 10:00:00', '2025-10-24 10:00:00'),
(6, 1, 'Sprint 6 - Testing & Launch', 'ECOM-S6', 'Final testing, bug fixes and production deployment', 'NOT_STARTED', '2025-11-10', '2025-11-23', 14, 5, '2025-11-07 10:00:00', '2025-11-07 10:00:00'),

-- Project 2: API Gateway (4 sprints)
(7, 2, 'Sprint 1 - Architecture', 'API-S1', 'Design and setup API gateway architecture', 'COMPLETED', '2025-10-01', '2025-10-14', 14, 7, '2025-09-28 09:00:00', '2025-10-14 17:00:00'),
(8, 2, 'Sprint 2 - Core Features', 'API-S2', 'Implement routing, rate limiting, authentication', 'IN_PROGRESS', '2025-10-15', '2025-10-28', 14, 7, '2025-10-12 09:00:00', '2025-11-19 08:30:00'),
(9, 2, 'Sprint 3 - Monitoring', 'API-S3', 'Add logging, monitoring and alerting', 'NOT_STARTED', '2025-10-29', '2025-11-11', 14, 7, '2025-10-26 09:00:00', '2025-10-26 09:00:00'),
(10, 2, 'Sprint 4 - Production Ready', 'API-S4', 'Load testing and production deployment', 'NOT_STARTED', '2025-11-12', '2025-11-25', 14, 7, '2025-11-09 09:00:00', '2025-11-09 09:00:00'),

-- Project 3: DevOps Pipeline (3 sprints)
(11, 3, 'Sprint 1 - CI Setup', 'DO-S1', 'Setup Jenkins and containerization', 'COMPLETED', '2025-09-15', '2025-09-28', 14, 11, '2025-09-12 08:00:00', '2025-09-28 17:00:00'),
(12, 3, 'Sprint 2 - CD Pipeline', 'DO-S2', 'Implement continuous deployment with K8s', 'IN_PROGRESS', '2025-09-29', '2025-10-12', 14, 11, '2025-09-26 08:00:00', '2025-11-19 07:45:00'),
(13, 3, 'Sprint 3 - Optimization', 'DO-S3', 'Pipeline optimization and documentation', 'NOT_STARTED', '2025-10-13', '2025-10-26', 14, 11, '2025-10-10 08:00:00', '2025-10-10 08:00:00'),

-- Project 10: Phoenix Mobile (5 sprints)
(14, 10, 'Sprint 1 - Setup', 'PHX-S1', 'Project setup and navigation structure', 'COMPLETED', '2025-10-01', '2025-10-14', 14, 12, '2025-09-28 09:00:00', '2025-10-14 17:00:00'),
(15, 10, 'Sprint 2 - Authentication', 'PHX-S2', 'User authentication and profile management', 'IN_PROGRESS', '2025-10-15', '2025-10-28', 14, 12, '2025-10-12 09:00:00', '2025-11-19 08:15:00'),
(16, 10, 'Sprint 3 - Core Features', 'PHX-S3', 'Project and task management features', 'NOT_STARTED', '2025-10-29', '2025-11-11', 14, 12, '2025-10-26 09:00:00', '2025-10-26 09:00:00'),
(17, 10, 'Sprint 4 - Collaboration', 'PHX-S4', 'Comments, notifications and real-time updates', 'NOT_STARTED', '2025-11-12', '2025-11-25', 14, 12, '2025-11-09 09:00:00', '2025-11-09 09:00:00'),
(18, 10, 'Sprint 5 - Polish', 'PHX-S5', 'UI polish, performance optimization and testing', 'NOT_STARTED', '2025-11-26', '2025-12-09', 14, 12, '2025-11-23 09:00:00', '2025-11-23 09:00:00');

-- =============================================
-- BƯỚC 5.2: EPICS (Nhóm lớn các tính năng)
-- =============================================
INSERT INTO epics (id, project_id, name, epic_code, description, color, status, start_date, due_date, created_by_id, created_at, updated_at) VALUES
-- Project 1: E-Commerce Platform
(1, 1, 'User Management', 'ECOM-E1', 'Complete user authentication, authorization and profile management system', '#3498db', 'COMPLETED', '2025-09-01', '2025-09-28', 5, '2025-08-25 14:00:00', '2025-09-28 17:00:00'),
(2, 1, 'Product Catalog', 'ECOM-E2', 'Product listing, search, filtering and detail pages', '#2ecc71', 'IN_PROGRESS', '2025-09-15', '2025-10-26', 5, '2025-08-25 14:15:00', '2025-11-18 16:00:00'),
(3, 1, 'Shopping & Checkout', 'ECOM-E3', 'Shopping cart, checkout flow and order processing', '#f39c12', 'IN_PROGRESS', '2025-10-01', '2025-11-15', 5, '2025-08-25 14:30:00', '2025-11-19 08:00:00'),
(4, 1, 'Payment & Orders', 'ECOM-E4', 'Payment integration and order management', '#e74c3c', 'OPEN', '2025-10-20', '2025-12-15', 5, '2025-08-25 14:45:00', '2025-10-20 10:00:00'),

-- Project 2: API Gateway
(5, 2, 'Gateway Core', 'API-E1', 'Core gateway functionality - routing, load balancing', '#3498db', 'IN_PROGRESS', '2025-10-01', '2025-11-15', 7, '2025-09-20 10:00:00', '2025-11-19 08:30:00'),
(6, 2, 'Security & Auth', 'API-E2', 'Authentication, authorization and rate limiting', '#9b59b6', 'IN_PROGRESS', '2025-10-01', '2025-11-30', 7, '2025-09-20 10:15:00', '2025-11-18 15:00:00'),

-- Project 10: Phoenix Mobile
(7, 10, 'User Features', 'PHX-E1', 'Authentication, profile and user settings', '#3498db', 'IN_PROGRESS', '2025-10-01', '2025-10-28', 12, '2025-09-20 10:00:00', '2025-11-19 08:15:00'),
(8, 10, 'Project Management', 'PHX-E2', 'Create, manage and track projects', '#2ecc71', 'OPEN', '2025-10-15', '2025-12-15', 12, '2025-09-20 10:15:00', '2025-10-15 09:00:00'),
(9, 10, 'Collaboration', 'PHX-E3', 'Comments, notifications and team collaboration', '#f39c12', 'OPEN', '2025-11-01', '2025-12-31', 12, '2025-09-20 10:30:00', '2025-11-01 09:00:00');

-- =============================================
-- BƯỚC 5.3: TASKS (100+ tasks)
-- =============================================
INSERT INTO tasks (id, project_id, epic_id, sprint_id, task_code, title, description, task_type, status_id, priority, assigner_id, assignee_id, reviewer_id, story_points, estimated_hours, start_date, due_date, completed_at, created_by_id, created_at, updated_at) VALUES
-- ========== PROJECT 1: E-COMMERCE PLATFORM ==========
-- Sprint 1 (COMPLETED)
(1, 1, 1, 1, 'ECOM-1', 'Design and implement database schema', 'Create complete database schema for e-commerce platform including users, products, orders, payments tables', 'TASK', 4, 'HIGH', 5, 7, 5, 8, 16, '2025-09-01', '2025-09-05', '2025-09-04', 5, '2025-09-01 09:00:00', '2025-09-04 17:00:00'),
(2, 1, 1, 1, 'ECOM-2', 'Setup user authentication system', 'Implement JWT-based authentication with registration, login, password reset', 'STORY', 4, 'URGENT', 5, 7, 10, 13, 24, '2025-09-05', '2025-09-10', '2025-09-10', 5, '2025-09-01 09:15:00', '2025-09-10 17:30:00'),
(3, 1, 1, 1, 'ECOM-3', 'Create user profile management API', 'RESTful API endpoints for user profile CRUD operations', 'TASK', 4, 'MEDIUM', 5, 7, 5, 8, 12, '2025-09-08', '2025-09-12', '2025-09-11', 5, '2025-09-01 09:30:00', '2025-09-11 16:00:00'),

-- Sprint 2 (COMPLETED)
(4, 1, 2, 2, 'ECOM-4', 'Build product catalog API', 'Backend API for product CRUD with pagination, sorting, filtering', 'STORY', 4, 'HIGH', 5, 7, 5, 13, 20, '2025-09-15', '2025-09-20', '2025-09-20', 5, '2025-09-14 10:00:00', '2025-09-20 17:00:00'),
(5, 1, 2, 2, 'ECOM-5', 'Implement product search with Elasticsearch', 'Full-text search functionality for products with fuzzy matching', 'TASK', 4, 'HIGH', 5, 7, 5, 8, 16, '2025-09-18', '2025-09-24', '2025-09-24', 5, '2025-09-14 10:30:00', '2025-09-24 16:30:00'),
(6, 1, 2, 2, 'ECOM-6', 'Design product listing page UI', 'Responsive product grid with filters, sorting options', 'TASK', 4, 'MEDIUM', 5, 9, 5, 5, 8, '2025-09-15', '2025-09-22', '2025-09-21', 5, '2025-09-14 11:00:00', '2025-09-21 15:00:00'),
(7, 1, 2, 2, 'ECOM-7', 'Design product detail page UI', 'Detailed product page with image gallery, specifications, reviews', 'TASK', 4, 'MEDIUM', 5, 9, 5, 5, 10, '2025-09-20', '2025-09-27', '2025-09-26', 5, '2025-09-14 11:30:00', '2025-09-26 16:00:00'),

-- Sprint 3 (COMPLETED)
(8, 1, 2, 3, 'ECOM-8', 'Implement product listing page frontend', 'Build React component with infinite scroll, filters', 'TASK', 4, 'HIGH', 5, 8, 10, 8, 16, '2025-09-29', '2025-10-05', '2025-10-05', 5, '2025-09-28 09:00:00', '2025-10-05 17:00:00'),
(9, 1, 3, 3, 'ECOM-9', 'Build shopping cart UI component', 'Interactive cart with add/remove items, quantity adjustment', 'STORY', 4, 'HIGH', 5, 9, 10, 8, 12, '2025-10-01', '2025-10-08', '2025-10-07', 5, '2025-09-28 09:30:00', '2025-10-07 16:00:00'),
(10, 1, 3, 3, 'ECOM-10', 'Implement checkout flow UI', 'Multi-step checkout: shipping, payment, review', 'STORY', 2, 'HIGH', 5, 9, 5, 13, 20, '2025-10-06', '2025-10-12', NULL, 5, '2025-09-28 10:00:00', '2025-11-18 14:30:00'),
(11, 1, 2, 3, 'ECOM-11', 'Add product review and rating system', 'Allow users to submit reviews and rate products', 'TASK', 4, 'LOW', 5, 8, 10, 5, 8, '2025-10-08', '2025-10-12', '2025-10-11', 5, '2025-09-28 10:30:00', '2025-10-11 17:00:00'),

-- Sprint 4 (IN_PROGRESS)
(12, 1, 3, 4, 'ECOM-12', 'Fix cart calculation bug for discounted items', 'Cart total incorrect when discount codes applied', 'BUG', 2, 'URGENT', 10, 7, 5, 3, 4, '2025-10-10', '2025-10-11', NULL, 10, '2025-10-10 15:30:00', '2025-11-19 08:00:00'),
(13, 1, 3, 4, 'ECOM-13', 'Integrate Stripe payment gateway', 'Complete Stripe integration for credit card payments', 'STORY', 2, 'URGENT', 5, 7, 5, 13, 24, '2025-10-13', '2025-10-20', NULL, 5, '2025-10-12 09:00:00', '2025-11-18 16:00:00'),
(14, 1, 3, 4, 'ECOM-14', 'Build order confirmation page', 'Display order summary after successful checkout', 'TASK', 1, 'MEDIUM', 5, 8, 10, 5, 6, '2025-10-18', '2025-10-22', NULL, 5, '2025-10-12 09:30:00', '2025-11-18 10:00:00'),
(15, 1, 4, 4, 'ECOM-15', 'Create order management dashboard', 'Admin dashboard to view and manage customer orders', 'STORY', 1, 'HIGH', 5, 8, 5, 8, 16, '2025-10-20', '2025-10-26', NULL, 5, '2025-10-12 10:00:00', '2025-10-20 09:00:00'),

-- ========== PROJECT 2: API GATEWAY ==========
-- Sprint 1 (COMPLETED)
(16, 2, 5, 7, 'API-1', 'Setup Kong API Gateway', 'Install and configure Kong gateway with PostgreSQL', 'TASK', 9, 'URGENT', 7, 7, 5, 8, 12, '2025-10-01', '2025-10-05', '2025-10-04', 7, '2025-09-30 09:00:00', '2025-10-04 17:00:00'),
(17, 2, 5, 7, 'API-2', 'Implement service routing logic', 'Configure routing rules for microservices', 'STORY', 9, 'HIGH', 7, 8, 7, 13, 20, '2025-10-03', '2025-10-10', '2025-10-09', 7, '2025-09-30 09:30:00', '2025-10-09 16:30:00'),
(18, 2, 6, 7, 'API-3', 'Add JWT authentication middleware', 'Validate JWT tokens on all protected routes', 'TASK', 9, 'URGENT', 7, 8, 7, 8, 12, '2025-10-08', '2025-10-12', '2025-10-12', 7, '2025-09-30 10:00:00', '2025-10-12 17:00:00'),

-- Sprint 2 (IN_PROGRESS)
(19, 2, 6, 8, 'API-4', 'Implement rate limiting plugin', 'Rate limit API calls per user/IP address', 'STORY', 6, 'HIGH', 7, 8, 7, 8, 16, '2025-10-15', '2025-10-22', NULL, 7, '2025-10-14 09:00:00', '2025-11-19 08:30:00'),
(20, 2, 5, 8, 'API-5', 'Setup load balancing configuration', 'Configure round-robin load balancing across service instances', 'TASK', 7, 'MEDIUM', 7, 7, 5, 5, 10, '2025-10-18', '2025-10-25', NULL, 7, '2025-10-14 09:30:00', '2025-11-18 15:00:00'),
(21, 2, 5, 8, 'API-6', 'Add request/response logging', 'Comprehensive logging for debugging and analytics', 'TASK', 6, 'MEDIUM', 7, 8, 7, 5, 8, '2025-10-20', '2025-10-26', NULL, 7, '2025-10-14 10:00:00', '2025-11-17 14:00:00'),

-- ========== PROJECT 3: DEVOPS PIPELINE ==========
-- Sprint 1 (COMPLETED)
(22, 3, NULL, 11, 'DO-1', 'Setup Jenkins CI server', 'Install Jenkins and configure initial pipelines', 'TASK', 13, 'HIGH', 11, 11, 5, 5, 8, '2025-09-15', '2025-09-20', '2025-09-19', 11, '2025-09-14 08:00:00', '2025-09-19 17:00:00'),
(23, 3, NULL, 11, 'DO-2', 'Create Dockerfiles for all services', 'Containerize backend, frontend and database services', 'STORY', 13, 'HIGH', 11, 7, 11, 8, 16, '2025-09-18', '2025-09-25', '2025-09-24', 11, '2025-09-14 08:30:00', '2025-09-24 16:00:00'),
(24, 3, NULL, 11, 'DO-3', 'Setup automated testing in CI', 'Run unit and integration tests on every commit', 'TASK', 13, 'MEDIUM', 11, 10, 11, 5, 10, '2025-09-22', '2025-09-28', '2025-09-27', 11, '2025-09-14 09:00:00', '2025-09-27 17:00:00'),

-- Sprint 2 (IN_PROGRESS)
(25, 3, NULL, 12, 'DO-4', 'Setup Kubernetes cluster', 'Configure K8s cluster for production deployment', 'STORY', 10, 'URGENT', 11, 11, 5, 13, 24, '2025-09-29', '2025-10-08', NULL, 11, '2025-09-28 08:00:00', '2025-11-19 07:45:00'),
(26, 3, NULL, 12, 'DO-5', 'Implement blue-green deployment', 'Zero-downtime deployment strategy', 'TASK', 10, 'HIGH', 11, 7, 11, 8, 16, '2025-10-05', '2025-10-12', NULL, 11, '2025-09-28 08:30:00', '2025-11-18 16:00:00'),

-- ========== PROJECT 8: Q4 MARKETING CAMPAIGN ==========
(27, 8, NULL, NULL, 'MKT-1', 'Create social media content calendar', 'Plan daily posts for Facebook, Instagram, LinkedIn for Q4', 'TASK', 17, 'HIGH', 4, 4, 3, NULL, 8, '2025-10-01', '2025-10-10', '2025-10-09', 4, '2025-09-30 10:00:00', '2025-10-09 17:00:00'),
(28, 8, NULL, NULL, 'MKT-2', 'Design email marketing templates', 'Create 5 email templates for different campaign stages', 'TASK', 18, 'MEDIUM', 4, 9, 4, NULL, 12, '2025-10-05', '2025-10-15', NULL, 4, '2025-09-30 10:30:00', '2025-11-18 15:30:00'),
(29, 8, NULL, NULL, 'MKT-3', 'Launch Facebook ad campaign', 'Setup and launch paid advertising on Facebook', 'STORY', 18, 'HIGH', 4, 4, 3, NULL, 16, '2025-10-10', '2025-10-20', NULL, 4, '2025-09-30 11:00:00', '2025-11-17 16:00:00'),
(30, 8, NULL, NULL, 'MKT-4', 'Create promotional graphics for social media', 'Design 20+ graphics for various social platforms', 'TASK', 17, 'MEDIUM', 4, 9, 4, NULL, 20, '2025-10-15', '2025-10-30', '2025-10-28', 4, '2025-10-10 10:00:00', '2025-10-28 17:00:00'),

-- ========== PROJECT 9: SEO OPTIMIZATION ==========
(31, 9, NULL, NULL, 'SEO-1', 'Conduct comprehensive SEO audit', 'Audit current website for SEO issues and opportunities', 'TASK', 22, 'HIGH', 4, 4, 3, NULL, 16, '2025-09-01', '2025-09-10', '2025-09-09', 4, '2025-08-30 09:00:00', '2025-09-09 17:00:00'),
(32, 9, NULL, NULL, 'SEO-2', 'Optimize meta tags and descriptions', 'Update meta tags for all pages with target keywords', 'STORY', 23, 'HIGH', 4, 4, 3, NULL, 12, '2025-09-10', '2025-09-20', NULL, 4, '2025-08-30 09:30:00', '2025-11-19 08:30:00'),
(33, 9, NULL, NULL, 'SEO-3', 'Improve site speed and Core Web Vitals', 'Optimize images, code splitting, lazy loading', 'TASK', 24, 'URGENT', 4, 8, 4, NULL, 20, '2025-09-15', '2025-10-05', '2025-10-04', 4, '2025-08-30 10:00:00', '2025-10-04 16:00:00'),
(34, 9, NULL, NULL, 'SEO-4', 'Build quality backlinks', 'Outreach campaign to acquire 50+ quality backlinks', 'STORY', 24, 'MEDIUM', 4, 4, 3, NULL, 40, '2025-10-01', '2025-11-30', '2025-11-15', 4, '2025-08-30 10:30:00', '2025-11-15 17:00:00'),

-- ========== PROJECT 10: PHOENIX MOBILE APP ==========
-- Sprint 1 (COMPLETED)
(35, 10, 7, 14, 'PHX-1', 'Setup React Native project with TypeScript', 'Initialize project with necessary dependencies and folder structure', 'TASK', 28, 'HIGH', 12, 12, 5, 5, 8, '2025-10-01', '2025-10-05', '2025-10-04', 12, '2025-09-30 09:00:00', '2025-10-04 17:00:00'),
(36, 10, 7, 14, 'PHX-2', 'Implement navigation structure', 'Setup React Navigation with tab and stack navigators', 'STORY', 28, 'HIGH', 12, 8, 12, 8, 12, '2025-10-03', '2025-10-10', '2025-10-09', 12, '2025-09-30 09:30:00', '2025-10-09 16:30:00'),
(37, 10, 7, 14, 'PHX-3', 'Design app UI components library', 'Create reusable UI components following design system', 'TASK', 28, 'MEDIUM', 12, 9, 12, 5, 16, '2025-10-05', '2025-10-12', '2025-10-11', 12, '2025-09-30 10:00:00', '2025-10-11 17:00:00'),

-- Sprint 2 (IN_PROGRESS)
(38, 10, 7, 15, 'PHX-4', 'Build authentication screens', 'Login, register, forgot password screens', 'STORY', 27, 'URGENT', 12, 9, 12, 8, 12, '2025-10-15', '2025-10-22', NULL, 12, '2025-10-14 09:00:00', '2025-11-19 08:15:00'),
(39, 10, 7, 15, 'PHX-5', 'Implement authentication logic with Redux', 'State management for auth flow with token storage', 'TASK', 27, 'URGENT', 12, 8, 12, 8, 16, '2025-10-18', '2025-10-25', NULL, 12, '2025-10-14 09:30:00', '2025-11-18 17:00:00'),
(40, 10, 7, 15, 'PHX-6', 'Create user profile screen', 'Display and edit user information', 'TASK', 26, 'MEDIUM', 12, 9, 12, 5, 10, '2025-10-20', '2025-10-26', NULL, 12, '2025-10-14 10:00:00', '2025-11-17 14:00:00'),

-- ========== PROJECT 14: ABC CORP WEBSITE REDESIGN ==========
(41, 14, NULL, NULL, 'ABC-1', 'Client requirements gathering workshop', 'Meet with stakeholders to gather detailed requirements', 'TASK', 29, 'HIGH', 21, 21, NULL, NULL, 8, '2025-10-05', '2025-10-08', '2025-10-07', 21, '2025-10-05 10:00:00', '2025-10-07 17:00:00'),
(42, 14, NULL, NULL, 'ABC-2', 'Create website wireframes', 'Low-fidelity wireframes for all key pages', 'STORY', 29, 'HIGH', 21, 21, NULL, NULL, 16, '2025-10-08', '2025-10-15', '2025-10-14', 21, '2025-10-05 10:30:00', '2025-10-14 16:00:00'),
(43, 14, NULL, NULL, 'ABC-3', 'Design high-fidelity mockups', 'Complete visual designs with branding applied', 'STORY', 30, 'URGENT', 21, 21, NULL, NULL, 24, '2025-10-15', '2025-10-25', NULL, 21, '2025-10-05 11:00:00', '2025-11-18 17:00:00'),
(44, 14, NULL, NULL, 'ABC-4', 'Develop homepage', 'Responsive homepage with hero section, features', 'TASK', 31, 'URGENT', 21, 22, 21, NULL, 16, '2025-10-22', '2025-10-28', NULL, 21, '2025-10-20 09:00:00', '2025-11-18 16:00:00'),
(45, 14, NULL, NULL, 'ABC-5', 'Develop about page', 'Company history, team members, mission/vision', 'TASK', 31, 'HIGH', 21, 22, 21, NULL, 12, '2025-10-26', '2025-11-02', NULL, 21, '2025-10-20 09:30:00', '2025-11-18 14:30:00'),
(46, 14, NULL, NULL, 'ABC-6', 'Develop services page', 'Showcase all services with detailed descriptions', 'TASK', 30, 'HIGH', 21, 23, 21, NULL, 14, '2025-10-28', '2025-11-05', NULL, 21, '2025-10-20 10:00:00', '2025-11-17 15:00:00'),
(47, 14, NULL, NULL, 'ABC-7', 'Integrate contact form with email service', 'Working contact form with validation and email notifications', 'TASK', 31, 'MEDIUM', 21, 22, 21, NULL, 8, '2025-11-01', '2025-11-08', NULL, 21, '2025-10-28 09:00:00', '2025-11-15 10:00:00'),
(48, 14, NULL, NULL, 'ABC-8', 'Setup CMS for blog management', 'WordPress integration for easy content updates', 'TASK', 30, 'MEDIUM', 21, 22, 21, NULL, 12, '2025-11-05', '2025-11-12', NULL, 21, '2025-10-28 09:30:00', '2025-11-15 09:00:00'),
(49, 14, NULL, NULL, 'ABC-9', 'Perform SEO optimization', 'On-page SEO, meta tags, structured data', 'TASK', 31, 'HIGH', 21, 22, 21, NULL, 10, '2025-11-10', '2025-11-18', NULL, 21, '2025-11-05 10:00:00', '2025-11-15 09:30:00'),
(50, 14, NULL, NULL, 'ABC-10', 'Final QA and client review', 'Complete testing and client approval', 'TASK', 31, 'URGENT', 21, 21, NULL, NULL, 12, '2025-11-18', '2025-11-25', NULL, 21, '2025-11-10 10:00:00', '2025-11-18 15:00:00'),

-- ========== PROJECT 15: PRODUCT PHOTOGRAPHY ==========
(51, 15, NULL, NULL, 'PHOTO-1', 'Setup photography studio and equipment', 'Prepare lighting, backdrop, camera settings', 'TASK', NULL, 'HIGH', 21, 21, NULL, NULL, 6, '2025-10-15', '2025-10-18', '2025-10-17', 21, '2025-10-14 09:00:00', '2025-10-17 16:00:00'),
(52, 15, NULL, NULL, 'PHOTO-2', 'Photograph electronics category (50 items)', 'Studio shots and lifestyle photos for electronics', 'STORY', NULL, 'HIGH', 21, 21, NULL, NULL, 20, '2025-10-18', '2025-10-30', '2025-10-29', 21, '2025-10-14 09:30:00', '2025-10-29 17:00:00'),
(53, 15, NULL, NULL, 'PHOTO-3', 'Photograph fashion category (80 items)', 'Product shots with models for clothing and accessories', 'STORY', NULL, 'HIGH', 21, 21, NULL, NULL, 32, '2025-10-25', '2025-11-15', NULL, 21, '2025-10-14 10:00:00', '2025-11-18 14:30:00'),
(54, 15, NULL, NULL, 'PHOTO-4', 'Post-production editing batch 1', 'Color correction, background removal, retouching', 'TASK', NULL, 'MEDIUM', 21, 22, 21, NULL, 24, '2025-11-01', '2025-11-10', NULL, 21, '2025-10-28 09:00:00', '2025-11-10 15:00:00'),
(55, 15, NULL, NULL, 'PHOTO-5', 'Organize and deliver final files', 'Categorize, rename and upload to client\'s DAM system', 'TASK', NULL, 'LOW', 21, 22, 21, NULL, 8, '2025-11-12', '2025-11-18', NULL, 21, '2025-11-05 10:00:00', '2025-11-12 09:00:00');

-- =============================================
-- BƯỚC 5.5: SUB-TASKS (Đã sửa parent_task_id cho đúng)
-- =============================================
INSERT INTO sub_tasks (id, parent_task_id, title, description, status, assignee_id, estimated_hours, sort_order, created_by_id, created_at, updated_at) VALUES
-- Sub-tasks cho ECOM-2 (Task ID 2: Setup authentication)
(1, 2, 'Setup JWT authentication middleware', 'Create middleware to validate JWT tokens on protected routes', 'DONE', 7, 4, 1, 5, '2025-09-06 10:00:00', '2025-09-08 16:00:00'),
(2, 2, 'Implement registration endpoint with validation', 'User registration with email validation and password hashing', 'DONE', 7, 6, 2, 5, '2025-09-06 10:15:00', '2025-09-09 17:00:00'),
(3, 2, 'Implement login endpoint and token generation', 'Login endpoint that returns JWT access and refresh tokens', 'DONE', 7, 5, 3, 5, '2025-09-06 10:30:00', '2025-09-10 15:30:00'),
(4, 2, 'Add rate limiting for auth endpoints', 'Prevent brute force attacks on login/register endpoints', 'DONE', 7, 3, 4, 5, '2025-09-06 10:45:00', '2025-09-10 16:30:00'),

-- Sub-tasks cho ECOM-10 (Task ID 10: Checkout flow UI)
(5, 10, 'Create shipping address form component', 'Form for entering shipping address with validation', 'IN_PROGRESS', 9, 4, 1, 5, '2025-10-07 11:00:00', '2025-11-18 14:00:00'),
(6, 10, 'Integrate address validation API', 'Validate addresses using Google Maps API', 'TO_DO', 9, 3, 2, 5, '2025-10-07 11:15:00', '2025-10-07 11:15:00'),
(7, 10, 'Add save address for future use feature', 'Allow users to save addresses to their profile', 'TO_DO', 9, 2, 3, 5, '2025-10-07 11:30:00', '2025-10-07 11:30:00'),

-- Sub-tasks cho PHX-5 (Task ID 39: Authentication logic)
(8, 39, 'Setup Redux slice for authentication', 'Create Redux slice with auth actions and reducers', 'DONE', 8, 3, 1, 12, '2025-10-18 09:00:00', '2025-10-20 16:00:00'),
(9, 39, 'Implement secure token storage', 'Store tokens securely using encrypted AsyncStorage', 'DONE', 8, 2, 2, 12, '2025-10-18 09:15:00', '2025-10-21 17:00:00'),
(10, 39, 'Add biometric authentication option', 'Optional fingerprint/face ID login for returning users', 'IN_PROGRESS', 8, 5, 3, 12, '2025-10-18 09:30:00', '2025-11-19 08:00:00'),

-- Sub-tasks cho ABC-3 (Task ID 43: Design mockups)
(11, 43, 'Design homepage mockup', 'Complete visual design for homepage', 'DONE', 21, 8, 1, 21, '2025-10-16 09:00:00', '2025-10-20 17:00:00'),
(12, 43, 'Design about page mockup', 'Complete visual design for about page', 'IN_PROGRESS', 21, 6, 2, 21, '2025-10-20 09:00:00', '2025-11-18 15:00:00'),
(13, 43, 'Design services page mockup', 'Complete visual design for services page', 'TO_DO', 21, 6, 3, 21, '2025-10-23 09:00:00', '2025-10-23 09:00:00'),
(14, 43, 'Design contact page mockup', 'Complete visual design for contact page', 'TO_DO', 21, 4, 4, 21, '2025-10-24 09:00:00', '2025-10-24 09:00:00');



INSERT INTO tags (id, project_id, name, color, description, created_by_id, created_at) VALUES
-- Project 1: E-Commerce
(1, 1, 'Backend', '#e74c3c', 'Backend development tasks', 5, NOW()),
(2, 1, 'Frontend', '#3498db', 'Frontend interface tasks', 5, NOW()),
(3, 1, 'Database', '#f1c40f', 'Database schema and queries', 5, NOW()),
(4, 1, 'Bug', '#c0392b', 'System errors and defects', 5, NOW()),
(5, 1, 'Security', '#8e44ad', 'Security and compliance', 5, NOW()),
(6, 1, 'API', '#2c3e50', 'API integration', 5, NOW()),

-- Project 10: Phoenix Mobile
(7, 10, 'React Native', '#61dbfb', 'Mobile framework', 12, NOW()),
(8, 10, 'iOS', '#5856d6', 'iOS specific tasks', 12, NOW()),
(9, 10, 'Android', '#3ddc84', 'Android specific tasks', 12, NOW()),

-- Project 8: Marketing
(10, 8, 'Social Media', '#e1306c', 'Social media content', 4, NOW()),
(11, 8, 'Graphics', '#fd79a8', 'Visual assets', 4, NOW()),

-- Project 14: ABC Website (ID 12 bị bỏ qua trong dữ liệu mẫu của bạn, ta bắt đầu từ 13)
(13, 14, 'UI/UX', '#a29bfe', 'Design and User Experience', 21, NOW()),
(14, 14, 'Development', '#0984e3', 'Coding and implementation', 21, NOW()),
(15, 14, 'Content', '#00b894', 'Text and copy', 21, NOW()),
(16, 14, 'SEO', '#6c5ce7', 'Search Engine Optimization', 21, NOW());

-- =============================================
-- BƯỚC 5.6: TASK TAGS 
-- =============================================
INSERT INTO task_tags (task_id, tag_id, created_at) VALUES
-- Project 1: E-Commerce tags
(1, 1, '2025-09-01 09:30:00'), (1, 3, '2025-09-01 09:30:00'),
(2, 1, '2025-09-01 09:45:00'), (2, 5, '2025-09-01 09:45:00'), (2, 6, '2025-09-01 09:45:00'),
(3, 1, '2025-09-01 10:00:00'), (3, 5, '2025-09-01 10:00:00'),
(4, 1, '2025-09-14 10:30:00'), (4, 3, '2025-09-14 10:30:00'),
(5, 1, '2025-09-14 11:00:00'), (5, 6, '2025-09-14 11:00:00'),
(6, 2, '2025-09-14 11:30:00'),
(7, 2, '2025-09-14 12:00:00'),
(8, 1, '2025-09-28 09:30:00'),
(9, 2, '2025-09-28 10:00:00'),
(10, 2, '2025-09-28 10:30:00'),
(11, 2, '2025-09-28 11:00:00'),
(12, 1, '2025-10-10 16:00:00'), (12, 4, '2025-10-10 16:00:00'), (12, 6, '2025-10-10 16:00:00'),
(13, 1, '2025-10-12 09:15:00'), (13, 6, '2025-10-12 09:15:00'),
(14, 2, '2025-10-12 09:45:00'),
(15, 1, '2025-10-12 10:15:00'), (15, 6, '2025-10-12 10:15:00'),

-- Project 10: Phoenix Mobile tags
(35, 7, '2025-09-30 11:00:00'), (35, 9, '2025-09-30 11:00:00'),
(36, 8, '2025-09-30 11:15:00'),
(37, 7, '2025-09-30 11:30:00'), (37, 8, '2025-09-30 11:30:00'), (37, 9, '2025-09-30 11:30:00'),
(38, 7, '2025-10-14 09:45:00'), (38, 9, '2025-10-14 09:45:00'),
(39, 7, '2025-10-14 10:00:00'), (39, 8, '2025-10-14 10:00:00'),
(40, 7, '2025-10-14 10:30:00'), (40, 8, '2025-10-14 10:30:00'),

-- Project 8: Marketing tags
(27, 10, '2025-10-01 10:00:00'),
(28, 10, '2025-10-01 10:15:00'),
(29, 11, '2025-10-01 10:30:00'),
(30, 11, '2025-10-15 11:30:00'),

-- Project 14: ABC Website tags
(41, 13, '2025-10-05 12:00:00'),
(42, 13, '2025-10-05 12:15:00'),
(43, 13, '2025-10-05 12:30:00'),
(44, 14, '2025-10-20 09:30:00'),
(45, 14, '2025-10-20 10:00:00'),
(46, 14, '2025-10-20 10:30:00'), (46, 15, '2025-10-20 10:30:00'),
(47, 14, '2025-10-28 11:00:00'),
(48, 14, '2025-10-28 11:30:00'), (48, 15, '2025-10-28 11:30:00'),
(49, 14, '2025-11-05 10:30:00'), (49, 16, '2025-11-05 10:30:00');

-- =============================================
-- BƯỚC 5.7: TASK COMMENTS (Đã sửa task_id và commenter_id)
-- =============================================
INSERT INTO task_comments (id, task_id, commenter_id, content, parent_comment_id, created_at, updated_at) VALUES
-- Comments for ECOM-9 (Task 9: Shopping cart UI)
(1, 9, 10, 'I tested the cart UI on mobile and the quantity buttons are a bit small. Can we make them larger for better touch targets?', NULL, '2025-10-07 10:30:00', '2025-10-07 10:30:00'),
(2, 9, 9, 'Good catch! I will increase the button size to 44x44px as per iOS guidelines. Will update today.', 1, '2025-10-07 11:00:00', '2025-10-07 11:00:00'),
(3, 9, 5, 'Also, let\'s add a loading spinner when updating quantities to give better feedback to users.', 1, '2025-10-07 14:00:00', '2025-10-07 14:00:00'),

-- Comments for ECOM-12 (Task 12: Cart calculation bug)
(4, 12, 7, 'Found the issue! The SQL query was using string comparison instead of numeric comparison for price filtering. Fixing now.', NULL, '2025-10-10 16:30:00', '2025-10-10 16:30:00'),
(5, 12, 5, 'Great! Please also add unit tests for all filter combinations to prevent this in the future.', 4, '2025-10-10 17:00:00', '2025-10-10 17:00:00'),

-- Comments for PHX-5 (Task 39: Authentication logic)
(6, 39, 8, 'The JWT token refresh logic is working well in testing. Should we also implement automatic retry on 401 errors?', NULL, '2025-10-24 15:00:00', '2025-10-24 15:00:00'),
(7, 39, 12, 'Yes, good idea. Implement it with exponential backoff. Max 3 retries.', 6, '2025-10-24 16:00:00', '2025-10-24 16:00:00'),

-- Comments for MKT-4 (Task 30: Social media graphics)
(8, 30, 4, 'The social media graphics look amazing! Just one note: can we make the product image more prominent in the Instagram version?', NULL, '2025-10-27 09:00:00', '2025-10-27 09:00:00'),
(9, 30, 9, 'Thank you! I will adjust the composition to give more space to the product. Will have the updated version by EOD.', 8, '2025-10-27 10:30:00', '2025-10-27 10:30:00'),

-- Comments for ABC-5 (Task 45: About page development)
(10, 45, 21, 'The about page design is approved by the client. We can proceed with development.', NULL, '2025-10-30 14:00:00', '2025-10-30 14:00:00'),
(11, 45, 22, 'Perfect! I will start coding this afternoon. Should have it done by tomorrow.', 10, '2025-10-30 15:00:00', '2025-10-30 15:00:00'),

-- Comments for ECOM-1 (Task 1: Database schema)
(12, 1, 8, 'The database schema looks solid. I like the indexing strategy for better query performance.', NULL, '2025-09-03 11:00:00', '2025-09-03 11:00:00'),
(13, 1, 7, 'Thanks! I also added composite indexes for common query patterns. Should help a lot with scale.', 12, '2025-09-03 14:30:00', '2025-09-03 14:30:00'),

-- Comments for ECOM-13 (Task 13: Stripe integration)
(14, 13, 7, 'Stripe integration is 80% complete. Working on webhook handlers for payment events.', NULL, '2025-10-18 16:00:00', '2025-10-18 16:00:00'),
(15, 13, 5, 'Excellent progress! Make sure to handle all edge cases like failed payments and refunds.', 14, '2025-10-19 09:00:00', '2025-10-19 09:00:00'),

-- Comments for ABC-4 (Task 44: Homepage development)
(16, 44, 21, 'Homepage is looking great! Client wants to add a video background to the hero section.', NULL, '2025-10-26 10:00:00', '2025-10-26 10:00:00'),
(17, 44, 22, 'Sure, I can add that. Will need an optimized video file from you. Max 5MB please.', 16, '2025-10-26 11:00:00', '2025-10-26 11:00:00'),

-- Comments for DO-5 (Task 26: Blue-green deployment)
(18, 26, 11, 'Blue-green deployment strategy is working in staging. Ready for production testing.', NULL, '2025-11-15 15:00:00', '2025-11-15 15:00:00'),
(19, 26, 5, 'Great! Let\'s schedule a production test for this weekend when traffic is low.', 18, '2025-11-15 16:00:00', '2025-11-15 16:00:00');

-- =============================================
-- BƯỚC 5.8: TASK ATTACHMENTS (Đã sửa task_id và uploaded_by_id)
-- =============================================
INSERT INTO task_attachments (id, task_id, file_name, file_path, file_type, file_size, uploaded_by_id, uploaded_at) VALUES
-- Attachments for ECOM-6 (Task 6: Product listing UI)
(1, 6, 'product-listing-wireframe-v1.fig', '/uploads/ecom/2025/09/product-listing-wireframe-v1.fig', 'application/figma', 2458624, 9, '2025-09-16 14:00:00'),
(2, 6, 'product-listing-mockup-final.fig', '/uploads/ecom/2025/09/product-listing-mockup-final.fig', 'application/figma', 3147856, 9, '2025-09-21 16:30:00'),

-- Attachments for ECOM-7 (Task 7: Product detail UI)
(3, 7, 'product-detail-desktop.png', '/uploads/ecom/2025/09/product-detail-desktop.png', 'image/png', 1245789, 9, '2025-09-25 10:00:00'),
(4, 7, 'product-detail-mobile.png', '/uploads/ecom/2025/09/product-detail-mobile.png', 'image/png', 856321, 9, '2025-09-25 10:15:00'),

-- Attachments for ECOM-12 (Task 12: Cart bug)
(5, 12, 'bug-report-screenshot.png', '/uploads/ecom/2025/10/bug-report-screenshot.png', 'image/png', 445632, 10, '2025-10-10 15:45:00'),
(6, 12, 'console-error-logs.txt', '/uploads/ecom/2025/10/console-error-logs.txt', 'text/plain', 12458, 10, '2025-10-10 16:00:00'),

-- Attachments for PHX-4 (Task 38: Auth screens)
(7, 38, 'auth-screens-design-system.fig', '/uploads/phoenix/2025/10/auth-screens-design-system.fig', 'application/figma', 4256987, 9, '2025-10-16 15:00:00'),
(8, 38, 'mobile-ui-specs.pdf', '/uploads/phoenix/2025/10/mobile-ui-specs.pdf', 'application/pdf', 856421, 9, '2025-10-16 16:00:00'),

-- Attachments for MKT-4 (Task 30: Social graphics)
(9, 30, 'social-graphics-batch-1.zip', '/uploads/marketing/2025/11/social-graphics-batch-1.zip', 'application/zip', 15478965, 9, '2025-10-27 14:00:00'),
(10, 30, 'instagram-post-preview.jpg', '/uploads/marketing/2025/11/instagram-post-preview.jpg', 'image/jpeg', 987456, 9, '2025-10-28 11:00:00'),

-- Attachments for ABC-2 (Task 42: Wireframes)
(11, 42, 'abc-wireframes-all-pages.fig', '/uploads/abc-web/2025/10/abc-wireframes-all-pages.fig', 'application/figma', 5874123, 21, '2025-10-12 14:00:00'), 

-- Attachments for ABC-3 (Task 43: Mockups)
(12, 43, 'abc-corp-mockups-final.fig', '/uploads/abc-web/2025/10/abc-corp-mockups-final.fig', 'application/figma', 8965421, 21, '2025-10-22 17:00:00'),
(13, 43, 'abc-brand-guidelines.pdf', '/uploads/abc-web/2025/10/abc-brand-guidelines.pdf', 'application/pdf', 3458921, 21, '2025-10-16 10:00:00'),

-- Attachments for ABC-4 (Task 44: Homepage dev)
(14, 44, 'homepage-hero-video.mp4', '/uploads/abc-web/2025/10/homepage-hero-video.mp4', 'video/mp4', 4856321, 22, '2025-10-27 14:00:00'),

-- Attachments for PHOTO-2 (Task 52: Electronics photography)
(15, 52, 'electronics-batch-raw-files.zip', '/uploads/photo/2025/10/electronics-batch-raw-files.zip', 'application/zip', 125478965, 21, '2025-10-29 16:00:00'),

-- Attachments for PHOTO-3 (Task 53: Fashion photography)
(16, 53, 'fashion-shoot-contact-sheet.pdf', '/uploads/photo/2025/11/fashion-shoot-contact-sheet.pdf', 'application/pdf', 8965421, 21, '2025-11-05 17:00:00');
-- =============================================
-- BƯỚC 6: CÁC CÂU TRUY VẤN KIỂM TRA (DEBUG)
-- =============================================
SELECT 'Database setup completed successfully!' as Status;
SELECT 
    p.group_name AS permission_group,
    p.permission_code,
    p.permission_name AS description,
    IFNULL(
        GROUP_CONCAT(DISTINCT r.role_code ORDER BY r.role_code SEPARATOR ', '), 
        '--- CHƯA GÁN CHO VAI TRÒ NÀO ---'
    ) AS granted_to_roles
FROM permissions p
LEFT JOIN role_permissions rp ON p.id = rp.permission_id
LEFT JOIN roles r ON r.id = rp.role_id
GROUP BY p.id
ORDER BY 
    FIELD(p.group_name, 'Company', 'Workspace', 'Project', 'Task', 'Sprint', 'Backlog'),
    p.permission_code;

-- Permission by Role
SELECT 
    r.level AS role_level,
    r.role_code,
    r.role_name,
    IFNULL(
        GROUP_CONCAT(DISTINCT p.permission_code ORDER BY p.permission_code SEPARATOR ', '), 
        '--- KHÔNG CÓ QUYỀN NÀO ---'
    ) AS granted_permissions
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.id
GROUP BY r.id
ORDER BY 
    FIELD(r.level, 'SYSTEM', 'COMPANY', 'WORKSPACE', 'PROJECT'),
    r.role_code;

 -- ROLE BY USER
SELECT 
    u.id AS user_id,
    u.full_name,
    u.email,
    GROUP_CONCAT(DISTINCT r.role_code ORDER BY r.level SEPARATOR ', ') AS roles,
    GROUP_CONCAT(DISTINCT p.permission_code ORDER BY p.permission_code SEPARATOR ', ') AS permissions
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN company_members cm ON cm.user_id = u.id
LEFT JOIN workspace_members wm ON wm.user_id = u.id
LEFT JOIN project_members pm ON pm.user_id = u.id
LEFT JOIN roles r ON r.id IN (ur.role_id, cm.role_id, wm.role_id, pm.role_id)
LEFT JOIN role_permissions rp ON rp.role_id = r.id
LEFT JOIN permissions p ON p.id = rp.permission_id
WHERE u.status = 'ACTIVE'
GROUP BY u.id, u.full_name, u.email
ORDER BY u.id;