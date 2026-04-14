<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Pouring Statistics - Smart Water</title>
        <link rel="stylesheet" href="css/style.css">
        <style>
            .filter-bar {
                background: white;
                padding: 20px;
                border-radius: 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 15px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.05);
                margin-bottom: 25px;
            }
            .stat-grid {
                display: flex;
                gap: 20px;
                justify-content: space-between;
                text-align: center;
            }
            .stat-item {
                flex: 1;
                background: #fff;
                padding: 30px 20px;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.05);
                border-top: 4px solid #3498db;
            }
            .stat-item:nth-child(2) {
                border-top-color: #27ae60;
            }
            .stat-item:nth-child(3) {
                border-top-color: #e74c3c;
            }
            .stat-value {
                font-size: 36px;
                font-weight: bold;
                color: #2c3e50;
                display: block;
                margin-top: 10px;
            }
            .stat-label {
                font-size: 14px;
                color: #7f8c8d;
                text-transform: uppercase;
                font-weight: 700;
                letter-spacing: 1px;
            }
            .stat-empty {
                background: #fff;
                color: #7f8c8d;
                padding: 60px 0;
                font-size: 18px;
                text-align: center;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.05);
                font-weight: 500;
            }
            .val-success {
                color: #27ae60;
            }
            .val-error {
                color: #e74c3c;
            }
        </style>
    </head>
    <body class="dashboard-page">
        <div class="banner dashboard-banner">
            <div class="banner-left">
                <h2>Pouring Statistics</h2>
            </div>
            <div class="banner-right">
                <div class="device-stats">
                    <a href="DashboardController" style="color: #ecf0f1; text-decoration: none; font-weight: bold; font-size: 15px;">Back to Dashboard</a>
                </div>
            </div>
        </div>

        <div class="dashboard-shell" style="max-width: 1000px; margin: 0 auto; padding-top: 30px;">

            <div class="filter-bar">
                <label style="font-weight: 600; color: #2c3e50; font-size: 24px;">Select Time Range:</label>
                <form action="StatisticController" method="GET" style="display: flex; gap: 10px; align-items: center; margin: 0;">
                    <select name="range" class="form-control" style="width: 250px; height: 42px;">
                        <option value="today" ${selectedRange == 'today' ? 'selected' : ''}>Today</option>
                        <option value="yesterday" ${selectedRange == 'yesterday' ? 'selected' : ''}>Yesterday</option>
                        <option value="3days" ${selectedRange == '3days' ? 'selected' : ''}>Last 3 Days</option>
                        <option value="1week" ${selectedRange == '1week' ? 'selected' : ''}>Last 1 Week</option>
                        <option value="1month" ${selectedRange == '1month' ? 'selected' : ''}>Last 1 Month</option>
                    </select>
                    <button type="submit" class="btn btn-primary" style="margin: 0; height: 42px; padding: 0 25px;">Search</button>
                </form>
            </div>

            <c:choose>
                <c:when test="${totalSessions == 0}">
                    <div class="stat-empty">
                        <p style="margin: 0;">No data to show for this period.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="stat-grid">
                        <div class="stat-item">
                            <span class="stat-label">Total Sessions</span>
                            <span class="stat-value">${totalSessions}</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-label">Total Poured (ml)</span>
                            <span class="stat-value val-success">${totalMlSuccess}</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-label">Failed Sessions</span>
                            <span class="stat-value ${failedSessions > 0 ? 'val-error' : ''}">${failedSessions}</span>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </body>
</html>