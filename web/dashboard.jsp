<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Dashboard - Smart Water</title>
        <link rel="stylesheet" href="css/style.css">
        <style>
            .radio-group {
                display: flex;
                gap: 10px;
                margin-bottom: 15px;
            }
            .radio-group label {
                flex: 1;
                text-align: center;
                background: #fff;
                border: 2px solid #ddd;
                padding: 10px 0;
                border-radius: 6px;
                cursor: pointer;
                font-weight: bold;
                color: #555;
                transition: all 0.2s ease;
            }
            .radio-group input[type="radio"] {
                display: none;
            }
            .radio-group input[type="radio"]:checked + label {
                border-color: #3498db;
                background: #ebf5fb;
                color: #2980b9;
                box-shadow: 0 2px 6px rgba(52, 152, 219, 0.3);
            }
        </style>
    </head>
    <body class="dashboard-page">
        <div class="banner dashboard-banner">
            <div class="banner-left">
                <h2>Smart Water System</h2>
                <div class="device-stats">
                    <span>User: <strong>${sessionScope.LOGIN_USER.username}</strong> (${sessionScope.LOGIN_USER.role})</span>
                    <span>|</span>
                    <a href="LogoutController">Logout</a>
                    <c:if test="${sessionScope.LOGIN_USER.role == 'ADMIN'}">
                        <span>|</span>
                        <a href="AdminController?view=users" class="banner-link-strong">Manage Users</a>
                    </c:if>
                </div>
            </div>

            <div class="banner-right">
                <div class="device-stats">
                    <span>Device ID: <strong>#${DEVICE.deviceId}</strong></span>
                    <span>Loc: ${DEVICE.location}</span>
                    <span>Ver: ${DEVICE.firmwareVer}</span>
                    <span class="status-badge status-${DEVICE.status}">${DEVICE.status}</span>
                </div>
            </div>
        </div>

        <div id="toastArea" class="toast-area"></div>

        <div class="dashboard-shell compact-dashboard-shell">

            <div class="dashboard-top-grid compact-top-grid">

                <c:choose>
                    <c:when test="${sessionScope.LOGIN_USER.role != 'AUDITOR'}">
                        <section class="dashboard-card action-card compact-action-card">
                            <div class="card-header-row compact-header-row">
                                <div>
                                    <h3>Device Control</h3>
                                    <p>Operate the pouring device.</p>
                                </div>
                            </div>

                            <div style="margin: 15px 20px;">
                                <p style="font-size: 14px; font-weight: 600; margin-bottom: 8px;">Select Volume (ml):</p>
                                <div class="radio-group">
                                    <input type="radio" id="vol90" name="target_ml" value="90">
                                    <label for="vol90">90</label>

                                    <input type="radio" id="vol180" name="target_ml" value="180">
                                    <label for="vol180">180</label>

                                    <input type="radio" id="vol270" name="target_ml" value="270">
                                    <label for="vol270">270</label>

                                    <input type="radio" id="vol360" name="target_ml" value="360" checked>
                                    <label for="vol360">360</label>
                                </div>
                            </div>

                            <div class="action-button-row compact-action-row">
                                <button
                                    id="btnStart"
                                    type="button"
                                    class="action-btn action-btn-start"
                                    onclick="triggerPour()"
                                    disabled>
                                    START
                                </button>

                                <button
                                    id="btnStop"
                                    type="button"
                                    class="action-btn action-btn-stop"
                                    onclick="triggerStop()"
                                    disabled>
                                    STOP
                                </button>
                            </div>
                        </section>
                    </c:when>

                    <c:otherwise>
                        <section class="dashboard-card observer-card compact-observer-card">
                            <h3>Observer Mode</h3>
                            <p>No device control permission.</p>
                        </section>
                    </c:otherwise>
                </c:choose>

                <c:if test="${sessionScope.LOGIN_USER.role == 'ADMIN'}">
                    <section class="dashboard-card admin-control-card compact-admin-card">
                        <div class="card-header-row compact-header-row">
                            <div>
                                <h3>Admin Controls</h3>
                                <p>Update the device operating status.</p>
                            </div>
                        </div>

                        <form action="AdminController" method="POST" class="status-form compact-status-form">
                            <input type="hidden" name="action" value="update_status">

                            <div class="status-form-row compact-status-row">
                                <div class="form-group compact-group">
                                    <label>Set Device Status</label>
                                    <select name="device_status" class="form-control" required>
                                        <option value="ACTIVE" ${DEVICE.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                        <option value="MAINTENANCE" ${DEVICE.status == 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                                        <c:if test="${DEVICE.status == 'OFFLINE'}">
                                            <option value="OFFLINE" selected disabled>OFFLINE (System Auto)</option>
                                        </c:if>
                                        <c:if test="${DEVICE.status == 'ERROR'}">
                                            <option value="ERROR" selected disabled>ERROR (System Auto)</option>
                                        </c:if>
                                    </select>
                                </div>

                                <button type="submit" class="btn btn-secondary compact-submit-btn"><p>Set Status</p></button>
                            </div>
                        </form>
                    </section>
                </c:if>
            </div>

            <section class="dashboard-card modules-card">
                <div class="card-header-row compact-header-row">
                    <div>
                        <p>Smart Water System</p>
                    </div>
                </div>

                <div class="dashboard-link-grid compact-module-grid">
                    <c:if test="${sessionScope.LOGIN_USER.role == 'ADMIN'}">
                        <a href="SensorTypeController" class="dashboard-link-card compact-link-card config-link-card">
                            <span class="link-title">Sensor Types</span>
                            <span class="link-subtitle">Manage sensor categories</span>
                        </a>
                    </c:if>

                    <c:if test="${sessionScope.LOGIN_USER.role != 'GUEST'}">
                        <a href="PourHistoryController" class="dashboard-link-card compact-link-card">
                            <span class="link-title">Pour History</span>
                            <span class="link-subtitle">Water pouring history</span>
                        </a>

                        <c:if test="${sessionScope.LOGIN_USER.role != 'GUEST' && sessionScope.LOGIN_USER.role != 'OPERATOR'}">
                            <a href="PourSessionMetaController" class="dashboard-link-card compact-link-card">
                                <span class="link-title">Pour Session Meta</span>
                                <span class="link-subtitle">View or adjust metadata</span>
                            </a>

                            <c:if test="${sessionScope.LOGIN_USER.role == 'AUDITOR' || sessionScope.LOGIN_USER.role == 'ADMIN' || sessionScope.LOGIN_USER.role == 'TECHNICIAN'}">
                                <a href="SensorLogController" class="dashboard-link-card compact-link-card">
                                    <span class="link-title">Sensor Logs</span>
                                    <span class="link-subtitle">Raw data and metadata</span>
                                </a>

                                <a href="SensorHealthController" class="dashboard-link-card compact-link-card">
                                    <span class="link-title">Health Report</span>
                                    <span class="link-subtitle">Device metrics every 5 minutes</span>
                                </a>

                                <c:if test="${sessionScope.LOGIN_USER.role == 'AUDITOR' || sessionScope.LOGIN_USER.role == 'ADMIN'}">
                                    <a href="AuditLogController" class="dashboard-link-card compact-link-card">
                                        <span class="link-title">Full Audit Logs</span>
                                        <span class="link-subtitle">Blockchain hash chain</span>
                                    </a>
                                </c:if>

                                <c:if test="${sessionScope.LOGIN_USER.role == 'ADMIN' || sessionScope.LOGIN_USER.role == 'TECHNICIAN'}">
                                    <a href="MaintenanceController" class="dashboard-link-card compact-link-card maintenance-link-card">
                                        <span class="link-title">Maintenance</span>
                                        <span class="link-subtitle">Maintenance and issue handling</span>

                                        <c:if test="${ACTIVE_ALERT_COUNT > 0}">
                                            <span class="link-badge">${ACTIVE_ALERT_COUNT}</span>
                                        </c:if>
                                    </a>
                                </c:if>

                                <c:if test="${sessionScope.LOGIN_USER.role == 'AUDITOR' || sessionScope.LOGIN_USER.role == 'ADMIN' || sessionScope.LOGIN_USER.role == 'TECHNICIAN'}">

                                    <a href="StatisticController" class="dashboard-link-card compact-link-card">
                                        <span class="link-title">My Pouring Stats</span>
                                        <span class="link-subtitle">View water consumption data</span>
                                    </a>

                                </c:if>
                            </c:if>
                        </c:if>
                    </c:if>
                </div>
            </section>
        </div>

        <script>
            const btnStart = document.getElementById('btnStart');
            const btnStop = document.getElementById('btnStop');
            let lastDeviceState = '';

            function showToast(message, type) {
                const area = document.getElementById('toastArea');
                if (!area)
                    return;

                const toast = document.createElement('div');
                toast.className = 'app-toast ' + (type || 'info');
                toast.textContent = message;
                area.appendChild(toast);

                setTimeout(() => {
                    toast.classList.add('show');
                }, 10);

                setTimeout(() => {
                    toast.classList.remove('show');
                    setTimeout(() => {
                        if (toast.parentNode) {
                            toast.parentNode.removeChild(toast);
                        }
                    }, 250);
                }, 3000);
            }

            function setButtonsEnabled(enabled) {
                if (btnStart) {
                    btnStart.disabled = !enabled;
                    if (enabled) {
                        btnStart.classList.remove('is-disabled');
                    } else {
                        btnStart.classList.add('is-disabled');
                    }
                }

                if (btnStop) {
                    btnStop.disabled = !enabled;
                    if (enabled) {
                        btnStop.classList.remove('is-disabled');
                    } else {
                        btnStop.classList.add('is-disabled');
                    }
                }
            }

            function syncDeviceState() {
                if (!btnStart && !btnStop)
                    return;

                fetch('PourController?action=ping')
                        .then(response => response.text())
                        .then(data => {
                            const state = data.trim();

                            if (state === 'OK_ACTIVE') {
                                setButtonsEnabled(true);
                                if (lastDeviceState !== 'OK_ACTIVE' && lastDeviceState !== '') {
                                    showToast('The device connection has been restored.', 'success');
                                }
                            } else if (state === 'OK_MAINTENANCE') {
                                setButtonsEnabled(false);
                                if (lastDeviceState !== 'OK_MAINTENANCE') {
                                    showToast('The system is currently in maintenance mode.', 'warning');
                                }
                            } else {
                                setButtonsEnabled(false);
                                if (lastDeviceState !== 'UNAVAILABLE') {
                                    showToast('Unable to connect to the ESP32 device right now.', 'error');
                                }
                            }

                            lastDeviceState = (state === 'OK_ACTIVE' || state === 'OK_MAINTENANCE') ? state : 'UNAVAILABLE';
                        })
                        .catch(() => {
                            setButtonsEnabled(false);
                            if (lastDeviceState !== 'UNAVAILABLE') {
                                showToast('Unable to connect to the ESP32 device right now.', 'error');
                            }
                            lastDeviceState = 'UNAVAILABLE';
                        });
            }

            function triggerPour() {
                if (!btnStart || btnStart.disabled)
                    return;

                setButtonsEnabled(false);

                // CÁCH MỚI LẤY GIÁ TRỊ RADIO
                let selectedMl = '360';
                const radioButtons = document.querySelectorAll('input[name="target_ml"]');
                for (const rb of radioButtons) {
                    if (rb.checked) {
                        selectedMl = rb.value;
                        break;
                    }
                }

                fetch('PourController?action=start_pour&target_ml=' + selectedMl, {method: 'POST'})
                        .then(response => response.text())
                        .then(data => {
                            const result = data.trim();

                            if (result === 'OK') {
                                showToast('Pour command sent for ' + selectedMl + 'ml.', 'success');
                            } else if (result === 'MAINTENANCE') {
                                showToast('The system is locked for maintenance and cannot pour.', 'warning');
                            } else if (result === 'BUSY') {
                                showToast('Hệ thống đang bận phục vụ người khác!', 'warning');
                            } else {
                                showToast('The device is busy or no cup has been placed.', 'error');
                            }

                            syncDeviceState();
                        })
                        .catch(() => {
                            showToast('Unable to send the pour command to the device.', 'error');
                            syncDeviceState();
                        });
            }

            function triggerStop() {
                if (!btnStop || btnStop.disabled)
                    return;

                setButtonsEnabled(false);

                fetch('PourController?action=stop_pour', {method: 'POST'})
                        .then(response => response.text())
                        .then(data => {
                            const result = data.trim();

                            if (result === 'STOP_OK') {
                                showToast('EMERGENCY STOP COMMAND SENT!', 'warning');
                            } else {
                                showToast('Failed to send stop command.', 'error');
                            }

                            syncDeviceState();
                        })
                        .catch(() => {
                            showToast('Unable to send the stop command to the device. Network error.', 'error');
                            syncDeviceState();
                        });
            }

            // Gắn event listener chuẩn
            document.addEventListener('DOMContentLoaded', function () {
                syncDeviceState();
                setInterval(syncDeviceState, 3000);
            });

            // TÁCH JSTL RA KHỎI EVENT LISTENER ĐỂ TRÁNH LỖI CÚ PHÁP
            <c:if test="${not empty param.error}">
            setTimeout(() => showToast('Error: ${param.error}', 'error'), 500);
            </c:if>

            <c:if test="${not empty param.msg && param.msg == 'success'}">
            setTimeout(() => showToast('Updated successfully.', 'success'), 500);
            </c:if>
        </script>
    </body>
</html>