import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletResponse;

final class Html {
	private Html() {
	}

	static PrintWriter begin(HttpServletResponse response, String title) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.print("<!doctype html><html lang='es'><head><meta charset='UTF-8'>"
				+ "<meta name='viewport' content='width=device-width, initial-scale=1'>"
				+ "<title>" + escape(title) + "</title><link rel='stylesheet' href='"
				+ response.encodeURL("assets/styles.css?v=rosa-3") + "'><script defer src='"
				+ response.encodeURL("assets/theme.js?v=rosa-3") + "'></script></head><body><div class='page-tools'><button id='theme-toggle' class='theme-toggle' type='button' "
				+ "aria-label='Cambiar a modo oscuro' aria-pressed='false'><span class='theme-icon' aria-hidden='true'>☾</span>"
				+ "<span class='theme-label'>Modo oscuro</span></button></div><main class='card'>");
		return out;
	}

	static void end(PrintWriter out) {
		out.print("</main></body></html>");
	}

	static String escape(String value) {
		if (value == null) return "";
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}

	static void noCache(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}
}
