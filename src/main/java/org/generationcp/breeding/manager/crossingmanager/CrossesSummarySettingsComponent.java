package org.generationcp.breeding.manager.crossingmanager;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossesSummarySettingsComponent extends HorizontalLayout implements BreedingManagerLayout,
		InitializingBean {

	private static final long serialVersionUID = -862705470562935447L;
	
	private static final Logger LOG = LoggerFactory.getLogger(CrossesSummarySettingsComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	private VerticalLayout additionalDetailsComponent;
	
	private CrossingManagerSetting setting;
	
	public CrossesSummarySettingsComponent(CrossingManagerSetting setting){
		this.setting = setting;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void instantiateComponents() {
//		initializeBreedingMethodComponent();
		initializeAdditionalDetailsComponent();
	}

	@Override
	public void initializeValues() {
	}

	@Override
	public void addListeners() {
	}

	@Override
	public void layoutComponents() {
		setHeight("75px");
		setWidth("100%");
		addComponent(additionalDetailsComponent);
	}
	
	private void initializeAdditionalDetailsComponent() {
		additionalDetailsComponent = new VerticalLayout();
		
		Label additionalDetailsTitle = new Label(messageSource.getMessage(Message.HARVEST_DETAILS).toUpperCase());
		additionalDetailsTitle.addStyleName(Bootstrap.Typography.H4.styleName());
		additionalDetailsTitle.addStyleName(AppConstants.CssStyles.BOLD);
		
		Label harvestLocationLabel = new Label(messageSource.getMessage(Message.HARVEST_LOCATION) + ":");
		harvestLocationLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		Integer locationId = setting.getAdditionalDetailsSetting().getHarvestLocationId();
		Label harvestLocationValue = initializeHarvestLocationValue(locationId);
		
		Label harvestDateLabel = new Label(messageSource.getMessage(Message.DATE_LABEL) + ":");
		harvestDateLabel.addStyleName(AppConstants.CssStyles.BOLD);
		Label harvestDateValue = initializeHarvestDateValue();

		Label dummyLabel = new Label();
		dummyLabel.setWidth("30px"); // for spacing only
		
        //layout components
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.addComponent(harvestLocationLabel);
        layout.addComponent(harvestLocationValue);
        layout.addComponent(dummyLabel);
        layout.addComponent(harvestDateLabel);
        layout.addComponent(harvestDateValue);

        additionalDetailsComponent.addComponent(additionalDetailsTitle);
        additionalDetailsComponent.addComponent(layout);
        
	}

	private Label initializeHarvestLocationValue(Integer locationId) {
		Label harvestLocationValue = new Label("-"); 
		if (locationId != null){
			try {
				Location location = germplasmDataManager.getLocationByID(locationId);
				harvestLocationValue.setValue(location.getLname());
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage());
			} 
		}
		return harvestLocationValue;
	}

	private Label initializeHarvestDateValue() {
		Label harvestDateValue = new Label("-");
		AdditionalDetailsSetting addSetting = setting.getAdditionalDetailsSetting();
		harvestDateValue.setValue(addSetting.getHarvestYear() + " " + addSetting.getHarvestMonth());
		return harvestDateValue;
	}

}
