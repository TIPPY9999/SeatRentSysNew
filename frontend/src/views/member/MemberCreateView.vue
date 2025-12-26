<template>
  <div class="container">
    <h2>新增會員</h2>

    <!-- 錯誤訊息 -->
    <p v-if="errorMsg" style="color:red; font-weight:bold; text-align:center;">
      {{ errorMsg }}
    </p>

    <!-- 成功訊息 -->
    <p v-if="successMsg" class="msg">
      {{ successMsg }}
    </p>

    <form @submit.prevent="submitCreate" autocomplete="off">
      <label>帳號</label>
      <input type="text" v-model="member.memUsername" autocomplete="new-username" required />

      <label>密碼</label>
      <input type="password" v-model="member.memPassword" autocomplete="new-password" required />

      <label>姓名</label>
      <input type="text" v-model="member.memName" required />

      <label>信箱</label>
      <input type="text" v-model="member.memEmail" required />

      <label>手機</label>
      <input type="text" v-model="member.memPhone" required />

      <label>發票載具</label>
      <input type="text" v-model="member.memInvoice" />

      <button type="submit">確認新增</button>
    </form>

    <a class="home-btn" @click.prevent="goBack">回會員列表</a>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "MemberCreateView",
  data() {
    return {
      member: {
        memUsername: "",
        memPassword: "",
        memName: "",
        memEmail: "",
        memPhone: "",
        memInvoice: "",
        memStatus: 1,
        memLevel: 1  
      },
      errorMsg: "",
      successMsg: ""
    };
  },
  methods: {
    submitCreate() {
      axios
        .post("http://localhost:8080/members", {
          ...this.member,
          memStatus: 1,
          memLevel: 1
          })
          .then(res => {
            alert(res.data); // 會員新增成功
            this.$router.push("/admin/members");
          })
          .catch(err => {
            alert(err.response?.data || "新增失敗");
          });
    },
    goBack() {
      this.$router.push("/admin/members");
    }
  }
};
</script>

<style scoped>

.container {
  width: 380px;
  margin: 40px auto;
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.15);
  text-align: center;
}

h2 {
  color: #3366cc;
  margin-bottom: 10px;
}

.msg {
  color: green;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

label {
  display: block;
  text-align: left;
  margin-bottom: 4px;
  font-weight: bold;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
  height: 32px;
  box-sizing: border-box;
  margin-bottom: 12px;
  font-size: 14px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #4d88ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 5px;
}

button:hover {
  background-color: #3366cc;
}

.home-btn {
  display: block;
  margin-top: 15px;
  text-decoration: none;
  color: #3366cc;
  font-size: 15px;
  cursor: pointer;
}

.home-btn:hover {
  text-decoration: underline;
}
</style>