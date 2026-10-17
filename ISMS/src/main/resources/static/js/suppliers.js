// ==========================================================
// File: suppliers.js - Interactivity & API CRUD for Suppliers
// ==========================================================

document.addEventListener("DOMContentLoaded", function () {
    // 1. Guard page authentication
    if (window.Auth) {
        Auth.requireAuth();
    }

    const tableBody = document.getElementById("supplierTableBody");
    const searchInput = document.querySelector("input[placeholder='Search suppliers...']");
    const addSupplierForm = document.getElementById("addSupplierForm");

    // Load initial suppliers
    loadSuppliers();

    // 2. Fetch and render suppliers from backend
    async function loadSuppliers() {
        if (!tableBody) return;
        try {
            const res = await fetch("/api/suppliers");
            const data = await res.json();
            if (res.ok && data.success) {
                renderTable(data.data);
            } else {
                tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">${data.message || 'Error loading suppliers'}</td></tr>`;
            }
        } catch (e) {
            tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Unable to connect to server.</td></tr>`;
        }
    }

    // Render table rows
    function renderTable(suppliers) {
        if (!suppliers || suppliers.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">No suppliers found. Click "Add Supplier" to add one.</td></tr>`;
            return;
        }

        tableBody.innerHTML = suppliers.map(s => `
            <tr data-id="${s.supplierId}">
                <td><strong>#${s.supplierId}</strong></td>
                <td class="fw-semibold">${s.supplierName}</td>
                <td><a href="mailto:${s.email || ''}" class="text-decoration-none">${s.email || 'N/A'}</a></td>
                <td>${s.phone || 'N/A'}</td>
                <td>
                    <button class="btn btn-outline-danger btn-sm btn-delete" data-id="${s.supplierId}" title="Delete Supplier">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `).join("");
    }

    // 3. Live search filter on table rows
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

    // 4. Confirm and delete supplier
    if (tableBody) {
        tableBody.addEventListener("click", async function (event) {
            const deleteBtn = event.target.closest(".btn-delete");
            if (deleteBtn) {
                const id = deleteBtn.getAttribute("data-id");
                if (confirm(`Are you sure you want to delete Supplier #${id}?`)) {
                    try {
                        const res = await fetch(`/api/suppliers/${id}`, { method: "DELETE" });
                        const data = await res.json();
                        if (res.ok && data.success) {
                            loadSuppliers();
                        } else {
                            alert(data.message || "Failed to delete supplier");
                        }
                    } catch (e) {
                        alert("Error connecting to server to delete supplier.");
                    }
                }
            }
        });
    }

    // 5. Handle Add Supplier form submission
    if (addSupplierForm) {
        addSupplierForm.addEventListener("submit", async function (event) {
            event.preventDefault();
            const supplierName = addSupplierForm.supplier_name.value.trim();
            const email = addSupplierForm.email.value.trim();
            const phone = addSupplierForm.phone.value.trim();

            if (!supplierName) {
                alert("Please enter a supplier name!");
                return;
            }
            if (phone.length < 6) {
                alert("Please enter a valid phone number!");
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
                    const modalEl = document.getElementById("addSupplierModal");
                    const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                    modalInstance.hide();
                    loadSuppliers();
                } else {
                    alert(data.message || "Failed to add supplier");
                }
            } catch (e) {
                alert("Error connecting to server to save supplier.");
            }
        });
    }
});
