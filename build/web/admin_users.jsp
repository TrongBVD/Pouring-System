<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>User Management - Smart Water</title>
        <link rel="stylesheet" href="css/style.css">
        <style>
            /* Reset body cho layout full-width */
            body {
                background-color: #f4f7f6;
                margin: 0;
                padding: 0;
                font-family: 'Segoe UI', sans-serif;
            }

            /* Container mở rộng toàn màn hình với khoảng cách lề */
            .admin-users-container {
                width: 100%;
                padding: 30px;
                box-sizing: border-box;
            }

            /* Box panel màu trắng */
            .admin-panel-full {
                background: white;
                padding: 25px;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.05);
                margin-bottom: 25px;
            }

            /* --- FORM TẠO MỚI (FLEXBOX ALIGNMENT) --- */
            /* Sử dụng align-items: stretch để tất cả các phần tử con có cùng chiều cao */
            .form-row-inline {
                display: flex;
                gap: 15px;
                align-items: stretch;
                margin-top: 15px;
            }
            .form-control-inline {
                flex: 1;
                padding: 10px 15px;
                border: 1px solid #ddd;
                border-radius: 6px;
                font-size: 14px;
                box-sizing: border-box;
                outline: none;
            }
            .form-control-inline:focus {
                border-color: #3498db;
            }

            /* Nút Create tự động ăn theo chiều cao của ô input nhờ Flexbox */
            .btn-create-inline {
                padding: 0 30px;
                background: #3498db;
                color: white;
                border: none;
                border-radius: 6px;
                font-weight: 600;
                font-size: 15px;
                cursor: pointer;
                transition: 0.2s ease;
            }
            .btn-create-inline:hover {
                background: #2980b9;
            }

            /* --- BẢNG DỮ LIỆU FULL WIDTH --- */
            table.full-width-table {
                width: 100%;
                border-collapse: collapse;
            }
            .full-width-table th {
                background: #f8f9fa;
                padding: 16px 25px;
                text-align: left;
                border-bottom: 2px solid #edf2f7;
                color: #2c3e50;
                font-weight: 600;
            }
            .full-width-table td {
                padding: 16px 25px;
                border-bottom: 1px solid #edf2f7;
                vertical-align: middle;
            }

            /* --- NÚT ACTION TRONG BẢNG --- */
            .table-action-form {
                display: flex;
                gap: 8px;
                align-items: center;
                margin: 0;
            }
            .table-btn {
                padding: 7px 14px;
                border-radius: 6px;
                border: none;
                cursor: pointer;
                font-size: 13px;
                font-weight: 600;
                color: #fff;
                transition: transform 0.2s ease, opacity 0.2s ease;
            }
            .table-btn:hover {
                transform: translateY(-1px);
                opacity: 0.9;
            }
            .btn-lock {
                background: #f39c12;
            }
            .btn-disable {
                background: #e74c3c;
            }
            .btn-active {
                background: #2ecc71;
            }

            /* --- STATUS BADGES --- */
            .status-badge {
                display: inline-block;
                padding: 5px 12px;
                border-radius: 20px;
                font-size: 11px;
                font-weight: 700;
                text-transform: uppercase;
            }
            .st-ACTIVE {
                background: #d4edda;
                color: #155724;
            }
            .st-LOCKED {
                background: #fff3cd;
                color: #856404;
            }
            .st-DISABLED {
                background: #f8d7da;
                color: #721c24;
            }

            /* --- MESSAGES --- */
            .msg-error {
                color: #e74c3c;
                font-weight: 600;
                margin-top: 15px;
                font-size: 14px;
            }
            .msg-success {
                color: #2ecc71;
                font-weight: 600;
                margin-top: 15px;
                font-size: 14px;
            }
        </style>
    </head>
    <body>
        <div class="banner">
            <div style="width: 100%; display: flex; justify-content: space-between; align-items: center; padding: 0 20px;">
                <h2 style="margin: 0;">User Management</h2>
                <div class="device-stats" style="margin: 0;">
                    <span>Admin: <strong>${sessionScope.LOGIN_USER.username}</strong></span> | 
                    <a href="DashboardController" style="color: #bdc3c7; text-decoration: none; font-weight: 500;">Back to Dashboard</a>
                </div>
            </div>
        </div>

        <div class="admin-users-container">
            <div class="admin-panel-full">
                <h3 style="margin-top: 0; color: #2c3e50;">Create New Staff</h3>
                <form action="AdminController" method="POST">
                    <input type="hidden" name="action" value="create_internal_user">

                    <div class="form-row-inline">
                        <input type="text" name="new_user" placeholder="Username" class="form-control-inline" required>
                        <input type="password" name="new_pass" placeholder="Password" class="form-control-inline" required>
                        <select name="new_role" class="form-control-inline" required>
                            <option value="" disabled selected>-- Select Role --</option>
                            <option value="ADMIN">Admin</option>
                            <option value="TECHNICIAN">Technician</option> 
                            <option value="AUDITOR">Auditor</option>
                            <option value="OPERATOR">Operator</option>
                        </select>
                        <button type="submit" class="btn-create-inline">Create</button>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="msg-error">${error}</div>
                    </c:if>
                    <c:if test="${not empty success}">
                        <div class="msg-success">${success}</div>
                    </c:if>
                </form>
            </div>

            <div class="admin-panel-full" style="padding: 0; overflow: hidden;">
                <table class="full-width-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Username</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="u" items="${USER_LIST}">
                            <tr>
                                <td>#${u.userId}</td>
                                <td><strong>${u.username}</strong></td>
                                <td>${u.role}</td>
                                <td><span class="status-badge st-${u.status}">${u.status}</span></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${u.userId == sessionScope.LOGIN_USER.userId}">
                                            <span style="color:#95a5a6; font-size:13px; font-style: italic;">(You)</span>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="AdminController" method="POST" class="table-action-form">
                                                <input type="hidden" name="action" value="toggle_user">
                                                <input type="hidden" name="uid" value="${u.userId}">

                                                <c:choose>
                                                    <c:when test="${u.status == 'ACTIVE'}">
                                                        <button name="status" value="LOCKED" class="table-btn btn-lock">Lock</button>
                                                        <button name="status" value="DISABLED" class="table-btn btn-disable">Disable</button>
                                                    </c:when>
                                                    <c:when test="${u.status == 'LOCKED'}">
                                                        <button name="status" value="ACTIVE" class="table-btn btn-active">Activate</button>
                                                        <button name="status" value="DISABLED" class="table-btn btn-disable">Disable</button>
                                                    </c:when>
                                                    <c:when test="${u.status == 'DISABLED'}">
                                                        <span style="color:#e74c3c; font-weight:bold; font-size:13px; padding: 6px 0;">(Disabled)</span>
                                                    </c:when>
                                                </c:choose>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>