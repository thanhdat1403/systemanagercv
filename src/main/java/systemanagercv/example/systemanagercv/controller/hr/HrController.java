package systemanagercv.example.systemanagercv.controller.hr;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hr")// Gom tất cả các đường dẫn bắt đầu bằng /hr vào controller này
public class HrController {
    /**
     * ĐÓN NHẬN ĐƯỜNG DẪN /hr/index
     * Hàm này sẽ hứng lệnh điều hướng từ successHandler gửi sang
     */
    @GetMapping("/index")
    public String index() {
        return "/hr/index";
    }
}
