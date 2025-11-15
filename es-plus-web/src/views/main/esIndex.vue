<template>
  <div class="container">
    <div class="searchInput">
      <el-row :gutter="20" align="middle">
        <!-- 左侧搜索组 -->
        <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
          <div class="search-group">
            <el-icon class="group-icon"><Search /></el-icon>
            <span class="group-title">搜索</span>
            <el-input
                v-model="keyword"
                placeholder="请输入索引名称"
                style="flex: 1; max-width: 300px"
                @change="onSearch"
            />
            <el-button
                type="primary"
                @click="onSearch()"
                plain
                style="margin-left: 10px"
            >
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </div>
        </el-col>

        <!-- 右侧操作组 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <div class="action-group">
            <el-icon class="group-icon"><Operation /></el-icon>
            <span class="group-title">操作</span>
            <div class="action-buttons">
              <el-button type="primary" @click="clickClusterInfo()" plain>
                <el-icon><Connection /></el-icon>
                集群信息
              </el-button>
              <el-button type="primary" @click="clickCreateIndex()" plain>
                <el-icon><Plus /></el-icon>
                新建索引
              </el-button>
            </div>
          </div>
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
          <el-button
              type="primary"
              plain
              @click="clickReindex(item.index, item.alias)"
          >迁移索引</el-button
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
        v-model="reindexVisble"
        :title="'当前索引:' + currentIndex"
        :style="{ height: `${reindexDialogHeigt}px`, width: `600px` }"
    >
      <el-button-group style="margin-top: -0px; margin-bottom: 11px">
        <el-button
            type="primary"
            plain
            @click="
            (jumpDataMove = 1),
              (reindexDialogHeigt = 230),
              (moveName = '点击复制')
          "
        >索引复制</el-button
        >
        <el-button
            type="primary"
            plain
            @click="
            (jumpDataMove = 2),
              (reindexDialogHeigt = 200),
              (moveName = '点击迁移')
          "
        >同源迁移</el-button
        >
        <el-button
            type="primary"
            plain
            @click="
            (jumpDataMove = 3),
              (reindexDialogHeigt = 300),
              (moveName = '点击迁移')
          "
        >跨数据源</el-button
        >
      </el-button-group>
      <div
          v-show="jumpDataMove == 1 || jumpDataMove == 3"
          style="margin-bottom: 10px"
      >
        <div class="dataMoveClient-form">
          目标数据源:
          <el-select
              v-model="dataMoveClient"
              class="m-2"
              placeholder="选择ES"
              style="width: 225px"
              filterable
          >
            <el-option
                v-for="item in options"
                :key="item.id"
                :label="`${item.unikey} (${item.name})`"
                :value="item.unikey"
                :valueKey="item.unikey"
            />
          </el-select>
        </div>
      </div>
      <div v-show="jumpDataMove == 3" class="dataMoveClient-form">
        最大迁移数量:
        <el-input
            placeholder="本次迁移最大限制数量"
            v-model="moveSize"
            style="display: inline"
        />
      </div>
      <el-input placeholder="目标索引" v-model="reindexName" />
      <el-button
          type="primary"
          plain
          @click="doMove"
          style="position: absolute; right: 110px; bottom: 10px"
      >{{ moveName }}
      </el-button>
      <el-button
          type="primary"
          plain
          @click="clickReindexTaskList"
          style="position: absolute; right: 10px; bottom: 10px"
      >任务明细
      </el-button>
    </el-dialog>

    <el-dialog
        v-model="reindexTableVisble"
        title="迁移任务明细"
        style="
        width: 1500px;
        position: relative;
        height: 600px;
        transform: translateY(80px);
        transform: translateX(50px);
      "
    >
      <el-table
          :data="reindexTableData"
          style="max-width: 1500px; max-height: 1000px; min-height: 300px"
          size="large"
      >
        <el-table-column
            v-for="item in tableHeader"
            :key="item.prop"
            :label="item.label"
            :prop="item.prop"
            width="180"
        >
          <template #default="scope">
            {{
              item.prop === "type"
                  ? scope.row.type === 1
                      ? "同源迁移"
                      : scope.row.type === 2
                          ? "跨源迁移"
                          : "同源迁移"
                  : scope.row[item.prop]
            }}
          </template>
        </el-table-column>
      </el-table>
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
      <el-button
          type="primary"
          plain
          @click="settingsOpen"
          style="position: absolute; right: 10px; bottom: 10px"
      >
        索引配置编辑
      </el-button>
    </el-dialog>

    <el-dialog
        v-model="dialogSettings"
        title="索引可修改配置"
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
            v-model:value="settings"
            height="600"
            styles="width: 100%"
            title="索引配置信息"
        />
      </div>
      <el-button
          type="primary"
          plain
          @click="updateSettings"
          style="position: absolute; right: 10px; bottom: 10px"
      >
        保存
      </el-button>
    </el-dialog>

    <el-dialog
        v-model="clusterInfoVisble"
        title="集群信息"
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
            v-model:value="cluseterInfo"
            height="600"
            styles="width: 100%"
            title="集群信息"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { getCurrentInstance, onMounted, reactive, ref, computed } from "vue";
import type { FormProps } from "element-plus";
import { Search, Connection, Plus, Operation } from "@element-plus/icons-vue";
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
const dataMoveClient = ref();
const jumpDataMove = ref(1);

const clusterInfoVisble = ref(false);
const addIndexVisble = ref(false);
const createAliasVisble = ref(false);
const dialogIndexInfo = ref(false);
const reindexVisble = ref(false);
const reindexTableVisble = ref(false);
const dialogSettings = ref(false);

const saveIndexMappings = ref("{}");
const reindexName = ref("");
const reindexTableData = ref([]);
const moveSize = ref(100000);
const reindexDialogHeigt = ref(230);

const moveName = ref("点击迁移");

const cluseterInfo = ref("");

const tableHeader = [
  {
    prop: "id",
    label: "id",
  },
  {
    prop: "sourceClient",
    label: "来源数据源",
  },
  {
    prop: "targetClient",
    label: "目标数据源",
  },
  {
    prop: "sourceIndex",
    label: "来源索引",
  },
  {
    prop: "targetIndex",
    label: "目标索引",
  },
  {
    prop: "taskId",
    label: "任务id",
  },
  {
    prop: "createTime",
    label: "创建时间",
  },
  {
    prop: "createUid",
    label: "创建人id",
  },
  {
    prop: "completed",
    label: "完成",
  },
  {
    prop: "type",
    label: "任务类型",
  },
  {
    prop: "taskJson",
    label: "任务明细json",
  },
];

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
const settings = ref("");

// const jsonStrData = computed(() => {
//   console.log("获取新的值" + code.value);
//   return code.value;
// });

const settingsOpen = async () => {
  dialogSettings.value = true;
  const setting = JSON.parse(indexInfo.value).settingsObj;

  Object.keys(setting).forEach((key) => {
    if (key.startsWith("index.")) {
      // 检查键是否以 "index." 开头
      const newKey = key.replace(/^index\./, ""); // 删除开头的 "index."
      setting[newKey] = setting[key]; // 创建新键值对
      if (newKey == "creation_date") {
        delete setting[newKey]; // 删除旧键
      }
      if (newKey == "uuid") {
        delete setting[newKey]; // 删除旧键
      }
      if (newKey == "version.created") {
        delete setting[newKey]; // 删除旧键
      }
      if (newKey == "number_of_shards") {
        delete setting[newKey]; // 删除旧键
      }
      if (newKey == "provided_name") {
        delete setting[newKey]; // 删除旧键
      }
      if (newKey == "routing.allocation.include._tier_preference") {
        delete setting[newKey]; // 删除旧键
      }
      delete setting[key]; // 删除旧键
    }
    if (setting["max_result_window"] == null) {
      setting["max_result_window"] = "10000";
    }
  });

  settings.value = JSON.stringify(setting, null, 2);
  console.log("aaa" + JSON.stringify(settings.value));
};

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
  onSearch();
};

const updateSettings = async () => {
  ElMessageBox.confirm("你确定保存吗?", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
  .then(() => {
    doUpdateSettings(currentIndex.value, settings.value);
    elMessage.success();
  })
  .catch(() => {});
};

const doUpdateSettings = async (indexName, settings) => {
  const param = {
    indexName: indexName,
    settings: settings,
  };

  let res = await proxy.$api.esIndex.updateSettings(param);
  dialogFormVisible.value = false;
  getIndices("");
};

const doMove = async () => {
  if (jumpDataMove.value === 3) {
    doDataMove();
  } else if (jumpDataMove.value === 2) {
    doReindex();
  } else if (jumpDataMove.value === 1) {
    copyIndex();
  }
};

const copyIndex = async () => {
  ElMessageBox.confirm(
      "确认复制索引到 " + dataMoveClient.value + "." + reindexName.value + " 吗?",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      }
  )
  .then(() => {
    doCopyIndexApi();
  })
  .catch(() => {});
};

const doCopyIndexApi = async () => {
  const param = {
    targetClient: dataMoveClient.value,
    targetIndex: reindexName.value,
    sourceIndex: currentIndex.value,
  };

  let res = await proxy.$api.esIndex.copyIndex(param);
  reindexVisble.value = false;
  reindexName.value = "";
};

const doReindex = async () => {
  ElMessageBox.confirm("确认迁移到 " + reindexName.value + " 吗?", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
  .then(() => {
    reindexApi();
  })
  .catch(() => {});
};

const doDataMove = async () => {
  ElMessageBox.confirm(
      "确认迁移到 " + dataMoveClient.value + "." + reindexName.value + " 吗?",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      }
  )
  .then(() => {
    doDataMoveApi();
  })
  .catch(() => {});
};

const doDataMoveApi = async () => {
  const param = {
    targetClient: dataMoveClient.value,
    maxSize: moveSize.value,
    targetIndex: reindexName.value,
    sourceIndex: currentIndex.value,
  };

  let res = await proxy.$api.esIndex.indexDataMove(param);
  reindexVisble.value = false;
  reindexName.value = "";
};

const reindexApi = async () => {
  const param = {
    targetIndex: reindexName.value,
    sourceIndex: currentIndex.value,
  };

  let res = await proxy.$api.esIndex.reindex(param);
  reindexVisble.value = false;
  reindexName.value = "";
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
const clickReindexTaskList = async (index, alias) => {
  reindexTableVisble.value = true;
  const param = {
    sourceIndex: currentIndex.value,
  };
  let res = await proxy.$api.esIndex.reindexTaskList(param);

  reindexTableData.value = res;
};

const clickReindex = async (index, alias) => {
  reindexVisble.value = true;
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
  onSearch();
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
  onSearch();
};

const clickCreateIndex = async () => {
  addIndexVisble.value = true;
};

const clickClusterInfo = async () => {
  let res = await proxy.$api.esIndex.getIndexStat();
  cluseterInfo.value = JSON.stringify(res, null, 2);
  console.log(res);
  clusterInfoVisble.value = true;
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
  margin-bottom: 20px;
  margin-top: -10px;
}

/* 搜索组样式 - 与统计组高度一致 */
.search-group {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  height: 56px;
  box-sizing: border-box;
}

.search-group:hover {
  border-color: #409eff;
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.1);
}

/* 操作组样式 - 与连接组高度一致 */
.action-group {
  display: flex;
  align-items: center;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  height: 56px;
  box-sizing: border-box;
}

.action-group:hover {
  border-color: #409eff;
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.1);
}

/* 操作按钮容器 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

/* 强制设置按钮高度 */
.action-buttons :deep(.el-button) {
  height: 24px;
  padding: 0 12px;
  font-size: 12px;
}

.action-buttons :deep(.el-button .el-icon) {
  font-size: 12px;
}

/* 搜索组中的input和button高度控制 */
.search-group :deep(.el-input) {
  height: 24px;
}

.search-group :deep(.el-input .el-input__wrapper) {
  height: 24px;
  min-height: 24px;
}

.search-group :deep(.el-button) {
  height: 24px;
  padding: 0 12px;
  font-size: 12px;
}

.search-group :deep(.el-button .el-icon) {
  font-size: 12px;
}

/* 组图标样式 */
.group-icon {
  font-size: 18px;
  color: #409eff;
  margin-right: 8px;
}

/* 组标题样式 */
.group-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-right: 16px;
  white-space: nowrap;
}

/* 操作按钮容器 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .search-group,
  .action-group {
    margin-bottom: 10px;
    padding: 12px 16px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }

  .group-title {
    margin-right: 12px;
    font-size: 13px;
  }
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
.dataMoveClient-form {
  margin-bottom: 10px;
}
</style>