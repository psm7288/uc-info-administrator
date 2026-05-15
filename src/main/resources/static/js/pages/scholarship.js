document.addEventListener("DOMContentLoaded", function () {
    const scholarshipButtons = document.querySelectorAll("[data-scholarship-toast]");

    scholarshipButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const message = button.dataset.scholarshipToast || "장학금 작업이 처리되었습니다.";

            if (typeof showToast === "function") {
                showToast(message);
            }
        });
    });
});