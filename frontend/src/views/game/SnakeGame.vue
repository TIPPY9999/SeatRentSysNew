<template>
  <div class="container-fluid p-4">
    <div class="row justify-content-center">
      <div class="col-md-8 col-lg-6">
        <div class="card shadow-lg border-0">
          <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center py-3">
            <h5 class="mb-0"><i class="bi bi-controller me-2"></i>蛇蛇賺點數 (100分 = 1點)</h5>
       <span class="badge bg-warning text-dark">
          <template v-if="memberId">會員ID: {{ memberId }}</template>
          <template v-else>
          <router-link to="/login" class="text-dark text-decoration-none">⚠️ 未登入 (點我登入)</router-link>
          </template>
        </span>
        <nav aria-label="breadcrumb" class="mb-3">
          <router-link to="/mall" class="text-decoration-none text-secondary">
            <h5 class="mb-0"><i class="bi bi-house-door"></i> 回點數商城</h5>
          </router-link>
        </nav>

          </div>

          <div class="card-body text-center bg-light">
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
                <button @click="startGame" class="btn btn-success btn-lg px-5 rounded-pill shadow">開始挑戰</button>
              </div>

              <div v-if="gameOver" class="game-overlay d-flex flex-column justify-content-center align-items-center text-white p-4">
                <h2 class="display-5 fw-bold mb-2">GAME OVER</h2>
                <p class="mb-4">本次得分 {{ score }}，可換取 <span class="text-warning fw-bold">{{ Math.floor(score / 100) }}</span> 點數</p>
                
                <div class="d-grid gap-2 col-10 mx-auto">
                  <button @click="uploadScore" class="btn btn-warning btn-lg" :disabled="isUploaded || Math.floor(score / 100) < 1">
                    {{ isUploaded ? '點數已入帳' : (Math.floor(score / 100) < 1 ? '分數不足兌換' : '領取點數') }}
                  </button>
                  <button @click="startGame" class="btn btn-outline-light btn-sm">重新開始</button>
                </div>
              </div>
            </div>

            <div class="mt-4 p-3 bg-white rounded border text-start">
              <h6 class="small fw-bold text-primary"><i class="bi bi-info-circle me-1"></i> 遊戲規則：</h6>
              <ul class="list-unstyled mb-0 x-small text-muted" style="font-size: 0.8rem;">
                <li>• 使用鍵盤 <b>方向鍵</b> 控制蛇移動</li>
                <li>• 每得 50 分蛇的移動速度會加快</li>
                <li>• 撞到牆壁或自己則遊戲結束</li>
                <li>• 遊戲得分達到 <b>100 分</b> 即可兌換 <b>1 點</b> 會員點數。</li>
                <li>• 點數採無條件捨去（例：190 分兌換 1 點）。</li>
                <li>• 撞牆或吃到自己時遊戲結束，結算當前分數。</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const gameCanvas = ref(null);
const score = ref(0);
const gameSpeed = ref(150);
const isGameRunning = ref(false);
const gameOver = ref(false);
const isUploaded = ref(false);
const memberId = ref(localStorage.getItem('memberId'));
// 增加一個更新會員資訊的方法
const updateMemberInfo = () => {
  const storedId = localStorage.getItem('memberId');
  memberId.value = storedId;
  console.log("當前登入會員:", storedId);
};

let ctx = null;
let snake = [];
let food = { x: 5, y: 5 };
let direction = { x: 1, y: 0 };
let nextDirection = { x: 1, y: 0 };
const gridSize = 20;

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
    score.value += 20; // 吃到一個食物加 20 分
    if (score.value % 50 === 0 && gameSpeed.value > 50) gameSpeed.value -= 15;
    spawnFood();
  } else {
    snake.pop();
  }
};

const draw = () => {
  if (!ctx) return;
  ctx.fillStyle = "#1e272e";
  ctx.fillRect(0, 0, 400, 400);

  // 食物
  ctx.fillStyle = "#ff4757";
  ctx.fillRect(food.x * gridSize + 2, food.y * gridSize + 2, gridSize - 4, gridSize - 4);

  // 蛇
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
  if (e.key === 'ArrowUp' && direction.y === 0) nextDirection = { x: 0, y: -1 };
  if (e.key === 'ArrowDown' && direction.y === 0) nextDirection = { x: 0, y: 1 };
  if (e.key === 'ArrowLeft' && direction.x === 0) nextDirection = { x: -1, y: 0 };
  if (e.key === 'ArrowRight' && direction.x === 0) nextDirection = { x: 1, y: 0 };
};

const uploadScore = async () => {
  // 1. 檢查是否有登入
  const currentMemberId = localStorage.getItem('memberId');

  if (!currentMemberId) {
    // 未登入：將分數存入 SessionStorage (瀏覽器關閉前都在)
    sessionStorage.setItem('pendingSnakeScore', score.value);
    
    if (confirm(`您目前獲得 ${score.value} 分！登入後即可換取點數，是否前往登入？`)) {
      // 跳轉到登入頁，並帶上一個「回傳路徑」參數，方便登入後跳回來
      router.push({ path: '/login', query: { redirect: '/snake' } });
    }
    return;
  }

  // 2. 已登入：正常執行上傳
  const pointsToDeliver = Math.floor(score.value / 100);
  try {
    const res = await axios.post('http://localhost:8080/api/game/add-points', {
      memberId: currentMemberId,
      points: pointsToDeliver
    });
    isUploaded.value = true;
    alert(`兌換成功！目前帳戶點數：${res.data.currentPoints}`);
    sessionStorage.removeItem('pendingSnakeScore'); // 上傳完就清除暫存
  } catch (e) {
    alert("存檔失敗，請確認後端連線。");
  }
};
onMounted(() => {
  ctx = gameCanvas.value.getContext('2d');
  window.addEventListener('keydown', handleKeyDown);
  
  // 檢查是否有登入後跳回來的暫存分數
  const savedScore = sessionStorage.getItem('pendingSnakeScore');
  const currentMemberId = localStorage.getItem('memberId');

  if (savedScore && currentMemberId) {
    score.value = parseInt(savedScore);
    gameOver.value = true; // 直接進入結算畫面
    isGameRunning.value = false;
    alert(`歡迎回來！您剛才獲得的 ${score.value} 分現在可以領取了！`);
  }
  
  draw();
});
onMounted(() => {
  updateMemberInfo(); // 組件掛載時執行一次
  ctx = gameCanvas.value.getContext('2d');
});
onUnmounted(() => window.removeEventListener('keydown', handleKeyDown));
</script>

<style scoped>
.game-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(2px);
  z-index: 5;
}
canvas { background: #1e272e; display: block; }
.shadow { box-shadow: 0 10px 30px rgba(0,0,0,0.2) !important; }
</style>