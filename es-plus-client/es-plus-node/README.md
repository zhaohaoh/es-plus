# es-plus-node

用于替代 `es-plus-console` 的 Node.js 22 本地服务，默认监听 `127.0.0.1:8989`，不依赖 Java。

## 命令

| 命令 | 用途 |
| --- | --- |
| `npm run dev` | 直接运行 TypeScript 源码 |
| `npm test` | 执行转换和 HTTP 集成测试 |
| `npm run build` | 生成 `dist/app.js` 和供 Electron 使用的 `dist/server.js` |
| `npm start` | 启动构建后的服务 |

通过 `PORT` 设置端口，通过 `WEB_ROOT` 设置 Vue 静态文件目录。数据库默认使用 `%USERPROFILE%\.es-plus-console\sqlite.db`。

公开 API 与 `es-plus-web/src/api` 使用的 Console 接口保持兼容，测试通过 HTTP 边界验证 SQLite、ES 请求转换、索引管理、迁移任务和 Excel 导出。
