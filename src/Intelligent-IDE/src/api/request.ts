// src/api/request.ts
import axios from 'axios'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000, // 请求超时时间（10秒）
})

// 请求拦截器（比如自动加 token）
request.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器（统一处理错误提示等）
request.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    switch (status) {
      case 401:
        alert('未登录或登录已过期')
        break
      case 403:
        alert('没有权限')
        break
      case 500:
        alert('服务器错误')
        break
      default:
        alert('请求失败')
    }
    return Promise.reject(error)
  },
)

export default request
