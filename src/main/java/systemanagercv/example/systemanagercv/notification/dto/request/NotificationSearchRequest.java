package systemanagercv.example.systemanagercv.notification.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationSearchRequest {

    @Min(value = 0, message = "Page phải lớn hơn hoặc bằng 0")
    private int page = 0;

    @Min(value = 1, message = "Size phải lớn hơn hoặc bằng 1")
    @Max(value = 100, message = "Size không được lớn hơn 100")
    private int size = 10;

    private Boolean unreadOnly = false;
}
/*DTO này phục vụ API sau này, ví dụ:
* GET /api/v1/notifications?page=0&size=10
* hoặc: GET /api/v1/notifications?page=0&size=10&unreadOnly=true*/