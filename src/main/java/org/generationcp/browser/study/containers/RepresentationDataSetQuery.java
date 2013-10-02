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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.study.listeners.GidLinkButtonClickListener;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
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

    private StudyDataManagerImpl studyDataManager;
    private Integer datasetId;
    private List<String> columnIds;
    private boolean fromUrl;    //this is true if this component is created by accessing the Study Details page directly from the URL
    private int size;
    
    /**
     * These parameters are passed by the QueryFactory which instantiates
     * objects of this class.
     * 
     * @param dataManager
     * @param datasetId
     * @param columnIds
     */
    public RepresentationDataSetQuery( StudyDataManagerImpl studyDataManager, Integer datasetId, 
            List<String> columnIds, boolean fromUrl) {
        super();
        this.studyDataManager = studyDataManager;
        this.datasetId = datasetId;
        this.columnIds = columnIds;
        this.fromUrl = fromUrl;
        this.size = -1;
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
        Map<Integer, Item> itemMap = new LinkedHashMap<Integer, Item>();
        List<Experiment> experiments = new ArrayList<Experiment>();
        
        try {
            experiments = studyDataManager.getExperiments(datasetId, start, numOfRows);
        } catch (MiddlewareQueryException ex) {
            // Log error in log file
            LOG.error("Error with getting ounitids for representation: " + datasetId + "\n" + ex.toString());
            experiments = new ArrayList<Experiment>();
        }

        if (!experiments.isEmpty()) {
            
            for(Experiment experiment : experiments){
                List<Variable> variables = new ArrayList<Variable>();
                
                VariableList factors = experiment.getFactors();
                if(factors != null){
                    variables.addAll(factors.getVariables());
                }
                
                VariableList variates = experiment.getVariates();
                if(variates != null){
                    variables.addAll(variates.getVariables());
                }
                
                for(Variable variable : variables){
                    String columnId = new StringBuffer().append(variable.getVariableType().getId()).append("-")
                            .append(variable.getVariableType().getLocalName()).toString();
                    
                    //check factor name, if it's a GID, then make the GID as a link. else, show it as a value only
                    //make GID as link only if the page wasn't directly accessed from the URL
                    if ("GID".equals(variable.getVariableType().getLocalName().trim()) && !fromUrl) {
                        // get Item for ounitid
                        Item item = itemMap.get(Integer.valueOf(experiment.getId()));
                        if (item == null) {
                            // not yet in map so create a new Item and add to map
                            item = new PropertysetItem();
                            itemMap.put(Integer.valueOf(experiment.getId()), item);
                        }
                        
                        String value = variable.getValue();
                        if(value != null){
                            Button gidButton = new Button(value.trim(), new GidLinkButtonClickListener(value.trim()));
                            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                            gidButton.setDescription("Click to view Germplasm information");
                            item.addItemProperty(columnId, new ObjectProperty<Button>(gidButton));
                        } else{
                            item.addItemProperty(columnId, null);
                        }
                    //end GID link creation
                    } else {
                        Item item = itemMap.get(Integer.valueOf(experiment.getId()));
                        if (item == null) {
                            // not yet in map so create a new Item and add to map
                            item = new PropertysetItem();
                            itemMap.put(Integer.valueOf(experiment.getId()), item);
                        }
    
                        //check if the variable value is a number to remove decimal portion if there is no value after the decimal point
                        String value = variable.getDisplayValue();
                        if(value != null){
                            try{
                                Double doubleValue = Double.valueOf(value);
                                if (doubleValue % 1.0 > 0) {
                                    item.addItemProperty(columnId, new ObjectProperty<String>(value));
                                } else {
                                    item.addItemProperty(columnId, new ObjectProperty<String>(String.format("%.0f",doubleValue)));
                                }
                            } catch(NumberFormatException ex){
                                //add value as String
                                item.addItemProperty(columnId, new ObjectProperty<String>(value));
                            }
                        } else{
                            item.addItemProperty(columnId, null);
                        }
                    }
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
        if(this.size == -1){
            try {
                Long count = Long.valueOf(studyDataManager.countExperiments(this.datasetId));
                this.size = count.intValue(); 
            } catch (MiddlewareQueryException ex) {
                LOG.error("Error with getting experiments for dataset: " + datasetId + "\n" + ex.toString());
            
            }
        }
        
        return this.size;
    }

}
