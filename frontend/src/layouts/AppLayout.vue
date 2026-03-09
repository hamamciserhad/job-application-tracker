<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside class="w-60 bg-white border-r border-gray-200 flex flex-col">
      <div class="h-16 flex items-center px-6 border-b border-gray-200">
        <span class="text-xl font-bold text-indigo-600">JobTracker</span>
      </div>

      <nav class="flex-1 py-4 px-3 space-y-1">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          :class="[
            'block px-3 py-2 rounded-md text-sm font-medium transition-colors',
            isActive(item.to)
              ? 'bg-indigo-50 text-indigo-700'
              : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
          ]"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="py-4 px-3 border-t border-gray-200">
        <button
          @click="logout"
          class="w-full text-left px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:bg-red-50 hover:text-red-600 transition-colors"
        >
          Sign out
        </button>
      </div>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col min-h-0">
      <header class="h-16 bg-white border-b border-gray-200 flex items-center px-6">
        <h1 class="text-base font-semibold text-gray-800">{{ pageTitle }}</h1>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const navItems = [
  { label: 'Dashboard', to: '/' },
  { label: 'Applications', to: '/applications' },
  { label: 'Kanban Board', to: '/kanban' }
]

const isActive = (to: string) => {
  if (to === '/') return route.path === '/'
  return route.path.startsWith(to)
}

const pageTitle = computed(() => {
  const match = navItems.find((item) => isActive(item.to))
  return match?.label ?? 'Job Application Tracker'
})

const logout = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}
</script>
