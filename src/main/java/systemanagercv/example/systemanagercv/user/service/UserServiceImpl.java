package systemanagercv.example.systemanagercv.user.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import systemanagercv.example.systemanagercv.common.exception.BusinessException;
import systemanagercv.example.systemanagercv.role.entity.Role;
import systemanagercv.example.systemanagercv.user.dto.request.UserCreateRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserSearchRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserUpdateRequest;
import systemanagercv.example.systemanagercv.user.dto.response.UserDetailResponse;
import systemanagercv.example.systemanagercv.user.dto.response.UserResponse;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.role.entity.UserRole;
import systemanagercv.example.systemanagercv.role.entity.UserRoleId;
import systemanagercv.example.systemanagercv.role.repository.RoleRepository;
import systemanagercv.example.systemanagercv.user.mapper.UserMapper;
import systemanagercv.example.systemanagercv.user.repository.UserRepository;
import systemanagercv.example.systemanagercv.user.specification.UserSpecification;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor //tự động viết hộ một hàm khởi tạo (Constructor) chứa tất cả các biến được khai báo với từ khóa final
/**
 * @Transactional: Tấm lưới bảo hiểm cho Database (Tính toàn vẹn dữ liệu).
 * - Tác dụng: Bắt các lệnh Thêm/Sửa/Xóa bên trong hàm phải CÙNG THÀNH CÔNG hoặc CÙNG THẤT BẠI.
 * - Nếu hàm chạy mượt mà: Chính thức lưu vĩnh viễn dữ liệu vào Database (Commit).
 * - Nếu hàm gặp lỗi giữa chừng (ném ra Exception): Tự động xóa sạch các lệnh vừa chạy dở dang,
 *   đưa Database quay về trạng thái an toàn ban đầu (Rollback), tránh mất mát hay sai lệch dữ liệu.
 */
@Transactional
public class UserServiceImpl implements UserService {

    // Phải có chữ final, Spring Boot sẽ tự hiểu và nạp các linh kiện này vào kho
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // =====================================================
    // USER MANAGEMENT
    // =====================================================

    /**
     * Lấy tất cả User.
     *
     * API không trả Entity trực tiếp.
     * Entity User được Mapper chuyển sang UserResponse.
     */

    @Override
    @Transactional(readOnly = true) //Đánh dấu hàm chỉ đọc dữ liệu (SELECT) -> giúp tối ưu hiệu năng, tăng tốc truy vấn
    public List<UserResponse> getAll() {
        return userRepository.findAll() //1.Đi vào Database lấy nên toàn bộ danh sách User thô (Entity)
                .stream()   //2.Bật "băng chuyền" Stream để đẩy từng đối tượng User ra xử lý lần lượt
                .map(userMapper::toResponse)  //3.Ứng với mỗi User trên băng chuyền, nhờ Mapper chuyển đổi thành kiểu UserResponse (chỉ chứa thông tin cần thiết cho Frontend)
                .toList();  // 4. Gom tất cả các UserResponse đã chuyển đổi trên băng chuyền lại thành một danh sách (List) hoàn chỉnh để trả về
    }


    /**
     * Tìm User theo ID.
     *
     * Dùng UserDetailResponse vì đây là dữ liệu chi tiết
     * được trả về cho API.
     */
    //Đoạn code này có tác dụng: Tìm kiếm một người dùng cụ thể trong cơ sở dữ liệu dựa vào mã ID truyền vào. Nếu tìm thấy thì trả về thông tin chi tiết (UserDetailResponse), còn nếu không tìm thấy thì lập tức chặn lại và ném ra lỗi.
    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse findById(Long id) {

        //1. Vào Database tìm User theo ID
        //-NẾU TÌM THẤY: Gán dữ liệu vào biến 'user'
        //-NẾU K THẤY: Chặn đứng hàm lại và ném ra lỗi BusinessException với mã 'error.user.notFound'
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("error.user.notFound"));

        //2.Sau khi đã tìm thấy User hợp lệ, nhờ Mapper chuyển đổi đối tượng User thô (Entity)
        //thành kiểu dữ liệu cấu trúc chi tiết (UserDetailResponse) rồi trả về controller
        return userMapper.toDetailResponse(user);
    }


    // =====================================================
    // SPRING SECURITY
    // =====================================================

    /**
     * Dùng cho Spring Security khi đăng nhập.
     *
     * Đây là hàm nội bộ nên vẫn trả Entity User.
     * Không phải API response.
     */

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // =====================================================
    // CREATE USER
    // =====================================================

    /**
     * Tạo User + UserRole.
     *
     * Input:
     * UserCreateRequest (Thông tin người dùng nhập vào từ giao diện)
     *
     * Output:
     * UserResponse (Thông tin tài khoản đã tạo thành công để phản hồi)
     */
    @Override
    @Transactional // <--- BẮT BUỘC PHẢI THÊM NHÃN NÀY để bảo vệ dữ liệu, nếu lỗi ở bước sau thì tự động xóa bước trước (Rollback)
    public UserResponse createUser(UserCreateRequest request) {

        // =================================================
        // 1. Kiểm tra Quyền (Role) xem có hợp lệ hay không
        // =================================================
        // Tìm quyền trong Database theo ID người dùng gửi lên. Nếu không tồn tại quyền này -> dừng lại và ném lỗi "Không tìm thấy quyền"
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("error.role.notFound"));


        // =================================================
        // 2. Kiểm tra Tên đăng nhập (Username) xem đã bị trùng chưa
        // =================================================
        // Nếu trong Database đã có ai đăng ký tên này rồi -> dừng lại và ném lỗi "Tên đăng nhập đã tồn tại"
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("error.user.username.exists");
        }


        // =================================================
        // 3. Kiểm tra Email xem đã bị trùng chưa
        // =================================================
        // Nếu trong Database đã có ai đăng ký email này rồi -> dừng lại và ném lỗi "Email đã tồn tại"
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("error.user.email.exists");
        }


        // =================================================
        // 4. Chuyển đổi dữ liệu (Map Request DTO → Entity)
        // =================================================
        // Nhờ Mapper biến đổi gói dữ liệu người dùng gửi lên (DTO) thành đối tượng dữ liệu thô (Entity) để chuẩn bị lưu vào Database
        User user = userMapper.toEntity(request);


        // =================================================
        // 5. Mã hóa Mật khẩu (Password) để bảo mật
        // =================================================
        // Tuyệt đối không lưu mật khẩu dạng chữ rõ ràng (ví dụ: "123456") vào Database. Phải dùng passwordEncoder để băm/mã hóa thành chuỗi bảo mật phức tạp
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));


        // =================================================
        // 6. Đặt trạng thái mặc định cho User mới là Hoạt động
        // =================================================
        user.setEnabled(true);


        // =================================================
        // 7. Lưu thông tin User cơ bản vào Database trước
        // =================================================
        // Lưu tạm thời để Database sinh ra mã ID tự động cho User này (vì ID này cần dùng ở bước tạo quyền phía dưới)
        User savedUser = userRepository.save(user);


        // =================================================
        // 8. Tạo đối tượng liên kết Quyền (UserRole)
        // =================================================
        // Khởi tạo thực thể UserRole để gắn User vừa tạo ở Bước 7 với Quyền (Role) tìm thấy ở Bước 1
        UserRole userRole = new UserRole();

        userRole.setUser(savedUser);
        userRole.setRole(role);

        //Cài đặt khóa chính hỗn hợp (Composite Key) cho bảng trung gian bao gồm (userId, roleId)
        userRole.setId(
                new UserRoleId(
                        savedUser.getId(),
                        role.getId()
                )
        );


        // =================================================
        // 9. Thêm UserRole vào danh sách quyền nội bộ của User
        // =================================================
        // Nếu danh sách quyền của User đang trống (null), khởi tạo mới một danh sách (HashSet) để tránh lỗi NullPointerException
        if (savedUser.getUserRoles() == null) {
            savedUser.setUserRoles(new HashSet<>()); // sử dụng HashSet<>() là một cấu trúc dữ liệu thuộc Java Collections Framework, đại diện cho 1 tập hợp các phần tử k trùng lặp và k sắp xếp theo thứ tự nhập vào
        }


        // Đút đối tượng liên kết quyền vừa tạo ở Bước 8 vào danh sách của User
        savedUser.getUserRoles().add(userRole);


        // =================================================
        // 10. Lưu lại User một lần nữa kèm theo danh sách Quyền
        // =================================================
        // Hibernate sẽ tự động lưu mối quan hệ này vào bảng trung gian user_role trong Database
        User result = userRepository.save(savedUser);


        // =================================================
        // 11. Chuyển đổi kết quả (Entity → Response DTO)
        // =================================================
        // Biến đổi đối tượng Entity thô sau khi lưu thành gói dữ liệu sạch đẹp (UserResponse) để trả về kết quả cho giao diện hiển thị
        return userMapper.toResponse(result);
    }


    // =====================================================
    // UPDATE USER (TT User đã tồn tại trong hệ thống)
    // =====================================================

    /**
     * Cập nhật thông tin User + Quyền (Role).
     * Không nhận trực tiếp User Entity từ Controller để đảm bảo an toàn bảo mật.
     * Controller chỉ truyền vào: ID của user cần sửa và Gói dữ liệu chứa thông tin mới (UserUpdateRequest).
     */

    @Override
    @Transactional // <--- BẮT BUỘC PHẢI THÊM NHÃN NÀY để kích hoạt tính năng bảo hiểm dữ liệu (Rollback nếu xảy ra lỗi giữa chừng)
    public UserResponse updateUser(
            Long id,
            UserUpdateRequest request
    ) {

        // =================================================
        // 1. Tìm thông tin User hiện tại đang lưu trong kho
        // =================================================
        // Lấy thông tin cũ lên để chuẩn bị sửa đổi. Nếu ID không tồn tại -> ngừng xử lý và báo lỗi "Không tìm thấy người dùng"
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("error.user.notFound"));


        // =================================================
        // 2. Tìm thông tin Quyền (Role) mới mà người dùng chọn
        // =================================================
        // Tìm xem quyền mới có hợp lệ không. Nếu ID quyền không tồn tại -> ngừng xử lý và báo lỗi "Không tìm thấy quyền"
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("error.role.notFound"));


        // =================================================
        // 3. Kiểm tra tính hợp lệ của Tên đăng nhập (Username) mới
        // =================================================
        // Phân tích logic điều kiện IF:
        // !existingUser.getUsername().equals(request.getUsername()): Nếu người dùng thực sự muốn thay ĐỔI sang một username mới hoàn toàn (khác cái cũ đang dùng)
        // && userRepository.existsByUsername(request.getUsername()): VÀ cái username mới đó lại ĐÃ BỊ người khác đăng ký mất rồi
        // -> Lập tức chặn lại và ném lỗi "Tên đăng nhập đã tồn tại"
        if (!existingUser.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("error.user.username.exists");
        }


        // =================================================
        // 4. Kiểm tra tính hợp lệ của Email mới
        // =================================================
        // Tương tự như kiểm tra Username: Nếu người dùng thay ĐỔI email mới mà email đó lại trùng với email của một người khác trong hệ thống
        // -> Lập tức chặn lại và ném lỗi "Email đã tồn tại"
        if (!existingUser.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("error.user.email.exists");
        }


        // =================================================
        // 5. Cập nhật các thông tin cơ bản cho User
        // =================================================
        // Đè dữ liệu mới từ request lên đối tượng cũ đang nằm trong bộ nhớ tạm
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setEnabled(request.getEnabled());


        // =================================================
        // 6. Xử lý Mật khẩu (Password) mới nếu có nhập
        // =================================================
        // Kiểm tra xem người dùng có truyền vào mật khẩu mới hay không (không rỗng và không phải khoảng trắng)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {

            // Nếu có nhập mật khẩu mới, bắt buộc phải mã hóa/băm mật khẩu bằng passwordEncoder trước khi lưu
            // (Nếu họ để trống ô mật khẩu trên giao diện nghĩa là không muốn đổi mật khẩu -> Giữ nguyên mật khẩu cũ)
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }


        // =================================================
        // 7. Xóa sạch Quyền (Role) cũ của User
        // =================================================
        // Để thay đổi quyền mới, cách an toàn nhất là xóa hết các mối quan hệ liên kết cũ đang có trong danh sách của User này
        existingUser.getUserRoles().clear();


        // =================================================
        // 8. Khởi tạo đối tượng liên kết Quyền (UserRole) mới
        // =================================================
        // Gắn User hiện tại với thực thể Role mới vừa tìm thấy ở Bước 2
        UserRole userRole = new UserRole();

        userRole.setUser(existingUser);
        userRole.setRole(role);

        //Cài đặt lại khóa chính hỗn hợp (userId, roleId) cho bản ghi mới này
        userRole.setId(
                new UserRoleId(
                        existingUser.getId(),
                        role.getId()
                )
        );


        // =================================================
        // 9. Thêm liên kết Quyền mới vào danh sách của User
        // =================================================
        // Đút thực thể quyền mới tạo ở Bước 8 vào danh sách trống của User (danh sách vừa được clear ở Bước 7)
        existingUser.getUserRoles().add(userRole);


        // =================================================
        // 10. Lưu toàn bộ thay đổi của User xuống Database
        // =================================================
        // Hibernate sẽ tự động so sánh dữ liệu cũ và mới, tiến hành xóa bản ghi quyền cũ trong bảng 'user_role' và thêm bản ghi quyền mới vào, đồng thời cập nhật bảng 'user'
        User result = userRepository.save(existingUser);


        // =================================================
        // 11. Chuyển đổi kết quả (Entity → Response DTO)
        // =================================================
        // Biến đổi đối tượng sau khi lưu thành cấu trúc sạch đẹp (UserResponse) để gửi về phản hồi cho phía giao diện (Frontend)
        return userMapper.toResponse(result);
    }


    // =====================================================
    // DELETE USER
    // =====================================================

    @Override
    public void delete(Long id) {

        User user = userRepository.findById(id)
                        .orElseThrow(() -> new BusinessException("error.user.notFound"));
        userRepository.delete(user);
    }


    // =====================================================
    // EMPLOYEE - USER
    // =====================================================

    /**
     * Lấy những User có ROLE_EMPLOYEE
     * và chưa được liên kết với Employee.
     *
     * Hàm này phục vụ nghiệp vụ Employee.
     * Vì Employee Service cần Entity User nên vẫn trả User.
     */

    @Override
    @Transactional(readOnly = true)
    public List<User> getEmployeeUsers() {

        return userRepository.findUsersAvailableForEmployee(
                "EMPLOYEE"
        );
    }


    /**
     * Khi Edit Employee:
     *
     * - User EMPLOYEE chưa được liên kết
     * - User hiện tại của Employee đang sửa
     */

    @Override
    @Transactional(readOnly = true)
    public List<User> getEmployeeUsersForEdit(Long employeeId) {

        return userRepository.findUsersAvailableForEmployeeEdit(
                "EMPLOYEE",
                employeeId
        );
    }



    // =====================================================
    // SEARCH / PAGINATION (Tìm kiếm người dùng nâng cao kết hợp phân trang và sắp xếp dữ liệu.)
    // =====================================================

    /**
     * Tìm kiếm nâng cao kết hợp Phân trang (Pagination) và Sắp xếp (Sort).
     * Input: UserSearchRequest (Chứa từ khóa tìm kiếm, số trang muốn xem, số lượng dòng mỗi trang)
     * Output: Page<UserResponse> (Một "trang" dữ liệu sạch đẹp chứa danh sách người dùng kèm thông tin tổng số trang, tổng số dòng)
     */
    @Override
    @Transactional(readOnly = true) // Hàm này chỉ đọc dữ liệu (SELECT) để phân trang -> giúp tối ưu hiệu năng và tốc độ truy vấn
    public Page<UserResponse> search(UserSearchRequest request) {

        // =================================================
        // Bước 1: Cấu hình quy tắc Sắp xếp (Sort)
        // =================================================
        // Tạo ra một đối tượng Sort: Sắp xếp theo chiều giảm dần (DESC - Mới nhất lên đầu)
        // dựa vào trường dữ liệu mà người dùng yêu cầu (ví dụ truyền vào "id" hoặc "username")
        Sort sort = Sort.by(
                Sort.Direction.DESC,
                request.getSortBy()
        );

        // =================================================
        // Bước 2: Khởi tạo thông số Phân trang (Pageable)
        // =================================================
        // Định nghĩa một "yêu cầu phân trang" bao gồm:
        // request.getPage(): Số thứ tự trang muốn xem (Trang 0, Trang 1, Trang 2...)
        // request.getSize(): Số lượng bản ghi muốn hiển thị trên 1 trang (Ví dụ: 10 người / trang)
        // sort: Kèm theo quy tắc sắp xếp vừa cấu hình ở Bước 1
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        // =================================================
        // Bước 3: Đẩy xuống Database quét dữ liệu
        // =================================================
        // Gọi hàm findAll() được cung cấp bởi JpaSpecificationExecutor trong Repository để kết hợp 2 thứ:
        // 1. UserSpecification.search(request): Bộ lọc tìm kiếm động (Chỉ lấy user chưa xóa, khớp từ khóa, đúng role...)
        // 2. pageable: Khuôn phân trang (Chỉ lấy đúng số lượng của trang hiện tại)
        // Kết quả trả về một gói 'Page<User>' chứa dữ liệu thô của trang đó.
        Page<User> userPage = userRepository.findAll(
                UserSpecification.search(request),
                pageable
        );

        // =================================================
        // Bước 4: Đổi sang dữ liệu sạch (Map Entity → Response DTO)
        // =================================================
        // Vòng lặp tự động duyệt qua từng ông 'User' thô có trong trang hiện tại,
        // nhờ 'userMapper' chuyển đổi thành 'UserResponse' sạch đẹp để trả về cho Frontend hiển thị.
        return userPage.map(userMapper::toResponse);
    }
}