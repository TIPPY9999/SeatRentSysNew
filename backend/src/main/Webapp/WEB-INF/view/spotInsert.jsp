<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>新增租借點</title>
</head>
<body>

	<h2>新增租借點</h2>

	<form action="<c:url value='/spot/insert'/>" method="post">

		代碼：<input type="text" name="spotCode" required><br>
		<br> 名稱：<input type="text" name="spotName" required><br>
		<br> 地址：<input type="text" name="spotAddress" required><br>
		<br> 狀態：<input type="text" name="spotStatus" required><br>
		<br> 商家ID：<input type="number" name="merchantId" required><br>
		<br> 緯度：<input type="text" name="latitude"><br>
		<br> 經度：<input type="text" name="longitude"><br>
		<br>

		<button type="submit">送出</button>

	</form>

</body>
</html>
