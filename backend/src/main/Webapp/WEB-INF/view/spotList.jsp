<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html lang="zh-Hant">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>租借點列表</title>

    <!-- AdminLTE / Bootstrap / FontAwesome -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/vendor/adminlte/plugins/fontawesome-free/css/all.min.css" />
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/vendor/adminlte/dist/css/adminlte.min.css" />


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@400;500;600;700&display=swap" rel="stylesheet">

  
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/MtifServlet.css" />

  
    <style>
        .table td, .table th { vertical-align: middle; }
        .btn-gap { margin-right: 6px; }
    </style>
</head>

<body>
<div class="content-wrapper" style="min-height: 100vh;">


    <section class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-6 d-flex align-items-center">
                    <h1 class="mb-0">租借點列表</h1>
                </div>

                <div class="col-sm-6 text-right">
                    <!-- 新增按鈕：改用 AdminLTE/Bootstrap button -->
                    <a class="btn btn-success btn-sm"
                       href="${pageContext.request.contextPath}/spot/insert">
                        <i class="fas fa-plus mr-1"></i>新增租借點
                    </a>
                </div>
            </div>
        </div>
    </section>


    <section class="content">
        <div class="container-fluid">

            <!-- 你的米色表格區（沿用工單頁 wrapper/table-wrapper/table-title/jy-table） -->
            <div class="wrapper">
                <div class="table-wrapper">
                    <div class="table-title">租借點列表</div>

                    <table class="jy-table table table-striped table-hover table-sm">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>代號</th>
                            <th>名稱</th>
                            <th>地址</th>
                            <th>狀態</th>
                            <th style="width: 220px;">操作</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:choose>
                            <c:when test="${not empty spotList}">
                                <c:forEach var="spot" items="${spotList}">
                                    <tr>
                                        <td>${spot.spotId}</td>
                                        <td>${spot.spotCode}</td>
                                        <td>${spot.spotName}</td>
                                        <td>${spot.spotAddress}</td>
                                        <td>${spot.spotStatus}</td>

                                        <td>
                                            <!-- 查看 -->
                                            <a class="btn btn-outline-primary btn-sm btn-gap"
                                               href="${pageContext.request.contextPath}/spot/one?spotId=${spot.spotId}">
                                                查看
                                            </a>

                                            <!-- 修改 -->
                                            <a class="btn btn-outline-info btn-sm btn-gap"
                                               href="${pageContext.request.contextPath}/spot/update?spotId=${spot.spotId}">
                                                修改
                                            </a>

                                            <!-- 刪除：維持 POST -->
                                            <form action="${pageContext.request.contextPath}/spot/delete"
                                                  method="post"
                                                  style="display:inline;">
                                                <input type="hidden" name="spotId" value="${spot.spotId}">
                                                <button type="submit"
                                                        class="btn btn-outline-danger btn-sm"
                                                        onclick="return confirm('確定要刪除嗎？');">
                                                    刪除
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>

                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="text-center">目前沒有租借點資料。</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </section>
</div>

<!-- AdminLTE 必要 JS -->
<script src="${pageContext.request.contextPath}/vendor/adminlte/plugins/jquery/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/vendor/adminlte/plugins/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/vendor/adminlte/dist/js/adminlte.min.js"></script>
</body>
</html>
