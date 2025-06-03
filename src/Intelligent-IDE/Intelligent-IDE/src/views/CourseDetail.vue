<template>
  <!-- /** -->
  <!-- * AI-generated-content -->
  <!-- * tool: grok, chatgpt, copilot -->
  <!-- * version: latest -->
  <!-- * usage: 开局生成模板，然后自己调样式;in-text debugging;生成一个与code-editor一样的ai-assistant;实现拖拽功能；实现幻灯片（未成功）；实现静态语法检查-->
  <!-- */ -->
  <div class="course-detail">
    <h1>Course: {{ courseTitle }}</h1>
    <div class="content-container">
      <!-- 幻灯片区域 -->
      <div class="slides-area">
        <h2>Slides</h2>
        <!-- 上传区域 -->
        <div class="upload-area" v-if="!pdfUrl">
          <p class="upload-prompt">请上传PDF文件</p>
          <div class="upload-controls">
            <input type="file" @change="handleFileChange" accept=".pdf" id="file-upload" />
            <label for="file-upload" class="custom-file-label">选择 PDF 文件</label>
            <button
              @click="previewPdf"
              :disabled="!selectedFile || isLoading"
              class="upload-button"
            >
              {{ isLoading ? '加载中...' : '预览' }}
            </button>
          </div>
        </div>
        <!-- PDF预览 -->
        <div v-else class="pdf-container">
          <iframe :src="pdfUrl" class="pdf-viewer"></iframe>
        </div>
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
            <!-- <h3>Output:</h3> -->
            <pre :class="{ 'error-output': isError, 'code-output': !isError }">{{ output }}</pre>
          </div>
        </div>
      </div>

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
import { defineComponent, ref, computed, watch, onMounted, useId } from 'vue'
import { useRoute } from 'vue-router'
import { Codemirror } from 'vue-codemirror'
import { python } from '@codemirror/lang-python'
import { cpp } from '@codemirror/lang-cpp'
import { java } from '@codemirror/lang-java'
import { oneDark } from '@codemirror/theme-one-dark'
import JSZip from 'jszip'

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

export default defineComponent({
  name: 'CourseDetail',
  components: {
    Codemirror,
  },
  setup() {
    const route = useRoute()

    // 课程标题
    const courseId = computed(() => parseInt(route.params.id as string))
    const courseTitle = computed(() => {
      const titles: { [key: number]: string } = {
        1: 'Introduction to JavaScript',
        2: 'Vue.js Fundamentals',
        3: 'Advanced CSS',
      }
      return titles[courseId.value] || 'Unknown Course'
    })

    // 幻灯片数据
    const slides = ref<Slide[]>([])
    const selectedFile = ref<File | null>(null)
    const isLoading = ref(false)
    const errorMessage = ref<string | null>(null)
    const currentSlide = ref(0) // 添加当前幻灯片索引

    // PDF相关
    const pdfUrl = ref<string | null>(null)

    // 处理文件选择
    const handleFileChange = (event: Event) => {
      const input = event.target as HTMLInputElement
      if (input.files && input.files.length > 0) {
        const file = input.files[0]
        if (file.size > 10 * 1024 * 1024) {
          // 限制10MB
          alert('文件过大，请上传小于10MB的文件')
          return
        }
        selectedFile.value = file
      }
    }

    // 预览PDF
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
        const input = document.querySelector('input[type="file"]') as HTMLInputElement
        if (input) input.value = ''
      }
    }

    // 代码编辑器相关
    const selectedLanguage = ref('java')
    const code = ref(
      `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}`,
    )
    const cmExtensions = ref([java(), oneDark])
    const output = ref<string | null>(null)
    const isError = ref(false)
    const slideId = 1

    const defaultCodeSnippets: { [key: string]: string } = {
      python: `print('Hello, World!')`,
      cpp: `#include <stdio.h>\n\nint main() {\n    printf("Hello, World!\\n");\n    return 0;\n}`,
      java: `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}`,
    }

    const updateLanguage = () => {
      switch (selectedLanguage.value) {
        case 'python':
          cmExtensions.value = [python(), oneDark]
          break
        case 'cpp':
          cmExtensions.value = [cpp(), oneDark]
          break
        case 'java':
          cmExtensions.value = [java(), oneDark]
          break
        default:
          cmExtensions.value = [java(), oneDark]
      }
      code.value = defaultCodeSnippets[selectedLanguage.value] || defaultCodeSnippets['java']
      output.value = null
      isError.value = false
    }

    watch(selectedLanguage, updateLanguage)

    const runCode = async () => {
      console.log('runCode 被调用')
      console.log('参数:', selectedLanguage.value, code.value)
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
        console.log('runCodeApi 返回结果:', result)
        // 处理结果
      } catch (err) {
        console.error('错误:', err)
        // 处理错误
      }
    }
    // 笔记相关
    const notes = ref('')

    const loadNotes = async () => {
      try {
        console.log('userId in sessionStorage:', sessionStorage.getItem('userId'))
        const userId = sessionStorage.getItem('userId')
        if (!userId) throw new Error('User ID is missing')

        // 先查询是否存在笔记
        const result = await Notes.getNotesByUserLectureSlide(
          Number(userId),
          courseId.value,
          slideId,
        )

        if (result.length > 0) {
          // 有笔记，加载内容
          notes.value = result[0].content
        } else {
          // 无笔记，创建空笔记并初始化
          await Notes.createNote(Number(userId), courseId.value, slideId, { content: '' })
          notes.value = ''
        }
      } catch (error) {
        notes.value = ''
        console.error('加载笔记失败:', error)
      }
    }

    const saveNotes = async () => {
      try {
        const userId = sessionStorage.getItem('userId')
        if (!userId) throw new Error('User ID is missing')

        // 先查笔记，判断是更新还是创建
        const result = await Notes.getNotesByUserLectureSlide(
          Number(userId),
          courseId.value,
          slideId,
        )

        if (result.length > 0) {
          const noteId = result[0].id // 假设笔记对象里有id字段
          // 更新已有笔记
          await Notes.updateNote(noteId, Number(userId), courseId.value, slideId, {
            content: notes.value,
          })
        } else {
          // 创建新笔记
          await Notes.createNote(Number(userId), courseId.value, slideId, { content: notes.value })
        }
        alert('笔记已保存！')
      } catch (error) {
        console.error('保存笔记失败:', error)
        alert('保存失败，请重试')
      }
    }

    // AI 助手相关
    const prompt = ref('How to learn Python?')
    const aiOutput = ref<string | null>(null)

    const sendPrompt = async () => {
      console.log('sendPrompt 被调用')
      console.log('Prompt:', prompt.value)
      aiOutput.value = null
      isError.value = false

      try {
        const result = await sendPromptToAI(prompt.value)
        aiOutput.value = result
        console.log('sendPromptToAI 返回结果:', result)
      } catch (err) {
        console.error('错误:', err)
        isError.value = true
        aiOutput.value = '发生错误，请重试。'
      }
    }
    // 拖动相关
    const codePosition = ref({ top: 24, left: 109 }) // 默认位置：左侧顶部
    const notePosition = ref({ top: 24, left: 513 }) // 默认位置：右侧顶部
    const aiPosition = ref({ top: 24, left: 916 }) // 默认位置：右侧顶部
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
        // 存储事件处理器以便移除
        window as any, ((window as any).dragHandler = dragHandler)
        ;(window as any).stopHandler = stopHandler
      }
    }

    const drag = (event: MouseEvent, type: 'code' | 'note' | 'ai') => {
      if (isDragging.value[type]) {
        const position =
          type === 'code' ? codePosition : type === 'note' ? notePosition : aiPosition // 添加 aiPosition 的处理
        const newLeft = event.clientX - dragOffset.value.x
        const newTop = event.clientY - dragOffset.value.y
        const maxX = window.innerWidth - 400 // 假设窗口宽度为400px
        const maxY = window.innerHeight - 300 // 假设窗口高度为300px
        position.value.left = Math.max(0, Math.min(newLeft, maxX))
        position.value.top = Math.max(0, Math.min(newTop, maxY))
      }
    }

    const stopDragging = (type: 'code' | 'note' | 'ai') => {
      isDragging.value[type] = false
      document.removeEventListener('mousemove', (window as any).dragHandler)
      document.removeEventListener('mouseup', (window as any).stopHandler)
    }

    // 最小化/展开窗口
    const isCodeWindowMinimized = ref(true) // 默认折叠
    const isNoteWindowMinimized = ref(true) // 默认折叠
    const isAiWindowMinimized = ref(true) // Default minimized

    const toggleWindow = (type: 'code' | 'note' | 'ai') => {
      if (type === 'code') {
        isCodeWindowMinimized.value = !isCodeWindowMinimized.value
      } else if (type === 'note') {
        isNoteWindowMinimized.value = !isNoteWindowMinimized.value
      } else if (type === 'ai') {
        isAiWindowMinimized.value = !isAiWindowMinimized.value
      }
    }

    // 在组件挂载时加载笔记
    onMounted(() => {
      loadNotes()
    })

    return {
      courseTitle,
      slides,
      selectedFile,
      isLoading,
      errorMessage,
      handleFileChange,
      previewPdf,
      selectedLanguage,
      code,
      cmExtensions,
      output,
      aiOutput,
      isError,
      runCode,
      updateLanguage,
      codePosition,
      notePosition,
      aiPosition, // Added aiPosition to the returned object
      isDragging,
      startDragging,
      isCodeWindowMinimized,
      isNoteWindowMinimized,
      isAiWindowMinimized, // Default minimized
      toggleWindow,
      notes,
      saveNotes,
      prompt, // Added prompt to the returned object
      sendPrompt, // Added sendPrompt to the returned object
      currentSlide,
      pdfUrl,
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

.upload-area {
  height: calc(100% - 40px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #ecf0f1;
  border: 2px dashed #bdc3c7;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #3498db;
  background-color: #f5f6fa;
}

.upload-prompt {
  margin-bottom: 20px;
  font-size: 18px;
  color: #7f8c8d;
}

.upload-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
}

#file-upload {
  display: none;
}

.custom-file-label {
  padding: 10px 20px;
  background-color: #ecf0f1;
  color: #2c3e50;
  border: 1px solid #bdc3c7;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.custom-file-label:hover {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.upload-button {
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.upload-button:hover {
  background-color: #2980b9;
}

.upload-button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
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
.save-button:hover {
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

.editor-section {
  flex: 1;
  overflow: auto;
}

.controls {
  display: flex;
  justify-content: flex-end;
  padding: 10px;
}

.output-section {
  flex: 1;
  overflow: auto;
  background-color: #f5f5f5;
  padding: 10px;
}
.ai-response {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f0f8ff; /* Light blue background for better readability */
  padding: 15px; /* Increased padding for a cleaner look */
  border-radius: 8px; /* Slightly more rounded corners */
  border: 1px solid #b0c4de; /* Softer border color */
  color: #2c3e50; /* Darker text for better contrast */
  font-family: 'Courier New', Courier, monospace;
  font-size: 15px; /* Slightly larger font size for readability */
  line-height: 1.6; /* Improved line spacing */
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); /* Subtle shadow for a modern look */
}

.slide-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.nav-button {
  padding: 8px 16px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.nav-button:hover:not(:disabled) {
  background-color: #2980b9;
}

.nav-button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.slide-counter {
  font-size: 16px;
  color: #2c3e50;
}

.slide {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin: 0 auto;
  max-width: 800px;
  position: relative;
  aspect-ratio: 16/9;
  overflow: hidden; /* 防止内容溢出 */
}

.slide-item {
  position: absolute;
  margin: 0;
  padding: 0;
  white-space: pre-wrap; /* 保留换行和空格 */
  word-wrap: break-word; /* 允许长单词换行 */
  box-sizing: border-box; /* 确保padding不会影响总宽度 */
}

.slide-item p {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  line-height: 1.5; /* 增加行高 */
  white-space: pre-wrap; /* 保留换行和空格 */
  word-wrap: break-word; /* 允许长单词换行 */
  overflow: hidden; /* 防止文本溢出 */
}

.slide-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  margin: 0;
  padding: 0;
  display: block; /* 防止图片下方出现间隙 */
}

/* 添加文本容器样式 */
.text-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* 调整文本位置的计算 */
.slide-item[style*='position: absolute'] {
  transform: translate(-50%, -50%); /* 使用transform来居中定位 */
  left: calc(var(--x) * 1%); /* 使用CSS变量来存储位置 */
  top: calc(var(--y) * 1%);
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
