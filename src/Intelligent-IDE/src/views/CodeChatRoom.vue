<template>
  <div class="code-chat-room">
    <div class="header">
      <h1>{{ room?.name || '代码编辑室' }}</h1>
      <div class="language-badge">{{ room?.language || 'javascript' }}</div>
      <div class="actions">
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
        </div>
        <div class="editor-container">
          <textarea 
            v-model="code" 
            @input="handleCodeChange" 
            class="code-textarea"
            :placeholder="'在这里输入' + (room?.language || 'JavaScript') + '代码...'"
          ></textarea>
        </div>
      </div>
      
      <div class="chat-panel">
        <div class="messages-container">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.type.toLowerCase()]">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import codeRoomApi, { type CodeRoom, type CodeMessage } from '../api/codeRoomApi';
import websocketService from '../api/websocketService';

const route = useRoute();
const router = useRouter();
const roomId = ref<string>(route.params.id as string);
const room = ref<CodeRoom | null>(null);
const code = ref<string>('');
const messages = ref<CodeMessage[]>([]);
const chatMessage = ref<string>('');
const userId = ref<string>(sessionStorage.getItem('userId') || '1');
const userName = ref<string>(sessionStorage.getItem('currentUser') || '用户');

// 加载房间信息
const loadRoom = async () => {
  try {
    const roomData = await codeRoomApi.getRoom(Number(roomId.value));
    room.value = roomData;
    code.value = roomData.currentCode || '';
  } catch (error) {
    console.error('加载房间失败', error);
  }
};

// 处理收到的消息
const handleReceivedMessage = (message: CodeMessage) => {
  messages.value.push(message);
  
  // 如果是代码消息且不是自己发送的，则更新编辑器
  if (message.type === 'CODE' && message.senderId !== userId.value) {
    code.value = message.content;
  }
};

// 发送代码更新
let codeChangeTimeout: number | null = null;
const handleCodeChange = () => {
  if (codeChangeTimeout) {
    clearTimeout(codeChangeTimeout);
  }
  
  codeChangeTimeout = window.setTimeout(() => {
    sendCodeMessage();
  }, 500); // 防抖，500ms后发送
};

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
      type: 'CODE'
    };
    
    await websocketService.sendMessage(`room/${roomId.value}/code`, message);
  } catch (error) {
    console.error('发送代码失败', error);
  }
};

// 发送编辑消息
const sendChatMessage = async () => {
  if (!chatMessage.value.trim()) return;
  
  try {
    const message: CodeMessage = {
      roomId: roomId.value,
      senderId: userId.value,
      senderName: userName.value,
      content: chatMessage.value,
      language: room.value?.language || 'javascript',
      timestamp: new Date().toISOString(),
      type: 'CHAT'
    };
    
    await websocketService.sendMessage(`room/${roomId.value}/chat`, message);
    chatMessage.value = '';
  } catch (error) {
    console.error('发送消息失败', error);
  }
};

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
      type: 'JOIN'
    };
    
    await websocketService.sendMessage(`room/${roomId.value}/join`, message);
  } catch (error) {
    console.error('加入房间失败', error);
  }
};

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
      type: 'LEAVE'
    };
    
    await websocketService.sendMessage(`room/${roomId.value}/leave`, message);
  } catch (error) {
    console.error('离开房间失败', error);
  }
};

// 退出编辑室
const exitRoom = async () => {
  await leaveRoom();
  websocketService.unsubscribeFromRoom(roomId.value);
  router.push('/code-rooms');
};

// 监听路由变化
watch(() => route.params.id, (newId) => {
  if (newId && newId !== roomId.value) {
    roomId.value = newId as string;
    loadRoom();
  }
});

onMounted(async () => {
  await loadRoom();
  await websocketService.subscribeToRoom(roomId.value, handleReceivedMessage);
  await joinRoom();
});

onUnmounted(async () => {
  await leaveRoom();
  websocketService.unsubscribeFromRoom(roomId.value);
});
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
  background-color: #4CAF50;
  color: white;
  border-radius: 4px;
  font-size: 0.8rem;
}

.actions {
  margin-left: auto;
  margin-right: 1rem;
}

.exit-btn {
  padding: 0.5rem 1rem;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
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
}

.editor-header h3 {
  margin: 0;
}

.editor-container {
  flex: 1;
  overflow: auto;
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

.message.join, .message.leave {
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
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.chat-input button:hover {
  background-color: #45a049;
}
</style> 