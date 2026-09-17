document.addEventListener("DOMContentLoaded", function () {
    const scheduleModal = document.getElementById("scheduleModal");
    const scheduleForm = document.getElementById("scheduleForm");
    const scheduleModalTitle = document.getElementById("scheduleModalTitle");

    const titleInput = document.getElementById("scheduleTitle");
    const categoryInput = document.getElementById("scheduleCategory");
    const departmentInput = document.getElementById("scheduleDepartment");
    const gradeInput = document.getElementById("scheduleGrade");
    const startDateInput = document.getElementById("scheduleStart");
    const endDateInput = document.getElementById("scheduleEnd");
    const visibleInput = document.getElementById("scheduleVisible");

    const deleteModal = document.getElementById("deleteModal");

    if (!scheduleModal || !scheduleForm) {
        return;
    }

    const createButtons = document.querySelectorAll("[data-schedule-create]");

    createButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            resetScheduleForm();

            scheduleModalTitle.textContent = "학사 일정 추가";
            scheduleForm.action = "/schedules";
        });
    });

    const editButtons = document.querySelectorAll("[data-schedule-edit]");

    editButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const id = button.dataset.id;

            scheduleModalTitle.textContent = "학사 일정 수정";
            scheduleForm.action = "/schedules/" + id + "/edit";

            titleInput.value = button.dataset.title || "";
            categoryInput.value = button.dataset.category || "";
            departmentInput.value = button.dataset.deptId || "";
            gradeInput.value = button.dataset.grade || "";
            startDateInput.value = button.dataset.startDate || "";
            endDateInput.value = button.dataset.endDate || "";

            visibleInput.checked =
                button.dataset.visible === "true";
        });
    });


    const deleteButtons = document.querySelectorAll("[data-schedule-delete]");

    deleteButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const id = button.dataset.id;
            if (!deleteModal) {
                return;
            }
            const confirmButton = deleteModal.querySelector(".danger-btn");

            if (!confirmButton) {
                return;
            }

            confirmButton.onclick = function () {
                const form = document.createElement("form");
                form.method = "post";
                form.action = "/schedules/" + id + "/delete";
                document.body.appendChild(form);
                form.submit();
            };
        });
    });

    function resetScheduleForm() {
        scheduleForm.reset();

        titleInput.value = "";
        categoryInput.value = "";
        departmentInput.value = "";
        gradeInput.value = "";
        startDateInput.value = "";
        endDateInput.value = "";

        visibleInput.checked = true;
    }

    const modalState = document.getElementById("scheduleModalState");

    if (modalState && modalState.dataset.open === "true") {
        const editingId = modalState.dataset.editingId;
        if (editingId) {
            scheduleModalTitle.textContent = "학사 일정 수정";
            scheduleForm.action = "/schedules/" + editingId + "/edit";
        } else {
            scheduleModalTitle.textContent = "학사 일정 추가";
            scheduleForm.action = "/schedules";
        }

        if (typeof openModal === "function") {
            openModal("scheduleModal");
        }
    }
});