<template>
  <div class="home-container">
    <transition name="fade">
      <div v-if="!showUserInfo" class="welcome-text">欢迎回来，{{ username }}！</div>
    </transition>

    <transition name="fade">
      <div v-if="showUserInfo" class="user-info">
        <h2>你好，{{ username }}</h2>
        <p>欢迎使用 Intelligent IDE！</p>
        <button @click="logout">退出登录</button>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const username = ref('未知用户')
const showUserInfo = ref(false)
const router = useRouter()

onMounted(() => {
  username.value = sessionStorage.getItem('currentUser') || '未知用户'

  // 延迟显示用户信息
  setTimeout(() => {
    showUserInfo.value = true
  }, 2000)
})

const logout = () => {
  sessionStorage.removeItem('loggedIn')
  sessionStorage.removeItem('currentUser')
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px;
  font-family: 'Segoe UI', sans-serif;
}

.welcome-text {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  animation: zoomIn 1s ease-in-out;
}

.user-info {
  background-color: white;
  border-radius: 12px;
  padding: 30px 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-width: 400px;
  animation: fadeInUp 1s ease;
}

.user-info h2 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.user-info p {
  margin-bottom: 20px;
  color: #555;
}

.user-info button {
  padding: 10px 20px;
  background-color: #2980b9;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.user-info button:hover {
  background-color: #1f6390;
}

/* 动画过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes zoomIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
