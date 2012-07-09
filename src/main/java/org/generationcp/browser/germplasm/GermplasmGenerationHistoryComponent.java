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
import com.vaadin.ui.Table;

public class GermplasmGenerationHistoryComponent extends I18NTable{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public GermplasmGenerationHistoryComponent(GermplasmIndexContainer DataIndexContainer, GermplasmDetailModel gDetailModel, I18N i18n) {

        super(i18n);

        IndexedContainer dataSourceGenerationHistory = DataIndexContainer.getGermplasGenerationHistory(gDetailModel);

        this.setContainerDataSource(dataSourceGenerationHistory);
        setSelectable(true);
        setMultiSelect(false);
        setSizeFull();
        setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
        setColumnHeaders(new String[] {
                i18n.getMessage("gid.label"),
                i18n.getMessage("prefname.label")});
    }

}
