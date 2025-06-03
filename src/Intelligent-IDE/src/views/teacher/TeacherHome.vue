<template>
  <div class="teacher-home">
    <transition name="fade">
      <div v-if="!showInfo" class="welcome-text">
        <div class="welcome-content">
          欢迎回来，<span class="highlight">{{ teacherName }}</span>！
        </div>
      </div>
    </transition>

    <transition name="fade-slide">
      <div v-if="showInfo" class="info-box">
        <div class="teacher-avatar">
          <svg-icon name="teacher" class="icon" />
        </div>
        <h2>{{ teacherName }} 老师</h2>
        <p class="welcome-message">欢迎使用 Intelligent IDE 教学管理系统</p>
        <div class="stats-container">
          <div class="stat-item">
            <div class="stat-value">{{ courseCount }}</div>
            <div class="stat-label">在授课程</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ studentCount }}</div>
            <div class="stat-label">学生人数</div>
          </div>
        </div>
        <button @click="logout" class="logout-btn">
          <svg-icon name="logout" class="btn-icon" />
          退出登录
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import config from '../../config'
import { useRouter } from 'vue-router'

const router = useRouter()
const teacherName = ref('')
const showInfo = ref(false)
const courseCount = ref(0)
const studentCount = ref(0)
const userId = ref<number>(parseInt(sessionStorage.getItem('userId') || '0'))

onMounted(async () => {
  teacherName.value = sessionStorage.getItem('currentUser') || '教师'

  try {
    // 获取教师教授的课程数量
    const coursesResponse = await axios.get(`${config.apiBaseUrl}/courses/${userId.value}/getByUser`)
    courseCount.value = coursesResponse.data.length

    // 获取所有学生数量
    const studentsResponse = await axios.get(`${config.apiBaseUrl}/users/getAllUsers`)
    studentCount.value = studentsResponse.data.filter((user: any) => user.role === 'student').length

    showInfo.value = true
  } catch (error) {
    console.error('加载数据失败:', error)
    // 如果API请求失败，使用默认值
    courseCount.value = 0
    studentCount.value = 0
    showInfo.value = true
  }
})

const logout = () => {
  sessionStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
/* 主容器 - 动态渐变背景 */
.teacher-home {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2f6 100%);
  font-family: 'Segoe UI', system-ui, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 欢迎文字动画 */
.welcome-text {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  text-align: center;
  position: relative;
  z-index: 1;
}

.welcome-content {
  display: inline-block;
  animation: textScale 1.2s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.highlight {
  color: #4361ee;
  position: relative;
}

.highlight::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border-radius: 3px;
  animation: underlineExpand 1s 0.5s forwards;
  transform-origin: left;
  transform: scaleX(0);
}

/* 信息卡片 - 现代化设计 */
.info-box {
  background: rgba(255, 255, 255, 0.98);
  padding: 2.5rem;
  border-radius: 16px;
  box-shadow:
    0 10px 25px -5px rgba(0, 0, 0, 0.1),
    0 8px 10px -6px rgba(0, 0, 0, 0.05);
  width: 100%;
  max-width: 480px;
  text-align: center;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.info-box::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 0%,
    rgba(255, 255, 255, 0.1) 50%,
    transparent 100%
  );
  animation: shimmer 6s infinite linear;
  z-index: 0;
}

.teacher-avatar {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 5px 15px rgba(67, 97, 238, 0.3);
  position: relative;
}

.icon {
  width: 50px;
  height: 50px;
  color: white;
}

.info-box h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  position: relative;
  z-index: 1;
}

.welcome-message {
  color: #64748b;
  margin-bottom: 2rem;
  font-size: 1.1rem;
  position: relative;
  z-index: 1;
}

/* 统计数据样式 */
.stats-container {
  display: flex;
  justify-content: space-around;
  margin: 2rem 0;
  position: relative;
  z-index: 1;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 2.2rem;
  font-weight: 700;
  color: #4361ee;
  margin-bottom: 0.3rem;
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  color: #64748b;
  font-size: 0.9rem;
}

/* 退出按钮 - 悬浮效果 */
.logout-btn {
  padding: 0.8rem 1.8rem;
  background: linear-gradient(135deg, #f8fafc, #e2e8f0);
  color: #64748b;
  border: none;
  border-radius: 50px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow:
    0 2px 5px rgba(0, 0, 0, 0.1),
    inset 0 1px 1px rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 1;
  overflow: hidden;
}

.logout-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.logout-btn:hover {
  color: #334155;
  transform: translateY(-2px);
  box-shadow:
    0 5px 15px rgba(0, 0, 0, 0.1),
    inset 0 1px 1px rgba(255, 255, 255, 0.8);
}

.logout-btn:hover::before {
  opacity: 1;
}

.logout-btn:active {
  transform: translateY(0);
}

.btn-icon {
  width: 18px;
  height: 18px;
}

/* 动画效果 */
@keyframes textScale {
  0% { transform: scale(0.8); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes underlineExpand {
  0% { transform: scaleX(0); }
  100% { transform: scaleX(1); }
}

@keyframes shimmer {
  0% { transform: translateX(-50%) translateY(-50%) rotate(0deg); }
  100% { transform: translateX(-50%) translateY(-50%) rotate(360deg); }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.6s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-slide-enter-active {
  animation: fadeInUp 0.8s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.fade-slide-leave-active {
  animation: fadeInUp 0.8s reverse cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .welcome-text {
    font-size: 2rem;
  }

  .info-box {
    padding: 1.5rem;
    width: 90%;
  }

  .stat-value {
    font-size: 1.8rem;
  }
}
</style>
