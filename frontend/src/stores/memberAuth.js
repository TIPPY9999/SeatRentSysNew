import { defineStore } from 'pinia'
import axios from 'axios'

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
      memUsername: '',
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
        memUsername: memberData.memUsername,
        memName: memberData.memName,
        memPoints: memberData.memPoints,
        memInvoice: memberData.memInvoice,
      }
    },

    /* 會員登出 (優化版) */
    async clearMemberLogin() {
      // 1. 先嘗試通知後端清除 Session (Google 登出)
      try {
        await axios.post('http://localhost:8080/api/auth/logout', {}, { withCredentials: true });
        console.log('後端 Session 已清除');
      } catch (err) {
        // 如果後端本來就沒 Session 也沒關係，繼續清理前端
        console.log('後端無 Session 或清除失敗');
      }

      // 2. 清理前端 Pinia 狀態
      this.isLogin = false
      this.member = {
        memId: null,
        memUsername: '',
        memName: '',
        memPoints: 0,
        memInvoice: '',
      }

      // 3. 清理前端 localStorage
      localStorage.removeItem('member_user');
      localStorage.removeItem('token');
      
      console.log('前端登入資訊已清理');
    },
  },
})