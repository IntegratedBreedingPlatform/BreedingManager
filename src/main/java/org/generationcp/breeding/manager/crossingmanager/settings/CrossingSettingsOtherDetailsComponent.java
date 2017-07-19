
package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.customfields.HarvestDateField;
import org.generationcp.breeding.manager.application.BreedingManagerWindowGenerator;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
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

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class CrossingSettingsOtherDetailsComponent extends CssLayout implements BreedingManagerLayout, InternationalizableComponent,
		InitializingBean {

	public enum SaveSettingOption {
		YES, NO
	}

	private static final long serialVersionUID = -4119454332332114156L;

	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsOtherDetailsComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private BreedingManagerWindowGenerator breedingManagerWindowGenerator;

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
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.harvestDetailsLabel.setValue(this.messageSource.getMessage(Message.HARVEST_DETAILS).toUpperCase());
		this.saveSettingsLabel.setValue(this.messageSource.getMessage(Message.SAVE_SETTINGS).toUpperCase());

		this.messageSource.setCaption(this.showFavouriteLocations, Message.SHOW_ONLY_FAVORITE_LOCATIONS);
		this.messageSource.setCaption(this.manageFavoriteLocations, Message.MANAGE_LOCATIONS);
		this.messageSource
				.setCaption(this.setAsDefaultSettingCheckbox, Message.SET_AS_DEFAULT_FOR_THIS_PROGRAM_OVERRIDES_PREVIOUS_DEFAULTS);
	}

	@Override
	public void instantiateComponents() {
		try {
			this.programUniqueId = this.breedingManagerService.getCurrentProject().getUniqueID();
		} catch (final MiddlewareQueryException e) {
			CrossingSettingsOtherDetailsComponent.LOG.error(e.getMessage(), e);
		}
		this.initializeHarvestDetailsSection();
		this.initializeSaveSettingsSection();
	}

	private void initializeSaveSettingsSection() {
		this.saveSettingsLabel = new Label(this.messageSource.getMessage(Message.SAVE_SETTINGS));
		this.saveSettingsLabel.setDebugId("saveSettingsLabel");
		this.saveSettingsLabel.setStyleName(Bootstrap.Typography.H2.styleName());
		this.settingsNameTextfield = new TextField(this.messageSource.getMessage(Message.SAVE_AS_DESC) + ":");
		this.settingsNameTextfield.setDebugId("settingsNameTextfield");
		this.setAsDefaultSettingCheckbox = new CheckBox();
		this.setAsDefaultSettingCheckbox.setDebugId("setAsDefaultSettingCheckbox");
	}

	private void initializeHarvestDetailsSection() {
		this.harvestDetailsLabel = new Label(this.messageSource.getMessage(Message.HARVEST_DETAILS).toUpperCase());
		this.harvestDetailsLabel.setDebugId("harvestDetailsLabel");
		this.harvestDetailsLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		this.harvestLocations = new ComboBox(this.messageSource.getMessage(Message.HARVEST_LOCATION) + ":");
		this.harvestLocations.setDebugId("harvestLocations");
		this.harvestLocations.setNullSelectionAllowed(true);

		this.harvestDateField =
				new HarvestDateField(Calendar.getInstance().get(Calendar.YEAR),
						this.messageSource.getMessage(Message.ESTIMATED_HARVEST_DATE) + ":");

		this.showFavouriteLocations = new CheckBox();
		this.showFavouriteLocations.setDebugId("showFavouriteLocations");
		this.showFavouriteLocations.setImmediate(true);

		this.manageFavoriteLocations = new Button();
		this.manageFavoriteLocations.setDebugId("manageFavoriteLocations");
		this.manageFavoriteLocations.setStyleName(BaseTheme.BUTTON_LINK);
	}

	@Override
	public void initializeValues() {
		try {
			this.locations = this.locationDataManager.getLocationsByUniqueID(this.programUniqueId);
		} catch (final MiddlewareQueryException e) {
			CrossingSettingsOtherDetailsComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR), "Error getting breeding locations!");
		}
		this.setFieldsDefaultValue();

		this.initPopulateFavLocation(this.programUniqueId);
	}

	public boolean initPopulateFavLocation(final String programUUID) {
		boolean hasFavorite = false;
		if (BreedingManagerUtil.hasFavoriteLocation(this.germplasmDataManager, this.locationDataManager, 0, programUUID)) {
			this.showFavouriteLocations.setValue(true);
			this.populateHarvestLocation(true, programUUID);
			hasFavorite = true;
		}
		return hasFavorite;
	}

	@Override
	public void addListeners() {
		this.showFavouriteLocations.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				CrossingSettingsOtherDetailsComponent.this.populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true),
						CrossingSettingsOtherDetailsComponent.this.programUniqueId);
			}

		});

		this.manageFavoriteLocations.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					final Project project = CrossingSettingsOtherDetailsComponent.this.contextUtil.getProjectInContext();
					final Window manageFavoriteLocationsWindow =
							breedingManagerWindowGenerator.openLocationManagerPopupWindow(
									project.getProjectId(), CrossingSettingsOtherDetailsComponent.this.getWindow(),
									CrossingSettingsOtherDetailsComponent.this.messageSource.getMessage(Message.MANAGE_LOCATIONS));
					manageFavoriteLocationsWindow.addListener(new CloseListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void windowClose(final CloseEvent e) {
							final Object lastValue = CrossingSettingsOtherDetailsComponent.this.harvestLocations.getValue();
							CrossingSettingsOtherDetailsComponent.this.populateHarvestLocation(
									((Boolean) CrossingSettingsOtherDetailsComponent.this.showFavouriteLocations.getValue()).equals(true),
									CrossingSettingsOtherDetailsComponent.this.programUniqueId);
							CrossingSettingsOtherDetailsComponent.this.harvestLocations.setValue(lastValue);
						}
					});
				} catch (final MiddlewareQueryException e) {
					CrossingSettingsOtherDetailsComponent.LOG.error("Error on manageFavoriteLocations click", e);
				}
			}
		});
	}

	@Override
	public void layoutComponents() {

		final CssLayout favMethodsLayout = new CssLayout();
		favMethodsLayout.setDebugId("favMethodsLayout");
		favMethodsLayout.addComponent(this.showFavouriteLocations);
		favMethodsLayout.addComponent(this.manageFavoriteLocations);

		final FormLayout harvestFormFields = new FormLayout();
		harvestFormFields.setDebugId("harvestFormFields");
		harvestFormFields.addComponent(this.harvestDateField);
		harvestFormFields.addComponent(this.harvestLocations);
		harvestFormFields.addComponent(favMethodsLayout);

		final FormLayout settingsFormFields = new FormLayout();
		settingsFormFields.setDebugId("settingsFormFields");
		settingsFormFields.addComponent(this.settingsNameTextfield);
		settingsFormFields.addComponent(this.setAsDefaultSettingCheckbox);

		this.addComponent(this.harvestDetailsLabel);
		this.addComponent(harvestFormFields);
		this.addComponent(this.saveSettingsLabel);
		this.addComponent(settingsFormFields);
	}

	private void populateHarvestLocation(final String programUUID) {
		this.populateHarvestLocation(((Boolean) this.showFavouriteLocations.getValue()).equals(true), programUUID);
	}

	private void populateHarvestLocation(final boolean showOnlyFavorites, final String programUUID) {
		this.harvestLocations.removeAllItems();
		this.mapLocation = new HashMap<String, Integer>();

		if (showOnlyFavorites) {
			try {
				BreedingManagerUtil.populateWithFavoriteLocations(this.workbenchDataManager, this.germplasmDataManager,
						this.harvestLocations, this.mapLocation, programUUID);
			} catch (final MiddlewareQueryException e) {
				CrossingSettingsOtherDetailsComponent.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
						"Error getting favorite locations!");
			}
		} else {
			this.populateWithLocations(programUUID);
		}
	}

	private void populateWithLocations(final String programUUID) {

		try {
			this.locations = this.locationDataManager.getLocationsByUniqueID(programUUID);
		} catch (final MiddlewareQueryException e) {
			CrossingSettingsOtherDetailsComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR), "Error getting breeding locations!");
		}

		this.harvestLocations.removeAllItems();

		for (final Location loc : this.locations) {
			this.harvestLocations.addItem(loc.getLocid());
			this.harvestLocations.setItemCaption(loc.getLocid(), BreedingManagerUtil.getLocationNameDisplay(loc));
			this.mapLocation.put(loc.getLname(), new Integer(loc.getLocid()));
		}
	}

	public TextField getSettingsNameTextfield() {
		return this.settingsNameTextfield;
	}

	public CheckBox getSetAsDefaultSettingCheckbox() {
		return this.setAsDefaultSettingCheckbox;
	}

	public void setSetAsDefaultSettingCheckbox(final Boolean value) {
		this.setAsDefaultSettingCheckbox.setValue(value);
	}

	public HarvestDateField getHarvestDtDateField() {
		return this.harvestDateField;
	}

	public ComboBox getHarvestLocComboBox() {
		return this.harvestLocations;
	}

	public boolean validateInputFields() {

		try {
			this.harvestDateField.validate();
		} catch (final InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(this.getWindow(), e.getMessage());
			return false;
		}

		if ((Boolean) this.setAsDefaultSettingCheckbox.getValue() == true
				&& (this.settingsNameTextfield.getValue() == null || this.settingsNameTextfield.getValue().equals(""))) {
			MessageNotifier.showRequiredFieldError(this.getWindow(),
					this.messageSource.getMessage(Message.PLEASE_ENTER_A_NAME_FOR_THIS_SETTING_IF_YOU_WANT_TO_SET_IT_AS_DEFAULT));
			return false;
		}

		return true;
	}

	public void setFields(final AdditionalDetailsSetting additionalDetailsSetting, final String name, final Boolean isDefault) {
		this.showFavouriteLocations.setValue(false);
		this.populateHarvestLocation(this.programUniqueId);
		this.harvestLocations.select(additionalDetailsSetting.getHarvestLocationId());
		this.settingsNameTextfield.setValue(name);
		this.setAsDefaultSettingCheckbox.setValue(isDefault);
	}

	public void setFieldsDefaultValue() {
		this.harvestLocations.select(null);
		this.settingsNameTextfield.setValue("");
		this.setAsDefaultSettingCheckbox.setValue(false);
		this.showFavouriteLocations.setValue(false);

		this.populateHarvestLocation(this.programUniqueId);
	}

	public Boolean settingsFileNameProvided() {
		return this.settingsNameTextfield.getValue() != null && !StringUtils.isEmpty((String) this.settingsNameTextfield.getValue());
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public GermplasmDataManager getGermplasmDataManager() {
		return this.germplasmDataManager;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public void setBreedingManagerService(final BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}
}
