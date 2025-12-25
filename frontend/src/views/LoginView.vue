<template>
  <div class="page">
    <div class="login-wrapper">
      <!-- Logo -->
      <h1 class="logo">SeatRentSys</h1>

      <!-- Login Card -->
      <div class="login-box">
        <h2 class="title">會員登入</h2>

        <div class="form-group">
          <label>帳號</label>
          <input
            type="text"
            v-model="memUsername"
            placeholder="請輸入帳號"
          />
        </div>

        <div class="form-group">
          <label>密碼</label>
          <input
            type="password"
            v-model="memPassword"
            placeholder="請輸入密碼"
          />
        </div>

        <button class="login-btn" @click="login">
          登入
        </button>

        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <p v-if="successMsg" class="success">{{ successMsg }}</p>
      </div>

      <div class="footer">© 2025 SeatRentSys</div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "LoginView",
  data() {
    return {
      memUsername: "",
      memPassword: "",
      errorMsg: "",
      successMsg: ""
    };
  },
  methods: {
    login() {
      this.errorMsg = "";
      this.successMsg = "";

      axios
        .post("http://localhost:8080/login/member", {
          memUsername: this.memUsername,
          memPassword: this.memPassword
        })
        .then(() => {
          this.successMsg = "登入成功";
          this.$router.push("/admin");
        })
        .catch((err) => {
          if (err.response && err.response.data) {
            this.errorMsg = err.response.data;
          } else {
            this.errorMsg = "伺服器連線失敗";
          }
        });
    }
  }
};
</script>

<style scoped>
.page {
  width: 100%;
  min-height: 100vh;
  background-color: #f2f3f5;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow-x: hidden;
  color: #000;
}

.login-wrapper {
  text-align: center;
}

.logo {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 30px;
  letter-spacing: 2px;
}

.login-box {
  background-color: #ffffff;
  width: 460px;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  box-sizing: border-box; /* 🔥 防止撐破 */
}

.title {
  margin-bottom: 30px;
}

.form-group {
  text-align: left;
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 10px;
  font-size: 15px;
  border: 1px solid #ccc;
  box-sizing: border-box; /* 🔥 */
}

.login-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  background-color: #1677ff;
  color: #fff;
  border: none;
  cursor: pointer;
}

.login-btn:hover {
  background-color: #0958d9;
}

.error {
  margin-top: 15px;
  color: red;
  text-align: center;
}

.success {
  margin-top: 15px;
  color: green;
  text-align: center;
}

.footer {
  margin-top: 25px;
  color: #666;
  font-size: 14px;
}
</style>