<template>
    <div class="course-list">
      <h2>Available Courses</h2>
      <ul>
        <li
          v-for="course in courses"
          :key="course.id"
          @click="goToCourseDetail(course.id)"
          class="course-item"
        >
          {{ course.title }} - {{ course.description }}
        </li>
      </ul>
    </div>
  </template>
  
  <script lang="ts">
  import { defineComponent } from 'vue'
  import { useRouter } from 'vue-router'
  
  // 定义课程类型
  interface Course {
    id: number
    title: string
    description: string
  }
  
  export default defineComponent({
    name: 'CourseList',
    setup() {
      const router = useRouter()
  
      const courses: Course[] = [
        { id: 1, title: 'Introduction to JavaScript', description: 'Learn the basics of JavaScript programming.' },
        { id: 2, title: 'Vue.js Fundamentals', description: 'Understand the core concepts of Vue.js.' },
        { id: 3, title: 'Advanced CSS', description: 'Master CSS layouts and animations.' }
      ]
  
      const goToCourseDetail = (id: number) => {
        router.push({ name: 'CourseDetail', params: { id: id.toString() } })
      }
  
      return {
        courses,
        goToCourseDetail
      }
    }
  })
  </script>
  
  <style scoped>
  .course-list {
    margin-bottom: 20px;
  }
  
  h2 {
    color: #34495e;
  }
  
  ul {
    list-style-type: none;
    padding: 0;
  }
  
  .course-item {
    padding: 10px;
    margin: 5px 0;
    background-color: #ecf0f1;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  .course-item:hover {
    background-color: #dfe6e9;
  }
  </style>