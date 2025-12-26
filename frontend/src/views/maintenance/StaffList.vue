<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

// 1. 定義資料狀態
const staffList = ref<any[]>([])
const searchText = ref('')
const loading = ref(true)

// 2. 取得後端資料 (對接你的 Spring Boot API)
const fetchStaff = async () => {
  try {
    loading.value = true
    // 請確認你的後端 Server 有啟動，且 URL 正確
    const response = await axios.get('http://localhost:8080/api/maintenance/staff')
    staffList.value = response.data
  } catch (error) {
    console.error('API 呼叫失敗:', error)
    alert('無法取得維護人員資料，請檢查後端連線')
  } finally {
    loading.value = false
  }
}

// 3. 搜尋過濾邏輯 (取代原本的 JS filterStaff)
const filteredStaff = computed(() => {
  const query = searchText.value.trim().toLowerCase()
  if (!query) return staffList.value
  return staffList.value.filter(
    (s) =>
      s.staffName?.toLowerCase().includes(query) ||
      s.staffCompany?.toLowerCase().includes(query) ||
      s.staffPhone?.toLowerCase().includes(query) ||
      s.staffEmail?.toLowerCase().includes(query),
  )
})

// 4. 刪除功能
const deleteStaff = async (id: number) => {
  if (!confirm(`確認刪除維護人員 #${id} 嗎？`)) return
  try {
    await axios.delete(`http://localhost:8080/api/maintenance/staff/${id}`)
    alert('刪除成功')
    fetchStaff() // 重新整理列表
  } catch (error) {
    alert('刪除失敗')
  }
}

onMounted(() => {
  fetchStaff()
})
</script>

<template>
  <div class="content-wrapper" style="margin-left: 0">
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>維護人員列表</h1>
          </div>
          <div class="col-sm-6 text-right">
            <button class="btn btn-success btn-sm">新增維護人員</button>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="card">
          <div class="card-header">
            <div class="input-group input-group-sm" style="max-width: 320px">
              <input
                v-model="searchText"
                type="text"
                class="form-control"
                placeholder="搜尋姓名 / 公司 / 電話 / Email"
              />
              <div class="input-group-append">
                <button class="btn btn-primary" type="button"><i class="fas fa-search"></i></button>
              </div>
            </div>
          </div>
          <div class="card-body p-0">
            <table class="table table-bordered table-hover table-sm mb-0">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>姓名</th>
                  <th>公司</th>
                  <th>電話</th>
                  <th>Email</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in filteredStaff" :key="s.staffId">
                  <td>{{ s.staffId }}</td>
                  <td>{{ s.staffName }}</td>
                  <td>{{ s.staffCompany }}</td>
                  <td>{{ s.staffPhone }}</td>
                  <td>{{ s.staffEmail }}</td>
                  <td>
                    <button class="btn btn-outline-primary btn-sm mr-1">編輯</button>
                    <button @click="deleteStaff(s.staffId)" class="btn btn-outline-danger btn-sm">
                      刪除
                    </button>
                  </td>
                </tr>
                <tr v-if="filteredStaff.length === 0">
                  <td colspan="6" class="text-center">目前沒有維護人員資料。</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
