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
    UserResponse toResponse(User user);

    UserResponse toResponse(UserListProjection projection);

    UserDetailResponse toDetailResponse(User user);

    User toEntity(UserCreateRequest request);
}
