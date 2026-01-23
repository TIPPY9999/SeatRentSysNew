import { defineStore } from 'pinia'

export const useMemberAuthStore = defineStore('memberAuth', {
  state: () => ({
    /* =====================
     * 類型 1：登入狀態（前台系統用）
     * ===================== */
    isLogin: false, // 是否有會員登入

    /* =====================
     * 類型 2：會員業務資料（前台功能會用）
     * ===================== */
    member: {
      memId: null,
      memName: '',
      memPoints: 0,
      memInvoice: '',
    },
  }),

  actions: {
    /* 會員登入成功時呼叫 */
    setMemberLogin(memberData) {
      this.isLogin = true
      this.member = {
        memId: memberData.memId,
        memName: memberData.memName,
        memPoints: memberData.memPoints,
        memInvoice: memberData.memInvoice,
      }
    },

    /* 會員登出 */
    clearMemberLogin() {
      this.isLogin = false
      this.member = {
        memId: null,
        memName: '',
        memPoints: 0,
        memInvoice: '',
      }
    },
  },
})