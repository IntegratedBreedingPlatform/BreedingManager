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

import java.util.ArrayList;

import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.Database;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public final class GermplasmIndexContainer{

    // Germplasm SearchResult Model
    private static final Object GERMPLASM_GID = "gid";
    private static final Object GERMPLASM_NAMES = "names";
    private static final Object GERMPLASM_METHOD = "method";
    private static final Object GERMPLASM_LOCATION = "location";

    // GermplasmNamesAttribute Model
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_TYPE = "type";
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_NAME = "name";
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_DATE = "date";
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_LOCATION = "location";
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_TYPE_DESC = "typedesc";

    private static final String GERMPLASM_SEARCH_BY_NAMES = "Names";
    @SuppressWarnings("unused")
	private static final String GERMPLASM_SEARCH_BY_GID = "GID";

    private static final Object GERMPLASM_PREFNAME = "prefname";

    @SuppressWarnings("unused")
	private static String choice;
    @SuppressWarnings("unused")
	private static String searchValue;

    private GermplasmQueries qQuery;

    public GermplasmIndexContainer(GermplasmQueries qQuery) {
        this.qQuery = qQuery;
    }

    public IndexedContainer getGermplasResultContainer(String choice, String searchValue, Database databaseInstance) throws QueryException {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties - Germplasm Search Result
        container.addContainerProperty(GERMPLASM_GID, Integer.class, 0);
        container.addContainerProperty(GERMPLASM_NAMES, String.class, "");
        container.addContainerProperty(GERMPLASM_METHOD, String.class, "");
        container.addContainerProperty(GERMPLASM_LOCATION, String.class, "");

        ArrayList<GermplasmSearchResultModel> queryByNames = null;
        GermplasmSearchResultModel queryByGid = null;
        if (choice.equals(GERMPLASM_SEARCH_BY_NAMES)) {
            queryByNames = qQuery.getGermplasmListResultByPrefName(choice, searchValue, databaseInstance);
            for (GermplasmSearchResultModel q : queryByNames) {
                addGermplasResultContainer(container, q.getGid(), q.getNames(), q.getMethod(), q.getLocation());
            }
        } else {
            queryByGid = qQuery.getGermplasmResultByGID(searchValue);
            addGermplasResultContainer(container, queryByGid.getGid(), queryByGid.getNames(), queryByGid.getMethod(),
                    queryByGid.getLocation());
        }

        return container;
    }

    private static void addGermplasResultContainer(Container container, int gid, String names, String method, String location) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(GERMPLASM_GID).setValue(gid);
        item.getItemProperty(GERMPLASM_NAMES).setValue(names);
        item.getItemProperty(GERMPLASM_METHOD).setValue(method);
        item.getItemProperty(GERMPLASM_LOCATION).setValue(location);
    }

    public IndexedContainer getGermplasAttribute(GermplasmDetailModel g) {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        addContainerProperties(container);

        final ArrayList<GermplasmNamesAttributesModel> query = g.getAttributes();
        System.out.println("Size of the query" + query.size());
        for (GermplasmNamesAttributesModel q : query) {
            addGermplasNamesAttributeContainer(container, q.getType(), q.getName(), q.getDate(), q.getLocation(), q.getTypeDesc());
        }
        return container;
    }

    public IndexedContainer getGermplasNames(GermplasmDetailModel g) {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        addContainerProperties(container);

        final ArrayList<GermplasmNamesAttributesModel> query = g.getNames();
        for (GermplasmNamesAttributesModel q : query) {
            addGermplasNamesAttributeContainer(container, q.getType(), q.getName(), q.getDate(), q.getLocation(), q.getTypeDesc());
        }
        return container;
    }

    private void addContainerProperties(Container container) {
        container.addContainerProperty(GERMPLASM_NAMES_ATTRIBUTE_TYPE, String.class, "");
        container.addContainerProperty(GERMPLASM_NAMES_ATTRIBUTE_NAME, String.class, "");
        container.addContainerProperty(GERMPLASM_NAMES_ATTRIBUTE_DATE, String.class, "");
        container.addContainerProperty(GERMPLASM_NAMES_ATTRIBUTE_LOCATION, String.class, "");
        container.addContainerProperty(GERMPLASM_NAMES_ATTRIBUTE_TYPE_DESC, String.class, "");
    }

    private static void addGermplasNamesAttributeContainer(Container container, String type, String name, String date, String location,
            String typeDesc) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_TYPE).setValue(type);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_NAME).setValue(name);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_DATE).setValue(date);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_LOCATION).setValue(location);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_TYPE_DESC).setValue(typeDesc);

    }

    public IndexedContainer getGermplasGenerationHistory(GermplasmDetailModel G) {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(GERMPLASM_GID, Integer.class, 0);
        container.addContainerProperty(GERMPLASM_PREFNAME, String.class, "");

        for (GermplasmDetailModel g : G.getGenerationhistory()) {
            addGermplasGenerationHistory(container, g.getGid(), g.getGermplasmPreferredName());
        }
        return container;
    }

    private static void addGermplasGenerationHistory(Container container, int gid, String prefname) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(GERMPLASM_GID).setValue(gid);
        item.getItemProperty(GERMPLASM_PREFNAME).setValue(prefname);

    }

}
