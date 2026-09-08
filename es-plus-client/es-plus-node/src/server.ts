import { createReadStream, existsSync, statSync } from "node:fs";
import { createServer, type IncomingMessage, type ServerResponse } from "node:http";
import { extname, join, normalize } from "node:path";
import { fileURLToPath } from "node:url";
import { randomUUID } from "node:crypto";
import { sqlToDsl } from "../../shared/index.ts";
import { EsHttpClient } from "./es/es-client.ts";
import { ClientStore, type EsClientConfig } from "./storage/client-store.ts";
import { IndexService } from "./services/index-service.ts";
import { MigrationService } from "./services/migration-service.ts";
import { QueryService } from "./services/query-service.ts";

export interface ServerOptions {
  port?: number;
  store?: ClientStore;
  esClient?: EsHttpClient;
  webRoot?: string;
}

interface RequestContext {
  request: IncomingMessage;
  response: ServerResponse;
  url: URL;
  body: any;
  params: Record<string, string>;
  store: ClientStore;
  http: EsHttpClient;
  query: QueryService;
  index: IndexService;
  migration: MigrationService;
  webRoot: string;
}

const sessions = new Map<string, { userId: number; username: string }>();

export function createEsPlusServer(options: ServerOptions = {}) {
  const store = options.store ?? new ClientStore();
  const http = options.esClient ?? new EsHttpClient();
  const query = new QueryService(http);
  const index = new IndexService(http);
  const migration = new MigrationService(store, http);
  const port = options.port ?? Number(process.env.PORT ?? 8989);
  const webRoot = options.webRoot ?? process.env.WEB_ROOT ??
    fileURLToPath(new URL("../../../es-plus-web/dist/", import.meta.url));

  const server = createServer(async (request, response) => {
    setCorsHeaders(response);
    if (request.method === "OPTIONS") return end(response, 204);

    try {
      const url = new URL(request.url ?? "/", "http://localhost");
      const body = await readBody(request);
      const params = { ...Object.fromEntries(url.searchParams), ...(isObject(body) ? body : {}) };
      const context: RequestContext = { request, response, url, body, params, store, http, query, index, migration, webRoot };
      const result = await route(context);
      if (!response.writableEnded) writeJson(response, 200, result);
    } catch (error) {
      if (!response.writableEnded) {
        const message = error instanceof Error ? error.message : String(error);
        writeJson(response, message === "未登录" ? 401 : 400, { code: message === "未登录" ? "401" : "400", message });
      }
    }
  });

  return {
    server,
    get port() {
      const address = server.address();
      return typeof address === "object" && address ? address.port : port;
    },
    listen: () => new Promise<void>((resolve) => server.listen(port, "127.0.0.1", resolve)),
    close: () => new Promise<void>((resolve, reject) => server.close((error) => {
      if (error) return reject(error);
      store.close();
      resolve();
    })),
  };
}

async function route(context: RequestContext): Promise<unknown> {
  const { request, response, url, params, body, store, http, query, index, migration } = context;
  const method = request.method ?? "GET";
  const path = url.pathname;

  if (path === "/health") return { success: true, service: "es-plus-node" };
  if (path === "/user/doLogin") return login(params);
  if (path === "/user/doLogout") return logout(request);
  if (path === "/es/file/excelExport" && method === "POST") return exportExcel(response, Array.isArray(body) ? body : []);

  if (path === "/es/client/list" && method === "POST") return store.list();
  if (path === "/es/client/save" && method === "POST") return store.save(body as EsClientConfig);
  if (path === "/es/client/get" && method === "GET") return store.getById(requiredNumber(params.id, "id"));
  if (path === "/es/client/delete" && method === "DELETE") {
    store.delete(requiredNumber(params.id, "id"));
    return null;
  }
  if (path === "/es/client/testClient" && method === "POST") {
    try {
      await http.request(body as EsClientConfig, "/");
      return true;
    } catch {
      return false;
    }
  }

  if (!path.startsWith("/es/")) return serveWeb(context);
  const config = requireClient(request, store);

  if (path === "/es/esQuery/epl" && method === "GET") return query.epl(config, required(params.epl, "epl"));
  if (path === "/es/esQuery/sql2Dsl" && method === "GET") return sqlToDsl(required(params.sql, "sql"));
  if (path === "/es/esQuery/sql" && method === "GET") return query.sql(config, required(params.sql, "sql"));
  if (path === "/es/esQuery/explain" && method === "GET") {
    return query.explain(config, required(params.sql, "sql"), params.index);
  }
  if (path === "/es/esQuery/dsl" && method === "POST") {
    const parsed = typeof body.dsl === "string" ? JSON.parse(body.dsl) : body.dsl;
    if (!isObject(parsed)) throw new Error("DSL 必须是 JSON 对象");
    // 兼容页面单独传 index 和用户把 index 写进 DSL JSON 两种格式
    const index = required(body.index ?? parsed.index, "index");
    const { index: _ignored, ...dsl } = parsed;
    return http.search(config, index, dsl);
  }
  if (path === "/es/esQuery/sqlPage" && method === "POST") return query.sql(config, required(body.sql, "sql"));
  if (path === "/es/deleteByIds" && method === "DELETE") {
    return query.deleteBatch(config, required(body.index, "index"), requiredArray(body.datas, "datas"));
  }
  if (path === "/es/updateBatch" && method === "PUT") {
    return query.updateBatch(config, required(body.index, "index"), requiredArray(body.datas, "datas"));
  }
  if (path === "/es/testConnection" && method === "GET") {
    await http.request(config, "/");
    return { success: true, clusterHealth: await http.request(config, "/_cluster/health") };
  }
  if (path === "/es/indexInfo" && method === "GET") return index.getMapping(config, required(params.index, "index"));
  if (path === "/es/refresh" && method === "POST") {
    return http.request(config, "/" + encodeURIComponent(required(params.index, "index")) + "/_refresh", { method: "POST" });
  }

  if (path === "/es/index/list" && method === "GET") return index.list(config, params.keyword ?? "");
  if (path === "/es/index/getIndex" && method === "GET") return index.getIndex(config, required(params.index, "index"));
  if (path === "/es/index/getIndexStat" && method === "GET") return http.request(config, "/_cat/indices/*?format=json&v");
  if (path === "/es/index/getIndexHealth" && (method === "GET" || method === "POST")) {
    return http.request(config, "/_cluster/health?level=indices");
  }
  if (path === "/es/index/getIndices" && method === "GET") return index.details(config, params.keyword ?? "");
  if (path === "/es/index/getMapping" && method === "GET") return index.getMapping(config, required(params.indexName, "indexName"));
  if (path === "/es/index/putMapping" && method === "POST") {
    return http.request(config, "/" + encodeURIComponent(required(params.indexName, "indexName")) + "/_mapping", {
      method: "PUT",
      body: normalizeMappingJson(params.mappings),
    });
  }
  if (path === "/es/index/updateSettings" && method === "POST") {
    return http.request(config, "/" + encodeURIComponent(required(params.indexName, "indexName")) + "/_settings", {
      method: "PUT",
      body: normalizeJson(params.settings),
    });
  }
  if (path === "/es/index/createAlias" && method === "POST") {
    return http.request(config, "/" + encodeURIComponent(required(params.index, "index")) + "/_alias/" +
      encodeURIComponent(required(params.alias, "alias")), { method: "PUT", body: JSON.stringify({}) });
  }
  if (path === "/es/index/removeAlias" && method === "POST") {
    requireAdmin(request);
    return http.request(config, "/_aliases", {
      method: "POST",
      body: JSON.stringify({
        actions: [{ remove: {
          index: required(params.index, "index"),
          alias: required(params.alias, "alias"),
        } }],
      }),
    });
  }
  if (path === "/es/index/createIndex" && method === "POST") return index.createIndex(config, body);
  if (path === "/es/index/deleteIndex" && method === "DELETE") {
    requireAdmin(request);
    return http.request(config, "/" + encodeURIComponent(required(params.indexName, "indexName")), { method: "DELETE" });
  }
  if (path === "/es/index/copyIndex" && method === "POST") {
    const target = store.get(required(body.targetClient, "targetClient"));
    if (!target) throw new Error("找不到目标 ES 客户端配置");
    return index.copyIndex(config, target, required(body.sourceIndex, "sourceIndex"), required(body.targetIndex, "targetIndex"));
  }
  if (path === "/es/index/reindex" && method === "POST") {
    requireAdmin(request);
    return migration.reindex(config, required(body.sourceIndex, "sourceIndex"), required(body.targetIndex, "targetIndex"));
  }
  if (path === "/es/index/reindexTaskList" && method === "GET") {
    return migration.listTasks(config, required(params.sourceIndex, "sourceIndex"));
  }
  if (path === "/es/index/reindexTaskGet" && method === "GET") {
    return migration.taskStatus(config, required(params.taskId, "taskId"));
  }
  if (path === "/es/index/indexDataMove" && method === "POST") {
    return migration.startDataMove({ ...body, sourceClient: config.unikey });
  }
  if (path === "/es/index/indexDataMoveStop" && method === "POST") {
    return migration.stopDataMove(required(body.taskId, "taskId"));
  }
  if (path === "/es/index/refreshIndexCache" && method === "POST") return null;
  if (path === "/es/cluster/getNodes" && method === "GET") return http.request(config, "/_nodes");

  throw new Error("接口不存在: " + method + " " + path);
}

function login(params: Record<string, string>): Record<string, unknown> {
  const username = params.username;
  const password = params.password;
  const adminPassword = process.env.ADMIN_PASSWORD ?? "es123456";
  const userId = username === "admin" && password === adminPassword ? 1 :
    username === "dev" && password === "123456" ? 10001 : 0;
  if (!userId) throw new Error("登录失败");
  const tokenValue = randomUUID();
  sessions.set(tokenValue, { userId, username });
  return { tokenName: "satoken", tokenValue, loginId: String(userId), isLogin: true };
}

function logout(request: IncomingMessage): null {
  const token = request.headers.satoken;
  if (typeof token === "string") sessions.delete(token);
  return null;
}

function requireAdmin(request: IncomingMessage): void {
  const token = request.headers.satoken;
  const session = typeof token === "string" ? sessions.get(token) : undefined;
  if (!session) throw new Error("未登录");
  if (session.userId !== 1) throw new Error("无权操作");
}

function requireClient(request: IncomingMessage, store: ClientStore): EsClientConfig {
  const key = request.headers["currentesclient"];
  if (typeof key !== "string" || !key) throw new Error("缺少 currentEsClient 请求头");
  const config = store.get(key);
  if (!config) throw new Error("找不到 ES 客户端配置: " + key);
  return config;
}

async function readBody(request: IncomingMessage): Promise<unknown> {
  if (request.method === "GET" || request.method === "HEAD") return {};
  const chunks: Buffer[] = [];
  for await (const chunk of request) chunks.push(Buffer.from(chunk));
  const text = Buffer.concat(chunks).toString("utf8");
  if (!text) return {};
  if (request.headers["content-type"]?.includes("application/x-www-form-urlencoded")) {
    return Object.fromEntries(new URLSearchParams(text));
  }
  return JSON.parse(text);
}

async function exportExcel(response: ServerResponse, rows: Record<string, unknown>[]): Promise<void> {
  const { default: ExcelJS } = await import("exceljs");
  const headers = [...new Set(rows.flatMap((row) => Object.keys(row)))];
  const workbook = new ExcelJS.Workbook();
  const worksheet = workbook.addWorksheet("Sheet1");
  worksheet.columns = headers.map((header) => ({ header, key: header, width: Math.max(12, header.length + 2) }));
  for (const row of rows) {
    worksheet.addRow(Object.fromEntries(headers.map((header) => [
      header,
      row[header] !== null && typeof row[header] === "object" ? JSON.stringify(row[header]) : row[header],
    ])));
  }
  const content = Buffer.from(await workbook.xlsx.writeBuffer());
  response.writeHead(200, {
    "content-type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "content-disposition": "attachment; filename=export.xlsx",
    "content-length": content.length,
  });
  response.end(content);
}

function serveWeb(context: RequestContext): Promise<unknown> | unknown {
  const { response, url, webRoot } = context;
  if (!existsSync(webRoot)) throw new Error("前端尚未构建，请先在 es-plus-web 执行 npm run build");
  const requested = url.pathname === "/" ? "index.html" : url.pathname.replace(/^\/+/, "");
  const normalized = normalize(requested).replace(/^(\.\.[/\\])+/, "");
  let file = join(webRoot, normalized);
  if (!existsSync(file) || statSync(file).isDirectory()) file = join(webRoot, "index.html");
  const contentType = mimeType(extname(file));
  response.writeHead(200, { "content-type": contentType });
  return new Promise<void>((resolve, reject) => {
    createReadStream(file).on("error", reject).on("end", resolve).pipe(response);
  });
}

function mimeType(extension: string): string {
  return ({
    ".html": "text/html; charset=utf-8",
    ".js": "text/javascript; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".json": "application/json",
    ".svg": "image/svg+xml",
    ".png": "image/png",
    ".woff2": "font/woff2",
    ".ttf": "font/ttf",
  } as Record<string, string>)[extension] ?? "application/octet-stream";
}

function normalizeJson(value: unknown): string {
  if (typeof value === "string") {
    JSON.parse(value);
    return value;
  }
  return JSON.stringify(value ?? {});
}

function normalizeMappingJson(value: unknown): string {
  const parsed = typeof value === "string" ? JSON.parse(value) : value ?? {};
  if (parsed !== null && typeof parsed === "object" && !Array.isArray(parsed) && "mappings" in parsed) {
    return JSON.stringify((parsed as Record<string, unknown>).mappings ?? {});
  }
  return JSON.stringify(parsed);
}

function required(value: unknown, name: string): string {
  if (value == null || String(value).trim() === "") throw new Error(name + " 不能为空");
  return String(value);
}

function requiredNumber(value: unknown, name: string): number {
  const number = Number(required(value, name));
  if (!Number.isFinite(number)) throw new Error(name + " 必须是数字");
  return number;
}

function requiredArray<T>(value: unknown, name: string): T[] {
  if (!Array.isArray(value) || value.length === 0) throw new Error(name + " 不能为空");
  return value as T[];
}

function isObject(value: unknown): value is Record<string, string> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function setCorsHeaders(response: ServerResponse): void {
  response.setHeader("access-control-allow-origin", "*");
  response.setHeader("access-control-allow-headers", "Content-Type, currentEsClient, satoken, Authorization");
  response.setHeader("access-control-allow-methods", "GET, POST, PUT, DELETE, OPTIONS");
}

function writeJson(response: ServerResponse, status: number, body: unknown): void {
  response.writeHead(status, { "content-type": "application/json; charset=utf-8" });
  response.end(JSON.stringify(body));
}

function end(response: ServerResponse, status: number): void {
  response.writeHead(status);
  response.end();
}
