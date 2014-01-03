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

package org.generationcp.breeding.manager.crossingmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.TemplateCrossingCondition;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCrosses;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;

/**
 * This class contains the absolute layout of UI elements in Cross Info section
 * in "Enter Additional Details..." tab in Crossing Manager application
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class AdditionalDetailsCrossInfoComponent extends AbsoluteLayout 
        implements InitializingBean, InternationalizableComponent, CrossesMadeContainerUpdateListener{

    private static final long serialVersionUID = -1197900610042529900L;
    private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsCrossNameComponent.class);

    private Label harvestDateLabel;
    private Label harvestLocationLabel;
    
    private DateField harvestDtDateField;
    private ComboBox harvestLocComboBox;
    private Map<String, Integer> mapLocation;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    private List<Location> locations;
  
    private CrossesMadeContainer container;
    
    private CheckBox showFavoriteLocationsCheckBox;
    
    public ComboBox getHarvestLocComboBox() {
        return harvestLocComboBox;
    }

    @Override
    public void setCrossesMadeContainer(CrossesMadeContainer container) {
        this.container = container;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {  
        setHeight("95px");
        setWidth("700px");
            
        harvestDateLabel = new Label();
        
        harvestDtDateField = new DateField();
        harvestDtDateField.setResolution(DateField.RESOLUTION_DAY);
        harvestDtDateField.setDateFormat(CrossingManagerMain.DATE_FORMAT);
        harvestDtDateField.setWidth("280px");
        
        harvestLocationLabel = new Label();
        
        harvestLocComboBox = new ComboBox();
        harvestLocComboBox.setWidth("280px");
        harvestLocComboBox.setNullSelectionAllowed(true);
        
        showFavoriteLocationsCheckBox = new CheckBox();
        showFavoriteLocationsCheckBox.setCaption(messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_LOCATIONS));
        showFavoriteLocationsCheckBox.setImmediate(true);
        showFavoriteLocationsCheckBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true));
				//harvestLocComboBox.select(harvestLocComboBox.getItem(harvestLocComboBox.getItemIds().iterator().next()).toString());
				//harvestLocComboBox.select(harvestLocComboBox.getItemIds().iterator().next());
			}
			
		});
        
                
        // layout components
        addComponent(harvestDateLabel, "top:30px;left:0px");
        addComponent(harvestDtDateField, "top:10px;left:120px");
        addComponent(harvestLocationLabel, "top:60px;left:0px");
        addComponent(harvestLocComboBox, "top:40px;left:120px");
        addComponent(showFavoriteLocationsCheckBox, "top:44px;left:410px");

        locations = germplasmDataManager.getAllBreedingLocations();
        populateHarvestLocation();
    }
    
    private void populateHarvestLocation() {
    	populateHarvestLocation(((Boolean) showFavoriteLocationsCheckBox.getValue()).equals(true));
    }
    
    private void populateHarvestLocation(boolean showOnlyFavorites) {
        harvestLocComboBox.removeAllItems();
        mapLocation = new HashMap<String, Integer>();

        if(showOnlyFavorites){
        	try {
        		
				BreedingManagerUtil.populateWithFavoriteLocations(workbenchDataManager, 
						germplasmDataManager, harvestLocComboBox, mapLocation);
				
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR), 
						"Error getting favorite locations!");
			}
			
        } else {
        	populateWithLocations();
        }

    }
    
    private void populateWithLocations(){
    	
		harvestLocComboBox.removeAllItems();
		
    	if (this.container != null && this.container.getCrossesMade() != null && 
                this.container.getCrossesMade().getCrossingManagerUploader() !=null){
    		
            ImportedGermplasmCrosses importedCrosses = this.container.getCrossesMade().getCrossingManagerUploader().getImportedGermplasmCrosses();
            String site = importedCrosses.getImportedConditionValue(TemplateCrossingCondition.SITE.getValue());
            String siteId = importedCrosses.getImportedConditionValue(TemplateCrossingCondition.SITE_ID.getValue());
            
            if(!mapLocation.containsKey(site)){
            	if(site.length() > 0 && siteId.length() > 0){
            		harvestLocComboBox.addItem(site);
            		mapLocation.put(site, Integer.valueOf(siteId));
            		harvestLocComboBox.select(site);
            	}else{
            		harvestLocComboBox.select("");
            	}
            }
        }
        
        for (Location loc : locations) {
        	harvestLocComboBox.addItem(loc.getLocid());
    		harvestLocComboBox.setItemCaption(loc.getLocid(), loc.getLname());
    		mapLocation.put(loc.getLname(), new Integer(loc.getLocid()));
        }
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
        
    @Override
    public void updateLabels() {
        messageSource.setCaption(harvestDateLabel, Message.HARVEST_DATE);
        messageSource.setCaption(harvestLocationLabel, Message.HARVEST_LOCATION);
    }

    @Override
    public boolean updateCrossesMadeContainer() {
        if (validateRequiredFields()){
            
            if (this.container != null && this.container.getCrossesMade() != null && 
                    this.container.getCrossesMade().getCrossesMap()!= null) {
                
                    Integer dateIntValue = 0;
                    Integer harvestLocationId = 0;
                    
                    if(harvestLocComboBox.getValue() != null){
                        harvestLocationId = (Integer) harvestLocComboBox.getValue();
                    }
                    
                    if(harvestDtDateField.getValue() != null){
                        Date harvestDate = (Date) harvestDtDateField.getValue();
                        SimpleDateFormat formatter = new SimpleDateFormat(CrossingManagerMain.DATE_AS_NUMBER_FORMAT);
                                dateIntValue = Integer.parseInt(formatter.format(harvestDate));
                    }
                
                    Map<Germplasm, Name> crossesMap = container.getCrossesMade().getCrossesMap();
                for (Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()){
                    Germplasm germplasm = entry.getKey();
                    germplasm.setLocationId(harvestLocationId);
                    germplasm.setGdate(dateIntValue);
                    
                    Name name = entry.getValue();
                    name.setLocationId(harvestLocationId);
                    name.setNdate(dateIntValue);
                }
                return true;
            }
        }
        return false;
    }

    private boolean validateRequiredFields() {
        return true;
                //validation of these fields are removed for now
            //CrossingManagerUtil.validateRequiredField(getWindow(), harvestDtDateField, messageSource, (String) harvestDateLabel.getCaption()) &&
            //CrossingManagerUtil.validateRequiredField(getWindow(), harvestLocComboBox, messageSource, (String) harvestLocationLabel.getCaption());
    }

}
