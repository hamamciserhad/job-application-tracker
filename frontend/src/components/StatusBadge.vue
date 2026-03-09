<template>
  <span :class="['inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium', config.color]">
    {{ config.label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: string }>()

const statusConfig: Record<string, { label: string; color: string }> = {
  APPLIED: { label: 'Applied', color: 'bg-blue-100 text-blue-800' },
  PHONE_SCREEN: { label: 'Phone Screen', color: 'bg-violet-100 text-violet-800' },
  INTERVIEW: { label: 'Interview', color: 'bg-amber-100 text-amber-800' },
  TECHNICAL_TEST: { label: 'Technical Test', color: 'bg-orange-100 text-orange-800' },
  OFFER: { label: 'Offer', color: 'bg-green-100 text-green-800' },
  ACCEPTED: { label: 'Accepted', color: 'bg-emerald-100 text-emerald-800' },
  REJECTED: { label: 'Rejected', color: 'bg-red-100 text-red-800' },
  WITHDRAWN: { label: 'Withdrawn', color: 'bg-gray-100 text-gray-800' }
}

const config = computed(
  () => statusConfig[props.status] ?? { label: props.status, color: 'bg-gray-100 text-gray-800' }
)
</script>
