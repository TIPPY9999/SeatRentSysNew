<template>
  <div class="spot-form container-fluid">
    <div class="card card-primary card-outline mt-3">
      <div class="card-header">
        <h3 class="card-title">{{ isEdit ? '編輯景點' : '新增景點' }}</h3>
      </div>

      <form @submit.prevent="saveSpot">
        <div class="card-body">
          <div class="form-group row">
            <label class="col-sm-2 col-form-label">代碼 (Code)</label>
            <div class="col-sm-10">
              <input
                v-model="formData.spotCode"
                type="text"
                class="form-control"
                required
                :disabled="isEdit"
                placeholder="例如: TP001"
              />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">名稱 (Name)</label>
            <div class="col-sm-10">
              <input
                v-model="formData.spotName"
                type="text"
                class="form-control"
                required
                placeholder="例如: 台北車站大廳點"
              />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">地址 (Address)</label>
            <div class="col-sm-10">
              <input v-model="formData.spotAddress" type="text" class="form-control" required />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">狀態 (Status)</label>
            <div class="col-sm-10">
              <select v-model="formData.spotStatus" class="form-control" required>
                <option value="" disabled>請選擇狀態</option>
                <option value="啟用">啟用</option>
                <option value="停用">停用</option>
              </select>
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">Merchant ID</label>
            <div class="col-sm-10">
              <input
                v-model.number="formData.merchantId"
                type="number"
                class="form-control"
                required
              />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">緯度 (Latitude)</label>
            <div class="col-sm-10">
              <input v-model="formData.latitude" type="number" step="any" class="form-control" />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">經度 (Longitude)</label>
            <div class="col-sm-10">
              <input v-model="formData.longitude" type="number" step="any" class="form-control" />
            </div>
          </div>
        </div>

        <div class="card-footer">
          <button type="submit" class="btn btn-primary mr-2">儲存</button>
          <button type="button" class="btn btn-secondary" @click="$router.push('/admin/spot/list')">
            取消
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const formData = ref({
  spotCode: '',
  spotName: '',
  spotAddress: '',
  spotStatus: '',
  merchantId: null,
  latitude: null,
  longitude: null,
})

const isEdit = computed(() => !!route.params.id)

onMounted(async () => {
  if (isEdit.value) {
    try {
      // [修正] 加上 /api
      const response = await axios.get('/api/spot/update', { params: { spotId: route.params.id } })
      formData.value = response.data
    } catch (error) {
      console.error('Error fetching spot data:', error)
    }
  }
})

const saveSpot = async () => {
  if (
    !formData.value.spotCode?.trim() ||
    !formData.value.spotName?.trim() ||
    !formData.value.spotAddress?.trim() ||
    !formData.value.spotStatus?.trim() ||
    !formData.value.merchantId
  ) {
    alert('請檢查所有必填欄位 (代碼、名稱、地址、狀態、Merchant ID) 是否皆已填寫！')
    return
  }

  if (isEdit.value) {
    formData.value.spotId = route.params.id
  }

  try {
    if (isEdit.value) {
      await axios.post('/api/spot/update', formData.value)
    } else {
      await axios.post('/api/spot/insert', formData.value)
    }
    // [修正] 加上 /admin
    router.push('/admin/spot/list')
  } catch (error) {
    console.error('Save failed:', error)
    alert('儲存失敗')
  }
}
</script>

<style scoped>
/* 讓卡片有點陰影比較好看 */
.card {
  box-shadow:
    0 0 1px rgba(0, 0, 0, 0.125),
    0 1px 3px rgba(0, 0, 0, 0.2);
}
</style>
