import axios from 'axios'

const apiUrl = 'http://127.0.0.1:8000/ai/ai/ask'

export const sendPromptToAI = async (question: string) => {
  try {
    const response = await axios.post(
      apiUrl,
      { question }, // 修正请求体为 JSON 对象
      {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('access_token')}`, // 从本地存储获取 token
        },
      },
    )
    return response.data
  } catch (error) {
    const axiosError = error as any
    console.error('调用异常：', axiosError.message)
    throw new Error(axiosError.response?.data?.detail || '提问失败')
  }
}
