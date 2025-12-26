<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Seat Detail</title>
<style>
table {
	width: 60%;
	border-collapse: collapse;
	margin-top: 12px;
}

th, td {
	border: 1px solid #aaa;
	padding: 8px;
}

th {
	background: #eee;
	width: 160px;
}
</style>
</head>
<body>

	<h2>Seat 詳細資料</h2>

	<c:if test="${seat == null}">
		<p>找不到資料</p>
	</c:if>

	<c:if test="${seat != null}">
		<table>
			<tr>
				<th>ID</th>
				<td>${seat.seatsId}</td>
			</tr>
			<tr>
				<th>名稱</th>
				<td>${seat.seatsName}</td>
			</tr>
			<tr>
				<th>類型</th>
				<td>${seat.seatsType}</td>
			</tr>
			<tr>
				<th>狀態</th>
				<td>${seat.seatsStatus}</td>
			</tr>
			<tr>
				<th>SpotId</th>
				<td>${seat.spotId}</td>
			</tr>
			<tr>
				<th>序號</th>
				<td>${seat.serialNumber}</td>
			</tr>
			<tr>
				<th>CreatedAt</th>
				<td>${seat.createdAt}</td>
			</tr>
			<tr>
				<th>UpdatedAt</th>
				<td>${seat.updatedAt}</td>
			</tr>
		</table>
	</c:if>

	<br>
	<a href="${pageContext.request.contextPath}/seat/list">回清單</a>

</body>
</html>
