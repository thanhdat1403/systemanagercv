package systemanagercv.example.systemanagercv.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import systemanagercv.example.systemanagercv.notification.dto.response.NotificationResponse;
import systemanagercv.example.systemanagercv.notification.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(
            target = "typeDescription",
            source = "type.description"
    )
    NotificationResponse toResponse(Notification notification);
}
/*Thông thườn MapStruct có thể tự map:
* Notification.id
        ↓
NotificationResponse.id

Notification.title
        ↓
NotificationResponse.title

Notification.message
        ↓
NotificationResponse.message

Notification.isRead
        ↓
NotificationResponse.isRead

Notification.readAt
        ↓
NotificationResponse.readAt

Notification.createdDate
        ↓
NotificationResponse.createdDate*/