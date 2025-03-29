<template>
  <div id="app">
    <div class="sidebar">
      <h2>Intelligent IDE</h2>
      <nav>
        <router-link to="/">Home</router-link>
        <router-link to="/dashboard">Course Dashboard</router-link>
      </nav>
    </div>
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'App'
})
</script>

<style scoped>
.sidebar {
  width: 200px;
  background-color: #2c3e50;
  color: white; /* 确保侧边栏文字可见 */
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
}

.sidebar h2 {
  margin: 0 0 20px;
  font-size: 24px;
  color: white; /* 明确设置标题颜色 */
}

.sidebar nav {
  display: flex;
  flex-direction: column;
}

.sidebar a {
  color: white; /* 确保链接文字可见 */
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
  flex: 1; /* 占据剩余空间 */
  width: 100%;
  background-color: #f5f6fa;
  overflow-y: auto;
  box-sizing: border-box;
  color: var(--color-text); /* 继承 base.css 的文字颜色 */
}
</style>