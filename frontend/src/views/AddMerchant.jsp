<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head><title>新增商家</title>
<link rel="stylesheet" href="/SeatRentSys/css/MtifServlet.css">
</head>
<body>
<h2>新增商家</h2>

<c:if test="${not empty sessionScope.msg}">
    <p>${sessionScope.msg}</p>
    <c:remove var="msg" scope="session"/>
</c:if>

<form action="${pageContext.request.contextPath}/InsertMerchant" method="post">
    商家名稱: <input type="text" name="merchantName" required><br>
    電話: <input type="text" name="merchantPhone"><br>
    Email: <input type="email" name="merchantEmail"><br>
    地址: <input type="text" name="merchantAddress" required><br>
    狀態: <select name="merchantStatus"><option value="1">合作中</option><option value="2">尚未合作</option></select><br>
    <button type="submit">新增</button>
    <button type="reset">重置</button>
    
</form>

<p><a href="${pageContext.request.contextPath}/GetAllMerchant">查看商家清單</a></p>
</body>
</html>