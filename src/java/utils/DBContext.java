package utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBContext {

    private static HikariDataSource primaryDataSource;
    private static HikariDataSource localDataSource;

    // Các hằng số cấu hình giữ nguyên như cũ
    private static final String PRIMARY_DB_URL = "jdbc:sqlserver://192.168.4.2:1433;databaseName=SmartWaterAuditDB;encrypt=true;trustServerCertificate=true;";
    private static final String PRIMARY_USER = "sa";
    private static final String PRIMARY_PASS = "12345";

    private static final String LOCAL_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SmartWaterAuditDB;encrypt=true;trustServerCertificate=true;";
    private static final String LOCAL_USER = "sa";
    private static final String LOCAL_PASS = "12345";

    static {
        // Khởi tạo Pool tĩnh cho DB Chính
        HikariConfig primaryConfig = new HikariConfig();
        primaryConfig.setJdbcUrl(PRIMARY_DB_URL);
        primaryConfig.setUsername(PRIMARY_USER);
        primaryConfig.setPassword(PRIMARY_PASS);
        primaryConfig.setMaximumPoolSize(10); // Giữ sẵn 10 kết nối trên RAM
        
        // THÊM 2 DÒNG NÀY ĐỂ DỌN DẸP BỘ NHỚ:
        primaryConfig.setMaxLifetime(1800000); // Đóng và thay mới kết nối sau mỗi 30 phút (tránh bị treo ngầm)
        primaryConfig.setIdleTimeout(600000);  // Trả bớt kết nối về hệ điều hành nếu rảnh rỗi quá 10 phút
        
        primaryConfig.setConnectionTimeout(3000); // Tương đương loginTimeout=3
        primaryConfig.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        try {
            primaryDataSource = new HikariDataSource(primaryConfig);
        } catch (Exception e) {
            System.err.println("[DB] Không thể khởi tạo Pool cho DB Chính.");
        }

        // Khởi tạo Pool tĩnh cho DB Local dự phòng
        HikariConfig localConfig = new HikariConfig();
        localConfig.setJdbcUrl(LOCAL_DB_URL);
        localConfig.setUsername(LOCAL_USER);
        localConfig.setPassword(LOCAL_PASS);
        localConfig.setMaximumPoolSize(5);
        localConfig.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        localDataSource = new HikariDataSource(localConfig);
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Lấy reference kết nối từ Pool (trực tiếp từ RAM)
            return primaryDataSource.getConnection();
        } catch (SQLException | NullPointerException e) {
            System.err.println("[DB] Kết nối DB chính thất bại, tự động chuyển sang Local...");
            return localDataSource.getConnection();
        }
    }
}
