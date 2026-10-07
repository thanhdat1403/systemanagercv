"use strict";


/* =========================================================
   SYSTEMANAGERCV
   CV INDEX - ROLE BASED
   FINAL LAYOUT VERSION
   ========================================================= */


/* =========================================================
   STATE
   ========================================================= */

let currentPage = 0;

const pageSize = 10;

let currentKeyword = "";

let currentStatus = "";

let totalPages = 1;

let currentRole = "";

let currentUser = null;

let currentEmployeeId = null;

let sidebarAnimating = false;


/* =========================================================
   ROLE CONFIG
   ========================================================= */

const CV_ROLE_CONFIG = {

    ADMIN: {

        eyebrow:
            "QUẢN TRỊ HỆ THỐNG",

        title:
            "Danh sách CV toàn hệ thống",

        description:
            "Quản lý, tìm kiếm và xem hồ sơ CV " +
            "của nhân viên trên toàn hệ thống.",

        scope:
            "Toàn hệ thống",

        noticeTitle:
            "Phạm vi quản trị",

        noticeDescription:
            "Bạn có thể xem danh sách CV trong " +
            "toàn bộ hệ thống.",

        tableTitle:
            "Tất cả hồ sơ CV",

        searchDescription:
            "Tìm theo mã nhân viên, tên nhân viên " +
            "hoặc trạng thái CV.",

        canEdit:
            false,

        canCreate:
            false
    },


    HR: {

        eyebrow:
            "QUẢN LÝ NHÂN SỰ",

        title:
            "Quản lý CV",

        description:
            "Theo dõi hồ sơ CV, trạng thái cập nhật " +
            "và tình trạng xử lý của nhân viên.",

        scope:
            "Toàn hệ thống",

        noticeTitle:
            "Phạm vi HR",

        noticeDescription:
            "Bạn có thể xem danh sách CV của nhân viên " +
            "trên toàn hệ thống.",

        tableTitle:
            "Hồ sơ CV nhân viên",

        searchDescription:
            "Tìm theo mã nhân viên, tên nhân viên " +
            "hoặc trạng thái CV.",

        canEdit:
            false,

        canCreate:
            false
    },


    TECH_LEAD: {

        eyebrow:
            "KIỂM DUYỆT CHUYÊN MÔN",

        title:
            "CV trong phòng ban",

        description:
            "Theo dõi hồ sơ CV của nhân viên " +
            "trong phòng ban được phân quyền.",

        scope:
            "Phòng ban của bạn",

        noticeTitle:
            "Phạm vi Tech Lead",

        noticeDescription:
            "Hệ thống chỉ trả về CV thuộc phòng ban " +
            "của bạn. Backend chịu trách nhiệm giới hạn dữ liệu.",

        tableTitle:
            "CV trong phòng ban",

        searchDescription:
            "Tìm trong phạm vi CV của phòng ban hiện tại.",

        canEdit:
            true,

        canCreate:
            false
    },


    EMPLOYEE: {

        eyebrow:
            "HỒ SƠ CÁ NHÂN",

        title:
            "CV của tôi",

        description:
            "Quản lý hồ sơ CV cá nhân và theo dõi " +
            "trạng thái xử lý của CV.",

        scope:
            "Chính tôi",

        noticeTitle:
            "Phạm vi cá nhân",

        noticeDescription:
            "Hệ thống chỉ trả về CV của chính tài khoản " +
            "nhân viên đang đăng nhập.",

        tableTitle:
            "Hồ sơ CV của tôi",

        searchDescription:
            "Tìm trong hồ sơ CV cá nhân.",

        canEdit:
            true,

        canCreate:
            true
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
                await initializeCVAuthentication();


            if (!authState) {

                return;

            }


            currentUser =
                authState.currentUser ||
                null;


            currentRole =
                normalizeRole(
                    authState.role
                );

            console.log(
                "CV PAGE CURRENT ROLE =",
                currentRole
            );


            if (
                ![
                    "ADMIN",
                    "HR",
                    "TECH_LEAD",
                    "EMPLOYEE"
                ].includes(
                    currentRole
                )
            ) {

                throw new Error(
                    "Role không hợp lệ: " +
                    currentRole
                );

            }


            currentEmployeeId =
                resolveCurrentEmployeeId(
                    currentUser
                );


            applyCVRoleUI(
                currentRole
            );


            initializeCVPageEvents();


            await loadCVs();

        } catch (error) {

            console.error(
                "CV INDEX INITIALIZATION ERROR:",
                error
            );


            showCVError(
                error?.message ||
                "Không thể khởi tạo trang danh sách CV."
            );

        }

    }
);


/* =========================================================
   LOAD COMMON COMPONENTS
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


/* =========================================================
   AUTHENTICATION
   ========================================================= */

async function initializeCVAuthentication() {

    if (
        typeof initializeHeaderUser !==
        "function"
    ) {

        throw new Error(
            "Không tìm thấy initializeHeaderUser()."
        );

    }


    const authState =
        await initializeHeaderUser();


    if (!authState) {

        throw new Error(
            "Không thể lấy thông tin người dùng."
        );

    }


    const role =
        normalizeRole(
            authState.role
        );


    const validRoles = [
        "ADMIN",
        "HR",
        "TECH_LEAD",
        "EMPLOYEE"
    ];


    if (
        !validRoles.includes(
            role
        )
    ) {

        throw new Error(
            "Role hiện tại không được phép truy cập CV List."
        );

    }


    if (
        typeof initializeSidebar ===
        "function"
    ) {

        initializeSidebar(
            role
        );

    } else {

        throw new Error(
            "Không tìm thấy initializeSidebar()."
        );

    }


    if (
        typeof setupLogout ===
        "function"
    ) {

        setupLogout();

    }


    initializeCVSidebarToggle();


    return {
        ...authState,
        role
    };

}


/* =========================================================
   ROLE UI
   ========================================================= */

function applyCVRoleUI(
    role
) {

    const config =
        CV_ROLE_CONFIG[
            role
        ];


    if (!config) {

        return;

    }


    setText(
        "cv-page-eyebrow",
        config.eyebrow
    );


    setText(
        "cv-page-title",
        config.title
    );


    setText(
        "cv-page-description",
        config.description
    );


    setText(
        "cv-scope-label",
        config.scope
    );


    setText(
        "cv-role-notice-title",
        config.noticeTitle
    );


    setText(
        "cv-role-notice-description",
        config.noticeDescription
    );


    setText(
        "cv-table-title",
        config.tableTitle
    );


    setText(
        "cv-search-description",
        config.searchDescription
    );


    const keywordInput =
        document.getElementById(
            "cv-keyword"
        );


    if (keywordInput) {

        if (
            role === "EMPLOYEE"
        ) {

            keywordInput.placeholder =
                "Tìm trong hồ sơ CV của tôi...";

        } else {

            keywordInput.placeholder =
                "Mã nhân viên hoặc tên nhân viên...";

        }

    }


    document.body.dataset.currentRole =
        role;


    document.body.dataset.cvScope =
        config.scope;

}


/* =========================================================
   LOAD COMPONENT
   ========================================================= */

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


    const html =
        await response.text();


    if (!html.trim()) {

        throw new Error(
            `${filePath} trả về nội dung rỗng.`
        );

    }


    container.innerHTML =
        html;

}


/* =========================================================
   SIDEBAR TOGGLE
   =========================================================

   DUY NHẤT một cơ chế:

   Sidebar:
       translate3d(-240px)

   Main:
       width 100%
       margin-left 0

   Không dùng:
       hide-left-bar
       merge-left
       transform trên main-content
   ========================================================= */

function initializeCVSidebarToggle() {

    if (
        document.documentElement.dataset
            .cvSidebarToggleReady ===
        "true"
    ) {

        return;

    }


    document.addEventListener(
        "click",
        handleCVSidebarClick,
        true
    );


    document.documentElement.dataset
        .cvSidebarToggleReady =
        "true";


    document.body.dataset.sidebarState =
        "expanded";


    applyCVSidebarLayout(
        false
    );

}


/* =========================================================
   SIDEBAR CLICK
   ========================================================= */

function handleCVSidebarClick(
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


    if (
        sidebarAnimating
    ) {

        return;

    }


    const sidebarContainer =
        document.getElementById(
            "sidebar-container"
        );


    const mainContent =
        document.getElementById(
            "main-content"
        );


    if (
        !sidebarContainer ||
        !mainContent
    ) {

        return;

    }


    const currentState =
        document.body.dataset.sidebarState ===
        "collapsed"
            ? "collapsed"
            : "expanded";


    const nextState =
        currentState ===
        "collapsed"
            ? "expanded"
            : "collapsed";


    sidebarAnimating =
        true;


    document.body.dataset.sidebarState =
        nextState;


    toggle.setAttribute(
        "aria-expanded",
        String(
            nextState !==
            "collapsed"
        )
    );


    applyCVSidebarLayout(
        true
    );


    window.setTimeout(
        function () {

            sidebarAnimating =
                false;


            refreshCVSidebarScroll();

        },
        430
    );

}


/* =========================================================
   APPLY SIDEBAR LAYOUT
   ========================================================= */

function applyCVSidebarLayout(
    animated
) {

    const sidebarContainer =
        document.getElementById(
            "sidebar-container"
        );


    const mainContent =
        document.getElementById(
            "main-content"
        );


    if (
        !sidebarContainer ||
        !mainContent
    ) {

        return;

    }


    const collapsed =
        document.body.dataset.sidebarState ===
        "collapsed";


    const isMobile =
        window.matchMedia(
            "(max-width: 991px)"
        ).matches;


    const duration =
        "420ms cubic-bezier(0.22, 1, 0.36, 1)";


    if (animated) {

        sidebarContainer.style.setProperty(
            "transition",
            `transform ${duration}`,
            "important"
        );


        mainContent.style.setProperty(
            "transition",
            `width ${duration}, margin-left ${duration}`,
            "important"
        );

    }


    /* =====================================================
       MOBILE
       ===================================================== */

    if (isMobile) {

        sidebarContainer.style.setProperty(
            "transform",
            collapsed
                ? "translate3d(-240px, 0, 0)"
                : "translate3d(0, 0, 0)",
            "important"
        );


        mainContent.style.setProperty(
            "margin-left",
            "0",
            "important"
        );


        mainContent.style.setProperty(
            "width",
            "100%",
            "important"
        );


        return;

    }


    /* =====================================================
       DESKTOP
       ===================================================== */

    sidebarContainer.style.setProperty(
        "transform",
        collapsed
            ? "translate3d(-240px, 0, 0)"
            : "translate3d(0, 0, 0)",
        "important"
    );


    mainContent.style.setProperty(
        "margin-left",
        collapsed
            ? "0"
            : "240px",
        "important"
    );


    mainContent.style.setProperty(
        "width",
        collapsed
            ? "100%"
            : "calc(100% - 240px)",
        "important"
    );

}


/* =========================================================
   WINDOW RESIZE
   ========================================================= */

window.addEventListener(
    "resize",
    function () {

        applyCVSidebarLayout(
            false
        );

    }
);


/* =========================================================
   NICE SCROLL
   ========================================================= */

function refreshCVSidebarScroll() {

    if (
        typeof jQuery ===
        "undefined"
    ) {

        return;

    }


    const navigation =
        document.querySelector(
            "#sidebar-container .leftside-navigation"
        );


    if (!navigation) {

        return;

    }


    try {

        const scroll =
            jQuery(
                navigation
            ).getNiceScroll();


        if (
            scroll &&
            typeof scroll.resize ===
            "function"
        ) {

            scroll.resize();

        }

    } catch (error) {

        console.warn(
            "CV Sidebar NiceScroll resize error:",
            error
        );

    }

}


/* =========================================================
   PAGE EVENTS
   ========================================================= */

function initializeCVPageEvents() {


    /* =====================================================
       SEARCH
       ===================================================== */

    const searchForm =
        document.getElementById(
            "cv-search-form"
        );


    if (searchForm) {

        searchForm.addEventListener(
            "submit",
            function (event) {

                event.preventDefault();


                currentKeyword =
                    document.getElementById(
                        "cv-keyword"
                    )?.value
                        ?.trim() ||
                    "";


                currentStatus =
                    document.getElementById(
                        "cv-status"
                    )?.value ||
                    "";


                currentPage =
                    0;


                loadCVs();

            }
        );

    }


    /* =====================================================
       REFRESH
       ===================================================== */

    const refreshButton =
        document.getElementById(
            "cv-refresh"
        );


    if (refreshButton) {

        refreshButton.addEventListener(
            "click",
            function () {

                const keywordInput =
                    document.getElementById(
                        "cv-keyword"
                    );


                const statusSelect =
                    document.getElementById(
                        "cv-status"
                    );


                if (keywordInput) {

                    keywordInput.value =
                        "";

                }


                if (statusSelect) {

                    statusSelect.value =
                        "";

                }


                currentKeyword =
                    "";


                currentStatus =
                    "";


                currentPage =
                    0;


                loadCVs();

            }
        );

    }


    /* =====================================================
       PREVIOUS
       ===================================================== */

    const previousButton =
        document.getElementById(
            "cv-prev"
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

                    loadCVs();

                }

            }
        );

    }


    /* =====================================================
       NEXT
       ===================================================== */

    const nextButton =
        document.getElementById(
            "cv-next"
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

                    loadCVs();

                }

            }
        );

    }

}


/* =========================================================
   LOAD CVS
   ========================================================= */

async function loadCVs() {

    hideCVMessages();


    const tableBody =
        document.getElementById(
            "cv-table-body"
        );


    const empty =
        document.getElementById(
            "cv-empty"
        );


    if (empty) {

        empty.style.display =
            "none";

    }


    if (tableBody) {

        tableBody.innerHTML = `
            <tr>
                <td
                    colspan="7"
                    class="cv-loading"
                >
                    <i class="fa fa-spinner fa-spin"></i>
                    Đang tải dữ liệu CV...
                </td>
            </tr>
        `;

    }


    try {

        if (
            typeof getCVs !==
            "function"
        ) {

            throw new Error(
                "Không tìm thấy getCVs()."
            );

        }


        const response =
            await getCVs({

                page:
                    currentPage,

                size:
                    pageSize,

                keyword:
                    currentKeyword,

                status:
                    currentStatus,

                sortBy:
                    "id",

                sortDirection:
                    "DESC"

            });


        const pageData =
            response?.data;


        if (!pageData) {

            throw new Error(
                "API không trả về dữ liệu phân trang CV."
            );

        }


        renderCVs(
            pageData
        );


        updateCVPagination(
            pageData
        );


    } catch (error) {

        console.error(
            "LOAD CVS ERROR:",
            error
        );


        if (tableBody) {

            tableBody.innerHTML =
                "";

        }


        showCVError(
            error?.message ||
            "Không thể tải danh sách CV."
        );

    }

}


/* =========================================================
   RENDER CVS
   ========================================================= */

function renderCVs(
    pageData
) {

    const tableBody =
        document.getElementById(
            "cv-table-body"
        );


    const empty =
        document.getElementById(
            "cv-empty"
        );


    const cvs =
        Array.isArray(
            pageData?.content
        )
            ? pageData.content
            : [];


    const totalElement =
        document.getElementById(
            "cv-total"
        );


    if (totalElement) {

        totalElement.textContent =
            pageData?.totalElements ??
            cvs.length;

    }


    if (
        cvs.length ===
        0
    ) {

        if (tableBody) {

            tableBody.innerHTML =
                "";

        }


        renderCVEmptyState();


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


    cvs.forEach(
        function (
            cv,
            index
        ) {

            const row =
                createCVRow(
                    cv,
                    index
                );


            tableBody.appendChild(
                row
            );

        }
    );

}


/* =========================================================
   CREATE ROW
   ========================================================= */

function createCVRow(
    cv,
    index
) {

    const row =
        document.createElement(
            "tr"
        );


    const stt =
        currentPage *
        pageSize +
        index +
        1;


    const cvId =
        cv?.id ??
        "";


    const employeeCode =
        cv?.employeeCode ??
        "---";


    const employeeName =
        cv?.employeeName ??
        "---";


    const department =
        [
            cv?.departmentCode,
            cv?.departmentName
        ]
            .filter(Boolean)
            .join(" - ") ||
        "---";


    const status =
        cv?.status ??
        "";


    const statusText =
        getCVStatusText(
            status
        );


    const statusClass =
        getCVStatusClass(
            status
        );


    const version =
        cv?.currentVersion ??
        "---";


    const ownCV =
        isCurrentUserCV(
            cv
        );


    const actionHtml =
        buildCVActions(
            cvId,
            ownCV
        );


    row.innerHTML = `

        <td class="cv-cell-stt">

            <span class="cv-stt">
                ${stt}
            </span>

        </td>


        <td>

            <span
                class="cv-code"
                title="${escapeHtml(
                    employeeCode
                )}"
            >
                ${escapeHtml(
                    employeeCode
                )}
            </span>

        </td>


        <td>

            <div class="cv-name">

                <span class="cv-name-icon">
                    <i
                        class="fa fa-file-text-o"
                    ></i>
                </span>


                <span
                    class="cv-name-text"
                    title="${escapeHtml(
                        employeeName
                    )}"
                >
                    ${escapeHtml(
                        employeeName
                    )}
                </span>

            </div>

        </td>


        <td>

            <span
                class="cv-text"
                title="${escapeHtml(
                    department
                )}"
            >
                ${escapeHtml(
                    department
                )}
            </span>

        </td>


        <td>

            <span
                class="cv-status ${statusClass}"
            >
                ${escapeHtml(
                    statusText
                )}
            </span>

        </td>


        <td>

            <span
                class="cv-version"
                title="${escapeHtml(
                    version
                )}"
            >
                ${escapeHtml(
                    version
                )}
            </span>

        </td>


        <td>

            <div class="cv-actions">

                ${actionHtml}

            </div>

        </td>

    `;


    return row;

}


/* =========================================================
   ACTIONS
   ========================================================= */

function buildCVActions(
    cvId,
    ownCV
) {

    if (!cvId) {

        return `
            <span class="cv-action-disabled">
                Không có ID
            </span>
        `;

    }


    const viewUrl =
        `./detail.html?id=${
            encodeURIComponent(
                cvId
            )
        }`;


    let html = `

        <a
            href="${viewUrl}"
            class="cv-action cv-action-view"
            title="Xem chi tiết CV"
        >

            <i
                class="fa fa-eye"
            ></i>

            <span>
                Xem
            </span>

        </a>

    `;


    if (
        (
            currentRole ===
            "EMPLOYEE" ||
            currentRole ===
            "TECH_LEAD"
        ) &&
        ownCV
    ) {

        const editUrl =
            `./edit.html?id=${
                encodeURIComponent(
                    cvId
                )
            }`;


        html += `

            <a
                href="${editUrl}"
                class="cv-action cv-action-edit"
                title="Chỉnh sửa CV"
            >

                <i
                    class="fa fa-pencil"
                ></i>

                <span>
                    Sửa
                </span>

            </a>

        `;

    }


    return html;

}


/* =========================================================
   CURRENT USER CV
   ========================================================= */

function isCurrentUserCV(
    cv
) {

    if (
        currentEmployeeId ===
        null ||
        currentEmployeeId ===
        undefined
    ) {

        return false;

    }


    if (
        cv?.employeeId ===
        null ||
        cv?.employeeId ===
        undefined
    ) {

        return false;

    }


    return (
        String(
            cv.employeeId
        ) ===
        String(
            currentEmployeeId
        )
    );

}


/* =========================================================
   EMPTY STATE
   ========================================================= */

function renderCVEmptyState() {

    const empty =
        document.getElementById(
            "cv-empty"
        );


    const title =
        document.getElementById(
            "cv-empty-title"
        );


    const description =
        document.getElementById(
            "cv-empty-description"
        );


    const createButton =
        document.getElementById(
            "cv-empty-create"
        );


    if (!empty) {

        return;

    }


    /*
     * ---------------------------------------------------------
     * LUÔN ẨN NÚT TẠO TRƯỚC
     * ---------------------------------------------------------
     */

    if (createButton) {

        createButton.style.setProperty(
            "display",
            "none",
            "important"
        );

    }


    /*
     * ---------------------------------------------------------
     * EMPLOYEE
     * ---------------------------------------------------------
     */

    if (
        currentRole ===
        "EMPLOYEE"
    ) {

        if (title) {

            title.textContent =
                "Bạn chưa có hồ sơ CV";

        }


        if (description) {

            description.textContent =
                "Bạn chưa có CV cá nhân. " +
                "Hãy tạo hồ sơ CV để bắt đầu quy trình.";

        }


        /*
         * Chỉ EMPLOYEE mới được thấy Tạo CV.
         */

        if (
            createButton &&
            CV_ROLE_CONFIG.EMPLOYEE.canCreate === true
        ) {

            createButton.style.setProperty(
                "display",
                "inline-flex",
                "important"
            );

        }

    }


    /*
     * ---------------------------------------------------------
     * TECH LEAD
     * ---------------------------------------------------------
     */

    else if (
        currentRole ===
        "TECH_LEAD"
    ) {

        if (title) {

            title.textContent =
                "Không tìm thấy CV trong phạm vi phòng ban";

        }


        if (description) {

            description.textContent =
                "Không có CV nào phù hợp với điều kiện " +
                "tìm kiếm trong phòng ban của bạn.";

        }

    }


    /*
     * ---------------------------------------------------------
     * HR
     * ---------------------------------------------------------
     */

    else if (
        currentRole ===
        "HR"
    ) {

        if (title) {

            title.textContent =
                "Không tìm thấy CV";

        }


        if (description) {

            description.textContent =
                "Không có hồ sơ CV nào phù hợp " +
                "với điều kiện tìm kiếm.";

        }

    }


    /*
     * ---------------------------------------------------------
     * ADMIN
     * ---------------------------------------------------------
     */

    else if (
        currentRole ===
        "ADMIN"
    ) {

        if (title) {

            title.textContent =
                "Không tìm thấy CV";

        }


        if (description) {

            description.textContent =
                "Không có hồ sơ CV nào phù hợp " +
                "với điều kiện tìm kiếm.";

        }


        /*
         * ADMIN TUYỆT ĐỐI KHÔNG ĐƯỢC CÓ TẠO CV.
         */

        if (createButton) {

            createButton.style.setProperty(
                "display",
                "none",
                "important"
            );

        }

    }


    /*
     * ---------------------------------------------------------
     * OTHER
     * ---------------------------------------------------------
     */

    else {

        if (title) {

            title.textContent =
                "Không tìm thấy CV";

        }


        if (description) {

            description.textContent =
                "Không có hồ sơ CV nào phù hợp " +
                "với điều kiện tìm kiếm.";

        }

    }


    /*
     * ---------------------------------------------------------
     * HIỂN THỊ EMPTY STATE
     * ---------------------------------------------------------
     */

    empty.classList.add(
        "is-visible"
    );

}


/* =========================================================
   STATUS
   ========================================================= */

function getCVStatusText(
    status
) {

    switch (
        normalizeStatus(
            status
        )
    ) {

        case "UPDATED":

            return "Đã cập nhật";


        case "NOT_UPDATED":

            return "Chưa cập nhật";


        case "REQUEST_CANCELLED":

            return "Hủy yêu cầu";


        default:

            return (
                status ||
                "---"
            );

    }

}


function getCVStatusClass(
    status
) {

    switch (
        normalizeStatus(
            status
        )
    ) {

        case "UPDATED":

            return "cv-status-updated";


        case "NOT_UPDATED":

            return "cv-status-not-updated";


        case "REQUEST_CANCELLED":

            return "cv-status-cancelled";


        default:

            return "cv-status-default";

    }

}


/* =========================================================
   PAGINATION
   ========================================================= */

function updateCVPagination(
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
        "cv-current-page",
        currentPage + 1
    );


    setText(
        "cv-total-pages",
        totalPages
    );


    const previousButton =
        document.getElementById(
            "cv-prev"
        );


    if (previousButton) {

        previousButton.disabled =
            currentPage <=
            0;

    }


    const nextButton =
        document.getElementById(
            "cv-next"
        );


    if (nextButton) {

        nextButton.disabled =
            currentPage >=
            totalPages - 1;

    }


    renderPageNumbers();

}


function renderPageNumbers() {

    const container =
        document.getElementById(
            "cv-page-numbers"
        );


    if (!container) {

        return;

    }


    container.innerHTML =
        "";


    const maxVisiblePages =
        7;


    let startPage =
        Math.max(
            0,
            currentPage - 3
        );


    let endPage =
        Math.min(
            totalPages,
            startPage +
            maxVisiblePages
        );


    if (
        endPage -
        startPage <
        maxVisiblePages
    ) {

        startPage =
            Math.max(
                0,
                endPage -
                maxVisiblePages
            );

    }


    for (
        let i =
            startPage;

        i <
        endPage;

        i++
    ) {

        const button =
            document.createElement(
                "button"
            );


        button.type =
            "button";


        button.className =
            "cv-pagination-number";


        button.textContent =
            i + 1;


        if (
            i ===
            currentPage
        ) {

            button.classList.add(
                "cv-pagination-active"
            );

        }


        button.addEventListener(
            "click",
            function () {

                currentPage =
                    i;

                loadCVs();

            }
        );


        container.appendChild(
            button
        );

    }

}


/* =========================================================
   HELPERS
   ========================================================= */

function getCurrentScopeLabel() {

    return (
        CV_ROLE_CONFIG[
            currentRole
        ]?.scope ||
        "---"
    );

}


function resolveCurrentEmployeeId(
    user
) {

    const value =
        user?.employeeId ??
        user?.employee?.id ??
        user?.employeeInfo?.id ??
        null;


    if (
        value ===
        null ||
        value ===
        undefined ||
        value ===
        ""
    ) {

        return null;

    }


    return value;

}


function normalizeRole(
    value
) {

    return String(
        value ||
        ""
    )
        .trim()
        .toUpperCase()
        .replace(
            /^ROLE_/,
            ""
        );

}


function normalizeStatus(
    value
) {

    return String(
        value ||
        ""
    )
        .trim()
        .toUpperCase();

}


function setText(
    elementId,
    value
) {

    const element =
        document.getElementById(
            elementId
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


function hideCVMessages() {

    const error =
        document.getElementById(
            "cv-error"
        );


    const success =
        document.getElementById(
            "cv-success"
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


    if (success) {

        success.classList.remove(
            "is-visible"
        );

        success.style.setProperty(
            "display",
            "none",
            "important"
        );

    }

}


function showCVError(
    message
) {

    const element =
        document.getElementById(
            "cv-error"
        );


    const text =
        document.getElementById(
            "cv-error-text"
        );


    if (!element) {

        return;

    }


    if (text) {

        text.textContent =
            message ||
            "Có lỗi xảy ra.";

    }


    element.classList.add(
        "is-visible"
    );


    element.style.setProperty(
        "display",
        "flex",
        "important"
    );

}


function showCVSuccess(
    message
) {

    const element =
        document.getElementById(
            "cv-success"
        );


    const text =
        document.getElementById(
            "cv-success-text"
        );


    if (!element) {

        return;

    }


    if (text) {

        text.textContent =
            message ||
            "Thành công.";

    }


    element.classList.add(
        "is-visible"
    );


    element.style.setProperty(
        "display",
        "flex",
        "important"
    );

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