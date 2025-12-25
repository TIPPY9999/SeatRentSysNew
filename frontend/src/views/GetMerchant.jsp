<%@page import="com.seatrentsys.model.merchantandcoupon.merchantBean"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>顯示廠商資料</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/MtifServlet.css">
</head>
<body>
	<div align="center">
		<div class="wrapper">
			<div class="table-wrapper">
				<div class="table-title">商家清單</div>
				<jsp:useBean id="merchant" scope="request"
					class="com.seatrentsys.model.merchantandcoupon.merchantBean" />
				<table class="jy-table">
					<tr>
						<td>廠商編號
						<td><input type="text"
							value="<%=merchant.getMerchantId()%> " readonly><br>
					<tr>
						<td>廠商名稱
						<td><input type="text"
							value="<%=merchant.getMerchantName()%> ">
					<tr>
						<td>廠商電話
						<td><input type="text"
							value="<%=merchant.getMerchantPhone()%> ">
					<tr>
						<td>Email
						<td><input type="text"
							value="<%=merchant.getMerchantEmail()%> ">
					<tr>
						<td>地址
						<td><input type="text"
							value="<%=merchant.getMerchantAddress()%> ">
					<tr>
						<td>狀態
						<td><input type="text"
							value="<%=merchant.getMerchantStatus()%> ">
					<tr>
						<td>建立時間
						<td><input type="text"
							value="<%=merchant.getCreatedTime()%> ">
				</table>
			</div>
			<br>
			<div style="text-align: center; margin-top: 20px;">
				<button
					onclick="location.href='http://localhost:8080/SeatRentSys/pages/home.html'">回首頁</button>
			</div>
		</div>
	</div>
</body>
</html>