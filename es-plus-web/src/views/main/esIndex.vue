<template>
  <div class="container">
    <div class="searchInput">
      <el-row>
        <el-col :span="22">
          <el-input
            v-model="keyword"
            placeholder="请输入索引名称"
            style="width: 300px"
            @change="onSearch"
          />
        </el-col>
        <el-col :span="2" style="transform: translateX(10px)">
          <el-button type="primary" @click="clickCreateIndex()" plain
            >新建索引</el-button
          >
        </el-col>
      </el-row>
    </div>
    <el-scrollbar height="700px">
      <div v-for="item in data" :key="item.index" class="scrollbar-demo-item">
        <div class="index-item">
          <span style="font-size: 28px; font-weight: bold">{{
            item.index
          }}</span>

          <span class="health-font">别名:</span>
          <span class="health-font" style="font-size: 18px">{{
            item.alias
          }}</span>

          <span class="health-font">健康状态:</span
          ><span class="health" :style="{ background: item.health }"> </span>
          <span class="health-font">{{ item.health }}</span>

          <!-- <span class="health-font">索引状态:</span>
          <span class="health-font">open</span> -->
        </div>
        <span class="index-size"> size: {{ item["store.size"] }}</span>
        <span class="index-size">
          docs: {{ item["docs.count"] }} delete_docs:
          {{ item["docs.deleted"] }}</span
        >
        <span class="index-size">
          shards: {{ item["pri"] }} replice: {{ item["rep"] }}</span
        >
        <div class="editClass">
          <el-button type="primary" @click="clickMappings(item.index)" plain
            >设置映射</el-button
          >
          <el-button type="primary" @click="clickIndex(item.index)" plain
            >查看索引信息</el-button
          >
          <el-button
            type="primary"
            @click="clickSetAlias(item.index, item.alias)"
            plain
            >设置别名</el-button
          >
          <el-button type="danger" @click="clickDelete(item.index)" plain
            >删除</el-button
          >
        </div>
      </div>
    </el-scrollbar>

    <el-dialog
      v-model="createAliasVisble"
      title="设置别名"
      style="
        width: 600px;
        max-width: 600px;
        position: relative;
        height: 150px;
        transform: translateY(80px);
        transform: translateX(50px);
      "
    >
      <el-input v-model="changeAlias" />

      <el-button
        type="primary"
        plain
        @click="setAlias"
        style="position: absolute; right: 110px; bottom: 10px"
        >设置别名
      </el-button>
      <el-button
        type="primary"
        plain
        @click="removeAlias"
        style="position: absolute; right: 10px; bottom: 10px"
        >删除别名
      </el-button>
    </el-dialog>

    <el-dialog
      v-model="addIndexVisble"
      title="新建索引"
      style="
        width: 1200px;
        max-width: 1200px;
        position: relative;
        height: 650px;
        transform: translateY(-50px);
      "
    >
      <el-row>
        <el-col :span="8">
          <el-form
            :inline="true"
            :model="esIndexAdd"
            ref="clientForm"
            label-width="100px"
            style="max-width: 660px"
            label-position="top"
          >
            <el-form-item label="索引名" prop="indexName" style="width: 100%">
              <el-col :span="24">
                <el-input v-model="esIndexAdd.indexName" />
              </el-col>
            </el-form-item>
            <el-form-item label="别名" prop="alias" style="width: 100%">
              <el-col :span="24">
                <el-input v-model="esIndexAdd.alias" />
              </el-col>
            </el-form-item>
            <el-form-item
              label="分片数"
              prop="number_of_shards"
              style="width: 100%"
            >
              <el-col :span="24">
                <el-input v-model="esIndexAdd.number_of_shards" />
              </el-col>
            </el-form-item>
            <el-form-item
              label="副本数"
              prop="number_of_replicas"
              style="width: 100%"
            >
              <el-col :span="24">
                <el-input v-model="esIndexAdd.number_of_replicas" />
              </el-col>
            </el-form-item>
            <el-form-item
              label="最大查询数"
              prop="max_result_window"
              style="width: 100%"
            >
              <el-col :span="24">
                <el-input v-model="esIndexAdd.max_result_window" />
              </el-col>
            </el-form-item>
            <el-form-item
              label="刷新间隔秒数"
              prop="refresh_interval"
              style="width: 100%"
            >
              <el-col :span="24">
                <el-input v-model="esIndexAdd.refresh_interval" />
              </el-col>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="16">
          <div>
            <JsonEditor
              v-model:value="saveIndexMappings"
              height="450"
              styles="width: 50%"
              title="设置映射"
            />
          </div>
        </el-col>
      </el-row>
      <el-button
        type="primary"
        plain
        @click="saveIndex"
        style="position: absolute; right: 10px; bottom: 10px"
      >
        确认新增索引
      </el-button>
    </el-dialog>

    <el-dialog
      v-model="dialogFormVisible"
      title="设置映射"
      style="
        width: 1000px;
        max-width: 1000px;
        position: relative;
        height: 750px;
        transform: translateY(-50px);
      "
    >
      <div>
        <JsonEditor
          v-model:value="code"
          height="600"
          styles="width: 50%"
          title="新增映射结构"
          @update:value="handleValueUpdate"
        />
      </div>
      <el-button
        type="primary"
        plain
        @click="saveMappinng"
        style="position: absolute; right: 10px; bottom: 10px"
      >
        保存/修改映射
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
          v-model:value="indexInfo"
          height="600"
          styles="width: 100%"
          title="索引信息"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { getCurrentInstance, onMounted, reactive, ref, computed } from "vue";
import type { FormProps } from "element-plus";
import JsonEditor from "../../components/JsonEditor/index.vue";
import elMessage from "../../util/message";
// import VueJsonHelper from "@/views/indices/components/Helper.vue";
import { ElMessageBox } from "element-plus";
import options from "../../store/global";
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

const addIndexVisble = ref(false);
const createAliasVisble = ref(false);
const dialogIndexInfo = ref(false);

const saveIndexMappings = ref("{}");

const esIndexAdd = reactive({
  indexName: "",
  number_of_shards: 5,
  number_of_replicas: 1,
  max_result_window: 10000,
  refresh_interval: "1s",
  alias: "",
});

const esIndexAddMappings = null;

const code = ref("");
const indexInfo = ref("");

const changeAlias = ref("");

// const jsonStrData = computed(() => {
//   console.log("获取新的值" + code.value);
//   return code.value;
// });

const saveIndex = async () => {
  ElMessageBox.confirm("你确定保存吗?", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
    .then(() => {
      doSaveIndex(currentIndex.value);
      elMessage.success();
    })
    .catch(() => {});
};

const doSaveIndex = async (index) => {
  const param = {
    indexName: esIndexAdd.indexName,
    alias: esIndexAdd.alias,
    esSettings: { ...esIndexAdd },
    mapping: JSON.parse(saveIndexMappings.value),
  };

  let res = await proxy.$api.esIndex.createIndex(param);

  console.log(res);
  addIndexVisble.value = false;
};

const saveMappinng = async () => {
  ElMessageBox.confirm("你确定保存吗?", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
    .then(() => {
      putMapping(currentIndex.value, code.value);
      elMessage.success();
    })
    .catch(() => {});
};

const putMapping = async (indexName, mappings) => {
  const param = {
    indexName: indexName,
    mappings: mappings,
  };

  let res = await proxy.$api.esIndex.putMapping(param);
  dialogFormVisible.value = false;
  getIndices("");
};

const handleValueUpdate = (newValue) => {
  // 这里可以同步到父组件的 data
  code.value = newValue;
};
const keyword = ref("");
const esIndex = reactive({
  id: null,
  unikey: "",
  name: "",
  address: "http://",
  username: "",
  password: "",
  schema: "http",
});

let dialogFormVisible = ref(false);
let currentIndex = ref("");

const data = ref([]);

const { proxy } = getCurrentInstance() as any;

const clickMappings = async (index) => {
  await getMapping(index);
  // jsonView.value = res;

  dialogFormVisible.value = true;
  currentIndex.value = index;
};

const clickIndex = async (index) => {
  getIndex(index);
  dialogIndexInfo.value = true;
  currentIndex.value = index;
};

const clickSetAlias = async (index, alias) => {
  changeAlias.value = alias;
  createAliasVisble.value = true;
  currentIndex.value = index;
};

const setAlias = async () => {
  createAlias(currentIndex.value, changeAlias.value);
};

const createAlias = async (index, alias) => {
  const param = {
    index: index,
    alias: alias,
  };
  let res = await proxy.$api.esIndex.createAlias(param);
  createAliasVisble.value = false;
};
const removeAlias = async () => {
  deleteAlias(currentIndex.value, changeAlias.value);
};

const deleteAlias = async (index, alias) => {
  const param = {
    index: index,
    alias: alias,
  };
  let res = await proxy.$api.esIndex.removeAlias(param);
  createAliasVisble.value = false;
};

const clickCreateIndex = async () => {
  addIndexVisble.value = true;
};

const getIndex = async (index) => {
  const param = {
    index: index,
  };
  let res = await proxy.$api.esIndex.getIndex(param);

  indexInfo.value = JSON.stringify(res, null, 2);
};

const getMapping = async (index) => {
  const param = {
    indexName: index,
  };
  let res = await proxy.$api.esIndex.getMapping(param);

  code.value = JSON.stringify(res, null, 2);
};

onMounted(() => {
  getIndices(keyword.value);
});

const onSearch = () => {
  getIndices(keyword.value);
};

const getIndices = async (keyword) => {
  const param = {
    keyword: keyword,
  };
  let res = await proxy.$api.esIndex.getIndices(param);

  data.value = res;
};

const clickDelete = async (index) => {
  ElMessageBox.confirm("你确定删除索引" + index + "吗? 谨慎操作", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
    .then(() => {
      esIndexDelete(index);
    })
    .catch(() => {});
};

// 删除
const esIndexDelete = async (data) => {
  const param = {
    indexName: data,
  };
  let res = await proxy.$api.esIndex.deleteIndex(param);
  elMessage.success();
  getIndices(keyword.value);
};
</script>
<style scoped>
/* 在全局样式或组件样式中添加 */
.scrollbar-demo-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  height: 120px;
  margin: 20px;
  padding: 10px 10px 10px 10px;
  text-align: center;
  border-radius: 4px;
  background: #ffffff;
  border: 1px solid #e5e6eb;
  color: #165dff;
  /* 关键：设置相对定位 */
  position: relative;
}

.index-item {
  display: flex;
  align-items: flex-start;
  /* justify-content: center; */
  height: 30px;
  text-align: center;
  border-radius: 4px;
  background: #ffffff;
  color: #165dff;
}
.index-size {
  margin-top: 8px;
  color: black;
  font-size: 14px;
}

span {
  margin-right: 20px;
}
.health-font {
  /* color: ; */
  font-family: "Times New Roman", Times, serif;
  margin-top: 8px;
  margin-right: 5px;
}
.health {
  margin-top: 11px;
  height: 10px;
  width: 10px;
  background: green;
  margin-right: 5px;
}
.searchInput {
  margin-left: 21px;
}
.editClass {
  /* 关键：设置绝对定位 */
  position: absolute;
  bottom: 10px; /* 距离底部 10px */
  right: 10px; /* 距离右侧 10px */
}

.json-output {
  width: 100%;
  height: calc(100% - 50px);
  /* padding: 12px; */
  border: 1px solid #ddd;
  /* border-radius: 4px; */
  resize: none;
  font-family: "Courier New", monospace;
  white-space: pre-wrap;
  color: #333;
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
</style>
