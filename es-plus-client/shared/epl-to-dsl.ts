import { parseEpl, type EplMethodCall } from "./epl-parser.ts";

export interface EsDsl {
  query?: Record<string, unknown>;
  sort?: Record<string, string | { order: string }>[];
  size?: number;
  from?: number;
  _source?: string[];
  aggs?: Record<string, unknown>;
}

export function eplToDsl(epl: string): EsDsl {
  const dsl: EsDsl = {};
  const conditions: Record<string, unknown>[] = [];
  const calls = parseEpl(epl);

  for (const call of calls) {
    applyCall(call, conditions, dsl);
  }

  if (conditions.length === 0) {
    dsl.query = { match_all: {} };
  } else if (conditions.length === 1) {
    dsl.query = conditions[0];
  } else {
    dsl.query = { bool: { must: conditions } };
  }
  return dsl;
}

function applyCall(call: EplMethodCall, conditions: Record<string, unknown>[], dsl: EsDsl): void {
  const p = call.parameters;
  const field = typeof p[0] === "string" ? p[0] : undefined;

  if (call.name === "root" || call.name === "chainQuery") return;

  if (["term", "match", "wildcard"].includes(call.name) && field && p.length > 1) {
    conditions.push({ [call.name]: { [field]: p[1] } });
    return;
  }
  if (call.name === "terms" && field && p.length > 1 && !dsl.aggs) {
    const values = p.slice(1).flatMap((item) => Array.isArray(item) ? item : [item]);
    conditions.push({ terms: { [field]: values } });
    return;
  }
  if (["ge", "le", "gt", "lt"].includes(call.name) && field && p.length > 1) {
    const operator = { ge: "gte", le: "lte", gt: "gt", lt: "lt" }[call.name];
    conditions.push({ range: { [field]: { [operator]: p[1] } } });
    return;
  }
  if (call.name === "range" && field && p.length > 2) {
    conditions.push({ range: { [field]: { gte: p[1], lte: p[2] } } });
    return;
  }
  if (["sortBy", "sortByAsc", "sortByDesc"].includes(call.name) && field) {
    const sortField = call.name === "sortBy" ? String(p[1] ?? "") : field;
    const direction = call.name === "sortByDesc" || String(p[0]).toLowerCase() === "desc" ? "desc" : "asc";
    if (sortField) (dsl.sort ??= []).push({ [sortField === "_id" ? "_doc" : sortField]: { order: direction } });
    return;
  }
  if (call.name === "search") {
    if (typeof p[0] === "number") dsl.size = p[0];
    if (typeof p[1] === "number") dsl.from = p[1];
    return;
  }
  if (call.name === "includes" && p.length > 0) {
    dsl._source = p.flatMap((item) => Array.isArray(item) ? item : [item]).map(String);
    return;
  }
  if (call.name === "esAggWrapper") {
    dsl.aggs = {};
    return;
  }
  if (call.name === "terms" && field && dsl.aggs) {
    dsl.aggs[field + "_terms"] = { terms: { field, size: 100 } };
    return;
  }
  if (["sum", "avg", "max", "min"].includes(call.name) && dsl.aggs) {
    const name = String(p[0] ?? "");
    const aggField = String(p[1] ?? "");
    if (name && aggField) dsl.aggs[name] = { [call.name]: { field: aggField } };
  }
}
