<template>
  <div class="meeting-board">
    <h1>Course Meeting Board</h1>

    <div class="meeting-block">
      <div class="block-header">
        <h3>加入会议</h3>
      </div>
      <div class="block-body">
        <input v-model="meetingCode" placeholder="请输入会议号" />
        <button @click="joinMeeting">加入</button>
      </div>
    </div>

    <div class="meeting-block">
      <div class="block-header">
        <h3>创建会议</h3>
      </div>
      <div class="block-body">
        <input v-model="newMeetingName" placeholder="请输入会议名称" />
        <button @click="createMeeting">创建</button>
      </div>
    </div>

    <div class="meeting-block" v-if="conferenceList.length">
      <div class="block-header">
        <h3>当前会议列表</h3>
      </div>
      <div class="block-body">
        <ul>
          <li v-for="conf in conferenceList" :key="conf.conference_id">
            {{ conf.conference_name }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import config from '../config'

// 用户信息
const currentUser = ref(sessionStorage.getItem('currentUser') || '')

// 输入状态
const meetingCode = ref('')
const newMeetingName = ref('')
const joinedMeetingCode = ref('')

// 会议列表类型定义与状态
interface Conference {
  conference_id: string
  conference_name: string
}
const conferenceList = ref<Conference[]>([])

// 加入会议逻辑
const joinMeeting = async () => {
  if (!meetingCode.value.trim()) {
    alert('请输入会议号')
    return
  }

  try {
    const res = await axios.post(`${config.meetingUrl}/join`, {
      username: currentUser.value,
      conference_name: meetingCode.value,
    })

    if (res.data.status === 'joining') {
      joinedMeetingCode.value = meetingCode.value
      alert('已启动加入会议程序')
    } else {
      alert('加入失败')
    }
  } catch (err) {
    alert('加入会议失败，请检查网络或会议号')
  }
}

// 创建会议逻辑
const createMeeting = async () => {
  if (!newMeetingName.value.trim()) {
    alert('请输入会议名称')
    return
  }

  try {
    const res = await axios.post(`${config.meetingUrl}/create`, {
      username: currentUser.value,
      conference_name: newMeetingName.value,
    })

    if (res.data.status === 'creating') {
      alert('会议创建成功，正在启动...')
    } else {
      alert('创建失败')
    }
  } catch (err) {
    alert('创建会议失败，请检查网络或名称')
  }
}

// 获取会议列表
const loadConferences = async () => {
  try {
    const res = await axios.get(`${config.meetingUrl}/conferences`)
    conferenceList.value = res.data
  } catch (err) {
    console.error('加载会议列表失败')
  }
}

// 初始化加载
onMounted(() => {
  loadConferences()
})
</script>

<style scoped>
.meeting-board {
  padding: 2rem;
  background: #f5f7fa;
}

h1 {
  margin-bottom: 1.5rem;
  font-size: 2rem;
  color: #2c3e50;
}

.meeting-block {
  background: white;
  margin-bottom: 1rem;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.block-header {
  padding: 1rem;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.block-header h3 {
  margin: 0;
  color: #34495e;
}

.block-body {
  padding: 1rem;
}

input {
  width: 70%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  margin-right: 10px;
}

button {
  padding: 0.4rem 1rem;
  background: #409eff;
  border: none;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

button:hover {
  background-color: #318ee8;
}

ul {
  list-style-type: disc;
  padding-left: 20px;
}
</style>
