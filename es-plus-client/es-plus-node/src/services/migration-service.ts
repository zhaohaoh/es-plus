import { randomUUID } from "node:crypto";
import { EsHttpClient } from "../es/es-client.ts";
import { ClientStore, type EsClientConfig, type ReindexTask } from "../storage/client-store.ts";

export interface DataMoveRequest {
  sourceClient: string;
  targetClient: string;
  sourceIndex: string;
  targetIndex: string;
  maxSize?: number;
  sleepTime?: number;
  batchSize?: number;
}

export class MigrationService {
  private readonly store: ClientStore;
  private readonly http: EsHttpClient;
  private readonly runningTasks = new Map<string, number>();
  private readonly stoppedTasks = new Set<string>();

  constructor(store: ClientStore, http: EsHttpClient) {
    this.store = store;
    this.http = http;
  }

  async reindex(config: EsClientConfig, sourceIndex: string, targetIndex: string): Promise<string> {
    const result = await this.http.request(config, "/_reindex?wait_for_completion=false", {
      method: "POST",
      body: JSON.stringify({ source: { index: sourceIndex }, dest: { index: targetIndex }, conflicts: "proceed" }),
    }) as Record<string, unknown>;
    const taskId = String(result.task ?? "");
    if (!taskId) throw new Error("Elasticsearch 未返回 Reindex 任务编号");
    this.store.saveTask({
      sourceClient: config.unikey,
      targetClient: config.unikey,
      sourceIndex,
      targetIndex,
      type: 1,
      taskId,
      createTime: Date.now(),
      createUid: 1,
    });
    return taskId;
  }

  async taskStatus(config: EsClientConfig, taskId: string): Promise<Record<string, unknown>> {
    const result = await this.http.request(config, "/_tasks/" + encodeURIComponent(taskId)) as Record<string, unknown>;
    return {
      taskInfo: JSON.stringify(result.task ?? result.response ?? result),
      completed: Boolean(result.completed),
    };
  }

  async listTasks(config: EsClientConfig, sourceIndex: string): Promise<ReindexTask[]> {
    const tasks = this.store.listTasks(config.unikey, sourceIndex);
    return Promise.all(tasks.map(async (task) => {
      if (task.type !== 1 || !task.taskId) return task;
      try {
        const status = await this.taskStatus(config, task.taskId);
        return { ...task, taskJson: status.taskInfo as string, completed: Boolean(status.completed) };
      } catch {
        return task;
      }
    }));
  }

  startDataMove(request: DataMoveRequest): string {
    const taskId = randomUUID();
    const task = this.store.saveTask({
      sourceClient: request.sourceClient,
      targetClient: request.targetClient,
      sourceIndex: request.sourceIndex,
      targetIndex: request.targetIndex,
      type: 2,
      taskId,
      createTime: Date.now(),
      createUid: 1,
      taskJson: JSON.stringify({ status: "running", copied: 0 }),
    });
    this.runningTasks.set(taskId, task.id as number);
    void this.runDataMove(task, request);
    return taskId;
  }

  stopDataMove(taskId: string): boolean {
    const databaseId = this.runningTasks.get(taskId);
    if (!databaseId) return false;
    this.stoppedTasks.add(taskId);
    this.store.updateTask(databaseId, JSON.stringify({ status: "stopping" }), false);
    return true;
  }

  private async runDataMove(task: ReindexTask, request: DataMoveRequest): Promise<void> {
    const source = this.requireClient(request.sourceClient);
    const target = this.requireClient(request.targetClient);
    const batchSize = Math.max(1, Math.min(request.batchSize ?? 1000, 5000));
    const maxSize = request.maxSize && request.maxSize > 0 ? request.maxSize : Number.MAX_SAFE_INTEGER;
    let copied = 0;
    let scrollId = "";

    try {
      let page = await this.http.request(source, "/" + encodeURIComponent(request.sourceIndex) + "/_search?scroll=1m", {
        method: "POST",
        body: JSON.stringify({ size: batchSize, sort: ["_doc"], track_total_hits: true }),
      }) as Record<string, any>;
      scrollId = String(page._scroll_id ?? "");

      while (copied < maxSize) {
        if (this.stoppedTasks.has(String(task.taskId))) {
          this.store.updateTask(task.id as number, JSON.stringify({ status: "stopped", copied }), true);
          return;
        }
        const hits = (page.hits?.hits ?? []).slice(0, maxSize - copied);
        if (hits.length === 0) break;
        const operations: unknown[] = [];
        for (const hit of hits) {
          const action: Record<string, unknown> = { _index: request.targetIndex, _id: hit._id };
          if (hit._routing) action.routing = hit._routing;
          operations.push({ index: action }, hit._source ?? {});
        }
        const bulk = await this.http.bulk(target, operations) as Record<string, unknown>;
        if (bulk.errors) throw new Error("目标集群批量写入存在失败记录");
        copied += hits.length;
        this.store.updateTask(task.id as number, JSON.stringify({ status: "running", copied }), false);
        if (copied >= maxSize || !scrollId) break;
        if ((request.sleepTime ?? 0) > 0) await delay(request.sleepTime as number);
        page = await this.http.request(source, "/_search/scroll", {
          method: "POST",
          body: JSON.stringify({ scroll: "1m", scroll_id: scrollId }),
        }) as Record<string, any>;
        scrollId = String(page._scroll_id ?? scrollId);
      }
      this.store.updateTask(task.id as number, JSON.stringify({ status: "completed", copied }), true);
    } catch (error) {
      this.store.updateTask(task.id as number, JSON.stringify({
        status: "failed",
        copied,
        message: error instanceof Error ? error.message : String(error),
      }), true);
    } finally {
      this.runningTasks.delete(String(task.taskId));
      this.stoppedTasks.delete(String(task.taskId));
      if (scrollId) {
        try {
          await this.http.request(source, "/_search/scroll", {
            method: "DELETE",
            body: JSON.stringify({ scroll_id: [scrollId] }),
          });
        } catch {
          // 清理失败不覆盖迁移结果。
        }
      }
    }
  }

  private requireClient(key: string): EsClientConfig {
    const client = this.store.get(key);
    if (!client) throw new Error("找不到 ES 客户端配置: " + key);
    return client;
  }
}

function delay(milliseconds: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}
