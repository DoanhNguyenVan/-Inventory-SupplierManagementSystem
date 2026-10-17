document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("productTableBody");
    const searchInput = document.querySelector("input[placeholder*='Search products']");
    const addProductForm = document.getElementById("addProductForm");
    const editProductForm = document.getElementById("editProductForm");
    const addSupplierSelect = document.getElementById("productSupplierSelect");
    const editSupplierSelect = document.getElementById("editProductSupplierSelect");

    const addModalEl = document.getElementById("addProductModal");
    const addModal = addModalEl ? (bootstrap.Modal.getInstance(addModalEl) || new bootstrap.Modal(addModalEl)) : null;

    const editModalEl = document.getElementById("editProductModal");
    const editModal = editModalEl ? (bootstrap.Modal.getInstance(editModalEl) || new bootstrap.Modal(editModalEl)) : null;

    const viewModalEl = document.getElementById("viewProductModal");
    const viewModal = viewModalEl ? (bootstrap.Modal.getInstance(viewModalEl) || new bootstrap.Modal(viewModalEl)) : null;

    const userRole = (window.CURRENT_USER_ROLE || "USER").toUpperCase();
    const isReadOnly = (userRole === "USER");

    let productsList = [];
    let suppliersList = [];

    loadProducts();
    if (!isReadOnly) {
        loadSuppliersDropdown();
    }

    async function loadProducts() {
        if (!tableBody) return;
        try {
            const res = await fetch("/api/products");
            const data = await res.json();
            if (res.ok && data.success) {
                productsList = data.data;
                renderTable(productsList);
            } else {
                tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">${data.message || 'Error loading products'}</td></tr>`;
            }
        } catch (e) {
            tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">Unable to connect to server.</td></tr>`;
        }
    }

    function renderTable(products) {
        if (!products || products.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No products found.</td></tr>`;
            return;
        }

        tableBody.innerHTML = products.map(p => {
            const qty = (p.quantityInStock != null) ? p.quantityInStock : 0;

            let rowClass = "";
            let badge = "";

            if (qty === 0) {
                rowClass = "row-out-of-stock";
                badge = `<span class="badge badge-out-of-stock"><i class="bi bi-x-circle me-1"></i>Out of Stock (0)</span>`;
            } else if (qty <= 5) {
                rowClass = "row-low-stock";
                badge = `<span class="badge badge-low-stock"><i class="bi bi-exclamation-triangle me-1"></i>Low Stock (${qty})</span>`;
            } else {
                rowClass = "";
                badge = `<span class="badge badge-stock-in"><i class="bi bi-check-circle me-1"></i>In Stock (${qty})</span>`;
            }

            let actionButtonsHtml = "";
            if (isReadOnly) {
                actionButtonsHtml = `
                    <button class="btn btn-outline-info btn-sm btn-view" 
                            data-id="${p.productId}" 
                            title="View Details">
                        <i class="bi bi-eye"></i> Details
                    </button>
                `;
            } else {
                actionButtonsHtml = `
                    <button class="btn btn-outline-info btn-sm btn-view me-1" 
                            data-id="${p.productId}" 
                            title="View Details">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-outline-primary btn-sm btn-edit me-1" 
                            data-id="${p.productId}" 
                            data-name="${escapeHtml(p.productName)}" 
                            data-supplier-id="${p.supplierId || ''}" 
                            data-quantity="${qty}"
                            title="Edit Product">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    <button class="btn btn-outline-danger btn-sm btn-delete" 
                            data-id="${p.productId}" 
                            data-name="${escapeHtml(p.productName)}" 
                            title="Delete Product">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                `;
            }

            return `
                <tr class="${rowClass}" data-id="${p.productId}">
                    <td><strong>#${p.productId}</strong></td>
                    <td class="fw-semibold">${escapeHtml(p.productName)}</td>
                    <td>${p.supplierName ? `<span class="text-primary fw-medium">${escapeHtml(p.supplierName)}</span>` : '<span class="text-muted">Unassigned</span>'}</td>
                    <td><strong class="fs-6">${qty}</strong></td>
                    <td>${badge}</td>
                    <td class="text-end">
                        ${actionButtonsHtml}
                    </td>
                </tr>
            `;
        }).join("");
    }

    async function loadSuppliersDropdown() {
        try {
            const res = await fetch("/api/suppliers");
            const data = await res.json();
            if (res.ok && data.success) {
                suppliersList = data.data;
                const options = `<option value="">-- Select Supplier --</option>` +
                    suppliersList.map(s => `<option value="${s.supplierId}">${escapeHtml(s.supplierName)}</option>`).join("");
                if (addSupplierSelect) addSupplierSelect.innerHTML = options;
                if (editSupplierSelect) editSupplierSelect.innerHTML = options;
            }
        } catch (e) {
            console.error("Error loading suppliers dropdown:", e);
        }
    }

    if (searchInput) {
        searchInput.addEventListener("input", function () {
            const keyword = searchInput.value.toLowerCase().trim();
            const filtered = productsList.filter(p =>
                (p.productName && p.productName.toLowerCase().includes(keyword)) ||
                (p.supplierName && p.supplierName.toLowerCase().includes(keyword)) ||
                String(p.productId).includes(keyword)
            );
            renderTable(filtered);
        });
    }

    if (tableBody) {
        tableBody.addEventListener("click", async function (event) {
            const viewBtn = event.target.closest(".btn-view");
            if (viewBtn) {
                const id = viewBtn.getAttribute("data-id");
                const p = productsList.find(item => String(item.productId) === String(id));
                if (p && viewModal) {
                    const qty = (p.quantityInStock != null) ? p.quantityInStock : 0;
                    document.getElementById("viewProductId").textContent = `#${p.productId}`;
                    document.getElementById("viewProductName").textContent = p.productName || "—";
                    document.getElementById("viewProductSupplier").textContent = p.supplierName || "No supplier assigned";
                    document.getElementById("viewProductQuantity").textContent = qty;

                    if (qty === 0) {
                        document.getElementById("viewProductStatus").innerHTML = `<span class="badge badge-out-of-stock"><i class="bi bi-x-circle me-1"></i>Out of Stock (0)</span>`;
                    } else if (qty <= 5) {
                        document.getElementById("viewProductStatus").innerHTML = `<span class="badge badge-low-stock"><i class="bi bi-exclamation-triangle me-1"></i>Low Stock (${qty})</span>`;
                    } else {
                        document.getElementById("viewProductStatus").innerHTML = `<span class="badge badge-stock-in"><i class="bi bi-check-circle me-1"></i>In Stock (${qty})</span>`;
                    }

                    viewModal.show();
                }
                return;
            }

            const editBtn = event.target.closest(".btn-edit");
            if (editBtn && !isReadOnly) {
                const id = editBtn.getAttribute("data-id");
                const name = editBtn.getAttribute("data-name");
                const supplierId = editBtn.getAttribute("data-supplier-id");
                const quantity = editBtn.getAttribute("data-quantity");

                document.getElementById("editProductId").value = id;
                document.getElementById("editProductName").value = name;
                document.getElementById("editProductQuantity").value = quantity;
                if (editSupplierSelect) {
                    editSupplierSelect.value = supplierId || "";
                }

                if (editModal) editModal.show();
                return;
            }

            const deleteBtn = event.target.closest(".btn-delete");
            if (deleteBtn && !isReadOnly) {
                const id = deleteBtn.getAttribute("data-id");
                const name = deleteBtn.getAttribute("data-name");
                if (confirm(`Are you sure you want to delete product "${name}" (#${id})?`)) {
                    try {
                        const res = await fetch(`/api/products/${id}`, { method: "DELETE" });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            loadProducts();
                        } else {
                            alert(data.message || "Failed to delete product");
                        }
                    } catch (e) {
                        alert("Error connecting to server while deleting product.");
                    }
                }
            }
        });
    }

    if (addProductForm && !isReadOnly) {
        addProductForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const productName = addProductForm.product_name.value.trim();
            const supplierIdVal = addProductForm.supplier_id.value;

            if (!productName) {
                alert("Please enter a product name!");
                return;
            }
            if (!supplierIdVal) {
                alert("A supplier must be selected for this product!");
                return;
            }

            try {
                const res = await fetch("/api/products", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        productName: productName,
                        supplierId: parseInt(supplierIdVal, 10),
                        quantityInStock: 0
                    })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    addProductForm.reset();
                    if (addModal) addModal.hide();
                    loadProducts();
                } else {
                    alert(data.message || "Failed to create product");
                }
            } catch (e) {
                alert("Error connecting to server while saving product.");
            }
        });
    }

    if (editProductForm && !isReadOnly) {
        editProductForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const id = document.getElementById("editProductId").value;
            const productName = document.getElementById("editProductName").value.trim();
            const supplierIdVal = document.getElementById("editProductSupplierSelect").value;

            if (!productName) {
                alert("Please enter a product name!");
                return;
            }
            if (!supplierIdVal) {
                alert("A supplier must be selected!");
                return;
            }

            try {
                const res = await fetch(`/api/products/${id}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        productName: productName,
                        supplierId: parseInt(supplierIdVal, 10)
                    })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    if (editModal) editModal.hide();
                    loadProducts();
                } else {
                    alert(data.message || "Failed to update product");
                }
            } catch (e) {
                alert("Error connecting to server while updating product.");
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
