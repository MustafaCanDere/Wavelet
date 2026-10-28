// Copies the shared web app (../web) into this folder before running or packaging.
const fs = require("fs"), path = require("path");
const src = path.join(__dirname, "..", "web"), dst = path.join(__dirname, "web");
fs.rmSync(dst, { recursive: true, force: true });
fs.cpSync(src, dst, { recursive: true });
