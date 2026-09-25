package systemanagercv.example.systemanagercv.notification.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import systemanagercv.example.systemanagercv.common.exception.ResourceNotFoundException;
import systemanagercv.example.systemanagercv.notification.dto.request.NotificationSearchRequest;
import systemanagercv.example.systemanagercv.notification.dto.response.NotificationResponse;
import systemanagercv.example.systemanagercv.notification.entity.Notification;
import systemanagercv.example.systemanagercv.notification.enums.NotificationType;
import systemanagercv.example.systemanagercv.notification.mapper.NotificationMapper;
import systemanagercv.example.systemanagercv.notification.repository.NotificationRepository;
import systemanagercv.example.systemanagercv.notification.service.NotificationService;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    /**
     * Tạo notification cho một User.
     */
    @Override
    public NotificationResponse createNotification(
            Long recipientUserId,
            NotificationType type,
            String title,
            String message,
            String referenceType,
            Long referenceId
    ) {

        User recipient =
                userService.findActiveUserEntityById(recipientUserId);

        Notification notification = new Notification();

        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notification.setReadAt(null);

        Notification saved =
                notificationRepository.save(notification);

        return notificationMapper.toResponse(saved);
    }

    /**
     * Lấy notification của User hiện tại.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(
            NotificationSearchRequest request
    ) {

        User currentUser = getCurrentUser();

        Pageable pageable =
                PageRequest.of(
                        request.getPage(),
                        request.getSize()
                );

        Page<Notification> notifications;

        if (Boolean.TRUE.equals(request.getUnreadOnly())){

            notifications =
                    notificationRepository
                            .findAllByRecipientIdAndIsReadFalseAndDeletedFalseOrderByCreatedDateDesc(
                                    currentUser.getId(),
                                    pageable
                            );
        } else {

            notifications =
                    notificationRepository
                            .findAllByRecipientIdAndDeletedFalseOrderByCreatedDateDesc(
                                    currentUser.getId(),
                                    pageable
                            );
        }
        return notifications.map(
                notificationMapper::toResponse
        );
    }

    /**
     * Đếm notification chưa đọc của User hiện tại.
     */
    @Override
    @Transactional(readOnly = true)
    public long countMyUnread() {

        User currentUser = getCurrentUser();

        return notificationRepository
                .countByRecipientIdAndIsReadFalseAndDeletedFalse(
                        currentUser.getId()
                );
    }

    /**
     * Đánh dấu notification đã đọc.
     */
    @Override
    public void markAsRead(Long notificationId) {

        User currentUser = getCurrentUser();

        Notification notification =
                notificationRepository
                        .findByIdAndDeletedFalse(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification không tồn tại"
                                )
                        );

        // Nếu người nhận (Recipient) notification là null
        if (notification.getRecipient() == null
                || !notification
                .getRecipient()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Bạn không có quyền đọc notification này"
            );
        }

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    /**
     * Lấy User hiện tại đang đăng nhập.
     */
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()){

            throw new AccessDeniedException(
                    "Người dùng chưa đăng nhập"
            );

        }
        return  userService.findByUsername(authentication.getName());
    }
}
