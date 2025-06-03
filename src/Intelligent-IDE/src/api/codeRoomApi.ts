import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

// 创建一个axios实例，设置默认配置
const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000 // 10秒超时
});

// 响应拦截器，处理错误
apiClient.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export interface CodeRoom {
  id: number;
  name: string;
  language: string;
  currentCode: string;
  createdAt: string;
  updatedAt: string;
  owner: {
    userId: number;
    userName: string;
  };
  members: Array<{
    userId: number;
    userName: string;
  }>;
}

export interface CodeMessage {
  roomId: string;
  senderId: string;
  senderName: string;
  content: string;
  language: string;
  timestamp: string;
  type: 'CODE' | 'JOIN' | 'LEAVE' | 'CHAT';
}

const codeRoomApi = {
  createRoom: async (name: string, language: string, userId: number): Promise<CodeRoom> => {
    try {
      const response = await apiClient.post(`/rooms`, null, {
        params: {
          name,
          language,
          userId
        }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to create room:', error);
      throw error;
    }
  },
  
  getRoom: async (roomId: number): Promise<CodeRoom> => {
    try {
      const response = await apiClient.get(`/rooms/${roomId}`);
      return response.data;
    } catch (error) {
      console.error(`Failed to get room ${roomId}:`, error);
      throw error;
    }
  },
  
  getUserRooms: async (userId: number): Promise<CodeRoom[]> => {
    try {
      const response = await apiClient.get(`/rooms/user/${userId}`);
      return response.data || [];
    } catch (error) {
      console.error(`Failed to get rooms for user ${userId}:`, error);
      return []; // 返回空数组而不是抛出异常
    }
  },
  
  getAllRooms: async (): Promise<CodeRoom[]> => {
    try {
      const response = await apiClient.get(`/rooms/all`);
      return response.data || [];
    } catch (error) {
      console.error('Failed to get all rooms:', error);
      return []; // 返回空数组而不是抛出异常
    }
  },
  
  updateCode: async (roomId: number, code: string): Promise<CodeRoom> => {
    try {
      const response = await apiClient.put(`/rooms/${roomId}/code`, { code });
      return response.data;
    } catch (error) {
      console.error(`Failed to update code for room ${roomId}:`, error);
      throw error;
    }
  },
  
  addMember: async (roomId: number, userId: number): Promise<boolean> => {
    try {
      const response = await apiClient.post(`/rooms/${roomId}/members/${userId}`);
      return response.data?.success || false;
    } catch (error) {
      console.error(`Failed to add member ${userId} to room ${roomId}:`, error);
      return false;
    }
  },
  
  removeMember: async (roomId: number, userId: number): Promise<boolean> => {
    try {
      const response = await apiClient.delete(`/rooms/${roomId}/members/${userId}`);
      return response.data?.success || false;
    } catch (error) {
      console.error(`Failed to remove member ${userId} from room ${roomId}:`, error);
      return false;
    }
  },
  
  deleteRoom: async (roomId: number): Promise<boolean> => {
    try {
      const response = await apiClient.delete(`/rooms/${roomId}`);
      return response.data?.deleted || false;
    } catch (error) {
      console.error(`Failed to delete room ${roomId}:`, error);
      return false;
    }
  },
  
  // 检查API可用性
  checkHealth: async (): Promise<boolean> => {
    try {
      // 尝试访问诊断接口
      await apiClient.get('/diagnostic/db-info', { timeout: 5000 });
      return true;
    } catch (error) {
      console.error('API健康检查失败:', error);
      return false;
    }
  },
  
  // 用于获取诊断数据的方法
  getDiagnosticInfo: async (): Promise<any> => {
    try {
      const response = await apiClient.get('/diagnostic/db-info');
      return response.data;
    } catch (error) {
      console.error('获取诊断信息失败:', error);
      return { error: '服务器错误' };
    }
  }
};

export default codeRoomApi; 