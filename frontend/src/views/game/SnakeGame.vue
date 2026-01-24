<template>
  <div class="container-fluid p-4">
    <div class="row justify-content-center">
      <div class="col-md-8 col-lg-6">
        <div class="card shadow-lg border-0">
          <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center py-3">
            <h5 class="mb-0"><i class="bi bi-controller me-2"></i>蛇蛇賺點數 (100分 = 1點)</h5>
            
            <span class="badge bg-warning text-dark px-3 py-2">
              <template v-if="memberId">
                <i class="bi bi-person-fill me-1"></i>{{ memName }} 
                <span class="mx-2 opacity-50">|</span>
                <i class="bi bi-coin me-1"></i>{{ currentPoints }} Pts
                <span class="mx-2 opacity-50">|</span>
                ID: {{ memberId }}
              </template>
              <template v-else>
                <router-link to="/login" class="text-dark text-decoration-none">⚠️ 未登入 (點我登入)</router-link>
              </template>
            </span>
          </div>

          <div class="card-body text-center bg-light">
            <nav aria-label="breadcrumb" class="mb-3 text-start">
              <router-link to="/mall" class="text-decoration-none text-secondary small">
                <i class="bi bi-arrow-left"></i> 回點數商城
              </router-link>
            </nav>

            <div class="row mb-3">
              <div class="col-6">
                <div class="p-2 border rounded bg-white shadow-sm">
                  <small class="text-muted d-block">目前得分</small>
                  <h3 class="text-dark mb-0 fw-bold">{{ score }}</h3>
                </div>
              </div>
              <div class="col-6">
                <div class="p-2 border rounded bg-white shadow-sm">
                  <small class="text-muted d-block">可換點數</small>
                  <h3 class="text-success mb-0 fw-bold">{{ Math.floor(score / 100) }}</h3>
                </div>
              </div>
            </div>

            <div class="canvas-wrapper position-relative d-inline-block shadow">
              <canvas ref="gameCanvas" width="400" height="400" class="rounded"></canvas>
              
              <div v-if="!isGameRunning && !gameOver" class="game-overlay d-flex flex-column justify-content-center align-items-center">
                <button @click="startGame" class="btn btn-success btn-lg px-5 rounded-pill shadow-lg">開始挑戰</button>
              </div>

              <div v-if="gameOver" class="game-overlay d-flex flex-column justify-content-center align-items-center text-white p-4">
                <h2 class="display-5 fw-bold mb-2">GAME OVER</h2>
                <p class="mb-4">本次得分 {{ score }}，可換取 <span class="text-warning fw-bold">{{ Math.floor(score / 100) }}</span> 點數</p>
                
                <div class="d-grid gap-2 col-10 mx-auto">
                  <button 
                    @click="uploadScore" 
                    class="btn btn-warning btn-lg fw-bold" 
                    :disabled="isUploaded || Math.floor(score / 100) < 1"
                  >
                    {{ isUploaded ? '✅ 點數已入帳' : (Math.floor(score / 100) < 1 ? '分數不足兌換' : '🎁 領取點數') }}
                  </button>
                  <button @click="startGame" class="btn btn-outline-light btn-sm mt-2">重新開始</button>
                </div>
              </div>
            </div>

            <div class="mt-4 p-3 bg-white rounded border text-start">
              <h6 class="small fw-bold text-primary"><i class="bi bi-info-circle me-1"></i> 遊戲規則：</h6>
              <ul class="list-unstyled mb-0 text-muted" style="font-size: 0.8rem;">
                <li>• 方向鍵控制移動，撞牆或撞到自己即結束。</li>
                <li>• <b>100 分 = 1 點</b> 會員點數（採無條件捨去）。</li>
                <li>• 領取點數前請確保已登入會員帳號。</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useMemberAuthStore } from '@/stores/memberAuth'; 
import axios from 'axios';
import Swal from 'sweetalert2';

const router = useRouter();
const memberAuthStore = useMemberAuthStore();

// 狀態變數
const gameCanvas = ref(null);
const score = ref(0);
const gameSpeed = ref(150);
const isGameRunning = ref(false);
const gameOver = ref(false);
const isUploaded = ref(false);

// 會員響應式資料 (修正變數名稱一致性)
const memberId = computed(() => memberAuthStore.member?.memId);
const memName = computed(() => memberAuthStore.member?.memName || '訪客');
const currentPoints = computed(() => memberAuthStore.member?.memPoints || 0);

// 遊戲邏輯變數
let ctx = null;
let snake = [];
let food = { x: 5, y: 5 };
let direction = { x: 1, y: 0 };
let nextDirection = { x: 1, y: 0 };
const gridSize = 20;

// 遊戲主迴圈
const startGame = () => {
  score.value = 0;
  gameSpeed.value = 150;
  direction = { x: 1, y: 0 };
  nextDirection = { x: 1, y: 0 };
  snake = [{ x: 10, y: 10 }, { x: 9, y: 10 }, { x: 8, y: 10 }];
  gameOver.value = false;
  isUploaded.value = false;
  isGameRunning.value = true;
  spawnFood();
  gameLoop();
};

const gameLoop = () => {
  if (!isGameRunning.value) return;
  setTimeout(() => {
    update();
    draw();
    gameLoop();
  }, gameSpeed.value);
};

const update = () => {
  direction = nextDirection;
  const head = { x: snake[0].x + direction.x, y: snake[0].y + direction.y };

  if (head.x < 0 || head.x >= 20 || head.y < 0 || head.y >= 20 ||
      snake.some(s => s.x === head.x && s.y === head.y)) {
    isGameRunning.value = false;
    gameOver.value = true;
    return;
  }

  snake.unshift(head);
  if (head.x === food.x && head.y === food.y) {
    score.value += 20;
    if (score.value % 100 === 0 && gameSpeed.value > 60) gameSpeed.value -= 15;
    spawnFood();
  } else {
    snake.pop();
  }
};

const draw = () => {
  if (!ctx) return;
  ctx.fillStyle = "#1e272e";
  ctx.fillRect(0, 0, 400, 400);

  ctx.fillStyle = "#ff4757";
  ctx.beginPath();
  ctx.arc(food.x * gridSize + 10, food.y * gridSize + 10, 8, 0, Math.PI * 2);
  ctx.fill();

  snake.forEach((p, i) => {
    ctx.fillStyle = i === 0 ? "#ffffff" : "#2ecc71";
    ctx.fillRect(p.x * gridSize + 1, p.y * gridSize + 1, gridSize - 2, gridSize - 2);
  });
};

const spawnFood = () => {
  food = { x: Math.floor(Math.random() * 20), y: Math.floor(Math.random() * 20) };
  if (snake.some(s => s.x === food.x && s.y === food.y)) spawnFood();
};

const handleKeyDown = (e) => {
  const key = e.key;
  if (key === 'ArrowUp' && direction.y === 0) nextDirection = { x: 0, y: -1 };
  if (key === 'ArrowDown' && direction.y === 0) nextDirection = { x: 0, y: 1 };
  if (key === 'ArrowLeft' && direction.x === 0) nextDirection = { x: -1, y: 0 };
  if (key === 'ArrowRight' && direction.x === 0) nextDirection = { x: 1, y: 0 };
};

const uploadScore = async () => {
  if (!memberId.value) {
    sessionStorage.setItem('pendingSnakeScore', score.value);
    const result = await Swal.fire({
      title: '請先登入',
      text: `您獲得了 ${score.value} 分，登入後即可換取點數！`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: '去登入',
      cancelButtonText: '下次再說'
    });

    if (result.isConfirmed) {
      router.push({ path: '/login', query: { redirect: '/snake' } });
    }
    return;
  }

  const points = Math.floor(score.value / 100);
  
  try {
    const res = await axios.post('http://localhost:8080/api/game/add-points', {
      memberId: memberId.value,
      points: points
    });

    isUploaded.value = true;
    sessionStorage.removeItem('pendingSnakeScore');

    await Swal.fire({
      title: '兌換成功！',
      text: `已入帳 ${res.data.addPoints} 點！`,
      icon: 'success'
    });

    // ✅ 自動刷新全站點數
    if (typeof memberAuthStore.refreshPoints === 'function') {
      await memberAuthStore.refreshPoints();
    }
  } catch (err) {
    console.error("上傳失敗", err);
    Swal.fire('系統錯誤', '無法連接到伺服器', 'error');
  }
};

onMounted(() => {
  ctx = gameCanvas.value.getContext('2d');
  window.addEventListener('keydown', handleKeyDown);

  const savedScore = sessionStorage.getItem('pendingSnakeScore');
  if (savedScore && memberId.value) {
    score.value = parseInt(savedScore);
    gameOver.value = true;
    isGameRunning.value = false;
    Swal.fire('歡迎回來', `已偵測到您剛才獲得的 ${score.value} 分，快領取點數吧！`, 'info');
  }
  
  draw();
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
});
</script>

<style scoped>
.game-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4px);
  z-index: 5;
  border-radius: 8px;
}
canvas { 
  background: #1e272e; 
  display: block; 
  border: 4px solid #34495e;
}
.shadow { box-shadow: 0 10px 30px rgba(0,0,0,0.3) !important; }
</style>