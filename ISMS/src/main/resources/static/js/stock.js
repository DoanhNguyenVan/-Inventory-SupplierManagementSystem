// ==========================================================
// File: stock.js - Interactivity & API CRUD for Stock Movements
// ==========================================================

document.addEventListener("DOMContentLoaded", function () {
    // 1. Guard page authentication
    if (window.Auth) {
        Auth.requireAuth();
    }

    const tableBody = document.getElementById("stockTableBody");
    const searchInput = document.querySelector("input[placeholder='Search stock movements...']");
    const addStockForm = document.getElementById("addStockForm");
    const productSelect = document.getElementById("stockProductSelect");
    const movementDateInput = document.getElementById("movementDateInput");

    // Initialize datetime picker with current local time
    if (movementDateInput) {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        movementDateInput.value = now.toISOString().slice(0, 16);
    }

    loadStockMovements();
    loadProductsDropdown();

    // 2. Fetch and render stock movements from backend
    async function loadStockMovements() {
        if (!tableBody) return;
        try {
            const res = await fetch("/api/stock-movements");
            const data = await res.json();
            if (res.ok && data.success) {
                renderTable(data.data);
            } else {
                tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">${data.message || 'Error loading stock movements'}</td></tr>`;
            }
        } catch (e) {
            tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Unable to connect to server.</td></tr>`;
        }
    }

    // Render table rows
    function renderTable(movements) {
        if (!movements || movements.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">No stock movements recorded yet.</td></tr>`;
            return;
        }

        tableBody.innerHTML = movements.map(m => {
            const isStockIn = m.quantityChange > 0;
            const badgeClass = isStockIn ? "badge-stock-in" : "badge-stock-out";
            const icon = isStockIn ? '<i class="bi bi-arrow-down-left"></i>' : '<i class="bi bi-arrow-up-right"></i>';
            const sign = isStockIn ? `+${m.quantityChange}` : `${m.quantityChange}`;

            // Format date nicely
            const dateStr = m.movementDate ? new Date(m.movementDate).toLocaleString() : 'N/A';

            return `
                <tr data-id="${m.movementId}">
                    <td><strong>#${m.movementId}</strong></td>
                    <td class="fw-semibold">${m.productName || 'Unknown Product'}</td>
                    <td>
                        <span class="badge ${badgeClass} d-inline-flex align-items-center gap-1">
                            ${icon} ${sign}
                        </span>
                    </td>
                    <td class="text-muted">${dateStr}</td>
                    <td>
                        <button class="btn btn-outline-danger btn-sm btn-delete" data-id="${m.movementId}" title="Delete Stock Movement">
                            <i class="bi bi-trash"></i> Delete
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }

    // 3. Fetch products to populate dropdown
    async function loadProductsDropdown() {
        if (!productSelect) return;
        try {
            const res = await fetch("/api/products");
            const data = await res.json();
            if (res.ok && data.success) {
                productSelect.innerHTML = `<option value="">Select Product</option>` +
                    data.data.map(p => `<option value="${p.productId}">${p.productName} (In stock: ${p.quantityInStock})</option>`).join("");
            }
        } catch (e) {
            console.error("Failed to load products for dropdown", e);
        }
    }

    // 4. Live search filter on table rows
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

    // 5. Delete stock movement with confirmation
    if (tableBody) {
        tableBody.addEventListener("click", async function (event) {
            const deleteBtn = event.target.closest(".btn-delete");
            if (deleteBtn) {
                const id = deleteBtn.getAttribute("data-id");
                if (confirm(`Are you sure you want to delete Movement #${id}?`)) {
                    try {
                        const res = await fetch(`/api/stock-movements/${id}`, { method: "DELETE" });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            loadStockMovements();
                            loadProductsDropdown(); // Reload product stock levels
                        } else {
                            alert(data.message || "Failed to delete stock movement");
                        }
                    } catch (e) {
                        alert("Error connecting to server to delete stock movement.");
                    }
                }
            }
        });
    }

    // 6. Handle Add Stock Movement form submit
    if (addStockForm) {
        addStockForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const productIdVal = addStockForm.product_id.value;
            const quantityChange = parseInt(addStockForm.quantity_change.value, 10);
            const movementDate = addStockForm.movement_date.value;

            if (!productIdVal) {
                alert("Please select a product!");
                return;
            }
            if (isNaN(quantityChange) || quantityChange === 0) {
                alert("Quantity change cannot be zero!");
                return;
            }

            try {
                const res = await fetch("/api/stock-movements", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        productId: parseInt(productIdVal, 10),
                        quantityChange: quantityChange,
                        movementDate: movementDate ? movementDate + ":00" : null
                    })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    addStockForm.reset();
                    const modalEl = document.getElementById("addStockModal");
                    const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                    modalInstance.hide();
                    loadStockMovements();
                    loadProductsDropdown();
                } else {
                    alert(data.message || "Failed to save stock movement");
                }
            } catch (e) {
                alert("Error connecting to server to save stock movement.");
            }
        });
    }
});
