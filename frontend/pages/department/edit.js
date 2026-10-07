/**
 * ============================================================
 * SYSTEMANAGERCV
 * DEPARTMENT EDIT PAGE
 * File: pages/department/edit.js
 * ============================================================
 */


let departmentId =
    null;


let originalDepartment =
    null;


/* ============================================================
 * INITIALIZATION
 * ============================================================
 */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        console.log(
            "=== DEPARTMENT EDIT PAGE START ==="
        );


        try {

            /* ==================================================
             * GET ID
             * ==================================================
             */

            const params =
                new URLSearchParams(
                    window.location.search
                );


            departmentId =
                params.get(
                    "id"
                );


            if (
                !departmentId
            ) {

                throw new Error(
                    "Không tìm thấy ID phòng ban."
                );

            }


            /* ==================================================
             * LOAD COMPONENTS
             * ==================================================
             */

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


            /* ==================================================
             * AUTH
             * ==================================================
             */

            if (
                typeof initializeHeaderUser ===
                "function"
            ) {

                await initializeHeaderUser();

            }


            if (
                typeof initializeSidebar ===
                "function"
            ) {

                initializeSidebar();

            }


            initializeDepartmentSidebarToggle();


            if (
                typeof setupLogout ===
                "function"
            ) {

                setupLogout();

            }


            /* ==================================================
             * LOAD DEPARTMENT
             * ==================================================
             */

            await loadDepartment();


            /* ==================================================
             * FORM
             * ==================================================
             */

            initializeForm();


            initializeReset();


        } catch (error) {

            console.error(
                "DEPARTMENT EDIT INITIALIZATION ERROR:",
                error
            );


            showError(
                error.message ||
                "Không thể khởi tạo trang sửa phòng ban."
            );

        }

    }
);


/* ============================================================
 * COMPONENT
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

        throw new Error(
            `Không tìm thấy #${elementId}.`
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
            `Không thể tải ${filePath}.`
        );

    }


    container.innerHTML =
        await response.text();

}


/* ============================================================
 * SIDEBAR TOGGLE
 * ============================================================
 */

function initializeDepartmentSidebarToggle() {

    if (
        document.documentElement.dataset
            .smcvDepartmentSidebarToggle ===
        "true"
    ) {

        return;

    }


    document.addEventListener(
        "click",
        function (event) {

            const toggle =
                event.target.closest(
                    ".sidebar-toggle-box .fa-bars"
                );


            if (!toggle) {

                return;

            }


            const sidebar =
                document.getElementById(
                    "sidebar"
                );


            const mainContent =
                document.getElementById(
                    "main-content"
                );


            const footer =
                document.getElementById(
                    "footer-container"
                );


            if (
                !sidebar ||
                !mainContent
            ) {

                return;

            }


            event.preventDefault();

            event.stopPropagation();

            event.stopImmediatePropagation();


            const shouldCollapse =
                !sidebar.classList.contains(
                    "hide-left-bar"
                );


            sidebar.classList.toggle(
                "hide-left-bar",
                shouldCollapse
            );


            mainContent.classList.toggle(
                "merge-left",
                shouldCollapse
            );


            if (footer) {

                footer.classList.toggle(
                    "merge-left",
                    shouldCollapse
                );

            }

        },
        true
    );


    document.documentElement.dataset
        .smcvDepartmentSidebarToggle =
        "true";

}


/* ============================================================
 * LOAD DEPARTMENT
 * ============================================================
 */

async function loadDepartment() {

    try {

        showLoading();


        const response =
            await getDepartmentById(
                departmentId
            );


        console.log(
            "DEPARTMENT DETAIL RESPONSE:",
            response
        );


        const department =
            response?.data;


        if (!department) {

            throw new Error(
                "Không tìm thấy dữ liệu phòng ban."
            );

        }


        originalDepartment = {

            code:
                department.code ||
                "",

            name:
                department.name ||
                "",

            description:
                department.description ||
                "",

            status:
                normalizeDepartmentStatus(
                    department.status
                ) ||
                "ACTIVE"

        };


        fillDepartmentForm(
            originalDepartment
        );


        hideLoading();


    } catch (error) {

        hideLoading();


        console.error(
            "LOAD DEPARTMENT ERROR:",
            error
        );


        throw error;

    }

}


/* ============================================================
 * FILL FORM
 * ============================================================
 */

function fillDepartmentForm(
    department
) {

    const code =
        document.getElementById(
            "departmentCode"
        );


    const name =
        document.getElementById(
            "departmentName"
        );


    const description =
        document.getElementById(
            "departmentDescription"
        );


    const status =
        document.getElementById(
            "departmentStatus"
        );


    const hiddenId =
        document.getElementById(
            "department-id"
        );


    if (hiddenId) {

        hiddenId.value =
            departmentId;

    }


    if (code) {

        code.value =
            department.code ||
            "";

    }


    if (name) {

        name.value =
            department.name ||
            "";

    }


    if (description) {

        description.value =
            department.description ||
            "";

    }


    if (status) {

        status.value =
            normalizeDepartmentStatus(
                department.status
            ) ||
            "ACTIVE";

    }

}


/* ============================================================
 * FORM
 * ============================================================
 */

function initializeForm() {

    const form =
        document.getElementById(
            "department-edit-form"
        );


    if (!form) {

        throw new Error(
            "Không tìm thấy form sửa phòng ban."
        );

    }


    form.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            hideMessages();


            const code =
                document.getElementById(
                    "departmentCode"
                )?.value.trim() ||
                "";


            const name =
                document.getElementById(
                    "departmentName"
                )?.value.trim() ||
                "";


            const description =
                document.getElementById(
                    "departmentDescription"
                )?.value.trim() ||
                "";


            const status =
                document.getElementById(
                    "departmentStatus"
                )?.value ||
                "ACTIVE";


            /* ==================================================
             * VALIDATION
             * ==================================================
             */

            if (!code) {

                showError(
                    "Mã phòng ban không được để trống."
                );


                document
                    .getElementById(
                        "departmentCode"
                    )
                    ?.focus();


                return;

            }


            if (!name) {

                showError(
                    "Tên phòng ban không được để trống."
                );


                document
                    .getElementById(
                        "departmentName"
                    )
                    ?.focus();


                return;

            }


            /* ==================================================
             * REQUEST DATA
             * ==================================================
             */

            const data = {

                code,

                name,

                description:
                    description ||
                    null,

                status

            };


            const saveButton =
                document.getElementById(
                    "department-save-button"
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

                console.log(
                    "UPDATE DEPARTMENT REQUEST:",
                    {
                        id:
                            departmentId,

                        data
                    }
                );


                const response =
                    await updateDepartment(
                        departmentId,
                        data
                    );


                console.log(
                    "UPDATE DEPARTMENT RESPONSE:",
                    response
                );


                showSuccess(
                    "Cập nhật phòng ban thành công."
                );


                /*
                 * Cập nhật snapshot.
                 */

                originalDepartment = {

                    code,

                    name,

                    description,

                    status

                };


                setTimeout(
                    function () {

                        window.location.replace(
                            "./index.html"
                        );

                    },
                    800
                );


            } catch (error) {

                console.error(
                    "UPDATE DEPARTMENT ERROR:",
                    error
                );


                showError(
                    error.message ||
                    "Không thể cập nhật phòng ban."
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
    );

}


/* ============================================================
 * RESET
 * ============================================================
 */

function initializeReset() {

    const form =
        document.getElementById(
            "department-edit-form"
        );


    const resetButton =
        document.getElementById(
            "department-reset"
        );


    if (
        !form ||
        !resetButton
    ) {

        return;

    }


    resetButton.addEventListener(
        "click",
        function () {

            /*
             * Không dùng form.reset()
             * vì dữ liệu ban đầu được lấy từ API.
             */

            fillDepartmentForm(
                originalDepartment
            );


            hideMessages();

        }
    );

}


/* ============================================================
 * STATUS
 * ============================================================
 */

function normalizeDepartmentStatus(
    status
) {

    if (
        status === null ||
        status === undefined
    ) {

        return "";

    }


    if (
        typeof status ===
        "string"
    ) {

        return status
            .trim()
            .toUpperCase();

    }


    if (
        typeof status ===
        "object"
    ) {

        return String(
            status.name ||
            ""
        )
            .trim()
            .toUpperCase();

    }


    return "";

}


/* ============================================================
 * LOADING
 * ============================================================
 */

function showLoading() {

    const form =
        document.getElementById(
            "department-edit-form"
        );


    if (!form) {

        return;

    }


    form.style.opacity =
        "0.6";

}


function hideLoading() {

    const form =
        document.getElementById(
            "department-edit-form"
        );


    if (!form) {

        return;

    }


    form.style.opacity =
        "1";

}


/* ============================================================
 * MESSAGES
 * ============================================================
 */

function hideMessages() {

    const error =
        document.getElementById(
            "department-error"
        );


    const success =
        document.getElementById(
            "department-success"
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
            "department-error"
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
            "department-success"
        );


    if (!element) {

        return;

    }


    element.textContent =
        message;


    element.style.display =
        "";

}