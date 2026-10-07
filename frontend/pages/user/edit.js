/**
 * ============================================================
 * SYSTEMANAGERCV
 * USER EDIT PAGE
 * File: pages/user/edit.js
 * ============================================================
 */

let userId = null;
let originalUsername = "";


/* ============================================================
 * INITIALIZATION
 * ============================================================ */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        try {

            await loadComponent(
                "header-container",
                "../../components/header.html"
            );

            await loadComponent(
                "sidebar-container",
                "../../components/sidebar.html"
            );

            await loadComponent(
                "footer-container",
                "../../components/footer.html"
            );


            if (
                typeof initializeHeaderUser ===
                "function"
            ) {

                initializeHeaderUser();

            }


            if (
                typeof initializeSidebar ===
                "function"
            ) {

                initializeSidebar();

            }


            if (
                typeof initializeUserPageSidebarToggle ===
                "function"
            ) {

                initializeUserPageSidebarToggle();

            }


            if (
                typeof setupLogout ===
                "function"
            ) {

                setupLogout();

            }


            userId =
                new URLSearchParams(
                    window.location.search
                ).get("id");


            if (!userId) {

                throw new Error(
                    "Không tìm thấy ID người dùng."
                );

            }


            const userIdDisplay =
                document.getElementById(
                    "user-id-display"
                );


            if (userIdDisplay) {

                userIdDisplay.textContent =
                    userId;

            }


            /*
             * Load Role trước
             * để sau đó load User và set Role hiện tại.
             */
            await loadRoles();


            await loadUser();


            initializeForm();


        } catch (error) {

            console.error(
                "USER EDIT INITIALIZATION ERROR:",
                error
            );


            showError(
                error.message ||
                "Không thể khởi tạo trang sửa User."
            );

        }

    }
);


/* ============================================================
 * COMPONENT
 * ============================================================ */

async function loadComponent(
    elementId,
    filePath
) {

    const container =
        document.getElementById(
            elementId
        );


    if (!container) {

        throw new Error(
            `Không tìm thấy #${elementId}`
        );

    }


    const response =
        await fetch(
            filePath,
            {
                method: "GET",
                cache: "no-cache"
            }
        );


    if (!response.ok) {

        throw new Error(
            `Không thể tải ${filePath}`
        );

    }


    container.innerHTML =
        await response.text();

}


/* ============================================================
 * LOAD ROLES
 * ============================================================ */

async function loadRoles() {

    const roleSelect =
        document.getElementById(
            "roleId"
        );


    if (!roleSelect) {

        return;

    }


    if (
        typeof getRoles !==
        "function"
    ) {

        throw new Error(
            "Không tìm thấy hàm getRoles(). Hãy kiểm tra roleApi.js."
        );

    }


    try {

        const response =
            await getRoles();


        const roles =
            response?.data || [];


        roleSelect.innerHTML = `
            <option value="">
                -- Chọn quyền --
            </option>
        `;


        roles.forEach(
            function (role) {

                const option =
                    document.createElement(
                        "option"
                    );


                option.value =
                    role.id;


                option.textContent =
                    role.name;


                roleSelect.appendChild(
                    option
                );

            }
        );


    } catch (error) {

        console.error(
            "LOAD ROLES ERROR:",
            error
        );


        throw new Error(
            error.message ||
            "Không thể tải danh sách quyền."
        );

    }

}


/* ============================================================
 * LOAD USER
 * ============================================================ */

async function loadUser() {

    try {

        const response =
            await getUserById(
                userId
            );


        const user =
            response?.data;


        if (!user) {

            throw new Error(
                "Không tìm thấy dữ liệu User."
            );

        }


        /*
         * Lưu username cũ.
         *
         * Dùng để xác định:
         * "Có phải User hiện tại đang tự đổi username hay không?"
         */
        originalUsername =
            user.username || "";


        const username =
            document.getElementById(
                "username"
            );


        const fullName =
            document.getElementById(
                "fullName"
            );


        const email =
            document.getElementById(
                "email"
            );


        const enabled =
            document.getElementById(
                "enabled"
            );


        const roleId =
            document.getElementById(
                "roleId"
            );


        if (username) {

            username.value =
                user.username || "";

        }


        if (fullName) {

            fullName.value =
                user.fullName || "";

        }


        if (email) {

            email.value =
                user.email || "";

        }


        if (enabled) {

            enabled.value =
                user.enabled
                    ? "true"
                    : "false";

        }


        if (
            roleId &&
            user.roleId !== null &&
            user.roleId !== undefined
        ) {

            roleId.value =
                String(user.roleId);

        }


    } catch (error) {

        console.error(
            "LOAD USER ERROR:",
            error
        );

        throw new Error(
            error.message ||
            "Không thể tải thông tin User."
        );

    }

}


/* ============================================================
 * FORM
 * ============================================================ */

function initializeForm() {

    const form =
        document.getElementById(
            "user-edit-form"
        );


    if (!form) {

        throw new Error(
            "Không tìm thấy form sửa User."
        );

    }


    form.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            hideMessages();


            const username =
                document.getElementById(
                    "username"
                )?.value.trim() || "";


            const fullName =
                document.getElementById(
                    "fullName"
                )?.value.trim() || "";


            const email =
                document.getElementById(
                    "email"
                )?.value.trim() || "";


            const password =
                document.getElementById(
                    "password"
                )?.value || "";


            const roleId =
                document.getElementById(
                    "roleId"
                )?.value || "";


            const enabled =
                document.getElementById(
                    "enabled"
                )?.value === "true";


            /* ==================================================
             * VALIDATION
             * ================================================== */

            if (!username) {

                showError(
                    "Tên đăng nhập không được để trống."
                );

                return;

            }


            if (!fullName) {

                showError(
                    "Họ và tên không được để trống."
                );

                return;

            }


            /*
             * Password ở Edit KHÔNG bắt buộc.
             *
             * Rỗng:
             * -> Backend giữ password cũ.
             *
             * Có nhập:
             * -> Phải từ 6 đến 255 ký tự.
             */
            if (
                password !== ""
                &&
                (
                    password.length < 6
                    ||
                    password.length > 255
                )
            ) {

                showError(
                    "Mật khẩu phải có từ 6 đến 255 ký tự."
                );

                return;

            }


            if (!roleId) {

                showError(
                    "Vui lòng chọn vai trò."
                );

                return;

            }


            /* ==================================================
             * REQUEST DATA
             * ================================================== */

            const data = {

                username,

                fullName,

                email:
                    email || null,

                password,

                roleId:
                    Number(roleId),

                enabled

            };


            try {

                /*
                 * Lưu username hiện tại đang đăng nhập
                 * TRƯỚC khi update.
                 */
                const loggedInUsername =
                    sessionStorage.getItem(
                        "username"
                    );


                /*
                 * UPDATE USER
                 */
                await updateUser(
                    userId,
                    data
                );


                /*
                 * =================================================
                 * TRƯỜNG HỢP ĐẶC BIỆT:
                 * ADMIN ĐANG SỬA CHÍNH TÀI KHOẢN CỦA MÌNH
                 * VÀ ĐỔI USERNAME.
                 *
                 * Ví dụ:
                 *
                 * admin
                 *   ↓
                 * thanhdat
                 *
                 * JWT hiện tại vẫn chứa:
                 *
                 * username = admin
                 *
                 * Nhưng Database đã là:
                 *
                 * username = thanhdat
                 *
                 * Vì vậy KHÔNG được quay lại index.html.
                 * Phải đăng nhập lại.
                 * =================================================
                 */
                if (
                    loggedInUsername
                    &&
                    loggedInUsername === originalUsername
                    &&
                    username !== originalUsername
                ) {

                    showSuccess(
                        "Cập nhật tài khoản thành công. Vui lòng đăng nhập lại bằng tên đăng nhập mới."
                    );


                    /*
                     * Xóa thông tin đăng nhập phía Frontend.
                     */
                    sessionStorage.clear();


                    /*
                     * Cố gắng xóa HttpOnly JWT cookie
                     * thông qua API logout.
                     *
                     * Nếu API logout lỗi thì vẫn tiếp tục
                     * chuyển sang Login.
                     */
                    try {

                        if (
                            typeof logout ===
                            "function"
                        ) {

                            await logout();

                        }

                    } catch (logoutError) {

                        console.warn(
                            "SELF RENAME LOGOUT WARNING:",
                            logoutError
                        );

                    }


                    setTimeout(
                        function () {

                            window.location.replace(
                                "../login/login.html"
                            );

                        },
                        1200
                    );


                    return;

                }


                /*
                 * Các trường hợp sửa User khác:
                 * quay về danh sách.
                 */
                showSuccess(
                    "Cập nhật người dùng thành công."
                );


                setTimeout(
                    function () {

                        window.location.replace(
                            "./index.html"
                        );

                    },
                    700
                );


            } catch (error) {

                console.error(
                    "UPDATE USER ERROR:",
                    error
                );


                showError(
                    error.message ||
                    "Không thể cập nhật User."
                );

            }

        }
    );

}


/* ============================================================
 * MESSAGE
 * ============================================================ */

function hideMessages() {

    const error =
        document.getElementById(
            "user-error"
        );


    const success =
        document.getElementById(
            "user-success"
        );


    if (error) {

        error.style.display =
            "none";

    }


    if (success) {

        success.style.display =
            "none";

    }

}


function showError(
    message
) {

    const element =
        document.getElementById(
            "user-error"
        );


    if (!element) {

        return;

    }


    element.textContent =
        message;


    element.style.display =
        "";

}


function showSuccess(
    message
) {

    const element =
        document.getElementById(
            "user-success"
        );


    if (!element) {

        return;

    }


    element.textContent =
        message;


    element.style.display =
        "";

}