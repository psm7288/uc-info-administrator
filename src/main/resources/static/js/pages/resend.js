document.addEventListener("DOMContentLoaded", function () {
    const resendButtons = document.querySelectorAll("[data-resend-toast]");

    resendButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const message = button.dataset.resendToast || "재발송 처리가 완료되었습니다.";

            if (typeof showToast === "function") {
                showToast(message);
            }
        });
    });
});