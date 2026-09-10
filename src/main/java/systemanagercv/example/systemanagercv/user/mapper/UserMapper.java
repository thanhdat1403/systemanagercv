package systemanagercv.example.systemanagercv.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import systemanagercv.example.systemanagercv.user.dto.request.UserCreateRequest;
import systemanagercv.example.systemanagercv.user.dto.response.UserDetailResponse;
import systemanagercv.example.systemanagercv.user.dto.response.UserResponse;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.projection.UserListProjection;

@Mapper(componentModel = "spring")
public interface UserMapper {
    /*Entity ↔ DTO phải mapping trong package mapper;
     ưu tiên MapStruct; Controller/Repository không được mapping.
     Database
       ↓
    User Entity
       ↓
    UserMapper
       ↓
    UserResponse
       ↓
    API JSON*/
    /*NẾU K CÓ MAPPER, Thì ta sẽ phải viết như này:
    * UserResponse response = UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .enabled(user.isEnabled())
        .build();, nếu có 10 DTO thì Service sẽ rất dài, MapStruct sẽ tự sinh code mapping cho ta*/
    // =====================================================
    // USER → USER RESPONSE
    // =====================================================

    @Mapping(
            target = "roleId",
            expression = "java(getRoleId(user))"
    )
    @Mapping(
            target = "roleName",
            expression = "java(getRoleName(user))"
    )
    @Mapping(
            target = "roleDescription",
            expression = "java(getRoleDescription(user))"
    )
    UserResponse toResponse(User user);


    // =====================================================
    // PROJECTION → USER RESPONSE
    // =====================================================

    UserResponse toResponse(UserListProjection projection);


    // =====================================================
    // USER → USER DETAIL RESPONSE
    // =====================================================

    @Mapping(
            target = "roleId",
            expression = "java(getRoleId(user))"
    )
    @Mapping(
            target = "roleName",
            expression = "java(getRoleName(user))"
    )
    @Mapping(
            target = "roleDescription",
            expression = "java(getRoleDescription(user))"
    )
    UserDetailResponse toDetailResponse(User user);


    // =====================================================
    // REQUEST → USER
    // =====================================================

    User toEntity(UserCreateRequest request);


    // =====================================================
    // ROLE HELPER METHODS
    // =====================================================

    default Long getRoleId(User user) {

        if (user == null
                || user.getUserRoles() == null
                || user.getUserRoles().isEmpty()) {

            return null;
        }

        return user.getUserRoles()
                .iterator()
                .next()
                .getRole()
                .getId();
    }


    default String getRoleName(User user) {

        if (user == null
                || user.getUserRoles() == null
                || user.getUserRoles().isEmpty()) {

            return null;
        }

        return user.getUserRoles()
                .iterator()
                .next()
                .getRole()
                .getName();
    }


    default String getRoleDescription(User user) {

        if (user == null
                || user.getUserRoles() == null
                || user.getUserRoles().isEmpty()) {

            return null;
        }

        return user.getUserRoles()
                .iterator()
                .next()
                .getRole()
                .getDescription();
    }
}
