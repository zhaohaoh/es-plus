var editor = CodeMirror.fromTextArea(document.getElementById('json-output'), {
  mode: "javascript", // 实现代码高亮
  lineNumbers: true,
  lineWrapping: true, // 自动换行
  styleActiveLine: true, // 光标行高亮
  height: "100%" ,//设置初始化高度
});

var esQuery1 = CodeMirror.fromTextArea(document.getElementById('esQuery1'), {
  mode: "text/javascript", // 实现代码高亮
  lineNumbers: true,
  lineWrapping: true, // 自动换行
  styleActiveLine: true, // 光标行高亮
  indentUnit: 4,
  tabSize: 4,
});

var esQuery2 = CodeMirror.fromTextArea(document.getElementById('esQuery2'), {
  mode: "text/javascript", // 实现代码高亮
  lineNumbers: true,
  lineWrapping: true, // 自动换行
  styleActiveLine: true, // 光标行高亮
  indentUnit: 4,
  tabSize: 4,
});



document.addEventListener('DOMContentLoaded', function() {

  const submitBtn1 = document.getElementById('submitBtn1');
  // const jsonOutput = document.getElementById('json-output');

  submitBtn1.addEventListener('click', function() {
    const text = esQuery1.getValue().trim();
    esQuery(text);
  });
});

document.addEventListener('DOMContentLoaded', function() {

  const submitBtn2 = document.getElementById('submitBtn2');
  // const jsonOutput = document.getElementById('json-output');

  submitBtn2.addEventListener('click', function() {
    const text = esQuery2.getValue().trim();
    esQuery(text);
  });
});



function esQuery(text) {
  if (!text) {
    alert('请输入文字');
    return;
  }
  // 替换为实际的后端接口URL
  fetch('http://localhost:8080/web/esQuery/epl?epl=' + encodeURIComponent(text))
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP错误! 状态码: ${response.status}`);
    }
    return response.json();
  })
  .then(data => {
    // 格式化JSON并显示
    const formattedJson = JSON.stringify(data, null, 2);
    try {
      editor.setValue(formattedJson);
    } catch (error) {
      console.log(error)
      editor.setValue("错误无法获取数据");
    }
  })
  .catch(error => {
    console.error('请求失败:', error);
    editor.setValue("错误无法获取数据");
  });
}



// 代码联想提示源
const codeHints = {
  javascript: [
    "Es",
    "chainQuery()",
    'filter()',
    'must()',
    'should()',
    'mustNot()',
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
    'ids([])',
    'count()',
    "search()",
    'esAggWrapper()',
    'EsChainQueryWrapper esChainQueryWrapper = ',


    //   聚合字段
    'sum("name","")',
    'count("","")',
    'min("","")',
    'max("","")',
    'avg("","")',
    'nested("","")',
    'range("","")',
    'filter("","")',
    'filters("","")'
  ],
};


// 代码联想提示源
const esAggWrapperHints = {
  javascript: [

  ],
};

// 实现代码联想功能
esQuery1.on("inputRead", () => {
  if (esQuery1.somethingSelected()) return;

  const cursor = esQuery1.getCursor();
  const token = esQuery1.getTokenAt(cursor);
  const context = token.state.context;
  console.log(cursor)
  console.log(token)
  // 获取当前语言模式的提示列表
  const currentMode = esQuery1.getOption("mode").split("/")[1];
  const hints = codeHints[currentMode] || [];

  // 显示代码提示
  esQuery1.showHint({
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

// 实现代码联想功能
esQuery2.on("inputRead", () => {
  if (esQuery2.somethingSelected()) return;

  const cursor = esQuery2.getCursor();
  const token = esQuery2.getTokenAt(cursor);
  const context = token.state.context;

  // 获取当前语言模式的提示列表
  const currentMode = esQuery2.getOption("mode").split("/")[1];
  const hints = codeHints[currentMode] || [];

  const esAggWrapperHints = codeHints[esAggWrapperHints] || [];

  // 显示代码提示
  esQuery2.showHint({
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





// 初始化历史记录
let history = JSON.parse(localStorage.getItem('esQueryHistory')) || [];

// 执行查询时记录历史
function executeQuery(code) {
  const startTime = Date.now();

  // 假设这是原有的执行查询函数
  originalExecuteFunction(code).then(result => {
    const endTime = Date.now();
    const duration = endTime - startTime;

    // 记录历史
    history.unshift({
      timestamp: new Date().toLocaleString(),
      code: code,
      result: {
        took: result.took,
        hits: result.hits.total.value,
        timed_out: result.timed_out
      },
      duration: duration
    });

    // 保存最多50条记录
    if(history.length > 50) history.pop();

    // 更新本地存储
    localStorage.setItem('esQueryHistory', JSON.stringify(history));

    // 刷新历史面板
    renderHistory();
  });
}

// 渲染历史记录
function renderHistory() {
  const container = document.getElementById('history-list');
  container.innerHTML = history.slice(0,5).map((item, index) => `
    <div class="history-item">
      <div class="history-time">${item.timestamp}</div>
      <div class="history-code">${item.code}</div>
      <div class="history-result">
        耗时：${item.duration}ms | 
        结果：${item.result.hits}条记录 | 
        超时：${item.result.timed_out ? '是' : '否'}
      </div>
      <button onclick="reExecute('${item.code}')">重新执行</button>
    </div>
  `).join('');
}

// 重新执行查询
function reExecute(code) {
  document.getElementById('code-input-1').value = code;
  executeQuery(code);
}

// 清空历史
function clearHistory() {
  history = [];
  localStorage.removeItem('esQueryHistory');
  renderHistory();
}

// 初始化渲染
renderHistory();