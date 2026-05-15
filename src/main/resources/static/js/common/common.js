document.addEventListener("DOMContentLoaded", function () {
    const noticeDrawer = document.getElementById("noticeDrawer");
    const openNoticeDrawer = document.getElementById("openNoticeDrawer");
    const drawerClose = document.querySelector(".drawer-close");
    const switches = document.querySelectorAll(".switch");

    if (openNoticeDrawer && noticeDrawer) {
        openNoticeDrawer.addEventListener("click", function () {
            noticeDrawer.classList.remove("hidden");
        });
    }

    if (drawerClose) {
        drawerClose.addEventListener("click", function () {
            closeDrawer();
        });
    }

    switches.forEach(function (switchButton) {
        switchButton.addEventListener("click", function () {
            switchButton.classList.toggle("on");

            if (switchButton.classList.contains("on")) {
                switchButton.setAttribute("aria-label", "활성화됨");

                if (typeof showToast === "function") {
                    showToast("설정이 활성화되었습니다.");
                }
            } else {
                switchButton.setAttribute("aria-label", "비활성화됨");

                if (typeof showToast === "function") {
                    showToast("설정이 비활성화되었습니다.");
                }
            }
        });
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            if (typeof closeAllModals === "function") {
                closeAllModals();
            }

            if (typeof closeDrawer === "function") {
                closeDrawer();
            }
        }
    });
});