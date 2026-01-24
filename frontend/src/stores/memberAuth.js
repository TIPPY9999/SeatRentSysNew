import { defineStore } from 'pinia'
import axios from 'axios'

export const useMemberAuthStore = defineStore('memberAuth', {
  state: () => {
    const savedMember = localStorage.getItem('member_user')
    const savedLogin = localStorage.getItem('isLogin') === 'true'

    return {
      isLogin: savedLogin,
      member: savedMember ? JSON.parse(savedMember) : {
        memId: null,
        memUsername: '',
        memName: '',
        memPoints: 0,
        memInvoice: '',
      },
    }
  },

  actions: {
    // 登入
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

    // 刷新點數與資料
    async refreshPoints() {
      if (!this.member.memId) return
      
      try {
        const res = await axios.get(`http://localhost:8080/api/members/find`, {
          params: { memId: this.member.memId }
        })

        if (res.data) {
          // 更新 Store 裡的資料
          this.member.memPoints = res.data.memPoints
          if (res.data.memName) this.member.memName = res.data.memName
          
          // 同步回 LocalStorage
          localStorage.setItem('member_user', JSON.stringify(this.member))
          console.log('✅ 自動同步成功：', this.member)
        }
      } catch (error) {
        console.warn('同步點數失敗', error)
      }
    },

    // 登出
    clearMemberLogin() {
      this.isLogin = false
      this.member = {
        memId: null,
        memUsername: '',
        memName: '',
        memPoints: 0,
        memInvoice: '',
      }
      localStorage.removeItem('member_user')
      localStorage.removeItem('isLogin')
      localStorage.removeItem('token')
    }
  } // <--- actions 的結束
}) // <--- defineStore 的結束