import axios from 'axios'

const apiUrl = 'http://localhost:8080/api/codes'

export async function runCode(code: string, language: string): Promise<string> {
  try {
    const response = await axios.post(`${apiUrl}/run`, {
      code,
      language,
    })
    return response.data
  } catch (error) {
    console.error('Error running code:', error)
    throw error
  }
}
