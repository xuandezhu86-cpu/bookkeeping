import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/auth/Login.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/auth/Register.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      component: () => import('../layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/dashboard/Index.vue'),
        },
        {
          path: 'records',
          name: 'Records',
          component: () => import('../views/records/Index.vue'),
        },
        {
          path: 'categories',
          name: 'Categories',
          component: () => import('../views/categories/Index.vue'),
        },
        {
          path: 'reports',
          name: 'Reports',
          component: () => import('../views/reports/Index.vue'),
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/profile/Index.vue'),
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('../views/settings/Index.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth !== false && !auth.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
