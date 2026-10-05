/**
 * Gym Management System - Core API Client & Shared UI Helpers
 */

const API_BASE = window.location.origin.includes('http') && !window.location.origin.includes('file') 
    ? '/api' 
    : 'http://localhost:8080/api';

const GymAPI = {
    // Current user helpers
    getUser() {
        try {
            const u = localStorage.getItem('gym_user');
            return u ? JSON.parse(u) : null;
        } catch (e) {
            return null;
        }
    },

    setUser(user) {
        localStorage.setItem('gym_user', JSON.stringify(user));
    },

    logout() {
        localStorage.removeItem('gym_user');
        window.location.href = 'index.html';
    },

    requireRole(expectedRole) {
        const user = this.getUser();
        if (!user || user.role !== expectedRole) {
            alert('Access Denied: Please log in as ' + expectedRole);
            window.location.href = 'index.html';
            return null;
        }
        return user;
    },

    // HTTP Request Wrapper
    async request(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        try {
            const resp = await fetch(url, { ...options, headers });
            const data = await resp.json().catch(() => ({}));
            if (!resp.ok) {
                const err = (data && (data.error || data.message)) || `Error ${resp.status}: ${resp.statusText}`;
                throw new Error(err);
            }
            return data;
        } catch (err) {
            console.error(`API Error [${endpoint}]:`, err);
            throw err;
        }
    },

    // Auth Endpoints
    async login(email, password, role) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password, role })
        });
    },

    async register(name, email, password, phone, address) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ name, email, password, phone, address })
        });
    },

    // Admin Endpoints
    async getAdminStats() {
        return this.request('/admin/stats');
    },

    async getAllTrainers() {
        return this.request('/admin/trainers');
    },

    async addTrainer(trainerData) {
        return this.request('/admin/trainers', {
            method: 'POST',
            body: JSON.stringify(trainerData)
        });
    },

    async updateTrainer(id, trainerData) {
        return this.request(`/admin/trainers/${id}`, {
            method: 'PUT',
            body: JSON.stringify(trainerData)
        });
    },

    async deleteTrainer(id) {
        return this.request(`/admin/trainers/${id}`, {
            method: 'DELETE'
        });
    },

    async getAllCustomers() {
        return this.request('/admin/customers');
    },

    async getCustomerDetails(id) {
        return this.request(`/admin/customers/${id}`);
    },

    async getAllSlots() {
        return this.request('/admin/slots');
    },

    async getAllBookings() {
        return this.request('/admin/bookings');
    },

    async getAllEquipment() {
        return this.request('/admin/equipment');
    },

    async addEquipment(data) {
        return this.request('/admin/equipment', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    async getEquipmentRequests() {
        return this.request('/admin/equipment-requests');
    },

    async reviewEquipmentRequest(requestId, action) {
        return this.request(`/admin/equipment-requests/${requestId}/review?action=${action}`, {
            method: 'POST'
        });
    },

    // Trainer Endpoints
    async getTrainerProfile(trainerId) {
        return this.request(`/trainer/profile/${trainerId}`);
    },

    async updateAvailability(trainerId, status) {
        return this.request(`/trainer/profile/${trainerId}/availability?status=${status}`, {
            method: 'PUT'
        });
    },

    async getTrainerSchedule(trainerId) {
        return this.request(`/trainer/schedule/${trainerId}`);
    },

    async addTrainerSlot(trainerId, date, startTime, endTime) {
        return this.request(`/trainer/slots/${trainerId}?date=${date}&startTime=${startTime}&endTime=${endTime}`, {
            method: 'POST'
        });
    },

    async getTrainerCustomerRequests(trainerId) {
        return this.request(`/trainer/requests/${trainerId}`);
    },

    async handleCustomerRequest(requestId, action) {
        return this.request(`/trainer/requests/${requestId}/handle?action=${action}`, {
            method: 'POST'
        });
    },

    async getTrainerCustomers(trainerId) {
        return this.request(`/trainer/customers/${trainerId}`);
    },

    async getTrainerBookings(trainerId) {
        return this.request(`/trainer/bookings/${trainerId}`);
    },

    async submitEquipmentRequest(data) {
        return this.request('/trainer/equipment-requests', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    async getTrainerEquipmentRequests(trainerId) {
        return this.request(`/trainer/equipment-requests/${trainerId}`);
    },

    // Customer Endpoints
    async getCustomerProfile(customerId) {
        return this.request(`/customer/profile/${customerId}`);
    },

    async getCustomerTrainers() {
        return this.request('/customer/trainers');
    },

    async getAvailableSlots(trainerId, date) {
        const q = date ? `?date=${date}` : '';
        return this.request(`/customer/trainers/${trainerId}/slots${q}`);
    },

    async sendTrainerRequest(requestData) {
        return this.request('/customer/requests', {
            method: 'POST',
            body: JSON.stringify(requestData)
        });
    },

    async getCustomerRequests(customerId) {
        return this.request(`/customer/requests/${customerId}`);
    },

    async createBooking(bookingData) {
        return this.request('/customer/bookings', {
            method: 'POST',
            body: JSON.stringify(bookingData)
        });
    },

    async getCustomerBookings(customerId) {
        return this.request(`/customer/bookings/${customerId}`);
    },

    async getCustomerMembership(customerId) {
        return this.request(`/customer/membership/${customerId}`);
    },

    async getCustomerPayments(customerId) {
        return this.request(`/customer/payments/${customerId}`);
    },

    async processPayment(paymentData) {
        return this.request('/customer/pay', {
            method: 'POST',
            body: JSON.stringify(paymentData)
        });
    },

    async getCustomerQr(customerId) {
        return this.request(`/customer/qr/${customerId}`);
    },

    // QR Access Verification (Gate Simulator)
    async verifyQrAccess(token) {
        return this.request(`/qr/verify?token=${encodeURIComponent(token)}`);
    }
};

// UI Notification Toasts
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Modal Helpers
function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('active');
}

// Generate Offline-Capable Clean SVG QR Pattern for Viva Demos
function renderQrSvg(token, containerId, size = 160) {
    const container = document.getElementById(containerId);
    if (!container) return;

    // Deterministic pseudo-random pattern based on string hash
    let hash = 0;
    for (let i = 0; i < token.length; i++) {
        hash = ((hash << 5) - hash) + token.charCodeAt(i);
        hash |= 0;
    }

    const cells = 21; // standard QR dimension
    const cellSize = size / cells;

    let svg = `<svg width="${size}" height="${size}" viewBox="0 0 ${size} ${size}" xmlns="http://www.w3.org/2000/svg">`;
    svg += `<rect width="${size}" height="${size}" fill="#ffffff"/>`;

    // Function to check if coordinate is in the three corner finder patterns
    const isFinder = (r, c) => {
        if (r < 7 && c < 7) return true;
        if (r < 7 && c >= cells - 7) return true;
        if (r >= cells - 7 && c < 7) return true;
        return false;
    };

    // Draw 3 corner finder eyes
    const drawEye = (x, y) => {
        svg += `<rect x="${x}" y="${y}" width="${7 * cellSize}" height="${7 * cellSize}" fill="#000000"/>`;
        svg += `<rect x="${x + cellSize}" y="${y + cellSize}" width="${5 * cellSize}" height="${5 * cellSize}" fill="#ffffff"/>`;
        svg += `<rect x="${x + 2 * cellSize}" y="${y + 2 * cellSize}" width="${3 * cellSize}" height="${3 * cellSize}" fill="#000000"/>`;
    };

    drawEye(0, 0);
    drawEye((cells - 7) * cellSize, 0);
    drawEye(0, (cells - 7) * cellSize);

    // Draw data pixels
    let seed = Math.abs(hash);
    for (let r = 0; r < cells; r++) {
        for (let c = 0; c < cells; c++) {
            if (isFinder(r, c)) continue;
            seed = (seed * 9301 + 49297) % 233280;
            if (seed / 233280 > 0.5) {
                svg += `<rect x="${c * cellSize}" y="${r * cellSize}" width="${cellSize}" height="${cellSize}" fill="#0f172a"/>`;
            }
        }
    }

    svg += `</svg>`;
    container.innerHTML = svg;
}
