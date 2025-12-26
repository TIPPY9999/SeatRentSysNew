<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

// 定義資料類型
interface InactiveStaff {
  staffId: number
  staffName: string
  staffCompany: string
  staffPhone: string
  staffNote: string
}

const staffList = ref<InactiveStaff[]>([])
const searchText = ref('')
const loading = ref(false)

// 取得離職或不活躍的人員紀錄
const fetchHistory = async () => {
  try {
    loading.value = true
    const res = await axios.get('http://localhost:8080/api/maintenance/staff/inactive')
    staffList.value = res.data
  } catch (error) {
    console.error('Fetch error:', error)
    alert('無法載入歷史紀錄，請檢查後端連線')
  } finally {
    loading.value = false
  }
}

// 快速搜尋過濾
const filteredList = computed(() => {
  const key = searchText.value.trim().toLowerCase()
  if (!key) return staffList.value
  return staffList.value.filter(
    (s) =>
      (s.staffName || '').toLowerCase().includes(key) ||
      (s.staffCompany || '').toLowerCase().includes(key),
  )
})

onMounted(() => fetchHistory())
</script>

<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>維護人員歷史紀錄</h1>
          </div>
          <div class="col-sm-6 text-right">
            <router-link to="/admin/staff-list" class="btn btn-outline-secondary btn-sm">
              <i class="fas fa-arrow-left mr-1"></i> 返回列表
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="card card-outline card-info">
          <div class="card-header">
            <h3 class="card-title">已封存人員資料</h3>
            <div class="card-tools">
              <div class="input-group input-group-sm" style="width: 200px">
                <input
                  v-model="searchText"
                  type="text"
                  class="form-control"
                  placeholder="搜尋姓名或公司..."
                />
                <div class="input-group-append">
                  <span class="btn btn-default">
                    <i class="fas fa-search"></i>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-bordered table-striped m-0">
                <thead>
                  <tr class="bg-light">
                    <th style="width: 80px">ID</th>
                    <th>姓名</th>
                    <th>公司</th>
                    <th>電話</th>
                    <th>備註</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="s in filteredList" :key="s.staffId">
                    <td>{{ s.staffId }}</td>
                    <td>{{ s.staffName }}</td>
                    <td>{{ s.staffCompany }}</td>
                    <td>{{ s.staffPhone }}</td>
                    <td>{{ s.staffNote }}</td>
                  </tr>
                  <tr v-if="filteredList.length === 0">
                    <td colspan="5" class="text-center py-4">
                      <span v-if="loading">載入中...</span>
                      <span v-else class="text-muted">暫無歷史紀錄資料</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* 保持與內容區塊一致的邊距 */
.content-header {
  padding: 15px 0.5rem;
}

/* 讓表格在小螢幕上也能橫向捲動 */
.table-responsive {
  overflow-x: auto;
}

.jy-table th {
  white-space: nowrap;
}
</style>
