import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: "sass" })],
    }),
  ],
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: ` @use "src/assets/scss/element/index.scss" as *;`,
      },
    },
  },
  // 本地反向代理解决浏览器跨域限制
  server: {
    host: "localhost",
    port: 5173,
    open: false, // 启动是否自动打开浏览器
    // proxy: {
    //   ["/api"]: {
    //     target: "http://localhost", //
    //     changeOrigin: true,
    //     log: "debug",
    //     rewrite: (path) => {
    //       return path.replace(new RegExp("^" + "/api"), "");
    //     },
    //   },
    // },
  },
});
