package org.generationcp.breeding.manager.application;

import org.generationcp.commons.util.LogoutUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LogoutUtil.manuallyLogout(request, response);
		response.getOutputStream().print("BreedingManager successfully logged out");
	}
}
