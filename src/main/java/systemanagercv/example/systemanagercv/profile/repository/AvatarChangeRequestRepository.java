package systemanagercv.example.systemanagercv.profile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import systemanagercv.example.systemanagercv.profile.entity.AvatarChangeRequest;
import systemanagercv.example.systemanagercv.profile.enums.AvatarChangeRequestStatus;


import java.util.Optional;

@Repository
public interface AvatarChangeRequestRepository
        extends JpaRepository<AvatarChangeRequest, Long> {

    // =====================================================
    // KIỂM TRA USER CÓ REQUEST PENDING HAY KHÔNG
    // =====================================================
    // Method 1: này có nhiệm vụ VD: Nguyễn Văn A -> đã có request PENDING chưa?
    boolean existsByUser_IdAndStatusAndDeletedFalse(
            Long userId,
            AvatarChangeRequestStatus status
    );


    // =====================================================
    // LẤY REQUEST PENDING CỦA USER
    // =====================================================
    //Method 2: phục vụ Profile: Tôi đang có yêu cầu đổi avatar nào?
    Optional<AvatarChangeRequest>
    findByUser_IdAndStatusAndDeletedFalse(
            Long userId,
            AvatarChangeRequestStatus status
    );


    // =====================================================
    // ADMIN: LẤY DANH SÁCH REQUEST THEO STATUS
    // =====================================================
    //Method 3: sẽ phục vụ ADMIN: ADMIN -> Quản lý yêu cầu Avatar -> PENDING
    Page<AvatarChangeRequest>
    findAllByStatusAndDeletedFalse(
            AvatarChangeRequestStatus status,
            Pageable pageable
    );

    // =====================================================
    // LẤY LỊCH SỬ REQUEST CỦA USER
    // =====================================================
    // Method 4: phục vụ lịch sử Profile
    Page<AvatarChangeRequest>
    findAllByUser_IdAndDeletedFalse(
            Long userId,
            Pageable pageable
    );
}
