import cookies from "../util/cookie";
// 定义一个用户存储模块
const user = {
  // 定义为user的命名空间 那么它的状态会被存储在全局状态的user属性下， mutations会被注册为user/mutations
  namespaced: true,
  state: {
    //折叠还是展开菜单
    isCollapse: false,
    token: "",
    menu: [],
    currentMenu: null,
  },
  mutations: {
    updateCollapse(state, payload) {
      state.isCollapse = !state.isCollapse;
    },
    setCurrentMenu(state, value) {
      state.currentMenu = value;
    },
    //设置token
    setUserInfo(state, value) {
      // 设置访问令牌
      state.token = value.access_token;
      state.refreshToken = value.refresh_token;
      //后面要乘以1000毫秒
      let time = new Date(new Date().getTime() + value.expires_in);
      cookies.set("token", value.access_token, {
        expires: time,
      });
      cookies.set("refreshToken", value.refresh_token);
      //设置菜单
      state.menu = value.menu;
      console.log(value);
      localStorage.setItem("menu", JSON.stringify(value.menu));
    },
    //退出登录时删除token
    removeUserInfo(state) {
      // 设置访问令牌
      state.token = "";
      state.menu = [];
      cookies.remove("token");
      cookies.remove("refreshToken");
      localStorage.removeItem("menu");
    },
    // 初始化菜单
    initMenu(state, router) {
      if (!localStorage.getItem("menu")) {
        return;
      }
      let menu = JSON.parse(localStorage.getItem("menu"));
      state.menu = menu;

      let menusRouter = [];
      // 动态路由开始
      const modules = import.meta.glob("../views/*/*.vue");
      menu.forEach((item) => {
        if (item.children) {
          item.children.forEach((item) => {
            if (item.path) {
              // 根据前端路由获取vue路由的name
              let str = item.path.substring(1, item.path.length);
              item.name = str.replace("/", "-");
              let url = `../views/${item.url}.vue`;
              // item.component = () => import(url);
              item.component = modules[url];
              console.log(item);
              menusRouter.push(item);
            }
          });
        } else {
          if (item.path) {
            let url = `../views/${item.url}.vue`;
            item.component = modules[url];
            // 根据前端路由获取vue路由的name
            let str = item.path.substring(1, item.path.length);
            item.name = str.replace("/", "-");
            menusRouter.push(item);
          }
        }
      });
      menusRouter.forEach((item) => {
        router.addRoute("main", item);
      });
      // 动态路径结束
    },
  },
};

export default user;
