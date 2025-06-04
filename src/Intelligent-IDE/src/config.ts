import axios from 'axios'

// 默认配置（开发环境）
const defaultConfig = {
  // 后端API服务器地址'http://10.13.189.15:8080/api'
  apiBaseUrl: 'http://10.13.189.15:8080/api',

  // 会议服务器地址
  meetingUrl: 'http://127.0.0.1:5001',
}

// 存储实际配置
let config = { ...defaultConfig }

// 动态加载配置
export const loadConfig = async () => {
  try {
    // 尝试从后端加载配置
    const response = await axios.get(`${config.apiBaseUrl}/config`)
    if (response.data) {
      // 使用后端返回的配置更新本地配置
      config.apiBaseUrl = response.data.apiBaseUrl
      console.log('配置已从后端加载', config)
    }
  } catch (error) {
    console.warn('无法从后端加载配置，使用默认配置', error)
  }
  return config
}

// 导出配置
export default config
