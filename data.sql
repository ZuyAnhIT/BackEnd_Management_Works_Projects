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
-- BƯỚC 1: TẠO CẤU TRÚC BẢNG (ĐÃ SẮP XẾP LẠI)
-- =============================================

-- Cấp 0: Không phụ thuộc
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

CREATE TABLE project_types (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) UNIQUE,
    model ENUM('SCRUM', 'KANBAN', 'WATERFALL', 'HYBRID') NOT NULL,
    description TEXT,
    configuration JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cấp 1: Phụ thuộc Cấp 0
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
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
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

-- Cấp 2: Phụ thuộc Cấp 1
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

-- Cấp 3: Phụ thuộc Cấp 2
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

-- Cấp 4: Phụ thuộc Cấp 3
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

CREATE TABLE sprints (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sprint_code VARCHAR(50),
    goal TEXT,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'NOT_STARTED',
    start_date DATE,
    end_date DATE,
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

-- Cấp 5: Phụ thuộc Cấp 4 (TASK là phức tạp nhất)
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
    assigner_id INT,
    assignee_id INT,
    reviewer_id INT,
    story_points INT,
    estimated_hours DECIMAL(10,2),
    logged_hours DECIMAL(10,2),
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
    FOREIGN KEY (status_id) REFERENCES project_statuses(id) ON DELETE SET NULL,
    UNIQUE KEY uk_task_code (task_code, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cấp 6: Phụ thuộc Cấp 5
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

-- Cấp 7: Phụ thuộc Cấp 6
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
-- NẠP QUYỀN (PERMISSIONS)
INSERT INTO permissions (permission_code, permission_name, group_name) VALUES
('company:create', 'Tạo Công ty', 'Company'),
('company:view', 'Xem Công ty', 'Company'),
('company:edit', 'Sửa Công ty', 'Company'),
('company:delete', 'Xóa Công ty', 'Company'),
('company:invite_member', 'Mời Thành viên Công ty', 'Company'),
('company:remove_member', 'Xóa Thành viên Công ty', 'Company'),
('company:manage_roles', 'Quản lý Vai trò Công ty', 'Company'),
('workspace:create', 'Tạo Không gian', 'Workspace'),
('workspace:view', 'Xem Không gian', 'Workspace'),
('workspace:edit', 'Sửa Không gian', 'Workspace'),
('workspace:delete', 'Xóa Không gian', 'Workspace'),
('workspace:invite_member', 'Mời Thành viên Không gian', 'Workspace'),
('workspace:remove_member', 'Xóa Thành viên Không gian', 'Workspace'),
('workspace:manage_roles', 'Quản lý Vai trò Không gian', 'Workspace'),
('project:create', 'Tạo Dự án', 'Project'),
('project:view', 'Xem Dự án', 'Project'),
('project:edit', 'Sửa Dự án', 'Project'),
('project:delete', 'Xóa Dự án', 'Project'),
('project:invite_member', 'Mời Thành viên Dự án', 'Project'),
('project:manage_roles', 'Quản lý Vai trò Dự án', 'Project'),
('task:create', 'Tạo Công việc', 'Task'),
('task:view', 'Xem Công việc', 'Task'),
('task:edit', 'Sửa Công việc', 'Task'),
('task:delete', 'Xóa Công việc', 'Task'),
('task:assign', 'Gán Công việc', 'Task'),
('task:comment', 'Bình luận Công việc', 'Task'),
('task:comment:view', 'Xem Bình luận', 'Task'),
('task:attach_file', 'Đính kèm Tệp', 'Task'),
('sprint:create', 'Tạo Sprint', 'Sprint'),
('sprint:start', 'Bắt đầu Sprint', 'Sprint'),
('sprint:edit', 'Sửa Sprint', 'Sprint'),
('sprint:delete', 'Xóa Sprint', 'Sprint'),
('backlog:view', 'Xem Backlog', 'Backlog'),
('backlog:manage', 'Quản lý Backlog', 'Backlog');

-- NẠP VAI TRÒ (ROLES)
INSERT INTO roles (id, role_code, role_name, level, description) VALUES
(1, 'SYSTEM_ADMIN', 'Quản trị Hệ thống', 'SYSTEM', 'Toàn quyền truy cập hệ thống'),
(2, 'USER', 'Người dùng Hệ thống', 'SYSTEM', 'Người dùng cơ bản, có thể tạo công ty'),
(3, 'COMPANY_ADMIN', 'Quản trị Công ty', 'COMPANY', 'Toàn quyền truy cập trong công ty của họ'),
(4, 'COMPANY_MEMBER', 'Thành viên Công ty', 'COMPANY', 'Thành viên tiêu chuẩn của công ty'),
(5, 'WORKSPACE_ADMIN', 'Quản trị Không gian', 'WORKSPACE', 'Quản lý một không gian làm việc cụ thể'),
(6, 'WORKSPACE_MEMBER', 'Thành viên Không gian', 'WORKSPACE', 'Thành viên tiêu chuẩn của không gian làm việc'),
(7, 'PROJECT_ADMIN', 'Quản trị Dự án', 'PROJECT', 'Quản lý một dự án cụ thể (PM)'),
(8, 'PROJECT_MEMBER', 'Thành viên Dự án', 'PROJECT', 'Thành viên tiêu chuẩn của dự án (Dev, QA)'),
(9, 'GUEST_PROJECT', 'Khách (Dự án)', 'PROJECT', 'Quyền chỉ xem dự án (Khách hàng)');

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
    'task:comment:view'
) WHERE r.role_code = 'GUEST_PROJECT';


-- =============================================
-- BƯỚC 4: TẠO DỮ LIỆU CƠ BẢN
-- Mật khẩu cho tất cả user: admin123 (Hash: $2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG)
-- =============================================
-- TẠO CÁC USER (18 users)
INSERT INTO users (id, email, password, full_name, avatar_url, phone_number, date_of_birth, gender, status, is_email_verified) VALUES
(1, 'super.admin@system.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Quản trị Hệ thống', 'https://i.pravatar.cc/150?img=1', '0900000001', '1985-01-01', 'MALE', 'ACTIVE', 1),
(2, 'system.user@system.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Người dùng Hệ thống', 'https://i.pravatar.cc/150?img=2', '0900000002', '1990-05-15', 'FEMALE', 'ACTIVE', 1),
(3, 'admin@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Nguyễn Văn An', 'https://i.pravatar.cc/150?img=10', '0901234001', '1988-03-20', 'MALE', 'ACTIVE', 1),
(4, 'manager@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Trần Thị Bình', 'https://i.pravatar.cc/150?img=11', '0901234002', '1990-07-12', 'FEMALE', 'ACTIVE', 1),
(5, 'member1@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Lê Văn Cường', 'https://i.pravatar.cc/150?img=12', '0901234003', '1992-11-05', 'MALE', 'ACTIVE', 1),
(6, 'member2@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Phạm Thị Dung', 'https://i.pravatar.cc/150?img=13', '0901234004', '1995-02-28', 'FEMALE', 'ACTIVE', 1),
(7, 'dev1@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Hoàng Văn Em', 'https://i.pravatar.cc/150?img=14', '0901234005', '1993-09-18', 'MALE', 'ACTIVE', 1),
(8, 'dev2@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Võ Thị Phương', 'https://i.pravatar.cc/150?img=15', '0901234006', '1994-06-22', 'FEMALE', 'ACTIVE', 1),
(9, 'designer@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Nguyễn Thị Giang', 'https://i.pravatar.cc/150?img=16', '0901234007', '1996-12-30', 'FEMALE', 'ACTIVE', 1),
(10, 'tester@techvision.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Đỗ Văn Hải', 'https://i.pravatar.cc/150?img=17', '0901234008', '1997-04-15', 'MALE', 'ACTIVE', 1),
(11, 'admin@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Bùi Văn Khánh', 'https://i.pravatar.cc/150?img=20', '0902345001', '1987-08-10', 'MALE', 'ACTIVE', 1),
(12, 'manager@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Đặng Thị Lan', 'https://i.pravatar.cc/150?img=21', '0902345002', '1991-05-25', 'FEMALE', 'ACTIVE', 1),
(13, 'dev@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Trần Văn Minh', 'https://i.pravatar.cc/150?img=22', '0902345003', '1994-10-08', 'MALE', 'ACTIVE', 1),
(14, 'designer@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Lê Thị Nga', 'https://i.pravatar.cc/150?img=23', '0902345004', '1995-03-17', 'FEMALE', 'ACTIVE', 1),
(15, 'analyst@innovatech.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Phan Văn Oanh', 'https://i.pravatar.cc/150?img=24', '0902345005', '1993-07-21', 'OTHER', 'ACTIVE', 1),
(16, 'admin@digitalwave.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Ngô Văn Phúc', 'https://i.pravatar.cc/150?img=30', '0903456001', '1989-11-30', 'MALE', 'ACTIVE', 1),
(17, 'pm@digitalwave.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Vũ Thị Quỳnh', 'https://i.pravatar.cc/150?img=31', '0903456002', '1992-02-14', 'FEMALE', 'ACTIVE', 1),
(18, 'dev@digitalwave.com', '$2a$10$ldKpmYjkmjDzALsBGZ0x3Ov6pSpZu35IvLoccRqlRd7Drk9HVHKkG', 'Trương Văn Sơn', 'https://i.pravatar.cc/150?img=32', '0903456003', '1996-08-19', 'MALE', 'ACTIVE', 1);

-- BỔ SUNG BẢNG PROJECT TYPES
INSERT INTO project_types (id, type_name, type_code, model, description) VALUES
(1, 'Phát triển phần mềm (Scrum)', 'SW_SCRUM', 'SCRUM', 'Mô hình Scrum cho dự án phần mềm 2 tuần/sprint.'),
(2, 'Marketing (Kanban)', 'MKT_KANBAN', 'KANBAN', 'Mô hình Kanban liên tục cho team Marketing.');

-- TẠO MÔI TRƯỜNG (CÔNG TY, WORKSPACE, PROJECT)
INSERT INTO companies (id, name, company_code, description, logo_url, created_by_id, status, address, phone_number, email, website) VALUES
(1, 'TechVision Solutions', 'TECHV', 'Công ty phát triển phần mềm và giải pháp công nghệ, tập trung vào AI và Dữ liệu lớn.', 'https://api.dicebear.com/7.x/shapes/svg?seed=techvision', 3, 'ACTIVE', '123 Võ Văn Tần, Q3, TP.HCM', '02838123456', 'contact@techvision.com', 'https://techvision.com'),
(2, 'InnovaTech Group', 'INNOV', 'Tập đoàn công nghệ và đổi mới sáng tạo, chuyên về giải pháp IoT và Smart City.', 'https://api.dicebear.com/7.x/shapes/svg?seed=innovatech', 11, 'ACTIVE', '456 Lê Lợi, Q1, TP.HCM', '02838765432', 'info@innovatech.com', 'https://innovatech.com'),
(3, 'DigitalWave Agency', 'DIGW', 'Đại lý marketing số và thiết kế sáng tạo, chuyên về branding và chiến dịch.', 'https://api.dicebear.com/7.x/shapes/svg?seed=digitalwave', 16, 'ACTIVE', '789 Hai Bà Trưng, Đà Nẵng', '02363123789', 'hello@digitalwave.com', 'https://digitalwave.com');

INSERT INTO workspaces (id, company_id, name, created_by_id, status, description, cover_image_url, color) VALUES
(1, 1, 'Phòng Kỹ thuật (R&D)', 3, 'ACTIVE', 'Không gian cho team Kỹ thuật và R&D của TechVision.', 'https://images.unsplash.com/photo-1519389950473-47ba0277781c?q=80&w=2070', '#3498db'),
(2, 1, 'Phòng Thiết kế & Sản phẩm', 3, 'ACTIVE', 'Không gian cho team Thiết kế và Quản lý Sản phẩm.', 'https://images.unsplash.com/photo-1557804506-669a67965ba0?q=80&w=1974', '#9b59b6'),
(3, 1, 'Phòng Marketing & Sales', 4, 'ACTIVE', 'Không gian của team Marketing và Sales TechVision (do Manager tạo).', 'https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?q=80&w=2070', '#e67e22'),
(4, 2, 'Lab IoT InnovaTech', 11, 'ACTIVE', 'Phòng lab nghiên cứu và phát triển giải pháp IoT.', 'https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=2070', '#1abc9c'),
(5, 3, 'Phòng Sáng tạo DigitalWave', 16, 'ACTIVE', 'Không gian sáng tạo của DigitalWave.', 'https://images.unsplash.com/photo-1496065187959-7e07b8353c55?q=80&w=2070', '#e74c3c');

INSERT INTO projects (id, workspace_id, project_type_id, name, project_code, created_by_id, status, description, goal, manager_id, start_date, due_date) VALUES
(1, 1, 1, 'Nền tảng E-Commerce', 'ECOM', 5, 'IN_PROGRESS', 'Xây dựng nền tảng E-Commerce mới cho khách hàng XYZ.', 'Ra mắt MVP vào cuối Quý 4.', 5, '2025-09-01', '2025-12-31'),
(2, 1, 1, 'Ứng dụng Di động (Phoenix)', 'PHX', 7, 'IN_PROGRESS', 'Phát triển ứng dụng di động nội bộ cho TechVision.', 'Đạt 10,000 lượt tải trong 3 tháng đầu.', 5, '2025-10-01', '2026-03-31'),
(3, 2, 2, 'Tái định vị Thương hiệu 2026', 'REBRAND', 9, 'NEW', 'Thiết kế lại bộ nhận diện thương hiệu TechVision.', 'Hoàn thành logo và brand guidelines.', 9, '2025-11-15', '2026-01-31'),
(4, 3, 2, 'Chiến dịch Marketing Q4', 'MKT-Q4', 4, 'IN_PROGRESS', 'Chiến dịch quảng bá sản phẩm mới Quý 4.', 'Tăng 30% lead so với Quý 3.', 4, '2025-10-01', '2025-12-31'),
(5, 4, 1, 'Hub Nhà thông minh', 'SHH', 11, 'IN_PROGRESS', 'Phát triển Hub điều khiển trung tâm Smart Home.', 'Tích hợp 5 loại thiết bị.', 12, '2025-09-15', '2026-02-15'),
(6, 2, 2, 'Website ABC Corp (Kanban)', 'ABC-WEB', 9, 'IN_PROGRESS', 'Thiết kế và phát triển website cho khách hàng ABC Corp (chạy Kanban).', 'Bàn giao website trước 30/11.', 9, '2025-10-10', '2025-11-30');

-- =============================================
-- BƯỚC 5: TẠO DỮ LIỆU LIÊN QUAN (SPRINT, STATUS, TASK,...)
-- (ĐÃ SẮP XẾP LẠI THỨ TỰ VÀ SỬA LỖI)
-- =============================================

-- BƯỚC 5.1: TẠO PROJECT_STATUSES (BẮT BUỘC TRƯỚC TASK)
-- Project 1 (Scrum)
INSERT INTO project_statuses (id, project_id, name, color, sort_order, is_completed_status) VALUES
(1, 1, 'Cần làm', '#808080', 0, 0),
(2, 1, 'Đang làm', '#3498db', 1, 0),
(3, 1, 'Đang review', '#f1c40f', 2, 0),
(4, 1, 'Hoàn thành', '#2ecc71', 3, 1);
-- Project 2 (Scrum)
INSERT INTO project_statuses (id, project_id, name, color, sort_order, is_completed_status) VALUES
(5, 2, 'Cần làm', '#808080', 0, 0),
(6, 2, 'Đang làm', '#3498db', 1, 0),
(7, 2, 'Hoàn thành', '#2ecc71', 2, 1);
-- Project 3 (Kanban)
INSERT INTO project_statuses (id, project_id, name, color, sort_order, is_completed_status) VALUES
(8, 3, 'Backlog', '#a0aec0', 0, 0),
(9, 3, 'Đang thiết kế', '#9b59b6', 1, 0),
(10, 3, 'Hoàn thành', '#2ecc71', 2, 1);
-- Project 4 (Kanban)
INSERT INTO project_statuses (id, project_id, name, color, sort_order, is_completed_status) VALUES
(11, 4, 'Backlog', '#a0aec0', 0, 0),
(12, 4, 'Đang viết nội dung', '#e67e22', 1, 0),
(13, 4, 'Đang review', '#f1c40f', 2, 0),
(14, 4, 'Đã xuất bản', '#2ecc71', 3, 1);
-- Project 5 (Scrum)
INSERT INTO project_statuses (id, project_id, name, color, sort_order, is_completed_status) VALUES
(15, 5, 'Cần làm', '#808080', 0, 0),
(16, 5, 'Đang làm', '#3498db', 1, 0),
(17, 5, 'Hoàn thành', '#2ecc71', 2, 1);
-- Project 6 (Kanban)
INSERT INTO project_statuses (id, project_id, name, color, sort_order, is_completed_status) VALUES
(18, 6, 'Yêu cầu', '#a0aec0', 0, 0),
(19, 6, 'Đang thiết kế', '#9b59b6', 1, 0),
(20, 6, 'Đang phát triển', '#3498db', 2, 0),
(21, 6, 'Hoàn thành', '#2ecc71', 3, 1);

-- BƯỚC 5.2: TẠO EPICS VÀ SPRINTS
-- Project 1
INSERT INTO epics (id, project_id, name, epic_code, description, created_by_id, start_date, due_date) VALUES
(1, 1, 'Xác thực Người dùng', 'ECOM-A', 'Bao gồm đăng ký, đăng nhập, quên mật khẩu, OAuth2', 5, '2025-09-01', '2025-09-30'),
(2, 1, 'Danh mục Sản phẩm', 'ECOM-P', 'Hiển thị, tìm kiếm, lọc, chi tiết sản phẩm', 5, '2025-09-15', '2025-10-31');
-- Project 2
INSERT INTO epics (id, project_id, name, epic_code, description, created_by_id) VALUES
(3, 2, 'Các tính năng Cốt lõi (MVP)', 'MOB-C', 'Các tính năng cốt lõi cho bản MVP', 5);

INSERT INTO sprints (id, project_id, name, goal, status, start_date, end_date, duration_days, created_by_id) VALUES
(1, 1, 'Sprint 1 (Auth & Setup)', 'Hoàn thành luồng đăng nhập cơ bản và CSDL', 'COMPLETED', '2025-09-01', '2025-09-14', 14, 5),
(2, 1, 'Sprint 2 (Product List)', 'Người dùng có thể xem và lọc sản phẩm', 'IN_PROGRESS', '2025-09-15', '2025-09-28', 14, 5),
(3, 1, 'Sprint 3 (Checkout)', 'Hoàn thành giỏ hàng và thanh toán', 'NOT_STARTED', '2025-09-29', '2025-10-12', 14, 5),
(4, 2, 'Sprint 1 (Setup & Login)', 'Thiết lập dự án và đăng nhập', 'IN_PROGRESS', '2025-10-01', '2025-10-14', 14, 5);


-- BƯỚC 5.3: TẠO TASKS (ĐÃ SỬA DÙNG status_id VÀ SỬA LỖI CỘT)
INSERT INTO tasks (
    id, project_id, sprint_id, epic_id, task_code, 
    title, description, status_id, priority, 
    created_by_id, assigner_id, assignee_id, 
    story_points, due_date, start_date, task_type
) VALUES
-- Project 1 (Statuses: 1-To Do, 2-In Progress, 3-Review, 4-Done)
(1, 1, 1, 1, 'ECOM-1', 'Tạo CSDL User và AuthToken', 'Mô tả chi tiết cho task Tạo CSDL User và AuthToken', 4, 'HIGH', 5, 5, 7, 3, '2025-09-05', '2025-09-02', 'TASK'),
(2, 1, 1, 1, 'ECOM-2', 'API Đăng nhập và Đăng ký (Email/Pass)', 'Mô tả chi tiết cho task API Đăng nhập', 4, 'HIGH', 5, 5, 7, 5, '2025-09-10', '2025-09-06', 'TASK'),
(3, 1, 2, 2, 'ECOM-3', 'API Lấy danh sách sản phẩm (Phân trang, Lọc)', 'Mô tả chi tiết cho task API Lấy danh sách sản phẩm', 2, 'MEDIUM', 5, 5, 8, 8, '2025-09-20', '2025-09-16', 'TASK'),
(4, 1, 2, 2, 'ECOM-4', 'UI Trang chủ và Danh sách sản phẩm', 'Mô tả chi tiết cho task UI Trang chủ', 1, 'MEDIUM', 5, 5, 9, 5, '2025-09-22', '2025-09-17', 'STORY'),
(5, 1, 2, NULL, 'ECOM-5', 'Lỗi 500 khi filter theo giá sản phẩm', 'Người dùng báo lỗi khi filter giá từ 1000-5000', 1, 'URGENT', 10, 5, 7, NULL, '2025-09-18', NULL, 'BUG'),
-- Project 2 (Statuses: 5-To Do, 6-In Progress, 7-Done)
(6, 2, 4, 3, 'MOB-1', 'Thiết lập dự án React Native', 'Cài đặt Expo và các thư viện điều hướng', 6, 'HIGH', 5, 5, 8, 3, '2025-10-05', '2025-10-02', 'TASK'),
(7, 2, 4, 3, 'MOB-2', 'UI/UX Màn hình đăng nhập', 'Thiết kế Figma và implement UI', 5, 'MEDIUM', 5, 5, 9, 5, '2025-10-10', '2025-10-06', 'STORY'),
-- Project 4 (Statuses: 11-Backlog, 12-Content, 13-Review, 14-Published)
(8, 4, NULL, NULL, 'MKTG-2', 'Viết bài blog cho sản phẩm mới', 'Viết bài blog 1000 từ về tính năng mới.', 12, 'MEDIUM', 4, 4, 5, 5, '2025-11-25', '2025-11-15', 'TASK'),
(9, 4, NULL, NULL, 'MKTG-3', 'Thiết kế banner quảng cáo Facebook', 'Thiết kế 3 mẫu banner cho chiến dịch Q4.', 11, 'LOW', 4, 4, 4, 2, '2025-11-30', NULL, 'TASK'),
-- Project 6 (Statuses: 18-Requirement, 19-Designing, 20-Dev, 21-Done)
(10, 6, NULL, NULL, 'ABC-1', 'Phân tích yêu cầu khách hàng', 'Họp với khách hàng ABC và chốt yêu cầu', 21, 'HIGH', 9, 9, 9, NULL, '2025-11-05', '2025-11-01', 'TASK'),
(11, 6, NULL, NULL, 'ABC-2', 'Thiết kế Wireframe (Homepage, Contact)', 'Sử dụng Figma để vẽ wireframe', 19, 'HIGH', 9, 9, 9, NULL, '2025-11-15', '2025-11-06', 'TASK'),
(12, 6, NULL, NULL, 'ABC-3', 'Thiết kế Mockup UI (Figma)', 'Hoàn thiện UI dựa trên wireframe đã duyệt', 18, 'MEDIUM', 9, 9, 9, NULL, '2025-11-25', '2025-11-16', 'TASK');


-- BƯỚC 5.4: THÊM SUB-TASKS (CHO TASK ID 2)
INSERT INTO sub_tasks (id, parent_task_id, title, status, assignee_id, created_by_id) VALUES
(1, 2, 'Endpoint /register', 'DONE', 7, 10),
(2, 2, 'Endpoint /login (JWT)', 'IN_PROGRESS', 7, 10),
(3, 2, 'Endpoint /forgot-password', 'TO_DO', 7, 10);

-- BƯỚC 5.5: THÊM TASK COMMENTS 
INSERT INTO task_comments (id, task_id, commenter_id, content, parent_comment_id) VALUES
(1, 4, 10, 'Em check lại màu sắc cho nút CTA nhé, hơi tối.', NULL), 
(2, 4, 9, 'Dạ vâng, em đã cập nhật lại màu #3498db ạ.', 1),
(3, 1, 8, 'Thiết kế CSDL nhìn ổn, tôi thích.', NULL),
(4, 9, 4, 'Charlie duyệt xong nội dung chưa? Nhớ thêm hashtag #PixelCore nhé.', NULL);

-- BƯỚC 5.6: THÊM TAGS
INSERT INTO tags (id, project_id, name, color, created_by_id) VALUES
(1, 1, 'Backend', '#f39c12', 5), 
(2, 1, 'Frontend', '#3498db', 5), 
(3, 1, 'Bug', '#e74c3c', 5), 
(4, 1, 'Auth', '#8e44ad', 5),
(5, 4, 'Marketing', '#2ecc71', 4),
(6, 6, 'Design', '#9b59b6', 9);

-- BƯỚC 5.7: THÊM TASK_TAGS
INSERT INTO task_tags (task_id, tag_id) VALUES
(1, 1), (1, 4), (2, 1), (2, 4), (3, 1), (4, 2), (5, 1), (5, 3),
(8, 5), (9, 5), (11, 6), (12, 6);

-- BƯỚC 5.8: THÊM TASK ATTACHMENTS (CHO TASK ID 4)
INSERT INTO task_attachments (id, task_id, file_name, file_path, file_type, file_size, uploaded_by_id) VALUES
(1, 4, 'Homepage_Mockup_v1.fig', '/uploads/project1/Homepage_Mockup_v1.fig', 'application/figma', 1024000, 9),
(2, 4, 'Homepage_Mockup_v2_updated.fig', '/uploads/project1/Homepage_Mockup_v2_updated.fig', 'application/figma', 1056000, 9);


-- =============================================
-- BƯỚC 6: GÁN VAI TRÒ (MEMBERSHIPS)
-- =============================================

-- GÁN VAI TRÒ CẤP HỆ THỐNG
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, (SELECT id FROM roles WHERE role_code = 'SYSTEM_ADMIN')),
(2, (SELECT id FROM roles WHERE role_code = 'USER'));

-- GÁN VAI TRÒ CẤP CÔNG TY
INSERT INTO company_members (company_id, user_id, role_id, status, job_title, department) VALUES
-- Cty 1: TechVision
(1, 3, (SELECT id FROM roles WHERE role_code = 'COMPANY_ADMIN'), 'ACTIVE', 'Giám đốc Điều hành', 'Ban Giám đốc'),
(1, 4, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Trưởng phòng Marketing', 'Marketing'),
(1, 5, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Project Manager', 'Engineering'),
(1, 6, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Product Owner', 'Product'),
(1, 7, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Senior Developer', 'Engineering'),
(1, 8, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Full-stack Developer', 'Engineering'),
(1, 9, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'UI/UX Designer', 'Design'),
(1, 10, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'QA Lead', 'Engineering'),
-- Cty 2: InnovaTech
(2, 11, (SELECT id FROM roles WHERE role_code = 'COMPANY_ADMIN'), 'ACTIVE', 'Giám đốc', 'Ban Giám đốc'),
(2, 12, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Trưởng phòng Kinh doanh', 'Sales'),
(2, 13, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'IoT Developer', 'R&D'),
(2, 14, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'ACTIVE', 'Lead Designer', 'Design'),
(2, 15, (SELECT id FROM roles WHERE role_code = 'COMPANY_MEMBER'), 'SUSPENDED', 'Business Analyst', 'Analysis');

-- GÁN VAI TRÒ CẤP WORKSPACE
-- WS 1: Kỹ thuật (Cty 1)
INSERT INTO workspace_members (workspace_id, user_id, role_id, status) VALUES
(1, 3, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE'), -- An (Admin Cty)
(1, 5, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE'), -- Cường (PM) là Admin WS
(1, 7, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'), -- Em
(1, 8, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'), -- Phương
(1, 10, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'); -- Hải (QA)
-- WS 2: Thiết kế & Sản phẩm (Cty 1)
INSERT INTO workspace_members (workspace_id, user_id, role_id, status) VALUES
(2, 3, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE'), -- An (Admin Cty)
(2, 6, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE'), -- Dung (PO) là Admin WS
(2, 9, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'), -- Giang
(2, 5, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'); -- Cường (PM) cũng ở WS này
-- WS 3: Marketing & Sales (Cty 1)
INSERT INTO workspace_members (workspace_id, user_id, role_id, status) VALUES
(3, 3, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE'), -- An (Admin Cty)
(3, 4, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'); -- Bình
-- WS 4: Lab IoT (Cty 2)
INSERT INTO workspace_members (workspace_id, user_id, role_id, status) VALUES
(4, 11, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_ADMIN'), 'ACTIVE'), -- Khánh (Admin Cty)
(4, 12, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'), -- Lan
(4, 13, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'ACTIVE'), -- Minh
(4, 15, (SELECT id FROM roles WHERE role_code = 'WORKSPACE_MEMBER'), 'REMOVED'); -- Oanh (Đã xóa khỏi WS)

-- GÁN VAI TRÒ CẤP PROJECT
-- Project 1: E-Commerce Platform (thuộc WS 1)
INSERT INTO project_members (project_id, user_id, role_id, status) VALUES
(1, 3, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE'), -- An (Admin Cty)
(1, 5, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE'), -- Cường (Admin WS)
(1, 7, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE'), -- Em (Dev)
(1, 8, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE'), -- Phương (Dev)
(1, 10, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE'), -- Hải (QA)
(1, 9, (SELECT id FROM roles WHERE role_code = 'GUEST_PROJECT'), 'ACTIVE'); -- Giang (Guest từ WS 2)
-- Project 2: Mobile App (Phoenix) (thuộc WS 1)
INSERT INTO project_members (project_id, user_id, role_id, status) VALUES
(2, 3, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE'), -- An (Admin Cty)
(2, 5, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE'), -- Cường (Admin WS)
(2, 8, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE'), -- Phương
(2, 9, (SELECT id FROM roles WHERE role_code = 'PROJECT_MEMBER'), 'ACTIVE'); -- Giang
-- Project 6: Website ABC Corp (Kanban) (thuộc WS 2)
INSERT INTO project_members (project_id, user_id, role_id, status) VALUES
(6, 3, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE'), -- An (Admin Cty)
(6, 6, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE'), -- Dung (Admin WS)
(6, 9, (SELECT id FROM roles WHERE role_code = 'PROJECT_ADMIN'), 'ACTIVE'), -- Giang (Designer) là Admin Dự án này
(6, 5, (SELECT id FROM roles WHERE role_code = 'GUEST_PROJECT'), 'ACTIVE'); -- Cường (PM) là Khách xem

-- =============================================
-- BƯỚC 7: DỮ LIỆU MẪU KHÁC (INVITATIONS, TOKENS)
-- =============================================
INSERT INTO company_invitations (company_id, email, role_id, invited_by_id, token, status, expires_at) VALUES
-- An (ID 3) mời 'user.new@example.com' làm COMPANY_MEMBER (ID 4) cho Cty 1
(1, 'new.guest@example.com', 4, 3, 'token-pending-1', 'PENDING', '2025-12-01 00:00:00'),
-- Khánh (ID 11) mời 'dev1@techvision.com' (User 7) làm COMPANY_MEMBER (ID 4) cho Cty 2
(2, 'dev1@techvision.com', 4, 11, 'token-pending-2', 'PENDING', '2025-12-01 00:00:00');

INSERT INTO auth_tokens (user_id, token, token_type, status, expires_at) VALUES
(3, 'token-anna-reset', 'RESET_PASSWORD', 'ACTIVE', '2025-12-01 00:00:00'),
(2, 'token-newuser-verify', 'EMAIL_VERIFICATION', 'ACTIVE', '2025-12-01 00:00:00');

-- =============================================
-- BƯỚC 8: CÁC CÂU TRUY VẤN KIỂM TRA (DEBUG)
-- =============================================
-- (Giữ nguyên các câu SELECT debug của bạn)
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