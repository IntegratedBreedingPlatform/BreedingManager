/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/

package org.generationcp.browser.germplasm;

import org.generationcp.browser.application.GermplasmBrowserMainApplication;
import org.generationcp.browser.application.StudyBrowserMainApplication;
import org.generationcp.browser.application.WelcomeTab;
import org.generationcp.browser.germplasm.listeners.GermplasmSelectedTabChangeListener;
import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.ManagerFactory;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchGermplasmByPhenotypic extends Application{

    private final static Logger LOG = LoggerFactory.getLogger(SearchGermplasmByPhenotypic.class);

    private static final long serialVersionUID = -2630998412856758023L;

    private Window window;
    private ManagerFactory factory;

    private VerticalLayout rootLayoutForGermplasmBrowser;
    private VerticalLayout rootLayoutForStudyBrowser;
    private VerticalLayout rootLayoutForGermplasmByPhenoTab;

    @Override
    public void init() {

	// create blank root layouts for the other tabs, the content will be
	// added as the tabs are selected
	// or as the buttons on the WelcomeTab are clicked
	rootLayoutForGermplasmBrowser = new VerticalLayout();
	rootLayoutForStudyBrowser = new VerticalLayout();
	rootLayoutForGermplasmByPhenoTab = new VerticalLayout();

	// initialize Middleware ManagerFactory
	try {
	    initDataSource();
	} catch (Exception e1) {
	    System.out.println(e1);
	    e1.printStackTrace();
	    return;
	}

	window = new Window("Retrieve Germplasms By Phenotypic Data");
	setMainWindow(window);
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

	WelcomeTab welcomeTab = new WelcomeTab(tabSheet, this.factory, layouts);

	tabSheet.addTab(welcomeTab, "Welcome");
	tabSheet.addTab(rootLayoutForGermplasmBrowser, "Germplasm Browser");
	tabSheet.addTab(rootLayoutForStudyBrowser, "Study Browser");
	tabSheet.addTab(rootLayoutForGermplasmByPhenoTab, "Search for Germplasms By Phenotypic Data");

	window.addComponent(tabSheet);
    }

    @Override
    public Window getWindow(String name) {
	// dynamically create other application-level windows which is
	// associated with specific URLs
	// these windows are the jumping on points to parts of the application
	if (super.getWindow(name) == null) {
	    if (name.equals("germplasm-by-pheno")) {
		GidByPhenotypicQueries gidByPhenoQueries = null;
		try {
		    gidByPhenoQueries = new GidByPhenotypicQueries(this.factory, this.factory.getStudyDataManager());
		} catch (ConfigException e) {
		    // Log into log file
		    LOG.warn(e.toString() + "\n" + e.getStackTrace());
		    e.printStackTrace();
		}
		TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer(this.factory,
			this.factory.getTraitDataManager());

		Window germplasmByPhenoWindow = new Window("Search for Germplasm By Phenotypic Data");
		germplasmByPhenoWindow.setName("germplasm-by-pheno");
		germplasmByPhenoWindow.setSizeUndefined();
		try {
		    germplasmByPhenoWindow.addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries,
			    traitDataCon));
		} catch (QueryException e) {
		    // Log into log file
		    LOG.error(e.toString() + "\n" + e.getStackTrace());
		    e.printStackTrace();
		}
		this.addWindow(germplasmByPhenoWindow);

		return germplasmByPhenoWindow;
	    }

	    else if (name.equals("study-browser")) {

		Window studyBrowserWindow = new Window("Study Browser");
		studyBrowserWindow.setName("study-browser");
		studyBrowserWindow.setSizeUndefined();
		studyBrowserWindow.addComponent(new StudyBrowserMainApplication(factory));
		this.addWindow(studyBrowserWindow);
		return studyBrowserWindow;
	    }

	    else if (name.equals("germplasm-browser")) {

		Window germplasmBrowserWindow = new Window("Germplasm Browser");
		germplasmBrowserWindow.setName("germplasm-browser");
		germplasmBrowserWindow.setSizeUndefined();
		germplasmBrowserWindow.addComponent(new GermplasmBrowserMainApplication(factory));
		this.addWindow(germplasmBrowserWindow);
		return germplasmBrowserWindow;
	    }

	}

	return super.getWindow(name);
    }

    private void initDataSource() throws Exception {
	this.factory = new DatasourceConfig().getManagerFactory();
    }
    
    
    public void tabSheetSelectedTabChangeAction(TabSheet source) {
	
	GidByPhenotypicQueries gidByPhenoQueries = null;
	try {
	    gidByPhenoQueries = new GidByPhenotypicQueries(factory, factory.getStudyDataManager());
	} catch (ConfigException e) {
	    // Log into log file
	    LOG.warn(e.toString() + "\n" + e.getStackTrace());
	    e.printStackTrace();
	}
	final TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer(factory, factory
		.getTraitDataManager());
	if (source.getSelectedTab() == rootLayoutForGermplasmByPhenoTab) {
	    if (rootLayoutForGermplasmByPhenoTab.getComponentCount() == 0) {
		try {
		    rootLayoutForGermplasmByPhenoTab.addComponent(new SearchGermplasmByPhenotypicTab(
			    gidByPhenoQueries, traitDataCon));
		} catch (QueryException e) {
		    // Log into log file
		    LOG.error(e.toString() + "\n" + e.getStackTrace());
		    e.printStackTrace();
		}
	    }

	} else if (source.getSelectedTab() == rootLayoutForGermplasmBrowser) {
	    if (rootLayoutForGermplasmBrowser.getComponentCount() == 0) {
		rootLayoutForGermplasmBrowser.addComponent(new GermplasmBrowserMainApplication(factory));
	    }
	} else if (source.getSelectedTab() == rootLayoutForStudyBrowser) {
	    if (rootLayoutForStudyBrowser.getComponentCount() == 0) {
		rootLayoutForStudyBrowser.addComponent(new StudyBrowserMainApplication(factory));
	    }
	}
    }


}
