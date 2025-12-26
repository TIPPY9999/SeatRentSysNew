<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const staffList = ref<any[]>([])
const searchText = ref('')
const loading = ref(false)

const fetchHistory = async () => {
  try {
    loading.value = true
    const res = await axios.get('http://localhost:8080/api/maintenance/staff/inactive')
    staffList.value = res.data
  } catch (error) {
    alert('無法載入歷史紀錄')
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() => {
  const key = searchText.value.trim().toLowerCase()
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
            <router-link to="/staff-list" class="btn btn-outline-secondary btn-sm"
              >返回列表</router-link
            >
          </div>
        </div>
      </div>
    </section>
    <section class="content">
      <div class="container-fluid">
        <div class="card card-outline card-info">
          <div class="card-header">
            <input v-model="searchText" class="form-control w-25" placeholder="快速搜尋..." />
          </div>
          <div class="card-body p-0">
            <table class="table table-bordered table-striped jy-table">
              <thead>
                <tr>
                  <th>ID</th>
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
                  <td colspan="5" class="text-center">無資料</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
