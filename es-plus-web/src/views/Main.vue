<template>
  <div class="common-layout">
    <!-- 定义容器 响应式开发 -->
    <el-container>
      <el-aside width="200px" class="menu">
        <ComponentMenu></ComponentMenu>
      </el-aside>
      <el-divider direction="vertical" height="100%" />
      <el-container>
        <el-header><ComponentHead></ComponentHead></el-header>
        <el-main><RouterView></RouterView></el-main>
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
#app,
.el-container {
  background-color: #ffffff;
  height: 100%;
}
/* 分割线 */
.el-divider--vertical {
  height: 100%;
}

.common-layout {
  height: 100%;
}
.el-aside menu {
  height: 100%;
}
.menu {
  background-color: #ffffff;
  height: 100%;
}
</style>
