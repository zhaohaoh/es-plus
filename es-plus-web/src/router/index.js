import { createRouter, createWebHashHistory } from "vue-router";
// vue路由管理
const routes = [
  {
    path: "/",
    name: "main",
    redirect: "/login",
    component: () => import("../views/Main.vue"),
    meta: { title: "首页" },
    children: [
      {
        path: "/search",
        name: "search",
        component: () => import("../views/main/search.vue"),
      },
      {
        path: "/esAddress",
        name: "esAddress",
        component: () => import("../views/main/esAddress.vue"),
      },
      {
        path: "/esIndex",
        name: "esIndex",
        component: () => import("../views/main/esIndex.vue"),
      },
      {
        path: "/esTable",
        name: "esTable",
        component: () => import("../views/main/esTable.vue"),
      },
    ],
  },
  // 登录的路由
  {
    path: "/login",
    name: "login",
    component: () => import("../views/main/login.vue"),
  },
];

const router = createRouter({
  // 使用hash路由
  history: createWebHashHistory(),
  routes,
});

export default router;
