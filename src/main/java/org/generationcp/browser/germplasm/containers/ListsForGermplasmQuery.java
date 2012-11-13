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
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.Query;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * An implementation of Query which is needed for using the LazyQueryContainer.
 * 
 * Reference:
 * https://vaadin.com/wiki/-/wiki/Main/Lazy%20Query%20Container/#section
 * -Lazy+Query+Container-HowToImplementCustomQueryAndQueryFactory
 * 
 * @author Kevin Manansala
 * 
 */
public class ListsForGermplasmQuery implements Query{

    private final static Logger LOG = LoggerFactory.getLogger(ListsForGermplasmQuery.class);

    public static final Object GERMPLASMLIST_NAME = "name";
    public static final Object GERMPLASMLIST_DATE = "date";
    public static final Object GERMPLASMLIST_DESCRIPTION = "description";

    private GermplasmListManager dataManager;
    private Integer gid;

    /**
     * These parameters are passed by the QueryFactory which instantiates
     * objects of this class.
     * 
     * @param dataManager
     * @param representationId
     * @param columnIds
     */
    public ListsForGermplasmQuery(GermplasmListManager dataManager, Integer gid) {
        super();
        this.dataManager = dataManager;
        this.gid = gid;
    }

    /**
     * This method seems to be called for creating blank items on the Table
     */
    @Override
    public Item constructItem() {
        PropertysetItem item = new PropertysetItem();
        item.addItemProperty(GERMPLASMLIST_NAME, new ObjectProperty<String>(""));
        item.addItemProperty(GERMPLASMLIST_DATE, new ObjectProperty<String>(""));
        item.addItemProperty(GERMPLASMLIST_DESCRIPTION, new ObjectProperty<String>(""));
        return item;
    }

    @Override
    public boolean deleteAllItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Item> loadItems(int start, int numOfRows) {
        List<Item> items = new ArrayList<Item>();

        List<GermplasmListData> listDatas = new ArrayList<GermplasmListData>();
        try {
            listDatas.addAll(this.dataManager.getGermplasmListDataByGID(gid, start, numOfRows));
        } catch (MiddlewareQueryException ex) {
            LOG.error("Error with getting list data for gid = " + gid + ": " + ex.getMessage());
            return new ArrayList<Item>();
        }

        for (GermplasmListData listData : listDatas) {
            PropertysetItem item = new PropertysetItem();
            item.addItemProperty(GERMPLASMLIST_NAME, new ObjectProperty<String>(listData.getList().getName()));
            item.addItemProperty(GERMPLASMLIST_DATE, new ObjectProperty<String>(String.valueOf(listData.getList().getDate())));
            item.addItemProperty(GERMPLASMLIST_DESCRIPTION, new ObjectProperty<String>(listData.getList().getDescription()));
            items.add(item);
        }

        return items;
    }

    @Override
    public void saveItems(List<Item> arg0, List<Item> arg1, List<Item> arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the total number of rows to be displayed on the Table
     */
    @Override
    public int size() {
        int size = 0;
        try {
            size = ((Long) dataManager.countGermplasmListDataByGID(gid)).intValue();
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in countGermplasmListDataByGID in size() " + e.getMessage());
            e.printStackTrace();
        }
        return size;
    }

}
