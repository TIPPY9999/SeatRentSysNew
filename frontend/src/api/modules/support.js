import http from '../http'

/**
 * Support 模組 API 封裝
 * 用途：客服支援相關功能（FAQ、Coze Bootstrap 等）
 */

/**
 * 取得 Coze Web Chat SDK Bootstrap 配置
 * GET /support/coze/bootstrap
 *
 * 注意：http instance 的 baseURL 已包含 /api，
 * 因此這裡只需 '/support/...'，不要加 '/api/support/...'
 *
 * @returns {Promise} { botId, token, sdkSrc, expiresIn, serverTime, note }
 */
export const getCozeBootstrap = () => {
  return http.get('/support/coze/bootstrap')
}

// 匯出所有 API
export default {
  getCozeBootstrap,
}
