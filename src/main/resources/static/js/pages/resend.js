document.addEventListener("DOMContentLoaded", function () {
    const resendForms = document.querySelectorAll(".resend-form");
    const resendState = document.getElementById("resendState");

    resendForms.forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (!confirm("이 공지를 다시 발송하시겠습니까?")) {
                event.preventDefault();
            }
        });
    });

    if (resendState && resendState.dataset.message) {
        if (typeof showToast === "function") {
            showToast(resendState.dataset.message);
        }
    }
});