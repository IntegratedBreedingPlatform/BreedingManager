package org.generationcp.breeding.manager.crossingmanager.settings;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.customfields.HarvestDateField;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configurable
public class CrossingSettingsOtherDetailsComponent extends CssLayout
		implements BreedingManagerLayout, InternationalizableComponent,
		InitializingBean {

	public enum SaveSettingOption {
		YES, NO
	}

	private static final long serialVersionUID = -4119454332332114156L;

	private static final Logger LOG = LoggerFactory
			.getLogger(CrossingSettingsOtherDetailsComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Resource
	private ContextUtil contextUtil;

	private Label harvestDetailsLabel;

	private ComboBox harvestLocations;
	private CheckBox showFavouriteLocations;
	private Button manageFavoriteLocations;

	private HarvestDateField harvestDateField;

	private Label saveSettingsLabel;

	private TextField settingsNameTextfield;
	private CheckBox setAsDefaultSettingCheckbox;

	private Map<String, Integer> mapLocation;
	private List<Location> locations;

	@Autowired
	private BreedingManagerService breedingManagerService;
	private String programUniqueId;

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}

	@Override
	public void updateLabels() {
		harvestDetailsLabel
				.setValue(messageSource.getMessage(Message.HARVEST_DETAILS).toUpperCase());
		saveSettingsLabel.setValue(messageSource.getMessage(Message.SAVE_SETTINGS).toUpperCase());

		messageSource.setCaption(showFavouriteLocations, Message.SHOW_ONLY_FAVORITE_LOCATIONS);
		messageSource.setCaption(manageFavoriteLocations, Message.MANAGE_LOCATIONS);
		messageSource.setCaption(setAsDefaultSettingCheckbox,
				Message.SET_AS_DEFAULT_FOR_THIS_PROGRAM_OVERRIDES_PREVIOUS_DEFAULTS);
	}

	@Override
	public void instantiateComponents() {
		try {
			programUniqueId = breedingManagerService.getCurrentProject().getUniqueID();
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		initializeHarvestDetailsSection();
		initializeSaveSettingsSection();
	}

	private void initializeSaveSettingsSection() {
		saveSettingsLabel = new Label(messageSource.getMessage(Message.SAVE_SETTINGS));
		saveSettingsLabel.setStyleName(Bootstrap.Typography.H2.styleName());
		settingsNameTextfield = new TextField(messageSource.getMessage(Message.SAVE_AS_DESC) + ":");
		setAsDefaultSettingCheckbox = new CheckBox();
	}

	private void initializeHarvestDetailsSection() {
		harvestDetailsLabel = new Label(
				messageSource.getMessage(Message.HARVEST_DETAILS).toUpperCase());
		harvestDetailsLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		harvestLocations = new ComboBox(messageSource.getMessage(Message.HARVEST_LOCATION) + ":");
		harvestLocations.setNullSelectionAllowed(false);
		harvestLocations.addStyleName("mandatory-field");

		harvestDateField = new HarvestDateField(2014,
				messageSource.getMessage(Message.ESTIMATED_HARVEST_DATE) + ":");

		showFavouriteLocations = new CheckBox();
		showFavouriteLocations.setImmediate(true);

		manageFavoriteLocations = new Button();
		manageFavoriteLocations.setStyleName(Reindeer.BUTTON_LINK);
	}

	@Override
	public void initializeValues() {
		try {
			locations = locationDataManager.getLocationsByUniqueID(programUniqueId);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR),
					"Error getting breeding locations!");
		}
		setFieldsDefaultValue();

		initPopulateFavLocation(programUniqueId);
	}

	public boolean initPopulateFavLocation(String programUUID) {
		boolean hasFavorite = false;
		if (BreedingManagerUtil.hasFavoriteLocation(germplasmDataManager, 0, programUUID)) {
			showFavouriteLocations.setValue(true);
			populateHarvestLocation(true, programUUID);
			hasFavorite = true;
		}
		return hasFavorite;
	}

	@Override
	public void addListeners() {
		showFavouriteLocations.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true),
						programUniqueId);
			}

		});

		manageFavoriteLocations.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					Project project = contextUtil.getProjectInContext();
					Window manageFavoriteLocationsWindow = Util
							.launchLocationManager(workbenchDataManager, project.getProjectId(),
									getWindow(),
									messageSource.getMessage(Message.MANAGE_LOCATIONS));
					manageFavoriteLocationsWindow.addListener(new CloseListener() {
						private static final long serialVersionUID = 1L;

						@Override
						public void windowClose(CloseEvent e) {
							Object lastValue = harvestLocations.getValue();
							populateHarvestLocation(
									((Boolean) showFavouriteLocations.getValue()).equals(true),
									programUniqueId);
							harvestLocations.setValue(lastValue);
						}
					});
				} catch (MiddlewareQueryException e) {
					LOG.error("Error on manageFavoriteLocations click", e);
				}
			}
		});
	}

	@Override
	public void layoutComponents() {

		final CssLayout favMethodsLayout = new CssLayout();
		favMethodsLayout.addComponent(showFavouriteLocations);
		favMethodsLayout.addComponent(manageFavoriteLocations);

		final FormLayout harvestFormFields = new FormLayout();
		harvestFormFields.addComponent(harvestDateField);
		harvestFormFields.addComponent(harvestLocations);
		harvestFormFields.addComponent(favMethodsLayout);

		final FormLayout settingsFormFields = new FormLayout();
		settingsFormFields.addComponent(settingsNameTextfield);
		settingsFormFields.addComponent(setAsDefaultSettingCheckbox);

		addComponent(harvestDetailsLabel);
		addComponent(harvestFormFields);
		addComponent(saveSettingsLabel);
		addComponent(settingsFormFields);
	}

	private void populateHarvestLocation(String programUUID) {
		populateHarvestLocation(((Boolean) showFavouriteLocations.getValue()).equals(true),
				programUUID);
	}

	private void populateHarvestLocation(boolean showOnlyFavorites, String programUUID) {
		harvestLocations.removeAllItems();
		mapLocation = new HashMap<String, Integer>();

		if (showOnlyFavorites) {
			try {
				BreedingManagerUtil.populateWithFavoriteLocations(workbenchDataManager,
						germplasmDataManager, harvestLocations, mapLocation, programUUID);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR),
						"Error getting favorite locations!");
			}
		} else {
			populateWithLocations(programUUID);
		}
	}

	private void populateWithLocations(String programUUID) {

		try {
			locations = locationDataManager.getLocationsByUniqueID(programUUID);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR),
					"Error getting breeding locations!");
		}

		harvestLocations.removeAllItems();

		for (Location loc : locations) {
			harvestLocations.addItem(loc.getLocid());
			harvestLocations.setItemCaption(loc.getLocid(), loc.getLname());
			mapLocation.put(loc.getLname(), new Integer(loc.getLocid()));
		}
	}

	public TextField getSettingsNameTextfield() {
		return settingsNameTextfield;
	}

	public CheckBox getSetAsDefaultSettingCheckbox() {
		return setAsDefaultSettingCheckbox;
	}

	public void setSetAsDefaultSettingCheckbox(Boolean value) {
		setAsDefaultSettingCheckbox.setValue(value);
	}

	public HarvestDateField getHarvestDtDateField() {
		return harvestDateField;
	}

	public ComboBox getHarvestLocComboBox() {
		return harvestLocations;
	}

	public boolean validateInputFields() {

		try {
			harvestDateField.validate();
		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(getWindow(), e.getMessage());
			return false;
		}

		if (harvestLocations.getValue() == null || harvestLocations.getValue().equals("")) {

			MessageNotifier.showRequiredFieldError(getWindow(),
					messageSource.getMessage(Message.HARVEST_LOCATION_IS_MANDATORY));
			return false;
		}

		if ((Boolean) setAsDefaultSettingCheckbox.getValue() == true && (
				settingsNameTextfield.getValue() == null || settingsNameTextfield.getValue()
						.equals(""))) {
			MessageNotifier.showRequiredFieldError(getWindow(), messageSource.getMessage(
					Message.PLEASE_ENTER_A_NAME_FOR_THIS_SETTING_IF_YOU_WANT_TO_SET_IT_AS_DEFAULT));
			return false;
		}

		return true;
	}

	public void setFields(AdditionalDetailsSetting additionalDetailsSetting, String name,
			Boolean isDefault) {
		showFavouriteLocations.setValue(false);
		populateHarvestLocation(programUniqueId);
		harvestLocations.select(additionalDetailsSetting.getHarvestLocationId());
		settingsNameTextfield.setValue(name);
		setAsDefaultSettingCheckbox.setValue(isDefault);
	}

	public void setFieldsDefaultValue() {
		harvestLocations.select(null);
		settingsNameTextfield.setValue("");
		setAsDefaultSettingCheckbox.setValue(false);
		showFavouriteLocations.setValue(false);

		populateHarvestLocation(programUniqueId);
	}

	public Boolean settingsFileNameProvided() {
		return settingsNameTextfield.getValue() != null &&
				!StringUtils.isEmpty((String) settingsNameTextfield.getValue());
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public GermplasmDataManager getGermplasmDataManager() {
		return germplasmDataManager;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public void setBreedingManagerService(
			BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}
}
