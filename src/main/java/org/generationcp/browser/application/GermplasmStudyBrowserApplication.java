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

import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.generationcp.browser.germplasm.GermplasmBrowserMain;
import org.generationcp.browser.germplasm.GidByPhenotypicQueries;
import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
import org.generationcp.browser.germplasm.containers.TraitDataIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmSelectedTabChangeListener;
import org.generationcp.browser.germplasmlist.GermplasmListBrowserMain;
import org.generationcp.browser.study.StudyBrowserMain;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.actions.UpdateComponentLabelsAction;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmStudyBrowserApplication extends SpringContextApplication{

    private final static Logger LOG = LoggerFactory.getLogger(GermplasmStudyBrowserApplication.class);

    private static final long serialVersionUID = 1L;
    
    public static final String GERMPLASM_WINDOW_NAME = "germplasm"; 

    private Window window;

    private VerticalLayout rootLayoutForGermplasmBrowser;
    private VerticalLayout rootLayoutForStudyBrowser;
    private VerticalLayout rootLayoutForGermplasmByPhenoTab;
    private VerticalLayout rootLayoutForGermplasmListBrowser;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private UpdateComponentLabelsAction messageSourceListener;

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

        window = new Window(messageSource.getMessage(Message.retrieve_germplasm_by_pheno_label)); // "Retrieve Germplasms By Phenotypic Data"
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

        tabSheet.addTab(welcomeTab, messageSource.getMessage(Message.welcome_label)); // "Welcome"
        tabSheet.addTab(rootLayoutForGermplasmBrowser, messageSource.getMessage(Message.germplasmbrowser_title)); // "Germplasm Browser"
        tabSheet.addTab(rootLayoutForGermplasmListBrowser, messageSource.getMessage(Message.germplasmlist_browser_title)); // "Germplasm List Browser"
        tabSheet.addTab(rootLayoutForStudyBrowser, messageSource.getMessage(Message.studybrowser_title)); // "Study Browser"
        tabSheet.addTab(rootLayoutForGermplasmByPhenoTab, messageSource.getMessage(Message.germplasm_by_pheno_title)); // "Search for Germplasms By Phenotypic Data"

        window.addComponent(tabSheet);

        // Override the existing error handler that shows the stack trace
        setErrorHandler(this);
    }

    @Override
    public Window getWindow(String name) {
        // dynamically create other application-level windows which is associated with specific URLs
        // these windows are the jumping on points to parts of the application
        if (super.getWindow(name) == null) {
            if ("germplasm-by-pheno".equals(name)) {
                GidByPhenotypicQueries gidByPhenoQueries = null;
                try {
                    gidByPhenoQueries = new GidByPhenotypicQueries();
                } catch (ConfigException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    if (getMainWindow() != null) {
                        MessageNotifier.showError(getMainWindow(),   // TESTED 
                                messageSource.getMessage(Message.error_with_configuration),
                                (e.getLocalizedMessage() == null ? "</br>"
                                        + messageSource.getMessage(Message.error_please_contact_administrator) : "</br>"
                                        + e.getLocalizedMessage()));
                    }
                }
                TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer();

                Window germplasmByPhenoWindow = new Window(messageSource.getMessage(Message.germplasm_by_pheno_title)); // "Search for Germplasms By Phenotypic Data"
                germplasmByPhenoWindow.setName("germplasm-by-pheno");
                germplasmByPhenoWindow.setSizeUndefined();
                germplasmByPhenoWindow.addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries, traitDataCon, germplasmByPhenoWindow));
                this.addWindow(germplasmByPhenoWindow);

                return germplasmByPhenoWindow;
                
            } else if ("study".equals(name)) {
                Window studyBrowserWindow = new Window(messageSource.getMessage(Message.studybrowser_title)); // Study
                // Browser
                studyBrowserWindow.setName("study");
                studyBrowserWindow.setSizeUndefined();
                studyBrowserWindow.addComponent(new StudyBrowserMain());
                this.addWindow(studyBrowserWindow);
                return studyBrowserWindow;
            }

            else if (GERMPLASM_WINDOW_NAME.equals(name)) {

                Window germplasmBrowserWindow = new Window(messageSource.getMessage(Message.germplasmbrowser_title)); // "Germplasm Browser"
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
            }
            
            else if ("germplasmlist".equals(name)) {
                Window germplasmListBrowserWindow = new Window(messageSource.getMessage(Message.germplasmlist_browser_title)); // "Germplasm List Browser"
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
            }

        }

        return super.getWindow(name);
    }

    public void tabSheetSelectedTabChangeAction(TabSheet source) throws InternationalizableException {

        GidByPhenotypicQueries gidByPhenoQueries = null;
        try {
            gidByPhenoQueries = new GidByPhenotypicQueries();
        } catch (ConfigException e) {
            throw new InternationalizableException(e, Message.error_with_configuration, 
                                Message.error_please_contact_administrator);  // TESTED 
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
                rootLayoutForStudyBrowser.addComponent(new StudyBrowserMain());
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
            MessageNotifier.showError(getMainWindow(), messageSource.getMessage(Message.error_internal),  // TESTED
                    messageSource.getMessage(Message.error_please_contact_administrator)
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

}
