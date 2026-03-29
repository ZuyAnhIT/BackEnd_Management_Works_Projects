package com.quanlyduan.project_manager_api.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;

/**
 * Controller xử lý việc truy xuất và tải xuống các tệp tin (ảnh, tài liệu) được lưu trữ cục bộ trên server.
 */
@Controller
@RequestMapping("/api/files")
@CrossOrigin("*")
public class FileController {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final String CONTENT_DISPOSITION_INLINE = "inline; filename=\"%s\"";

    private final String uploadDir;

    // Khởi tạo thủ công và tiêm giá trị cấu hình thư mục lưu trữ
    public FileController(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * API truy xuất nội dung tệp tin dựa trên tên tệp.
     * Hỗ trợ hiển thị trực tiếp (inline) trên trình duyệt đối với các tệp hình ảnh.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            // Xác định và chuẩn hóa đường dẫn tệp tin
            Path fileStorageLocation = Paths.get(this.uploadDir).toAbsolutePath().normalize();
            Path filePath = fileStorageLocation.resolve(filename).normalize();

            // Nạp tệp tin vào Spring Resource
            Resource resource = new UrlResource(filePath.toUri());

            // Kiểm tra tệp tin có tồn tại và có thể đọc được hay không
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + filename);
            }

            // Xác định loại nội dung (MIME type)
            String contentType = determineContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_INLINE, resource.getFilename()))
                    .body(resource);

        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File data not found: " + filename);
        }
    }

    /**
     * Tự động xác định loại nội dung của tệp dựa trên định dạng tệp tin.
     */
    private String determineContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            return (contentType != null) ? contentType : DEFAULT_CONTENT_TYPE;
        } catch (IOException ex) {
            return DEFAULT_CONTENT_TYPE;
        }
    }
}