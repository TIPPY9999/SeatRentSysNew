<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>租借點詳情</title>

<style>
table {
	width: 60%;
	border-collapse: collapse;
	margin-top: 20px;
}

th, td {
	border: 1px solid #aaa;
	padding: 8px;
	text-align: left;
}

th {
	background-color: #eee;
	width: 150px;
}
</style>
</head>
<body>

	<h2>租借點詳細資料</h2>

	<table>
		<tr>
			<th>ID</th>
			<td>${spot.spotId}</td>
		</tr>
		<tr>
			<th>代號</th>
			<td>${spot.spotCode}</td>
		</tr>
		<tr>
			<th>名稱</th>
			<td>${spot.spotName}</td>
		</tr>
		<tr>
			<th>地址</th>
			<td>${spot.spotAddress}</td>
		</tr>
		<tr>
			<th>狀態</th>
			<td>${spot.spotStatus}</td>
		</tr>
		<tr>
			<th>經度</th>
			<td>${spot.longitude}</td>
		</tr>
		<tr>
			<th>緯度</th>
			<td>${spot.latitude}</td>
		</tr>
		<tr>
			<th>Merchant ID</th>
			<td>${spot.merchantId}</td>
		</tr>
	</table>

</body>
</html>
