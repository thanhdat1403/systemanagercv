/**
 * ============================================================
 * SYSTEMANAGERCV
 * DEPARTMENT INDEX PAGE
 * File: pages/department/index.js
 * ============================================================
 */


let currentPage = 0;

const pageSize = 10;


/* ============================================================
 * INITIALIZATION
 * ============================================================
 */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        console.log(
            "=== DEPARTMENT INDEX PAGE START ==="
        );


        try {

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
             * AUTH / SIDEBAR / LOGOUT
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
             * EVENTS
             * ==================================================
             */

            initializeEvents();


            /* ==================================================
             * LOAD DEPARTMENTS
             * ==================================================
             */

            await loadDepartments();


            console.log(
                "=== DEPARTMENT INDEX PAGE READY ==="
            );


        } catch (error) {

            console.error(
                "DEPARTMENT INDEX INITIALIZATION ERROR:",
                error
            );


            showError(
                error.message ||
                "Không thể khởi tạo trang phòng ban."
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


            try {

                if (
                    window.jQuery &&
                    typeof window.jQuery.fn.niceScroll !==
                    "undefined"
                ) {

                    const navigation =
                        window.jQuery(
                            ".leftside-navigation"
                        );


                    if (
                        navigation.length > 0 &&
                        typeof navigation
                            .getNiceScroll ===
                        "function"
                    ) {

                        if (
                            shouldCollapse
                        ) {

                            navigation
                                .getNiceScroll()
                                .hide();

                        } else {

                            navigation
                                .getNiceScroll()
                                .show();

                            navigation
                                .getNiceScroll()
                                .resize();

                        }

                    }

                }

            } catch (error) {

                console.warn(
                    "DEPARTMENT SIDEBAR:",
                    error
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
 * EVENTS
 * ============================================================
 */

function initializeEvents() {

    const searchForm =
        document.getElementById(
            "department-search-form"
        );


    if (searchForm) {

        searchForm.addEventListener(
            "submit",
            function (event) {

                event.preventDefault();

                currentPage = 0;

                loadDepartments();

            }
        );

    }


    const resetButton =
        document.getElementById(
            "department-reset"
        );


    if (resetButton) {

        resetButton.addEventListener(
            "click",
            function () {

                const keyword =
                    document.getElementById(
                        "department-keyword"
                    );


                const status =
                    document.getElementById(
                        "department-status"
                    );


                if (keyword) {

                    keyword.value =
                        "";

                }


                if (status) {

                    status.value =
                        "";

                }


                currentPage =
                    0;


                loadDepartments();

            }
        );

    }


    const previous =
        document.getElementById(
            "department-prev"
        );


    if (previous) {

        previous.addEventListener(
            "click",
            function () {

                if (
                    currentPage >
                    0
                ) {

                    currentPage--;

                    loadDepartments();

                }

            }
        );

    }


    const next =
        document.getElementById(
            "department-next"
        );


    if (next) {

        next.addEventListener(
            "click",
            function () {

                const totalPages =
                    Number(
                        document.getElementById(
                            "department-total-pages"
                        )?.textContent ||
                        "0"
                    );


                if (
                    currentPage <
                    totalPages - 1
                ) {

                    currentPage++;

                    loadDepartments();

                }

            }
        );

    }

}


/* ============================================================
 * LOAD DEPARTMENTS
 * ============================================================
 */

async function loadDepartments() {

    hideMessages();


    const keyword =
        document.getElementById(
            "department-keyword"
        )?.value.trim() ||
        "";


    const status =
        document.getElementById(
            "department-status"
        )?.value ||
        "";


    try {

        const response =
            await getDepartments({

                page:
                    currentPage,

                size:
                    pageSize,

                keyword,

                status,

                sortBy:
                    "createdDate",

                sortDirection:
                    "DESC"

            });


        console.log(
            "DEPARTMENT LIST RESPONSE:",
            response
        );


        const page =
            response?.data;


        if (!page) {

            throw new Error(
                "API không trả về dữ liệu phòng ban."
            );

        }


        /*
         * Nếu xóa phần tử ở trang cuối
         * khiến totalPages giảm.
         */

        if (
            page.totalPages > 0 &&
            currentPage >=
            page.totalPages
        ) {

            currentPage =
                page.totalPages - 1;


            await loadDepartments();


            return;

        }


        renderDepartments(
            page
        );


    } catch (error) {

        console.error(
            "LOAD DEPARTMENTS ERROR:",
            error
        );


        renderEmpty(
            "Không thể tải danh sách phòng ban."
        );


        showError(
            error.message ||
            "Không thể tải danh sách phòng ban."
        );

    }

}


/* ============================================================
 * RENDER DEPARTMENTS
 * ============================================================
 */

function renderDepartments(
    page
) {

    const body =
        document.getElementById(
            "department-table-body"
        );


    if (!body) {

        return;

    }


    const departments =
        page.content ||
        [];


    const totalElements =
        Number(
            page.totalElements ||
            0
        );


    const totalItems =
        document.getElementById(
            "department-total-items"
        );


    if (totalItems) {

        totalItems.textContent =
            totalElements;

    }


    if (
        departments.length ===
        0
    ) {

        renderEmpty(
            "Chưa có phòng ban nào phù hợp."
        );


        updatePagination(
            page
        );


        return;

    }


    body.innerHTML =
        "";


    departments.forEach(
        function (
            department,
            index
        ) {

            const row =
                document.createElement(
                    "tr"
                );


            const stt =
                (
                    Number(page.number || 0) *
                    Number(page.size || pageSize)
                )
                +
                index
                +
                1;


            const code =
                department.code ||
                "---";


            const name =
                department.name ||
                "---";


            const description =
                department.description ||
                "Chưa có mô tả";


            const status =
                normalizeDepartmentStatus(
                    department.status
                );


            const statusLabel =
                getDepartmentStatusLabel(
                    status
                );


            const createdDate =
                formatDateTime(
                    department.createdDate
                );

            const updatedDate =
            formatDateTime(
                department.updatedDate
            );

            row.innerHTML = `

                <td class="category-col-stt">

                    <span class="category-stt">

                        ${stt}

                    </span>

                </td>


                <td class="category-col-code">

                    <span class="category-code">

                        ${escapeHtml(code)}

                    </span>

                </td>


                <td class="category-col-name">

                    <div class="category-name">

                        <span class="category-name-icon">

                            <i class="fa fa-building"></i>

                        </span>


                        <span class="category-name-text">

                            ${escapeHtml(name)}

                        </span>

                    </div>

                </td>


                <td class="category-col-description">

                    <span class="category-description">

                        ${escapeHtml(description)}

                    </span>

                </td>


                <td class="category-col-status">

                    ${
                        status
                            ? `

                                <span class="
                                    category-status
                                    ${
                                        status ===
                                        "ACTIVE"
                                            ? "category-status-active"
                                            : "category-status-inactive"
                                    }
                                ">

                                    <i class="
                                        ${
                                            status ===
                                            "ACTIVE"
                                                ? "fa fa-check-circle"
                                                : "fa fa-times-circle"
                                        }
                                    "></i>

                                    ${escapeHtml(statusLabel)}

                                </span>

                              `
                            : `

                                <span>
                                    ---
                                </span>

                              `
                    }

                </td>


                <td class="category-col-created">

                    <span class="category-created">

                        ${escapeHtml(createdDate)}

                    </span>

                </td>


                <td class="category-col-updated">

                    <span class="category-updated">

                        ${escapeHtml(updatedDate)}

                    </span>

                </td>


                <td class="category-col-action">

                    <div class="category-action-buttons">


                        <a
                            href="./edit.html?id=${encodeURIComponent(
                                department.id
                            )}"
                            class="category-action category-action-edit"
                            title="Sửa phòng ban"
                        >

                            <i class="fa fa-pencil"></i>

                            <span>
                                Sửa
                            </span>

                        </a>


                        <button
                            type="button"
                            class="category-action category-action-delete"
                            data-department-id="${department.id}"
                            data-department-name="${escapeAttribute(
                                name
                            )}"
                            title="Xóa phòng ban"
                        >

                            <i class="fa fa-trash"></i>

                            <span>
                                Xóa
                            </span>

                        </button>

                    </div>

                </td>

            `;


            const deleteButton =
                row.querySelector(
                    "[data-department-id]"
                );


            if (deleteButton) {

                deleteButton.addEventListener(
                    "click",
                    function () {

                        handleDeleteDepartment(
                            department.id,
                            department.name
                        );

                    }
                );

            }


            body.appendChild(
                row
            );

        }
    );


    updatePagination(
        page
    );

}


/* ============================================================
 * DELETE
 * ============================================================
 */

async function handleDeleteDepartment(
    id,
    name
) {

    const confirmed =
        window.confirm(
            `Bạn có chắc chắn muốn xóa phòng ban "${name}" không?`
        );


    if (!confirmed) {

        return;

    }


    try {

        await deleteDepartment(
            id
        );


        showSuccess(
            "Xóa phòng ban thành công."
        );


        await loadDepartments();


    } catch (error) {

        console.error(
            "DELETE DEPARTMENT ERROR:",
            error
        );


        showError(
            error.message ||
            "Không thể xóa phòng ban."
        );

    }

}


/* ============================================================
 * PAGINATION
 * ============================================================
 */

function updatePagination(
    page
) {

    const current =
        document.getElementById(
            "department-current-page"
        );


    const total =
        document.getElementById(
            "department-total-pages"
        );


    const currentNumber =
        Number(
            page.number ||
            0
        )
        +
        1;


    const totalNumber =
        Number(
            page.totalPages ||
            0
        );


    if (current) {

        current.textContent =
            totalNumber > 0
                ? currentNumber
                : 1;

    }


    if (total) {

        total.textContent =
            totalNumber > 0
                ? totalNumber
                : 1;

    }


    const previous =
        document.getElementById(
            "department-prev"
        );


    const next =
        document.getElementById(
            "department-next"
        );


    if (previous) {

        previous.disabled =
            currentPage <= 0;

    }


    if (next) {

        next.disabled =
            totalNumber <= 0 ||
            currentPage >=
            totalNumber - 1;

    }


    renderPageNumbers(
        totalNumber
    );

}


/* ============================================================
 * PAGE NUMBERS
 * ============================================================
 */

function renderPageNumbers(
    totalPages
) {

    const container =
        document.getElementById(
            "department-page-numbers"
        );


    if (!container) {

        return;

    }


    container.innerHTML =
        "";


    if (
        !totalPages ||
        totalPages <= 0
    ) {

        return;

    }


    for (
        let pageIndex = 0;
        pageIndex < totalPages;
        pageIndex++
    ) {

        const button =
            document.createElement(
                "button"
            );


        button.type =
            "button";


        button.className =
            "pagination-button";


        button.textContent =
            pageIndex + 1;


        if (
            pageIndex ===
            currentPage
        ) {

            button.classList.add(
                "pagination-active"
            );

        }


        button.addEventListener(
            "click",
            function () {

                currentPage =
                    pageIndex;


                loadDepartments();

            }
        );


        container.appendChild(
            button
        );

    }

}


/* ============================================================
 * EMPTY
 * ============================================================
 */

function renderEmpty(
    message
) {

    const body =
        document.getElementById(
            "department-table-body"
        );


    if (!body) {

        return;

    }


    body.innerHTML = `

        <tr>

            <td
                colspan="8"
                class="text-center"
            >

                <div class="category-empty">


                    <div class="category-empty-icon">

                        <i class="fa fa-building-o"></i>

                    </div>


                    <h4>

                        Không có phòng ban

                    </h4>


                    <p>

                        ${escapeHtml(message)}

                    </p>

                </div>

            </td>

        </tr>

    `;

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


function getDepartmentStatusLabel(
    status
) {

    switch (
        normalizeDepartmentStatus(
            status
        )
    ) {

        case "ACTIVE":

            return "Hoạt động";


        case "INACTIVE":

            return "Không hoạt động";


        default:

            return String(
                status ||
                "---"
            );

    }

}


/* ============================================================
 * DATE
 * ============================================================
 */

function formatDateTime(
    value
) {

    if (!value) {

        return "---";

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

        return "---";

    }


    const day =
        String(
            date.getDate()
        )
            .padStart(
                2,
                "0"
            );


    const month =
        String(
            date.getMonth() + 1
        )
            .padStart(
                2,
                "0"
            );


    const year =
        date.getFullYear();


    const hours =
        String(
            date.getHours()
        )
            .padStart(
                2,
                "0"
            );


    const minutes =
        String(
            date.getMinutes()
        )
            .padStart(
                2,
                "0"
            );


    return `${day}/${month}/${year} ${hours}:${minutes}`;

}


/* ============================================================
 * ESCAPE HTML
 * ============================================================
 */

function escapeHtml(
    value
) {

    return String(
        value ?? ""
    )
        .replaceAll(
            "&",
            "&amp;"
        )
        .replaceAll(
            "<",
            "&lt;"
        )
        .replaceAll(
            ">",
            "&gt;"
        )
        .replaceAll(
            '"',
            "&quot;"
        )
        .replaceAll(
            "'",
            "&#039;"
        );

}


function escapeAttribute(
    value
) {

    return escapeHtml(
        value
    );

}


/* ============================================================
 * MESSAGE
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