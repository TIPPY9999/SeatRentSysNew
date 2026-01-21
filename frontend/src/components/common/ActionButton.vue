<script setup>
const props = defineProps({
  type: {
    type: String,
    default: 'primary', // primary, secondary, success, danger, warning
  },
  size: {
    type: String,
    default: 'medium', // small, medium, large
  },
  icon: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['click'])

const handleClick = (event) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<template>
  <button
    :class="['action-btn', `btn-${type}`, `btn-${size}`]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <i v-if="loading" class="fas fa-spinner fa-spin"></i>
    <i v-else-if="icon" :class="icon"></i>
    <span v-if="$slots.default" class="btn-text">
      <slot></slot>
    </span>
  </button>
</template>

<style scoped>
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

/* 尺寸 */
.btn-small {
  padding: 6px 12px;
  font-size: 13px;
}

.btn-medium {
  padding: 10px 20px;
  font-size: 14px;
}

.btn-large {
  padding: 14px 28px;
  font-size: 16px;
}

/* 主要按鈕 */
.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);
  transform: translateY(-2px);
}

/* 次要按鈕 */
.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #e5e7eb;
  transform: translateY(-1px);
}

/* 成功按鈕 */
.btn-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.btn-success:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.5);
  transform: translateY(-2px);
}

/* 危險按鈕 */
.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

.btn-danger:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.5);
  transform: translateY(-2px);
}

/* 警告按鈕 */
.btn-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

.btn-warning:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.5);
  transform: translateY(-2px);
}

.btn-text {
  line-height: 1;
}
</style>
