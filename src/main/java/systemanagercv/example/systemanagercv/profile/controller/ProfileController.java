package systemanagercv.example.systemanagercv.profile.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;
import systemanagercv.example.systemanagercv.profile.dto.response.AvatarUploadResponse;
import systemanagercv.example.systemanagercv.profile.service.AvatarService;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AvatarService avatarService;

    /**
     * =========================================================
     * UPLOAD AVATAR
     * =========================================================
     *
     * POST /api/v1/profile/avatar
     *
     * Content-Type:
     * multipart/form-data
     *
     * Form-data:
     * avatar = file
     *
     * Username được lấy từ Authentication hiện tại,
     * không nhận username từ request.
     */
    @PostMapping(
            value = "/avatar",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AvatarUploadResponse>> uploadAvatar(
            @RequestPart("avatar") MultipartFile file,
            Authentication authentication
    ) {

        String username =
                authentication.getName();

        AvatarUploadResponse response =
                avatarService.uploadAvatar(
                        username,
                        file
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}