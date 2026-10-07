/**
 * ============================================================
 * SYSTEMANAGERCV
 * USER API
 * File: services/api/userApi.js
 * ============================================================
 */

async function searchUsers({
    page = 0,
    size = 10,
    keyword = "",
    roleId = "",
    enabled = "",
    sortBy = "createdDate",
    sortDirection = "DESC"
} = {}) {

    const params = new URLSearchParams();

    params.set("page", page);
    params.set("size", size);
    params.set("sortBy", sortBy);
    params.set("sortDirection", sortDirection);

    if (keyword) {
        params.set("keyword", keyword);
    }

    if (roleId !== "" && roleId !== null) {
        params.set("roleId", roleId);
    }

    if (enabled !== "" && enabled !== null) {
        params.set("enabled", enabled);
    }

    return apiRequest(
        `/users?${params.toString()}`,
        {
            method: "GET"
        }
    );
}


/**
 * GET USER DETAIL
 *
 * GET /api/v1/users/{id}
 */
async function getUserById(id) {

    if (!id) {
        throw new Error("ID người dùng không hợp lệ.");
    }

    return apiRequest(
        `/users/${id}`,
        {
            method: "GET"
        }
    );
}


/**
 * CREATE USER
 *
 * POST /api/v1/users
 */
async function createUser(data) {

    return apiRequest(
        "/users",
        {
            method: "POST",
            body: JSON.stringify(data)
        }
    );
}


/**
 * UPDATE USER
 *
 * PUT /api/v1/users/{id}
 */
async function updateUser(id, data) {

    if (!id) {
        throw new Error("ID người dùng không hợp lệ.");
    }

    return apiRequest(
        `/users/${id}`,
        {
            method: "PUT",
            body: JSON.stringify(data)
        }
    );
}


/**
 * DELETE USER
 *
 * DELETE /api/v1/users/{id}
 */
async function deleteUser(id) {

    if (!id) {
        throw new Error("ID người dùng không hợp lệ.");
    }

    return apiRequest(
        `/users/${id}`,
        {
            method: "DELETE"
        }
    );
}