package systemanagercv.example.systemanagercv.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String upload(MultipartFile file);

    void delete(String objectKey);
}