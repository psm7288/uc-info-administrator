document.addEventListener("DOMContentLoaded", function () {
    const noticeForm = document.querySelector(".notice-form");
    const titleInput = document.getElementById("title");
    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");

    if (!noticeForm) {
        return;
    }

    noticeForm.addEventListener("submit", function (event) {
        const title = titleInput ? titleInput.value.trim() : "";

        if (!title) {
            event.preventDefault();

            if (typeof showToast === "function") {
                showToast("공지 제목을 입력해주세요.");
            } else {
                alert("공지 제목을 입력해주세요.");
            }

            if (titleInput) {
                titleInput.focus();
            }

            return;
        }

        if (startDateInput && endDateInput) {
            const startDate = startDateInput.value;
            const endDate = endDateInput.value;

            if (startDate && endDate && startDate > endDate) {
                event.preventDefault();

                if (typeof showToast === "function") {
                    showToast("게시 종료일은 시작일보다 빠를 수 없습니다.");
                } else {
                    alert("게시 종료일은 시작일보다 빠를 수 없습니다.");
                }

                endDateInput.focus();
            }
        }
    });
});