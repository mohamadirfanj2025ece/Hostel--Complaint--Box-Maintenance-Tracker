const state = {
  user: JSON.parse(localStorage.getItem('complaintboxUser') || 'null'),
  activeView: JSON.parse(localStorage.getItem('complaintboxUser') || 'null')?.role === 'USER'
    ? 'profile'
    : 'dashboard',
};

const authSection = document.getElementById('authSection');
const dashboardSection = document.getElementById('dashboardSection');
const complaintsSection = document.getElementById('complaintsSection');
const createSection = document.getElementById('createSection');
const adminSection = document.getElementById('adminSection');
const profileSection = document.getElementById('profileSection');
const navButtons = document.querySelectorAll('.nav-btn');
const loginForm = document.getElementById('loginForm');
const complaintForm = document.getElementById('complaintForm');
const authMessage = document.getElementById('authMessage');
const userBox = document.getElementById('userBox');
const userName = document.getElementById('userName');
const userRole = document.getElementById('userRole');
const userAvatar = document.getElementById('userAvatar');

function setMessage(el, message, type) {
  el.className = 'message';
  if (type) el.classList.add(type);
  el.textContent = message;
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (character) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  })[character]);
}

function showAuthScreen() {
  authSection.classList.add('active');
  dashboardSection.classList.remove('active');
  profileSection.classList.remove('active');
  complaintsSection.classList.remove('active');
  createSection.classList.remove('active');
  adminSection.classList.remove('active');
  document.getElementById('sidebar').style.display = 'none';
}

function showAppScreen() {
  authSection.classList.remove('active');
  document.getElementById('sidebar').style.display = 'flex';

  if (state.user && state.user.role === 'ADMIN') {
    document.querySelector('.admin-only').style.display = 'block';
    document.querySelectorAll('.student-only').forEach((button) => {
      button.style.display = 'none';
    });
    document.querySelector('[data-view="dashboard"]').style.display = 'block';
  } else {
    document.querySelector('.admin-only').style.display = 'none';
    document.querySelectorAll('.student-only').forEach((button) => {
      button.style.display = 'block';
    });
    document.querySelector('[data-view="dashboard"]').style.display = 'none';
  }

  renderView(state.activeView || 'dashboard');
}

function renderView(view) {
  const adminViews = ['dashboard', 'admin'];
  const studentViews = ['profile', 'complaints', 'create'];
  const allowedViews = state.user?.role === 'ADMIN' ? adminViews : studentViews;
  if (!state.user || !allowedViews.includes(view)) return;

  state.activeView = view;
  const allViews = ['dashboardSection', 'profileSection', 'complaintsSection', 'createSection', 'adminSection'];
  allViews.forEach((id) => {
    const el = document.getElementById(id);
    el.classList.toggle('active', id === `${view}Section`);
  });

  navButtons.forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.view === view);
  });

  if (state.user?.role === 'ADMIN' && view === 'admin') {
    loadAdminDashboard();
  }

  if (state.user.role === 'USER' && view === 'profile') loadStudentProfile();
  if (state.user.role === 'ADMIN' && view === 'dashboard') loadDashboard();
  if (state.user.role === 'USER' && view === 'complaints') loadMyComplaints();
}

function updateUserUI() {
  if (!state.user) {
    userName.textContent = 'Not signed in';
    userRole.textContent = 'Guest';
    userAvatar.textContent = 'G';
    showAuthScreen();
    return;
  }

  userName.textContent = state.user.name || state.user.email;
  userRole.textContent = state.user.role;
  userAvatar.textContent = (state.user.name || state.user.email).charAt(0).toUpperCase();
  showAppScreen();
}

async function apiRequest(url, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = headers['Content-Type'] || 'application/json';
  }

  const response = await fetch(url, {
    ...options,
    headers
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(data?.message || 'Request failed');
  }

  return data;
}

function setAuthHeader() {
  const user = state.user;
  const email = user?.email || '';
  const password = user?.password || '';

  return {
    Authorization: 'Basic ' + btoa(`${email}:${password}`)
  };
}

function saveUserSession(userData) {
  state.user = {
    ...userData,
    password: document.getElementById(userData.role === 'ADMIN' ? 'loginPassword' : 'loginPassword').value || ''
  };
  localStorage.setItem('complaintboxUser', JSON.stringify({
    name: userData.name,
    email: userData.email,
    role: userData.role,
    password: state.user.password
  }));
  updateUserUI();
}

async function loadDashboard() {
  try {
    const data = await apiRequest('/api/admin/dashboard', {
      headers: setAuthHeader()
    });

    const stats = document.getElementById('dashboardStats');
    const cards = [
      { label: 'Total', value: data.total },
      { label: 'Open', value: data.open },
      { label: 'In Progress', value: data.inProgress },
      { label: 'Resolved', value: data.resolved },
      { label: 'Overdue', value: data.overdue }
    ];

    stats.innerHTML = cards.map(item => `
      <div class="stat-card">
        <div class="label">${item.label}</div>
        <div class="value">${item.value}</div>
      </div>
    `).join('');
  } catch (error) {
    document.getElementById('dashboardStats').innerHTML = `<div class="empty-state">${error.message}</div>`;
  }
}

async function loadMyComplaints() {
  try {
    const complaints = await apiRequest('/api/complaints/my', {
      headers: setAuthHeader()
    });
    renderComplaintCards(complaints, 'complaintsList');
  } catch (error) {
    document.getElementById('complaintsList').innerHTML = `<div class="empty-state">${error.message}</div>`;
  }
}

async function loadStudentProfile() {
  const container = document.getElementById('profileDetails');
  try {
    const profile = await apiRequest('/api/students/me', { headers: setAuthHeader() });
    const incomplete = ['registerNumber', 'department', 'hostelBlock', 'floor', 'roomNumber', 'phoneNumber']
      .some((field) => !profile[field]);
    if (incomplete) {
      renderStudentProfileForm(profile, container, false);
      return;
    }
    renderStudentProfileDetails(profile, container);
  } catch (error) {
    container.innerHTML = `<div class="empty-state">${error.message}</div>`;
  }
}

function renderStudentProfileDetails(profile, container) {
  const fields = [
    ['Full name', profile.name],
    ['Email', profile.email],
    ['Register number', profile.registerNumber],
    ['Department', profile.department],
    ['Hostel block', profile.hostelBlock],
    ['Floor', profile.floor],
    ['Room number', profile.roomNumber],
    ['Phone number', profile.phoneNumber]
  ];
  container.innerHTML = `
    <div class="profile-toolbar"><button class="secondary-btn" id="editProfileBtn" type="button">Edit details</button></div>
    ${fields.map(([label, value]) => `
      <div class="profile-item"><span>${label}</span><strong>${escapeHtml(value || 'Not provided')}</strong></div>
    `).join('')}`;
  document.getElementById('editProfileBtn').addEventListener('click', () => {
    renderStudentProfileForm(profile, container, true);
  });
}

function renderStudentProfileForm(profile, container, canCancel) {
  container.innerHTML = `
    <form id="completeProfileForm" class="complaint-form">
      ${canCancel ? '' : '<p class="eyebrow">Complete your profile</p><p>Finish these personal details to continue.</p>'}
      <div class="two-cols">
        <div><label for="profileRegisterNumber">Register number</label><input id="profileRegisterNumber" required /></div>
        <div><label for="profileDepartment">Department</label><input id="profileDepartment" required /></div>
      </div>
      <div class="two-cols">
        <div><label for="profileHostelBlock">Hostel block</label><input id="profileHostelBlock" required /></div>
        <div><label for="profileFloor">Floor</label><input id="profileFloor" required /></div>
      </div>
      <div class="two-cols">
        <div><label for="profileRoomNumber">Room number</label><input id="profileRoomNumber" required /></div>
        <div><label for="profilePhoneNumber">Phone number</label><input id="profilePhoneNumber" required /></div>
      </div>
      <div class="profile-form-actions">
        <button class="primary-btn" type="submit">${canCancel ? 'Save' : 'Save Personal Details'}</button>
        ${canCancel ? '<button class="secondary-btn" id="cancelProfileEdit" type="button">Cancel</button>' : ''}
      </div>
      <div id="profileMessage" class="message"></div>
    </form>`;
  const profileFields = {
    profileRegisterNumber: profile.registerNumber,
    profileDepartment: profile.department,
    profileHostelBlock: profile.hostelBlock,
    profileFloor: profile.floor,
    profileRoomNumber: profile.roomNumber,
    profilePhoneNumber: profile.phoneNumber
  };
  Object.entries(profileFields).forEach(([id, value]) => {
    document.getElementById(id).value = value || '';
  });
  document.getElementById('completeProfileForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const payload = {
      registerNumber: document.getElementById('profileRegisterNumber').value.trim(),
      department: document.getElementById('profileDepartment').value.trim(),
      hostelBlock: document.getElementById('profileHostelBlock').value.trim(),
      floor: document.getElementById('profileFloor').value.trim(),
      roomNumber: document.getElementById('profileRoomNumber').value.trim(),
      phoneNumber: document.getElementById('profilePhoneNumber').value.trim()
    };
    try {
      await apiRequest('/api/students/me/profile', {
        method: 'PUT',
        headers: setAuthHeader(),
        body: JSON.stringify(payload)
      });
      await loadStudentProfile();
    } catch (error) {
      setMessage(document.getElementById('profileMessage'), error.message, 'error');
    }
  });
  if (canCancel) {
    document.getElementById('cancelProfileEdit').addEventListener('click', () => {
      renderStudentProfileDetails(profile, container);
    });
  }
}

async function loadAdminDashboard() {
  try {
    const complaints = await apiRequest('/api/admin/complaints', {
      headers: setAuthHeader()
    });
    renderComplaintCards(complaints, 'adminList', true);
  } catch (error) {
    document.getElementById('adminList').innerHTML = `<div class="empty-state">${error.message}</div>`;
  }
}

function renderComplaintCards(items, containerId, isAdmin = false) {
  const container = document.getElementById(containerId);

  if (!items || items.length === 0) {
    container.innerHTML = '<div class="empty-state">No complaints found.</div>';
    return;
  }

  container.innerHTML = items.map(item => {
    const statusClass = item.status === 'OPEN'
      ? 'status-open'
      : item.status === 'IN_PROGRESS'
        ? 'status-in-progress'
        : 'status-resolved';

    const overdue = item.overdue ? 'status-overdue' : '';
    const badgeText = item.overdue ? 'OVERDUE' : item.status;

    const controls = isAdmin ? `
      <div class="admin-actions">
        <button class="action-btn open" data-action="OPEN" data-id="${item.id}">Open</button>
        <button class="action-btn progress" data-action="IN_PROGRESS" data-id="${item.id}">In Progress</button>
        <button class="action-btn resolve" data-action="RESOLVED" data-id="${item.id}">Resolved</button>
      </div>
    ` : '';

    return `
      <article class="complaint-card">
        <div class="complaint-top">
          <span class="badge-pill ${statusClass}">${item.status}</span>
          ${item.overdue ? `<span class="badge-pill ${overdue}">${badgeText}</span>` : ''}
        </div>
        <div class="meta">${escapeHtml(item.category)} • ${escapeHtml(item.hostelBlock || 'Block not provided')} • Floor ${escapeHtml(item.floor || 'N/A')} • Room ${escapeHtml(item.roomNumber)}</div>
        <h4>${escapeHtml(item.title || item.category)}</h4>
        <div class="meta">${escapeHtml(item.priority)} priority${item.landmark ? ` • Near ${escapeHtml(item.landmark)}` : ''}</div>
        <p>${escapeHtml(item.description)}</p>
        <div class="meta">Reported by: ${escapeHtml(item.userName || 'Resident')}</div>
        ${controls}
      </article>
    `;
  }).join('');

  if (isAdmin) {
    document.querySelectorAll('.action-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        const status = btn.dataset.action;
        const id = btn.dataset.id;
        try {
          await apiRequest(`/api/admin/complaints/${id}/status`, {
            method: 'PUT',
            headers: setAuthHeader(),
            body: JSON.stringify({ status })
          });
          loadAdminDashboard();
        } catch (error) {
          setMessage(authMessage, error.message, 'error');
        }
      });
    });
  }
}

loginForm?.addEventListener('submit', async (event) => {
  event.preventDefault();
  const email = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value.trim();

  try {
    const result = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });

    state.user = { ...result, password };
    localStorage.setItem('complaintboxUser', JSON.stringify(state.user));
    updateUserUI();
    state.activeView = state.user.role === 'ADMIN' ? 'dashboard' : 'profile';
    renderView(state.activeView);
    setMessage(authMessage, `${result.message} Welcome ${result.name}!`, 'success');
  } catch (error) {
    setMessage(authMessage, error.message, 'error');
  }
});

complaintForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const payload = {
    category: document.getElementById('complaintCategory').value,
    title: document.getElementById('complaintTitle').value.trim(),
    hostelBlock: document.getElementById('complaintBlock').value.trim(),
    floor: document.getElementById('complaintFloor').value.trim(),
    roomNumber: document.getElementById('complaintRoom').value.trim(),
    landmark: document.getElementById('complaintLandmark').value.trim() || null,
    description: document.getElementById('complaintDescription').value.trim(),
    priority: document.getElementById('complaintPriority').value
  };

  try {
    await apiRequest('/api/complaints', {
      method: 'POST',
      headers: setAuthHeader(),
      body: JSON.stringify(payload)
    });
    complaintForm.reset();
    setMessage(authMessage, 'Complaint submitted successfully!', 'success');
    renderView('complaints');
  } catch (error) {
    setMessage(authMessage, error.message, 'error');
  }
});

navButtons.forEach((btn) => {
  btn.addEventListener('click', () => {
    const view = btn.dataset.view;
    renderView(view);
  });
});

document.getElementById('logoutBtn').addEventListener('click', () => {
  localStorage.removeItem('complaintboxUser');
  state.user = null;
  updateUserUI();
});

document.getElementById('refreshComplaintsBtn').addEventListener('click', loadMyComplaints);
document.getElementById('refreshAdminBtn').addEventListener('click', loadAdminDashboard);

if (state.user) {
  updateUserUI();
  renderView(state.user.role === 'ADMIN' ? 'dashboard' : 'profile');
} else {
  updateUserUI();
}

