<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert Seat</title>
</head>
<body>

	<h2>新增 Seat</h2>

	<form action="${pageContext.request.contextPath}/seat/insert"
		method="post">
		名稱：<input type="text" name="seatsName" required><br>
		<br> 類型：<input type="text" name="seatsType" required><br>
		<br> 狀態： <select name="seatsStatus" required>
			<option value="可用">可用</option>
			<option value="維修">維修</option>
			<option value="停用">停用</option>
		</select> <br>
		<br> SpotId（可空）：<input type="number" name="spotId"><br>
		<br> 序號（可空）：<input type="text" name="serialNumber"><br>
		<br>

		<button type="submit">新增</button>
	</form>

	<br>
	<a href="${pageContext.request.contextPath}/seat/list">回清單</a>

</body>
</html>
