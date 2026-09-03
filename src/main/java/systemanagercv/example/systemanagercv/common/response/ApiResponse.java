package systemanagercv.example.systemanagercv.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse<T> { // lớp này đc dùng lm khuôn mẫu chung duy nhất để trả về dữ liệu trừ Server (BE) về cho client (FE) cho mọi API trong dự án

    private String code;

    private String message;

    private T data;
}
/*Lớp này có tác dụng trả về API theo cấu trúc response (Quy đinh số 4):
* VD: API trả về 1 User: ApiResponse<UserResponse>
JSON:{
    "code": "success",
    "message": "Success",
    "data": {
        "id": 1,
        "username": "admin",
        "email": "admin@gmail.com",
        "enabled": true,
        "roleId": 1,
        "roleName": "ADMIN",
        "roleDescription": "System administrator"
    }
}
* thì API trả danh sách: ApiResponse<Page<UserResponse>>
Thì: {
    "code": "success",
    "message": "Success",
    "data": {
        "content": [],
        "totalElements": 20,
        "totalPages": 2,
        "size": 10,
        "number": 0
    }
}*/