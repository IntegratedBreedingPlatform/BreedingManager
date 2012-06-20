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

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class SearchResultTable extends VerticalLayout{

    Table resultTable;
    IndexedContainer dataSource;

    public SearchResultTable(IndexedContainer dataSource) {
        this.dataSource = dataSource;
    }

    public Table getResultTable() {
        resultTable = new Table("", this.dataSource);
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
