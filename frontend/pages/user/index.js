/**
 * ============================================================
 * SYSTEMANAGERCV
 * USER LIST PAGE
 * File: pages/user/index.js
 * ============================================================
 */

let currentPage = 0;

const pageSize = 10;


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


            /*
             * Tải Role cho bộ lọc.
             */
            await loadRoles();


            initializeEvents();


            await loadUsers();


        } catch (error) {

            console.error(
                "USER INDEX INITIALIZATION ERROR:",
                error
            );


            showError(
                error.message ||
                "Không thể khởi tạo trang User."
            );

        }

    }
);


/* ============================================================
 * LOAD COMPONENT
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
            "user-role"
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
                -- Tất cả quyền --
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
 * EVENTS
 * ============================================================ */

function initializeEvents() {

    const searchForm =
        document.getElementById(
            "user-search-form"
        );


    if (searchForm) {

        searchForm.addEventListener(
            "submit",
            function (event) {

                event.preventDefault();

                currentPage = 0;

                loadUsers();

            }
        );

    }


    const resetButton =
        document.getElementById(
            "user-reset"
        );


    if (resetButton) {

        resetButton.addEventListener(
            "click",
            function () {

                const keyword =
                    document.getElementById(
                        "user-keyword"
                    );


                const role =
                    document.getElementById(
                        "user-role"
                    );


                const enabled =
                    document.getElementById(
                        "user-enabled"
                    );


                if (keyword) {

                    keyword.value = "";

                }


                if (role) {

                    role.value = "";

                }


                if (enabled) {

                    enabled.value = "";

                }


                currentPage = 0;

                loadUsers();

            }
        );

    }


    document
        .getElementById("user-prev")
        ?.addEventListener(
            "click",
            function () {

                if (currentPage > 0) {

                    currentPage--;

                    loadUsers();

                }

            }
        );


    document
        .getElementById("user-next")
        ?.addEventListener(
            "click",
            function () {

                const totalPagesElement =
                    document.getElementById(
                        "user-total-pages"
                    );


                const totalPages =
                    Number(
                        totalPagesElement?.textContent ||
                        "0"
                    );


                if (
                    currentPage <
                    totalPages - 1
                ) {

                    currentPage++;

                    loadUsers();

                }

            }
        );

}


/* ============================================================
 * LOAD USERS
 * ============================================================ */

async function loadUsers() {

    hideMessages();


    const keyword =
        document.getElementById(
            "user-keyword"
        )?.value.trim() || "";


    const roleId =
        document.getElementById(
            "user-role"
        )?.value || "";


    const enabled =
        document.getElementById(
            "user-enabled"
        )?.value || "";


    try {

        const response =
            await searchUsers({

                page:
                    currentPage,

                size:
                    pageSize,

                keyword,

                roleId,

                enabled,

                sortBy:
                    "createdDate",

                sortDirection:
                    "DESC"

            });


        const page =
            response?.data;


        if (!page) {

            throw new Error(
                "API không trả về dữ liệu User."
            );

        }


        /*
         * Nếu currentPage vượt quá trang cuối
         * do vừa xóa User.
         */
        if (
            page.totalPages > 0 &&
            currentPage >= page.totalPages
        ) {

            currentPage =
                page.totalPages - 1;

            await loadUsers();

            return;

        }


        renderUsers(page);


    } catch (error) {

        console.error(
            "LOAD USERS ERROR:",
            error
        );


        renderEmpty(
            "Không thể tải danh sách người dùng."
        );


        showError(
            error.message ||
            "Không thể tải danh sách người dùng."
        );

    }

}


/* ============================================================
 * RENDER USERS
 * ============================================================ */

function renderUsers(page) {

    const body =
        document.getElementById(
            "user-table-body"
        );


    if (!body) {

        return;

    }


    const users =
        page.content || [];


    if (users.length === 0) {

        renderEmpty(
            "Không tìm thấy người dùng phù hợp."
        );


        updatePagination(
            page
        );

        return;

    }


    body.innerHTML = "";


    users.forEach(
        function (user, index) {

            const tr =
                document.createElement(
                    "tr"
                );


            const stt =
                (page.number * page.size)
                + index
                + 1;


            tr.innerHTML = `

                <td class="category-col-stt">

                    <span class="category-stt">
                        ${stt}
                    </span>

                </td>


                <td class="category-col-name">

                    <div class="category-name">

                        <span class="category-name-icon">

                            <i class="fa fa-user"></i>

                        </span>

                        <span class="category-name-text">

                            ${escapeHtml(
                                user.username ||
                                "---"
                            )}

                        </span>

                    </div>

                </td>


                <td>

                    ${escapeHtml(
                        user.fullName ||
                        "---"
                    )}

                </td>


                <td>

                    ${escapeHtml(
                        user.email ||
                        "---"
                    )}

                </td>


                <td>

                    <span class="category-role">

                        ${escapeHtml(
                            user.roleName ||
                            "---"
                        )}

                    </span>

                </td>


                <td>

                    ${
                        user.roleDescription
                            ? escapeHtml(
                                user.roleDescription
                              )
                            : "---"
                    }

                </td>


                <td class="category-col-status">

                    ${
                        user.enabled

                            ? `

                                <span class="category-status category-status-active">

                                    <i class="fa fa-check-circle"></i>

                                    Hoạt động

                                </span>

                              `

                            : `

                                <span class="category-status category-status-inactive">

                                    <i class="fa fa-times-circle"></i>

                                    Không hoạt động

                                </span>

                              `
                    }

                </td>


                <td class="category-col-action">

                    <div class="category-action-buttons">

                        <a
                            href="./edit.html?id=${encodeURIComponent(user.id)}"
                            class="category-action category-action-edit"
                            title="Sửa người dùng"
                        >

                            <i class="fa fa-pencil"></i>

                            <span>Sửa</span>

                        </a>


                        <button
                            type="button"
                            class="category-action category-action-delete"
                            data-user-id="${user.id}"
                        >

                            <i class="fa fa-trash"></i>

                            <span>Xóa</span>

                        </button>

                    </div>

                </td>

            `;


            const deleteButton =
                tr.querySelector(
                    "[data-user-id]"
                );


            deleteButton?.addEventListener(
                "click",
                function () {

                    handleDeleteUser(
                        user.id,
                        user.username
                    );

                }
            );


            body.appendChild(
                tr
            );

        }
    );


    updatePagination(
        page
    );

}


/* ============================================================
 * DELETE USER
 * ============================================================ */

async function handleDeleteUser(
    id,
    username
) {

    const confirmed =
        window.confirm(
            `Bạn có chắc chắn muốn xóa người dùng "${username}" không?`
        );


    if (!confirmed) {

        return;

    }


    try {

        await deleteUser(
            id
        );


        showSuccess(
            "Xóa người dùng thành công."
        );


        await loadUsers();


    } catch (error) {

        console.error(
            "DELETE USER ERROR:",
            error
        );


        showError(
            error.message ||
            "Không thể xóa người dùng."
        );

    }

}


/* ============================================================
 * PAGINATION
 * ============================================================ */

function updatePagination(
    page
) {

    const current =
        document.getElementById(
            "user-current-page"
        );


    const total =
        document.getElementById(
            "user-total-pages"
        );


    const currentNumber =
        (page.number || 0)
        + 1;


    const totalNumber =
        page.totalPages > 0
            ? page.totalPages
            : 1;


    if (current) {

        current.textContent =
            currentNumber;

    }


    if (total) {

        total.textContent =
            totalNumber;

    }


    const previous =
        document.getElementById(
            "user-prev"
        );


    const next =
        document.getElementById(
            "user-next"
        );


    if (previous) {

        previous.disabled =
            currentPage <= 0;

    }


    if (next) {

        next.disabled =
            currentPage >=
            totalNumber - 1;

    }


    renderPageNumbers(
        totalNumber
    );

}


/* ============================================================
 * PAGE NUMBERS
 * ============================================================ */

function renderPageNumbers(
    totalPages
) {

    const container =
        document.getElementById(
            "user-page-numbers"
        );


    if (!container) {

        return;

    }


    container.innerHTML = "";


    if (totalPages <= 0) {

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

                loadUsers();

            }
        );


        container.appendChild(
            button
        );

    }

}


/* ============================================================
 * EMPTY
 * ============================================================ */

function renderEmpty(
    message
) {

    const body =
        document.getElementById(
            "user-table-body"
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

                        <i class="fa fa-user-o"></i>

                    </div>


                    <h4>
                        Không có người dùng
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


/* ============================================================
 * HTML ESCAPE
 * ============================================================ */

function escapeHtml(
    value
) {

    return String(value)
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