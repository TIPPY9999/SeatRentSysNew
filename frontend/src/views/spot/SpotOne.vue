<template>
  <div class="content-wrapper" style="min-height: 100vh;">
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>景點詳細資料</h1>
          </div>
          <div class="col-sm-6">
            <ol class="breadcrumb float-sm-right">
              <li class="breadcrumb-item"><router-link to="/spot/list">Home</router-link></li>
              <li class="breadcrumb-item active">Spot Detail</li>
            </ol>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div v-if="loading" class="alert alert-info">
          載入中...
        </div>
        <div v-else-if="!spot" class="alert alert-warning">
          找不到資料
        </div>
        <div v-else class="card card-primary card-outline">
          <div class="card-body">
            <table class="table table-bordered table-striped">
              <tbody>
                <tr>
                  <th style="width: 200px">ID</th>
                  <td>{{ spot.spotId }}</td>
                </tr>
                <tr>
                  <th>代碼 (Code)</th>
                  <td>{{ spot.spotCode }}</td>
                </tr>
                <tr>
                  <th>名稱 (Name)</th>
                  <td>{{ spot.spotName }}</td>
                </tr>
                <tr>
                  <th>地址 (Address)</th>
                  <td>{{ spot.spotAddress }}</td>
                </tr>
                <tr>
                  <th>狀態 (Status)</th>
                  <td>{{ spot.spotStatus }}</td>
                </tr>
                <tr>
                  <th>Merchant ID</th>
                  <td>{{ spot.merchantId }}</td>
                </tr>
                <tr>
                  <th>緯度 (Latitude)</th>
                  <td>{{ spot.latitude }}</td>
                </tr>
                <tr>
                  <th>經度 (Longitude)</th>
                  <td>{{ spot.longitude }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="card-footer">
            <router-link to="/spot/list" class="btn btn-secondary">回列表</router-link>
            <router-link :to="`/spot/edit/${spot.spotId}`" class="btn btn-info float-right">編輯</router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const spot = ref(null);
const loading = ref(true);

onMounted(async () => {
  const id = route.params.id;
  if (id) {
    try {
      const response = await axios.get('/spot/one', { params: { spotId: id } });
      spot.value = response.data;
    } catch (error) {
      console.error('Error fetching spot:', error);
    }
  }
  loading.value = false;
});
</script>

<style scoped>
/* 配合 AdminLTE 樣式，可依需求調整 */
</style>