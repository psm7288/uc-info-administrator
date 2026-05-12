const sidebar = document.getElementById("sidebar");
const menuBtn = document.getElementById("menuBtn");

const navItems = document.querySelectorAll(".nav-item");
const pages = document.querySelectorAll(".page");
const pageTitle = document.getElementById("pageTitle");
const pageLabel = document.getElementById("pageLabel");

const modalBackdrop = document.getElementById("modalBackdrop");
const modals = document.querySelectorAll(".modal");
const modalOpenButtons = document.querySelectorAll("[data-modal]");
const modalCloseButtons = document.querySelectorAll(".modal-close");
const saveButtons = document.querySelectorAll("[data-save]");

const toast = document.getElementById("toast");
const toastButtons = document.querySelectorAll("[data-toast]");

const noticeDrawer = document.getElementById("noticeDrawer");
const openNoticeDrawer = document.getElementById("openNoticeDrawer");
const drawerClose = document.querySelector(".drawer-close");

const pageMoveButtons = document.querySelectorAll("[data-page-move]");
const switches = document.querySelectorAll(".switch");

const pageInfo = {
    dashboardPage: {
        title: "관리자 대시보드",
        label: "Dashboard",
    },
    noticePage: {
        title: "공지 관리",
        label: "Notice",
    },
    trackingPage: {
        title: "열람 트래킹",
        label: "Tracking",
    },
    resendPage: {
        title: "미확인자 재발송",
        label: "Push Resend",
    },
    bannerPage: {
        title: "배너 관리",
        label: "Banner",
    },
    benefitPage: {
        title: "장학금 관리",
        label: "Benefit",
    },
    schedulePage: {
        title: "학사 일정",
        label: "Schedule",
    },
    shuttlePage: {
        title: "셔틀버스 관리",
        label: "Shuttle Bus",
    },
    settingPage: {
        title: "관리자 설정",
        label: "Settings",
    },
};

navItems.forEach(function (item) {
    item.addEventListener("click", function () {
        const pageId = item.dataset.page;
        changePage(pageId);
    });
});

pageMoveButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        const pageId = button.dataset.pageMove;
        changePage(pageId);
    });
});

modalOpenButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        const modalId = button.dataset.modal;
        openModal(modalId);
    });
});

modalCloseButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        closeAllModals();
    });
});

saveButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        const message = button.dataset.save || "처리가 완료되었습니다.";

        closeAllModals();
        showToast(message);
    });
});

toastButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        const message = button.dataset.toast || "처리가 완료되었습니다.";
        showToast(message);
    });
});

switches.forEach(function (switchButton) {
    switchButton.addEventListener("click", function () {
        switchButton.classList.toggle("on");

        if (switchButton.classList.contains("on")) {
            switchButton.setAttribute("aria-label", "활성화됨");
            showToast("설정이 활성화되었습니다.");
        } else {
            switchButton.setAttribute("aria-label", "비활성화됨");
            showToast("설정이 비활성화되었습니다.");
        }
    });
});

if (modalBackdrop) {
    modalBackdrop.addEventListener("click", function () {
        closeAllModals();
    });
}

if (openNoticeDrawer) {
    openNoticeDrawer.addEventListener("click", function () {
        noticeDrawer.classList.remove("hidden");
    });
}

if (drawerClose) {
    drawerClose.addEventListener("click", function () {
        closeDrawer();
    });
}

if (menuBtn) {
    menuBtn.addEventListener("click", function () {
        sidebar.classList.toggle("open");
    });
}

document.addEventListener("click", function (event) {
    const clickedSidebar = event.target.closest("#sidebar");
    const clickedMenuButton = event.target.closest("#menuBtn");

    if (!clickedSidebar && !clickedMenuButton && sidebar.classList.contains("open")) {
        sidebar.classList.remove("open");
    }
});

document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
        closeAllModals();
        closeDrawer();
    }
});

function changePage(pageId) {
    pages.forEach(function (page) {
        page.classList.remove("active");
    });

    const targetPage = document.getElementById(pageId);

    if (targetPage) {
        targetPage.classList.add("active");
    }

    navItems.forEach(function (item) {
        item.classList.remove("active");

        if (item.dataset.page === pageId) {
            item.classList.add("active");
        }
    });

    if (pageInfo[pageId]) {
        pageTitle.textContent = pageInfo[pageId].title;
        pageLabel.textContent = pageInfo[pageId].label;
    }

    if (sidebar) {
        sidebar.classList.remove("open");
    }

    window.scrollTo({
        top: 0,
        behavior: "smooth",
    });
}

function openModal(modalId) {
    const targetModal = document.getElementById(modalId);

    if (!targetModal) {
        return;
    }

    closeDrawer();
    closeAllModals();

    modalBackdrop.classList.remove("hidden");
    targetModal.classList.remove("hidden");
}

function closeAllModals() {
    if (modalBackdrop) {
        modalBackdrop.classList.add("hidden");
    }

    modals.forEach(function (modal) {
        modal.classList.add("hidden");
    });
}

function closeDrawer() {
    if (noticeDrawer) {
        noticeDrawer.classList.add("hidden");
    }
}

let toastTimer = null;

function showToast(message) {
    if (!toast) {
        return;
    }

    toast.textContent = message;
    toast.classList.remove("hidden");

    if (toastTimer) {
        clearTimeout(toastTimer);
    }

    toastTimer = setTimeout(function () {
        toast.classList.add("hidden");
    }, 2200);
}