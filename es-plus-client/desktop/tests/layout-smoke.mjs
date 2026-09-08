import { app, BrowserWindow } from "electron";
import { dirname, join } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const currentDirectory = dirname(fileURLToPath(import.meta.url));
app.disableHardwareAcceleration();

async function inspectLayout(window, width, height) {
  window.setSize(width, height);
  await new Promise((resolve) => setTimeout(resolve, 300));
  return window.webContents.executeJavaScript(`(() => {
    const root = document.documentElement;
    const body = document.body;
    const main = document.querySelector('.app-main');
    return {
      viewport: [window.innerWidth, window.innerHeight],
      root: [root.clientWidth, root.scrollWidth, root.clientHeight, root.scrollHeight],
      body: [body.clientWidth, body.scrollWidth, body.clientHeight, body.scrollHeight],
      main: main ? [main.clientWidth, main.scrollWidth, main.clientHeight, main.scrollHeight] : null,
    };
  })()`);
}

app.whenReady().then(async () => {
  let server;
  try {
    // 1. 启动真实本地服务并加载查询页面
    const serverFile = join(currentDirectory, "..", "..", "es-plus-node", "dist", "server.js");
    const webRoot = join(currentDirectory, "..", "..", "..", "es-plus-web", "dist");
    const { createEsPlusServer } = await import(pathToFileURL(serverFile).href);
    server = createEsPlusServer({ port: 0, webRoot });
    await server.listen();
    const window = new BrowserWindow({ show: false, width: 1440, height: 900 });
    await window.loadURL("http://127.0.0.1:" + server.port + "/#/search");
    await new Promise((resolve) => setTimeout(resolve, 800));

    // 2. 分别验证常用尺寸和允许的最小窗口尺寸
    const layouts = [
      await inspectLayout(window, 1440, 900),
      await inspectLayout(window, 1100, 700),
    ];
    const overflow = layouts.some((layout) =>
      layout.root[1] > layout.root[0] || layout.root[3] > layout.root[2] ||
      layout.body[1] > layout.body[0] || layout.body[3] > layout.body[2] ||
      layout.main?.[1] > layout.main?.[0] || layout.main?.[3] > layout.main?.[2]
    );
    console.info("ES Plus 布局验收结果: " + JSON.stringify(layouts));
    await server.close();
    app.exit(overflow ? 1 : 0);
  } catch (error) {
    if (server) await server.close();
    console.error("ES Plus 布局验收失败: " + (error instanceof Error ? error.stack : String(error)));
    app.exit(1);
  }
});
