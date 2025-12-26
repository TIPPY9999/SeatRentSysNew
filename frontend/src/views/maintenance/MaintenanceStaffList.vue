<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

interface Staff {
  staffId: number
  staffName: string
  staffCompany: string
  staffPhone: string
  staffEmail: string
  staffNote: string
  createdAt: string
}

const staffList = ref<Staff[]>([])
const searchText = ref('')
const loading = ref(false)

const fetchStaff = async () => {
  try {
    loading.value = true
    const res = await axios.get('http://localhost:8080/api/maintenance/staff')
    staffList.value = res.data
  } catch (error) {
    console.error(error)
    alert('無法載入人員列表，請檢查連線')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id: number) => {
  if (!confirm(`確認刪除維護人員 #${id} 嗎？`)) return
  try {
    await axios.delete(`http://localhost:8080/api/maintenance/staff/${id}`)
    alert('刪除成功')
    fetchStaff()
  } catch (error) {
    alert('刪除失敗')
  }
}

const filteredList = computed(() => {
  const key = searchText.value.trim().toLowerCase()
  if (!key) return staffList.value
  return staffList.value.filter(
    (s) =>
      (s.staffName || '').toLowerCase().includes(key) ||
      (s.staffCompany || '').toLowerCase().includes(key) ||
      (s.staffPhone || '').toLowerCase().includes(key) ||
      (s.staffEmail || '').toLowerCase().includes(key),
  )
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-TW', { hour12: false })
}

onMounted(() => fetchStaff())
</script>

<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6"><h1>維護人員列表</h1></div>
          <div class="col-sm-6 text-right">
            <router-link to="/staff-form" class="btn btn-success btn-sm">新增維護人員</router-link>
            <router-link to="/staff-history" class="btn btn-outline-info btn-sm ml-2"
              >查看歷史紀錄</router-link
            >
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="row mb-3">
          <div class="col-md-4 col-sm-6 col-12">
            <div class="small-box bg-info">
              <div class="inner">
                <h3>{{ staffList.length }}</h3>
                <p>維護人員總數</p>
              </div>
              <div class="icon"><i class="fas fa-users"></i></div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header bg-light">
            <h3 class="card-title mb-0"><i class="fas fa-user-cog mr-1"></i> 維護人員管理</h3>
          </div>
          <div class="card-body">
            <div class="row mb-2">
              <div class="col-sm-6">
                <input
                  v-model="searchText"
                  type="text"
                  class="form-control"
                  placeholder="搜尋姓名 / 公司 / 電話 / Email"
                />
              </div>
            </div>

            <table class="table table-bordered table-hover table-sm mb-0 jy-table">
              <thead>
                <tr>
                  <th style="width: 50px">ID</th>
                  <th>姓名</th>
                  <th>公司</th>
                  <th>電話</th>
                  <th>Email</th>
                  <th>備註</th>
                  <th>建立時間</th>
                  <th style="width: 140px">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in filteredList" :key="s.staffId">
                  <td>{{ s.staffId }}</td>
                  <td>{{ s.staffName }}</td>
                  <td>{{ s.staffCompany }}</td>
                  <td>{{ s.staffPhone }}</td>
                  <td>{{ s.staffEmail }}</td>
                  <td>{{ s.staffNote }}</td>
                  <td>{{ formatDate(s.createdAt) }}</td>
                  <td>
                    <router-link
                      :to="`/staff-form/${s.staffId}`"
                      class="btn btn-outline-primary btn-sm mr-1"
                      >編輯</router-link
                    >
                    <button @click="handleDelete(s.staffId)" class="btn btn-outline-danger btn-sm">
                      刪除
                    </button>
                  </td>
                </tr>
                <tr v-if="filteredList.length === 0">
                  <td colspan="8" class="text-center">
                    {{ loading ? '載入中...' : '目前沒有資料' }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
