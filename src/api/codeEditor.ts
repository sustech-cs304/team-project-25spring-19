import axios from 'axios'

interface CodeSnippet {
  code: string
  language: string
}

// Default API URL, configurable via environment variables
const apiUrl = 'http://localhost:8080/api'

export async function runCodeAPI(codeSnippet: CodeSnippet): Promise<string> {
  try {
    const response = await axios.post(`${apiUrl}/codes/run`, codeSnippet, {
      headers: {
        'Content-Type': 'application/json',
        // Example: Add authentication if needed
        // 'Authorization': `Bearer ${someToken}`,
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
