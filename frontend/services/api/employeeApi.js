
// =====================================================
// SYSTEMANAGERCV - EMPLOYEE API
// =====================================================


/**
 * Lấy danh sách Employee.
 *
 * Backend:
 * GET /api/v1/employees
 *
 * @param {Object} params
 * @param {number} params.page
 * @param {number} params.size
 * @param {string} params.keyword
 */
async function getEmployees({
    page = 0,
    size = 10,
    keyword = ""
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


    if (keyword) {

        params.set(
            "keyword",
            keyword
        );
    }


    return apiRequest(
        `/employees?${params.toString()}`,
        {
            method: "GET"
        }
    );
}


/**
 * Lấy chi tiết Employee.
 *
 * Sẽ dùng ở bước Employee Detail.
 */
async function getEmployeeById(id) {

    return apiRequest(
        `/employees/${id}`,
        {
            method: "GET"
        }
    );
}


/**
 * Xóa Employee.
 *
 * Sẽ dùng ở bước tiếp theo.
 */
async function deleteEmployee(id) {

    return apiRequest(
        `/employees/${id}`,
        {
            method: "DELETE"
        }
    );
}

async function updateEmployee(id, data) {

    return apiRequest(
        `/employees/${id}`,
        {
            method: "PUT",
            body: JSON.stringify(data)
        }
    );
}

async function getEmployeeEditOptions(id){

    return apiRequest(
        `/employees/${id}/edit-options`,
        {
            method: "GET"
        }
    );
}

async function getEmployeeCreateOptions() {
    return apiRequest(
        "/employees/create-options",
        {
            method: "GET"
        }
    );
}

async function createEmployee(data) {

    return apiRequest(
        "/employees",
        {
            method: "POST",
            body: JSON.stringify(data)
        }
    );
}