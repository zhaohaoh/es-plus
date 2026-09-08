import { createEsPlusServer } from "./server.ts";

const app = createEsPlusServer();
await app.listen();
console.info("ES-Plus Node 服务已启动: http://127.0.0.1:" + app.port);

process.on("SIGINT", async () => {
  await app.close();
  process.exit(0);
});
