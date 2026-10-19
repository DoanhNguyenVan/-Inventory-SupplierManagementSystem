document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("productTableBody");
    const searchInput = document.querySelector("input[placeholder*='Search products']");
    const addProductForm = document.getElementById("addProductForm");
    const editProductForm = document.getElementById("editProductForm");
    const restoreProductForm = document.getElementById("restoreProductForm");
    const addSupplierSelect = document.getElementById("productSupplierSelect");
    const editSupplierSelect = document.getElementById("editProductSupplierSelect");
    const editProductStatusSelect = document.getElementById("editProductStatus");

    const tabActiveBtn = document.getElementById("tabActiveProducts");
    const tabTrashBtn = document.getElementById("tabTrashProducts");
    const activeCountBadge = document.getElementById("activeCountBadge");
    const trashCountBadge = document.getElementById("trashCountBadge");
    const activeStockAlerts = document.getElementById("activeStockAlerts");
    const thQuantity = document.getElementById("thQuantity");

    const addModalEl = document.getElementById("addProductModal");
    const addModal = addModalEl ? (bootstrap.Modal.getInstance(addModalEl) || new bootstrap.Modal(addModalEl)) : null;

    const editModalEl = document.getElementById("editProductModal");
    const editModal = editModalEl ? (bootstrap.Modal.getInstance(editModalEl) || new bootstrap.Modal(editModalEl)) : null;

    const viewModalEl = document.getElementById("viewProductModal");
    const viewModal = viewModalEl ? (bootstrap.Modal.getInstance(viewModalEl) || new bootstrap.Modal(viewModalEl)) : null;

    const restoreModalEl = document.getElementById("restoreProductModal");
    const restoreModal = restoreModalEl ? (bootstrap.Modal.getInstance(restoreModalEl) || new bootstrap.Modal(restoreModalEl)) : null;

    const userRole = (window.CURRENT_USER_ROLE || "USER").toUpperCase();
    const isReadOnly = (userRole === "USER");

    let currentTab = "active"; // "active" or "trash"
    let activeProductsList = [];
    let trashProductsList = [];
    let suppliersList = [];

    // Tab switching event listeners
    if (tabActiveBtn) {
        tabActiveBtn.addEventListener("click", function () {
            switchTab("active");
        });
    }

    if (tabTrashBtn) {
        tabTrashBtn.addEventListener("click", function () {
            switchTab("trash");
        });
    }

    function switchTab(tab) {
        currentTab = tab;
        if (searchInput) searchInput.value = "";

        if (tab === "active") {
            tabActiveBtn.className = "nav-link active fw-semibold shadow-sm px-3 py-2";
            if (tabTrashBtn) tabTrashBtn.className = "nav-link text-secondary fw-semibold bg-white border shadow-sm px-3 py-2";
            if (addProductBtn) addProductBtn.style.display = "";
            if (activeStockAlerts) activeStockAlerts.classList.remove("d-none");
            if (thQuantity) thQuantity.style.display = "";
            if (searchInput) searchInput.placeholder = "Search products by name...";
        } else {
            tabActiveBtn.className = "nav-link text-secondary fw-semibold bg-white border shadow-sm px-3 py-2";
            if (tabTrashBtn) tabTrashBtn.className = "nav-link active bg-danger text-white fw-semibold shadow-sm px-3 py-2";
            if (addProductBtn) addProductBtn.style.display = "none";
            if (activeStockAlerts) activeStockAlerts.classList.add("d-none");
            if (thQuantity) thQuantity.style.display = "none";
            if (searchInput) searchInput.placeholder = "Search in recycle bin...";
        }
        loadProducts();
    }

    loadProducts();
    updateCounts();
    if (!isReadOnly) {
        loadSuppliersDropdown();
    }

    async function loadProducts() {
        if (!tableBody) return;
        const colspan = (currentTab === "trash") ? 5 : 6;
        tableBody.innerHTML = `<tr><td colspan="${colspan}" class="text-center text-muted py-4"><div class="spinner-border spinner-border-sm me-2 text-primary" role="status"></div>Loading ${currentTab === "trash" ? "recycle bin" : "products"}...</td></tr>`;

        const url = (currentTab === "trash") ? "/api/products/trash" : "/api/products";
        try {
            const res = await fetch(url);
            const data = await res.json();
            if (res.ok && data.success) {
                if (currentTab === "trash") {
                    trashProductsList = data.data || [];
                    renderTable(trashProductsList);
                } else {
                    activeProductsList = data.data || [];
                    renderTable(activeProductsList);
                }
            } else {
                tableBody.innerHTML = `<tr><td colspan="${colspan}" class="text-center text-danger py-4">${data.message || 'Error loading products'}</td></tr>`;
            }
        } catch (e) {
            tableBody.innerHTML = `<tr><td colspan="${colspan}" class="text-center text-danger py-4">Unable to connect to server.</td></tr>`;
        }
        updateCounts();
    }

    async function updateCounts() {
        try {
            const res = await fetch("/api/products/counts");
            const data = await res.json();
            if (res.ok && data.success && data.data) {
                if (activeCountBadge) activeCountBadge.textContent = data.data.active != null ? data.data.active : 0;
                if (trashCountBadge) trashCountBadge.textContent = data.data.trash != null ? data.data.trash : 0;
            }
        } catch (e) {
            console.error("Error updating counts:", e);
        }
    }

    function renderTable(products) {
        const colspan = (currentTab === "trash") ? 5 : 6;
        if (!products || products.length === 0) {
            if (currentTab === "trash") {
                tableBody.innerHTML = `<tr><td colspan="${colspan}" class="text-center text-muted py-5"><i class="bi bi-trash3 fs-2 d-block text-secondary mb-2"></i>Recycle bin is empty. No discontinued products.</td></tr>`;
            } else {
                tableBody.innerHTML = `<tr><td colspan="${colspan}" class="text-center text-muted py-5"><i class="bi bi-box fs-2 d-block text-secondary mb-2"></i>No products yet. Click "+ Add Product" to add one.</td></tr>`;
            }
            return;
        }

        tableBody.innerHTML = products.map(p => {
            const qty = (p.quantityInStock != null) ? p.quantityInStock : 0;
            const status = (p.status || "ACTIVE").toUpperCase();

            let rowClass = "";
            let badge = "";

            if (currentTab === "trash") {
                rowClass = "table-light";
                badge = `<span class="badge bg-danger-subtle text-danger border border-danger-subtle"><i class="bi bi-slash-circle me-1"></i>Discontinued</span>`;
            } else {
                if (status === "SUSPENDED") {
                    rowClass = "table-warning bg-warning bg-opacity-10";
                    badge = `<span class="badge bg-warning text-dark"><i class="bi bi-pause-circle me-1"></i>Suspended</span>`;
                } else if (qty === 0) {
                    rowClass = "row-out-of-stock";
                    badge = `<span class="badge badge-out-of-stock"><i class="bi bi-x-circle me-1"></i>Out of Stock (0)</span>`;
                } else if (qty <= 5) {
                    rowClass = "row-low-stock";
                    badge = `<span class="badge badge-low-stock"><i class="bi bi-exclamation-triangle me-1"></i>Low Stock (${qty})</span>`;
                } else {
                    rowClass = "";
                    badge = `<span class="badge badge-stock-in"><i class="bi bi-check-circle me-1"></i>In Stock (${qty})</span>`;
                }
            }

            let actionButtonsHtml = "";
            if (currentTab === "trash") {
                if (isReadOnly) {
                    actionButtonsHtml = `<span class="text-muted small">Discontinued</span>`;
                } else {
                    actionButtonsHtml = `
                        <button class="btn btn-success btn-sm btn-open-restore shadow-sm" 
                                data-id="${p.productId}" 
                                data-name="${escapeHtml(p.productName)}"
                                data-supplier="${escapeHtml(p.supplierName || '—')}"
                                data-quantity="${qty}"
                                title="Restore product & set stock quantity">
                            <i class="bi bi-arrow-counterclockwise"></i> Restore
                        </button>
                    `;
                }
            } else {
                if (isReadOnly) {
                    actionButtonsHtml = `
                        <button class="btn btn-outline-info btn-sm btn-view" 
                                data-id="${p.productId}" 
                                title="View Details">
                            <i class="bi bi-eye"></i> Details
                        </button>
                    `;
                } else {
                    const toggleStatusBtn = (status === "SUSPENDED")
                        ? `<button class="btn btn-outline-success btn-sm btn-toggle-status me-1" 
                                   data-id="${p.productId}" 
                                   data-status="ACTIVE" 
                                   data-name="${escapeHtml(p.productName)}"
                                   title="Reactivate this product for sale">
                               <i class="bi bi-play-circle"></i> Reactivate
                           </button>`
                        : `<button class="btn btn-outline-warning btn-sm btn-toggle-status me-1" 
                                   data-id="${p.productId}" 
                                   data-status="SUSPENDED" 
                                   data-name="${escapeHtml(p.productName)}"
                                   title="Suspend due to issue or defect">
                               <i class="bi bi-pause-circle"></i> Suspend
                           </button>`;

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
                                data-status="${status}"
                                title="Edit Product">
                            <i class="bi bi-pencil"></i> Edit
                        </button>
                        ${toggleStatusBtn}
                        <button class="btn btn-outline-danger btn-sm btn-delete" 
                                data-id="${p.productId}" 
                                data-name="${escapeHtml(p.productName)}" 
                                data-quantity="${qty}"
                                title="Discontinue product (move to Recycle Bin)">
                            <i class="bi bi-trash"></i> Discontinue
                        </button>
                    `;
                }
            }

            if (currentTab === "trash") {
                return `
                    <tr class="${rowClass}" data-id="${p.productId}">
                        <td><strong>#${p.productId}</strong></td>
                        <td class="fw-semibold">${escapeHtml(p.productName)}</td>
                        <td>${p.supplierName ? `<span class="text-primary fw-medium">${escapeHtml(p.supplierName)}</span>` : '<span class="text-muted">Unassigned</span>'}</td>
                        <td>${badge}</td>
                        <td class="text-end">
                            ${actionButtonsHtml}
                        </td>
                    </tr>
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
            const sourceList = (currentTab === "trash") ? trashProductsList : activeProductsList;
            const filtered = sourceList.filter(p =>
                (p.productName && p.productName.toLowerCase().includes(keyword)) ||
                (p.supplierName && p.supplierName.toLowerCase().includes(keyword)) ||
                String(p.productId).includes(keyword)
            );
            renderTable(filtered);
        });
    }

    if (tableBody) {
        tableBody.addEventListener("click", async function (event) {
            // View Details
            const viewBtn = event.target.closest(".btn-view");
            if (viewBtn) {
                const id = viewBtn.getAttribute("data-id");
                const sourceList = (currentTab === "trash") ? trashProductsList : activeProductsList;
                const p = sourceList.find(item => String(item.productId) === String(id));
                if (p && viewModal) {
                    const qty = (p.quantityInStock != null) ? p.quantityInStock : 0;
                    const status = (p.status || "ACTIVE").toUpperCase();

                    document.getElementById("viewProductId").textContent = `#${p.productId}`;
                    document.getElementById("viewProductName").textContent = p.productName || "—";
                    document.getElementById("viewProductSupplier").textContent = p.supplierName || "No supplier assigned";
                    document.getElementById("viewProductQuantity").textContent = qty;

                    if (Boolean(p.isDeleted)) {
                        document.getElementById("viewProductStatus").innerHTML = `<span class="badge bg-danger"><i class="bi bi-slash-circle me-1"></i>Discontinued (Recycle Bin)</span>`;
                    } else if (status === "SUSPENDED") {
                        document.getElementById("viewProductStatus").innerHTML = `<span class="badge bg-warning text-dark"><i class="bi bi-pause-circle me-1"></i>Suspended (Issue / Defect)</span>`;
                    } else if (qty === 0) {
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

            // Edit Product
            const editBtn = event.target.closest(".btn-edit");
            if (editBtn && !isReadOnly) {
                const id = editBtn.getAttribute("data-id");
                const name = editBtn.getAttribute("data-name");
                const supplierId = editBtn.getAttribute("data-supplier-id");
                const quantity = editBtn.getAttribute("data-quantity");
                const status = editBtn.getAttribute("data-status") || "ACTIVE";

                document.getElementById("editProductId").value = id;
                document.getElementById("editProductName").value = name;
                document.getElementById("editProductQuantity").value = quantity;
                if (editProductStatusSelect) {
                    editProductStatusSelect.value = status;
                }
                if (editSupplierSelect) {
                    editSupplierSelect.value = supplierId || "";
                }

                if (editModal) editModal.show();
                return;
            }

            // Quick Toggle Status (ACTIVE <-> SUSPENDED)
            const toggleStatusBtn = event.target.closest(".btn-toggle-status");
            if (toggleStatusBtn && !isReadOnly) {
                const id = toggleStatusBtn.getAttribute("data-id");
                const targetStatus = toggleStatusBtn.getAttribute("data-status");
                const name = toggleStatusBtn.getAttribute("data-name");

                const confirmText = (targetStatus === "SUSPENDED")
                    ? `Are you sure you want to SUSPEND product "${name}" (#${id})?\n(Use this when the product is defective or has an issue that needs to be resolved.)`
                    : `Are you sure you want to REACTIVATE product "${name}" (#${id}) for sale?`;

                if (confirm(confirmText)) {
                    try {
                        const res = await fetch(`/api/products/${id}/status`, {
                            method: "PUT",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ status: targetStatus })
                        });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            await loadProducts();
                        } else {
                            alert(data.message || "Failed to update product status");
                        }
                    } catch (e) {
                        alert("Connection error while updating product status.");
                    }
                }
                return;
            }

            // Soft Delete Product (Ngừng kinh doanh -> Thùng rác)
            const deleteBtn = event.target.closest(".btn-delete");
            if (deleteBtn && !isReadOnly) {
                const id = deleteBtn.getAttribute("data-id");
                const name = deleteBtn.getAttribute("data-name");
                const qty = parseInt(deleteBtn.getAttribute("data-quantity") || "0", 10);

                let confirmMsg = `Bạn có chắc chắn muốn NGỪNG KINH DOANH sản phẩm "${name}" (#${id}) và đưa vào Thùng rác?`;
                if (qty > 0) {
                    confirmMsg = `⚠️ Cảnh báo tồn kho: Sản phẩm "${name}" (#${id}) hiện vẫn còn tồn kho ${qty} cái!\n\nBạn có chắc chắn muốn ngừng kinh doanh (đưa vào Thùng rác) sản phẩm này không? Khi muốn bán lại bạn có thể vào Thùng rác để Khôi phục.`;
                }

                if (confirm(confirmMsg)) {
                    try {
                        const res = await fetch(`/api/products/${id}`, { method: "DELETE" });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            await loadProducts();
                            await updateCounts();
                        } else {
                            alert(data.message || "Failed to delete product");
                        }
                    } catch (e) {
                        alert("Error connecting to server while deleting product.");
                    }
                }
                return;
            }

            // Open Restore Modal (Edit stock quantity on restore)
            const openRestoreBtn = event.target.closest(".btn-open-restore");
            if (openRestoreBtn && !isReadOnly) {
                const id = openRestoreBtn.getAttribute("data-id");
                const name = openRestoreBtn.getAttribute("data-name");
                const supplier = openRestoreBtn.getAttribute("data-supplier");
                const qty = openRestoreBtn.getAttribute("data-quantity") || "0";

                document.getElementById("restoreProductId").value = id;
                document.getElementById("restoreProductName").value = name;
                document.getElementById("restoreProductSupplier").value = supplier;
                document.getElementById("restoreProductQuantity").value = qty;

                if (restoreModal) {
                    restoreModal.show();
                }
                return;
            }

            // Permanent Delete from Database
            const permanentDeleteBtn = event.target.closest(".btn-permanent-delete");
            if (permanentDeleteBtn && !isReadOnly) {
                const id = permanentDeleteBtn.getAttribute("data-id");
                const name = permanentDeleteBtn.getAttribute("data-name");

                if (confirm(`⚠️ CẢNH BÁO: Bạn có chắc chắn muốn xóa VĨNH VIỄN sản phẩm "${name}" (#${id}) khỏi cơ sở dữ liệu?\n\nHành động này không thể hoàn tác!`)) {
                    try {
                        const res = await fetch(`/api/products/${id}/permanent`, { method: "DELETE" });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            await loadProducts();
                            await updateCounts();
                        } else {
                            alert(data.message || "Failed to permanently delete product");
                        }
                    } catch (e) {
                        alert("Error connecting to server while deleting product.");
                    }
                }
                return;
            }
        });
    }

    // Submit Restore Form (Restore product and update stock quantity)
    if (restoreProductForm && !isReadOnly) {
        restoreProductForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const id = document.getElementById("restoreProductId").value;
            const qtyVal = document.getElementById("restoreProductQuantity").value.trim();

            const quantity = parseInt(qtyVal, 10);
            if (isNaN(quantity) || quantity < 0) {
                alert("Please enter a valid stock quantity (>= 0)!");
                return;
            }

            try {
                const res = await fetch(`/api/products/${id}/restore`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ quantityInStock: quantity })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    if (restoreModal) restoreModal.hide();
                    await loadProducts();
                    await updateCounts();
                    alert("Product restored successfully! The product has been relisted for sale.");
                } else {
                    alert(data.message || "Failed to restore product");
                }
            } catch (e) {
                alert("Connection error while restoring product.");
            }
        });
    }

    // Submit Add Form
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
                        quantityInStock: 0,
                        status: "ACTIVE"
                    })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    addProductForm.reset();
                    if (addModal) addModal.hide();
                    await loadProducts();
                    await updateCounts();
                } else {
                    alert(data.message || "Failed to create product");
                }
            } catch (e) {
                alert("Error connecting to server while saving product.");
            }
        });
    }

    // Submit Edit Form
    if (editProductForm && !isReadOnly) {
        editProductForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const id = document.getElementById("editProductId").value;
            const productName = document.getElementById("editProductName").value.trim();
            const supplierIdVal = document.getElementById("editProductSupplierSelect").value;
            const statusVal = editProductStatusSelect ? editProductStatusSelect.value : "ACTIVE";

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
                        supplierId: parseInt(supplierIdVal, 10),
                        status: statusVal
                    })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    if (editModal) editModal.hide();
                    await loadProducts();
                    await updateCounts();
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
