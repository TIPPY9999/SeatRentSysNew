<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Search Seat</title>
</head>
<body>

	<h2>Seat 條件查詢</h2>

	<form action="${pageContext.request.contextPath}/seat/condition"
		method="get">
		名稱(模糊)：<input type="text" name="seatsName"><br>
		<br> 類型(模糊)：<input type="text" name="seatsType"><br>
		<br> 狀態： <select name="seatsStatus">
			<option value="">(不限制)</option>
			<option value="可用">可用</option>
			<option value="維修">維修</option>
			<option value="停用">停用</option>
		</select> <br>
		<br> SpotId(精準)：<input type="number" name="spotId"><br>
		<br> 序號(模糊)：<input type="text" name="serialNumber"><br>
		<br>

		<button type="submit">查詢</button>
	</form>

	<br>
	<a href="${pageContext.request.contextPath}/seat/list">回清單</a>

</body>
</html>
