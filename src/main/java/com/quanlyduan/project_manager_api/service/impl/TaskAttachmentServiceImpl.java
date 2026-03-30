package com.quanlyduan.project_manager_api.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.TaskAttachment;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.TaskAttachmentRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.service.QuotaValidationService;
import com.quanlyduan.project_manager_api.service.TaskAttachmentService;

@Service
public class TaskAttachmentServiceImpl implements TaskAttachmentService {

    // Khai bao hang so de loai bo hardcode
    public static final String ACTION_UPLOAD = "UPLOAD";
    public static final String ENTITY_TASK = "TASK";
    public static final String DESC_UPLOAD_FILE = "Upload file attachment";

    public static final String ERROR_TASK_NOT_FOUND = "Task not found.";
    public static final String ERROR_UPLOADER_NOT_FOUND = "Uploader user not found.";

    public static final String FOLDER_PREFIX_TASK = "task-";
    public static final String FILE_SEPARATOR_UNDERSCORE = "_";
    public static final String URL_API_DOWNLOAD = "/api/attachments/%d/download";

    // Duong dan thu muc luu tru duoc cau hinh tu file properties
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Khai bao cac bien phu thuoc
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAttachmentRepository attachmentRepository;
    private final QuotaValidationService quotaValidationService;
    private final CompanyRepository companyRepository;

    // Constructor khoi tao thu cong
    public TaskAttachmentServiceImpl(TaskRepository taskRepository,
                                     UserRepository userRepository,
                                     TaskAttachmentRepository attachmentRepository,
                                     QuotaValidationService quotaValidationService,
                                     CompanyRepository companyRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.quotaValidationService = quotaValidationService;
        this.companyRepository = companyRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPLOAD, entityType = ENTITY_TASK, description = DESC_UPLOAD_FILE)
    public TaskAttachmentResponse storeAttachment(Integer taskId, MultipartFile file, Integer uploaderId) throws IOException {
        // Kiem tra cong viec ton tai
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND));
        
        // Kiem tra thong tin nguoi tai len
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_UPLOADER_NOT_FOUND));

        // Kiem tra han muc dung luong cua cong ty truoc khi luu file
        Company company = task.getProject().getWorkspace().getCompany();
        quotaValidationService.validateStorageQuota(company.getId(), file.getSize());

        // Tao thu muc rieng de quan ly file cho tung cong viec
        Path dirPath = Paths.get(uploadDir, FOLDER_PREFIX_TASK + taskId);
        Files.createDirectories(dirPath); 

        // Sinh ten file duy nhat ket hop voi thoi gian de tranh trung lap
        String originalFileName = file.getOriginalFilename();
        String storedFileName = System.currentTimeMillis() + FILE_SEPARATOR_UNDERSCORE + originalFileName;
        Path filePath = dirPath.resolve(storedFileName);

        // Luu file vao he thong luu tru cuc bo
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Luu thong tin tep dinh kem vao co so du lieu
        TaskAttachment attachment = TaskAttachment.builder()
                .task(task)
                .fileName(originalFileName)
                .filePath(filePath.toString()) 
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(uploader)
                .build();

        TaskAttachment saved = attachmentRepository.save(attachment);

        // Cap nhat lai tong dung luong da su dung cua cong ty
        long currentStorage = company.getCurrentStorageBytes() != null ? company.getCurrentStorageBytes() : 0;
        company.setCurrentStorageBytes(currentStorage + file.getSize());
        companyRepository.save(company);

        return mapToAttachmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAttachmentResponse> getAttachmentsForTask(Integer taskId) {
        // Kiem tra su ton tai cua cong viec truoc khi truy xuat danh sach tep dinh kem
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException(ERROR_TASK_NOT_FOUND);
        }

        List<TaskAttachment> attachments = attachmentRepository.findByTask_Id(taskId);

        // Chuyen doi danh sach tep dinh kem tu kieu thuc the sang DTO phan hoi
        return attachments.stream()
                .map(this::mapToAttachmentResponse)
                .collect(Collectors.toList());
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private TaskAttachmentResponse mapToAttachmentResponse(TaskAttachment a) {
        // Tao URL ao de ho tro viec tai tep thong qua API he thong
        String fileUrl = String.format(URL_API_DOWNLOAD, a.getId());

        return TaskAttachmentResponse.builder()
                .id(a.getId())
                .taskId(a.getTask().getId())
                .fileName(a.getFileName())
                .fileType(a.getFileType())
                .fileSize(a.getFileSize())
                .fileUrl(fileUrl) 
                .uploadedById(a.getUploadedBy().getId())
                .uploadedByName(a.getUploadedBy().getFullName())
                .uploadedAt(a.getUploadedAt())
                .projectId(a.getTask().getProject().getId())
                .workspaceId(a.getTask().getProject().getWorkspace().getId())
                .companyId(a.getTask().getProject().getWorkspace().getCompany().getId())
                .build();
    }
}