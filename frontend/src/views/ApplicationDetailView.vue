<template>
  <div class="space-y-6">
    <!-- Back link -->
    <div>
      <RouterLink
        :to="{ name: 'applications' }"
        class="inline-flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-800"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        Back to Applications
      </RouterLink>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-16">
      <div class="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Error -->
    <div
      v-else-if="error"
      class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm"
    >
      {{ error }}
    </div>

    <template v-else-if="app">
      <!-- Detail card -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <div class="flex items-start justify-between gap-4 flex-wrap">
          <div>
            <h1 class="text-xl font-semibold text-gray-900">{{ app.companyName }}</h1>
            <p class="text-base text-gray-600 mt-0.5">{{ app.position }}</p>
          </div>
          <div class="flex items-center gap-3">
            <StatusBadge :status="app.status" />
            <button
              @click="showForm = true"
              class="px-3 py-1.5 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
            >
              Edit
            </button>
          </div>
        </div>

        <div class="mt-6 grid grid-cols-2 gap-x-8 gap-y-5 sm:grid-cols-3">
          <div>
            <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Applied Date</p>
            <p class="mt-1 text-sm text-gray-900">{{ formatDate(app.appliedDate) }}</p>
          </div>
          <div>
            <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Location</p>
            <p class="mt-1 text-sm text-gray-900">{{ app.location ?? '\u2014' }}</p>
          </div>
          <div>
            <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Salary</p>
            <p class="mt-1 text-sm text-gray-900">{{ formatSalary(app.salary) }}</p>
          </div>
          <div class="col-span-2 sm:col-span-2">
            <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Job URL</p>
            <p class="mt-1 text-sm break-all">
              <a
                v-if="app.jobUrl"
                :href="app.jobUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="text-indigo-600 hover:text-indigo-800"
              >{{ app.jobUrl }}</a>
              <span v-else class="text-gray-900">\u2014</span>
            </p>
          </div>
          <div>
            <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Added</p>
            <p class="mt-1 text-sm text-gray-900">{{ formatDate(app.createdAt) }}</p>
          </div>
        </div>

        <div v-if="app.notes" class="mt-5 border-t border-gray-100 pt-4">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Notes</p>
          <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ app.notes }}</p>
        </div>
      </div>

      <!-- Status history timeline -->
      <div class="bg-white rounded-lg border border-gray-200 p-6">
        <h2 class="text-sm font-semibold text-gray-900 mb-5">Status History</h2>
        <p v-if="history.length === 0" class="text-sm text-gray-400">No status changes recorded yet.</p>
        <ol v-else class="relative border-l border-gray-200 space-y-6 ml-3">
          <li v-for="entry in history" :key="entry.id" class="ml-6">
            <span
              class="absolute -left-2.5 mt-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-indigo-100 ring-4 ring-white"
            >
              <svg class="w-2.5 h-2.5 text-indigo-600" fill="currentColor" viewBox="0 0 8 8">
                <circle cx="4" cy="4" r="3" />
              </svg>
            </span>
            <div>
              <div class="flex flex-wrap items-center gap-1.5 text-sm text-gray-700">
                <template v-if="entry.oldStatus">
                  <StatusBadge :status="entry.oldStatus" />
                  <span class="text-gray-400">&rarr;</span>
                  <StatusBadge :status="entry.newStatus" />
                </template>
                <template v-else>
                  <span class="text-gray-500">Started as</span>
                  <StatusBadge :status="entry.newStatus" />
                </template>
              </div>
              <p class="mt-1 text-xs text-gray-400">{{ formatDateTime(entry.changedAt) }}</p>
              <p v-if="entry.notes" class="mt-0.5 text-xs text-gray-500 italic">{{ entry.notes }}</p>
            </div>
          </li>
        </ol>
      </div>
    </template>

    <!-- Edit modal -->
    <ApplicationForm
      v-if="showForm && app"
      :application="app"
      @close="showForm = false"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import api from '../api/axios'
import type { Application } from '../stores/applicationStore'
import StatusBadge from '../components/StatusBadge.vue'
import ApplicationForm from '../components/ApplicationForm.vue'

interface StatusHistoryEntry {
  id: number
  oldStatus: string | null
  newStatus: string
  changedAt: string
  notes: string | null
}

const route = useRoute()
const loading = ref(true)
const error = ref<string | null>(null)
const app = ref<Application | null>(null)
const history = ref<StatusHistoryEntry[]>([])
const showForm = ref(false)

const fetchData = async () => {
  loading.value = true
  error.value = null
  try {
    const id = route.params.id
    const [appRes, historyRes] = await Promise.all([
      api.get(`/api/applications/${id}`),
      api.get(`/api/applications/${id}/history`)
    ])
    app.value = appRes.data.data
    history.value = historyRes.data.data
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    error.value = e?.response?.data?.message ?? 'Failed to load application'
  } finally {
    loading.value = false
  }
}

const onSaved = async () => {
  showForm.value = false
  await fetchData()
}

const formatDate = (date: string | null) => {
  if (!date) return '\u2014'
  return new Date(date).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
}

const formatDateTime = (dt: string) => {
  return new Date(dt).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatSalary = (salary: number | null) => {
  if (salary === null || salary === undefined) return '\u2014'
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(salary)
}

onMounted(fetchData)
</script>
