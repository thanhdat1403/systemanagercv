package systemanagercv.example.systemanagercv.cv.update_request.dto.request;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVUpdateRequestCreateRequest {

    /**
     * Danh sách Employee được yêu cầu cập nhật CV.
     */
    @NotEmpty(
            message = "Danh sách Employee không được để trống"
    )
    private List<
            @NotNull(
                    message = "Employee ID không được để trống"
            )
                    Long
            > employeeIds;


    /**
     * Nội dung yêu cầu cập nhật CV.
     */
    @NotNull(
            message = "Nội dung yêu cầu không được để trống"
    )
    private String content;


    /**
     * Deadline hoàn thành yêu cầu.
     */
    @NotNull(
            message = "Deadline không được để trống"
    )
    @FutureOrPresent(
            message = "Deadline phải từ ngày hiện tại trở đi"
    )
    private LocalDate deadline;
}
//{ mở rộng chức năng k chỉ chọn 1 nhân viên yêu cầu mà có thể chọn 1 nhóm nv yêu cầu sửa CV
//        "employeeIds": [1, 2, 5, 8],
//        "content": "Cập nhật CV theo dự án mới, bổ sung kỹ năng và kinh nghiệm trong năm 2026.",
//        "deadline": "2026-10-15"
//        }