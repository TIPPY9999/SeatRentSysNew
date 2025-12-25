<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="discount" scope="request"
	class="com.seatrentsys.model.merchantandcoupon.discountBean" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>修改優惠券</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/MtifServlet.css">
</head>
<body>
	<div class="wrapper">
		<div class="table-wrapper">
			<div class="table-title">修改優惠券</div>

			<form action="${pageContext.request.contextPath}/UpdateDiscount"
				method="post" enctype="multipart/form-data">
				<input type="hidden" name="couponId" value="${discount.couponId}">
				<input type="hidden" name="oldCouponImg"
					value="${discount.couponImg}"> 優惠券名稱: <input type="text"
					name="couponName" value="${discount.couponName}" required><br>
				優惠內容: <input type="text" name="couponDescription"
					value="${discount.couponDescription}" required><br>
				所需點數: <input type="number" name="pointsRequired"
					value="${discount.pointsRequired}" required><br> 開始日期:
				<input type="date" name="startDate" value="${discount.startDate}"><br>
				結束日期: <input type="date" name="endDate" value="${discount.endDate}"><br>

				商家： <select name="merchantId" required>
					<option value="">-- 請選擇商家 --</option>
					<c:forEach var="m" items="${merchants}">
						<option value="${m.merchantId}"
							<c:if test="${m.merchantId == discount.merchantId}">selected</c:if>>
							${m.merchantName}</option>
					</c:forEach>
				</select><br> 上下架狀態： <select name="couponStatus">
					<option value="0"
						<c:if test="${discount.couponStatus == 0}">selected</c:if>>尚未開放</option>
					<option value="1"
						<c:if test="${discount.couponStatus == 1}">selected</c:if>>活動開放</option>
					<option value="2"
						<c:if test="${discount.couponStatus == 2}">selected</c:if>>已結束</option>
					<option value="3"
						<c:if test="${discount.couponStatus == 3}">selected</c:if>>已下架</option>
				</select><br> 圖片:<input type="hidden" name="oldCouponImg"
					value="${discount.couponImg}"> <input type="file"
					name="couponImg">
				<c:if test="${not empty discount.couponImg}">
					<img
						src="${pageContext.request.contextPath}/images/${discount.couponImg}"
						alt="優惠券圖片" width="200">
				</c:if>
				<br>

				<button type="submit">確認修改</button>
				<button type="reset">重置</button>
			</form>

			<br>
			<button
				onclick="location.href='${pageContext.request.contextPath}/GetAllDiscount'">返回優惠券列表</button>
		</div>
	</div>
</body>
</html>