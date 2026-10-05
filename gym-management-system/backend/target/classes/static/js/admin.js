/**
 * Admin Dashboard Logic & MySQL Interactivity
 */

let currentAdmin = null;

document.addEventListener('DOMContentLoaded', () => {
    currentAdmin = GymAPI.requireRole('ADMIN');
    if (!currentAdmin) return;

    document.getElementById('adminName').innerText = currentAdmin.name;
    document.getElementById('adminAvatar').innerText = currentAdmin.name.charAt(0);

    loadAllAdminData();
});

function showTab(tabId, el) {
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.getElementById(tabId).classList.add('active');

    if (el) {
        document.querySelectorAll('.sidebar-menu li').forEach(li => li.classList.remove('active'));
        el.parentElement.classList.add('active');
    }

    const titles = {
        'tab-overview': 'Admin Dashboard Overview',
        'tab-trainers': 'Trainer Management',
        'tab-customers': 'Customer Management & Memberships',
        'tab-schedules': 'Trainer Schedules & Slot Allocation',
        'tab-bookings': 'Bookings & Line Item Sessions',
        'tab-equipment': 'Equipment Inventory & Requisitions',
        'tab-scanner': 'QR Turnstile Gate Scanner'
    };
    if (titles[tabId]) {
        document.getElementById('pageTitleHeading').innerText = titles[tabId];
    }
}

async function loadAllAdminData() {
    loadStats();
    loadTrainers();
    loadCustomers();
    loadSlots();
    loadBookings();
    loadEquipment();
    loadEquipmentRequests();
}

async function loadStats() {
    try {
        const stats = await GymAPI.getAdminStats();
        document.getElementById('statTotalTrainers').innerText = stats.totalTrainers || 0;
        document.getElementById('statTotalCustomers').innerText = stats.totalCustomers || 0;
        document.getElementById('statActiveMembers').innerText = stats.activeMembers || 0;
        document.getElementById('statTodayBookings').innerText = stats.todayBookings || 0;
        document.getElementById('statPendingTrainerReq').innerText = stats.pendingTrainerRequests || 0;
        document.getElementById('statPendingEquipmentReq').innerText = stats.pendingEquipmentRequests || 0;
        document.getElementById('statAvailableEquipment').innerText = stats.availableEquipment || 0;
        document.getElementById('statTotalRevenue').innerText = '₹' + Number(stats.totalRevenue || 0).toLocaleString('en-IN');
    } catch (e) {
        console.error('Stats error:', e);
    }
}

async function loadTrainers() {
    try {
        const list = await GymAPI.getAllTrainers();
        const tbody = document.querySelector('#trainersTable tbody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;">No trainers found</td></tr>';
            return;
        }

        list.forEach(t => {
            const availClass = t.availabilityStatus === 'AVAILABLE' ? 'badge-success' : 'badge-warning';
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><b>#${t.trainerId}</b></td>
                <td><strong>${t.name}</strong><br><small style="color:var(--slate)">${t.email}</small></td>
                <td><span class="badge badge-info">${t.specialization}</span></td>
                <td>${t.experienceYears} Years</td>
                <td>${t.qualification}</td>
                <td><b style="color:#0f172a">₹${Number(t.salary).toLocaleString('en-IN')}</b></td>
                <td><span class="badge ${availClass}">${t.availabilityStatus}</span></td>
                <td>${t.phone}</td>
                <td>
                    <button class="btn btn-sm btn-outline" onclick="openEditTrainerModal(${JSON.stringify(t).replace(/"/g, '&quot;')})">Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteTrainer(${t.trainerId})">Delete</button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        showToast('Error loading trainers: ' + e.message, 'error');
    }
}

function openAddTrainerModal() {
    document.getElementById('trainerModalTitle').innerText = 'Add New Trainer';
    document.getElementById('trModalId').value = '';
    document.getElementById('trainerForm').reset();
    openModal('trainerModal');
}

function openEditTrainerModal(t) {
    document.getElementById('trainerModalTitle').innerText = 'Edit Trainer #' + t.trainerId;
    document.getElementById('trModalId').value = t.trainerId;
    document.getElementById('trName').value = t.name;
    document.getElementById('trEmail').value = t.email;
    document.getElementById('trPhone').value = t.phone;
    document.getElementById('trSpecialization').value = t.specialization;
    document.getElementById('trExperience').value = t.experienceYears;
    document.getElementById('trSalary').value = t.salary;
    document.getElementById('trQualification').value = t.qualification;
    document.getElementById('trAvailability').value = t.availabilityStatus;
    openModal('trainerModal');
}

async function saveTrainer(e) {
    e.preventDefault();
    const id = document.getElementById('trModalId').value;
    const dto = {
        name: document.getElementById('trName').value,
        email: document.getElementById('trEmail').value,
        phone: document.getElementById('trPhone').value,
        specialization: document.getElementById('trSpecialization').value,
        experienceYears: parseInt(document.getElementById('trExperience').value),
        salary: parseFloat(document.getElementById('trSalary').value),
        qualification: document.getElementById('trQualification').value,
        availabilityStatus: document.getElementById('trAvailability').value
    };

    try {
        if (id) {
            await GymAPI.updateTrainer(id, dto);
            showToast('Trainer updated successfully!', 'success');
        } else {
            dto.password = 'trainer123';
            await GymAPI.addTrainer(dto);
            showToast('Trainer added successfully!', 'success');
        }
        closeModal('trainerModal');
        loadTrainers();
        loadStats();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteTrainer(id) {
    if (!confirm('Are you sure you want to delete trainer #' + id + '? This will also remove their schedule slots.')) return;
    try {
        await GymAPI.deleteTrainer(id);
        showToast('Trainer removed from database.', 'success');
        loadTrainers();
        loadStats();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function loadCustomers() {
    try {
        const list = await GymAPI.getAllCustomers();
        const tbody = document.querySelector('#customersTable tbody');
        tbody.innerHTML = '';

        list.forEach(c => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><b>#${c.customerId}</b></td>
                <td><strong>${c.name}</strong></td>
                <td>${c.email}</td>
                <td>${c.phone}</td>
                <td><small>${c.address}</small></td>
                <td>${c.registrationDate}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="viewCustomerDetails(${c.customerId})">View Details</button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

async function viewCustomerDetails(id) {
    try {
        const data = await GymAPI.getCustomerDetails(id);
        const c = data.customer;
        const memberships = data.memberships || [];
        const payments = data.payments || [];
        const bookings = data.bookings || [];

        document.getElementById('custDetailTitle').innerText = `${c.name} (Customer #${c.customerId})`;

        let mHtml = memberships.map(m => `
            <tr>
                <td><b>${m.planName}</b></td>
                <td>₹${Number(m.amount).toLocaleString('en-IN')}</td>
                <td>${m.startDate}</td>
                <td>${m.endDate}</td>
                <td><span class="badge ${m.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'}">${m.status}</span></td>
            </tr>
        `).join('') || '<tr><td colspan="5">No memberships on record</td></tr>';

        let pHtml = payments.map(p => `
            <tr>
                <td>₹${Number(p.amount).toLocaleString('en-IN')}</td>
                <td>${p.paymentMethod}</td>
                <td>${p.transactionReference}</td>
                <td>${p.paymentDate}</td>
                <td><span class="badge badge-success">${p.paymentStatus}</span></td>
            </tr>
        `).join('') || '<tr><td colspan="5">No payments found</td></tr>';

        document.getElementById('customerDetailBody').innerHTML = `
            <div style="margin-bottom: 16px;">
                <p><b>Email:</b> ${c.email} &bull; <b>Phone:</b> ${c.phone}</p>
                <p><b>Address:</b> ${c.address}</p>
                <p><b>Member Since:</b> ${c.registrationDate}</p>
            </div>
            <h4 style="margin: 16px 0 8px; font-size:14px;">Memberships (Table: <code>membership</code>)</h4>
            <div class="table-responsive">
                <table class="custom-table">
                    <thead><tr><th>Plan</th><th>Amount</th><th>Start</th><th>End</th><th>Status</th></tr></thead>
                    <tbody>${mHtml}</tbody>
                </table>
            </div>
            <h4 style="margin: 16px 0 8px; font-size:14px;">Payment Audit Trail (Table: <code>payment</code>)</h4>
            <div class="table-responsive">
                <table class="custom-table">
                    <thead><tr><th>Amount</th><th>Method</th><th>Reference</th><th>Date</th><th>Status</th></tr></thead>
                    <tbody>${pHtml}</tbody>
                </table>
            </div>
        `;
        openModal('customerDetailModal');
    } catch (e) {
        showToast('Error loading customer details: ' + e.message, 'error');
    }
}

async function loadSlots() {
    try {
        const slots = await GymAPI.getAllSlots();
        const tbody = document.querySelector('#slotsTable tbody');
        tbody.innerHTML = '';

        slots.forEach(s => {
            const badgeClass = s.status === 'AVAILABLE' ? 'badge-success' : (s.status === 'BOOKED' ? 'badge-danger' : 'badge-warning');
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${s.slotId}</td>
                <td><b>${s.trainer.name}</b></td>
                <td>${s.trainer.specialization}</td>
                <td>${s.slotDate}</td>
                <td>${s.startTime}</td>
                <td>${s.endTime}</td>
                <td><span class="badge ${badgeClass}">${s.status}</span></td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadBookings() {
    try {
        const list = await GymAPI.getAllBookings();
        const tbody = document.querySelector('#bookingsTable tbody');
        tbody.innerHTML = '';

        list.forEach(b => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><b>#${b.bookingId}</b></td>
                <td>${b.customer.name}</td>
                <td>${b.trainer.name}</td>
                <td><span class="badge badge-info">${b.bookingType}</span></td>
                <td>${b.startDate}</td>
                <td>${b.endDate}</td>
                <td>${b.createdAt ? b.createdAt.substring(0,19).replace('T', ' ') : '-'}</td>
                <td><span class="badge badge-success">${b.status}</span></td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadEquipment() {
    try {
        const list = await GymAPI.getAllEquipment();
        const tbody = document.querySelector('#equipmentTable tbody');
        tbody.innerHTML = '';

        list.forEach(eq => {
            const badgeClass = eq.status === 'AVAILABLE' ? 'badge-success' : (eq.status === 'LOW_STOCK' ? 'badge-warning' : 'badge-danger');
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${eq.equipmentId}</td>
                <td><b>${eq.equipmentName}</b></td>
                <td><small>${eq.description || '-'}</small></td>
                <td>${eq.totalQuantity}</td>
                <td><b style="color:${eq.availableQuantity <= 2 ? '#ef4444' : '#10b981'}">${eq.availableQuantity}</b></td>
                <td><span class="badge ${badgeClass}">${eq.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline" onclick="openEditEquipmentModal(${JSON.stringify(eq).replace(/"/g, '&quot;')})">Edit Qty</button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

function openAddEquipmentModal() {
    document.getElementById('equipmentModalTitle').innerText = 'Add New Equipment';
    document.getElementById('eqModalId').value = '';
    document.getElementById('equipmentForm').reset();
    openModal('equipmentModal');
}

function openEditEquipmentModal(eq) {
    document.getElementById('equipmentModalTitle').innerText = 'Update Equipment #' + eq.equipmentId;
    document.getElementById('eqModalId').value = eq.equipmentId;
    document.getElementById('eqName').value = eq.equipmentName;
    document.getElementById('eqDesc').value = eq.description;
    document.getElementById('eqTotal').value = eq.totalQuantity;
    document.getElementById('eqAvailable').value = eq.availableQuantity;
    openModal('equipmentModal');
}

async function saveEquipment(e) {
    e.preventDefault();
    const id = document.getElementById('eqModalId').value;
    const dto = {
        equipmentName: document.getElementById('eqName').value,
        description: document.getElementById('eqDesc').value,
        totalQuantity: parseInt(document.getElementById('eqTotal').value),
        availableQuantity: parseInt(document.getElementById('eqAvailable').value)
    };

    try {
        if (id) {
            await GymAPI.request(`/admin/equipment/${id}`, { method: 'PUT', body: JSON.stringify(dto) });
            showToast('Equipment updated in MySQL inventory!', 'success');
        } else {
            await GymAPI.addEquipment(dto);
            showToast('New equipment added to MySQL!', 'success');
        }
        closeModal('equipmentModal');
        loadEquipment();
        loadStats();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function loadEquipmentRequests() {
    try {
        const list = await GymAPI.getEquipmentRequests();
        const tbody = document.querySelector('#equipmentRequestsTable tbody');
        tbody.innerHTML = '';

        list.forEach(r => {
            const prioClass = r.priority === 'HIGH' ? 'badge-danger' : (r.priority === 'MEDIUM' ? 'badge-warning' : 'badge-info');
            const statusClass = r.status === 'APPROVED' ? 'badge-success' : (r.status === 'REJECTED' ? 'badge-danger' : 'badge-secondary');

            let actions = '-';
            if (r.status === 'PENDING') {
                actions = `
                    <button class="btn btn-sm btn-success" onclick="reviewReq(${r.equipmentRequestId}, 'APPROVE')">Approve</button>
                    <button class="btn btn-sm btn-danger" onclick="reviewReq(${r.equipmentRequestId}, 'REJECT')">Reject</button>
                `;
            }

            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${r.equipmentRequestId}</td>
                <td><b>${r.trainer.name}</b></td>
                <td>${r.equipment.equipmentName}</td>
                <td><b>${r.quantity}</b> unit(s)</td>
                <td><span class="badge ${prioClass}">${r.priority}</span></td>
                <td><small>${r.reason}</small></td>
                <td>${r.requestDate}</td>
                <td><span class="badge ${statusClass}">${r.status}</span></td>
                <td>${actions}</td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

async function reviewReq(id, action) {
    if (!confirm(`Are you sure you want to ${action} this equipment request?`)) return;
    try {
        await GymAPI.reviewEquipmentRequest(id, action);
        showToast(`Request ${action}ED successfully! Stock updated.`, 'success');
        loadEquipmentRequests();
        loadEquipment();
        loadStats();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function adminScanQr() {
    const token = document.getElementById('adminQrInput').value.trim();
    const disp = document.getElementById('adminQrResult');
    disp.style.display = 'flex';

    try {
        const res = await GymAPI.verifyQrAccess(token);
        if (res.granted) {
            disp.innerHTML = `
                <div class="gate-status-text granted">&#10004; ACCESS GRANTED</div>
                <p style="color: #6ee7b7; margin-top: 8px;">${res.message}</p>
                <p style="color: #cbd5e1; font-size: 13px;">Customer: <b>${res.customerName}</b> &bull; Plan: <b>${res.planName}</b></p>
            `;
        } else {
            disp.innerHTML = `
                <div class="gate-status-text denied">&#10008; ACCESS DENIED</div>
                <p style="color: #fca5a5; margin-top: 8px;">${res.message}</p>
            `;
        }
    } catch (err) {
        disp.innerHTML = `<div class="gate-status-text denied">&#10008; ERROR</div><p style="color:#fca5a5">${err.message}</p>`;
    }
}
