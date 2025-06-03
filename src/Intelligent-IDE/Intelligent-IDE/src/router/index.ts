import { createRouter, createWebHistory } from 'vue-router'

// 学生端页面
import CourseDashboard from '../views/CourseDashboard.vue'
import CourseDetail from '../views/CourseDetail.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'

// 教师端页面
import TeacherLogin from '../views/teacher/TeacherLogin.vue'
import TeacherHome from '../views/teacher/TeacherHome.vue'
import TeacherCourse from '../views/teacher/TeacherCourse.vue'
import AppTeacher from '../layouts/AppTeacher.vue' // ✅ 教师侧边布局
// 在 src/router/index.ts 中添加：
import TeacherRegister from '../views/teacher/TeacherRegister.vue'

const routes = [
  // 🧑‍🎓 学生端路由
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/home', name: 'Home', component: Home, meta: { requiresAuth: true, role: 'student' } },
  {
    path: '/dashboard',
    name: 'CourseDashboard',
    component: CourseDashboard,
    meta: { requiresAuth: true, role: 'student' },
  },
  {
    path: '/course/:id',
    name: 'CourseDetail',
    component: CourseDetail,
    meta: { requiresAuth: true, role: 'student' },
  },
  {
    path: '/discussion',
    name: 'DiscussionBoard',
    component: () => import('@/views/DiscussionBoard.vue'),
  },

  // 🧑‍🏫 教师登录页
  { path: '/teacher-login', name: 'TeacherLogin', component: TeacherLogin },
  { path: '/teacher-register', name: 'TeacherRegister', component: TeacherRegister },

  // 🧑‍🏫 教师后台布局 + 子页面
  {
    path: '/teacher',
    component: AppTeacher,
    meta: { requiresAuth: true, role: 'teacher' },
    children: [
      { path: 'home', name: 'TeacherHome', component: TeacherHome },
      { path: 'course', name: 'TeacherCourse', component: TeacherCourse },
    ],
  },

  // ✅ 默认重定向
  { path: '/', redirect: '/login' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// ✅ 路由守卫：身份和登录验证
router.beforeEach((to, from, next) => {
  const isLoggedIn = sessionStorage.getItem('loggedIn') === 'true'
  const userType = sessionStorage.getItem('userType') // 'student' or 'teacher'

  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.meta.role && to.meta.role !== userType) {
    // 如果角色不匹配，也跳转登录
    next('/login')
  } else {
    next()
  }
})

export default router
