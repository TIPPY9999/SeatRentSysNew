import { defineStore } from 'pinia'

export const useAdminAuthStore = defineStore('adminAuth', {
  state: () => ({
    isLogin: false,
    admin: {
      username: '',
      name: '',
      role: null,
    },
  }),
  actions: {
    setAdmin(admin) {
      this.isLogin = true
      this.admin = admin
    },
    clearAdmin() {
      this.isLogin= false
      this.admin = {
        username: '',
        name: '',
        role: null,
      }
    },
  },
})