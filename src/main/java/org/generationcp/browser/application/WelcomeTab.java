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

import org.generationcp.browser.germplasm.GermplasmBrowserMain;
import org.generationcp.browser.germplasm.GidByPhenotypicQueries;
import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
import org.generationcp.browser.germplasm.TraitDataIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.browser.study.StudyBrowserMain;
import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.browser.i18n.ui.I18NVerticalLayout;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;

/**
 * This class extends a VerticalLayout and is basically a container for the
 * components to be shown on the Welcome tab of the application. From the
 * Welcome tab, you can access the other tabs of the main application window
 * thru the use of the buttons.
 * 
 * @author Kevin Manansala
 * 
 */
public class WelcomeTab extends I18NVerticalLayout{

    private final static Logger LOG = LoggerFactory.getLogger(WelcomeTab.class);
    private static final long serialVersionUID = -917787404988386915L;
    @SuppressWarnings("unused")
	private int screenWidth;

    private I18NVerticalLayout rootLayoutForGermplasmBrowser;

    private I18NVerticalLayout rootLayoutForStudyBrowser;
    private ManagerFactory factory;
    private TabSheet theTabSheet;

    private I18NVerticalLayout rootLayoutForGermplasmByPheno;
    private GidByPhenotypicQueries gidByPhenoQueries;
    private TraitDataIndexContainer traitDataCon;

    private Label welcomeLabel;
    private Label questionLabel;
    private Button germplasmButton;
    private Button studyButton;
    private Button germplasmByPhenoButton;
    
    public WelcomeTab(TabSheet tabSheet, final ManagerFactory factory, I18NVerticalLayout rootLayoutsForOtherTabs[], I18N i18n) {
        super(i18n);
        this.factory = factory;
        this.setSpacing(true);
        this.setMargin(true);
        this.theTabSheet = tabSheet;
        this.gidByPhenoQueries = new GidByPhenotypicQueries(factory, factory.getStudyDataManager());
        this.traitDataCon = new TraitDataIndexContainer(factory, factory.getTraitDataManager());

        welcomeLabel = new Label(i18n.getMessage("welcome.label")); // "<h1>Welcome to the Germplasm and Study Browser</h1>"
        welcomeLabel.setContentMode(Label.CONTENT_XHTML);
        this.addComponent(welcomeLabel);

        questionLabel = new Label(i18n.getMessage("question.label")); // "<h3>What do you want to do?</h3>"
        questionLabel.setContentMode(Label.CONTENT_XHTML);
        this.addComponent(questionLabel);

        germplasmButton = new Button(i18n.getMessage("germplasm.button.label")); // "I want to browse Germplasm information"
        germplasmButton.setWidth(400, UNITS_PIXELS);
        this.rootLayoutForGermplasmBrowser = rootLayoutsForOtherTabs[0];

        germplasmButton.addListener(new GermplasmButtonClickListener(this, i18n));
        this.addComponent(germplasmButton);

        studyButton = new Button(i18n.getMessage("study.button.label")); // "I want to browse Studies and their Datasets"
        studyButton.setWidth(400, UNITS_PIXELS);
        rootLayoutForStudyBrowser = rootLayoutsForOtherTabs[1];

        studyButton.addListener(new StudyButtonClickListener(this, i18n));

        this.addComponent(studyButton);
 
        germplasmByPhenoButton = new Button(i18n.getMessage("germplasmsByPheno.label")); // "I want to retrieve Germplasms by Phenotypic Data"
        germplasmByPhenoButton.setWidth(400, UNITS_PIXELS);
        rootLayoutForGermplasmByPheno = rootLayoutsForOtherTabs[2];

        germplasmByPhenoButton.addListener(new GermplasmButtonClickListener(this, i18n));
        this.addComponent(germplasmByPhenoButton);
    }

    // Called by GermplasmButtonClickListener
    public void browserGermplasmInfoButtonClickAction() {
        if (rootLayoutForGermplasmBrowser.getComponentCount() == 0) {
            rootLayoutForGermplasmBrowser.addComponent(new GermplasmBrowserMain(factory, getI18N()));
            rootLayoutForGermplasmBrowser.addStyleName("addSpacing");
        }

        theTabSheet.setSelectedTab(rootLayoutForGermplasmBrowser);
    }

    // Called by StudyButtonClickListener
    public void browseStudiesAndDataSets() {
        if (rootLayoutForStudyBrowser.getComponentCount() == 0) {
        	rootLayoutForStudyBrowser.setWidth("100%");
            rootLayoutForStudyBrowser.addComponent(new StudyBrowserMain(factory, getI18N()));
            rootLayoutForStudyBrowser.addStyleName("addSpacing");
        }

        theTabSheet.setSelectedTab(rootLayoutForStudyBrowser);

    }

    // Called by GermplasmButtonClickListener
    public void searchGermplasmByPhenotyicDataButtonClickAction() {
        // when the button is clicked, content for the tab is
        // dynamically created
        // creation of content is done only once
        if (rootLayoutForGermplasmByPheno.getComponentCount() == 0) {
            try {
                rootLayoutForGermplasmByPheno.addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries, traitDataCon, getI18N()));
            } catch (QueryException e) {
                // Log into log file
                LOG.error(e.toString() + "\n" + e.getStackTrace());
                e.printStackTrace();
            }
        }
        theTabSheet.setSelectedTab(rootLayoutForGermplasmByPheno);

    }
}
