<template>
  <div class="auth-container">
    <div class="auth-box">
      <h2>教师注册</h2>
      <form @submit.prevent="handleRegister">
        <input v-model="username" type="text" placeholder="用户名" required />
        <input v-model="password" type="password" placeholder="密码" required />
        <button type="submit">注册</button>
      </form>
      <p class="switch-link">
        已有账号？
        <router-link to="/teacher-login">去登录</router-link>
      </p>
    </div>
    <!-- 星空动态挂饰：闪烁的星星 -->
    <div class="stars">
      <span class="star star1"></span>
      <span class="star star2"></span>
      <span class="star star3"></span>
      <span class="star star4"></span>
      <span class="star star5"></span>
      <span class="star star6"></span>
      <span class="star star7"></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import config from '../../config'

const username = ref('')
const password = ref('')
const router = useRouter()

const handleRegister = async () => {
  if (!username.value || !password.value) {
    alert('请输入用户名和密码')
    return
  }

  try {
    // 先进行用户名查重
    const checkResponse = await axios.get(`${config.apiBaseUrl}/users/getAllUsers`)
    const existingTeacher = checkResponse.data.find((user: any) => user.userName === username.value)

    if (existingTeacher) {
      alert('用户名已存在，请更换')
      return
    }

    // 如果用户名没有重复，则进行注册
    const registerResponse = await axios.post(`${config.apiBaseUrl}/users/register`, {
      userName: username.value,
      email: `${username.value}@example.com`,  // 默认使用用户名@... 邮箱格式
      password: password.value,
      role: 'teacher',
      profile: '一名教师'
    })

    alert('注册成功！请登录')
    router.push('/teacher-login')
  } catch (error) {
    alert('注册失败，请稍后再试')
    console.error(error)
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: radial-gradient(ellipse at center, #1a1a2e 0%, #0f0f23 100%); /* 星空渐变背景，与登录页一致 */
  position: relative;
  overflow: hidden;
}

/* 注册框样式 */
.auth-box {
  background: rgba(255, 255, 255, 0.95); /* 半透明白色，融入星空 */
  padding: 40px 30px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2), 0 0 20px rgba(255, 255, 255, 0.1);
  text-align: center;
  width: 340px;
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease;
  animation: floatBox 4s ease-in-out infinite; /* 卡片沉浮效果 */
}

.auth-box:hover {
  transform: translateY(-5px); /* 悬浮效果 */
}

.auth-box h2 {
  margin-bottom: 25px;
  background: linear-gradient(135deg, #00d4ff, #6b48ff); /* 星空渐变，与登录页一致 */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.6);
}

/* 输入框样式 */
.auth-box input {
  width: 100%;
  padding: 14px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  animation: floatInput 4s ease-in-out infinite; /* 输入框沉浮效果，与登录页一致 */
  transition: all 0.3s ease;
}

.auth-box input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.auth-box input:focus {
  border-color: #00d4ff;
  background: rgba(255, 255, 255, 0.2);
  outline: none;
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
  animation: none; /* 聚焦时停止沉浮，增强可读性 */
}

/* 按钮样式 */
.auth-box button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(to right, #00d4ff, #6b48ff); /* 星空风格渐变，与登录页一致 */
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}

.auth-box button:hover {
  background: linear-gradient(to right, #00b7d4, #5a3de0);
  transform: scale(1.02);
  box-shadow: 0 0 15px rgba(0, 212, 255, 0.7);
}

/* 切换链接样式 */
.switch-link {
  margin-top: 20px;
  font-size: 14px;
  color: #2c3e50; /* 与登录页一致，提高对比度 */
}

.switch-link a {
  color: #00d4ff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.switch-link a:hover {
  color: #6b48ff;
}

/* 星空动态挂饰：闪烁的星星 */
.stars {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
}

.star {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.8), 0 0 20px rgba(255, 255, 255, 0.5);
  animation: twinkle 5s ease-in-out infinite;
}

.star1 {
  width: 4px;
  height: 4px;
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.star2 {
  width: 6px;
  height: 6px;
  top: 25%;
  left: 70%;
  animation-delay: 1s;
}

.star3 {
  width: 3px;
  height: 3px;
  top: 50%;
  left: 20%;
  animation-delay: 2s;
}

.star4 {
  width: 5px;
  height: 5px;
  top: 70%;
  left: 80%;
  animation-delay: 3s;
}

.star5 {
  width: 7px;
  height: 7px;
  top: 30%;
  left: 40%;
  animation-delay: 1.5s;
}

.star6 {
  width: 4px;
  height: 4px;
  top: 60%;
  left: 60%;
  animation-delay: 4s;
}

.star7 {
  width: 6px;
  height: 6px;
  top: 10%;
  left: 90%;
  animation-delay: 2.5s;
}

/* 星星闪烁动画 */
@keyframes twinkle {
  0% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
  100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
}

/* 输入框沉浮动画 */
@keyframes floatInput {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
  100% {
    transform: translateY(0px);
  }
}

/* 卡片沉浮动画 */
@keyframes floatBox {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
  100% {
    transform: translateY(0px);
  }
}
</style>
