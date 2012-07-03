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
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class SearchResultTable extends I18NVerticalLayout{

    private I18NTable resultTable;
    private IndexedContainer dataSource;

    public SearchResultTable(IndexedContainer dataSource, I18N i18n) {

        super(i18n);

        this.dataSource = dataSource;
    }

    public I18NTable getResultTable() {
        resultTable = new I18NTable("", this.dataSource, getI18N());
        resultTable.setWidth("100%");
        resultTable.setHeight("200px");
        // selectable
        resultTable.setSelectable(true);
        resultTable.setMultiSelect(false);
        resultTable.setImmediate(true); // react at once when something is
        // selected
        // turn on column reordering and collapsing
        resultTable.setColumnReorderingAllowed(true);
        resultTable.setColumnCollapsingAllowed(true);

        // set column headers
        resultTable.setColumnHeaders(new String[] { "GID", "NAMES", "METHOD", "LOCATION" });

        resultTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return "Click to view germplasm details.";
            }
        });

        return resultTable;
    }

    public void setDataSource(IndexedContainer dataSource) {
        this.dataSource = dataSource;
    }

}
