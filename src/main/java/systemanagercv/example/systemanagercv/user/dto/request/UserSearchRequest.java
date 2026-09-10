package systemanagercv.example.systemanagercv.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchRequest {

    private String keyword;

    private Long roleId;

    private Boolean enabled;

    @Min(
            value = 0,
            message = "error.page.invalid"
    )
    private int page = 0;

    @Min(
            value = 1,
            message = "error.page.size.invalid"
    )
    @Max(
            value = 100,
            message = "error.page.size.max"
    )
    private int size = 10;

    /**
     * Tên field Java dùng để sort.
     *
     * BaseEntity sử dụng createdDate,
     * không phải createdAt.
     */
    private String sortBy = "createdDate";

    private String sortDirection = "DESC";

}