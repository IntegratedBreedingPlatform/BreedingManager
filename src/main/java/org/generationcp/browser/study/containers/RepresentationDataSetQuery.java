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

package org.generationcp.browser.study.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.study.listeners.RepresentationDatasetGidButtonClickListener;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.CharacterDataElement;
import org.generationcp.middleware.pojos.CharacterLevelElement;
import org.generationcp.middleware.pojos.NumericDataElement;
import org.generationcp.middleware.pojos.NumericLevelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.Query;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

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
public class RepresentationDataSetQuery implements Query{

    private final static Logger LOG = LoggerFactory.getLogger(RepresentationDataSetQuery.class);

    private StudyDataManager dataManager;
    private Integer representationId;
    private List<String> columnIds;
    private boolean fromUrl;				//this is true if this component is created by accessing the Study Details page directly from the URL
    
    /**
     * These parameters are passed by the QueryFactory which instantiates
     * objects of this class.
     * 
     * @param dataManager
     * @param representationId
     * @param columnIds
     */
    public RepresentationDataSetQuery(StudyDataManager dataManager, Integer representationId, List<String> columnIds, boolean fromUrl) {
        super();
        this.dataManager = dataManager;
        this.representationId = representationId;
        this.columnIds = columnIds;
        this.fromUrl = fromUrl;
    }

    /**
     * This method seems to be called for creating blank items on the Table
     */
    @Override
    public Item constructItem() {
        PropertysetItem item = new PropertysetItem();
        for (String id : columnIds) {
            item.addItemProperty(id, new ObjectProperty<String>(""));
        }
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
        Map<Integer, Item> itemMap = new HashMap<Integer, Item>();
        List<Integer> ounitids = new ArrayList<Integer>();

        try {
            ounitids = dataManager.getOunitIDsByRepresentationId(representationId, start, numOfRows);
        } catch (MiddlewareQueryException ex) {
            // Log error in log file
            LOG.error("Error with getting ounitids for representation: " + representationId + "\n" + ex.toString());
            ounitids = new ArrayList<Integer>();
        }

        if (!ounitids.isEmpty()) {
            // get character levels
            List<CharacterLevelElement> charLevels = new ArrayList<CharacterLevelElement>();

            try {
                charLevels = dataManager.getCharacterLevelValuesByOunitIdList(ounitids);
            } catch (MiddlewareQueryException ex) {
                LOG.error("Error with getting character level values" + "\n" + ex.toString());
                charLevels = new ArrayList<CharacterLevelElement>();
            }

            for (CharacterLevelElement charLevel : charLevels) {
                String columnId = new StringBuffer().append(charLevel.getFactorId()).append("-").append(charLevel.getFactorName()).toString();
                // get Item for ounitid
                Item item = itemMap.get(charLevel.getOunitId());
                if (item == null) {
                    // not yet in map so create a new Item and add to map
                    item = new PropertysetItem();
                    itemMap.put(charLevel.getOunitId(), item);
                }

                item.addItemProperty(columnId, new ObjectProperty<String>(charLevel.getValue()));
            }

            // get numeric levels
            List<NumericLevelElement> numericLevels = new ArrayList<NumericLevelElement>();

            try {
                numericLevels = dataManager.getNumericLevelValuesByOunitIdList(ounitids);
            } catch (MiddlewareQueryException ex) {
                LOG.error("Error with getting numeric level values" + "\n" + ex.toString());
                numericLevels = new ArrayList<NumericLevelElement>();
            }

            for (NumericLevelElement numericLevel : numericLevels) {
                String columnId = new StringBuffer().append(numericLevel.getFactorId()).append("-").append(numericLevel.getFactorName()).toString();
                //check factor name, if it's a GID, then make the GID as a link. else, show it as a value only
                //make GID as link only if the page wasn't directly accessed from the URL
                if ("GID".equals(numericLevel.getFactorName().trim()) && !fromUrl) {
                    // get Item for ounitid
                    Item item = itemMap.get(numericLevel.getOunitId());
                    if (item == null) {
                        // not yet in map so create a new Item and add to map
                        item = new PropertysetItem();
                        itemMap.put(numericLevel.getOunitId(), item);
                    }
                    
                   	String gid = String.format("%.0f",numericLevel.getValue());

                   	Button gidButton = new Button(gid, new RepresentationDatasetGidButtonClickListener(gid));
                    gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                    gidButton.setDescription("Click to view Germplasm information");

                    item.addItemProperty(columnId, new ObjectProperty<Button>(gidButton));
                //end GID link creation
                } else {
                    // get Item for ounitid
                    Item item = itemMap.get(numericLevel.getOunitId());
                    if (item == null) {
                        // not yet in map so create a new Item and add to map
                        item = new PropertysetItem();
                        itemMap.put(numericLevel.getOunitId(), item);
                    }
                    
                    if(numericLevel.getValue() != null){
                        item.addItemProperty(columnId, new ObjectProperty<String>(numericLevel.getValue().toString()));
                    } else{
                        item.addItemProperty(columnId, null);
                    }
                }
            }

            // get character data
            List<CharacterDataElement> characterDatas = new ArrayList<CharacterDataElement>();

            try {
                characterDatas = dataManager.getCharacterDataValuesByOunitIdList(ounitids);
            } catch (MiddlewareQueryException ex) {
                LOG.error("Error with getting character data values" + "\n" + ex.toString());
                characterDatas = new ArrayList<CharacterDataElement>();
            }

            for (CharacterDataElement characterData : characterDatas) {
                String columnId = characterData.getVariateId().toString();
                // get Item for ounitid
                Item item = itemMap.get(characterData.getOunitId());
                if (item == null) {
                    // not yet in map so create a new Item and add to map
                    item = new PropertysetItem();
                    itemMap.put(characterData.getOunitId(), item);
                }

                item.addItemProperty(columnId, new ObjectProperty<String>(characterData.getValue()));
            }

            // get numeric data
            List<NumericDataElement> numericDatas = new ArrayList<NumericDataElement>();

            try {
                numericDatas = dataManager.getNumericDataValuesByOunitIdList(ounitids);
            } catch (MiddlewareQueryException ex) {
                LOG.error("Error with getting character data values" + "\n" + ex.toString());
                numericDatas = new ArrayList<NumericDataElement>();
            }

            for (NumericDataElement numericData : numericDatas) {
                String columnId = numericData.getVariateId().toString();
                // get Item for ounitid
                Item item = itemMap.get(numericData.getOunitId());
                if (item == null) {
                    // not yet in map so create a new Item and add to map
                    item = new PropertysetItem();
                    itemMap.put(numericData.getOunitId(), item);
                }

                if(numericData.getValue() != null){
                    item.addItemProperty(columnId, new ObjectProperty<String>(numericData.getValue().toString()));
                } else{
                    item.addItemProperty(columnId, null);
                }
            }
        }

        items.addAll(itemMap.values());
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
            size = ((Long) dataManager.countOunitIDsByRepresentationId(representationId)).intValue();
        } catch (MiddlewareQueryException ex) {
            LOG.error("Error with getting number of ounitids for representation: " + representationId + "\n" + ex.toString());
        }
        return size;
    }

}
