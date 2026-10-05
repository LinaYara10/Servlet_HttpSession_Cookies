import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LoginServlet.invalidateSession(request);
		LoginServlet.removeSessionCookie(request, response);
		Html.noCache(response);
		response.sendRedirect(request.getContextPath() + "/login?loggedOut=1");
	}
}
