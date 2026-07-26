const fs = require('fs');
const path = require('path');

const root = __dirname;
const sourcePath = path.join(root, '临床研发管线管理系统_PRD_v1.0.md');
const outputPath = path.join(root, '临床研发管线管理系统_PRD_v1.0.html');
const markdown = fs.readFileSync(sourcePath, 'utf8').replace(/\r\n/g, '\n');
const versionMatch = markdown.match(/^> 文档版本：([^\n]+)$/m);
if (!versionMatch) {
  throw new Error('PRD source must declare "> 文档版本：..." metadata.');
}
const documentVersion = versionMatch[1].trim();

const escapeHtml = (value) => value
  .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;').replace(/'/g, '&#39;');

const slugCounts = new Map();
const slugify = (text) => {
  const base = text.replace(/[`*_]/g, '').replace(/[^\p{L}\p{N}]+/gu, '-').replace(/^-|-$/g, '').toLowerCase() || 'section';
  const count = slugCounts.get(base) || 0;
  slugCounts.set(base, count + 1);
  return count ? `${base}-${count + 1}` : base;
};

const inline = (raw) => {
  const tokens = [];
  let value = raw.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (_, alt, src) => {
    const token = `@@TOKEN${tokens.length}@@`;
    tokens.push(`<figure class="doc-image"><button type="button" class="image-button" data-image="${escapeHtml(src)}" aria-label="放大查看：${escapeHtml(alt)}"><img src="${escapeHtml(src)}" alt="${escapeHtml(alt)}" loading="lazy"></button><figcaption>${escapeHtml(alt)}</figcaption></figure>`);
    return token;
  });
  value = escapeHtml(value)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');
  tokens.forEach((token, index) => { value = value.replace(`@@TOKEN${index}@@`, token); });
  return value;
};

const parseMermaid = (code) => {
  const labels = new Map();
  const edges = [];
  const cleanLabel = (text) => text.replace(/^[\[(]+|[\])]+$/g, '').replace(/^"|"$/g, '');
  for (const line of code.split('\n')) {
    const match = line.trim().match(/^([A-Za-z0-9_]+)(\[[^\]]+\]|\([^\)]+\))?\s*-->(?:\|[^|]+\|)?\s*([A-Za-z0-9_]+)(\[[^\]]+\]|\([^\)]+\))?/);
    if (!match) continue;
    const [, from, fromLabel, to, toLabel] = match;
    if (fromLabel) labels.set(from, cleanLabel(fromLabel));
    if (toLabel) labels.set(to, cleanLabel(toLabel));
    edges.push([from, to]);
  }
  if (!edges.length) return `<pre class="code-block"><code>${escapeHtml(code)}</code></pre>`;
  const outDegree = new Map();
  edges.forEach(([from]) => outDegree.set(from, (outDegree.get(from) || 0) + 1));
  const branched = [...outDegree.values()].some((count) => count > 1);
  if (branched) {
    return `<div class="relation-map" aria-label="数据关系图">${edges.map(([from, to]) => `<div class="relation"><span>${escapeHtml(labels.get(from) || from)}</span><b aria-hidden="true">→</b><span>${escapeHtml(labels.get(to) || to)}</span></div>`).join('')}</div>`;
  }
  const order = [];
  edges.forEach(([from, to]) => { if (!order.includes(from)) order.push(from); if (!order.includes(to)) order.push(to); });
  return `<div class="step-flow" aria-label="流程图">${order.map((id, index) => `<div class="flow-node"><i>${String(index + 1).padStart(2, '0')}</i><span>${escapeHtml(labels.get(id) || id)}</span></div>${index < order.length - 1 ? '<b class="flow-arrow" aria-hidden="true">→</b>' : ''}`).join('')}</div>`;
};

const lines = markdown.split('\n');
const toc = [];
const output = [];
let i = 0;
let paragraph = [];

const flushParagraph = () => {
  if (!paragraph.length) return;
  const content = inline(paragraph.join(' '));
  output.push(content.startsWith('<figure') ? content : `<p>${content}</p>`);
  paragraph = [];
};

while (i < lines.length) {
  const line = lines[i];
  if (line.startsWith('>')) {
    flushParagraph();
    const metadata = [];
    while (i < lines.length) {
      const match = lines[i].match(/^>\s?(.*)$/);
      if (!match) break;
      metadata.push(match[1]);
      i += 1;
    }
    output.push(`<div class="source-meta">${metadata.map((item) => `<span>${inline(item)}</span>`).join('')}</div>`);
    continue;
  }
  if (line.startsWith('```')) {
    flushParagraph();
    const language = line.slice(3).trim();
    const code = [];
    i += 1;
    while (i < lines.length && !lines[i].startsWith('```')) code.push(lines[i++]);
    output.push(language === 'mermaid' ? parseMermaid(code.join('\n')) : `<pre class="code-block"><code>${escapeHtml(code.join('\n'))}</code></pre>`);
    i += 1;
    continue;
  }
  const heading = line.match(/^(#{1,4})\s+(.+)$/);
  if (heading) {
    flushParagraph();
    const level = heading[1].length;
    const text = heading[2].trim();
    const id = slugify(text);
    if (level >= 2) toc.push({ level, text, id });
    output.push(`<h${level} id="${id}">${inline(text)}<a class="anchor" href="#${id}" aria-label="链接到本节">#</a></h${level}>`);
    i += 1;
    continue;
  }
  if (line.trim().startsWith('|') && i + 1 < lines.length && /^\s*\|?\s*:?-{3}/.test(lines[i + 1])) {
    flushParagraph();
    const rows = [];
    while (i < lines.length && lines[i].trim().startsWith('|')) {
      rows.push(lines[i].trim().replace(/^\||\|$/g, '').split('|').map((cell) => cell.trim()));
      i += 1;
    }
    const bodyRows = rows.slice(2);
    output.push(`<div class="table-wrap"><table><thead><tr>${rows[0].map((cell) => `<th>${inline(cell)}</th>`).join('')}</tr></thead><tbody>${bodyRows.map((row) => `<tr>${row.map((cell) => `<td>${inline(cell)}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`);
    continue;
  }
  const bullet = line.match(/^\s*[-*]\s+(.+)$/);
  if (bullet) {
    flushParagraph();
    const items = [];
    while (i < lines.length) {
      const match = lines[i].match(/^\s*[-*]\s+(.+)$/);
      if (!match) break;
      items.push(match[1]); i += 1;
    }
    output.push(`<ul>${items.map((item) => `<li>${inline(item)}</li>`).join('')}</ul>`);
    continue;
  }
  const ordered = line.match(/^\s*\d+[.)]\s+(.+)$/);
  if (ordered) {
    flushParagraph();
    const items = [];
    while (i < lines.length) {
      const match = lines[i].match(/^\s*\d+[.)]\s+(.+)$/);
      if (!match) break;
      items.push(match[1]); i += 1;
    }
    output.push(`<ol>${items.map((item) => `<li>${inline(item)}</li>`).join('')}</ol>`);
    continue;
  }
  if (!line.trim()) flushParagraph(); else paragraph.push(line.trim());
  i += 1;
}
flushParagraph();

const titleMatch = markdown.match(/^#\s+(.+)$/m);
const title = titleMatch ? titleMatch[1] : '产品需求文档';
const tocHtml = toc.filter((item) => item.level <= 3).map((item) => `<a href="#${item.id}" class="toc-level-${item.level}">${escapeHtml(item.text)}</a>`).join('');

const html = `<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${escapeHtml(title)}</title>
<style>
:root{--brand:#1e5ed6;--brand-dark:#1747a6;--text:#172033;--muted:#667085;--line:#e4e9f2;--canvas:#f4f6f9;--surface:#fff;--soft:#edf4ff;--green:#168864;--orange:#b8640e;--radius:10px}*{box-sizing:border-box}html{scroll-behavior:smooth;scroll-padding-top:84px}body{margin:0;background:var(--canvas);color:var(--text);font:14px/1.75 "Microsoft YaHei","PingFang SC",Arial,sans-serif}.topbar{position:fixed;z-index:20;inset:0 0 auto;height:64px;display:flex;align-items:center;gap:16px;padding:0 28px;background:rgba(255,255,255,.96);border-bottom:1px solid var(--line);backdrop-filter:blur(12px)}.brand{display:flex;align-items:center;gap:11px;font-weight:800;white-space:nowrap}.brand-mark{width:30px;height:30px;border-radius:8px;display:grid;place-items:center;background:var(--brand);color:#fff}.top-title{color:var(--muted);overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.tools{margin-left:auto;display:flex;gap:8px}.search{width:240px;height:36px;border:1px solid #ccd5e3;border-radius:8px;padding:0 12px;color:var(--text);outline:none}.search:focus{border-color:var(--brand);box-shadow:0 0 0 3px #dce9ff}.tool-btn{height:36px;border:1px solid #ccd5e3;border-radius:8px;background:#fff;color:var(--text);padding:0 13px;cursor:pointer;font-weight:700}.tool-btn:hover{border-color:var(--brand);color:var(--brand)}.layout{display:grid;grid-template-columns:276px minmax(0,1fr);max-width:1540px;margin:0 auto;padding-top:64px}.sidebar{position:sticky;top:64px;height:calc(100vh - 64px);overflow:auto;padding:24px 18px 40px;border-right:1px solid var(--line);background:#f8fafc}.sidebar-label{padding:0 10px 12px;color:#8a94a6;font-size:12px;font-weight:800;letter-spacing:1.4px}.toc a{display:block;padding:7px 10px;border-radius:7px;color:#48546a;text-decoration:none;font-size:13px;line-height:1.4}.toc a:hover,.toc a.active{color:var(--brand);background:#e8f1ff}.toc .toc-level-3{padding-left:24px;color:#68758a;font-size:12px}.document{min-width:0;padding:32px 42px 80px}.paper{max-width:1120px;margin:0 auto;background:#fff;border:1px solid var(--line);border-radius:14px;box-shadow:0 12px 34px rgba(31,52,85,.07);overflow:hidden}.hero{padding:48px 56px 42px;background:linear-gradient(110deg,#fff 55%,#f0f6ff);border-top:6px solid var(--brand);border-bottom:1px solid var(--line)}.hero-kicker{font-size:12px;font-weight:800;letter-spacing:2px;color:var(--brand);text-transform:uppercase}.hero h1{font-size:34px;margin:10px 0 18px;line-height:1.3}.hero-meta{display:flex;flex-wrap:wrap;gap:8px}.hero-meta span{padding:6px 10px;border-radius:99px;background:#e7f0ff;color:var(--brand-dark);font-size:12px;font-weight:700}.content{padding:32px 56px 72px}.content>h1:first-child{display:none}.source-meta{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:7px 18px;margin:0 0 28px;padding:16px 18px;border:1px solid #dce6f4;border-radius:10px;background:#f8fbff;color:#526078;font-size:12px}.source-meta span{min-width:0}.source-meta a{color:var(--brand)}h2{margin:48px 0 18px;padding:0 0 10px;border-bottom:2px solid #dce7f7;font-size:24px;line-height:1.4}h2:first-of-type{margin-top:10px}h3{margin:32px 0 14px;font-size:18px;color:#263a59}h4{margin:24px 0 10px;font-size:15px}.anchor{margin-left:8px;color:#a8b3c4;text-decoration:none;opacity:0}.content :is(h2,h3,h4):hover .anchor{opacity:1}p{margin:10px 0;color:#3f4c61}ul,ol{margin:10px 0 18px;padding-left:24px}li{margin:5px 0;color:#3f4c61}code{padding:2px 5px;border-radius:5px;background:#eef3f8;color:#164c9a;font:12px/1.5 Consolas,monospace}.table-wrap{margin:14px 0 28px;overflow:auto;border:1px solid var(--line);border-radius:10px}table{border-collapse:collapse;width:100%;min-width:720px;font-size:12.5px}th{position:sticky;top:0;padding:11px 12px;background:#edf3fb;color:#263a59;text-align:left;white-space:nowrap}td{padding:10px 12px;border-top:1px solid #e7ebf1;vertical-align:top;color:#465268}tbody tr:nth-child(even){background:#fafbfd}tbody tr:hover{background:#f2f7ff}.doc-image{margin:22px 0 30px;text-align:center}.image-button{display:block;width:100%;padding:0;border:1px solid var(--line);border-radius:10px;background:#fff;overflow:hidden;cursor:zoom-in}.doc-image img{display:block;width:100%;height:auto}.doc-image figcaption{margin-top:8px;color:var(--muted);font-size:12px}.step-flow{display:flex;align-items:center;gap:10px;margin:16px 0 28px;padding:18px;overflow:auto;border:1px solid #dce6f4;border-radius:10px;background:#f7faff}.flow-node{flex:0 0 150px;min-height:70px;padding:11px;border-radius:8px;background:#fff;border:1px solid #dce4ef}.flow-node i{display:block;color:var(--brand);font-size:10px;font-style:normal;font-weight:800}.flow-node span{display:block;margin-top:5px;font-weight:700;line-height:1.35}.flow-arrow{color:#87a9dd}.relation-map{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px;margin:16px 0 28px}.relation{display:grid;grid-template-columns:1fr auto 1fr;align-items:center;gap:8px;padding:11px;border:1px solid #dce6f4;border-radius:8px;background:#f8fbff;font-size:12px}.relation span{padding:6px;background:#fff;border-radius:5px;text-align:center;font-weight:700}.relation b{color:var(--brand)}.code-block{overflow:auto;padding:16px;background:#172033;color:#eaf0f8;border-radius:9px}dialog{width:min(94vw,1500px);max-height:94vh;padding:12px;border:0;border-radius:12px;box-shadow:0 24px 80px #10182855}dialog::backdrop{background:#101828cc}.dialog-close{position:sticky;float:right;top:0;z-index:2;width:36px;height:36px;border:0;border-radius:50%;background:#172033;color:#fff;cursor:pointer}.dialog-image{display:block;max-width:100%;margin:auto}.empty-result{display:none;position:fixed;z-index:30;top:74px;right:28px;padding:10px 14px;border-radius:8px;background:#172033;color:#fff;font-size:12px}@media(max-width:900px){.layout{display:block}.sidebar{display:none}.document{padding:20px 12px 60px}.content,.hero{padding-left:24px;padding-right:24px}.search{width:150px}.top-title{display:none}.relation-map,.source-meta{grid-template-columns:1fr}}@media(max-width:560px){.topbar{padding:0 12px}.brand span:last-child{display:none}.search{width:130px}.document{padding-top:12px}.hero h1{font-size:26px}.content{padding:22px 16px 50px}h2{font-size:21px}.tools .tool-btn{display:none}}@media print{.topbar,.sidebar{display:none}.layout{display:block;padding:0}.document{padding:0}.paper{max-width:none;border:0;box-shadow:none}.content,.hero{padding-left:24px;padding-right:24px}.table-wrap{overflow:visible}h2,h3{break-after:avoid}.doc-image,.table-wrap{break-inside:avoid}}
</style></head><body>
<header class="topbar"><div class="brand"><span class="brand-mark">PRD</span><span>产品需求文档</span></div><div class="top-title">${escapeHtml(title)}</div><div class="tools"><input id="search" class="search" type="search" placeholder="搜索文档，回车定位" aria-label="搜索文档"><button class="tool-btn" id="print" type="button">打印 / PDF</button></div></header>
<div class="layout"><aside class="sidebar"><div class="sidebar-label">DOCUMENT OUTLINE</div><nav class="toc" aria-label="文档目录">${tocHtml}</nav></aside><main class="document"><article class="paper"><header class="hero"><div class="hero-kicker">Clinical R&amp;D Pipeline Management</div><h1>${escapeHtml(title)}</h1><div class="hero-meta"><span>版本 ${escapeHtml(documentVersion)}</span><span>页面功能与字段逻辑</span><span>权限与验收标准</span><span>离线可打开</span></div></header><section class="content">${output.join('\n')}</section></article></main></div>
<div id="empty" class="empty-result" role="status">未找到匹配内容</div><dialog id="viewer"><button class="dialog-close" type="button" aria-label="关闭图片">×</button><img class="dialog-image" alt="放大预览"></dialog>
<script>
const links=[...document.querySelectorAll('.toc a')];const targets=links.map(a=>document.querySelector(a.hash)).filter(Boolean);const observer=new IntersectionObserver(entries=>{const visible=entries.filter(e=>e.isIntersecting).sort((a,b)=>a.boundingClientRect.top-b.boundingClientRect.top)[0];if(!visible)return;links.forEach(a=>a.classList.toggle('active',a.hash==='#'+visible.target.id));},{rootMargin:'-80px 0px -72% 0px'});targets.forEach(t=>observer.observe(t));
const search=document.getElementById('search'),empty=document.getElementById('empty');search.addEventListener('keydown',e=>{if(e.key!=='Enter')return;const q=search.value.trim();const found=q&&window.find(q,false,false,true);empty.style.display=found?'none':'block';if(!found)setTimeout(()=>empty.style.display='none',1800)});document.addEventListener('keydown',e=>{if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='k'){e.preventDefault();search.focus()}});document.getElementById('print').addEventListener('click',()=>window.print());
const viewer=document.getElementById('viewer'),viewerImage=viewer.querySelector('img');document.querySelectorAll('[data-image]').forEach(button=>button.addEventListener('click',()=>{viewerImage.src=button.dataset.image;viewerImage.alt=button.querySelector('img').alt;viewer.showModal()}));viewer.querySelector('button').addEventListener('click',()=>viewer.close());viewer.addEventListener('click',e=>{if(e.target===viewer)viewer.close()});
</script></body></html>`;

fs.writeFileSync(outputPath, html, 'utf8');
console.log(`Generated ${outputPath}`);

// Build-time integrity checks: keep the offline preview self-contained and navigable.
const imageSources = [...html.matchAll(/<img[^>]+src="([^"]+)"/g)].map((match) => match[1]);
const missingImages = imageSources.filter((source) => !fs.existsSync(path.resolve(root, source)));
const ids = new Set([...html.matchAll(/\sid="([^"]+)"/g)].map((match) => match[1]));
const anchorTargets = [...html.matchAll(/href="#([^"]+)"/g)].map((match) => match[1]);
const brokenAnchors = [...new Set(anchorTargets.filter((id) => !ids.has(id)))];
const inlineScripts = [...html.matchAll(/<script>([\s\S]*?)<\/script>/g)].map((match) => match[1]);
inlineScripts.forEach((script) => new Function(script));
if (missingImages.length || brokenAnchors.length) {
  throw new Error(`Integrity check failed: missing images=${missingImages.join(',')}; broken anchors=${brokenAnchors.join(',')}`);
}
console.log(`Verified ${imageSources.length} images, ${anchorTargets.length} anchors and ${inlineScripts.length} script.`);
