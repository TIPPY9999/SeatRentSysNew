<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Update Seat</title>
</head>
<body>

	<h2>修改 Seat</h2>

	<c:if test="${seat == null}">
		<p>找不到資料</p>
	</c:if>

	<c:if test="${seat != null}">
		<form action="${pageContext.request.contextPath}/seat/update"
			method="post">
			<input type="hidden" name="seatsId" value="${seat.seatsId}">

			名稱：<input type="text" name="seatsName" value="${seat.seatsName}"
				required><br>
			<br> 類型：<input type="text" name="seatsType"
				value="${seat.seatsType}" required><br>
			<br> 狀態： <select name="seatsStatus" required>
				<option value="可用" ${seat.seatsStatus=='可用'?'selected':''}>可用</option>
				<option value="維修" ${seat.seatsStatus=='維修'?'selected':''}>維修</option>
				<option value="停用" ${seat.seatsStatus=='停用'?'selected':''}>停用</option>
			</select> <br>
			<br> SpotId（可空）：<input type="number" name="spotId"
				value="${seat.spotId}"><br>
			<br> 序號（可空）：<input type="text" name="serialNumber"
				value="${seat.serialNumber}"><br>
			<br>

			<button type="submit">更新</button>
		</form>
	</c:if>

	<br>
	<a href="${pageContext.request.contextPath}/seat/list">回清單</a>

</body>
</html>
