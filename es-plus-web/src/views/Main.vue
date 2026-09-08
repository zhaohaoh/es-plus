<template>
  <div class="common-layout">
    <!-- 定义容器 响应式开发 -->
    <el-container class="app-shell">
      <el-aside width="200px" class="menu">
        <ComponentMenu></ComponentMenu>
      </el-aside>
      <el-divider direction="vertical" height="100%" />
      <el-container class="content-shell">
        <el-header class="app-header"><ComponentHead></ComponentHead></el-header>
        <el-main class="app-main"><RouterView></RouterView></el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script>
import { defineComponent } from "vue";
import ComponentMenu from "../components/ComponentMenu.vue";
import ComponentHead from "../components/ComponentHead.vue";
export default defineComponent({
  components: {
    ComponentMenu,
    ComponentHead,
  },
});
</script>

<style>
html,
body,
#app {
  background-color: #ffffff;
  width: 100%;
  height: 100%;
  overflow: hidden;
}
/* 分割线 */
.el-divider--vertical {
  height: 100%;
}

.common-layout {
  width: 100%;
  height: 100%;
  overflow: hidden;
}
.app-shell {
  width: 100%;
  height: 100%;
  overflow: hidden;
}
.content-shell {
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}
.app-header {
  flex-shrink: 0;
  overflow: hidden;
}
.app-main {
  min-width: 0;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
}
.el-aside menu {
  height: 100%;
}
.menu {
  flex-shrink: 0;
  background-color: #ffffff;
  height: 100%;
  overflow: hidden;
}
</style>
