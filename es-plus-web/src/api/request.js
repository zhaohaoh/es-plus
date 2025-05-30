import axios from "axios";
import config from "../config";
import cookie from "../util/cookie";
import qs from "qs";
const NETWORK_ERROR = "网络请求异常,请稍后重试.....";
/**
 * axios的传参方式：
 * 1.url 传参 一般用于Get和Delete 实现方式：config.params={JSON}
 * 2.body传参 实现方式：config.data = {JSON}，且请求头为：headers: { 'Content-Type': 'application/json;charset=UTF-8' }
 * 3.表单传参 实现方式：config.data = qs.stringify({JSON})，且请求头为：且请求头为：headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' }
 */
const services = axios.create({
  // 全局前缀 获取配置文件中的
  baseURL: config.baseUrl,
  // 超时时间 10秒
  timeout: 10000,
});

// 请求头封装
services.interceptors.request.use((config) => {
  let token = cookie.get("token");
  if (token) {
    //如果没有才添加
    if (!config.headers["Authorization"]) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
  }
  const client = localStorage.getItem("currentClient");

  if (client) {
    console.log("请求头" + client);
    config.headers["currentEsClient"] = client;
  }

  const esToken = localStorage.getItem("es-plus-token");

  if (esToken) {
    console.log("请求头" + esToken);
    config.headers["satoken"] = esToken;
  }

  return config;
});

//返回体封装
services.interceptors.response.use(
  (response) => {
    const data = response.data;
    console.log("返回体:");
    console.log(response);
    if (data.code === "401" || response.status === 401) {
      window.location.href = "/#/login";
    }

    return data;
  },
  (error) => {
    console.log("错误");
    console.log(error);
    const data = error.response.data;
    if (data) {
      //刷新令牌过期
      if (data.code === "RT401") {
        window.location.href = "/#/login";
        ElMessage({
          type: "error",
          message: data.message || NETWORK_ERROR,
          showClose: true,
        });
      } else if (data.code === "401" || error.response.status === 401) {
        //普通令牌过期
        if (cookie.get("refreshToken")) {
          console.log("token expire doRefreshToken");
          //去服务端重新获取token
          refreshToken(error);
        } else {
          window.location.href = "/#/login";
          ElMessage({
            type: "error",
            message: data.message || NETWORK_ERROR,
            showClose: true,
          });
        }
      } else {
        ElMessage({
          type: "error",
          message: error.response.data.message,
        });
      }
    }
    //异步异常处理
    return Promise.reject(error);
  }
);

//服务端去刷新令牌
const refreshToken = (error) => {
  return api
    .post({
      url: "/oauth/token",
      data: {
        grant_type: "refresh_token",
        refresh_token: cookie.get("refreshToken"),
      },
      headers: {
        Authorization: "Basic c3lzOmh6aA==",
        "Content-Type": "application/json;charset=UTF-8",
      },
    })
    .then((response) => {
      console.log(response);
      //必须解构重新设置否则会报错
      error.config.headers = { ...error.config.headers };
      //新获取到的令牌重新设置
      cookie.set("token", response.access_token);
      let token = cookie.get("token");
      error.config.headers["Authorization"] = `Bearer ${token}`;
      return services(error.config);
    });
};

// 自定义多种路由
const api = {
  get(options) {
    options.method = "get";
    options.params = options.data;
    return services(options);
  },
  post(options) {
    options.method = "post";
    options.data = JSON.stringify(options.data);
    services.defaults.headers = {
      "Content-Type": "application/json;charset=UTF-8",
    };
    return services(options);
  },
  postParam(options) {
    options.method = "post";
    //form有区别需要用的data
    options.data = qs.stringify(options.data);
    options.headers = {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    };
    return services(options);
  },
  put(options) {
    options.method = "put";
    services.defaults.headers = {
      "Content-Type": "application/json;charset=UTF-8",
    };
    options.data = JSON.stringify(options.data);
    return services(options);
  },
  delete(options) {
    options.method = "delete";
    services.defaults.headers = {
      "Content-Type": "application/json;charset=UTF-8",
    };
    options.data = JSON.stringify(options.data);
    return services(options);
  },
  deleteParam(options) {
    options.method = "delete";
    //form有区别需要用的data
    options.data = qs.stringify(options.data);
    options.headers = {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    };
    return services(options);
  },
};

export default api;
