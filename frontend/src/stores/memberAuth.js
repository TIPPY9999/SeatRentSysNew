import { defineStore } from 'pinia'
import axios from 'axios'

export const useMemberAuthStore = defineStore('memberAuth', {
  state: () => {
    const savedMember = localStorage.getItem('member_user')
    const savedLogin = localStorage.getItem('isLogin') === 'true'

    return {
      isLogin: savedLogin,
      member: savedMember
        ? JSON.parse(savedMember)
        : {
            memId: null,
            memUsername: '',
            memName: '',
            memPoints: 0,
            memInvoice: '',
          },
    }
  },

  actions: {
    /* ========= 登入 ========= */
    setMemberLogin(memberData) {
      this.isLogin = true
      this.member = {
        memId: memberData.memId,
        memUsername: memberData.memUsername,
        memName: memberData.memName,
        memPoints: memberData.memPoints,
        memInvoice: memberData.memInvoice,
      }

      localStorage.setItem('member_user', JSON.stringify(this.member))
      localStorage.setItem('isLogin', 'true')
    },

    /* ========= 點數 / 資料刷新（保留 HEAD） ========= */
    async refreshPoints() {
      if (!this.member.memId) return

      try {
        const res = await axios.get('http://localhost:8080/api/members/find', {
          params: { memId: this.member.memId },
        })

        if (res.data) {
          // 重設整個物件，確保 Vue reactivity
          this.member = {
            ...this.member,
            memPoints: res.data.memPoints,
            memName: res.data.memName || this.member.memName,
          }

          localStorage.setItem('member_user', JSON.stringify(this.member))
          console.log('✅ 點數同步成功', this.member.memPoints)
        }
      } catch (err) {
        console.warn('❌ 點數同步失敗', err)
      }
    },

    /* ========= 登出（合併強化版） ========= */
    async clearMemberLogin() {
      // 1️⃣ 通知後端清除 Session（member2 的優點）
      try {
        await axios.post('http://localhost:8080/api/auth/logout', {}, { withCredentials: true })
        console.log('✅ 後端 Session 已清除')
      } catch (err) {
        console.log('ℹ️ 後端無 Session 或清除失敗')
      }

      // 2️⃣ 清前端狀態
      this.isLogin = false
      this.member = {
        memId: null,
        memUsername: '',
        memName: '',
        memPoints: 0,
        memInvoice: '',
      }

      // 3️⃣ 清 localStorage
      localStorage.removeItem('member_user')
      localStorage.removeItem('isLogin')
      localStorage.removeItem('token')

      console.log('✅ 前端登入資訊已清理')
    },
  },
})
