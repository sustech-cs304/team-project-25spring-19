<template>
  <div class="app-container">
    <div class="sidebar">
      <h2>Intelligent IDE</h2>
      <p class="subtitle">教师后台</p>
      <nav>
        <router-link to="/teacher/home" exact-active-class="active">首页</router-link>
        <router-link to="/teacher/course" exact-active-class="active">课程任务</router-link>
      </nav>
    </div>

    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const logout = () => {
  sessionStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 220px;
  background-color: #2c3e50;
  color: white;
  padding: 20px;
  box-sizing: border-box;
}
.sidebar h2 {
  margin: 0;
  font-size: 22px;
  font-weight: bold;
}
.subtitle {
  margin-top: 4px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #bbb;
}
.sidebar nav {
  display: flex;
  flex-direction: column;
}
.sidebar a {
  color: white;
  text-decoration: none;
  padding: 10px;
  margin: 6px 0;
  border-radius: 6px;
  font-size: 16px;
  transition: background 0.2s;
}
.sidebar a:hover,
.sidebar a.active {
  background-color: #34495e;
}
.main-content {
  flex: 1;
  background-color: #f5f6fa;
  overflow-y: auto;
  padding: 20px;
  box-sizing: border-box;
}
</style>
