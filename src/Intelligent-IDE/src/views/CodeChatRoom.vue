<template>
  <div class="code-chat-room">
    <div class="header">
      <h1>{{ room?.name || '代码实时编辑室' }}</h1>
      <div class="language-badge">{{ room?.language || 'javascript' }}</div>
      <div class="actions">
        <button class="run-btn" @click="runCode" :disabled="!code.trim()">运行代码</button>
        <button 
          v-if="room?.owner?.userId === Number(userId)" 
          class="delete-btn" 
          @click="confirmDeleteRoom"
        >
          删除编辑室
        </button>
        <button class="exit-btn" @click="exitRoom">退出编辑室</button>
      </div>
      <div class="members">
        <span v-for="member in room?.members" :key="member.userId" class="member">
          {{ member.userName }}
        </span>
      </div>
    </div>

    <div class="content">
      <div class="code-editor">
        <div class="editor-header">
          <h3>代码编辑器</h3>
          <div class="editor-actions">
            <button class="highlight-btn" @click="toggleHighlight">
              {{ highlightEnabled ? '关闭高亮' : '开启高亮' }}
            </button>
          </div>
        </div>
        <div class="editor-container">
          <div v-if="highlightEnabled" class="highlighted-code" v-html="highlightedCode"></div>
          <textarea
            v-model="code"
            @input="handleCodeChange"
            @scroll="syncScroll"
            ref="codeTextarea"
            class="code-textarea"
            :class="{ 'transparent': highlightEnabled }"
            :placeholder="'在这里输入' + (room?.language || 'JavaScript') + '代码...'"
          ></textarea>
        </div>
        
        <!-- 代码运行结果 -->
        <div v-if="codeOutput" class="code-output">
          <div class="output-header">
            <h4>运行结果</h4>
            <button @click="codeOutput = ''" class="close-output">×</button>
          </div>
          <pre :class="['output-content', { 'error': codeOutputError }]">{{ codeOutput }}</pre>
        </div>
      </div>

      <div class="chat-panel">
        <div class="messages-container">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.type.toLowerCase()]"
          >
            <div class="message-header">
              <span class="sender">{{ msg.senderName }}</span>
              <span class="time">{{ msg.timestamp }}</span>
            </div>
            <div class="message-content">
              <pre v-if="msg.type === 'CODE'">{{ msg.content }}</pre>
              <p v-else>{{ msg.content }}</p>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <input
            v-model="chatMessage"
            @keyup.enter="sendChatMessage"
            placeholder="输入编辑消息..."
            type="text"
          />
          <button @click="sendChatMessage">发送</button>
        </div>
      </div>
    </div>

    <!-- 删除确认模态框 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal">
        <h3>确认删除</h3>
        <p>您确定要删除编辑室 "{{ room?.name }}" 吗？此操作不可撤销。</p>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showDeleteModal = false">取消</button>
          <button class="delete-confirm-btn" @click="deleteRoom" :disabled="deleting">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import codeRoomApi, { type CodeRoom, type CodeMessage } from '../api/codeRoomApi'
import websocketService from '../api/websocketService'
import hljs from 'highlight.js'
import 'highlight.js/styles/monokai-sublime.css'

const route = useRoute()
const router = useRouter()
const roomId = ref<string>(route.params.id as string)
const room = ref<CodeRoom | null>(null)
const code = ref<string>('')
const messages = ref<CodeMessage[]>([])
const chatMessage = ref<string>('')
const userId = ref<string>(sessionStorage.getItem('userId') || '1')
const userName = ref<string>(sessionStorage.getItem('currentUser') || '用户')

// 新增状态
const codeOutput = ref<string>('')
const codeOutputError = ref<boolean>(false)
const showDeleteModal = ref<boolean>(false)
const deleting = ref<boolean>(false)
const highlightEnabled = ref<boolean>(true) // 默认开启高亮
const codeTextarea = ref<HTMLTextAreaElement>()
const highlightedCodeRef = ref<HTMLElement>()

// 加载房间信息
const loadRoom = async () => {
  try {
    const roomData = await codeRoomApi.getRoom(Number(roomId.value))
    room.value = roomData
    code.value = roomData.currentCode || ''
  } catch (error) {
    console.error('加载房间失败', error)
  }
}

// 处理收到的消息
const handleReceivedMessage = (message: CodeMessage) => {
  messages.value.push(message)

  // 如果是代码消息且不是自己发送的，则更新编辑器
  if (message.type === 'CODE' && message.senderId !== userId.value) {
    code.value = message.content
  }
}

// 发送代码更新
let codeChangeTimeout: number | null = null
const handleCodeChange = () => {
  if (codeChangeTimeout) {
    clearTimeout(codeChangeTimeout)
  }

  codeChangeTimeout = window.setTimeout(() => {
    sendCodeMessage()
  }, 500) // 防抖，500ms后发送
}

// 发送代码消息
const sendCodeMessage = async () => {
  try {
    const message: CodeMessage = {
      roomId: roomId.value,
      senderId: userId.value,
      senderName: userName.value,
      content: code.value,
      language: room.value?.language || 'javascript',
      timestamp: new Date().toISOString(),
      type: 'CODE',
    }

    await websocketService.sendMessage(`room/${roomId.value}/code`, message)
  } catch (error) {
    console.error('发送代码失败', error)
  }
}

// 发送编辑消息
const sendChatMessage = async () => {
  if (!chatMessage.value.trim()) return

  try {
    const message: CodeMessage = {
      roomId: roomId.value,
      senderId: userId.value,
      senderName: userName.value,
      content: chatMessage.value,
      language: room.value?.language || 'javascript',
      timestamp: new Date().toISOString(),
      type: 'CHAT',
    }

    await websocketService.sendMessage(`room/${roomId.value}/chat`, message)
    chatMessage.value = ''
  } catch (error) {
    console.error('发送消息失败', error)
  }
}

// 加入房间
const joinRoom = async () => {
  try {
    const message: CodeMessage = {
      roomId: roomId.value,
      senderId: userId.value,
      senderName: userName.value,
      content: '',
      language: room.value?.language || 'javascript',
      timestamp: new Date().toISOString(),
      type: 'JOIN',
    }

    await websocketService.sendMessage(`room/${roomId.value}/join`, message)
  } catch (error) {
    console.error('加入房间失败', error)
  }
}

// 离开房间
const leaveRoom = async () => {
  try {
    const message: CodeMessage = {
      roomId: roomId.value,
      senderId: userId.value,
      senderName: userName.value,
      content: '',
      language: room.value?.language || 'javascript',
      timestamp: new Date().toISOString(),
      type: 'LEAVE',
    }

    await websocketService.sendMessage(`room/${roomId.value}/leave`, message)
  } catch (error) {
    console.error('离开房间失败', error)
  }
}

// 退出编辑室
const exitRoom = async () => {
  await leaveRoom()
  websocketService.unsubscribeFromRoom(roomId.value)
  router.push('/code-rooms')
}

// 运行代码
const runCode = async () => {
  if (!code.value.trim()) return

  try {
    codeOutputError.value = false
    const result = await codeRoomApi.runCode(Number(roomId.value), code.value, room.value?.language || 'javascript')
    codeOutput.value = result.output || '程序执行完成'
  } catch (error: any) {
    codeOutputError.value = true
    codeOutput.value = `运行错误: ${error.response?.data?.error || error.message || '未知错误'}`
  }
}

// 确认删除房间
const confirmDeleteRoom = () => {
  showDeleteModal.value = true
}

// 删除房间
const deleteRoom = async () => {
  deleting.value = true
  try {
    await codeRoomApi.deleteRoom(Number(roomId.value), Number(userId.value))
    router.push('/code-rooms')
  } catch (error: any) {
    console.error('删除房间失败', error)
    alert(`删除失败: ${error.response?.data?.error || '服务器错误'}`)
  } finally {
    deleting.value = false
    showDeleteModal.value = false
  }
}

// 代码高亮功能 - 使用highlight.js
const highlightedCode = computed(() => {
  if (!highlightEnabled.value || !code.value) return ''
  
  const language = getHighlightLanguage(room.value?.language || 'javascript')
  try {
    const result = hljs.highlight(code.value, { language })
    return result.value
  } catch (error) {
    // 如果指定语言失败，尝试自动检测
    try {
      const result = hljs.highlightAuto(code.value)
      return result.value
    } catch (autoError) {
      // 如果都失败了，返回原始代码（转义HTML）
      return escapeHtml(code.value)
    }
  }
})

// 映射项目语言到highlight.js语言标识符
const getHighlightLanguage = (language: string): string => {
  const languageMap: { [key: string]: string } = {
    'python': 'python',
    'java': 'java',
    'c++': 'cpp',
    'cpp': 'cpp',
    'c#': 'csharp',
    'csharp': 'csharp',
    'go': 'go',
    'rust': 'rust',
    'typescript': 'typescript',
    'html': 'html',
    'css': 'css',
    'sql': 'sql',
    'json': 'json',
    'xml': 'xml',
    'yaml': 'yaml',
    'markdown': 'markdown'
  }
  
  return languageMap[language.toLowerCase()] || 'javascript'
}

// HTML转义函数
const escapeHtml = (text: string): string => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 切换代码高亮
const toggleHighlight = () => {
  highlightEnabled.value = !highlightEnabled.value
  if (highlightEnabled.value) {
    nextTick(() => {
      syncScroll()
    })
  }
}

// 同步滚动
const syncScroll = () => {
  if (!highlightEnabled.value || !codeTextarea.value || !highlightedCodeRef.value) return
  
  highlightedCodeRef.value.scrollTop = codeTextarea.value.scrollTop
  highlightedCodeRef.value.scrollLeft = codeTextarea.value.scrollLeft
}

// 监听代码变化，实时更新高亮
watch(code, () => {
  if (highlightEnabled.value) {
    nextTick(() => {
      syncScroll()
    })
  }
}, { flush: 'post' })

// 监听路由变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId && newId !== roomId.value) {
      roomId.value = newId as string
      loadRoom()
    }
  },
)

onMounted(async () => {
  await loadRoom()
  await websocketService.subscribeToRoom(roomId.value, handleReceivedMessage)
  await joinRoom()
})

onUnmounted(async () => {
  await leaveRoom()
  websocketService.unsubscribeFromRoom(roomId.value)
})
</script>

<style scoped>
.code-chat-room {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.header {
  padding: 1rem;
  background-color: #f5f5f5;
  border-bottom: 1px solid #ddd;
  display: flex;
  align-items: center;
}

.header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.language-badge {
  margin-left: 1rem;
  padding: 0.25rem 0.5rem;
  background-color: #4caf50;
  color: white;
  border-radius: 4px;
  font-size: 0.8rem;
}

.actions {
  margin-left: auto;
  margin-right: 1rem;
  display: flex;
  gap: 0.5rem;
}

.run-btn {
  background-color: #2196f3;
}

.run-btn:hover:not([disabled]) {
  background-color: #0b7dda;
}

.run-btn[disabled] {
  background-color: #9e9e9e;
  cursor: not-allowed;
}

.delete-btn {
  background-color: #ff9800;
}

.delete-btn:hover {
  background-color: #f57c00;
}

.run-btn,
.delete-btn,
.exit-btn {
  padding: 0.5rem 1rem;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.exit-btn {
  background-color: #f44336;
}

.exit-btn:hover {
  background-color: #d32f2f;
}

.members {
  display: flex;
  gap: 0.5rem;
}

.member {
  background-color: #e0e0e0;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.code-editor {
  flex: 3;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #ddd;
}

.editor-header {
  padding: 0.5rem 1rem;
  background-color: #f9f9f9;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-header h3 {
  margin: 0;
}

.highlight-btn {
  padding: 0.25rem 0.5rem;
  background-color: #9c27b0;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.highlight-btn:hover {
  background-color: #7b1fa2;
}

.editor-container {
  flex: 1;
  overflow: auto;
  position: relative;
}

.highlighted-code {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  padding: 1rem;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.5;
  background-color: #272822;
  color: #f8f8f2;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow: auto;
  pointer-events: none;
  z-index: 1;
}

.code-textarea {
  width: 100%;
  height: 100%;
  padding: 1rem;
  border: none;
  resize: none;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.5;
  background-color: #272822;
  color: #f8f8f2;
  position: relative;
  z-index: 2;
}

.code-textarea.transparent {
  background-color: transparent;
  color: transparent;
  caret-color: #f8f8f2;
}

/* 代码高亮样式 */
.highlighted-code .keyword {
  color: #66d9ef;
  font-weight: bold;
}

.highlighted-code .string {
  color: #e6db74;
}

.highlighted-code .comment {
  color: #75715e;
  font-style: italic;
}

.highlighted-code .number {
  color: #ae81ff;
}

.code-output {
  padding: 1rem;
  border-top: 1px solid #ddd;
  background-color: #f9f9f9;
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.output-header h4 {
  margin: 0;
  color: #333;
}

.close-output {
  padding: 0.25rem 0.5rem;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.close-output:hover {
  background-color: #d32f2f;
}

.output-content {
  margin: 0;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 12px;
  padding: 0.5rem;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.output-content.error {
  color: #f44336;
  background-color: #ffebee;
  border-color: #f44336;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages-container {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.message {
  padding: 0.5rem;
  border-radius: 4px;
  max-width: 80%;
}

.message.code {
  background-color: #f0f0f0;
  align-self: flex-start;
  width: 100%;
}

.message.chat {
  background-color: #e3f2fd;
  align-self: flex-start;
}

.message.join,
.message.leave {
  background-color: #f9f9f9;
  align-self: center;
  font-style: italic;
  color: #666;
}

.message-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

.sender {
  font-weight: bold;
}

.time {
  color: #666;
}

.message-content pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 12px;
}

.chat-input {
  display: flex;
  padding: 1rem;
  border-top: 1px solid #ddd;
}

.chat-input input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
}

.chat-input button {
  padding: 0.5rem 1rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.chat-input button:hover {
  background-color: #45a049;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
}

.modal h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
}

.modal p {
  margin-bottom: 1.5rem;
  color: #666;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.cancel-btn {
  padding: 0.5rem 1rem;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.delete-confirm-btn {
  padding: 0.5rem 1rem;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.delete-confirm-btn:hover:not([disabled]) {
  background-color: #d32f2f;
}

.delete-confirm-btn[disabled] {
  background-color: #9e9e9e;
  cursor: not-allowed;
}
</style>
