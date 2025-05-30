// 用户模块的接口
import api from "./request.js";

const user = {
  doLogin(params) {
    return api.postParam({ url: "/user/doLogin", data: params });
  },
};

export default user;
