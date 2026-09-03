package systemanagercv.example.systemanagercv.common.constant;

public final class ResponseCode {

    public static final String SUCCESS = "success";

    private ResponseCode() {}
}
/*
* Theo quy định không hard-code giá trị cố định
* Vì vậy không nên rải "success" khắp Controller
* Sau này Controller dùng: ResponseCode.SUCCESS thay vì "success"*/
