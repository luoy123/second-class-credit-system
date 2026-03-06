import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'
import AppLayout from '../layouts/AppLayout.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import StudentsView from '../views/StudentsView.vue'
import ActivitiesView from '../views/ActivitiesView.vue'
import CreditApplyView from '../views/CreditApplyView.vue'
import ReviewCenterView from '../views/ReviewCenterView.vue'
import RecordsView from '../views/RecordsView.vue'
import AnalyticsView from '../views/AnalyticsView.vue'
import ReviewLogsView from '../views/ReviewLogsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true, title: '登录' },
    },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { title: '概览' } },
        { path: 'students', name: 'students', component: StudentsView, meta: { title: '学生管理' } },
        { path: 'activities', name: 'activities', component: ActivitiesView, meta: { title: '活动管理' } },
        { path: 'apply', name: 'apply', component: CreditApplyView, meta: { title: '学分申请' } },
        { path: 'review', name: 'review', component: ReviewCenterView, meta: { title: '审批中心' } },
        { path: 'records', name: 'records', component: RecordsView, meta: { title: '学分记录' } },
        { path: 'analytics', name: 'analytics', component: AnalyticsView, meta: { title: '统计分析' } },
        { path: 'review-logs', name: 'review-logs', component: ReviewLogsView, meta: { title: '审批日志' } },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const token = getToken()
  if (to.meta.public) {
    if (token && to.path === '/login') {
      return '/dashboard'
    }
    return true
  }
  if (!token) {
    return '/login'
  }
  return true
})

export default router
