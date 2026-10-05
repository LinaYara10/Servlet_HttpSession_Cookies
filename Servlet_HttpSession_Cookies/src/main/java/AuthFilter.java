import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = { "/dashboard", "/preferences" })
public class AuthFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		Html.noCache(httpResponse);
		HttpSession session = httpRequest.getSession(false);
		if (session == null || !Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
			httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
			return;
		}
		chain.doFilter(request, response);
	}
}
