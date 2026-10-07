/* ============================================================
   SYSTEMANAGERCV
   EMPLOYEE INDEX PAGE

   File:
   pages/employee/index.js

   CHỈ FILE NÀY xử lý:
   - Header
   - Sidebar
   - Footer
   - Search
   - Employee API
   - Render table
   - Pagination
   - Delete

   KHÔNG dùng employee.js
   KHÔNG dùng user-page-layout.js
   ============================================================ */


"use strict";


/* ============================================================
   GLOBAL STATE
   ============================================================ */

let currentPage = 0;

const pageSize = 10;

let currentKeyword = "";

let totalPages = 1;


/* ============================================================
   DOM READY
   ============================================================ */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        console.log(
            "=========================================="
        );

        console.log(
            "EMPLOYEE INDEX PAGE START"
        );

        console.log(
            "=========================================="
        );


        try {

            /*
             * 1. Load Header
             */

            await loadHeader();


            /*
             * 2. Load Sidebar
             */

            await loadSidebar();


            /*
             * 3. Initialize Sidebar
             */

            initializeSidebar();


            /*
             * 4. Initialize Logout
             */

            initializeLogout();


            /*
             * 5. Load Footer
             */

            await loadFooter();


            /*
             * 6. Initialize Employee Events
             */

            initializeEmployeeEvents();


            /*
             * 7. Load Employee Data
             */

            await loadEmployees();


        } catch (error) {

            console.error(
                "EMPLOYEE INDEX INITIALIZATION ERROR:",
                error
            );

            showEmployeeError(
                error.message ||
                "Không thể khởi tạo trang nhân viên."
            );

        }

    }
);


/* ============================================================
   LOAD COMPONENT
   ============================================================ */

async function loadComponent(
    containerId,
    url
) {

    const container =
        document.getElementById(
            containerId
        );


    if (!container) {

        console.error(
            "Không tìm thấy container:",
            containerId
        );

        return;

    }


    try {

        const response =
            await fetch(url);


        if (!response.ok) {

            throw new Error(
                `Không thể tải ${url}. HTTP ${response.status}`
            );

        }


        const html =
            await response.text();


        container.innerHTML =
            html;


        console.log(
            `${url} -> component loaded`
        );


    } catch (error) {

        console.error(
            `LOAD COMPONENT ERROR: ${url}`,
            error
        );

        throw error;

    }

}


/* ============================================================
   LOAD HEADER
   ============================================================ */

async function loadHeader() {

    await loadComponent(
        "header-container",
        "../../components/header.html"
    );


    /*
     * Nếu scripts.js của hệ thống có
     * initializeHeaderUser()
     * thì gọi lại sau khi Header được insert.
     */

    if (
        typeof initializeHeaderUser ===
        "function"
    ) {

        initializeHeaderUser();

    }

}


/* ============================================================
   LOAD SIDEBAR
   ============================================================ */

async function loadSidebar() {

    await loadComponent(
        "sidebar-container",
        "../../components/sidebar.html"
    );


    applyRoleToSidebar();

}


/* ============================================================
   LOAD FOOTER
   ============================================================ */

async function loadFooter() {

    await loadComponent(
        "footer-container",
        "../../components/footer.html"
    );

}

/* ============================================================
   APPLY ROLE TO SIDEBAR
   ------------------------------------------------------------
   Nhiệm vụ:
   - ADMIN:
       Dashboard
       Quản lý User
       Quản lý phòng ban
       Quản lý nhân viên
       Quản lý CV
       Thông báo
       Đăng xuất

   - HR:
       Dashboard
       Quản lý nhân viên
       Quản lý CV
       Yêu cầu cập nhật CV
       Thông báo
       Đăng xuất

   - TECH_LEAD:
       Dashboard
       CV của tôi
       CV cần kiểm duyệt
       Thông báo
       Đăng xuất

   - EMPLOYEE:
       Dashboard
       CV của tôi
       Thông báo
       Đăng xuất

   Sidebar dùng data-roles làm nguồn cấu hình.
   ============================================================ */

function applyRoleToSidebar() {

    console.log(
        "=== APPLY EMPLOYEE PAGE SIDEBAR ROLE ==="
    );


    /* ========================================================
       1. LẤY ROLE TỪ SESSION
       ======================================================== */

    const rolesJson =
        sessionStorage.getItem(
            "roles"
        );


    if (!rolesJson) {

        console.warn(
            "Không tìm thấy roles trong sessionStorage."
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
            "Không thể parse roles:",
            error
        );

        return;
    }


    if (
        !Array.isArray(roles) ||
        roles.length === 0
    ) {

        console.warn(
            "Roles không hợp lệ:",
            roles
        );

        return;
    }


    /* ========================================================
       2. CHUẨN HÓA ROLE
       ======================================================== */

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
            "Không xác định được Role hiện tại."
        );

        return;
    }


    console.log(
        "CURRENT ROLE =",
        currentRole
    );


    /* ========================================================
       3. LƯU ROLE LÊN BODY
       ======================================================== */

    if (
        document.body
    ) {

        document.body.dataset.currentRole =
            currentRole;
    }


    /* ========================================================
       4. LỌC TẤT CẢ MENU CÓ data-roles
       ======================================================== */

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


            const isAllowed =
                allowedRoles.includes(
                    currentRole
                );


            /*
             * ĐƯỢC PHÉP:
             */

            if (isAllowed) {

                element.style.display =
                    "";

                return;
            }


            /*
             * KHÔNG ĐƯỢC PHÉP:
             */

            element.style.display =
                "none";


            /*
             * Đóng submenu nếu menu này
             * đang mở.
             */

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
       5. ĐẢM BẢO MENU CHA ĐÚNG ROLE
       ======================================================== */

    const parentMenus =
        document.querySelectorAll(
            "#nav-accordion > li.sub-menu"
        );


    parentMenus.forEach(
        function (menu) {

            const roleAttribute =
                menu.dataset.roles ||
                "";


            /*
             * Menu cha đã bị ẩn ở bước trên
             * thì giữ nguyên.
             */

            if (
                menu.style.display ===
                "none"
            ) {

                return;
            }


            /*
             * Nếu menu cha có data-roles
             * thì không cần xử lý tiếp.
             */

            if (
                roleAttribute
            ) {

                return;
            }


            /*
             * Trường hợp menu cha không có
             * data-roles nhưng toàn bộ menu con
             * đều bị ẩn thì cũng ẩn menu cha.
             */

            const visibleChildren =
                menu.querySelectorAll(
                    ":scope > ul.sub > li:not([style*='display: none'])"
                );


            if (
                visibleChildren.length ===
                0
            ) {

                menu.style.display =
                    "none";
            }

        }
    );


    /* ========================================================
       6. LOG
       ======================================================== */

    console.log(
        "SIDEBAR ROLE APPLIED =",
        currentRole
    );

}


/* ============================================================
   INITIALIZE SIDEBAR
   ============================================================ */

function initializeSidebar() {

    console.log(
        "=== INITIALIZE SIDEBAR ==="
    );


    if (
        typeof jQuery ===
        "undefined"
    ) {

        console.warn(
            "jQuery chưa được load."
        );

        return;

    }


    const sidebar =
        jQuery(
            "#sidebar-container"
        );


    const mainContent =
        jQuery(
            "#main-content"
        );


    /*
     * ========================================================
     * 1. DC ACCORDION
     *
     * Sidebar được load bằng fetch()
     * nên Accordion phải được khởi tạo
     * sau khi sidebar.html được insert.
     * ========================================================
     */

    const navAccordion =
        jQuery(
            "#nav-accordion"
        );


    if (
        navAccordion.length > 0 &&
        typeof jQuery.fn.dcAccordion ===
        "function"
    ) {

        navAccordion.dcAccordion({

            /*
             * Click để mở / đóng submenu
             */
            eventType:
                "click",

            /*
             * Chỉ mở một menu tại một thời điểm
             */
            autoClose:
                true,

            /*
             * Không lưu trạng thái menu
             * để mỗi lần tải trang submenu
             * trở về trạng thái đóng.
             */
            saveState:
                false,

            /*
             * Menu cha không chuyển trang
             */
            disableLink:
                true,

            /*
             * Dùng tốc độ của hệ thống.
             *
             * "slow" cho hiệu ứng trượt
             * chậm và mượt hơn "fast".
             */
            speed:
                "slow",

            showCount:
                false,

            /*
             * Không tự động mở toàn bộ submenu
             */
            autoExpand:
                false,

            classExpand:
                "dcjq-current-parent"

        });


        console.log(
            "Sidebar: DC Accordion đã được khởi tạo."
        );

    } else {

        console.warn(
            "Không tìm thấy #nav-accordion hoặc dcAccordion plugin."
        );

    }


    /*
     * ========================================================
     * 2. HAMBURGER
     *
     * Thu gọn / mở toàn bộ Sidebar.
     * ========================================================
     */

    const toggle =
        jQuery(
            "#header-container .sidebar-toggle-box .fa-bars"
        );


    if (
        !toggle.length
    ) {

        console.warn(
            "Không tìm thấy hamburger."
        );

        return;

    }


    toggle.off(
        "click.employeeIndex"
    );


    toggle.on(
        "click.employeeIndex",
        function () {

            sidebar.toggleClass(
                "hide-left-bar"
            );


            mainContent.toggleClass(
                "merge-left"
            );

        }
    );


    console.log(
        "Sidebar hamburger đã được khởi tạo."
    );

}


/* ============================================================
   INITIALIZE LOGOUT
   ============================================================ */

function initializeLogout() {

    const sidebarLogout =
        document.getElementById(
            "sidebar-logout-button"
        );


    if (sidebarLogout) {

        sidebarLogout.addEventListener(
            "click",
            handleLogout
        );

    }


    const headerLogout =
        document.getElementById(
            "logout-button"
        );


    if (headerLogout) {

        headerLogout.addEventListener(
            "click",
            handleLogout
        );

    }

}


/* ============================================================
   HANDLE LOGOUT
   ============================================================ */

function handleLogout(event) {

    event.preventDefault();


    sessionStorage.clear();


    localStorage.removeItem(
        "accessToken"
    );

    localStorage.removeItem(
        "username"
    );

    localStorage.removeItem(
        "roles"
    );


    window.location.href =
        "../login/login.html";

}


/* ============================================================
   INITIALIZE EMPLOYEE EVENTS
   ============================================================ */

function initializeEmployeeEvents() {


    /*
     * SEARCH
     */

    const searchForm =
        document.getElementById(
            "employee-search-form"
        );


    if (searchForm) {

        searchForm.addEventListener(
            "submit",
            function (event) {

                event.preventDefault();


                const input =
                    document.getElementById(
                        "employee-keyword"
                    );


                currentKeyword =
                    input
                        ? input.value.trim()
                        : "";


                currentPage = 0;


                loadEmployees();

            }
        );

    }


    /*
     * REFRESH
     */

    const refreshButton =
        document.getElementById(
            "employee-refresh"
        );


    if (refreshButton) {

        refreshButton.addEventListener(
            "click",
            function () {

                const input =
                    document.getElementById(
                        "employee-keyword"
                    );


                if (input) {

                    input.value = "";

                }


                currentKeyword = "";

                currentPage = 0;


                loadEmployees();

            }
        );

    }


    /*
     * PREVIOUS
     */

    const previousButton =
        document.getElementById(
            "employee-prev"
        );


    if (previousButton) {

        previousButton.addEventListener(
            "click",
            function () {

                if (
                    currentPage > 0
                ) {

                    currentPage--;

                    loadEmployees();

                }

            }
        );

    }


    /*
     * NEXT
     */

    const nextButton =
        document.getElementById(
            "employee-next"
        );


    if (nextButton) {

        nextButton.addEventListener(
            "click",
            function () {

                if (
                    currentPage <
                    totalPages - 1
                ) {

                    currentPage++;

                    loadEmployees();

                }

            }
        );

    }


    /*
     * TABLE DELETE
     */

    const tableBody =
        document.getElementById(
            "employee-table-body"
        );


    if (tableBody) {

        tableBody.addEventListener(
            "click",
            handleEmployeeTableClick
        );

    }

}


/* ============================================================
   LOAD EMPLOYEES
   ============================================================ */

async function loadEmployees() {

    hideEmployeeMessages();


    const tableBody =
        document.getElementById(
            "employee-table-body"
        );


    if (tableBody) {

        tableBody.innerHTML = `
            <tr>
                <td
                    colspan="10"
                    class="employee-loading"
                >
                    Đang tải dữ liệu...
                </td>
            </tr>
        `;

    }


    try {

        if (
            typeof getEmployees !==
            "function"
        ) {

            throw new Error(
                "Không tìm thấy getEmployees(). Kiểm tra employeeApi.js."
            );

        }


        console.log(
            "GET EMPLOYEES:",
            {
                page: currentPage,
                size: pageSize,
                keyword: currentKeyword
            }
        );


        const response =
            await getEmployees({

                page:
                    currentPage,

                size:
                    pageSize,

                keyword:
                    currentKeyword

            });


        console.log(
            "EMPLOYEE API RESPONSE:",
            response
        );


        const pageData =
            response?.data;


        if (!pageData) {

            throw new Error(
                "API không trả về data."
            );

        }


        renderEmployees(
            pageData
        );


        updatePagination(
            pageData
        );


    } catch (error) {

        console.error(
            "LOAD EMPLOYEES ERROR:",
            error
        );


        if (tableBody) {

            tableBody.innerHTML =
                "";

        }


        showEmployeeError(
            error.message ||
            "Không thể tải danh sách nhân viên."
        );

    }

}


/* ============================================================
   RENDER EMPLOYEES
   ============================================================ */

function renderEmployees(
    pageData
) {

    const tableBody =
        document.getElementById(
            "employee-table-body"
        );


    const empty =
        document.getElementById(
            "employee-empty"
        );


    const employees =
        Array.isArray(
            pageData.content
        )
            ? pageData.content
            : [];


    /*
     * TOTAL
     */

    const totalElement =
        document.getElementById(
            "employee-total"
        );


    if (totalElement) {

        totalElement.textContent =
            pageData.totalElements ??
            employees.length;

    }


    /*
     * EMPTY
     */

    if (
        employees.length === 0
    ) {

        if (tableBody) {

            tableBody.innerHTML =
                "";

        }


        if (empty) {

            empty.style.display =
                "flex";

        }


        return;

    }


    if (empty) {

        empty.style.display =
            "none";

    }


    if (!tableBody) {

        return;

    }


    tableBody.innerHTML =
        "";


    employees.forEach(
        function (
            employee,
            index
        ) {

            const row =
                createEmployeeRow(
                    employee,
                    index
                );


            tableBody.appendChild(
                row
            );

        }
    );

}


/* ============================================================
   CREATE EMPLOYEE ROW
   ============================================================ */

function createEmployeeRow(
    employee,
    index
) {

    const row =
        document.createElement(
            "tr"
        );


    /*
     * STT
     */

    const stt =
        currentPage *
            pageSize +
        index +
        1;


    /*
     * DATA
     */

    const employeeId =
        employee.id ??
        "";


    const employeeCode =
        employee.employeeCode ??
        employee.code ??
        "---";


    const fullName =
        employee.fullName ??
        "---";


    const email =
        employee.email ??
        "---";


    const phone =
        employee.phone ??
        "---";


    const department =
        employee.departmentName ??
        employee.departmentCode ??
        "---";


    const position =
        employee.positionDescription ??
        employee.position ??
        "---";


    const jobTitle =
        employee.jobTitle ??
        "---";


    const status =
        employee.status ??
        "---";


    const statusText =
        employee.statusDescription ??
        (
            status === "ACTIVE"
                ? "Hoạt động"
                : status
        );


    row.innerHTML = `

        <td class="employee-cell-stt">

            <span class="employee-stt">
                ${stt}
            </span>

        </td>


        <td>

            <span
                class="employee-code"
                title="${escapeHtml(employeeCode)}"
            >
                ${escapeHtml(employeeCode)}
            </span>

        </td>


        <td>

            <div class="employee-name">

                <span class="employee-name-icon">

                    <i class="fa fa-user"></i>

                </span>

                <span
                    class="employee-name-text"
                    title="${escapeHtml(fullName)}"
                >
                    ${escapeHtml(fullName)}
                </span>

            </div>

        </td>


        <td>

            <span
                class="employee-text"
                title="${escapeHtml(email)}"
            >
                ${escapeHtml(email)}
            </span>

        </td>


        <td>

            <span
                class="employee-text"
                title="${escapeHtml(phone)}"
            >
                ${escapeHtml(phone)}
            </span>

        </td>


        <td>

            <span
                class="employee-text"
                title="${escapeHtml(department)}"
            >
                ${escapeHtml(department)}
            </span>

        </td>


        <td>

            <span
                class="employee-text"
                title="${escapeHtml(position)}"
            >
                ${escapeHtml(position)}
            </span>

        </td>


        <td>

            <span
                class="employee-text"
                title="${escapeHtml(jobTitle)}"
            >
                ${escapeHtml(jobTitle)}
            </span>

        </td>


        <td>

            <span
                class="employee-status
                ${
                    status === "ACTIVE"
                        ? "employee-status-active"
                        : "employee-status-inactive"
                }"
            >

                <i class="fa fa-check-circle"></i>

                ${escapeHtml(statusText)}

            </span>

        </td>


        <td>

            <div class="employee-actions">


                <a
                    href="./edit.html?id=${encodeURIComponent(employeeId)}"
                    class="employee-action employee-action-edit"
                    title="Sửa nhân viên"
                >

                    <i class="fa fa-pencil"></i>

                    <span>
                        Sửa
                    </span>

                </a>


                <a
                    href="#"
                    class="employee-action employee-action-delete"
                    data-employee-id="${escapeHtml(employeeId)}"
                    title="Xóa nhân viên"
                >

                    <i class="fa fa-trash"></i>

                    <span>
                        Xóa
                    </span>

                </a>


            </div>

        </td>

    `;


    return row;

}


/* ============================================================
   DELETE
   ============================================================ */

async function handleEmployeeTableClick(
    event
) {

    const deleteButton =
        event.target.closest(
            ".employee-action-delete"
        );


    if (!deleteButton) {

        return;

    }


    event.preventDefault();


    const employeeId =
        deleteButton.dataset.employeeId;


    if (!employeeId) {

        return;

    }


    const confirmed =
        window.confirm(
            "Bạn có chắc chắn muốn xóa nhân viên này không?"
        );


    if (!confirmed) {

        return;

    }


    try {

        if (
            typeof deleteEmployee !==
            "function"
        ) {

            throw new Error(
                "Không tìm thấy deleteEmployee()."
            );

        }


        await deleteEmployee(
            employeeId
        );


        showEmployeeSuccess(
            "Xóa nhân viên thành công."
        );


        await loadEmployees();


    } catch (error) {

        console.error(
            "DELETE EMPLOYEE ERROR:",
            error
        );


        showEmployeeError(
            error.message ||
            "Không thể xóa nhân viên."
        );

    }

}


/* ============================================================
   PAGINATION
   ============================================================ */

function updatePagination(
    pageData
) {

    currentPage =
        pageData.number ??
        0;


    totalPages =
        pageData.totalPages ||
        1;


    /*
     * CURRENT
     */

    const currentPageElement =
        document.getElementById(
            "employee-current-page"
        );


    if (currentPageElement) {

        currentPageElement.textContent =
            currentPage + 1;

    }


    /*
     * TOTAL
     */

    const totalPagesElement =
        document.getElementById(
            "employee-total-pages"
        );


    if (totalPagesElement) {

        totalPagesElement.textContent =
            totalPages;

    }


    /*
     * PREVIOUS
     */

    const previousButton =
        document.getElementById(
            "employee-prev"
        );


    if (previousButton) {

        previousButton.disabled =
            currentPage <= 0;

    }


    /*
     * NEXT
     */

    const nextButton =
        document.getElementById(
            "employee-next"
        );


    if (nextButton) {

        nextButton.disabled =
            currentPage >=
            totalPages - 1;

    }


    renderPageNumbers();

}


/* ============================================================
   PAGE NUMBERS
   ============================================================ */

function renderPageNumbers() {

    const container =
        document.getElementById(
            "employee-page-numbers"
        );


    if (!container) {

        return;

    }


    container.innerHTML =
        "";


    for (
        let i = 0;
        i < totalPages;
        i++
    ) {

        const button =
            document.createElement(
                "button"
            );


        button.type =
            "button";


        button.className =
            "employee-pagination-button";


        button.textContent =
            i + 1;


        if (
            i === currentPage
        ) {

            button.classList.add(
                "employee-pagination-active"
            );

        }


        button.addEventListener(
            "click",
            function () {

                currentPage =
                    i;


                loadEmployees();

            }
        );


        container.appendChild(
            button
        );

    }

}


/* ============================================================
   MESSAGE
   ============================================================ */

function hideEmployeeMessages() {

    const error =
        document.getElementById(
            "employee-error"
        );


    const success =
        document.getElementById(
            "employee-success"
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


/* ============================================================
   ERROR
   ============================================================ */

function showEmployeeError(
    message
) {

    const element =
        document.getElementById(
            "employee-error"
        );


    if (!element) {

        return;

    }


    element.textContent =
        message;


    element.style.display =
        "block";

}


/* ============================================================
   SUCCESS
   ============================================================ */

function showEmployeeSuccess(
    message
) {

    const element =
        document.getElementById(
            "employee-success"
        );


    if (!element) {

        return;

    }


    element.textContent =
        message;


    element.style.display =
        "block";


    setTimeout(
        function () {

            element.style.display =
                "none";

        },
        2500
    );

}


/* ============================================================
   ESCAPE HTML
   ============================================================ */

function escapeHtml(
    value
) {

    return String(
        value ?? ""
    )
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );

}