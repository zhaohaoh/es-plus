import { eplToDsl, parseEpl, sqlToDsl } from "../../../shared/index.ts";
import { EsHttpClient } from "../es/es-client.ts";
import type { EsClientConfig } from "../storage/client-store.ts";

export class QueryService {
  private readonly http: EsHttpClient;

  constructor(http: EsHttpClient) {
    this.http = http;
  }

  async epl(config: EsClientConfig, epl: string): Promise<unknown> {
    const dsl = eplToDsl(epl);
    const index = parseEpl(epl).find((call) => call.name === "index")?.parameters[0];
    if (typeof index !== "string" || !index) throw new Error("EPL 中未指定索引");
    const result = await this.http.search(config, index, dsl);
    if (dsl.aggs && result && typeof result === "object" && "aggregations" in result) {
      return (result as Record<string, unknown>).aggregations;
    }
    return result;
  }

  async sql(config: EsClientConfig, sql: string): Promise<unknown> {
    const converted = sqlToDsl(/\bLIMIT\b/i.test(sql) ? sql : sql + " LIMIT 100");
    const { index, ...dsl } = converted;
    return this.http.search(config, index, dsl);
  }

  async explain(config: EsClientConfig, query: string, index?: string): Promise<unknown> {
    let targetIndex = index;
    let dsl: Record<string, unknown>;
    if (/^\s*(SELECT|SHOW|DESCRIBE|DESC)\b/i.test(query)) {
      const converted = sqlToDsl(query);
      targetIndex = converted.index;
      const { index: ignored, ...body } = converted;
      dsl = body;
    } else {
      dsl = JSON.parse(query);
    }
    if (!targetIndex) throw new Error("执行计划需要指定索引");
    return this.http.request(config, "/" + encodeURIComponent(targetIndex) + "/_search", {
      method: "POST",
      body: JSON.stringify({ ...dsl, profile: true, size: 0 }),
    });
  }

  async updateBatch(config: EsClientConfig, index: string, documents: Record<string, any>[]): Promise<unknown> {
    const operations: unknown[] = [];
    for (const document of documents) {
      const { _id, _routing, ...source } = document;
      if (!_id) throw new Error("批量更新数据缺少 _id");
      const action: Record<string, unknown> = { _index: index, _id };
      if (_routing) action.routing = _routing;
      operations.push({ index: action }, source);
    }
    return this.http.bulk(config, operations);
  }

  async deleteBatch(config: EsClientConfig, index: string, documents: Record<string, any>[]): Promise<unknown> {
    const operations = documents.map((document) => {
      const action: Record<string, unknown> = { _index: index, _id: document._id };
      if (document._routing) action.routing = document._routing;
      return { delete: action };
    });
    return this.http.bulk(config, operations);
  }
}
