package systemanagercv.example.systemanagercv.employee.service;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeCreateRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeSearchRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeUpdateRequest;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeDetailResponse;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeResponse;

public interface EmployeeService {

    /**
     * Tìm kiếm và phân trang danh sách nhân viên.
     */
    Page<EmployeeResponse> search(EmployeeSearchRequest request);

    /**
     * Lấy chi tiết nhân viên theo ID.
     */
    EmployeeDetailResponse findById(Long id);

    /**
     * Thêm mới nhân viên.
     */
    EmployeeResponse create(EmployeeCreateRequest request);

    /**
     * Cập nhật thông tin nhân viên.
     */
    EmployeeResponse update(Long id, EmployeeUpdateRequest request);

    /**
     * Xóa mềm nhân viên.
     */
    void delete(Long id);
}