<template>
  <el-row class="row-bg" :gutter="20" align="middle" style="margin-left: 21px; margin-right: 21px;">
    <!-- 左侧统计信息 - 简单样式 -->
    <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
      <div class="stats-info">
        <span class="stats-text">共 <strong>{{ indexCount }}</strong> 个索引</span>
        <span class="stats-separator">|</span>
        <span class="stats-text">存储 <strong>{{ totalStorage }}</strong></span>
        <span class="stats-separator">|</span>
        <span class="stats-text">健康 <strong>{{ healthRate }}</strong></span>
      </div>
    </el-col>

    <!-- 右侧选择ES - 简单样式右对齐 -->
    <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
      <div style="display: flex; justify-content: flex-end;">
        <el-select
            v-model="selectClient"
            placeholder="选择ES"
            style="width: 225px"
            filterable
            @change="clickSelect"
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
    </el-col>
  </el-row>
</template>

<script lang="ts" setup>
import { getCurrentInstance, onMounted, reactive, ref, computed } from "vue";
import { DataBoard, Connection } from "@element-plus/icons-vue";

const { proxy } = getCurrentInstance() as any;
import options from "../store/global";
import { ElMessage } from "element-plus";
const selectClient = ref();
const indicesData = ref([]);

const clickSelect = (item) => {
  localStorage.setItem("currentClient", item);
};

// 统计数据计算
const indexCount = computed(() => indicesData.value.length);

const totalStorage = computed(() => {
  let totalBytes = 0;
  indicesData.value.forEach(item => {
    const sizeStr = item['store.size'] || '0b';
    const sizeMatch = sizeStr.match(/^([\d.]+)(\w+)$/i);
    if (sizeMatch) {
      const size = parseFloat(sizeMatch[1]);
      const unit = sizeMatch[2].toLowerCase();
      switch (unit) {
        case 'b': totalBytes += size; break;
        case 'kb': totalBytes += size * 1024; break;
        case 'mb': totalBytes += size * 1024 * 1024; break;
        case 'gb': totalBytes += size * 1024 * 1024 * 1024; break;
        case 'tb': totalBytes += size * 1024 * 1024 * 1024 * 1024; break;
      }
    }
  });

  if (totalBytes < 1024 * 1024 * 1024) return (totalBytes / (1024 * 1024)).toFixed(1) + 'MB';
  if (totalBytes < 1024 * 1024 * 1024 * 1024) return (totalBytes / (1024 * 1024 * 1024)).toFixed(1) + 'GB';
  return (totalBytes / (1024 * 1024 * 1024 * 1024)).toFixed(1) + 'TB';
});

const healthRate = computed(() => {
  if (indicesData.value.length === 0) return '0%';
  let healthyCount = 0;
  indicesData.value.forEach(item => {
    if (item.health && item.health.toLowerCase() === 'green') {
      healthyCount++;
    }
  });
  return Math.round(healthyCount / indicesData.value.length * 100) + '%';
});

onMounted(() => {
  const client = localStorage.getItem("currentClient");
  if (!client) {
    ElMessage.error("请选择索引链接");
    return;
  }
  selectClient.value = client;
  getList();
  getIndicesData();
});

// 获取索引数据
const getIndicesData = async () => {
  try {
    const param = { keyword: "" };
    let res = await proxy.$api.esIndex.getIndices(param);
    if (res) {
      indicesData.value = res;
    }
  } catch (error) {
    console.log("获取索引数据失败:", error);
  }
};

// 获取es客户端list
const getList = async () => {
  if (!selectClient.value) {
    ElMessage.error("请选择索引链接");
    return;
  }
  let res = await proxy.$api.esClient.esClientList();
  if (res && res.length > 0) {
    options.value.length = 0;

    res.forEach((element, index) => {
      options.value[index] = element;
    });
  }
};
</script>

<style>
.el-row {
  margin-bottom: 0px;
  margin-top: 20px;
}
.el-row:last-child {
  margin-bottom: 0;
}
.el-col {
  border-radius: 4px;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
}

/* 简单统计信息样式 */
.stats-info {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
  padding: 16px 0;
}

.stats-text {
  color: #606266;
}

.stats-text strong {
  color: #409eff;
  font-weight: 600;
  margin: 0 2px;
}

.stats-separator {
  margin: 0 12px;
  color: #dcdfe6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-info {
    font-size: 13px;
    padding: 12px 0;
  }

  .stats-separator {
    margin: 0 8px;
  }
}
</style>