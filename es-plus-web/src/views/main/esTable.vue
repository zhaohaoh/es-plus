<template>
  <div class="searchForm" style="max-height: 200px; margin-left: -15px">
    <el-row
      v-for="(item, index) in searchItems"
      :key="index"
      style="width: 100%; margin-bottom: 5px"
      class="custom-margin"
    >
      <el-col :span="3">
        <el-select v-model="item.mustType" placeholder="请选择">
          <el-option label="must" value="must" />
          <el-option label="should" value="should" />
          <el-option label="mustNot" value="mustNot" />
        </el-select>
      </el-col>
      <el-col :span="3">
        <el-select filterable v-model="item.field" placeholder="请选择字段">
          <el-option
            v-for="field in currentMapping"
            :key="field"
            :label="field"
            :value="field"
          />
        </el-select>
      </el-col>
      <el-col :span="3">
        <el-select v-model="item.searchType" placeholder="请选择">
          <el-option label="terms" value="terms" />
          <el-option label="match" value="match" />
          <el-option label="wildcard" value="wildcard" />
          <el-option label="lt" value="lt" />
          <el-option label="le" value="le" />
          <el-option label="gt" value="gt" />
          <el-option label="ge" value="ge" />
        </el-select>
      </el-col>
      <el-col :span="3">
        <el-input v-model="item.searchKeyword" clearable> </el-input>
      </el-col>
      <el-col :span="8">
        <el-button @click="addTerm" type="primary">+</el-button>
        <el-button @click="removeTerm(index)" type="danger">-</el-button>
      </el-col>
    </el-row>
    <el-row
      style="
        display: flex;
        justify-content: end;
        margin-top: -0px;
        margin-right: 40px;
        flex-direction: row;
      "
    >
      <el-col :span="1"></el-col>
      <el-col :span="3">
        <el-select
          filterable
          v-model="sortParam.sortName"
          placeholder="请选择字段"
        >
          <el-option
            v-for="field in currentMapping"
            :key="field"
            :label="field"
            :value="field"
          />
        </el-select>
      </el-col>

      <el-col :span="2">
        <el-select v-model="sortParam.sort" placeholder="请选择">
          <el-option label="DESC" value="DESC" />
          <el-option label="ASC" value="ASC" />
        </el-select>
      </el-col>
      <el-button @click="submitQuery" type="primary"> 查询 </el-button>
    </el-row>
  </div>

  <!-- 容器 -->
  <div class="container" style="margin-left: -20px; margin-top: 10px">
    <div class="buju">
      <el-aside width="250px">
        <el-scrollbar height="600px">
          <p
            v-for="item in indexData"
            :key="item"
            class="scrollbar-demo-item"
            @click="clickIndex(item)"
            :style="{
              background:
                selectIndex === item
                  ? '#84BEFC'
                  : 'var(--el-color-primary-light-9)',
            }"
          >
            {{ item }}
          </p>
        </el-scrollbar>
      </el-aside>

      <div
        class="right-container"
        v-show="tableData.length > 0"
        style="margin-top: -20px"
      >
        <el-row :gutter="10">
          <el-table
            :data="tableData"
            style="max-width: 1200px; max-height: 1000px"
            size="large"
          >
            <el-table-column
              v-for="item in tableHeader"
              :key="item.prop"
              :label="item.label"
              :prop="item.prop"
              width="150"
            />
          </el-table>
        </el-row>
        <el-row class="row-bg" justify="end">
          <el-col :span="1">
            <el-button type="success" plain @click="saveClick">新增</el-button>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
  <div class="downIndex">
    <el-input
      v-model="indexKeyword"
      placeholder="请输入索引名称"
      style="width: 240px; margin-top: 20px; margin-left: -15px"
      @change="onSearch"
    />
    <el-button
      type="primary"
      @click="clickMappings(selectIndex)"
      plain
      style="width: 240px; margin-top: 20px; margin-left: -15px"
      >查看映射</el-button
    >
  </div>

  <el-dialog
    v-model="dialogFormVisible"
    title="查看映射"
    style="
      max-width: 700px;
      position: relative;
      height: 750px;
      transform: translateY(-50px);
    "
  >
    <!-- <Codemirror
        v-model:value="code"
        :options="cmOptions"
        :width="width"
        border
        height="600px"
        readonly="true"
        @ready="onReady"
      /> -->
    <div>
      <JsonEditor
        v-model:value="code"
        height="600"
        styles="width: 100%"
        title="查看映射"
      />
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { getCurrentInstance, onMounted, reactive, ref } from "vue";
import type { FormProps } from "element-plus";
import { ElMessageBox } from "element-plus";
import options from "../../store/global";
import { ElMessage } from "element-plus";

let dialogFormVisible = ref(false);
const code = ref("");

const indexKeyword = ref("");

const onSearch = () => {
  getIndices(indexKeyword.value);
};

const searchItems = ref([
  {
    mustType: "must",
    field: "",
    searchType: "terms",
    searchKeyword: "",
    sortName: "_id",
    sort: "DESC",
  },
]);

const sortParam = ref({
  sortName: "_id",
  sort: "DESC",
});

const addTerm = () => {
  searchItems.value.push({
    mustType: "must",
    field: "",
    searchType: "terms",
    searchKeyword: "",
    sortName: "",
    sort: "",
  });
};

const removeTerm = (index) => {
  searchItems.value.splice(index, 1);
};

const tableData = ref([]);

const mappings = ref([]);

const currentMapping = ref();

const indexData = ref([]);

const { proxy } = getCurrentInstance() as any;

const clickMappings = async (index) => {
  await getMapping(index);
  // jsonView.value = res;

  dialogFormVisible.value = true;
  selectIndex.value = index;
};

const getMapping = async (index) => {
  const param = {
    indexName: index,
  };
  let res = await proxy.$api.esIndex.getMapping(param);

  code.value = JSON.stringify(res, null, 2);
};

const selectIndex = ref();

const clickIndex = (index) => {
  selectIndex.value = index;
  currentMapping.value = mappings.value[selectIndex.value];
  submitQuery();
};

const getIndices = async (keyword) => {
  const param = {
    keyword: keyword,
  };
  let res = await proxy.$api.esIndex.indexList(param);

  mappings.value = res.flatMappings;

  indexData.value = res.indices;
};

let tableHeader = [];
const dsl = ref("Es.chainQuery()");

onMounted(() => {
  // getList();
  getIndices("");
});

const submitQuery = () => {
  if (!selectIndex.value || selectIndex.value == undefined) {
    ElMessage("请选择索引");
    return;
  }
  getList();
};

// 获取分页
const getList = async () => {
  // 构造查询语句
  let queryDsl = dsl.value + '.index("' + selectIndex.value + '")';

  searchItems.value.forEach((i) => {
    if (i && i.searchType && i.field && i.searchKeyword) {
      const mustType = i.mustType;
      queryDsl += "." + mustType + "()";
      const isStr = checkType(i.searchKeyword);
      let searchKeyword = i.searchKeyword;
      if (isStr) {
        searchKeyword = '"' + i.searchKeyword + '"';
      }
      queryDsl +=
        "." + i.searchType + '("' + i.field + '",' + searchKeyword + ")";
    }
  });

  queryDsl =
    queryDsl +
    '.sortBy("' +
    sortParam.value.sort +
    '","' +
    sortParam.value.sortName +
    '")';
  queryDsl += ".search(10)";
  console.log(queryDsl);
  eplQuery(queryDsl);
};

function checkType(value) {
  return isNaN(value);
}

const eplQuery = async (epl) => {
  const param = {
    epl: epl,
  };
  let res = await proxy.$api.tools.eplQuery(param);

  // const jsonObject = JSON.parse(res);
  const list = res.hits.hits;

  const source = list.map((item) => item._source);
  //列表数据
  if (source != null && source.length > 0) {
    //列表数据字段名
    let keys = Object.keys(source[0]);
    const entries = Object.entries(source[0]);

    // 1. 收集需要前置的键（保持entries顺序）
    const keysToFront = [];
    entries.forEach(([key, value]) => {
      if (value != null && value !== "") {
        // 更严格的非空判断
        keysToFront.push(key);
      }
    });

    // 2. 过滤并重组键数组
    keys = keys.filter((key) => !keysToFront.includes(key)); // 保留未匹配的键
    keys.unshift(...keysToFront); // 将目标键添加到开头

    const idIndex = keys.indexOf("id");
    if (idIndex > -1) {
      // 1. 移除 id
      const [id] = keys.splice(idIndex, 1);
      // 2. 插入到数组开头
      keys.unshift(id);
    }

    tableHeader.length = 0;

    keys.forEach((k) => {
      tableHeader.push({
        prop: k,
        label: k,
      });
    });
    console.log("表头" + keys);
    console.log("表头:::" + JSON.stringify(tableHeader));
  }

  tableData.value = source;
  console.log(tableData.value);
};
</script>

<style scoped>
.buju {
  display: flex;
  flex-direction: row;
}
.el-row {
  margin-bottom: 20px;
  margin-right: 10px;
}
.el-row:last-child {
  margin-bottom: 0;
}
.el-col {
  border-radius: 4px;
  margin-right: 10px;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
}
.scrollbar-demo-item {
  /* 文字换行组合技 */
  word-break: break-all; /* 强制任意字符换行 */
  overflow-wrap: break-word; /* 单词边界换行 */
  white-space: pre-wrap; /* 保留空白符并换行 */

  padding: 0px 10px;
  /* 确保 padding 不会增加元素总宽度
保持容器始终为 240px 宽度*/
  /* box-sizing: border-box; */

  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  margin: 5px 5px 5px 5px;
  border-radius: 4px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-size: 15px;
  word-wrap: break-word;
}
.right-container {
  margin-left: 10px;
}

.el-form-item__content {
  display: flex;
  /* gap: 10px; */
}
.searchForm {
  display: flex;
  flex-direction: column;
  margin-top: -20px;
}
.custom-margin {
  margin-top: 1px; /* 上边距 */
  margin-bottom: 1px; /* 下边距 */
}
.el-input__inner {
  padding: 12px 15px;
}
.el-input {
  width: 100%;
}
.el-input__inner {
  width: 100%;
}

.el-input__inner {
  position: relative;
}
.downIndex {
  display: flex;
  flex-direction: column;
}
</style>
