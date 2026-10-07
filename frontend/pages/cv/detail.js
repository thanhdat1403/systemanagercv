"use strict";


/* =========================================================
   SYSTEMANAGERCV
   CV DETAIL - STEP 4
   ROLE BASED
   ========================================================= */


/* =========================================================
   STATE
   ========================================================= */

let cvId = null;

let currentCV = null;

let currentUser = null;

let currentRole = "";

let currentEmployeeId = null;

let pendingRejectType = null;

let sidebarAnimating = false;


/* =========================================================
   ROLE CONFIG
   ========================================================= */

const CV_DETAIL_ROLE_CONFIG = {

    ADMIN: {

        eyebrow:
            "QUẢN TRỊ HỆ THỐNG",

        title:
            "Chi tiết CV",

        description:
            "Xem hồ sơ CV và trạng thái xử lý " +
            "trong phạm vi toàn hệ thống.",

        contextTitle:
            "Quyền quản trị",

        contextDescription:
            "Bạn đang xem CV với quyền quản trị hệ thống. " +
            "Các thao tác workflow không được hiển thị cho ADMIN."

    },


    HR: {

        eyebrow:
            "QUẢN LÝ NHÂN SỰ",

        title:
            "Chi tiết CV",

        description:
            "Kiểm tra hồ sơ CV và xử lý các phiên bản " +
            "đang chờ HR phê duyệt.",

        contextTitle:
            "Quyền HR",

        contextDescription:
            "HR có thể kiểm tra và phê duyệt hoặc từ chối " +
            "CV khi trạng thái đang chờ HR."
    },


    TECH_LEAD: {

        eyebrow:
            "KIỂM DUYỆT CHUYÊN MÔN",

        title:
            "Chi tiết CV",

        description:
            "Xem hồ sơ CV trong phạm vi phòng ban " +
            "và kiểm duyệt chuyên môn.",

        contextTitle:
            "Quyền Tech Lead",

        contextDescription:
            "Bạn chỉ được truy cập CV thuộc phạm vi " +
            "phòng ban của mình và có quyền review chuyên môn."
    },


    EMPLOYEE: {

        eyebrow:
            "HỒ SƠ CÁ NHÂN",

        title:
            "CV của tôi",

        description:
            "Xem hồ sơ CV cá nhân và theo dõi trạng thái workflow.",

        contextTitle:
            "Quyền cá nhân",

        contextDescription:
            "Bạn đang xem hồ sơ CV của chính mình."
    }

};


/* =========================================================
   DOM READY
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        cvId =
            new URLSearchParams(
                window.location.search
            ).get("id");


        if (
            !cvId ||
            !/^\d+$/.test(
                String(cvId)
            )
        ) {

            showCVError(
                "CV ID không hợp lệ hoặc chưa được truyền trên URL."
            );

            hideLoading();

            return;

        }


        try {

            await loadHeader();


            /*
             * Auth phải được lấy trước Sidebar
             * để Sidebar nhận chính xác role.
             */

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


            currentEmployeeId =
                resolveCurrentEmployeeId(
                    currentUser
                );


            validateRole();


            await loadSidebar();


            if (
                typeof initializeSidebar ===
                "function"
            ) {

                initializeSidebar(
                    currentRole
                );

            }


            initializeCVSidebarToggle();


            if (
                typeof setupLogout ===
                "function"
            ) {

                setupLogout();

            }


            await loadFooter();


            applyRoleUI();


            initializeCVEvents();


            await loadCVDetail();


        } catch (error) {

            console.error(
                "CV DETAIL INITIALIZATION ERROR:",
                error
            );


            showCVError(
                error?.message ||
                "Không thể khởi tạo trang chi tiết CV."
            );

        } finally {

            hideLoading();

        }

    }
);


/* =========================================================
   LOAD COMMON COMPONENTS
   ========================================================= */

async function loadComponent(
    containerId,
    url
) {

    const container =
        document.getElementById(
            containerId
        );


    if (!container) {

        throw new Error(
            `Không tìm thấy container: ${containerId}`
        );

    }


    const response =
        await fetch(
            url,
            {
                method:
                    "GET",

                cache:
                    "no-cache"
            }
        );


    if (!response.ok) {

        throw new Error(
            `Không thể tải ${url}. HTTP ${response.status}`
        );

    }


    container.innerHTML =
        await response.text();

}


async function loadHeader() {

    await loadComponent(
        "header-container",
        "../../components/header.html"
    );

}


async function loadSidebar() {

    await loadComponent(
        "sidebar-container",
        "../../components/sidebar.html"
    );

}


async function loadFooter() {

    await loadComponent(
        "footer-container",
        "../../components/footer.html"
    );

}


/* =========================================================
   ROLE VALIDATION
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
            "Role hiện tại không hợp lệ: " +
            currentRole
        );

    }


    console.log(
        "CV DETAIL CURRENT ROLE =",
        currentRole
    );

}


/* =========================================================
   ROLE UI
   ========================================================= */

function applyRoleUI() {

    const config =
        CV_DETAIL_ROLE_CONFIG[
            currentRole
        ];


    if (!config) {

        return;

    }


    setText(
        "cv-detail-eyebrow",
        config.eyebrow
    );


    setText(
        "cv-detail-page-title",
        config.title
    );


    setText(
        "cv-detail-page-description",
        config.description
    );


    setText(
        "cv-role-context-title",
        config.contextTitle
    );


    setText(
        "cv-role-context-description",
        config.contextDescription
    );


    document.body.dataset.currentRole =
        currentRole;


    document.body.dataset.cvDetailScope =
        getRoleScopeText();

}


/* =========================================================
   ROLE SCOPE
   ========================================================= */

function getRoleScopeText() {

    switch (
        currentRole
    ) {

        case "ADMIN":

        case "HR":

            return "TOÀN HỆ THỐNG";


        case "TECH_LEAD":

            return "PHÒNG BAN";


        case "EMPLOYEE":

            return "CÁ NHÂN";


        default:

            return "---";

    }

}


/* =========================================================
   SIDEBAR TOGGLE
   ========================================================= */

function initializeCVSidebarToggle() {

    if (
        document.documentElement.dataset
            .cvDetailSidebarReady ===
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
        .cvDetailSidebarReady =
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


    if (
        sidebarAnimating
    ) {

        return;

    }


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


    applySidebarLayout(
        true
    );


    window.setTimeout(
        function () {

            sidebarAnimating =
                false;


            refreshSidebarScroll();

        },
        430
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


    const mobile =
        window.matchMedia(
            "(max-width: 991px)"
        ).matches;


    const transition =
        "420ms cubic-bezier(0.22, 1, 0.36, 1)";


    if (animated) {

        sidebar.style.setProperty(
            "transition",
            `transform ${transition}`,
            "important"
        );


        main.style.setProperty(
            "transition",
            `width ${transition}, margin-left ${transition}`,
            "important"
        );

    }


    if (mobile) {

        sidebar.style.setProperty(
            "transform",
            collapsed
                ? "translate3d(-240px,0,0)"
                : "translate3d(0,0,0)",
            "important"
        );


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


    sidebar.style.setProperty(
        "transform",
        collapsed
            ? "translate3d(-240px,0,0)"
            : "translate3d(0,0,0)",
        "important"
    );


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


function refreshSidebarScroll() {

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
            "Sidebar resize error:",
            error
        );

    }

}


/* =========================================================
   PAGE EVENTS
   ========================================================= */

function initializeCVEvents() {

    const actions =
        document.getElementById(
            "cv-actions"
        );


    if (actions) {

        actions.addEventListener(
            "click",
            handleCVActionClick
        );

    }


    document.addEventListener(
        "click",
        function (event) {

            const element =
                event.target.closest(
                    "[data-action]"
                );


            if (!element) {

                return;

            }


            const action =
                element.dataset.action;


            if (
                action ===
                "close-reject-modal"
            ) {

                closeRejectModal();

            }

        }
    );


    const confirmReject =
        document.getElementById(
            "cv-confirm-reject"
        );


    if (confirmReject) {

        confirmReject.addEventListener(
            "click",
            confirmRejectCV
        );

    }


    const rejectionInput =
        document.getElementById(
            "cv-rejection-input"
        );


    if (rejectionInput) {

        rejectionInput.addEventListener(
            "keydown",
            function (event) {

                if (
                    (
                        event.ctrlKey ||
                        event.metaKey
                    ) &&
                    event.key ===
                    "Enter"
                ) {

                    confirmRejectCV();

                }

            }
        );

    }

}


/* =========================================================
   LOAD DETAIL
   ========================================================= */

async function loadCVDetail() {

    hideCVError();

    hideCVSuccess();


    try {

        const response =
            await getCVById(
                cvId
            );


        currentCV =
            response?.data ||
            null;


        if (!currentCV) {

            throw new Error(
                "API không trả về dữ liệu CV."
            );

        }


        console.log(
            "CV DETAIL RESPONSE:",
            currentCV
        );


        renderCV(
            currentCV
        );


        showDetail();

    } catch (error) {

        console.error(
            "LOAD CV DETAIL ERROR:",
            error
        );


        showCVError(
            error?.message ||
            "Không thể tải chi tiết CV."
        );

    }

}


/* =========================================================
   RENDER CV
   ========================================================= */

function renderCV(
    cv
) {

    renderEmployeeHeader(
        cv
    );


    renderVersions(
        cv
    );


    renderWorkflow(
        cv
    );


    renderProfile(
        cv.profile
    );


    renderSkills(
        cv.skills
    );


    renderEducations(
        cv.educations
    );


    renderExperiences(
        cv.experiences
    );


    renderProjects(
        cv.projects
    );


    renderCertificates(
        cv.certificates
    );


    renderLanguages(
        cv.languages
    );


    renderActions(
        cv
    );

}


/* =========================================================
   EMPLOYEE HEADER
   ========================================================= */

function renderEmployeeHeader(
    cv
) {

    setText(
        "cv-employee-name",
        cv.employeeName ||
        "---"
    );


    setText(
        "cv-employee-code",
        cv.employeeCode ||
        "---"
    );


    setText(
        "cv-employee-email",
        cv.employeeEmail ||
        "---"
    );


    const department =
        [
            cv.departmentCode,
            cv.departmentName
        ]
            .filter(Boolean)
            .join(" - ");


    setText(
        "cv-department",
        department ||
        "---"
    );


    const avatar =
        document.getElementById(
            "cv-avatar"
        );


    if (avatar) {

        avatar.src =
            cv.profile?.avatarUrl ||
            "../../assets/images/1.png";


        avatar.onerror =
            function () {

                this.src =
                    "../../assets/images/1.png";

            };

    }

}


/* =========================================================
   VERSION / STATUS
   ========================================================= */

function renderVersions(
    cv
) {

    setText(
        "cv-current-version",
        cv.currentVersion ||
        "Chưa có"
    );


    setText(
        "cv-workflow-version",
        cv.workflowVersion ||
        "Không có"
    );


    setStatusBadge(
        "cv-status",
        cv.status,
        "cv-status-badge"
    );


    setStatusBadge(
        "cv-current-version-status",
        cv.currentVersionStatus,
        "cv-version-status"
    );


    setStatusBadge(
        "cv-workflow-version-status",
        cv.workflowVersionStatus,
        "cv-version-status"
    );

}


/* =========================================================
   WORKFLOW
   ========================================================= */

function renderWorkflow(
    cv
) {

    const status =
        cv.workflowVersionStatus ||
        cv.currentVersionStatus ||
        cv.status ||
        "";


    const description =
        document.getElementById(
            "cv-workflow-description"
        );


    const source =
        document.getElementById(
            "cv-content-source"
        );


    const rejectionAlert =
        document.getElementById(
            "cv-rejection-alert"
        );


    const rejectionReason =
        document.getElementById(
            "cv-rejection-reason"
        );


    if (description) {

        description.textContent =
            getWorkflowDescription(
                status
            );

    }


    const hasWorkflow =
        Boolean(
            cv.workflowVersionId
        );


    if (source) {

        source.textContent =
            hasWorkflow
                ? `Nội dung đang hiển thị theo phiên bản workflow ${cv.workflowVersion || "mới nhất"}.`
                : `Nội dung đang hiển thị theo phiên bản chính thức ${cv.currentVersion || "hiện tại"}.`;

    }


    if (
        rejectionAlert &&
        rejectionReason
    ) {

        if (
            cv.rejectionReason
        ) {

            rejectionReason.textContent =
                cv.rejectionReason;


            rejectionAlert.classList.add(
                "is-visible"
            );

        } else {

            rejectionReason.textContent =
                "";


            rejectionAlert.classList.remove(
                "is-visible"
            );

        }

    }


    renderWorkflowTimeline(
        status
    );

}


/* =========================================================
   WORKFLOW DESCRIPTION
   ========================================================= */

function getWorkflowDescription(
    status
) {

    switch (
        normalizeStatus(
            status
        )
    ) {

        case "DRAFT":

            return (
                "CV đang ở bản nháp và chưa " +
                "được gửi kiểm duyệt."
            );


        case "PENDING_TECH_LEAD":

            return (
                "CV đang chờ Tech Lead " +
                "kiểm tra chuyên môn."
            );


        case "TECH_LEAD_REJECTED":

            return (
                "Tech Lead đã từ chối CV. " +
                "Employee cần chỉnh sửa lại."
            );


        case "PENDING_HR":

            return (
                "CV đã qua Tech Lead và đang " +
                "chờ HR kiểm tra, phê duyệt."
            );


        case "HR_REJECTED":

            return (
                "HR đã từ chối CV. " +
                "Employee cần chỉnh sửa lại."
            );


        case "OFFICIAL":

            return (
                "CV hiện tại là phiên bản chính thức."
            );


        case "REQUEST_CANCELLED":

            return (
                "Yêu cầu cập nhật CV đã được hủy."
            );


        default:

            return (
                "Chưa có phiên bản workflow " +
                "đang chờ xử lý."
            );

    }

}


/* =========================================================
   WORKFLOW TIMELINE
   ========================================================= */

function renderWorkflowTimeline(
    status
) {

    const container =
        document.getElementById(
            "cv-workflow-timeline"
        );


    if (!container) {

        return;

    }


    const normalized =
        normalizeStatus(
            status
        );


    const steps = [

        {
            key:
                "DRAFT",

            title:
                "Draft",

            description:
                "Bản nháp"
        },

        {
            key:
                "PENDING_TECH_LEAD",

            title:
                "Tech Lead",

            description:
                "Duyệt chuyên môn"
        },

        {
            key:
                "PENDING_HR",

            title:
                "HR",

            description:
                "Duyệt hồ sơ"
        },

        {
            key:
                "OFFICIAL",

            title:
                "Official",

            description:
                "Chính thức"
        }

    ];


    /*
     * ---------------------------------------------------------
     * XÁC ĐỊNH BƯỚC HIỆN TẠI
     * ---------------------------------------------------------
     */

    let currentIndex =
        0;


    if (
        normalized ===
        "DRAFT"
    ) {

        currentIndex =
            0;

    } else if (
        normalized ===
            "PENDING_TECH_LEAD" ||
        normalized ===
            "TECH_LEAD_REJECTED"
    ) {

        currentIndex =
            1;

    } else if (
        normalized ===
            "PENDING_HR" ||
        normalized ===
            "HR_REJECTED"
    ) {

        currentIndex =
            2;

    } else if (
        normalized ===
        "OFFICIAL"
    ) {

        currentIndex =
            3;

    }


    /*
     * ---------------------------------------------------------
     * XÁC ĐỊNH BƯỚC BỊ REJECT
     * ---------------------------------------------------------
     */

    const rejectedAt =
        normalized ===
            "TECH_LEAD_REJECTED"

            ? 1

            : normalized ===
                "HR_REJECTED"

                ? 2

                : -1;


    /*
     * ---------------------------------------------------------
     * RENDER
     * ---------------------------------------------------------
     */

    container.innerHTML =
        steps.map(
            function (
                step,
                index
            ) {

                let stateClass =
                    "";


                /*
                 * =================================================
                 * CÁC BƯỚC ĐÃ HOÀN THÀNH
                 * =================================================
                 *
                 * Đặc biệt:
                 *
                 * OFFICIAL = index 3
                 *
                 * => Official cũng phải là COMPLETE
                 * => hiển thị dấu ✓ màu xanh
                 */

                if (
                    index <=
                    currentIndex &&
                    normalized ===
                    "OFFICIAL"
                ) {

                    stateClass +=
                        " is-complete";

                }


                /*
                 * =================================================
                 * CÁC BƯỚC ĐÃ QUA
                 * =================================================
                 */

                else if (
                    index <
                    currentIndex
                ) {

                    stateClass +=
                        " is-complete";

                }


                /*
                 * =================================================
                 * BƯỚC ĐANG CHỜ XỬ LÝ
                 * =================================================
                 */

                if (
                    index ===
                    currentIndex &&
                    normalized !==
                    "OFFICIAL" &&
                    rejectedAt ===
                    -1
                ) {

                    stateClass +=
                        " is-current";

                }


                /*
                 * =================================================
                 * BƯỚC BỊ TỪ CHỐI
                 * =================================================
                 */

                if (
                    index ===
                    rejectedAt
                ) {

                    stateClass +=
                        " is-rejected";

                }


                /*
                 * =================================================
                 * CHỌN ICON
                 * =================================================
                 */

                let icon =
                    "fa-circle-o";


                /*
                 * OFFICIAL:
                 * Tất cả 4 bước hoàn thành.
                 */

                if (
                    normalized ===
                    "OFFICIAL" &&
                    index <=
                    currentIndex
                ) {

                    icon =
                        "fa-check";

                }


                /*
                 * REJECT
                 */

                else if (
                    index ===
                    rejectedAt
                ) {

                    icon =
                        "fa-times";

                }


                /*
                 * CÁC BƯỚC ĐÃ HOÀN THÀNH
                 */

                else if (
                    index <
                    currentIndex
                ) {

                    icon =
                        "fa-check";

                }


                /*
                 * =================================================
                 * HTML
                 * =================================================
                 */

                return `

                    <div
                        class="cv-workflow-step ${stateClass.trim()}"
                    >

                        <div
                            class="cv-workflow-dot"
                        >

                            <i
                                class="fa ${icon}"
                            ></i>

                        </div>


                        <div
                            class="cv-workflow-title"
                        >
                            ${escapeHtml(
                                step.title
                            )}
                        </div>


                        <div
                            class="cv-workflow-state"
                        >
                            ${escapeHtml(
                                step.description
                            )}
                        </div>

                    </div>

                `;

            }
        ).join("");

}


/* =========================================================
   PROFILE
   ========================================================= */

function renderProfile(
    profile
) {

    const container =
        document.getElementById(
            "cv-profile"
        );


    if (!container) {

        return;

    }


    if (!profile) {

        container.innerHTML =
            emptyState(
                "Chưa có thông tin hồ sơ."
            );

        return;

    }


    container.innerHTML = `

        ${profileItem(
            "Headline",
            profile.headline
        )}

        ${profileItem(
            "Địa chỉ",
            profile.address
        )}

        ${profileItem(
            "Mục tiêu nghề nghiệp",
            profile.careerObjective,
            true
        )}

    `;

}


function profileItem(
    label,
    value,
    fullWidth = false
) {

    return `

        <div
            class="cv-profile-item ${
                fullWidth
                    ? "full-width"
                    : ""
            }"
        >

            <div
                class="cv-field-label"
            >
                ${escapeHtml(
                    label
                )}
            </div>


            <div
                class="cv-field-value"
            >
                ${nl2br(
                    escapeHtml(
                        value ||
                        "Chưa cập nhật"
                    )
                )}
            </div>

        </div>

    `;

}


/* =========================================================
   SKILLS
   ========================================================= */

function renderSkills(
    skills
) {

    const container =
        document.getElementById(
            "cv-skills"
        );


    if (!container) {

        return;

    }


    if (
        !Array.isArray(
            skills
        ) ||
        skills.length ===
        0
    ) {

        container.innerHTML =
            emptyState(
                "Chưa có kỹ năng."
            );

        return;

    }


    container.innerHTML = `

        <div
            class="cv-tags"
        >

            ${skills.map(
                function (
                    skill
                ) {

                    return `

                        <div
                            class="cv-skill-tag"
                        >

                            <div
                                class="cv-skill-name"
                            >
                                ${escapeHtml(
                                    skill.skillName ||
                                    "---"
                                )}
                            </div>


                            <div
                                class="cv-skill-level"
                            >
                                ${escapeHtml(
                                    skill.skillLevel ||
                                    "Chưa xác định"
                                )}
                            </div>


                            <div
                                class="cv-skill-description"
                            >
                                ${nl2br(
                                    escapeHtml(
                                        skill.description ||
                                        ""
                                    )
                                )}
                            </div>

                        </div>

                    `;

                }
            ).join("")}

        </div>

    `;

}


/* =========================================================
   EDUCATIONS
   ========================================================= */

function renderEducations(
    items
) {

    renderListSection(
        "cv-educations",
        items,
        "Chưa có thông tin học vấn.",
        function (
            item
        ) {

            const dateRange =
                formatDateRange(
                    item.startDate,
                    item.endDate
                );


            return `

                <div
                    class="cv-list-item"
                >

                    <div
                        class="cv-list-item-title"
                    >
                        ${escapeHtml(
                            item.schoolName ||
                            "---"
                        )}
                    </div>


                    <div
                        class="cv-list-item-subtitle"
                    >
                        ${escapeHtml(
                            item.major ||
                            "---"
                        )}

                        ${
                            item.degree
                                ? " • " +
                                  escapeHtml(
                                      item.degree
                                  )
                                : ""
                        }
                    </div>


                    <div
                        class="cv-item-meta"
                    >

                        ${
                            dateRange
                                ? `<span>
                                    <i class="fa fa-calendar"></i>
                                    ${escapeHtml(
                                        dateRange
                                    )}
                                   </span>`
                                : ""
                        }

                    </div>


                    <div
                        class="cv-list-item-description"
                    >
                        ${nl2br(
                            escapeHtml(
                                item.description ||
                                ""
                            )
                        )}
                    </div>

                </div>

            `;

        }
    );

}


/* =========================================================
   EXPERIENCES
   ========================================================= */

function renderExperiences(
    items
) {

    renderListSection(
        "cv-experiences",
        items,
        "Chưa có kinh nghiệm làm việc.",
        function (
            item
        ) {

            const dateRange =
                formatDateRange(
                    item.startDate,
                    item.endDate
                );


            return `

                <div
                    class="cv-list-item"
                >

                    <div
                        class="cv-list-item-title"
                    >
                        ${escapeHtml(
                            item.position ||
                            "---"
                        )}
                    </div>


                    <div
                        class="cv-list-item-subtitle"
                    >
                        ${escapeHtml(
                            item.companyName ||
                            "---"
                        )}
                    </div>


                    <div
                        class="cv-item-meta"
                    >

                        ${
                            dateRange
                                ? `<span>
                                    <i class="fa fa-calendar"></i>
                                    ${escapeHtml(
                                        dateRange
                                    )}
                                   </span>`
                                : ""
                        }

                    </div>


                    <div
                        class="cv-list-item-description"
                    >
                        ${nl2br(
                            escapeHtml(
                                item.description ||
                                ""
                            )
                        )}
                    </div>

                </div>

            `;

        }
    );

}


/* =========================================================
   PROJECTS
   ========================================================= */

function renderProjects(
    items
) {

    renderListSection(
        "cv-projects",
        items,
        "Chưa có dự án.",
        function (
            item
        ) {

            const dateRange =
                formatDateRange(
                    item.startDate,
                    item.endDate
                );


            return `

                <div
                    class="cv-list-item"
                >

                    <div
                        class="cv-list-item-title"
                    >
                        ${escapeHtml(
                            item.projectName ||
                            "---"
                        )}
                    </div>


                    <div
                        class="cv-list-item-subtitle"
                    >
                        Vai trò:
                        ${escapeHtml(
                            item.role ||
                            "---"
                        )}
                    </div>


                    <div
                        class="cv-project-meta"
                    >

                        ${
                            dateRange
                                ? `<span>
                                    <i class="fa fa-calendar"></i>
                                    ${escapeHtml(
                                        dateRange
                                    )}
                                   </span>`
                                : ""
                        }

                        ${
                            item.teamSize !==
                                null &&
                            item.teamSize !==
                                undefined

                                ? `<span>
                                    <i class="fa fa-users"></i>
                                    Team ${escapeHtml(
                                        item.teamSize
                                    )}
                                   </span>`

                                : ""
                        }

                    </div>


                    <div
                        class="cv-list-item-description"
                    >

                        ${
                            item.description
                                ? `<div>
                                    ${nl2br(
                                        escapeHtml(
                                            item.description
                                        )
                                    )}
                                   </div>`
                                : ""
                        }


                        ${
                            item.technologies
                                ? `<div>
                                    <b>Công nghệ:</b>
                                    ${nl2br(
                                        escapeHtml(
                                            item.technologies
                                        )
                                    )}
                                   </div>`
                                : ""
                        }


                        ${
                            item.responsibilities
                                ? `<div>
                                    <b>Trách nhiệm:</b>
                                    ${nl2br(
                                        escapeHtml(
                                            item.responsibilities
                                        )
                                    )}
                                   </div>`
                                : ""
                        }

                    </div>

                </div>

            `;

        }
    );

}


/* =========================================================
   CERTIFICATES
   ========================================================= */

function renderCertificates(
    items
) {

    renderListSection(
        "cv-certificates",
        items,
        "Chưa có chứng chỉ.",
        function (
            item
        ) {

            return `

                <div
                    class="cv-list-item"
                >

                    <div
                        class="cv-list-item-title"
                    >
                        ${escapeHtml(
                            item.certificateName ||
                            item.name ||
                            "---"
                        )}
                    </div>


                    <div
                        class="cv-list-item-subtitle"
                    >

                        ${escapeHtml(
                            item.issuer ||
                            item.organization ||
                            ""
                        )}

                        ${
                            item.issueDate
                                ? " • " +
                                  escapeHtml(
                                      formatDate(
                                          item.issueDate
                                      )
                                  )
                                : ""
                        }

                    </div>


                    <div
                        class="cv-list-item-description"
                    >
                        ${nl2br(
                            escapeHtml(
                                item.description ||
                                ""
                            )
                        )}
                    </div>

                </div>

            `;

        }
    );

}


/* =========================================================
   LANGUAGES
   ========================================================= */

function renderLanguages(
    items
) {

    const container =
        document.getElementById(
            "cv-languages"
        );


    if (!container) {

        return;

    }


    if (
        !Array.isArray(
            items
        ) ||
        items.length ===
        0
    ) {

        container.innerHTML =
            emptyState(
                "Chưa có thông tin ngoại ngữ."
            );

        return;

    }


    container.innerHTML = `

        <div
            class="cv-tags"
        >

            ${items.map(
                function (
                    item
                ) {

                    return `

                        <div
                            class="cv-skill-tag"
                        >

                            <div
                                class="cv-skill-name"
                            >
                                ${escapeHtml(
                                    item.languageName ||
                                    item.languageCode ||
                                    "---"
                                )}
                            </div>


                            <div
                                class="cv-skill-level"
                            >
                                ${escapeHtml(
                                    item.proficiency ||
                                    "Chưa xác định"
                                )}
                            </div>

                        </div>

                    `;

                }
            ).join("")}

        </div>

    `;

}


/* =========================================================
   GENERIC LIST
   ========================================================= */

function renderListSection(
    containerId,
    items,
    emptyMessage,
    renderer
) {

    const container =
        document.getElementById(
            containerId
        );


    if (!container) {

        return;

    }


    if (
        !Array.isArray(
            items
        ) ||
        items.length ===
        0
    ) {

        container.innerHTML =
            emptyState(
                emptyMessage
            );

        return;

    }


    container.innerHTML = `

        <div
            class="cv-list"
        >

            ${items.map(
                renderer
            ).join("")}

        </div>

    `;

}


/* =========================================================
   ACTIONS
   ========================================================= */

function renderActions(
    cv
) {

    const container =
        document.getElementById(
            "cv-actions"
        );


    if (!container) {

        return;

    }


    container.innerHTML =
        "";


    const buttons = [];


    const workflowStatus =
        normalizeStatus(
            cv.workflowVersionStatus
        );


    const workflowVersionId =
        cv.workflowVersionId;


    const isOwner =
        isCurrentEmployee(
            cv.employeeId
        );


    /*
     * =====================================================
     * EDIT
     * Chỉ EMPLOYEE / TECH_LEAD
     * và phải là CV của chính mình.
     * =====================================================
     */

    if (
        (
            currentRole ===
            "EMPLOYEE" ||
            currentRole ===
            "TECH_LEAD"
        ) &&
        isOwner
    ) {

        buttons.push(
            `
            <a
                href="./edit.html?id=${
                    encodeURIComponent(
                        cv.id
                    )
                }"
                class="cv-action-button cv-action-edit"
            >

                <i
                    class="fa fa-pencil"
                ></i>

                <span>
                    Sửa CV
                </span>

            </a>
            `
        );

    }


    /*
     * =====================================================
     * DRAFT
     * EMPLOYEE / TECH_LEAD owner.
     * =====================================================
     */

    if (
        workflowVersionId &&
        workflowStatus ===
        "DRAFT" &&
        (
            currentRole ===
            "EMPLOYEE" ||
            currentRole ===
            "TECH_LEAD"
        ) &&
        isOwner
    ) {

        buttons.push(
            actionButton(
                "submit",
                "cv-action-submit",
                "fa-paper-plane",
                "Gửi kiểm duyệt"
            )
        );


        buttons.push(
            actionButton(
                "cancel",
                "cv-action-cancel",
                "fa-ban",
                "Hủy bản nháp"
            )
        );

    }


    /*
     * =====================================================
     * TECH LEAD REVIEW
     * =====================================================
     */

    if (
        workflowVersionId &&
        workflowStatus ===
        "PENDING_TECH_LEAD" &&
        currentRole ===
        "TECH_LEAD"
    ) {

        buttons.push(
            actionButton(
                "approve-tech",
                "cv-action-tech-approve",
                "fa-check",
                "Duyệt chuyên môn"
            )
        );


        buttons.push(
            actionButton(
                "reject-tech",
                "cv-action-tech-reject",
                "fa-times",
                "Từ chối"
            )
        );

    }


    /*
     * =====================================================
     * HR REVIEW
     *
     * Chỉ HR.
     * ADMIN không được hiển thị action HR trong UI.
     * =====================================================
     */

    if (
        workflowVersionId &&
        workflowStatus ===
        "PENDING_HR" &&
        currentRole ===
        "HR"
    ) {

        buttons.push(
            actionButton(
                "approve-hr",
                "cv-action-hr-approve",
                "fa-check",
                "HR phê duyệt"
            )
        );


        buttons.push(
            actionButton(
                "reject-hr",
                "cv-action-hr-reject",
                "fa-times",
                "HR từ chối"
            )
        );

    }


    if (
        buttons.length >
        0
    ) {

        container.innerHTML =
            buttons.join("");

    }

}


/* =========================================================
   ACTION BUTTON
   ========================================================= */

function actionButton(
    action,
    className,
    icon,
    label
) {

    return `

        <button
            type="button"
            class="cv-action-button ${className}"
            data-action="${action}"
        >

            <i
                class="fa ${icon}"
            ></i>

            <span>
                ${escapeHtml(
                    label
                )}
            </span>

        </button>

    `;

}


/* =========================================================
   ACTION EVENT
   ========================================================= */

async function handleCVActionClick(
    event
) {

    const button =
        event.target.closest(
            "button[data-action]"
        );


    if (!button) {

        return;

    }


    const action =
        button.dataset.action;


    const versionId =
        currentCV?.workflowVersionId;


    if (
        action ===
        "submit"
    ) {

        await submitWorkflowVersion(
            versionId,
            button
        );

        return;

    }


    if (
        action ===
        "cancel"
    ) {

        await cancelWorkflowVersion(
            versionId,
            button
        );

        return;

    }


    if (
        action ===
        "approve-tech"
    ) {

        await approveByTechLead(
            versionId,
            button
        );

        return;

    }


    if (
        action ===
        "reject-tech"
    ) {

        openRejectModal(
            "TECH_LEAD"
        );

        return;

    }


    if (
        action ===
        "approve-hr"
    ) {

        await approveByHr(
            versionId,
            button
        );

        return;

    }


    if (
        action ===
        "reject-hr"
    ) {

        openRejectModal(
            "HR"
        );

    }

}


/* =========================================================
   SUBMIT
   ========================================================= */

async function submitWorkflowVersion(
    versionId,
    button
) {

    if (!versionId) {

        showCVError(
            "Không có workflow version để submit."
        );

        return;

    }


    if (
        !window.confirm(
            "Bạn có chắc chắn muốn gửi CV này để kiểm duyệt không?"
        )
    ) {

        return;

    }


    setButtonBusy(
        button,
        true
    );


    try {

        const response =
            await submitCV(
                versionId
            );


        showCVSuccess(
            response?.message ||
            "CV đã được gửi kiểm duyệt."
        );


        await loadCVDetail();

    } catch (error) {

        console.error(
            "SUBMIT CV ERROR:",
            error
        );


        showCVError(
            error?.message ||
            "Không thể gửi CV."
        );

    } finally {

        setButtonBusy(
            button,
            false
        );

    }

}


/* =========================================================
   CANCEL
   ========================================================= */

async function cancelWorkflowVersion(
    versionId,
    button
) {

    if (!versionId) {

        showCVError(
            "Không có workflow version để hủy."
        );

        return;

    }


    if (
        !window.confirm(
            "Bạn có chắc chắn muốn hủy bản nháp này không?"
        )
    ) {

        return;

    }


    setButtonBusy(
        button,
        true
    );


    try {

        const response =
            await cancelCVDraft(
                versionId
            );


        showCVSuccess(
            response?.message ||
            "Đã hủy bản nháp CV."
        );


        await loadCVDetail();

    } catch (error) {

        console.error(
            "CANCEL CV ERROR:",
            error
        );


        showCVError(
            error?.message ||
            "Không thể hủy bản nháp."
        );

    } finally {

        setButtonBusy(
            button,
            false
        );

    }

}


/* =========================================================
   TECH LEAD APPROVE
   ========================================================= */

async function approveByTechLead(
    versionId,
    button
) {

    if (!versionId) {

        showCVError(
            "Không tìm thấy workflow version."
        );

        return;

    }


    if (
        !window.confirm(
            "Bạn có chắc chắn muốn duyệt CV này và chuyển sang HR không?"
        )
    ) {

        return;

    }


    setButtonBusy(
        button,
        true
    );


    try {

        const response =
            await approveCVByTechLead(
                versionId
            );


        showCVSuccess(
            response?.message ||
            "Tech Lead đã duyệt CV."
        );


        await loadCVDetail();

    } catch (error) {

        console.error(
            "TECH LEAD APPROVE ERROR:",
            error
        );


        showCVError(
            error?.message ||
            "Không thể duyệt CV."
        );

    } finally {

        setButtonBusy(
            button,
            false
        );

    }

}


/* =========================================================
   HR APPROVE
   ========================================================= */

async function approveByHr(
    versionId,
    button
) {

    if (!versionId) {

        showCVError(
            "Không tìm thấy workflow version."
        );

        return;

    }


    if (
        currentRole !==
        "HR"
    ) {

        showCVError(
            "Chỉ HR được thực hiện thao tác này."
        );

        return;

    }


    if (
        !window.confirm(
            "Bạn có chắc chắn muốn phê duyệt CV này và đưa thành phiên bản chính thức không?"
        )
    ) {

        return;

    }


    setButtonBusy(
        button,
        true
    );


    try {

        const response =
            await approveCVByHr(
                versionId
            );


        showCVSuccess(
            response?.message ||
            "HR đã phê duyệt CV."
        );


        await loadCVDetail();

    } catch (error) {

        console.error(
            "HR APPROVE ERROR:",
            error
        );


        showCVError(
            error?.message ||
            "Không thể phê duyệt CV."
        );

    } finally {

        setButtonBusy(
            button,
            false
        );

    }

}


/* =========================================================
   REJECT MODAL
   ========================================================= */

function openRejectModal(
    type
) {

    pendingRejectType =
        type;


    const modal =
        document.getElementById(
            "cv-reject-modal"
        );


    const title =
        document.getElementById(
            "cv-reject-modal-title"
        );


    const input =
        document.getElementById(
            "cv-rejection-input"
        );


    if (
        !modal ||
        !title ||
        !input
    ) {

        return;

    }


    title.textContent =
        type ===
            "TECH_LEAD"

            ? "Tech Lead từ chối CV"

            : "HR từ chối CV";


    input.value =
        "";


    modal.classList.add(
        "is-visible"
    );


    modal.setAttribute(
        "aria-hidden",
        "false"
    );


    document.body.classList.add(
        "cv-modal-open"
    );


    window.setTimeout(
        function () {

            input.focus();

        },
        50
    );

}


function closeRejectModal() {

    const modal =
        document.getElementById(
            "cv-reject-modal"
        );


    if (modal) {

        modal.classList.remove(
            "is-visible"
        );


        modal.setAttribute(
            "aria-hidden",
            "true"
        );

    }


    document.body.classList.remove(
        "cv-modal-open"
    );


    pendingRejectType =
        null;

}


/* =========================================================
   CONFIRM REJECT
   ========================================================= */

async function confirmRejectCV() {

    const input =
        document.getElementById(
            "cv-rejection-input"
        );


    const confirmButton =
        document.getElementById(
            "cv-confirm-reject"
        );


    const reason =
        input?.value
            ?.trim() ||
        "";


    const versionId =
        currentCV?.workflowVersionId;


    if (!pendingRejectType) {

        return;

    }


    if (!reason) {

        showCVError(
            "Vui lòng nhập lý do từ chối."
        );


        input?.focus();

        return;

    }


    if (!versionId) {

        showCVError(
            "Không tìm thấy workflow version."
        );

        return;

    }


    /*
     * Frontend guard.
     */

    if (
        pendingRejectType ===
        "TECH_LEAD" &&
        currentRole !==
        "TECH_LEAD"
    ) {

        showCVError(
            "Chỉ Tech Lead được từ chối ở bước chuyên môn."
        );

        return;

    }


    if (
        pendingRejectType ===
        "HR" &&
        currentRole !==
        "HR"
    ) {

        showCVError(
            "Chỉ HR được từ chối ở bước HR."
        );

        return;

    }


    if (confirmButton) {

        setButtonBusy(
            confirmButton,
            true
        );

    }


    try {

        let response;


        if (
            pendingRejectType ===
            "TECH_LEAD"
        ) {

            response =
                await rejectCVByTechLead(
                    versionId,
                    reason
                );

        } else {

            response =
                await rejectCVByHr(
                    versionId,
                    reason
                );

        }


        closeRejectModal();


        showCVSuccess(
            response?.message ||
            "Đã từ chối CV."
        );


        await loadCVDetail();

    } catch (error) {

        console.error(
            "REJECT CV ERROR:",
            error
        );


        showCVError(
            error?.message ||
            "Không thể từ chối CV."
        );

    } finally {

        if (confirmButton) {

            setButtonBusy(
                confirmButton,
                false
            );

        }

    }

}


/* =========================================================
   OWNER
   ========================================================= */

function isCurrentEmployee(
    employeeId
) {

    if (
        currentEmployeeId ===
        null ||
        employeeId ===
        null ||
        employeeId ===
        undefined
    ) {

        return false;

    }


    return sameId(
        employeeId,
        currentEmployeeId
    );

}


function resolveCurrentEmployeeId(
    user
) {

    return (
        user?.employeeId ??
        user?.employee?.id ??
        user?.employeeInfo?.id ??
        null
    );

}


function sameId(
    first,
    second
) {

    return String(
        first
    ) ===
    String(
        second
    );

}


/* =========================================================
   STATUS
   ========================================================= */

function setStatusBadge(
    id,
    status,
    prefix
) {

    const element =
        document.getElementById(
            id
        );


    if (!element) {

        return;

    }


    const normalized =
        normalizeStatus(
            status
        );


    element.textContent =
        statusText(
            normalized
        );


    element.className =
        `${prefix} ${
            statusClass(
                normalized
            )
        }`.trim();

}


function statusText(
    status
) {

    const map = {

        UPDATED:
            "Đã cập nhật",

        NOT_UPDATED:
            "Chưa cập nhật",

        REQUEST_CANCELLED:
            "Hủy yêu cầu",

        DRAFT:
            "Bản nháp",

        PENDING_TECH_LEAD:
            "Chờ Tech Lead duyệt",

        TECH_LEAD_REJECTED:
            "Tech Lead từ chối",

        TECH_LEAD_APPROVED:
            "Tech Lead đã duyệt",

        PENDING_HR:
            "Chờ HR duyệt",

        HR_REJECTED:
            "HR từ chối",

        HR_APPROVED:
            "HR đã duyệt",

        OFFICIAL:
            "Chính thức",

        ARCHIVED:
            "Lưu trữ"

    };


    return (
        map[status] ||
        status ||
        "Chưa xác định"
    );

}


function statusClass(
    status
) {

    if (
        [
            "UPDATED",
            "OFFICIAL",
            "HR_APPROVED",
            "TECH_LEAD_APPROVED"
        ].includes(
            status
        )
    ) {

        return "cv-status-updated";

    }


    if (
        [
            "PENDING_TECH_LEAD",
            "PENDING_HR",
            "DRAFT"
        ].includes(
            status
        )
    ) {

        return "cv-status-pending";

    }


    if (
        [
            "TECH_LEAD_REJECTED",
            "HR_REJECTED",
            "NOT_UPDATED",
            "REQUEST_CANCELLED"
        ].includes(
            status
        )
    ) {

        return "cv-status-rejected";

    }


    return "cv-status-default";

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
        .replace(
            /^ROLE_/,
            ""
        )
        .trim()
        .toUpperCase();

}


function normalizeStatus(
    status
) {

    return String(
        status ||
        ""
    )
        .trim()
        .toUpperCase();

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
            value ??
            "";

    }

}


function formatDate(
    value
) {

    if (!value) {

        return "";

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


    return date.toLocaleDateString(
        "vi-VN"
    );

}


function formatDateRange(
    startDate,
    endDate
) {

    const start =
        formatDate(
            startDate
        );


    const end =
        formatDate(
            endDate
        );


    if (
        start &&
        end
    ) {

        return (
            `${start} - ${end}`
        );

    }


    return (
        start ||
        end ||
        ""
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


function emptyState(
    message
) {

    return `

        <div
            class="cv-inline-empty"
        >

            <i
                class="fa fa-folder-open-o"
            ></i>

            <span>
                ${escapeHtml(
                    message
                )}
            </span>

        </div>

    `;

}


/* =========================================================
   BUTTON BUSY
   ========================================================= */

function setButtonBusy(
    button,
    busy
) {

    if (!button) {

        return;

    }


    button.disabled =
        busy;


    if (busy) {

        button.dataset.originalHtml =
            button.innerHTML;


        button.innerHTML =
            `
                <i
                    class="fa fa-spinner fa-spin"
                ></i>

                <span>
                    Đang xử lý...
                </span>
            `;

    } else if (
        button.dataset.originalHtml
    ) {

        button.innerHTML =
            button.dataset.originalHtml;


        delete button.dataset.originalHtml;

    }

}


/* =========================================================
   VIEW STATE
   ========================================================= */

function showDetail() {

    const detail =
        document.getElementById(
            "cv-detail"
        );


    if (detail) {

        detail.style.display =
            "block";

    }

}


function hideLoading() {

    const loading =
        document.getElementById(
            "cv-loading"
        );


    if (loading) {

        loading.style.display =
            "none";

    }

}


/* =========================================================
   ERROR
   ========================================================= */

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


    if (
        !element ||
        !text
    ) {

        return;

    }


    text.textContent =
        message ||
        "Có lỗi xảy ra.";


    element.classList.add(
        "is-visible"
    );


    element.style.setProperty(
        "display",
        "flex",
        "important"
    );

}


function hideCVError() {

    const element =
        document.getElementById(
            "cv-error"
        );


    if (element) {

        element.classList.remove(
            "is-visible"
        );


        element.style.setProperty(
            "display",
            "none",
            "important"
        );

    }

}


/* =========================================================
   SUCCESS
   ========================================================= */

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


    if (
        !element ||
        !text
    ) {

        return;

    }


    text.textContent =
        message ||
        "Thành công.";


    element.classList.add(
        "is-visible"
    );


    element.style.setProperty(
        "display",
        "flex",
        "important"
    );


    window.setTimeout(
        function () {

            hideCVSuccess();

        },
        3000
    );

}


function hideCVSuccess() {

    const element =
        document.getElementById(
            "cv-success"
        );


    if (element) {

        element.classList.remove(
            "is-visible"
        );


        element.style.setProperty(
            "display",
            "none",
            "important"
        );

    }

}