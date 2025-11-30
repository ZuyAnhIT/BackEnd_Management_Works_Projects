// File: src/main/java/com/quanlyduan/project_manager_api/service/FileStorageService.java
package com.quanlyduan.project_manager_api.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface Service quản lý các nghiệp vụ chung liên quan đến việc lưu trữ tệp tin (File Storage).
 * Service này chịu trách nhiệm lưu file vật lý vào hệ thống và trả về đường dẫn để lưu vào Database.
 */
public interface FileStorageService {
    
    /**
     * Lưu file đã upload từ client vào thư mục con chỉ định.
     * Logic này sử dụng để lưu Logo, Avatar, Cover Image, v.v.
     * * @param file File upload từ client (MultipartFile).
     * @param folderName Tên thư mục con (ví dụ: "avatars", "company-logos").
     * @return Đường dẫn tương đối (hoặc tên file) để lưu vào CSDL (ví dụ: /avatars/uuid.jpg).
     */
    String storeFile(MultipartFile file, String folderName);
    
    // (Ghi chú: Sau này có thể bổ sung các hàm như deleteFile, loadFile, copyFile để hoàn thiện chức năng)
}