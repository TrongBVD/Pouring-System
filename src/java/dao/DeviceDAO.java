package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Device;
import model.User;
import utils.DBContext;

public class DeviceDAO {

    private static final Logger LOGGER = Logger.getLogger(DeviceDAO.class.getName());

    public String getDeviceStatus(int deviceId) {
        String sql = "SELECT status FROM Device WHERE device_id = ?";
        try ( Connection conn = DBContext.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deviceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OFFLINE";
    }

    public int getActiveCalibrationId(int deviceId) {
        String sql = "SELECT TOP 1 calib_id FROM Calibration WHERE device_id = ? AND valid_to IS NULL ORDER BY valid_from DESC";
        try ( Connection conn = DBContext.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deviceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("calib_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int getDefaultProfileId() {
        String sql = "SELECT TOP 1 profile_id FROM PourProfile";
        try ( Connection conn = DBContext.getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("profile_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public Device getDeviceInfo() {
        // Ánh xạ chính xác các cột tương ứng với các biến trong class Device
        String sql = "SELECT device_id, location, firmware_ver, status FROM Device WHERE device_id = 1";

        try ( Connection conn = DBContext.getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Device d = new Device();
                d.setDeviceId(rs.getInt("device_id"));
                d.setLocation(rs.getString("location"));
                d.setFirmwareVer(rs.getString("firmware_ver"));
                d.setStatus(rs.getString("status"));
                return d;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn thông tin thiết bị", e);
        }

        return null;
    }

    // =========================================================================
    // HÀM MỚI: Cập nhật trạng thái máy và gọi Stored Procedure để ghi Audit Log
    // =========================================================================
    public void updateDeviceStatus(int deviceId, String status, User actor) {
        String sql = "{call Device_UpdateStatus_User(?, ?, ?, ?, ?, ?, ?)}";
        try ( Connection conn = DBContext.getConnection();  CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, deviceId);
            cs.setString(2, status);
            cs.setInt(3, actor.getUserId());
            cs.setString(4, actor.getRole());
            cs.setNull(5, java.sql.Types.NVARCHAR); // Lý do (reason) - Không bắt buộc

            // 2 biến OUTPUT bắt buộc của Procedure Audit (id và chuỗi băm)
            cs.registerOutParameter(6, java.sql.Types.INTEGER);
            cs.registerOutParameter(7, java.sql.Types.CHAR);

            cs.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
