// File: src/main/java/com/quanlyduan/project_manager_api/controller/FileController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

@Controller
@RequestMapping("/api/files") // Endpoint chung cho việc truy xuất files (ảnh avatar, attachment)
@CrossOrigin("*")
/**
 * Controller xử lý việc truy xuất và tải xuống các tệp đã lưu trữ cục bộ.
 */
public class FileController {

    // Lấy đường dẫn thư mục upload từ cấu hình
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ======================================================
    // API TRUY XUẤT FILE (SERVE FILE)
    // ======================================================
    @GetMapping("/{filename:.+}") // Regex (.+) cho phép cả tên file có dấu chấm
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            // 1. TẠO ĐƯỜNG DẪN VÀ CHUẨN HÓA
            // Lấy vị trí lưu trữ gốc
            Path fileStorageLocation = Paths.get(this.uploadDir).toAbsolutePath().normalize();
            // Giải quyết đường dẫn đến file cụ thể
            Path filePath = fileStorageLocation.resolve(filename).normalize();

            // 2. LOAD FILE VÀO SPRING RESOURCE
            Resource resource = new UrlResource(filePath.toUri());

            // 3. KIỂM TRA TỒN TẠI VÀ KHẢ DỤNG
            if (!resource.exists() || !resource.isReadable()) {
                // Sửa thông báo sang tiếng Anh
                throw new ResourceNotFoundException("File not found: " + filename);
            }

            // 4. XÁC ĐỊNH LOẠI NỘI DUNG (MIME TYPE)
            String contentType = null;
            try {
                // Thử đoán Content Type từ tệp
                contentType = Files.probeContentType(filePath);
            } catch (IOException ex) {
                // Mặc định là binary stream nếu không xác định được loại tệp
                contentType = "application/octet-stream";
            }

            // 5. XÂY DỰNG HTTP RESPONSE
            return ResponseEntity.ok()
                    // Thiết lập Content Type
                    .contentType(MediaType.parseMediaType(contentType))
                    // Thiết lập header: 'inline' để hiển thị trực tiếp trên trình duyệt (cho ảnh)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    // Trả về Resource
                    .body(resource);

        } catch (MalformedURLException ex) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("File data not found: " + filename);
        }
    }
}