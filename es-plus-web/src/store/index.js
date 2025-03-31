import { createStore } from "vuex";
import user from "../store/user";
// 创建一个新的 store 实例  组件间的数据共享调用
const store = createStore({
  modules: {
    user,
  },
});

export default store;
