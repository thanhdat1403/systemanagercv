
// =====================================================
// SYSTEMANAGERCV - API CORE
// =====================================================

const API_BASE_URL = "http://localhost:8080/api/v1";


/**
 * Gọi API chung.
 *
 * JWT nằm trong HttpOnly Cookie "accessToken",
 * vì vậy frontend phải dùng:
 *
 * credentials: "include"
 */
async function apiRequest(
    endpoint,
    options = {}
) {

    const config = {
        ...options,

        credentials: "include",

        headers: {
            "Content-Type": "application/json",

            ...(options.headers || {})
        }
    };


    let response;

    try {

        response = await fetch(
            API_BASE_URL + endpoint,
            config
        );

    } catch (error) {

        console.error(
            "API CONNECTION ERROR:",
            error
        );

        throw new Error(
            "Không thể kết nối tới máy chủ."
        );
    }


    // =================================================
    // HTTP 401
    // =================================================

    if (response.status === 401) {

        console.error(
            "API 401 - Unauthorized"
        );

        sessionStorage.clear();

        window.location.href =
            "../login/login.html";

        throw new Error(
            "Phiên đăng nhập đã hết hạn."
        );
    }


    // =================================================
    // HTTP 204
    // =================================================

    if (response.status === 204) {

        return null;
    }


    // =================================================
    // Đọc JSON
    // =================================================

    let result;

    try {

        result = await response.json();

    } catch (error) {

        console.error(
            "API RESPONSE IS NOT JSON:",
            error
        );

        throw new Error(
            "Server trả về dữ liệu không hợp lệ."
        );
    }


    // =================================================
    // HTTP ERROR
    // =================================================

    if (!response.ok) {

        throw new Error(
            result.message ||
            "API request thất bại."
        );
    }


    // =================================================
    // ApiResponse của Backend
    // =================================================

    if (
        result &&
        result.code &&
        result.code !== "success"
    ) {

        throw new Error(
            result.message ||
            "API request thất bại."
        );
    }


    return result;
}
