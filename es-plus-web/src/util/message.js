import { ElMessage } from "element-plus";

const elMessage = {
  success() {
    ElMessage({
      message: "操作成功",
      type: "success",
    });
  },
  error() {
    ElMessage({
      message: "操作失败",
      type: "fail",
    });
  },
  error(messsages) {
    ElMessage({
      message: messsages,
      type: "fail",
      center: true,
      showClose: true,
    });
  },
};
export default elMessage;
