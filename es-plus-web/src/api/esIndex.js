// 用户模块的接口
import api from "./request.js";

const esIndex = {
  indexList(params) {
    return api.get({ url: "/es/index/list", data: params });
  },
  getIndex(params) {
    return api.get({ url: "/es/index/getIndex", data: params });
  },

  getIndexStat(params) {
    return api.post({ url: "/es/index/getIndexStat", data: params });
  },

  getIndexHealth(params) {
    return api.post({ url: "/es/index/getIndexHealth", data: params });
  },
  getIndices(params) {
    return api.get({ url: "/es/index/getIndices", data: params });
  },
  getMapping(params) {
    return api.get({ url: "/es/index/getMapping", data: params });
  },
  putMapping(params) {
    return api.postParam({ url: "/es/index/putMapping", data: params });
  },
  createAlias(params) {
    return api.postParam({ url: "/es/index/createAlias", data: params });
  },
  removeAlias(params) {
    return api.postParam({ url: "/es/index/removeAlias", data: params });
  },
  createIndex(params) {
    return api.post({ url: "/es/index/createIndex", data: params });
  },
  deleteIndex(params) {
    return api.deleteParam({ url: "/es/index/deleteIndex", data: params });
  },
};

export default esIndex;
