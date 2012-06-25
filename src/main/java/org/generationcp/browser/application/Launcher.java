/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.application;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.generationcp.browser.util.SpringApplicationServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import com.vaadin.terminal.gwt.server.ApplicationServlet;

public class Launcher{

    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setMaxIdleTime(0); // in ms, 0 for infinite timeout
        connector.setSoLingerTime(-1);
        connector.setPort(8080);
        server.setConnectors(new Connector[] { connector });

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("./src");
        
        context.addEventListener(new ContextLoaderListener());
        context.addEventListener(new RequestContextListener());
        
        context.setInitParameter("contextConfigLocation", "classpath*:applicationContext.xml");

        ServletHolder germplasmBrowser = new ServletHolder(new ApplicationServlet());
        // germplasmBrowser.setInitParameter("application",
        // "org.generationcp.browser.germplasm.application.MainApplication");
        germplasmBrowser.setInitParameter("application2", 
        		"org.generationcp.browser.application.GermplasmBrowserOnlyApplication");
        // vaadinLoader.setInitParameter("widgetsets",
        // "org.generationcp.browser.germplasm.application.widgetset.GermplasmBrowserWidgetset");

        ServletHolder germplasmBrowserByPhenotypic = new ServletHolder(new SpringApplicationServlet());
        germplasmBrowserByPhenotypic.setInitParameter("application",
                "org.generationcp.browser.application.GermplasmStudyBrowserApplication");

        context.addServlet(germplasmBrowser, "/GermplasmBrowser/*");
        context.addServlet(germplasmBrowser, "/VAADIN/*");
        context.addServlet(germplasmBrowserByPhenotypic, "/GermplasmStudyBrowser/*");
        server.setHandler(context);
        server.start();
        LOG.info(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
        // System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
        // try {
        // System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
        // System.in.read();
        // System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
        // server.stop();
        // //server.join();
        // } catch (Exception e) {
        // e.printStackTrace();
        // System.exit(1);
        // }

    }

}
