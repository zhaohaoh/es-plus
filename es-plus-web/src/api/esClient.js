// 用户模块的接口
import api from "./request.js";

const esClient = {
  esClientSave(params) {
    return api.post({ url: "/es/client/save", data: params });
  },

  esClientList(params) {
    return api.post({ url: "/es/client/list", data: params });
  },
  esClientDelete(params) {
    return api.deleteParam({ url: "/es/client/delete", data: params });
  },
  get(params) {
    return api.get({ url: "/es/client/get", data: params });
  },
  testClient(params) {
    return api.post({ url: "/es/client/testClient", data: params });
  },
};

export default esClient;
