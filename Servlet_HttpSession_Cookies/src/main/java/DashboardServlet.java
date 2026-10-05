import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("username");
		CookiePreferences preferences = CookiePreferences.read(request);
		PrintWriter out = Html.begin(response, "Inicio");
		out.print("<style>:root{font-size:" + preferences.fontSize + "px}.card{padding:"
				+ (preferences.compactMode ? "1.15rem" : "clamp(1.25rem,4vw,2rem)") + "}</style>");
		out.print("<h1>Bienvenido, " + Html.escape(username) + "</h1>"
				+ "<p>Tu sesión está activa. Desde aquí puedes gestionar tus preferencias personales.</p>"
				+ "<div class='actions'><a class='button' href='" + response.encodeURL("preferences")
				+ "'>Editar preferencias</a><form action='" + response.encodeURL(request.getContextPath() + "/logout")
				+ "' method='post'><button class='secondary' type='submit'>Cerrar sesión</button></form></div>");
		Html.end(out);
	}
}
