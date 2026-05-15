document.addEventListener("DOMContentLoaded", function () {
    const scheduleButtons = document.querySelectorAll("[data-schedule-toast]");

    scheduleButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const message = button.dataset.scheduleToast || "학사 일정 작업이 처리되었습니다.";

            if (typeof showToast === "function") {
                showToast(message);
            }
        });
    });
});