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
        <p>讲座数量: {{ lectures.length }}</p>
      </div>
    </section>

    <!-- 讲座管理 -->
    <section class="lecture-list" v-if="selectedCourseId">
      <h2>讲座管理</h2>
      <button @click="addLecture" :disabled="!selectedCourseId" class="add-lecture-button">
        {{ lectures.length === 0 ? '添加第一个讲座' : '添加新讲座' }}
      </button>

      <div v-if="lectures.length === 0" class="empty-tip">
        <p>当前课程还没有讲座，点击上方按钮添加第一个讲座</p>
      </div>

      <select v-else v-model="selectedLectureId" @change="loadLectureDetails">
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
import config from '../../config'
const userId = ref<number>(parseInt(sessionStorage.getItem('userId') || '0'))

// 数据状态
const courses = ref<any[]>([])
const lectures = ref<any[]>([])
const students = ref<any[]>([])
const progressData = ref<any[]>([])

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
})

const isCourseValid = computed(() => {
  return newCourse.value.title.trim() !== '' && newCourse.value.description.trim() !== ''
})

// 用于添加课件
const newSlide = ref({ content: '', file: null as File | null })
const selectedFile = ref<File | null>(null)
const pdfUrl = ref<string | null>(null)
const isLoading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

// 获取所有课程
onMounted(async () => {
  try {
    const response = await axios.get(`${config.apiBaseUrl}/courses/${userId.value}/getByUser`)
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
      `${config.apiBaseUrl}/lectures/${selectedCourseId.value}/getByCourse`,
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
      `${config.apiBaseUrl}/lectures/${selectedLectureId.value}/getById`,
    )
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
    const response = await axios.get(`${config.apiBaseUrl}/users/getAllUsers`)
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
    const response = await axios.get(
      `${config.apiBaseUrl}/process/getByCourse/${selectedCourseId.value}`,
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
  newCourse.value = { title: '', description: '' }
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
      `${config.apiBaseUrl}/courses/${userId.value}/create`,
      newCourse.value,
    )

    courses.value.push(response.data)
    isAddingCourse.value = false
    selectedCourseId.value = response.data.courseId
    lectures.value = [] // 确保新课程讲座列表为空
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
      `${config.apiBaseUrl}/lectures/${userId.value}/${selectedCourseId.value}/create`,
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
    const slideData = {
      content: newSlide.value.content,
    }

    const createResponse = await axios.post(
      `${config.apiBaseUrl}/slides/${userId.value}/${selectedLectureId.value}/create`,
      slideData,
    )

    const slideId = createResponse.data.slideId

    if (newSlide.value.file) {
      const formData = new FormData()
      formData.append('file', newSlide.value.file)

      await axios.post(
        `${config.apiBaseUrl}/slides/${userId.value}/${slideId}/updateFile`,
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
/* 动态背景与主容器 */
.teacher-course {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2f6 100%);
  min-height: 100vh;
}

/* 动态卡片效果 */
.course-list, .lecture-list, .add-slide, .student-progress {
  margin-bottom: 30px;
  padding: 25px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.course-list:hover, .lecture-list:hover,
.add-slide:hover, .student-progress:hover {
  transform: translateY(-2px);
  box-shadow:
    0 7px 14px rgba(0, 0, 0, 0.1),
    0 3px 6px rgba(0, 0, 0, 0.08);
}

/* 标题样式 */
h1 {
  color: #2c3e50;
  font-size: 2rem;
  margin-bottom: 1.5rem;
  position: relative;
  display: inline-block;
}

h1::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, #3498db, #2ecc71);
  border-radius: 3px;
}

h2 {
  color: #2c3e50;
  font-size: 1.5rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

/* 现代化输入控件 */
select, input, textarea {

  width: 100%;
  padding: 12px 15px;
  margin: 10px 0;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  background-color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 1em;
}

input:focus, textarea:focus, select:focus {
  border-color: #3498db;
  box-shadow:
    0 0 0 3px rgba(52, 152, 219, 0.2),
    inset 0 1px 3px rgba(0, 0, 0, 0.1);
  outline: none;
}

/* 增强按钮效果 */
button {
  padding: 12px 20px;
  margin: 8px 5px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.add-course-button, .add-lecture-button {
  background: linear-gradient(135deg, #3498db, #4361ee);

  color: white;
  margin-top: 15px;
}

.add-slide-button {
  background: linear-gradient(135deg, #2ecc71, #38b000);
  color: white;
  margin-top: 20px;
  padding: 14px 24px;
  font-weight: 600;
}

.clear-button {
  background: linear-gradient(135deg, #e74c3c, #d00000);
  color: white;
}

/* 按钮悬停效果 */
button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

button:active:not(:disabled) {
  transform: translateY(0);
}

button:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
}

/* 按钮点击涟漪效果 */
button::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  background: rgba(255, 255, 255, 0.5);
  opacity: 0;
  border-radius: 100%;
  transform: scale(1, 1) translate(-50%, -50%);
  transform-origin: 50% 50%;
}

button:focus:not(:active)::after {
  animation: ripple 0.6s ease-out;
}

@keyframes ripple {
  0% { transform: scale(0, 0); opacity: 0.5; }
  100% { transform: scale(20, 20); opacity: 0; }
}

/* 文件上传区域增强 */
.upload-area {
  margin-top: 20px;
  padding: 30px;
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  text-align: center;
  background: rgba(241, 245, 249, 0.5);
  transition: all 0.3s;
}

.upload-area.drag-over {
  border-color: #2ecc71;
  background: rgba(46, 204, 113, 0.05);
  animation: pulseBorder 1.5s infinite;
}

@keyframes pulseBorder {
  0% { box-shadow: 0 0 0 0 rgba(46, 204, 113, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(46, 204, 113, 0); }
  100% { box-shadow: 0 0 0 0 rgba(46, 204, 113, 0); }
}

.upload-prompt {
  margin-bottom: 15px;
  color: #64748b;
  font-size: 1.1rem;
}

.custom-file-label {
  padding: 12px 24px;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  color: #334155;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.custom-file-label:hover {
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
}

/* PDF预览区域 */
.pdf-container {
  width: 100%;
  height: 600px;
  margin-top: 25px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.pdf-viewer {
  width: 100%;
  height: 100%;
  border: none;
}

/* 学生进度条增强 */
progress {
  height: 10px;
  border-radius: 5px;
}

progress::-webkit-progress-bar {
  background-color: #f1f5f9;
  border-radius: 5px;
}

progress::-webkit-progress-value {
  background: linear-gradient(90deg, #3b82f6, #6366f1);
  border-radius: 5px;
}

.progress-value {
  font-weight: 700;
  color: #3b82f6;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .teacher-course {
    padding: 20px;
  }

  .upload-controls {
    flex-direction: column;
    gap: 10px;
  }

  .pdf-container {
    height: 400px;
  }
}

/* 微调动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.course-list, .lecture-list, .add-slide, .student-progress {
  animation: fadeIn 0.4s ease-out forwards;
}

.course-list { animation-delay: 0.1s; }
.lecture-list { animation-delay: 0.2s; }
.add-slide { animation-delay: 0.3s; }
.student-progress { animation-delay: 0.4s; }
</style>
