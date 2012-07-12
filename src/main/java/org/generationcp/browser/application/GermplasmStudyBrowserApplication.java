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
import org.generationcp.browser.germplasm.TraitDataIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmSelectedTabChangeListener;
import org.generationcp.browser.study.StudyBrowserMain;
import org.generationcp.commons.actions.UpdateComponentLabelsAction;
import org.generationcp.commons.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.exceptions.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmStudyBrowserApplication extends SpringContextApplication{

    private final static Logger LOG = LoggerFactory.getLogger(GermplasmStudyBrowserApplication.class);

    private static final long serialVersionUID = -2630998412856758023L;

    private Window window;

    private VerticalLayout rootLayoutForGermplasmBrowser;
    private VerticalLayout rootLayoutForStudyBrowser;
    private VerticalLayout rootLayoutForGermplasmByPhenoTab;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private UpdateComponentLabelsAction messageSourceListener;
    
    public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void initSpringApplication(ConfigurableWebApplicationContext arg0) {

        // create blank root layouts for the other tabs, the content will be
        // added as the tabs are selected
        // or as the buttons on the WelcomeTab are clicked
        
        messageSourceListener = new UpdateComponentLabelsAction(this);
        messageSource.addListener(messageSourceListener);
    	
        rootLayoutForGermplasmBrowser = new VerticalLayout();
        rootLayoutForStudyBrowser = new VerticalLayout();
        rootLayoutForGermplasmByPhenoTab = new VerticalLayout();

        // initialize Middleware ManagerFactory
/*        try {
            initDataSource();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
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

        // this will be passed to WelcomeTab so that it will have a reference to
        // the root layout of the other tabs
        VerticalLayout layouts[] = new VerticalLayout[3];
        layouts[0] = rootLayoutForGermplasmBrowser;
        layouts[1] = rootLayoutForStudyBrowser;
        layouts[2] = rootLayoutForGermplasmByPhenoTab;

        WelcomeTab welcomeTab = new WelcomeTab(tabSheet, layouts);

        tabSheet.addTab(welcomeTab, messageSource.getMessage(Message.welcome_label)); // "Welcome"
        tabSheet.addTab(rootLayoutForGermplasmBrowser, messageSource.getMessage(Message.germplasmbrowser_title)); // "Germplasm Browser"
        tabSheet.addTab(rootLayoutForStudyBrowser, messageSource.getMessage(Message.studybrowser_title)); // "Study Browser"
        tabSheet.addTab(rootLayoutForGermplasmByPhenoTab, messageSource.getMessage(Message.germplasm_by_pheno_title)); // "Search for Germplasms By Phenotypic Data"

        window.addComponent(tabSheet);
        

    }

    @Override
    public Window getWindow(String name) {
        // dynamically create other application-level windows which is
        // associated with specific URLs
        // these windows are the jumping on points to parts of the application
        if (super.getWindow(name) == null) {
            if ("germplasm-by-pheno".equals(name)) {
                GidByPhenotypicQueries gidByPhenoQueries = null;
                try {
                    gidByPhenoQueries = new GidByPhenotypicQueries();
                } catch (ConfigException e) {
                    // Log into log file
                    LOG.warn(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                }
                TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer();

                Window germplasmByPhenoWindow = new Window(messageSource.getMessage(Message.germplasm_by_pheno_title)); // "Search for Germplasms By Phenotypic Data"
                germplasmByPhenoWindow.setName("germplasm-by-pheno");
                germplasmByPhenoWindow.setSizeUndefined();
                try {
                    germplasmByPhenoWindow.addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries, traitDataCon));
                } catch (QueryException e) {
                    // Log into log file
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                }
                this.addWindow(germplasmByPhenoWindow);

                return germplasmByPhenoWindow;
            }

            else if ("study".equals(name)) {

                Window studyBrowserWindow = new Window(messageSource.getMessage(Message.studybrowser_title)); // Study
                // Browser
                studyBrowserWindow.setName("study");
                studyBrowserWindow.setSizeUndefined();
                studyBrowserWindow.addComponent(new StudyBrowserMain());
                this.addWindow(studyBrowserWindow);
                return studyBrowserWindow;
            }

            else if ("germplasm".equals(name)) {

                Window germplasmBrowserWindow = new Window(messageSource.getMessage(Message.germplasmbrowser_title)); // "Germplasm Browser"
                germplasmBrowserWindow.setName("germplasm");
                germplasmBrowserWindow.setSizeUndefined();
                germplasmBrowserWindow.addComponent(new GermplasmBrowserMain());
                this.addWindow(germplasmBrowserWindow);
                return germplasmBrowserWindow;
            }

        }

        return super.getWindow(name);
    }

    public void tabSheetSelectedTabChangeAction(TabSheet source) {

        GidByPhenotypicQueries gidByPhenoQueries = null;
        try {
            gidByPhenoQueries = new GidByPhenotypicQueries();
        } catch (ConfigException e) {
            // Log into log file
            LOG.warn(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
        }
        final TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer();
        if (source.getSelectedTab() == rootLayoutForGermplasmByPhenoTab) {
            if (rootLayoutForGermplasmByPhenoTab.getComponentCount() == 0) {
                try {
                    rootLayoutForGermplasmByPhenoTab
                            .addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries, traitDataCon));
                } catch (QueryException e) {
                    // Log into log file
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                }
            }

        } else if (source.getSelectedTab() == rootLayoutForGermplasmBrowser) {
            if (rootLayoutForGermplasmBrowser.getComponentCount() == 0) {
                rootLayoutForGermplasmBrowser.addComponent(new GermplasmBrowserMain());
            }
        } else if (source.getSelectedTab() == rootLayoutForStudyBrowser) {
            if (rootLayoutForStudyBrowser.getComponentCount() == 0) { 
                rootLayoutForStudyBrowser.addComponent(new StudyBrowserMain());
            }
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
