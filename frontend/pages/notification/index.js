"use strict";


/* =========================================================
   SYSTEMANAGERCV
   NOTIFICATION PAGE
   STEP 5
   ========================================================= */


/* =========================================================
   STATE
   ========================================================= */

let currentPage = 0;

const pageSize = 10;

let totalPages = 1;

let currentFilter = "all";

let currentRole = "";

let currentUser = null;

let loading = false;


/* =========================================================
   ROLE CONFIG
   ========================================================= */

const NOTIFICATION_ROLE_CONFIG = {

    ADMIN: {

        title:
            "Thông báo quản trị",

        description:
            "Theo dõi các thông báo được gửi tới " +
            "tài khoản quản trị.",

        contextTitle:
            "Phạm vi quản trị",

        contextDescription:
            "Danh sách chỉ bao gồm notification " +
            "được backend gửi cho tài khoản ADMIN."

    },


    HR: {

        title:
            "Thông báo HR",

        description:
            "Theo dõi các thông báo liên quan đến " +
            "kiểm duyệt CV và quy trình nhân sự.",

        contextTitle:
            "Phạm vi HR",

        contextDescription:
            "Các notification được backend gửi tới " +
            "tài khoản HR theo workflow."

    },


    TECH_LEAD: {

        title:
            "Thông báo Tech Lead",

        description:
            "Theo dõi CV đang chờ kiểm duyệt " +
            "và các thông báo workflow.",

        contextTitle:
            "Phạm vi Tech Lead",

        contextDescription:
            "Các notification được backend gửi theo " +
            "phạm vi và workflow của Tech Lead."

    },


    EMPLOYEE: {

        title:
            "Thông báo của tôi",

        description:
            "Theo dõi yêu cầu cập nhật CV và " +
            "kết quả kiểm duyệt hồ sơ.",

        contextTitle:
            "Phạm vi cá nhân",

        contextDescription:
            "Các notification được backend gửi trực tiếp " +
            "cho tài khoản nhân viên hiện tại."

    }

};


/* =========================================================
   DOM READY
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        try {

            await loadCommonComponents();


            const authState =
                await initializeHeaderUser();


            if (!authState) {

                throw new Error(
                    "Không thể lấy thông tin người dùng."
                );

            }


            currentUser =
                authState.currentUser ||
                null;


            currentRole =
                normalizeRole(
                    authState.role
                );


            validateRole();


            initializeSidebar(
                currentRole
            );


            if (
                typeof setupLogout ===
                "function"
            ) {

                setupLogout();

            }


            initializeSidebarToggle();


            applyRoleUI();


            initializeNotificationEvents();


            await loadUnreadCount();


            await loadNotifications();


        } catch (error) {

            console.error(
                "NOTIFICATION PAGE INITIALIZATION ERROR:",
                error
            );


            showNotificationError(
                error?.message ||
                "Không thể khởi tạo trang thông báo."
            );

        }

    }
);


/* =========================================================
   LOAD COMPONENT
   ========================================================= */

async function loadCommonComponents() {

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

}


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
                method:
                    "GET",

                cache:
                    "no-cache"
            }
        );


    if (!response.ok) {

        throw new Error(
            `Không thể tải ${filePath}. HTTP ${response.status}`
        );

    }


    container.innerHTML =
        await response.text();

}


/* =========================================================
   ROLE
   ========================================================= */

function validateRole() {

    const validRoles = [

        "ADMIN",
        "HR",
        "TECH_LEAD",
        "EMPLOYEE"

    ];


    if (
        !validRoles.includes(
            currentRole
        )
    ) {

        throw new Error(
            "Role không hợp lệ: " +
            currentRole
        );

    }


    document.body.dataset.currentRole =
        currentRole;

}


function applyRoleUI() {

    const config =
        NOTIFICATION_ROLE_CONFIG[
            currentRole
        ];


    if (!config) {

        return;

    }


    setText(
        "notification-page-title",
        config.title
    );


    setText(
        "notification-page-description",
        config.description
    );


    setText(
        "notification-role-title",
        config.contextTitle
    );


    setText(
        "notification-role-description",
        config.contextDescription
    );

}


/* =========================================================
   EVENTS
   ========================================================= */

function initializeNotificationEvents() {

    const tabs =
        document.querySelectorAll(
            ".notification-tab"
        );


    tabs.forEach(
        function (
            tab
        ) {

            tab.addEventListener(
                "click",
                function () {

                    const filter =
                        tab.dataset.filter ||
                        "all";


                    currentFilter =
                        filter;


                    currentPage =
                        0;


                    updateActiveTab();


                    loadNotifications();

                }
            );

        }
    );


    const refreshButton =
        document.getElementById(
            "notification-refresh"
        );


    if (refreshButton) {

        refreshButton.addEventListener(
            "click",
            async function () {

                await refreshNotifications();

            }
        );

    }


    const previousButton =
        document.getElementById(
            "notification-prev"
        );


    if (previousButton) {

        previousButton.addEventListener(
            "click",
            function () {

                if (
                    currentPage >
                    0
                ) {

                    currentPage--;

                    loadNotifications();

                }

            }
        );

    }


    const nextButton =
        document.getElementById(
            "notification-next"
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

                    loadNotifications();

                }

            }
        );

    }


    updateActiveTab();

}


/* =========================================================
   LOAD UNREAD COUNT
   ========================================================= */

async function loadUnreadCount() {

    try {

        const response =
            await getUnreadNotificationCount();


        const count =
            Number(
                response?.data ??
                0
            );


        renderUnreadCount(
            count
        );


        /*
         * Đồng bộ luôn badge trên Header hiện tại.
         */

        updateHeaderNotificationBadge(
            count
        );


    } catch (error) {

        console.error(
            "LOAD UNREAD COUNT ERROR:",
            error
        );


        renderUnreadCount(
            0
        );

    }

}


/* =========================================================
   RENDER UNREAD COUNT
   ========================================================= */

function renderUnreadCount(
    count
) {

    const total =
        document.getElementById(
            "notification-unread-total"
        );


    if (total) {

        total.textContent =
            formatCount(
                count
            );

    }

}


function updateHeaderNotificationBadge(
    count
) {

    const badge =
        document.getElementById(
            "notification-count"
        );


    if (badge) {

        badge.textContent =
            formatCount(
                count
            );

    }


    const message =
        document.getElementById(
            "notification-message"
        );


    if (message) {

        if (
            count >
            0
        ) {

            message.textContent =
                `Bạn có ${count} thông báo chưa đọc.`;

        } else {

            message.textContent =
                "Không có thông báo mới.";

        }

    }

}


/* =========================================================
   LOAD NOTIFICATIONS
   ========================================================= */

async function loadNotifications() {

    if (loading) {

        return;

    }


    loading =
        true;


    hideNotificationError();

    showNotificationLoading();

    hideNotificationEmpty();


    try {

        const response =
            await getNotifications({

                page:
                    currentPage,

                size:
                    pageSize,

                unreadOnly:
                    currentFilter ===
                    "unread"

            });


        const pageData =
            response?.data;


        if (!pageData) {

            throw new Error(
                "API không trả về dữ liệu notification."
            );

        }


        renderNotifications(
            pageData
        );


        renderPagination(
            pageData
        );


        setText(
            "notification-result-summary",
            buildResultSummary(
                pageData
            )
        );


        await loadUnreadCount();


    } catch (error) {

        console.error(
            "LOAD NOTIFICATIONS ERROR:",
            error
        );


        showNotificationError(
            error?.message ||
            "Không thể tải danh sách thông báo."
        );


        renderNotificationList(
            []
        );


    } finally {

        loading =
            false;


        hideNotificationLoading();

    }

}


/* =========================================================
   REFRESH
   ========================================================= */

async function refreshNotifications() {

    const button =
        document.getElementById(
            "notification-refresh"
        );


    if (button) {

        button.disabled =
            true;

    }


    try {

        await Promise.all([

            loadNotifications(),

            loadUnreadCount()

        ]);

    } finally {

        if (button) {

            button.disabled =
                false;

        }

    }

}


/* =========================================================
   RENDER
   ========================================================= */

function renderNotifications(
    pageData
) {

    const notifications =
        Array.isArray(
            pageData?.content
        )
            ? pageData.content
            : [];


    renderNotificationList(
        notifications
    );


    if (
        notifications.length ===
        0
    ) {

        showNotificationEmpty();

    } else {

        hideNotificationEmpty();

    }

}


function renderNotificationList(
    notifications
) {

    const container =
        document.getElementById(
            "notification-list"
        );


    if (!container) {

        return;

    }


    container.innerHTML =
        "";


    notifications.forEach(
        function (
            notification
        ) {

            container.appendChild(
                createNotificationCard(
                    notification
                )
            );

        }
    );

}


/* =========================================================
   CREATE CARD
   ========================================================= */

function createNotificationCard(
    notification
) {

    const card =
        document.createElement(
            "article"
        );


    card.className =
        "notification-item";


    if (
        !Boolean(
            notification?.isRead
        )
    ) {

        card.classList.add(
            "notification-item-unread"
        );

    }


    const type =
        normalizeType(
            notification?.type
        );


    const iconConfig =
        getNotificationTypeConfig(
            type
        );


    const title =
        notification?.title ||
        notification?.typeDescription ||
        "Thông báo";


    const message =
        notification?.message ||
        "";


    const createdDate =
        formatDateTime(
            notification?.createdDate
        );


    const isRead =
        Boolean(
            notification?.isRead
        );


    card.innerHTML = `

        <div
            class="notification-item-icon
                   ${iconConfig.className}"
        >

            <i
                class="fa ${iconConfig.icon}"
            ></i>

        </div>


        <div
            class="notification-item-body"
        >

            <div
                class="notification-item-top"
            >

                <div>

                    <span
                        class="notification-item-type"
                    >
                        ${escapeHtml(
                            notification?.typeDescription ||
                            iconConfig.label
                        )}
                    </span>


                    <h3>
                        ${escapeHtml(
                            title
                        )}
                    </h3>

                </div>


                <span
                    class="notification-item-time"
                >
                    ${escapeHtml(
                        createdDate
                    )}
                </span>

            </div>


            <p
                class="notification-item-message"
            >
                ${nl2br(
                    escapeHtml(
                        message
                    )
                )}
            </p>


            <div
                class="notification-item-footer"
            >

                <span
                    class="notification-read-state"
                >

                    <i
                        class="fa ${
                            isRead
                                ? "fa-envelope-open-o"
                                : "fa-envelope-o"
                        }"
                    ></i>

                    ${
                        isRead
                            ? "Đã đọc"
                            : "Chưa đọc"
                    }

                </span>


                <div
                    class="notification-item-actions"
                >

                    <button
                        type="button"
                        class="notification-read-button"
                        data-notification-id="${escapeHtml(
                            notification?.id
                        )}"
                        ${
                            isRead
                                ? "disabled"
                                : ""
                        }
                    >

                        <i
                            class="fa fa-check"
                        ></i>

                        ${
                            isRead
                                ? "Đã đọc"
                                : "Đánh dấu đã đọc"
                        }

                    </button>

                </div>

            </div>

        </div>

    `;


    const readButton =
        card.querySelector(
            ".notification-read-button"
        );


    if (readButton) {

        readButton.addEventListener(
            "click",
            function () {

                handleMarkAsRead(
                    notification,
                    readButton,
                    card
                );

            }
        );

    }


    return card;

}


/* =========================================================
   MARK AS READ
   ========================================================= */

async function handleMarkAsRead(
    notification,
    button,
    card
) {

    const id =
        notification?.id;


    if (
        id ===
            null ||
        id ===
            undefined ||
        id ===
            ""
    ) {

        return;

    }


    if (
        Boolean(
            notification.isRead
        )
    ) {

        return;

    }


    if (button) {

        button.disabled =
            true;


        button.innerHTML = `
            <i
                class="fa fa-spinner fa-spin"
            ></i>

            Đang xử lý...
        `;

    }


    try {

        await markNotificationAsRead(
            id
        );


        notification.isRead =
            true;


        notification.readAt =
            new Date().toISOString();


        if (card) {

            card.classList.remove(
                "notification-item-unread"
            );

        }


        if (button) {

            button.innerHTML = `
                <i
                    class="fa fa-envelope-open-o"
                ></i>

                Đã đọc
            `;

        }


        const readState =
            card?.querySelector(
                ".notification-read-state"
            );


        if (readState) {

            readState.innerHTML = `
                <i
                    class="fa fa-envelope-open-o"
                ></i>

                Đã đọc
            `;

        }


        await loadUnreadCount();


        /*
         * Trong filter "Chưa đọc",
         * notification vừa đọc phải biến mất khỏi list.
         */

        if (
            currentFilter ===
            "unread"
        ) {

            await loadNotifications();

        }

    } catch (error) {

        console.error(
            "MARK NOTIFICATION READ ERROR:",
            error
        );


        if (button) {

            button.disabled =
                false;


            button.innerHTML = `
                <i
                    class="fa fa-check"
                ></i>

                Đánh dấu đã đọc
            `;

        }


        showNotificationError(
            error?.message ||
            "Không thể đánh dấu notification đã đọc."
        );

    }

}


/* =========================================================
   TYPE CONFIG
   ========================================================= */

function getNotificationTypeConfig(
    type
) {

    const configs = {

        CV_SUBMITTED: {

            icon:
                "fa-paper-plane",

            className:
                "notification-type-blue",

            label:
                "CV được gửi duyệt"

        },


        CV_TECH_LEAD_APPROVED: {

            icon:
                "fa-check-circle",

            className:
                "notification-type-green",

            label:
                "Tech Lead phê duyệt"

        },


        CV_TECH_LEAD_REJECTED: {

            icon:
                "fa-times-circle",

            className:
                "notification-type-red",

            label:
                "Tech Lead từ chối"

        },


        CV_HR_APPROVED: {

            icon:
                "fa-check-circle",

            className:
                "notification-type-purple",

            label:
                "HR phê duyệt"

        },


        CV_HR_REJECTED: {

            icon:
                "fa-times-circle",

            className:
                "notification-type-orange",

            label:
                "HR từ chối"

        },


        CV_UPDATE_REQUESTED: {

            icon:
                "fa-refresh",

            className:
                "notification-type-yellow",

            label:
                "Yêu cầu cập nhật CV"

        }

    };


    return (
        configs[type] ||
        {

            icon:
                "fa-bell-o",

            className:
                "notification-type-default",

            label:
                "Thông báo"

        }
    );

}


/* =========================================================
   PAGINATION
   ========================================================= */

function renderPagination(
    pageData
) {

    currentPage =
        Number(
            pageData?.number ??
            0
        );


    totalPages =
        Math.max(
            Number(
                pageData?.totalPages ||
                1
            ),
            1
        );


    setText(
        "notification-current-page",
        currentPage + 1
    );


    setText(
        "notification-total-pages",
        totalPages
    );


    const previous =
        document.getElementById(
            "notification-prev"
        );


    const next =
        document.getElementById(
            "notification-next"
        );


    if (previous) {

        previous.disabled =
            currentPage <=
            0;

    }


    if (next) {

        next.disabled =
            currentPage >=
            totalPages - 1;

    }


    renderPageNumbers();

}


function renderPageNumbers() {

    const container =
        document.getElementById(
            "notification-page-numbers"
        );


    if (!container) {

        return;

    }


    container.innerHTML =
        "";


    const maxPages =
        7;


    const start =
        Math.max(
            0,
            Math.min(
                currentPage - 3,
                Math.max(
                    totalPages -
                    maxPages,
                    0
                )
            )
        );


    const end =
        Math.min(
            totalPages,
            start +
            maxPages
        );


    for (
        let pageIndex =
            start;

        pageIndex <
        end;

        pageIndex++
    ) {

        const button =
            document.createElement(
                "button"
            );


        button.type =
            "button";


        button.className =
            "notification-page-number";


        button.textContent =
            pageIndex + 1;


        if (
            pageIndex ===
            currentPage
        ) {

            button.classList.add(
                "active"
            );

        }


        button.addEventListener(
            "click",
            function () {

                currentPage =
                    pageIndex;


                loadNotifications();

            }
        );


        container.appendChild(
            button
        );

    }

}


/* =========================================================
   TABS
   ========================================================= */

function updateActiveTab() {

    const all =
        document.getElementById(
            "notification-tab-all"
        );


    const unread =
        document.getElementById(
            "notification-tab-unread"
        );


    if (all) {

        all.classList.toggle(
            "active",
            currentFilter ===
            "all"
        );

    }


    if (unread) {

        unread.classList.toggle(
            "active",
            currentFilter ===
            "unread"
        );

    }


    setText(
        "notification-empty-title",
        currentFilter ===
            "unread"
            ? "Không có thông báo chưa đọc"
            : "Không có thông báo"
    );


    setText(
        "notification-empty-description",
        currentFilter ===
            "unread"
            ? "Bạn hiện không có notification chưa đọc."
            : "Hiện chưa có notification nào " +
              "trong phạm vi tài khoản của bạn."
    );

}


/* =========================================================
   STATES
   ========================================================= */

function showNotificationLoading() {

    const loading =
        document.getElementById(
            "notification-loading"
        );


    if (loading) {

        loading.style.display =
            "flex";

    }

}


function hideNotificationLoading() {

    const loading =
        document.getElementById(
            "notification-loading"
        );


    if (loading) {

        loading.style.display =
            "none";

    }

}


function showNotificationEmpty() {

    const empty =
        document.getElementById(
            "notification-empty"
        );


    if (empty) {

        empty.style.display =
            "flex";

    }

}


function hideNotificationEmpty() {

    const empty =
        document.getElementById(
            "notification-empty"
        );


    if (empty) {

        empty.style.display =
            "none";

    }

}


/* =========================================================
   ERROR
   ========================================================= */

function showNotificationError(
    message
) {

    const error =
        document.getElementById(
            "notification-error"
        );


    const text =
        document.getElementById(
            "notification-error-text"
        );


    if (!error) {

        return;

    }


    if (text) {

        text.textContent =
            message ||
            "Có lỗi xảy ra.";

    }


    error.classList.add(
        "is-visible"
    );


    error.style.setProperty(
        "display",
        "flex",
        "important"
    );

}


function hideNotificationError() {

    const error =
        document.getElementById(
            "notification-error"
        );


    if (error) {

        error.classList.remove(
            "is-visible"
        );


        error.style.setProperty(
            "display",
            "none",
            "important"
        );

    }

}


/* =========================================================
   SUMMARY
   ========================================================= */

function buildResultSummary(
    pageData
) {

    const total =
        Number(
            pageData?.totalElements ||
            0
        );


    if (
        total ===
        0
    ) {

        return "0 thông báo";

    }


    return `${total} thông báo`;

}


/* =========================================================
   HELPERS
   ========================================================= */

function normalizeRole(
    role
) {

    return String(
        role ||
        ""
    )
        .trim()
        .toUpperCase()
        .replace(
            /^ROLE_/,
            ""
        );

}


function normalizeType(
    type
) {

    return String(
        type ||
        ""
    )
        .trim()
        .toUpperCase();

}


function formatCount(
    count
) {

    const numeric =
        Number(
            count ||
            0
        );


    if (
        numeric >
        99
    ) {

        return "99+";

    }


    return String(
        numeric
    );

}


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

        return String(
            value
        );

    }


    return date.toLocaleString(
        "vi-VN",
        {
            day:
                "2-digit",

            month:
                "2-digit",

            year:
                "numeric",

            hour:
                "2-digit",

            minute:
                "2-digit"
        }
    );

}


function setText(
    id,
    value
) {

    const element =
        document.getElementById(
            id
        );


    if (element) {

        element.textContent =
            value ===
                null ||
            value ===
                undefined ||
            value ===
                ""
                ? "—"
                : String(
                    value
                );

    }

}


function escapeHtml(
    value
) {

    return String(
        value ??
        ""
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


function nl2br(
    value
) {

    return String(
        value ||
        ""
    ).replace(
        /\n/g,
        "<br>"
    );

}


/* =========================================================
   SIDEBAR TOGGLE
   ========================================================= */

function initializeSidebarToggle() {

    if (
        document.documentElement.dataset
            .notificationSidebarReady ===
        "true"
    ) {

        return;

    }


    document.addEventListener(
        "click",
        handleSidebarClick,
        true
    );


    document.documentElement.dataset
        .notificationSidebarReady =
        "true";


    document.body.dataset.sidebarState =
        "expanded";


    applySidebarLayout(
        false
    );


    window.addEventListener(
        "resize",
        function () {

            applySidebarLayout(
                false
            );

        }
    );

}


function handleSidebarClick(
    event
) {

    const toggle =
        event.target.closest(
            "#header-container " +
            ".sidebar-toggle-box .fa-bars"
        );


    if (!toggle) {

        return;

    }


    event.preventDefault();

    event.stopPropagation();

    event.stopImmediatePropagation();


    const sidebar =
        document.getElementById(
            "sidebar-container"
        );


    const main =
        document.getElementById(
            "main-content"
        );


    if (
        !sidebar ||
        !main
    ) {

        return;

    }


    const collapsed =
        document.body.dataset.sidebarState ===
        "collapsed";


    document.body.dataset.sidebarState =
        collapsed
            ? "expanded"
            : "collapsed";


    applySidebarLayout(
        true
    );

}


function applySidebarLayout(
    animated
) {

    const sidebar =
        document.getElementById(
            "sidebar-container"
        );


    const main =
        document.getElementById(
            "main-content"
        );


    if (
        !sidebar ||
        !main
    ) {

        return;

    }


    const collapsed =
        document.body.dataset.sidebarState ===
        "collapsed";


    if (animated) {

        sidebar.style.setProperty(
            "transition",
            "transform 420ms cubic-bezier(0.22,1,0.36,1)",
            "important"
        );


        main.style.setProperty(
            "transition",
            "width 420ms cubic-bezier(0.22,1,0.36,1), margin-left 420ms cubic-bezier(0.22,1,0.36,1)",
            "important"
        );

    }


    sidebar.style.setProperty(
        "transform",
        collapsed
            ? "translate3d(-240px,0,0)"
            : "translate3d(0,0,0)",
        "important"
    );


    if (
        window.matchMedia(
            "(max-width: 991px)"
        ).matches
    ) {

        main.style.setProperty(
            "width",
            "100%",
            "important"
        );


        main.style.setProperty(
            "margin-left",
            "0",
            "important"
        );


        return;

    }


    main.style.setProperty(
        "width",
        collapsed
            ? "100%"
            : "calc(100% - 240px)",
        "important"
    );


    main.style.setProperty(
        "margin-left",
        collapsed
            ? "0"
            : "240px",
        "important"
    );

}