package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    // 1. Cấu hình Database chính (Máy trong mạng)
    // THÊM loginTimeout=3 để máy tự động bỏ cuộc sau 3s nếu mất mạng, không bị treo Web
    private static final String PRIMARY_DB_URL = "jdbc:sqlserver://localhost\\SQL2022:1433;databaseName=SmartWaterAuditDB;encrypt=true;trustServerCertificate=true;loginTimeout=3;";
    private static final String PRIMARY_USER = "sa";
    private static final String PRIMARY_PASS = "12345";

    // 2. Cấu hình Database dự phòng (Máy Local / localhost)
    private static final String LOCAL_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SmartWaterAuditDB;encrypt=true;trustServerCertificate=true;loginTimeout=3;";
    private static final String LOCAL_USER = "sa";     // Đổi lại user máy local của bạn nếu khác
    private static final String LOCAL_PASS = "12345";  // Đổi lại pass máy local của bạn nếu khác

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        try {
            // Bước 1: Thử gọi đến máy chủ chính
            // System.out.println("[DB] Đang kết nối đến Database chính (192.168.4.2)...");
            return DriverManager.getConnection(PRIMARY_DB_URL, PRIMARY_USER, PRIMARY_PASS);

        } catch (SQLException e) {
            // Bước 2: Bắt lỗi nếu mạng rớt, chập chờn, hoặc máy chủ kia đang tắt
            System.err.println("[DB] Kết nối DB chính thất bại: " + e.getMessage());
            System.out.println("[DB] Đang tự động chuyển sang Database Local (localhost)...");

            // Lập tức gọi về máy Local
            return DriverManager.getConnection(LOCAL_DB_URL, LOCAL_USER, LOCAL_PASS);
        }
    }
}
