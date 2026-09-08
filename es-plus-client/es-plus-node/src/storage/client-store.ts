import { mkdirSync } from "node:fs";
import { homedir } from "node:os";
import { dirname, join } from "node:path";
import { DatabaseSync } from "node:sqlite";

export interface EsClientConfig {
  id?: number;
  unikey: string;
  name: string;
  address: string;
  schema?: string;
  username?: string;
  password?: string;
  version?: string;
}

export interface ReindexTask {
  id?: number;
  sourceClient: string;
  targetClient: string;
  sourceIndex: string;
  targetIndex: string;
  type: number;
  taskId?: string;
  createTime: number;
  createUid: number;
  taskJson?: string;
  completed?: boolean;
}

export class ClientStore {
  private readonly database: DatabaseSync;

  constructor(databasePath = join(homedir(), ".es-plus-console", "sqlite.db")) {
    mkdirSync(dirname(databasePath), { recursive: true });
    this.database = new DatabaseSync(databasePath);
    this.database.exec(
      "CREATE TABLE IF NOT EXISTS es_client (" +
      "id INTEGER PRIMARY KEY AUTOINCREMENT," +
      "unikey TEXT NOT NULL UNIQUE," +
      "name TEXT NOT NULL," +
      "address TEXT NOT NULL," +
      "schema TEXT DEFAULT 'http'," +
      "username TEXT," +
      "password TEXT," +
      "version TEXT)"
    );
    this.database.exec(
      "CREATE TABLE IF NOT EXISTS es_reindex_task (" +
      "id INTEGER PRIMARY KEY AUTOINCREMENT," +
      "source_client TEXT NOT NULL," +
      "target_client TEXT NOT NULL," +
      "source_index TEXT NOT NULL," +
      "target_index TEXT NOT NULL," +
      "type INTEGER NOT NULL," +
      "task_id TEXT," +
      "create_time INTEGER NOT NULL," +
      "create_uid INTEGER NOT NULL DEFAULT 0," +
      "task_json TEXT," +
      "completed INTEGER NOT NULL DEFAULT 0)"
    );
    const taskColumns = this.database.prepare("PRAGMA table_info(es_reindex_task)").all() as { name: string }[];
    if (!taskColumns.some((column) => column.name === "completed")) {
      this.database.exec("ALTER TABLE es_reindex_task ADD COLUMN completed INTEGER NOT NULL DEFAULT 0");
    }
  }

  list(): EsClientConfig[] {
    return this.database.prepare("SELECT id, unikey, name, address, schema, username, password, version FROM es_client ORDER BY id").all() as EsClientConfig[];
  }

  save(config: EsClientConfig): EsClientConfig {
    if (!config.unikey || !config.name || !config.address) {
      throw new Error("ES 客户端的 unikey、name、address 不能为空");
    }
    const existing = this.get(config.unikey);
    if (existing) {
      this.database.prepare(
        "UPDATE es_client SET name = ?, address = ?, schema = ?, username = ?, password = ?, version = ? WHERE unikey = ?"
      ).run(config.name, config.address, config.schema ?? "http", config.username ?? null, config.password ?? null,
        config.version ?? null, config.unikey);
    } else {
      this.database.prepare(
        "INSERT INTO es_client (unikey, name, address, schema, username, password, version) VALUES (?, ?, ?, ?, ?, ?, ?)"
      ).run(config.unikey, config.name, config.address, config.schema ?? "http", config.username ?? null,
        config.password ?? null, config.version ?? null);
    }
    return this.get(config.unikey) as EsClientConfig;
  }

  get(unikey: string): EsClientConfig | undefined {
    return this.database.prepare("SELECT id, unikey, name, address, schema, username, password, version FROM es_client WHERE unikey = ?").get(unikey) as EsClientConfig | undefined;
  }

  getById(id: number): EsClientConfig | undefined {
    return this.database.prepare("SELECT id, unikey, name, address, schema, username, password, version FROM es_client WHERE id = ?").get(id) as EsClientConfig | undefined;
  }

  delete(id: number): void {
    this.database.prepare("DELETE FROM es_client WHERE id = ?").run(id);
  }

  saveTask(task: ReindexTask): ReindexTask {
    const result = this.database.prepare(
      "INSERT INTO es_reindex_task (source_client, target_client, source_index, target_index, type, task_id, create_time, create_uid, task_json) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
    ).run(task.sourceClient, task.targetClient, task.sourceIndex, task.targetIndex, task.type, task.taskId ?? null,
      task.createTime, task.createUid, task.taskJson ?? null);
    return { ...task, id: Number(result.lastInsertRowid) };
  }

  updateTask(id: number, taskJson: string, completed: boolean): void {
    this.database.prepare("UPDATE es_reindex_task SET task_json = ?, completed = ? WHERE id = ?").run(taskJson, completed ? 1 : 0, id);
  }

  listTasks(sourceClient: string, sourceIndex: string): ReindexTask[] {
    const tasks = this.database.prepare(
      "SELECT id, source_client AS sourceClient, target_client AS targetClient, source_index AS sourceIndex, " +
      "target_index AS targetIndex, type, task_id AS taskId, create_time AS createTime, create_uid AS createUid, " +
      "task_json AS taskJson, completed " +
      "FROM es_reindex_task WHERE source_client = ? AND source_index = ? ORDER BY create_time DESC LIMIT 10"
    ).all(sourceClient, sourceIndex) as Array<ReindexTask & { completed: boolean | number }>;
    return tasks.map((task) => ({ ...task, completed: Boolean(task.completed) }));
  }

  /**
   * 关闭 SQLite 连接 供桌面端退出和测试清理使用
   */
  close(): void {
    this.database.close();
  }
}
