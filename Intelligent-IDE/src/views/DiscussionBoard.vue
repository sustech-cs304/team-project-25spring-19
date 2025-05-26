<template>
  <div class="discussion-board">
    <h1>Course Discussion Board</h1>

    <div v-for="thread in threads" :key="thread.id" class="discussion-thread">
      <div class="thread-header" @click="toggleThread(thread.id)">
        <h3>{{ thread.title }}</h3>
        <span class="toggle-icon">{{ thread.expanded ? '▼' : '▶' }}</span>
      </div>

      <div v-show="thread.expanded" class="thread-comments">
        <div v-for="comment in thread.comments" :key="comment.id" class="comment-item">
          <div class="comment-author">{{ comment.author }}:</div>
          <div class="comment-text">
            {{ comment.text }}
          </div>
        </div>

        <!-- 发表评论 -->
        <div class="comment-input">
          <textarea v-model="newComments[thread.id]" placeholder="Add a comment..."></textarea>
          <button @click="postComment(thread.id)">Post</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'

interface Comment {
  id: number
  author: string
  text: string
}

interface Thread {
  id: number
  title: string
  expanded: boolean
  comments: Comment[]
}

export default defineComponent({
  name: 'DiscussionBoard',
  setup() {
    const userName = sessionStorage.getItem('currentUser') || 'Anonymous'
    const userId = sessionStorage.getItem('userId') || '0'

    const threads = ref<Thread[]>([
      {
        id: 1,
        title: 'Vue组件生命周期问题',
        expanded: false,
        comments: [
          { id: 1, author: 'Alice', text: 'created 和 mounted 有什么区别？' },
          {
            id: 2,
            author: 'Bob',
            text: 'created 是实例初始化完成但 DOM 未挂载，mounted 是挂载完成。',
          },
        ],
      },
      {
        id: 2,
        title: 'TypeScript 和 Vue 配合写法',
        expanded: false,
        comments: [{ id: 3, author: 'Charlie', text: 'setup 中如何定义响应式数组？' }],
      },
    ])

    const newComments = ref<{ [threadId: number]: string }>({})

    const toggleThread = (threadId: number) => {
      threads.value = threads.value.map((thread) =>
        thread.id === threadId ? { ...thread, expanded: !thread.expanded } : thread,
      )
    }

    const postComment = (threadId: number) => {
      const text = newComments.value[threadId]?.trim()
      if (!text) return

      const thread = threads.value.find((t) => t.id === threadId)
      if (thread) {
        thread.comments.push({
          id: Date.now(),
          author: userName,
          text,
        })
        newComments.value[threadId] = ''
      }
    }

    return {
      threads,
      toggleThread,
      newComments,
      postComment,
    }
  },
})
</script>

<style scoped>
.discussion-board {
  padding: 2rem;
  background: #f5f7fa;
}

h1 {
  margin-bottom: 1.5rem;
  font-size: 2rem;
  color: #2c3e50;
}

.discussion-thread {
  background: white;
  margin-bottom: 1rem;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.thread-header {
  padding: 1rem;
  background: #f8f9fa;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
}

.thread-comments {
  padding: 1rem;
}

.comment-item {
  margin-bottom: 0.75rem;
}

.comment-author {
  font-weight: bold;
  margin-bottom: 0.25rem;
}

.comment-text {
  padding-left: 0.5rem;
}

.comment-input {
  margin-top: 1rem;
}

.comment-input textarea {
  width: 100%;
  min-height: 60px;
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #ccc;
  resize: vertical;
}

.comment-input button {
  margin-top: 0.5rem;
  padding: 0.4rem 1rem;
  background: #409eff;
  border: none;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}
</style>
