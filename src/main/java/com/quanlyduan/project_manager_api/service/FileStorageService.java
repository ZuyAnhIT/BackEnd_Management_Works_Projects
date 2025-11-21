// File: src/main/java/com/quanlyduan/project_manager_api/service/FileStorageService.java
package com.quanlyduan.project_manager_api.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Lưu file vào thư mục chỉ định.
     * @param file File upload từ client
     * @param folderName Tên thư mục con (ví dụ: "avatars", "logos")
     * @return Đường dẫn (hoặc tên file) để lưu vào CSDL
     */
    String storeFile(MultipartFile file, String folderName);
    
    // (Sau này có thể thêm hàm deleteFile, loadFile...)
}