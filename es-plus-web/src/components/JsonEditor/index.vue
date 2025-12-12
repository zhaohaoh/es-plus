<template>
  <div ref="maxDiv" style="width: 100%; height: 100%; display: flex; flex-direction: column;">
    <div class="tools-box" v-if="showToolbar">
      <div>
        <span v-if="title != ''">{{ title }}</span>
      </div>
      <div class="tools-list" v-if="showTools">
        <span class="aides" style="margin-right: 1rem" @click="format">
          <i class="el-icon-edit"></i>
          美化</span
        >
        <span class="aides" @click="copy">
          <i class="el-icon-delete"></i>
          复制</span
        >
      </div>
    </div>
    <div
        id="container"
        class="monaco-editor"
        ref="container"
        :style="{
          flex: height === '100%' ? '1' : 'none',
          height: height === '100%' ? 'auto' : (height.includes('calc') || height.includes('%')) ? height : `${height}px`,
          width: containerWidth
        }"
    ></div>
  </div>
</template>
<script setup lang="ts">
import {monaco} from "../../util/monaco";

import {computed, nextTick, onMounted, ref, watch} from "vue";
import handleClipboard from "../../util/clipboard";

let isEditorReady = ref(false); // 添加初始化状态标志

let props = defineProps({
  read: {
    type: Boolean,
    default: () => {
      return false;
    },
  },
  title: {
    type: String,
    default: "",
  },
  value: {
    type: String,
    default: () => {
      return "";
    },
  },
  pointOut: {
    type: Array,
    default: () => {
      return [];
    },
  },
  height: {
    type: String,
    default: () => {
      return "400";
    },
  },
  fontSize: {
    type: Number,
    default: () => {
      return 18;
    },
  },
  language: {
    type: String,
    default: () => {
      return "json";
    },
  },
  showToolbar: {
    type: Boolean,
    default: () => {
      return true;
    },
  },
  showTools: {
    type: Boolean,
    default: () => {
      return true;
    },
  },
});

let container = ref(null);
let maxDiv = ref(null);
let containerWidth = ref("100%");

const emits = defineEmits(["update:value"]);

let editor = null;
const jsonValue = ref(props.value);
const json = computed({
  get() {
    return props.value;
  },
  set(val) {
    emits("update:value", val);
  },
});

// 创建编辑器
const createEditor = () => {
  const monacoEditor = monaco.editor.create(container.value, {
    value: jsonValue.value,
    language: props.language,
    theme: "vs",
    automaticLayout: true,
    fontSize: props.fontSize,
    wordWrap: "on",
    minimap: {enabled: false},
    scrollBeyondLastLine: false,
    readOnly: props.read,
  });

  editor = monacoEditor;
  isEditorReady.value = true;

  // 监听内容变化
  editor.onDidChangeModelContent(() => {
    json.value = editor.getValue();
  });

  monaco.languages.registerCompletionItemProvider(props.language, {
    triggerCharacters: [' ', '(', '.', '"', "'"],
    provideCompletionItems: (model, position) => {
      let suggestions = [];

      // 自定义提示词
      if (props.pointOut && props.pointOut.length > 0) {
        suggestions = props.pointOut.map((item) => ({
          label: item,
          kind: monaco.languages.CompletionItemKind.Text,
          insertText: item,
          detail: '自定义'
        }));
      }

      // SQL语言查询提示
      if (props.language === 'sql') {
        const sqlSuggestions = [
          // SQL关键字
          {
            label: 'SELECT',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'SELECT ',
            detail: '查询语句'
          },
          {label: 'FROM', kind: monaco.languages.CompletionItemKind.Keyword, insertText: 'FROM ', detail: '指定表名'},
          {label: 'WHERE', kind: monaco.languages.CompletionItemKind.Keyword, insertText: 'WHERE ', detail: '条件过滤'},
          {label: 'AND', kind: monaco.languages.CompletionItemKind.Keyword, insertText: ' AND ', detail: '逻辑与'},
          {label: 'OR', kind: monaco.languages.CompletionItemKind.Keyword, insertText: ' OR ', detail: '逻辑或'},
          {
            label: 'ORDER BY',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'ORDER BY ',
            detail: '排序'
          },
          {
            label: 'GROUP BY',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'GROUP BY ',
            detail: '分组'
          },
          {
            label: 'HAVING',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'HAVING ',
            detail: '分组后过滤'
          },
          {label: 'LIMIT', kind: monaco.languages.CompletionItemKind.Keyword, insertText: 'LIMIT ', detail: '限制行数'},
          {label: 'OFFSET', kind: monaco.languages.CompletionItemKind.Keyword, insertText: 'OFFSET ', detail: '偏移量'},
          {label: 'JOIN', kind: monaco.languages.CompletionItemKind.Keyword, insertText: 'JOIN ', detail: '连接表'},
          {
            label: 'LEFT JOIN',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'LEFT JOIN ',
            detail: '左连接'
          },
          {
            label: 'RIGHT JOIN',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'RIGHT JOIN ',
            detail: '右连接'
          },
          {
            label: 'INNER JOIN',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'INNER JOIN ',
            detail: '内连接'
          },
          {
            label: 'UNION',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'UNION ',
            detail: '合并结果集'
          },
          {
            label: 'DISTINCT',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'DISTINCT ',
            detail: '去重'
          },

          // 聚合函数
          {
            label: 'COUNT',
            kind: monaco.languages.CompletionItemKind.Function,
            insertText: 'COUNT($1)',
            detail: '计数函数'
          },
          {label: 'SUM', kind: monaco.languages.CompletionItemKind.Function, insertText: 'SUM($1)', detail: '求和函数'},
          {
            label: 'AVG',
            kind: monaco.languages.CompletionItemKind.Function,
            insertText: 'AVG($1)',
            detail: '平均值函数'
          },
          {
            label: 'MAX',
            kind: monaco.languages.CompletionItemKind.Function,
            insertText: 'MAX($1)',
            detail: '最大值函数'
          },
          {
            label: 'MIN',
            kind: monaco.languages.CompletionItemKind.Function,
            insertText: 'MIN($1)',
            detail: '最小值函数'
          },

          // 操作符
          {label: '=', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' = ', detail: '等于'},
          {label: '!=', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' != ', detail: '不等于'},
          {label: '<>', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' <> ', detail: '不等于'},
          {label: '<', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' < ', detail: '小于'},
          {label: '>', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' > ', detail: '大于'},
          {label: '<=', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' <= ', detail: '小于等于'},
          {label: '>=', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' >= ', detail: '大于等于'},
          {label: 'LIKE', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' LIKE ', detail: '模糊匹配'},
          {label: 'IN', kind: monaco.languages.CompletionItemKind.Operator, insertText: ' IN ', detail: '包含于'},
          {
            label: 'NOT IN',
            kind: monaco.languages.CompletionItemKind.Operator,
            insertText: ' NOT IN ',
            detail: '不包含于'
          },
          {
            label: 'IS NULL',
            kind: monaco.languages.CompletionItemKind.Operator,
            insertText: ' IS NULL',
            detail: '为空'
          },
          {
            label: 'IS NOT NULL',
            kind: monaco.languages.CompletionItemKind.Operator,
            insertText: ' IS NOT NULL',
            detail: '非空'
          },
          {
            label: 'BETWEEN',
            kind: monaco.languages.CompletionItemKind.Operator,
            insertText: ' BETWEEN ',
            detail: '区间'
          },
          {label: 'EXISTS', kind: monaco.languages.CompletionItemKind.Operator, insertText: 'EXISTS ', detail: '存在'},

          // 排序关键字
          {label: 'ASC', kind: monaco.languages.CompletionItemKind.Keyword, insertText: ' ASC', detail: '升序'},
          {label: 'DESC', kind: monaco.languages.CompletionItemKind.Keyword, insertText: ' DESC', detail: '降序'},

          // 通配符
          {label: '*', kind: monaco.languages.CompletionItemKind.Operator, insertText: '*', detail: '所有字段'}
        ];

        suggestions = [...suggestions, ...sqlSuggestions];
      }

      return {suggestions};
    },
  });
};

// 格式化代码
const format = () => {
  if (editor) {
    editor.getAction("editor.action.formatDocument").run();
  }
};

// 复制内容
const copy = () => {
  handleClipboard(json.value, () => {
    console.log("复制成功");
  });
};

// 监听value变化
watch(
    () => props.value,
    (newVal) => {
      if (editor && newVal !== editor.getValue()) {
        editor.setValue(newVal);
      }
    }
);

// 监听language变化
watch(
    () => props.language,
    (newLanguage) => {
      if (editor) {
        const model = editor.getModel();
        if (model) {
          monaco.editor.setModelLanguage(model, newLanguage);
        }
      }
    }
);

onMounted(() => {
  nextTick(() => {
    createEditor();
  });
});

// 暴露方法给父组件
defineExpose({
  format,
  copy,
  getValue: () => editor?.getValue() || "",
  setValue: (value: string) => {
    if (editor) {
      editor.setValue(value);
    }
  },
});
</script>

<style scoped>
.monaco-editor {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.tools-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  font-size: 14px;
  color: #606266;
}

.tools-list {
  display: flex;
  gap: 8px;
}

.aides {
  cursor: pointer;
  color: #409eff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.aides:hover {
  background-color: #ecf5ff;
}

.aides i {
  margin-right: 4px;
}
</style>