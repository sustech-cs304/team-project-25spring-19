```vue
<template>
  <div class="course-dashboard">
    <h1>{{ courseTitle }} Dashboard</h1>

    <!-- 可用课程区域 -->
    <div class="dashboard-section">
      <h2>Available Courses</h2>
      <div v-if="!courses.length" class="no-courses">No courses available</div>
      <div v-else class="course-list">
        <div v-for="course in courses" :key="course.id" class="course-card">
          <div class="course-header" @click="toggleCourse(course.id)">
            <h3>{{ course.title }}</h3>
            <span class="toggle-icon">
              {{ course.isExpanded ? '▼' : '▶' }}
            </span>
          </div>

          <div v-show="course.isExpanded" class="lecture-list">
            <div v-for="lecture in course.lectures" :key="lecture.id" class="lecture-item">
              <span class="lecture-status">
                {{ lecture.completed ? '✓' : '◯' }}
              </span>
              <span class="lecture-title" @click="goToCourseDetail(course.id, lecture.id)">
                {{ lecture.title }}
              </span>
              <span class="lecture-progress">
                {{ getProgressState(lecture.progressState) }}
              </span>
              <button class="test-button" @click.stop="goToTestPage(course.id, lecture.id)">
                Take Test
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程进度区域 -->
    <div class="dashboard-section">
      <h2>Course Progress</h2>
      <div class="progress-container">
        <div v-for="course in courses" :key="course.id" class="progress-card">
          <div class="progress-header" @click="toggleProgress(course.id)">
            <div class="progress-title">
              {{ course.title }}
              <span class="progress-percent">
                ({{ completedLectures(course) }}/{{ course.lectures.length }})
              </span>
            </div>
            <span class="toggle-icon">
              {{ course.progressExpanded ? '▼' : '▶' }}
            </span>
          </div>

          <div v-show="course.progressExpanded" class="progress-details">
            <div
              v-for="lecture in course.lectures"
              :key="lecture.id"
              class="progress-item"
              @click="goToCourseDetail(course.id, lecture.id)"
            >
              <span
                :class="['status-indicator', lecture.completed ? 'completed' : 'pending']"
              ></span>
              <span class="lecture-title">{{ lecture.title }}</span>
              <span class="lecture-progress">{{ getProgressState(lecture.progressState) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

interface Lecture {
  id: number;
  title: string;
  completed: boolean;
  progressState: string;
}

interface Course {
  id: number;
  title: string;
  description: string;
  lectures: Lecture[];
  isExpanded: boolean;
  progressExpanded: boolean;
}

interface CourseProgressDTO {
  progressId: number;
  userId: number;
  courseId: number;
  lectureId: number;
  slideId: number;
  state: string;
  finished: number;
  started: number;
  notStart: number;
}

export default defineComponent({
  name: 'CourseDashboard',
  props: {
    processId: {
      type: Number,
      required: true,
    },
  },
  setup(props) {
    const router = useRouter();
    const courseTitle = ref('Course Dashboard');
    const courses = ref<Course[]>([]);
    const userId = ref(sessionStorage.getItem('userId') || '1');

    // Fetch progress for a specific lecture
    const fetchProgress = async (courseId: number, lectureId: number): Promise<string> => {
      try {
        if (!userId.value) {
          throw new Error('未找到用户 ID');
        }

        console.log(`正在为用户 ${userId.value} 获取所有课程进度`);
        const response = await axios.get(
          `http://10.13.189.15:8080/api/process/${userId.value}/getByUser`,
          {
            // headers: {
            //   'Content-Type': 'application/json',
            //   Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
            // },
          },
        );

        // API 返回 List<CourseProgressDTO>
        const progressList: CourseProgressDTO[] = response.data;
        if (Array.isArray(progressList) && progressList.length > 0) {
          // 查找匹配的 courseId 和 lectureId 的记录
          const progress = progressList.find(
            (p) => p.courseId === courseId && p.lectureId === lectureId,
          );
          return progress ? progress.state : '未完成';
        }
        return '未完成';
      } catch (error) {
        console.error(`获取课程 ${courseId} 讲座 ${lectureId} 进度失败:`, error);
        // 后备到 sessionStorage
        const progressKey = `course_progress_${courseId}_${lectureId}`;
        return sessionStorage.getItem(progressKey) || '未完成';
      }
    };

    // Fetch all courses and their lectures
    const fetchCourses = async () => {
      try {
        // const accessToken = sessionStorage.getItem('access_token');
        // if (!accessToken) {
        //   throw new Error('No access token found. Please log in.');
        // }

        // Fetch all courses
        const courseResponse = await axios.get(
          'http://10.13.189.15:8080/api/courses/getAllCourse',
          {
            // headers: {
            //   'Content-Type': 'application/json',
            //   Authorization: `Bearer ${accessToken}`,
            // },
          },
        );

        // Map courses and fetch lectures and progress for each
        const fetchedCourses: Course[] = await Promise.all(
          courseResponse.data.map(async (course: any) => {
            // Fetch lectures for this course
            const lectureResponse = await axios.get(
              `http://10.13.189.15:8080/api/lectures/${course.courseId}/getByCourse`,
              {
                // headers: {
                //   'Content-Type': 'application/json',
                //   Authorization: `Bearer ${accessToken}`,
                // },
              },
            );

            // Map lectures and fetch progress
            const lectures: Lecture[] = await Promise.all(
              lectureResponse.data.map(async (lecture: any) => {
                const progressState = await fetchProgress(course.courseId, lecture.lectureId);
                return {
                  id: lecture.lectureId,
                  title: lecture.title,
                  completed: progressState === '已完成',
                  progressState,
                };
              }),
            );

            // Store course data in sessionStorage for CourseDetail and CourseTest
            sessionStorage.setItem(
              `course_${course.courseId}`,
              JSON.stringify({
                title: course.title,
                lectures: lectures.map((l) => ({ id: l.id, title: l.title })),
              }),
            );

            return {
              id: course.courseId,
              title: course.title,
              description: course.description,
              lectures,
              isExpanded: false,
              progressExpanded: false,
            };
          }),
        );

        courses.value = fetchedCourses;
      } catch (error) {
        console.error('Error fetching courses or lectures:', error);
        courses.value = [];
      }
    };

    // Get display text for progress state
    const getProgressState = (state: string): string => {
      return state || '未完成';
    };

    // Toggle course expansion
    const toggleCourse = (courseId: number) => {
      courses.value = courses.value.map((course) =>
        course.id === courseId ? { ...course, isExpanded: !course.isExpanded } : course,
      );
    };

    // Toggle progress expansion
    const toggleProgress = (courseId: number) => {
      courses.value = courses.value.map((course) =>
        course.id === courseId ? { ...course, progressExpanded: !course.progressExpanded } : course,
      );
    };

    // Compute completed lectures
    const completedLectures = computed(() => (course: Course) => {
      return course.lectures.filter((l) => l.completed).length;
    });

    // Navigate to course detail with lectureId
    const goToCourseDetail = (courseId: number, lectureId: number) => {
      sessionStorage.setItem('currentLectureId', lectureId.toString());
      router.push({
        name: 'CourseDetail',
        params: { id: courseId.toString() },
        query: { lectureId: lectureId.toString() },
      });
    };

    // Navigate to test page with lectureId
    const goToTestPage = (courseId: number, lectureId: number) => {
      sessionStorage.setItem('currentLectureId', lectureId.toString());
      sessionStorage.setItem('currentCourseId', courseId.toString());
      console.log(`Navigating to test page for course ${courseId}, lecture ${lectureId}`);
      router.push({
        name: 'CourseTest',
        params: { id: courseId.toString() },
        query: { lectureId: lectureId.toString() },
      });
    };

    // Fetch courses on mount
    onMounted(() => {
      if (!userId.value) {
        console.error('未找到用户 ID，请登录');
        router.push({ name: 'Login' });
        return;
      }
      fetchCourses();
    });

    return {
      courseTitle,
      courses,
      toggleCourse,
      toggleProgress,
      completedLectures,
      goToCourseDetail,
      goToTestPage,
      getProgressState,
    };
  },
});
</script>

<style scoped>
.course-dashboard {
  width: 100%;
  height: 100%;
  padding: 2rem;
  box-sizing: border-box;
  background: #f5f7fa;
}

h1 {
  color: #2c3e50;
  margin-bottom: 2rem;
  font-size: 2rem;
}

.dashboard-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  padding: 1.5rem;
}

h2 {
  color: #34495e;
  margin-bottom: 1rem;
  font-size: 1.25rem;
}

.course-card,
.progress-card {
  border: 1px solid #eaecef;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.course-header,
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  cursor: pointer;
  transition: background 0.2s;
}

.course-header:hover,
.progress-header:hover {
  background: #f1f3f5;
}

.toggle-icon {
  color: #6c757d;
}

.lecture-list {
  padding: 0.5rem 1rem;
}

.lecture-item {
  display: flex;
  align-items: center;
  padding: 0.75rem;
  border-bottom: 1px solid #eee;
  transition: background 0.2s;
}

.lecture-item:hover {
  background: #f8f9fa;
}

.lecture-item:last-child {
  border-bottom: none;
}

.lecture-status {
  margin-right: 1rem;
  font-size: 1.2em;
}

.lecture-title {
  flex-grow: 1;
  cursor: pointer;
}

.lecture-progress {
  margin-right: 1rem;
  color: #666;
  font-size: 0.9em;
}

.test-button {
  padding: 0.5rem 1rem;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
  margin-left: 1rem;
}

.test-button:hover {
  background: #45a049;
}

.no-courses {
  padding: 1rem;
  text-align: center;
  color: #666;
}

.progress-container {
  max-height: 600px;
  overflow-y: auto;
}

.progress-title {
  flex-grow: 1;
}

.progress-percent {
  color: #666;
  font-size: 0.9em;
  margin-left: 1rem;
}

.progress-details {
  padding: 1rem;
  background: #f8fafc;
}

.progress-item {
  display: flex;
  align-items: center;
  padding: 0.75rem;
  background: white;
  margin-bottom: 0.5rem;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: background 0.2s;
}

.progress-item:hover {
  background: #f8f9fa;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 1rem;
}

.status-indicator.completed {
  background: #4caf50;
}

.status-indicator.pending {
  background: #ff9800;
}

.lecture-title {
  flex-grow: 1;
}
</style>