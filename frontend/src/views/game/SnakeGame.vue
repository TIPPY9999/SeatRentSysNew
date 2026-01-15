<template>
  <div class="game-wrapper">
    <div class="status-bar">
      <h2>🐍 貪吃蛇挑戰賽</h2>
      <div class="info">
        <span>得分: <strong>{{ score }}</strong></span>
        <span>難度速度: <strong>{{ gameSpeed }}ms</strong></span>
      </div>
    </div>

    <div class="canvas-container">
      <canvas ref="gameCanvas" width="400" height="400"></canvas>
      
      <div v-if="gameOver" class="overlay">
        <h3>遊戲結束!</h3>
        <p>最終分數: {{ score }}</p>
        <button @click="startGame" class="btn-primary">再玩一次</button>
        <button @click="uploadScore" class="btn-secondary" :disabled="isUploaded">
          {{ isUploaded ? '已上傳' : '上傳分數' }}
        </button>
      </div>

      <div v-if="!isGameRunning && !gameOver" class="overlay">
        <button @click="startGame" class="btn-start">開始遊戲</button>
      </div>
    </div>
    
    <div class="instructions">
      使用方向鍵 ↑ ↓ ← → 控制移動
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';

// 遊戲狀態
const gameCanvas = ref(null);
const score = ref(0);
const gameSpeed = ref(150);
const isGameRunning = ref(false);
const gameOver = ref(false);
const isUploaded = ref(false);

let ctx = null;
let snake = [];
let food = { x: 5, y: 5 };
let direction = { x: 1, y: 0 };
let nextDirection = { x: 1, y: 0 };
const gridSize = 20;
const tileCount = 20;

// 初始化遊戲
const startGame = () => {
  score.value = 0;
  gameSpeed.value = 150;
  direction = { x: 1, y: 0 };
  nextDirection = { x: 1, y: 0 };
  snake = [
    { x: 10, y: 10 },
    { x: 9, y: 10 },
    { x: 8, y: 10 }
  ];
  gameOver.value = false;
  isUploaded.value = false;
  isGameRunning.value = true;
  spawnFood();
  gameLoop();
};

// 遊戲主循環 (使用 setTimeout 達成動態變速)
const gameLoop = () => {
  if (!isGameRunning.value) return;

  setTimeout(() => {
    update();
    draw();
    gameLoop();
  }, gameSpeed.value);
};

// 邏輯更新
const update = () => {
  direction = nextDirection;
  const head = { x: snake[0].x + direction.x, y: snake[0].y + direction.y };

  // 1. 碰撞偵測 (牆壁與身體)
  if (head.x < 0 || head.x >= tileCount || head.y < 0 || head.y >= tileCount || 
      snake.some(seg => seg.x === head.x && seg.y === head.y)) {
    endGame();
    return;
  }

  snake.unshift(head);

  // 2. 吃到食物偵測
  if (head.x === food.x && head.y === food.y) {
    score.value += 10;
    // 每得 50 分加速一次
    if (score.value % 50 === 0 && gameSpeed.value > 60) {
      gameSpeed.value -= 15;
    }
    spawnFood();
  } else {
    snake.pop();
  }
};

// 繪製畫面
const draw = () => {
  // 背景
  ctx.fillStyle = "#2c3e50";
  ctx.fillRect(0, 0, 400, 400);

  // 食物
  ctx.fillStyle = "#e63946";
  ctx.shadowBlur = 10;
  ctx.shadowColor = "#e63946";
  ctx.fillRect(food.x * gridSize + 2, food.y * gridSize + 2, gridSize - 4, gridSize - 4);

  // 蛇
  ctx.shadowBlur = 0;
  snake.forEach((part, index) => {
    ctx.fillStyle = index === 0 ? "#ffffff" : "#42b983"; // 蛇頭是白色的
    ctx.fillRect(part.x * gridSize + 1, part.y * gridSize + 1, gridSize - 2, gridSize - 2);
  });
};

const spawnFood = () => {
  food = {
    x: Math.floor(Math.random() * tileCount),
    y: Math.floor(Math.random() * tileCount)
  };
  // 防止食物長在蛇身上
  if (snake.some(seg => seg.x === food.x && seg.y === food.y)) spawnFood();
};

const handleKeydown = (e) => {
  switch (e.key) {
    case 'ArrowUp': if (direction.y === 0) nextDirection = { x: 0, y: -1 }; break;
    case 'ArrowDown': if (direction.y === 0) nextDirection = { x: 0, y: 1 }; break;
    case 'ArrowLeft': if (direction.x === 0) nextDirection = { x: -1, y: 0 }; break;
    case 'ArrowRight': if (direction.x === 0) nextDirection = { x: 1, y: 0 }; break;
  }
};

const endGame = () => {
  isGameRunning.value = false;
  gameOver.value = true;
};

const uploadScore = async () => {
  try {
    await axios.post('http://localhost:8080/api/game/score', { score: score.value });
    isUploaded.value = true;
    alert('分數上傳成功！');
  } catch (error) {
    alert('上傳失敗，請檢查後端連線');
  }
};

onMounted(() => {
  ctx = gameCanvas.value.getContext('2d');
  window.addEventListener('keydown', handleKeydown);
  draw(); // 畫出初始背景
});

onUnmounted(() => window.removeEventListener('keydown', handleKeydown));
</script>

<style scoped>
.game-wrapper { display: flex; flex-direction: column; align-items: center; background: #ecf0f1; padding: 20px; border-radius: 15px; }
.status-bar { width: 400px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.canvas-container { position: relative; border: 8px solid #34495e; border-radius: 5px; }
canvas { display: block; background: #2c3e50; }
.overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.7); display: flex; flex-direction: column; justify-content: center; align-items: center; color: white; }
.btn-start, .btn-primary { padding: 12px 30px; font-size: 1.2rem; background: #42b983; color: white; border: none; border-radius: 25px; cursor: pointer; margin: 10px; }
.btn-secondary { padding: 8px 20px; background: #3498db; color: white; border: none; border-radius: 20px; cursor: pointer; }
.btn-secondary:disabled { background: #95a5a6; }
.instructions { margin-top: 15px; color: #7f8c8d; font-size: 0.9rem; }
</style>