<template>
  <div class="space-y-4">
    <!-- Action row -->
    <div class="flex justify-end">
      <button
        @click="openCreate"
        class="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 transition-colors"
      >
        + New Application
      </button>
    </div>

    <!-- Filter bar -->
    <div class="bg-white rounded-lg border border-gray-200 p-4 flex flex-wrap gap-3 items-center">
      <input
        v-model="store.filters.companyName"
        @keyup.enter="store.applyFilters"
        type="text"
        placeholder="Search by company..."
        class="flex-1 min-w-[200px] px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
      />
      <select
        v-model="store.filters.status"
        @change="store.applyFilters"
        class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
      >
        <option value="">All Statuses</option>
        <option v-for="s in statuses" :key="s.value" :value="s.value">{{ s.label }}</option>
      </select>
      <button
        @click="store.applyFilters"
        class="px-4 py-2 bg-indigo-600 text-white text-sm rounded-md hover:bg-indigo-700 transition-colors"
      >
        Search
      </button>
      <button
        v-if="store.filters.companyName || store.filters.status"
        @click="clearFilters"
        class="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
      >
        Clear
      </button>
    </div>

    <!-- Loading spinner -->
    <div v-if="store.loading" class="flex items-center justify-center py-16">
      <div class="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Error state -->
    <div
      v-else-if="store.error"
      class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm"
    >
      {{ store.error }}
    </div>

    <!-- Table card -->
    <div v-else class="bg-white rounded-lg border border-gray-200 overflow-hidden">
      <!-- Empty state -->
      <div
        v-if="store.applications.length === 0"
        class="flex flex-col items-center justify-center py-16 text-gray-400"
      >
        <svg class="w-12 h-12 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="1.5"
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
        <p class="text-sm font-medium text-gray-500">No applications found</p>
        <p class="text-xs mt-1 text-gray-400">
          {{
            store.filters.companyName || store.filters.status
              ? 'Try adjusting your filters.'
              : 'Add your first application to get started.'
          }}
        </p>
      </div>

      <!-- Data table -->
      <table v-else class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wide">Company</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wide">Position</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wide">Status</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wide">Location</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wide">Applied</th>
            <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wide">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr
            v-for="app in store.applications"
            :key="app.id"
            class="hover:bg-gray-50 transition-colors"
          >
            <td class="px-4 py-3 font-medium text-gray-900">{{ app.companyName }}</td>
            <td class="px-4 py-3 text-gray-600">{{ app.position }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="app.status" />
            </td>
            <td class="px-4 py-3 text-gray-500">{{ app.location ?? '\u2014' }}</td>
            <td class="px-4 py-3 text-gray-500">{{ formatDate(app.appliedDate) }}</td>
            <td class="px-4 py-3 text-right space-x-3">
              <RouterLink
                :to="{ name: 'application-detail', params: { id: app.id } }"
                class="text-indigo-600 hover:text-indigo-800 font-medium"
              >
                View
              </RouterLink>
              <button
                @click="openEdit(app)"
                class="text-gray-600 hover:text-gray-900 font-medium"
              >
                Edit
              </button>
              <button
                @click="confirmDelete(app)"
                class="text-red-500 hover:text-red-700 font-medium"
              >
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination footer -->
      <div
        v-if="store.applications.length > 0"
        class="px-4 py-3 border-t border-gray-200"
      >
        <Pagination
          :page="store.pagination.page"
          :total-pages="store.pagination.totalPages"
          :total-elements="store.pagination.totalElements"
          :size="store.pagination.size"
          @change="store.setPage"
        />
      </div>
    </div>

    <!-- Delete confirmation modal -->
    <div
      v-if="deleteTarget"
      class="fixed inset-0 bg-black/30 flex items-center justify-center z-50"
      @click.self="deleteTarget = null"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-sm mx-4 w-full">
        <h3 class="text-base font-semibold text-gray-900 mb-2">Delete application?</h3>
        <p class="text-sm text-gray-600 mb-5">
          Are you sure you want to delete the application for
          <strong>{{ deleteTarget.position }}</strong> at
          <strong>{{ deleteTarget.companyName }}</strong>? This cannot be undone.
        </p>
        <div class="flex justify-end gap-3">
          <button
            @click="deleteTarget = null"
            class="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button
            @click="handleDelete"
            :disabled="deleting"
            class="px-4 py-2 text-sm text-white bg-red-600 rounded-md hover:bg-red-700 disabled:opacity-50 transition-colors"
          >
            {{ deleting ? 'Deleting\u2026' : 'Delete' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Application form modal -->
    <ApplicationForm
      v-if="showForm"
      :application="editTarget"
      @close="closeForm"
      @saved="closeForm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useApplicationStore } from '../stores/applicationStore'
import type { Application } from '../stores/applicationStore'
import StatusBadge from '../components/StatusBadge.vue'
import Pagination from '../components/Pagination.vue'
import ApplicationForm from '../components/ApplicationForm.vue'

const store = useApplicationStore()
const deleteTarget = ref<Application | null>(null)
const deleting = ref(false)
const showForm = ref(false)
const editTarget = ref<Application | null>(null)

const statuses = [
  { value: 'APPLIED', label: 'Applied' },
  { value: 'PHONE_SCREEN', label: 'Phone Screen' },
  { value: 'INTERVIEW', label: 'Interview' },
  { value: 'TECHNICAL_TEST', label: 'Technical Test' },
  { value: 'OFFER', label: 'Offer' },
  { value: 'ACCEPTED', label: 'Accepted' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'WITHDRAWN', label: 'Withdrawn' }
]

const formatDate = (date: string | null) => {
  if (!date) return '\u2014'
  return new Date(date).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
}

const clearFilters = () => {
  store.filters.companyName = ''
  store.filters.status = ''
  store.applyFilters()
}

const openCreate = () => {
  editTarget.value = null
  showForm.value = true
}

const openEdit = (app: Application) => {
  editTarget.value = app
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
  editTarget.value = null
}

const confirmDelete = (app: Application) => {
  deleteTarget.value = app
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await store.deleteApplication(deleteTarget.value.id)
    deleteTarget.value = null
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  store.fetchApplications()
})
</script>
