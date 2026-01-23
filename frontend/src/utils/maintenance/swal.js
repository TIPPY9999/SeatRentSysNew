/**
 * Maintenance SweetAlert2 封裝
 * 用途: 統一 maintenance 模組的 SweetAlert2 彈窗樣式和行為
 * 範圍: 僅限 maintenance 模組使用，避免影響其他模組
 * 
 * 使用方式:
 * import { mtSwal, mtSwalSuccess, mtSwalError, mtSwalConfirmDanger } from '@/utils/maintenance/swal'
 * 
 * mtSwalSuccess({ title: '操作成功', text: '資料已更新' })
 * mtSwalConfirmDanger({ title: '確認刪除', text: '此操作無法復原' })
 */

import Swal from 'sweetalert2'

// ==================== 預設配置 ====================
const defaultConfig = {
  // 統一的 class 樣式
  customClass: {
    popup: 'mt-swal-popup',
    confirmButton: 'mt-swal-confirm',
    cancelButton: 'mt-swal-cancel'
  },
  // 統一的動畫效果 (優先使用 animate.css，若無則用 CSS class)
  showClass: {
    popup: 'animate__animated animate__bounceIn animate__faster'
  },
  hideClass: {
    popup: 'animate__animated animate__fadeOut animate__faster'
  },
  // 按鈕文字本地化
  confirmButtonText: '確認',
  cancelButtonText: '取消',
  // 響應式設計
  width: '500px',
  padding: '20px',
  // 移除預設顏色設定，改用 CSS class 控制
  confirmButtonColor: null,
  cancelButtonColor: null
}

// ==================== 主要封裝函數 ====================
/**
 * 通用 SweetAlert2 封裝
 * @param {Object} options - Swal.fire 的參數
 * @returns {Promise} SweetAlert2 Promise
 */
export function mtSwal(options = {}) {
  // 合併預設配置和用戶配置
  const config = {
    ...defaultConfig,
    ...options,
    customClass: {
      ...defaultConfig.customClass,
      ...(options.customClass || {})
    }
  }
  
  return Swal.fire(config)
}

// ==================== 成功提示 ====================
/**
 * 成功操作提示
 * @param {Object} options - { title, text, timer }
 */
export function mtSwalSuccess(options = {}) {
  return mtSwal({
    icon: 'success',
    title: options.title || '操作成功',
    text: options.text,
    timer: options.timer || 1500,
    timerProgressBar: true,
    showConfirmButton: options.showConfirmButton !== false ? false : true,
    customClass: {
      popup: 'mt-swal-popup',
      confirmButton: 'mt-swal-confirm-success'
    },
    showClass: {
      popup: 'animate__animated animate__fadeInUp animate__faster'
    }
  })
}

// ==================== 錯誤提示 ====================
/**
 * 錯誤操作提示
 * @param {Object} options - { title, text }
 */
export function mtSwalError(options = {}) {
  return mtSwal({
    icon: 'error',
    title: options.title || '操作失敗',
    text: options.text || '請稍後再試',
    confirmButtonText: '知道了',
    customClass: {
      popup: 'mt-swal-popup',
      confirmButton: 'mt-swal-confirm-danger'
    },
    showClass: {
      popup: 'animate__animated animate__shake animate__faster'
    }
  })
}

// ==================== 警告確認 ====================
/**
 * 警告確認對話框
 * @param {Object} options - { title, text, confirmButtonText, cancelButtonText }
 */
export function mtSwalWarning(options = {}) {
  return mtSwal({
    icon: 'warning',
    title: options.title || '請確認',
    text: options.text,
    showCancelButton: true,
    confirmButtonText: options.confirmButtonText || '確認',
    cancelButtonText: options.cancelButtonText || '取消',
    customClass: {
      popup: 'mt-swal-popup',
      confirmButton: 'mt-swal-confirm-warning',
      cancelButton: 'mt-swal-cancel'
    },
    showClass: {
      popup: 'animate__animated animate__fadeInDown animate__faster'
    }
  })
}

// ==================== 危險操作確認 ====================
/**
 * 危險操作確認對話框（如刪除、取消等）
 * @param {Object} options - { title, text, confirmButtonText, html }
 */
export function mtSwalConfirmDanger(options = {}) {
  return mtSwal({
    icon: 'warning',
    title: options.title || '危險操作',
    text: options.text,
    html: options.html, // 支援 HTML 內容
    showCancelButton: true,
    confirmButtonText: options.confirmButtonText || '確認刪除',
    cancelButtonText: options.cancelButtonText || '取消',
    reverseButtons: true, // 將取消按鈕放在左側
    customClass: {
      popup: 'mt-swal-popup',
      confirmButton: 'mt-swal-confirm-danger',
      cancelButton: 'mt-swal-cancel'
    },
    showClass: {
      popup: 'animate__animated animate__bounceIn animate__faster'
    }
  })
}

// ==================== 輸入對話框 ====================
/**
 * 帶輸入框的確認對話框
 * @param {Object} options - { title, inputPlaceholder, inputValidator, confirmButtonText }
 */
export function mtSwalInput(options = {}) {
  return mtSwal({
    title: options.title || '請輸入',
    input: options.inputType || 'text',
    inputPlaceholder: options.inputPlaceholder || '請輸入內容...',
    inputValidator: options.inputValidator || ((value) => {
      if (!value) return '請輸入內容！'
    }),
    showCancelButton: true,
    confirmButtonText: options.confirmButtonText || '確認',
    cancelButtonText: options.cancelButtonText || '取消',
    customClass: {
      popup: 'mt-swal-popup',
      confirmButton: 'mt-swal-confirm',
      cancelButton: 'mt-swal-cancel'
    }
  })
}

// ==================== 多行文字輸入 ====================
/**
 * 多行文字輸入對話框（如取消原因、備註等）
 * @param {Object} options - { title, inputPlaceholder, rows }
 */
export function mtSwalTextarea(options = {}) {
  return mtSwal({
    title: options.title || '請輸入',
    input: 'textarea',
    inputPlaceholder: options.inputPlaceholder || '請輸入內容...',
    inputAttributes: {
      rows: options.rows || 3
    },
    inputValidator: options.inputValidator || ((value) => {
      if (!value) return '請輸入內容！'
    }),
    showCancelButton: true,
    confirmButtonText: options.confirmButtonText || '確認',
    cancelButtonText: options.cancelButtonText || '取消',
    customClass: {
      popup: 'mt-swal-popup',
      confirmButton: 'mt-swal-confirm',
      cancelButton: 'mt-swal-cancel'
    }
  })
}

// ==================== 載入狀態 ====================
/**
 * 顯示載入狀態
 * @param {Object} options - { title, text }
 */
export function mtSwalLoading(options = {}) {
  return mtSwal({
    title: options.title || '處理中...',
    text: options.text || '請稍候',
    showConfirmButton: false,
    showCancelButton: false,
    allowOutsideClick: false,
    allowEscapeKey: false,
    customClass: {
      popup: 'mt-swal-popup mt-swal-loading'
    },
    didOpen: () => {
      Swal.showLoading()
    }
  })
}

// ==================== 訊息提示（無按鈕） ====================
/**
 * 純訊息提示（自動消失）
 * @param {Object} options - { icon, title, timer }
 */
export function mtSwalToast(options = {}) {
  return mtSwal({
    icon: options.icon || 'info',
    title: options.title || '提示',
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: options.timer || 3000,
    timerProgressBar: true,
    showClass: {
      popup: 'animate__animated animate__slideInRight animate__faster'
    },
    hideClass: {
      popup: 'animate__animated animate__slideOutRight animate__faster'
    }
  })
}

// ==================== 詳細信息彈窗 ====================
/**
 * 顯示詳細信息的彈窗（適用於工單詳情等）
 * @param {Object} options - { title, html, width }
 */
export function mtSwalDetail(options = {}) {
  return mtSwal({
    title: options.title || '詳細信息',
    html: options.html,
    width: options.width || '600px',
    confirmButtonText: '關閉',
    customClass: {
      popup: 'mt-swal-popup',
      confirmButton: 'mt-swal-cancel' // 使用 cancel 樣式作為關閉按鈕
    },
    showClass: {
      popup: 'animate__animated animate__zoomIn animate__faster'
    },
    hideClass: {
      popup: 'animate__animated animate__zoomOut animate__faster'
    }
  })
}

// ==================== 向後相容性支援 ====================
/**
 * 將原有的 Swal.fire 呼叫逐步遷移到新的封裝
 * 這個函數用來包裝現有的呼叫，方便逐步重構
 */
export function migrateSwalCall(originalOptions) {
  // TODO: 待確認後移除 - 這是暫時的相容性函數
  console.warn('mtSwal: 使用相容性函數，建議遷移到新的 mtSwal 系列函數')
  return mtSwal(originalOptions)
}

// ==================== 預設匯出 ====================
export default mtSwal