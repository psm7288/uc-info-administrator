let toastTimer = null;

function showToast(message) {
    const toast = document.getElementById("toast");

    if (!toast) {
        return;
    }

    toast.textContent = message;
    toast.classList.remove("hidden");

    if (toastTimer) {
        clearTimeout(toastTimer);
    }

    toastTimer = setTimeout(function () {
        toast.classList.add("hidden");
    }, 2200);
}

document.addEventListener("DOMContentLoaded", function () {
    const toastButtons = document.querySelectorAll("[data-toast]");

    toastButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const message = button.dataset.toast || "처리가 완료되었습니다.";
            showToast(message);
        });
    });
});