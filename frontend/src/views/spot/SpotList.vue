<template>
  <div class="content-wrapper" style="min-height: 100vh;">
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6 d-flex align-items-center">
            <h1 class="mb-0">租借點列表</h1>
          </div>

          <div class="col-sm-6 text-right">
            <!-- 新增按鈕 -->
            <router-link to="/spot/add" class="btn btn-success btn-sm">
              <i class="fas fa-plus mr-1"></i>新增租借點
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="wrapper">
          <div class="table-wrapper">
            <div class="table-title">租借點列表</div>

            <table class="jy-table table table-striped table-hover table-sm">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>代號</th>
                  <th>名稱</th>
                  <th>地址</th>
                  <th>狀態</th>
                  <th style="width: 220px;">操作</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="spotList.length === 0">
                  <td colspan="6" class="text-center">目前沒有租借點資料。</td>
                </tr>
                
                <tr v-for="spot in spotList" :key="spot.spotId">
                  <td>{{ spot.spotId }}</td>
                  <td>{{ spot.spotCode }}</td>
                  <td>{{ spot.spotName }}</td>
                  <td>{{ spot.spotAddress }}</td>
                  <td>{{ spot.spotStatus }}</td>

                  <td>
                    <!-- 查看 (假設共用 Form 組件或有獨立 Detail 頁面) -->
                    <router-link :to="`/spot/view/${spot.spotId}`" class="btn btn-outline-primary btn-sm btn-gap">
                      查看
                    </router-link>

                    <!-- 修改 -->
                    <router-link :to="`/spot/edit/${spot.spotId}`" class="btn btn-outline-info btn-sm btn-gap">
                      修改
                    </router-link>

                    <!-- 刪除 -->
                    <button @click="deleteSpot(spot.spotId)" class="btn btn-outline-danger btn-sm">
                      刪除
                    </button>
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

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const spotList = ref([]);

onMounted(async () => {
  fetchSpots();
});

const fetchSpots = async () => {
  try {
    const response = await axios.get('/spot/list');
    spotList.value = response.data;
  } catch (error) {
    console.error('Error fetching spots:', error);
  }
};

const deleteSpot = async (id) => {
  if (!confirm('確定要刪除嗎？')) return;
  
  try {
    const params = new URLSearchParams();
    params.append('spotId', id);
    await axios.post('/spot/delete', params);
    // 重新整理列表
    fetchSpots();
  } catch (error) {
    console.error('Delete failed:', error);
  }
};
</script>

<style scoped>
.table td, .table th { vertical-align: middle; }
.btn-gap { margin-right: 6px; }
</style>