document.addEventListener("DOMContentLoaded", function () {
    const sidebar = document.getElementById("sidebar");
    const menuBtn = document.getElementById("menuBtn");
    const navItems = document.querySelectorAll(".sidebar-nav .nav-item");

    if (menuBtn && sidebar) {
        menuBtn.addEventListener("click", function () {
            sidebar.classList.toggle("open");
        });
    }

    navItems.forEach(function (item) {
        const itemPath = item.getAttribute("href");
        const currentPath = window.location.pathname;

        if (itemPath === currentPath) {
            item.classList.add("active");
        }

        item.addEventListener("click", function () {
            if (sidebar) {
                sidebar.classList.remove("open");
            }
        });
    });

    document.addEventListener("click", function (event) {
        const clickedSidebar = event.target.closest("#sidebar");
        const clickedMenuButton = event.target.closest("#menuBtn");

        if (
            sidebar &&
            !clickedSidebar &&
            !clickedMenuButton &&
            sidebar.classList.contains("open")
        ) {
            sidebar.classList.remove("open");
        }
    });
});