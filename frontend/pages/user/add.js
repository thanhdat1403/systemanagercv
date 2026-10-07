/**
 * ============================================================
 * SYSTEMANAGERCV
 * USER ADD PAGE
 * File: pages/user/add.js
 * ============================================================
 */


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


            initializeUserPageSidebarToggle();


            if (
                typeof setupLogout ===
                "function"
            ) {

                setupLogout();

            }


            /*
             * Tải Role trước khi khởi tạo Form.
             */
            await loadRoles();


            initializeForm();


        } catch (error) {

            console.error(
                "USER ADD INITIALIZATION ERROR:",
                error
            );


            showError(
                error.message ||
                "Không thể khởi tạo trang thêm User."
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
 * ROLE
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
 * FORM
 * ============================================================ */

function initializeForm() {

    const form =
        document.getElementById(
            "user-add-form"
        );


    if (!form) {

        return;

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


            const confirmPassword =
                document.getElementById(
                    "confirmPassword"
                )?.value || "";


            const roleId =
                document.getElementById(
                    "roleId"
                )?.value || "";


            const enabled =
                document.getElementById(
                    "enabled"
                )?.value === "true";


            /* ================================================
             * VALIDATION
             * ================================================ */

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


            if (
                password.length < 6 ||
                password.length > 255
            ) {

                showError(
                    "Mật khẩu phải có từ 6 đến 255 ký tự."
                );

                return;

            }


            if (
                password !==
                confirmPassword
            ) {

                showError(
                    "Mật khẩu xác nhận không khớp."
                );

                return;

            }


            if (!roleId) {

                showError(
                    "Vui lòng chọn quyền."
                );

                return;

            }


            /* ================================================
             * REQUEST DATA
             * ================================================ */

            const data = {

                username,

                password,

                fullName,

                email:
                    email || null,

                roleId:
                    Number(roleId),

                enabled

            };


            try {

                const response =
                    await createUser(
                        data
                    );


                console.log(
                    "CREATE USER RESPONSE:",
                    response
                );


                showSuccess(
                    "Tạo người dùng thành công."
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
                    "CREATE USER ERROR:",
                    error
                );


                showError(
                    error.message ||
                    "Không thể tạo người dùng."
                );

            }

        }
    );


    document
        .getElementById(
            "user-reset"
        )
        ?.addEventListener(
            "click",
            function () {

                hideMessages();

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