/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/
package org.generationcp.browser.germplasm.application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.vaadin.terminal.gwt.server.ApplicationServlet;

public class Launcher {

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
		context.setResourceBase("./src");

		ServletHolder germplasmBrowser = new ServletHolder(new ApplicationServlet());
		germplasmBrowser.setInitParameter("application", "org.generationcp.browser.germplasm.application.MainApplication");
		//vaadinLoader.setInitParameter("widgetsets", "org.generationcp.browser.germplasm.application.widgetset.GermplasmBrowserWidgetset");

		ServletHolder germplasmBrowserByPhenotypic= new ServletHolder(new ApplicationServlet());
		germplasmBrowserByPhenotypic.setInitParameter("application", "org.generationcp.browser.germplasm.application.SearchGermplasmByPhenotypic");
		
		context.addServlet(germplasmBrowser, "/GermplasmBrowser/*");
		context.addServlet(germplasmBrowser, "/VAADIN/*");
		context.addServlet(germplasmBrowserByPhenotypic, "/GermplasmBrowser2/*");
		server.setHandler(context);
		server.start();
		System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
//		try {
//			System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
//			System.in.read();
//			System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
//			server.stop();
//			//server.join();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}


	}

}
