
package org.generationcp.breeding.manager.crossingmanager;

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
public class CrossesSummarySettingsComponent extends HorizontalLayout implements BreedingManagerLayout, InitializingBean {

	private static final long serialVersionUID = -862705470562935447L;

	private static final Logger LOG = LoggerFactory.getLogger(CrossesSummarySettingsComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private VerticalLayout additionalDetailsComponent;

	private final CrossingManagerSetting setting;

	public CrossesSummarySettingsComponent(CrossingManagerSetting setting) {
		this.setting = setting;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.initializeAdditionalDetailsComponent();
	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@Override
	public void addListeners() {
		// not implemented
	}

	@Override
	public void layoutComponents() {
		this.setHeight("75px");
		this.setWidth("100%");
		this.addComponent(this.additionalDetailsComponent);
	}

	private void initializeAdditionalDetailsComponent() {
		this.additionalDetailsComponent = new VerticalLayout();
		this.additionalDetailsComponent.setDebugId("additionalDetailsComponent");

		Label additionalDetailsTitle = new Label(this.messageSource.getMessage(Message.HARVEST_DETAILS).toUpperCase());
		additionalDetailsTitle.setDebugId("additionalDetailsTitle");
		additionalDetailsTitle.addStyleName(Bootstrap.Typography.H4.styleName());
		additionalDetailsTitle.addStyleName(AppConstants.CssStyles.BOLD);

		Label harvestLocationLabel = new Label(this.messageSource.getMessage(Message.HARVEST_LOCATION) + ":");
		harvestLocationLabel.setDebugId("harvestLocationLabel");
		harvestLocationLabel.addStyleName(AppConstants.CssStyles.BOLD);

		Integer locationId = this.setting.getAdditionalDetailsSetting().getHarvestLocationId();
		Label harvestLocationValue = this.initializeHarvestLocationValue(locationId);

		Label harvestDateLabel = new Label(this.messageSource.getMessage(Message.DATE_LABEL) + ":");
		harvestDateLabel.setDebugId("harvestDateLabel");
		harvestDateLabel.addStyleName(AppConstants.CssStyles.BOLD);
		Label harvestDateValue = this.initializeHarvestDateValue();

		Label dummyLabel = new Label();
		dummyLabel.setDebugId("dummyLabel");
		dummyLabel.setWidth("30px"); // for spacing only

		// layout components
		HorizontalLayout layout = new HorizontalLayout();
		layout.setDebugId("layout");
		layout.setSpacing(true);
		layout.addComponent(harvestLocationLabel);
		layout.addComponent(harvestLocationValue);
		layout.addComponent(dummyLabel);
		layout.addComponent(harvestDateLabel);
		layout.addComponent(harvestDateValue);

		this.additionalDetailsComponent.addComponent(additionalDetailsTitle);
		this.additionalDetailsComponent.addComponent(layout);

	}

	@SuppressWarnings("deprecation")
	private Label initializeHarvestLocationValue(Integer locationId) {
		Label harvestLocationValue = new Label("-");
		harvestLocationValue.setDebugId("harvestLocationValue");
		if (locationId != null) {
			try {
				Location location = this.germplasmDataManager.getLocationByID(locationId);
				harvestLocationValue.setValue(location.getLname());
			} catch (MiddlewareQueryException e) {
				CrossesSummarySettingsComponent.LOG.error(e.getMessage());
			}
		}
		return harvestLocationValue;
	}

	private Label initializeHarvestDateValue() {
		Label harvestDateValue = new Label("-");
		harvestDateValue.setDebugId("harvestDateValue");
		AdditionalDetailsSetting addSetting = this.setting.getAdditionalDetailsSetting();
		harvestDateValue.setValue(addSetting.getHarvestYear() + " " + addSetting.getHarvestMonth());
		return harvestDateValue;
	}

}
