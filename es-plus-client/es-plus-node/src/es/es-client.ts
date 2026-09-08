import type { EsDsl } from "../../../shared/epl-to-dsl.ts";
import type { EsClientConfig } from "../storage/client-store.ts";

export class EsHttpClient {
  async search(config: EsClientConfig, index: string, dsl: EsDsl): Promise<unknown> {
    return this.request(config, "/" + encodeURIComponent(index) + "/_search", {
      method: "POST",
      body: JSON.stringify(dsl),
    });
  }

  async request(config: EsClientConfig, path: string, init: RequestInit = {}): Promise<unknown> {
    const baseAddress = config.address.split(",")[0].trim().replace(/\/+$/, "");
    const scheme = config.schema ?? "http";
    const url = /^https?:\/\//i.test(baseAddress) ? baseAddress : scheme + "://" + baseAddress;
    const headers = new Headers(init.headers);
    if (!headers.has("content-type")) headers.set("content-type", "application/json");
    if (config.username) {
      headers.set("authorization", "Basic " + Buffer.from(config.username + ":" + (config.password ?? "")).toString("base64"));
    }
    const response = await fetch(url + path, { ...init, headers });
    const text = await response.text();
    let body: unknown = text;
    try {
      body = JSON.parse(text);
    } catch {
      // ES 可能返回纯文本错误，保留原始响应。
    }
    if (!response.ok) throw new Error("Elasticsearch 请求失败: HTTP " + response.status + " " + text);
    return body;
  }

  async text(config: EsClientConfig, path: string, init: RequestInit = {}): Promise<string> {
    const result = await this.request(config, path, init);
    return typeof result === "string" ? result : JSON.stringify(result);
  }

  async bulk(config: EsClientConfig, operations: unknown[]): Promise<unknown> {
    const body = operations.map((operation) => JSON.stringify(operation)).join("\n") + "\n";
    return this.request(config, "/_bulk", {
      method: "POST",
      headers: { "content-type": "application/x-ndjson" },
      body,
    });
  }
}
