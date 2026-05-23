package dao;

import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.DBContext;

public class PourSessionDAO {

    public JsonObject getUserStats(int userId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        JsonObject result = new JsonObject();
        result.addProperty("total_sessions", 0);
        result.addProperty("total_ml_all", 0.0);
        result.addProperty("total_ml_success", 0.0);
        result.addProperty("failed_sessions", 0);

        String sql = "EXEC dbo.PourSession_ListStats_ByUser @actor_user_id = ?, @date_from = ?, @date_to = ?";

        try ( Connection conn = new DBContext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setObject(2, dateFrom);
            ps.setObject(3, dateTo);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.addProperty("total_sessions", rs.getInt("total_sessions"));
                    result.addProperty("total_ml_all", rs.getDouble("total_ml_all"));
                    result.addProperty("total_ml_success", rs.getDouble("total_ml_success"));
                    result.addProperty("failed_sessions", rs.getInt("failed_sessions"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Lấy các đặc trưng thống kê dòng chảy nâng cao phục vụ chấm điểm Weka từ
     * View PourSession_ML_Features
     */
    public MlFeatureRow getMlFeaturesBySessionId(int sessionId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT actual_ml, duration_s, target_ml, avg_flow, "
                + "COALESCE(avg_flow_sample, 0) AS avg_flow_sample, "
                + "COALESCE(peak_flow_sample, 0) AS peak_flow_sample, "
                + "COALESCE(min_flow_sample, 0) AS min_flow_sample, "
                + "COALESCE(std_flow_sample, 0) AS std_flow_sample, "
                + "flow_sample_count, start_reason, stop_reason, result_code "
                + "FROM dbo.PourSession_ML_Features WHERE session_id = ?";

        try ( Connection conn = new DBContext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MlFeatureRow row = new MlFeatureRow();
                    row.setActualMl(rs.getDouble("actual_ml"));
                    row.setDurationS(rs.getDouble("duration_s"));
                    row.setTargetMl(rs.getDouble("target_ml"));
                    row.setAvgFlow(rs.getDouble("avg_flow"));
                    row.setAvgFlowSample(rs.getDouble("avg_flow_sample"));
                    row.setPeakFlowSample(rs.getDouble("peak_flow_sample"));
                    row.setMinFlowSample(rs.getDouble("min_flow_sample"));
                    row.setStdFlowSample(rs.getDouble("std_flow_sample"));
                    row.setFlowSampleCount(rs.getInt("flow_sample_count"));
                    row.setStartReason(rs.getString("start_reason"));
                    row.setStopReason(rs.getString("stop_reason"));
                    row.setResultCode(rs.getString("result_code"));
                    return row;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
