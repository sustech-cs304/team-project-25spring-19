import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/Home.vue'
import CourseDashboard from '../views/CourseDashboard.vue'
import CourseDetail from '../views/CourseDetail.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/dashboard',
    name: 'CourseDashboard',
    component: CourseDashboard
  },
  {
    path: '/course/:id',
    name: 'CourseDetail',
    component: CourseDetail
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router