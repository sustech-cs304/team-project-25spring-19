<template>
    <div class="teacher-course">
      <h1>教师任务管理</h1>
  
      <section class="add-task">
        <h2>添加任务</h2>
        <input v-model="newTask.title" placeholder="任务标题" />
        <textarea v-model="newTask.description" placeholder="任务描述"></textarea>
        <button @click="addTask">添加任务</button>
      </section>
  
      <section class="task-list">
        <h2>当前任务</h2>
        <ul>
          <li v-for="task in tasks" :key="task.id">
            <strong>{{ task.title }}</strong>: {{ task.description }}
          </li>
        </ul>
      </section>
  
      <section class="student-progress">
        <h2>学生任务完成情况</h2>
        <table>
          <thead>
            <tr>
              <th>学生</th>
              <th v-for="task in tasks" :key="task.id">{{ task.title }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in students" :key="student.username">
              <td>{{ student.username }}</td>
              <td v-for="task in tasks" :key="task.id">
                <span :class="getProgressClass(student.username, task.id)">
                  {{ isTaskCompleted(student.username, task.id) ? '✅' : '❌' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue'
  
  const tasks = ref<any[]>([])
  const students = ref<any[]>([])
  const newTask = ref({ title: '', description: '' })
  const userProgress = ref<{ [username: string]: number[] }>({})
  
  onMounted(() => {
    tasks.value = JSON.parse(localStorage.getItem('tasks') || '[]')
    students.value = JSON.parse(localStorage.getItem('users') || '[]')
    userProgress.value = JSON.parse(localStorage.getItem('userProgress') || '{}')
  })
  
  const addTask = () => {
    if (!newTask.value.title.trim()) return
    const id = Date.now()
    tasks.value.push({ id, ...newTask.value })
    localStorage.setItem('tasks', JSON.stringify(tasks.value))
    newTask.value = { title: '', description: '' }
  }
  
  const isTaskCompleted = (username: string, taskId: number) => {
    return userProgress.value[username]?.includes(taskId)
  }
  
  const getProgressClass = (username: string, taskId: number) => {
    return isTaskCompleted(username, taskId) ? 'done' : 'not-done'
  }
  </script>
  
  <style scoped>
  .teacher-course {
    padding: 30px;
    max-width: 900px;
    margin: 0 auto;
  }
  .add-task textarea {
    width: 100%;
    margin: 8px 0;
    padding: 8px;
    resize: vertical;
    border-radius: 4px;
    border: 1px solid #ccc;
  }
  .add-task input,
  .add-task button {
    margin-top: 10px;
    display: block;
    padding: 8px;
  }
  .task-list ul {
    padding-left: 20px;
  }
  .student-progress table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
  }
  .student-progress th,
  .student-progress td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: center;
  }
  .done {
    color: green;
  }
  .not-done {
    color: red;
  }
  </style>
  