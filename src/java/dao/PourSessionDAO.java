package dao;

import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import utils.DBContext;

public class PourSessionDAO {

    // ... các code khác của bạn ...
    public JsonObject getUserStats(int userId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        JsonObject result = new JsonObject();
        result.addProperty("total_sessions", 0);
        result.addProperty("total_ml_all", 0.0);
        result.addProperty("total_ml_success", 0.0);
        result.addProperty("failed_sessions", 0);

        String sql = "EXEC dbo.PourSession_ListStats_ByUser @actor_user_id = ?, @date_from = ?, @date_to = ?";

        // Sử dụng class DBContext của bạn để kết nối
        try ( Connection conn = new DBContext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setObject(2, dateFrom); // JDBC 4.2 tự hiểu LocalDateTime
            ps.setObject(3, dateTo);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.addProperty("total_sessions", rs.getInt("total_sessions"));
                    result.addProperty("total_ml_all", rs.getDouble("total_ml_all"));
                    // Ép kiểu double để phòng hờ DB trả về float/decimal
                    result.addProperty("total_ml_success", rs.getDouble("total_ml_success"));
                    result.addProperty("failed_sessions", rs.getInt("failed_sessions"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
