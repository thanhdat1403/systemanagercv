/**
 * ============================================================
 * SYSTEMANAGERCV
 * USER PAGE COMMON LAYOUT
 * File: assets/js/user-page-layout.js
 * ============================================================
 */


/* ============================================================
 * USER PAGE SIDEBAR TOGGLE
 * ============================================================ */

function initializeUserPageSidebarToggle() {

    /*
     * Không đăng ký listener nhiều lần.
     */

    if (
        document.documentElement.dataset
            .smcvUserSidebarToggle === "true"
    ) {

        return;

    }


    document.addEventListener(
        "click",
        function (event) {

            /*
             * Tìm đúng nút hamburger.
             *
             * Có thể click trực tiếp vào:
             *
             * <i class="fa fa-bars"></i>
             *
             * hoặc element cha của nó.
             */

            const toggle =
                event.target.closest(
                    ".sidebar-toggle-box .fa-bars"
                );


            if (!toggle) {

                return;

            }


            const sidebar =
                document.getElementById(
                    "sidebar"
                );


            const mainContent =
                document.getElementById(
                    "main-content"
                );


            const footer =
                document.getElementById(
                    "footer-container"
                );


            if (
                !sidebar ||
                !mainContent
            ) {

                console.error(
                    "USER PAGE SIDEBAR: Không tìm thấy Sidebar/Main Content."
                );

                return;

            }


            /*
             * Chặn các handler cũ của:
             *
             * scripts.js
             * dashboard.js
             * auth.js
             * hoặc handler legacy khác.
             */

            event.preventDefault();

            event.stopPropagation();

            event.stopImmediatePropagation();


            /*
             * Kiểm tra trạng thái hiện tại.
             *
             * Không có class:
             *     Sidebar đang mở
             *
             * Có class:
             *     Sidebar đang đóng
             */

            const shouldCollapse =
                !sidebar.classList.contains(
                    "hide-left-bar"
                );


            /* ====================================================
             * SIDEBAR
             * ==================================================== */

            sidebar.classList.toggle(
                "hide-left-bar",
                shouldCollapse
            );


            /* ====================================================
             * MAIN CONTENT
             * ==================================================== */

            mainContent.classList.toggle(
                "merge-left",
                shouldCollapse
            );


            /* ====================================================
             * FOOTER
             * ==================================================== */

            if (footer) {

                footer.classList.toggle(
                    "merge-left",
                    shouldCollapse
                );

            }


            /*
             * Cập nhật NiceScroll nếu đang sử dụng.
             */

            try {

                if (
                    window.jQuery &&
                    typeof window.jQuery.fn.niceScroll !==
                    "undefined"
                ) {

                    const leftNavigation =
                        window.jQuery(
                            ".leftside-navigation"
                        );


                    if (
                        leftNavigation.length > 0 &&
                        typeof leftNavigation.getNiceScroll ===
                        "function"
                    ) {

                        const niceScroll =
                            leftNavigation.getNiceScroll();


                        if (
                            niceScroll &&
                            typeof niceScroll.hide ===
                            "function" &&
                            typeof niceScroll.show ===
                            "function"
                        ) {

                            if (shouldCollapse) {

                                niceScroll.hide();

                            } else {

                                niceScroll.show();

                                if (
                                    typeof niceScroll.resize ===
                                    "function"
                                ) {

                                    niceScroll.resize();

                                }

                            }

                        }

                    }

                }

            } catch (error) {

                console.warn(
                    "USER PAGE SIDEBAR: Không thể cập nhật NiceScroll.",
                    error
                );

            }

        },
        true
    );


    /*
     * Đánh dấu đã khởi tạo.
     */

    document.documentElement.dataset
        .smcvUserSidebarToggle = "true";


    console.log(
        "USER PAGE SIDEBAR TOGGLE: READY"
    );

}