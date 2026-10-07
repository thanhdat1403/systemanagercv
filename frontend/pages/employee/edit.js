/*
 * ================================================================
 * SYSTEMANAGERCV
 * EMPLOYEE EDIT PAGE
 * ================================================================
 */


/*
 * ================================================================
 * GLOBAL VARIABLES
 * ================================================================
 */

let employeeId = null;

let originalEmployee = null;

let originalOptions = null;


/*
 * ================================================================
 * PAGE INITIALIZATION
 * ================================================================
 */

document.addEventListener(
    "DOMContentLoaded",
    async function () {


        console.log(
            "================================="
        );

        console.log(
            "EMPLOYEE EDIT PAGE START"
        );

        console.log(
            "================================="
        );


        /*
         * ========================================================
         * 1. GET EMPLOYEE ID
         * ========================================================
         */

        const urlParams =
            new URLSearchParams(
                window.location.search
            );


        employeeId =
            urlParams.get("id");


        console.log(
            "Employee ID =",
            employeeId
        );


        if (!employeeId) {

            showError(
                "Không tìm thấy ID nhân viên."
            );

            disableForm();

            return;

        }


        /*
         * ========================================================
         * 2. LOAD HEADER
         * ========================================================
         */

        await loadComponent(
            "header-container",
            "../../components/header.html"
        );


        /*
         * ========================================================
         * 3. LOAD SIDEBAR
         * ========================================================
         */

        await loadComponent(
            "sidebar-container",
            "../../components/sidebar.html"
        );


        /*
         * ========================================================
         * 4. LOAD FOOTER
         * ========================================================
         */

        await loadComponent(
            "footer-container",
            "../../components/footer.html"
        );


        /*
         * ========================================================
         * 5. INITIALIZE HEADER USER
         * ========================================================
         */

        if (
            typeof initializeHeaderUser ===
            "function"
        ) {

            initializeHeaderUser();

        } else {

            console.warn(
                "initializeHeaderUser() không tồn tại."
            );

        }


        /*
         * ========================================================
         * 6. INITIALIZE SIDEBAR
         * ========================================================
         */

        if (
            typeof initializeSidebar ===
            "function"
        ) {

            initializeSidebar();

        } else {

            console.error(
                "initializeSidebar() không tồn tại."
            );

        }


        /*
         * ========================================================
         * 7. INITIALIZE HAMBURGER
         * ========================================================
         *
         * Đây là phần còn thiếu ở file Edit trước.
         *
         * Dashboard hiện tại của project cũng dùng chính
         * cơ chế này.
         *
         * - #sidebar
         * - #main-content
         * - .hide-left-bar
         * - .merge-left
         *
         */

        initializeSidebarToggle();


        /*
         * ========================================================
         * 8. LOAD EMPLOYEE DATA
         * ========================================================
         */

        try {


            showLoading();


            const result =
                await getEmployeeById(
                    employeeId
                );


            console.log(
                "GET EMPLOYEE DETAIL:",
                result
            );


            const employee =
                result.data;


            if (!employee) {

                throw new Error(
                    "Không tìm thấy dữ liệu nhân viên."
                );

            }


            /*
             * Lưu snapshot.
             */

            originalEmployee =
                createEmployeeSnapshot(
                    employee
                );


            /*
             * Đổ dữ liệu vào form.
             */

            fillEmployeeForm(
                employee
            );


            /*
             * ====================================================
             * LOAD EDIT OPTIONS
             * ====================================================
             */

            await loadEditOptions(
                employeeId
            );


            hideLoading();


        } catch (error) {


            console.error(
                "LOAD EMPLOYEE ERROR:",
                error
            );


            hideLoading();


            showError(
                error.message ||
                "Không thể tải thông tin nhân viên."
            );

        }


        /*
         * ========================================================
         * 9. FORM SUBMIT
         * ========================================================
         */

        const form =
            document.getElementById(
                "employeeEditForm"
            );


        if (form) {

            form.addEventListener(
                "submit",
                handleSubmit
            );

        }


        /*
         * ========================================================
         * 10. RESET BUTTON
         * ========================================================
         */

        const resetButton =
            document.getElementById(
                "resetButton"
            );


        if (resetButton) {

            resetButton.addEventListener(
                "click",
                restoreOriginalData
            );

        }


    }
);


/*
 * ================================================================
 * LOAD COMPONENT
 * ================================================================
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
            "Không tìm thấy component container:",
            elementId
        );

        return;

    }


    console.log(
        "Đang load component:",
        filePath
    );


    try {


        const response =
            await fetch(
                filePath,
                {
                    method: "GET",
                    cache: "no-cache"
                }
            );


        console.log(
            `${filePath} → HTTP ${response.status}`
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
                `${filePath} trả về HTML rỗng.`
            );

        }


        container.innerHTML =
            html;


        console.log(
            `${elementId} → component đã được insert`
        );


    } catch (error) {


        console.error(
            `Không thể load component ${filePath}:`,
            error
        );

    }

}


/*
 * ================================================================
 * SIDEBAR TOGGLE
 * ================================================================
 *
 * PHẦN NÀY COPY THEO CƠ CHẾ ĐANG CHẠY Ở DASHBOARD.
 * ================================================================
 */

function initializeSidebarToggle() {


    /*
     * Kiểm tra jQuery.
     */

    if (
        typeof jQuery ===
        "undefined"
    ) {

        console.error(
            "Sidebar toggle: jQuery chưa được load."
        );

        return;

    }


    /*
     * Tìm hamburger.
     */

    const sidebarToggle =
        jQuery(
            ".sidebar-toggle-box .fa-bars"
        );


    console.log(
        "Sidebar toggle buttons:",
        sidebarToggle.length
    );


    if (
        sidebarToggle.length === 0
    ) {

        console.warn(
            "Sidebar: không tìm thấy nút hamburger."
        );

        return;

    }


    /*
     * Xóa event cũ nếu có.
     */

    sidebarToggle.off(
        "click.employeeEditSidebar"
    );


    /*
     * Đăng ký event.
     */

    sidebarToggle.on(
        "click.employeeEditSidebar",
        function (event) {


            event.preventDefault();

            event.stopPropagation();


            console.log(
                "Employee Edit Sidebar Toggle: CLICK"
            );


            /*
             * Sidebar
             */

            const sidebar =
                jQuery(
                    "#sidebar"
                );


            /*
             * Main content
             */

            const mainContent =
                jQuery(
                    "#main-content"
                );


            /*
             * Container
             */

            const container =
                jQuery(
                    "#container"
                );


            /*
             * Kiểm tra element.
             */

            if (
                sidebar.length === 0 ||
                mainContent.length === 0
            ) {

                console.error(
                    "Sidebar hoặc main-content không tồn tại."
                );

                return;

            }


            /*
             * ====================================================
             * TOGGLE SIDEBAR
             * ====================================================
             */

            sidebar.toggleClass(
                "hide-left-bar"
            );


            /*
             * ====================================================
             * TOGGLE MAIN CONTENT
             * ====================================================
             */

            mainContent.toggleClass(
                "merge-left"
            );


            /*
             * ====================================================
             * NICE SCROLL
             * ====================================================
             */

            const leftNavigation =
                jQuery(
                    ".leftside-navigation"
                );


            if (
                leftNavigation.length > 0 &&
                typeof leftNavigation.getNiceScroll ===
                "function"
            ) {


                if (
                    sidebar.hasClass(
                        "hide-left-bar"
                    )
                ) {


                    leftNavigation
                        .getNiceScroll()
                        .hide();


                } else {


                    leftNavigation
                        .getNiceScroll()
                        .show();


                    leftNavigation
                        .getNiceScroll()
                        .resize();

                }

            }


            /*
             * ====================================================
             * CLOSE RIGHT PANEL
             * ====================================================
             */

            if (
                container.hasClass(
                    "open-right-panel"
                )
            ) {

                container.removeClass(
                    "open-right-panel"
                );

            }


            /*
             * ====================================================
             * CLOSE RIGHT SIDEBAR
             * ====================================================
             */

            const rightSidebar =
                jQuery(
                    ".right-sidebar"
                );


            if (
                rightSidebar.hasClass(
                    "open-right-bar"
                )
            ) {

                rightSidebar.removeClass(
                    "open-right-bar"
                );

            }


            /*
             * ====================================================
             * HEADER
             * ====================================================
             */

            const header =
                jQuery(
                    ".header"
                );


            if (
                header.hasClass(
                    "merge-header"
                )
            ) {

                header.removeClass(
                    "merge-header"
                );

            }

        }
    );


    console.log(
        "Sidebar: hamburger toggle đã được khởi tạo."
    );

}


/*
 * ================================================================
 * LOAD EDIT OPTIONS
 * ================================================================
 */

async function loadEditOptions(
    employeeId
) {


    if (
        typeof getEmployeeEditOptions !==
        "function"
    ) {

        throw new Error(
            "Không tìm thấy hàm getEmployeeEditOptions()."
        );

    }


    const result =
        await getEmployeeEditOptions(
            employeeId
        );


    console.log(
        "GET EMPLOYEE EDIT OPTIONS:",
        result
    );


    const options =
        result.data;


    if (!options) {

        throw new Error(
            "Không nhận được dữ liệu tùy chọn chỉnh sửa."
        );

    }


    originalOptions =
        options;


    /*
     * User
     */

    populateUserSelect(
        options.users || []
    );


    /*
     * Department
     */

    populateDepartmentSelect(
        options.departments || []
    );


    /*
     * Chọn lại user hiện tại.
     */

    if (originalEmployee) {


        const userSelect =
            document.getElementById(
                "userId"
            );


        if (userSelect) {

            userSelect.value =
                originalEmployee.userId ?? "";

        }


        const departmentSelect =
            document.getElementById(
                "departmentId"
            );


        if (departmentSelect) {

            departmentSelect.value =
                originalEmployee.departmentId ?? "";

        }

    }

}


/*
 * ================================================================
 * POPULATE USER
 * ================================================================
 */

function populateUserSelect(
    users
) {


    const select =
        document.getElementById(
            "userId"
        );


    if (!select) {

        return;

    }


    select.innerHTML = `
        <option value="">
            -- Chọn tài khoản --
        </option>
    `;


    users.forEach(
        function (user) {


            const option =
                document.createElement(
                    "option"
                );


            option.value =
                user.id;


            option.textContent =
                user.username;


            select.appendChild(
                option
            );

        }
    );

}


/*
 * ================================================================
 * POPULATE DEPARTMENT
 * ================================================================
 */

function populateDepartmentSelect(
    departments
) {


    const select =
        document.getElementById(
            "departmentId"
        );


    if (!select) {

        return;

    }


    select.innerHTML = `
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
                department.id;


            option.textContent =
                department.code
                    ? `${department.code} - ${department.name}`
                    : department.name;


            select.appendChild(
                option
            );

        }
    );

}


/*
 * ================================================================
 * FILL EMPLOYEE FORM
 * ================================================================
 */

function fillEmployeeForm(
    employee
) {


    setValue(
        "employeeCode",
        employee.employeeCode
    );


    setValue(
        "fullName",
        employee.fullName
    );


    setValue(
        "email",
        employee.email
    );


    setValue(
        "phone",
        employee.phone
    );


    setValue(
        "position",
        employee.position
    );


    setValue(
        "jobTitle",
        employee.jobTitle
    );


    setValue(
        "joinDate",
        employee.joinDate
    );


    setValue(
        "status",
        employee.status || "ACTIVE"
    );


    /*
     * Audit.
     */

    const createdDate =
        document.getElementById(
            "createdDate"
        );


    if (createdDate) {

        createdDate.textContent =
            formatDateTime(
                employee.createdDate
            );

    }


    const updatedDate =
        document.getElementById(
            "updatedDate"
        );


    if (updatedDate) {

        updatedDate.textContent =
            formatDateTime(
                employee.updatedDate,
                "Chưa cập nhật"
            );

    }

}


/*
 * ================================================================
 * SET VALUE
 * ================================================================
 */

function setValue(
    elementId,
    value
) {


    const element =
        document.getElementById(
            elementId
        );


    if (!element) {

        return;

    }


    element.value =
        value ?? "";

}


/*
 * ================================================================
 * CREATE SNAPSHOT
 * ================================================================
 */

function createEmployeeSnapshot(
    employee
) {


    return {


        userId:
            employee.userId,


        employeeCode:
            employee.employeeCode ?? "",


        fullName:
            employee.fullName ?? "",


        email:
            employee.email ?? "",


        phone:
            employee.phone ?? "",


        departmentId:
            employee.departmentId,


        position:
            employee.position ?? "",


        jobTitle:
            employee.jobTitle ?? "",


        joinDate:
            employee.joinDate ?? "",


        status:
            employee.status ?? "ACTIVE"

    };

}


/*
 * ================================================================
 * RESTORE
 * ================================================================
 */

function restoreOriginalData() {


    if (!originalEmployee) {

        return;

    }


    fillEmployeeForm(
        originalEmployee
    );


    const userSelect =
        document.getElementById(
            "userId"
        );


    if (userSelect) {

        userSelect.value =
            originalEmployee.userId ?? "";

    }


    const departmentSelect =
        document.getElementById(
            "departmentId"
        );


    if (departmentSelect) {

        departmentSelect.value =
            originalEmployee.departmentId ?? "";

    }


    clearMessages();


    console.log(
        "FORM RESTORED"
    );

}


/*
 * ================================================================
 * HANDLE SUBMIT
 * ================================================================
 */

async function handleSubmit(
    event
) {


    event.preventDefault();


    clearMessages();


    if (!employeeId) {

        showError(
            "Không xác định được nhân viên cần cập nhật."
        );

        return;

    }


    /*
     * ============================================================
     * FORM DATA
     * ============================================================
     */

    const data = {


        userId:
            Number(
                document.getElementById(
                    "userId"
                ).value
            ),


        employeeCode:
            document.getElementById(
                "employeeCode"
            ).value.trim(),


        fullName:
            document.getElementById(
                "fullName"
            ).value.trim(),


        email:
            document.getElementById(
                "email"
            ).value.trim(),


        phone:
            document.getElementById(
                "phone"
            ).value.trim(),


        departmentId:
            Number(
                document.getElementById(
                    "departmentId"
                ).value
            ),


        position:
            document.getElementById(
                "position"
            ).value || null,


        jobTitle:
            document.getElementById(
                "jobTitle"
            ).value.trim(),


        joinDate:
            document.getElementById(
                "joinDate"
            ).value || null,


        status:
            document.getElementById(
                "status"
            ).value

    };


    console.log(
        "UPDATE EMPLOYEE DATA:",
        data
    );


    /*
     * ============================================================
     * VALIDATE
     * ============================================================
     */

    const validationError =
        validateEmployeeForm(
            data
        );


    if (validationError) {

        showError(
            validationError
        );

        return;

    }


    /*
     * ============================================================
     * SAVE BUTTON
     * ============================================================
     */

    const saveButton =
        document.getElementById(
            "saveButton"
        );


    if (saveButton) {

        saveButton.disabled =
            true;


        saveButton.innerHTML = `
            <i class="fa fa-spinner fa-spin"></i>
            <span>Đang lưu...</span>
        `;

    }


    try {


        const result =
            await updateEmployee(
                employeeId,
                data
            );


        console.log(
            "UPDATE EMPLOYEE RESULT:",
            result
        );


        /*
         * ========================================================
         * UPDATE SNAPSHOT
         * ========================================================
         */

        if (
            result &&
            result.data
        ) {


            originalEmployee =
                createEmployeeSnapshot(
                    result.data
                );


            fillEmployeeForm(
                result.data
            );


            const userSelect =
                document.getElementById(
                    "userId"
                );


            if (userSelect) {

                userSelect.value =
                    result.data.userId ?? "";

            }


            const departmentSelect =
                document.getElementById(
                    "departmentId"
                );


            if (departmentSelect) {

                departmentSelect.value =
                    result.data.departmentId ?? "";

            }

        }


        showSuccess(
            "Cập nhật thông tin nhân viên thành công."
        );


    } catch (error) {


        console.error(
            "UPDATE EMPLOYEE ERROR:",
            error
        );


        showError(
            error.message ||
            "Không thể cập nhật nhân viên."
        );


    } finally {


        if (saveButton) {

            saveButton.disabled =
                false;


            saveButton.innerHTML = `
                <i class="fa fa-save"></i>
                <span>Lưu thay đổi</span>
            `;

        }

    }

}


/*
 * ================================================================
 * VALIDATE
 * ================================================================
 */

function validateEmployeeForm(
    data
) {


    if (!data.userId) {

        return "Vui lòng chọn tài khoản đăng nhập.";

    }


    if (!data.employeeCode) {

        return "Mã nhân viên không được để trống.";

    }


    if (!data.fullName) {

        return "Họ tên không được để trống.";

    }


    if (!data.departmentId) {

        return "Vui lòng chọn phòng ban.";

    }


    if (!data.status) {

        return "Vui lòng chọn trạng thái.";

    }


    if (
        data.email &&
        !isValidEmail(
            data.email
        )
    ) {

        return "Email không đúng định dạng.";

    }


    return null;

}


/*
 * ================================================================
 * EMAIL
 * ================================================================
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


/*
 * ================================================================
 * FORMAT DATETIME
 * ================================================================
 */

function formatDateTime(
    value,
    emptyText = "---"
) {


    if (!value) {

        return emptyText;

    }


    const date =
        new Date(
            value
        );


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return emptyText;

    }


    const day =
        String(
            date.getDate()
        ).padStart(
            2,
            "0"
        );


    const month =
        String(
            date.getMonth() + 1
        ).padStart(
            2,
            "0"
        );


    const year =
        date.getFullYear();


    const hours =
        String(
            date.getHours()
        ).padStart(
            2,
            "0"
        );


    const minutes =
        String(
            date.getMinutes()
        ).padStart(
            2,
            "0"
        );


    return `${day}/${month}/${year} ${hours}:${minutes}`;

}


/*
 * ================================================================
 * SHOW ERROR
 * ================================================================
 */

function showError(
    message
) {


    const element =
        document.getElementById(
            "errorMessage"
        );


    const textElement =
        document.getElementById(
            "errorMessageText"
        );


    if (!element) {

        console.error(
            message
        );

        return;

    }


    if (textElement) {

        textElement.textContent =
            message;

    }


    element.style.display =
        "flex";


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });

}


/*
 * ================================================================
 * SHOW SUCCESS
 * ================================================================
 */

function showSuccess(
    message
) {


    const element =
        document.getElementById(
            "successMessage"
        );


    const textElement =
        document.getElementById(
            "successMessageText"
        );


    if (!element) {

        console.log(
            message
        );

        return;

    }


    if (textElement) {

        textElement.textContent =
            message;

    }


    element.style.display =
        "flex";


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });

}


/*
 * ================================================================
 * CLEAR MESSAGES
 * ================================================================
 */

function clearMessages() {


    const errorMessage =
        document.getElementById(
            "errorMessage"
        );


    const successMessage =
        document.getElementById(
            "successMessage"
        );


    if (errorMessage) {

        errorMessage.style.display =
            "none";

    }


    if (successMessage) {

        successMessage.style.display =
            "none";

    }

}


/*
 * ================================================================
 * SHOW LOADING
 * ================================================================
 */

function showLoading() {


    const saveButton =
        document.getElementById(
            "saveButton"
        );


    if (!saveButton) {

        return;

    }


    saveButton.disabled =
        true;


    saveButton.dataset.originalHtml =
        saveButton.innerHTML;


    saveButton.innerHTML = `
        <i class="fa fa-spinner fa-spin"></i>
        Đang tải...
    `;

}


/*
 * ================================================================
 * HIDE LOADING
 * ================================================================
 */

function hideLoading() {


    const saveButton =
        document.getElementById(
            "saveButton"
        );


    if (!saveButton) {

        return;

    }


    saveButton.disabled =
        false;


    if (
        saveButton.dataset.originalHtml
    ) {


        saveButton.innerHTML =
            saveButton.dataset.originalHtml;


        delete saveButton.dataset.originalHtml;


    } else {


        saveButton.innerHTML = `
            <i class="fa fa-save"></i>
            <span>Lưu thay đổi</span>
        `;

    }

}


/*
 * ================================================================
 * DISABLE FORM
 * ================================================================
 */

function disableForm() {


    const form =
        document.getElementById(
            "employeeEditForm"
        );


    if (!form) {

        return;

    }


    const elements =
        form.querySelectorAll(
            "input, select, textarea, button"
        );


    elements.forEach(
        function (element) {

            element.disabled =
                true;

        }
    );

}