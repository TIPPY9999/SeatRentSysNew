<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/MtifServlet.css">
</head>
<body>
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
			<div class="table-title">搜尋結果</div>

			<table border="1" class="jy-table">
				<thead>
					<tr>
						<th>ID</th>
						<th>商家名稱</th>
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
							<td><a
								href="${pageContext.request.contextPath}/UpdateMerchant?merchantId=${m.merchantId}">修改
							</a></td>
							<td><a
								href="${pageContext.request.contextPath}/DeleteMerchant?merchantId=${m.merchantId}"
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
			<div style="text-align: center;">
				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/GetAllMerchant'">
					回商家清單</button>
				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/GetAllDiscount'">
					回優惠券清單</button>
				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/pages/home.html'">
					回首頁</button>
			</div>
		</div>
	</div>
</body>
</html>