<template>
  <div class="teacher-course">
    <h1>教师任务管理</h1>

    <!-- 选择课程 -->
    <section class="course-list">
      <h2>选择课程</h2>
      <select v-model="selectedCourseId" @change="loadLectures">
        <option disabled value="">请选择课程</option>
        <option v-for="course in courses" :key="course.courseId" :value="course.courseId">
          {{ course.title }}
        </option>
      </select>
      <!-- 添加新课程 -->
      <button @click="addCourse" class="add-course-button">添加新课程</button>
    </section>

    <!-- 添加课程表单 -->
    <section v-if="isAddingCourse" class="add-course-form">
      <h2>添加新课程</h2>
      <input v-model="newCourse.title" placeholder="课程名称" />
      <textarea v-model="newCourse.description" placeholder="课程描述"></textarea>
      <input v-model="newCourse.lectureNum" type="number" placeholder="讲座数量" min="1" />
      <div class="form-buttons">
        <button @click="createCourse" :disabled="!isCourseValid">创建课程</button>
        <button @click="cancelAddCourse">取消</button>
      </div>
    </section>

    <!-- 课程详情 -->
    <section v-if="selectedCourse" class="course-details">
      <h2>课程详情</h2>
      <div class="course-info">
        <h3>{{ selectedCourse.title }}</h3>
        <p>{{ selectedCourse.description }}</p>
        <p>讲座数量: {{ selectedCourse.lectureNum }}</p>
      </div>
    </section>

    <!-- 讲座管理 -->
    <section class="lecture-list" v-if="selectedCourseId">
      <h2>讲座管理</h2>
      <button @click="addLecture" class="add-lecture-button">添加讲座</button>

      <select v-model="selectedLectureId" @change="loadLectureDetails">
        <option disabled value="">请选择讲座</option>
        <option v-for="lecture in lectures" :key="lecture.lectureId" :value="lecture.lectureId">
          {{ lecture.title }} (顺序: {{ lecture.lectureOrder }})
        </option>
      </select>
    </section>

    <!-- 讲座详情 -->
    <section v-if="selectedLecture" class="lecture-details">
      <h2>讲座详情</h2>
      <div class="lecture-info">
        <h3>{{ selectedLecture.title }}</h3>
        <p>{{ selectedLecture.description }}</p>
        <p>顺序: {{ selectedLecture.lectureOrder }}</p>
      </div>
    </section>

    <!-- 添加课件（PDF 文件） -->
    <section class="add-slide" v-if="selectedLectureId">
      <h2>添加课件</h2>
      <textarea v-model="newSlide.content" placeholder="课件内容（可选）"></textarea>

      <!-- PDF 上传区域 -->
      <div class="upload-area" v-if="!pdfUrl" @dragover.prevent @drop.prevent="handleDrop">
        <p class="upload-prompt">请上传PDF文件（支持拖拽，最大10MB）</p>
        <div class="upload-controls">
          <input
            type="file"
            ref="fileInputRef"
            @change="handleFileChange"
            accept=".pdf"
            id="file-upload"
            style="display: none"
          />
          <label for="file-upload" class="custom-file-label">选择 PDF 文件</label>
          <button @click="previewPdf" :disabled="!selectedFile || isLoading" class="upload-button">
            {{ isLoading ? '加载中...' : '预览' }}
          </button>
        </div>
      </div>
      <!-- PDF 预览 -->
      <div v-else class="pdf-container">
        <iframe :src="pdfUrl" class="pdf-viewer"></iframe>
        <button @click="clearPdf" class="clear-button">清除 PDF</button>
      </div>

      <button @click="addSlide" :disabled="!pdfUrl && !newSlide.content" class="add-slide-button">
        添加课件
      </button>
    </section>

    <!-- 学生列表与进度 -->
    <section class="student-progress" v-if="selectedCourseId">
      <h2>学生进度</h2>
      <div class="student-list">
        <div v-for="student in students" :key="student.userId" class="student-item">
          <div class="student-info">
            <span class="student-name">{{ student.userName }}</span>
            <span class="student-email">{{ student.email }}</span>
          </div>
          <div class="progress-container">
            <progress :value="getStudentProgress(student.userId)" max="100"></progress>
            <span class="progress-value">{{ getStudentProgress(student.userId) }}%</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
const userId = ref<number>(parseInt(sessionStorage.getItem('userId') || '0'))

// 数据状态
const courses = ref<any[]>([])
const lectures = ref<any[]>([])
const students = ref<any[]>([]) // 学生列表
const progressData = ref<any[]>([]) // 进度数据

const selectedCourseId = ref<number | string>('')
const selectedLectureId = ref<number | string>('')

// 计算属性
const selectedCourse = computed(() => {
  return courses.value.find((c) => c.courseId === selectedCourseId.value) || null
})

const selectedLecture = computed(() => {
  return lectures.value.find((l) => l.lectureId === selectedLectureId.value) || null
})

// 用于添加课程
const isAddingCourse = ref(false)
const newCourse = ref({
  title: '',
  description: '',
  lectureNum: 0,
})

const isCourseValid = computed(() => {
  return (
    newCourse.value.title.trim() !== '' &&
    newCourse.value.description.trim() !== '' &&
    newCourse.value.lectureNum > 0
  )
})

// 用于添加课件
const newSlide = ref({ content: '', file: null as File | null })
const selectedFile = ref<File | null>(null)
const pdfUrl = ref<string | null>(null)
const isLoading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

// 当前登录用户的 userId（通过登录获取）
// 通过登录接口获取真实的 userId

// 获取所有课程
onMounted(async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/courses/${userId.value}/getByUser`)
    courses.value = response.data
  } catch (error) {
    console.error('加载课程数据失败', error)
    alert('无法加载课程数据，请稍后重试')
  }
})

// 获取课程讲座
const loadLectures = async () => {
  if (!selectedCourseId.value) return
  try {
    const response = await axios.get(
      `http://localhost:8080/api/lectures/${selectedCourseId.value}/getByCourse`,
    )
    lectures.value = response.data
    selectedLectureId.value = ''

    // 加载学生列表和进度数据
    await loadStudents()
    await loadProgressData()
  } catch (error) {
    console.error('加载讲座数据失败', error)
    alert('无法加载讲座数据，请稍后重试')
  }
}

// 加载讲座详情
const loadLectureDetails = async () => {
  if (!selectedLectureId.value) return
  try {
    const response = await axios.get(
      `http://localhost:8080/api/lectures/${selectedLectureId.value}/getById`,
    )
    // 更新讲座列表中的该讲座信息
    const index = lectures.value.findIndex((l) => l.lectureId === selectedLectureId.value)
    if (index !== -1) {
      lectures.value[index] = response.data
    }
  } catch (error) {
    console.error('加载讲座详情失败', error)
  }
}

// 加载学生列表
const loadStudents = async () => {
  try {
    // 获取所有用户（实际应用中应过滤学生角色）
    const response = await axios.get('http://localhost:8080/api/users/getAllUsers')
    // 过滤出学生角色（假设角色字段为role，学生为'student'）
    students.value = response.data.filter((user: any) => user.role === 'student')
  } catch (error) {
    console.error('加载学生数据失败', error)
    alert('无法加载学生数据，请稍后重试')
  }
}

// 加载进度数据
const loadProgressData = async () => {
  if (!selectedCourseId.value) return
  try {
    // 获取该课程的所有进度记录
    const response = await axios.get(
      `http://localhost:8080/api/process/getByCourse/${selectedCourseId.value}`,
    )
    progressData.value = response.data
  } catch (error) {
    console.error('加载进度数据失败', error)
    alert('无法加载进度数据，请稍后重试')
  }
}

// 获取学生进度
const getStudentProgress = (studentId: number) => {
  const studentProgress = progressData.value.filter((p) => p.userId === studentId)
  if (studentProgress.length === 0) return 0

  // 计算完成比例
  const total = studentProgress.length
  const completed = studentProgress.filter((p) => p.state === '已完成').length
  return Math.round((completed / total) * 100)
}

// 处理拖拽上传
const handleDrop = (e: DragEvent) => {
  const file = e.dataTransfer?.files?.[0]
  if (file && /\.pdf$/i.test(file.name)) {
    if (file.size > 10 * 1024 * 1024) {
      alert('文件过大，请上传小于10MB的文件')
      return
    }
    selectedFile.value = file
    newSlide.value.file = file
  } else {
    alert('请上传 .pdf 文件')
  }
}

// 处理文件选择
const handleFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file && /\.pdf$/i.test(file.name)) {
    if (file.size > 10 * 1024 * 1024) {
      alert('文件过大，请上传小于10MB的文件')
      return
    }
    selectedFile.value = file
    newSlide.value.file = file
  } else {
    alert('请上传 .pdf 文件')
  }
}

// 预览 PDF
const previewPdf = () => {
  if (!selectedFile.value) return

  isLoading.value = true
  pdfUrl.value = null

  try {
    const url = URL.createObjectURL(selectedFile.value)
    pdfUrl.value = url
  } catch (error) {
    console.error('PDF预览失败:', error)
    alert('无法预览PDF文件，请检查文件格式或稍后重试')
  } finally {
    isLoading.value = false
    selectedFile.value = null
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

// 清除 PDF 预览
const clearPdf = () => {
  if (pdfUrl.value) {
    URL.revokeObjectURL(pdfUrl.value)
  }
  pdfUrl.value = null
  newSlide.value.file = null
  selectedFile.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
}

// 添加课程
const addCourse = () => {
  isAddingCourse.value = true
  newCourse.value = { title: '', description: '', lectureNum: 0 }
}

// 取消添加课程
const cancelAddCourse = () => {
  isAddingCourse.value = false
}

// 创建课程
const createCourse = async () => {
  if (!isCourseValid.value) return

  try {
    const response = await axios.post(
      `http://localhost:8080/api/courses/${userId.value}/create`,
      newCourse.value,
    )

    courses.value.push(response.data)
    isAddingCourse.value = false
    selectedCourseId.value = response.data.courseId
    await loadLectures()
  } catch (error) {
    console.error('创建课程失败', error)
    alert('创建课程失败，请稍后重试')
  }
}

// 添加讲座
const addLecture = async () => {
  if (!selectedCourseId.value) return

  const lectureData = {
    title: `讲座 ${lectures.value.length + 1}`,
    description: '新讲座描述',
    lectureOrder: lectures.value.length + 1,
  }

  try {
    const response = await axios.post(
      `http://localhost:8080/api/lectures/${userId.value}/${selectedCourseId.value}/create`,
      lectureData,
    )

    lectures.value.push(response.data)
    selectedLectureId.value = response.data.lectureId
    await loadLectureDetails()
  } catch (error) {
    console.error('添加讲座失败', error)
    alert('添加讲座失败，请稍后重试')
  }
}

// 添加课件
const addSlide = async () => {
  if (!selectedLectureId.value || (!pdfUrl.value && !newSlide.value.content)) return

  try {
    // 1. 创建课件记录
    const slideData = {
      content: newSlide.value.content,
    }

    const createResponse = await axios.post(
      `http://localhost:8080/api/slides/${userId.value}/${selectedLectureId.value}/create`,
      slideData,
    )

    const slideId = createResponse.data.slideId

    // 2. 如果有PDF文件，上传文件
    if (newSlide.value.file) {
      const formData = new FormData()
      formData.append('file', newSlide.value.file)

      await axios.post(
        `http://localhost:8080/api/slides/${userId.value}/${slideId}/updateFile`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        },
      )
    }

    alert('课件添加成功!')
    newSlide.value = { content: '', file: null }
    clearPdf()
  } catch (error) {
    console.error('添加课件失败', error)
    alert('添加课件失败，请稍后重试')
  }
}
</script>

<style scoped>
.teacher-course {
  padding: 30px;
  max-width: 900px;
  margin: 0 auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.course-list,
.lecture-list,
.add-slide,
.student-progress {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h1,
h2 {
  color: #2c3e50;
}

select,
input,
textarea {
  width: 100%;
  padding: 10px;
  margin: 8px 0;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

button {
  padding: 10px 15px;
  margin: 5px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.3s;
}

.add-course-button,
.add-lecture-button {
  background-color: #3498db;
  color: white;
  margin-top: 10px;
}

.add-course-button:hover,
.add-lecture-button:hover {
  background-color: #2980b9;
}

.add-slide-button {
  background-color: #2ecc71;
  color: white;
  margin-top: 15px;
  padding: 12px 20px;
}

.add-slide-button:hover {
  background-color: #27ae60;
}

.add-slide-button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.add-course-form {
  padding: 20px;
  background-color: #ecf0f1;
  border-radius: 8px;
  margin-bottom: 20px;
}

.form-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.course-details,
.lecture-details {
  padding: 20px;
  background-color: #e8f4f8;
  border-radius: 8px;
  margin-bottom: 20px;
}

.course-info h3,
.lecture-info h3 {
  margin-top: 0;
  color: #16a085;
}

.upload-area {
  margin-top: 15px;
  padding: 20px;
  border: 2px dashed #bdc3c7;
  border-radius: 8px;
  text-align: center;
  background-color: #f8f9fa;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #3498db;
  background-color: #edf7ff;
}

.upload-prompt {
  margin-bottom: 15px;
  color: #7f8c8d;
}

.upload-controls {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.custom-file-label {
  padding: 10px 20px;
  background-color: #ecf0f1;
  color: #2c3e50;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.custom-file-label:hover {
  background-color: #3498db;
  color: white;
}

.upload-button {
  background-color: #3498db;
  color: white;
}

.upload-button:hover:not(:disabled) {
  background-color: #2980b9;
}

.upload-button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.clear-button {
  background-color: #e74c3c;
  color: white;
  margin-top: 10px;
}

.clear-button:hover {
  background-color: #c0392b;
}

.pdf-container {
  width: 100%;
  height: 500px;
  margin-top: 20px;
  display: flex;
  flex-direction: column;
}

.pdf-viewer {
  width: 100%;
  flex: 1;
  border: none;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.student-progress {
  margin-top: 30px;
}

.student-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.student-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.student-info {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-weight: 600;
  color: #2c3e50;
}

.student-email {
  font-size: 14px;
  color: #7f8c8d;
}

.progress-container {
  flex: 1;
  max-width: 300px;
  display: flex;
  align-items: center;
  gap: 10px;
}

progress {
  flex: 1;
  height: 12px;
  border-radius: 6px;
  overflow: hidden;
}

progress::-webkit-progress-bar {
  background-color: #ecf0f1;
  border-radius: 6px;
}

progress::-webkit-progress-value {
  background-color: #2ecc71;
  border-radius: 6px;
}

.progress-value {
  min-width: 40px;
  text-align: right;
  font-weight: 600;
  color: #27ae60;
}
</style>
