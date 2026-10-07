// =====================================================
// SYSTEMANAGERCV - CV API
// File: services/api/cvApi.js
// =====================================================


/**
 * =====================================================
 * GET CV LIST
 *
 * Backend:
 * GET /api/v1/cvs
 *
 * LƯU Ý QUAN TRỌNG:
 *
 * Frontend KHÔNG gửi:
 * - scopeEmployeeId
 * - scopeDepartmentId
 * - role
 *
 * Backend tự lấy User hiện tại và xác định scope:
 *
 * ADMIN      -> ALL
 * HR         -> ALL
 * TECH_LEAD  -> DEPARTMENT
 * EMPLOYEE   -> SELF
 *
 * =====================================================
 */

async function getCVs({

    page =
        0,

    size =
        10,

    keyword =
        "",

    departmentId =
        "",

    status =
        "",

    sortBy =
        "id",

    sortDirection =
        "DESC"

} = {}) {


    const params =
        new URLSearchParams();


    params.set(
        "page",
        page
    );


    params.set(
        "size",
        size
    );


    params.set(
        "sortBy",
        sortBy
    );


    params.set(
        "sortDirection",
        sortDirection
    );


    if (
        keyword !==
        null &&
        keyword !==
        undefined &&
        String(
            keyword
        ).trim() !==
        ""
    ) {

        params.set(
            "keyword",
            String(
                keyword
            ).trim()
        );

    }


    /*
     * departmentId chỉ là filter nghiệp vụ
     * nếu page khác cần dùng.
     *
     * Không dùng field này để bảo mật scope.
     */

    if (
        departmentId !==
            "" &&
        departmentId !==
            null &&
        departmentId !==
            undefined
    ) {

        params.set(
            "departmentId",
            departmentId
        );

    }


    if (
        status !==
            "" &&
        status !==
            null &&
        status !==
            undefined
    ) {

        params.set(
            "status",
            status
        );

    }


    return apiRequest(
        `/cvs?${params.toString()}`,
        {
            method:
                "GET"
        }
    );

}


/**
 * =====================================================
 * GET CV DETAIL
 *
 * GET /api/v1/cvs/{id}
 * =====================================================
 */

async function getCVById(
    id
) {

    if (
        id ===
            null ||
        id ===
            undefined ||
        id ===
            ""
    ) {

        throw new Error(
            "CV ID không hợp lệ."
        );

    }


    return apiRequest(
        `/cvs/${encodeURIComponent(id)}`,
        {
            method:
                "GET"
        }
    );

}


/**
 * =====================================================
 * CREATE CV
 *
 * POST /api/v1/cvs
 * =====================================================
 */

async function createCV(
    data
) {

    if (
        !data
    ) {

        throw new Error(
            "Dữ liệu tạo CV không hợp lệ."
        );

    }


    return apiRequest(
        "/cvs",
        {
            method:
                "POST",

            body:
                JSON.stringify(
                    data
                )
        }
    );

}


/**
 * =====================================================
 * UPDATE CV
 *
 * PUT /api/v1/cvs/{id}
 * =====================================================
 */

async function updateCV(
    id,
    data
) {

    if (
        id ===
            null ||
        id ===
            undefined ||
        id ===
            ""
    ) {

        throw new Error(
            "CV ID không hợp lệ."
        );

    }


    if (
        !data
    ) {

        throw new Error(
            "Dữ liệu cập nhật CV không hợp lệ."
        );

    }


    return apiRequest(
        `/cvs/${encodeURIComponent(id)}`,
        {
            method:
                "PUT",

            body:
                JSON.stringify(
                    data
                )
        }
    );

}


/**
 * =====================================================
 * DELETE CV
 *
 * DELETE /api/v1/cvs/{id}
 * =====================================================
 */

async function deleteCV(
    id
) {

    if (
        id ===
            null ||
        id ===
            undefined ||
        id ===
            ""
    ) {

        throw new Error(
            "CV ID không hợp lệ."
        );

    }


    return apiRequest(
        `/cvs/${encodeURIComponent(id)}`,
        {
            method:
                "DELETE"
        }
    );

}


/**
 * =====================================================
 * SUBMIT CV DRAFT
 *
 * POST /api/v1/cvs/versions/{versionId}/submit
 * =====================================================
 */

async function submitCV(
    versionId
) {

    if (
        versionId ===
            null ||
        versionId ===
            undefined ||
        versionId ===
            ""
    ) {

        throw new Error(
            "Version ID không hợp lệ."
        );

    }


    return apiRequest(
        `/cvs/versions/${encodeURIComponent(versionId)}/submit`,
        {
            method:
                "POST"
        }
    );

}


/**
 * =====================================================
 * CANCEL CV DRAFT
 *
 * POST /api/v1/cvs/versions/{versionId}/cancel
 * =====================================================
 */

async function cancelCVDraft(
    versionId
) {

    if (
        versionId ===
            null ||
        versionId ===
            undefined ||
        versionId ===
            ""
    ) {

        throw new Error(
            "Version ID không hợp lệ."
        );

    }


    return apiRequest(
        `/cvs/versions/${encodeURIComponent(versionId)}/cancel`,
        {
            method:
                "POST"
        }
    );

}


/**
 * =====================================================
 * TECH LEAD APPROVE
 *
 * PENDING_TECH_LEAD
 * ->
 * PENDING_HR
 * =====================================================
 */

async function approveCVByTechLead(
    versionId
) {

    if (
        versionId ===
            null ||
        versionId ===
            undefined ||
        versionId ===
            ""
    ) {

        throw new Error(
            "Version ID không hợp lệ."
        );

    }


    return apiRequest(
        `/cvs/versions/${encodeURIComponent(versionId)}/approve`,
        {
            method:
                "POST"
        }
    );

}


/**
 * =====================================================
 * TECH LEAD REJECT
 * =====================================================
 */

async function rejectCVByTechLead(
    versionId,
    rejectionReason
) {

    if (
        versionId ===
            null ||
        versionId ===
            undefined ||
        versionId ===
            ""
    ) {

        throw new Error(
            "Version ID không hợp lệ."
        );

    }


    if (
        !rejectionReason ||
        !String(
            rejectionReason
        ).trim()
    ) {

        throw new Error(
            "Lý do từ chối không được để trống."
        );

    }


    return apiRequest(
        `/cvs/versions/${encodeURIComponent(versionId)}/reject`,
        {
            method:
                "POST",

            body:
                JSON.stringify({

                    rejectionReason:
                        String(
                            rejectionReason
                        ).trim()

                })
        }
    );

}


/**
 * =====================================================
 * HR / ADMIN APPROVE
 *
 * PENDING_HR
 * ->
 * OFFICIAL
 * =====================================================
 */

async function approveCVByHr(
    versionId
) {

    if (
        versionId ===
            null ||
        versionId ===
            undefined ||
        versionId ===
            ""
    ) {

        throw new Error(
            "Version ID không hợp lệ."
        );

    }


    return apiRequest(
        `/cvs/versions/${encodeURIComponent(versionId)}/approve-hr`,
        {
            method:
                "POST"
        }
    );

}


/**
 * =====================================================
 * HR / ADMIN REJECT
 * =====================================================
 */

async function rejectCVByHr(
    versionId,
    rejectionReason
) {

    if (
        versionId ===
            null ||
        versionId ===
            undefined ||
        versionId ===
            ""
    ) {

        throw new Error(
            "Version ID không hợp lệ."
        );

    }


    if (
        !rejectionReason ||
        !String(
            rejectionReason
        ).trim()
    ) {

        throw new Error(
            "Lý do từ chối không được để trống."
        );

    }


    return apiRequest(
        `/cvs/versions/${encodeURIComponent(versionId)}/reject-hr`,
        {
            method:
                "POST",

            body:
                JSON.stringify({

                    rejectionReason:
                        String(
                            rejectionReason
                        ).trim()

                })
        }
    );

}