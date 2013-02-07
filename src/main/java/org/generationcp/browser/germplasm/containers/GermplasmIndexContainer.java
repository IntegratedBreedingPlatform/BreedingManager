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

package org.generationcp.browser.germplasm.containers;

import java.util.ArrayList;

import org.generationcp.browser.germplasm.GermplasmBrowserMain;
import org.generationcp.browser.germplasm.GermplasmDetailModel;
import org.generationcp.browser.germplasm.GermplasmNamesAttributesModel;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.GermplasmSearchResultModel;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.StudyInfo;
import org.generationcp.middleware.pojos.report.LotReportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public final class GermplasmIndexContainer{

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmIndexContainer.class);

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

    // Germplasm Inventory Model
    private static final Object GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE = "lotbalance";
    private static final Object GERMPLASM_INVENTORY_LOCATION_NAME = "location";
    private static final Object GERMPLASM_INVENTORY_SCALE_NAME = "scale";
    private static final Object GERMPLASM_INVENTORY_LOT_COMMENT = "lotcomment";

    // Study Information Model
    public static final String STUDY_ID = "studyid";
    public static final String STUDY_NAME = "studyname";
    public static final String STUDY_DESCRIPTION = "description";
    public static final String STUDY_NUMBER_OF_ROWS = "rowCount";

    @SuppressWarnings("unused")
    private static final String GERMPLASM_SEARCH_BY_GID = "GID";

    private static final Object GERMPLASM_PREFNAME = "prefname";

    private GermplasmQueries qQuery;

    public GermplasmIndexContainer(GermplasmQueries qQuery) {
        this.qQuery = qQuery;
    }

    public IndexedContainer getGermplasmResultContainer(String choice, String searchValue)
            throws InternationalizableException {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties - Germplasm Search Result
        container.addContainerProperty(GERMPLASM_GID, Integer.class, 0);
        container.addContainerProperty(GERMPLASM_NAMES, String.class, "");
        container.addContainerProperty(GERMPLASM_METHOD, String.class, "");
        container.addContainerProperty(GERMPLASM_LOCATION, String.class, "");

        ArrayList<GermplasmSearchResultModel> queryByNames = null;
        GermplasmSearchResultModel queryByGid = null;
        if (choice.equals(GermplasmBrowserMain.SEARCH_OPTION_NAME)) {
                queryByNames = qQuery.getGermplasmListResultByPrefStandardizedName(searchValue);
                for (GermplasmSearchResultModel q : queryByNames) {
                    addGermplasmResultContainer(container, q.getGid(), q.getNames(), q.getMethod(), q.getLocation());
                }
        } else {
            queryByGid = qQuery.getGermplasmResultByGID(searchValue);
            if (queryByGid != null){
                addGermplasmResultContainer(container, queryByGid.getGid(), queryByGid.getNames(), queryByGid.getMethod(),
                    queryByGid.getLocation());
            }
        }

        return container;
    }
    
    public LazyQueryContainer getGermplasmResultLazyContainer(GermplasmDataManager germplasmDataManager, String searchChoice, String searchValue)
            throws InternationalizableException {

        GermplasmSearchQueryFactory factory = new GermplasmSearchQueryFactory(germplasmDataManager, searchChoice, searchValue);
        LazyQueryContainer container = new LazyQueryContainer(factory, false, 10);

        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
        container.addContainerProperty(GermplasmSearchQuery.GID, String.class, null);
        container.addContainerProperty(GermplasmSearchQuery.NAMES, String.class, null);
        container.addContainerProperty(GermplasmSearchQuery.METHOD, String.class, null);
        container.addContainerProperty(GermplasmSearchQuery.LOCATION, String.class, null);

        container.getQueryView().getItem(0); // initialize the first batch of data to be displayed
        return container;
    }


    private static void addGermplasmResultContainer(Container container, int gid, String names, String method, String location) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(GERMPLASM_GID).setValue(gid);
        item.getItemProperty(GERMPLASM_NAMES).setValue(names);
        item.getItemProperty(GERMPLASM_METHOD).setValue(method);
        item.getItemProperty(GERMPLASM_LOCATION).setValue(location);
    }

    public IndexedContainer getGermplasmAttribute(GermplasmDetailModel g) {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        addContainerProperties(container);

        final ArrayList<GermplasmNamesAttributesModel> query = qQuery.getAttributes(g.getGid());
        LOG.info("Size of the query" + query.size());
        for (GermplasmNamesAttributesModel q : query) {
            addGermplasmNamesAttributeContainer(container, q.getType(), q.getTypeDesc(), q.getName(), q.getDate(), q.getLocation());
        }
        return container;
    }

    public IndexedContainer getGermplasmNames(GermplasmDetailModel g) {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        addContainerProperties(container);

        final ArrayList<GermplasmNamesAttributesModel> query = qQuery.getNames(Integer.valueOf(g.getGid()));
        for (GermplasmNamesAttributesModel q : query) {
            addGermplasmNamesAttributeContainer(container, q.getName(), q.getDate(), q.getLocation(), q.getType(), q.getTypeDesc());
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

    private static void addGermplasmNamesAttributeContainer(Container container, String type, String name, String date, String location,
            String typeDesc) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_TYPE).setValue(type);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_NAME).setValue(name);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_DATE).setValue(date);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_LOCATION).setValue(location);
        item.getItemProperty(GERMPLASM_NAMES_ATTRIBUTE_TYPE_DESC).setValue(typeDesc);

    }

    public IndexedContainer getGermplasmGenerationHistory(GermplasmDetailModel G) {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(GERMPLASM_GID, Integer.class, 0);
        container.addContainerProperty(GERMPLASM_PREFNAME, String.class, "");

        final ArrayList<GermplasmDetailModel> query = qQuery.getGenerationHistory(Integer.valueOf(G.getGid()));
        for (GermplasmDetailModel g : query) {
            addGermplasmGenerationHistory(container, g.getGid(), g.getGermplasmPreferredName());
        }
        return container;
    }

    private static void addGermplasmGenerationHistory(Container container, int gid, String prefname) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(GERMPLASM_GID).setValue(gid);
        item.getItemProperty(GERMPLASM_PREFNAME).setValue(prefname);
    }

    /**
        public IndexedContainer getGermplasmListNames(GermplasmDetailModel g) throws InternationalizableException {
                IndexedContainer container = new IndexedContainer();

                // Create the container properties
                container.addContainerProperty(GERMPLASMLIST_NAME, String.class, "");
                container.addContainerProperty(GERMPLASMLIST_DATE, String.class, "");
                container.addContainerProperty(GERMPLASMLIST_DESCRIPTION, String.class, "");


                final ArrayList<GermplasmListData> germplasmListData =(ArrayList<GermplasmListData>) qQuery.getGermplasmListByGID(g.getGid());

                for(GermplasmListData gListData : germplasmListData ){

                        addGermplasmListContainer(container, gListData.getList().getName(), String.valueOf(gListData.getList().getDate()), gListData.getList().getDescription());
                }

                return container;
        }
        

    private static void addGermplasmListContainer(Container container, String name, String date, String description) {
    	Object itemId = container.addItem();
    	Item item = container.getItem(itemId);
    	item.getItemProperty(GERMPLASMLIST_NAME).setValue(name);
    	item.getItemProperty(GERMPLASMLIST_DATE).setValue(date);
    	item.getItemProperty(GERMPLASMLIST_DESCRIPTION).setValue(description);
    }
        **/

    public IndexedContainer getReportOnLots(GermplasmDetailModel g) throws InternationalizableException {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE, String.class, "");
        container.addContainerProperty(GERMPLASM_INVENTORY_LOCATION_NAME, String.class, "");
        container.addContainerProperty(GERMPLASM_INVENTORY_SCALE_NAME, String.class, "");
        container.addContainerProperty(GERMPLASM_INVENTORY_LOT_COMMENT, String.class, "");

        final ArrayList<LotReportRow> lotReportRowData = (ArrayList<LotReportRow>) qQuery.getReportOnLotsByEntityTypeAndEntityId(
                "GERMPLSM", Integer.valueOf(g.getGid()));

        for (LotReportRow lotReportRow : lotReportRowData) {
            addLotReportRowContainer(container, String.valueOf(lotReportRow.getActualLotBalance()),
                    lotReportRow.getLocationOfLot() == null ? null : lotReportRow.getLocationOfLot().getLname(),
                    lotReportRow.getScaleOfLot() == null ? null : lotReportRow.getScaleOfLot().getName(), lotReportRow.getCommentOfLot());
        }

        return container;
    }

    private static void addLotReportRowContainer(Container container, String lotBalance, String locationName, String scaleName,
            String lotComment) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE).setValue(lotBalance);
        item.getItemProperty(GERMPLASM_INVENTORY_LOCATION_NAME).setValue(locationName);
        item.getItemProperty(GERMPLASM_INVENTORY_SCALE_NAME).setValue(scaleName);
        item.getItemProperty(GERMPLASM_INVENTORY_LOT_COMMENT).setValue(lotComment);
    }

    public IndexedContainer getGermplasmStudyInformation(GermplasmDetailModel G) {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(STUDY_ID, Integer.class, 0);
        container.addContainerProperty(STUDY_NAME, String.class, "");
        container.addContainerProperty(STUDY_DESCRIPTION, String.class, "");
        container.addContainerProperty(STUDY_NUMBER_OF_ROWS, Integer.class, 0);

        final ArrayList<StudyInfo> query = (ArrayList<StudyInfo>) qQuery.getGermplasmStudyInfo(Integer.valueOf(G.getGid()));
        for (StudyInfo info : query) {
            addGermplasmStudyInformation(container, info);
        }
        return container;
    }

    private static void addGermplasmStudyInformation(Container container, StudyInfo info) {
    	StudyInfo studyInfo = info;
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty(STUDY_ID).setValue(studyInfo.getId());
        item.getItemProperty(STUDY_NAME).setValue(studyInfo.getName());
        item.getItemProperty(STUDY_DESCRIPTION).setValue(studyInfo.getTitle());
        item.getItemProperty(STUDY_NUMBER_OF_ROWS).setValue(studyInfo.getRowCount());
    }

}
