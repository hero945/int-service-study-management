const $ = (selector) => document.querySelector(selector);
let csrf;
let currentUser;
let initialization;

async function refreshCsrf() {
  csrf = await fetchJson('/api/v1/platform/auth/csrf');
}

async function fetchJson(url, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body && !(options.body instanceof URLSearchParams)) headers.set('Content-Type', 'application/json');
  if (csrf && options.method && options.method !== 'GET') headers.set(csrf.headerName, csrf.token);
  const response = await fetch(url, {...options, headers});
  if (response.status === 401) throw new Error('请先登录');
  const contentType = response.headers.get('content-type') || '';
  const responseText = await response.text();
  const data = contentType.includes('json') && responseText ? JSON.parse(responseText) : null;
  if (!response.ok) throw new Error(data?.message || `请求失败（${response.status}）`);
  return data;
}

async function boot() {
  await refreshCsrf();
  try {
    currentUser = await fetchJson('/api/v1/platform/me');
    await showApp();
  } catch {
    $('#loginView').hidden = false;
  }
}

$('#loginForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  $('#loginError').textContent = '';
  const form = new FormData(event.currentTarget);
  try {
    await initialization;
    await refreshCsrf();
    await fetchJson('/api/v1/platform/auth/login', {method: 'POST', body: new URLSearchParams(form)});
    await refreshCsrf();
    currentUser = await fetchJson('/api/v1/platform/me');
    await showApp();
  } catch (error) { $('#loginError').textContent = error.message; }
});

async function showApp() {
  $('#loginView').hidden = true;
  $('#appView').hidden = false;
  $('#displayName').textContent = currentUser.displayName;
  $('#userRole').textContent = currentUser.role === 'ADMIN' ? '平台管理员' : '业务用户';
  $('#avatar').textContent = currentUser.displayName.slice(0, 1);
  $('#adminNav').hidden = currentUser.role !== 'ADMIN';
  const publicSettings = await fetchJson('/api/v1/platform/settings/public');
  const nameSetting = publicSettings.find(item => item.configKey === 'platform.display-name');
  if (nameSetting) { $('#platformName').textContent = nameSetting.configValue; document.title = nameSetting.configValue; }
  await loadPipeline();
}

async function loadPipeline() {
  const [overview, studies] = await Promise.all([
    fetchJson('/api/v1/clinical-pipeline/overview'), fetchJson('/api/v1/clinical-pipeline/studies')
  ]);
  const allMetrics = [{label: '全部项目', count: overview.total, tone: 'neutral'}, ...overview.statuses];
  $('#metrics').innerHTML = allMetrics.map(item => `<article class="metric ${escapeHtml(item.tone)}"><span>${escapeHtml(item.label)}</span><strong>${item.count}</strong></article>`).join('');
  $('#studyCount').textContent = `${studies.length} 项`;
  $('#emptyStudies').hidden = studies.length > 0;
  $('#studyRows').innerHTML = studies.map(study => `<tr><td><strong>${escapeHtml(study.code)}</strong></td><td>${escapeHtml(study.name)}</td><td>${escapeHtml(study.indication)}</td><td>${escapeHtml(study.phase)}</td><td><span class="badge ${escapeHtml(study.statusTone)}">${escapeHtml(study.statusLabel)}</span></td><td>${escapeHtml(study.ownerName)}</td><td>${study.startDate || '—'}</td></tr>`).join('');
}

document.querySelectorAll('.nav-item').forEach(button => button.addEventListener('click', async () => {
  document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
  button.classList.add('active');
  const admin = button.dataset.view === 'admin';
  $('#pipelinePanel').hidden = admin;
  $('#adminPanel').hidden = !admin;
  if (admin) await loadAdmin();
}));

$('#openStudyDialog').addEventListener('click', () => $('#studyDialog').showModal());
$('#openUserDialog').addEventListener('click', () => $('#userDialog').showModal());
document.querySelectorAll('.close-dialog').forEach(button => button.addEventListener('click', () => button.closest('dialog').close()));

$('#studyForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const payload = Object.fromEntries(new FormData(form));
  if (!payload.startDate) payload.startDate = null;
  try {
    await fetchJson('/api/v1/clinical-pipeline/studies', {method: 'POST', body: JSON.stringify(payload)});
    $('#studyDialog').close(); form.reset(); toast('项目已创建'); await loadPipeline();
  } catch (error) { form.querySelector('.form-error').textContent = error.message; }
});

async function loadAdmin() {
  const [settings, users] = await Promise.all([fetchJson('/api/v1/platform/settings'), fetchJson('/api/v1/platform/users')]);
  $('#settingsList').innerHTML = settings.map(setting => `<div class="setting-row"><div><strong>${escapeHtml(setting.configKey)}</strong><small>${escapeHtml(setting.description)}</small></div><input data-setting-key="${escapeHtml(setting.configKey)}" value="${escapeHtml(setting.configValue)}" maxlength="1000" aria-label="${escapeHtml(setting.description)}"></div>`).join('');
  document.querySelectorAll('[data-setting-key]').forEach(input => input.addEventListener('change', () => updateSetting(input)));
  $('#userList').innerHTML = users.map(user => `<div class="user-row-item"><div><strong>${escapeHtml(user.displayName)}</strong><small>${escapeHtml(user.username)}</small></div><span class="role-pill">${user.role === 'ADMIN' ? '管理员' : '普通用户'}</span></div>`).join('');
}

async function updateSetting(input) {
  try {
    await fetchJson(`/api/v1/platform/settings?key=${encodeURIComponent(input.dataset.settingKey)}`, {method: 'PUT', body: JSON.stringify({value: input.value})});
    toast('配置已实时生效');
    if (input.dataset.settingKey === 'platform.display-name') $('#platformName').textContent = input.value;
  } catch (error) { toast(error.message); }
}

$('#userForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const payload = Object.fromEntries(new FormData(form));
  try {
    await fetchJson('/api/v1/platform/users', {method: 'POST', body: JSON.stringify(payload)});
    $('#userDialog').close(); form.reset(); toast('账号已创建'); await loadAdmin();
  } catch (error) { form.querySelector('.form-error').textContent = error.message; }
});

$('#logoutButton').addEventListener('click', async () => {
  await fetchJson('/api/v1/platform/auth/logout', {method: 'POST'});
  window.location.reload();
});

function toast(message) { const node = $('#toast'); node.textContent = message; node.hidden = false; setTimeout(() => node.hidden = true, 2400); }
function escapeHtml(value) { return String(value ?? '').replace(/[&<>'"]/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char])); }

initialization = boot();
