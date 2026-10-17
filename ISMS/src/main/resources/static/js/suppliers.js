// ==========================================================
// File: suppliers.js - Basic UI interactions for Suppliers page
// (Data rendering and CRUD operations are handled by Backend)
// ==========================================================

document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("supplierTableBody");
    const searchInput = document.querySelector("input[placeholder='Search suppliers...']");
    const addSupplierForm = document.getElementById("addSupplierForm");

    // 1. Live search filter on table rows
    if (searchInput && tableBody) {
        searchInput.addEventListener("input", function () {
            const keyword = searchInput.value.toLowerCase().trim();
            const rows = tableBody.querySelectorAll("tr");

            rows.forEach(function (row) {
                const text = row.innerText.toLowerCase();
                if (text.includes(keyword)) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            });
        });
    }

    // 2. Confirm before deleting a supplier
    if (tableBody) {
        tableBody.addEventListener("click", function (event) {
            const deleteBtn = event.target.closest(".btn-danger, .btn-delete");
            if (deleteBtn) {
                const confirmed = confirm("Are you sure you want to delete this supplier?");
                if (!confirmed) {
                    event.preventDefault(); // Cancel deletion if user clicks Cancel
                }
            }
        });
    }

    // 3. Basic client-side validation for Add Supplier form
    if (addSupplierForm) {
        addSupplierForm.addEventListener("submit", function (event) {
            const phoneInput = addSupplierForm.querySelector("input[name='phone']");
            if (phoneInput && phoneInput.value.trim().length < 6) {
                alert("Please enter a valid phone number!");
                event.preventDefault();
            }
        });
    }
});
