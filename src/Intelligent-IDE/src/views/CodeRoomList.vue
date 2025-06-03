<template>
  <div class="code-room-list">
    <div class="header">
      <h1>代码聊天室列表</h1>
      <div class="header-actions">
        <button class="refresh-btn" @click="loadRooms" title="刷新列表">
          刷新
        </button>
        <button class="create-btn" @click="showCreateModal = true">创建新聊天室</button>
      </div>
    </div>
    
    <div class="view-options">
      <button 
        :class="['view-btn', { active: viewMode === 'mine' }]" 
        @click="setViewMode('mine')"
      >
        我的聊天室
      </button>
      <button 
        :class="['view-btn', { active: viewMode === 'all' }]" 
        @click="setViewMode('all')"
      >
        所有聊天室
      </button>
    </div>
    
    <!-- API状态提示 -->
    <div v-if="!apiHealthy" class="api-status-warning">
      <span>
        <strong>警告:</strong> 服务器连接异常，请检查后端服务是否正常运行。
        <button class="link-btn" @click="checkApiHealth">重试连接</button>
      </span>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-message">
      <span>{{ errorMessage }}</span>
      <button @click="errorMessage = ''" class="close-btn">&times;</button>
    </div>
    
    <div class="rooms-container">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      <div v-else-if="rooms.length === 0" class="no-rooms">
        {{viewMode === 'mine' ? '暂无您的代码编辑室，点击上方按钮创建一个新的编辑室' : '暂无可用的代码编辑室，点击上方按钮创建'}}
      </div>
      <div v-else class="room-grid">
        <div v-for="room in rooms" :key="room.id" class="room-card" @click="goToRoom(room.id)">
          <div class="room-header">
            <h3>{{ room.name }}</h3>
            <span class="language-badge">{{ room.language }}</span>
          </div>
          <div class="room-info">
            <p class="owner">创建者: {{ room.owner?.userName || '未知用户' }}</p>
            <p class="members">成员数: {{ room.members?.length || 0 }}</p>
            <p class="update-time">更新时间: {{ formatDate(room.updatedAt) }}</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 创建聊天室模态框 -->
    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal">
        <h2>创建新的代码聊天室</h2>
        <div v-if="createError" class="error-message in-modal">
          {{ createError }}
        </div>
        <div class="form-group">
          <label for="roomName">聊天室名称</label>
          <input 
            id="roomName" 
            v-model="newRoom.name" 
            type="text" 
            placeholder="输入聊天室名称"
          />
        </div>
        <div class="form-group">
          <label for="language">编程语言</label>
          <select id="language" v-model="newRoom.language">
            <option value="javascript">JavaScript</option>
            <option value="python">Python</option>
            <option value="java">Java</option>
            <option value="c++">C++</option>
            <option value="c#">C#</option>
            <option value="go">Go</option>
            <option value="rust">Rust</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showCreateModal = false">取消</button>
          <button class="create-btn" @click="createRoom" :disabled="!newRoom.name || creatingRoom">
            {{ creatingRoom ? '创建中...' : '创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import codeRoomApi, { type CodeRoom } from '../api/codeRoomApi';

const router = useRouter();
const rooms = ref<CodeRoom[]>([]);
const loading = ref<boolean>(true);
const showCreateModal = ref<boolean>(false);
const userId = ref<number>(Number(sessionStorage.getItem('userId')) || 1);
// 自动刷新定时器
const refreshInterval = ref<number | null>(null);
// API健康状态
const apiHealthy = ref<boolean>(true);
// 视图模式：mine-我的聊天室，all-所有聊天室
const viewMode = ref<'mine' | 'all'>('mine');
// 错误信息
const errorMessage = ref<string>('');
const createError = ref<string>('');
// 创建中状态
const creatingRoom = ref<boolean>(false);

const newRoom = ref({
  name: '',
  language: 'javascript'
});

// 检查API健康状态
const checkApiHealth = async () => {
  apiHealthy.value = await codeRoomApi.checkHealth();
  return apiHealthy.value;
};

// 加载聊天室列表
const loadRooms = async () => {
  loading.value = true;
  errorMessage.value = '';
  
  // 先检查API健康状态
  const isHealthy = await checkApiHealth();
  if (!isHealthy) {
    errorMessage.value = '服务器连接异常，无法加载聊天室列表';
    loading.value = false;
    rooms.value = [];
    return;
  }
  
  try {
    if (viewMode.value === 'mine') {
      rooms.value = await codeRoomApi.getUserRooms(userId.value);
    } else {
      rooms.value = await codeRoomApi.getAllRooms();
    }
  } catch (error) {
    console.error('加载聊天室列表失败', error);
    errorMessage.value = '加载聊天室列表失败，请稍后再试';
    rooms.value = []; // 确保为空数组
  } finally {
    loading.value = false;
  }
};

// 设置视图模式
const setViewMode = (mode: 'mine' | 'all') => {
  viewMode.value = mode;
  loadRooms();
};

// 创建新聊天室
const createRoom = async () => {
  if (!newRoom.value.name) return;
  
  // 检查API健康状态
  if (!apiHealthy.value) {
    createError.value = '服务器连接异常，无法创建聊天室';
    return;
  }
  
  createError.value = '';
  creatingRoom.value = true;
  
  try {
    const createdRoom = await codeRoomApi.createRoom(
      newRoom.value.name,
      newRoom.value.language,
      userId.value
    );
    
    // 重置表单
    newRoom.value.name = '';
    newRoom.value.language = 'javascript';
    showCreateModal.value = false;
    
    // 刷新聊天室列表
    await loadRooms();
    
    // 跳转到新创建的聊天室
    router.push(`/code-room/${createdRoom.id}`);
  } catch (error: any) {
    console.error('创建聊天室失败', error);
    createError.value = `创建失败: ${error.response?.data?.error || '服务器错误，请稍后再试'}`;
  } finally {
    creatingRoom.value = false;
  }
};

// 跳转到聊天室
const goToRoom = (roomId: number) => {
  router.push(`/code-room/${roomId}`);
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '未知时间';
  try {
    const date = new Date(dateString);
    return date.toLocaleString();
  } catch (e) {
    return '未知时间';
  }
};

// 启动定时刷新
const startAutoRefresh = () => {
  // 每30秒刷新一次列表和API健康状态
  refreshInterval.value = window.setInterval(() => {
    loadRooms();
  }, 30000);
};

// 清除定时刷新
const stopAutoRefresh = () => {
  if (refreshInterval.value !== null) {
    clearInterval(refreshInterval.value);
    refreshInterval.value = null;
  }
};

onMounted(async () => {
  // 首次加载时检查API健康状态
  await checkApiHealth();
  loadRooms();
  startAutoRefresh();
});

onUnmounted(() => {
  stopAutoRefresh();
});
</script>

<style scoped>
.code-room-list {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.header h1 {
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.view-options {
  display: flex;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #ddd;
}

.view-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 1rem;
  color: #666;
  transition: all 0.2s;
  position: relative;
}

.view-btn.active {
  color: #4CAF50;
  font-weight: bold;
}

.view-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #4CAF50;
}

.view-btn:hover {
  color: #45a049;
}

.api-status-warning {
  background-color: #fff3cd;
  color: #856404;
  padding: 0.75rem 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.link-btn {
  background: none;
  border: none;
  color: #0d6efd;
  text-decoration: underline;
  cursor: pointer;
  padding: 0;
  font-size: inherit;
}

.error-message {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 0.75rem 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-message.in-modal {
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.close-btn {
  background: none;
  border: none;
  color: #d32f2f;
  font-size: 1.2rem;
  cursor: pointer;
}

.refresh-btn {
  padding: 0.5rem 1rem;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.refresh-btn:hover {
  background-color: #0b7dda;
}

.create-btn {
  padding: 0.5rem 1rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.create-btn:hover:not([disabled]) {
  background-color: #45a049;
}

.create-btn[disabled] {
  background-color: #9e9e9e;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-left-color: #4CAF50;
  animation: spin 1s ease infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.no-rooms {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.room-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.room-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.room-header {
  padding: 1rem;
  background-color: #f5f5f5;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.language-badge {
  padding: 0.25rem 0.5rem;
  background-color: #4CAF50;
  color: white;
  border-radius: 4px;
  font-size: 0.8rem;
}

.room-info {
  padding: 1rem;
}

.room-info p {
  margin: 0.5rem 0;
  color: #666;
  font-size: 0.9rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  width: 100%;
  max-width: 500px;
}

.modal h2 {
  margin-top: 0;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

.cancel-btn {
  padding: 0.5rem 1rem;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}
</style> 