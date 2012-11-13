/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * @author Kevin L. Manansala
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/
package org.generationcp.browser.germplasm.containers;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
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
public class GroupRelativesQuery implements Query{
    private final static Logger LOG = LoggerFactory.getLogger(GroupRelativesQuery.class);

    public static final Object GID = "gid";
    public static final Object PREFERRED_NAME = "preferred name";
    
    private GermplasmDataManager dataManager;
    private Integer gid;
    
    /**
     * These parameters are passed by the QueryFactory which instantiates
     * objects of this class.
     * 
     * @param dataManager
     * @param gid
     * @param columnIds
     */
    public GroupRelativesQuery(GermplasmDataManager dataManager, Integer gid) {
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
        item.addItemProperty(GID, new ObjectProperty<String>(""));
        item.addItemProperty(PREFERRED_NAME, new ObjectProperty<String>(""));
        return item;
    }

    @Override
    public boolean deleteAllItems() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves the dataset by batches of rows. Used for lazy loading the
     * dataset.
     */
    @Override
    public List<Item> loadItems(int start, int numOfRows) {
        List<Item> items = new ArrayList<Item>();

        List<Germplasm> germplasms = new ArrayList<Germplasm>();
        try {
            germplasms.addAll(this.dataManager.getGroupRelatives(gid, start, numOfRows));
        } catch (MiddlewareQueryException ex) {
            LOG.error("Error with getting group relatives for gid = " + gid + ": " + ex.getMessage());
            return new ArrayList<Item>();
        }

        for (Germplasm germplasm : germplasms) {
            PropertysetItem item = new PropertysetItem();
            item.addItemProperty(GID, new ObjectProperty<String>(germplasm.getGid().toString()));
            if(germplasm.getPreferredName() != null){
                item.addItemProperty(PREFERRED_NAME, new ObjectProperty<String>(germplasm.getPreferredName().getNval()));
            } else {
                item.addItemProperty(PREFERRED_NAME, new ObjectProperty<String>("-"));
            }
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
            size = ((Long) dataManager.countGroupRelatives(gid)).intValue();
        } catch (MiddlewareQueryException ex) {
            LOG.error("Error with getting number of group relatives for gid: " + gid + "\n" + ex.toString());
        }
        return size;
    }

}
