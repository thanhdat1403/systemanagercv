console.log("LOGIN.JS ĐÃ ĐƯỢC LOAD");
document.addEventListener("DOMContentLoaded", function () {

    const loginForm = document.getElementById("loginForm");
    console.log("loginForm =", loginForm);

    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const loginError = document.getElementById("loginError");
    const loginButton = document.getElementById("loginButton");


    /*
     * =========================================================
     * LOGIN FORM
     * =========================================================
     */

loginForm.addEventListener("submit", async function (event) {

    console.log("SUBMIT EVENT ĐÃ ĐƯỢC BẮT");

    event.preventDefault();


        /*
         * =====================================================
         * BƯỚC 1
         * Lấy username/password
         * =====================================================
         */

        const username = usernameInput.value.trim();
        const password = passwordInput.value;


        /*
         * =====================================================
         * BƯỚC 2
         * Xóa lỗi cũ
         * =====================================================
         */

        loginError.style.display = "none";
        loginError.textContent = "";


        /*
         * =====================================================
         * BƯỚC 3
         * Kiểm tra dữ liệu cơ bản
         * =====================================================
         */

        if (!username) {

            showLoginError("Vui lòng nhập username.");

            usernameInput.focus();

            return;
        }


        if (!password) {

            showLoginError("Vui lòng nhập password.");

            passwordInput.focus();

            return;
        }


        /*
         * =====================================================
         * BƯỚC 4
         * Disable button trong lúc login
         * =====================================================
         */

        loginButton.disabled = true;

        loginButton.value = "Signing In...";


        try {

            /*
             * =================================================
             * BƯỚC 5
             * Gọi Backend API
             *
             * Backend:
             *
             * POST
             * http://localhost:8080/api/v1/auth/login
             *
             * Body:
             *
             * {
             *     "username": "...",
             *     "password": "..."
             * }
             * =================================================
             */

            const response = await fetch(
                "http://localhost:8080/api/v1/auth/login",
                {
                    method: "POST",

                    credentials: "include",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        username: username,
                        password: password
                    })
                }
            );


            /*
             * =================================================
             * BƯỚC 6
             * Đọc JSON response
             * =================================================
             */

            const result = await response.json();


            /*
             * =================================================
             * BƯỚC 7
             * Kiểm tra HTTP error
             * =================================================
             */

            if (!response.ok) {

                showLoginError(
                    result.message || "Đăng nhập thất bại."
                );

                return;
            }


            /*
             * =================================================
             * BƯỚC 8
             * Kiểm tra ApiResponse.code
             *
             * Backend trả:
             *
             * {
             *     "code": "success",
             *     "message": "Success",
             *     "data": {...}
             * }
             * =================================================
             */

            if (result.code !== "success") {

                showLoginError(
                    result.message || "Đăng nhập thất bại."
                );

                return;
            }


            /*
             * =================================================
             * BƯỚC 9
             * Lấy LoginResponse
             * =================================================
             */

            const loginData = result.data;


            if (!loginData) {

                showLoginError(
                    "Server không trả về thông tin đăng nhập."
                );

                return;
            }


            /*
             * =================================================
             * BƯỚC 10
             * Kiểm tra roles
             * =================================================
             */

            const roles = loginData.roles;


            if (
                !Array.isArray(roles) ||
                roles.length === 0
            ) {

                showLoginError(
                    "Tài khoản chưa được cấp quyền."
                );

                return;
            }


            /*
             * =================================================
             * BƯỚC 11
             * Lưu thông tin UI
             *
             * KHÔNG lưu accessToken.
             *
             * accessToken đã được Backend đặt vào
             * HttpOnly Cookie.
             * =================================================
             */

            sessionStorage.setItem(
                "username",
                loginData.username
            );

            sessionStorage.setItem(
                "roles",
                JSON.stringify(roles)
            );


            /*
             * =================================================
             * BƯỚC 12
             * Login thành công
             *
             * Tất cả role dùng chung Dashboard.
             * =================================================
             */

            window.location.href =
                "../dashboard/index.html";


        } catch (error) {

            /*
             * =================================================
             * BƯỚC 13
             * Lỗi kết nối Backend
             * =================================================
             */

            console.error(
                "Login error:",
                error
            );


            showLoginError(
                "Không thể kết nối tới máy chủ."
            );

        } finally {

            /*
             * =================================================
             * BƯỚC 14
             * Enable lại button
             * =================================================
             */

            loginButton.disabled = false;

            loginButton.value = "Sign In";
        }

    });


    /*
     * =========================================================
     * HIỂN THỊ LOGIN ERROR
     * =========================================================
     */

    function showLoginError(message) {

        loginError.textContent = message;

        loginError.style.display = "block";
    }

});