import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import type { CodeMessage } from './codeRoomApi';

// 确保全局对象存在
if (typeof window !== 'undefined' && !(window as any).global) {
  (window as any).global = window;
}

class WebSocketService {
  private stompClient: any = null;
  private connected: boolean = false;
  private messageHandlers: Map<string, ((message: CodeMessage) => void)[]> = new Map();

  connect(): Promise<boolean> {
    return new Promise((resolve) => {
      if (this.connected) {
        resolve(true);
        return;
      }

      // 使用 try-catch 包装 SockJS 创建
      try {
        const socket = new SockJS('http://10.13.189.15:8080/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, () => {
          this.connected = true;
          resolve(true);
        }, (error: any) => {
          console.error('WebSocket连接失败:', error);
          this.connected = false;
          resolve(false);
        });
      } catch (err) {
        console.error('创建SockJS实例时出错:', err);
        this.connected = false;
        resolve(false);
      }
    });
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
      this.connected = false;
      this.messageHandlers.clear();
    }
  }

  subscribeToRoom(roomId: string, callback: (message: CodeMessage) => void): Promise<void> {
    return new Promise(async (resolve, reject) => {
      if (!this.connected) {
        const connected = await this.connect();
        if (!connected) {
          reject('无法连接到WebSocket服务器');
          return;
        }
      }

      const destination = `/topic/room/${roomId}`;

      if (!this.messageHandlers.has(destination)) {
        this.messageHandlers.set(destination, []);

        this.stompClient.subscribe(destination, (message: any) => {
          const messageData = JSON.parse(message.body) as CodeMessage;

          const handlers = this.messageHandlers.get(destination) || [];
          handlers.forEach(handler => handler(messageData));
        });
      }

      // 添加新的回调处理函数
      const handlers = this.messageHandlers.get(destination) || [];
      handlers.push(callback);
      this.messageHandlers.set(destination, handlers);

      resolve();
    });
  }

  unsubscribeFromRoom(roomId: string, callback?: (message: CodeMessage) => void) {
    const destination = `/topic/room/${roomId}`;

    if (callback) {
      // 只移除特定的回调处理函数
      const handlers = this.messageHandlers.get(destination) || [];
      const index = handlers.indexOf(callback);
      if (index !== -1) {
        handlers.splice(index, 1);
        this.messageHandlers.set(destination, handlers);
      }
    } else {
      // 移除所有回调处理函数
      this.messageHandlers.delete(destination);
    }
  }

  sendMessage(destination: string, message: CodeMessage): Promise<void> {
    return new Promise(async (resolve, reject) => {
      if (!this.connected) {
        const connected = await this.connect();
        if (!connected) {
          reject('无法连接到WebSocket服务器');
          return;
        }
      }

      this.stompClient.send(`/app/${destination}`, {}, JSON.stringify(message));
      resolve();
    });
  }
}

export default new WebSocketService();
