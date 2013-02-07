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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.generationcp.browser.germplasm.GermplasmBrowserMain;
import org.generationcp.browser.germplasm.GermplasmDetail;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.GidByPhenotypicQueries;
import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasm.containers.TraitDataIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmSelectedTabChangeListener;
import org.generationcp.browser.germplasmlist.GermplasmListBrowserMain;
import org.generationcp.browser.study.StudyAccordionMenu;
import org.generationcp.browser.study.StudyBrowserMain;
import org.generationcp.browser.study.StudyDetailComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.hibernate.util.HttpRequestAwareUtil;
import org.generationcp.commons.vaadin.actions.UpdateComponentLabelsAction;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The main Vaadin application class for the project.
 *
 */
@Configurable
public class GermplasmStudyBrowserApplication extends SpringContextApplication implements ApplicationContextAware {

    private final static Logger LOG = LoggerFactory.getLogger(GermplasmStudyBrowserApplication.class);

    private static final long serialVersionUID = 1L;
    
    public static final String GERMPLASM_WINDOW_NAME = "germplasm"; 
    public static final String STUDY_WINDOW_NAME = "study";
    public static final String STUDY_DETAILS_PREFIX = "study-";
    public static final String GERMPLASM_DETAILS_PREFIX = "germplasm-";

    private Window window;

    private VerticalLayout rootLayoutForGermplasmBrowser;
    private VerticalLayout rootLayoutForStudyBrowser;
    private VerticalLayout rootLayoutForGermplasmByPhenoTab;
    private VerticalLayout rootLayoutForGermplasmListBrowser;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private UpdateComponentLabelsAction messageSourceListener;

    private ApplicationContext applicationContext;
    
    @Autowired
    private StudyDataManager studyDataManager;
    
    @Autowired
    private TraitDataManager traitDataManager;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void initSpringApplication(ConfigurableWebApplicationContext arg0) {

        // create blank root layouts for the other tabs, the content will be
        // added as the tabs are selected or as the buttons on the WelcomeTab are clicked
        messageSourceListener = new UpdateComponentLabelsAction(this);
        messageSource.addListener(messageSourceListener);

        rootLayoutForGermplasmBrowser = new VerticalLayout();
        rootLayoutForGermplasmListBrowser = new VerticalLayout();
        rootLayoutForStudyBrowser = new VerticalLayout();
        rootLayoutForGermplasmByPhenoTab = new VerticalLayout();

        // initialize Middleware ManagerFactory
        /*        try {
                    initDataSource();
                } catch (Exception e) {
                    log.error(e.toString() + "\n" + e.getStackTrace());
                    return;
                }*/

        window = new Window(messageSource.getMessage(Message.RETRIEVE_GERMPLASM_BY_PHENO_LABEL)); // "Retrieve Germplasms By Phenotypic Data"
        setMainWindow(window);
        setTheme("gcp-default");
        window.setSizeUndefined();

        TabSheet tabSheet = new TabSheet();
        // add listener triggered by selecting tabs, this listener will create
        // the content for the tabs dynamically as needed
        tabSheet.addListener(new GermplasmSelectedTabChangeListener(this));

        // this will be passed to WelcomeTab so that it will have a reference to the root layout of the other tabs
        VerticalLayout layouts[] = new VerticalLayout[4];
        layouts[0] = rootLayoutForGermplasmBrowser;
        layouts[1] = rootLayoutForGermplasmListBrowser;
        layouts[2] = rootLayoutForStudyBrowser;
        layouts[3] = rootLayoutForGermplasmByPhenoTab;

        WelcomeTab welcomeTab = new WelcomeTab(tabSheet, layouts);

        tabSheet.addTab(welcomeTab, messageSource.getMessage(Message.WELCOME_LABEL)); // "Welcome"
        tabSheet.addTab(rootLayoutForGermplasmBrowser, messageSource.getMessage(Message.GERMPLASM_BROWSER_TITLE)); // "Germplasm Browser"
        tabSheet.addTab(rootLayoutForGermplasmListBrowser, messageSource.getMessage(Message.GERMPLASM_LIST_BROWSER_TITLE)); // "Germplasm List Browser"
        tabSheet.addTab(rootLayoutForStudyBrowser, messageSource.getMessage(Message.STUDY_BROWSER_TITLE)); // "Study Browser"
        tabSheet.addTab(rootLayoutForGermplasmByPhenoTab, messageSource.getMessage(Message.GERMPLASM_BY_PHENO_TITLE)); // "Search for Germplasms By Phenotypic Data"

        window.addComponent(tabSheet);

        // Override the existing error handler that shows the stack trace
        setErrorHandler(this);
    }

    @Override
    public Window getWindow(String name) {
        // dynamically create other application-level windows which is associated with specific URLs
        // these windows are the jumping on points to parts of the application
        if (super.getWindow(name) == null) {
            if("germplasm-by-pheno".equals(name)) {
                GidByPhenotypicQueries gidByPhenoQueries = null;
                try {
                    gidByPhenoQueries = new GidByPhenotypicQueries();
                } catch (ConfigException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    if (getMainWindow() != null) {
                        MessageNotifier.showError(getMainWindow(),   // TESTED 
                                messageSource.getMessage(Message.ERROR_CONFIGURATION),
                                (e.getLocalizedMessage() == null ? "</br>"
                                        + messageSource.getMessage(Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR) : "</br>"
                                        + e.getLocalizedMessage()));
                    }
                }
                TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer();

                Window germplasmByPhenoWindow = new Window(messageSource.getMessage(Message.GERMPLASM_BY_PHENO_TITLE)); // "Search for Germplasms By Phenotypic Data"
                germplasmByPhenoWindow.setName("germplasm-by-pheno");
                germplasmByPhenoWindow.setSizeUndefined();
                germplasmByPhenoWindow.addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries, traitDataCon, germplasmByPhenoWindow));
                this.addWindow(germplasmByPhenoWindow);

                return germplasmByPhenoWindow;
            } else if(STUDY_WINDOW_NAME.equals(name)) {
                Window studyBrowserWindow = new Window(messageSource.getMessage(Message.STUDY_BROWSER_TITLE)); // Study
                // Browser
                studyBrowserWindow.setName("study");
                studyBrowserWindow.setSizeUndefined();
                studyBrowserWindow.addComponent(new StudyBrowserMain(true));
                this.addWindow(studyBrowserWindow);
                return studyBrowserWindow;
            } else if(GERMPLASM_WINDOW_NAME.equals(name)) {
                Window germplasmBrowserWindow = new Window(messageSource.getMessage(Message.GERMPLASM_BROWSER_TITLE)); // "Germplasm Browser"
                germplasmBrowserWindow.setName("germplasm");
                germplasmBrowserWindow.setSizeUndefined();
                try {
                    germplasmBrowserWindow.addComponent(new GermplasmBrowserMain(true));
                    this.addWindow(germplasmBrowserWindow);
                    return germplasmBrowserWindow;
                } catch (InternationalizableException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    if (getMainWindow() != null) {
                        MessageNotifier.showError(getMainWindow(), e.getCaption(), e.getDescription());
                    }
                }
            } else if("germplasmlist".equals(name)) {
                Window germplasmListBrowserWindow = new Window(messageSource.getMessage(Message.GERMPLASM_LIST_BROWSER_TITLE)); // "Germplasm List Browser"
                germplasmListBrowserWindow.setName("germplasmlist");
                germplasmListBrowserWindow.setSizeUndefined();
                try {
                    germplasmListBrowserWindow.addComponent(new GermplasmListBrowserMain());
                    this.addWindow(germplasmListBrowserWindow);
                    return germplasmListBrowserWindow;
                } catch (InternationalizableException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    if (getMainWindow() != null) {
                        MessageNotifier.showError(getMainWindow(), e.getCaption(), e.getDescription());
                    }
                }
            } else if(name.startsWith(STUDY_DETAILS_PREFIX)) {
                String studyIdPart = name.substring(name.indexOf("-") + 1);
                try {
                    int studyId = Integer.parseInt(studyIdPart);
                    Window studyDetailsWindow = new Window(messageSource.getMessage(Message.STUDY_DETAILS_TEXT) + " " + studyId);  // "Study Details" + study id
                    studyDetailsWindow.setSizeUndefined();
                    //TODO should disable export functions for this screen
                    studyDetailsWindow.addComponent(new StudyAccordionMenu(studyId, new StudyDetailComponent(studyDataManager, studyId)
                        , studyDataManager, traitDataManager, false, true));
                    this.addWindow(studyDetailsWindow);
                    return studyDetailsWindow;
                } catch (Exception ex) {
                    LOG.error(messageSource.getMessage(Message.ERROR_IN_CREATING_STUDY_DETAILS_WINDOW)  // "Error with creating study details window for"
                            + " " + name + ex.toString() + "\n" + ex.getStackTrace());
                    Window emptyStudyDetailsWindow = new Window(messageSource.getMessage(Message.STUDY_DETAILS_TEXT));  // "Study Details"
                    emptyStudyDetailsWindow.setSizeUndefined();
                    emptyStudyDetailsWindow.addComponent(new Label(messageSource.getMessage(Message.NULL_STUDY_DETAILS)  // "No study details for:"
                            + " " + studyIdPart));
                    this.addWindow(emptyStudyDetailsWindow);
                    return emptyStudyDetailsWindow;
                }
             } else if(name.startsWith(GERMPLASM_DETAILS_PREFIX)) {
                 String gidPart = name.substring(name.indexOf("-") + 1);
                 try {
                     int gid = Integer.parseInt(gidPart);
                     Window germplasmDetailsWindow = new Window(messageSource.getMessage(Message.GERMPLASM_DETAILS_TEXT) + " " + gid);  // "Germplasm Details"
                     germplasmDetailsWindow.setSizeUndefined();
                     GermplasmQueries queries = new GermplasmQueries();
                     GermplasmIndexContainer container = new GermplasmIndexContainer(queries);
                     germplasmDetailsWindow.addComponent(new GermplasmDetail(gid, queries, container, null, null, true));
                     this.addWindow(germplasmDetailsWindow);
                     return germplasmDetailsWindow;
                 } catch (Exception ex) {
                     LOG.error(messageSource.getMessage(Message.ERROR_IN_CREATING_GERMPLASM_DETAILS_WINDOW)   // "Error with creating germplasm details window for"
                             + " " + name + ex.toString() + "\n" + ex.getStackTrace());
                     Window emptyGermplasmDetailsWindow = new Window(messageSource.getMessage(Message.GERMPLASM_DETAILS_TEXT));    // "Germplasm Details"
                     emptyGermplasmDetailsWindow.setSizeUndefined();
                     emptyGermplasmDetailsWindow.addComponent(new Label(messageSource.getMessage(Message.NULL_GERMPLASM_DETAILS)    // "No germplasm details for:"
                             + " " + gidPart));
                     this.addWindow(emptyGermplasmDetailsWindow);
                     return emptyGermplasmDetailsWindow;
                 }
             }

        }

        return super.getWindow(name);
    }

    public void tabSheetSelectedTabChangeAction(TabSheet source) throws InternationalizableException {

        GidByPhenotypicQueries gidByPhenoQueries = null;
        try {
            gidByPhenoQueries = new GidByPhenotypicQueries();
        } catch (ConfigException e) {
            throw new InternationalizableException(e, Message.ERROR_CONFIGURATION, 
                                Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR);  // TESTED 
        }
        final TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer();
        if (source.getSelectedTab() == rootLayoutForGermplasmByPhenoTab) {
            if (rootLayoutForGermplasmByPhenoTab.getComponentCount() == 0) {
                rootLayoutForGermplasmByPhenoTab.addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries, traitDataCon,
                        getMainWindow()));
            }

        } else if (source.getSelectedTab() == rootLayoutForGermplasmBrowser) {
            if (rootLayoutForGermplasmBrowser.getComponentCount() == 0) {
                rootLayoutForGermplasmBrowser.addComponent(new GermplasmBrowserMain(false));
            }
        } else if (source.getSelectedTab() == rootLayoutForStudyBrowser) {
            if (rootLayoutForStudyBrowser.getComponentCount() == 0) {
                rootLayoutForStudyBrowser.addComponent(new StudyBrowserMain(false));
            }
        } else if (source.getSelectedTab() == rootLayoutForGermplasmListBrowser) {
            if (rootLayoutForGermplasmListBrowser.getComponentCount() == 0) {
                rootLayoutForGermplasmListBrowser.addComponent(new GermplasmListBrowserMain());
            }
        }
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
    
    public static GermplasmStudyBrowserApplication get() {
        return get(GermplasmStudyBrowserApplication.class);
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
