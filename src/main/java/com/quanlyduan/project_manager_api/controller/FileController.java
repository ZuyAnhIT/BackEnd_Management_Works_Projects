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
@RequestMapping("/api/files") // We'll create a new endpoint for all files
@CrossOrigin("*")
public class FileController {

    // Use the same upload directory as your TaskAttachmentService
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            // 1. Create the full path to the file
            Path fileStorageLocation = Paths.get(this.uploadDir).toAbsolutePath().normalize();
            Path filePath = fileStorageLocation.resolve(filename).normalize();
            
            // 2. Load the file as a Spring Resource
            Resource resource = new UrlResource(filePath.toUri());

            // 3. Check if the file exists and is readable
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Không tìm thấy tệp " + filename);
            }

            // 4. Determine the file's content type
            String contentType = null;
            try {
                contentType = Files.probeContentType(filePath);
            } catch (IOException ex) {
                // Default to a binary stream if type is unknown
                contentType = "application/octet-stream";
            }

            // 5. Build the response
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Không tìm thấy tệp dữ liệu " + filename);
        }
    }
}