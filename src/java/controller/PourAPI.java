package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dao.LogDAO;
import dao.PourSessionDAO;
import dao.MlFeatureRow;
import model.User;
import service.WekaService;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PourAPI", urlPatterns = {"/api/pour-session/batch"})
public class PourAPI extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PourAPI.class.getName());
    private static final String DEFAULT_MODEL_PATH = "/WEB-INF";
    private static final double ML_REASON_THRESHOLD = 0.80;

    private String normUpper(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        value = value.trim().toUpperCase();
        return value.isEmpty() ? fallback : value;
    }

    private double getDouble(JsonObject json, String key, double fallback) {
        try {
            if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsDouble();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private boolean getBoolean(JsonObject json, String key, boolean fallback) {
        try {
            if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsBoolean();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private String getString(JsonObject json, String key, String fallback) {
        try {
            if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
                String s = json.get(key).getAsString();
                return (s == null || s.trim().isEmpty()) ? fallback : s;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private void applyCupMissingOverride(JsonObject jsonObject) {
        boolean cupPresent = getBoolean(jsonObject, "cup_present", true);
        String startReason = normUpper(getString(jsonObject, "start_reason", "REMOTE_APP"), "REMOTE_APP");
        String stopReason = normUpper(getString(jsonObject, "stop_reason", "AUTO_PROFILE"), "AUTO_PROFILE");
        String resultCode = normUpper(getString(jsonObject, "result_code", "SUCCESS"), "SUCCESS");

        jsonObject.addProperty("start_reason", startReason);
        jsonObject.addProperty("stop_reason", stopReason);
        jsonObject.addProperty("result_code", resultCode);

        if (!cupPresent) {
            jsonObject.addProperty("stop_reason", "ERROR_ABORT");
            jsonObject.addProperty("result_code", "NO_CUP");
        }
    }

    /**
     * Logic Weka được cải tiến để nạp dữ liệu thống kê tổng hợp từ tầng
     * Database View sau khi lưu
     */
    private void executeWekaWithDatabaseView(int sessionId, JsonObject jsonObject) throws SQLException, ClassNotFoundException {
        String finalStopReason = normUpper(getString(jsonObject, "stop_reason", "AUTO_PROFILE"), "AUTO_PROFILE");
        String finalResultCode = normUpper(getString(jsonObject, "result_code", "SUCCESS"), "SUCCESS");

        boolean mlEligible = !"MANUAL_BUTTON".equals(finalStopReason) && !"NO_CUP".equals(finalResultCode);
        jsonObject.addProperty("ml_eligible", mlEligible);

        if (!mlEligible) {
            jsonObject.addProperty("ml_exclusion_reason", "MANUAL_BUTTON".equals(finalStopReason) ? "MANUAL_BUTTON" : "NO_CUP");
            return;
        }

        PourSessionDAO sessionDAO = new PourSessionDAO();
        MlFeatureRow dbFeatures = sessionDAO.getMlFeaturesBySessionId(sessionId);

        if (dbFeatures != null) {
            WekaService weka = new WekaService(getServletContext().getRealPath(DEFAULT_MODEL_PATH));
            double riskScore = weka.analyzeFeaturesRisk(dbFeatures);

            if (riskScore >= 0.0) {
                jsonObject.addProperty("ml_risk_score", riskScore);
                if (riskScore > ML_REASON_THRESHOLD) {
                    jsonObject.addProperty("ml_reason_json",
                            "[{\"feature_name\":\"Weka_Advanced_Risk\",\"contribution\":" + riskScore + ",\"importance_rank\":1}]"
                    );
                }
            }
        }
    }

    private String processTelemetry(JsonObject jsonObject) {
        if (!jsonObject.has("telemetry") || !jsonObject.get("telemetry").isJsonArray()) {
            return "[]";
        }

        JsonArray telemetry = jsonObject.getAsJsonArray("telemetry");
        Map<Long, JsonObject> flowByTime = new LinkedHashMap<>();
        Map<Long, JsonObject> loadcellByTime = new LinkedHashMap<>();

        for (JsonElement el : telemetry) {
            if (el == null || !el.isJsonObject()) {
                continue;
            }
            JsonObject obj = el.getAsJsonObject();
            if (!obj.has("t_offset_ms") || !obj.has("sensor_type_id")) {
                continue;
            }

            long t = obj.get("t_offset_ms").getAsLong();
            int sensorType = obj.get("sensor_type_id").getAsInt();

            if (sensorType == 2) {
                flowByTime.put(t, obj);
            } else if (sensorType == 3) {
                loadcellByTime.put(t, obj);
            }
        }

        List<Long> times = new ArrayList<>(flowByTime.keySet());
        Collections.sort(times);

        List<Double> flowWindow = new ArrayList<>();
        double lastEma = 0.0;
        boolean isFirstFlow = true;
        Double lastWeight = null;

        for (Long t : times) {
            JsonObject flowObj = flowByTime.get(t);
            JsonObject lcObj = loadcellByTime.get(t);
            if (flowObj == null || !flowObj.has("value")) {
                continue;
            }

            double rawFlow = flowObj.get("value").getAsDouble();
            Double refValue = null;
            if (lcObj != null && lcObj.has("value")) {
                double currentWeight = lcObj.get("value").getAsDouble();
                if (lastWeight != null) {
                    refValue = currentWeight - lastWeight;
                }
                lastWeight = currentWeight;
            }

            flowWindow.add(rawFlow);
            if (flowWindow.size() > 3) {
                flowWindow.remove(0);
            }

            List<Double> sortedWindow = new ArrayList<>(flowWindow);
            Collections.sort(sortedWindow);

            double medianFlow = sortedWindow.size() == 2
                    ? (sortedWindow.get(0) + sortedWindow.get(1)) / 2.0 : sortedWindow.get(sortedWindow.size() / 2);

            double filteredFlow = isFirstFlow ? rawFlow : (0.25 * medianFlow + 0.75 * lastEma);
            isFirstFlow = false;
            lastEma = filteredFlow;

            flowObj.addProperty("filtered_value", filteredFlow);
            if (refValue != null) {
                flowObj.addProperty("reference_type", "LOADCELL_DELTA");
                flowObj.addProperty("reference_value", refValue);
            }
        }
        return telemetry.toString();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            StringBuilder sb = new BufferedReader(request.getReader())
                    .lines()
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);

            if (jsonObject == null) {
                response.setStatus(400);
                response.getWriter().print("{\"status\":\"error\", \"message\":\"Invalid JSON body\"}");
                return;
            }

            if (!jsonObject.has("upload_id") || jsonObject.get("upload_id").isJsonNull()
                    || jsonObject.get("upload_id").getAsString().trim().isEmpty()) {
                jsonObject.addProperty("upload_id", "UP_" + UUID.randomUUID().toString().substring(0, 8));
            }

            String telemetryJsonString = processTelemetry(jsonObject);
            applyCupMissingOverride(jsonObject);

            User actor = new User();
            actor.setUserId(2);
            actor.setRole("SYSTEM");

            LogDAO logDAO = new LogDAO();
            int newSessionId = logDAO.saveSessionBatch(jsonObject, telemetryJsonString, actor);

            if (newSessionId > 0) {
                // Thực thi nạp dữ liệu thống kê và chấm điểm Weka từ cấu trúc View mới
                executeWekaWithDatabaseView(newSessionId, jsonObject);

                response.setStatus(200);
                response.getWriter().print("{\"status\":\"success\", \"session_id\":" + newSessionId + "}");
            } else {
                response.setStatus(500);
                response.getWriter().print("{\"status\":\"error\", \"message\":\"Database SP failed\"}");
            }
        } catch (JsonSyntaxException e) {
            response.setStatus(400);
            response.getWriter().print("{\"status\":\"error\", \"message\":\"Invalid JSON\"}");
        } catch (SQLException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            response.setStatus(500);
            response.getWriter().print("{\"status\":\"error\", \"message\":\"System Error\"}");
        }
    }
}
