import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

final class CookiePreferences {
	static final int MAX_AGE = 30 * 24 * 60 * 60;
	static final String[] NAMES = { "nickname", "favoriteActivity", "weeklySummary", "compactMode", "fontSize", "importantDate" };
	String nickname = "";
	String favoriteActivity = "futbol";
	boolean weeklySummary;
	boolean compactMode;
	int fontSize = 16;
	String importantDate = "";
	String invalidCookiesMessage = "";

	static CookiePreferences read(HttpServletRequest request) {
		CookiePreferences value = new CookiePreferences();
		Map<String, String> found = new HashMap<>();
		Set<String> seen = new HashSet<>();
		List<String> warnings = new ArrayList<>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) for (Cookie cookie : cookies) {
			String name = cookie.getName();
			if (isKnownCookie(name)) {
				if (!seen.add(name)) warnings.add("Hay cookies duplicadas para " + name + ".");
				found.putIfAbsent(name, cookie.getValue());
			}
		}

		String encodedNickname = found.get("nickname");
		String nickname = decode(encodedNickname);
		if (encodedNickname != null) {
			if (validNickname(nickname)) value.nickname = nickname;
			else warnings.add("La cookie del apodo no es válida; se usó el valor predeterminado.");
		}
		String activity = found.get("favoriteActivity");
		if (validActivity(activity)) value.favoriteActivity = activity;
		else if (activity != null) warnings.add("La cookie de actividad favorita no es válida; se usó el valor predeterminado.");

		value.weeklySummary = readBoolean(found.get("weeklySummary"), "resumen semanal", warnings);
		value.compactMode = readBoolean(found.get("compactMode"), "vista compacta", warnings);
		String fontSize = found.get("fontSize");
		Integer parsedFontSize = parseFontSize(fontSize);
		if (parsedFontSize != null) value.fontSize = parsedFontSize;
		else if (fontSize != null) warnings.add("La cookie del tamaño de texto no es válida; se usó el valor predeterminado.");
		String date = found.get("importantDate");
		if (date != null && validDate(date)) value.importantDate = date;
		else if (date != null) warnings.add("La cookie de fecha no es válida; se usó el valor predeterminado.");

		String encodedUsername = found.get("uname");
		String username = decode(encodedUsername);
		if (encodedUsername == null) warnings.add("La cookie de usuario no está presente; se usa la sesión activa.");
		else {
			HttpSession session = request.getSession(false);
			Object sessionUsername = session == null ? null : session.getAttribute("username");
			if (!(sessionUsername instanceof String) || !sessionUsername.equals(username))
				warnings.add("La cookie de usuario no coincide con la sesión; se usa la sesión activa.");
		}
		value.invalidCookiesMessage = String.join(" ", warnings.stream().distinct().toList());
		return value;
	}

	private static boolean readBoolean(String raw, String label, List<String> warnings) {
		if ("true".equals(raw)) return true;
		if (raw != null && !"false".equals(raw)) warnings.add("La cookie de " + label + " no es válida; se usó el valor predeterminado.");
		return false;
	}

	private static boolean isKnownCookie(String name) {
		if ("uname".equals(name)) return true;
		for (String knownName : NAMES) if (knownName.equals(name)) return true;
		return false;
	}

	static void save(HttpServletRequest request, HttpServletResponse response, CookiePreferences value) {
		String[] values = { encode(value.nickname), value.favoriteActivity, Boolean.toString(value.weeklySummary),
				Boolean.toString(value.compactMode), Integer.toString(value.fontSize), value.importantDate };
		String path = request.getContextPath();
		if (path == null || path.isEmpty()) path = "/";
		for (int i = 0; i < NAMES.length; i++) {
			Cookie cookie = new Cookie(NAMES[i], values[i]);
			cookie.setPath(path);
			cookie.setMaxAge(MAX_AGE);
			cookie.setHttpOnly(true);
			cookie.setSecure(request.isSecure());
			response.addCookie(cookie);
		}
	}

	static boolean validNickname(String value) {
		if (value == null) return false;
		String clean = value.trim();
		if (clean.length() < 1 || clean.length() > 32) return false;
		for (int i = 0; i < clean.length(); i++) if (Character.isISOControl(clean.charAt(i))) return false;
		return true;
	}

	static boolean validActivity(String value) {
		return "futbol".equals(value) || "videojuegos".equals(value) || "baloncesto".equals(value)
				|| "musica".equals(value) || "cine".equals(value) || "lectura".equals(value)
				|| "cocina".equals(value) || "senderismo".equals(value);
	}

	static Integer parseFontSize(String value) {
		if (value == null || !value.matches("[0-9]{1,2}")) return null;
		try {
			int parsed = Integer.parseInt(value);
			return parsed >= 12 && parsed <= 24 ? parsed : null;
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	static boolean validDate(String value) {
		if (value == null || value.isEmpty()) return true;
		try {
			LocalDate.parse(value);
			return true;
		} catch (DateTimeParseException ex) {
			return false;
		}
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static String decode(String value) {
		if (value == null) return null;
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}
}
