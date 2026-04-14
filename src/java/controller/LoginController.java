package controller;

import dao.UserDAO;
import model.User;
import java.io.IOException;
import java.net.URLEncoder; // Thêm thư viện này để mã hóa tham số URL
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "LoginController", urlPatterns = {"/LoginController"})
public class LoginController extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        UserDAO dao = new UserDAO();
        HttpSession session = request.getSession();
        User user = null;

        // Lấy lại dữ liệu người dùng đã nhập
        String reqUsername = request.getParameter("username");
        String reqRole = request.getParameter("role");

        // Mã hóa URL để tránh lỗi nếu username chứa ký tự đặc biệt hoặc khoảng trắng
        String safeUsername = (reqUsername != null) ? URLEncoder.encode(reqUsername, "UTF-8") : "";
        String safeRole = (reqRole != null) ? URLEncoder.encode(reqRole, "UTF-8") : "";

        if ("guest_login".equals(action)) {
            user = dao.getAnonymousGuest();
        } else {
            user = dao.authenticate(reqUsername, request.getParameter("password"), reqRole);
        }

        if (user != null) {
            if ("LOCKED".equals(user.getStatus())) {
                // Trả về lỗi kèm theo dữ liệu đã nhập
                response.sendRedirect("login.jsp?error=locked&username=" + safeUsername + "&role=" + safeRole);
            } else if ("DISABLED".equals(user.getStatus())) {
                // Trả về lỗi kèm theo dữ liệu đã nhập
                response.sendRedirect("login.jsp?error=disabled&username=" + safeUsername + "&role=" + safeRole);
            } else {
                session.setAttribute("LOGIN_USER", user);
                response.sendRedirect("DashboardController");
            }
        } else {
            // ĐÃ FIX: Chuyển hướng trang kèm theo cả error, username và role trên URL
            // (Password không được truyền đi nên sẽ tự động bị bỏ trống)
            response.sendRedirect("login.jsp?error=invalid&username=" + safeUsername + "&role=" + safeRole);
        }
    }
}