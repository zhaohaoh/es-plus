import { EsHttpClient } from "../es/es-client.ts";
import type { EsClientConfig } from "../storage/client-store.ts";

export class IndexService {
  private readonly http: EsHttpClient;

  constructor(http: EsHttpClient) {
    this.http = http;
  }

  async list(config: EsClientConfig, keyword = ""): Promise<Record<string, unknown>> {
    const pattern = encodeURIComponent("*" + keyword + "*");
    const indices = await this.http.request(config, "/_cat/indices/" + pattern + "?format=json") as Record<string, unknown>[];
    const names = indices.map((item) => String(item.index));
    const aliases = await this.aliasMap(config, names);
    const flatMappings = await this.flatMappings(config, names);
    return { indices: names, aliases, flatMappings, mappings: {}, settings: {} };
  }

  async details(config: EsClientConfig, keyword = ""): Promise<Record<string, unknown>[]> {
    const pattern = encodeURIComponent("*" + keyword + "*");
    const indices = await this.http.request(config, "/_cat/indices/" + pattern + "?format=json&v") as Record<string, unknown>[];
    const aliases = await this.aliasMap(config, indices.map((item) => String(item.index)));
    return indices.map((item) => ({ ...item, alias: (aliases[String(item.index)] ?? []).join(",") }));
  }

  async getIndex(config: EsClientConfig, index: string): Promise<Record<string, unknown>> {
    const result = await this.http.request(config, "/" + encodeURIComponent(index)) as Record<string, any>;
    const info = result[index] ?? result;
    return {
      mappings: info.mappings,
      settings: info.settings,
      settingsObj: flattenObject(info.settings ?? {}),
      aliases: info.aliases,
    };
  }

  async getMapping(config: EsClientConfig, index: string): Promise<unknown> {
    const result = await this.http.request(config, "/" + encodeURIComponent(index) + "/_mapping") as Record<string, any>;
    return result[index] ?? result;
  }

  async createIndex(config: EsClientConfig, body: Record<string, any>): Promise<unknown> {
    const settings = cleanSettings(body.esSettings ?? body.settings ?? {});
    const mappings = unwrapMappings(body.mapping ?? body.mappings ?? {});
    const request: Record<string, unknown> = { settings, mappings };
    if (body.alias) request.aliases = { [body.alias]: {} };
    return this.http.request(config, "/" + encodeURIComponent(String(body.indexName)), {
      method: "PUT",
      body: JSON.stringify(request),
    });
  }

  async copyIndex(source: EsClientConfig, target: EsClientConfig, sourceIndex: string, targetIndex: string): Promise<unknown> {
    const raw = await this.http.request(source, "/" + encodeURIComponent(sourceIndex)) as Record<string, any>;
    const sourceInfo = raw[sourceIndex];
    if (!sourceInfo) throw new Error("无法获取源索引信息");
    return this.http.request(target, "/" + encodeURIComponent(targetIndex), {
      method: "PUT",
      body: JSON.stringify({
        settings: cleanSettings(sourceInfo.settings?.index ?? sourceInfo.settings ?? {}),
        mappings: sourceInfo.mappings ?? {},
      }),
    });
  }

  private async aliasMap(config: EsClientConfig, indices: string[]): Promise<Record<string, string[]>> {
    const result: Record<string, string[]> = {};
    if (indices.length === 0) return result;
    try {
      // 与 Java 端保持一致 查询全部别名后按当前索引过滤
      const aliases = await this.http.request(config, "/_cat/aliases?format=json") as Record<string, unknown>[];
      const indexSet = new Set(indices);
      for (const item of aliases) {
        const index = String(item.index);
        if (!indexSet.has(index)) continue;
        (result[index] ??= []).push(String(item.alias));
      }
    } catch {
      // 没有别名时 Elasticsearch 可能返回 404。
    }
    return result;
  }

  private async flatMappings(config: EsClientConfig, indices: string[]): Promise<Record<string, Record<string, string>>> {
    const result: Record<string, Record<string, string>> = {};
    if (indices.length === 0) return result;
    const mappings = await this.http.request(
      config,
      "/" + encodeURIComponent(indices.join(",")) + "/_mapping"
    ) as Record<string, any>;
    for (const index of indices) {
      result[index] = {};
      flattenProperties(mappings[index]?.mappings?.properties ?? {}, "", result[index]);
    }
    return result;
  }
}

function unwrapMappings(value: unknown): Record<string, unknown> {
  const parsed = typeof value === "string" ? JSON.parse(value) : value;
  if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) return {};
  const mapping = parsed as Record<string, unknown>;
  const wrapped = mapping.mappings;
  return wrapped && typeof wrapped === "object" && !Array.isArray(wrapped)
    ? wrapped as Record<string, unknown>
    : mapping;
}

function flattenProperties(properties: Record<string, any>, prefix: string, output: Record<string, string>): void {
  for (const [name, definition] of Object.entries(properties)) {
    const path = prefix ? prefix + "." + name : name;
    if (definition.type) output[path] = definition.type;
    if (definition.properties) flattenProperties(definition.properties, path, output);
    if (definition.fields) flattenProperties(definition.fields, path, output);
  }
}

function cleanSettings(settings: Record<string, any>): Record<string, unknown> {
  const excluded = new Set(["creation_date", "uuid", "provided_name", "version", "indexName", "alias"]);
  return Object.fromEntries(Object.entries(settings).filter(([key, value]) =>
    !excluded.has(key) && !key.startsWith("version.") && value !== undefined && value !== ""
  ));
}

function flattenObject(value: Record<string, any>, prefix = "", output: Record<string, unknown> = {}): Record<string, unknown> {
  for (const [key, item] of Object.entries(value)) {
    const path = prefix ? prefix + "." + key : key;
    if (item && typeof item === "object" && !Array.isArray(item)) flattenObject(item, path, output);
    else output[path] = item;
  }
  return output;
}
