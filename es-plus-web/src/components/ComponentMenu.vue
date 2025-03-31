<template>
  <!-- :collapse-transition="false" 关闭伸缩的动画 -->
  <el-menu
    default-active="2"
    background-color="#F2F3F5"
    class="el-menu-vertical-demo"
    :collapse="isCollapse"
    @open="handleOpen"
    @close="handleClose"
    :default-active="$route.path"
    router
  >
    <el-menu-item index="1">
      <el-icon><icon-menu /></el-icon>
      <template #title>索引管理</template>
    </el-menu-item>
    <el-menu-item index="/search">
      <el-icon><icon-menu /></el-icon>
      <template #title>基础搜索</template>
    </el-menu-item>
    <el-menu-item index="3">
      <el-icon><icon-menu /></el-icon>
      <template #title>高级搜索</template>
    </el-menu-item>
    <el-menu-item index="/esAddress">
      <el-icon><icon-menu /></el-icon>
      <template #title>链接管理</template>
    </el-menu-item>
  </el-menu>
</template>

<style>
:root {
  --el-menu-active-color: #86909c;
  --el-menu-hover-text-color: #165dff;
}
/* 激活后的字体颜色和背景颜色 */
.el-menu-item.is-active {
  color: #165dff !important;
  background-color: #f2f3f5;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 850px;
  background-color: #ffffff;
  /* color: #545c64 !important; */
}
.icons {
  width: 28px;
  height: 28px;
}
.el-menu-item {
  width: 200px;
  color: #4e5969;
}

.el-menu {
  border-right: none;
}
</style>

<script>
import { onMounted, ref, getCurrentInstance } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
export default {
  setup() {
    // export的时候是{}则必须用{}  否则就得去掉{}
    const router = useRouter();
    const store = useStore();
    const { proxy } = getCurrentInstance();
    let noChild = ref([]);
    let hasChild = ref([]);
    // 点击菜单跳转路由
    const openMenu = (item) => {
      store.commit("user/setCurrentMenu", item);
      router.push(item.path);
    };

    // const getList = async () => {
    //   let a = await proxy.$api.user.userMenuList();
    //   return a;
    // };

    // 页面加载完执行添加菜单
    onMounted(() => {
      let menu = store.state.user.menu;
      noChild.value = menu.filter((item) => item.type === "MENU");
      console.log(noChild);
      hasChild.value = menu.filter((item) => item.type === "CATALOG");
      console.log(hasChild);
    });

    return {
      noChild,
      hasChild,
      openMenu,
    };
  },
};
</script>
