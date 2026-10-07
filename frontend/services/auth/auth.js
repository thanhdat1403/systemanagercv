/* =========================================================
   SYSTEMANAGERCV
   AUTH.JS
   =========================================================

   Vai trò:

   - Lấy thông tin người đăng nhập từ /api/v1/auth/me

   - Hiển thị fullName lên Header và Dashboard

   - Hiển thị Role

   - Khởi tạo Sidebar

   - Xử lý Logout dùng JWT HttpOnly Cookie

   File này KHÔNG tự chạy bằng DOMContentLoaded.

   Mỗi page sau khi load header/sidebar sẽ gọi:

       initializeHeaderUser();

       initializeSidebar();

       setupLogout();

/* =========================================================
   INITIALIZE HEADER USER
   ========================================================= */

async function initializeHeaderUser() {
  console.log("=== INITIALIZE HEADER USER ===");

  const sessionUsername = sessionStorage.getItem("username") || "";

  const rolesJson = sessionStorage.getItem("roles");

  let roles = [];

  if (rolesJson) {
    try {
      roles = JSON.parse(rolesJson);
    } catch (error) {
      console.error("Không thể parse roles:", error);
      roles = [];
    }
  }

  const sessionRole = getCurrentRole(roles);

  let currentUser = null;
  let displayName = sessionUsername || "User";
  let backendRole = sessionRole || "";

  if (typeof getCurrentUser !== "function") {
    console.error("Không tìm thấy getCurrentUser(). Hãy kiểm tra authApi.js.");
  } else {
    try {
      const response = await getCurrentUser();
      currentUser = response?.data || null;

      if (currentUser) {
        displayName =
          String(currentUser.fullName || "").trim() ||
          currentUser.username ||
          sessionUsername ||
          "User";

        backendRole = normalizeRole(currentUser.role || sessionRole);
      } else {
        console.warn(
          "GET /api/v1/auth/me không trả về data. Dùng session fallback.",
        );
      }
    } catch (error) {
      console.error("LOAD CURRENT USER ERROR:", error);
    }
  }

  backendRole = normalizeRole(backendRole);

  updateHeaderDisplayName(displayName);
  updateHeaderRole(backendRole || "USER");

  refreshNotificationHeaderBadge();

  if (document.body && backendRole) {
    document.body.dataset.currentRole = backendRole;
  }

  return {
    currentUser,
    displayName,
    role: backendRole,
  };
}

/* ============================================================
   LOAD UNREAD NOTIFICATION COUNT (Tải những thông báo chưa đọc)
   ============================================================ */

   async function refreshNotificationHeaderBadge(){

    const badge = 
    document.getElementById(
        "notification-count"
    );

    const message =
    document.getElementById(
        "notification-message"
    );

    if (!badge){
        return;
    }

    try {

        const response =
            await fetch(
                API_BASE_URL +
                "/notifications/unread-count",
                {
                    method:
                        "GET",

                    credentials:
                        "include",

                    headers: {
                        "Content-Type":
                            "application/json"
                    }
                }
            );


        if (
            !response.ok
        ) {

            return;

        }


        const result =
            await response.json();


        const count =
            Number(
                result?.data ??
                0
            );


        badge.textContent =
            count > 99
                ? "99+"
                : String(
                    count
                );


        if (message) {

            message.textContent =
                count > 0
                    ? `Bạn có ${count} thông báo chưa đọc.`
                    : "Không có thông báo mới.";

        }


    } catch (error) {

        console.warn(
            "NOTIFICATION HEADER BADGE ERROR:",
            error
        );

    }
   }

/* =========================================================
   UPDATE DISPLAY NAME
   ========================================================= */

function updateHeaderDisplayName(displayName) {
  const safeDisplayName = String(displayName || "User").trim() || "User";

  /* =====================================================



       HEADER



       ===================================================== */

  const headerUsername = document.getElementById("header-username");

  if (headerUsername) {
    headerUsername.textContent = safeDisplayName;
  }

  /* =====================================================



       DASHBOARD WELCOME



       ===================================================== */

  const dashboardUsername = document.getElementById("dashboard-username");

  if (dashboardUsername) {
    dashboardUsername.textContent = safeDisplayName;
  }

  /* =====================================================



       DASHBOARD ACCOUNT



       ===================================================== */

  const dashboardAccountUsername = document.getElementById(
    "dashboard-account-username",
  );

  if (dashboardAccountUsername) {
    dashboardAccountUsername.textContent = safeDisplayName;
  }

  console.log(
    "DISPLAY NAME UPDATED =",

    safeDisplayName,
  );
}

/* =========================================================



   UPDATE HEADER ROLE



   ========================================================= */

function updateHeaderRole(role) {
  const headerRole = document.getElementById("header-role");

  if (!headerRole) {
    return;
  }

  const normalizedRole = normalizeRole(role || "USER");

  headerRole.textContent = formatRole(normalizedRole);

  headerRole.dataset.role = normalizedRole;
}

/* =========================================================



   GET CURRENT ROLE



   ========================================================= */

function getCurrentRole(roles) {
  if (!Array.isArray(roles) || roles.length === 0) {
    return "";
  }

  const validRoles = ["ADMIN", "HR", "TECH_LEAD", "EMPLOYEE"];

  const role = roles.find(function (item) {
    const normalized = normalizeRole(item);

    return validRoles.includes(normalized);
  });

  return role ? normalizeRole(role) : "";
}

/* =========================================================



   NORMALIZE ROLE



   ========================================================= */

function normalizeRole(role) {
  return String(role || "")
    .replace(
      /^ROLE_/,

      "",
    )

    .trim()

    .toUpperCase();
}

/* =========================================================



   FORMAT ROLE



   ========================================================= */

function formatRole(role) {
  switch (normalizeRole(role)) {
    case "ADMIN":
      return "ADMIN";

    case "HR":
      return "HR";

    case "TECH_LEAD":
      return "TECH LEAD";

    case "EMPLOYEE":
      return "EMPLOYEE";

    default:
      return role || "USER";
  }
}

/* =========================================================



   SIDEBAR ROLE PERMISSION



   ========================================================= */

function applySidebarRoleVisibility(roleOverride = "") {
  /*



     * =====================================================



     * 1. LẤY ROLE



     * =====================================================



     */

  let currentRole = roleOverride;

  /*



     * =====================================================



     * 2. NẾU KHÔNG TRUYỀN ROLE



     *    -> LẤY TỪ SESSION STORAGE



     * =====================================================



     */

  if (!currentRole) {
    const rolesJson = sessionStorage.getItem("roles");

    if (!rolesJson) {
      console.warn("SIDEBAR ROLE: Không tìm thấy roles.");

      return;
    }

    let roles = [];

    try {
      roles = JSON.parse(rolesJson);
    } catch (error) {
      console.error(
        "SIDEBAR ROLE: Không thể parse roles.",

        error,
      );

      return;
    }

    currentRole = getCurrentRole(roles);
  }

  /*



     * =====================================================



     * 3. CHUẨN HÓA ROLE



     * =====================================================



     */

  currentRole = String(currentRole || "")
    .replace(
      /^ROLE_/,

      "",
    )

    .trim()

    .toUpperCase();

  /*



     * =====================================================



     * 4. KIỂM TRA ROLE



     * =====================================================



     */

  const validRoles = ["ADMIN", "HR", "TECH_LEAD", "EMPLOYEE"];

  if (!validRoles.includes(currentRole)) {
    console.warn(
      "SIDEBAR ROLE: Role không hợp lệ:",

      currentRole,
    );

    return;
  }

  console.log(
    "SIDEBAR ROLE CURRENT =",

    currentRole,
  );

  /*



     * =====================================================



     * 5. LẤY TẤT CẢ MENU CÓ data-roles



     * =====================================================



     */

  const roleMenus = document.querySelectorAll("#nav-accordion [data-roles]");

  roleMenus.forEach(function (menu) {
    const allowedRoles = menu.dataset.roles

      .split(",")

      .map(function (item) {
        return item

          .trim()

          .toUpperCase();
      });

    const isAllowed = allowedRoles.includes(currentRole);

    if (isAllowed) {
      menu.style.display = "";
    } else {
      menu.style.display = "none";
    }
  });

  /*



     * =====================================================



     * 6. LƯU ROLE LÊN BODY



     *    DÙNG CHO CÁC MODULE KHÁC SAU NÀY



     * =====================================================



     */

  if (document.body) {
    document.body.dataset.currentRole = currentRole;
  }

  console.log(
    "SIDEBAR ROLE PERMISSION APPLIED:",

    currentRole,
  );
}

/* =========================================================



   INITIALIZE SIDEBAR



   ========================================================= */

function initializeSidebar(roleOverride = "") {
  console.log("=== INITIALIZE SIDEBAR ===");

  /* =====================================================

       0. ROLE PERMISSION

       ===================================================== */

  applySidebarRoleVisibility(roleOverride);

  console.log("Sidebar: Role permission đã được áp dụng.");

  /* =====================================================



       1. KIỂM TRA JQUERY



       ===================================================== */

  if (typeof jQuery === "undefined") {
    console.error("Sidebar: jQuery chưa được load.");

    return;
  }

  /* =====================================================



       2. KIỂM TRA DC ACCORDION



       ===================================================== */

  if (typeof jQuery.fn.dcAccordion === "undefined") {
    console.error("Sidebar: dcAccordion chưa được load.");

    return;
  }

  /* =====================================================



       3. LẤY SIDEBAR MENU



       ===================================================== */

  const navAccordion = jQuery("#nav-accordion");

  if (navAccordion.length === 0) {
    console.error("Sidebar: #nav-accordion không tồn tại.");

    return;
  }

  console.log("Sidebar: tìm thấy #nav-accordion.");

  /* =====================================================



       4. ĐÓNG TẤT CẢ SUBMENU BAN ĐẦU



       ===================================================== */

  navAccordion

    .find("ul.sub")

    .hide();

  /* =====================================================



       5. KHỞI TẠO DC ACCORDION



       ===================================================== */

  navAccordion.dcAccordion({
    eventType: "click",

    autoClose: true,

    saveState: false,

    disableLink: true,

    speed: "slow",

    showCount: false,

    autoExpand: false,

    classExpand: "dcjq-current-parent",
  });

  console.log("Sidebar: DC Accordion đã được khởi tạo.");

  /* =====================================================



       6. NICE SCROLL



       ===================================================== */

  if (typeof jQuery.fn.niceScroll !== "undefined") {
    const leftNavigation = jQuery(".leftside-navigation");

    if (leftNavigation.length > 0) {
      leftNavigation.niceScroll({
        cursorcolor: "#8b5c7e",

        cursorborder: "0px solid #fff",

        cursorborderradius: "0px",

        cursorwidth: "3px",
      });

      if (typeof leftNavigation.getNiceScroll === "function") {
        leftNavigation

          .getNiceScroll()

          .resize();
      }

      console.log("Sidebar: NiceScroll đã được khởi tạo.");
    }
  } else {
    console.warn("Sidebar: NiceScroll chưa được load.");
  }

  console.log("=== SIDEBAR INITIALIZATION COMPLETE ===");
}

/* =========================================================



   LOGOUT



   ========================================================= */

function setupLogout() {
  /*



     * Header và Sidebar được load động bằng innerHTML.



     *



     * Dùng event delegation trên document để đảm bảo



     * nút Logout luôn hoạt động.



     */

  if (document.documentElement.dataset.smcvLogoutInitialized === "true") {
    return;
  }

  document.addEventListener(
    "click",

    async function (event) {
      const logoutButton = event.target.closest(
        "#logout-button, #sidebar-logout-button",
      );

      if (!logoutButton) {
        return;
      }

      event.preventDefault();

      event.stopPropagation();

      event.stopImmediatePropagation();

      await handleLogout();
    },

    true,
  );

  document.documentElement.dataset.smcvLogoutInitialized = "true";

  console.log("LOGOUT: READY");
}

/* =========================================================



   HANDLE LOGOUT



   ========================================================= */

async function handleLogout() {
  try {
    /*



         * Xóa trạng thái phía Frontend.



         */

    sessionStorage.clear();

    /*



         * Xóa JWT HttpOnly Cookie phía Backend.



         */

    if (typeof logout === "function") {
      try {
        await logout();
      } catch (error) {
        /*



                 * Backend lỗi cũng không được ngăn



                 * việc chuyển về Login.



                 */

        console.warn(
          "LOGOUT API WARNING:",

          error,
        );
      }
    }
  } finally {
    window.location.replace("../login/login.html");
  }
}
