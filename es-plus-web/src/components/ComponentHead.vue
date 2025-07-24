<template>
  <el-row class="row-bg">
    <el-col :span="2" :offset="19">
      <el-select
        v-model="selectClient"
        class="m-2"
        placeholder="Select"
        style="width: 225px; transform: translateX(60px)"
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
    </el-col>
  </el-row>
</template>

<script lang="ts" setup>
import { getCurrentInstance, onMounted, reactive, ref } from "vue";

const { proxy } = getCurrentInstance() as any;
import options from "../store/global";
import { ElMessage } from "element-plus";
const selectClient = ref();

const clickSelect = (item) => {
  localStorage.setItem("currentClient", item);
};

onMounted(() => {
   const client = localStorage.getItem("currentClient");
  if (!client) {
    ElMessage.error("请选择索引链接");
    return;
  }
  selectClient.value = client;
  getList();
});

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
</style>
