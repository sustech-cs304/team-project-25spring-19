<template>
  <div class="course-detail">
    <h1>Course: {{ courseTitle }}</h1>
    <div class="content-container">
      <!-- 幻灯片区域 -->
      <div class="slides-area">
        <h2>Slides</h2>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        <div v-else-if="pdfUrl" class="pdf-container">
          <iframe :src="pdfUrl" class="pdf-viewer"></iframe>
        </div>
        <div v-else class="loading">Loading PDF...</div>
      </div>

      <!-- 代码区域 -->
      <div
        ref="codeWindow"
        class="code-area"
        :style="{ top: codePosition.top + 'px', left: codePosition.left + 'px' }"
        @mousedown="startDragging('code', $event)"
      >
        <div class="code-header" @mousedown="startDragging('code', $event)">
          <span>Code Editor</span>
          <button @click="toggleWindow('code')" class="toggle-button">
            {{ isCodeWindowMinimized ? 'Expand' : 'Minimize' }}
          </button>
        </div>
        <div v-if="!isCodeWindowMinimized" class="code-content">
          <div class="language-selector">
            <label for="language">Select Language:</label>
            <select id="language" v-model="selectedLanguage" @change="updateLanguage">
              <option value="python">Python</option>
              <option value="cpp">C</option>
              <option value="java">Java</option>
            </select>
          </div>
          <Codemirror v-model="code" :extensions="cmExtensions" class="code-editor" />
          <button @click="runCode" class="run-button">Run Code</button>
          <div class="code-output" v-if="output">
            <pre :class="{ 'error-output': isError, 'code-output': !isError }">{{ output }}</pre>
          </div>
        </div>
      </div>

      <!-- AI助手区域 -->
      <div
        ref="aiWindow"
        class="ai-area"
        :style="{ top: aiPosition.top + 'px', left: aiPosition.left + 'px' }"
        @mousedown="startDragging('ai', $event)"
      >
        <div class="ai-header" @mousedown="startDragging('ai', $event)">
          <span>AI Assistant</span>
          <button @click="toggleWindow('ai')" class="toggle-button">
            {{ isAiWindowMinimized ? 'Expand' : 'Minimize' }}
          </button>
        </div>
        <div v-if="!isAiWindowMinimized" class="ai-content">
          <textarea
            v-model="prompt"
            class="prompt-editor"
            placeholder="Enter your prompt here..."
          ></textarea>
          <button @click="sendPrompt" class="send-button">Send Prompt</button>
          <div class="ai-output" v-if="aiOutput">
            <h3>AI Response:</h3>
            <pre class="ai-response">{{
              typeof aiOutput === 'string' ? aiOutput : JSON.stringify(aiOutput, null, 2)
            }}</pre>
          </div>
        </div>
      </div>

      <!-- 笔记区域 -->
      <div
        ref="noteWindow"
        class="note-area"
        :style="{ top: notePosition.top + 'px', left: notePosition.left + 'px' }"
        @mousedown="startDragging('note', $event)"
      >
        <div class="note-header" @mousedown="startDragging('note', $event)">
          <span>Notes</span>
          <button @click="toggleWindow('note')" class="toggle-button">
            {{ isNoteWindowMinimized ? 'Expand' : 'Minimize' }}
          </button>
        </div>
        <div v-if="!isNoteWindowMinimized" class="note-content">
          <textarea
            v-model="notes"
            class="note-editor"
            placeholder="Write your notes here..."
          ></textarea>
          <button @click="saveNotes" class="save-button">Save Notes</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { sendPromptToAI } from '@/api/ai'
import { runCodeAPI } from '@/api/codeEditor'
import * as Notes from '@/api/notes'
import { getLectureSlidesByLectureId, LectureSlideDTO } from '@/api/pdf'
import { defineComponent, ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Codemirror } from 'vue-codemirror'
import { python } from '@codemirror/lang-python'
import { cpp } from '@codemirror/lang-cpp'
import { java } from '@codemirror/lang-java'
import { oneDark } from '@codemirror/theme-one-dark'
import axios from 'axios'

interface SlideItem {
  type: 'text' | 'image'
  content: string
  x?: number
  y?: number
  width?: number
  height?: number
  fontSize?: number
  color?: string
  alignment?: string
}

interface Slide {
  items: SlideItem[]
}

interface NewSlide {
  content: string
  file: File | null
}

export default defineComponent({
  name: 'CourseDetail',
  components: {
    Codemirror,
  },
  setup() {
    const route = useRoute()

    // Course and lecture
    const courseId = computed(() => parseInt(route.params.id as string))
    const lectureId = ref(
      sessionStorage.getItem('currentLectureId') || (route.query.lectureId as string) || '1',
    )
    const courseTitle = computed(() => {
      const courseData = sessionStorage.getItem(`course_${courseId.value}`)
      if (courseData) {
        const course = JSON.parse(courseData)
        return course.title || 'Unknown Course'
      }
      return 'Unknown Course'
    })

    // Slide data
    const slides = ref<LectureSlideDTO[]>([])
    const pdfUrl = ref<string | null>(null)
    const errorMessage = ref<string | null>(null)
    const currentSlide = ref(0)
    const slideId = ref<number | null>(null)

    // New slide data
    const newSlide = ref<NewSlide>({ content: '', file: null })
    const userId = ref(sessionStorage.getItem('userId') || '1')
    const config = { apiBaseUrl: 'http://10.13.189.15:8080/api' } // 假设 API 基础 URL

    // Load slides
    // Load slides
    const loadSlides = async () => {
      try {
        const slidesData = await getLectureSlidesByLectureId(Number(lectureId.value))
        if (slidesData.length === 0) {
          errorMessage.value = 'No slides available for this lecture'
          return
        }
        slides.value = slidesData
        console.log('Full extractedText length:', slidesData[0].extractedText?.length)

        const pdfStorageKey = `lecture_pdf_${lectureId.value}`

        if (slidesData[0].url) {
          // 优先使用外部 URL
          pdfUrl.value = slidesData[0].url
          console.log('Using external URL:', pdfUrl.value)
        } else if (slidesData[0].extractedText) {
          try {
            const base64Data = slidesData[0].extractedText
            // 验证 base64 数据
            const decoded = atob(base64Data)
            const byteArray = new Uint8Array(decoded.split('').map((char) => char.charCodeAt(0)))
            const blob = new Blob([byteArray], { type: 'application/pdf' })
            // 生成 Blob URL
            const url = URL.createObjectURL(blob)
            pdfUrl.value = url
            console.log('Using Blob URL:', pdfUrl.value)

            sessionStorage.setItem(pdfStorageKey, base64Data)
            console.log('PDF Base64 data saved to sessionStorage.')

            // 如果需要下载，可以在这里保留下载逻辑
            // const link = document.createElement('a')
            // link.href = url
            // link.download = 'test.pdf'
            // link.click()
          } catch (e) {
            errorMessage.value = 'Invalid or unrenderable PDF data.'
            console.error('Base64 decode or PDF rendering error:', e)
          }
        } else {
          errorMessage.value = 'No PDF content available'
        }
        slideId.value = slidesData[0].slideId
      } catch (error) {
        errorMessage.value = 'Failed to load slides.'
        console.error('Error loading slides:', error)
      }
    }

    // Add slide
    const addSlide = async () => {
      if (!lectureId.value || (!newSlide.value.file && !newSlide.value.content)) {
        alert('Please provide a PDF file or content')
        return
      }

      if (newSlide.value.file) {
        if (newSlide.value.file.type !== 'application/pdf') {
          alert('Please upload a PDF file')
          return
        }
        if (newSlide.value.file.size > 10 * 1024 * 1024) {
          alert('File size exceeds 10MB limit')
          return
        }
      }

      try {
        const token = sessionStorage.getItem('access_token')
        const headers = token ? { Authorization: `Bearer ${token}` } : {}

        const slideData = { content: newSlide.value.content }
        const createResponse = await axios.post(
          `${config.apiBaseUrl}/slides/${userId.value}/${lectureId.value}/create`,
          slideData,
          { headers },
        )

        if (!createResponse.data.slideId) {
          throw new Error('Failed to retrieve slideId from create response')
        }

        const slideId = createResponse.data.slideId

        if (newSlide.value.file) {
          const formData = new FormData()
          formData.append('file', newSlide.value.file)
          await axios.post(
            `${config.apiBaseUrl}/slides/${userId.value}/${slideId}/updateFile`,
            formData,
            { headers: { ...headers } }, // axios 自动设置 multipart/form-data
          )
        }

        alert('Slide added successfully!')
        newSlide.value = { content: '', file: null }
        clearPdf()
        await loadSlides() // 刷新课件列表
      } catch (error) {
        const err = error as any
        console.error('Add slide error:', err.response?.data || err.message)
        alert(`Failed to add slide: ${err.response?.data?.message || 'Please try again'}`)
      }
    }

    // Clear PDF
    const clearPdf = () => {
      pdfUrl.value = null
      newSlide.value.file = null
    }

    // Code editor
    const codeWindow = ref<HTMLElement | null>(null)
    const isCodeWindowMinimized = ref(true)
    const selectedLanguage = ref('java')
    const code = ref(
      `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}`,
    )
    const cmExtensions = computed(() => {
      const langMap = {
        python: [python()],
        cpp: [cpp()],
        java: [java()],
      }
      return [...(langMap[selectedLanguage.value] || [java()]), oneDark]
    })
    const output = ref<string | null>(null)
    const isError = ref(false)

    const defaultCodeSnippets: { [key: string]: string } = {
      python: `print('Hello, World!')`,
      cpp: `#include <stdio.h>\n\nint main() {\n    printf("Hello, World!\\n");\n    return 0;\n}`,
      java: `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}`,
    }

    const updateLanguage = () => {
      code.value = defaultCodeSnippets[selectedLanguage.value] || defaultCodeSnippets['java']
      output.value = null
      isError.value = false
    }

    const runCode = async () => {
      output.value = null
      isError.value = false
      try {
        const result = await runCodeAPI({
          language: selectedLanguage.value,
          code: code.value,
        })
        output.value = result
        if (
          result
            .split(' ')
            .slice(0, 5)
            .some((word) => word.toLowerCase().includes('error'))
        ) {
          isError.value = true
        }
      } catch (err) {
        console.error('Run code error:', err)
        output.value = 'Error running code'
        isError.value = true
      }
    }

    // AI assistant
    const aiWindow = ref<HTMLElement | null>(null)
    const isAiWindowMinimized = ref(true)
    const prompt = ref('How to learn Python?')
    const aiOutput = ref<string | null>(null)

    const sendPrompt = async () => {
      aiOutput.value = null
      isError.value = false
      try {
        const result = await sendPromptToAI(prompt.value)
        aiOutput.value = result
      } catch (err) {
        console.error('AI prompt error:', err)
        aiOutput.value = 'Error processing prompt'
        isError.value = true
      }
    }

    // Notes
    const noteWindow = ref(null)
    const isNoteWindowMinimized = ref(true)
    const notes = ref('')

    // 定义一个函数来生成 sessionStorage 的键名，确保唯一性
    const getNoteStorageKey = (userId, slideId) => {
      // 结合 userId 和 slideId 来创建唯一的键
      // 假设 lectureId 也是构成唯一性的因素，如果 courseId 也需要，可以加上
      return `notes_${userId}_${slideId}`
    }

    const loadNotes = async () => {
      try {
        const userId = sessionStorage.getItem('userId')
        if (!userId) {
          console.warn('User ID is missing, cannot load notes from sessionStorage.')
          notes.value = '' // 清空笔记，因为无法确定用户
          return
        }
        if (!slideId.value) {
          console.warn('Slide ID is missing, cannot load notes from sessionStorage.')
          notes.value = '' // 清空笔记，因为无法确定幻灯片
          return
        }

        const storageKey = getNoteStorageKey(userId, slideId.value)
        const storedNotes = sessionStorage.getItem(storageKey)

        if (storedNotes !== null) {
          notes.value = storedNotes
          console.log('Notes loaded from sessionStorage.')
        } else {
          notes.value = '' // 如果没有存储的笔记，则初始化为空
          console.log('No notes found in sessionStorage for this slide.')
        }
      } catch (error) {
        notes.value = ''
        console.error('Error loading notes from sessionStorage:', error)
      }
    }

    const saveNotes = async () => {
      try {
        const userId = sessionStorage.getItem('userId')
        if (!userId) {
          console.error('User ID is missing, cannot save notes to sessionStorage.')
          alert('Failed to save notes: User ID is missing.')
          return
        }
        if (!slideId.value) {
          console.error('Slide ID is missing, cannot save notes to sessionStorage.')
          alert('Failed to save notes: Slide ID is missing.')
          return
        }

        const storageKey = getNoteStorageKey(userId, slideId.value)
        sessionStorage.setItem(storageKey, notes.value)
        console.log('Notes saved to sessionStorage.')
        alert('Notes saved successfully!')
      } catch (error) {
        console.error('Error saving notes to sessionStorage:', error)
        alert('Failed to save notes.')
      }
    }

    // Draggable windows
    const codePosition = ref({ top: 24, left: 109 })
    const notePosition = ref({ top: 24, left: 513 })
    const aiPosition = ref({ top: 24, left: 916 })
    const isDragging = ref({ code: false, note: false, ai: false })
    const dragOffset = ref({ x: 0, y: 0 })

    const startDragging = (type: 'code' | 'note' | 'ai', event: MouseEvent) => {
      if ((event.target as HTMLElement).closest(`.${type}-header`)) {
        isDragging.value[type] = true
        const position =
          type === 'code' ? codePosition : type === 'note' ? notePosition : aiPosition
        dragOffset.value = {
          x: event.clientX - position.value.left,
          y: event.clientY - position.value.top,
        }
        const dragHandler = (e: MouseEvent) => drag(e, type)
        const stopHandler = () => stopDragging(type)
        document.addEventListener('mousemove', dragHandler)
        document.addEventListener('mouseup', stopHandler)
        ;(window as any).dragHandler = dragHandler
        ;(window as any).stopHandler = stopHandler
      }
    }

    const drag = (event: MouseEvent, type: 'code' | 'note' | 'ai') => {
      if (isDragging.value[type]) {
        const position =
          type === 'code' ? codePosition : type === 'note' ? notePosition : aiPosition
        const newLeft = event.clientX - dragOffset.value.x
        const newTop = event.clientY - dragOffset.value.y
        const maxX = window.innerWidth - 400
        const maxY = window.innerHeight - 300
        position.value.left = Math.max(0, Math.min(newLeft, maxX))
        position.value.top = Math.max(0, Math.min(newTop, maxY))
      }
    }

    const stopDragging = (type: 'code' | 'note' | 'ai') => {
      isDragging.value[type] = false
      document.removeEventListener('mousemove', (window as any).dragHandler)
      document.removeEventListener('mouseup', (window as any).stopHandler)
    }

    const toggleWindow = (type: 'code' | 'note' | 'ai') => {
      if (type === 'code') {
        isCodeWindowMinimized.value = !isCodeWindowMinimized.value
      } else if (type === 'note') {
        isNoteWindowMinimized.value = !isNoteWindowMinimized.value
      } else if (type === 'ai') {
        isAiWindowMinimized.value = !isAiWindowMinimized.value
      }
    }

    // Load slides and notes on mount
    onMounted(async () => {
      await loadSlides()
      if (slideId.value) {
        await loadNotes()
      }
    })

    return {
      courseId,
      courseTitle,
      slides,
      pdfUrl,
      errorMessage,
      currentSlide,
      codeWindow,
      isCodeWindowMinimized,
      selectedLanguage,
      code,
      cmExtensions,
      output,
      isError,
      runCode,
      updateLanguage,
      aiWindow,
      isAiWindowMinimized,
      prompt,
      aiOutput,
      sendPrompt,
      noteWindow,
      isNoteWindowMinimized,
      notes,
      loadNotes,
      saveNotes,
      codePosition,
      notePosition,
      aiPosition,
      isDragging,
      startDragging,
      drag,
      stopDragging,
      toggleWindow,
      newSlide,
      addSlide,
      clearPdf,
    }
  },
})
</script>

<style scoped>
.course-detail {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
  color: var(--color-text);
}

h1 {
  color: var(--color-heading);
  margin-bottom: 20px;
  font-size: 24px;
}

.content-container {
  flex: 1;
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.slides-area {
  width: 100%;
  height: 100%;
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 5px;
  overflow-y: auto;
  box-sizing: border-box;
  color: var(--color-text);
}

.slides-area h2 {
  margin-bottom: 15px;
  font-size: 22px;
  color: #2c3e50;
}

.loading {
  text-align: center;
  padding: 20px;
  font-size: 16px;
  color: #7f8c8d;
}

.error-message {
  text-align: center;
  padding: 20px;
  font-size: 16px;
  color: #e74c3c;
}

.code-area,
.note-area,
.ai-area {
  position: absolute;
  width: 400px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.code-header,
.note-header,
.ai-header {
  background-color: #2c3e50;
  color: white;
  padding: 10px;
  cursor: move;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
}

.toggle-button {
  background-color: #3498db;
  border: none;
  padding: 5px 10px;
  color: white;
  border-radius: 3px;
  cursor: pointer;
}

.toggle-button:hover {
  background-color: #2980b9;
}

.code-content,
.note-content,
.ai-content {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.language-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.language-selector label {
  font-weight: bold;
}

.language-selector select {
  padding: 5px;
  border-radius: 5px;
  border: 1px solid #ddd;
  background-color: #fff;
}

.code-editor,
.prompt-editor {
  height: 200px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.run-button,
.save-button,
.send-button {
  padding: 10px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.run-button:hover,
.save-button:hover,
.send-button:hover {
  background-color: #2980b9;
}

.code-output,
.ai-output {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f6fa;
  border-radius: 5px;
  max-height: 150px;
  overflow-y: auto;
}

.error-output {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f6fa;
  border-radius: 5px;
  max-height: 150px;
  overflow-y: auto;
  color: #e74c3c;
}

.note-editor {
  width: 100%;
  height: 200px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  resize: none;
}

.ai-response {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f0f8ff;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #b0c4de;
  color: #2c3e50;
  font-family: 'Courier New', Courier, monospace;
  font-size: 15px;
  line-height: 1.6;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.pdf-container {
  width: 100%;
  height: calc(100% - 60px);
  display: flex;
  flex-direction: column;
}

.pdf-viewer {
  flex: 1;
  width: 100%;
  border: none;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}
</style>
