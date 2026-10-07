package systemanagercv.example.systemanagercv.profile.service;

import org.springframework.web.multipart.MultipartFile;
import systemanagercv.example.systemanagercv.profile.dto.response.AvatarUploadResponse;

public interface AvatarService {

    AvatarUploadResponse uploadAvatar(
            String username,
            MultipartFile file
    );
}