package systemanagercv.example.systemanagercv.role.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;
import systemanagercv.example.systemanagercv.common.security.SecurityAuthorization;
import systemanagercv.example.systemanagercv.role.dto.response.RoleSelectResponse;
import systemanagercv.example.systemanagercv.role.entity.Role;
import systemanagercv.example.systemanagercv.role.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * =========================================================
     * GET ALL ROLES
     * =========================================================
     *
     * GET /api/v1/roles
     *
     * Dùng cho dropdown Role ở màn hình:
     * - Thêm User
     * - Sửa User
     */
    @GetMapping
    @PreAuthorize(SecurityAuthorization.ADMIN)
    public ResponseEntity<ApiResponse<List<RoleSelectResponse>>> getAll(){

        List<RoleSelectResponse> result =
                roleService.getAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(result)
        );
    }

    /**
     * Entity Role -> DTO
     */
    private RoleSelectResponse toResponse(Role role){

        return RoleSelectResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
