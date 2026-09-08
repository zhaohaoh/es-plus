import type { EsDsl } from "./epl-to-dsl.ts";

type Scalar = string | number | boolean | null;

interface AggregationSpec {
  functionName: "COUNT" | "SUM" | "AVG" | "MAX" | "MIN" | "TOPHITS";
  field: string;
  name: string;
}

/**
 * 将 Console 支持的 SQL 子集转换为 Elasticsearch Query DSL。
 */
export function sqlToDsl(sql: string): EsDsl & { index: string } {
  const source = sql.trim().replace(/;$/, "");
  const from = source.match(/\bFROM\s+([A-Za-z0-9_.*,-]+)/i);
  if (!from) throw new Error("无法从 SQL 中提取索引名");

  // 1. 提取 SQL 组成部分
  const selectClause = source.match(/\bSELECT\s+(.+?)\s+\bFROM\b/is)?.[1]?.trim() ?? "*";
  const aggregations = parseAggregations(selectClause);
  const groupByFields = parseGroupBy(source);
  const isAggregationQuery = aggregations.length > 0 || groupByFields.length > 0;
  const dsl: EsDsl & { index: string } = { index: from[1] };

  // 2. 普通查询保留返回字段
  if (!isAggregationQuery && selectClause !== "*") dsl._source = splitComma(selectClause);

  // 3. 构建查询条件
  const conditions = parseWhere(source);
  dsl.query = conditions.length === 0
    ? { match_all: {} }
    : conditions.length === 1
      ? conditions[0]
      : { bool: { must: conditions } };

  // 4. 构建聚合或普通分页排序
  if (isAggregationQuery) {
    dsl.aggs = buildAggregations(groupByFields, aggregations);
    dsl.size = 0;
    return dsl;
  }
  const order = source.match(/\bORDER\s+BY\s+([A-Za-z0-9_.-]+)(?:\s+(ASC|DESC))?/i);
  if (order) dsl.sort = [{ [order[1]]: (order[2] ?? "ASC").toLowerCase() }];
  const limit = source.match(/\bLIMIT\s+(\d+)/i);
  dsl.size = limit ? Number.parseInt(limit[1], 10) : 10;
  return dsl;
}

function parseWhere(sql: string): Record<string, unknown>[] {
  const where = sql.match(/\bWHERE\s+(.+?)(?=\s+GROUP\s+BY|\s+ORDER\s+BY|\s+LIMIT|$)/is)?.[1];
  if (!where) return [];
  const conditions: Record<string, unknown>[] = [];
  for (const expression of splitAnd(where)) {
    const inMatch = expression.match(/^([A-Za-z0-9_.-]+)\s+IN\s*\((.*)\)$/is);
    if (inMatch) {
      conditions.push({ terms: { [inMatch[1]]: splitComma(inMatch[2]).map(parseValue) } });
      continue;
    }
    const likeMatch = expression.match(/^([A-Za-z0-9_.-]+)\s+LIKE\s+(.+)$/is);
    if (likeMatch) {
      conditions.push({ wildcard: { [likeMatch[1]]: { value: String(parseValue(likeMatch[2])).replaceAll("%", "*") } } });
      continue;
    }
    const comparison = expression.match(/^([A-Za-z0-9_.-]+)\s*(>=|<=|!=|<>|=|>|<)\s*(.+)$/is);
    if (!comparison) throw new Error("不支持的 WHERE 条件: " + expression);
    const [, field, operator, rawValue] = comparison;
    const value = parseValue(rawValue);
    if (operator === "=") conditions.push({ term: { [field]: value } });
    else if (operator === "!=" || operator === "<>") conditions.push({ bool: { must_not: [{ term: { [field]: value } }] } });
    else {
      const rangeOperator = ({ ">": "gt", ">=": "gte", "<": "lt", "<=": "lte" } as Record<string, string>)[operator];
      conditions.push({ range: { [field]: { [rangeOperator]: value } } });
    }
  }
  return conditions;
}

function parseAggregations(selectClause: string): AggregationSpec[] {
  const result: AggregationSpec[] = [];
  const expression = /\b(COUNT|SUM|AVG|MAX|MIN|TOPHITS)\s*\(\s*([*A-Za-z0-9_.-]+)\s*\)/gi;
  for (const match of selectClause.matchAll(expression)) {
    const functionName = match[1].toUpperCase() as AggregationSpec["functionName"];
    const field = match[2];
    result.push({ functionName, field, name: functionName.toLowerCase() + "_" + (field === "*" ? "value" : field) });
  }
  return result;
}

function parseGroupBy(sql: string): string[] {
  const match = sql.match(/\bGROUP\s+BY\s+(.+?)(?=\s+ORDER\s+BY|\s+LIMIT|$)/is);
  return match ? splitComma(match[1]) : [];
}

function buildAggregations(groupFields: string[], metrics: AggregationSpec[]): Record<string, unknown> {
  if (groupFields.length === 0) return Object.fromEntries(metrics.map((metric) => [metric.name, buildMetric(metric)]));
  const root: Record<string, unknown> = {};
  let container = root;
  for (let index = 0; index < groupFields.length; index += 1) {
    const field = groupFields[index];
    const aggregation: Record<string, unknown> = { terms: { field, size: 10000 } };
    container["group_by_" + field] = aggregation;
    const children: Record<string, unknown> = {};
    if (index === groupFields.length - 1) {
      for (const metric of metrics) {
        if (metric.functionName !== "COUNT" || metric.field !== "*") children[metric.name] = buildMetric(metric);
      }
    }
    if (index < groupFields.length - 1 || Object.keys(children).length > 0) aggregation.aggs = children;
    container = children;
  }
  return root;
}

function buildMetric(metric: AggregationSpec): Record<string, unknown> {
  if (metric.functionName === "COUNT") return { value_count: { field: metric.field === "*" ? "_id" : metric.field } };
  if (metric.functionName === "TOPHITS") {
    const field = metric.field === "*" ? "_id" : metric.field;
    return { top_hits: { size: 5, _source: { includes: [field] } } };
  }
  return { [metric.functionName.toLowerCase()]: { field: metric.field } };
}

function splitAnd(value: string): string[] {
  const result: string[] = [];
  let start = 0;
  let quote = "";
  for (let index = 0; index < value.length; index += 1) {
    const character = value[index];
    if (quote) {
      if (character === quote && value[index - 1] !== "\\") quote = "";
      continue;
    }
    if (character === "'" || character === '"') {
      quote = character;
      continue;
    }
    const match = value.slice(index).match(/^\s+AND\s+/i);
    if (match) {
      result.push(value.slice(start, index).trim());
      index += match[0].length - 1;
      start = index + 1;
    }
  }
  result.push(value.slice(start).trim());
  return result.filter(Boolean);
}

function splitComma(value: string): string[] {
  const result: string[] = [];
  let start = 0;
  let quote = "";
  let depth = 0;
  for (let index = 0; index < value.length; index += 1) {
    const character = value[index];
    if (quote) {
      if (character === quote && value[index - 1] !== "\\") quote = "";
    } else if (character === "'" || character === '"') quote = character;
    else if (character === "(") depth += 1;
    else if (character === ")") depth -= 1;
    else if (character === "," && depth === 0) {
      result.push(value.slice(start, index).trim());
      start = index + 1;
    }
  }
  result.push(value.slice(start).trim());
  return result.filter(Boolean);
}

function parseValue(value: string): Scalar {
  const text = value.trim();
  if ((text.startsWith("'") && text.endsWith("'")) || (text.startsWith('"') && text.endsWith('"'))) {
    return text.slice(1, -1).replace(/\\(['"])/g, "$1");
  }
  if (/^-?\d+$/.test(text)) return Number.parseInt(text, 10);
  if (/^-?\d+\.\d+$/.test(text)) return Number.parseFloat(text);
  if (/^true$/i.test(text)) return true;
  if (/^false$/i.test(text)) return false;
  if (/^null$/i.test(text)) return null;
  return text;
}
