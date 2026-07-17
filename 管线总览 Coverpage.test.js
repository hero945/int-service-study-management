const assert = require('assert');
const fs = require('fs');
const path = require('path');

const htmlPath = path.join(__dirname, '管线总览 Coverpage.dc.html');
const html = fs.readFileSync(htmlPath, 'utf8');
const scriptMatch = html.match(/<script[^>]*data-dc-script[^>]*>([\s\S]*?)<\/script>/);

assert(scriptMatch, 'business script not found');

class DCLogic {
  constructor(props = {}) {
    this.props = props;
  }

  setState(patch) {
    this.state = {...this.state, ...patch};
  }
}

const Component = new Function(
  'DCLogic',
  'StreamableLogic',
  'React',
  scriptMatch[1] + '; return Component;'
)(DCLogic, DCLogic, {});

function createStorage(initial = {}) {
  const values = {...initial};
  return {
    values,
    getItem(key) {
      return Object.prototype.hasOwnProperty.call(values, key) ? values[key] : null;
    },
    setItem(key, value) {
      values[key] = String(value);
    },
    removeItem(key) {
      delete values[key];
    },
  };
}

function test(name, fn) {
  try {
    fn();
    console.log('PASS ' + name);
  } catch (error) {
    console.error('FAIL ' + name);
    throw error;
  }
}

test('maps configured PreIND and IND statuses to two new pipeline overview columns', () => {
  const component = new Component({});
  component.state.config = [
    {_key: 'P1|S1', ph3tag: 'PreIND'},
    {_key: 'P2|S2', ph3tag: 'IND'},
    {_key: 'P3|S3', ph3tag: 'Phase 1'},
  ];

  assert.deepStrictEqual(
    component.PHASE_TAGS,
    ['PreIND', 'IND', 'Phase 1', 'Phase 2', 'PRE-3', 'Phase 3-1', 'Phase 3-2']
  );
  assert.strictEqual(component.studyPhaseIdx({program: 'P1', study: 'S1', reach: 0}), 0);
  assert.strictEqual(component.studyPhaseIdx({program: 'P2', study: 'S2', reach: 0}), 1);
  assert.strictEqual(component.studyPhaseIdx({program: 'P3', study: 'S3', reach: 0}), 2);
  assert.match(scriptMatch[1], /const stageIndices=\[0,1,2,3,4,5,6\]/);
  assert.match(scriptMatch[1], /ph3Options:self\.PHASE_TAGS/);
  assert.match(html, /<sc-raw-td colspan="11"/);

  const defaultConfig = new Component({}).buildConfig();
  assert.strictEqual(defaultConfig.find(row => row.study === 'HDM2020-001').ph3tag, 'PreIND');
  assert.strictEqual(defaultConfig.find(row => row.study === 'HDM2015-102').ph3tag, 'IND');
});

test('keeps one pipeline overview row per project and backfills earlier phases with a hover explanation', () => {
  assert.match(scriptMatch[1], /const byProject=\{\}/);
  assert.match(scriptMatch[1], /byProject\[r\.project\]/);
  assert.doesNotMatch(scriptMatch[1], /const byCompound=\{\}/);

  const phase1 = new Component({});
  phase1.state.config = [{_key: 'P1|S1', ph3tag: 'Phase 1'}];
  const phase1Study = {program: 'P1', study: 'S1', reach: 2};
  assert.strictEqual(phase1.renderProjectCell([phase1Study], 0, 0).statusText, '已完成');
  assert.strictEqual(phase1.renderProjectCell([phase1Study], 1, 1).statusText, '已完成');

  const laterPhase = new Component({});
  laterPhase.state.config = [{_key: 'P2|S2', ph3tag: 'Phase 2'}];
  const phase2Study = {program: 'P2', study: 'S2', reach: 3};
  assert.strictEqual(laterPhase.renderProjectCell([phase2Study], 0, 0).statusText, '已完成');
  assert.strictEqual(laterPhase.renderProjectCell([phase2Study], 0, 0).backfillText, 'PreIND 实际无项目，由 Phase 2 回填');
  assert.strictEqual(laterPhase.renderProjectCell([phase2Study], 1, 1).statusText, '已完成');
  assert.strictEqual(laterPhase.renderProjectCell([phase2Study], 1, 1).backfillText, 'IND 实际无项目，由 Phase 2 回填');

  const phase3 = new Component({});
  phase3.state.config = [{_key: 'P3|S3', ph3tag: 'Phase 3-1'}];
  const phase3Study = {program: 'P3', study: 'S3', reach: 5};
  assert.strictEqual(phase3.renderProjectCell([phase3Study], 0, 0).statusText, '已完成');
  assert.strictEqual(phase3.renderProjectCell([phase3Study], 0, 0).backfillText, 'PreIND 实际无项目，由 Phase 3-1 回填');
  assert.strictEqual(phase3.renderProjectCell([phase3Study], 1, 1).statusText, '已完成');
  assert.strictEqual(phase3.renderProjectCell([phase3Study], 2, 2).backfillText, 'Phase 1 实际无项目，由 Phase 3-1 回填');

  const backfilledCell = phase3.renderProjectCell([phase3Study], 1, 1);
  backfilledCell.onEnter({clientX: 10, clientY: 20});
  assert.strictEqual(phase3.state.tip.sub, 'IND 实际无项目，由 Phase 3-1 回填');

  const demoRows = new Component({}).buildRaw();
  assert.strictEqual(new Set(demoRows.map(row => row.project)).size, demoRows.length);
});

test('extracts PreIND and IND status from the matching study milestones', () => {
  const component = new Component({});
  component.state.config = [{_key: 'P1|S1', ph3tag: 'IND'}];
  component.state.msEditData = {
    'S1|1-0': {actualStart: '2026-01-01', actualEnd: '2026-01-02'},
    'S1|1-1': {actualStart: '2026-01-03'},
  };
  const indStudy = {program: 'P1', study: 'S1', reach: 1, cur: '已递交', na: [], risk: null};

  assert.strictEqual(component.renderProjectCell([indStudy], 1, 1).statusText, 'IND 形审发补');

  component.MS_GROUPS[1].items.forEach((item, index) => {
    component.state.msEditData['S1|1-' + index] = {actualEnd: '2026-02-01'};
  });
  assert.strictEqual(component.renderProjectCell([indStudy], 1, 1).statusText, '已完成');

  const progressed = {...indStudy, reach: 2};
  assert.strictEqual(component.renderProjectCell([progressed], 1, 1).statusText, '已完成');

  const preInd = new Component({});
  preInd.state.config = [{_key: 'P0|S0', ph3tag: 'PreIND'}];
  preInd.state.msEditData = {
    'S0|0-0': {actualEnd: '2025-12-01'},
    'S0|0-1': {actualStart: '2025-12-02'},
  };
  const preIndStudy = {program: 'P0', study: 'S0', reach: 0, cur: '已递交', na: [], risk: null};
  assert.strictEqual(preInd.renderProjectCell([preIndStudy], 0, 0).statusText, 'PreIND 反馈-临床医学');
});

test('mirrors a Phase 1 study current PreIND or IND milestone into the regulatory columns', () => {
  const component = new Component({});
  component.state.config = [{_key: 'P1|S1', ph3tag: 'Phase 1', pstatus: '进行中'}];

  const indStudy = {program: 'P1', study: 'S1', reach: 1, cur: '已递交', na: [], risk: null};
  assert.strictEqual(component.renderProjectCell([indStudy], 0, 0).statusText, '已完成');
  assert.strictEqual(component.renderProjectCell([indStudy], 1, 1).statusText, 'IND 递交');
  assert.strictEqual(component.renderProjectCell([indStudy], 2, 2).statusText, 'IND 递交');

  const preIndStudy = {...indStudy, reach: 0};
  assert.strictEqual(component.renderProjectCell([preIndStudy], 0, 0).statusText, 'PreIND 递交');
  assert.strictEqual(component.renderProjectCell([preIndStudy], 1, 1).statusText, '—');
  assert.strictEqual(component.renderProjectCell([preIndStudy], 2, 2).statusText, 'PreIND 递交');

  const protocolStudy = {...indStudy, reach: 2};
  assert.strictEqual(component.renderProjectCell([protocolStudy], 0, 0).statusText, '已完成');
  assert.strictEqual(component.renderProjectCell([protocolStudy], 1, 1).statusText, '已完成');
});

test('uses the eastchinapharm.com domain for every demo account', () => {
  const component = new Component({});

  assert.deepStrictEqual(
    component.ROSTER.map(account => account.id),
    [
      'chen@eastchinapharm.com',
      'zhangwei@eastchinapharm.com',
      'lijing@eastchinapharm.com',
      'wangfang@eastchinapharm.com',
      'liuyang@eastchinapharm.com',
    ]
  );
});

test('migrates saved legacy demo accounts and session IDs to the new domain', () => {
  global.localStorage = createStorage({
    'coverpage-accounts-v3': JSON.stringify([
      {id: 'chen@hengrui.com', name: '陈研发', title: '系统管理员', role: 'Admin', pwd: '1234'},
      {id: 'custom@hengrui.com', name: '自建账号', title: '', role: 'Viewer', pwd: '1234'},
    ]),
    'coverpage-auth-v3': JSON.stringify({id: 'chen@hengrui.com'}),
  });
  global.window = {addEventListener() {}, removeEventListener() {}};

  const component = new Component({});
  component.componentDidMount();

  assert.strictEqual(component.state.accounts[0].id, 'chen@eastchinapharm.com');
  assert.strictEqual(component.state.accounts[1].id, 'custom@hengrui.com');
  assert.strictEqual(component.state.auth.id, 'chen@eastchinapharm.com');
});

test('loads, saves, and removes values through semantic storage names', () => {
  global.localStorage = createStorage({
    'coverpage-order-v2': JSON.stringify({Oncology: ['P1']}),
    'coverpage-risks-v2': '{invalid json',
  });
  const component = new Component({});

  assert.deepStrictEqual(component.loadStored('order', {}), {Oncology: ['P1']});
  assert.deepStrictEqual(component.loadStored('risks', []), []);
  assert.strictEqual(component.saveStored('auth', {id: 'viewer@example.com'}), true);
  assert.deepStrictEqual(
    JSON.parse(global.localStorage.values['coverpage-auth-v3']),
    {id: 'viewer@example.com'}
  );
  assert.strictEqual(component.removeStored('auth'), true);
  assert.strictEqual(global.localStorage.getItem('coverpage-auth-v3'), null);
});

test('rejects unknown semantic storage names without touching localStorage', () => {
  global.localStorage = createStorage();
  const component = new Component({});
  const originalWarn = console.warn;
  console.warn = () => {};

  try {
    assert.strictEqual(component.loadStored('unknown', 'fallback'), 'fallback');
    assert.strictEqual(component.saveStored('unknown', {value: 1}), false);
    assert.strictEqual(component.removeStored('unknown'), false);
  } finally {
    console.warn = originalWarn;
  }

  assert.deepStrictEqual(global.localStorage.values, {});
});

test('contains browser storage exceptions inside the storage interface', () => {
  global.localStorage = {
    getItem() { throw new Error('blocked'); },
    setItem() { throw new Error('blocked'); },
    removeItem() { throw new Error('blocked'); },
  };
  const component = new Component({});
  const originalWarn = console.warn;
  console.warn = () => {};

  try {
    assert.strictEqual(component.loadStored('auth', 'fallback'), 'fallback');
    assert.strictEqual(component.saveStored('auth', {id: 'viewer@example.com'}), false);
    assert.strictEqual(component.removeStored('auth'), false);
  } finally {
    console.warn = originalWarn;
  }
});

test('keeps direct localStorage access and persisted keys inside the storage interface', () => {
  const businessScript = scriptMatch[1];
  const directAccesses = businessScript.match(/localStorage\.(?:getItem|setItem|removeItem)/g) || [];
  const persistedKeys = businessScript.match(/['"]coverpage-[a-z-]+-v\d+['"]/g) || [];

  assert.strictEqual(directAccesses.length, 3);
  assert.strictEqual(persistedKeys.length, 9);
});

test('restores role from the account record instead of the saved session', () => {
  global.localStorage = createStorage({
    'coverpage-auth-v3': JSON.stringify({
      id: 'liuyang@eastchinapharm.com',
      name: '任意名称',
      title: '任意职务',
      role: 'Admin',
    }),
  });
  global.window = {addEventListener() {}, removeEventListener() {}};

  const component = new Component({});
  component.componentDidMount();

  assert.strictEqual(component.state.auth.id, 'liuyang@eastchinapharm.com');
  assert.strictEqual(component.state.auth.role, 'Viewer');
  assert.strictEqual(component.state.auth.name, '刘洋');
});

test('rejects monthly report writes from a Viewer', () => {
  const component = new Component({role: 'Viewer'});

  const result = component.setMonthlyText('HDM1005-302', 'RA', '2026-07', '越权写入');

  assert.strictEqual(result, false);
  assert.deepStrictEqual(component.state.monthlyReports, {});
});

test('allows monthly report writes for an assigned Member department', () => {
  const component = new Component({role: 'Member'});
  component._perms = {
    studies: {
      'HDM1005-302': {depts: new Set(['RA'])},
    },
  };

  const result = component.setMonthlyText('HDM1005-302', 'RA', '2026-07', '合规写入');

  assert.strictEqual(result, true);
  assert.strictEqual(
    component.state.monthlyReports['HDM1005-302'].RA['2026-07'],
    '合规写入'
  );
});

test('rejects milestone writes from users who are not study leads', () => {
  const component = new Component({role: 'Viewer'});

  const result = component.setMilestoneEdit('HDM1005-302', 0, 1, 'note', '越权修改');

  assert.strictEqual(result, false);
  assert.strictEqual(component.state.msEditData, undefined);
});

test('allows milestone writes for the study lead', () => {
  const component = new Component({role: 'Member'});
  component._perms = {
    studies: {
      'HDM1005-302': {lead: true, depts: new Set()},
    },
  };

  const result = component.setMilestoneEdit('HDM1005-302', 0, 1, 'note', '负责人修改');

  assert.strictEqual(result, true);
  assert.strictEqual(component.state.msEditData['HDM1005-302|0-1'].note, '负责人修改');
});

test('derives project-wide edit and team-matrix permissions from PL or PM assignments', () => {
  const component = new Component({role: 'Member'});
  component.state.auth = {name: '项目负责人', role: 'Member'};
  component.state.teamData = {
    'S1||PL': '项目负责人',
    'S3||APL': '项目负责人',
  };
  component.effectiveRaw = () => [
    {study: 'S1', project: 'PROJECT-A'},
    {study: 'S2', project: 'PROJECT-A'},
    {study: 'S3', project: 'PROJECT-B'},
    {study: 'S4', project: 'PROJECT-B'},
  ];
  component.teamSeed = () => ({});

  component._perms = component.computePerms();

  assert.deepStrictEqual(component.TEAM_MANAGER_ROLES, ['PL', 'PM']);
  assert.strictEqual(component.canEditStudy('S1'), true);
  assert.strictEqual(component.canEditStudy('S2'), true);
  assert.strictEqual(component.canManageTeam('S1'), true);
  assert.strictEqual(component.canManageTeam('S2'), true);
  assert.strictEqual(component.canEditStudy('S3'), true);
  assert.strictEqual(component.canManageTeam('S3'), false);
  assert.strictEqual(component.canEditStudy('S4'), false);
  assert.strictEqual(component.canManageTeam('S4'), false);
  assert.deepStrictEqual([...component.relatedStudySet()].sort(), ['S1', 'S2', 'S3']);
  assert.strictEqual(component.setTeamMembers('S2', 'RA Manager', '新增成员'), true);
  assert.strictEqual(component.state.teamData['S2||RA Manager'], '新增成员');
  assert.strictEqual(component.setTeamMembers('S3', 'RA Manager', '越权成员'), false);
  assert.strictEqual(component.state.teamData['S3||RA Manager'], undefined);

  const pm = new Component({role: 'Member'});
  pm.state.auth = {name: '项目经理', role: 'Member'};
  pm.state.teamData = {'S1||PM': '项目经理'};
  pm.effectiveRaw = component.effectiveRaw;
  pm.teamSeed = () => ({});
  pm._perms = pm.computePerms();
  assert.strictEqual(pm.canManageTeam('S2'), true);
});

test('keeps project team-matrix editing limited to administrators and assigned PL or PM users', () => {
  const admin = new Component({role: 'Admin'});
  assert.strictEqual(admin.canManageTeam('ANY-STUDY'), true);

  const viewer = new Component({role: 'Viewer'});
  assert.strictEqual(viewer.canManageTeam('ANY-STUDY'), false);
  assert.match(scriptMatch[1], /const te = teamEditOn && self\.canManageTeam\(s\.study\)/);
});

test('keeps risk writes and deletes blocked for a Viewer', () => {
  const component = new Component({role: 'Viewer'});
  const existing = {id: 'RSK-KEEP', program: 'HDM1005-1', study: 'HDM1005-302', func: 'RA'};
  component.state.risks = [existing];
  component.state.riskDraft = {
    ...existing,
    owner: '刘洋',
    desc: '越权修改',
    impact: 5,
    likelihood: 5,
    detectability: 5,
  };

  component.saveRisk();
  assert.deepStrictEqual(component.state.risks, [existing]);

  component.deleteRisk();
  assert.deepStrictEqual(component.state.risks, [existing]);
});

test('builds a complete versioned backup without the active session', () => {
  const component = new Component({role: 'Admin'});
  component.state = {
    ...component.state,
    auth: {id: 'chen@eastchinapharm.com', role: 'Admin'},
    accounts: [{id: 'admin@example.com', name: '管理员', role: 'Admin', pwd: '1234'}],
    risks: [{id: 'RSK-001'}],
    config: [{_key: 'N1'}],
    monthlyReports: {S1: {RA: {'2026-07': '进展'}}},
    teamData: {'S1||RA Manager': '管理员'},
    msEditData: {'0-0': {note: '完成'}},
    order: {Oncology: ['P1']},
    overrides: {P1: {IND: '已受理'}},
  };

  const backup = component.buildBackup();

  assert.strictEqual(backup.schemaVersion, 1);
  assert.match(backup.exportedAt, /^\d{4}-\d{2}-\d{2}T/);
  assert.deepStrictEqual(Object.keys(backup.data).sort(), [
    'accounts',
    'config',
    'monthlyReports',
    'msEditData',
    'order',
    'overrides',
    'risks',
    'teamData',
  ]);
  assert.strictEqual(Object.prototype.hasOwnProperty.call(backup.data, 'auth'), false);
});

test('restores a valid backup and persists every data group', () => {
  global.localStorage = createStorage();
  const component = new Component({role: 'Admin'});
  component.state.auth = {id: 'admin@example.com', role: 'Admin'};
  const data = {
    accounts: [{id: 'admin@example.com', name: '管理员', title: '', role: 'Admin', pwd: '1234'}],
    risks: [{id: 'RSK-007'}],
    config: [{_key: 'N7'}],
    monthlyReports: {S7: {RA: {'2026-07': '恢复内容'}}},
    teamData: {'S7||RA Manager': '管理员'},
    msEditData: {'0-0': {note: '恢复'}},
    order: {Oncology: ['P7']},
    overrides: {P7: {IND: '已受理'}},
  };

  const result = component.restoreBackup({schemaVersion: 1, exportedAt: new Date().toISOString(), data});

  assert.deepStrictEqual(result, {ok: true, error: null});
  assert.deepStrictEqual(component.state.monthlyReports, data.monthlyReports);
  assert.strictEqual(component.state.auth.name, '管理员');
  assert.deepStrictEqual(
    JSON.parse(global.localStorage.values['coverpage-team-v3']),
    data.teamData
  );
  assert.deepStrictEqual(
    JSON.parse(global.localStorage.values['coverpage-order-v2']),
    data.order
  );
});

test('rejects invalid backups without changing current data', () => {
  global.localStorage = createStorage();
  const component = new Component({role: 'Admin'});
  component.state.risks = [{id: 'KEEP'}];

  const result = component.restoreBackup({
    schemaVersion: 2,
    data: {
      accounts: {},
      risks: [],
      config: [],
      monthlyReports: {},
      teamData: {},
      msEditData: {},
      order: {},
      overrides: {},
    },
  });

  assert.strictEqual(result.ok, false);
  assert.match(result.error, /版本/);
  assert.deepStrictEqual(component.state.risks, [{id: 'KEEP'}]);
  assert.deepStrictEqual(global.localStorage.values, {});

  const invalidType = component.restoreBackup({
    schemaVersion: 1,
    data: {
      accounts: {},
      risks: [],
      config: [],
      monthlyReports: {},
      teamData: {},
      msEditData: {},
      order: {},
      overrides: {},
    },
  });

  assert.strictEqual(invalidType.ok, false);
  assert.match(invalidType.error, /列表字段/);
  assert.deepStrictEqual(component.state.risks, [{id: 'KEEP'}]);
  assert.deepStrictEqual(global.localStorage.values, {});
});

test('exposes administrator backup controls and handlers', () => {
  const component = new Component({role: 'Admin'});

  assert.strictEqual(typeof component.downloadBackup, 'function');
  assert.strictEqual(typeof component.importBackupFile, 'function');
  assert.match(html, />导出备份</);
  assert.match(html, />导入备份</);
  assert.match(html, /accept="application\/json,.json"/);
  assert.match(html, /备份文件包含账号数据/);
});
