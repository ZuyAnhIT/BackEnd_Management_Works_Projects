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

    @Value("${app.upload.dir:uploads}") // Mặc định lưu vào thư mục "uploads" ở gốc dự án
    private String baseUploadDir;

    @Override
    public String storeFile(MultipartFile file, String folderName) {
        // 1. Kiểm tra file rỗng
        if (file.isEmpty()) {
            throw new BadRequestException("Không thể lưu tệp rỗng.");
        }

        // 2. Làm sạch tên file
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new BadRequestException("Tên tệp chứa đường dẫn không hợp lệ: " + originalFileName);
        }

        // 3. Tạo tên file mới (UUID) để tránh trùng lặp
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // 4. Tạo thư mục đích nếu chưa tồn tại (ví dụ: uploads/avatars)
            Path uploadPath = Paths.get(baseUploadDir, folderName);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 5. Lưu file
            Path filePath = uploadPath.resolve(newFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 6. Trả về đường dẫn tương đối để lưu vào DB (hoặc URL đầy đủ nếu muốn)
            // Ví dụ trả về: "/avatars/uuid-abc.jpg"
            return "/" + folderName + "/" + newFileName;

        } catch (IOException ex) {
            throw new BadRequestException("Không thể lưu tệp " + newFileName + ". Vui lòng thử lại!");
        }
    }
}