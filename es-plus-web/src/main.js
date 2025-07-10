import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
// 一定要引入的样式不然看起来没问题实际上一堆坑
import "element-plus/dist/index.css";
import "./assets/scss/index.scss";
import store from "./store";
import api from "./api/api.js";

import JsonViewer from "vue3-json-viewer";
import "vue3-json-viewer/dist/index.css"; // 引入样式

import ElementPlus from "element-plus";

// 获取组件的props
const TableProps = ElTable.props;
const TableColumnProps = ElTableColumn.props;

// 修改默认props
// 全局el-table设置
TableProps.border = { type: Boolean, default: true }; // 边框线
// 全局el-table-column设置
TableColumnProps.align = { type: String, default: "center" }; // 居中
TableColumnProps.showOverflowTooltip = { type: Boolean, default: true }; // 文本溢出

const app = createApp(App);
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}
// 把api加入到vue的属性中$api中
app.config.globalProperties.$api = api;
// 这一步为啥要这么做不太清楚
// store.commit("user/initMenu", router);
// app.use(ElementPlus)
// 要在mount前面否则未渲染
//
app.use(router).use(store).use(JsonViewer).use(ElemnoentPlus);
app.mount("#app");
