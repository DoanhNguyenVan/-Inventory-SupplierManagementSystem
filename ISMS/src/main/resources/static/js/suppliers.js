document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("supplierTableBody");
    const searchInput = document.querySelector("input[placeholder='Search suppliers...']");
    const addSupplierForm = document.getElementById("addSupplierForm");
    const editSupplierForm = document.getElementById("editSupplierForm");

    const addModalEl = document.getElementById("addSupplierModal");
    const addModal = addModalEl ? (bootstrap.Modal.getInstance(addModalEl) || new bootstrap.Modal(addModalEl)) : null;

    const editModalEl = document.getElementById("editSupplierModal");
    const editModal = editModalEl ? (bootstrap.Modal.getInstance(editModalEl) || new bootstrap.Modal(editModalEl)) : null;

    const viewModalEl = document.getElementById("viewSupplierModal");
    const viewModal = viewModalEl ? (bootstrap.Modal.getInstance(viewModalEl) || new bootstrap.Modal(viewModalEl)) : null;

    const userRole = (window.CURRENT_USER_ROLE || "USER").toUpperCase();
    const isReadOnly = (userRole === "USER");

    let suppliersList = [];

    loadSuppliers();

    async function loadSuppliers() {
        if (!tableBody) return;
        try {
            const res = await fetch("/api/suppliers");
            const data = await res.json();
            if (res.ok && data.success) {
                suppliersList = data.data;
                renderTable(suppliersList);
            } else {
                tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">${data.message || 'Error loading suppliers'}</td></tr>`;
            }
        } catch (e) {
            tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Unable to connect to server.</td></tr>`;
        }
    }

    function renderTable(suppliers) {
        if (!suppliers || suppliers.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">No suppliers found.</td></tr>`;
            return;
        }

        tableBody.innerHTML = suppliers.map(s => {
            let actionButtonsHtml = "";
            if (isReadOnly) {
                actionButtonsHtml = `
                    <button class="btn btn-outline-info btn-sm btn-view" 
                            data-id="${s.supplierId}" 
                            title="View Details">
                        <i class="bi bi-eye"></i> Details
                    </button>
                `;
            } else {
                actionButtonsHtml = `
                    <button class="btn btn-outline-info btn-sm btn-view me-1" 
                            data-id="${s.supplierId}" 
                            title="View Details">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-outline-primary btn-sm btn-edit me-1" 
                            data-id="${s.supplierId}" 
                            data-name="${escapeHtml(s.supplierName)}" 
                            data-email="${escapeHtml(s.email || '')}" 
                            data-phone="${escapeHtml(s.phone || '')}" 
                            title="Edit Supplier">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    <button class="btn btn-outline-danger btn-sm btn-delete" 
                            data-id="${s.supplierId}" 
                            data-name="${escapeHtml(s.supplierName)}" 
                            title="Delete Supplier">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                `;
            }

            return `
                <tr data-id="${s.supplierId}">
                    <td><strong>#${s.supplierId}</strong></td>
                    <td class="fw-semibold">${escapeHtml(s.supplierName)}</td>
                    <td><a href="mailto:${escapeHtml(s.email || '')}" class="text-decoration-none">${escapeHtml(s.email || 'N/A')}</a></td>
                    <td>${escapeHtml(s.phone || 'N/A')}</td>
                    <td class="text-end">
                        ${actionButtonsHtml}
                    </td>
                </tr>
            `;
        }).join("");
    }

    if (searchInput) {
        searchInput.addEventListener("input", function () {
            const keyword = searchInput.value.toLowerCase().trim();
            const filtered = suppliersList.filter(s =>
                (s.supplierName && s.supplierName.toLowerCase().includes(keyword)) ||
                (s.email && s.email.toLowerCase().includes(keyword)) ||
                (s.phone && s.phone.toLowerCase().includes(keyword)) ||
                String(s.supplierId).includes(keyword)
            );
            renderTable(filtered);
        });
    }

    if (tableBody) {
        tableBody.addEventListener("click", async function (event) {
            const viewBtn = event.target.closest(".btn-view");
            if (viewBtn) {
                const id = viewBtn.getAttribute("data-id");
                const s = suppliersList.find(item => String(item.supplierId) === String(id));
                if (s && viewModal) {
                    document.getElementById("viewSupplierId").textContent = `#${s.supplierId}`;
                    document.getElementById("viewSupplierName").textContent = s.supplierName || "—";
                    document.getElementById("viewSupplierEmail").textContent = s.email || "N/A";
                    document.getElementById("viewSupplierPhone").textContent = s.phone || "N/A";

                    const productsContainer = document.getElementById("viewSupplierProductsContainer");
                    if (productsContainer) {
                        productsContainer.innerHTML = `<small class="text-muted"><i class="bi bi-hourglass-split"></i> Loading linked products...</small>`;
                        fetch(`/api/suppliers/${id}/products`)
                            .then(r => r.json())
                            .then(res => {
                                if (res.success && res.data && res.data.length > 0) {
                                    productsContainer.innerHTML = `
                                        <div class="table-responsive">
                                            <table class="table table-sm table-bordered mt-2 mb-0">
                                                <thead class="table-light">
                                                    <tr>
                                                        <th>ID</th>
                                                        <th>Product Name</th>
                                                        <th>Stock</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    ${res.data.map(p => `
                                                        <tr>
                                                            <td>#${p.productId}</td>
                                                            <td class="fw-semibold">${escapeHtml(p.productName)}</td>
                                                            <td><span class="badge ${p.quantityInStock <= 5 ? 'bg-warning text-dark' : 'bg-success'}">${p.quantityInStock}</span></td>
                                                        </tr>
                                                    `).join("")}
                                                </tbody>
                                            </table>
                                        </div>
                                    `;
                                } else {
                                    productsContainer.innerHTML = `<span class="text-muted small fst-italic">This supplier has no linked products.</span>`;
                                }
                            })
                            .catch(() => {
                                productsContainer.innerHTML = `<span class="text-danger small">Failed to load linked products.</span>`;
                            });
                    }

                    viewModal.show();
                }
                return;
            }

            const editBtn = event.target.closest(".btn-edit");
            if (editBtn && !isReadOnly) {
                const id = editBtn.getAttribute("data-id");
                const name = editBtn.getAttribute("data-name");
                const email = editBtn.getAttribute("data-email");
                const phone = editBtn.getAttribute("data-phone");

                document.getElementById("editSupplierId").value = id;
                document.getElementById("editSupplierName").value = name;
                document.getElementById("editSupplierEmail").value = email;
                document.getElementById("editSupplierPhone").value = phone;

                if (editModal) editModal.show();
                return;
            }

            const deleteBtn = event.target.closest(".btn-delete");
            if (deleteBtn && !isReadOnly) {
                const id = deleteBtn.getAttribute("data-id");
                const name = deleteBtn.getAttribute("data-name");
                if (confirm(`Are you sure you want to delete supplier "${name}" (#${id})?`)) {
                    try {
                        const res = await fetch(`/api/suppliers/${id}`, { method: "DELETE" });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            loadSuppliers();
                        } else {
                            alert(data.message || "Cannot delete this supplier due to existing product links!");
                        }
                    } catch (e) {
                        alert("Error connecting to server while deleting supplier.");
                    }
                }
            }
        });
    }

    if (addSupplierForm && !isReadOnly) {
        addSupplierForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const supplierName = addSupplierForm.supplier_name.value.trim();
            const email = addSupplierForm.email.value.trim();
            const phone = addSupplierForm.phone.value.trim();

            if (!supplierName) {
                alert("Please enter a supplier name!");
                return;
            }

            try {
                const res = await fetch("/api/suppliers", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        supplierName: supplierName,
                        email: email,
                        phone: phone
                    })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    addSupplierForm.reset();
                    if (addModal) addModal.hide();
                    loadSuppliers();
                } else {
                    alert(data.message || "Failed to add supplier");
                }
            } catch (e) {
                alert("Error connecting to server while saving supplier.");
            }
        });
    }

    if (editSupplierForm && !isReadOnly) {
        editSupplierForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const id = document.getElementById("editSupplierId").value;
            const supplierName = document.getElementById("editSupplierName").value.trim();
            const email = document.getElementById("editSupplierEmail").value.trim();
            const phone = document.getElementById("editSupplierPhone").value.trim();

            if (!supplierName) {
                alert("Please enter a supplier name!");
                return;
            }

            try {
                const res = await fetch(`/api/suppliers/${id}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        supplierName: supplierName,
                        email: email,
                        phone: phone
                    })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    if (editModal) editModal.hide();
                    loadSuppliers();
                } else {
                    alert(data.message || "Failed to update supplier");
                }
            } catch (e) {
                alert("Error connecting to server while updating supplier.");
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
