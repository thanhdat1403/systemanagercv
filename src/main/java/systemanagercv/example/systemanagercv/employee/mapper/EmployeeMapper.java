package systemanagercv.example.systemanagercv.employee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeCreateRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeUpdateRequest;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeDetailResponse;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeResponse;
import systemanagercv.example.systemanagercv.employee.entity.Employee;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    /**
     * Entity → Response
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentCode", source = "department.code")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(
            target = "positionDescription",
            expression = "java(employee.getPosition() != null ? employee.getPosition().getDescription() : null)"
    )
    @Mapping(
            target = "statusDescription",
            expression = "java(employee.getStatus() != null ? employee.getStatus().getDescription() : null)"
    )
    EmployeeResponse toResponse(Employee employee);

    /**
     * Entity → Detail Response
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentCode", source = "department.code")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(
            target = "positionDescription",
            expression = "java(employee.getPosition() != null ? employee.getPosition().getDescription() : null)"
    )
    @Mapping(
            target = "statusDescription",
            expression = "java(employee.getStatus() != null ? employee.getStatus().getDescription() : null)"
    )
    EmployeeDetailResponse toDetailResponse(Employee employee);

    /**
     * CreateRequest → Entity
     *
     * Các quan hệ User và Department không map ở đây.
     * EmployeeService sẽ lấy Entity tương ứng và set vào.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Employee toEntity(EmployeeCreateRequest request);

    /**
     * UpdateRequest → Entity hiện tại
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(
            EmployeeUpdateRequest request,
            @MappingTarget Employee employee
    );
}