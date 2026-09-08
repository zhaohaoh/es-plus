# ES Plus Client

`es-plus-client` 是不依赖 Java 的客户端父工程。现有 Vue 工程仍保留在上级 `es-plus-web`，构建时由桌面端直接复用。

| 目录 | 职责 | 当前状态 |
| --- | --- | --- |
| `shared` | EPL/SQL 解析与 Elasticsearch DSL 转换 | 已实现 |
| `es-plus-node` | ES HTTP、SQLite、索引管理、查询、迁移和导出服务 | 已实现 |
| `desktop` | Electron Windows 客户端和 NSIS 安装包 | 已实现 |
| `extension` | Chrome 插件 | 暂未创建 不影响 Windows 客户端 |
| `../es-plus-web` | 原 Vue 页面 | 保持原目录 |

## 环境

- Node.js 22
- Windows x64
- SQLite 使用 Node 22 内置的 `node:sqlite`
- 运行和安装 Windows 客户端均不需要 JDK 或 Java 服务

## 开发与测试

```powershell
cd es-plus-client\es-plus-node
npm install
npm test
npm run build
```

```powershell
cd ..\..\es-plus-web
npm install
npm run build
```

```powershell
cd ..\es-plus-client\desktop
npm install
npm start
```

客户端配置和迁移任务继续保存在 `%USERPROFILE%\.es-plus-console\sqlite.db`，可以直接读取原 Console 的 SQLite 数据。

## 构建 Windows 安装包

建议先构建免安装试用版：

```powershell
npm run portable:windows
```

试用产物位于 `desktop/dist/ES-Plus-Portable-0.1.0-x64.exe`，双击即可运行，不写入 Windows 安装目录。确认功能后再构建安装包。

在 `es-plus-client` 执行：

```powershell
npm run dist:windows
```

产物位于：

```text
desktop/dist/ES-Plus-Setup-0.1.0-x64.exe
```

安装包内含 Vue 页面、Electron 和 Node 服务。应用只监听随机的 `127.0.0.1` 本地端口，关闭客户端时会同步关闭本地服务。

## 功能范围

| 模块 | 能力 |
| --- | --- |
| 客户端 | 保存、列表、详情、删除、连接测试、Basic Auth |
| 查询 | EPL、SQL、SQL 转 DSL、DSL、Explain、批量更新、批量删除 |
| 索引 | 列表、详情、Mapping、Settings、Alias、创建、删除、复制 |
| 迁移 | Reindex、任务状态、跨集群 Scroll + Bulk、停止任务 |
| 集群 | 健康状态、节点、刷新、索引统计 |
| 文件 | 真实 XLSX 导出 |

当前 Windows 安装包未配置商业代码签名证书，因此首次运行时 Windows SmartScreen 可能显示发布者未知。

升级正式安装版时直接运行更高版本的安装程序覆盖安装，无需先卸载。应用数据保存在用户目录，不会被安装目录覆盖；发布新版本时应同步递增 `desktop/package.json` 的版本号。
