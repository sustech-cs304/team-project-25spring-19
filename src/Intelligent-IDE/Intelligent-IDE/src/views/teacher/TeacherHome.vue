<template>
    <div class="teacher-home">
      <transition name="fade">
        <div v-if="!showInfo" class="welcome-text">
          欢迎回来，{{ teacherName }}！
        </div>
      </transition>
  
      <transition name="fade">
        <div v-if="showInfo" class="info-box">
          <h2>{{ teacherName }} 老师</h2>
          <p>欢迎使用 Intelligent IDE 教学管理系统！</p>
          <button @click="logout">退出登录</button>
        </div>
      </transition>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  
  const teacherName = ref('')
  const showInfo = ref(false)
  const router = useRouter()
  
  onMounted(() => {
    teacherName.value = localStorage.getItem('currentUser') || '教师'
    setTimeout(() => {
      showInfo.value = true
    }, 2000)
  })
  
  const logout = () => {
    localStorage.clear()
    router.push('/login')
  }
  </script>
  
  <style scoped>
  .teacher-home {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    padding: 40px;
    text-align: center;
    background-color: #f5f6fa;
    font-family: 'Segoe UI', sans-serif;
  }
  .welcome-text {
    font-size: 28px;
    font-weight: bold;
    color: #2c3e50;
    animation: zoomIn 1s ease-in-out;
  }
  .info-box {
    background-color: white;
    padding: 30px 40px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    animation: fadeInUp 1s ease;
    max-width: 400px;
  }
  .info-box h2 {
    margin-bottom: 10px;
    color: #2c3e50;
  }
  .info-box p {
    margin-bottom: 20px;
    color: #555;
  }
  .info-box button {
    padding: 10px 20px;
    background-color: #2980b9;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    cursor: pointer;
    transition: background 0.3s;
  }
  .info-box button:hover {
    background-color: #1f6390;
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity 0.5s;
  }
  .fade-enter-from, .fade-leave-to {
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
  