package systemanagercv.example.systemanagercv.storage.service.Impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import systemanagercv.example.systemanagercv.storage.service.FileStorageService;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl
        implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File must not be empty"
            );
        }

        String extension = getExtension(
                file.getOriginalFilename()
        );

        String objectKey = String.format(
                "cv/%d/%02d/%s%s",
                LocalDate.now().getYear(),
                LocalDate.now().getMonthValue(),
                UUID.randomUUID(),
                extension
        );

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            return objectKey;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to upload file to MinIO",
                    e
            );
        }
    }

    @Override
    public void delete(String objectKey) {

        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        try {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
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

    private String getExtension(String filename) {

        if (filename == null || filename.isBlank()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex < 0) {
            return "";
        }

        return filename
                .substring(lastDotIndex)
                .toLowerCase();
    }
}