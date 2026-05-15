document.addEventListener("DOMContentLoaded", function () {
    const trackingFilterForm = document.querySelector(".tracking-filter-form");

    if (!trackingFilterForm) {
        return;
    }

    trackingFilterForm.addEventListener("submit", function (event) {
        event.preventDefault();

        if (typeof showToast === "function") {
            showToast("열람 조건이 적용되었습니다.");
        }
    });
});