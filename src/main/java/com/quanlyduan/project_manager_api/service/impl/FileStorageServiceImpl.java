// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/FileStorageServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    // Đường dẫn gốc để lưu trữ file, đọc từ application.properties/yml, mặc định là "uploads"
    @Value("${app.upload.dir:uploads}")
    private String baseUploadDir;

    // ======================================================
    // LOGIC LƯU TRỮ FILE (STORE FILE)
    // ======================================================
    @Override
    public String storeFile(MultipartFile file, String folderName) {

        // 1. Kiểm tra file rỗng
        if (file.isEmpty()) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Cannot store an empty file.");
        }

        // 2. Làm sạch tên file và kiểm tra bảo mật (Path Traversal)
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        // Kiểm tra ký tự đường dẫn không hợp lệ
        if (originalFileName.contains("..")) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("File name contains invalid path sequence: " + originalFileName);
        }

        // 3. Tạo tên file mới (UUID + Extension) để tránh trùng lặp
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // 4. Định nghĩa và tạo thư mục đích (ví dụ: uploads/avatars)
            Path uploadPath = Paths.get(baseUploadDir, folderName);
            
            // Nếu thư mục chưa tồn tại, tạo mới
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 5. Lưu file vào đường dẫn cuối cùng
            Path filePath = uploadPath.resolve(newFileName);
            try (InputStream inputStream = file.getInputStream()) {
                // Copy stream, ghi đè nếu đã tồn tại file cùng tên
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 6. Trả về đường dẫn tương đối (để lưu vào Database)
            // Format: /<tên_thư_mục>/<tên_file_mới>
            return "/" + folderName + "/" + newFileName;

        } catch (IOException ex) {
            // Xử lý lỗi I/O trong quá trình lưu file
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Could not store file " + newFileName + ". Please try again!");
        }
    }
}