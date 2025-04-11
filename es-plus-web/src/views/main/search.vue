<template>
  <div class="container">
    <el-aside width="200px">Aside</el-aside>
    <div class="left-panel">
      <div class="esQuery-container">
        <label class="esQuery-label">
          <Codemirror
            v-model:value="epl"
            :options="epOptions"
            ref="epEditor"
            border
            height="300"
            width="100%"
            @ready="onEpReady"
            class="json-input"
            placeholder="请输入es语句..."
          />
        </label>
        <div class="controls">
          <el-button size="small" :icon="Search" @click="">搜索</el-button>
          <!-- 绑定点击事件 -->
        </div>
      </div>

      <div class="sqlQuery-container">
        <label class="sqlQuery-label">
          <Codemirror
            v-model:value="sql"
            :options="cmOptions"
            ref="sqlEditor"
            border
            height="300"
            width="100%"
            @ready="onSqlReady"
            class="json-input"
            placeholder="请输入Sql语句..."
          />
        </label>
        <div class="controls">
          <!-- 绑定点击事件 -->
          <el-button size="small" :icon="Search" @click="submitSqlQuery"
            >搜索</el-button
          >
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
      />
      <div class="footer-button" justify="end">
        <span class="dialog-footer">
          <el-button @click="clickSave" type="danger" size="small"
            >修改</el-button
          >
          <el-button type="primary" @click="clickDelete" size="small"
            >删除</el-button
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
</template>

<script lang="ts" setup>
import { ref, reactive, proxy, getCurrentInstance, refs, onMounted } from "vue";

import Codemirror from "codemirror-editor-vue3";
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

import { ElMessage, ElMessageBox } from "element-plus";
import esClient from "../../api/esClient";

const { proxy } = getCurrentInstance() as any;
const sql = ref("SELECT * from fast_test_new_v128 order by id desc limit 10");
const epl = ref('Es.chainQuery().index("").search(10)');
const jsonEditor = ref();
const jsonView = ref("");

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

const jsonOptions = {
  // 主题
  theme: "default",
  // 语言及语法模式
  mode: "text/javascript",
  lineNumbers: true,
  lineWrapping: true, // 自动换行
  styleActiveLine: true, // 光标行高亮
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

// // 提交查询方法
const submitSqlQuery = () => {
  sqlQuery(sql.value);
};

const sqlQuery = async (sql) => {
  const param = {
    sql: sql,
  };
  let res = await proxy.$api.tools.sqlQuery(param);
  const formattedJson = JSON.stringify(res, null, 2);
  jsonView.value = formattedJson;
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

.container {
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
}

.right-panel {
  flex: 1;
  padding: 20px;
  background: #ffffff;
  border-radius: 0 4px 4px 0;
  /* overflow-y: auto; */
  height: 750px;
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
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background: #45a049;
}

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
