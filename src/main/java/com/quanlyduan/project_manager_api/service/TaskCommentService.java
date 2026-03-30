package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;

/**
 * Service quan ly toan bo nghiep vu lien quan den Binh luan (Comment) trong pham vi Task.
 * Chiu trach nhiem thiet lap luong thao tac trao doi thong tin giua cac thanh vien du an.
 */
public interface TaskCommentService {

    // ======================================================
    // 1. GHI VA LUU BINH LUAN (WRITE OPERATIONS)
    // ======================================================

    /**
     * Thuc hien them mot Binh luan moi vao Cong viec (Task).
     * Logic nghiep vu bao gom: Xac thuc nguoi dung, kiem tra su ton tai cua Task va luu noi dung trao doi.
     * * @param taskId ID dinh danh cua Cong viec can binh luan
     * @param request Noi dung chi tiet cua binh luan tu nguoi dung
     * @return DTO chua thong tin binh luan vua duoc khoi tao thanh cong
     */
    TaskCommentResponse addComment(Integer taskId, CommentRequest request);

    // ======================================================
    // 2. TRICH XUAT DU LIEU (READ OPERATIONS)
    // ======================================================

    /**
     * Truy xuat toan bo danh sach cac binh luan thuoc ve mot Cong viec.
     * Du lieu duoc tu dong sap xep theo thoi gian tao (Chronological Order - cu nhat den moi nhat).
     * * @param taskId ID dinh danh cua Cong viec
     * @return Danh sach cac DTO phan hoi chua noi dung va thong tin nguoi binh luan
     */
    List<TaskCommentResponse> getComments(Integer taskId);
}