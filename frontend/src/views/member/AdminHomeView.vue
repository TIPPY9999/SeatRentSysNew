<template>
  <div class="home">
    <h2>後台首頁</h2>

    <div class="cards">
      <div class="card" @click="$router.push('/admin/members/create')">
      <h3>會員管理</h3>
      <p>新增會員</p>
      </div>

      <div class="card">
        <h3>管理員管理</h3>
        <p>管理後台帳號</p>
      </div>

      <div class="card">
        <h3>租借紀錄</h3>
        <p>查看租借與歸還紀錄</p>
      </div>

      <div class="card">
        <h3>維護人員管理</h3>
        <p>管理後台帳號</p>
      </div>

      <div class="card">
        <h3>維修工單管理</h3>
        <p>管理後台帳號</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home h2 {
  margin-bottom: 20px;
}

.cards {
  display: flex;
  gap: 20px;
}

.card {
  background: #fff;
  width: 220px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0,0,0,0.1);
  cursor: pointer;
  user-select: none;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}
</style>