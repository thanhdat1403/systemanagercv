/* ============================================================
 * SYSTEMANAGERCV - EMPLOYEE ADD PAGE
 * File: pages/employee/add.js
 *
 * Mục tiêu:
 * 1. Load Header / Sidebar / Footer.
 * 2. Khởi tạo Header User.
 * 3. Khởi tạo Sidebar Accordion / NiceScroll.
 * 4. Nút fa fa-bars thu / mở Sidebar ổn định.
 * 5. API create-options lỗi không làm chết giao diện.
 * 6. Form Add Employee hoạt động độc lập.
 * ============================================================ */


/* ============================================================
 * PAGE INITIALIZATION
 * ============================================================ */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        console.log(
            "================================="
        );

        console.log(
            "EMPLOYEE ADD PAGE START"
        );

        console.log(
            "================================="
        );


        /* ========================================================
         * 1. LOAD HEADER
         * ========================================================
         */

        const headerLoaded =
            await loadComponent(
                "header-container",
                "../../components/header.html"
            );


        if (!headerLoaded) {

            console.error(
                "EMPLOYEE ADD: Không thể tải Header."
            );

            return;
        }


        /* ========================================================
         * 2. LOAD SIDEBAR
         * ========================================================
         */

        const sidebarLoaded =
            await loadComponent(
                "sidebar-container",
                "../../components/sidebar.html"
            );


        if (!sidebarLoaded) {

            console.error(
                "EMPLOYEE ADD: Không thể tải Sidebar."
            );

            return;
        }
        applyEmployeeAddSidebarRole();


        /* ========================================================
         * 3. LOAD FOOTER
         * ========================================================
         */

        await loadComponent(
            "footer-container",
            "../../components/footer.html"
        );


        /* ========================================================
         * 4. INITIALIZE HEADER USER
         * ========================================================
         */

        try {

            if (
                typeof initializeHeaderUser ===
                "function"
            ) {

                initializeHeaderUser();

            }

        } catch (error) {

            console.error(
                "HEADER USER INITIALIZATION ERROR:",
                error
            );
        }


        /* ========================================================
         * 5. INITIALIZE SIDEBAR
         *
         * Hàm này phụ trách:
         * - Accordion
         * - NiceScroll
         *
         * Không dùng nó cho hamburger.
         * ========================================================
         */

        try {

            if (
                typeof initializeSidebar ===
                "function"
            ) {

                initializeSidebar();

            }

        } catch (error) {

            console.error(
                "SIDEBAR INITIALIZATION ERROR:",
                error
            );
        }


        /* ========================================================
         * 6. INITIALIZE HAMBURGER
         *
         * Rất quan trọng:
         *
         * Header được load bằng fetch().
         * Vì vậy phải tìm nút hamburger SAU KHI
         * header đã được insert vào DOM.
         * ========================================================
         */

        try {

            initializeEmployeeAddSidebarToggle();

        } catch (error) {

            console.error(
                "SIDEBAR TOGGLE INITIALIZATION ERROR:",
                error
            );
        }


        /* ========================================================
         * 7. LOGOUT
         * ========================================================
         */

        try {

            if (
                typeof setupLogout ===
                "function"
            ) {

                setupLogout();

            }

        } catch (error) {

            console.error(
                "LOGOUT INITIALIZATION ERROR:",
                error
            );
        }


        /* ========================================================
         * 8. INITIALIZE FORM
         * ========================================================
         */

        try {

            initializeForm();

            initializeResetButton();

        } catch (error) {

            console.error(
                "FORM INITIALIZATION ERROR:",
                error
            );

            showError(
                error.message ||
                "Không thể khởi tạo biểu mẫu thêm nhân viên."
            );

        }


        /* ========================================================
         * 9. LOAD CREATE OPTIONS
         *
         * API lỗi chỉ làm lỗi dropdown.
         * Không được làm chết Sidebar.
         * ========================================================
         */

        await loadCreateOptions();


        console.log(
            "================================="
        );

        console.log(
            "EMPLOYEE ADD PAGE INITIALIZED"
        );

        console.log(
            "================================="
        );

    }
);


/* ================================================================
 * SIDEBAR TOGGLE - EMPLOYEE ADD PAGE
 * ================================================================
 *
 * Đây là phần QUAN TRỌNG NHẤT.
 *
 * Không sử dụng:
 *
 *     initializeSidebarToggle()
 *
 * Không sử dụng document capture.
 *
 * Không xử lý qua dashboard.js.
 *
 * Cách làm:
 *
 * 1. Header đã được load.
 * 2. Tìm trực tiếp:
 *
 *      .sidebar-toggle-box .fa-bars
 *
 * 3. Clone nút để loại bỏ toàn bộ event listener cũ.
 * 4. Thay nút cũ bằng nút mới.
 * 5. Gắn duy nhất một event click.
 *
 * Khi click:
 *
 *      #sidebar
 *          margin-left: -240px
 *
 *      #main-content
 *          margin-left: 0
 *
 * Khi click lần nữa:
 *
 *      #sidebar
 *          margin-left: giá trị ban đầu
 *
 *      #main-content
 *          margin-left: giá trị ban đầu
 *
 * ================================================================
 */

function initializeEmployeeAddSidebarToggle() {

    console.log(
        "=== INITIALIZE EMPLOYEE ADD SIDEBAR TOGGLE ==="
    );


    const sidebar =
        document.getElementById(
            "sidebar"
        );


    const mainContent =
        document.getElementById(
            "main-content"
        );


    const sidebarToggleBox =
        document.querySelector(
            ".sidebar-toggle-box"
        );


    const originalToggle =
        document.querySelector(
            ".sidebar-toggle-box .fa-bars"
        );


    /* ============================================================
     * CHECK SIDEBAR
     * ============================================================
     */

    if (!sidebar) {

        console.error(
            "Employee Add Sidebar: không tìm thấy #sidebar."
        );

        return;
    }


    /* ============================================================
     * CHECK MAIN CONTENT
     * ============================================================
     */

    if (!mainContent) {

        console.error(
            "Employee Add Sidebar: không tìm thấy #main-content."
        );

        return;
    }


    /* ============================================================
     * CHECK HAMBURGER
     * ============================================================
     */

    if (!sidebarToggleBox) {

        console.error(
            "Employee Add Sidebar: không tìm thấy .sidebar-toggle-box."
        );

        return;
    }


    if (!originalToggle) {

        console.error(
            "Employee Add Sidebar: không tìm thấy .fa-bars."
        );

        return;
    }


    /* ============================================================
     * LƯU GIÁ TRỊ BAN ĐẦU
     * ============================================================
     */

    const initialSidebarMargin =
        window.getComputedStyle(
            sidebar
        ).marginLeft;


    const initialMainMargin =
        window.getComputedStyle(
            mainContent
        ).marginLeft;


    const initialSidebarTransition =
        window.getComputedStyle(
            sidebar
        ).transition;


    const initialMainTransition =
        window.getComputedStyle(
            mainContent
        ).transition;


    sidebar.dataset.smcvInitialMarginLeft =
        initialSidebarMargin;


    mainContent.dataset.smcvInitialMarginLeft =
        initialMainMargin;


    sidebar.dataset.smcvSidebarCollapsed =
        "false";


    /* ============================================================
     * XÓA HANDLER CŨ BẰNG CÁCH CLONE NODE
     *
     * Đây là điểm quan trọng.
     *
     * cloneNode(true) tạo một DOM element mới
     * nhưng KHÔNG mang theo các Event Listener cũ.
     * ============================================================
     */

    const newToggle =
        originalToggle.cloneNode(
            true
        );


    originalToggle.parentNode.replaceChild(
        newToggle,
        originalToggle
    );


    /* ============================================================
     * CSS / CURSOR
     * ============================================================
     */

    sidebar.style.setProperty(
        "transition",
        "margin-left 0.3s ease-in-out",
        "important"
    );


    mainContent.style.setProperty(
        "transition",
        "margin-left 0.3s ease-in-out",
        "important"
    );


    newToggle.style.cursor =
        "pointer";


    /* ============================================================
     * CLICK EVENT
     * ============================================================
     */

    newToggle.addEventListener(
        "click",
        function (event) {

            event.preventDefault();

            event.stopPropagation();


            console.log(
                "EMPLOYEE ADD SIDEBAR: HAMBURGER CLICK"
            );


            /* ====================================================
             * LẤY TRẠNG THÁI
             * ====================================================
             */

            const isCollapsed =
                sidebar.dataset.smcvSidebarCollapsed ===
                "true";


            const nextCollapsed =
                !isCollapsed;


            /* ====================================================
             * COLLAPSE SIDEBAR
             * ====================================================
             */

            if (nextCollapsed) {

                console.log(
                    "EMPLOYEE ADD SIDEBAR: COLLAPSE"
                );


                /*
                 * Thêm class legacy để
                 * tương thích CSS cũ.
                 */

                sidebar.classList.add(
                    "hide-left-bar"
                );


                /*
                 * Class riêng của trang Add.
                 */

                sidebar.classList.add(
                    "smcv-add-sidebar-collapsed"
                );


                /*
                 * Thu Sidebar.
                 *
                 * CSS của project đã có:
                 *
                 * .hide-left-bar {
                 *      margin-left:-240px !important;
                 * }
                 *
                 * Nhưng ta dùng inline !important
                 * để chắc chắn thắng các CSS khác.
                 */

                sidebar.style.setProperty(
                    "margin-left",
                    "-240px",
                    "important"
                );


                /*
                 * Mở rộng Main Content.
                 */

                mainContent.classList.add(
                    "merge-left"
                );


                mainContent.classList.add(
                    "smcv-add-main-expanded"
                );


                mainContent.style.setProperty(
                    "margin-left",
                    "0px",
                    "important"
                );


                sidebar.dataset.smcvSidebarCollapsed =
                    "true";


                /*
                 * NiceScroll
                 */

                refreshEmployeeAddNiceScroll(
                    true
                );


                console.log(
                    "EMPLOYEE ADD SIDEBAR: COLLAPSED"
                );

            }


            /* ====================================================
             * EXPAND SIDEBAR
             * ====================================================
             */

            else {

                console.log(
                    "EMPLOYEE ADD SIDEBAR: EXPAND"
                );


                /*
                 * Xóa class collapse.
                 */

                sidebar.classList.remove(
                    "hide-left-bar"
                );


                sidebar.classList.remove(
                    "smcv-add-sidebar-collapsed"
                );


                /*
                 * Khôi phục margin ban đầu.
                 */

                const originalSidebarMargin =
                    sidebar.dataset.smcvInitialMarginLeft ||
                    "0px";


                sidebar.style.setProperty(
                    "margin-left",
                    originalSidebarMargin,
                    "important"
                );


                /*
                 * Main Content trở lại vị trí ban đầu.
                 */

                mainContent.classList.remove(
                    "merge-left"
                );


                mainContent.classList.remove(
                    "smcv-add-main-expanded"
                );


                const originalMainMargin =
                    mainContent.dataset.smcvInitialMarginLeft ||
                    "240px";


                mainContent.style.setProperty(
                    "margin-left",
                    originalMainMargin,
                    "important"
                );


                sidebar.dataset.smcvSidebarCollapsed =
                    "false";


                /*
                 * NiceScroll
                 */

                refreshEmployeeAddNiceScroll(
                    false
                );


                console.log(
                    "EMPLOYEE ADD SIDEBAR: EXPANDED"
                );
            }


        },
        false
    );


    /* ============================================================
     * RESIZE
     *
     * Khi màn hình thay đổi kích thước:
     * - Desktop: giữ trạng thái hiện tại.
     * - Mobile: để CSS responsive của project xử lý.
     * ============================================================
     */

    window.addEventListener(
        "resize",
        function () {

            const collapsed =
                sidebar.dataset.smcvSidebarCollapsed ===
                "true";


            /*
             * Nếu đang mobile thì
             * không ép margin desktop.
             */

            if (
                window.innerWidth <=
                991
            ) {

                sidebar.style.removeProperty(
                    "margin-left"
                );

                mainContent.style.removeProperty(
                    "margin-left"
                );

                return;
            }


            /*
             * Trở lại desktop.
             */

            if (collapsed) {

                sidebar.style.setProperty(
                    "margin-left",
                    "-240px",
                    "important"
                );


                mainContent.style.setProperty(
                    "margin-left",
                    "0px",
                    "important"
                );

            } else {

                const originalSidebarMargin =
                    sidebar.dataset.smcvInitialMarginLeft ||
                    "0px";


                const originalMainMargin =
                    mainContent.dataset.smcvInitialMarginLeft ||
                    "240px";


                sidebar.style.setProperty(
                    "margin-left",
                    originalSidebarMargin,
                    "important"
                );


                mainContent.style.setProperty(
                    "margin-left",
                    originalMainMargin,
                    "important"
                );
            }

        }
    );


    console.log(
        "Employee Add: hamburger toggle đã được khởi tạo thành công."
    );
}


/* ================================================================
 * REFRESH NICESCROLL
 * ================================================================
 */

function refreshEmployeeAddNiceScroll(
    collapsed
) {

    if (
        typeof window.jQuery ===
        "undefined"
    ) {

        return;
    }


    const leftNavigation =
        window.jQuery(
            ".leftside-navigation"
        );


    if (
        leftNavigation.length ===
        0
    ) {

        return;
    }


    if (
        typeof leftNavigation.getNiceScroll !==
        "function"
    ) {

        return;
    }


    const niceScroll =
        leftNavigation.getNiceScroll();


    if (!niceScroll) {

        return;
    }


    try {

        if (collapsed) {

            if (
                typeof niceScroll.hide ===
                "function"
            ) {

                niceScroll.hide();
            }

        } else {

            if (
                typeof niceScroll.show ===
                "function"
            ) {

                niceScroll.show();
            }


            if (
                typeof niceScroll.resize ===
                "function"
            ) {

                niceScroll.resize();
            }
        }

    } catch (error) {

        console.warn(
            "Không thể refresh NiceScroll:",
            error
        );
    }
}


/* ============================================================
 * LOAD COMPONENT
 * ============================================================
 */

async function loadComponent(
    elementId,
    filePath
) {

    const container =
        document.getElementById(
            elementId
        );


    if (!container) {

        console.error(
            "Không tìm thấy container:",
            elementId
        );

        return false;
    }


    try {

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
                `HTTP ${response.status} - ${response.statusText}`
            );
        }


        const html =
            await response.text();


        if (!html.trim()) {

            throw new Error(
                `Component ${filePath} trả về HTML rỗng.`
            );
        }


        container.innerHTML =
            html;


        console.log(
            `Loaded component: ${filePath}`
        );


        return true;

    } catch (error) {

        console.error(
            `Không thể load component ${filePath}:`,
            error
        );

        return false;
    }
}


/* ============================================================
 * LOAD CREATE OPTIONS
 *
 * GET /api/v1/employees/create-options
 * ============================================================
 */

async function loadCreateOptions() {

    console.log(
        "Loading employee create options..."
    );


    try {

        if (
            typeof getEmployeeCreateOptions !==
            "function"
        ) {

            throw new Error(
                "getEmployeeCreateOptions() chưa tồn tại."
            );
        }


        const response =
            await getEmployeeCreateOptions();


        console.log(
            "CREATE OPTIONS RESPONSE:",
            response
        );


        const data =
            response?.data;


        if (!data) {

            throw new Error(
                "Server không trả về dữ liệu tạo Employee."
            );
        }


        /* ========================================================
         * USERS
         * ========================================================
         */

        const users =
            Array.isArray(
                data.users
            )
                ? data.users
                : [];


        const userSelect =
            document.getElementById(
                "userId"
            );


        if (userSelect) {

            userSelect.disabled =
                false;


            userSelect.innerHTML =
                `
                <option value="">
                    -- Chọn tài khoản đăng nhập --
                </option>
                `;


            users.forEach(
                function (user) {

                    const option =
                        document.createElement(
                            "option"
                        );


                    option.value =
                        user.id ?? "";


                    option.textContent =
                        `${user.username || ""} - ${formatRoleName(user.roleName)}`;


                    userSelect.appendChild(
                        option
                    );

                }
            );


            if (
                users.length ===
                0
            ) {

                userSelect.innerHTML =
                    `
                    <option value="">
                        -- Không có tài khoản phù hợp --
                    </option>
                    `;


                userSelect.disabled =
                    true;


                console.warn(
                    "Không có User nào có thể liên kết với Employee."
                );
            }
        }


        /* ========================================================
         * DEPARTMENTS
         * ========================================================
         */

        const departments =
            Array.isArray(
                data.departments
            )
                ? data.departments
                : [];


        const departmentSelect =
            document.getElementById(
                "departmentId"
            );


        if (departmentSelect) {

            departmentSelect.disabled =
                false;


            departmentSelect.innerHTML =
                `
                <option value="">
                    -- Chọn phòng ban --
                </option>
                `;


            departments.forEach(
                function (department) {

                    const option =
                        document.createElement(
                            "option"
                        );


                    option.value =
                        department.id ?? "";


                    option.textContent =
                        department.name ||
                        department.code ||
                        "";


                    departmentSelect.appendChild(
                        option
                    );

                }
            );


            if (
                departments.length ===
                0
            ) {

                departmentSelect.innerHTML =
                    `
                    <option value="">
                        -- Không có phòng ban --
                    </option>
                    `;


                departmentSelect.disabled =
                    true;


                console.warn(
                    "Không có phòng ban nào."
                );
            }
        }


        /*
         * Xóa lỗi trước đó nếu API thành công.
         */

        clearMessages();


        console.log(
            "Employee create options loaded successfully."
        );


        return true;

    } catch (error) {

        console.error(
            "LOAD CREATE OPTIONS ERROR:",
            error
        );


        showError(
            error.message ||
            "Không thể tải dữ liệu cho biểu mẫu thêm nhân viên."
        );


        /*
         * QUAN TRỌNG:
         *
         * Không throw lại.
         *
         * Như vậy API lỗi sẽ KHÔNG làm chết:
         * - Sidebar
         * - Hamburger
         * - Form
         */

        return false;
    }
}


/* ============================================================
 * FORM SUBMIT
 * ============================================================
 */

function initializeForm() {

    const form =
        document.getElementById(
            "employeeAddForm"
        );


    if (!form) {

        throw new Error(
            "Không tìm thấy #employeeAddForm."
        );
    }


    /*
     * Tránh gắn submit nhiều lần.
     */

    if (
        form.dataset.smcvEmployeeAddForm ===
        "true"
    ) {

        return;
    }


    form.dataset.smcvEmployeeAddForm =
        "true";


    form.addEventListener(
        "submit",
        handleSubmit
    );
}


/* ============================================================
 * HANDLE SUBMIT
 * ============================================================
 */

async function handleSubmit(
    event
) {

    event.preventDefault();


    clearMessages();


    const saveButton =
        document.getElementById(
            "saveButton"
        );


    try {

        /* ========================================================
         * GET VALUES
         * ========================================================
         */

        const userId =
            document
                .getElementById("userId")
                ?.value ||
            "";


        const employeeCode =
            document
                .getElementById("employeeCode")
                ?.value
                .trim() ||
            "";


        const fullName =
            document
                .getElementById("fullName")
                ?.value
                .trim() ||
            "";


        const email =
            document
                .getElementById("email")
                ?.value
                .trim() ||
            "";


        const phone =
            document
                .getElementById("phone")
                ?.value
                .trim() ||
            "";


        const departmentId =
            document
                .getElementById("departmentId")
                ?.value ||
            "";


        const position =
            document
                .getElementById("position")
                ?.value ||
            "";


        const jobTitle =
            document
                .getElementById("jobTitle")
                ?.value
                .trim() ||
            "";


        const joinDate =
            document
                .getElementById("joinDate")
                ?.value ||
            "";


        const status =
            document
                .getElementById("status")
                ?.value ||
            "";


        /* ========================================================
         * VALIDATION
         * ========================================================
         */

        if (!userId) {

            showError(
                "Vui lòng chọn tài khoản đăng nhập."
            );


            document
                .getElementById("userId")
                ?.focus();


            return;
        }


        if (!employeeCode) {

            showError(
                "Vui lòng nhập mã nhân viên."
            );


            document
                .getElementById("employeeCode")
                ?.focus();


            return;
        }


        if (!fullName) {

            showError(
                "Vui lòng nhập họ và tên."
            );


            document
                .getElementById("fullName")
                ?.focus();


            return;
        }


        if (
            email &&
            !isValidEmail(email)
        ) {

            showError(
                "Email không đúng định dạng."
            );


            document
                .getElementById("email")
                ?.focus();


            return;
        }


        if (!departmentId) {

            showError(
                "Vui lòng chọn phòng ban."
            );


            document
                .getElementById("departmentId")
                ?.focus();


            return;
        }


        if (!position) {

            showError(
                "Vui lòng chọn chức vụ."
            );


            document
                .getElementById("position")
                ?.focus();


            return;
        }


        if (!jobTitle) {

            showError(
                "Vui lòng nhập chức danh."
            );


            document
                .getElementById("jobTitle")
                ?.focus();


            return;
        }


        if (!joinDate) {

            showError(
                "Vui lòng chọn ngày vào làm."
            );


            document
                .getElementById("joinDate")
                ?.focus();


            return;
        }


        if (!status) {

            showError(
                "Vui lòng chọn trạng thái."
            );


            document
                .getElementById("status")
                ?.focus();


            return;
        }


        /* ========================================================
         * REQUEST BODY
         * ========================================================
         */

        const data = {

            userId:
                Number(userId),

            employeeCode:
                employeeCode,

            fullName:
                fullName,

            email:
                email ||
                null,

            phone:
                phone ||
                null,

            departmentId:
                Number(departmentId),

            position:
                position ||
                null,

            jobTitle:
                jobTitle ||
                null,

            joinDate:
                joinDate ||
                null,

            status:
                status ||
                "ACTIVE"
        };


        console.log(
            "CREATE EMPLOYEE REQUEST:",
            data
        );


        /* ========================================================
         * SAVE BUTTON
         * ========================================================
         */

        if (saveButton) {

            saveButton.disabled =
                true;


            saveButton.innerHTML =
                `
                <i class="fa fa-spinner fa-spin"></i>
                <span>Đang lưu...</span>
                `;
        }


        /* ========================================================
         * POST /api/v1/employees
         * ========================================================
         */

        const response =
            await createEmployee(
                data
            );


        console.log(
            "CREATE EMPLOYEE RESPONSE:",
            response
        );


        /* ========================================================
         * SUCCESS
         * ========================================================
         */

        showSuccess(
            "Thêm nhân viên thành công."
        );


        setTimeout(
            function () {

                window.location.href =
                    "./index.html";

            },
            1000
        );


    } catch (error) {

        console.error(
            "CREATE EMPLOYEE ERROR:",
            error
        );


        showError(
            error.message ||
            "Không thể thêm nhân viên."
        );


    } finally {

        if (saveButton) {

            saveButton.disabled =
                false;


            saveButton.innerHTML =
                `
                <i class="fa fa-save"></i>
                <span>Lưu nhân viên</span>
                `;
        }
    }
}


/* ============================================================
 * RESET BUTTON
 * ============================================================
 */

function initializeResetButton() {

    const form =
        document.getElementById(
            "employeeAddForm"
        );


    if (!form) {

        return;
    }


    if (
        form.dataset.smcvEmployeeAddReset ===
        "true"
    ) {

        return;
    }


    form.dataset.smcvEmployeeAddReset =
        "true";


    form.addEventListener(
        "reset",
        function () {

            setTimeout(
                function () {

                    clearMessages();

                },
                0
            );
        }
    );
}


/* ============================================================
 * EMAIL VALIDATION
 * ============================================================
 */

function isValidEmail(
    email
) {

    const pattern =
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


    return pattern.test(
        email
    );
}


/* ============================================================
 * ROLE DISPLAY
 * ============================================================
 */

function formatRoleName(
    roleName
) {

    switch (roleName) {

        case "TECH_LEAD":

            return "TECH LEAD";


        case "HR":

            return "HR";


        case "EMPLOYEE":

            return "EMPLOYEE";


        case "ADMIN":

            return "ADMIN";


        default:

            return roleName ||
                "";
    }
}


/* ============================================================
 * ERROR MESSAGE
 * ============================================================
 */

function showError(
    message
) {

    const errorBox =
        document.getElementById(
            "errorMessage"
        );


    const errorText =
        document.getElementById(
            "errorMessageText"
        );


    const successBox =
        document.getElementById(
            "successMessage"
        );


    if (successBox) {

        successBox.style.display =
            "none";
    }


    if (errorText) {

        errorText.textContent =
            message;
    }


    if (errorBox) {

        errorBox.style.display =
            "block";
    }
}


/* ============================================================
 * SUCCESS MESSAGE
 * ============================================================
 */

function showSuccess(
    message
) {

    const successBox =
        document.getElementById(
            "successMessage"
        );


    const successText =
        document.getElementById(
            "successMessageText"
        );


    const errorBox =
        document.getElementById(
            "errorMessage"
        );


    if (errorBox) {

        errorBox.style.display =
            "none";
    }


    if (successText) {

        successText.textContent =
            message;
    }


    if (successBox) {

        successBox.style.display =
            "block";
    }
}


/* ============================================================
 * CLEAR MESSAGES
 * ============================================================
 */

function clearMessages() {

    const errorBox =
        document.getElementById(
            "errorMessage"
        );


    const successBox =
        document.getElementById(
            "successMessage"
        );


    if (errorBox) {

        errorBox.style.display =
            "none";
    }


    if (successBox) {

        successBox.style.display =
            "none";
    }
}

/* ============================================================
 * APPLY ROLE TO SIDEBAR - EMPLOYEE ADD PAGE
 * ============================================================ */

function applyEmployeeAddSidebarRole() {

    console.log(
        "=== APPLY EMPLOYEE ADD SIDEBAR ROLE ==="
    );


    /* ========================================================
     * 1. SESSION ROLES
     * ======================================================== */

    const rolesJson =
        sessionStorage.getItem(
            "roles"
        );


    if (!rolesJson) {

        console.warn(
            "Employee Add: không tìm thấy roles."
        );

        return;
    }


    let roles = [];


    try {

        roles =
            JSON.parse(
                rolesJson
            );

    } catch (error) {

        console.error(
            "Employee Add: không parse được roles.",
            error
        );

        return;
    }


    if (
        !Array.isArray(roles) ||
        roles.length === 0
    ) {

        console.warn(
            "Employee Add: roles không hợp lệ."
        );

        return;
    }


    /* ========================================================
     * 2. NORMALIZE ROLE
     * ======================================================== */

    const validRoles = [
        "ADMIN",
        "HR",
        "TECH_LEAD",
        "EMPLOYEE"
    ];


    let currentRole =
        "";


    for (
        const item of roles
    ) {

        const normalized =
            String(
                item || ""
            )
                .replace(
                    /^ROLE_/i,
                    ""
                )
                .trim()
                .toUpperCase();


        if (
            validRoles.includes(
                normalized
            )
        ) {

            currentRole =
                normalized;

            break;
        }
    }


    if (!currentRole) {

        console.warn(
            "Employee Add: không xác định được role."
        );

        return;
    }


    console.log(
        "EMPLOYEE ADD CURRENT ROLE =",
        currentRole
    );


    /* ========================================================
     * 3. BODY ROLE
     * ======================================================== */

    document.body.dataset.currentRole =
        currentRole;


    /* ========================================================
     * 4. FILTER ALL ROLE MENUS
     * ======================================================== */

    const roleElements =
        document.querySelectorAll(
            "#nav-accordion [data-roles]"
        );


    roleElements.forEach(
        function (element) {

            const rolesAttribute =
                element.dataset.roles ||
                "";


            const allowedRoles =
                rolesAttribute
                    .split(",")
                    .map(
                        function (role) {

                            return String(
                                role
                            )
                                .replace(
                                    /^ROLE_/i,
                                    ""
                                )
                                .trim()
                                .toUpperCase();

                        }
                    )
                    .filter(
                        Boolean
                    );


            const allowed =
                allowedRoles.includes(
                    currentRole
                );


            if (allowed) {

                element.style.display =
                    "";

                return;
            }


            element.style.display =
                "none";


            element.classList.remove(
                "active",
                "dcjq-parent",
                "dcjq-current-parent"
            );


            const submenu =
                element.querySelector(
                    ":scope > ul.sub"
                );


            if (submenu) {

                submenu.style.display =
                    "none";
            }

        }
    );


    /* ========================================================
     * 5. LOG
     * ======================================================== */

    console.log(
        "EMPLOYEE ADD SIDEBAR ROLE APPLIED =",
        currentRole
    );

}