"use strict";

/* ============================================================
   SYSTEMANAGERCV
   NOTIFICATION API
   File:
   services/api/notificationApi.js
   ============================================================ */


/* ============================================================
   GET MY NOTIFICATIONS
   GET /api/v1/notifications
   ============================================================ */
/**
 * LẤY DANH SÁCH THÔNG BÁO CỦA TÔI
 * HTTP Method: GET
 * URL: /api/v1/notifications
 * 
 * @param { number} page - Số trang muốn lấy (mặc định là trang 0)
 * @param {number} size - Số lượng thông báo trên mỗi trang (mặc định là 10)
 * @param {boolean} unreadOnly - Chỉ lấy thông báo chưa đọc? (mặc định là false - lấy tất cả)
 */
// async chạy ngầm, k làm đơ máy, tức là vẫn lướt mượt mà trong lúc đợi tải dữ liệu nên
async function getNotifications({page = 0, size = 10, unreadOnly = false} = {}){
    // Tạo bộ quản lý tham số URL (Query Parameters)
    const params = new URLSearchParams();
    params.set('page', page);
    params.set('size', size);
    //unreadOnly là chỉ lấy thông báo chưa đọc, nếu true thì chỉ lấy thông báo chưa đọc, nếu false thì lấy tất cả thông báo
    // readOnly chỉ xem chứ không đc sửa
    params.set("unreadOnly", String(Boolean(unreadOnly)));// Chuyển kiểu true/false thành chuỗi "true"/"false"

    // Gọi API gửi yêu cầu lấy danh sách thông báo về
    return apiRequest(`/notifications?${params.toString()}`, { method: "GET" }); 
}

/**
 * LẤY SỐ LƯỢNG THÔNG BÁO CHƯA ĐỌC
 * HTTP Method: GET
 * URL: /api/v1/notifications/unread-count
 */
async function getUnreadNotificationCount() {
    // Gọi API để xem người dùng còn bao nhiêu thông báo chưa bấm xem
    return apiRequest("/notifications/unread-count", { method: "GET" }); 
} 

/**
 * ĐÁNH DẤU MỘT THÔNG BÁO LÀ ĐÃ ĐỌC
 * HTTP Method: POST
 * URL: /api/v1/notifications/{id}/read
 * 
 * @param {string|number} notificationId - ID của thông báo cần xử lý
 */
async function markNotificationAsRead(notificationId) {
    // Kiểm tra tính hợp lệ: Nếu ID trống, lập tức báo lỗi và dừng lại
    if (notificationId === null || notificationId === undefined || notificationId === "") {
         throw new Error("Notification ID không hợp lệ."); 
    }  

    // Mã hóa ID (tránh lỗi ký tự đặc biệt trên URL) và gửi yêu cầu cập nhật trạng thái "đã đọc" lên server
    return apiRequest(`/notifications/${encodeURIComponent(notificationId)}/read`, { method: "POST" }); 
}