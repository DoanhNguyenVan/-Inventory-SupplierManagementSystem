document.addEventListener("DOMContentLoaded", function () {
    const tableBody = document.getElementById("userTableBody");
    const searchInput = document.getElementById("userSearchInput");
    const btnRefresh = document.getElementById("btnRefreshUsers");
    const tabActiveUsers = document.getElementById("tabActiveUsers");
    const tabLockedUsers = document.getElementById("tabLockedUsers");
    const activeCountBadge = document.getElementById("activeUserCountBadge");
    const lockedCountBadge = document.getElementById("lockedUserCountBadge");

    const changeRoleForm = document.getElementById("changeRoleForm");
    const editUserIdInput = document.getElementById("editUserId");
    const editUsernameInput = document.getElementById("editUsername");
    const roleModalEl = document.getElementById("changeRoleModal");
    const roleModal = roleModalEl ? new bootstrap.Modal(roleModalEl) : null;

    let currentTab = "active"; // "active" | "locked"
    let usersList = [];

    // Initialize
    initTabs();
    loadUsers();
    updateLockedCount();

    if (btnRefresh) {
        btnRefresh.addEventListener("click", () => {
            loadUsers();
            updateLockedCount();
        });
    }

    function initTabs() {
        if (tabActiveUsers) {
            tabActiveUsers.addEventListener("click", () => {
                if (currentTab === "active") return;
                switchTab("active");
            });
        }
        if (tabLockedUsers) {
            tabLockedUsers.addEventListener("click", () => {
                if (currentTab === "locked") return;
                switchTab("locked");
            });
        }
    }

    function switchTab(tab) {
        currentTab = tab;
        if (tab === "active") {
            tabActiveUsers.className = "nav-link active fw-semibold shadow-sm px-3 py-2";
            tabLockedUsers.className = "nav-link text-secondary fw-semibold bg-white border shadow-sm px-3 py-2";
        } else {
            tabLockedUsers.className = "nav-link active bg-danger text-white fw-semibold shadow-sm px-3 py-2";
            tabActiveUsers.className = "nav-link text-secondary fw-semibold bg-white border shadow-sm px-3 py-2";
        }

        if (searchInput) searchInput.value = "";
        loadUsers();
        updateLockedCount();
    }

    async function updateLockedCount() {
        try {
            const res = await fetch("/api/users/locked/count");
            const result = await res.json();
            if (res.ok && result.success && lockedCountBadge) {
                lockedCountBadge.textContent = result.data;
            }
        } catch (e) {
            // Silently ignore counter failures
        }
    }

    async function loadUsers() {
        if (!tableBody) return;
        tableBody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted"><div class="spinner-border spinner-border-sm me-2" role="status"></div>Loading users...</td></tr>`;

        const endpoint = currentTab === "locked" ? "/api/users/locked" : "/api/users";
        try {
            const res = await fetch(endpoint);
            const result = await res.json();
            if (res.ok && result.success) {
                usersList = result.data || [];
                if (currentTab === "active" && activeCountBadge) {
                    activeCountBadge.textContent = usersList.length;
                } else if (currentTab === "locked" && lockedCountBadge) {
                    lockedCountBadge.textContent = usersList.length;
                }
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
            const emptyMsg = currentTab === "locked" ? "No locked accounts found." : "No active users found.";
            tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">${emptyMsg}</td></tr>`;
            return;
        }

        tableBody.innerHTML = users.map(u => {
            const isCurrent = (window.CURRENT_USER_ID && Number(window.CURRENT_USER_ID) === Number(u.userId));
            const roleBadge = getRoleBadge(u.role);
            const initial = (u.username || "U").charAt(0).toUpperCase();
            const dateStr = u.createdAt ? new Date(u.createdAt).toLocaleDateString() : "N/A";

            let actionHtml = "";
            let statusBadge = "";

            if (currentTab === "active") {
                statusBadge = `<span class="badge bg-success-subtle text-success border border-success-subtle ms-1" style="font-size: 0.65rem;">Active</span>`;
                actionHtml = `
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
                            title="Lock / Deactivate Account">
                        <i class="bi bi-person-slash"></i> Lock Account
                    </button>
                    ` : ''}
                `;
            } else {
                // Locked account
                statusBadge = `<span class="badge bg-danger-subtle text-danger border border-danger-subtle ms-1" style="font-size: 0.65rem;">Locked</span>`;
                actionHtml = `
                    <button class="btn btn-outline-success btn-sm btn-restore-user" 
                            data-id="${u.userId}" 
                            data-username="${escapeHtml(u.username)}"
                            title="Unlock / Restore Account">
                        <i class="bi bi-arrow-counterclockwise me-1"></i> Unlock Account
                    </button>
                `;
            }

            return `
                <tr data-id="${u.userId}" class="${currentTab === 'locked' ? 'table-light' : ''}">
                    <td><strong>#${u.userId}</strong></td>
                    <td>
                        <div class="d-flex align-items-center">
                            <div class="rounded-circle ${currentTab === 'locked' ? 'bg-secondary' : 'bg-dark'} text-white d-flex align-items-center justify-content-center me-2 fw-bold" 
                                 style="width: 34px; height: 34px; font-size: 14px;">
                                ${initial}
                            </div>
                            <div>
                                <span class="fw-semibold ${currentTab === 'locked' ? 'text-muted text-decoration-line-through' : ''}">${escapeHtml(u.username)}</span>
                                ${statusBadge}
                                ${isCurrent ? '<span class="badge bg-info-subtle text-info border border-info-subtle ms-1" style="font-size: 0.65rem;">You</span>' : ''}
                            </div>
                        </div>
                    </td>
                    <td>${escapeHtml(u.fullName || "—")}</td>
                    <td><a href="mailto:${escapeHtml(u.email || '')}" class="text-decoration-none">${escapeHtml(u.email || "—")}</a></td>
                    <td>${roleBadge}</td>
                    <td class="text-muted small">${dateStr}</td>
                    <td class="text-end">${actionHtml}</td>
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
            // Lock / Deactivate User
            const deleteBtn = event.target.closest(".btn-delete-user");
            if (deleteBtn) {
                const id = deleteBtn.getAttribute("data-id");
                const username = deleteBtn.getAttribute("data-username");
                if (confirm(`Are you sure you want to lock / deactivate user "${username}" (#${id})?\nThis user will not be able to log in or register with this email/username.`)) {
                    try {
                        const res = await fetch(`/api/users/${id}`, { method: "DELETE" });
                        const result = await res.json();
                        if (res.ok && result.success) {
                            loadUsers();
                            updateLockedCount();
                        } else {
                            alert(result.message || "Failed to lock user");
                        }
                    } catch (err) {
                        alert("Error connecting to server while locking user.");
                    }
                }
                return;
            }

            // Restore / Unlock User
            const restoreBtn = event.target.closest(".btn-restore-user");
            if (restoreBtn) {
                const id = restoreBtn.getAttribute("data-id");
                const username = restoreBtn.getAttribute("data-username");
                if (confirm(`Are you sure you want to unlock user "${username}" (#${id})?`)) {
                    try {
                        const res = await fetch(`/api/users/${id}/restore`, { method: "PUT" });
                        const result = await res.json();
                        if (res.ok && result.success) {
                            loadUsers();
                            updateLockedCount();
                        } else {
                            alert(result.message || "Failed to unlock user");
                        }
                    } catch (err) {
                        alert("Error connecting to server while unlocking user.");
                    }
                }
                return;
            }

            // Assign Role
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
