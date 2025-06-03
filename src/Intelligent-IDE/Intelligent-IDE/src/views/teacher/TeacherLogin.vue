<template>
  <div class="auth-container">
    <div class="auth-box">
      <h2>教师登录</h2>
      <form @submit.prevent="handleLogin">
        <input v-model="username" type="text" placeholder="用户名" required />
        <input v-model="password" type="password" placeholder="密码" required />
        <button type="submit">登录</button>
      </form>
      <p class="switch-link">
        还没有账号？
        <router-link to="/teacher-register">去注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const username = ref('')
const password = ref('')
const router = useRouter()

const handleLogin = async () => {
  // 其他用户名密码通过API验证
  try {
    const response = await axios.post('http://localhost:8080/api/users/login', {
      identifier: username.value, // 这里的 identifier 可以是用户名或邮箱
      password: password.value,
    })

    const userData = response.data
    console.log('返回的用户数据:', userData)

    // 登录成功后，存储用户信息和身份
    sessionStorage.setItem('loggedIn', 'true')
    sessionStorage.setItem('currentUser', userData.userName)
    sessionStorage.setItem('userType', userData.role) // 存储用户类型
    sessionStorage.setItem('userId', userData.userId)
    router.push('/teacher/home')
  } catch (error) {
    console.error('登录失败:', error)
    alert('用户名或密码错误')
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(to right, #6dd5fa, #2980b9);
}

.auth-box {
  background: #fff;
  padding: 40px 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  text-align: center;
  width: 320px;
}

.auth-box h2 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.auth-box input {
  width: 100%;
  padding: 12px;
  margin-bottom: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
}

.auth-box button {
  width: 100%;
  padding: 12px;
  background-color: #2980b9;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: 0.3s;
}

.auth-box button:hover {
  background-color: #1f6390;
}

.switch-link {
  margin-top: 15px;
  font-size: 14px;
  color: #666;
}

.switch-link a {
  color: #2980b9;
  text-decoration: none;
}
</style>
