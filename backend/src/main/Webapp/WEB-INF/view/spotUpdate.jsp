<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>修改租借點</title>
</head>
<body>

<h2>修改租借點</h2>

<form action="<c:url value='/spot/update'/>" method="post">

    <input type="hidden" name="spotId" value="${spot.spotId}">

    代碼：<input type="text" name="spotCode" value="${spot.spotCode}" required><br><br>
    名稱：<input type="text" name="spotName" value="${spot.spotName}" required><br><br>
    地址：<input type="text" name="spotAddress" value="${spot.spotAddress}" required><br><br>
    狀態：<input type="text" name="spotStatus" value="${spot.spotStatus}" required><br><br>
    商家ID：<input type="number" name="merchantId" value="${spot.merchantId}" required><br><br>
    緯度：<input type="text" name="latitude" value="${spot.latitude}"><br><br>
    經度：<input type="text" name="longitude" value="${spot.longitude}"><br><br>

    <button type="submit">更新</button>

</form>

</body>
</html>
