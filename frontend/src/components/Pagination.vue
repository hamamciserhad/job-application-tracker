<template>
  <div class="flex items-center justify-between">
    <p class="text-sm text-gray-600">
      Showing {{ start }} to {{ end }} of {{ totalElements }} result{{ totalElements !== 1 ? 's' : '' }}
    </p>
    <div class="flex items-center gap-1">
      <button
        @click="$emit('change', page - 1)"
        :disabled="page === 0"
        class="px-3 py-1.5 text-sm rounded-md border border-gray-300 disabled:opacity-40 hover:bg-gray-50 transition-colors"
      >
        Previous
      </button>
      <button
        v-for="p in visiblePages"
        :key="p"
        @click="$emit('change', p)"
        :class="[
          'px-3 py-1.5 text-sm rounded-md border transition-colors',
          p === page
            ? 'bg-indigo-600 text-white border-indigo-600'
            : 'border-gray-300 hover:bg-gray-50'
        ]"
      >
        {{ p + 1 }}
      </button>
      <button
        @click="$emit('change', page + 1)"
        :disabled="page >= totalPages - 1"
        class="px-3 py-1.5 text-sm rounded-md border border-gray-300 disabled:opacity-40 hover:bg-gray-50 transition-colors"
      >
        Next
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  page: number
  totalPages: number
  totalElements: number
  size: number
}>()

defineEmits<{ (e: 'change', page: number): void }>()

const start = computed(() => (props.totalElements === 0 ? 0 : props.page * props.size + 1))
const end = computed(() => Math.min((props.page + 1) * props.size, props.totalElements))

const visiblePages = computed(() => {
  const range: number[] = []
  const delta = 2
  const left = Math.max(0, props.page - delta)
  const right = Math.min(props.totalPages - 1, props.page + delta)
  for (let i = left; i <= right; i++) range.push(i)
  return range
})
</script>
