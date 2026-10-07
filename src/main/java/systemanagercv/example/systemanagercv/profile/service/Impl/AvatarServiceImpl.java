package systemanagercv.example.systemanagercv.profile.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import systemanagercv.example.systemanagercv.common.exception.BusinessException;
import systemanagercv.example.systemanagercv.profile.dto.response.AvatarUploadResponse;
import systemanagercv.example.systemanagercv.profile.entity.AvatarChangeRequest;
import systemanagercv.example.systemanagercv.profile.enums.AvatarChangeRequestStatus;
import systemanagercv.example.systemanagercv.profile.repository.AvatarChangeRequestRepository;
import systemanagercv.example.systemanagercv.profile.service.AvatarService;
import systemanagercv.example.systemanagercv.storage.service.FileStorageService;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AvatarServiceImpl implements AvatarService {

    private final UserRepository userRepository;

    private final AvatarChangeRequestRepository
            avatarChangeRequestRepository;

    private final FileStorageService fileStorageService;

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024L;

    private static final String JPG =
            "image/jpeg";

    private static final String PNG =
            "image/png";

    private static final String WEBP =
            "image/webp";

    @Override
    public AvatarUploadResponse uploadAvatar(
            String username,
            MultipartFile file
    ) {

        /*
         * =====================================================
         * 1. VALIDATE USERNAME
         * =====================================================
         */

        if (!StringUtils.hasText(username)) {

            throw new BusinessException(
                    "error.profile.avatar.userNotFound"
            );
        }

        /*
         * =====================================================
         * 2. VALIDATE FILE
         * =====================================================
         */

        validateAvatarFile(file);

        /*
         * =====================================================
         * 3. FIND CURRENT USER
         * =====================================================
         */

        User user =
                userRepository.findByUsernameAndDeletedFalse(
                        username
                );

        if (user == null) {
            throw new BusinessException(
                    "error.profile.avatar.userNotFound"
            );
        }

        /*
         * =====================================================
         * 4. CHECK PENDING REQUEST
         * =====================================================
         *
         * Không cho một User gửi liên tục nhiều request
         * avatar trong khi request cũ chưa được Admin xử lý.
         */

        boolean hasPendingRequest =
                avatarChangeRequestRepository
                        .existsByUser_IdAndStatusAndDeletedFalse(
                                user.getId(),
                                AvatarChangeRequestStatus.PENDING
                        );

        if (hasPendingRequest) {

            throw new BusinessException(
                    "error.profile.avatar.pendingExists"
            );
        }

        /*
         * =====================================================
         * 5. CREATE OBJECT KEY
         * =====================================================
         *
         * Ví dụ:
         *
         * profile/avatar/pending/5/2026/10/
         * 550e8400-e29b-41d4-a716-446655440000.jpg
         */

        String objectKey =
                buildPendingAvatarObjectKey(
                        user.getId(),
                        file
                );

        /*
         * =====================================================
         * 6. UPLOAD MINIO
         * =====================================================
         */

        String uploadedObjectKey;

        try {

            uploadedObjectKey =
                    fileStorageService.upload(
                            file,
                            objectKey
                    );

        } catch (Exception e) {

            throw new BusinessException(
                    "error.profile.avatar.uploadFailed"
            );
        }

        /*
         * =====================================================
         * 7. CREATE CHANGE REQUEST
         * =====================================================
         */

        try {

            AvatarChangeRequest request =
                    new AvatarChangeRequest();

            request.setUser(user);

            request.setPendingObjectKey(
                    uploadedObjectKey
            );

            request.setStatus(
                    AvatarChangeRequestStatus.PENDING
            );

            AvatarChangeRequest savedRequest =
                    avatarChangeRequestRepository.save(
                            request
                    );

            /*
             * =================================================
             * 8. RESPONSE
             * =================================================
             */

            return AvatarUploadResponse
                    .builder()
                    .requestId(
                            savedRequest.getId()
                    )
                    .status(
                            savedRequest.getStatus()
                    )
                    .build();

        } catch (Exception e) {

            /*
             * =================================================
             * COMPENSATING ACTION
             * =================================================
             *
             * Nếu upload MinIO thành công nhưng DB thất bại,
             * xóa object vừa upload để tránh file rác.
             */

            try {

                fileStorageService.delete(
                        uploadedObjectKey
                );

            } catch (Exception ignored) {
                // Không che mất lỗi DB ban đầu.
            }

            throw e;
        }
    }

    /*
     * =========================================================
     * VALIDATE AVATAR
     * =========================================================
     */

    private void validateAvatarFile(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {

            throw new BusinessException(
                    "error.profile.avatar.empty"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new BusinessException(
                    "error.profile.avatar.fileTooLarge"
            );
        }

        String contentType =
                file.getContentType();

        if (!isSupportedContentType(
                contentType
        )) {

            throw new BusinessException(
                    "error.profile.avatar.invalidType"
            );
        }

        String extension =
                getExtension(
                        file.getOriginalFilename()
                );

        if (!isSupportedExtension(
                extension
        )) {

            throw new BusinessException(
                    "error.profile.avatar.invalidExtension"
            );
        }
    }

    /*
     * =========================================================
     * SUPPORTED CONTENT TYPE
     * =========================================================
     */

    private boolean isSupportedContentType(
            String contentType
    ) {

        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        return JPG.equalsIgnoreCase(contentType)
                || PNG.equalsIgnoreCase(contentType)
                || WEBP.equalsIgnoreCase(contentType);
    }

    /*
     * =========================================================
     * SUPPORTED EXTENSION
     * =========================================================
     */

    private boolean isSupportedExtension(
            String extension
    ) {

        if (!StringUtils.hasText(extension)) {
            return false;
        }

        String normalized =
                extension.toLowerCase(
                        Locale.ROOT
                );

        return ".jpg".equals(normalized)
                || ".jpeg".equals(normalized)
                || ".png".equals(normalized)
                || ".webp".equals(normalized);
    }

    /*
     * =========================================================
     * BUILD OBJECT KEY
     * =========================================================
     */

    private String buildPendingAvatarObjectKey(
            Long userId,
            MultipartFile file
    ) {

        LocalDate now =
                LocalDate.now();

        String extension =
                normalizeExtension(
                        getExtension(
                                file.getOriginalFilename()
                        )
                );

        return String.format(
                "profile/avatar/pending/%d/%d/%02d/%s%s",
                userId,
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                extension
        );
    }

    /*
     * =========================================================
     * GET EXTENSION
     * =========================================================
     */

    private String getExtension(
            String filename
    ) {

        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int index =
                filename.lastIndexOf('.');

        if (index < 0) {
            return "";
        }

        return filename.substring(index);
    }

    /*
     * =========================================================
     * NORMALIZE EXTENSION
     * =========================================================
     */

    private String normalizeExtension(
            String extension
    ) {

        if (!StringUtils.hasText(extension)) {
            return "";
        }

        String normalized =
                extension.toLowerCase(
                        Locale.ROOT
                );

        if (".jpeg".equals(normalized)) {
            return ".jpg";
        }

        return normalized;
    }
}
