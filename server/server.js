/**
 * 别睡了官网 - Node 静态服务
 * 提供官网静态页、APK 下载、版本接口
 * 部署到云服务器时请配合 Nginx/IIS 做 HTTPS 与反向代理
 */
const express = require('express');
const path = require('path');
const fs = require('fs');

const app = express();

// 端口：优先环境变量，默认 5615（云服务器部署时可沿用或改环境变量）
const PORT = process.env.PORT || 5615;

// 路径：官网静态文件全部放在 server/public，APK 与版本配置在 server 下
const PUBLIC_DIR = path.join(__dirname, 'public');
const APK_DIR = path.join(__dirname, 'apk');
const VERSION_FILE = path.join(__dirname, 'version.json');

// ---------- 安全：禁用 X-Powered-By 响应头，避免暴露框架信息 ----------
app.disable('x-powered-by');

// ---------- 安全：限制请求体大小，防止大请求攻击 ----------
app.use(express.json({ limit: '1kb' }));
app.use(express.urlencoded({ extended: false, limit: '1kb' }));

// ---------- 安全：通用响应头（降低 XSS、点击劫持等风险） ----------
app.use((req, res, next) => {
  // 基础安全头
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'SAMEORIGIN');
  res.setHeader('X-XSS-Protection', '1; mode=block');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  
  // 内容安全策略：防止 XSS 和数据注入攻击
  res.setHeader('Content-Security-Policy', [
    "default-src 'self'",
    "script-src 'self' 'unsafe-inline'",  // 允许内联脚本（index.html 需要）
    "style-src 'self' 'unsafe-inline'",   // 允许内联样式
    "img-src 'self' data:",               // 允许本地图片和 data URI
    "font-src 'self'",
    "connect-src 'self'",                 // 允许 fetch 请求
    "frame-ancestors 'self'",             // 防止被嵌入 iframe
    "base-uri 'self'",
    "form-action 'self'"
  ].join('; '));
  
  // HTTPS 强制（仅在生产环境启用，需配合 Nginx 反向代理）
  if (process.env.NODE_ENV === 'production') {
    res.setHeader('Strict-Transport-Security', 'max-age=31536000; includeSubDomains');
  }
  
  // 禁止浏览器猜测 MIME 类型
  res.setHeader('X-Download-Options', 'noopen');
  
  // 权限策略：限制浏览器功能访问
  res.setHeader('Permissions-Policy', 'geolocation=(), microphone=(), camera=()');
  
  next();
});

// ---------- 静态：官网（首页即下载页） ----------
app.use(express.static(PUBLIC_DIR));

// ---------- APK 下载：仅允许安全路径，防止路径穿越 ----------
// 仅允许 /apk/xxx.apk 形式，禁止路径中出现 ..
app.use('/apk', (req, res, next) => {
  const decoded = decodeURIComponent(req.path);
  if (decoded.includes('..') || decoded.includes('%2e%2e')) {
    return res.status(400).send('Bad Request');
  }
  next();
});
app.use('/apk', express.static(APK_DIR));

// ---------- 版本接口：GET /api/version，带频率限制（含自动清理） ----------
const versionRateLimit = new Map();
const VERSION_RATE_WINDOW_MS = 60 * 1000;  // 1 分钟窗口
const VERSION_RATE_MAX = 120;               // 每 IP 最多 120 次/分钟
const RATE_LIMIT_CLEANUP_INTERVAL = 5 * 60 * 1000;  // 每 5 分钟清理过期记录

// 定期清理过期的频率限制记录，防止内存泄漏
setInterval(() => {
  const now = Date.now();
  for (const [ip, record] of versionRateLimit) {
    if (now - record.start > VERSION_RATE_WINDOW_MS * 2) {
      versionRateLimit.delete(ip);
    }
  }
}, RATE_LIMIT_CLEANUP_INTERVAL);

app.get('/api/version', (req, res) => {
  // 获取真实 IP（支持反向代理场景）
  const ip = req.headers['x-forwarded-for']?.split(',')[0]?.trim() 
           || req.ip 
           || req.socket?.remoteAddress 
           || 'unknown';
  const now = Date.now();
  let record = versionRateLimit.get(ip);
  if (!record || now - record.start > VERSION_RATE_WINDOW_MS) {
    record = { start: now, count: 0 };
    versionRateLimit.set(ip, record);
  }
  record.count++;
  if (record.count > VERSION_RATE_MAX) {
    res.setHeader('Retry-After', Math.ceil((record.start + VERSION_RATE_WINDOW_MS - now) / 1000));
    return res.status(429).json({ error: 'Too Many Requests' });
  }

  try {
    const raw = fs.readFileSync(VERSION_FILE, 'utf8');
    const data = JSON.parse(raw);
    // 只允许安全文件名，防止通过 version.json 注入路径穿越
    const rawName = (data.apkFileName || '').trim();
    const safeName = /^[a-zA-Z0-9_.-]+$/.test(rawName) ? rawName : '';
    const apkUrl = safeName ? '/apk/' + safeName : '';
    res.json({
      versionName: data.versionName || '',
      versionCode: data.versionCode ?? 0,
      apkFileName: safeName || data.apkFileName || '',
      apkUrl
    });
  } catch (e) {
    // 不暴露具体错误信息
    res.status(500).json({ error: 'version not configured' });
  }
});

// ---------- 未匹配路由：统一 404，避免暴露框架信息 ----------
app.use((req, res) => {
  res.status(404).send('Not Found');
});

app.listen(PORT, () => {
  console.log(`别睡了 官网已启动: http://localhost:${PORT}`);
  console.log(`静态目录: ${PUBLIC_DIR}`);
  console.log(`APK 目录: ${APK_DIR}`);
  console.log(`版本文件: ${VERSION_FILE}`);
});
