<template>
  <div class="container">
    <el-row :gutter="10">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column
          v-for="item in tableHeader"
          :key="item.prop"
          :label="item.label"
          :prop="item.prop"
        />

        <el-table-column prop="address" label="操作" width="180">
          <template #default="scope">
            <el-button type="success" plain>编辑</el-button>
            <el-button
              type="danger"
              @click="clickDelete(scope.$index, scope.row)"
              plain
              >删除</el-button
            >
          </template>
          <!-- </el-col> -->
        </el-table-column>
      </el-table>
    </el-row>
    <el-row class="row-bg" justify="end">
      <el-col :span="1">
        <el-button type="success" plain @click="saveClick">新增</el-button>
      </el-col>
      <!-- <el-col :span="1">
        <el-button type="success" plain>编辑</el-button>
      </el-col> -->
    </el-row>
  </div>

  <!-- 编辑的弹窗 -->
  <el-dialog
    v-model="dialogFormVisible"
    title="新增链接"
    :before-close="handleCancel"
    style="max-width: 660px"
  >
    <el-form
      :inline="true"
      :model="esClient"
      ref="clientForm"
      label-width="100px"
      style="max-width: 660px"
      label-position="top"
    >
      <el-form-item label="唯一英文标识" prop="name" style="width: 100%">
        <el-col :span="24">
          <el-input v-model="esClient.unikey" />
        </el-col>
      </el-form-item>
      <el-form-item label="连接中文名" prop="name" style="width: 100%">
        <el-col :span="24">
          <el-input v-model="esClient.name" />
        </el-col>
      </el-form-item>
      <el-form-item label="链接地址" prop="address" style="width: 100%">
        <el-col :span="24">
          <el-input v-model="esClient.address" />
        </el-col>
      </el-form-item>
      <el-form-item label="协议" prop="schema" style="width: 100%">
        <el-col :span="24">
          <el-input v-model="esClient.schema" />
        </el-col>
      </el-form-item>
      <el-form-item label="Username" prop="username" style="width: 100%">
        <el-col :span="24">
          <el-input v-model="esClient.username" />
        </el-col>
      </el-form-item>
      <el-form-item label="Password" prop="password" style="width: 100%">
        <el-col :span="24">
          <el-input v-model="esClient.password" />
        </el-col>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="save"> 保存 </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { getCurrentInstance, onMounted, reactive, ref } from "vue";
import type { FormProps } from "element-plus";
import { ElMessageBox } from "element-plus";
import options from "../../store/global";

const labelPosition = ref<FormProps["labelPosition"]>("right");

const esClient = reactive({
  id: null,
  unikey: "",
  name: "",
  address: "http://",
  username: "",
  password: "",
  schema: "http",
});

const tableData = ref([]);

const { proxy } = getCurrentInstance() as any;

const tableHeader = [
  {
    prop: "id",
    label: "id",
  },
  {
    prop: "unikey",
    label: "唯一英文标识",
  },
  {
    prop: "name",
    label: "名称",
  },
  {
    prop: "address",
    label: "集群地址",
  },
  {
    prop: "username",
    label: "用户名称",
  },
  {
    prop: "password",
    label: "密码",
  },
  {
    prop: "schema",
    label: "模式",
  },
];
let dialogFormVisible = ref(false);

const save = async () => {
  proxy.$refs.clientForm.validate(async (valid) => {
    dialogFormVisible.value = false;
    console.log(esClient);
    esClientSave(esClient);
    // elMessage.success();
    proxy.$refs.clientForm.resetFields();
  });
};

const saveClick = () => {
  dialogFormVisible.value = true;
};

const clickDelete = async (index, param) => {
  console.log(param.id);
  ElMessageBox.confirm("你确定删除吗?", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
    .then(() => {
      esClientDelete(param.id);
    })
    .catch(() => {});
};

onMounted(() => {
  getList();
});

// 获取分页
const getList = async () => {
  let res = await proxy.$api.esClient.esClientList();
  if (res && res.length > 0) {
    options.value.length = 0;

    res.forEach((element, index) => {
      options.value[index] = element;
    });
    tableData.value = res;

    // const newArray = tableData.value.map((item) => {
    //   return {
    //     label: item.name,
    //     value: item,
    //     id: item.id,
    //   };
    // });

    // options.value.length = 0;

    // console.log("22223" + JSON.stringify(options.value));
    // newArray.forEach((element, index) => {
    //   options.value[index] = element;
    // });
    // console.log("3" + JSON.stringify(options.value));
  }
};

// 保存
const esClientSave = async (data) => {
  let res = await proxy.$api.esClient.esClientSave(data);
  getList();
};

// 删除
const esClientDelete = async (data) => {
  const param = {
    id: data,
  };
  let res = await proxy.$api.esClient.esClientDelete(param);
  getList();
};

// 取消
const handleCancel = () => {
  dialogFormVisible.value = false;
  proxy.$refs.clientForm.resetFields();
};
</script>

<style scoped>
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
</style>
