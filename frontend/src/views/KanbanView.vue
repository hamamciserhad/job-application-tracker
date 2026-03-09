<template>
  <div class="space-y-4 min-w-0 w-full">
    <h1 class="text-xl font-semibold text-gray-900">Kanban Board</h1>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-16">
      <div class="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Error banner -->
    <div
      v-if="error"
      class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm"
    >
      {{ error }}
    </div>

    <!-- Board -->
    <div v-if="!loading" class="pb-4" style="display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px;">
      <div
        v-for="col in COLUMNS"
        :key="col.status"
        class="min-w-0"
      >
        <!-- Column header -->
        <div class="flex items-center justify-between mb-2 px-1">
          <span :class="['text-xs font-semibold uppercase tracking-wide', col.textColor]">
            {{ col.label }}
          </span>
          <span class="text-xs font-medium text-gray-400 bg-gray-100 rounded-full px-2 py-0.5">
            {{ groupedApplications[col.status]?.length ?? 0 }}
          </span>
        </div>

        <!-- Drop zone -->
        <div
          :class="[
            'min-h-48 rounded-lg border-2 border-dashed p-2 space-y-2 transition-colors',
            dragOverColumn === col.status
              ? 'border-indigo-400 bg-indigo-50'
              : 'border-gray-200 bg-gray-50'
          ]"
          @dragover.prevent="onDragOver(col.status)"
          @dragleave.self="onDragLeave"
          @drop.prevent="onDrop(col.status)"
        >
          <!-- Cards -->
          <div
            v-for="app in groupedApplications[col.status] ?? []"
            :key="app.id"
            draggable="true"
            :class="[
              'bg-white rounded-md border p-3 cursor-grab active:cursor-grabbing shadow-sm hover:shadow-md transition-all select-none',
              draggingId === app.id ? 'opacity-40 border-indigo-300' : 'border-gray-200'
            ]"
            @dragstart="onDragStart(app)"
            @dragend="onDragEnd"
          >
            <p class="text-sm font-semibold text-gray-900 truncate">{{ app.companyName }}</p>
            <p class="text-xs text-gray-500 truncate mt-0.5">{{ app.position }}</p>
            <span v-if="app.location" class="text-xs text-gray-400 truncate block mt-1">
              {{ app.location }}
            </span>
            <p v-if="app.appliedDate" class="text-xs text-gray-400 mt-1">
              {{ formatDate(app.appliedDate) }}
            </p>
          </div>

          <!-- Empty placeholder -->
          <div
            v-if="!groupedApplications[col.status]?.length"
            class="flex items-center justify-center py-6"
          >
            <p class="text-xs text-gray-400">Drop here</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '../api/axios'
import type { Application } from '../stores/applicationStore'

const COLUMNS = [
  { status: 'APPLIED', label: 'Applied', textColor: 'text-blue-600' },
  { status: 'PHONE_SCREEN', label: 'Phone Screen', textColor: 'text-purple-600' },
  { status: 'INTERVIEW', label: 'Interview', textColor: 'text-amber-600' },
  { status: 'TECHNICAL_TEST', label: 'Tech Test', textColor: 'text-orange-600' },
  { status: 'OFFER', label: 'Offer', textColor: 'text-green-600' },
  { status: 'ACCEPTED', label: 'Accepted', textColor: 'text-emerald-600' },
  { status: 'REJECTED', label: 'Rejected', textColor: 'text-red-600' },
  { status: 'WITHDRAWN', label: 'Withdrawn', textColor: 'text-gray-500' },
]

const loading = ref(true)
const error = ref<string | null>(null)
const applications = ref<Application[]>([])
const draggedApp = ref<Application | null>(null)
const draggingId = ref<number | null>(null)
const dragOverColumn = ref<string | null>(null)

const groupedApplications = computed(() => {
  const groups: Record<string, Application[]> = {}
  for (const col of COLUMNS) groups[col.status] = []
  for (const app of applications.value) {
    if (groups[app.status]) {
      groups[app.status].push(app)
    }
  }
  return groups
})

const fetchAll = async () => {
  loading.value = true
  error.value = null
  try {
    const { data } = await api.get('/api/applications', {
      params: { page: 0, size: 500, sort: 'createdAt,desc' }
    })
    applications.value = data.content
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    error.value = e?.response?.data?.message ?? 'Failed to load applications'
  } finally {
    loading.value = false
  }
}

const onDragStart = (app: Application) => {
  draggedApp.value = app
  draggingId.value = app.id
}

const onDragEnd = () => {
  draggingId.value = null
  dragOverColumn.value = null
}

const onDragOver = (status: string) => {
  dragOverColumn.value = status
}

const onDragLeave = () => {
  dragOverColumn.value = null
}

const onDrop = async (newStatus: string) => {
  dragOverColumn.value = null
  const app = draggedApp.value
  draggedApp.value = null
  draggingId.value = null

  if (!app || app.status === newStatus) return

  const oldStatus = app.status

  // Optimistic update — move card immediately
  const target = applications.value.find(a => a.id === app.id)
  if (target) target.status = newStatus

  try {
    await api.patch(`/api/applications/${app.id}/status`, { status: newStatus })
  } catch {
    // Rollback on error
    const t = applications.value.find(a => a.id === app.id)
    if (t) t.status = oldStatus
    error.value = 'Failed to update status — change rolled back.'
    setTimeout(() => { error.value = null }, 4000)
  }
}

const formatDate = (dateStr: string) =>
  new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })

onMounted(fetchAll)
</script>
