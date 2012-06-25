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

package org.generationcp.browser.util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

/**
 * An implementation of {@link AbstractApplicationServlet} that integrates
 * Spring with Vaadin.
 * 
 * This is based on Nicolas Frankel's implementation, which can be found here:
 * http://blog.frankel.ch/vaadin-spring-integration
 * 
 * @author Glenn Marintes
 */


public class SpringApplicationServlet extends AbstractApplicationServlet{

    private static final long serialVersionUID = 1L;

    /**
     * Default application bean name in Spring application context.
     */
    private static final String DEFAULT_APP_BEAN_NAME = "application";

    /**
     * Application bean name in Spring application context.
     */
    private String name;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String name = config.getInitParameter("applicationBeanName");
        this.name = name == null ? DEFAULT_APP_BEAN_NAME : name;
    }

    /**
     * Get a new application bean from Spring.
     * 
     * @see AbstractApplicationServlet#getNewApplication(HttpServletRequest)
     */
    @Override
    protected Application getNewApplication(HttpServletRequest request) throws ServletException {
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

        Object bean = wac.getBean(name);
        if (!(bean instanceof Application)) {
            throw new ServletException("Bean " + name + " is not an instance of " + Application.class);
        }

        return (Application) bean;
    }

    /**
     * Get the application class from the bean configured in Spring's context.
     * 
     * @see AbstractApplicationServlet#getApplicationClass()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

        Object bean = wac.getBean(name);
        if (bean == null) {
            throw new ClassNotFoundException("No application bean found under name " + name);
        }

        return (Class) bean.getClass();
    }
}
