<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>查詢租借點</title>

<style>
form {
	margin: 20px 0;
}

input {
	margin: 5px;
}
</style>
</head>
<body>

	<h2>多條件查詢租借點</h2>

	<form action="<c:url value='/spot/condition'/>" method="get">
		代碼：<input type="text" name="spotCode" /> 名稱：<input type="text"
			name="spotName" /> 狀態：<input type="text" name="spotStatus"
			placeholder="啟用/停用" /> 商家ID：<input type="number" name="merchantId" />
		<button type="submit">查詢</button>
	</form>

	<hr>

	<c:url var="listUrl" value="/spot/list" />
	<a href="${listUrl}">返回租借點列表</a>

</body>
</html>
