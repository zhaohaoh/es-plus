import assert from "node:assert/strict";
import { mkdtempSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";
import { EsHttpClient } from "../src/es/es-client.ts";
import { createEsPlusServer } from "../src/server.ts";
import { ClientStore, type EsClientConfig } from "../src/storage/client-store.ts";

class FakeEsHttpClient extends EsHttpClient {
  readonly requests: Array<{ config: string; path: string; method: string; body: unknown }> = [];
  readonly bulkCalls: Array<{ config: string; operations: unknown[] }> = [];
  private readonly aliasRows = new Set<string>(["orders\u0000orders_current"]);
  private readonly sourceHits = [
    { _id: "1", _source: { title: "one" } },
    { _id: "2", _source: { title: "two" } },
  ];
  async search(_config: EsClientConfig, index: string, dsl: Record<string, unknown>): Promise<unknown> {
    return { index, dsl, hits: { hits: [] } };
  }

  async bulk(_config: EsClientConfig, operations: unknown[]): Promise<unknown> {
    this.bulkCalls.push({ config: _config.unikey, operations });
    return { errors: false, operations };
  }

  async request(_config: EsClientConfig, path: string, init: RequestInit = {}): Promise<unknown> {
    const method = init.method ?? "GET";
    const body = parseBody(init.body);
    this.requests.push({ config: _config.unikey, path, method, body });
    if (path.startsWith("/_cat/indices/")) return [{ index: "orders", health: "green" }];
    if (path === "/_cat/aliases?format=json") {
      return [...this.aliasRows].map((row) => {
        const [index, alias] = row.split("\u0000");
        return { index, alias };
      });
    }
    if (path.startsWith("/_alias/") || path.includes("/_alias/")) {
      const match = path.match(/^\/(.+)\/_alias\/([^?]+)$/);
      if (match && method === "PUT") this.aliasRows.add(decodeURIComponent(match[1]) + "\u0000" + decodeURIComponent(match[2]));
      return { acknowledged: true };
    }
    if (path === "/_aliases" && method === "POST") {
      const actions = (body as { actions?: Array<{ remove?: { index?: string; alias?: string } }> })?.actions ?? [];
      for (const action of actions) {
        const remove = action.remove;
        if (remove?.index && remove.alias) this.aliasRows.delete(remove.index + "\u0000" + remove.alias);
      }
      return { acknowledged: true };
    }
    if (path.endsWith("/_mapping") && !init.method) {
      return { orders: { mappings: { properties: { title: { type: "text", fields: { keyword: { type: "keyword" } } } } } } };
    }
    if (path === "/orders") {
      return { orders: { settings: { index: { number_of_shards: "1" } }, mappings: { properties: { title: { type: "keyword" } } }, aliases: {} } };
    }
    if (path === "/orders/_search?scroll=1m") {
      const size = Number((body as { size?: number })?.size ?? this.sourceHits.length);
      return { _scroll_id: "scroll-1", hits: { hits: this.sourceHits.slice(0, size) } };
    }
    if (path === "/_search/scroll" && method === "POST") return { _scroll_id: "scroll-2", hits: { hits: [] } };
    if (path === "/_search/scroll" && method === "DELETE") return { succeeded: true };
    if (path.startsWith("/_reindex")) return { task: "node-1:99" };
    if (path.startsWith("/_tasks/")) return { completed: true, response: { total: 5 } };
    return { path, method: init.method ?? "GET", body: parseBody(init.body) };
  }
}

function parseBody(body: BodyInit | null | undefined): unknown {
  if (typeof body !== "string" || !body) return undefined;
  try {
    return JSON.parse(body);
  } catch {
    return body;
  }
}

async function withServer(run: (baseUrl: string, fake: FakeEsHttpClient) => Promise<void>): Promise<void> {
  const directory = mkdtempSync(join(tmpdir(), "es-plus-node-"));
  const store = new ClientStore(join(directory, "sqlite.db"));
  store.save({ unikey: "local", name: "本地", address: "127.0.0.1:9200" });
  const fake = new FakeEsHttpClient();
  const app = createEsPlusServer({ port: 0, store, esClient: fake, webRoot: join(import.meta.dirname, "fixtures") });
  await app.listen();
  try {
    await run("http://127.0.0.1:" + app.port, fake);
  } finally {
    await app.close();
  }
}

async function json(baseUrl: string, path: string, init: RequestInit = {}): Promise<any> {
  const response = await fetch(baseUrl + path, init);
  const body = await response.json();
  assert.equal(response.status, 200, JSON.stringify(body));
  return body;
}

function esHeaders(token?: string): Record<string, string> {
  return {
    "content-type": "application/json",
    currentEsClient: "local",
    ...(token ? { satoken: token } : {}),
  };
}

test("客户端配置可通过 HTTP 保存 查询和删除", async () => withServer(async (baseUrl) => {
  const saved = await json(baseUrl, "/es/client/save", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ unikey: "remote", name: "远程", address: "es.example.com:9200" }),
  });
  assert.equal(saved.unikey, "remote");
  const listed = await json(baseUrl, "/es/client/list", { method: "POST" });
  assert.deepEqual(listed.map((item: EsClientConfig) => item.unikey), ["local", "remote"]);
  await json(baseUrl, "/es/client/delete?id=" + saved.id, { method: "DELETE" });
  assert.equal((await json(baseUrl, "/es/client/list", { method: "POST" })).length, 1);
}));

test("查询接口将 EPL SQL DSL 和批量操作转换为 ES 请求", async () => withServer(async (baseUrl) => {
  const headers = esHeaders();
  const epl = await json(baseUrl, "/es/esQuery/epl?epl=" + encodeURIComponent('Es.chainQuery().index("orders").term("status", "paid").search(10)'), { headers });
  assert.deepEqual(epl.dsl, { query: { term: { status: "paid" } }, size: 10 });
  const sql = await json(baseUrl, "/es/esQuery/sql?sql=" + encodeURIComponent("SELECT * FROM orders WHERE amount >= 10 LIMIT 3"), { headers });
  assert.equal(sql.dsl.size, 3);
  const converted = await json(baseUrl, "/es/esQuery/sql2Dsl?sql=" + encodeURIComponent("SELECT SUM(amount) FROM orders"), { headers });
  assert.deepEqual(converted.aggs, { sum_amount: { sum: { field: "amount" } } });
  const dsl = await json(baseUrl, "/es/esQuery/dsl", { method: "POST", headers, body: JSON.stringify({ index: "orders", dsl: "{\"query\":{\"match_all\":{}}}" }) });
  assert.equal(dsl.index, "orders");
  const dslWithIndex = await json(baseUrl, "/es/esQuery/dsl", {
    method: "POST", headers,
    body: JSON.stringify({ dsl: JSON.stringify({ index: "orders", query: { match_all: {} }, sort: [{ id: "desc" }], size: 10 }) }),
  });
  assert.equal(dslWithIndex.index, "orders");
  assert.deepEqual(dslWithIndex.dsl, { query: { match_all: {} }, sort: [{ id: "desc" }], size: 10 });
  const updated = await json(baseUrl, "/es/updateBatch", { method: "PUT", headers, body: JSON.stringify({ index: "orders", datas: [{ _id: "1", title: "新标题" }] }) });
  assert.equal(updated.operations.length, 2);
  const deleted = await json(baseUrl, "/es/deleteByIds", { method: "DELETE", headers, body: JSON.stringify({ index: "orders", datas: [{ _id: "1" }] }) });
  assert.deepEqual(deleted.operations, [{ delete: { _index: "orders", _id: "1" } }]);
}));

test("索引接口覆盖 Web 使用的查询和管理能力", async () => withServer(async (baseUrl, fake) => {
  const login = await json(baseUrl, "/user/doLogin", {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: "username=admin&password=es123456",
  });
  const headers = esHeaders(login.tokenValue);
  const list = await json(baseUrl, "/es/index/list?keyword=ord", { headers });
  assert.deepEqual(list.indices, ["orders"]);
  assert.deepEqual(list.aliases, { orders: ["orders_current"] });
  assert.deepEqual(list.flatMappings.orders, { title: "text", "title.keyword": "keyword" });
  const detail = await json(baseUrl, "/es/index/getIndex?index=orders", { headers });
  assert.equal(detail.settingsObj["index.number_of_shards"], "1");
  const created = await json(baseUrl, "/es/index/createIndex", {
    method: "POST", headers,
    body: JSON.stringify({
      indexName: "orders_v2",
      alias: "orders_new",
      esSettings: { number_of_shards: 1 },
      mapping: { mappings: { properties: { title: { type: "keyword" } } } },
    }),
  });
  assert.equal(created.path, "/orders_v2");
  assert.deepEqual(created.body.mappings, { properties: { title: { type: "keyword" } } });
  const formHeaders = { ...headers, "content-type": "application/x-www-form-urlencoded" };
  const alias = await json(baseUrl, "/es/index/createAlias", { method: "POST", headers: formHeaders, body: "index=orders&alias=current" });
  assert.equal(alias.acknowledged, true);
  assert.equal(fake.requests.some((item) => item.path === "/orders/_alias/current" && item.method === "PUT"), true);
  assert.equal((await json(baseUrl, "/es/index/getIndices?keyword=ord", { headers }))[0].alias, "orders_current,current");
  const removed = await json(baseUrl, "/es/index/removeAlias", { method: "POST", headers: formHeaders, body: "index=orders&alias=current" });
  assert.equal(removed.acknowledged, true);
  assert.equal(fake.requests.some((item) => item.path === "/_aliases" && item.method === "POST" &&
    JSON.stringify(item.body) === JSON.stringify({ actions: [{ remove: { index: "orders", alias: "current" } }] })), true);
  assert.equal((await json(baseUrl, "/es/index/getIndices?keyword=ord", { headers }))[0].alias, "orders_current");
  const reindex = await json(baseUrl, "/es/index/reindex", { method: "POST", headers, body: JSON.stringify({ sourceIndex: "orders", targetIndex: "orders_v2" }) });
  assert.equal(reindex, "node-1:99");
  const reindexTasks = await json(baseUrl, "/es/index/reindexTaskList?sourceIndex=orders", { headers });
  assert.equal(reindexTasks[0].taskJson, JSON.stringify({ total: 5 }));
  assert.equal(reindexTasks[0].completed, true);

  // 其余 Web 索引接口逐一通过公开 HTTP 边界验证
  assert.equal(Array.isArray(await json(baseUrl, "/es/index/getIndexStat", { headers })), true);
  assert.equal((await json(baseUrl, "/es/index/getIndexHealth", { method: "POST", headers })).path, "/_cluster/health?level=indices");
  assert.equal(Array.isArray(await json(baseUrl, "/es/index/getIndices?keyword=ord", { headers })), true);
  assert.ok(await json(baseUrl, "/es/index/getMapping?indexName=orders", { headers }));
  const mappingUpdated = await json(baseUrl, "/es/index/putMapping", {
    method: "POST", headers: formHeaders,
    body: "indexName=orders&mappings=%7B%22mappings%22%3A%7B%22properties%22%3A%7B%22title%22%3A%7B%22type%22%3A%22keyword%22%7D%7D%7D%7D",
  });
  assert.deepEqual(mappingUpdated, {
    path: "/orders/_mapping",
    method: "PUT",
    body: { properties: { title: { type: "keyword" } } },
  });
  assert.equal((await json(baseUrl, "/es/index/updateSettings", {
    method: "POST", headers: formHeaders, body: "indexName=orders&settings=%7B%22number_of_replicas%22%3A0%7D",
  })).method, "PUT");
  assert.equal((await json(baseUrl, "/es/index/reindexTaskGet?taskId=node-1%3A99", { headers })).completed, true);
  assert.equal((await json(baseUrl, "/es/index/reindexTaskList?sourceIndex=orders", { headers })).length, 1);
  assert.equal((await json(baseUrl, "/es/cluster/getNodes", { headers })).path, "/_nodes");
  assert.equal((await json(baseUrl, "/es/testConnection", { headers })).success, true);
  assert.ok(await json(baseUrl, "/es/indexInfo?index=orders", { headers }));
  assert.equal((await json(baseUrl, "/es/refresh?index=orders", { method: "POST", headers })).method, "POST");

  const remote = await json(baseUrl, "/es/client/save", {
    method: "POST", headers: { "content-type": "application/json" },
    body: JSON.stringify({ unikey: "remote", name: "远程", address: "remote:9200" }),
  });
  assert.equal(remote.unikey, "remote");
  const copied = await json(baseUrl, "/es/index/copyIndex", {
    method: "POST", headers,
    body: JSON.stringify({ targetClient: "remote", sourceIndex: "orders", targetIndex: "orders_copy" }),
  });
  assert.equal(copied.path, "/orders_copy");
  const copyRequest = fake.requests.find((item) => item.config === "remote" && item.path === "/orders_copy" && item.method === "PUT");
  assert.deepEqual(copyRequest?.body, {
    settings: { number_of_shards: "1" },
    mappings: { properties: { title: { type: "keyword" } } },
  });
  const moveTask = await json(baseUrl, "/es/index/indexDataMove", {
    method: "POST", headers,
    body: JSON.stringify({ targetClient: "remote", sourceIndex: "orders", targetIndex: "orders_copy", maxSize: 1 }),
  });
  assert.equal(typeof moveTask, "string");
  const completedMove = await waitFor(async () => {
    const tasks = await json(baseUrl, "/es/index/reindexTaskList?sourceIndex=orders", { headers });
    return tasks.find((task: any) => task.taskId === moveTask && task.completed) ?? null;
  });
  assert.equal(JSON.parse(completedMove.taskJson).status, "completed");
  assert.equal(fake.bulkCalls.some((call) => call.config === "remote"), true);

  const stopTask = await json(baseUrl, "/es/index/indexDataMove", {
    method: "POST", headers,
    body: JSON.stringify({ targetClient: "remote", sourceIndex: "orders", targetIndex: "orders_stopped", maxSize: 2, batchSize: 1, sleepTime: 1000 }),
  });
  const stopResult = await json(baseUrl, "/es/index/indexDataMoveStop", {
    method: "POST", headers, body: JSON.stringify({ taskId: stopTask }),
  });
  assert.equal(stopResult, true);
  const stopped = await waitFor(async () => {
    const tasks = await json(baseUrl, "/es/index/reindexTaskList?sourceIndex=orders", { headers });
    return tasks.find((task: any) => task.taskId === stopTask && task.completed) ?? null;
  });
  assert.equal(JSON.parse(stopped.taskJson).status, "stopped");
  assert.equal((await json(baseUrl, "/es/index/deleteIndex?indexName=orders_copy", { method: "DELETE", headers })).method, "DELETE");
}));

async function waitFor<T>(read: () => Promise<T | null>, timeout = 2000): Promise<T> {
  const deadline = Date.now() + timeout;
  while (Date.now() < deadline) {
    const value = await read();
    if (value) return value;
    await new Promise((resolve) => setTimeout(resolve, 10));
  }
  throw new Error("等待迁移任务状态超时");
}

test("静态页面 Excel 导出和健康检查可直接使用", async () => withServer(async (baseUrl) => {
  assert.equal((await json(baseUrl, "/health")).success, true);
  const home = await fetch(baseUrl + "/");
  assert.match(await home.text(), /ES Plus Test/);
  const excel = await fetch(baseUrl + "/es/file/excelExport", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify([{ id: 1, name: "订单" }]),
  });
  const bytes = new Uint8Array(await excel.arrayBuffer());
  assert.equal(String.fromCharCode(bytes[0], bytes[1]), "PK");
}));
