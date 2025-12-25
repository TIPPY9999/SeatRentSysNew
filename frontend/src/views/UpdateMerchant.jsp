<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.seatrentsys.model.merchantandcoupon.merchantBean"%>

<jsp:useBean id="merchant" scope="request"
	class="com.seatrentsys.model.merchantandcoupon.merchantBean" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>修改廠商資料</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/MtifServlet.css">

</head>
<body align="center" >
	<div class="wrapper">
		<div class="table-wrapper">
			<div class="table-title">修改廠商資料</div>
			<form action="/SeatRentSys/UpdateMerchant" method="post"
				align="center">
				<div align="center">
					<label>廠商編號:</label> <input type="number"
						value="<%=merchant.getMerchantId()%>" readonly> <input
						type="hidden" name="merchantId"
						value="<%=merchant.getMerchantId()%>"><br> <label>廠商名稱:</label>
					<input type="text" name="merchantName"
						value="<%=merchant.getMerchantName()%>" required><br>
					<label>電話:</label> <input type="text" name="merchantPhone"
						value="<%=merchant.getMerchantPhone()%>" required><br>
					<label>Email:</label> <input type="text" name="merchantEmail"
						value="<%=merchant.getMerchantEmail()%>" required><br>
					<label>地址:</label> <input type="text" name="merchantAddress"
						value="<%=merchant.getMerchantAddress()%>" required><br>

					<label>狀態:</label> <select name="merchantStatus" required>
						&lt;option value="1"
							&lt;%=merchant.getMerchantStatus() == 1 ? "selected" : ""%&gt;&gt;合作中</option>
						&lt;option value="2"
							&lt;%=merchant.getMerchantStatus() == 2 ? "selected" : ""%&gt;&gt;尚未合作</option>
					</select> <br> <label>建立時間:</label> <input type="text"
						name="createdTime" value="<%=merchant.getCreatedTime()%>" readonly><br>
					<br> <input type="submit" value="確認修改"> <input
						type="reset" value="重置">
				</div>
			</form>
			<div style="text-align: center; margin-top: 20px;">
				<button
					onclick="location.href='http://localhost:8080/SeatRentSys/pages/home.html'">回首頁</button>
			</div>
		</div>
	</div>
</body>