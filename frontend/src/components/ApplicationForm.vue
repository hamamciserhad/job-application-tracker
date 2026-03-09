<template>
  <div
    class="fixed inset-0 bg-black/30 flex items-center justify-center z-50 p-4"
    @click.self="$emit('close')"
  >
    <div class="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[90vh] flex flex-col">
      <!-- Header -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
        <h2 class="text-base font-semibold text-gray-900">
          {{ isEdit ? 'Edit Application' : 'New Application' }}
        </h2>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Body -->
      <div class="flex-1 overflow-y-auto px-6 py-5 space-y-4">
        <!-- API error -->
        <div
          v-if="error"
          class="bg-red-50 border border-red-200 rounded-md p-3 text-red-700 text-sm"
        >
          {{ error }}
        </div>

        <!-- Company Name -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            Company Name <span class="text-red-500">*</span>
          </label>
          <input
            v-model="form.companyName"
            type="text"
            maxlength="255"
            placeholder="e.g. Acme Corp"
            :class="[
              'w-full px-3 py-2 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500',
              fieldErrors.companyName ? 'border-red-400' : 'border-gray-300'
            ]"
          />
          <p v-if="fieldErrors.companyName" class="mt-1 text-xs text-red-600">
            {{ fieldErrors.companyName }}
          </p>
        </div>

        <!-- Position -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            Position <span class="text-red-500">*</span>
          </label>
          <input
            v-model="form.position"
            type="text"
            maxlength="255"
            placeholder="e.g. Senior Engineer"
            :class="[
              'w-full px-3 py-2 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500',
              fieldErrors.position ? 'border-red-400' : 'border-gray-300'
            ]"
          />
          <p v-if="fieldErrors.position" class="mt-1 text-xs text-red-600">
            {{ fieldErrors.position }}
          </p>
        </div>

        <!-- Status + Applied Date -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select
              v-model="form.status"
              class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
            >
              <option v-for="s in statuses" :key="s.value" :value="s.value">{{ s.label }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Applied Date</label>
            <input
              v-model="form.appliedDate"
              type="date"
              class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        <!-- Location + Salary -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Location</label>
            <input
              v-model="form.location"
              type="text"
              maxlength="255"
              placeholder="e.g. Remote"
              class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Salary</label>
            <input
              v-model="form.salaryInput"
              type="number"
              min="0"
              step="1000"
              placeholder="e.g. 90000"
              class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        <!-- Job URL -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Job URL</label>
          <input
            v-model="form.jobUrl"
            type="url"
            placeholder="https://..."
            class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <!-- Notes -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Notes</label>
          <textarea
            v-model="form.notes"
            rows="3"
            placeholder="Any notes about this application..."
            class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
          ></textarea>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex justify-end gap-3 px-6 py-4 border-t border-gray-200">
        <button
          @click="$emit('close')"
          class="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
        >
          Cancel
        </button>
        <button
          @click="handleSubmit"
          :disabled="submitting"
          class="px-4 py-2 text-sm text-white bg-indigo-600 rounded-md hover:bg-indigo-700 disabled:opacity-50 transition-colors"
        >
          {{ submitting ? (isEdit ? 'Saving\u2026' : 'Adding\u2026') : (isEdit ? 'Save Changes' : 'Add Application') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { useApplicationStore } from '../stores/applicationStore'
import type { Application } from '../stores/applicationStore'

const props = defineProps<{ application?: Application | null }>()
const emit = defineEmits<{ (e: 'close'): void; (e: 'saved'): void }>()

const store = useApplicationStore()
const toast = useToast()

const isEdit = computed(() => !!props.application)
const submitting = ref(false)
const error = ref('')

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

const form = reactive({
  companyName: '',
  position: '',
  status: 'APPLIED',
  appliedDate: '',
  location: '',
  salaryInput: '',
  jobUrl: '',
  notes: ''
})

const fieldErrors = reactive({ companyName: '', position: '' })

const validate = () => {
  fieldErrors.companyName = form.companyName.trim() ? '' : 'Company name is required'
  fieldErrors.position = form.position.trim() ? '' : 'Position is required'
  return !fieldErrors.companyName && !fieldErrors.position
}

const handleSubmit = async () => {
  if (!validate()) return
  submitting.value = true
  error.value = ''
  try {
    const payload = {
      companyName: form.companyName.trim(),
      position: form.position.trim(),
      status: form.status || null,
      salary: form.salaryInput ? parseFloat(form.salaryInput) : null,
      location: form.location.trim() || null,
      jobUrl: form.jobUrl.trim() || null,
      notes: form.notes.trim() || null,
      appliedDate: form.appliedDate || null
    }
    if (props.application) {
      await store.updateApplication(props.application.id, payload)
      toast.success('Application updated!')
    } else {
      await store.createApplication(payload)
      toast.success('Application added!')
    }
    emit('saved')
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    error.value = e?.response?.data?.message ?? 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (props.application) {
    form.companyName = props.application.companyName
    form.position = props.application.position
    form.status = props.application.status
    form.appliedDate = props.application.appliedDate ?? ''
    form.location = props.application.location ?? ''
    form.salaryInput = props.application.salary !== null ? String(props.application.salary) : ''
    form.jobUrl = props.application.jobUrl ?? ''
    form.notes = props.application.notes ?? ''
  }
})
</script>
