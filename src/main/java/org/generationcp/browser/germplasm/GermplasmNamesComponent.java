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

import org.generationcp.browser.i18n.ui.I18NTable;
import org.generationcp.browser.i18n.ui.I18NVerticalLayout;

import com.github.peholmst.i18n4vaadin.I18N;
import com.vaadin.data.util.IndexedContainer;

public class GermplasmNamesComponent extends I18NVerticalLayout{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private I18NTable tableNames;

    public GermplasmNamesComponent(GermplasmIndexContainer DataIndexContainer, GermplasmDetailModel gDetailModel, I18N i18n) {

        super(i18n);

        IndexedContainer dataSourceNames = DataIndexContainer.getGermplasNames(gDetailModel);
        tableNames = new I18NTable("", dataSourceNames, getI18N());
        tableNames.setSelectable(true);
        tableNames.setMultiSelect(false);
        tableNames.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        tableNames.setColumnReorderingAllowed(true);
        tableNames.setColumnCollapsingAllowed(true);
        // set column headers
        tableNames.setColumnHeaders(new String[] { "Type", "Name", "Date", "Location", "Type Desc" });
        tableNames.setSizeFull();

        addComponent(tableNames);
        setSpacing(true);
        setMargin(true);
    }

}
