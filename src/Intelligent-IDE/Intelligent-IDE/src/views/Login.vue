<template>
  <div class="auth-container">
    <div class="auth-box">
      <h2>学生登录</h2>
      <form @submit.prevent="handleLogin">
        <input v-model="username" type="text" placeholder="用户名" required />
        <input v-model="password" type="password" placeholder="密码" required />
        <button type="submit">登录</button>
      </form>

      <p class="switch-link">
        还没有账号？
        <router-link to="/register">去注册</router-link>
      </p>

      <p class="switch-link">
        我是老师？
        <router-link to="/teacher-login">去教师登录</router-link>
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
  if (!username.value || !password.value) {
    alert('用户名和密码不能为空')
    return
  }

  try {
    const response = await axios.post('http://localhost:8080/api/users/login', {
      identifier: username.value, // 这里的 identifier 可以是用户名或邮箱
      password: password.value,
    })

    const userData = response.data

    console.log('返回的用户数据:', userData)

    if (userData.role === 'student') {
      sessionStorage.setItem('userId', userData.userId) // Make sure your backend returns userId
      sessionStorage.setItem('loggedIn', 'true')
      sessionStorage.setItem('currentUser', userData.userName) // Make sure your backend returns userName
      sessionStorage.setItem('userType', userData.role) // Store the actual userType from backend
      // Check if the userType from the backend is 'student'
      console.log('接口完整返回数据:', response)
      console.log('接口返回的 response.data:', response.data)
      console.log('userData.userId:', userData.userId) // Corrected to userId based on session storage
      console.log('userId in sessionStorage:', sessionStorage.getItem('userId'))
      console.log('userType in sessionStorage:', sessionStorage.getItem('userType'))
      // Assuming your backend returns a 'userType' field
      // Login successful, store user info and type
      router.push('/home') // Redirect to home page
    } else {
      // If userType is not 'student', you can show an error or redirect to a different page
      alert('您没有学生权限，请使用学生账号登录')
      // Optionally, you might want to clear any partial session storage items if set before this check
      sessionStorage.removeItem('userId')
      sessionStorage.removeItem('loggedIn')
      sessionStorage.removeItem('currentUser')
      sessionStorage.removeItem('userType')
    }
  } catch (error) {
    console.error('Login error:', error) // Log the full error for debugging
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
