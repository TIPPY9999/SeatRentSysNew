<template>
  <div class="container">
    <h2>修改會員資料</h2>

    <div v-if="errorMsg" class="error">
      {{ errorMsg }}
    </div>

    <div v-if="!member">
      尚未載入會員資料，請從會員列表點選修改。
    </div>

    <form v-else @submit.prevent="submitEdit">
      <input type="hidden" v-model="member.memId" />

      <label>帳號</label>
      <input type="text" v-model="member.memUsername" required />

      <label>密碼</label>
      <input type="password" v-model="newPassword" placeholder="不修改請留空" />

      <label>姓名</label>
      <input type="text" v-model="member.memName" required />

      <label>信箱</label>
      <input type="text" v-model="member.memEmail" required />

      <label>電話</label>
      <input type="text" v-model="member.memPhone" required />

      <label>狀態 (1正常 / 0停權)</label>
      <input type="number" v-model="member.memStatus" />

      <label>總積分</label>
      <input type="number" v-model="member.memPoints" />

      <label>違規次數</label>
      <input type="number" v-model="member.memViolation" />

      <label>會員等級</label>
      <input type="number" v-model="member.memLevel" />

      <label>發票載具</label>
      <input
        type="text"
        v-model="member.memInvoice"
        placeholder="未提供"
      />

      <button type="submit" class="submit-btn">確認修改</button>

      <button type="button" class="cancel-btn" @click="goBack">
        取消
      </button>
    </form>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "MemberEditView",
  data() {
    return {
      member: null,
      newPassword: "",
      errorMsg: ""
    };
  },
  mounted() {
    this.fetchMember();
  },
  methods: {
    fetchMember() {
      const id = this.$route.params.id;
      axios
        .get(`http://localhost:8080/members/find?memId=${id}`)
        .then(res => {
          this.member = res.data;
        })
        .catch(() => {
          this.errorMsg = "載入會員資料失敗";
        });
    },

    submitEdit() {
  const payload = {
    ...this.member,
    memPassword: this.newPassword || ""
  };

  axios
    .post("http://localhost:8080/members/update", payload)
    .then(() => {
      alert("會員修改成功");
      this.$router.push("/admin/members");
    })
    .catch(err => {
      console.error(err);
      this.errorMsg = "修改失敗";
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
  margin: 30px auto;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.15);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #3366cc;
}

label {
  font-weight: bold;
  margin-top: 10px;
  display: block;
}

input {
  width: 100%;
  padding: 6px;
  margin-bottom: 8px;
  box-sizing: border-box;
}

.submit-btn {
  width: 100%;
  padding: 10px;
  background-color: #4d88ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

.cancel-btn {
  width: 100%;
  margin-top: 10px;
  padding: 10px;
  background-color: #999;
  color: white;
  border-radius: 6px;
  cursor: pointer;
}

.error {
  color: red;
  text-align: center;
  margin-bottom: 10px;
}
</style>