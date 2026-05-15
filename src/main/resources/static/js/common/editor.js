document.addEventListener("DOMContentLoaded", function () {
    const editor = document.getElementById("noticeEditor");
    const hiddenContent = document.getElementById("noticeContent");
    const editorButtons = document.querySelectorAll(".editor-toolbar button");
    const noticeForm = document.querySelector(".notice-form");

    if (!editor || !hiddenContent) {
        return;
    }

    editorButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const command = button.dataset.command;

            if (!command) {
                return;
            }

            if (command === "createLink") {
                const url = prompt("링크 주소를 입력하세요.");

                if (url) {
                    document.execCommand("createLink", false, url);
                }

                editor.focus();
                return;
            }

            document.execCommand(command, false, null);
            editor.focus();
        });
    });

    editor.addEventListener("input", function () {
        hiddenContent.value = editor.innerHTML;
    });

    if (noticeForm) {
        noticeForm.addEventListener("submit", function (event) {
            const plainText = editor.textContent.trim();

            if (!plainText) {
                event.preventDefault();

                if (typeof showToast === "function") {
                    showToast("공지 내용을 입력해주세요.");
                } else {
                    alert("공지 내용을 입력해주세요.");
                }

                editor.focus();
                return;
            }

            hiddenContent.value = editor.innerHTML;
        });
    }
});