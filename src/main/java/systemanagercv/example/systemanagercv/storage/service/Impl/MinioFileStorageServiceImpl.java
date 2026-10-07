package systemanagercv.example.systemanagercv.storage.service.Impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import systemanagercv.example.systemanagercv.storage.service.FileStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl
        implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * =====================================================
     * UPLOAD FILE - CƠ CHẾ CŨ
     * =====================================================
     *
     * Dùng cho CV.
     *
     * Object key:
     *
     * cv/yyyy/MM/uuid.ext
     */
    @Override
    public String upload(
            MultipartFile file
    ) {

        validateFile(file);

        String extension =
                getExtension(
                        file.getOriginalFilename()
                );

        String objectKey =
                String.format(
                        "cv/%d/%02d/%s%s",
                        LocalDate.now().getYear(),
                        LocalDate.now().getMonthValue(),
                        UUID.randomUUID(),
                        extension
                );

        return upload(
                file,
                objectKey
        );
    }

    /**
     * =====================================================
     * UPLOAD FILE - OBJECT KEY CHỦ ĐỘNG
     * =====================================================
     *
     * Dùng cho Avatar / Profile.
     *
     * Ví dụ:
     *
     * profile/avatar/pending/5/2026/10/uuid.jpg
     */
    @Override
    public String upload(
            MultipartFile file,
            String objectKey
    ) {

        validateFile(file);

        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException(
                    "Object key must not be blank"
            );
        }

        try {

            String contentType =
                    StringUtils.hasText(
                            file.getContentType()
                    )
                            ? file.getContentType()
                            : "application/octet-stream";

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(contentType)
                            .build()
            );

            return objectKey;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to read file for MinIO upload",
                    e
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to upload file to MinIO",
                    e
            );
        }
    }

    /**
     * =====================================================
     * DOWNLOAD
     * =====================================================
     */
    @Override
    public InputStream download(
            String objectKey
    ) {

        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException(
                    "Object key must not be blank"
            );
        }

        try {

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to download file from MinIO",
                    e
            );
        }
    }

    /**
     * =====================================================
     * EXISTS
     * =====================================================
     */
    @Override
    public boolean exists(
            String objectKey
    ) {

        if (!StringUtils.hasText(objectKey)) {
            return false;
        }

        try {

            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    /**
     * =====================================================
     * DELETE
     * =====================================================
     */
    @Override
    public void delete(
            String objectKey
    ) {

        if (!StringUtils.hasText(objectKey)) {
            return;
        }

        try {

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to delete file from MinIO",
                    e
            );
        }
    }

    /**
     * =====================================================
     * VALIDATE FILE
     * =====================================================
     */
    private void validateFile(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "File must not be empty"
            );
        }
    }

    /**
     * =====================================================
     * GET EXTENSION
     * =====================================================
     */
    private String getExtension(
            String filename
    ) {

        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex =
                filename.lastIndexOf('.');

        if (lastDotIndex < 0) {
            return "";
        }

        return filename
                .substring(lastDotIndex)
                .toLowerCase();
    }
}