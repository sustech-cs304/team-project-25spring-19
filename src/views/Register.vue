<template>
  <div class="auth-container">
    <div class="auth-box">
      <h2>创建账号</h2>
      <form @submit.prevent="handleRegister">
        <input v-model="username" placeholder="用户名" required />
        <input v-model="email" type="email" placeholder="邮箱" required />
        <input v-model="password" type="password" placeholder="密码" required />
        <button type="submit">注册</button>
      </form>
      <p class="switch-link">
        已有账号？
        <router-link to="/login">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const username = ref('')
const email = ref('')
const password = ref('')
const router = useRouter()

const handleRegister = async () => {
  if (!username.value || !email.value || !password.value) {
    alert('用户名、邮箱和密码不能为空')
    return
  }

  try {
    const response = await axios.post('http://localhost:8080/api/users/register', {
      userName: username.value,
      email: email.value,
      password: password.value,
      role: 'student',
      profile: '一个学生'
    })

    alert('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    if (error.response && error.response.data) {
      alert(error.response.data.detail || '注册失败，请重试')
    } else {
      alert('注册失败，请检查网络连接')
    }
  }
}
</script>

<style scoped>
/* 使用与登录相同样式 */
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
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
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
