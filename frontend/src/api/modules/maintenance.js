/**
 * 維修模組 API
 * 封裝所有維修相關的 API 呼叫
 */
import http from '../http'

// ============ 維護人員 (Staff) API ============

/**
 * 取得所有在職維護人員
 */
export const getAllStaff = () => {
  return http.get('/maintenance/staff')
}

/**
 * 取得單一維護人員
 * @param {number} id - 人員 ID
 */
export const getStaffById = (id) => {
  return http.get(`/maintenance/staff/${id}`)
}

/**
 * 取得已停用的維護人員 (歷史紀錄)
 */
export const getInactiveStaff = () => {
  return http.get('/maintenance/staff/inactive')
}

/**
 * 新增維護人員
 * @param {Object} staff - 人員資料
 */
export const createStaff = (staff) => {
  return http.post('/maintenance/staff', staff)
}

/**
 * 更新維護人員
 * @param {number} id - 人員 ID
 * @param {Object} staff - 人員資料
 */
export const updateStaff = (id, staff) => {
  return http.put(`/maintenance/staff/${id}`, staff)
}

/**
 * 刪除維護人員 (軟刪除)
 * @param {number} id - 人員 ID
 */
export const deleteStaff = (id) => {
  return http.delete(`/maintenance/staff/${id}`)
}

// ============ 工單 (Ticket) API ============

/**
 * 取得全部工單
 */
export const getAllTickets = () => {
  return http.get('/maintenance/tickets')
}

/**
 * 取得待處理工單 (Active)
 */
export const getActiveTickets = () => {
  return http.get('/maintenance/tickets/active')
}

/**
 * 取得歷史工單 (History)
 */
export const getHistoryTickets = () => {
  return http.get('/maintenance/tickets/history')
}

/**
 * 取得單一工單
 * @param {number} id - 工單 ID
 */
export const getTicketById = (id) => {
  return http.get(`/maintenance/tickets/${id}`)
}

/**
 * 依據點 ID 查詢工單
 * @param {number} spotId - 據點 ID
 */
export const getTicketsBySpot = (spotId) => {
  return http.get(`/maintenance/tickets/spot/${spotId}`)
}

/**
 * 新增工單
 * @param {Object} ticket - 工單資料
 */
export const createTicket = (ticket) => {
  return http.post('/maintenance/tickets', ticket)
}

/**
 * 更新工單
 * @param {number} id - 工單 ID
 * @param {Object} ticket - 工單資料
 */
export const updateTicket = (id, ticket) => {
  return http.put(`/maintenance/tickets/${id}`, ticket)
}

// ============ 工單狀態流程 API ============

/**
 * 指派維修人員
 * @param {number} ticketId - 工單 ID
 * @param {number|null} staffId - 人員 ID (null 表示取消指派)
 */
export const assignStaff = (ticketId, staffId) => {
  return http.post(`/maintenance/tickets/${ticketId}/assign`, { staffId })
}

/**
 * 開始維修
 * @param {number} ticketId - 工單 ID
 */
export const startTicket = (ticketId) => {
  return http.post(`/maintenance/tickets/${ticketId}/start`)
}

/**
 * 結案
 * @param {number} ticketId - 工單 ID
 * @param {string} resultType - 結果類型 (FIXED, NOT_FIXED, NO_ISSUE, NOT_FIXABLE, OTHER)
 * @param {string} resolveNote - 備註
 */
export const resolveTicket = (ticketId, resultType, resolveNote) => {
  return http.post(`/maintenance/tickets/${ticketId}/resolve`, { resultType, resolveNote })
}

/**
 * 取消工單
 * @param {number} ticketId - 工單 ID
 * @param {string} reason - 取消原因
 */
export const cancelTicket = (ticketId, reason) => {
  return http.post(`/maintenance/tickets/${ticketId}/cancel`, { reason })
}

// ============ 據點 API (供工單表單使用) ============

/**
 * 取得所有據點 (供下拉選單使用)
 */
export const getAllSpots = () => {
  return http.get('/maintenance/spots')
}

// 匯出所有 API 作為預設物件 (方便一次 import)
export default {
  // Staff
  getAllStaff,
  getStaffById,
  getInactiveStaff,
  createStaff,
  updateStaff,
  deleteStaff,
  // Ticket
  getAllTickets,
  getActiveTickets,
  getHistoryTickets,
  getTicketById,
  getTicketsBySpot,
  createTicket,
  updateTicket,
  // Workflow
  assignStaff,
  startTicket,
  resolveTicket,
  cancelTicket,
  // Spot
  getAllSpots,
}
