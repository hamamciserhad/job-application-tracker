import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { guest: true }
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue')
        },
        {
          path: 'applications',
          name: 'applications',
          component: () => import('../views/ApplicationsView.vue')
        },
        {
          path: 'applications/:id',
          name: 'application-detail',
          component: () => import('../views/ApplicationDetailView.vue')
        },
        {
          path: 'kanban',
          name: 'kanban',
          component: () => import('../views/KanbanView.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const isAuthenticated = !!localStorage.getItem('accessToken')

  if (to.meta.requiresAuth && !isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.guest && isAuthenticated) {
    return { name: 'dashboard' }
  }
})

export default router
