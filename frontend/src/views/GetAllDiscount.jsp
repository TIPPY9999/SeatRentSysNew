<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page
	import="java.util.List, java.time.LocalDate, java.time.temporal.ChronoUnit"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>優惠券管理</title>
<style>
table {
	width: 100%;
	border-collapse: collapse;
	margin-top: 20px;
}

th, td {
	border: 1px solid #ccc;
	padding: 8px;
	text-align: center;
}

.soon-expire {
	background-color: yellow !important;
}

.ended {
	background-color: #f5b7b1 !important;
}

.disabled {
	opacity: 0.5;
	pointer-events: none;
}

.close {
	background-color: gray !important;
}

.star {
	background-color: lightgreen !important;
}
</style>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/MtifServlet.css">
</head>
<body>
	<c:if test="${not empty sessionScope.msg}">
		<script>
			Swal.fire({
				icon : '${sessionScope.msgType}', // success / error
				title : '${sessionScope.msg}',
				timer : 2000,
				showConfirmButton : false
			});
		</script>
		<c:remove var="msg" scope="session" />
		<c:remove var="msgType" scope="session" />
	</c:if>
	<div class="wrapper">
		<div class="table-wrapper">
			<div class="table-title">優惠券管理系統</div>
			<!-- 今天日期 -->
			<jsp:useBean id="today" class="java.util.Date" />
			<%
			java.time.LocalDate now = java.time.LocalDate.now();
			request.setAttribute("today", now);
			%>
			<c:if test="${not empty msg}">
				<p style="color: green;">${msg}</p>
			</c:if>
			<!-- 搜尋表單 -->
			<form action="${pageContext.request.contextPath}/GetAllDiscount"
				method="get">
				<input type="text" name="keyword" value="${keyword}"
					placeholder="輸入優惠券名稱或內容查詢">
				<button type="submit">搜尋</button>
				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/GetAllDiscount'">清除查詢</button>
			</form>
			<a href="${pageContext.request.contextPath}/InsertDiscount">
				<button>新增優惠券</button>
			</a>
			<!-- 優惠券表格 -->
			<table class="jy-table">
				<thead>
					<tr>
						<th>ID</th>
						<th>名稱</th>
						<th>內容</th>
						<th>所需點數</th>
						<th>開始日期</th>
						<th>結束日期</th>
						<th>合作廠商</th>
						<th>狀態</th>
						<th>圖片</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="d" items="${discounts}">
						<c:set var="statusText" value="" />
						<c:set var="rowClass" value="" />
						<%
						LocalDate start = (LocalDate) pageContext.getAttribute("d").getClass().getMethod("getStartDate")
								.invoke(pageContext.getAttribute("d"));
						LocalDate end = (LocalDate) pageContext.getAttribute("d").getClass().getMethod("getEndDate")
								.invoke(pageContext.getAttribute("d"));
						int couponStatus = (Integer) pageContext.getAttribute("d").getClass().getMethod("getCouponStatus")
								.invoke(pageContext.getAttribute("d"));
						LocalDate todayDate = (LocalDate) request.getAttribute("today");
						String sText = "";
						String rowCls = "";
						if (couponStatus == 3) {
							sText = "已下架";
							rowCls = "close";
						} else if (end != null && todayDate.isAfter(end)) {
							sText = "已結束";
							rowCls = "ended";
						} else if (start != null && todayDate.isBefore(start)) {
							sText = "尚未開放";
						} else {
							sText = "活動開放";
							rowCls = "star";
							if (end != null && ChronoUnit.DAYS.between(todayDate, end) <= 3) {
								rowCls = "soon-expire";
							}
						}
						pageContext.setAttribute("statusText", sText);
						pageContext.setAttribute("rowClass", rowCls);
						%>
						<tr class="${rowClass}">
							<td>${d.couponId}</td>
							<td>${d.couponName}</td>
							<td class="text-wrap">${d.couponDescription}</td>
							<td>${d.pointsRequired}</td>
							<td>${d.startDate}</td>
							<td>${d.endDate}</td>
							<td><a
								href="${pageContext.request.contextPath}/GetMerchantByKeyword?keyword=${d.merchantName}">${d.merchantName}</a></td>
							<td>${statusText}</td>
							<td><c:if test="${not empty d.couponImg}">
									<img
										src="${pageContext.request.contextPath}/images/${d.couponImg}"
										width="150">
								</c:if></td>
							<td><a
								href="${pageContext.request.contextPath}/UpdateDiscount?couponId=${d.couponId}">修改</a>
								<!-- 刪除 --> <a
								href="${pageContext.request.contextPath}/DeleteDiscount?couponId=${d.couponId}"
								onclick="return confirm('確定刪除？');">刪除</a> <!-- 下架 --> <c:if
									test="${d.couponStatus != 3}">
									<a
										href="${pageContext.request.contextPath}/UpdateDiscountStatus?couponId=${d.couponId}&newStatus=disable">
										下架 </a>
								</c:if> <!-- 重新上架 --> <c:if test="${d.couponStatus == 3}">
									<a
										href="${pageContext.request.contextPath}/UpdateDiscountStatus?couponId=${d.couponId}&newStatus=relist">
										重新上架 </a>
								</c:if></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
			<br>
			<h3>
				共
				<c:out value="${fn:length(discounts)}" />
				筆優惠券資料
			</h3>
			<div style="text-align: center; margin-top: 20px;">
				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/GetAllMerchant'">
					商家清單</button>
				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/GetAllDiscount'">
					優惠券清單</button>
				<button
					onclick="location.href='${pageContext.request.contextPath}/pages/home.html'">回首頁</button>
			</div>
		</div>
	</div>
</body>
</html>