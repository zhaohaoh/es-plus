// 用户模块的接口
import api from "./request.js";

const tools = {
  eplQuery(params) {
    return api.get({ url: "/es/esQuery/epl", data: params });
  },
  sqlQuery(params) {
    return api.get({ url: "/es/esQuery/sql", data: params });
  },
  dslQuery(params) {
    return api.get({ url: "/es/esQuery/dsl", data: params });
  },
  deleteByIds(params) {
    return api.delete({ url: "/es/deleteByIds", data: params });
  },
  updateBatch(params) {
    return api.put({ url: "/es/updateBatch", data: params });
  },
};

export default tools;
