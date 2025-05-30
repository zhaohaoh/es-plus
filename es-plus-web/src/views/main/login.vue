<template>
  <div class="login-container">
    <!-- 全新动态背景 -->
    <div class="bg-circles">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 升级版项目LOGO -->
    <div class="logo-container">
      <span class="logo-icon">E</span>
      <div class="logo-text">
        <span class="main-text">Es-plus</span>
        <span class="sub-text">智能管理平台</span>
      </div>
    </div>

    <!-- 智能表单组件 -->
    <form class="login-form" @submit.prevent="handleLogin">
      <div class="form-header">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-subtitle">请使用企业账号登录系统</p>
      </div>

      <div class="input-group">
        <div class="input-wrapper">
          <input
            type="text"
            v-model="username"
            class="form-input"
            placeholder=" "
            required
            autocomplete="username"
          />
          <label class="form-label">账号</label>
          <i class="input-icon icon-user"></i>
        </div>
      </div>

      <div class="input-group">
        <div class="input-wrapper">
          <input
            type="password"
            v-model="password"
            class="form-input"
            placeholder=" "
            required
            autocomplete="current-password"
          />
          <label class="form-label">密码</label>
          <i class="input-icon icon-lock"></i>
        </div>
      </div>

      <button type="submit" class="login-btn">
        <span class="btn-text">立即登录</span>
        <i class="btn-icon icon-arrow-right"></i>
      </button>

      <div class="form-footer">
        <a href="#" class="forgot-pwd">忘记密码？</a>
      </div>
    </form>
  </div>
</template>

<script setup>
import { getCurrentInstance, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";

const username = ref("");
const password = ref("");

const { proxy } = getCurrentInstance();
// export的时候是{}则必须用{}  否则就得去掉{}
const router = useRouter();
const handleLogin = async () => {
  console.log("登录信息:", {
    username: username.value,
    password: password.value,
  });
  const param = {
    username: username.value,
    password: password.value,
  };

  // 实际开发中应在此处调用API
  let res = await proxy.$api.user.doLogin(param);
  if (res.tokenValue) {
    localStorage.setItem("es-plus-token", res.tokenValue);
  }
  router.push("/esIndex");
};
</script>

<style scoped>
/* 基础重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 容器样式 */
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #f5f7fb;
  position: relative;
  overflow: hidden;
}

/* 动态背景装饰 */
.bg-circles {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
}

.circle {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -150px;
  right: -150px;
  animation-delay: 2s;
  background: rgba(116, 163, 255, 0.1);
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 100px;
  right: -100px;
  animation-delay: 4s;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-30px);
  }
}

/* LOGO样式 */
.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
  z-index: 2;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: #2a59d7;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.6rem;
  font-weight: 600;
  box-shadow: 0 4px 6px rgba(42, 89, 215, 0.2);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.main-text {
  font-size: 2.2rem;
  font-weight: 600;
  color: #2a59d7;
  line-height: 1;
}

.sub-text {
  font-size: 0.9rem;
  color: #666;
  margin-top: 4px;
}

/* 表单样式 */
.login-form {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 12px 24px rgba(42, 89, 215, 0.1);
  width: 100%;
  max-width: 440px;
  position: relative;
  z-index: 2;
  margin-top: 20px;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-title {
  font-size: 1.8rem;
  color: #2a59d7;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 0.9rem;
  color: #666;
}

/* 输入框样式 */
.input-group {
  margin-bottom: 25px;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 12px 15px 12px 40px;
  border: 2px solid #e0e7ff;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: transparent;
}

.form-input:focus {
  outline: none;
  border-color: #2a59d7;
  box-shadow: 0 0 0 4px rgba(42, 89, 215, 0.1);
}

.form-label {
  position: absolute;
  left: 40px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  pointer-events: none;
  transition: all 0.3s ease;
  background: white;
  padding: 0 8px;
  margin-left: -8px;
}

.form-input:focus + .form-label,
.form-input:not(:placeholder-shown) + .form-label {
  top: 0;
  transform: translateY(-50%) scale(0.85);
  color: #2a59d7;
  font-weight: 500;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 1.1rem;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  padding: 14px;
  background: #2a59d7;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
}

.login-btn:hover {
  background: #224ab8;
  transform: translateY(-1px);
  box-shadow: 0 5px 15px rgba(42, 89, 215, 0.3);
}

.btn-icon {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.login-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* 底部链接 */
.form-footer {
  text-align: center;
  margin-top: 15px;
}

.forgot-pwd {
  color: #666;
  font-size: 0.9rem;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-pwd:hover {
  color: #2a59d7;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-form {
    padding: 30px 25px;
    margin: 0 20px;
  }

  .logo-container {
    margin-bottom: 30px;
  }

  .form-title {
    font-size: 1.6rem;
  }
}
</style>
