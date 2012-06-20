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

package org.generationcp.browser.germplasm;

import org.generationcp.browser.germplasm.listeners.GermplasmSelectedTabChangeListener;
import org.generationcp.middleware.exceptions.QueryException;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

public class GermplasmDetail extends Accordion{

    private GermplasmIndexContainer DataIndexContainer;
    private GermplasmQueries qQuery;
    private GermplasmDetailModel gDetailModel;
    private VerticalLayout layoutNames;
    private VerticalLayout layoutAttributes;
    private VerticalLayout layoutGenerationHistory;
    private VerticalLayout layoutPedigreeTree;
    private VerticalLayout mainLayout;
    private int gid;
    private TabSheet tabSheet;
    private GermplasmIndexContainer dataResultIndexContainer;

    public GermplasmDetail(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer, VerticalLayout mainLayout,
            TabSheet tabSheet) throws QueryException {

        this.qQuery = qQuery;
        this.mainLayout = mainLayout;
        this.gid = gid;
        this.tabSheet = tabSheet;
        this.dataResultIndexContainer = dataResultIndexContainer;
        this.DataIndexContainer = dataResultIndexContainer;
        this.DataIndexContainer = dataResultIndexContainer;
        gDetailModel = this.qQuery.getGermplasmDetails(gid);

        layoutNames = new VerticalLayout();
        layoutAttributes = new VerticalLayout();
        layoutGenerationHistory = new VerticalLayout();
        layoutPedigreeTree = new VerticalLayout();

        this.addTab(new GermplasmCharacteristicsComponent(gDetailModel), "Characteristics");
        this.addTab(layoutNames, "Names");
        this.addTab(layoutAttributes, "Attributes");
        this.addTab(layoutGenerationHistory, "Generation History");
        this.addTab(layoutPedigreeTree, "Pedigree Tree");
        this.addListener(new GermplasmSelectedTabChangeListener(this));
        setSizeFull();

    }

    public void selectedTabChangeAction() throws QueryException {
        Component selected = this.getSelectedTab();
        Tab tab = this.getTab(selected);
        if (tab.getCaption().equals("Names")) {
            if (layoutNames.getComponentCount() == 0) {
                layoutNames.addComponent(new GermplasmNamesComponent(DataIndexContainer, gDetailModel));
            }
        } else if (tab.getCaption().equals("Attributes")) {
            if (layoutAttributes.getComponentCount() == 0) {
                layoutAttributes.addComponent(new GermplasmAttributesComponent(DataIndexContainer, gDetailModel));
            }
        } else if (tab.getCaption().equals("Generation History")) {
            if (layoutGenerationHistory.getComponentCount() == 0) {
                layoutGenerationHistory.addComponent(new GermplasmGenerationHistoryComponent(DataIndexContainer, gDetailModel));
            }
        } else if (tab.getCaption().equals("Pedigree Tree")) {
            if (layoutPedigreeTree.getComponentCount() == 0) {
                layoutPedigreeTree.addComponent(new GermplasmPedigreeTreeComponent(gid, qQuery, dataResultIndexContainer, mainLayout,
                        tabSheet));
            }
        }

    }

}