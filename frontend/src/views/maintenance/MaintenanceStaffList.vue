<script setup>
import { ref, onMounted, computed } from 'vue'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router'

const router = useRouter()
const staffList = ref([])
const searchText = ref('')
const loading = ref(true) // 預設 true 以顯示骨架屏

const fetchStaff = async () => {
  try {
    loading.value = true
    const res = await maintenanceApi.getAllStaff()
    staffList.value = res.data
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    loading.value = false
  }
}

const handleDelete = (id) => {
  Swal.fire({
    title: '確定要停用嗎？',
    text: '此操作為軟刪除，可在歷史紀錄查看',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: '停用',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await maintenanceApi.deleteStaff(id)
        Swal.fire('已停用!', '', 'success')
        fetchStaff()
      } catch {
        // 錯誤已由 http.js 攔截器處理
      }
    }
  })
}

const filteredList = computed(() => {
  const key = searchText.value.trim().toLowerCase()
  if (!key) return staffList.value
  return staffList.value.filter(
    (s) =>
      (s.staffName || '').toLowerCase().includes(key) ||
      (s.staffCompany || '').toLowerCase().includes(key) ||
      (s.staffPhone || '').includes(key),
  )
})

const formatDate = (row, column, cellValue) => {
  if (!cellValue) return '-'
  return new Date(cellValue).toLocaleDateString('zh-TW')
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
            <el-button type="success" @click="router.push('/admin/staff-form')"
              ><i class="fas fa-plus mr-1"></i> 新增</el-button
            >
            <el-button type="info" plain @click="router.push('/admin/staff-history')"
              ><i class="fas fa-history mr-1"></i> 歷史</el-button
            >
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="row mb-3">
          <div class="col-md-3">
            <el-card shadow="hover" :body-style="{ padding: '15px' }">
              <div class="d-flex align-items-center">
                <div class="text-success"><i class="fas fa-user-check fa-2x"></i></div>
                <div class="ml-3">
                  <h3 class="m-0">{{ staffList.length }}</h3>
                  <small class="text-muted">在職人員</small>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <el-card shadow="never">
          <template #header>
            <div class="d-flex justify-content-between align-items-center">
              <span><i class="fas fa-list mr-1"></i> 人員名單</span>
              <div style="width: 250px">
                <el-input
                  v-model="searchText"
                  placeholder="搜尋姓名、公司..."
                  prefix-icon="Search"
                  clearable
                />
              </div>
            </div>
          </template>

          <el-skeleton :rows="5" animated v-if="loading" />

          <el-table v-else :data="filteredList" stripe style="width: 100%">
            <el-table-column prop="staffId" label="ID" width="60" align="center" sortable />
            <el-table-column prop="staffName" label="姓名" width="120" sortable>
              <template #default="{ row }">
                <i
                  class="fas fa-circle text-success"
                  style="font-size: 8px; vertical-align: middle; margin-right: 5px"
                ></i>
                {{ row.staffName }}
              </template>
            </el-table-column>
            <el-table-column prop="staffCompany" label="公司" width="150" />
            <el-table-column prop="staffPhone" label="電話" width="150" />
            <el-table-column prop="staffEmail" label="Email" min-width="180" />
            <el-table-column prop="staffNote" label="備註" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="建立日" width="120" :formatter="formatDate" />

            <el-table-column label="操作" width="150" align="center">
              <template #default="scope">
                <el-button
                  size="small"
                  type="primary"
                  plain
                  @click="router.push(`/admin/staff-form/${scope.row.staffId}`)"
                  title="編輯"
                >
                  <i class="fas fa-edit"></i>
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  @click="handleDelete(scope.row.staffId)"
                  title="停用"
                >
                  <i class="fas fa-user-slash"></i>
                </el-button>
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
