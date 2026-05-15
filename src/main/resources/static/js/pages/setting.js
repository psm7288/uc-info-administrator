document.addEventListener("DOMContentLoaded", function () {
    const settingButtons = document.querySelectorAll("[data-setting-toast]");

    settingButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const message = button.dataset.settingToast || "설정이 저장되었습니다.";

            if (typeof showToast === "function") {
                showToast(message);
            }
        });
    });
});