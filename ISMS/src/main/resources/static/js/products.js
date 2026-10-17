// ==========================================================
// File: products.js - Interactivity & API CRUD for Products
// ==========================================================

document.addEventListener("DOMContentLoaded", function () {
    // 1. Guard page authentication
    if (window.Auth) {
        Auth.requireAuth();
    }

    const tableBody = document.getElementById("productTableBody");
    const searchInput = document.querySelector("input[placeholder='Search products...']");
    const addProductForm = document.getElementById("addProductForm");
    const supplierSelect = document.getElementById("productSupplierSelect");

    // Load initial data
    loadProducts();
    loadSuppliersDropdown();

    // 2. Fetch and render products from backend
    async function loadProducts() {
        if (!tableBody) return;
        try {
            const res = await fetch("/api/products");
            const data = await res.json();
            if (res.ok && data.success) {
                renderTable(data.data);
            } else {
                tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">${data.message || 'Error loading products'}</td></tr>`;
            }
        } catch (e) {
            tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">Unable to connect to server.</td></tr>`;
        }
    }

    // Render table rows
    function renderTable(products) {
        if (!products || products.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No products found. Click "Add Product" to create one.</td></tr>`;
            return;
        }

        tableBody.innerHTML = products.map(p => {
            const isLowStock = p.quantityInStock < 10;
            const badge = isLowStock 
                ? `<span class="badge badge-low-stock">Low Stock (${p.quantityInStock})</span>`
                : `<span class="badge badge-stock-in">In Stock (${p.quantityInStock})</span>`;

            return `
                <tr data-id="${p.productId}">
                    <td><strong>#${p.productId}</strong></td>
                    <td class="fw-semibold">${p.productName}</td>
                    <td>${p.supplierName ? `<span class="text-primary">${p.supplierName}</span>` : '<span class="text-muted">None</span>'}</td>
                    <td>${p.quantityInStock}</td>
                    <td>${badge}</td>
                    <td>
                        <button class="btn btn-outline-danger btn-sm btn-delete" data-id="${p.productId}" title="Delete Product">
                            <i class="bi bi-trash"></i> Delete
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }

    // 3. Fetch suppliers to populate dropdown
    async function loadSuppliersDropdown() {
        if (!supplierSelect) return;
        try {
            const res = await fetch("/api/suppliers");
            const data = await res.json();
            if (res.ok && data.success) {
                supplierSelect.innerHTML = `<option value="">No Supplier Assigned</option>` +
                    data.data.map(s => `<option value="${s.supplierId}">${s.supplierName}</option>`).join("");
            }
        } catch (e) {
            console.error("Failed to load suppliers for dropdown", e);
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

    // 5. Delete product with confirmation
    if (tableBody) {
        tableBody.addEventListener("click", async function (event) {
            const deleteBtn = event.target.closest(".btn-delete");
            if (deleteBtn) {
                const id = deleteBtn.getAttribute("data-id");
                if (confirm(`Are you sure you want to delete Product #${id}?`)) {
                    try {
                        const res = await fetch(`/api/products/${id}`, { method: "DELETE" });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            loadProducts();
                        } else {
                            alert(data.message || "Failed to delete product");
                        }
                    } catch (e) {
                        alert("Error connecting to server to delete product.");
                    }
                }
            }
        });
    }

    // 6. Handle Add Product form submit
    if (addProductForm) {
        addProductForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const productName = addProductForm.product_name.value.trim();
            const supplierIdVal = addProductForm.supplier_id.value;
            const quantity = parseInt(addProductForm.quantity_in_stock.value, 10);

            if (!productName) {
                alert("Please enter a product name!");
                return;
            }
            if (isNaN(quantity) || quantity < 0) {
                alert("Quantity cannot be negative!");
                return;
            }

            try {
                const res = await fetch("/api/products", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        productName: productName,
                        supplierId: supplierIdVal ? parseInt(supplierIdVal, 10) : null,
                        quantityInStock: quantity
                    })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    addProductForm.reset();
                    // Close bootstrap modal
                    const modalEl = document.getElementById("addProductModal");
                    const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                    modalInstance.hide();
                    // Refresh table
                    loadProducts();
                } else {
                    alert(data.message || "Failed to create product");
                }
            } catch (e) {
                alert("Error connecting to server to save product.");
            }
        });
    }
});
