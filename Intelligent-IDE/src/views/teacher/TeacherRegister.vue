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
