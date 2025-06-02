import axios from 'axios'
import config from '../config'

export interface NoteDTO {
  noteId?: number
  userId: number
  lectureId: number
  slideId: number
  content: string
}

const apiUrl = `${config.apiBaseUrl}/notes`

// 6.1 创建笔记
export async function createNote(
  userId: number,
  lectureId: number,
  slideId: number,
  note: { content: string },
): Promise<NoteDTO> {
  const response = await axios.post(`${apiUrl}/${userId}/${lectureId}/${slideId}/create`, note)
  return response.data
}

// 6.2 更新笔记
export async function updateNote(
  noteId: number,
  userId: number,
  lectureId: number,
  slideId: number,
  note: { content: string },
): Promise<NoteDTO> {
  const response = await axios.put(
    `${apiUrl}/${noteId}/${userId}/${lectureId}/${slideId}/update`,
    note,
  )
  return response.data
}

// 6.3 根据 ID 获取笔记
export async function getNoteById(noteId: number): Promise<NoteDTO> {
  const response = await axios.get(`${apiUrl}/${noteId}/getNoteById`)
  return response.data
}

// 6.4 获取某学生所有笔记
export async function getNotesByUser(userId: number): Promise<NoteDTO[]> {
  const response = await axios.get(`${apiUrl}/${userId}/getNotesByUser`)
  return response.data
}

// 6.5 获取某学生在某讲座所有笔记
export async function getNotesByUserLecture(userId: number, lectureId: number): Promise<NoteDTO[]> {
  const response = await axios.get(`${apiUrl}/${userId}/${lectureId}/getNotesByUserLecture`)
  return response.data
}

// 6.6 获取某学生在某讲座某课件所有笔记
export async function getNotesByUserLectureSlide(
  userId: number,
  lectureId: number,
  slideId: number,
): Promise<NoteDTO[]> {
  const response = await axios.get(
    `${apiUrl}/${userId}/${lectureId}/${slideId}/getNotesByUserLectureSlide`,
  )
  return response.data
}

// 6.7 删除笔记
export async function deleteNote(noteId: number): Promise<string> {
  const response = await axios.delete(`${apiUrl}/${noteId}/delete`)
  return response.data
}
