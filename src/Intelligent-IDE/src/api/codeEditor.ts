import axios from 'axios'
import config from '../config'
/**
 * AI-generated-content
 * tool: chatgpt
 * version: latest
 * usage: ai根据后端队友的api文档生成api，自己完善测试
 */
interface CodeSnippet {
  code: string
  language: string
}

// Default API URL, configurable via environment variables
const apiUrl = `${config.apiBaseUrl}`

export async function runCodeAPI(codeSnippet: CodeSnippet): Promise<string> {
  try {
    const response = await axios.post(`${apiUrl}/codes/run`, codeSnippet, {
      headers: {
        'Content-Type': 'application/json',
      },
    })

    return response.data
  } catch (error: any) {
    console.error('Error running code:', error)
    // Enhanced error handling
    if (error.response) {
      throw new Error(`Server error: ${error.response.status} - ${error.response.data}`)
    } else if (error.request) {
      throw new Error('No response from server. Check your network.')
    } else {
      throw new Error('Request failed. Please try again.')
    }
  }
}
