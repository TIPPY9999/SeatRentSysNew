<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>查詢結果</title>
</head>
<body>

	<h2>查詢結果列表</h2>

	<c:if test="${empty spotList}">
		<p>沒有找到符合條件的資料。</p>
	</c:if>

	<c:if test="${not empty spotList}">
		<table border="1" cellpadding="5">
			<tr>
				<th>ID</th>
				<th>代碼</th>
				<th>名稱</th>
				<th>地址</th>
				<th>狀態</th>
				<th>Merchant ID</th>
				<th>緯度</th>
				<th>經度</th>
			</tr>

			<c:forEach var="spot" items="${spotList}">
				<tr>
					<td>${spot.spotId}</td>
					<td>${spot.spotCode}</td>
					<td>${spot.spotName}</td>
					<td>${spot.spotAddress}</td>
					<td>${spot.spotStatus}</td>
					<td>${spot.merchantId}</td>
					<td>${spot.latitude}</td>
					<td>${spot.longitude}</td>
				</tr>
			</c:forEach>

		</table>
	</c:if>

	<br>
	<a href="${pageContext.request.contextPath}/spot/search">返回搜尋頁</a>

</body>
</html>
