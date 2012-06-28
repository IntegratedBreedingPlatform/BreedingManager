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

public class GermplasmAttributesComponent extends I18NVerticalLayout{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Table tableAttribute;

    public GermplasmAttributesComponent(GermplasmIndexContainer DataIndexContainer, GermplasmDetailModel gDetailModel, I18N i18n) {

    	super(i18n);
    	
        IndexedContainer dataSourceAttribute = DataIndexContainer.getGermplasAttribute(gDetailModel);
        tableAttribute = new Table("", dataSourceAttribute);
        // selectable
        tableAttribute.setSelectable(true);
        tableAttribute.setMultiSelect(false);
        tableAttribute.setImmediate(true); // react at once when something is
                                           // selected

        // turn on column reordering and collapsing
        tableAttribute.setColumnReorderingAllowed(true);
        tableAttribute.setColumnCollapsingAllowed(true);

        // set column headers
        tableAttribute.setColumnHeaders(new String[] { "Type", "Name", "Date", "Location", "Type Desc" });
        tableAttribute.setSizeFull();
        addComponent(tableAttribute);
        setSpacing(true);
        setMargin(true);
    }

}
