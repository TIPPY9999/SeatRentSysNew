<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>新增優惠券</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/MtifServlet.css">
</head>
<body>
	<c:if test="${not empty sessionScope.msg}">
		<p>${sessionScope.msg}</p>
		<c:remove var="msg" scope="session" />
	</c:if>
<div class="wrapper">
		<div class="table-wrapper">
		<div class="table-title">優惠券新增</div>
	<form action="${pageContext.request.contextPath}/InsertDiscount"
		method="post" enctype="multipart/form-data">
		
		優惠券名稱: <input type="text" name="couponName" required><br>
		優惠內容: <input type="text" name="couponDescription" required><br>
		所需點數: <input type="number" name="pointsRequired" required><br>
		開始日期: <input type="date" name="startDate"><br> 結束日期: <input
			type="date" name="endDate"><br> 
		商家： <select name="merchantId" required>
			<option value="">-- 請選擇商家 --</option>
			<c:forEach var="m" items="${merchants}">
				<option value="${m.merchantId}">${m.merchantName}</option>
			</c:forEach></select> <br> 
		狀態: <select name="couponStatus">
			<option value="1">上架</option>
			<option value="0">下架</option>
		</select><br> 
		圖片網址: <input type="file" name="couponImg"><img src="${pageContext.request.contextPath}/images/${discount.couponImg}" alt="優惠券圖片" width="200">
<br>
		<br>

		<button type="submit">新增優惠券</button>
		<button type="reset">重置</button>
	</form>

	<br>
	<button
		onclick="location.href='${pageContext.request.contextPath}/GetAllDiscount'">返回優惠券列表</button>
	<button
		onclick="location.href='${pageContext.request.contextPath}/pages/home.html'">返回首頁</button>

</div>
</div>
</body>
</html>