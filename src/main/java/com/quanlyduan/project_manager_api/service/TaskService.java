package com.quanlyduan.project_manager_api.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.ImportTaskResultResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskImportPreviewResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.Task;

/**
 * Service quan ly toan bo nghiep vu lien quan den Cong viec (Task).
 * Xu ly vong doi cua Task tu khi khoi tao, dieu chuyen giua cac Sprint/Status 
 * den cac tinh nang nang cao nhu nhap du lieu hang loat (Import).
 */
public interface TaskService {

    // ======================================================
    // 1. QUAN LY CO BAN (CRUD OPERATIONS)
    // ======================================================

    /**
     * Khoi tao mot Cong viec moi trong pham vi Du an.
     * Ho tro Quick Create (chi can tieu de) de toi uu trai nghiem nguoi dung.
     * * @param projectId ID Du an so huu Task
     * @param request Thong tin khoi tao Task
     * @return DTO tom tat cua Task vua tao
     */
    TaskSummaryResponse createTask(Integer projectId, CreateTaskRequest request);

    /**
     * Truy xuat ho so chi tiet cua mot Cong viec.
     * * @param taskId ID dinh danh cua Task
     * @return DTO phan hoi chua day du metadata, assignee, tags va subtasks
     */
    TaskResponse getTaskDetails(Integer taskId);

    /**
     * Cap nhat thong tin hanh chinh cua Task (Partial Update).
     * * @param taskId ID Task can cap nhat
     * @param request Cac truong thong tin thay doi (Tieu de, Mo ta, Priority...)
     * @return Ho so Task sau khi cap nhat
     */
    TaskResponse updateTask(Integer taskId, UpdateTaskRequest request);

    /**
     * Xoa vinh vien mot Cong viec khoi he thong.
     * * @param taskId ID Task can xoa
     */
    TaskResponse deleteTask(Integer taskId);

    // ======================================================
    // 2. DIEU HUONG VA KEO THA (LIFECYCLE & DRAG-DROP)
    // ======================================================

    /**
     * Dieu chuyen Task giua cac Sprint hoac dua ve Backlog.
     * Thuc hien cap nhat lai thu tu sap xep (sortOrder) tai vi tri moi.
     * * @param taskId ID cua Task
     * @param sprintId ID Sprint dich (null neu chuyen ve Backlog)
     * @param newSortOrder Vi tri moi trong danh sach
     */
    TaskResponse updateTaskSprint(Integer taskId, Integer sprintId, Integer newSortOrder);
    
    /**
     * Thay doi trang thai (Cot) cua Task tren giao dien Board.
     * * @param taskId ID cua Task
     * @param request DTO chua ID trang thai moi va vi tri sap xep
     */
    TaskResponse moveTaskToStatus(Integer taskId, MoveTaskStatusRequest request);
    
    /**
     * Gan hoac go lien ket giua Task va Epic (Muc tieu lon).
     * * @param request Chua epicId (null neu muon go lien ket)
     */
    TaskResponse updateTaskEpic(Integer taskId, UpdateTaskEpicRequest request);

    /**
     * Dua Task vao kho luu tru (Archived) - Khong hien thi tren Board/Backlog.
     */
    void archiveTask(Integer taskId);

    /**
     * Khoi phuc Task tu kho luu tru tro lai trang thai hoat dong.
     */
    void restoreTask(Integer taskId);

    // ======================================================
    // 3. NHAP DU LIEU HANG LOAT (BULK IMPORT)
    // ======================================================

    /**
     * Tao tep tin mau Excel de nguoi dung nhap lieu Task.
     */
    byte[] generateImportTemplate();
    
    /**
     * Doc tep tin Excel va tra ve danh sach xem truoc kem thong bao loi (neu co).
     */
    List<TaskImportPreviewResponse> previewImportTasks(Integer projectId, MultipartFile file);

    /**
     * Thuc hien luu hang loat cac Task da duoc xac thuc tu danh sach xem truoc.
     */
    ImportTaskResultResponse saveImportedTasks(Integer projectId, List<TaskImportPreviewResponse> validatedRows);

    // ======================================================
    // 4. LOGIC MAPPING (INTERNAL CONVERSION)
    // ======================================================

    /**
     * Chuyen doi thuc the Task sang DTO chi tiet.
     * * @param task Entity Task can convert
     * @return TaskResponse DTO
     */
    TaskResponse mapToTaskResponse(Task task);
}