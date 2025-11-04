package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.service.SecurityService; // Import service bảo mật
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; 
import java.util.stream.Collectors; 
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final KhongGianRepository khongGianRepository;
    private final KhongGianThanhVienRepository khongGianThanhVienRepository;
    private final CongTyRepository congTyRepository;
    private final RoleRepository roleRepository;
    private final SecurityService securityService; 

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(Integer congTyId, CreateWorkspaceRequest request) {
        
        // 1. Lấy thông tin người dùng và công ty
        NguoiDung creator = securityService.getCurrentAuthenticatedUser();
        CongTy congTy = congTyRepository.findById(congTyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        // 2. Kiểm tra nghiệp vụ (tên trùng)
        if (khongGianRepository.existsByCongTy_IdCongTyAndTenKhongGian(congTyId, request.getTenKhongGian())) {
            throw new BadRequestException("Tên không gian đã tồn tại trong công ty này");
        }

        // 3. Tìm Role "WORKSPACE_ADMIN"
        Role workspaceAdminRole = roleRepository.findFirstByMaRole(RoleCode.WORKSPACE_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy Role: " + RoleCode.WORKSPACE_ADMIN.name() + ". Vui lòng cấu hình CSDL."
                ));

        // 4. Tạo không gian mới
        KhongGian newWorkspace = KhongGian.builder()
                .congTy(congTy)
                .tenKhongGian(request.getTenKhongGian())
                .moTa(request.getMoTa())
                .anhBia(request.getAnhBia())
                .mauSac(request.getMauSac() != null ? request.getMauSac() : "#3498db")
                .nguoiTao(creator)
                .trangThai(WorkspaceStatus.HOAT_DONG)
                .build();
        
        KhongGian savedWorkspace = khongGianRepository.save(newWorkspace);

        // 5. Tự động gán người tạo làm Admin của không gian
        KhongGianThanhVien membership = KhongGianThanhVien.builder()
                .khongGian(savedWorkspace)
                .nguoiDung(creator)
                .role(workspaceAdminRole)
                .trangThai(MemberStatus.HOAT_DONG)
                .build();
        
        khongGianThanhVienRepository.save(membership);

        // 6. Map Entity sang DTO và trả về
        return mapToWorkspaceResponse(savedWorkspace);
    }


    // LOGIC HIỂN THỊ DANH SÁCH KHÔNG GIAN TRONG CÔNG TY
    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByCompany(Integer congTyId) {
        // 1. Lấy danh sách Entity từ CSDL
        // (Bảo mật sẽ được xử lý ở tầng Controller bằng @PreAuthorize)
        List<KhongGian> workspaces = khongGianRepository.findByCongTy_IdCongTy(congTyId);

        // 2. Chuyển đổi (map) danh sách Entity sang danh sách DTO
        return workspaces.stream()
                .map(this::mapToWorkspaceResponse) // Tái sử dụng helper đã tạo
                .collect(Collectors.toList());
    }

    // LOGIC XEM CHI TIET PHONG BAN
    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceDetails(Integer workspaceId) {
        // Bảo mật đã được xử lý bởi @PreAuthorize ở tầng Controller.
        // Tầng service chỉ cần thực hiện logic tìm kiếm.
        
        KhongGian workspace = khongGianRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc với ID: " + workspaceId));
                
        // Tái sử dụng helper đã tạo
        return mapToWorkspaceResponse(workspace);
    }

    /**
     * Hàm helper để chuyển đổi Entity KhongGian sang WorkspaceResponse DTO.
     * @param kg Entity KhongGian
     * @return WorkspaceResponse DTO
     */
    private WorkspaceResponse mapToWorkspaceResponse(KhongGian kg) {
        return WorkspaceResponse.builder()
                .idKhongGian(kg.getIdKhongGian())
                .congTyId(kg.getCongTy().getIdCongTy()) // Lấy ID an toàn
                .tenKhongGian(kg.getTenKhongGian())
                .moTa(kg.getMoTa())
                .anhBia(kg.getAnhBia())
                .mauSac(kg.getMauSac())
                .nguoiTaoId(kg.getNguoiTao().getIdNguoiDung()) // Lấy ID an toàn
                .trangThai(kg.getTrangThai().name()) // Trả về tên Enum (String)
                .ngayTao(kg.getNgayTao())
                .build();
    }
}