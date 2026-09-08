/**
 * 环境配置文件
 * 一般在企业级项目里面有三个环境
 * 开发环境
 * 测试环境
 * 线上环境
 */
// 当前的环境
const env = import.meta.env.MODE || "dev";

const EnvConfig = {
  dev: {
    baseUrl: "http://localhost:8989",
  },
  test: {
    baseUrl: "http://localhost:8989",
  },
  pro: {
    baseUrl: "//future.com/api",
  },
  production: {
    // Windows 客户端和静态部署统一使用当前页面同源服务
    baseUrl: "",
  },
};

export default {
  env,
  //获取指定环境的数据
  ...(EnvConfig[env] || EnvConfig.production),
};
