"use strict";


/* =========================================================
   DASHBOARD CONFIG
   ========================================================= */

const DASHBOARD_VALID_ROLES = [
    "ADMIN",
    "HR",
    "TECH_LEAD",
    "EMPLOYEE"
];

const DASHBOARD_WORKFLOW_STATUSES = [
    "DRAFT",
    "PENDING_TECH_LEAD",
    "PENDING_HR",
    "TECH_LEAD_REJECTED",
    "HR_REJECTED"
];


/* =========================================================
   DASHBOARD INIT
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        console.log(
            "=== DASHBOARD INIT ==="
        );

        try {

            /*
             * -------------------------------------------------
             * HEADER
             * -------------------------------------------------
             */

            await loadComponent(
                "header-container",
                "../../components/header.html"
            );


            /*
             * -------------------------------------------------
             * SIDEBAR
             * -------------------------------------------------
             */

            await loadComponent(
                "sidebar-container",
                "../../components/sidebar.html"
            );


            /*
             * -------------------------------------------------
             * FOOTER
             * -------------------------------------------------
             */

            await loadComponent(
                "footer-container",
                "../../components/footer.html"
            );


            /*
             * -------------------------------------------------
             * AUTH
             * -------------------------------------------------
             */

            if (
                typeof initializeHeaderUser !==
                "function"
            ) {

                throw new Error(
                    "Không tìm thấy initializeHeaderUser(). " +
                    "Hãy kiểm tra auth.js."
                );

            }


            const authState =
                await initializeHeaderUser();


            const currentRole =
                normalizeDashboardRole(
                    authState?.role ||
                    ""
                );


            /*
             * -------------------------------------------------
             * VALIDATE ROLE
             * -------------------------------------------------
             */

            if (
                !DASHBOARD_VALID_ROLES.includes(
                    currentRole
                )
            ) {

                throw new Error(
                    "Role Dashboard không hợp lệ: " +
                    currentRole
                );

            }


            /*
             * -------------------------------------------------
             * ACCOUNT
             * -------------------------------------------------
             */

            updateDashboardAccount(
                authState?.displayName ||
                "User",
                currentRole
            );


            /*
             * -------------------------------------------------
             * ROLE UI
             * -------------------------------------------------
             */

            updateDashboardByRole(
                currentRole
            );


            /*
             * -------------------------------------------------
             * SIDEBAR ROLE
             * -------------------------------------------------
             */

            if (
                typeof initializeSidebar ===
                "function"
            ) {

                initializeSidebar(
                    currentRole
                );

            }


            /*
             * -------------------------------------------------
             * SIDEBAR TOGGLE
             * -------------------------------------------------
             */

            initializeDashboardSidebar();


            /*
             * -------------------------------------------------
             * LOGOUT
             * -------------------------------------------------
             */

            if (
                typeof setupLogout ===
                "function"
            ) {

                setupLogout();

            }


            /*
             * -------------------------------------------------
             * DASHBOARD DATA
             * -------------------------------------------------
             */

            await loadDashboardData(
                currentRole,
                authState?.currentUser ||
                null
            );


            console.log(
                "DASHBOARD ROLE =",
                currentRole
            );

            console.log(
                "=== DASHBOARD INIT COMPLETE ==="
            );


        } catch (error) {

            console.error(
                "DASHBOARD INITIALIZATION ERROR:",
                error
            );


            if (
                typeof redirectToLogin ===
                "function" &&
                /auth|role|authenticated|initializeHeaderUser/i.test(
                    String(
                        error?.message ||
                        ""
                    )
                )
            ) {

                redirectToLogin();

            }

        }

    }
);


/* =========================================================
   COMPONENT LOADER
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
            `Không thể tải ${filePath}. ` +
            `HTTP ${response.status}`
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

}


/* =========================================================
   UPDATE DASHBOARD ACCOUNT
   ========================================================= */

function updateDashboardAccount(
    displayName,
    role
) {

    const safeName =
        String(
            displayName ||
            "User"
        )
            .trim() ||
        "User";


    const username =
        document.getElementById(
            "dashboard-account-username"
        );


    const roleElement =
        document.getElementById(
            "dashboard-account-role"
        );


    const welcomeUsername =
        document.getElementById(
            "dashboard-username"
        );


    if (username) {

        username.textContent =
            safeName;

    }


    if (welcomeUsername) {

        welcomeUsername.textContent =
            safeName;

    }


    if (roleElement) {

        roleElement.textContent =
            formatDashboardRole(
                role
            );

    }

}


/* =========================================================
   UPDATE DASHBOARD BY ROLE
   ========================================================= */

function updateDashboardByRole(
    role
) {

    const normalizedRole =
        normalizeDashboardRole(
            role
        );


    const summaryTitle =
        document.getElementById(
            "dashboard-role-summary-title"
        );


    const summaryDescription =
        document.getElementById(
            "dashboard-role-summary-description"
        );


    const welcomeDescription =
        document.getElementById(
            "dashboard-welcome-description"
        );


    const roleConfig = {

        ADMIN: {

            title:
                "Quản trị hệ thống",

            description:
                "Theo dõi User, phòng ban, nhân viên " +
                "và tình trạng hồ sơ CV trên toàn hệ thống.",

            welcome:
                "Theo dõi tổng quan hệ thống và các cảnh báo " +
                "liên quan đến nhân sự và hồ sơ CV."

        },


        HR: {

            title:
                "Quản lý nhân sự và CV",

            description:
                "Theo dõi nhân viên, CV, trạng thái chưa cập nhật " +
                "và các bước HR cần xử lý.",

            welcome:
                "Theo dõi nhân viên, hồ sơ CV và các yêu cầu " +
                "cập nhật đang cần xử lý."

        },


        TECH_LEAD: {

            title:
                "Kiểm duyệt chuyên môn",

            description:
                "Quản lý CV cá nhân và kiểm duyệt CV trong phạm vi " +
                "phòng ban được phân quyền.",

            welcome:
                "Theo dõi CV cá nhân và các CV đang chờ " +
                "bạn review trong phạm vi phòng ban."

        },


        EMPLOYEE: {

            title:
                "Hồ sơ cá nhân",

            description:
                "Quản lý CV của chính mình và theo dõi trạng thái " +
                "phiên bản đang xử lý.",

            welcome:
                "Theo dõi hồ sơ CV cá nhân và các yêu cầu " +
                "cập nhật được gửi đến."

        }

    };


    const config =
        roleConfig[
            normalizedRole
        ];


    if (
        config &&
        summaryTitle
    ) {

        summaryTitle.textContent =
            config.title;

    }


    if (
        config &&
        summaryDescription
    ) {

        summaryDescription.textContent =
            config.description;

    }


    if (
        config &&
        welcomeDescription
    ) {

        welcomeDescription.textContent =
            config.welcome;

    }


    /*
     * -------------------------------------------------------
     * ROLE BADGES
     * -------------------------------------------------------
     */

    [
        "dashboard-role-admin",
        "dashboard-role-hr",
        "dashboard-role-tech",
        "dashboard-role-employee"
    ].forEach(
        hideElement
    );


    const badgeMap = {

        ADMIN:
            "dashboard-role-admin",

        HR:
            "dashboard-role-hr",

        TECH_LEAD:
            "dashboard-role-tech",

        EMPLOYEE:
            "dashboard-role-employee"

    };


    const activeBadge =
        badgeMap[
            normalizedRole
        ];


    if (activeBadge) {

        showElement(
            activeBadge
        );

    }


    /*
     * -------------------------------------------------------
     * ROLE PANELS
     * -------------------------------------------------------
     */

    const panels =
        document.querySelectorAll(
            "#dashboard-role-content " +
            "[data-dashboard-role]"
        );


    panels.forEach(
        function (panel) {

            const panelRole =
                normalizeDashboardRole(
                    panel.dataset
                        .dashboardRole ||
                    ""
                );


            panel.style.display =
                panelRole ===
                normalizedRole
                    ? "block"
                    : "none";

        }
    );

}


/* =========================================================
   DASHBOARD DATA
   ========================================================= */

async function loadDashboardData(
    role,
    currentUser
) {

    switch (
        normalizeDashboardRole(
            role
        )
    ) {

        case "ADMIN":

            await loadAdminDashboard();

            break;


        case "HR":

            await loadHrDashboard();

            break;


        case "TECH_LEAD":

            await loadTechLeadDashboard(
                currentUser
            );

            break;


        case "EMPLOYEE":

            await loadEmployeeDashboard();

            break;


        default:

            break;

    }

}


/* =========================================================
   ADMIN DASHBOARD
   ========================================================= */

async function loadAdminDashboard() {

    const results =
        await Promise.allSettled([

            safeApiCall(
                "Users",
                function () {

                    return searchUsers({
                        page:
                            0,
                        size:
                            1
                    });

                }
            ),


            safeApiCall(
                "Employees",
                function () {

                    return getEmployees({
                        page:
                            0,
                        size:
                            1
                    });

                }
            ),


            safeApiCall(
                "Departments",
                function () {

                    return getDepartments({
                        page:
                            0,
                        size:
                            1
                    });

                }
            ),


            safeApiCall(
                "CVs",
                function () {

                    return getCVs({
                        page:
                            0,
                        size:
                            1
                    });

                }
            ),


            safeApiCall(
                "Not updated CVs",
                function () {

                    return getCVs({
                        page:
                            0,
                        size:
                            1,
                        status:
                            "NOT_UPDATED"
                    });

                }
            )

        ]);


    const [
        users,
        employees,
        departments,
        cvs,
        notUpdated
    ] =
        results.map(
            extractSettledValue
        );


    setText(
        "dashboard-admin-user-count",
        formatPageTotal(
            users
        )
    );


    setText(
        "dashboard-admin-employee-count",
        formatPageTotal(
            employees
        )
    );


    setText(
        "dashboard-admin-department-count",
        formatPageTotal(
            departments
        )
    );


    setText(
        "dashboard-admin-cv-count",
        formatPageTotal(
            cvs
        )
    );


    setText(
        "dashboard-admin-no-cv-count",
        formatNoCvSummary(
            employees,
            cvs
        )
    );


    const alert =
        document.getElementById(
            "dashboard-admin-no-cv-count"
        );


    if (
        alert &&
        notUpdated?.data?.totalElements !==
        undefined
    ) {

        alert.title =
            `Hiện có ${notUpdated.data.totalElements} ` +
            "CV đang ở trạng thái Chưa cập nhật.";

    }

}


/* =========================================================
   HR DASHBOARD
   ========================================================= */

async function loadHrDashboard() {

    const results =
        await Promise.allSettled([

            safeApiCall(
                "Employees",
                function () {

                    return getEmployees({
                        page:
                            0,
                        size:
                            1
                    });

                }
            ),


            safeApiCall(
                "CVs",
                function () {

                    return getCVs({
                        page:
                            0,
                        size:
                            1
                    });

                }
            ),


            safeApiCall(
                "Not updated CVs",
                function () {

                    return getCVs({
                        page:
                            0,
                        size:
                            1,
                        status:
                            "NOT_UPDATED"
                    });

                }
            ),


            safeApiCall(
                "Workflow CV details",
                function () {

                    return loadWorkflowDetails({
                        limit:
                            50
                    });

                }
            )

        ]);


    const [
        employees,
        cvs,
        notUpdated,
        workflowDetails
    ] =
        results.map(
            extractSettledValue
        );


    setText(
        "dashboard-hr-employee-count",
        formatPageTotal(
            employees
        )
    );


    setText(
        "dashboard-hr-cv-count",
        formatPageTotal(
            cvs
        )
    );


    setText(
        "dashboard-hr-update-request-count",
        formatPageTotal(
            notUpdated
        )
    );


    const pendingHr =
        countWorkflow(
            workflowDetails,
            "PENDING_HR"
        );


    setText(
        "dashboard-hr-pending-count",
        pendingHr.label
    );


    setText(
        "dashboard-hr-no-cv-count",
        formatNoCvSummary(
            employees,
            cvs
        )
    );

}


/* =========================================================
   TECH LEAD DASHBOARD
   ========================================================= */

async function loadTechLeadDashboard(
    currentUser
) {

    const ownCv =
        await safeApiCall(
            "Own CV",
            function () {

                return findOwnCv(
                    currentUser
                );

            }
        );


    if (ownCv?.data) {

        try {

            const detail =
                await getCVById(
                    ownCv.data.id
                );


            const cv =
                detail?.data ||
                null;


            setText(
                "dashboard-tech-my-cv-status",
                formatPersonalCvStatus(
                    cv
                )
            );


        } catch (error) {

            console.warn(
                "TECH LEAD OWN CV DETAIL ERROR:",
                error
            );


            setText(
                "dashboard-tech-my-cv-status",
                "Không tải được"
            );

        }

    } else {

        setText(
            "dashboard-tech-my-cv-status",
            "Chưa có CV"
        );

    }


    const workflowDetails =
        await safeApiCall(
            "Tech Lead workflow",
            function () {

                return loadWorkflowDetails({
                    limit:
                        50
                });

            }
        );


    const pending =
        countWorkflow(
            workflowDetails,
            "PENDING_TECH_LEAD"
        );


    const approved =
        countWorkflow(
            workflowDetails,
            "PENDING_HR"
        );


    const rejected =
        countWorkflowAny(
            workflowDetails,
            [
                "TECH_LEAD_REJECTED"
            ]
        );


    setText(
        "dashboard-tech-pending-count",
        pending.label
    );


    setText(
        "dashboard-tech-approved-count",
        approved.label
    );


    setText(
        "dashboard-tech-rejected-count",
        rejected.label
    );


    setText(
        "dashboard-tech-review-count",
        `${pending.label} CV đang chờ review.`
    );

}


/* =========================================================
   EMPLOYEE DASHBOARD
   ========================================================= */

async function loadEmployeeDashboard() {

    const ownCv =
        await safeApiCall(
            "Own CV",
            function () {

                return getCVs({
                    page:
                        0,
                    size:
                        1
                });

            }
        );


    const firstCv =
        ownCv?.data?.content?.[0] ||
        null;


    if (!firstCv) {

        setText(
            "dashboard-employee-cv-status",
            "Chưa có CV"
        );


        setText(
            "dashboard-employee-cv-description",
            "Bạn chưa có hồ sơ CV. " +
            "Hãy tạo CV để bắt đầu quy trình."
        );


        setText(
            "dashboard-employee-cv-title",
            "CV của tôi"
        );


        setFeatureCreateButtonByCv(
            null
        );


        return;

    }


    try {

        const detailResponse =
            await getCVById(
                firstCv.id
            );


        const cv =
            detailResponse?.data ||
            firstCv;


        setText(
            "dashboard-employee-cv-status",
            formatPersonalCvStatus(
                cv
            )
        );


        setText(
            "dashboard-employee-cv-description",
            formatWorkflowDescription(
                cv
            )
        );


        setText(
            "dashboard-employee-cv-title",
            `CV của tôi${cv.currentVersion ? ` - ${cv.currentVersion}` : ""}`
        );


        setFeatureCreateButtonByCv(
            cv
        );


    } catch (error) {

        console.warn(
            "EMPLOYEE CV DETAIL ERROR:",
            error
        );


        setText(
            "dashboard-employee-cv-status",
            formatCvStatus(
                firstCv.status
            )
        );

    }

}


/* =========================================================
   FIND OWN CV
   ========================================================= */

async function findOwnCv(
    currentUser
) {

    const response =
        await getCVs({
            page:
                0,
            size:
                1000
        });


    const page =
        response?.data;


    const items =
        Array.isArray(
            page?.content
        )
            ? page.content
            : [];


    if (
        items.length ===
        0
    ) {

        return {
            data:
                null
        };

    }


    const currentEmployeeId =
        currentUser?.employeeId ??
        currentUser?.employee?.id ??
        currentUser?.employeeInfo?.id ??
        null;


    const own =
        currentEmployeeId ===
        null

            ? items[0]

            : items.find(
                function (cv) {

                    return (
                        String(
                            cv.employeeId
                        ) ===
                        String(
                            currentEmployeeId
                        )
                    );

                }
            );


    return {
        data:
            own ||
            null
    };

}


/* =========================================================
   WORKFLOW DETAILS
   ========================================================= */

async function loadWorkflowDetails(
    {
        limit =
            50
    } = {}
) {

    const response =
        await getCVs({
            page:
                0,
            size:
                1000
        });


    const page =
        response?.data;


    const items =
        Array.isArray(
            page?.content
        )
            ? page.content
            : [];


    const selected =
        items.slice(
            0,
            limit
        );


    const details =
        await Promise.all(
            selected.map(
                async function (item) {

                    try {

                        const response =
                            await getCVById(
                                item.id
                            );


                        return (
                            response?.data ||
                            null
                        );


                    } catch (error) {

                        console.warn(
                            "WORKFLOW DETAIL LOAD ERROR:",
                            item?.id,
                            error
                        );


                        return null;

                    }

                }
            )
        );


    return {

        details:
            details.filter(
                Boolean
            ),

        total:
            Number(
                page?.totalElements ||
                items.length
            ),

        sampled:
            selected.length

    };

}


/* =========================================================
   WORKFLOW COUNT
   ========================================================= */

function countWorkflow(
    result,
    status
) {

    if (
        !result ||
        !Array.isArray(
            result.details
        )
    ) {

        return {
            label:
                "—",
            exact:
                false
        };

    }


    const count =
        result.details.filter(
            function (cv) {

                return (
                    normalizeStatus(
                        cv?.workflowVersionStatus
                    ) ===
                    status
                );

            }
        ).length;


    if (
        result.total >
        result.sampled
    ) {

        return {
            label:
                `≥ ${count}`,
            exact:
                false
        };

    }


    return {
        label:
            String(count),
        exact:
            true
    };

}


function countWorkflowAny(
    result,
    statuses
) {

    if (
        !result ||
        !Array.isArray(
            result.details
        )
    ) {

        return {
            label:
                "—",
            exact:
                false
        };

    }


    const normalizedStatuses =
        statuses.map(
            normalizeStatus
        );


    const count =
        result.details.filter(
            function (cv) {

                return normalizedStatuses.includes(
                    normalizeStatus(
                        cv?.workflowVersionStatus
                    )
                );

            }
        ).length;


    if (
        result.total >
        result.sampled
    ) {

        return {
            label:
                `≥ ${count}`,
            exact:
                false
        };

    }


    return {
        label:
            String(count),
        exact:
            true
    };

}


/* =========================================================
   SAFE API
   ========================================================= */

async function safeApiCall(
    label,
    callback
) {

    try {

        return await callback();

    } catch (error) {

        console.error(
            `DASHBOARD ${label} ERROR:`,
            error
        );


        return null;

    }

}


function extractSettledValue(
    result
) {

    if (
        result?.status ===
        "fulfilled"
    ) {

        return result.value;

    }


    return null;

}


/* =========================================================
   FORMAT PAGE TOTAL
   ========================================================= */

function formatPageTotal(
    response
) {

    const total =
        response?.data?.totalElements;


    if (
        total === null ||
        total === undefined
    ) {

        return "—";

    }


    return String(
        total
    );

}


/* =========================================================
   FORMAT NO CV
   ========================================================= */

function formatNoCvSummary(
    employeesResponse,
    cvsResponse
) {

    const employeeTotal =
        employeesResponse
            ?.data
            ?.totalElements;


    const cvTotal =
        cvsResponse
            ?.data
            ?.totalElements;


    if (
        employeeTotal ===
            null ||
        employeeTotal ===
            undefined ||
        cvTotal ===
            null ||
        cvTotal ===
            undefined
    ) {

        return "Chưa có đủ dữ liệu";

    }


    if (
        Number(cvTotal) >=
        Number(employeeTotal)
    ) {

        return "0 nhân viên";

    }


    return (
        `${Math.max(
            0,
            Number(employeeTotal) -
            Number(cvTotal)
        )} nhân viên`
    );

}


/* =========================================================
   PERSONAL CV STATUS
   ========================================================= */

function formatPersonalCvStatus(
    cv
) {

    const workflow =
        normalizeStatus(
            cv?.workflowVersionStatus
        );


    if (workflow) {

        const workflowText = {

            DRAFT:
                "Bản nháp",

            PENDING_TECH_LEAD:
                "Chờ Tech Lead",

            PENDING_HR:
                "Chờ HR",

            TECH_LEAD_REJECTED:
                "Bị Tech Lead từ chối",

            HR_REJECTED:
                "Bị HR từ chối"

        };


        if (
            workflowText[
                workflow
            ]
        ) {

            return workflowText[
                workflow
            ];

        }

    }


    return formatCvStatus(
        cv?.status
    );

}


/* =========================================================
   WORKFLOW DESCRIPTION
   ========================================================= */

function formatWorkflowDescription(
    cv
) {

    const workflow =
        normalizeStatus(
            cv?.workflowVersionStatus
        );


    if (
        workflow ===
        "PENDING_TECH_LEAD"
    ) {

        return (
            "CV đã được gửi và đang chờ " +
            "Tech Lead kiểm duyệt chuyên môn."
        );

    }


    if (
        workflow ===
        "PENDING_HR"
    ) {

        return (
            "CV đã qua bước chuyên môn " +
            "và đang chờ HR kiểm duyệt."
        );

    }


    if (
        workflow ===
        "TECH_LEAD_REJECTED"
    ) {

        return (
            `Tech Lead đã từ chối CV${
                cv?.rejectionReason
                    ? `: ${cv.rejectionReason}`
                    : "."
            }`
        );

    }


    if (
        workflow ===
        "HR_REJECTED"
    ) {

        return (
            `HR đã từ chối CV${
                cv?.rejectionReason
                    ? `: ${cv.rejectionReason}`
                    : "."
            }`
        );

    }


    if (
        workflow ===
        "DRAFT"
    ) {

        return (
            "CV hiện đang ở bản nháp. " +
            "Bạn có thể tiếp tục chỉnh sửa và gửi duyệt."
        );

    }


    return (
        `Trạng thái CV hiện tại: ${
            formatCvStatus(
                cv?.status
            )
        }.`
    );

}


/* =========================================================
   CV STATUS
   ========================================================= */

function formatCvStatus(
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
        case "CANCELED_REQUEST":

            return "Hủy yêu cầu";


        default:

            return (
                status ||
                "Chưa có dữ liệu"
            );

    }

}


/* =========================================================
   EMPLOYEE CV BUTTON
   ========================================================= */

function setFeatureCreateButtonByCv(
    cv
) {

    const createButton =
        document.querySelector(
            "#dashboard-role-employee-content " +
            ".smcv-role-button.primary"
        );


    if (!createButton) {

        return;

    }


    if (!cv) {

        createButton.style.display =
            "";

        createButton.textContent =
            "Tạo CV";

        return;

    }


    createButton.style.display =
        "";

    createButton.textContent =
        "Cập nhật / chỉnh sửa CV";

}


/* =========================================================
   DOM UTILITIES
   ========================================================= */

function setText(
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


    element.textContent =
        value === null ||
        value === undefined ||
        value === ""
            ? "—"
            : String(value);

}


function showElement(
    elementId
) {

    const element =
        document.getElementById(
            elementId
        );


    if (element) {

        element.style.display =
            "";

    }

}


function hideElement(
    elementId
) {

    const element =
        document.getElementById(
            elementId
        );


    if (element) {

        element.style.display =
            "none";

    }

}


/* =========================================================
   NORMALIZE
   ========================================================= */

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


function normalizeDashboardRole(
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


function formatDashboardRole(
    role
) {

    switch (
        normalizeDashboardRole(
            role
        )
    ) {

        case "ADMIN":

            return "ADMIN";


        case "HR":

            return "HR";


        case "TECH_LEAD":

            return "TECH LEAD";


        case "EMPLOYEE":

            return "EMPLOYEE";


        default:

            return (
                role ||
                "USER"
            );

    }

}


/* =========================================================
   SIDEBAR
   FINAL ROBUST VERSION
   ========================================================= */

const DASHBOARD_SIDEBAR_ANIMATION_MS =
    560;

let dashboardSidebarAnimating =
    false;


/* =========================================================
   INITIALIZE SIDEBAR
   ========================================================= */

function initializeDashboardSidebar() {

    const body =
        document.body;


    const sidebarContainer =
        document.getElementById(
            "sidebar-container"
        );


    const mainContent =
        document.getElementById(
            "main-content"
        );


    if (
        !body ||
        !sidebarContainer ||
        !mainContent
    ) {

        console.error(
            "DASHBOARD SIDEBAR: " +
            "Không tìm thấy DOM cần thiết."
        );

        return;

    }


    /*
     * -------------------------------------------------------
     * XÓA TRẠNG THÁI CŨ
     * -------------------------------------------------------
     */

    body.classList.remove(
        "sidebar-collapsed",
        "sidebar-mobile-open",
        "dashboard-sidebar-collapsed"
    );


    mainContent.classList.remove(
        "merge-left"
    );


    const sidebar =
        document.getElementById(
            "sidebar"
        );


    if (sidebar) {

        sidebar.classList.remove(
            "hide-left-bar"
        );

    }


    /*
     * -------------------------------------------------------
     * SIDEBAR INITIAL STYLE
     * -------------------------------------------------------
     *
     * Inline !important để thắng những
     * CSS cũ đang có !important.
     */

    sidebarContainer.style.setProperty(
        "transform",
        "translate3d(0,0,0)",
        "important"
    );


    sidebarContainer.style.setProperty(
        "transition",
        `transform ${DASHBOARD_SIDEBAR_ANIMATION_MS}ms cubic-bezier(0.22,1,0.36,1)`,
        "important"
    );


    sidebarContainer.style.setProperty(
        "will-change",
        "transform"
    );


    sidebarContainer.style.setProperty(
        "backface-visibility",
        "hidden"
    );


    sidebarContainer.style.setProperty(
        "-webkit-backface-visibility",
        "hidden"
    );


    /*
     * -------------------------------------------------------
     * MAIN INITIAL STYLE
     * -------------------------------------------------------
     */

    mainContent.style.setProperty(
        "width",
        "auto",
        "important"
    );


    mainContent.style.setProperty(
        "margin-left",
        "240px",
        "important"
    );


    mainContent.style.setProperty(
        "transition",
        `margin-left ${DASHBOARD_SIDEBAR_ANIMATION_MS}ms cubic-bezier(0.22,1,0.36,1)`,
        "important"
    );


    mainContent.style.setProperty(
        "will-change",
        "margin-left"
    );


    mainContent.style.setProperty(
        "background",
        "transparent",
        "important"
    );


    /*
     * -------------------------------------------------------
     * STATE
     * -------------------------------------------------------
     */

    body.dataset.dashboardSidebar =
        "expanded";


    /*
     * -------------------------------------------------------
     * ARIA
     * -------------------------------------------------------
     */

    const toggle =
        document.querySelector(
            "#header-container " +
            ".sidebar-toggle-box .fa-bars"
        );


    if (toggle) {

        toggle.setAttribute(
            "aria-expanded",
            "true"
        );

    }


    /*
     * -------------------------------------------------------
     * EVENT
     * -------------------------------------------------------
     */

    if (
        document.documentElement.dataset
            .dashboardSidebarReady ===
        "true"
    ) {

        return;

    }


    document.addEventListener(
        "click",
        handleDashboardSidebarClick,
        true
    );


    document.documentElement.dataset
        .dashboardSidebarReady =
        "true";

}


/* =========================================================
   SIDEBAR CLICK
   ========================================================= */

function handleDashboardSidebarClick(
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


    /*
     * Ngăn tất cả handler cũ xử lý cùng click.
     */

    event.preventDefault();

    event.stopPropagation();

    event.stopImmediatePropagation();


    /*
     * Không cho click liên tục trong
     * lúc animation chưa hoàn tất.
     */

    if (
        dashboardSidebarAnimating
    ) {

        return;

    }


    const body =
        document.body;


    const sidebarContainer =
        document.getElementById(
            "sidebar-container"
        );


    const mainContent =
        document.getElementById(
            "main-content"
        );


    const sidebar =
        document.getElementById(
            "sidebar"
        );


    if (
        !body ||
        !sidebarContainer ||
        !mainContent
    ) {

        console.error(
            "DASHBOARD SIDEBAR: " +
            "Không tìm thấy DOM."
        );

        return;

    }


    /*
     * -------------------------------------------------------
     * STATE HIỆN TẠI
     * -------------------------------------------------------
     */

    const isCollapsed =
        body.dataset.dashboardSidebar ===
        "collapsed";


    const nextState =
        isCollapsed
            ? "expanded"
            : "collapsed";


    dashboardSidebarAnimating =
        true;


    /*
     * -------------------------------------------------------
     * DỌN STATE LEGACY
     * -------------------------------------------------------
     */

    body.classList.remove(
        "sidebar-collapsed",
        "sidebar-mobile-open",
        "dashboard-sidebar-collapsed"
    );


    mainContent.classList.remove(
        "merge-left"
    );


    if (sidebar) {

        sidebar.classList.remove(
            "hide-left-bar"
        );

    }


    /*
     * -------------------------------------------------------
     * MAIN CONTENT
     * -------------------------------------------------------
     */

    mainContent.style.setProperty(
        "width",
        "auto",
        "important"
    );


    mainContent.style.setProperty(
        "transition",
        `margin-left ${DASHBOARD_SIDEBAR_ANIMATION_MS}ms cubic-bezier(0.22,1,0.36,1)`,
        "important"
    );


    /*
     * -------------------------------------------------------
     * SIDEBAR
     * -------------------------------------------------------
     */

    sidebarContainer.style.setProperty(
        "transition",
        `transform ${DASHBOARD_SIDEBAR_ANIMATION_MS}ms cubic-bezier(0.22,1,0.36,1)`,
        "important"
    );


    sidebarContainer.style.setProperty(
        "will-change",
        "transform"
    );


    mainContent.style.setProperty(
        "will-change",
        "margin-left"
    );


    /*
     * -------------------------------------------------------
     * STATE ATTRIBUTE
     * -------------------------------------------------------
     */

    body.dataset.dashboardSidebar =
        nextState;


    /*
     * -------------------------------------------------------
     * ACCESSIBILITY
     * -------------------------------------------------------
     */

    toggle.setAttribute(
        "aria-expanded",
        String(
            nextState !==
            "collapsed"
        )
    );


    /*
     * -------------------------------------------------------
     * RIGHT PANEL
     * -------------------------------------------------------
     */

    const container =
        document.getElementById(
            "container"
        );


    if (container) {

        container.classList.remove(
            "open-right-panel"
        );

    }


    const rightSidebar =
        document.querySelector(
            ".right-sidebar"
        );


    if (rightSidebar) {

        rightSidebar.classList.remove(
            "open-right-bar"
        );

    }


    /*
     * -------------------------------------------------------
     * FORCE BROWSER FRAME
     * -------------------------------------------------------
     *
     * Hai requestAnimationFrame giúp browser
     * ghi nhận state ban đầu trước khi chuyển
     * sang state mới.
     */

    requestAnimationFrame(
        function () {

            requestAnimationFrame(
                function () {

                    if (
                        nextState ===
                        "collapsed"
                    ) {

                        /*
                         * Sidebar đi sang trái.
                         */

                        sidebarContainer.style.setProperty(
                            "transform",
                            "translate3d(-240px,0,0)",
                            "important"
                        );


                        /*
                         * Main Content mở toàn màn hình.
                         */

                        mainContent.style.setProperty(
                            "margin-left",
                            "0px",
                            "important"
                        );


                    } else {

                        /*
                         * Sidebar quay về.
                         */

                        sidebarContainer.style.setProperty(
                            "transform",
                            "translate3d(0,0,0)",
                            "important"
                        );


                        /*
                         * Main Content trả lại
                         * khoảng Sidebar.
                         */

                        mainContent.style.setProperty(
                            "margin-left",
                            "240px",
                            "important"
                        );

                    }

                }
            );

        }
    );


    /*
     * -------------------------------------------------------
     * AFTER ANIMATION
     * -------------------------------------------------------
     */

    window.setTimeout(
        function () {

            dashboardSidebarAnimating =
                false;


            /*
             * Resize NiceScroll sau khi
             * animation đã hoàn thành.
             */

            if (
                typeof jQuery !==
                "undefined"
            ) {

                const navigation =
                    document.querySelector(
                        "#sidebar-container " +
                        ".leftside-navigation"
                    );


                if (navigation) {

                    try {

                        const niceScroll =
                            jQuery(
                                navigation
                            )
                                .getNiceScroll();


                        if (
                            niceScroll &&
                            typeof niceScroll.resize ===
                            "function"
                        ) {

                            niceScroll.resize();

                        }

                    } catch (error) {

                        console.warn(
                            "DASHBOARD SIDEBAR " +
                            "NICE SCROLL ERROR:",
                            error
                        );

                    }

                }

            }

        },
        DASHBOARD_SIDEBAR_ANIMATION_MS +
        50
    );

}


/* =========================================================
   PLACEHOLDER ROUTES
   ========================================================= */

document.addEventListener(
    "click",
    function (event) {

        const routeElement =
            event.target.closest(
                "[data-route]"
            );


        if (!routeElement) {

            return;

        }


        event.preventDefault();


        console.warn(
            "Dashboard route chưa được triển khai:",
            routeElement.dataset.route ||
            ""
        );

    }
);