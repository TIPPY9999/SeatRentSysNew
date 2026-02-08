 <script setup>
 import { ref, onMounted, computed } from 'vue';
 
 const allData = ref([]);
 const isLoading = ref(true);
 const error = ref(null);
 
 const currentPage = ref(1);
 const itemsPerPage = 10;
 
 // 從指定的 URL 獲取資料
 async function fetchDataFromUrl(url) {
   const response = await fetch(url);
   if (!response.ok) {
     throw new Error(`無法從 ${url} 獲取資料: ${response.status} ${response.statusText}`);
   }
   return await response.json();
 }
 
 // 獲取所有景點和餐廳資料
 async function fetchAllData() {
   isLoading.value = true;
   error.value = null;
 
   try {
     // 不再需要獲取 Token
     const restaurantUrl = 'https://tdx.transportdata.tw/api/basic/v2/Tourism/Restaurant?%24format=JSON';
     const scenicSpotUrl = 'https://tdx.transportdata.tw/api/basic/v2/Tourism/ScenicSpot?%24format=JSON';
 
     // 平行獲取兩份資料
     const [restaurants, scenicSpots] = await Promise.all([
       fetchDataFromUrl(restaurantUrl),
       fetchDataFromUrl(scenicSpotUrl)
     ]);
 
     // 格式化並合併資料
     const formattedRestaurants = restaurants.map(item => ({
       id: `res_${item.RestaurantID}`,
       name: item.RestaurantName,
       description: item.Description || '暫無描述',
       pictureUrl: item.Picture?.PictureUrl1,
       address: item.Address,
       type: '餐廳'
     }));
 
     const formattedScenicSpots = scenicSpots.map(item => ({
       id: `spot_${item.ScenicSpotID}`,
       name: item.ScenicSpotName,
       description: item.DescriptionDetail || '暫無描述',
       pictureUrl: item.Picture?.PictureUrl1,
       address: item.Address,
       type: '景點'
     }));
 
     allData.value = [...formattedRestaurants, ...formattedScenicSpots]
       .sort((a, b) => a.name.localeCompare(b.name)); // 按名稱排序
 
   } catch (e) {
     error.value = e.message;
     console.error(e);
   } finally {
     isLoading.value = false;
   }
 }
 
 // 計算總頁數
 const totalPages = computed(() => {
   return Math.ceil(allData.value.length / itemsPerPage);
 });
 
 // 計算當前頁要顯示的資料
 const paginatedData = computed(() => {
   const start = (currentPage.value - 1) * itemsPerPage;
   const end = start + itemsPerPage;
   return allData.value.slice(start, end);
 });
 
 // 前往下一頁
 function nextPage() {
   if (currentPage.value < totalPages.value) {
     currentPage.value++;
   }
 }
 
 // 回到上一頁
 function prevPage() {
   if (currentPage.value > 1) {
     currentPage.value--;
   }
 }
 
 // 元件掛載時自動獲取資料
 onMounted(() => {
   fetchAllData();
 });
 </script>
 
 <template>
   <div class="container-fluid pt-3">
     <div class="row">
       <div class="col-12">
         <div class="card">
           <div class="card-header">
             <h3 class="card-title">探索周邊景點與餐廳</h3>
             <div class="card-tools">
               <!-- 可以放過濾或搜尋工具 -->
             </div>
           </div>
           <div class="card-body">
             <!-- 載入中訊息 -->
             <div v-if="isLoading" class="text-center py-5">
               <div class="spinner-border text-primary" role="status">
                 <span class="visually-hidden">Loading...</span>
               </div>
               <p class="mt-2">資料載入中...</p>
             </div>
 
             <!-- 錯誤訊息 -->
             <div v-else-if="error" class="alert alert-danger">
               <h4>資料載入失敗</h4>
               <p>{{ error }}</p>
               <p>
                 **提醒**: 請確認您的網路連線是否正常。
               </p>
             </div>
 
             <!-- 資料列表 -->
             <div v-else-if="paginatedData.length > 0">
               <div class="row">
                 <div v-for="item in paginatedData" :key="item.id" class="col-md-6 col-lg-4 mb-4">
                   <div class="card h-100">
                     <img
                       :src="item.pictureUrl || 'https://picsum.photos/800?random=3'"
                       class="card-img-top"
                       :alt="item.name"
                       style="height: 200px; object-fit: cover;"
                     />
                     <div class="card-body d-flex flex-column">
                       <h5 class="card-title">{{ item.name }} <span class="badge" :class="item.type === '景點' ? 'bg-success' : 'bg-info'">{{ item.type }}</span></h5>
                       <p class="card-text text-muted flex-grow-1">{{ item.description.substring(0, 80) }}...</p>
                       <p class="card-text"><small class="text-muted">{{ item.address }}</small></p>
                     </div>
                   </div>
                 </div>
               </div>
             </div>
 
             <!-- 無資料訊息 -->
             <div v-else class="text-center py-5">
               <p>沒有找到任何景點或餐廳資料。</p>
             </div>
           </div>
           <div class="card-footer clearfix">
             <!-- 分頁控制項 -->
             <nav aria-label="Page navigation">
               <ul class="pagination pagination-sm m-0 float-right">
                 <li class="page-item" :class="{ disabled: currentPage === 1 }">
                   <a class="page-link" href="#" @click.prevent="prevPage">« 上一頁</a>
                 </li>
                 <li class="page-item active">
                   <span class="page-link">{{ currentPage }} / {{ totalPages }}</span>
                 </li>
                 <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                   <a class="page-link" href="#" @click.prevent="nextPage">下一頁 »</a>
                 </li>
               </ul>
             </nav>
           </div>
         </div>
       </div>
     </div>
   </div>
 </template>
 
 <style scoped>
 .card-title .badge {
   font-size: 0.8rem;
   vertical-align: middle;
 }
 .card-img-top {
   border-bottom: 1px solid #dee2e6;
 }
 </style>
 