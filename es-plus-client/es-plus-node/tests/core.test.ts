import test from "node:test";
import assert from "node:assert/strict";
import { eplToDsl } from "../../shared/epl-to-dsl.ts";
import { sqlToDsl } from "../../shared/sql-to-dsl.ts";

test("chain EPL converts to term DSL", () => {
  const actual = eplToDsl(
    'Es.chainQuery().index("sys_user").term("username", "zhangsan").search(10)'
  );

  assert.deepEqual(actual, {
    query: { term: { username: "zhangsan" } },
    size: 10,
  });
});

test("chain EPL supports range sort and paging", () => {
  const actual = eplToDsl(
    'Es.chainQuery().index("orders").ge("amount", 100).sortByDesc("created_at").search(20, 40)'
  );

  assert.deepEqual(actual, {
    query: { range: { amount: { gte: 100 } } },
    sort: [{ created_at: { order: "desc" } }],
    size: 20,
    from: 40,
  });
});

test("basic SQL converts to ES DSL", () => {
  const actual = sqlToDsl(
    "SELECT username, age FROM sys_user WHERE username = 'zhangsan' AND age > 18 ORDER BY age DESC LIMIT 20"
  );

  assert.deepEqual(actual, {
    index: "sys_user",
    _source: ["username", "age"],
    query: {
      bool: {
        must: [
          { term: { username: "zhangsan" } },
          { range: { age: { gt: 18 } } },
        ],
      },
    },
    sort: [{ age: "desc" }],
    size: 20,
  });
});

test("SQL supports IN LIKE and inclusive ranges", () => {
  const actual = sqlToDsl(
    "SELECT * FROM orders WHERE status IN ('paid', 'sent') AND title LIKE '%book%' AND amount >= 10 AND amount <= 99 LIMIT 50"
  );

  assert.deepEqual(actual, {
    index: "orders",
    query: {
      bool: {
        must: [
          { terms: { status: ["paid", "sent"] } },
          { wildcard: { title: { value: "*book*" } } },
          { range: { amount: { gte: 10 } } },
          { range: { amount: { lte: 99 } } },
        ],
      },
    },
    size: 50,
  });
});

test("SQL supports nested GROUP BY and metric aggregations", () => {
  const actual = sqlToDsl(
    "SELECT COUNT(*), SUM(amount), AVG(amount), MAX(amount), MIN(amount), TOPHITS(order_no) " +
    "FROM orders GROUP BY shop_id, status LIMIT 10"
  );

  assert.deepEqual(actual, {
    index: "orders",
    query: { match_all: {} },
    aggs: {
      group_by_shop_id: {
        terms: { field: "shop_id", size: 10000 },
        aggs: {
          group_by_status: {
            terms: { field: "status", size: 10000 },
            aggs: {
              sum_amount: { sum: { field: "amount" } },
              avg_amount: { avg: { field: "amount" } },
              max_amount: { max: { field: "amount" } },
              min_amount: { min: { field: "amount" } },
              tophits_order_no: { top_hits: { size: 5, _source: { includes: ["order_no"] } } },
            },
          },
        },
      },
    },
    size: 0,
  });
});
