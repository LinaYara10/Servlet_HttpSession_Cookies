import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Html.noCache(response);
		invalidateSession(request);
		removeSessionCookie(request, response);
		String message = "1".equals(request.getParameter("loggedOut"))
				? "Sesión cerrada. Ingresa nuevamente para continuar." : "";
		showForm(response, message, "", false);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		Html.noCache(response);
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String cleanUsername = username == null ? "" : username.trim();
		String error = validate(cleanUsername, password);
		if (error != null) {
			showForm(response, error, cleanUsername, true);
			return;
		}

		invalidateSession(request);
		HttpSession session = request.getSession(true);
		request.changeSessionId();
		session.setAttribute("authenticated", Boolean.TRUE);
		session.setAttribute("username", cleanUsername);
		addUsernameCookie(request, response, cleanUsername);
		response.sendRedirect(request.getContextPath() + "/dashboard");
	}

	private String validate(String username, String password) {
		if (username.isEmpty()) return "Ingresa tu usuario.";
		if (username.length() < 4 || username.length() > 20)
			return "El usuario debe tener entre 4 y 20 caracteres.";
		if (!Character.isLetter(username.charAt(0)))
			return "El usuario debe comenzar con una letra.";
		boolean hasLetter = false;
		for (int i = 0; i < username.length(); i++) {
			char c = username.charAt(i);
			if (Character.isLetter(c)) hasLetter = true;
			else if (!Character.isDigit(c) && c != '.' && c != '_' && c != '-')
				return "El usuario solo puede contener letras, números, punto, guion y guion bajo.";
		}
		if (!hasLetter) return "El usuario debe incluir al menos una letra; no puede ser solo numérico.";
		if (password == null || password.isEmpty()) return "Ingresa tu contraseña.";
		if (password.length() < 8 || password.length() > 64)
			return "La contraseña debe tener entre 8 y 64 caracteres.";
		if (password.chars().anyMatch(Character::isWhitespace))
			return "La contraseña no puede contener espacios en blanco.";
		if (password.equalsIgnoreCase(username))
			return "La contraseña no puede ser igual al usuario.";
		boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
		boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
		boolean hasNumber = password.chars().anyMatch(Character::isDigit);
		boolean hasSymbol = password.chars().anyMatch(character ->
				!Character.isLetterOrDigit(character) && !Character.isWhitespace(character));
		if (!hasUppercase || !hasLowercase || !hasNumber || !hasSymbol)
			return "La contraseña debe incluir mayúscula, minúscula, número y símbolo.";
		return null;
	}

	private void showForm(HttpServletResponse response, String message, String username, boolean isError)
			throws IOException {
		PrintWriter out = Html.begin(response, "Iniciar sesión");
		out.print("<p class='eyebrow'>ESPACIO PERSONAL</p><h1>Iniciar sesión</h1>"
				+ "<p class='muted'>Ingresa tus datos para continuar a tu espacio.</p>");
		if (!message.isEmpty()) out.print("<p class='" + (isError ? "error" : "success") + "' role='alert'>"
				+ Html.escape(message) + "</p>");
		out.print("<form action='" + response.encodeURL("login") + "' method='post'>"
				+ "<label for='username'>Usuario</label><input id='username' name='username' type='text' "
				+ "value='" + Html.escape(username) + "' minlength='4' maxlength='20' autocomplete='username' required>"
				+ "<small>4–20 caracteres; debe comenzar con una letra. Se permiten letras, números, punto, guion y guion bajo.</small>"
				+ "<label for='password'>Contraseña</label><input id='password' name='password' type='password' "
				+ "minlength='8' maxlength='64' autocomplete='current-password' required>"
				+ "<small>8–64 caracteres, sin espacios; debe incluir mayúscula, minúscula, número y símbolo, y ser distinta del usuario.</small>"
				+ "<button type='submit'>Entrar</button></form>");
		Html.end(out);
	}

	static void removeSessionCookie(HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = new Cookie("JSESSIONID", "");
		String path = request.getContextPath();
		cookie.setPath(path == null || path.isEmpty() ? "/" : path);
		cookie.setMaxAge(0);
		cookie.setHttpOnly(true);
		cookie.setSecure(request.isSecure());
		response.addCookie(cookie);
	}

	private void addUsernameCookie(HttpServletRequest request, HttpServletResponse response, String username) {
		Cookie cookie = new Cookie("uname", URLEncoder.encode(username, StandardCharsets.UTF_8));
		cookie.setPath(cookiePath(request));
		cookie.setMaxAge(30 * 24 * 60 * 60);
		cookie.setHttpOnly(true);
		cookie.setSecure(request.isSecure());
		response.addCookie(cookie);
	}

	private static String cookiePath(HttpServletRequest request) {
		String path = request.getContextPath();
		return path == null || path.isEmpty() ? "/" : path;
	}

	static void invalidateSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) return;
		try {
			session.invalidate();
		} catch (IllegalStateException ignored) {
			// Otra solicitud ya pudo invalidar esta sesión.
		}
	}
}
