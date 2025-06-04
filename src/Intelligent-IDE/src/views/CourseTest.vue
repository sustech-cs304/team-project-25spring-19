<template>
  <div class="course-test">
    <h1>{{ courseTitle }} - {{ lectureTitle }} Test</h1>

    <div class="test-section">
      <h2>Test Questions</h2>
      <div v-if="loading" class="loading">Loading questions...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else class="question-list">
        <div v-for="question in questions" :key="question.id" class="question-card">
          <div class="question-header" @click="toggleQuestion(question.id)">
            <h3>{{ question.text }}</h3>
            <span class="toggle-icon">
              {{ question.isExpanded ? '▼' : '▶' }}
            </span>
          </div>

          <div v-show="question.isExpanded" class="question-content">
            <div class="answer-options">
              <button
                :class="['answer-button', { selected: question.userAnswer === true }]"
                :disabled="question.showAnswer"
                @click="selectAnswer(question.id, true)"
              >
                True
              </button>
              <button
                :class="['answer-button', { selected: question.userAnswer === false }]"
                :disabled="question.showAnswer"
                @click="selectAnswer(question.id, false)"
              >
                False
              </button>
            </div>

            <div v-if="question.showAnswer" class="answer-section">
              <h4>Result</h4>
              <div v-if="question.answerLoading" class="loading">Loading answer...</div>
              <div v-else-if="question.answerError" class="error">{{ question.answerError }}</div>
              <div v-else class="answer-content">
                <span
                  :class="[
                    'answer-status',
                    { correct: question.isCorrect, incorrect: !question.isCorrect },
                  ]"
                >
                  {{ question.isCorrect ? 'Correct' : 'Incorrect' }}
                </span>
                (Correct Answer: {{ question.correctAnswer ? 'True' : 'False' }})
              </div>
            </div>

            <div v-if="question.showExplanation" class="explanation-section">
              <h4>Explanation</h4>
              <div v-if="question.explanationLoading" class="loading">Loading explanation...</div>
              <div v-else-if="question.explanationError" class="error">
                {{ question.explanationError }}
              </div>
              <div v-else class="explanation-content">
                <div>{{ question.explanation }}</div>
              </div>
            </div>

            <div
              v-if="question.userAnswer !== null && !question.showAnswer"
              class="explanation-button-container"
            >
              <button class="explanation-button" @click="fetchAnswerAndExplanation(question.id)">
                Check Answer & View Explanation
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="showScore" class="score-section">
        <h2>Your Score</h2>
        <div class="score-content">
          {{ totalScore }} / {{ questions.length }} ({{
            ((totalScore / questions.length) * 100).toFixed(2)
          }}%)
        </div>
        <button class="back-button" @click="returnToDashboard">Back to Dashboard</button>
      </div>

      <div v-else class="test-controls">
        <button
          class="submit-test-button"
          :disabled="questions.length === 0 || questions.some((q) => q.userAnswer === null)"
          @click="submitTest"
        >
          Submit Test
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { getLectureSlidesByLectureId, LectureSlideDTO } from '@/api/pdf'

interface Question {
  id: string
  text: string
  isExpanded: boolean
  userAnswer: boolean | null
  correctAnswer: boolean | null
  isCorrect: boolean | null
  showAnswer: boolean
  showExplanation: boolean
  explanation: string | null
  answerLoading: boolean
  answerError: string | null
  explanationLoading: boolean
  explanationError: string | null
}

export default defineComponent({
  name: 'CourseTest',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const courseId = ref(route.params.id as string)
    const lectureId = ref((route.query.lectureId as string) || '1') // Default to '1' if not provided
    const courseTitle = ref('')
    const lectureTitle = ref(`Lecture ${lectureId.value}`)
    const questions = ref<Question[]>([])
    const loading = ref(true)
    const error = ref<string | null>(null)
    const showScore = ref(false)

    // Calculate total score
    const totalScore = computed(() => {
      return questions.value.filter((q) => q.isCorrect).length
    })

    // Load course data from sessionStorage
    const loadCourseData = () => {
      const courseData = sessionStorage.getItem(`course_${courseId.value}`)
      if (courseData) {
        const course = JSON.parse(courseData)
        courseTitle.value = course.title || `Course ${courseId.value}`
        // Try to find lecture title from course data
        const lecture = course.lectures?.find(
          (l: { id: number }) => l.id === Number(lectureId.value),
        )
        lectureTitle.value = lecture?.title || `Lecture ${lectureId.value}`
      } else {
        courseTitle.value = `Course ${courseId.value}`
      }
    }

    // Generate questions using /ai/generate_truefalse
    const generateQuestions = async () => {
      try {
        console.log(sessionStorage.getItem('currentCourseId'))
        // 获取 slideId（假设从 slidesData 或其他地方获取）
        const slidesData = await getLectureSlidesByLectureId(Number(lectureId.value))
        if (slidesData.length === 0) {
          error.value = '此讲座没有可用的幻灯片'
          alert(error.value)
          return
        }
        const slideId = slidesData[0].slideId

        // 从 sessionStorage 中获取 PDF 数据
        const pdfStorageKey = `lecture_pdf_${lectureId.value}`
        const pdfBase64String = sessionStorage.getItem(pdfStorageKey)

        if (!pdfBase64String) {
          error.value = '无法找到PDF数据，请先阅读幻灯片'
          alert(error.value)
          return
        }

        // 处理 PDF 数据
        let pdfBlob
        try {
          // 移除可能的前缀
          const base64Data = pdfBase64String.replace(/^data:application\/pdf;base64,/, '')
          const decoded = atob(base64Data)
          const byteArray = new Uint8Array(decoded.split('').map((char) => char.charCodeAt(0)))
          pdfBlob = new Blob([byteArray], { type: 'application/pdf' })

          // 调试信息
          console.log('PDF Blob size:', pdfBlob.size)
          console.log('PDF Blob type:', pdfBlob.type)
        } catch (e) {
          error.value = 'PDF数据处理失败'
          console.error('PDF处理错误:', e)
          alert(error.value)
          return
        }

        // 验证 Blob 大小
        if (pdfBlob.size === 0) {
          error.value = 'PDF文件为空'
          console.error('PDF Blob 大小为0')
          alert(error.value)
          return
        }

        // 构建 FormData
        const formData = new FormData()
        const pdfFile = new File([pdfBlob], `lecture_${lectureId.value}.pdf`, {
          type: 'application/pdf',
        })
        formData.append('pdf_file', pdfFile)

        // 调试 FormData
        console.log('FormData entries:', [...formData.entries()])
        console.log('PDF File size:', pdfFile.size)
        console.log('PDF File type:', pdfFile.type)

        // 发送请求到后端
        try {
          loading.value = true // 显示加载状态
          const response = await axios.post(
            'http://127.0.0.1:8000/ai/generate_truefalse',
            formData,
            {
              // headers: {
              //   'Content-Type': 'multipart/form-data',
              //   Accept: 'application/json',
              // },
              timeout: 120000, // 增加超时时间到 120 秒
              onUploadProgress: (progressEvent) => {
                const percentCompleted = Math.round(
                  (progressEvent.loaded * 100) / progressEvent.total,
                )
                console.log('Upload progress:', percentCompleted + '%')
              },
              transformResponse: [
                (data) => {
                  console.log('Raw response data:', data)
                  try {
                    // 尝试解析 JSON 字符串
                    const parsedData = typeof data === 'string' ? JSON.parse(data) : data
                    console.log('Parsed response data:', parsedData)
                    return parsedData
                  } catch (e) {
                    console.error('Failed to parse response data:', e)
                    return data
                  }
                },
              ],
            },
          )

          // 验证响应格式
          console.log('Response validation:', {
            hasData: !!response.data,
            dataType: typeof response.data,
            hasQuestionId: response.data?.question_id,
            hasQuestion: response.data?.question,
            fullResponse: response.data,
          })

          if (response.data && response.data.question_id && response.data.question) {
            console.log('问题生成成功:', response.data)
            error.value = null
            loading.value = false
            return response.data
          } else {
            const errorMsg = '服务器返回的数据格式不正确: ' + JSON.stringify(response.data)
            console.error(errorMsg)
            throw new Error(errorMsg)
          }
        } catch (uploadError) {
          loading.value = false // 确保加载状态被关闭

          if (axios.isAxiosError(uploadError)) {
            console.error('上传错误详情:', {
              status: uploadError.response?.status,
              statusText: uploadError.response?.statusText,
              data: uploadError.response?.data,
              message: uploadError.message,
              config: {
                url: uploadError.config?.url,
                method: uploadError.config?.method,
                headers: uploadError.config?.headers,
              },
            })

            // 根据错误类型提供不同的错误信息
            let errorMessage = '生成问题失败'
            if (uploadError.code === 'ECONNABORTED') {
              errorMessage = '请求超时，请稍后重试'
            } else if (uploadError.response?.status === 500) {
              const errorData =
                typeof uploadError.response.data === 'string'
                  ? JSON.parse(uploadError.response.data)
                  : uploadError.response.data

              if (errorData.detail?.includes('Connection error')) {
                errorMessage = '服务器连接错误，请稍后重试'
              } else {
                errorMessage = errorData.detail || errorData.message || '服务器内部错误'
              }
            } else if (uploadError.response?.status === 422) {
              errorMessage = 'PDF文件格式不正确'
            }

            error.value = errorMessage
            alert(errorMessage)
          } else {
            console.error('未知上传错误:', uploadError)
            error.value =
              uploadError instanceof Error ? uploadError.message : '生成问题失败（未知错误）'
            alert(error.value)
          }
          return null
        }
      } catch (err) {
        loading.value = false // 确保加载状态被关闭
        console.error('生成问题时发生错误:', err)
        error.value = err instanceof Error ? err.message : '生成问题失败（未知错误）'
        alert(error.value)
        return null
      }
    }

    // Fetch questions using /ai/truefalse/list
    const fetchQuestions = async () => {
      try {
        loading.value = true
        const response = await axios.get('http://127.0.0.1:8000/ai/truefalse/list', {
          // headers: {
          //   'Content-Type': 'application/json',
          //   Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
          // },
          params: {
            lecture_id: lectureId.value, // Pass lectureId as query param
          },
        })
        questions.value = response.data.questions.map(
          (q: { question_id: string; question: string }) => ({
            id: q.question_id,
            text: q.question,
            isExpanded: false,
            userAnswer: null,
            correctAnswer: null,
            isCorrect: null,
            showAnswer: false,
            showExplanation: false,
            explanation: null,
            answerLoading: false,
            answerError: null,
            explanationLoading: false,
            explanationError: null,
          }),
        )
        loading.value = false
      } catch (err) {
        error.value = err instanceof Error ? err.message : 'Failed to load questions'
        loading.value = false
      }
    }

    const toggleQuestion = (questionId: string) => {
      questions.value = questions.value.map((q) =>
        q.id === questionId ? { ...q, isExpanded: !q.isExpanded } : q,
      )
    }

    const selectAnswer = (questionId: string, answer: boolean) => {
      questions.value = questions.value.map((q) =>
        q.id === questionId ? { ...q, userAnswer: answer } : q,
      )
    }

    const fetchAnswerAndExplanation = async (questionId: string) => {
      try {
        questions.value = questions.value.map((q) =>
          q.id === questionId
            ? {
                ...q,
                answerLoading: true,
                explanationLoading: true,
                answerError: null,
                explanationError: null,
              }
            : q,
        )

        const [answerResponse, explanationResponse] = await Promise.all([
          axios.get(`http://127.0.0.1:8000/ai/truefalse/${questionId}/answer`, {
            // headers: {
            //   'Content-Type': 'application/json',
            //   Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
            // },
          }),
          axios.get(`http://127.0.0.1:8000/ai/truefalse/${questionId}/explanation`, {
            // headers: {
            //   'Content-Type': 'application/json',
            //   Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
            // },
          }),
        ])

        questions.value = questions.value.map((q) =>
          q.id === questionId
            ? {
                ...q,
                showAnswer: true,
                showExplanation: true,
                correctAnswer: answerResponse.data.answer,
                isCorrect: q.userAnswer === answerResponse.data.answer,
                explanation: explanationResponse.data.explanation || 'No explanation available',
                answerLoading: false,
                explanationLoading: false,
              }
            : q,
        )
      } catch (err) {
        questions.value = questions.value.map((q) =>
          q.id === questionId
            ? {
                ...q,
                answerLoading: false,
                explanationLoading: false,
                answerError: err instanceof Error ? err.message : 'Failed to load answer',
                explanationError: err instanceof Error ? err.message : 'Failed to load explanation',
              }
            : q,
        )
      }
    }

    const submitTest = async () => {
      try {
        const score = questions.value.filter((q) => q.isCorrect).length
        const totalQuestions = questions.value.length
        const scorePercentage = (score / totalQuestions) * 100

        const userId = sessionStorage.getItem('userId')
        if (!userId) {
          throw new Error('User ID not found in sessionStorage')
        }

        const slideId = 1
        const course_id = sessionStorage.getItem('currentCourseId')
        

        // Create progress
        let progressId
        try {
          const createResponse = await axios.post(
            `http://10.13.189.15:8080/api/process/${userId}/${course_id}/${lectureId.value}/${slideId}/create`,
            {},
            // {
            //   headers: {
            //     Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
            //   },
            // },
          )
          progressId = createResponse.data.progressId
          console.log('Full response from progress creation:', createResponse.data)
          if (!progressId) {
            throw new Error('Progress ID not returned from server')
          }
          console.log('Progress created with ID:', progressId)
        } catch (createError) {
          console.error('Failed to create progress:', createError)
          // Fallback to sessionStorage
          // progressId = `fallback_${courseId.value}_${lectureId.value}_${slideId}`
        }

        // Determine progress state
        let progressState = '未完成'
        if (scorePercentage >= 80) {
          progressState = '已完成'
        } else if (scorePercentage > 0) {
          progressState = '已开始'
        }

        // Update progress
        try {
          await axios.put(
            `http://10.13.189.15:8080/api/process/${progressId}/update`,
            {
              state: progressState , // Example if the server expects nested "data"
            },
            // {
            //   headers: {
            //     Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
            //   },
            // },
          )
        } catch (updateError) {
          console.error('Failed to update progress:', updateError)
        }

        // Update sessionStorage for compatibility
        // const progressKey = `course_progress_${courseId.value}_${lectureId.value}`
        // sessionStorage.setItem(progressKey, progressState)

        // const courseKey = `course_${courseId.value}`
        // const courseData = sessionStorage.getItem(courseKey)
        // if (courseData) {
        //   const course = JSON.parse(courseData)
        //   const updatedLectures = course.lectures.map((lecture: any) => {
        //     if (lecture.id === Number(lectureId.value)) {
        //       return {
        //         ...lecture,
        //         progressState,
        //         completed: progressState === '已完成',
        //       }
        //     }
        //     return lecture
        //   })
        //   sessionStorage.setItem(
        //     courseKey,
        //     JSON.stringify({
        //       ...course,
        //       lectures: updatedLectures,
        //     }),
        //   )
        // }

        console.log(`Progress updated: ${progressState} (Score: ${scorePercentage}%)`)
        showScore.value = true
      } catch (error) {
        console.error('Error submitting test:', error)
        alert('提交测试失败，请稍后重试')
      }
    }

    const returnToDashboard = () => {
      router.push({ name: 'CourseDashboard' })
    }

    onMounted(async () => {
      loadCourseData()
      await generateQuestions()
      await fetchQuestions()
    })

    return {
      courseTitle,
      lectureTitle,
      questions,
      loading,
      error,
      showScore,
      totalScore,
      toggleQuestion,
      selectAnswer,
      fetchAnswerAndExplanation,
      submitTest,
      returnToDashboard,
    }
  },
})
</script>

<style scoped>
.course-test {
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

.test-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

h2 {
  color: #34495e;
  margin-bottom: 1rem;
  font-size: 1.25rem;
}

.question-card {
  border: 1px solid #eaecef;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  cursor: pointer;
  transition: background 0.2s;
}

.question-header:hover {
  background: #f1f3f5;
}

.toggle-icon {
  color: #6c757d;
}

.question-content {
  padding: 1rem;
}

.answer-options {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.answer-button {
  padding: 0.5rem 1rem;
  border: 1px solid #eaecef;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  transition: background 0.2s;
}

.answer-button:hover {
  background: #f8f9fa;
}

.answer-button.selected {
  background: #4caf50;
  color: white;
  border-color: #4caf50;
}

.answer-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.answer-section {
  margin-top: 1rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 4px;
}

.answer-content {
  font-size: 0.9em;
  color: #34495e;
}

.answer-status.correct {
  color: #4caf50;
  font-weight: bold;
}

.answer-status.incorrect {
  color: #ff4444;
  font-weight: bold;
}

.explanation-section {
  margin-top: 1rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 4px;
}

.explanation-content {
  font-size: 0.9em;
  color: #34495e;
}

.explanation-button-container {
  text-align: right;
  margin-top: 1rem;
}

.explanation-button {
  padding: 0.5rem 1rem;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.explanation-button:hover {
  background: #45a049;
}

.score-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  margin-top: 2rem;
}

.score-content {
  font-size: 1.2em;
  color: #34495e;
  margin-bottom: 1rem;
}

.back-button {
  padding: 0.75rem 1.5rem;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.back-button:hover {
  background: #45a049;
}

.test-controls {
  text-align: right;
  margin-top: 1rem;
}

.submit-test-button {
  padding: 0.75rem 1.5rem;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-test-button:hover {
  background: #45a049;
}

.submit-test-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.loading,
.error {
  padding: 1rem;
  text-align: center;
  color: #666;
}

.error {
  color: #ff4444;
}
</style>
