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
    <!-- 星空动态挂饰：闪烁的星星 -->
    <div class="stars">
      <span class="star star1"></span>
      <span class="star star2"></span>
      <span class="star star3"></span>
      <span class="star star4"></span>
      <span class="star star5"></span>
      <span class="star star6"></span>
      <span class="star star7"></span>
    </div>
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
/* 主容器 - 星空背景 */
.teacher-home {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
  background: radial-gradient(ellipse at center, #1a1a2e 0%, #0f0f23 100%); /* 星空渐变 */
  font-family: 'Segoe UI', system-ui, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 欢迎文字动画 */
.welcome-text {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #00d4ff, #6b48ff); /* 星空渐变文字 */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-align: center;
  position: relative;
  z-index: 1;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.6);
}

.welcome-content {
  display: inline-block;
  animation: textScale 1.2s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.highlight {
  background: linear-gradient(135deg, #00d4ff, #ffeb3b); /* 更亮的星空渐变 */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  position: relative;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.8);
}

.highlight::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #00d4ff, #6b48ff); /* 星空渐变下划线 */
  border-radius: 3px;
  animation: underlineExpand 1s 0.5s forwards;
  transform-origin: left;
  transform: scaleX(0);
}

/* 信息卡片 - 星空风格与沉浮效果 */
.info-box {
  background: rgba(255, 255, 255, 0.95); /* 半透明白色 */
  padding: 2.5rem;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2), 0 0 20px rgba(255, 255, 255, 0.1);
  width: 100%;
  max-width: 480px;
  text-align: center;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  animation: floatBox 5s ease-in-out infinite; /* 卡片沉浮效果 */
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
    rgba(0, 212, 255, 0.1) 50%,
    transparent 100%
  );
  animation: shimmer 6s infinite linear;
  z-index: 0;
}

.teacher-avatar {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  background: linear-gradient(135deg, #00d4ff, #6b48ff); /* 星空渐变 */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 5px 15px rgba(0, 212, 255, 0.3);
  position: relative;
  z-index: 1;
}

.icon {
  width: 50px;
  height: 50px;
  color: white;
}

.info-box h2 {
  font-size: 1.8rem;
  background: linear-gradient(135deg, #00d4ff, #6b48ff); /* 星空渐变文字 */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
  position: relative;
  z-index: 1;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.6);
}

.welcome-message {
  background: linear-gradient(135deg, #b0e7ff, #d8b9ff); /* 浅星空渐变 */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 2rem;
  font-size: 1.1rem;
  position: relative;
  z-index: 1;
  text-shadow: 0 0 6px rgba(0, 212, 255, 0.5);
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
  background: linear-gradient(135deg, #00d4ff, #6b48ff); /* 星空渐变文字 */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.3rem;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.6);
}

.stat-label {
  background: linear-gradient(135deg, #b0e7ff, #d8b9ff); /* 浅星空渐变 */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 0.9rem;
  text-shadow: 0 0 6px rgba(0, 212, 255, 0.5);
}

/* 退出按钮 - 沉浮效果 */
.logout-btn {
  padding: 0.8rem 1.8rem;
  background: linear-gradient(135deg, #00d4ff, #6b48ff); /* 星空渐变 */
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 5px 15px rgba(0, 212, 255, 0.3);
  position: relative;
  z-index: 1;
  overflow: hidden;
  animation: floatBox 4s ease-in-out infinite; /* 按钮沉浮效果 */
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
  box-shadow: 0 8px 20px rgba(0, 212, 255, 0.5);
  transform: scale(1.05); /* 悬浮时放大 */
}

.logout-btn:hover::before {
  opacity: 1;
}

.logout-btn:active {
  transform: scale(1);
}

.btn-icon {
  width: 18px;
  height: 18px;
  color: white;
}

/* 星空动态挂饰：闪烁的星星 */
.stars {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
}

.star {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.8), 0 0 20px rgba(255, 255, 255, 0.5);
  animation: twinkle 5s ease-in-out infinite;
}

.star1 {
  width: 4px;
  height: 4px;
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.star2 {
  width: 6px;
  height: 6px;
  top: 25%;
  left: 70%;
  animation-delay: 1s;
}

.star3 {
  width: 3px;
  height: 3px;
  top: 50%;
  left: 20%;
  animation-delay: 2s;
}

.star4 {
  width: 5px;
  height: 5px;
  top: 70%;
  left: 80%;
  animation-delay: 3s;
}

.star5 {
  width: 7px;
  height: 7px;
  top: 30%;
  left: 40%;
  animation-delay: 1.5s;
}

.star6 {
  width: 4px;
  height: 4px;
  top: 60%;
  left: 60%;
  animation-delay: 4s;
}

.star7 {
  width: 6px;
  height: 6px;
  top: 10%;
  left: 90%;
  animation-delay: 2.5s;
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

@keyframes twinkle {
  0% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
  100% { opacity: 0.3; transform: scale(0.8); }
}

@keyframes floatBox {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
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
