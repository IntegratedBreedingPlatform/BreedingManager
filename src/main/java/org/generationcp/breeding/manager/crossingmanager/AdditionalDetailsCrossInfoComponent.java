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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
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
public class AdditionalDetailsCrossInfoComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

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
  
    private CrossingManagerImportFileComponent wizardScreenOne;
    
    
	public AdditionalDetailsCrossInfoComponent(CrossingManagerImportFileComponent wizardScreenOne) {
	    this.wizardScreenOne=wizardScreenOne;
    }

	@Override
	public void afterPropertiesSet() throws Exception {  
		setHeight("95px");
        setWidth("700px");
        	
		harvestDateLabel = new Label();
		addComponent(harvestDateLabel, "top:30px;left:20px");
		
		harvestDtDateField = new DateField();
		harvestDtDateField.setResolution(DateField.RESOLUTION_DAY);
		harvestDtDateField.setDateFormat(CrossingManagerMain.DATE_FORMAT);
		addComponent(harvestDtDateField, "top:10px;left:140px");
		
		
		harvestLocationLabel = new Label();
		addComponent(harvestLocationLabel, "top:60px;left:20px");
		
		harvestLocComboBox = new ComboBox();
		harvestLocComboBox.setWidth("400px");
		addComponent(harvestLocComboBox, "top:40px;left:140px");
		populateHarvestLocation();
		
	}
	
	public void populateHarvestLocation() throws MiddlewareQueryException {
	    // TODO Auto-generated method stub
	    harvestLocComboBox.removeAllItems();
	    List<Location> locations = germplasmDataManager.getAllBreedingLocations();

	    mapLocation = new HashMap<String, Integer>();
	    String site=wizardScreenOne.getCrossingManagerUploader().getSite();
	    String siteId=wizardScreenOne.getCrossingManagerUploader().getSiteId();
	    if(site.length() > 0 && siteId.length() > 0){
		harvestLocComboBox.addItem(site);
		mapLocation.put(site, Integer.valueOf(siteId));
		harvestLocComboBox.select(site);
	    }else{
		harvestLocComboBox.select("");
	    }
	    for (Location loc : locations) {
		harvestLocComboBox.addItem(loc.getLname());
		mapLocation.put(loc.getLname(), new Integer(loc.getLocid()));
	    }


	    //Integer locId = mapLocation.get(harvestLocComboBox.getValue());


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

	
	public ComboBox getHarvestLocComboBox() {
	    return harvestLocComboBox;
	}

}
