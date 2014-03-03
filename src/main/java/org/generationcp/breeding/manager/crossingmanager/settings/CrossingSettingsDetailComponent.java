package org.generationcp.breeding.manager.crossingmanager.settings;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.settings.CrossingSettingsMethodComponent.CrossingMethodOption;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window.Notification;

@Configurable
public class CrossingSettingsDetailComponent extends AbsoluteLayout 
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = -7733004867121978697L;
	
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsDetailComponent.class);
	private static String CROSSING_MANAGER_TOOL_NAME = "crossing_manager";
	private static final int SETTING_NAME_MAX_LENGTH = 64;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    WorkbenchDataManager workbenchDataManager;
    
    public ManageCrossingSettingsMain manageCrossingSettingsMain;
    
    public enum Actions {
    	SAVE, CANCEL
    }
	
	private Label mandatoryLabel;
	private CrossingSettingsMethodComponent methodComponent;
	private CrossingSettingsNameComponent nameComponent;
	private CrossingSettingsOtherDetailsComponent additionalDetailsComponent;
	
	private Button saveButton;
	private Button cancelButton;
	
	private TemplateSetting currentSetting;
	
	public CrossingSettingsDetailComponent(ManageCrossingSettingsMain manageCrossingSettingsMain) {
		this.manageCrossingSettingsMain = manageCrossingSettingsMain;
	}

	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}
	
	@Override
	public void updateLabels() {
		messageSource.setCaption(saveButton, Message.SAVE_LABEL);
		messageSource.setCaption(cancelButton, Message.CANCEL);
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
		mandatoryLabel = new Label("<i>" +messageSource.getMessage(Message.MANDATORY_FIELDS_ARE_NOTED)
				+ "</i>", Label.CONTENT_XHTML);
		
		methodComponent = new CrossingSettingsMethodComponent();
		nameComponent = new CrossingSettingsNameComponent();
		additionalDetailsComponent = new CrossingSettingsOtherDetailsComponent();
		
        saveButton = new Button();
        saveButton.setData(Actions.SAVE);
        saveButton.setWidth("80px");
        saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        cancelButton = new Button();
        cancelButton.setData(Actions.CANCEL);
        cancelButton.setWidth("80px");
        cancelButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());
	}

	@Override
	public void initializeValues() {
		currentSetting = null;
	}

	@Override
	public void addListeners() {
		saveButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -432280582291837428L;

			@Override
			public void buttonClick(ClickEvent event) {
				doSaveAction();
				manageCrossingSettingsMain.getChooseSettingsComponent().setSettingsComboBox(currentSetting);
			}
		});
		
		cancelButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				doResetAction();
			}
			
		});
	}

	@Override
	public void layoutComponents() {
		setWidth("850px");
		setHeight("580px");
		addStyleName(AppConstants.CssStyles.GRAY_ROUNDED_BORDER);
		
		addComponent(mandatoryLabel, "top:7px; left:10px");
		addComponent(methodComponent, "top:30px; left:10px");
		addComponent(nameComponent, "top:200px; left:10px");
		addComponent(additionalDetailsComponent, "top:330px; left:10px");
		
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setWidth("200px");
		buttonBar.addComponent(cancelButton);
		buttonBar.addComponent(saveButton);
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.addComponent(buttonBar);
		layout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);
		
		addComponent(layout, "top:510px; left:0px");
	}
	
	public void doResetAction(){
		
		String message;
		if(currentSetting != null){
			message = "Are you sure you want to reset the current setting for '" + currentSetting.getName().toString()  + "'?";
		}
		else{
			message = "Are you sure you want to reset the current setting ?";
		}
		
		ConfirmDialog.show(getWindow(), "Reset Crossing Manage Setting",  message,
				"Yes", "No", new ConfirmDialog.Listener() {	
				private static final long serialVersionUID = 1L;	
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						if(currentSetting != null){
							setManageCrossingSettingsFields();
						}
						else{
							setDefaultManageCrossingSettingsFields();
						}
						MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), "Crossing Manager Setting has been reset."
								, 3000,Notification.POSITION_CENTERED);
					}
				}
			}
		);
	}// end of doResetAction
	
	public void setManageCrossingSettingsFields(){
		if(currentSetting != null){
			CrossingManagerSetting templateSetting;
			try {
				templateSetting = readXmlStringForSetting(currentSetting.getConfiguration());
				
				//set now all the fields in crossing settings
				methodComponent.setFields(templateSetting.getBreedingMethodSetting());
				nameComponent.setFields(templateSetting.getCrossNameSetting());
				additionalDetailsComponent.setFields(templateSetting.getAdditionalDetailsSetting(), templateSetting.getName(), currentSetting.isDefault());
				//set name text field
				additionalDetailsComponent.getSettingsNameTextfield().setValue(currentSetting.getName());
				
			} catch (JAXBException e) {
				LOG.error("Error with retrieving template setting.",e);
				e.printStackTrace();
			}
		}
	}
	
	public void doDeleteAction() {
		if(currentSetting != null){
			String message = "Are you sure you want to delete '" + currentSetting.getName().toString()  + "'?";
			ConfirmDialog.show(getWindow(), "Delete Crossing Manage Setting",  message,
					"Yes", "No", new ConfirmDialog.Listener() {	
					private static final long serialVersionUID = 1L;	
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							try {
								workbenchDataManager.deleteTemplateSetting(currentSetting);
								manageCrossingSettingsMain.getChooseSettingsComponent().setSettingsComboBox(null);
								setDefaultManageCrossingSettingsFields();
								
								MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), "Crossing Manager Setting has been deleted."
										, 3000,Notification.POSITION_CENTERED);
							} catch (MiddlewareQueryException e) {
								LOG.error("Error with deleting the manage crossing template setting", e);
								e.printStackTrace();
							}
						}
					}
				}
			);
		}
		else{
			MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.WARNING), "There is no selected crossing manager setting to delete.");
		}
		
	} // end of doDeleteAction

	private void doSaveAction(){
		if(!methodComponent.validateInputFields()){
			return;
		}
		
		if(!nameComponent.validateInputFields()){
			return;
		}
		
		if(!additionalDetailsComponent.validateInputFields()){
			return;
		}
		
		Project project = null;
		Tool crossingManagerTool = null;
		try{
			Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
			project = workbenchDataManager.getLastOpenedProject(wbUserId);
			crossingManagerTool = workbenchDataManager.getToolWithName(CROSSING_MANAGER_TOOL_NAME);
		} catch(MiddlewareQueryException ex){
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, "Error with retrieving currently opened Workbench Program and Crossing Manager Tool record.", Notification.POSITION_CENTERED);
			LOG.error("Error with retrieving currently opened Workbench Program and Crossing Manager Tool record.", ex);
			return;
		}
		
		CrossingManagerSetting currentlyDefinedSettingsInUi = getCurrentlyDefinedSetting();
		
		if(currentSetting == null){
			TemplateSetting templateSetting = new TemplateSetting();
			String settingName = (String) additionalDetailsComponent.getSettingsNameTextfield().getValue();
			settingName = settingName.trim();
			settingName = settingName.substring(0,
                    Math.min(settingName.length(), SETTING_NAME_MAX_LENGTH));
			templateSetting.setName(settingName);
			
			if(!doesSettingNameExist(settingName, Integer.valueOf(project.getProjectId().intValue()), crossingManagerTool)){
				templateSetting.setIsDefault(additionalDetailsComponent.getSetAsDefaultSettingCheckbox().booleanValue());
				templateSetting.setProjectId(Integer.valueOf(project.getProjectId().intValue()));
				templateSetting.setTool(crossingManagerTool);
				templateSetting.setTemplateSettingId(null);
				
				try{
					String configuration = getXmlStringForSetting(currentlyDefinedSettingsInUi);
					templateSetting.setConfiguration(configuration);
				} catch(JAXBException ex){
					MessageNotifier.showError(getWindow(), "XML Writing Error", "There was an error with writing the XML for the setting."
							, Notification.POSITION_CENTERED);
					LOG.error("Error with writing XML String.", ex);
					return;
				}
				
				try{
					Integer templateSettingId = workbenchDataManager.addTemplateSetting(templateSetting);
					List<TemplateSetting> results = workbenchDataManager.getTemplateSettings(new TemplateSetting(templateSettingId, null, null, null, null, null));
					if(!results.isEmpty()){
						currentSetting = results.get(0);
					} else{
						templateSetting.setTemplateSettingId(templateSettingId);
						currentSetting = templateSetting;
					}
					
					MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), "Crossing Manager Settings have been saved."
							, 3000, Notification.POSITION_CENTERED);
					return;
				} catch(MiddlewareQueryException ex){
					LOG.error("Error with saving template setting.", ex);
					MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
							, "Error with saving template setting.", Notification.POSITION_CENTERED);
				}
			} else{
				return;
			}
		} else{
			boolean thereIsAChange = false;
			String currentSettingNameInUi = (String) additionalDetailsComponent.getSettingsNameTextfield().getValue();
			currentSettingNameInUi = currentSettingNameInUi.toString();
			currentSettingNameInUi = currentSettingNameInUi.trim();
			currentSettingNameInUi = currentSettingNameInUi.substring(0,
			        Math.min(currentSettingNameInUi.length(), SETTING_NAME_MAX_LENGTH));
			
			if(!currentSetting.getName().equals(currentSettingNameInUi)){
				if(!doesSettingNameExist(currentSettingNameInUi, Integer.valueOf(project.getProjectId().intValue()), crossingManagerTool)){
					currentSetting.setName(currentSettingNameInUi);
					thereIsAChange = true;
				} else{
					return;
				}
			}
			
			try{
				CrossingManagerSetting savedSetting = readXmlStringForSetting(currentSetting.getConfiguration());
				if(!currentlyDefinedSettingsInUi.equals(savedSetting)){
					try{
						String configuration = getXmlStringForSetting(currentlyDefinedSettingsInUi);
						currentSetting.setConfiguration(configuration);
						thereIsAChange = true;
					} catch(JAXBException ex){
						MessageNotifier.showError(getWindow(), "XML Writing Error", "There was an error with writing the XML for the setting."
								, Notification.POSITION_CENTERED);
						LOG.error("Error with writing XML String.", ex);
						return;
					}
				}
			} catch(JAXBException ex){
				LOG.error("Error with parsing crossing manager XML string.", ex);
				MessageNotifier.showError(getWindow(), "XML Parsing Error", "Error with parsing XML string for Crossing Manager setting."
						, Notification.POSITION_CENTERED);
				return;
			}
			
			if(!currentSetting.isDefault().equals(additionalDetailsComponent.getSetAsDefaultSettingCheckbox().booleanValue())){
				currentSetting.setIsDefault(additionalDetailsComponent.getSetAsDefaultSettingCheckbox().booleanValue());
				thereIsAChange = true;
			}
			
			try{
				workbenchDataManager.updateTemplateSetting(currentSetting);
				MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), "Crossing Manager Setting has been updated."
						, 3000,Notification.POSITION_CENTERED);
			} catch(MiddlewareQueryException ex){
				LOG.error("Error with updating template setting record.", ex);
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), "Error with updating Crossing Manager Setting."
						, Notification.POSITION_CENTERED);
				return;
			}
		}	
		
	}
	
	private boolean doesSettingNameExist(String name, Integer projectId, Tool tool){
		TemplateSetting filter =  new TemplateSetting();
		filter.setName(name);
		filter.setConfiguration(null);
		filter.setIsDefaultToNull();
		filter.setProjectId(projectId);
		filter.setTemplateSettingId(null);
		filter.setTool(tool);
		try{
			List<TemplateSetting> settings = workbenchDataManager.getTemplateSettings(filter);
			if(!settings.isEmpty()){
				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.INVALID_INPUT)
						, "There is an existing setting with the same name you have sprcified. Please specify a different name.", Notification.POSITION_CENTERED);
				return true;
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error getting template settings for project:" + projectId + "and crossing manager tool.", ex);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, "Error with checking for uniqueness of settings name.", Notification.POSITION_CENTERED);
			return true;
		}
		return false;
	}
	
	/**
	 * Make sure to validate the input fields first before calling this method.
	 * @return
	 */
	private CrossingManagerSetting getCurrentlyDefinedSetting(){
		CrossingManagerSetting toreturn = new CrossingManagerSetting();
		
		String prefix = (String) nameComponent.getPrefixTextField().getValue();
		String suffix = (String) nameComponent.getSuffixTextField().getValue();
		if(suffix != null){
			suffix = suffix.trim();
		}
		if (suffix.length() == 0) {
		    suffix = null; //set as null so attribute will not be marshalled
		}
		boolean addSpaceBetweenPrefixAndCode = true;
		if(CrossingSettingsNameComponent.AddSpaceBetPrefixAndCodeOption.NO.equals(
		        nameComponent.getAddSpaceOptionGroup().getValue())){
			addSpaceBetweenPrefixAndCode = false;
		}
		Integer numOfDigits = null;
		if(nameComponent.getSequenceNumCheckBox().booleanValue()){
			numOfDigits = (Integer) nameComponent.getLeadingZerosSelect().getValue();
		}
		CrossNameSetting crossNameSettingPojo = new CrossNameSetting(prefix.trim(), suffix
				, addSpaceBetweenPrefixAndCode, numOfDigits);
		toreturn.setCrossNameSetting(crossNameSettingPojo);
		
		Integer locId = (Integer) additionalDetailsComponent.getHarvestLocComboBox().getValue();
		Date harvestDate = (Date) additionalDetailsComponent.getHarvestDtDateField().getValue();
        AdditionalDetailsSetting additionalDetails = new AdditionalDetailsSetting(locId, harvestDate);
		toreturn.setAdditionalDetailsSetting(additionalDetails);
		
		Integer methodId = (Integer) methodComponent.getCrossingMethodComboBox().getValue();
		boolean isBasedOnStatusOfParentalLines = true;
		if(methodComponent.getCrossingMethodOptionGroup().getValue().equals(CrossingMethodOption.SAME_FOR_ALL_CROSSES)){
			isBasedOnStatusOfParentalLines = false;
		}
		BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting(methodId, isBasedOnStatusOfParentalLines);
		toreturn.setBreedingMethodSetting(breedingMethodSetting);
		
		String settingName = (String) additionalDetailsComponent.getSettingsNameTextfield().getValue();
		settingName = settingName.trim();
		toreturn.setName(settingName);
		
		return toreturn;
	}
	
	private String getXmlStringForSetting(CrossingManagerSetting setting) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(CrossingManagerSetting.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(setting, writer);
        return writer.toString();
    }
	
	private CrossingManagerSetting readXmlStringForSetting(String xmlString) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(CrossingManagerSetting.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
        CrossingManagerSetting parsedSetting = (CrossingManagerSetting) unmarshaller.unmarshal(new StringReader(xmlString));
        return parsedSetting;
	}

	public TemplateSetting getCurrentSetting() {
		return currentSetting;
	}

	public void setCurrentSetting(TemplateSetting currentSetting) {
		this.currentSetting = currentSetting;
	}

	public void setDefaultManageCrossingSettingsFields() {
		methodComponent.setFieldsDefaultValue();
		nameComponent.setFieldsDefaultValue();
		additionalDetailsComponent.setFieldsDefaultValue();
	}

}

