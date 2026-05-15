document.addEventListener("DOMContentLoaded", function () {
    const shuttleButtons = document.querySelectorAll("[data-shuttle-toast]");

    shuttleButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const message = button.dataset.shuttleToast || "셔틀버스 정보가 처리되었습니다.";

            if (typeof showToast === "function") {
                showToast(message);
            }
        });
    });
});