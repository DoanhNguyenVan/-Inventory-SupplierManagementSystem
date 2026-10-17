// ==========================================================
// File: stock.js - Basic UI interactions for Stock Movements page
// (Data rendering and CRUD operations are handled by Backend)
// ==========================================================

document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("stockTableBody");
    const searchInput = document.querySelector("input[placeholder='Search stock movements...']");
    const addStockForm = document.getElementById("addStockForm");

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

    // 2. Confirm before deleting a stock movement
    if (tableBody) {
        tableBody.addEventListener("click", function (event) {
            const deleteBtn = event.target.closest(".btn-danger, .btn-delete");
            if (deleteBtn) {
                const confirmed = confirm("Are you sure you want to delete this stock movement?");
                if (!confirmed) {
                    event.preventDefault(); // Cancel deletion if user clicks Cancel
                }
            }
        });
    }

    // 3. Basic client-side validation for Add Stock Movement form
    if (addStockForm) {
        addStockForm.addEventListener("submit", function (event) {
            const quantityInput = addStockForm.querySelector("input[name='quantity_change']");
            if (quantityInput && Number(quantityInput.value) === 0) {
                alert("Quantity change cannot be zero!");
                event.preventDefault();
            }
        });
    }
});
