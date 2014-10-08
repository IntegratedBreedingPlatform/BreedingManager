package org.generationcp.breeding.manager.listmanager.util.germplasm;

import java.util.ArrayList;

import org.generationcp.breeding.manager.util.GermplasmDetailModel;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public final class GermplasmIndexContainer{

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmIndexContainer.class);

    // GermplasmNamesAttribute Model
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_TYPE = "type";
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_NAME = "name";
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_DATE = "date";
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_LOCATION = "location";
    private static final Object GERMPLASM_NAMES_ATTRIBUTE_TYPE_DESC = "typedesc";

    // Study Information Model
    public static final String STUDY_ID = "studyid";
    public static final String STUDY_NAME = "studyname";
    public static final String STUDY_DESCRIPTION = "description";
    
    @SuppressWarnings("unused")
    private static final String GERMPLASM_SEARCH_BY_GID = "GID";

    private GermplasmQueries qQuery;

    public GermplasmIndexContainer(GermplasmQueries qQuery) {
        this.qQuery = qQuery;
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

    public IndexedContainer getGermplasmAttribute(GermplasmDetailModel g) throws InternationalizableException{
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        addContainerProperties(container);

        final ArrayList<GermplasmAttributeModel> query = qQuery.getAttributes(g.getGid());
        for (GermplasmAttributeModel q : query) {
            addGermplasmNamesAttributeContainer(container, q.getType(), q.getTypeDesc(), q.getName(), q.getDate(), q.getLocation());
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
}

