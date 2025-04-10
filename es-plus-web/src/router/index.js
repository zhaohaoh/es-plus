import { createRouter, createWebHashHistory } from "vue-router";
// vue路由管理
const routes = [
  {
    path: "/",
    name: "main",
    // redirect: "/main",
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
    ],

    //子路由,其他路由在页面中动态添加

    // {
    //   path: "/queue",
    //   name: "queue",
    //   component: () => import("../views/mq/Queue.vue"),
    // },
    // {
    //   path: "/consumer/group",
    //   name: "consumerGroup",
    //   component: () => import("../views/mq/ConsumerGroup.vue"),
    // },
    // {
    //   path: "/consumer",
    //   name: "consumer",
    //   component: () => import("../views/mq/Consumer.vue"),
    // },
    // {
    //   path: "/historyMessage",
    //   name: "historyMessage",
    //   component: () => import("../views/mq/HistoryMessage.vue"),
    // },
  },
  // 登录的路由
  // {
  //   path: "/login",
  //   name: "login",
  //   component: () => import("../views/Login.vue"),
  // },
];

const router = createRouter({
  // 使用hash路由
  history: createWebHashHistory(),
  routes,
});

export default router;
