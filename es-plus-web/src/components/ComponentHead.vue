<template>
  <el-row class="row-bg">
    <el-col :span="2" :offset="19">
      <el-select
        v-model="value"
        class="m-2"
        placeholder="Select"
        style="width: 225px"
      >
        <el-option
          v-for="item in options"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-col>
  </el-row>
</template>

<script lang="ts" setup>
import { ref } from "vue";

const value = ref("");

const options = [
  {
    value: "dz_product_index",
    label: "dz_product_index",
  },
  {
    value: "Option2",
    label: "Option2",
  },
  {
    value: "Option3",
    label: "Option3",
  },
  {
    value: "Option4",
    label: "Option4",
  },
  {
    value: "Option5",
    label: "Option5",
  },
];
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
