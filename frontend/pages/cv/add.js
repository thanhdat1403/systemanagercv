// ============================================================
// SYSTEMANAGERCV - CV CREATE PAGE
// File: pages/cv/add.js
// ============================================================

"use strict";


/* ============================================================
 * STATE
 * ============================================================ */

let employeeOptions = [];


/* ============================================================
 * DOM READY
 * ============================================================ */

document.addEventListener(
    "DOMContentLoaded",
    async function () {

        try {

            await loadCommonComponents();

            initializePageEvents();

            await loadEmployeeOptions();

            initializeRepeaters();

        } catch (error) {

            console.error(
                "CV ADD INITIALIZATION ERROR:",
                error
            );

            showError(
                error.message ||
                "Không thể khởi tạo trang Tạo CV."
            );

        }

    }
);


/* ============================================================
 * LOAD COMMON COMPONENTS
 * ============================================================ */

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
        typeof initializeSidebarToggle ===
        "function"
    ) {

        initializeSidebarToggle();

    }


    if (
        typeof setupLogout ===
        "function"
    ) {

        setupLogout();

    }

}


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
            `Không thể tải ${filePath}. HTTP ${response.status}`
        );

    }


    container.innerHTML =
        await response.text();

}


/* ============================================================
 * LOAD EMPLOYEE OPTIONS
 *
 * GET /api/v1/employees
 * ============================================================ */

async function loadEmployeeOptions() {

    const select =
        document.getElementById(
            "employeeId"
        );


    if (!select) {

        return;

    }


    select.innerHTML = `
        <option value="">
            -- Đang tải nhân viên --
        </option>
    `;


    try {

        if (
            typeof getEmployees !==
            "function"
        ) {

            throw new Error(
                "Không tìm thấy getEmployees(). Kiểm tra employeeApi.js."
            );

        }


        /*
         * Project hiện tại của bạn có ít Employee,
         * nên lấy tối đa 100 bản ghi cho select.
         */
        const response =
            await getEmployees({
                page: 0,
                size: 100,
                keyword: ""
            });


        const pageData =
            response?.data;


        const employees =
            Array.isArray(
                pageData?.content
            )
                ? pageData.content
                : [];


        employeeOptions =
            employees;


        select.innerHTML = `
            <option value="">
                -- Chọn nhân viên --
            </option>
        `;


        employees.forEach(
            function (employee) {

                const option =
                    document.createElement(
                        "option"
                    );


                option.value =
                    employee.id;


                const code =
                    employee.employeeCode ||
                    "---";


                const name =
                    employee.fullName ||
                    employee.employeeName ||
                    "---";


                const department =
                    employee.departmentName ||
                    "";


                option.textContent =
                    `${code} - ${name}` +
                    (
                        department
                            ? ` - ${department}`
                            : ""
                    );


                select.appendChild(
                    option
                );

            }
        );


        if (
            employees.length === 0
        ) {

            select.innerHTML = `
                <option value="">
                    -- Không có nhân viên --
                </option>
            `;

            showError(
                "Không có nhân viên phù hợp để tạo CV."
            );

        }

    } catch (error) {

        console.error(
            "LOAD EMPLOYEE OPTIONS ERROR:",
            error
        );


        select.innerHTML = `
            <option value="">
                -- Không thể tải nhân viên --
            </option>
        `;


        throw error;

    }

}


/* ============================================================
 * PAGE EVENTS
 * ============================================================ */

function initializePageEvents() {

    const form =
        document.getElementById(
            "cv-add-form"
        );


    if (form) {

        form.addEventListener(
            "submit",
            handleSubmit
        );

    }


    document
        .querySelectorAll(
            "[data-add-section]"
        )
        .forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function () {

                        const section =
                            button.dataset.addSection;


                        addRepeaterRow(
                            section
                        );

                    }
                );

            }
        );

}


/* ============================================================
 * REPEATERS
 * ============================================================ */

function initializeRepeaters() {

    /*
     * Không tạo sẵn tất cả row.
     * Người dùng chỉ thêm khi cần.
     */

}


/* ============================================================
 * ADD ROW
 * ============================================================ */

function addRepeaterRow(
    section
) {

    const container =
        document.getElementById(
            `${section}-container`
        );


    if (!container) {

        return;

    }


    const wrapper =
        document.createElement(
            "div"
        );


    wrapper.className =
        "cv-repeat-item";


    wrapper.innerHTML =
        buildRepeaterTemplate(
            section
        );


    container.appendChild(
        wrapper
    );


    const removeButton =
        wrapper.querySelector(
            ".cv-repeat-remove"
        );


    if (removeButton) {

        removeButton.addEventListener(
            "click",
            function () {

                wrapper.remove();

                refreshSortOrders(
                    section
                );

            }
        );

    }


    refreshSortOrders(
        section
    );

}


/* ============================================================
 * BUILD REPEATER TEMPLATE
 * ============================================================ */

function buildRepeaterTemplate(
    section
) {

    switch (section) {

        case "skills":

            return `
                <div class="cv-repeat-item-header">

                    <strong>
                        Kỹ năng
                    </strong>

                    <button
                        type="button"
                        class="cv-repeat-remove"
                    >
                        <i class="fa fa-trash"></i>
                        Xóa
                    </button>

                </div>


                <div class="cv-add-grid-2">

                    <div class="cv-add-field">

                        <label>
                            Tên kỹ năng
                        </label>

                        <input
                            type="text"
                            class="cv-add-control skill-name"
                            placeholder="Java"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Cấp độ
                        </label>

                        <input
                            type="text"
                            class="cv-add-control skill-level"
                            placeholder="Advanced"
                        >

                    </div>


                    <div
                        class="cv-add-field
                               cv-add-field-full"
                    >

                        <label>
                            Mô tả
                        </label>

                        <textarea
                            rows="3"
                            class="
                                cv-add-control
                                cv-add-textarea
                                skill-description
                            "
                            placeholder="Mô tả kỹ năng..."
                        ></textarea>

                    </div>

                </div>
            `;


        case "educations":

            return `
                <div class="cv-repeat-item-header">

                    <strong>
                        Học vấn
                    </strong>

                    <button
                        type="button"
                        class="cv-repeat-remove"
                    >
                        <i class="fa fa-trash"></i>
                        Xóa
                    </button>

                </div>


                <div class="cv-add-grid-2">

                    <div class="cv-add-field">

                        <label>
                            Trường
                        </label>

                        <input
                            type="text"
                            class="cv-add-control education-school"
                            placeholder="Dai hoc Bach Khoa Ha Noi"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Chuyên ngành
                        </label>

                        <input
                            type="text"
                            class="cv-add-control education-major"
                            placeholder="Ky thuat Dien tu - Vien thong"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Bằng cấp
                        </label>

                        <input
                            type="text"
                            class="cv-add-control education-degree"
                            placeholder="Ky su"
                        >

                    </div>


                    <div class="cv-add-field"></div>


                    <div class="cv-add-field">

                        <label>
                            Ngày bắt đầu
                        </label>

                        <input
                            type="date"
                            class="cv-add-control education-start-date"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Ngày kết thúc
                        </label>

                        <input
                            type="date"
                            class="cv-add-control education-end-date"
                        >

                    </div>


                    <div
                        class="
                            cv-add-field
                            cv-add-field-full
                        "
                    >

                        <label>
                            Mô tả
                        </label>

                        <textarea
                            rows="3"
                            class="
                                cv-add-control
                                cv-add-textarea
                                education-description
                            "
                        ></textarea>

                    </div>

                </div>
            `;


        case "experiences":

            return `
                <div class="cv-repeat-item-header">

                    <strong>
                        Kinh nghiệm
                    </strong>

                    <button
                        type="button"
                        class="cv-repeat-remove"
                    >
                        <i class="fa fa-trash"></i>
                        Xóa
                    </button>

                </div>


                <div class="cv-add-grid-2">

                    <div class="cv-add-field">

                        <label>
                            Công ty
                        </label>

                        <input
                            type="text"
                            class="cv-add-control experience-company"
                            placeholder="ABC Technology"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Vị trí
                        </label>

                        <input
                            type="text"
                            class="cv-add-control experience-position"
                            placeholder="Backend Developer"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Ngày bắt đầu
                        </label>

                        <input
                            type="date"
                            class="cv-add-control experience-start-date"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Ngày kết thúc
                        </label>

                        <input
                            type="date"
                            class="cv-add-control experience-end-date"
                        >

                    </div>


                    <div
                        class="
                            cv-add-field
                            cv-add-field-full
                        "
                    >

                        <label>
                            Mô tả
                        </label>

                        <textarea
                            rows="4"
                            class="
                                cv-add-control
                                cv-add-textarea
                                experience-description
                            "
                        ></textarea>

                    </div>

                </div>
            `;


        case "projects":

            return `
                <div class="cv-repeat-item-header">

                    <strong>
                        Dự án
                    </strong>

                    <button
                        type="button"
                        class="cv-repeat-remove"
                    >
                        <i class="fa fa-trash"></i>
                        Xóa
                    </button>

                </div>


                <div class="cv-add-grid-2">

                    <div class="cv-add-field">

                        <label>
                            Tên dự án
                        </label>

                        <input
                            type="text"
                            class="cv-add-control project-name"
                            placeholder="Employee CV Management System"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Vai trò
                        </label>

                        <input
                            type="text"
                            class="cv-add-control project-role"
                            placeholder="Backend Developer"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Ngày bắt đầu
                        </label>

                        <input
                            type="date"
                            class="cv-add-control project-start-date"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Ngày kết thúc
                        </label>

                        <input
                            type="date"
                            class="cv-add-control project-end-date"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Team size
                        </label>

                        <input
                            type="number"
                            min="0"
                            class="cv-add-control project-team-size"
                            placeholder="3"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Công nghệ
                        </label>

                        <input
                            type="text"
                            class="cv-add-control project-technologies"
                            placeholder="Java, Spring Boot, MariaDB"
                        >

                    </div>


                    <div
                        class="
                            cv-add-field
                            cv-add-field-full
                        "
                    >

                        <label>
                            Mô tả
                        </label>

                        <textarea
                            rows="4"
                            class="
                                cv-add-control
                                cv-add-textarea
                                project-description
                            "
                        ></textarea>

                    </div>


                    <div
                        class="
                            cv-add-field
                            cv-add-field-full
                        "
                    >

                        <label>
                            Trách nhiệm
                        </label>

                        <textarea
                            rows="4"
                            class="
                                cv-add-control
                                cv-add-textarea
                                project-responsibilities
                            "
                        ></textarea>

                    </div>

                </div>
            `;


        case "languages":

            return `
                <div class="cv-repeat-item-header">

                    <strong>
                        Ngoại ngữ
                    </strong>

                    <button
                        type="button"
                        class="cv-repeat-remove"
                    >
                        <i class="fa fa-trash"></i>
                        Xóa
                    </button>

                </div>


                <div class="cv-add-grid-2">

                    <div class="cv-add-field">

                        <label>
                            Language code
                        </label>

                        <input
                            type="text"
                            class="cv-add-control language-code"
                            placeholder="en"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Tên ngôn ngữ
                        </label>

                        <input
                            type="text"
                            class="cv-add-control language-name"
                            placeholder="English"
                        >

                    </div>


                    <div class="cv-add-field">

                        <label>
                            Trình độ
                        </label>

                        <input
                            type="text"
                            class="cv-add-control language-proficiency"
                            placeholder="Upper-Intermediate"
                        >

                    </div>

                </div>
            `;


        default:

            return "";

    }

}


/* ============================================================
 * SORT ORDER
 * ============================================================ */

function refreshSortOrders(
    section
) {

    const rows =
        document.querySelectorAll(
            `#${section}-container .cv-repeat-item`
        );


    rows.forEach(
        function (row, index) {

            row.dataset.sortOrder =
                index;

        }
    );

}


/* ============================================================
 * SUBMIT
 * ============================================================ */

async function handleSubmit(
    event
) {

    event.preventDefault();

    clearMessages();


    const employeeId =
        Number(
            document
                .getElementById(
                    "employeeId"
                )
                .value
        );


    if (!employeeId) {

        showError(
            "Vui lòng chọn nhân viên."
        );

        document
            .getElementById(
                "employeeId"
            )
            ?.focus();

        return;

    }


    const profile =
        buildProfile();


    const skills =
        buildSkills();


    const educations =
        buildEducations();


    const experiences =
        buildExperiences();


    const projects =
        buildProjects();


    const languages =
        buildLanguages();


    /*
     * CVCertificateRequest chưa được cung cấp,
     * vì vậy phase này gửi danh sách rỗng.
     */
    const certificates = [];


    const payload = {

        employeeId,

        profile,

        skills,

        educations,

        experiences,

        projects,

        certificates,

        languages

    };


    console.log(
        "CREATE CV REQUEST:",
        payload
    );


    const saveButton =
        document.getElementById(
            "cv-create-button"
        );


    if (saveButton) {

        saveButton.disabled =
            true;

        saveButton.innerHTML = `
            <i class="fa fa-spinner fa-spin"></i>
            <span>Đang tạo CV...</span>
        `;

    }


    try {

        if (
            typeof createCV !==
            "function"
        ) {

            throw new Error(
                "Không tìm thấy createCV(). Kiểm tra cvApi.js."
            );

        }


        const response =
            await createCV(
                payload
            );


        console.log(
            "CREATE CV RESPONSE:",
            response
        );


        const createdCV =
            response?.data;


        if (
            !createdCV ||
            !createdCV.id
        ) {

            throw new Error(
                "Backend không trả về CV vừa tạo."
            );

        }


        showSuccess(
            "Tạo CV thành công. Đang chuyển tới chi tiết CV..."
        );


        setTimeout(
            function () {

                window.location.href =
                    `./detail.html?id=${encodeURIComponent(createdCV.id)}`;

            },
            700
        );


    } catch (error) {

        console.error(
            "CREATE CV ERROR:",
            error
        );


        showError(
            error.message ||
            "Không thể tạo CV."
        );


    } finally {

        if (saveButton) {

            saveButton.disabled =
                false;

            saveButton.innerHTML = `
                <i class="fa fa-save"></i>
                <span>Tạo CV</span>
            `;

        }

    }

}


/* ============================================================
 * BUILD PROFILE
 * ============================================================ */

function buildProfile() {

    const headline =
        getValue(
            "profile-headline"
        );


    const address =
        getValue(
            "profile-address"
        );


    const avatarUrl =
        getValue(
            "profile-avatar-url"
        );


    const careerObjective =
        getValue(
            "profile-career-objective"
        );


    /*
     * Không gửi object rỗng.
     */
    if (
        !headline &&
        !address &&
        !avatarUrl &&
        !careerObjective
    ) {

        return null;

    }


    return {

        headline:
            headline || null,

        address:
            address || null,

        avatarUrl:
            avatarUrl || null,

        careerObjective:
            careerObjective || null

    };

}


/* ============================================================
 * BUILD SKILLS
 * ============================================================ */

function buildSkills() {

    const rows =
        document.querySelectorAll(
            "#skills-container .cv-repeat-item"
        );


    return Array.from(
        rows
    )
        .map(
            function (row, index) {

                return {

                    skillName:
                        getRowValue(
                            row,
                            ".skill-name"
                        ),

                    skillLevel:
                        getRowValue(
                            row,
                            ".skill-level"
                        ),

                    description:
                        getRowValue(
                            row,
                            ".skill-description"
                        ),

                    sortOrder:
                        index

                };

            }
        )
        .filter(
            function (item) {

                return Boolean(
                    item.skillName ||
                    item.skillLevel ||
                    item.description
                );

            }
        );

}


/* ============================================================
 * BUILD EDUCATIONS
 * ============================================================ */

function buildEducations() {

    const rows =
        document.querySelectorAll(
            "#educations-container .cv-repeat-item"
        );


    return Array.from(
        rows
    )
        .map(
            function (row, index) {

                return {

                    schoolName:
                        getRowValue(
                            row,
                            ".education-school"
                        ),

                    major:
                        getRowValue(
                            row,
                            ".education-major"
                        ),

                    degree:
                        getRowValue(
                            row,
                            ".education-degree"
                        ),

                    startDate:
                        getRowValue(
                            row,
                            ".education-start-date"
                        ) || null,

                    endDate:
                        getRowValue(
                            row,
                            ".education-end-date"
                        ) || null,

                    description:
                        getRowValue(
                            row,
                            ".education-description"
                        ),

                    sortOrder:
                        index

                };

            }
        )
        .filter(
            function (item) {

                return Boolean(
                    item.schoolName ||
                    item.major ||
                    item.degree ||
                    item.startDate ||
                    item.endDate ||
                    item.description
                );

            }
        );

}


/* ============================================================
 * BUILD EXPERIENCES
 * ============================================================ */

function buildExperiences() {

    const rows =
        document.querySelectorAll(
            "#experiences-container .cv-repeat-item"
        );


    return Array.from(
        rows
    )
        .map(
            function (row, index) {

                return {

                    companyName:
                        getRowValue(
                            row,
                            ".experience-company"
                        ),

                    position:
                        getRowValue(
                            row,
                            ".experience-position"
                        ),

                    startDate:
                        getRowValue(
                            row,
                            ".experience-start-date"
                        ) || null,

                    endDate:
                        getRowValue(
                            row,
                            ".experience-end-date"
                        ) || null,

                    description:
                        getRowValue(
                            row,
                            ".experience-description"
                        ),

                    sortOrder:
                        index

                };

            }
        )
        .filter(
            function (item) {

                return Boolean(
                    item.companyName ||
                    item.position ||
                    item.startDate ||
                    item.endDate ||
                    item.description
                );

            }
        );

}


/* ============================================================
 * BUILD PROJECTS
 * ============================================================ */

function buildProjects() {

    const rows =
        document.querySelectorAll(
            "#projects-container .cv-repeat-item"
        );


    return Array.from(
        rows
    )
        .map(
            function (row, index) {

                const teamSizeText =
                    getRowValue(
                        row,
                        ".project-team-size"
                    );


                return {

                    projectName:
                        getRowValue(
                            row,
                            ".project-name"
                        ),

                    role:
                        getRowValue(
                            row,
                            ".project-role"
                        ),

                    startDate:
                        getRowValue(
                            row,
                            ".project-start-date"
                        ) || null,

                    endDate:
                        getRowValue(
                            row,
                            ".project-end-date"
                        ) || null,

                    description:
                        getRowValue(
                            row,
                            ".project-description"
                        ),

                    technologies:
                        getRowValue(
                            row,
                            ".project-technologies"
                        ),

                    teamSize:
                        teamSizeText
                            ? Number(teamSizeText)
                            : null,

                    responsibilities:
                        getRowValue(
                            row,
                            ".project-responsibilities"
                        ),

                    sortOrder:
                        index

                };

            }
        )
        .filter(
            function (item) {

                return Boolean(
                    item.projectName ||
                    item.role ||
                    item.startDate ||
                    item.endDate ||
                    item.description ||
                    item.technologies ||
                    item.teamSize !== null ||
                    item.responsibilities
                );

            }
        );

}


/* ============================================================
 * BUILD LANGUAGES
 * ============================================================ */

function buildLanguages() {

    const rows =
        document.querySelectorAll(
            "#languages-container .cv-repeat-item"
        );


    return Array.from(
        rows
    )
        .map(
            function (row, index) {

                return {

                    languageCode:
                        getRowValue(
                            row,
                            ".language-code"
                        ),

                    languageName:
                        getRowValue(
                            row,
                            ".language-name"
                        ),

                    proficiency:
                        getRowValue(
                            row,
                            ".language-proficiency"
                        ),

                    sortOrder:
                        index

                };

            }
        )
        .filter(
            function (item) {

                return Boolean(
                    item.languageCode ||
                    item.languageName ||
                    item.proficiency
                );

            }
        );

}


/* ============================================================
 * GET VALUE
 * ============================================================ */

function getValue(
    id
) {

    const element =
        document.getElementById(
            id
        );


    if (!element) {

        return "";

    }


    return (
        element.value ||
        ""
    ).trim();

}


/* ============================================================
 * GET ROW VALUE
 * ============================================================ */

function getRowValue(
    row,
    selector
) {

    const element =
        row.querySelector(
            selector
        );


    if (!element) {

        return "";

    }


    return (
        element.value ||
        ""
    ).trim();

}


/* ============================================================
 * MESSAGE
 * ============================================================ */

function clearMessages() {

    hideElement(
        "cv-add-error"
    );

    hideElement(
        "cv-add-success"
    );

}


function showError(
    message
) {

    const wrapper =
        document.getElementById(
            "cv-add-error"
        );


    const text =
        document.getElementById(
            "cv-add-error-text"
        );


    if (text) {

        text.textContent =
            message;

    }


    if (wrapper) {

        wrapper.style.display =
            "flex";

    }

}


function showSuccess(
    message
) {

    const wrapper =
        document.getElementById(
            "cv-add-success"
        );


    const text =
        document.getElementById(
            "cv-add-success-text"
        );


    if (text) {

        text.textContent =
            message;

    }


    if (wrapper) {

        wrapper.style.display =
            "flex";

    }

}


function hideElement(
    id
) {

    const element =
        document.getElementById(
            id
        );


    if (element) {

        element.style.display =
            "none";

    }

}
