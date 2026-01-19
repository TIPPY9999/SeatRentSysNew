import { defineStore } from 'pinia'

export const useAdminAuthStore = defineStore('adminAuth', {
  state: () => ({
    admin: {
      username: '',
      name: '',
      role: null,
    },
  }),
  actions: {
    setAdmin(admin) {
      this.admin = admin
    },
    clearAdmin() {
      this.admin = {
        username: '',
        name: '',
        role: null,
      }
    },
  },
})