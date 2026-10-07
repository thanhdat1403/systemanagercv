/**
 * ============================================================
 * SYSTEMANAGERCV
 * DEPARTMENT ADD PAGE
 * File: pages/department/add.js
 * ============================================================
 */


document.addEventListener(
    "DOMContentLoaded",
    async function () {

        console.log(
            "=== DEPARTMENT ADD PAGE START ==="
        );


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


            initializeForm();


            initializeReset();


        } catch (error) {

            console.error(
                "DEPARTMENT ADD INITIALIZATION ERROR:",
                error
            );


            showError(
                error.message ||
                "Không thể khởi tạo trang thêm phòng ban."
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
 * FORM
 * ============================================================
 */

function initializeForm() {

    const form =
        document.getElementById(
            "department-add-form"
        );


    if (!form) {

        return;

    }


    form.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            hideMessages();


            const code =
                document.getElementById(
                    "code"
                )?.value.trim() ||
                "";


            const name =
                document.getElementById(
                    "name"
                )?.value.trim() ||
                "";


            const description =
                document.getElementById(
                    "description"
                )?.value.trim() ||
                "";


            const status =
                document.getElementById(
                    "status"
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
                        "code"
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
                        "name"
                    )
                    ?.focus();


                return;

            }


            /* ==================================================
             * REQUEST
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
                    "CREATE DEPARTMENT REQUEST:",
                    data
                );


                const response =
                    await createDepartment(
                        data
                    );


                console.log(
                    "CREATE DEPARTMENT RESPONSE:",
                    response
                );


                showSuccess(
                    "Thêm phòng ban thành công."
                );


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
                    "CREATE DEPARTMENT ERROR:",
                    error
                );


                showError(
                    error.message ||
                    "Không thể thêm phòng ban."
                );


            } finally {

                if (saveButton) {

                    saveButton.disabled =
                        false;


                    saveButton.innerHTML = `
                        <i class="fa fa-save"></i>
                        <span>Lưu phòng ban</span>
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
            "department-add-form"
        );


    const resetButton =
        document.getElementById(
            "department-reset-form"
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

            setTimeout(
                function () {

                    hideMessages();

                },
                0
            );

        }
    );

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