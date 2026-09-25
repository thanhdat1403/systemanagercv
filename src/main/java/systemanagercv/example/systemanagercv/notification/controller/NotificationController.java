package systemanagercv.example.systemanagercv.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;
import systemanagercv.example.systemanagercv.common.security.SecurityAuthorization;
import systemanagercv.example.systemanagercv.notification.dto.request.NotificationSearchRequest;
import systemanagercv.example.systemanagercv.notification.dto.response.NotificationResponse;
import systemanagercv.example.systemanagercv.notification.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Lấy danh sách notification của User hiện tại.
     */
    @GetMapping
    @PreAuthorize(SecurityAuthorization.NOTIFICATION_READER)
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @Valid @ModelAttribute NotificationSearchRequest request
    ){

        Page<NotificationResponse> result =
                notificationService.getMyNotifications(request);

        ApiResponse<Page<NotificationResponse>> response =
                new ApiResponse<>(
                        "success",
                        "Success",
                        result
                );

        return ResponseEntity.ok(response);

    }

    /**
     * Đếm số notification chưa đọc của User hiện tại.
     */
    @GetMapping("/unread-count")
    @PreAuthorize(SecurityAuthorization.NOTIFICATION_READER)
    public ResponseEntity<ApiResponse<Long>> countMyUnread() {

        long result =
                notificationService.countMyUnread();

        ApiResponse<Long> response =
                new ApiResponse<>(
                        "success",
                        "Success",
                        result
                );
        return ResponseEntity.ok(response);
    }

    /**
     * Đánh dấu một notification là đã đọc.
     */
    @PostMapping("/{id}/read")
    @PreAuthorize(SecurityAuthorization.NOTIFICATION_MARK_READER)
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id
    ) {

        notificationService.markAsRead(id);

        ApiResponse<Void> response =
                ApiResponse.success(null);

        return ResponseEntity.ok(response);
    }
}
