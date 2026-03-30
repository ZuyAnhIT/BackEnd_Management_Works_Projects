package com.quanlyduan.project_manager_api.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;

/**
 * Service quan ly toan bo nghiep vu lien quan den Tep dinh kem (Attachment) trong Task.
 * Chiu trach nhiem phoi hop voi FileStorageService de luu tru tep vat ly 
 * va quan ly thong tin dinh danh cua tep trong CSDL.
 */
public interface TaskAttachmentService {
    
    // ======================================================
    // 1. LUU TRU VA KHOI TAO (WRITE OPERATIONS)
    // ======================================================

    /**
     * Thuc hien luu tep tin vat ly vao he thong va tao ban ghi dinh kem cho Task.
     * Logic nghiep vu bao gom: Kiem tra han muc (Quota), luu file va sinh metadata.
     * * @param taskId ID cua Cong viec duoc dinh kem tep
     * @param file Du lieu tep tin tai len tu Client (MultipartFile)
     * @param uploaderId ID cua nguoi dung thuc hien tai len
     * @return DTO chua thong tin chi tiet cua tep dinh kem vua tao
     * @throws IOException Neu xay ra loi trong qua trinh doc/ghi du lieu tep tin
     */
    TaskAttachmentResponse storeAttachment(Integer taskId, MultipartFile file, Integer uploaderId) throws IOException;

    // ======================================================
    // 2. TRICH XUAT DU LIEU (READ OPERATIONS)
    // ======================================================

    /**
     * Truy xuat danh sach toan bo cac tep tin dinh kem thuoc ve mot Cong viec.
     * * @param taskId ID dinh danh cua Cong viec
     * @return Danh sach cac DTO tep dinh kem (bao nam ten file, kich thuoc, ngay tao)
     */
    List<TaskAttachmentResponse> getAttachmentsForTask(Integer taskId);
}