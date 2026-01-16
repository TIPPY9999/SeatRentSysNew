<script setup>
import { ref, onMounted, computed } from 'vue'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router'

const router = useRouter()
const staffList = ref([])
const searchText = ref('')
const loading = ref(false)

// 取得已停用 (Inactive) 的資料
const fetchHistory = async () => {
  try {
    loading.value = true
    const res = await maintenanceApi.getInactiveStaff()
    staffList.value = res.data
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    loading.value = false
  }
}

// 前端搜尋
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
          <div class="col-sm-6"><h1>維護人員歷史紀錄</h1></div>
          <div class="col-sm-6 text-right">
            <el-button type="info" plain @click="router.push('/admin/staff-list')">
              <i class="fas fa-arrow-left mr-1"></i> 返回列表
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <el-card shadow="never">
          <template #header>
            <div class="d-flex justify-content-between align-items-center">
              <span class="text-secondary"><i class="fas fa-archive mr-1"></i> 已封存人員清單</span>
              <div style="width: 250px">
                <el-input
                  v-model="searchText"
                  placeholder="搜尋姓名或公司..."
                  prefix-icon="Search"
                  clearable
                />
              </div>
            </div>
          </template>

          <el-table
            :data="filteredList"
            v-loading="loading"
            border
            stripe
            style="width: 100%"
            empty-text="暫無歷史資料"
          >
            <el-table-column prop="staffId" label="ID" width="70" align="center" sortable />
            <el-table-column prop="staffName" label="姓名" width="120" sortable />
            <el-table-column prop="staffCompany" label="公司" width="150" sortable />
            <el-table-column prop="staffPhone" label="電話" width="150" />
            <el-table-column prop="staffEmail" label="Email" min-width="180" />
            <el-table-column prop="staffNote" label="備註" show-overflow-tooltip />

            <el-table-column label="狀態" width="100" align="center">
              <template #default>
                <el-tag type="info" effect="dark">已離職</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </section>
  </div>
</template>

<style scoped>
.content-header {
  padding: 15px 0.5rem;
}
</style>
