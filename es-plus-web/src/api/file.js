// 用户模块的接口
import api from "./request.js";

const file = {
  excelExport(params) {
    return api.post({ url: "/es/file/excelExport", data: params });
  },
};

export default file;
