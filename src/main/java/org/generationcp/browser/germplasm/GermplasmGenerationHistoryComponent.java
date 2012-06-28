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

import org.generationcp.browser.i18n.ui.I18NVerticalLayout;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

public class GermplasmGenerationHistoryComponent extends I18NVerticalLayout{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Table tableGermplasmHistory;

    public GermplasmGenerationHistoryComponent(GermplasmIndexContainer DataIndexContainer, GermplasmDetailModel gDetailModel, I18N i18n) {

    	super(i18n);
    	
        IndexedContainer dataSourceGenerationHistory = DataIndexContainer.getGermplasGenerationHistory(gDetailModel);
        tableGermplasmHistory = new Table("", dataSourceGenerationHistory);
        tableGermplasmHistory.setSelectable(true);
        tableGermplasmHistory.setMultiSelect(false);
        tableGermplasmHistory.setImmediate(true); // react at once when
                                                  // something is selected

        // turn on column reordering and collapsing
        tableGermplasmHistory.setColumnReorderingAllowed(true);
        tableGermplasmHistory.setColumnCollapsingAllowed(true);
        tableGermplasmHistory.setSizeFull();

        // set column headers
        tableGermplasmHistory.setColumnHeaders(new String[] { "GID", "PREFNAME" });
        addComponent(tableGermplasmHistory);
        setSpacing(true);
        setMargin(true);
    }

}
