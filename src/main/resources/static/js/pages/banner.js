document.addEventListener("DOMContentLoaded", function () {
    // 배너 등록/수정 모달 요소
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

    // 신규 등록 시 폼 초기화
    createButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            resetBannerForm();
        });
    });

    // 수정 시 기존 배너 데이터 세팅
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

    // 신규 등록 폼 상태로 초기화
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

    // 검증 실패 시 기존 입력값을 유지한 채 모달 다시 열기
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