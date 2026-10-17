// ==========================================================
// File: auth.js - Authentication & Session Management
// ==========================================================

const Auth = {
    // Check if user is logged in
    getUser: function () {
        const userStr = localStorage.getItem("isms_user");
        if (!userStr) return null;
        try {
            return JSON.parse(userStr);
        } catch (e) {
            localStorage.removeItem("isms_user");
            return null;
        }
    },

    setUser: function (user) {
        localStorage.setItem("isms_user", JSON.stringify(user));
    },

    clearUser: function () {
        localStorage.removeItem("isms_user");
    },

    // Guard protected pages
    requireAuth: function () {
        const user = this.getUser();
        if (!user) {
            window.location.href = "login.html";
            return;
        }
        this.renderUserBadge(user);
    },

    // Redirect to dashboard if already logged in on login/register pages
    redirectIfLoggedIn: function () {
        const user = this.getUser();
        if (user) {
            window.location.href = "index.html";
        }
    },

    // Render user profile and logout button in sidebar
    renderUserBadge: function (user) {
        const sidebar = document.querySelector(".sidebar");
        if (!sidebar) return;

        // Check if user badge already rendered
        if (document.getElementById("sidebarUserProfile")) return;

        const profileDiv = document.createElement("div");
        profileDiv.id = "sidebarUserProfile";
        profileDiv.className = "mt-auto pt-4 border-top border-secondary text-white";
        profileDiv.innerHTML = `
            <div class="d-flex align-items-center mb-3">
                <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2 fw-bold" 
                     style="width: 40px; height: 40px; font-size: 16px;">
                    ${(user.username || "U").charAt(0).toUpperCase()}
                </div>
                <div class="overflow-hidden">
                    <div class="fw-semibold text-truncate" style="max-width: 130px;">${user.fullName || user.username}</div>
                    <small class="badge bg-secondary text-capitalize" style="font-size: 0.7rem;">${user.role || 'USER'}</small>
                </div>
            </div>
            <button id="logoutBtn" class="btn btn-outline-danger btn-sm w-100 d-flex align-items-center justify-content-center gap-1">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-box-arrow-right" viewBox="0 0 16 16">
                    <path fill-rule="evenodd" d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0z"/>
                    <path fill-rule="evenodd" d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708z"/>
                </svg>
                <span>Logout</span>
            </button>
        `;

        sidebar.appendChild(profileDiv);

        document.getElementById("logoutBtn").addEventListener("click", () => {
            Auth.logout();
        });
    },

    // Login API call
    login: async function (username, password) {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });
        const result = await response.json();
        if (response.ok && result.success) {
            this.setUser(result.data);
            return { success: true, data: result.data };
        } else {
            return { success: false, message: result.message || "Login failed" };
        }
    },

    // Register API call
    register: async function (userData) {
        const response = await fetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(userData)
        });
        const result = await response.json();
        if (response.ok && result.success) {
            this.setUser(result.data);
            return { success: true, data: result.data };
        } else {
            return { success: false, message: result.message || "Registration failed" };
        }
    },

    // Logout API call
    logout: async function () {
        try {
            await fetch("/api/auth/logout", { method: "POST" });
        } catch (e) {
            console.error("Logout request error", e);
        }
        this.clearUser();
        window.location.href = "login.html";
    }
};
