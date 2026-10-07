/* ============================================================
 * SYSTEMANAGERCV
 * DEPARTMENT API
 * File: services/api/departmentApi.js
 * ============================================================
 */


/* ============================================================
 * GET DEPARTMENTS
 *
 * GET /api/v1/departments
 *
 * Hỗ trợ:
 * - keyword
 * - status
 * - page
 * - size
 * - sortBy
 * - sortDirection
 * ============================================================
 */

async function getDepartments({
    page = 0,
    size = 10,
    keyword = "",
    status = "",
    sortBy = "createdDate",
    sortDirection = "DESC"
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


    if (keyword) {

        params.set(
            "keyword",
            keyword
        );

    }


    if (
        status !== "" &&
        status !== null &&
        status !== undefined
    ) {

        params.set(
            "status",
            status
        );

    }


    return apiRequest(
        `/departments?${params.toString()}`,
        {
            method: "GET"
        }
    );

}


/* ============================================================
 * GET DEPARTMENT DETAIL
 *
 * GET /api/v1/departments/{id}
 * ============================================================
 */

async function getDepartmentById(
    id
) {

    if (
        id === null ||
        id === undefined ||
        id === ""
    ) {

        throw new Error(
            "ID phòng ban không hợp lệ."
        );

    }


    return apiRequest(
        `/departments/${encodeURIComponent(id)}`,
        {
            method: "GET"
        }
    );

}


/* ============================================================
 * CREATE DEPARTMENT
 *
 * POST /api/v1/departments
 * ============================================================
 */

async function createDepartment(
    data
) {

    return apiRequest(
        "/departments",
        {
            method: "POST",
            body: JSON.stringify(
                data
            )
        }
    );

}


/* ============================================================
 * UPDATE DEPARTMENT
 *
 * PUT /api/v1/departments/{id}
 * ============================================================
 */

async function updateDepartment(
    id,
    data
) {

    if (
        id === null ||
        id === undefined ||
        id === ""
    ) {

        throw new Error(
            "ID phòng ban không hợp lệ."
        );

    }


    return apiRequest(
        `/departments/${encodeURIComponent(id)}`,
        {
            method: "PUT",
            body: JSON.stringify(
                data
            )
        }
    );

}


/* ============================================================
 * DELETE DEPARTMENT
 *
 * DELETE /api/v1/departments/{id}
 * ============================================================
 */

async function deleteDepartment(
    id
) {

    if (
        id === null ||
        id === undefined ||
        id === ""
    ) {

        throw new Error(
            "ID phòng ban không hợp lệ."
        );

    }


    return apiRequest(
        `/departments/${encodeURIComponent(id)}`,
        {
            method: "DELETE"
        }
    );

}