// 用户模块的接口
import api from "./request.js";

const tools = {
  eplQuery(params) {
    return api.get({ url: "/codeGeneratorConfig/page", data: params });
  },
  sqlQuery(params) {
    return api.get({ url: "/codeGeneratorConfig/page", data: params });
  },

  esClientSave(params) {
    return api.post({ url: "/es/client/save", data: params });
  },

  esClientList(params) {
    return api.post({ url: "/es/client/list", data: params });
  },
};

export default tools;
