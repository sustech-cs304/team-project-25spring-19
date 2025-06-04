import axios from 'axios'

export interface LectureSlideDTO {
  slideId: number
  lectureId: number
  extractedText: string
  url: string
  content: string
}

/**
 * Fetches lecture slide content by slideId from the backend API.
 * @param slideId The ID of the slide to fetch
 * @returns Promise resolving to LectureSlideDTO
 * @throws Error if the request fails or response is invalid
 */
export async function getLectureSlideById(slideId: number): Promise<LectureSlideDTO> {
  try {
    // const accessToken = sessionStorage.getItem('access_token')
    // if (!accessToken) {
    //   throw new Error('No access token found. Please log in.')
    // }

    const response = await axios.get(`http://10.13.189.15:8080/api/slides/${slideId}/getById`, {
      headers: {
        'Content-Type': 'application/json',
        // Authorization: `Bearer ${accessToken}`,
      },
    })

    if (response.status !== 200) {
      throw new Error(`Failed to fetch slide: ${response.statusText}`)
    }

    const data: LectureSlideDTO = response.data
    // Validate response structure
    if (!data.slideId || !data.lectureId) {
      throw new Error('Invalid slide data received from server')
    }

    return data
  } catch (error) {
    throw error instanceof Error ? error : new Error('Unknown error occurred while fetching slide')
  }
}

/**
 * Fetches all lecture slides for a given lectureId from the backend API.
 * @param lectureId The ID of the lecture to fetch slides for
 * @returns Promise resolving to an array of LectureSlideDTO
 * @throws Error if the request fails or response is invalid
 */
export async function getLectureSlidesByLectureId(lectureId: number): Promise<LectureSlideDTO[]> {
  try {
    // const accessToken = sessionStorage.getItem('access_token')
    // if (!accessToken) {
    //   throw new Error('No access token found. Please log in.')
    // }

    const response = await axios.get(
      `http://10.13.189.15:8080/api/slides/${lectureId}/getByLecture`,
      {
        headers: {
          'Content-Type': 'application/json',
        //   Authorization: `Bearer ${accessToken}`,
        },
      },
    )

    if (response.status !== 200) {
      throw new Error(`Failed to fetch slides: ${response.statusText}`)
    }

    const data: LectureSlideDTO[] = response.data
    // Validate response structure
    if (!Array.isArray(data)) {
      throw new Error('Invalid slides data received from server')
    }

    return data
  } catch (error) {
    throw error instanceof Error ? error : new Error('Unknown error occurred while fetching slides')
  }
}
