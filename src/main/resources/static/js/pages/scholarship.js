document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("scholarshipForm");
    const modalTitle = document.getElementById("scholarshipModalTitle");
    const createButtons = document.querySelectorAll("[data-scholarship-create]");
    const editButtons = document.querySelectorAll("[data-scholarship-edit]");
    const deleteForms = document.querySelectorAll(".scholarship-delete-form");
    const modalState = document.getElementById("scholarshipModalState");

    const nameInput = document.getElementById("scholarshipName");
    const typeSelect = document.getElementById("scholarshipType");
    const departmentSelect = document.getElementById("scholarshipDepartment");
    const gradeSelect = document.getElementById("scholarshipGrade");
    const residenceInput = document.getElementById("scholarshipResidence");
    const deadlineInput = document.getElementById("scholarshipDeadline");
    const noticeSelect = document.getElementById("scholarshipNotice");
    const visibleCheckbox = document.getElementById("scholarshipVisible");

    function resetCreateForm() {
        form.action = "/scholarships";
        modalTitle.textContent = "장학금 등록";

        nameInput.value = "";
        typeSelect.value = "";
        departmentSelect.value = "";
        gradeSelect.value = "";
        residenceInput.value = "";
        deadlineInput.value = "";
        noticeSelect.value = "";
        visibleCheckbox.checked = true;
    }

    function fillEditForm(button) {
        const id = button.dataset.id;

        form.action = "/scholarships/" + id + "/edit";
        modalTitle.textContent = "장학금 수정";

        nameInput.value = button.dataset.name || "";
        typeSelect.value = button.dataset.type || "";
        departmentSelect.value = button.dataset.deptId || "";
        gradeSelect.value = button.dataset.targetGrade || "";
        residenceInput.value = button.dataset.residenceCondition || "";
        deadlineInput.value = button.dataset.deadline || "";
        noticeSelect.value = button.dataset.noticeId || "";
        visibleCheckbox.checked = button.dataset.visible === "true";
    }

    createButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            resetCreateForm();
        });
    });

    editButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            fillEditForm(button);
        });
    });

    deleteForms.forEach(function (deleteForm) {
        deleteForm.addEventListener("submit", function (event) {
            if (!confirm("정말 삭제하시겠습니까?")) {
                event.preventDefault();
            }
        });
    });

    if (modalState && modalState.dataset.open === "true") {
        const editingId = modalState.dataset.editingId;

        if (editingId) {
            form.action = "/scholarships/" + editingId + "/edit";
            modalTitle.textContent = "장학금 수정";
        } else {
            form.action = "/scholarships";
            modalTitle.textContent = "장학금 등록";
        }

        if (typeof openModal === "function") {
            openModal("scholarshipModal");
        }
    }
});