package systemanagercv.example.systemanagercv.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    /**
     * Upload file theo cơ chế hiện tại của hệ thống.
     *
     * Object key sẽ được service tự tạo.
     *
     * Ví dụ:
     * cv/2026/10/uuid.jpg
     */
    String upload(MultipartFile file);

    /**
     * Upload file với object key được truyền từ business service.
     *
     * Dùng cho các module cần quy định cấu trúc
     * object key riêng, ví dụ Profile / Avatar.
     */
    String upload(
            MultipartFile file,
            String objectKey
    );

    /**
     * Download object từ MinIO.
     */
    InputStream download(
            String objectKey
    );

    /**
     * Kiểm tra object có tồn tại hay không.
     */
    boolean exists(
            String objectKey
    );

    /**
     * Xóa object khỏi MinIO.
     */
    void delete(
            String objectKey
    );
}