<template>
  <div id="app" class="app-container">
    <!-- ✅ 仅学生身份 & 登录后显示侧边栏 -->
    <div v-if="showSidebar" class="sidebar">
      <h2>Intelligent IDE</h2>
      <nav>
        <router-link to="/home">Home</router-link>
        <router-link to="/dashboard">Course Board</router-link>
        <router-link to="/meeting">Meeting Room</router-link>
        <router-link to="/code-rooms">Coding Room</router-link>
      </nav>
    </div>

    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()

// ✅ 学生身份 & 非登录页/注册页显示侧边栏
const showSidebar = computed(() => {
  const hiddenRoutes = ['/login', '/register']
  const userType = sessionStorage.getItem('userType')
  return !hiddenRoutes.includes(route.path) && userType === 'student'
})
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background-color: #2c3e50;
  color: white;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
}

.sidebar h2 {
  margin: 0 0 20px;
  font-size: 24px;
  color: white;
}

.sidebar nav {
  display: flex;
  flex-direction: column;
}

.sidebar a {
  color: white;
  text-decoration: none;
  padding: 10px;
  margin: 5px 0;
  border-radius: 5px;
  font-size: 16px;
}

.sidebar a:hover {
  background-color: #34495e;
}

.main-content {
  flex: 1;
  width: 100%;
  background-color: #f5f6fa;
  overflow-y: auto;
  box-sizing: border-box;
}
</style>
