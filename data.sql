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
-- SCHEMA DATABASE - HỆ THỐNG QUẢN LÝ DỰ ÁN
-- Hỗ trợ mô hình Scrum, Kanban
-- =============================================

-- 1. BẢNG NGƯỜI DÙNG
CREATE TABLE NguoiDung (
    idNguoiDung INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    matKhau VARCHAR(255) NOT NULL,
    hoTen VARCHAR(255) NOT NULL,
    anhDaiDien VARCHAR(500),
    soDienThoai VARCHAR(20),
    ngaySinh DATE,
    gioiTinh ENUM('NAM', 'NU', 'KHAC'),
    trangThai ENUM('HOAT_DONG', 'TAM_KHOA', 'DA_XOA') DEFAULT 'HOAT_DONG',
    xacThucEmail BOOLEAN DEFAULT FALSE,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    lanDangNhapCuoi TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG TOKEN XÁC THỰC
CREATE TABLE Token (
    idToken INT PRIMARY KEY AUTO_INCREMENT,
    nguoiDungId INT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    loaiToken ENUM('ACCESS','REFRESH','RESET_PASSWORD','EMAIL_VERIFICATION','API') NOT NULL,
    trangThai ENUM('HOAT_DONG', 'DA_THU_HOI', 'HET_HAN') DEFAULT 'HOAT_DONG',
    ngayHetHan TIMESTAMP NOT NULL,
    diaChiIp VARCHAR(50),
    userAgent TEXT,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngaySuDungCuoi TIMESTAMP NULL,
    FOREIGN KEY (nguoiDungId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG CÀI ĐẶT NGƯỜI DÙNG
CREATE TABLE UserSetting (
    idUserSetting INT PRIMARY KEY AUTO_INCREMENT,
    nguoiDungId INT NOT NULL,
    ngonNgu VARCHAR(10) DEFAULT 'vi',
    muiGio VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
    cheDoHienThi ENUM('SANG', 'TOI', 'TU_DONG') DEFAULT 'SANG',
    thongBaoEmail BOOLEAN DEFAULT TRUE,
    thongBaoPush BOOLEAN DEFAULT TRUE,
    hienThiTrangChu VARCHAR(50) DEFAULT 'dashboard',
    cauHinhBoard JSON COMMENT 'Tùy chỉnh hiển thị board',
    cauHinhList JSON COMMENT 'Tùy chỉnh hiển thị list',
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (nguoiDungId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE,
    UNIQUE KEY uk_nguoiDung (nguoiDungId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG NHẬT KÝ HOẠT ĐỘNG
CREATE TABLE NguoiDungNhatKyHoatDong (
    idNguoiDungNhatKyHoatDong INT PRIMARY KEY AUTO_INCREMENT,
    nguoiDungId INT NOT NULL,
    hanhDong VARCHAR(255) NOT NULL,
    doiTuongLienQuan VARCHAR(100) COMMENT 'Task, Project, Comment...',
    doiTuongId INT,
    noiDungCu TEXT COMMENT 'Giá trị trước thay đổi',
    noiDungMoi TEXT COMMENT 'Giá trị sau thay đổi',
    diaChiIp VARCHAR(50),
    userAgent TEXT,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nguoiDungId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG QUYỀN HẠN (ROLE)
CREATE TABLE Role (
    idRole INT PRIMARY KEY AUTO_INCREMENT,
    maRole VARCHAR(100) NOT NULL UNIQUE COMMENT 'Mã định danh (code), VD: SYSTEM_ADMIN, COMPANY_ADMIN, PROJECT_MEMBER',
    tenRole VARCHAR(255) NOT NULL COMMENT 'Tên hiển thị, VD: Quản trị hệ thống, Quản trị công ty, Thành viên dự án',
    moTa TEXT,
    capDo ENUM('SYSTEM', 'COMPANY', 'WORKSPACE', 'PROJECT') NOT NULL COMMENT 'Phạm vi của quyền: Hệ thống, Công ty, Không gian, Dự án',
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BẢNG QUYỀN CHI TIẾT (PERMISSION)
CREATE TABLE Permission (
    idPermission INT PRIMARY KEY AUTO_INCREMENT,
    maPermission VARCHAR(100) NOT NULL UNIQUE COMMENT 'Mã định danh (code), VD: task:create, task:delete, project:invite_member',
    tenPermission VARCHAR(255) NOT NULL COMMENT 'Tên hiển thị, VD: Tạo công việc, Xóa công việc, Mời thành viên dự án',
    nhom VARCHAR(100) COMMENT 'Nhóm quyền để dễ quản lý, VD: Quản lý Task, Quản lý Dự án',
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. BẢNG MAP ROLE - PERMISSION
CREATE TABLE RolePermission (
    idRolePermission INT PRIMARY KEY AUTO_INCREMENT,
    roleId INT NOT NULL,
    permissionId INT NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roleId) REFERENCES Role(idRole) ON DELETE CASCADE,
    FOREIGN KEY (permissionId) REFERENCES Permission(idPermission) ON DELETE CASCADE,
    UNIQUE KEY uk_rolePermission (roleId, permissionId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. BẢNG MAP NGƯỜI DÙNG - ROLE (CHO QUYỀN HỆ THỐNG)
CREATE TABLE NguoiDungRole (
    idNguoiDungRole INT PRIMARY KEY AUTO_INCREMENT,
    nguoiDungId INT NOT NULL,
    roleId INT NOT NULL COMMENT 'FK đến Role có capDo = SYSTEM',
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nguoiDungId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE,
    FOREIGN KEY (roleId) REFERENCES Role(idRole) ON DELETE CASCADE,
    UNIQUE KEY uk_nguoiDungRole (nguoiDungId, roleId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. BẢNG CÔNG TY/TỔ CHỨC
CREATE TABLE CongTy (
    idCongTy INT PRIMARY KEY AUTO_INCREMENT,
    tenCongTy VARCHAR(255) NOT NULL,
    maCongTy VARCHAR(50) UNIQUE,
    moTa TEXT,
    logo VARCHAR(500),
    diaChi TEXT,
    soDienThoai VARCHAR(20),
    email VARCHAR(255),
    website VARCHAR(255),
    nguoiTaoId INT NOT NULL,
    trangThai ENUM('HOAT_DONG', 'TAM_DUNG', 'DA_XOA') DEFAULT 'HOAT_DONG',
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. BẢNG THÀNH VIÊN CÔNG TY
CREATE TABLE CongTyThanhVien (
    idCongTyThanhVien INT PRIMARY KEY AUTO_INCREMENT,
    congTyId INT NOT NULL,
    nguoiDungId INT NOT NULL,
    roleId INT NOT NULL COMMENT 'FK đến Role (capDo = COMPANY)',
    chucVu VARCHAR(100),
    phongBan VARCHAR(100),
    trangThai ENUM('HOAT_DONG', 'TAM_DUNG', 'DA_ROI') DEFAULT 'HOAT_DONG',
    ngayThamGia TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (congTyId) REFERENCES CongTy(idCongTy) ON DELETE CASCADE,
    FOREIGN KEY (nguoiDungId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE,
    FOREIGN KEY (roleId) REFERENCES Role(idRole) ON DELETE RESTRICT,
    UNIQUE KEY uk_congTyNguoiDung (congTyId, nguoiDungId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. BẢNG KHÔNG GIAN LÀM VIỆC (WORKSPACE)
CREATE TABLE KhongGian (
    idKhongGian INT PRIMARY KEY AUTO_INCREMENT,
    congTyId INT NOT NULL,
    tenKhongGian VARCHAR(255) NOT NULL,
    maKhongGian VARCHAR(50),
    moTa TEXT,
    anhBia VARCHAR(500),
    mauSac VARCHAR(7) DEFAULT '#3498db',
    nguoiTaoId INT NOT NULL,
    trangThai ENUM('HOAT_DONG', 'LUU_TRU', 'DA_XOA') DEFAULT 'HOAT_DONG',
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (congTyId) REFERENCES CongTy(idCongTy) ON DELETE CASCADE,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. BẢNG THÀNH VIÊN KHÔNG GIAN
CREATE TABLE KhongGianThanhVien (
    idKhongGianThanhVien INT PRIMARY KEY AUTO_INCREMENT,
    khongGianId INT NOT NULL,
    nguoiDungId INT NOT NULL,
    roleId INT NOT NULL COMMENT 'FK đến Role (capDo = WORKSPACE)',
    trangThai ENUM('HOAT_DONG', 'DA_ROI') DEFAULT 'HOAT_DONG',
    ngayThamGia TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (khongGianId) REFERENCES KhongGian(idKhongGian) ON DELETE CASCADE,
    FOREIGN KEY (nguoiDungId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE,
    FOREIGN KEY (roleId) REFERENCES Role(idRole) ON DELETE RESTRICT,
    UNIQUE KEY uk_khongGianNguoiDung (khongGianId, nguoiDungId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. BẢNG LOẠI DỰ ÁN
CREATE TABLE ProjectType (
    idProjectType INT PRIMARY KEY AUTO_INCREMENT,
    tenLoai VARCHAR(100) NOT NULL,
    maLoai VARCHAR(50) UNIQUE,
    moHinh ENUM('SCRUM', 'KANBAN', 'WATERFALL', 'HYBRID') NOT NULL,
    moTa TEXT,
    cauHinh JSON COMMENT 'Cấu hình workflow, trạng thái, quy trình',
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. BẢNG DỰ ÁN
CREATE TABLE DuAn (
    idDuAn INT PRIMARY KEY AUTO_INCREMENT,
    khongGianId INT NOT NULL,
    projectTypeId INT,
    tenDuAn VARCHAR(255) NOT NULL,
    maDuAn VARCHAR(50) NOT NULL,
    moTa TEXT,
    anhBia VARCHAR(500),
    mucTieu TEXT,
    nguoiQuanLyId INT,
    trangThai ENUM('MOI_TAO', 'DANG_THUC_HIEN', 'TAM_DUNG', 'HOAN_THANH', 'HUY_BO') DEFAULT 'MOI_TAO',
    uuTien ENUM('THAP', 'TRUNG_BINH', 'CAO', 'KHAN_CAP') DEFAULT 'TRUNG_BINH',
    ngayBatDau DATE,
    ngayKetThucDuKien DATE,
    ngayKetThucThucTe DATE,
    tienDo DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Phần trăm hoàn thành',
    nguoiTaoId INT NOT NULL,
    cauHinhBoard JSON COMMENT 'Cấu hình cột, workflow board',
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (khongGianId) REFERENCES KhongGian(idKhongGian) ON DELETE CASCADE,
    FOREIGN KEY (projectTypeId) REFERENCES ProjectType(idProjectType) ON DELETE SET NULL,
    FOREIGN KEY (nguoiQuanLyId) REFERENCES NguoiDung(idNguoiDung) ON DELETE SET NULL,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE RESTRICT,
    UNIQUE KEY uk_maDuAn (maDuAn, khongGianId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. BẢNG THÀNH VIÊN DỰ ÁN
CREATE TABLE DuAnThanhVien (
    idDuAnThanhVien INT PRIMARY KEY AUTO_INCREMENT,
    duAnId INT NOT NULL,
    nguoiDungId INT NOT NULL,
    roleId INT NOT NULL COMMENT 'FK đến Role (capDo = PROJECT)',
    trangThai ENUM('HOAT_DONG', 'DA_ROI') DEFAULT 'HOAT_DONG',
    ngayThamGia TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (duAnId) REFERENCES DuAn(idDuAn) ON DELETE CASCADE,
    FOREIGN KEY (nguoiDungId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE,
    FOREIGN KEY (roleId) REFERENCES Role(idRole) ON DELETE RESTRICT,
    UNIQUE KEY uk_duAnNguoiDung (duAnId, nguoiDungId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. BẢNG SPRINT
CREATE TABLE Sprint (
    idSprint INT PRIMARY KEY AUTO_INCREMENT,
    duAnId INT NOT NULL,
    tenSprint VARCHAR(255) NOT NULL,
    maSprint VARCHAR(50),
    mucTieu TEXT,
    trangThai ENUM('CHUA_BAT_DAU', 'DANG_THUC_HIEN', 'HOAN_THANH', 'HUY_BO') DEFAULT 'CHUA_BAT_DAU',
    ngayBatDau DATE,
    ngayKetThuc DATE,
    thoiLuongDuKien INT COMMENT 'Số ngày dự kiến',
    nguoiTaoId INT NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (duAnId) REFERENCES DuAn(idDuAn) ON DELETE CASCADE,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. BẢNG EPIC
CREATE TABLE Epic (
    idEpic INT PRIMARY KEY AUTO_INCREMENT,
    duAnId INT NOT NULL,
    tenEpic VARCHAR(255) NOT NULL,
    maEpic VARCHAR(50),
    moTa TEXT,
    mauSac VARCHAR(7) DEFAULT '#8e44ad',
    trangThai ENUM('MO', 'DANG_THUC_HIEN', 'HOAN_THANH', 'DONG') DEFAULT 'MO',
    ngayBatDau DATE,
    ngayKetThucDuKien DATE,
    nguoiTaoId INT NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (duAnId) REFERENCES DuAn(idDuAn) ON DELETE CASCADE,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. BẢNG TAG/NHÃN
CREATE TABLE Tag (
    idTag INT PRIMARY KEY AUTO_INCREMENT,
    duAnId INT NOT NULL,
    tenTag VARCHAR(100) NOT NULL,
    mauSac VARCHAR(7) DEFAULT '#95a5a6',
    moTa TEXT,
    nguoiTaoId INT NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (duAnId) REFERENCES DuAn(idDuAn) ON DELETE CASCADE,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE RESTRICT,
    UNIQUE KEY uk_tagDuAn (tenTag, duAnId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. BẢNG TASK (CÔNG VIỆC)
CREATE TABLE Task (
    idTask INT PRIMARY KEY AUTO_INCREMENT,
    duAnId INT NOT NULL,
    epicId INT,
    sprintId INT,
    taskChaId INT COMMENT 'Nếu là subtask',
    maTask VARCHAR(50) NOT NULL,
    tieuDe VARCHAR(500) NOT NULL,
    moTa TEXT,
    loaiTask ENUM('STORY', 'TASK', 'BUG', 'EPIC', 'SUBTASK') DEFAULT 'TASK',
    trangThai VARCHAR(50) DEFAULT 'TO_DO',
    uuTien ENUM('THAP', 'TRUNG_BINH', 'CAO', 'KHAN_CAP') DEFAULT 'TRUNG_BINH',
    nguoiGiaoId INT,
    nguoiThucHienId INT,
    nguoiDanhGiaId INT,
    storyPoint INT COMMENT 'Điểm Story cho Scrum',
    thoiGianUocTinh DECIMAL(10,2) COMMENT 'Thời gian ước tính (giờ)',
    thoiGianThucTe DECIMAL(10,2) COMMENT 'Thời gian thực tế (giờ)',
    ngayBatDau DATE,
    ngayKetThucDuKien DATE,
    ngayKetThucThucTe DATE,
    thuTuHienThi INT DEFAULT 0,
    nguoiTaoId INT NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (duAnId) REFERENCES DuAn(idDuAn) ON DELETE CASCADE,
    FOREIGN KEY (epicId) REFERENCES Epic(idEpic) ON DELETE SET NULL,
    FOREIGN KEY (sprintId) REFERENCES Sprint(idSprint) ON DELETE SET NULL,
    FOREIGN KEY (taskChaId) REFERENCES Task(idTask) ON DELETE CASCADE,
    FOREIGN KEY (nguoiGiaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE SET NULL,
    FOREIGN KEY (nguoiThucHienId) REFERENCES NguoiDung(idNguoiDung) ON DELETE SET NULL,
    FOREIGN KEY (nguoiDanhGiaId) REFERENCES NguoiDung(idNguoiDung) ON DELETE SET NULL,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE RESTRICT,
    UNIQUE KEY uk_maTask (maTask, duAnId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. BẢNG TASK SUB (CÔNG VIỆC CON)
CREATE TABLE TaskSub (
    idTaskSub INT PRIMARY KEY AUTO_INCREMENT,
    taskChaId INT NOT NULL,
    tieuDe VARCHAR(500) NOT NULL,
    moTa TEXT,
    trangThai ENUM('CHUA_LAM', 'DANG_LAM', 'HOAN_THANH') DEFAULT 'CHUA_LAM',
    nguoiThucHienId INT,
    thoiGianUocTinh DECIMAL(10,2) COMMENT 'Thời gian ước tính (giờ)',
    thuTuHienThi INT DEFAULT 0,
    nguoiTaoId INT NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (taskChaId) REFERENCES Task(idTask) ON DELETE CASCADE,
    FOREIGN KEY (nguoiThucHienId) REFERENCES NguoiDung(idNguoiDung) ON DELETE SET NULL,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. BẢNG LIÊN KẾT TAG VÀ TASK
CREATE TABLE TagTask (
    idTagTask INT PRIMARY KEY AUTO_INCREMENT,
    tagId INT NOT NULL,
    taskId INT NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tagId) REFERENCES Tag(idTag) ON DELETE CASCADE,
    FOREIGN KEY (taskId) REFERENCES Task(idTask) ON DELETE CASCADE,
    UNIQUE KEY uk_tagTask (tagId, taskId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. BẢNG BÌNH LUẬN TASK
CREATE TABLE TaskComment (
    idTaskComment INT PRIMARY KEY AUTO_INCREMENT,
    taskId INT NOT NULL,
    nguoiBinhLuanId INT NOT NULL,
    noiDung TEXT NOT NULL,
    commentChaId INT COMMENT 'Trả lời comment khác',
    daChinhSua BOOLEAN DEFAULT FALSE,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (taskId) REFERENCES Task(idTask) ON DELETE CASCADE,
    FOREIGN KEY (nguoiBinhLuanId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE,
    FOREIGN KEY (commentChaId) REFERENCES TaskComment(idTaskComment) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. BẢNG TỆP ĐÍNH KÈM TASK
CREATE TABLE TaskAttachment (
    idTaskAttachment INT PRIMARY KEY AUTO_INCREMENT,
    taskId INT NOT NULL,
    tenFile VARCHAR(500) NOT NULL,
    duongDan VARCHAR(1000) NOT NULL,
    loaiFile VARCHAR(100),
    kichThuoc BIGINT COMMENT 'Kích thước file (bytes)',
    nguoiTaiLenId INT NOT NULL,
    ngayTaiLen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (taskId) REFERENCES Task(idTask) ON DELETE CASCADE,
    FOREIGN KEY (nguoiTaiLenId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. BẢNG THÔNG BÁO
CREATE TABLE ThongBao (
    idThongBao INT PRIMARY KEY AUTO_INCREMENT,
    tieuDe VARCHAR(500) NOT NULL,
    noiDung TEXT NOT NULL,
    loaiThongBao ENUM('HE_THONG', 'DU_AN', 'TASK', 'BINH_LUAN', 'MENTION', 'DEADLINE') NOT NULL,
    lienKet VARCHAR(500) COMMENT 'Link đến nội dung liên quan',
    duAnId INT,
    taskId INT,
    nguoiTaoId INT,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (duAnId) REFERENCES DuAn(idDuAn) ON DELETE CASCADE,
    FOREIGN KEY (taskId) REFERENCES Task(idTask) ON DELETE CASCADE,
    FOREIGN KEY (nguoiTaoId) REFERENCES NguoiDung(idNguoiDung) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. BẢNG THÔNG BÁO - THÀNH VIÊN
CREATE TABLE ThongBaoThanhVien (
    idThongBaoThanhVien INT PRIMARY KEY AUTO_INCREMENT,
    thongBaoId INT NOT NULL,
    nguoiNhanId INT NOT NULL,
    daDoc BOOLEAN DEFAULT FALSE,
    ngayDoc TIMESTAMP NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (thongBaoId) REFERENCES ThongBao(idThongBao) ON DELETE CASCADE,
    FOREIGN KEY (nguoiNhanId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE,
    UNIQUE KEY uk_thongBaoNguoiNhan (thongBaoId, nguoiNhanId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 26. BẢNG LỜI MỜI VÀO CÔNG TY
CREATE TABLE CongTyLoiMoi (
    idLoiMoi INT PRIMARY KEY AUTO_INCREMENT,
    congTyId INT NOT NULL,
    email VARCHAR(255) NOT NULL COMMENT 'Email của người được mời',
    roleId INT NOT NULL COMMENT 'Role sẽ được gán sau khi chấp nhận',
    nguoiMoiId INT NOT NULL COMMENT 'Admin đã gửi lời mời',
    
    token VARCHAR(255) NOT NULL UNIQUE COMMENT 'Một chuỗi token duy nhất cho lời mời này',
    trangThai ENUM('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING',
    
    ngayHetHan TIMESTAMP NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (congTyId) REFERENCES CongTy(idCongTy) ON DELETE CASCADE,
    FOREIGN KEY (roleId) REFERENCES Role(idRole) ON DELETE RESTRICT,
    FOREIGN KEY (nguoiMoiId) REFERENCES NguoiDung(idNguoiDung) ON DELETE CASCADE,
    
    UNIQUE KEY uk_congTy_email_pending (congTyId, email, trangThai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DỮ LIỆU MẪU 
INSERT INTO Role (maRole, tenRole, moTa, capDo) 
VALUES ('COMPANY_ADMIN', 'Quản trị Công ty', 'Quyền cao nhất trong một công ty', 'COMPANY');

INSERT INTO Role (maRole, tenRole, moTa, capDo) 
VALUES ('COMPANY_MEMBER', 'Thành viên Công ty', 'Quyền cơ bản trong một công ty', 'COMPANY');