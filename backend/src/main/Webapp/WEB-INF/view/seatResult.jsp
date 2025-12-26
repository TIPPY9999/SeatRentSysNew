<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Seat Result</title>
<style>
table {
	width: 95%;
	border-collapse: collapse;
	margin-top: 12px;
}

th, td {
	border: 1px solid #aaa;
	padding: 8px;
}

th {
	background: #eee;
}
</style>
</head>
<body>

	<h2>Seat 查詢結果</h2>

	<a href="${pageContext.request.contextPath}/seat/search">回查詢</a>
	<a href="${pageContext.request.contextPath}/seat/list">回清單</a>

	<table>
		<tr>
			<th>ID</th>
			<th>名稱</th>
			<th>類型</th>
			<th>狀態</th>
			<th>SpotId</th>
			<th>序號</th>
			<th>UpdatedAt</th>
			<th>操作</th>
		</tr>

		<c:forEach var="s" items="${seatList}">
			<tr>
				<td>${s.seatsId}</td>
				<td>${s.seatsName}</td>
				<td>${s.seatsType}</td>
				<td>${s.seatsStatus}</td>
				<td>${s.spotId}</td>
				<td>${s.serialNumber}</td>
				<td>${s.updatedAt}</td>
				<td><a
					href="${pageContext.request.contextPath}/seat/one?seatsId=${s.seatsId}">詳細</a>
					<a
					href="${pageContext.request.contextPath}/seat/update?seatsId=${s.seatsId}">修改</a>
				</td>
			</tr>
		</c:forEach>
	</table>

</body>
</html>
