document.addEventListener("DOMContentLoaded", function () {
    const shuttleModal = document.getElementById("shuttleModal");
    const shuttleForm = document.getElementById("shuttleForm");
    const shuttleModalTitle = document.getElementById("shuttleModalTitle");
    const routeNameInput = document.getElementById("shuttleRouteName");
    const departureInput = document.getElementById("shuttleDeparture");
    const destinationInput = document.getElementById("shuttleDestination");
    const waypointsInput = document.getElementById("shuttleWaypoints");
    const firstDepartureInput = document.getElementById("shuttleFirstDeparture");
    const lastDepartureInput = document.getElementById("shuttleLastDeparture");
    const statusInput = document.getElementById("shuttleStatus");

    if (!shuttleModal || !shuttleForm) {
        return;
    }

    document.querySelectorAll("[data-shuttle-create]").forEach(function (button) {
        button.addEventListener("click", function () {
            resetShuttleForm();
            shuttleModalTitle.textContent = "셔틀버스 노선 추가";
            shuttleForm.action = "/shuttles";
        });
    });

    document.querySelectorAll("[data-shuttle-edit]").forEach(function (button) {
        button.addEventListener("click", function () {
            populateShuttleForm(button, button.dataset.status || "ACTIVE");
        });
    });

    document.querySelectorAll("[data-shuttle-suspend]").forEach(function (button) {
        button.addEventListener("click", function () {
            populateShuttleForm(button, "SUSPENDED");
        });
    });

    function populateShuttleForm(button, status) {
        shuttleModalTitle.textContent = "셔틀버스 노선 수정";
        shuttleForm.action = "/shuttles/" + button.dataset.id + "/edit";
        routeNameInput.value = button.dataset.routeName || "";
        departureInput.value = button.dataset.departure || "";
        destinationInput.value = button.dataset.destination || "";
        waypointsInput.value = button.dataset.waypoints || "";
        firstDepartureInput.value = button.dataset.firstDeparture || "";
        lastDepartureInput.value = button.dataset.lastDeparture || "";
        statusInput.value = status;
    }

    function resetShuttleForm() {
        shuttleForm.reset();
        routeNameInput.value = "";
        departureInput.value = "";
        destinationInput.value = "";
        waypointsInput.value = "";
        firstDepartureInput.value = "";
        lastDepartureInput.value = "";
        statusInput.value = "ACTIVE";
    }
    const modalState = document.getElementById("shuttleModalState");

    if (modalState && modalState.dataset.open === "true") {
        const editingId = modalState.dataset.editingId;

        if (editingId) {
            shuttleModalTitle.textContent = "셔틀버스 노선 수정";
            shuttleForm.action = "/shuttles/" + editingId + "/edit";
        } else {
            shuttleModalTitle.textContent = "셔틀버스 노선 추가";
            shuttleForm.action = "/shuttles";
        }

        if (typeof openModal === "function") {
            openModal("shuttleModal");
        }
    }
});