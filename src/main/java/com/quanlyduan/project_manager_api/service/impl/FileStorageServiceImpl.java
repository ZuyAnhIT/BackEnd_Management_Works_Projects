package com.quanlyduan.project_manager_api.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.service.FileStorageService;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ERROR_EMPTY_FILE = "Cannot store an empty file.";
    public static final String ERROR_INVALID_PATH = "File name contains invalid path sequence: ";
    public static final String ERROR_STORE_FILE_PREFIX = "Could not store file ";
    public static final String ERROR_STORE_FILE_SUFFIX = ". Please try again!";
    
    public static final String PATH_TRAVERSAL_SEQ = "..";
    public static final String PATH_SEPARATOR = "/";
    public static final String EMPTY_STRING = "";
    public static final char EXTENSION_SEPARATOR = '.';

    // Duong dan goc de luu tru file lay tu file cau hinh
    @Value("${app.upload.dir:uploads}")
    private String baseUploadDir;

    // Khoi tao mac dinh (Khong co @RequiredArgsConstructor do chi dung @Value)
    public FileStorageServiceImpl() {
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    public String storeFile(MultipartFile file, String folderName) {
        // Kiem tra file dau vao
        if (file.isEmpty()) {
            throw new BadRequestException(ERROR_EMPTY_FILE);
        }

        // Lam sach ten file de tranh cac cuoc tan cong thay doi duong dan (Path Traversal)
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        if (originalFileName.contains(PATH_TRAVERSAL_SEQ)) {
            throw new BadRequestException(ERROR_INVALID_PATH + originalFileName);
        }

        // Tao ten file moi bang UUID de dam bao tinh duy nhat
        String fileExtension = EMPTY_STRING;
        int dotIndex = originalFileName.lastIndexOf(EXTENSION_SEPARATOR);
        
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // Dinh nghia duong dan thu muc va tao moi neu chua ton tai
            Path uploadPath = Paths.get(baseUploadDir, folderName);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Ghi file vao he thong luu tru, ghi de neu da ton tai file cung ten
            Path filePath = uploadPath.resolve(newFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Tra ve duong dan tuong doi de phuc vu viec truy xuat va luu co so du lieu
            return PATH_SEPARATOR + folderName + PATH_SEPARATOR + newFileName;

        } catch (IOException ex) {
            // Nem loi ngoai le khi co su co lien quan den IO
            throw new BadRequestException(ERROR_STORE_FILE_PREFIX + newFileName + ERROR_STORE_FILE_SUFFIX);
        }
    }
}