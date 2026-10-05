/**
 * Customer Dashboard Logic & MySQL Interactivity
 */

let currentCustomer = null;
let selectedPlanName = 'Monthly Gold';
let selectedPlanAmount = 1500;
let currentBookingMode = 'ONE_DAY';
let myActiveQrToken = null;

document.addEventListener('DOMContentLoaded', () => {
    currentCustomer = GymAPI.requireRole('CUSTOMER');
    if (!currentCustomer) return;

    document.getElementById('custNameHeader').innerText = currentCustomer.name;
    document.getElementById('custAvatar').innerText = currentCustomer.name.charAt(0);

    const today = new Date().toISOString().split('T')[0];
    document.getElementById('bookOneDate').value = today;
    document.getElementById('bookDailyStart').value = today;

    const nextWeek = new Date();
    nextWeek.setDate(nextWeek.getDate() + 4);
    document.getElementById('bookDailyEnd').value = nextWeek.toISOString().split('T')[0];

    loadCustomerDashboard();
});

function showCustTab(tabId, el) {
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.getElementById(tabId).classList.add('active');

    if (el) {
        document.querySelectorAll('.sidebar-menu li').forEach(li => li.classList.remove('active'));
        el.parentElement.classList.add('active');
    }

    const titles = {
        'tab-membership': 'My Membership & Access Status',
        'tab-qr': 'My Digital Gym Access QR',
        'tab-trainers': 'Browse Certified Trainers',
        'tab-book': 'Schedule Coaching Session',
        'tab-mybookings': 'My Confirmed Bookings',
        'tab-myrequests': 'My Sent Coaching Requests',
        'tab-payments': 'Transaction Audit Trail',
        'tab-profile': 'My Member Profile'
    };
    if (titles[tabId]) {
        document.getElementById('custPageTitle').innerText = titles[tabId];
    }
}

async function loadCustomerDashboard() {
    loadMembership();
    loadQr();
    loadTrainers();
    loadMyBookings();
    loadMyRequests();
    loadMyPayments();
    loadProfile();
}

async function loadProfile() {
    try {
        const c = await GymAPI.getCustomerProfile(currentCustomer.userId);
        document.getElementById('custProfName').innerText = c.name;
        document.getElementById('custProfEmail').innerText = c.email;
        document.getElementById('custProfPhone').innerText = c.phone;
        document.getElementById('custProfAddress').innerText = c.address;
        document.getElementById('custProfRegDate').innerText = c.registrationDate;
    } catch (e) {
        console.error(e);
    }
}

async function loadMembership() {
    try {
        const res = await GymAPI.getCustomerMembership(currentCustomer.userId);
        const active = res.activeMembership;
        if (active) {
            document.getElementById('memPlanName').innerText = active.planName;
            document.getElementById('memAmount').innerText = '₹' + Number(active.amount).toLocaleString('en-IN');
            document.getElementById('memStartDate').innerText = active.startDate;
            document.getElementById('memEndDate').innerText = active.endDate;
            document.getElementById('memStatusBadge').className = 'badge badge-success';
            document.getElementById('memStatusBadge').innerText = active.status;
        } else {
            document.getElementById('memPlanName').innerText = 'No Active Plan';
            document.getElementById('memAmount').innerText = '₹0.00';
            document.getElementById('memStartDate').innerText = '-';
            document.getElementById('memEndDate').innerText = '-';
            document.getElementById('memStatusBadge').className = 'badge badge-danger';
            document.getElementById('memStatusBadge').innerText = 'EXPIRED / INACTIVE';
        }
    } catch (e) {
        console.error(e);
    }
}

function selectPlan(el, planName, amount) {
    document.querySelectorAll('.plan-card').forEach(c => c.classList.remove('selected'));
    el.classList.add('selected');
    selectedPlanName = planName;
    selectedPlanAmount = amount;
    document.getElementById('payAmountBtn').innerText = Number(amount).toLocaleString('en-IN');
}

async function executeSimulatedPayment() {
    const method = document.getElementById('paymentMethodSelect').value;
    try {
        const res = await GymAPI.processPayment({
            customerId: currentCustomer.userId,
            planName: selectedPlanName,
            amount: selectedPlanAmount,
            paymentMethod: method
        });
        showToast('Payment successful! Membership & QR Code generated in MySQL.', 'success');
        loadMembership();
        loadQr();
        loadMyPayments();
        showCustTab('tab-qr');
    } catch (err) {
        showToast('Payment transaction failed: ' + err.message, 'error');
    }
}

async function loadQr() {
    try {
        const res = await GymAPI.getCustomerQr(currentCustomer.userId);
        if (res.hasQr && res.qr) {
            const qr = res.qr;
            myActiveQrToken = qr.qrToken;
            document.getElementById('qrCustName').innerText = currentCustomer.name;
            document.getElementById('qrCustPlan').innerText = 'Membership ID #' + qr.membership.membershipId + ' &bull; ' + qr.membership.planName;
            document.getElementById('qrTokenString').innerText = qr.qrToken;
            document.getElementById('qrValidUntil').innerText = qr.validUntil;

            document.getElementById('qrStatusBadge').className = qr.status === 'ACTIVE' ? 'badge badge-success' : 'badge badge-danger';
            document.getElementById('qrStatusBadge').innerText = qr.status;

            renderQrSvg(qr.qrToken, 'qrCanvasBox', 170);
        } else {
            document.getElementById('qrCustName').innerText = currentCustomer.name;
            document.getElementById('qrCustPlan').innerText = 'No Active Membership Found';
            document.getElementById('qrTokenString').innerText = 'NOT AVAILABLE';
            document.getElementById('qrValidUntil').innerText = 'Please activate plan';
            document.getElementById('qrStatusBadge').className = 'badge badge-danger';
            document.getElementById('qrStatusBadge').innerText = 'INACTIVE';
            document.getElementById('qrCanvasBox').innerHTML = '<p style="color:var(--slate); font-size:12px; padding: 20px;">Activate membership to generate QR pass</p>';
        }
    } catch (e) {
        console.error(e);
    }
}

async function testMyQrAtGate() {
    if (!myActiveQrToken) {
        showToast('No active QR pass found to test.', 'error');
        return;
    }
    const disp = document.getElementById('custGateResult');
    disp.style.display = 'flex';

    try {
        const res = await GymAPI.verifyQrAccess(myActiveQrToken);
        if (res.granted) {
            disp.innerHTML = `
                <div class="gate-status-text granted">&#10004; ACCESS GRANTED</div>
                <p style="color: #6ee7b7; margin-top: 8px;">${res.message}</p>
                <p style="color: #cbd5e1; font-size: 13px;">Plan: <b>${res.planName}</b> &bull; Valid Until: <b>${res.validUntil}</b></p>
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

async function loadTrainers() {
    try {
        const list = await GymAPI.getCustomerTrainers();
        const grid = document.getElementById('trainersListGrid');
        const sel = document.getElementById('bookTrainerSelect');

        grid.innerHTML = '';
        sel.innerHTML = '<option value="">-- Select Trainer --</option>';

        list.forEach(t => {
            const availClass = t.availabilityStatus === 'AVAILABLE' ? 'badge-success' : 'badge-warning';
            grid.innerHTML += `
                <div class="plan-card">
                    <h4>${t.name}</h4>
                    <span class="badge badge-info" style="margin: 8px 0;">${t.specialization}</span>
                    <p style="font-size: 13px; color: var(--slate); margin: 6px 0;"><b>Experience:</b> ${t.experienceYears} Years</p>
                    <p style="font-size: 12px; color: var(--slate); margin-bottom: 12px;"><b>Certification:</b> ${t.qualification}</p>
                    <span class="badge ${availClass}" style="margin-bottom: 14px;">${t.availabilityStatus}</span>
                    <button class="btn btn-sm btn-primary" style="width: 100%;" onclick="quickSelectTrainer(${t.trainerId})">
                        Book Session &rarr;
                    </button>
                </div>
            `;
            sel.innerHTML += `<option value="${t.trainerId}">${t.name} (${t.specialization}) - ${t.availabilityStatus}</option>`;
        });
    } catch (e) {
        console.error(e);
    }
}

function quickSelectTrainer(trainerId) {
    document.getElementById('bookTrainerSelect').value = trainerId;
    handleTrainerSelectionChange();
    showCustTab('tab-book');
}

function switchBookingMode(mode) {
    currentBookingMode = mode;
    if (mode === 'ONE_DAY') {
        document.getElementById('btnModeOne').classList.add('active');
        document.getElementById('btnModeDaily').classList.remove('active');
        document.getElementById('oneDaySection').style.display = 'block';
        document.getElementById('dailySection').style.display = 'none';
    } else {
        document.getElementById('btnModeOne').classList.remove('active');
        document.getElementById('btnModeDaily').classList.add('active');
        document.getElementById('oneDaySection').style.display = 'none';
        document.getElementById('dailySection').style.display = 'block';
    }
}

function handleTrainerSelectionChange() {
    if (currentBookingMode === 'ONE_DAY') {
        loadAvailableSlotsForBooking();
    }
}

async function loadAvailableSlotsForBooking() {
    const trainerId = document.getElementById('bookTrainerSelect').value;
    const date = document.getElementById('bookOneDate').value;
    const slotSel = document.getElementById('bookSlotSelect');

    if (!trainerId || !date) return;

    try {
        const slots = await GymAPI.getAvailableSlots(trainerId, date);
        slotSel.innerHTML = '<option value="">-- Choose Available Slot --</option>';

        if (!slots || slots.length === 0) {
            slotSel.innerHTML = '<option value="">No slots free on this date</option>';
            return;
        }

        slots.forEach(s => {
            slotSel.innerHTML += `<option value="${s.slotId}">${s.startTime} - ${s.endTime} (${s.status})</option>`;
        });
    } catch (e) {
        console.error(e);
    }
}

async function handleSessionBooking(e) {
    e.preventDefault();
    const trainerId = document.getElementById('bookTrainerSelect').value;
    const message = document.getElementById('bookMessage').value;

    if (!trainerId) {
        showToast('Please select a trainer.', 'error');
        return;
    }

    try {
        if (currentBookingMode === 'ONE_DAY') {
            const slotId = document.getElementById('bookSlotSelect').value;
            const date = document.getElementById('bookOneDate').value;

            if (!slotId) {
                showToast('Please select an available slot for ONE DAY booking.', 'error');
                return;
            }

            await GymAPI.createBooking({
                customerId: currentCustomer.userId,
                trainerId: parseInt(trainerId),
                slotId: parseInt(slotId),
                startDate: date,
                bookingType: 'ONE_DAY',
                message: message
            });

            showToast('ONE DAY Booking confirmed in MySQL!', 'success');
        } else {
            // DAILY Booking
            const startDate = document.getElementById('bookDailyStart').value;
            const endDate = document.getElementById('bookDailyEnd').value;
            const startTime = document.getElementById('bookDailyTime').value + ':00';
            const endTime = document.getElementById('bookDailyEndTime').value + ':00';

            await GymAPI.createBooking({
                customerId: currentCustomer.userId,
                trainerId: parseInt(trainerId),
                bookingType: 'DAILY',
                startDate: startDate,
                endDate: endDate,
                startTime: startTime,
                endTime: endTime,
                message: message
            });

            showToast('DAILY recurring booking confirmed! Line items created in booking_slot.', 'success');
        }

        loadMyBookings();
        showCustTab('tab-mybookings');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleSendTrainerRequest() {
    const trainerId = document.getElementById('bookTrainerSelect').value;
    const slotId = document.getElementById('bookSlotSelect').value;
    const message = document.getElementById('bookMessage').value;

    if (!trainerId || !slotId) {
        showToast('Please select trainer, date, and available slot to send request.', 'error');
        return;
    }

    try {
        await GymAPI.sendTrainerRequest({
            customerId: currentCustomer.userId,
            trainerId: parseInt(trainerId),
            slotId: parseInt(slotId),
            message: message
        });
        showToast('Trainer coaching request submitted! Awaiting trainer response.', 'success');
        loadMyRequests();
        showCustTab('tab-myrequests');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function loadMyBookings() {
    try {
        const list = await GymAPI.getCustomerBookings(currentCustomer.userId);
        const tbody = document.querySelector('#myBookingsTable tbody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No sessions booked yet.</td></tr>';
            return;
        }

        list.forEach(b => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><b>#${b.bookingId}</b></td>
                <td><b>${b.trainer.name}</b></td>
                <td><span class="badge badge-info">${b.trainer.specialization}</span></td>
                <td><span class="badge badge-secondary">${b.bookingType}</span></td>
                <td>${b.startDate}</td>
                <td>${b.endDate}</td>
                <td><span class="badge badge-success">${b.status}</span></td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadMyRequests() {
    try {
        const list = await GymAPI.getCustomerRequests(currentCustomer.userId);
        const tbody = document.querySelector('#myRequestsTable tbody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No trainer requests sent.</td></tr>';
            return;
        }

        list.forEach(r => {
            const badgeClass = r.status === 'ACCEPTED' ? 'badge-success' : (r.status === 'REJECTED' ? 'badge-danger' : 'badge-warning');
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${r.requestId}</td>
                <td><b>${r.trainer.name}</b> (${r.trainer.specialization})</td>
                <td>${r.slot.slotDate}</td>
                <td>${r.slot.startTime} - ${r.slot.endTime}</td>
                <td><small>${r.message || '-'}</small></td>
                <td>${r.requestDate}</td>
                <td><span class="badge ${badgeClass}">${r.status}</span></td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadMyPayments() {
    try {
        const list = await GymAPI.getCustomerPayments(currentCustomer.userId);
        const tbody = document.querySelector('#myPaymentsTable tbody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No transactions recorded.</td></tr>';
            return;
        }

        list.forEach(p => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><b>#${p.paymentId}</b></td>
                <td>${p.membership ? p.membership.planName : 'Gym Membership'}</td>
                <td><b>₹${Number(p.amount).toLocaleString('en-IN')}</b></td>
                <td>${p.paymentDate ? p.paymentDate.substring(0, 19).replace('T', ' ') : '-'}</td>
                <td>${p.paymentMethod}</td>
                <td><span class="badge badge-success">${p.paymentStatus}</span></td>
                <td><code>${p.transactionReference}</code></td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error(e);
    }
}
