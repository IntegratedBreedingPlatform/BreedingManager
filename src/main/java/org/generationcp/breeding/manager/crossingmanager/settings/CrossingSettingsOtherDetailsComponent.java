package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMain;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
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
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class CrossingSettingsOtherDetailsComponent extends AbsoluteLayout
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

	private Label additionalDetailsSectionTitle;
	private Label harvestDateLabel;
    private Label harvestLocationLabel;

    private Label saveSettingsSectionTitle;
    private Label saveSettingsLabel;
    private Label saveAsLabel;
    private TextField settingsNameTextfield;
    private CheckBox setAsDefaultSettingCheckbox;
    private OptionGroup saveSettingsOptionGroup;
    
    private DateField harvestDtDateField;
    private ComboBox harvestLocComboBox;
    private Button manageFavoriteLocationsLink; 
    private CheckBox showFavoriteLocationsCheckBox;
    
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
    	additionalDetailsSectionTitle.setValue(messageSource.getMessage(Message.ADDITIONAL_DETAILS).toUpperCase());
    	saveSettingsSectionTitle.setValue(messageSource.getMessage(Message.SAVE_SETTINGS).toUpperCase());
    	
    	harvestDateLabel.setValue(messageSource.getMessage(Message.HARVEST_DATE) + ":");
    	harvestLocationLabel.setValue(messageSource.getMessage(Message.HARVEST_LOCATION) + ":");
    	saveSettingsLabel.setValue(messageSource.getMessage(Message.SAVE_THIS_SETTING_TO_USE_AGAIN));
    	saveAsLabel.setValue(messageSource.getMessage(Message.SAVE_AS) + ":");
    	
    	messageSource.setCaption(showFavoriteLocationsCheckBox, Message.SHOW_ONLY_FAVORITE_LOCATIONS);
    	messageSource.setCaption(manageFavoriteLocationsLink, Message.MANAGE_LOCATIONS);
    	messageSource.setCaption(setAsDefaultSettingCheckbox, Message.SET_AS_DEFAULT_FOR_THIS_PROGRAM_OVERRIDES_PREVIOUS_DEFAULTS);
    	
    }

	@Override
	public void instantiateComponents() {
		initializeAdditionalDetailsSection();
		initializeSaveSettingsSection();
		
	}

	private void initializeSaveSettingsSection() {
		saveSettingsSectionTitle = new Label();
		saveSettingsSectionTitle.setStyleName(Bootstrap.Typography.H4.styleName());
		
		saveSettingsLabel = new Label();
		saveAsLabel = new Label();
		settingsNameTextfield = new TextField();
		settingsNameTextfield.setWidth("240px");
		
		saveSettingsOptionGroup = new OptionGroup();
		saveSettingsOptionGroup.setImmediate(true);
		saveSettingsOptionGroup.addStyleName(AppConstants.CssStyles.HORIZONTAL_GROUP);
		
		setAsDefaultSettingCheckbox = new CheckBox();
	}

	private void initializeAdditionalDetailsSection() {
		additionalDetailsSectionTitle = new Label();
		additionalDetailsSectionTitle.setStyleName(Bootstrap.Typography.H4.styleName());
		
		harvestDateLabel = new Label();
        
        harvestDtDateField = new DateField();
        harvestDtDateField.setResolution(DateField.RESOLUTION_DAY);
        harvestDtDateField.setDateFormat(CrossingManagerMain.DATE_FORMAT);
        harvestDtDateField.setWidth("240px");
        
        harvestLocationLabel = new Label();
        
        harvestLocComboBox = new ComboBox();
        harvestLocComboBox.setWidth("240px");
        harvestLocComboBox.setNullSelectionAllowed(true);
        
        showFavoriteLocationsCheckBox = new CheckBox();
        showFavoriteLocationsCheckBox.setImmediate(true);
        
        manageFavoriteLocationsLink = new Button();
        manageFavoriteLocationsLink.setStyleName(Reindeer.BUTTON_LINK);
	}

	
	@Override
	public void initializeValues() {
		try {
			locations = germplasmDataManager.getAllBreedingLocations();
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR), 
					"Error getting breeding locations!");
		}
        
        saveSettingsOptionGroup.addItem(SaveSettingOption.NO);
		saveSettingsOptionGroup.setItemCaption(SaveSettingOption.NO, messageSource.getMessage(Message.NO));
		saveSettingsOptionGroup.addItem(SaveSettingOption.YES);
		saveSettingsOptionGroup.setItemCaption(SaveSettingOption.YES, messageSource.getMessage(Message.YES));
		
		setFieldsDefaultValue();
	}

	@Override
	public void addListeners() {
		showFavoriteLocationsCheckBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true));
			}
			
		});
        
		
		manageFavoriteLocationsLink.addListener(new ClickListener(){
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
							Object lastValue = harvestLocComboBox.getValue();
							populateHarvestLocation(((Boolean) showFavoriteLocationsCheckBox.getValue()).equals(true));
							harvestLocComboBox.setValue(lastValue);
						}
					});
				} catch (MiddlewareQueryException e){
					LOG.error("Error on manageFavoriteLocations click", e);
				}
		
			}
			
		});
		
		saveSettingsOptionGroup.addListener(new Property.ValueChangeListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				toggleSaveSettingFields(SaveSettingOption.YES.equals(event.getProperty().getValue()));
			}
		});
	}
	

	@Override
	public void layoutComponents() {
		// Additional Details Section
		addComponent(additionalDetailsSectionTitle, "top:0px;left:0px");
		
		addComponent(harvestLocationLabel, "top:26px;left:0px");
		addComponent(harvestLocComboBox, "top:26px;left:145px");
		addComponent(showFavoriteLocationsCheckBox, "top:26px;left:410px");
		addComponent(manageFavoriteLocationsLink,"top:44px;left:430px;");

		addComponent(harvestDateLabel, "top:60px;left:0px");
        addComponent(harvestDtDateField, "top:60px;left:145px");
		
		
        // Save Settings section
		addComponent(saveSettingsSectionTitle, "top:105px;left:0px");
		addComponent(saveSettingsLabel, "top:130px; left:0px;");
		addComponent(saveSettingsOptionGroup, "top:130px; left:190px");
		addComponent(saveAsLabel, "top:130px; left:300px;");
		addComponent(settingsNameTextfield, "top:130px; left:370px;");
		addComponent(setAsDefaultSettingCheckbox, "top:160px; left:297px;");
        
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
				LOG.error(e.getMessage());
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR), 
						"Error getting favorite locations!");
			}
			
        } else {
        	populateWithLocations();
        }

    }
    
    private void populateWithLocations(){
		harvestLocComboBox.removeAllItems();
        
        for (Location loc : locations) {
        	harvestLocComboBox.addItem(loc.getLocid());
    		harvestLocComboBox.setItemCaption(loc.getLocid(), loc.getLname());
    		mapLocation.put(loc.getLname(), new Integer(loc.getLocid()));
        }
    }

	public TextField getSettingsNameTextfield() {
		return settingsNameTextfield;
	}

	public CheckBox getSetAsDefaultSettingCheckbox() {
		return setAsDefaultSettingCheckbox;
	}

	public DateField getHarvestDtDateField() {
		return harvestDtDateField;
	}

	public ComboBox getHarvestLocComboBox() {
		return harvestLocComboBox;
	}
    
	public boolean validateInputFields(){
		String settingsName = (String) settingsNameTextfield.getValue();
		
		// validations only when setting will be saved
		if (doSaveSetting()){
			if(settingsName == null || settingsName.trim().length() == 0){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT), "Please specify a name for the setting."
						, Notification.POSITION_CENTERED);
				return false;
			}
		}

		return true;
	}

	public void setFields(AdditionalDetailsSetting additionalDetailsSetting, String name, Boolean isDefault ) {
		showFavoriteLocationsCheckBox.setValue(false);
		populateHarvestLocation();
		
		harvestLocComboBox.select(additionalDetailsSetting.getHarvestLocationId());
		harvestDtDateField.setValue(additionalDetailsSetting.getHarvestDate());
		
		
		saveSettingsOptionGroup.select(SaveSettingOption.YES);
		settingsNameTextfield.setValue(name);
		
		setAsDefaultSettingCheckbox.setValue(isDefault);
	}

	public void setFieldsDefaultValue() {
		harvestLocComboBox.select(null);
		harvestDtDateField.setValue(null);
		settingsNameTextfield.setValue("");
		setAsDefaultSettingCheckbox.setValue(false);
		showFavoriteLocationsCheckBox.setValue(false);
		
		saveSettingsOptionGroup.select(SaveSettingOption.NO);
		toggleSaveSettingFields(false);
		
		populateHarvestLocation();
	}
	
	private void toggleSaveSettingFields(boolean enabled){
		saveAsLabel.setEnabled(enabled);
		settingsNameTextfield.setEnabled(enabled);
	}
	
	public boolean doSaveSetting(){
		return SaveSettingOption.YES.equals(saveSettingsOptionGroup);
	}
}
