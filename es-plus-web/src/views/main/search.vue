<template>
  <div class="container">
    <div class="left-panel">
      <div class="esQuery-container">
        <el-button-group style="margin-top: -10px; margin-bottom: 10px">
          <el-button type="primary" plain size="small" @click="clickSql"
            >SQL</el-button
          >
          <el-button type="primary" plain size="small" @click="clickEpl"
            >EPL</el-button
          >
          <el-button type="primary" plain size="small" @click="clickDsl"
            >DSL</el-button
          >
        </el-button-group>

        <div style="min-height: 30px">
          <!-- :class="{ hidden: queryType !== 'dsl' }" -->
          <el-select
            v-model="index"
            placeholder="索引"
            filterable
            style="width: 400px; margin-bottom: 10px"
          >
            <el-option
              v-for="item in indexData"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <!-- :class="{ hidden: queryType !== 'dsl' }" -->
          <el-button
            @click="copySelectedText"
            type="primary"
            style="margin-left: 10px; margin-bottom: 10px"
            plain
            >复制名称
          </el-button>
          <el-button
            type="primary"
            @click="clickIndex"
            plain
            style="margin-left: 10px; margin-bottom: 10px"
            >查看索引</el-button
          >
        </div>

        <label class="esQuery-label">
          <Codemirror
            v-model:value="queryDsl"
            :options="cmOptions"
            ref="sqlEditor"
            border
            height="630"
            width="100%"
            @ready="onSqlReady"
            class="json-input"
            placeholder="请输入Sql语句..."
          />
        </label>
        <div class="controls">
          <el-button
            size="small"
            @click="sql2Dsl"
            type="primary"
            plain
            v-show="queryType == 'sql'"
            >转DSL</el-button
          >
          <el-button
            size="small"
            @click="explain"
            type="primary"
            plain
            v-show="queryType == 'sql'"
            >输出执行计划</el-button
          >
          <el-button
            size="small"
            :icon="Search"
            @click="submitQuery"
            plain
            type="primary"
            >搜索</el-button
          >
          <!-- 绑定点击事件 -->
        </div>
      </div>
    </div>
    <div class="right-panel">
      <Codemirror
        v-model:value="jsonView"
        :options="jsonOptions"
        ref="jsonEd"
        border
        class="json-output"
        height="91%"
        style="margin-top: 30px"
      />
      <div class="footer-button" justify="end">
        <span class="dialog-footer">
          <el-button @click="clickAdd" type="primary" size="small" plain
            >新增</el-button
          >
          <el-button @click="clickSave" type="primary" size="small" plain
            >修改</el-button
          >
          <el-button type="primary" @click="clickDelete" size="small" plain
            >删除</el-button
          >
          <el-button type="primary" @click="clickGetField" size="small" plain
            >更多操作</el-button
          >
        </span>
      </div>

      <!-- <textarea
        id="json-output"
        class="json-output"
        placeholder="输出"
        ref="textarea"
      ></textarea> -->
      <!-- <div class="error" id="error-msg">{{ errorMessage }}</div> -->
    </div>
  </div>
  <el-dialog
    v-model="addDataVisible"
    :title="'当前索引:' + index"
    style="
      max-width: 700px;
      position: relative;
      height: 750px;
      transform: translateY(-50px);
    "
  >
    <div>
      <JsonEditor
        v-model:value="addData"
        height="600"
        styles="width: 100%"
        title="新增数据注意:_id必填,否则会自动生成id"
      />
    </div>
    <el-button
      type="primary"
      @click="doAdd"
      style="position: absolute; right: 10px; bottom: 10px"
    >
      保存数据
    </el-button>
  </el-dialog>

  <el-dialog
    v-model="convertFieldVisible"
    title="更多操作"
    style="
      width: 600px;
      max-width: 600px;
      position: relative;
      height: 200px;
      transform: translateY(80px);
      transform: translateX(50px);
    "
  >
    <el-button-group style="margin-top: 0px; margin-bottom: 11px">
      <el-button type="primary" plain>提取字段</el-button>
      <el-button type="primary" plain @click="excelExport">数据导出</el-button>
    </el-button-group>
    <el-input v-model="convertField" />
    <el-button
      type="primary"
      plain
      @click="confirmConvertField"
      style="position: absolute; right: 10px; bottom: 10px"
      >确认转换
    </el-button>
  </el-dialog>

  <el-dialog
    v-model="dialogIndexInfo"
    title="索引信息"
    style="
      width: 900px;
      max-width: 1000px;
      position: relative;
      height: 750px;
      transform: translateY(-50px);
    "
  >
    <div>
      <JsonEditor
        v-model:value="indexInfo"
        height="600"
        styles="width: 100%"
        title="索引信息"
      />
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, proxy, getCurrentInstance, refs, onMounted } from "vue";

import Codemirror from "codemirror-editor-vue3";
// 主题样式（我直接用了纯白色的，看着比较舒服）
import "codemirror/theme/rubyblue.css";
// 括号显示匹配
import "codemirror/addon/edit/matchbrackets";
import "codemirror/addon/selection/active-line";
// 括号、引号编辑和删除时成对出现
import "codemirror/addon/edit/closebrackets";
// 引入css文件
import "codemirror/lib/codemirror.css";
// 引入主题 可以从 codemirror/theme/ 下引入多个
import "codemirror/theme/idea.css";
// 引入语言模式 可以从 codemirror/mode/ 下引入多个
import "codemirror/mode/sql/sql.js";
// 代码提示功能 具体语言可以从 codemirror/addon/hint/ 下引入多个
import "codemirror/addon/hint/show-hint.css";
import "codemirror/addon/hint/show-hint";
import "codemirror/addon/hint/sql-hint";
import "codemirror/mode/javascript/javascript.js";
import { Search } from "@element-plus/icons-vue";
import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import esClient from "../../api/esClient";
import elMessage from "../../util/message";
import config from "../../config";
const { proxy } = getCurrentInstance() as any;
let sql = "SELECT * from fast_test_new_v128 order by id desc limit 10";
let dsl =
  "{\n" +
  '    "query": {\n' +
  '        "bool": {\n' +
  '            "must": [\n' +
  "                {\n" +
  '                    "match_all": {}\n' +
  "                }\n" +
  "            ],\n" +
  '            "must_not": [],\n' +
  '            "should": []\n' +
  "        }\n" +
  "    },\n" +
  '    "from": 0,\n' +
  '    "size": 1,\n' +
  '    "sort": [],\n' +
  '    "aggs": {}\n' +
  "}";
let epl = 'Es.chainQuery().index("").search(10)';
let queryDsl = ref("");
let queryType = ref("sql");
let index = ref("");

const jsonView = ref("");
const convertField = ref("");
const convertFieldVisible = ref(false);
const addDataVisible = ref(false);
const addData = ref("");
const indexData = ref([]);
onMounted(() => {
  clickSql();
  getIndices("");
});

// 复制选中文本
const copySelectedText = () => {
  if (index.value) {
    navigator.clipboard
      .writeText(index.value)
      .then(() => ElMessage.success("复制成功"))
      .catch(() => fallbackCopy(index.value));
  }
};

// 备用方法（兼容旧浏览器和内网 HTTP）
function fallbackCopy(text) {
  const textarea = document.createElement("textarea");
  textarea.value = text;
  document.body.appendChild(textarea);
  textarea.select();

  try {
    const success = document.execCommand("copy");
    // console.log(success ? "降级复制成功" : "降级复制失败");
  } catch (err) {
    console.error("降级复制出错:", err);
    // 最终提示用户手动复制
    alert("自动复制失败，请按 Ctrl+C 手动复制：\n" + text);
  }

  document.body.removeChild(textarea);
}

const indexInfo = ref("");
const dialogIndexInfo = ref(false);
const clickIndex = async () => {
  if (!index.value) {
    ElMessage.error("请选择索引");
    return;
  }
  getIndex(index.value);
  dialogIndexInfo.value = true;
};

const getIndex = async (index) => {
  const param = {
    index: index,
  };
  let res = await proxy.$api.esIndex.getIndex(param);

  indexInfo.value = JSON.stringify(res, null, 2);
};

const getIndices = async (keyword) => {
  const param = {
    keyword: keyword,
  };
  let res = await proxy.$api.esIndex.indexList(param);

  indexData.value = res.indices;
};

const clickSql = () => {
  const lastSql = localStorage.getItem("lastSql");
  if (lastSql) {
    queryDsl.value = lastSql;
  } else {
    queryDsl.value = sql;
  }
  queryType.value = "sql";
};

const clickEpl = () => {
  const lastEpl = localStorage.getItem("lastEpl");
  if (lastEpl) {
    queryDsl.value = lastEpl;
  } else {
    queryDsl.value = epl;
  }
  queryType.value = "epl";
};

const clickDsl = () => {
  const lastDsl = localStorage.getItem("lastDsl");
  if (lastDsl) {
    queryDsl.value = lastDsl;
  } else {
    queryDsl.value = dsl;
  }
  queryType.value = "dsl";
};

const excelExport = () => {
  const data = JSON.parse(jsonView.value);
  let d = data.hits.hits
    .map((hit) => hit._source) // 安全提取并展开数组
    .filter(Boolean);
  let res = exportExcel(d);
};

// 在组件方法中
const exportExcel = async (param) => {
  try {
    const response = await axios({
      method: "post", // 或 'post' 根据接口要求
      url: "/es/file/excelExport",
      baseURL: config.baseUrl,
      responseType: "blob", // 关键配置：指定响应类型为二进制流
      data: param, // POST 请求参数
    });

    // 处理文件名（后端可能通过 Content-Disposition 返回）
    const filename = getFilenameFromHeader(response.headers) || "export.xlsx";

    // 创建 Blob 对象并触发下载
    const blob = new Blob([response.data], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const link = document.createElement("a");
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    link.click();

    // 清理资源
    window.URL.revokeObjectURL(link.href);
  } catch (error) {
    console.error("导出失败:", error);
    // 可添加用户提示（如使用 Element Plus 的 ElMessage）
    // ElMessage.error('导出失败，请重试');
  }
};

// 从响应头解析文件名（可选）
const getFilenameFromHeader = (headers) => {
  const contentDisposition = headers["content-disposition"];
  if (!contentDisposition) return "";
  const filenameMatch = contentDisposition.match(/filename="?(.+)"?/);
  return filenameMatch?.[1] || "";
};

const clickGetField = () => {
  convertFieldVisible.value = true;
};

const confirmConvertField = () => {
  console.log(convertField.value);
  const data = JSON.parse(jsonView.value);
  let allWareIds = data.hits.hits
    .flatMap((hit) => hit._source?.[convertField.value] || []) // 安全提取并展开数组
    .filter(Boolean); // 过滤空值（如果存在）
  console.log(allWareIds);
  // 提取所有 wareId 的逻辑
  if (allWareIds.length <= 0) {
    allWareIds = data.hits.hits
      .flatMap((hit) => hit.fields?.[convertField.value] || []) // 安全提取并展开数组
      .filter(Boolean); // 过滤空值（如果存在）
  }
  convertFieldVisible.value = false;
  jsonView.value = JSON.stringify(allWareIds);
};

const jsonOptions = {
  // 主题
  theme: "default",
  // 语言及语法模式
  mode: "application/json",
  lineNumbers: true,
  matchBrackets: true, //括号匹配
  lineWrapping: true, // 折叠
  styleActiveLine: true, // 光标行高亮
  lint: true, // 打开json校验
};

const cmOptions = {
  // 语言及语法模式
  mode: "text/x-sql",
  // 主题
  theme: "default", // 'idea'
  // 显示函数
  line: true,
  // 显示行号
  lineNumbers: true,
  // 软换行
  lineWrapping: true,
  // tab宽度
  tabSize: 4,
  // 代码提示功能
  hintOptions: {
    // 避免由于提示列表只有一个提示信息时，自动填充
    completeSingle: false,
    // 不同的语言支持从配置中读取自定义配置 sql语言允许配置表和字段信息，用于代码提示
    tables: {
      BPSuv: ["DocEntry", "Subject", "DocStatus", "Remarks"],
      BPSuvA: ["DocEntry", "LineNum", "Question", "QstType"],
      BPSuvB: ["DocEntry", "LineNum", "UserID", "UserName"],
    },
  },
};

const epOptions = {
  // 语言及语法模式
  mode: "text/javascript",
  // 主题
  theme: "default", // 'idea'
  // 显示函数
  line: true,
  // 显示行号
  lineNumbers: true,
  // 软换行
  lineWrapping: true,
  // tab宽度
  tabSize: 4,
  indentUnit: 4,
  // 代码提示功能
  hintOptions: {
    // 避免由于提示列表只有一个提示信息时，自动填充
    completeSingle: false,
    // 不同的语言支持从配置中读取自定义配置 sql语言允许配置表和字段信息，用于代码提示
    tables: {
      BPSuv: ["DocEntry", "Subject", "DocStatus", "Remarks"],
      BPSuvA: ["DocEntry", "LineNum", "Question", "QstType"],
      BPSuvB: ["DocEntry", "LineNum", "UserID", "UserName"],
    },
  },
};

// 代码联想提示源
const codeHints = {
  javascript: [
    "Es",
    "chainQuery()",
    "filter()",
    "must()",
    "should()",
    "mustNot()",
    'range("","from","to")',
    'term("","")',
    'terms("","")',
    'match("","")',
    'sortBy(DESC,"")',
    'ge("","")',
    'le("","")',
    'gt("","")',
    'lt("","")',
    'matchPhrase("","")',
    'termsKeyword("","")',
    'wildcard("","")',
    'fuzzy("","")',
    "ids([])",
    "count()",
    "search()",
    "esAggWrapper()",
    "EsChainQueryWrapper esChainQueryWrapper = ",

    //   聚合字段
    'sum("name","")',
    'count("","")',
    'min("","")',
    'max("","")',
    'avg("","")',
    'nested("","")',
    'range("","")',
    'filter("","")',
    'filters("","")',
  ],
};

const onEpReady = (epEditor) => {
  epEditor.on("inputRead", () => {
    if (epEditor.somethingSelected()) {
      return;
    }

    const cursor = epEditor.getCursor();
    const token = epEditor.getTokenAt(cursor);
    const lineTokens = epEditor.getLineTokens(cursor.line);
    const tokenType = epEditor.getTokenTypeAt(cursor);
    console.log(token);
    console.log(lineTokens);
    console.log(tokenType);
    // 获取当前语言模式的提示列表
    const currentMode = epEditor.getOption("mode").split("/")[1];
    const hints = codeHints[currentMode] || [];

    // 显示代码提示
    epEditor.showHint({
      completeSingle: false,
      hint: () => ({
        from: CodeMirror.Pos(cursor.line, token.start),
        to: CodeMirror.Pos(cursor.line, token.end),
        list: hints.filter((item) =>
          item.toLowerCase().startsWith(token.string.toLowerCase())
        ),
      }),
    });
  });
};

const onSqlReady = (sqlEditor) => {
  sqlEditor.on("inputRead", function (cm, location) {
    if (/[a-zA-Z]/.test(location.text[0])) {
      cm.showHint();
    }
  });
};

// // 响应式数据
// const esQuery1 = ref('Es.chainQuery().index("sys_user2ttt_alias").search(10)');
// const esQuery2 = ref('Es.chainQuery().index("sys_user2ttt_alias").search(10)');

// // 提交查询方法
// const submitQuery = (queryNum) => {
//   try {
//     // 这里可以添加实际的ES查询逻辑
//     const query = queryNum === 1 ? esQuery1.value : esQuery2.value;
//     // 示例：简单回显查询内容到输出
//     jsonOutput.value = `执行查询: ${query}`;

//     // 添加历史记录
//     history.push(`执行查询: ${new Date().toLocaleString()} - ${query}`);

//     errorMessage.value = "";
//   } catch (error) {
//     errorMessage.value = "无效的查询格式";
//     jsonOutput.value = "";
//   }
// };
const submitQuery = () => {
  if (queryType.value == "epl") {
    eplQuery(queryDsl.value);
  }
  if (queryType.value == "sql") {
    sqlQuery(queryDsl.value);
  }
  if (queryType.value == "dsl") {
    dslQuery(queryDsl.value);
  }
};

const sql2Dsl = async () => {
  const param = {
    sql: queryDsl.value,
  };
  let res = await proxy.$api.tools.sql2Dsl(param);
  const formattedJson = JSON.stringify(res, null, 2);
  jsonView.value = formattedJson;
};

const explain = async () => {
  const param = {
    sql: queryDsl.value,
  };
  let res = await proxy.$api.tools.explain(param);
  const formattedJson = JSON.stringify(res, null, 2);
  jsonView.value = formattedJson;
};

const dslQuery = async (dsl) => {
  localStorage.setItem("lastDsl", dsl);
  const param = {
    dsl: dsl,
    index: index.value,
  };
  let res = await proxy.$api.tools.dslQuery(param);
  const formattedJson = JSON.stringify(res, null, 2);
  jsonView.value = formattedJson;
};

// // 提交查询方法
const submitEplQuery = () => {
  eplQuery(queryDsl.value);
};

const eplQuery = async (epl) => {
  localStorage.setItem("lastEpl", epl);
  const param = {
    epl: epl,
  };
  let res = await proxy.$api.tools.eplQuery(param);
  const formattedJson = JSON.stringify(res, null, 2);
  jsonView.value = formattedJson;
};

// // 提交查询方法
const submitSqlQuery = () => {
  sqlQuery(queryDsl.value);
};

const sqlQuery = async (sql) => {
  localStorage.setItem("lastSql", sql);
  const param = {
    sql: sql,
  };
  let res = await proxy.$api.tools.sqlQuery(param);
  const formattedJson = JSON.stringify(res, null, 2);
  jsonView.value = formattedJson;
};

const clickAdd = () => {
  addDataVisible.value = true;
};

//点击新增
const doAdd = () => {
  if (index.value == null || index.value == "") {
    ElMessage.success("索引名称未填写");
  } else {
    ElMessageBox.confirm("索引:" + index.value, "新增数据", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      dangerouslyUseHTMLString: true,
    })
      .then(() => {
        const data = JSON.parse(addData.value);
        const datas: any[] = [];
        // 如果是数组，展开元素；否则直接推入
        if (Array.isArray(data)) {
          datas.push(...data); // 使用展开运算符
        } else {
          datas.push(data);
        }
        saveByIds(index.value, datas);
      })
      .catch(() => {});
  }
};

//点击保存
const clickSave = () => {
  if (jsonView.value) {
    const jsonObject = JSON.parse(jsonView.value);
    const list = jsonObject.hits.hits;
    if (list && list.length > 0) {
      const index = list[0]._index;
      const ids = list.map((item) => item._id);
      const source = list.map((item) => item._source);
      console.log(index + source);
      ElMessageBox.confirm(
        "确定编辑" + ids[0] + "...总计" + ids.length + "个数据?",
        "编辑确认",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          dangerouslyUseHTMLString: true,
        }
      )
        .then(() => {
          saveByIds(index, source);
        })
        .catch(() => {});
    }
  }
};

//点击删除
const clickDelete = () => {
  if (jsonView.value) {
    const jsonObject = JSON.parse(jsonView.value);
    const list = jsonObject.hits.hits;
    if (list && list.length > 0) {
      const index = list[0]._index;
      const ids = list.map((item) => item._id);
      console.log(index + ids);
      ElMessageBox.confirm(
        "确定删除" + ids[0] + "...总计" + ids.length + "个数据?",
        "删除确认",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          dangerouslyUseHTMLString: true,
        }
      )
        .then(() => {
          console.log("确认删除");
          deleteByIds(index, ids);
        })
        .catch(() => {});
    }
  }
};

const deleteByIds = async (index, ids) => {
  const param = {
    ids: ids,
    index: index,
  };
  let res = await proxy.$api.tools.deleteByIds(param);
  const formattedJson = JSON.stringify(res, null, 2);
  jsonView.value = formattedJson;
};

const saveByIds = async (index, source) => {
  const param = {
    index: index,
    datas: source,
  };
  console.log(param);
  let res = await proxy.$api.tools.updateBatch(param);
};

// // 清空历史记录
// const clearHistory = () => {
//   history.splice(0, history.length);
// };
</script>

<style scoped>
/* 保持原有样式，添加scoped属性确保样式只作用于当前组件 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.footer-button {
  margin-top: 10px;
  display: flex;
  flex: 1;
  justify-content: end;
}
.hidden {
  display: none !important;
}
.container {
  max-height: 9000px;
  flex: 1;
  display: flex;
  align-items: stretch;
  gap: 1px;
  background: #f0f0f0;
  max-width: 1300px;
  margin: 0 auto;
  padding: 5px;
}

.left-panel {
  flex: 1;
  padding: 20px;
  background: white;
  border-radius: 4px 0 0 4px;
  width: 500px;
  height: 780px;
}

.right-panel {
  flex: 1;
  padding: 20px;
  background: #ffffff;
  border-radius: 0 4px 4px 0;
  /* overflow-y: auto; */
  height: 780px;
  max-width: 650px;
}

.json-input {
  width: 100%;
  height: calc(50% - 50px);
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: none;
  font-family: "Courier New", monospace;
}

.json-output {
  width: 100%;
  height: calc(100% - 50px);
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: none;
  font-family: "Courier New", monospace;
  white-space: pre-wrap;
  color: #333;
}

.controls {
  margin: 10px 0px 10px 0px;
  text-align: right;
}

button {
  padding: 8px 16px;
  /* background: #4caf50; */
  /* color: white; */
  /* border: none; */
  border-radius: 4px;
  cursor: pointer;
}

/* button:hover {
  background: #45a049;
} */

.error {
  color: #ff4444;
  margin-top: 10px;
}

.CodeMirror {
  width: 100%;
  height: calc(100% - 50px);
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: none;
  font-family: "Courier New", monospace;
}

.esQueryDiv {
  height: 50%;
}

.history-panel {
  margin-left: 20px; /* 添加左边距避免与左面板重叠 */
  padding: 20px;
  background: white;
  border-radius: 4px;
  width: 300px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

#history-list {
  height: 300px;
  overflow-y: auto;
}
</style>
