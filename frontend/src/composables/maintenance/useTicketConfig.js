/**
 * 工單配置 Composable
 * 統一管理工單的狀態、優先級等配置
 */

// ==================== 優先級配置 ====================
export const priorityConfig = {
  LOW: { 
    color: '#909399', 
    bgColor: '#f4f4f5', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-circle text-info',
    text: '低優先', 
    desc: '可稍後處理',
    tagType: 'info'
  },
  NORMAL: { 
    color: '#409eff', 
    bgColor: '#ecf5ff', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-circle text-primary',
    text: '普通', 
    desc: '正常排程處理',
    tagType: ''
  },
  HIGH: { 
    color: '#e6a23c', 
    bgColor: '#fdf6ec', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-exclamation-circle text-warning',
    text: '高優先', 
    desc: '優先安排處理',
    tagType: 'warning'
  },
  URGENT: { 
    color: '#f56c6c', 
    bgColor: '#fef0f0', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-fire text-danger',
    text: '緊急', 
    desc: '立即處理',
    tagType: 'danger'
  },
}

// ==================== 狀態配置 ====================
export const statusConfig = {
  REPORTED: { 
    text: '已通報', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-clipboard-list text-info',
    tagType: 'info',
    color: '#17a2b8'
  },
  ASSIGNED: { 
    text: '已指派', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-user-check text-primary',
    tagType: 'primary',
    color: '#007bff'
  },
  UNDER_MAINTENANCE: { 
    text: '維修中', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-tools text-warning',
    tagType: 'warning',
    color: '#ffc107'
  },
  RESOLVED: { 
    text: '已完成', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-check-circle text-success',
    tagType: 'success',
    color: '#28a745'
  },
  CANCELLED: { 
    text: '已取消', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-times-circle text-secondary',
    tagType: 'info',
    color: '#6c757d'
  },
}

// ==================== 問題類型選項 ====================
export const issueTypeOptions = [
  { value: '椅子損壞', icon: '', iconClass: 'fas fa-chair' }, // UI: replace emoji with icon for consistency
  { value: '機台故障異常', icon: '', iconClass: 'fas fa-desktop' }, // UI: replace emoji with icon for consistency
  { value: '保養', icon: '', iconClass: 'fas fa-tools' }, // UI: replace emoji with icon for consistency
]

// ==================== 結案結果配置 ====================
export const resultConfig = {
  NO_ISSUE: { 
    text: '未發現問題', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-search text-secondary',
    color: '#909399',
    desc: '檢查後未發現異常'
  },
  FIXED: { 
    text: '已修復', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-check-circle text-success',
    color: '#67c23a',
    desc: '問題已成功修復'
  },
  REPLACED: { 
    text: '已更換', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-exchange-alt text-primary',
    color: '#409eff',
    desc: '已更換損壞零件'
  },
  NOT_FIXABLE: { 
    text: '無法修復', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-times-circle text-danger',
    color: '#f56c6c',
    desc: '問題無法修復，需更換設備'
  },
  OTHER: { 
    text: '其他', 
    icon: '', // UI: replace emoji with icon for consistency
    iconClass: 'fas fa-clipboard-list text-warning',
    color: '#e6a23c',
    desc: '其他情況'
  },
}

// ==================== 工具函數 ====================

/**
 * 取得優先級對應的 Tag 類型
 * @param {string} priority - 優先級代碼
 * @returns {string} Element Plus tag type
 */
export const getPriorityTag = (priority) => {
  return priorityConfig[priority]?.tagType || 'info'
}

/**
 * 取得狀態對應的 Tag 類型
 * @param {string} status - 狀態代碼
 * @returns {string} Element Plus tag type
 */
export const getStatusTag = (status) => {
  return statusConfig[status]?.tagType || ''
}

/**
 * 取得優先級文字
 * @param {string} priority - 優先級代碼
 * @returns {string} 優先級文字
 */
export const getPriorityText = (priority) => {
  return priorityConfig[priority]?.text || priority
}

/**
 * 取得狀態文字
 * @param {string} status - 狀態代碼
 * @returns {string} 狀態文字
 */
export const getStatusText = (status) => {
  return statusConfig[status]?.text || status
}

/**
 * 取得優先級圖示 (向後相容)
 * @param {string} priority - 優先級代碼
 * @returns {string} emoji 圖示 (TODO: 待確認後移除)
 */
export const getPriorityIcon = (priority) => {
  return priorityConfig[priority]?.icon || '❓'
}

/**
 * 取得優先級圖示 CSS Class
 * @param {string} priority - 優先級代碼
 * @returns {string} CSS class 字串
 */
export const getPriorityIconClass = (priority) => {
  return priorityConfig[priority]?.iconClass || 'fas fa-question-circle'
}

/**
 * 取得狀態圖示 (向後相容)
 * @param {string} status - 狀態代碼
 * @returns {string} emoji 圖示 (TODO: 待確認後移除)
 */
export const getStatusIcon = (status) => {
  return statusConfig[status]?.icon || '❓'
}

/**
 * 取得狀態圖示 CSS Class
 * @param {string} status - 狀態代碼
 * @returns {string} CSS class 字串
 */
export const getStatusIconClass = (status) => {
  return statusConfig[status]?.iconClass || 'fas fa-question-circle'
}

/**
 * 取得結案結果文字
 * @param {string} resultType - 結案結果代碼
 * @returns {string} 結果文字 (找不到則回傳原始代碼)
 */
export const getResultText = (resultType) => {
  if (!resultType) return '-'
  return resultConfig[resultType]?.text || resultType
}

/**
 * 取得結案結果圖示 (向後相容)
 * @param {string} resultType - 結案結果代碼
 * @returns {string} emoji 圖示 (TODO: 待確認後移除)
 */
export const getResultIcon = (resultType) => {
  return resultConfig[resultType]?.icon || '❓'
}

/**
 * 取得結案結果圖示 CSS Class
 * @param {string} resultType - 結案結果代碼
 * @returns {string} CSS class 字串
 */
export const getResultIconClass = (resultType) => {
  return resultConfig[resultType]?.iconClass || 'fas fa-question-circle'
}

// ==================== Composable Hook ====================
export function useTicketConfig() {
  return {
    priorityConfig,
    statusConfig,
    resultConfig,
    issueTypeOptions,
    getPriorityTag,
    getStatusTag,
    getPriorityText,
    getStatusText,
    getPriorityIcon,
    getPriorityIconClass, // UI: new function for icon classes
    getStatusIcon,
    getStatusIconClass, // UI: new function for icon classes
    getResultText,
    getResultIcon,
    getResultIconClass, // UI: new function for icon classes
  }
}

export default useTicketConfig
