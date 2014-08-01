package org.generationcp.breeding.manager.crossingmanager.settings;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;

@Configurable
public class CrossingSettingsDetailComponent extends CssLayout 
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {
	
	private static final long serialVersionUID = -7733004867121978697L;
	
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsDetailComponent.class);
	private static final int SETTING_NAME_MAX_LENGTH = 64;
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    WorkbenchDataManager workbenchDataManager;
    
    public ManageCrossingSettingsMain manageCrossingSettingsMain;
    
    public enum Actions {
    	SAVE, CANCEL, DELETE
    }
	
	private DefineCrossingSettingComponent defineSettingComponent;
	private CrossingSettingsMethodComponent methodComponent;
	private CrossingSettingsNameComponent nameComponent;
	private CrossingSettingsOtherDetailsComponent additionalDetailsComponent;
	
	private Button nextButton;
	private Button cancelButton;
	
	private TemplateSetting currentSetting;
	private Panel sectionPanel;
	
	private Project project;
	private Tool crossingManagerTool;
	
	private TemplateSetting defaultSetting;
	
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
		messageSource.setCaption(nextButton, Message.NEXT);
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
		
		defineSettingComponent = new DefineCrossingSettingComponent(this);
		
		methodComponent = new CrossingSettingsMethodComponent();
		nameComponent = new CrossingSettingsNameComponent();
		additionalDetailsComponent = new CrossingSettingsOtherDetailsComponent();
		
        nextButton = new Button();
        nextButton.setData(Actions.SAVE);
        nextButton.setWidth("80px");
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        cancelButton = new Button();
        cancelButton.setData(Actions.CANCEL);
        cancelButton.setWidth("80px");
        cancelButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());
	}

	@Override
	public void initializeValues() {
		currentSetting = null;
		project = null;
		crossingManagerTool = null;
		
		if(defineSettingComponent.getSelectedTemplateSetting()!=null){
			setCurrentSetting(defineSettingComponent.getSelectedTemplateSetting());
			setManageCrossingSettingsFields();
		}
	}

	@Override
	public void addListeners() {
		nextButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -432280582291837428L;

			@Override
			public void buttonClick(ClickEvent event) {
				doNextAction();
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

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		setWidth("900px");
		setHeight("990px");
		
		sectionPanel = new Panel();
		sectionPanel.setWidth("100%");
		sectionPanel.setHeight("940px");
		sectionPanel.addStyleName(AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);
		
		CssLayout sectionLayout = new CssLayout();
		sectionLayout.setMargin(false, true, true, true);
		
		// cs is our crossing settings namespace
		sectionLayout.addStyleName("cs");
		defineSettingComponent.addStyleName("cs-panel-section");
		methodComponent.addStyleName("cs-panel-section");
		nameComponent.addStyleName("cs-panel-section");
		
		sectionLayout.addComponent(defineSettingComponent);
		sectionLayout.addComponent(methodComponent);
		sectionLayout.addComponent(nameComponent);
		sectionLayout.addComponent(additionalDetailsComponent);
		
		sectionPanel.setLayout(sectionLayout);
		
		//3
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setSpacing(true);
		buttonBar.setMargin(true);
		buttonBar.addComponent(cancelButton);
		buttonBar.addComponent(nextButton);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(buttonBar);
		buttonLayout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);
		
		addComponent(sectionPanel);
		addComponent(buttonLayout);
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
				@Override
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						if(currentSetting != null){
							setManageCrossingSettingsFields();
						}
						else{
							setDefaultManageCrossingSettingsFields();
						}
						MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), "Crossing Manager Setting has been reset.");
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
//				additionalDetailsComponent.getSettingsNameTextfield().setValue(currentSetting.getName());
				
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
					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							try {
								workbenchDataManager.deleteTemplateSetting(currentSetting);
								defineSettingComponent.setSettingsComboBox(null);
								setDefaultManageCrossingSettingsFields();
								
								MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), "Crossing Manager Setting has been deleted.");
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

	private void doNextAction(){
		if(nameComponent.validateInputFields() && additionalDetailsComponent.validateInputFields() && methodComponent.validateInputFields()){
			if (additionalDetailsComponent.settingsFileNameProvided()){
				
				if(defaultSetting!=null && !defaultSetting.equals(currentSetting) && (Boolean) additionalDetailsComponent.getSetAsDefaultSettingCheckbox().getValue()==true){
					ConfirmDialog.show(getWindow(), "Save Crossing Setting", "There is already a default setting. " 
						+ "Do you want to change that and set this as the default setting instead?"
						, "Yes", "No", new ConfirmDialog.Listener() {	
							private static final long serialVersionUID = 1L;	
							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									saveSetting();
								}
								else{
									additionalDetailsComponent.setSetAsDefaultSettingCheckbox(false);
									saveSetting();
								}
							}
						});
				
				} else {
					saveSetting();
				}
			}
			else{
				manageCrossingSettingsMain.nextStep();
			}
		}
	}
	
	public void updateTemplateSettingVariables(){
		try{
 			Integer wbUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
 			project = workbenchDataManager.getLastOpenedProject(wbUserId);
 			crossingManagerTool = workbenchDataManager.getToolWithName(CrossingManagerSetting.CROSSING_MANAGER_TOOL_NAME);
 		} catch(MiddlewareQueryException ex){
 			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
 					, "Error with retrieving currently opened Workbench Program and Crossing Manager Tool record.");
 			LOG.error("Error with retrieving currently opened Workbench Program and Crossing Manager Tool record.", ex);
 			return;
 		}
	}

	private void saveSetting() {
		
		updateTemplateSettingVariables();
		
		CrossingManagerSetting currentlyDefinedSettingsInUi = getCurrentlyDefinedSetting();
		
		if(currentSetting == null){
			TemplateSetting templateSetting = new TemplateSetting();
			String settingName = getCurrentSettingNameinUI();
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
					MessageNotifier.showError(getWindow(), "XML Writing Error", "There was an error with writing the XML for the setting.");
					LOG.error("Error with writing XML String.", ex);
					return;
				}
				
				try{
					Integer templateSettingId = workbenchDataManager.addTemplateSetting(templateSetting);
					List<TemplateSetting> results = workbenchDataManager.getTemplateSettings(new TemplateSetting(templateSettingId, null, null, null, null, null));
					if(!results.isEmpty()){
						currentSetting = results.get(0);
						defineSettingComponent.setSettingsComboBox(currentSetting);
						
					} else{
						templateSetting.setTemplateSettingId(templateSettingId);
						currentSetting = templateSetting;
					}
					
					MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), "Crossing Manager Settings have been saved.");
				} catch(MiddlewareQueryException ex){
					LOG.error("Error with saving template setting.", ex);
					MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
							, "Error with saving template setting.");
				}
			} else{
				confirmCrossingSettingOverwrite();
				return;
			}
		} else{
			boolean thereIsAChange = false;
			String currentSettingNameInUi = getCurrentSettingNameinUI();
			
			if(!currentSetting.getName().equals(currentSettingNameInUi)){
				if(!doesSettingNameExist(currentSettingNameInUi, Integer.valueOf(project.getProjectId().intValue()), crossingManagerTool)){
					currentSetting.setName(currentSettingNameInUi);
					thereIsAChange = true;
				} else{
					confirmCrossingSettingOverwrite();
					return;
				}
			}
			
			updateSetting(currentlyDefinedSettingsInUi,thereIsAChange);
		}
		
		manageCrossingSettingsMain.nextStep();
	}
	
	public void confirmCrossingSettingOverwrite(){
		ConfirmDialog.show(getWindow(), "Save Crossing Setting", "There is an existing setting with the same name you have specified." 
				+ " Do you want to overwrite the existing setting?"
				, "Yes", "No", new ConfirmDialog.Listener() {	
					private static final long serialVersionUID = 1L;	
					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							overwriteSetting();
						}
						else{
							additionalDetailsComponent.getSettingsNameTextfield().focus();
						}
					}
				}
			);
	}//end of confirmCrossingSettingOverwrite
	
	public void overwriteSetting(){
		CrossingManagerSetting currentlyDefinedSettingsInUi = getCurrentlyDefinedSetting();
		
		//get the existing setting
		TemplateSetting templateSettingToOverwrite = getExistingTemplateSetting(Integer.valueOf(project.getProjectId().intValue()),crossingManagerTool);
		currentSetting = templateSettingToOverwrite;
		
		updateSetting(currentlyDefinedSettingsInUi,false);
		
		manageCrossingSettingsMain.nextStep();
	}// end of ovewriteSetting
	
	
	public void updateSetting(CrossingManagerSetting currentlyDefinedSettingsInUi, boolean thereIsAChange){
		try{
			CrossingManagerSetting savedSetting = readXmlStringForSetting(currentSetting.getConfiguration());
			if(!currentlyDefinedSettingsInUi.equals(savedSetting)){
				try{
					String configuration = getXmlStringForSetting(currentlyDefinedSettingsInUi);
					currentSetting.setConfiguration(configuration);
					thereIsAChange = true;
				} catch(JAXBException ex){
					MessageNotifier.showError(getWindow(), "XML Writing Error", "There was an error with writing the XML for the setting.");
					LOG.error("Error with writing XML String.", ex);
					return;
				}
			}
		} catch(JAXBException ex){
			LOG.error("Error with parsing crossing manager XML string.", ex);
			MessageNotifier.showError(getWindow(), "XML Parsing Error", "Error with parsing XML string for Crossing Manager setting.");
			return;
		}
		
		if(!currentSetting.isDefault().equals(additionalDetailsComponent.getSetAsDefaultSettingCheckbox().booleanValue())){
			currentSetting.setIsDefault(additionalDetailsComponent.getSetAsDefaultSettingCheckbox().booleanValue());
			thereIsAChange = true;
		}
		
		try{
			if (thereIsAChange){
				workbenchDataManager.updateTemplateSetting(currentSetting);
				//must reload settings combobox to solve out of sync when going back to this screen
				defineSettingComponent.setSettingsComboBox(currentSetting);
				MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS),
						"Crossing Manager Setting has been updated.");
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with updating template setting record.", ex);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), 
					"Error with updating Crossing Manager Setting.");
			return;
		}
	}
	
	public TemplateSetting getExistingTemplateSetting(Integer projectId, Tool tool){
		
		String name = this.getCurrentSettingNameinUI();
		TemplateSetting existingTemplateSetting; 
		
		TemplateSetting filter =  new TemplateSetting();
		filter.setName(name);
		filter.setConfiguration(null);
		filter.setIsDefaultToNull();
		filter.setProjectId(projectId);
		filter.setTemplateSettingId(null);
		filter.setTool(tool);
		try{
			List<TemplateSetting> results = workbenchDataManager.getTemplateSettings(filter);
			if(!results.isEmpty()){
				existingTemplateSetting = results.get(0);
				
				return existingTemplateSetting;
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error getting template settings for project:" + projectId + "and crossing manager tool.", ex);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, "Error with checking for uniqueness of settings name.");
		}
		return null;
	}
	
	public String getCurrentSettingNameinUI(){
		String currentSettingNameInUi = (String) additionalDetailsComponent.getSettingsNameTextfield().getValue();
		currentSettingNameInUi = currentSettingNameInUi.toString();
		currentSettingNameInUi = currentSettingNameInUi.trim();
		currentSettingNameInUi = currentSettingNameInUi.substring(0,
		        Math.min(currentSettingNameInUi.length(), SETTING_NAME_MAX_LENGTH));
		
		return currentSettingNameInUi;
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
				return true;
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error getting template settings for project:" + projectId + "and crossing manager tool.", ex);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
					, "Error with checking for uniqueness of settings name.");
			return true;
		}
		return false;
	}
	
	/**
	 * Make sure to validate the input fields first before calling this method.
	 * @return
	 */
	public CrossingManagerSetting getCurrentlyDefinedSetting(){
		CrossingManagerSetting toreturn = new CrossingManagerSetting();
		
		CrossNameSetting crossNameSettingPojo = nameComponent.getCrossNameSettingObject();
		toreturn.setCrossNameSetting(crossNameSettingPojo);
		
		Integer locId = (Integer) additionalDetailsComponent.getHarvestLocComboBox().getValue();
		String harvestDate = additionalDetailsComponent.getHarvestDtDateField().getValue();
        AdditionalDetailsSetting additionalDetails = new AdditionalDetailsSetting(locId, harvestDate);
		toreturn.setAdditionalDetailsSetting(additionalDetails);
		
		final Integer methodId = methodComponent.getSelectedBreedingMethodId();
		boolean isBasedOnStatusOfParentalLines = methodComponent.isBasedOnStatusOfParentalLines();
		
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

	public void setDefaultSetting(TemplateSetting defaultSetting){
		this.defaultSetting = defaultSetting;
	}
	
}

