/**
 * ============================================================
 * SYSTEMANAGERCV
 * ROLE API
 * File: services/api/roleApi.js
 * ============================================================
 */

async function getRoles() {

    return apiRequest(
        "/roles",
        {
            method: "GET"
        }
    );
}