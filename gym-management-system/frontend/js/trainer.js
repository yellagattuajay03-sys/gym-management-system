/**
 * Trainer Dashboard Logic & MySQL Interactivity
 */

let currentTrainer = null;

document.addEventListener('DOMContentLoaded', () => {
    currentTrainer = GymAPI.requireRole('TRAINER');
    if (!currentTrainer) return;

    document.getElementById('trNameHeader').innerText = currentTrainer.name;
    document.getElementById('trAvatar').innerText = currentTrainer.name.charAt(0);

    const today = new Date().toISOString().split('T')[0];
    document.getElementById('newSlotDate').value = today;

    loadTrainerDashboard();
});

function showTrainerTab(tabId, el) {
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.getElementById(tabId).classList.add('active');

    if (el) {
        document.querySelectorAll('.sidebar-menu li').forEach(li => li.classList.remove('active'));
        el.parentElement.classList.add('active');
    }

    const titles = {
        'tab-profile': 'Trainer Profile & Availability',
        'tab-requests': 'Incoming Customer Requests',
        'tab-schedule': 'My Schedule & Slots',
        'tab-customers': 'My Assigned Clients',
        'tab-equipment': 'Equipment Requisitions'
    };
    if (titles[tabId]) {
        document.getElementById('trainerPageTitle').innerText = titles[tabId];
    }
}

async function loadTrainerDashboard() {
    loadProfile();
    loadCustomerRequests();
    loadScheduleSlots();
    loadMyCustomers();
    loadEquipmentOptions();
    loadMyEquipmentRequests();
}

async function loadProfile() {
    try {
        const p = await GymAPI.getTrainerProfile(currentTrainer.userId);
        document.getElementById('profName').innerText = p.name;
        document.getElementById('profEmail').innerText = p.email;
        document.getElementById('profSpec').innerText = p.specialization;
        document.getElementById('profExp').innerText = p.experienceYears + ' Years Professional Coaching';
        document.getElementById('profQual').innerText = p.qualification;
        document.getElementById('profPhone').innerText = p.phone;
        document.getElementById('trSpecializationHeader').innerText = p.specialization;
        document.getElementById('availabilitySelect').value = p.availabilityStatus;
    } catch (e) {
        showToast('Error loading profile: ' + e.message, 'error');
    }
}

async function updateTrainerAvailability() {
    const status = document.getElementById('availabilitySelect').value;
    try {
        await GymAPI.updateAvailability(currentTrainer.userId, status);
        showToast('Availability status updated to ' + status + ' in MySQL!', 'success');
        loadProfile();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadCustomerRequests() {
    try {
        const list = await GymAPI.getTrainerCustomerRequests(currentTrainer.userId);
        const tbody = document.querySelector('#customerRequestsTable tbody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No customer requests at this time.</td></tr>';
            return;
        }

        list.forEach(r => {
            const badgeClass = r.status === 'ACCEPTED' ? 'badge-success' : (r.status === 'REJECTED' ? 'badge-danger' : 'badge-warning');

            let actionBtns = '-';
            if (r.status === 'PENDING') {
                actionBtns = `
                    <button class="btn btn-sm btn-success" onclick="handleReq(${r.requestId}, 'ACCEPT')">Accept</button>
                    <button class="btn btn-sm btn-danger" onclick="handleReq(${r.requestId}, 'REJECT')">Reject</button>
                `;
            }

            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${r.requestId}</td>
                <td><b>${r.customer.name}</b></td>
                <td>${r.customer.phone}</td>
                <td>${r.slot.slotDate}</td>
                <td>${r.slot.startTime} - ${r.slot.endTime}</td>
                <td><small>${r.message || '-'}</small></td>
                <td><span class="badge ${badgeClass}">${r.status}</span></td>
                <td>${actionBtns}</td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

async function handleReq(requestId, action) {
    if (!confirm(`Are you sure you want to ${action} this request?`)) return;
    try {
        await GymAPI.handleCustomerRequest(requestId, action);
        showToast(`Request ${action}ED! MySQL booking updated.`, 'success');
        loadCustomerRequests();
        loadScheduleSlots();
        loadMyCustomers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function loadScheduleSlots() {
    try {
        const slots = await GymAPI.getTrainerSchedule(currentTrainer.userId);
        const tbody = document.querySelector('#mySlotsTable tbody');
        tbody.innerHTML = '';

        if (!slots || slots.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No schedule slots found.</td></tr>';
            return;
        }

        slots.forEach(s => {
            const badgeClass = s.status === 'AVAILABLE' ? 'badge-success' : (s.status === 'BOOKED' ? 'badge-danger' : 'badge-warning');
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${s.slotId}</td>
                <td><b>${s.slotDate}</b></td>
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

async function handleCreateSlot(e) {
    e.preventDefault();
    const date = document.getElementById('newSlotDate').value;
    const startTime = document.getElementById('newSlotStart').value + ':00';
    const endTime = document.getElementById('newSlotEnd').value + ':00';

    try {
        await GymAPI.addTrainerSlot(currentTrainer.userId, date, startTime, endTime);
        showToast('Schedule slot created successfully in MySQL!', 'success');
        closeModal('addSlotModal');
        loadScheduleSlots();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function loadMyCustomers() {
    try {
        const list = await GymAPI.getTrainerCustomers(currentTrainer.userId);
        const tbody = document.querySelector('#myCustomersTable tbody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No clients booked yet.</td></tr>';
            return;
        }

        list.forEach(c => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${c.customerId}</td>
                <td><b>${c.name}</b></td>
                <td>${c.email}</td>
                <td>${c.phone}</td>
                <td><small>${c.address}</small></td>
                <td>${c.registrationDate}</td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadEquipmentOptions() {
    try {
        const list = await GymAPI.getAllEquipment();
        const sel = document.getElementById('trEquipSelect');
        sel.innerHTML = '<option value="">-- Choose Equipment from Gym Inventory --</option>';
        list.forEach(eq => {
            sel.innerHTML += `<option value="${eq.equipmentId}">${eq.equipmentName} (Available Stock: ${eq.availableQuantity})</option>`;
        });
    } catch (e) {
        console.error(e);
    }
}

async function submitEquipmentRequisition(e) {
    e.preventDefault();
    const equipmentId = document.getElementById('trEquipSelect').value;
    const quantity = parseInt(document.getElementById('trEquipQty').value);
    const priority = document.getElementById('trEquipPriority').value;
    const reason = document.getElementById('trEquipReason').value;

    if (!equipmentId) {
        showToast('Please select equipment.', 'error');
        return;
    }

    try {
        await GymAPI.submitEquipmentRequest({
            trainerId: currentTrainer.userId,
            equipmentId: parseInt(equipmentId),
            quantity: quantity,
            priority: priority,
            reason: reason
        });
        showToast('Equipment request submitted to Admin for review!', 'success');
        document.getElementById('trainerEquipmentForm').reset();
        loadMyEquipmentRequests();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function loadMyEquipmentRequests() {
    try {
        const list = await GymAPI.getTrainerEquipmentRequests(currentTrainer.userId);
        const tbody = document.querySelector('#myEquipmentRequestsTable tbody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No equipment requisitions filed.</td></tr>';
            return;
        }

        list.forEach(r => {
            const prioClass = r.priority === 'HIGH' ? 'badge-danger' : (r.priority === 'MEDIUM' ? 'badge-warning' : 'badge-info');
            const statusClass = r.status === 'APPROVED' ? 'badge-success' : (r.status === 'REJECTED' ? 'badge-danger' : 'badge-secondary');
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${r.equipmentRequestId}</td>
                <td><b>${r.equipment.equipmentName}</b></td>
                <td>${r.quantity} unit(s)</td>
                <td><span class="badge ${prioClass}">${r.priority}</span></td>
                <td><small>${r.reason}</small></td>
                <td>${r.requestDate}</td>
                <td><span class="badge ${statusClass}">${r.status}</span></td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}
