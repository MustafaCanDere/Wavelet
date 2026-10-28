// Wavelet desktop: serves the bundled web app from 127.0.0.1 so YouTube's player gets a real http origin.
const { app, BrowserWindow, shell, Menu } = require("electron");
const http = require("http");
const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "web");
const TYPES = { ".html": "text/html; charset=utf-8", ".js": "text/javascript", ".css": "text/css", ".png": "image/png", ".json": "application/json", ".webmanifest": "application/manifest+json", ".svg": "image/svg+xml" };
const PORTS = [47823, 47824, 47825, 47826]; // fixed ports keep the same origin, so playlists and likes survive restarts

function serve(port) {
  return new Promise((resolve, reject) => {
    const srv = http.createServer((req, res) => {
      let p = decodeURIComponent(new URL(req.url, "http://x").pathname);
      if (p === "/") p = "/index.html";
      const file = path.normalize(path.join(ROOT, p));
      if (!file.startsWith(ROOT)) { res.writeHead(403); return res.end(); }
      fs.readFile(file, (err, data) => {
        if (err) { res.writeHead(404); return res.end(); }
        res.writeHead(200, { "Content-Type": TYPES[path.extname(file)] || "application/octet-stream" });
        res.end(data);
      });
    });
    srv.once("error", reject);
    srv.listen(port, "127.0.0.1", () => resolve(port));
  });
}

async function start() {
  let port = null;
  for (const p of PORTS) { try { port = await serve(p); break; } catch (e) {} }
  if (!port) port = await serve(0);
  Menu.setApplicationMenu(null);
  const win = new BrowserWindow({
    width: 1280, height: 820, minWidth: 380, minHeight: 560,
    backgroundColor: "#070b18", title: "Wavelet", autoHideMenuBar: true,
    icon: path.join(ROOT, "icon-512.png"),
    webPreferences: { contextIsolation: true, sandbox: true }
  });
  win.webContents.setUserAgent(win.webContents.getUserAgent() + " BelesifyApp");
  const own = u => u.startsWith(`http://127.0.0.1:${port}/`);
  win.webContents.setWindowOpenHandler(({ url }) => { shell.openExternal(url); return { action: "deny" }; });
  win.webContents.on("will-navigate", (e, url) => { if (!own(url)) { e.preventDefault(); shell.openExternal(url); } });
  win.loadURL(`http://127.0.0.1:${port}/`);
}

if (!app.requestSingleInstanceLock()) app.quit();
else {
  app.on("second-instance", () => { const w = BrowserWindow.getAllWindows()[0]; if (w) { if (w.isMinimized()) w.restore(); w.focus(); } });
  app.whenReady().then(start);
  app.on("window-all-closed", () => app.quit());
}
