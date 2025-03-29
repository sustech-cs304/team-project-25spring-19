<template>
  <div class="course-detail">
    <h1>Course: {{ courseTitle }}</h1>
    <div class="content-container">
      <!-- 幻灯片区域 -->
      <div class="slides-area">
        <h2>Slides</h2>
        <!-- 上传区域 -->
        <div class="upload-area" v-if="!slides.length">
          <p class="upload-prompt">请上传幻灯片</p>
          <div class="upload-controls">
            <input type="file" @change="handleFileChange" accept=".pptx" id="file-upload" />
            <label for="file-upload" class="custom-file-label">选择 PPTX 文件</label>
            <button @click="previewPptx" :disabled="!selectedFile || isLoading" class="upload-button">
              {{ isLoading ? '加载中...' : '预览' }}
            </button>
          </div>
        </div>
        <!-- 幻灯片预览 -->
        <div v-else class="slides-container">
          <div v-if="isLoading" class="loading">加载 PPTX 中...</div>
          <div v-else-if="errorMessage" class="error-message">{{ errorMessage }}</div>
          <div v-else class="pptx-content">
            <div v-for="(slide, index) in slides" :key="index" class="slide">
              <h3>Slide {{ index + 1 }}</h3>
              <div v-for="(item, i) in slide.items" :key="i" class="slide-item">
                <p v-if="item.type === 'text'">{{ item.content }}</p>
                <img v-if="item.type === 'image'" :src="item.content" alt="Slide Image" class="slide-image" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 代码区域 -->
      <div
        ref="codeWindow"
        class="code-area"
        :style="{ top: position.top + 'px', left: position.left + 'px' }"
        @mousedown="startDragging"
      >
        <div class="code-header" @mousedown="startDragging">
          <span>Code Editor</span>
          <button @click="toggleCodeWindow" class="toggle-button">
            {{ isCodeWindowMinimized ? 'Expand' : 'Minimize' }}
          </button>
        </div>
        <div v-if="!isCodeWindowMinimized" class="code-content">
          <div class="language-selector">
            <label for="language">Select Language:</label>
            <select id="language" v-model="selectedLanguage" @change="updateLanguage">
              <option value="javascript">JavaScript</option>
              <option value="python">Python</option>
              <option value="cpp">C</option>
              <option value="java">Java</option>
            </select>
          </div>
          <Codemirror
            v-model="code"
            :extensions="cmExtensions"
            class="code-editor"
          />
          <button @click="runCode" class="run-button">Run Code</button>
          <div class="code-output" v-if="output">
            <h3>Output:</h3>
            <pre :class="{ 'error-output': isError }">{{ output }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Codemirror } from 'vue-codemirror'
import { javascript } from '@codemirror/lang-javascript'
import { python } from '@codemirror/lang-python'
import { cpp } from '@codemirror/lang-cpp'
import { java } from '@codemirror/lang-java'
import { oneDark } from '@codemirror/theme-one-dark'
import JSZip from 'jszip'

interface SlideItem {
  type: 'text' | 'image'
  content: string
}

interface Slide {
  items: SlideItem[]
}

export default defineComponent({
  name: 'CourseDetail',
  components: {
    Codemirror
  },
  setup() {
    const route = useRoute()

    // 课程标题
    const courseId = computed(() => parseInt(route.params.id as string))
    const courseTitle = computed(() => {
      const titles: { [key: number]: string } = {
        1: 'Introduction to JavaScript',
        2: 'Vue.js Fundamentals',
        3: 'Advanced CSS'
      }
      return titles[courseId.value] || 'Unknown Course'
    })

    // 幻灯片数据
    const slides = ref<Slide[]>([])
    const selectedFile = ref<File | null>(null)
    const isLoading = ref(false)
    const errorMessage = ref<string | null>(null)

    // 处理文件选择
    const handleFileChange = (event: Event) => {
      const input = event.target as HTMLInputElement
      if (input.files && input.files.length > 0) {
        const file = input.files[0]
        if (file.size > 10 * 1024 * 1024) { // 限制 10MB
          errorMessage.value = '文件过大，请上传小于 10MB 的文件'
          return
        }
        selectedFile.value = file
      }
    }

    // 预览 PPTX
    const previewPptx = async () => {
      if (!selectedFile.value) return

      isLoading.value = true
      errorMessage.value = null
      slides.value = []

      try {
        const arrayBuffer = await selectedFile.value.arrayBuffer()
        const zip = await JSZip.loadAsync(arrayBuffer)

        const parsedSlides: Slide[] = []
        const slideFiles = Object.keys(zip.files)
          .filter((filename) => filename.match(/^ppt\/slides\/slide\d+\.xml$/))
          .sort((a, b) => {
            const numA = parseInt(a.match(/\d+/)![0])
            const numB = parseInt(b.match(/\d+/)![0])
            return numA - numB
          })

        const parser = new DOMParser()

        for (const slideFile of slideFiles) {
          const slideItems: SlideItem[] = []
          const slideXml = await zip.file(slideFile)!.async('string')
          const xmlDoc = parser.parseFromString(slideXml, 'text/xml')

          // 提取文本
          const textNodes = xmlDoc.getElementsByTagName('a:t')
          for (let i = 0; i < textNodes.length; i++) {
            const text = textNodes[i].textContent?.trim()
            if (text) {
              slideItems.push({ type: 'text', content: text })
            }
          }

          // 提取图片
          const imageNodes = xmlDoc.getElementsByTagName('a:blip')
          for (let i = 0; i < imageNodes.length; i++) {
            const embedId = imageNodes[i].getAttribute('r:embed')
            if (embedId) {
              const relsFile = slideFile.replace('slide', '_rels/slide') + '.rels'
              const relsXml = await zip.file(relsFile)?.async('string')
              if (relsXml) {
                const relsDoc = parser.parseFromString(relsXml, 'text/xml')
                const relNode = relsDoc.querySelector(`Relationship[Id="${embedId}"]`)
                const imagePath = relNode?.getAttribute('Target')
                if (imagePath) {
                  const fullImagePath = `ppt/${imagePath.replace('../', '')}`
                  const imageData = await zip.file(fullImagePath)?.async('blob')
                  if (imageData) {
                    const imageUrl = URL.createObjectURL(imageData)
                    slideItems.push({ type: 'image', content: imageUrl })
                  }
                }
              }
            }
          }

          if (slideItems.length) {
            parsedSlides.push({ items: slideItems })
          }
        }

        slides.value = parsedSlides.length ? parsedSlides : [{ items: [{ type: 'text', content: '无内容可显示' }] }]
      } catch (error) {
        console.error('PPTX 解析失败:', error)
        errorMessage.value = '无法解析 PPTX 文件，请检查文件格式或稍后重试'
        slides.value = []
      } finally {
        isLoading.value = false
        selectedFile.value = null
        const input = document.querySelector('input[type="file"]') as HTMLInputElement
        if (input) input.value = ''
      }
    }

    // 代码编辑器相关
    const selectedLanguage = ref('javascript')
    const code = ref(`console.log('Hello, World!');`)
    const cmExtensions = ref([javascript(), oneDark])
    const output = ref<string | null>(null)
    const isError = ref(false)

    const defaultCodeSnippets: { [key: string]: string } = {
      javascript: `console.log('Hello, World!');`,
      python: `print('Hello, World!')`,
      cpp: `#include <stdio.h>\n\nint main() {\n    printf("Hello, World!\\n");\n    return 0;\n}`,
      java: `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}`
    }

    const updateLanguage = () => {
      switch (selectedLanguage.value) {
        case 'javascript':
          cmExtensions.value = [javascript(), oneDark]
          break
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
          cmExtensions.value = [javascript(), oneDark]
      }
      code.value = defaultCodeSnippets[selectedLanguage.value] || defaultCodeSnippets['javascript']
      output.value = null
      isError.value = false
    }

    watch(selectedLanguage, updateLanguage)

    const checkCode = (language: string, code: string): { output: string | null; error: string | null } => {
      if (code.trim() === '') {
        return { output: null, error: 'Code is empty' }
      }
      switch (language) {
        case 'javascript':
          try {
            const result = eval(code)
            return { output: result !== undefined ? String(result) : 'No output', error: null }
          } catch (error) {
            return { output: null, error: (error as Error).message }
          }
        case 'python':
          if (!code.includes('print(')) {
            return { output: null, error: 'Missing print statement' }
          }
          return { output: `Simulated Python output:\n${code}`, error: null }
        case 'cpp':
          if (!code.includes('int main()') || !code.includes('return')) {
            return { output: null, error: 'Missing main function or return statement' }
          }
          return { output: `Simulated C output:\n${code}`, error: null }
        case 'java':
          if (!code.includes('public static void main')) {
            return { output: null, error: 'Missing main method' }
          }
          return { output: `Simulated Java output:\n${code}`, error: null }
        default:
          return { output: null, error: 'Unsupported language' }
      }
    }

    const runCode = () => {
      const result = checkCode(selectedLanguage.value, code.value)
      if (result.error) {
        isError.value = true
        output.value = `Error in ${selectedLanguage.value}:\n${result.error}`
      } else {
        isError.value = false
        output.value = result.output
      }
    }

    // 拖动相关
    const codeWindow = ref<HTMLElement | null>(null)
    const position = ref({ top: 50, left: 50 })
    const isDragging = ref(false)
    const dragOffset = ref({ x: 0, y: 0 })

    const startDragging = (event: MouseEvent) => {
      if ((event.target as HTMLElement).closest('.code-header')) {
        isDragging.value = true
        dragOffset.value = {
          x: event.clientX - position.value.left,
          y: event.clientY - position.value.top
        }
        document.addEventListener('mousemove', drag)
        document.addEventListener('mouseup', stopDragging)
      }
    }

    const drag = (event: MouseEvent) => {
      if (isDragging.value) {
        const newLeft = event.clientX - dragOffset.value.x
        const newTop = event.clientY - dragOffset.value.y
        const maxX = window.innerWidth - (codeWindow.value?.offsetWidth || 400)
        const maxY = window.innerHeight - (codeWindow.value?.offsetHeight || 300)
        position.value.left = Math.max(0, Math.min(newLeft, maxX))
        position.value.top = Math.max(0, Math.min(newTop, maxY))
      }
    }

    const stopDragging = () => {
      isDragging.value = false
      document.removeEventListener('mousemove', drag)
      document.removeEventListener('mouseup', stopDragging)
    }

    // 最小化/展开代码窗口
    const isCodeWindowMinimized = ref(false)
    const toggleCodeWindow = () => {
      isCodeWindowMinimized.value = !isCodeWindowMinimized.value
    }

    return {
      courseTitle,
      slides,
      selectedFile,
      isLoading,
      errorMessage,
      handleFileChange,
      previewPptx,
      selectedLanguage,
      code,
      cmExtensions,
      output,
      isError,
      runCode,
      updateLanguage,
      codeWindow,
      position,
      isDragging,
      startDragging,
      isCodeWindowMinimized,
      toggleCodeWindow
    }
  }
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

.slides-container {
  width: 100%;
  height: calc(100% - 40px);
  overflow-y: auto;
}

.pptx-content {
  width: 100%;
}

.slide {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.slide-item {
  margin: 10px 0;
}

.slide-image {
  max-width: 100%;
  height: auto;
  border-radius: 5px;
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

.code-area {
  position: absolute;
  width: 400px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.code-header {
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

.code-content {
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

.code-editor {
  height: 300px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.run-button {
  padding: 10px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.run-button:hover {
  background-color: #2980b9;
}

.code-output {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f6fa;
  border-radius: 5px;
  max-height: 150px;
  overflow-y: auto;
}

.error-output {
  color: #e74c3c;
}
</style>