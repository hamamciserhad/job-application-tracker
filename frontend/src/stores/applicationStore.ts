import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import api from '../api/axios'

export interface Application {
  id: number
  companyName: string
  position: string
  status: string
  salary: number | null
  location: string | null
  jobUrl: string | null
  notes: string | null
  appliedDate: string | null
  createdAt: string
  updatedAt: string
}

export interface ApplicationFilters {
  status: string
  companyName: string
  page: number
  size: number
}

export interface PaginationInfo {
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ApplicationFormData {
  companyName: string
  position: string
  status: string | null
  salary: number | null
  location: string | null
  jobUrl: string | null
  notes: string | null
  appliedDate: string | null
}

export const useApplicationStore = defineStore('applications', () => {
  const applications = ref<Application[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pagination = ref<PaginationInfo>({ page: 0, size: 10, totalElements: 0, totalPages: 0 })
  const filters = reactive<ApplicationFilters>({
    status: '',
    companyName: '',
    page: 0,
    size: 10
  })

  const fetchApplications = async () => {
    loading.value = true
    error.value = null
    try {
      const params: Record<string, string | number> = {
        page: filters.page,
        size: filters.size,
        sort: 'createdAt,desc'
      }
      if (filters.status) params.status = filters.status
      if (filters.companyName) params.companyName = filters.companyName

      const { data } = await api.get('/api/applications', { params })
      applications.value = data.content
      pagination.value = {
        page: data.page,
        size: data.size,
        totalElements: data.totalElements,
        totalPages: data.totalPages
      }
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } }
      error.value = e?.response?.data?.message ?? 'Failed to load applications'
    } finally {
      loading.value = false
    }
  }

  const createApplication = async (data: ApplicationFormData) => {
    await api.post('/api/applications', data)
    await fetchApplications()
  }

  const updateApplication = async (id: number, data: ApplicationFormData) => {
    await api.put(`/api/applications/${id}`, data)
    await fetchApplications()
  }

  const deleteApplication = async (id: number) => {
    await api.delete(`/api/applications/${id}`)
    await fetchApplications()
  }

  const setPage = (page: number) => {
    filters.page = page
    fetchApplications()
  }

  const applyFilters = () => {
    filters.page = 0
    fetchApplications()
  }

  return {
    applications,
    loading,
    error,
    pagination,
    filters,
    fetchApplications,
    createApplication,
    updateApplication,
    deleteApplication,
    setPage,
    applyFilters
  }
})
