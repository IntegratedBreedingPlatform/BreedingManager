package org.generationcp.breeding.manager.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.generationcp.commons.hibernate.util.HttpRequestAwareUtil;
import org.generationcp.commons.vaadin.actions.UpdateComponentLabelsAction;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;


public class BreedingManagerApplication extends SpringContextApplication implements ApplicationContextAware{
    private final static Logger LOG = LoggerFactory.getLogger(BreedingManagerApplication.class);

    private static final long serialVersionUID = 1L;
    
    private Window window;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private UpdateComponentLabelsAction messageSourceListener;

    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void initSpringApplication(ConfigurableWebApplicationContext arg0) {

        window = new Window(messageSource.getMessage(Message.MAIN_WINDOW_CAPTION)); // "Breeding Manager"
        setMainWindow(window);
        setTheme("gcp-default");
        window.setSizeUndefined();

        window.addComponent(new Label("Hello World!"));
        
        // Override the existing error handler that shows the stack trace
        setErrorHandler(this);
    }

    @Override
    public Window getWindow(String name) {
        // dynamically create other application-level windows which is associated with specific URLs
        // these windows are the jumping on points to parts of the application
        
        return super.getWindow(name);
    }

    /** 
     * Override terminalError() to handle terminal errors, to avoid showing the stack trace in the application 
     */
    @Override
    public void terminalError(Terminal.ErrorEvent event) {
        LOG.error("An unchecked exception occurred: ", event.getThrowable());
        event.getThrowable().printStackTrace();
        // Some custom behaviour.
        if (getMainWindow() != null) {
            MessageNotifier.showError(getMainWindow(), messageSource.getMessage(Message.ERROR_INTERNAL),  // TESTED
                    messageSource.getMessage(Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR)
                            + (event.getThrowable().getLocalizedMessage() == null ? "" : "</br>"
                                    + event.getThrowable().getLocalizedMessage()));
        }
    }

    @Override
    public void close() {
        super.close();

        // implement this when we need to do something on session timeout
        messageSource.removeListener(messageSourceListener);

        LOG.debug("Application closed");
    }
    
    public static BreedingManagerApplication get() {
        return get(BreedingManagerApplication.class);
    }

    @Override
    protected void doOnRequestStart(HttpServletRequest request, HttpServletResponse response) {
        super.doOnRequestStart(request, response);
        
        LOG.trace("Request started " + request.getRequestURI() + "?" + request.getQueryString());
        
        synchronized (this) {
            HttpRequestAwareUtil.onRequestEnd(applicationContext, request, response);
        }
    }
    
    @Override
    protected void doOnRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        super.doOnRequestEnd(request, response);
        
        LOG.trace("Request ended " + request.getRequestURI() + "?" + request.getQueryString());
        
        synchronized (this) {
            HttpRequestAwareUtil.onRequestEnd(applicationContext, request, response);
        }
    }

}
