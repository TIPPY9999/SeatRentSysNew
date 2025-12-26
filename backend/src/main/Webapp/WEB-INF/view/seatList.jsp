<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Seat List</title>
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

a {
	margin-right: 10px;
}
</style>
</head>
<body>

	<h2>Seat 清單</h2>

	<a href="${pageContext.request.contextPath}/seat/insert">新增 Seat</a>
	<a href="${pageContext.request.contextPath}/seat/search">條件查詢</a>

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

					<form action="${pageContext.request.contextPath}/seat/delete"
						method="post" style="display: inline;">
						<input type="hidden" name="seatsId" value="${s.seatsId}">
						<button type="submit" onclick="return confirm('確定刪除?')">刪除</button>
					</form></td>
			</tr>
		</c:forEach>
	</table>

</body>
</html>
