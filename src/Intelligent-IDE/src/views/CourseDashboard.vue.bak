<template>
  <div class="course-dashboard">
    <h1>{{ courseTitle }} Dashboard</h1>
    
    <!-- 可用课程区域 -->
    <div class="dashboard-section">
      <h2>Available Courses</h2>
      <div class="course-list">
        <div 
          v-for="course in courses" 
          :key="course.id" 
          class="course-card"
        >
          <div class="course-header" @click="toggleCourse(course.id)">
            <h3>{{ course.title }}</h3>
            <span class="toggle-icon">
              {{ course.isExpanded ? '▼' : '▶' }}
            </span>
          </div>
          
          <div v-show="course.isExpanded" class="lecture-list">
            <div 
              v-for="lecture in course.lectures" 
              :key="lecture.id" 
              class="lecture-item"
              @click="goToCourseDetail(course.id)"
            >
              <span class="lecture-status">
                {{ lecture.completed ? '✓' : '◯' }}
              </span>
              {{ lecture.title }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程进度区域 -->
    <div class="dashboard-section">
      <h2>Course Progress</h2>
      <div class="progress-container">
        <div 
          v-for="course in courses" 
          :key="course.id" 
          class="progress-card"
        >
          <div class="progress-header" @click="toggleProgress(course.id)">
            <div class="progress-title">
              {{ course.title }}
              <span class="progress-percent">
                ({{ completedLectures(course) }}/{{ course.lectures.length }})
              </span>
            </div>
            <span class="toggle-icon">
              {{ course.progressExpanded ? '▼' : '▶' }}
            </span>
          </div>

          <div v-show="course.progressExpanded" class="progress-details">
            <div 
              v-for="lecture in course.lectures" 
              :key="lecture.id" 
              class="progress-item"
              @click="goToCourseDetail(course.id)"
            >
              <span 
                :class="['status-indicator', lecture.completed ? 'completed' : 'pending']"
              ></span>
              <span class="lecture-title">{{ lecture.title }}</span>
              <span class="lecture-duration">{{ lecture.duration }}分钟</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { useRouter } from 'vue-router'

interface Lecture {
  id: number
  title: string
  duration: number
  completed: boolean
}

interface Course {
  id: number
  title: string
  description: string
  lectures: Lecture[]
  isExpanded: boolean
  progressExpanded: boolean
}

export default defineComponent({
  name: 'CourseDashboard',
  setup() {
    const router = useRouter()

    // 示例课程数据
    const courses = ref<Course[]>([
      {
        id: 1,
        title: 'Vue.js 基础',
        description: '掌握 Vue.js 核心概念',
        isExpanded: false,
        progressExpanded: false,
        lectures: [
          { id: 1, title: 'Vue 实例与组件', duration: 45, completed: true },
          { id: 2, title: '响应式原理', duration: 60, completed: true },
          { id: 3, title: '状态管理', duration: 90, completed: false }
        ]
      },
      {
        id: 2,
        title: 'TypeScript 进阶',
        description: '深入 TypeScript 类型系统',
        isExpanded: false,
        progressExpanded: false,
        lectures: [
          { id: 4, title: '高级类型', duration: 60, completed: true },
          { id: 5, title: '装饰器', duration: 45, completed: false }
        ]
      }
    ])

    const toggleCourse = (courseId: number) => {
      courses.value = courses.value.map(course => 
        course.id === courseId 
          ? { ...course, isExpanded: !course.isExpanded } 
          : course
      )
    }

    const toggleProgress = (courseId: number) => {
      courses.value = courses.value.map(course => 
        course.id === courseId 
          ? { ...course, progressExpanded: !course.progressExpanded } 
          : course
      )
    }

    const completedLectures = computed(() => (course: Course) => {
      return course.lectures.filter(l => l.completed).length
    })

    const goToCourseDetail = (id: number) => {
        router.push({ name: 'CourseDetail', params: { id: id.toString() } })
      }

    return {
      courses,
      toggleCourse,
      toggleProgress,
      completedLectures,
      goToCourseDetail,
    }
  }
})
</script>

<style scoped>
.course-dashboard {
  width: 100%;
  height: 100%;
  padding: 2rem;
  box-sizing: border-box;
  background: #f5f7fa;
}

h1 {
  color: #2c3e50;
  margin-bottom: 2rem;
  font-size: 2rem;
}

.dashboard-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
  padding: 1.5rem;
}

h2 {
  color: #34495e;
  margin-bottom: 1rem;
  font-size: 1.25rem;
}

.course-card, .progress-card {
  border: 1px solid #eaecef;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.course-header, .progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  cursor: pointer;
  transition: background 0.2s;
}

.course-header:hover, .progress-header:hover {
  background: #f1f3f5;
}

.toggle-icon {
  color: #6c757d;
}

.lecture-list {
  padding: 0.5rem 1rem;
}

.lecture-item {
  display: flex;
  align-items: center;
  padding: 0.75rem;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.lecture-item:hover {
  background: #f8f9fa;
}

.lecture-item:last-child {
  border-bottom: none;
}

.lecture-status {
  margin-right: 1rem;
  font-size: 1.2em;
}

.progress-container {
  max-height: 600px;
  overflow-y: auto;
}

.progress-title {
  flex-grow: 1;
}

.progress-percent {
  color: #666;
  font-size: 0.9em;
  margin-left: 1rem;
}

.progress-details {
  padding: 1rem;
  background: #f8fafc;
}

.progress-item {
  display: flex;
  align-items: center;
  padding: 0.75rem;
  background: white;
  margin-bottom: 0.5rem;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: background 0.2s;
}

.progress-item:hover {
  background: #f8f9fa;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 1rem;
}

.status-indicator.completed {
  background: #4caf50;
}

.status-indicator.pending {
  background: #ff9800;
}

.lecture-title {
  flex-grow: 1;
}

.lecture-duration {
  color: #666;
  font-size: 0.9em;
}
</style>