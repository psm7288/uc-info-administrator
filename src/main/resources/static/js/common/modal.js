function openModal(modalId) {
    const modalBackdrop = document.getElementById("modalBackdrop");
    const targetModal = document.getElementById(modalId);

    if (!modalBackdrop || !targetModal) {
        return;
    }

    closeDrawer();
    closeAllModals();

    modalBackdrop.classList.remove("hidden");
    targetModal.classList.remove("hidden");
}

function closeAllModals() {
    const modalBackdrop = document.getElementById("modalBackdrop");
    const modals = document.querySelectorAll(".modal");

    if (modalBackdrop) {
        modalBackdrop.classList.add("hidden");
    }

    modals.forEach(function (modal) {
        modal.classList.add("hidden");
    });
}

function closeDrawer() {
    const noticeDrawer = document.getElementById("noticeDrawer");

    if (noticeDrawer) {
        noticeDrawer.classList.add("hidden");
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const modalBackdrop = document.getElementById("modalBackdrop");
    const modalOpenButtons = document.querySelectorAll("[data-modal]");
    const modalCloseButtons = document.querySelectorAll(".modal-close");
    const saveButtons = document.querySelectorAll("[data-save]");

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

            if (typeof showToast === "function") {
                showToast(message);
            }
        });
    });

    if (modalBackdrop) {
        modalBackdrop.addEventListener("click", function () {
            closeAllModals();
        });
    }
});