package systemanagercv.example.systemanagercv.user.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO dùng cho danh sách tài khoản User được lựa chọn
 * khi tạo hoặc chỉnh sửa Employee.
 *
 * Chỉ chứa những thông tin cần thiết cho dropdown:
 * - id       : dùng làm value của option
 * - username : tên tài khoản
 * - roleName : role của tài khoản
 *
 * Ví dụ hiển thị trên giao diện:
 * tech01 - TECH_LEAD
 * hr01   - HR
 * emp01  - EMPLOYEE
 */
@Getter
@Builder
public class UserSelectResponse {

    private Long id;
    private String username;
    /**
     * Tên Role của tài khoản.
     *
     * Ví dụ:
     * HR
     * TECH_LEAD
     * EMPLOYEE
     */
    private String roleName;
}
