export interface EplMethodCall {
  name: string;
  parameters: unknown[];
}

function splitTopLevel(value: string, separator: string): string[] {
  const parts: string[] = [];
  let start = 0;
  let depth = 0;
  let quote = "";
  let escaped = false;

  for (let i = 0; i < value.length; i += 1) {
    const char = value[i];
    if (escaped) {
      escaped = false;
      continue;
    }
    if (quote && char === "\\") {
      escaped = true;
      continue;
    }
    if (quote) {
      if (char === quote) quote = "";
      continue;
    }
    if (char === "'" || char === '"') {
      quote = char;
    } else if (char === "(" || char === "[" || char === "{") {
      depth += 1;
    } else if (char === ")" || char === "]" || char === "}") {
      depth -= 1;
    } else if (char === separator && depth === 0) {
      parts.push(value.slice(start, i).trim());
      start = i + 1;
    }
  }
  parts.push(value.slice(start).trim());
  return parts.filter(Boolean);
}

function parseParameter(value: string): unknown {
  const text = value.trim();
  if (!text) return undefined;
  if ((text.startsWith('"') && text.endsWith('"')) || (text.startsWith("'") && text.endsWith("'"))) {
    return text.slice(1, -1).replace(/\\(["'])/g, "$1");
  }
  if (/^-?\d+$/.test(text)) return Number.parseInt(text, 10);
  if (/^-?\d+(?:\.\d+)?$/.test(text)) return Number.parseFloat(text);
  if (text === "true") return true;
  if (text === "false") return false;
  if (text === "null") return null;
  if (text.startsWith("[") && text.endsWith("]")) {
    return splitTopLevel(text.slice(1, -1), ",").map(parseParameter);
  }
  return text;
}

export function parseEpl(epl: string): EplMethodCall[] {
  const source = epl.trim().replace(/;$/, "").replace(/\s+/g, " ");
  if (!source) throw new Error("EPL 语句不能为空");

  const assignment = source.match(/^(?:[\w<>]+)\s+\w+\s*=\s*(.+)$/);
  const chain = (assignment ? assignment[1] : source).trim();
  const calls: EplMethodCall[] = [];
  let start = 0;
  let depth = 0;
  let quote = "";
  let escaped = false;

  for (let i = 0; i < chain.length; i += 1) {
    const char = chain[i];
    if (escaped) {
      escaped = false;
      continue;
    }
    if (quote && char === "\\") {
      escaped = true;
      continue;
    }
    if (quote) {
      if (char === quote) quote = "";
      continue;
    }
    if (char === "'" || char === '"') quote = char;
    else if (char === "(") depth += 1;
    else if (char === ")") depth -= 1;
    else if (char === "." && depth === 0) {
      const method = chain.slice(start, i).trim();
      if (method) calls.push(parseCall(method));
      start = i + 1;
    }
  }
  const last = chain.slice(start).trim();
  if (last) calls.push(parseCall(last));
  return calls;
}

function parseCall(value: string): EplMethodCall {
  const match = value.match(/^(\w+)\s*\((.*)\)$/);
  if (!match) {
    if (value === "Es") return { name: "root", parameters: [] };
    throw new Error("无法解析方法调用: " + value);
  }
  return {
    name: match[1],
    parameters: splitTopLevel(match[2], ",").map(parseParameter),
  };
}
