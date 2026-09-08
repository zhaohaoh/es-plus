import { app, BrowserWindow, dialog, Menu } from "electron";
import { dirname, join } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const currentDirectory = dirname(fileURLToPath(import.meta.url));
app.disableHardwareAcceleration();
let mainWindow;
let localServer;
let quitting = false;

async function startLocalServer() {
  // 1. 根据开发或安装环境定位 Node 服务和 Web 资源
  const packaged = app.isPackaged;
  const serverFile = packaged
    ? join(process.resourcesPath, "node", "server.js")
    : join(currentDirectory, "..", "es-plus-node", "dist", "server.js");
  const webRoot = packaged
    ? join(process.resourcesPath, "web")
    : join(currentDirectory, "..", "..", "es-plus-web", "dist");

  // 2. 在随机本地端口启动服务 避免固定端口冲突
  const { createEsPlusServer } = await import(pathToFileURL(serverFile).href);
  // 打包客户端使用固定端口 保证 localStorage 在重启后仍属于同一来源
  localServer = createEsPlusServer({ port: packaged ? 48989 : 0, webRoot });
  await localServer.listen();
  return "http://127.0.0.1:" + localServer.port;
}

async function createMainWindow() {
  try {
    const url = await startLocalServer();
    mainWindow = new BrowserWindow({
      width: 1440,
      height: 900,
      minWidth: 1100,
      minHeight: 700,
      show: false,
      webPreferences: {
        contextIsolation: true,
        nodeIntegration: false,
        sandbox: true,
      },
    });
    mainWindow.once("ready-to-show", () => mainWindow.show());
    const targetUrl = process.env.ES_PLUS_LAYOUT_TEST === "1" ? url + "/#/search" : url;
    await mainWindow.loadURL(targetUrl);
    if (process.env.ES_PLUS_LAYOUT_TEST === "1") {
      const overflow = await validateLayout(mainWindow);
      console.info("ES Plus 布局验收结果 overflow: " + overflow);
      quitting = true;
      await localServer.close();
      app.exit(overflow ? 1 : 0);
      return;
    }
    if (process.env.ES_PLUS_SMOKE_TEST === "1") {
      console.info("ES Plus 启动验收成功 url: " + url);
      setTimeout(() => app.quit(), 300);
    }
  } catch (error) {
    const message = error instanceof Error ? error.stack ?? error.message : String(error);
    dialog.showErrorBox("ES Plus 启动失败", message);
    app.quit();
  }
}

async function validateLayout(window) {
  for (const [width, height] of [[1440, 900], [1100, 700]]) {
    window.setSize(width, height);
    await new Promise((resolve) => setTimeout(resolve, 400));
    const overflow = await window.webContents.executeJavaScript(`(() => {
      const elements = [document.documentElement, document.body, document.querySelector('.app-main')].filter(Boolean);
      return elements.some((element) => element.scrollWidth > element.clientWidth || element.scrollHeight > element.clientHeight);
    })()`);
    if (overflow) return true;
  }
  return false;
}

function installApplicationMenu() {
  const template = [
    {
      label: "文件",
      submenu: [
        { label: "关闭窗口", role: "close" },
        { type: "separator" },
        { label: "退出", role: "quit" },
      ],
    },
    {
      label: "编辑",
      submenu: [
        { label: "撤销", role: "undo" },
        { label: "重做", role: "redo" },
        { type: "separator" },
        { label: "剪切", role: "cut" },
        { label: "复制", role: "copy" },
        { label: "粘贴", role: "paste" },
        { label: "全选", role: "selectAll" },
      ],
    },
    {
      label: "查看",
      submenu: [
        { label: "重新加载", role: "reload" },
        { label: "强制重新加载", role: "forceReload" },
        { label: "开发者工具", role: "toggleDevTools" },
        { type: "separator" },
        { label: "恢复默认缩放", role: "resetZoom" },
        { label: "放大", role: "zoomIn" },
        { label: "缩小", role: "zoomOut" },
        { type: "separator" },
        { label: "全屏", role: "togglefullscreen" },
      ],
    },
    {
      label: "窗口",
      submenu: [
        { label: "最小化", role: "minimize" },
        { label: "缩放窗口", role: "zoom" },
      ],
    },
    {
      label: "帮助",
      submenu: [
        {
          label: "关于 ES Plus",
          click: () => dialog.showMessageBox(mainWindow, {
            type: "info",
            title: "关于 ES Plus",
            message: "ES Plus",
            detail: "Elasticsearch 查询与迁移客户端",
          }),
        },
      ],
    },
  ];
  Menu.setApplicationMenu(Menu.buildFromTemplate(template));
}

app.whenReady().then(async () => {
  installApplicationMenu();
  await createMainWindow();
});

app.on("window-all-closed", () => app.quit());

app.on("before-quit", (event) => {
  if (quitting || !localServer) return;
  event.preventDefault();
  quitting = true;
  void localServer.close().finally(() => app.quit());
});
