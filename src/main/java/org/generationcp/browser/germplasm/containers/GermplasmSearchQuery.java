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

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.GermplasmBrowserMain;
import org.generationcp.browser.germplasm.GermplasmSearchResultModel;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
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
 * @author Joyce Avestro
 * 
 */
public class GermplasmSearchQuery implements Query{

    public static final Object GID = "gid";
    public static final Object NAMES = "names";
    public static final Object METHOD = "method";
    public static final Object LOCATION = "location";

    private GermplasmDataManager germplasmDataManager;
    private String searchChoice;
    private String searchValue;
    private int size;

    /**
     * These parameters are passed by the QueryFactory which instantiates
     * objects of this class.
     * 
     */
    public GermplasmSearchQuery(GermplasmDataManager germplasmDataManager, String searchChoice, String searchValue) {
        super();
        this.germplasmDataManager = germplasmDataManager;
        this.searchChoice = searchChoice;
        this.searchValue = searchValue;
        this.size = -1;
    }

    /**
     * This method seems to be called for creating blank items on the Table
     */
    @Override
    public Item constructItem() {
        PropertysetItem item = new PropertysetItem();
        item.addItemProperty(GID, new ObjectProperty<String>(""));
        item.addItemProperty(NAMES, new ObjectProperty<String>(""));
        item.addItemProperty(METHOD, new ObjectProperty<String>(""));
        item.addItemProperty(LOCATION, new ObjectProperty<String>(""));
        return item;
    }

    @Override
    public boolean deleteAllItems() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves the dataset by batches of rows. Used for lazy loading the dataset.
     */
    @Override
    public List<Item> loadItems(int start, int numOfRows) {
        List<Item> items = new ArrayList<Item>();

        List<GermplasmSearchResultModel> germplasms = new ArrayList<GermplasmSearchResultModel>();

        try {
            List<Germplasm> germplasmList;

            if (searchChoice.equals(GermplasmBrowserMain.SEARCH_OPTION_NAME)) {
                if (searchValue.contains("%")) {
                    germplasmList = germplasmDataManager.getGermplasmByName(searchValue, start, numOfRows, Operation.LIKE);
                } else {
                    germplasmList = germplasmDataManager.getGermplasmByName(searchValue, start, numOfRows, Operation.EQUAL);
                }
                for (Germplasm g : germplasmList) {
                    Germplasm gData = g;
                    GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();
                    germplasms.add(setGermplasmSearchResult(gResult, gData));

                }
            } else {
                Germplasm gData = germplasmDataManager.getGermplasmByGID(Integer.parseInt(searchValue));
                GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();

                if (gData != null) {
                    gResult = setGermplasmSearchResult(gResult, gData);
                    germplasms.add(gResult);
                }

            }

        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_GETTING_GERMPLASM_LIST_RESULT_BY_PREFERRED_NAME);
        }

        for (GermplasmSearchResultModel germplasm : germplasms) {
            PropertysetItem item = new PropertysetItem();
            item.addItemProperty(GID, new ObjectProperty<String>(germplasm.getGid().toString()));
            if (germplasm.getNames() != null) {
                item.addItemProperty(NAMES, new ObjectProperty<String>(germplasm.getNames()));
            } else {
                item.addItemProperty(NAMES, new ObjectProperty<String>("-"));
            }
            item.addItemProperty(METHOD, new ObjectProperty<String>(germplasm.getMethod()));
            item.addItemProperty(LOCATION, new ObjectProperty<String>(germplasm.getLocation()));
            items.add(item);
        }

        return items;
    }

    private GermplasmSearchResultModel setGermplasmSearchResult(GermplasmSearchResultModel gResult, Germplasm gData)
            throws InternationalizableException {
        gResult.setGid(gData.getGid());
        gResult.setNames(getGermplasmNames(gData.getGid()));

        Method method = germplasmDataManager.getMethodByID(gData.getMethodId());
        if (method != null) {
            gResult.setMethod(method.getMname());
        } else {
            gResult.setMethod("");
        }

        Location loc = germplasmDataManager.getLocationByID(gData.getLocationId());
        if (loc != null) {
            gResult.setLocation(loc.getLname());
        } else {
            gResult.setLocation("");
        }

        return gResult;
    }

    private String getGermplasmNames(int gid) throws InternationalizableException {

        try {
            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
            StringBuffer germplasmNames = new StringBuffer("");
            int i = 0;
            for (Name n : names) {
                if (i < names.size() - 1) {
                    germplasmNames.append(n.getNval() + ",");
                } else {
                    germplasmNames.append(n.getNval());
                }
                i++;
            }

            return germplasmNames.toString();
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_NAMES_BY_GERMPLASM_ID);
        }
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
        try {
            if(this.size == -1){
                if (searchChoice.equals(GermplasmBrowserMain.SEARCH_OPTION_NAME)) {
                    if (searchValue.contains("%")) {
                        this.size = (int) germplasmDataManager.countGermplasmByName(searchValue, Operation.LIKE);
                    } else {
                        this.size = (int) germplasmDataManager.countGermplasmByName(searchValue, Operation.EQUAL);
                    }
                } else {
                    this.size = germplasmDataManager.getGermplasmByGID(Integer.parseInt(searchValue)) != null ? 1 : 0;
                }
            }
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_GETTING_GERMPLASM_LIST_RESULT_BY_PREFERRED_NAME);
        }

        return this.size;
    }

}
