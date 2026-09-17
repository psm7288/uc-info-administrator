document.addEventListener("DOMContentLoaded", function () {
    const bannerForm = document.getElementById("bannerForm");
    const bannerModalTitle = document.getElementById("bannerModalTitle");

    const titleInput = document.getElementById("bannerTitle");
    const subtitleInput = document.getElementById("bannerSubtitle");
    const statusInput = document.getElementById("bannerStatus");
    const startDateInput = document.getElementById("bannerStartDate");
    const endDateInput = document.getElementById("bannerEndDate");
    const noticeInput = document.getElementById("bannerNotice");

    const modalState = document.getElementById("bannerModalState");

    const createButtons = document.querySelectorAll("[data-banner-create]");
    const editButtons = document.querySelectorAll("[data-banner-edit]");

    createButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            resetBannerForm();
        });
    });

    editButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const id = button.dataset.id;

            bannerModalTitle.textContent = "배너 수정";
            bannerForm.action = "/banners/" + id + "/edit";

            titleInput.value = button.dataset.title || "";
            subtitleInput.value = button.dataset.subtitle || "";
            statusInput.value = button.dataset.status || "";
            startDateInput.value = button.dataset.startDate || "";
            endDateInput.value = button.dataset.endDate || "";
            noticeInput.value = button.dataset.noticeId || "";
        });
    });

    function resetBannerForm() {
        bannerModalTitle.textContent = "배너 등록";
        bannerForm.action = "/banners";

        titleInput.value = "";
        subtitleInput.value = "";
        statusInput.value = "";
        startDateInput.value = "";
        endDateInput.value = "";
        noticeInput.value = "";
    }

    if (modalState && modalState.dataset.open === "true") {
        const editingId = modalState.dataset.editingId;

        if (editingId) {
            bannerModalTitle.textContent = "배너 수정";
            bannerForm.action = "/banners/" + editingId + "/edit";
        } else {
            bannerModalTitle.textContent = "배너 등록";
            bannerForm.action = "/banners";
        }

        if (typeof openModal === "function") {
            openModal("bannerModal");
        }
    }
});