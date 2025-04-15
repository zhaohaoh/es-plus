<template>
  <div class="container">
    <div class="searchInput">
      <el-input
        v-model="keyword"
        placeholder="请输入索引名称"
        style="width: 300px"
        @change="onSearch"
      />
    </div>
    <el-scrollbar height="700px">
      <div v-for="item in data" :key="item.index" class="scrollbar-demo-item">
        <div class="index-item">
          <span style="font-size: 28px; font-weight: bold">{{
            item.index
          }}</span>

          <span class="health-font">health:</span
          ><span class="health" :style="{ background: item.health }"> </span>
          <span class="health-font">{{ item.health }}</span>

          <span class="health-font">status:</span>
          <span class="health-font">open</span>
        </div>
        <div class="index-size">size:{{ item["store.size"] }}</div>
        <!-- <div class="index-size">shard:</div>
        <div class="index-size">repice:</div> -->
        <div class="index-size">docs:{{ item["docs.count"] }}</div>
        <div class="index-size">delete_docs:{{ item["docs.deleted"] }}</div>

        <div class="editClass">
          <el-button type="danger" @click="clickDelete(item.index)" plain
            >删除</el-button
          >
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script lang="ts" setup>
import { getCurrentInstance, onMounted, reactive, ref } from "vue";
import type { FormProps } from "element-plus";
import { ElMessageBox } from "element-plus";
import options from "../../store/global";

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

const data = ref([]);

const { proxy } = getCurrentInstance() as any;

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
  console.log("结果" + JSON.stringify(data.value));
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
  color: #165dff;
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
</style>
