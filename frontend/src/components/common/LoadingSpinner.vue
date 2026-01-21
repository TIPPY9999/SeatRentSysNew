<script setup>
const props = defineProps({
  size: {
    type: String,
    default: 'medium', // small, medium, large
  },
  text: {
    type: String,
    default: '',
  },
  fullscreen: {
    type: Boolean,
    default: false,
  },
})

const sizeMap = {
  small: '24px',
  medium: '40px',
  large: '56px',
}
</script>

<template>
  <div :class="['loading-container', { fullscreen }]">
    <div class="spinner-wrapper">
      <div 
        class="spinner" 
        :style="{ width: sizeMap[size], height: sizeMap[size] }"
      ></div>
      <p v-if="text" class="loading-text">{{ text }}</p>
    </div>
  </div>
</template>

<style scoped>
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.loading-container.fullscreen {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  z-index: 9999;
}

.spinner-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.spinner {
  border: 3px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  font-weight: 500;
}
</style>
