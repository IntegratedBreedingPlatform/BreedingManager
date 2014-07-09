package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
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
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class CrossingSettingsOtherDetailsComponent extends CssLayout
		implements BreedingManagerLayout, InternationalizableComponent,
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
	
	private Label harvestDetailsLabel;

	private ComboBox harvestLocations;
	private CheckBox showFavouriteLocations;
	private Button manageFavoriteLocations;

    private Label saveSettingsLabel;

    private TextField settingsNameTextfield;
    private CheckBox setAsDefaultSettingCheckbox;

    private Map<String, Integer> mapLocation;
    private List<Location> locations;

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
    	harvestDetailsLabel.setValue(messageSource.getMessage(Message.HARVEST_DETAILS).toUpperCase());
    	saveSettingsLabel.setValue(messageSource.getMessage(Message.SAVE_SETTINGS).toUpperCase());

    	messageSource.setCaption(showFavouriteLocations, Message.SHOW_ONLY_FAVORITE_LOCATIONS);
    	messageSource.setCaption(manageFavoriteLocations, Message.MANAGE_LOCATIONS);
    	messageSource.setCaption(setAsDefaultSettingCheckbox, Message.SET_AS_DEFAULT_FOR_THIS_PROGRAM_OVERRIDES_PREVIOUS_DEFAULTS);
    }

	@Override
	public void instantiateComponents() {
		initializeHarvestDetailsSection();
		initializeSaveSettingsSection();
	}

	private void initializeSaveSettingsSection() {
		saveSettingsLabel = new Label(messageSource.getMessage(Message.SAVE_SETTINGS));
		saveSettingsLabel.setStyleName(Bootstrap.Typography.H2.styleName());
		settingsNameTextfield = new TextField(messageSource.getMessage(Message.SAVE_AS));
		setAsDefaultSettingCheckbox = new CheckBox();
	}

	private void initializeHarvestDetailsSection() {
		harvestDetailsLabel = new Label(messageSource.getMessage(Message.HARVEST_DETAILS).toUpperCase());
		harvestDetailsLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		harvestLocations = new ComboBox(messageSource.getMessage(Message.HARVEST_LOCATION));
        harvestLocations.setNullSelectionAllowed(true);
        
        showFavouriteLocations = new CheckBox();
        showFavouriteLocations.setImmediate(true);

        manageFavoriteLocations = new Button();
        manageFavoriteLocations.setStyleName(Reindeer.BUTTON_LINK);
	}

	@Override
	public void initializeValues() {
		try {
			locations = locationDataManager.getAllLocations();
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR),
					"Error getting breeding locations!");
		}
		setFieldsDefaultValue();
	}

	@Override
	public void addListeners() {
		showFavouriteLocations.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true));
			}

		});

		manageFavoriteLocations.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
		            Project project = workbenchDataManager.getLastOpenedProject(wbUserId);
					Window manageFavoriteLocationsWindow = Util.launchLocationManager(workbenchDataManager, project.getProjectId(), getWindow(), messageSource.getMessage(Message.MANAGE_LOCATIONS));
					manageFavoriteLocationsWindow.addListener(new CloseListener(){
						private static final long serialVersionUID = 1L;
						@Override
						public void windowClose(CloseEvent e) {
							Object lastValue = harvestLocations.getValue();
							populateHarvestLocation(((Boolean) showFavouriteLocations.getValue()).equals(true));
							harvestLocations.setValue(lastValue);
						}
					});
				} catch (MiddlewareQueryException e){
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

	private void populateHarvestLocation() {
    	populateHarvestLocation(((Boolean) showFavouriteLocations.getValue()).equals(true));
    }

    private void populateHarvestLocation(boolean showOnlyFavorites) {
        harvestLocations.removeAllItems();
        mapLocation = new HashMap<String, Integer>();

        if(showOnlyFavorites){
        	try {
				BreedingManagerUtil.populateWithFavoriteLocations(workbenchDataManager,
						germplasmDataManager, harvestLocations, mapLocation);
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR),
						"Error getting favorite locations!");
			}
        } else {
        	populateWithLocations();
        }
    }

    private void populateWithLocations(){
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

	public ComboBox getHarvestLocComboBox() {
		return harvestLocations;
	}

	public void setFields(AdditionalDetailsSetting additionalDetailsSetting, String name, Boolean isDefault ) {
		showFavouriteLocations.setValue(false);
		populateHarvestLocation();

		harvestLocations.select(additionalDetailsSetting.getHarvestLocationId());
		settingsNameTextfield.setValue(name);

		setAsDefaultSettingCheckbox.setValue(isDefault);
	}

	public void setFieldsDefaultValue() {
		harvestLocations.select(null);
		settingsNameTextfield.setValue("");
		setAsDefaultSettingCheckbox.setValue(false);
		showFavouriteLocations.setValue(false);

		populateHarvestLocation();
	}

	public Boolean settingsFileNameProvided() {
		return settingsNameTextfield.getValue() != null && 
			!StringUtils.isEmpty((String) settingsNameTextfield.getValue());
	}
}
