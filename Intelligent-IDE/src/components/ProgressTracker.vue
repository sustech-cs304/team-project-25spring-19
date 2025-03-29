<template>
    <div class="progress-tracker">
      <h2>Your Progress</h2>
      <div v-for="course in progress" :key="course.id" class="progress-item">
        <span>{{ course.title }}</span>
        <div class="progress-bar">
          <div
            class="progress-bar-fill"
            :style="{ width: course.completion + '%' }"
          ></div>
        </div>
        <span>{{ course.completion }}% Completed</span>
      </div>
    </div>
  </template>
  
  <script lang="ts">
  import { defineComponent } from 'vue'
  
  // 定义进度类型
  interface Progress {
    id: number
    title: string
    completion: number
  }
  
  export default defineComponent({
    name: 'ProgressTracker',
    data() {
      return {
        progress: [
          { id: 1, title: 'Introduction to JavaScript', completion: 75 },
          { id: 2, title: 'Vue.js Fundamentals', completion: 40 },
          { id: 3, title: 'Advanced CSS', completion: 20 }
        ] as Progress[]
      }
    }
  })
  </script>
  
  <style scoped>
  .progress-tracker {
    margin-top: 20px;
  }
  
  h2 {
    color: #34495e;
  }
  
  .progress-item {
    margin: 10px 0;
  }
  
  .progress-bar {
    width: 100%;
    height: 20px;
    background-color: #ecf0f1;
    border-radius: 10px;
    overflow: hidden;
  }
  
  .progress-bar-fill {
    height: 100%;
    background-color: #3498db;
    transition: width 0.3s ease-in-out;
  }
  </style>