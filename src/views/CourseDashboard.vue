<template>
  <div class="course-dashboard">
    <h1>Course Dashboard</h1>
    <CourseList />
    <ProgressTracker />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import CourseList from '../components/CourseList.vue'
import ProgressTracker from '../components/ProgressTracker.vue'

export default defineComponent({
  name: 'CourseDashboard',
  components: {
    CourseList,
    ProgressTracker
  }
})
</script>

<style scoped>
.course-dashboard {
  width: 100%; /* 占满父容器宽度 */
  height: 100%; /* 占满父容器高度 */
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto; /* 允许内容垂直滚动 */
}

h1 {
  color: #2c3e50;
  margin-bottom: 20px;
}
</style>