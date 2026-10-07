/* ============================================================
 * SYSTEMANAGERCV
 * AUTH API
 * File: services/auth/authApi.js
 *
 * NHIỆM VỤ:
 *
 * 1. Lấy thông tin User hiện tại
 * 2. Đăng xuất
 *
 * JWT được lưu trong HttpOnly Cookie.
 * Frontend KHÔNG đọc accessToken trực tiếp.
 * ============================================================ */


/* ============================================================
 * GET CURRENT USER
 * ============================================================ */

async function getCurrentUser() {

    let response;


    try {

        response =
            await fetch(
                API_BASE_URL +
                "/auth/me",
                {
                    method:
                        "GET",

                    credentials:
                        "include",

                    headers: {
                        "Content-Type":
                            "application/json"
                    }
                }
            );

    } catch (error) {

        console.error(
            "GET CURRENT USER CONNECTION ERROR:",
            error
        );

        throw new Error(
            "Không thể kết nối tới máy chủ."
        );

    }


    /*
     * HTTP 401:
     *
     * JWT không còn hợp lệ
     * hoặc User không còn tồn tại.
     */

    if (
        response.status ===
        401
    ) {

        sessionStorage.clear();


        window.location.replace(
            "../login/login.html"
        );


        throw new Error(
            "Phiên đăng nhập đã hết hạn."
        );

    }


    let result =
        null;


    try {

        result =
            await response.json();

    } catch (error) {

        result =
            null;

    }


    if (
        !response.ok
    ) {

        throw new Error(
            result?.message ||
            "Không thể lấy thông tin người dùng hiện tại."
        );

    }


    return result;

}


/* ============================================================
 * LOGOUT
 * ============================================================ */

async function logout() {

    let response;


    try {

        response =
            await fetch(
                API_BASE_URL +
                "/auth/logout",
                {
                    method:
                        "POST",

                    credentials:
                        "include",

                    headers: {
                        "Content-Type":
                            "application/json"
                    }
                }
            );

    } catch (error) {

        console.error(
            "LOGOUT CONNECTION ERROR:",
            error
        );

        throw new Error(
            "Không thể kết nối tới máy chủ để đăng xuất."
        );

    }


    let result =
        null;


    try {

        result =
            await response.json();

    } catch (error) {

        /*
         * Backend có thể không trả JSON.
         */

        result =
            null;

    }


    if (
        !response.ok
    ) {

        throw new Error(
            result?.message ||
            "Đăng xuất thất bại."
        );

    }


    return result;

}