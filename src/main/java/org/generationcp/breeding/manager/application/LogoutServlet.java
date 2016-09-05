package org.generationcp.breeding.manager.application;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession context = request.getSession(false);
		if(context != null) {
			context.invalidate();
		}

		SecurityContext context1 = SecurityContextHolder.getContext();
		context1.setAuthentication((Authentication)null);

		for(Cookie cookie : request.getCookies()) {
			cookie.setMaxAge(0);
		}

		SecurityContextHolder.clearContext();
		response.getOutputStream().print("BreedingManager successfully logged out");
	}
}
