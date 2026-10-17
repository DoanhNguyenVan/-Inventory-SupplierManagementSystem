// ==========================================================
// File: products.js - Basic UI interactions for Products page
// (Data rendering and CRUD operations are handled by Backend)
// ==========================================================

document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("productTableBody");
    const searchInput = document.querySelector("input[placeholder='Search products...']");
    const addProductForm = document.getElementById("addProductForm");

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

    // 2. Confirm before deleting a product
    if (tableBody) {
        tableBody.addEventListener("click", function (event) {
            const deleteBtn = event.target.closest(".btn-danger, .btn-delete");
            if (deleteBtn) {
                const confirmed = confirm("Are you sure you want to delete this product?");
                if (!confirmed) {
                    event.preventDefault(); // Cancel deletion if user clicks Cancel
                }
            }
        });
    }

    // 3. Basic client-side validation for Add Product form
    if (addProductForm) {
        addProductForm.addEventListener("submit", function (event) {
            const quantityInput = addProductForm.querySelector("input[name='quantity_in_stock']");
            if (quantityInput && Number(quantityInput.value) < 0) {
                alert("Quantity cannot be negative!");
                event.preventDefault();
            }
        });
    }
});
