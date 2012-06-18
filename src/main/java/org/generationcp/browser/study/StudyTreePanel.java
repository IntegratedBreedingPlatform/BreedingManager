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

package org.generationcp.browser.study;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.browser.study.listeners.StudyItemClickListener;
import org.generationcp.browser.study.listeners.StudySelectedTabChangeListener;
import org.generationcp.browser.study.listeners.StudyTreeExpandListener;
import org.generationcp.browser.util.Util;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.generationcp.middleware.pojos.Study;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class StudyTreePanel extends VerticalLayout{

    private final static Logger LOG = LoggerFactory.getLogger(StudyTreePanel.class);

    private Panel panelStudyTree;
    private StudyDataManager studyDataManager;
    private Tree studyTree;
    private static TabSheet tabSheetStudy;
    private HorizontalLayout studyBrowserMainLayout;
    private VerticalLayout studyLayout;
    private TraitDataManager traitDataManager;
    private VerticalLayout layoutVariate;
    private VerticalLayout layoutFactor;
    private VerticalLayout layoutEffect;
    private Accordion accordion;
    private int studyId;

    public StudyTreePanel(ManagerFactory factory, HorizontalLayout studyBrowserMainLayout, Database database) {

	this.studyDataManager = factory.getStudyDataManager();
	this.traitDataManager = factory.getTraitDataManager();
	this.studyBrowserMainLayout = studyBrowserMainLayout;

	setSpacing(true);
	setMargin(true);

	panelStudyTree = new Panel();
	panelStudyTree.setStyleName(Reindeer.PANEL_LIGHT);
	studyLayout = new VerticalLayout();
	tabSheetStudy = new TabSheet();

	studyTree = createStudyTree(database);

	if (database == Database.LOCAL) {
	    Button refreshButton = new Button("Refresh");

	    refreshButton.addListener(new StudyButtonClickListener(this));
	    addComponent(refreshButton);
	}

	panelStudyTree.addComponent(studyTree);
	addComponent(panelStudyTree);

    }

    
    // Called by StudyButtonClickListener
    public void createTree() {
	studyTree = createStudyTree(Database.LOCAL);
	panelStudyTree.removeAllComponents();
	panelStudyTree.addComponent(studyTree);
    }

    private Tree createStudyTree(Database database) {
	List<Study> studyParent = new ArrayList<Study>();

	try {
	    studyParent = this.studyDataManager.getAllTopLevelStudies(0, 100, database);
	} catch (QueryException ex) {
	    // Put in an application log
	    LOG.error(ex.toString() + "\n" + ex.getStackTrace());

	    // TODO an error window in the UI should pop-up for this
	    // System.out.println(ex);
	    ex.printStackTrace();
	    studyParent = new ArrayList<Study>();
	}

	Tree studyTree = new Tree();

	for (Study ps : studyParent) {
	    studyTree.addItem(ps.getId());
	    studyTree.setItemCaption(ps.getId(), ps.getName());
	}

	studyTree.addListener(new StudyTreeExpandListener(this));
	studyTree.addListener(new StudyItemClickListener(this));

	return studyTree;
    }
    
    // Called by StudyItemClickListener    
    public void studyTreeItemClickAction(int studyId){
	try {
	    if (!hasChildStudy(studyId)) {
		createStudyInfoTab(studyId);
	    }
	} catch (NumberFormatException e) {
	    // Log into log file
	    LOG.error(e.toString() + "\n" + e.getStackTrace());
	    e.printStackTrace();
	} catch (QueryException e) {
	    // Log into log file
	    LOG.error(e.toString() + "\n" + e.getStackTrace());
	    e.printStackTrace();
	}

    }

    public void addStudyNode(int parentStudyId) {
	List<Study> studyChildren = new ArrayList<Study>();

	try {
	    studyChildren = this.studyDataManager.getStudiesByParentFolderID(parentStudyId, 0, 500);
	} catch (QueryException ex) {
	    // Put in an application log
	    LOG.error(ex.toString() + "\n" + ex.getStackTrace());

	    // TODO an error window in the UI should pop-up for this
	    // System.out.println(ex);
	    ex.printStackTrace();
	    studyChildren = new ArrayList<Study>();
	}

	for (Study sc : studyChildren) {
	    studyTree.addItem(sc.getId());
	    studyTree.setItemCaption(sc.getId(), sc.getName());
	    studyTree.setParent(sc.getId(), parentStudyId);
	    // check if the study has sub study
	    if (hasChildStudy(sc.getId())) {
		studyTree.setChildrenAllowed(sc.getId(), true);
	    } else {
		studyTree.setChildrenAllowed(sc.getId(), false);
	    }

	}
    }

    private void createStudyInfoTab(int studyId) throws QueryException {
	VerticalLayout layout = new VerticalLayout();
	tabSheetStudy.setWidth("900px");

	if (!Util.isTabExist(tabSheetStudy, getStudyName(studyId))) {
	    layout.addComponent(createStudyAccordionMenu(studyId));
	    Tab tab = tabSheetStudy.addTab(layout, getStudyName(studyId), null);
	    tab.setClosable(true);

	    studyBrowserMainLayout.addComponent(tabSheetStudy);
	    studyBrowserMainLayout.addComponent(studyLayout);
	    tabSheetStudy.setSelectedTab(layout);
	} else {
	    Tab tab = Util.getTabAlreadyExist(tabSheetStudy, getStudyName(studyId));
	    tabSheetStudy.setSelectedTab(tab.getComponent());
	}
    }

    private String getStudyName(int studyId) throws QueryException {
	String s = this.studyDataManager.getStudyByID(studyId).getName();
	return s;
    }

    @SuppressWarnings("serial")
    private Accordion createStudyAccordionMenu(final int studyId) throws QueryException {
	// Create the Accordion.
//	final Accordion accordion = new Accordion();
	this.accordion = new Accordion();
	this.studyId = studyId;

	// Have it take all space available in the layout.
	accordion.setSizeFull();

	layoutVariate = new VerticalLayout();
	layoutFactor = new VerticalLayout();
	layoutEffect = new VerticalLayout();

	accordion.addTab(new StudyDetailComponent(this.studyDataManager, studyId), "Study Detail");
	accordion.addTab(layoutFactor, "Factor");
	accordion.addTab(layoutVariate, "Variates");
	accordion.addTab(layoutEffect, "Effects");

	accordion.addListener(new StudySelectedTabChangeListener(this));
	return accordion;

    }

    // Called by StudySelectedTabChangeListener
    public void accordionSelectedTabChangeAction() {
	Component selected = accordion.getSelectedTab();
	Tab tab = accordion.getTab(selected);
	if (tab.getCaption().equals("Factor")) {
	    if (layoutFactor.getComponentCount() == 0) {
		try {
		    layoutFactor.addComponent(new StudyFactorComponent(studyDataManager, traitDataManager, studyId));
		} catch (QueryException e) {
		    e.printStackTrace();
		}
	    }
	} else if (tab.getCaption().equals("Variates")) {
	    if (layoutVariate.getComponentCount() == 0) {
		try {
		    layoutVariate.addComponent(new StudyVariateComponent(studyDataManager, traitDataManager, studyId));
		} catch (QueryException e) {
		    e.printStackTrace();
		}
	    }
	} else if (tab.getCaption().equals("Effects")) {
	    if (layoutEffect.getComponentCount() == 0) {
		layoutEffect.addComponent(new StudyEffectComponent(studyDataManager, studyId, accordion));

	    }
	}
    }

    private boolean hasChildStudy(int studyId) {

	List<Study> studyChildren = new ArrayList<Study>();

	try {
	    studyChildren = this.studyDataManager.getStudiesByParentFolderID(studyId, 0, 1);
	} catch (QueryException ex) {
	    // Put in an application log
	    LOG.error(ex.toString() + "\n" + ex.getStackTrace());

	    // TODO an error window in the UI should pop-up for this
	    // System.out.println(ex);
	    ex.printStackTrace();
	    studyChildren = new ArrayList<Study>();
	}
	if (studyChildren.size() > 0) {
	    return true;
	}
	return false;
    }
}
