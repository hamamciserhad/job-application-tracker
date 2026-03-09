<template>
  <div class="space-y-6">
    <h1 class="text-xl font-semibold text-gray-900">Dashboard</h1>

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

    <template v-else>
      <!-- Stat cards -->
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Total</p>
          <p class="mt-2 text-3xl font-bold text-gray-900">{{ overview.total }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Applied</p>
          <p class="mt-2 text-3xl font-bold text-blue-600">{{ overview.byStatus['APPLIED'] ?? 0 }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">In Progress</p>
          <p class="mt-2 text-3xl font-bold text-amber-500">{{ inProgress }}</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Offers</p>
          <p class="mt-2 text-3xl font-bold text-green-600">{{ offers }}</p>
        </div>
      </div>

      <!-- Charts 2x2 grid -->
      <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <!-- Doughnut: status breakdown -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h2 class="text-sm font-semibold text-gray-900 mb-4">Status Breakdown</h2>
          <div class="relative h-64">
            <Doughnut :data="doughnutData" :options="doughnutOptions" />
          </div>
        </div>

        <!-- Line: applications over time -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h2 class="text-sm font-semibold text-gray-900 mb-4">Applications Over Time</h2>
          <div class="relative h-64">
            <Line :data="lineData" :options="lineOptions" />
          </div>
        </div>

        <!-- Bar: by status -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h2 class="text-sm font-semibold text-gray-900 mb-4">Applications by Status</h2>
          <div class="relative h-64">
            <Bar :data="barData" :options="barOptions" />
          </div>
        </div>

        <!-- Funnel: conversion stages (horizontal bar) -->
        <div class="bg-white rounded-lg border border-gray-200 p-5">
          <h2 class="text-sm font-semibold text-gray-900 mb-4">Conversion Funnel</h2>
          <div class="relative h-64">
            <Bar :data="funnelData" :options="funnelOptions" />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  LineElement,
  BarElement,
  CategoryScale,
  LinearScale,
  PointElement,
  Filler
} from 'chart.js'
import { Doughnut, Line, Bar } from 'vue-chartjs'
import api from '../api/axios'

ChartJS.register(
  Title,
  Tooltip,
  Legend,
  ArcElement,
  LineElement,
  BarElement,
  CategoryScale,
  LinearScale,
  PointElement,
  Filler
)

interface OverviewData {
  total: number
  byStatus: Record<string, number>
}

interface TimelineData {
  labels: string[]
  values: number[]
}

interface ConversionEntry {
  stage: string
  count: number
}

const loading = ref(true)
const error = ref<string | null>(null)
const overview = ref<OverviewData>({ total: 0, byStatus: {} })
const timeline = ref<TimelineData>({ labels: [], values: [] })
const conversion = ref<ConversionEntry[]>([])

const STATUS_LABELS: Record<string, string> = {
  APPLIED: 'Applied',
  PHONE_SCREEN: 'Phone Screen',
  INTERVIEW: 'Interview',
  TECHNICAL_TEST: 'Technical Test',
  OFFER: 'Offer',
  ACCEPTED: 'Accepted',
  REJECTED: 'Rejected',
  WITHDRAWN: 'Withdrawn'
}

const STATUS_COLORS: Record<string, string> = {
  APPLIED: '#3B82F6',
  PHONE_SCREEN: '#8B5CF6',
  INTERVIEW: '#F59E0B',
  TECHNICAL_TEST: '#F97316',
  OFFER: '#22C55E',
  ACCEPTED: '#10B981',
  REJECTED: '#EF4444',
  WITHDRAWN: '#9CA3AF'
}

const inProgress = computed(() => {
  const s = overview.value.byStatus
  return (s['PHONE_SCREEN'] ?? 0) + (s['INTERVIEW'] ?? 0) + (s['TECHNICAL_TEST'] ?? 0)
})

const offers = computed(() => {
  const s = overview.value.byStatus
  return (s['OFFER'] ?? 0) + (s['ACCEPTED'] ?? 0)
})

const doughnutData = computed(() => {
  const entries = Object.entries(overview.value.byStatus).filter(([, v]) => v > 0)
  return {
    labels: entries.map(([k]) => STATUS_LABELS[k] ?? k),
    datasets: [{
      data: entries.map(([, v]) => v),
      backgroundColor: entries.map(([k]) => STATUS_COLORS[k] ?? '#9CA3AF'),
      borderWidth: 2,
      borderColor: '#ffffff'
    }]
  }
})

const lineData = computed(() => ({
  labels: timeline.value.labels,
  datasets: [{
    label: 'Applications',
    data: timeline.value.values,
    borderColor: '#6366F1',
    backgroundColor: 'rgba(99,102,241,0.1)',
    fill: true,
    tension: 0.4,
    pointRadius: 4,
    pointBackgroundColor: '#6366F1'
  }]
}))

const barData = computed(() => {
  const entries = Object.entries(overview.value.byStatus)
  return {
    labels: entries.map(([k]) => STATUS_LABELS[k] ?? k),
    datasets: [{
      label: 'Count',
      data: entries.map(([, v]) => v),
      backgroundColor: entries.map(([k]) => STATUS_COLORS[k] ?? '#9CA3AF'),
      borderRadius: 4
    }]
  }
})

const funnelData = computed(() => ({
  labels: conversion.value.map(e => STATUS_LABELS[e.stage] ?? e.stage),
  datasets: [{
    label: 'Count',
    data: conversion.value.map(e => e.count),
    backgroundColor: conversion.value.map(e => STATUS_COLORS[e.stage] ?? '#6366F1'),
    borderRadius: 4
  }]
}))

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom' as const,
      labels: { boxWidth: 12, font: { size: 11 } }
    }
  }
}

const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false }
  },
  scales: {
    y: { beginAtZero: true, ticks: { precision: 0 } }
  }
}

const barOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false }
  },
  scales: {
    y: { beginAtZero: true, ticks: { precision: 0 } }
  }
}

const funnelOptions = {
  indexAxis: 'y' as const,
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false }
  },
  scales: {
    x: { beginAtZero: true, ticks: { precision: 0 } }
  }
}

const fetchAll = async () => {
  loading.value = true
  error.value = null
  try {
    const [ovRes, tlRes, cvRes] = await Promise.all([
      api.get('/api/stats/overview'),
      api.get('/api/stats/timeline'),
      api.get('/api/stats/conversion')
    ])
    overview.value = ovRes.data.data
    timeline.value = tlRes.data.data
    conversion.value = cvRes.data.data
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    error.value = e?.response?.data?.message ?? 'Failed to load stats'
  } finally {
    loading.value = false
  }
}

onMounted(fetchAll)
</script>
