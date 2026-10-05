import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/preferences")
public class PreferencesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		show(request, response, CookiePreferences.read(request), List.of(), "");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		List<String> errors = new ArrayList<>();
		CookiePreferences value = new CookiePreferences();
		CookiePreferences currentData = CookiePreferences.read(request);
		value.invalidCookiesMessage = currentData.invalidCookiesMessage;
		String nickname = request.getParameter("nickname");
		value.nickname = nickname == null ? "" : nickname.trim();
		if (!CookiePreferences.validNickname(value.nickname))
			errors.add("El apodo es obligatorio, admite hasta 32 caracteres y no puede incluir controles invisibles.");

		String activity = request.getParameter("favoriteActivity");
		if (!CookiePreferences.validActivity(activity)) errors.add("Selecciona una actividad válida.");
		else value.favoriteActivity = activity;

		String[] summaryValues = request.getParameterValues("weeklySummary");
		if (summaryValues == null) value.weeklySummary = false;
		else if (summaryValues.length != 1 || !("true".equals(summaryValues[0]) || "false".equals(summaryValues[0])))
			errors.add("El resumen semanal debe tener un valor válido.");
		else value.weeklySummary = "true".equals(summaryValues[0]);

		String[] compactValues = request.getParameterValues("compactMode");
		if (compactValues == null) value.compactMode = false;
		else if (compactValues.length != 1 || !("true".equals(compactValues[0]) || "false".equals(compactValues[0])))
			errors.add("La vista compacta debe tener un valor válido.");
		else value.compactMode = "true".equals(compactValues[0]);

		String fontSize = request.getParameter("fontSize");
		Integer parsedFontSize = CookiePreferences.parseFontSize(fontSize);
		if (parsedFontSize == null) errors.add("El tamaño de texto debe ser un número entero entre 12 y 24.");
		else value.fontSize = parsedFontSize;

		String date = request.getParameter("importantDate");
		value.importantDate = date == null ? "" : date.trim();
		if (!CookiePreferences.validDate(value.importantDate)) errors.add("La fecha debe ser válida y tener el formato año-mes-día.");

		if (!errors.isEmpty()) {
			show(request, response, value, errors, "");
			return;
		}
		CookiePreferences.save(request, response, value);
		response.sendRedirect(request.getContextPath() + "/preferences?saved=1");
	}

	private void show(HttpServletRequest request, HttpServletResponse response, CookiePreferences value,
			List<String> errors, String ignored) throws IOException {
		PrintWriter out = Html.begin(response, "Tu espacio personal");
		out.print("<style>:root{font-size:" + value.fontSize + "px}.card{padding:"
				+ (value.compactMode ? "1.15rem" : "clamp(1.25rem,4vw,2rem)") + "}</style>");
		out.print("<h1>Preferencias personales</h1>"
				+ "<p class='muted'>Tus preferencias se guardan durante 30 días y se cargan en tu próxima visita.</p>");
		if ("1".equals(request.getParameter("saved"))) out.print("<p class='success' role='status'>Preferencias guardadas correctamente.</p>");
		if (value.invalidCookiesMessage != null && !value.invalidCookiesMessage.isEmpty())
			out.print("<p class='error' role='status'>" + Html.escape(value.invalidCookiesMessage) + "</p>");
		for (String error : errors) out.print("<p class='error' role='alert'>" + Html.escape(error) + "</p>");
		String action = response.encodeURL("preferences");
		out.print("<h2>Preferencias</h2><div class='preferences-layout'><section class='settings-panel'>"
				+ "<form action='" + action + "' method='post'>"
				+ "<label for='nickname'>Apodo para mostrar</label><input id='nickname' name='nickname' type='text' maxlength='32' required value='"
				+ Html.escape(value.nickname) + "'>"
				+ "<label for='favoriteActivity'>Actividad favorita</label><select id='favoriteActivity' name='favoriteActivity' required>"
				+ option("futbol", "Fútbol", value.favoriteActivity) + option("videojuegos", "Videojuegos", value.favoriteActivity)
				+ option("baloncesto", "Baloncesto", value.favoriteActivity) + option("musica", "Música", value.favoriteActivity)
				+ option("cine", "Cine", value.favoriteActivity) + option("lectura", "Lectura", value.favoriteActivity)
				+ option("cocina", "Cocina", value.favoriteActivity) + option("senderismo", "Senderismo", value.favoriteActivity) + "</select>"
				+ "<label class='check'><input type='checkbox' name='weeklySummary' value='true'"
				+ (value.weeklySummary ? " checked" : "") + "> Recibir resumen semanal</label>"
				+ "<label class='check'><input type='checkbox' name='compactMode' value='true'"
				+ (value.compactMode ? " checked" : "") + "> Usar vista compacta</label>"
				+ "<label for='fontSize'>Tamaño del texto</label>"
				+ "<input id='fontSize' name='fontSize' type='number' min='12' max='24' step='1' required value='"
				+ value.fontSize + "'>"
				+ "<label for='importantDate'>Fecha importante</label><input id='importantDate' name='importantDate' type='date' value='"
				+ Html.escape(value.importantDate) + "'>"
				+ "<button type='submit'>Guardar preferencias</button></form></section>"
				+ "<section class='saved-panel'><h2>Valores leídos de las cookies</h2><div class='cookie-values'>"
				+ valueRow("Apodo", Html.escape(value.nickname))
				+ valueRow("Actividad favorita", activityLabel(value.favoriteActivity))
				+ valueRow("Resumen semanal", value.weeklySummary ? "Activado" : "Desactivado")
				+ valueRow("Vista compacta", value.compactMode ? "Activada" : "Desactivada")
				+ valueRow("Tamaño del texto", value.fontSize + " px")
				+ valueRow("Fecha importante", value.importantDate.isEmpty() ? "Sin fecha" : Html.escape(value.importantDate))
				+ "</div><div class='actions'><a class='button secondary' href='" + response.encodeURL("dashboard")
				+ "'>← Volver al inicio</a><form action='" + response.encodeURL(request.getContextPath() + "/logout")
				+ "' method='post'><button class='secondary' type='submit'>Cerrar sesión</button></form></div></section></div>");
		Html.end(out);
	}

	private String option(String value, String label, String selected) {
		return "<option value='" + value + "'" + (value.equals(selected) ? " selected" : "") + ">" + label + "</option>";
	}

	private String valueRow(String label, String value) {
		return "<div class='cookie-value'><span>" + label + "</span><strong>" + value + "</strong></div>";
	}

	private String activityLabel(String activity) {
		return switch (activity) {
		case "futbol" -> "Fútbol";
		case "videojuegos" -> "Videojuegos";
		case "baloncesto" -> "Baloncesto";
		case "musica" -> "Música";
		case "cine" -> "Cine";
		case "lectura" -> "Lectura";
		case "cocina" -> "Cocina";
		case "senderismo" -> "Senderismo";
		default -> "Fútbol";
		};
	}
}
