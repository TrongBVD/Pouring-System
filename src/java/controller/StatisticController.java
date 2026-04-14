package controller;

import com.google.gson.JsonObject;
import dao.LogDAO;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.User;

@WebServlet(name = "StatisticController", urlPatterns = {"/StatisticController"})
public class StatisticController extends HttpServlet {

    // Múi giờ của người dùng hiển thị (Ví dụ: Việt Nam)
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    // Múi giờ lưu trong DB
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("LOGIN_USER");

        // Chặn quyền
        if (user == null || "GUEST".equals(user.getRole()) || "OPERATOR".equals(user.getRole())) {
            response.sendRedirect("DashboardController");
            return;
        }

        String range = request.getParameter("range");
        if (range == null || range.isEmpty()) {
            range = "today"; // Mặc định là hôm nay
        }

        // Tính toán khoảng thời gian theo NGÀY GIỜ ĐỊA PHƯƠNG (Việt Nam)
        LocalDateTime localDateFrom = null;
        LocalDateTime localDateTo = null;
        LocalDateTime localNow = LocalDateTime.now(LOCAL_ZONE);
        LocalDate todayLocal = localNow.toLocalDate();

        switch (range) {
            case "today":
                localDateFrom = LocalDateTime.of(todayLocal, LocalTime.MIN);
                localDateTo = localDateFrom.plusDays(1);
                break;
            case "yesterday":
                localDateFrom = LocalDateTime.of(todayLocal.minusDays(1), LocalTime.MIN);
                localDateTo = LocalDateTime.of(todayLocal, LocalTime.MIN);
                break;
            case "3days":
                localDateFrom = LocalDateTime.of(todayLocal.minusDays(2), LocalTime.MIN);
                localDateTo = LocalDateTime.of(todayLocal, LocalTime.MIN).plusDays(1);
                break;
            case "1week":
                localDateFrom = LocalDateTime.of(todayLocal.minusWeeks(1), LocalTime.MIN);
                localDateTo = LocalDateTime.of(todayLocal, LocalTime.MIN).plusDays(1);
                break;
            case "1month":
                localDateFrom = LocalDateTime.of(todayLocal.minusMonths(1), LocalTime.MIN);
                localDateTo = LocalDateTime.of(todayLocal, LocalTime.MIN).plusDays(1);
                break;
            default:
                localDateFrom = LocalDateTime.of(todayLocal, LocalTime.MIN);
                localDateTo = localDateFrom.plusDays(1);
        }

        // CHUYỂN ĐỔI MÚI GIỜ: Từ Giờ Địa Phương sang Giờ UTC để truy vấn DB
        ZonedDateTime zonedLocalFrom = ZonedDateTime.of(localDateFrom, LOCAL_ZONE);
        ZonedDateTime zonedLocalTo = ZonedDateTime.of(localDateTo, LOCAL_ZONE);

        LocalDateTime utcDateFrom = zonedLocalFrom.withZoneSameInstant(UTC_ZONE).toLocalDateTime();
        LocalDateTime utcDateTo = zonedLocalTo.withZoneSameInstant(UTC_ZONE).toLocalDateTime();

        // Khởi tạo DAO
        LogDAO dao = new LogDAO();

        // Gọi DAO với thời gian chuẩn UTC
        JsonObject stats = dao.getUserStats(user.getUserId(), utcDateFrom, utcDateTo);

        // Truyền dữ liệu sang giao diện JSP
        request.setAttribute("selectedRange", range);
        request.setAttribute("totalSessions", stats.get("total_sessions").getAsInt());
        request.setAttribute("totalMlSuccess", stats.get("total_ml_success").getAsDouble());
        request.setAttribute("failedSessions", stats.get("failed_sessions").getAsInt());

        request.getRequestDispatcher("statistics.jsp").forward(request, response);
    }
}
