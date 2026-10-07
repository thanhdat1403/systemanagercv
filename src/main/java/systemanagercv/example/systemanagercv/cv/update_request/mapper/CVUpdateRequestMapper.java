package systemanagercv.example.systemanagercv.cv.update_request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import systemanagercv.example.systemanagercv.cv.update_request.dto.response.CVUpdateRequestResponse;
import systemanagercv.example.systemanagercv.cv.update_request.entity.CVUpdateRequestEntity;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CVUpdateRequestMapper {

    /**
     * =====================================================
     * ENTITY -> RESPONSE
     * =====================================================
     *
     * Mapping:
     *
     * CVUpdateRequestEntity
     *      |
     *      +--> employeeCV
     *      |       |
     *      |       +--> id
     *      |       |
     *      |       +--> employee
     *      |               |
     *      |               +--> id
     *      |               +--> employeeCode
     *      |               +--> fullName
     *      |               +--> department
     *      |                       |
     *      |                       +--> code
     *      |                       +--> name
     *      |
     *      +--> requestedBy
     *      |       |
     *      |       +--> id
     *      |       +--> username
     *      |
     *      +--> status
     *      |
     *      +--> createdDate
     *      +--> updatedDate
     */
    @Mapping(
            target = "employeeCvId",
            source = "employeeCV.id"
    )
    @Mapping(
            target = "employeeId",
            source = "employeeCV.employee.id"
    )
    @Mapping(
            target = "employeeCode",
            source = "employeeCV.employee.employeeCode"
    )
    @Mapping(
            target = "employeeName",
            source = "employeeCV.employee.fullName"
    )
    @Mapping(
            target = "departmentCode",
            source = "employeeCV.employee.department.code"
    )
    @Mapping(
            target = "departmentName",
            source = "employeeCV.employee.department.name"
    )
    @Mapping(
            target = "requestedByUserId",
            source = "requestedBy.id"
    )
    @Mapping(
            target = "requestedByUsername",
            source = "requestedBy.username"
    )
    CVUpdateRequestResponse toResponse(
            CVUpdateRequestEntity entity
    );
}