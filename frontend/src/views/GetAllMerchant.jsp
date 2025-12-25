<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<title>商家清單</title>
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

	<c:if test="${not empty sessionScope.msg}">
		<p style="color: green;">${sessionScope.msg}</p>
		<c:remove var="msg" scope="session" />
	</c:if>
	<div class="wrapper">
		<div class="table-wrapper">
			<div class="table-title">商家清單</div>
			<form
				action="${pageContext.request.contextPath}/GetMerchantByKeyword"
				method="get">
				<input type="text" name="keyword" value="${keyword}"
					placeholder="輸入關鍵字查詢">
				<button type="submit">搜尋</button>

			</form>
			<a href="${pageContext.request.contextPath}/InsertMerchant">
				<button>新增廠商</button>
			</a>

			<table border="1" class="jy-table">
				<thead>
					<tr>
						<th>ID</th>
						<th>名稱</th>
						<th>電話</th>
						<th>Email</th>
						<th>地址</th>
						<th>狀態</th>
						<th>建立時間</th>
						<th>修改</th>
						<th>刪除</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="m" items="${merchants}">
						<tr>
							<td>${m.merchantId}</td>
							<td><a
								href="${pageContext.request.contextPath}/GetDiscountByMerchant?merchantId=${m.merchantId}">${m.merchantName}</a></td>
							<td>${m.merchantPhone}</td>
							<td>${m.merchantEmail}</td>
							<td>${m.merchantAddress}</td>
							<td><c:choose>
									<c:when test="${m.merchantStatus == 1}">合作中</c:when>
									<c:when test="${m.merchantStatus == 2}">尚未合作</c:when>
									<c:otherwise>未知狀態</c:otherwise>
								</c:choose></td>
							<td>${m.createdTime}</td>
							<td><a href="UpdateMerchant?merchantId=${m.merchantId}">修改</a></td>
							<td><a href="DeleteMerchant?merchantId=${m.merchantId}"
								onclick="return confirm('確定刪除？');">刪除</a></td>
						</tr>

					</c:forEach>
				</tbody>

			</table>

			<h3>
				共
				<c:out value="${fn:length(merchants)}" />
				筆商家資料
			</h3>

			<br>
			<div style="text-align: center; margin-top: 20px;">
				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/GetAllMerchant'">
					商家清單</button>
				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/GetAllDiscount'">
					優惠券清單</button>
				<a href="http://localhost:8080/SeatRentSys/pages/home.html">
					<button>回首頁</button>
				</a>
			</div>
		</div>
	</div>
</body>
</html>