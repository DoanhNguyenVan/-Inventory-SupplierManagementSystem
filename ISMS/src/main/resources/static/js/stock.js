document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("stockTableBody");
    const searchInput = document.querySelector("input[placeholder*='Search stock movements']");
    const addStockForm = document.getElementById("addStockForm");
    const productSelect = document.getElementById("stockProductSelect");
    const movementDateInput = document.getElementById("movementDateInput");

    const modalEl = document.getElementById("addStockModal");
    const modalInstance = modalEl ? (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)) : null;

    const viewModalEl = document.getElementById("viewStockModal");
    const viewModal = viewModalEl ? (bootstrap.Modal.getInstance(viewModalEl) || new bootstrap.Modal(viewModalEl)) : null;

    const userRole = (window.CURRENT_USER_ROLE || "USER").toUpperCase();
    const isReadOnly = (userRole === "USER");

    let movementsList = [];

    if (movementDateInput && !isReadOnly) {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        movementDateInput.value = now.toISOString().slice(0, 16);
    }

    loadStockMovements();
    if (!isReadOnly) {
        loadProductsDropdown();
    }

    async function loadStockMovements() {
        if (!tableBody) return;
        try {
            const res = await fetch("/api/stock-movements");
            const data = await res.json();
            if (res.ok && data.success) {
                movementsList = data.data;
                renderTable(movementsList);
            } else {
                tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">${data.message || 'Error loading stock movements'}</td></tr>`;
            }
        } catch (e) {
            tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">Unable to connect to server.</td></tr>`;
        }
    }

    function renderTable(movements) {
        if (!movements || movements.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No stock movement records found.</td></tr>`;
            return;
        }

        tableBody.innerHTML = movements.map(m => {
            const isStockIn = m.quantityChange > 0;
            const badgeClass = isStockIn ? "badge-stock-in" : "badge-stock-out";
            const icon = isStockIn ? '<i class="bi bi-arrow-down-left me-1"></i>' : '<i class="bi bi-arrow-up-right me-1"></i>';
            const sign = isStockIn ? `+${m.quantityChange}` : `${m.quantityChange}`;
            const dateStr = m.movementDate ? new Date(m.movementDate).toLocaleString() : 'N/A';
            const performer = m.performedBy ? escapeHtml(m.performedBy) : '<span class="text-muted">System</span>';

            let actionButtonsHtml = "";
            if (isReadOnly) {
                actionButtonsHtml = `
                    <button class="btn btn-outline-info btn-sm btn-view" 
                            data-id="${m.movementId}" 
                            title="View Details">
                        <i class="bi bi-eye"></i> Details
                    </button>
                `;
            } else {
                actionButtonsHtml = `
                    <button class="btn btn-outline-info btn-sm btn-view me-1" 
                            data-id="${m.movementId}" 
                            title="View Details">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-sm btn-delete" 
                            data-id="${m.movementId}" 
                            title="Delete Movement Record">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                `;
            }

            return `
                <tr data-id="${m.movementId}">
                    <td><strong>#${m.movementId}</strong></td>
                    <td class="fw-semibold">${escapeHtml(m.productName || 'Unknown Product')}</td>
                    <td>
                        <span class="badge ${badgeClass} d-inline-flex align-items-center">
                            ${icon} ${sign}
                        </span>
                    </td>
                    <td class="text-muted">${dateStr}</td>
                    <td><span class="badge bg-light text-dark border"><i class="bi bi-person-fill text-primary me-1"></i>${performer}</span></td>
                    <td class="text-end">
                        ${actionButtonsHtml}
                    </td>
                </tr>
            `;
        }).join("");
    }

    async function loadProductsDropdown() {
        if (!productSelect) return;
        try {
            const res = await fetch("/api/products");
            const data = await res.json();
            if (res.ok && data.success) {
                // Only show ACTIVE products - SUSPENDED products cannot have stock movements
                const activeProducts = data.data.filter(p => {
                    const status = (p.status || "ACTIVE").toUpperCase();
                    return status === "ACTIVE";
                });
                if (activeProducts.length === 0) {
                    productSelect.innerHTML = `<option value="">-- No active products available --</option>`;
                } else {
                    productSelect.innerHTML = `<option value="">-- Select Product --</option>` +
                        activeProducts.map(p => `<option value="${p.productId}">${escapeHtml(p.productName)} (Current stock: ${p.quantityInStock})</option>`).join("");
                }
            }
        } catch (e) {
            console.error("Error loading products dropdown:", e);
        }
    }

    if (searchInput) {
        searchInput.addEventListener("input", function () {
            const keyword = searchInput.value.toLowerCase().trim();
            const filtered = movementsList.filter(m =>
                (m.productName && m.productName.toLowerCase().includes(keyword)) ||
                (m.performedBy && m.performedBy.toLowerCase().includes(keyword)) ||
                String(m.quantityChange).includes(keyword) ||
                String(m.movementId).includes(keyword)
            );
            renderTable(filtered);
        });
    }

    if (tableBody) {
        tableBody.addEventListener("click", async function (event) {
            const viewBtn = event.target.closest(".btn-view");
            if (viewBtn) {
                const id = viewBtn.getAttribute("data-id");
                const m = movementsList.find(item => String(item.movementId) === String(id));
                if (m && viewModal) {
                    const isStockIn = m.quantityChange > 0;
                    document.getElementById("viewStockId").textContent = `#${m.movementId}`;
                    document.getElementById("viewStockProduct").textContent = m.productName || "Unknown Product";
                    document.getElementById("viewStockQuantity").innerHTML = isStockIn
                        ? `<span class="text-success fw-bold">+${m.quantityChange}</span>`
                        : `<span class="text-danger fw-bold">${m.quantityChange}</span>`;
                    document.getElementById("viewStockType").innerHTML = isStockIn
                        ? `<span class="badge badge-stock-in"><i class="bi bi-arrow-down-left me-1"></i>Stock In</span>`
                        : `<span class="badge badge-stock-out"><i class="bi bi-arrow-up-right me-1"></i>Stock Out</span>`;
                    document.getElementById("viewStockDate").textContent = m.movementDate ? new Date(m.movementDate).toLocaleString() : "N/A";
                    document.getElementById("viewStockUser").textContent = m.performedBy || "System";
                    viewModal.show();
                }
                return;
            }

            const deleteBtn = event.target.closest(".btn-delete");
            if (deleteBtn && !isReadOnly) {
                const id = deleteBtn.getAttribute("data-id");
                if (confirm(`Are you sure you want to delete Movement Record #${id}? (This action will revert the stock quantity of the product)`)) {
                    try {
                        const res = await fetch(`/api/stock-movements/${id}`, { method: "DELETE" });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            loadStockMovements();
                            loadProductsDropdown();
                        } else {
                            alert(data.message || "Failed to delete stock movement record!");
                        }
                    } catch (e) {
                        alert("Error connecting to server while deleting stock movement.");
                    }
                }
            }
        });
    }

    if (addStockForm && !isReadOnly) {
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
                alert("Quantity change cannot be zero! Enter a positive number for Stock In, negative for Stock Out.");
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
                    if (movementDateInput) {
                        const now = new Date();
                        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
                        movementDateInput.value = now.toISOString().slice(0, 16);
                    }
                    if (modalInstance) modalInstance.hide();
                    loadStockMovements();
                    loadProductsDropdown();
                } else {
                    alert(data.message || "Error recording stock movement!");
                }
            } catch (e) {
                alert("Error connecting to server while saving stock movement.");
            }
        });
    }

    function escapeHtml(str) {
        if (!str) return "";
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});
