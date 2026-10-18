document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("userTableBody");
    const searchInput = document.getElementById("userSearchInput");
    const btnRefresh = document.getElementById("btnRefreshUsers");
    const changeRoleForm = document.getElementById("changeRoleForm");
    const editUserIdInput = document.getElementById("editUserId");
    const editUsernameInput = document.getElementById("editUsername");
    const roleModalEl = document.getElementById("changeRoleModal");
    const roleModal = roleModalEl ? new bootstrap.Modal(roleModalEl) : null;

    let usersList = [];

    loadUsers();

    if (btnRefresh) {
        btnRefresh.addEventListener("click", () => loadUsers());
    }

    async function loadUsers() {
        if (!tableBody) return;
        try {
            const res = await fetch("/api/users");
            const result = await res.json();
            if (res.ok && result.success) {
                usersList = result.data;
                renderTable(usersList);
            } else {
                tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">${result.message || "Failed to load users"}</td></tr>`;
            }
        } catch (e) {
            tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">Cannot connect to server.</td></tr>`;
        }
    }

    function renderTable(users) {
        if (!users || users.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No users found.</td></tr>`;
            return;
        }

        tableBody.innerHTML = users.map(u => {
            const isCurrent = (window.CURRENT_USER_ID && Number(window.CURRENT_USER_ID) === Number(u.userId));
            const roleBadge = getRoleBadge(u.role);
            const initial = (u.username || "U").charAt(0).toUpperCase();
            const dateStr = u.createdAt ? new Date(u.createdAt).toLocaleDateString() : "N/A";

            return `
                <tr data-id="${u.userId}">
                    <td><strong>#${u.userId}</strong></td>
                    <td>
                        <div class="d-flex align-items-center">
                            <div class="rounded-circle bg-dark text-white d-flex align-items-center justify-content-center me-2 fw-bold" 
                                 style="width: 34px; height: 34px; font-size: 14px;">
                                ${initial}
                            </div>
                            <div>
                                <span class="fw-semibold">${escapeHtml(u.username)}</span>
                                ${isCurrent ? '<span class="badge bg-info-subtle text-info border border-info-subtle ms-1" style="font-size: 0.65rem;">You</span>' : ''}
                            </div>
                        </div>
                    </td>
                    <td>${escapeHtml(u.fullName || "—")}</td>
                    <td><a href="mailto:${escapeHtml(u.email || '')}" class="text-decoration-none">${escapeHtml(u.email || "—")}</a></td>
                    <td>${roleBadge}</td>
                    <td class="text-muted small">${dateStr}</td>
                    <td class="text-end">
                        <button class="btn btn-outline-primary btn-sm btn-edit-role me-1" 
                                data-id="${u.userId}" 
                                data-username="${escapeHtml(u.username)}" 
                                data-role="${u.role}"
                                title="Change Role">
                            <i class="bi bi-shield-check"></i> Assign Role
                        </button>
                        ${!isCurrent ? `
                        <button class="btn btn-outline-danger btn-sm btn-delete-user" 
                                data-id="${u.userId}" 
                                data-username="${escapeHtml(u.username)}"
                                title="Delete User">
                            <i class="bi bi-trash"></i>
                        </button>
                        ` : ''}
                    </td>
                </tr>
            `;
        }).join("");
    }

    function getRoleBadge(role) {
        switch ((role || "").toUpperCase()) {
            case "ADMIN":
                return `<span class="badge bg-danger"><i class="bi bi-shield-lock-fill me-1"></i>ADMIN</span>`;
            case "STAFF":
                return `<span class="badge bg-primary"><i class="bi bi-boxes me-1"></i>STAFF</span>`;
            case "USER":
                return `<span class="badge bg-secondary"><i class="bi bi-person me-1"></i>USER</span>`;
            default:
                return `<span class="badge bg-light text-dark border">${escapeHtml(role)}</span>`;
        }
    }

    if (searchInput) {
        searchInput.addEventListener("input", function () {
            const keyword = searchInput.value.toLowerCase().trim();
            const filtered = usersList.filter(u =>
                (u.username && u.username.toLowerCase().includes(keyword)) ||
                (u.fullName && u.fullName.toLowerCase().includes(keyword)) ||
                (u.email && u.email.toLowerCase().includes(keyword)) ||
                (u.role && u.role.toLowerCase().includes(keyword))
            );
            renderTable(filtered);
        });
    }

    if (tableBody) {
        tableBody.addEventListener("click", async function (event) {
            const deleteBtn = event.target.closest(".btn-delete-user");
            if (deleteBtn) {
                const id = deleteBtn.getAttribute("data-id");
                const username = deleteBtn.getAttribute("data-username");
                if (confirm(`Are you sure you want to delete user "${username}" (#${id})?`)) {
                    try {
                        const res = await fetch(`/api/users/${id}`, { method: "DELETE" });
                        const result = await res.json();
                        if (res.ok && result.success) {
                            loadUsers();
                        } else {
                            alert(result.message || "Failed to delete user");
                        }
                    } catch (err) {
                        alert("Error connecting to server while deleting user.");
                    }
                }
                return;
            }

            const editBtn = event.target.closest(".btn-edit-role");
            if (editBtn) {
                const id = editBtn.getAttribute("data-id");
                const username = editBtn.getAttribute("data-username");
                const currentRole = (editBtn.getAttribute("data-role") || "STAFF").toUpperCase();

                if (editUserIdInput) editUserIdInput.value = id;
                if (editUsernameInput) editUsernameInput.value = username;

                const radio = document.querySelector(`input[name="roleOption"][value="${currentRole}"]`);
                if (radio) radio.checked = true;

                if (roleModal) roleModal.show();
            }
        });
    }

    if (changeRoleForm) {
        changeRoleForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            const userId = editUserIdInput.value;
            const selectedRadio = document.querySelector('input[name="roleOption"]:checked');
            if (!selectedRadio) {
                alert("Please select a role!");
                return;
            }
            const newRole = selectedRadio.value;

            try {
                const res = await fetch(`/api/users/${userId}/role`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ role: newRole })
                });
                const result = await res.json();
                if (res.ok && result.success) {
                    if (roleModal) roleModal.hide();
                    loadUsers();
                } else {
                    alert(result.message || "Failed to update role");
                }
            } catch (err) {
                alert("Error connecting to server while updating role.");
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
