package org.generationcp.breeding.manager.crossingmanager.settings;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
	
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    WorkbenchDataManager workbenchDataManager;
    
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
		buttonBar.addComponent(saveButton);
		buttonBar.addComponent(cancelButton);
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.addComponent(buttonBar);
		layout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);
		
		addComponent(layout, "top:510px; left:0px");
	}

	private void doSaveAction(){
		boolean allInputsAreValid = true;
		allInputsAreValid = methodComponent.validateInputFields();
		allInputsAreValid = nameComponent.validateInputFields();
		allInputsAreValid = additionalDetailsComponent.validateInputFields();
		
		if(allInputsAreValid){
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
				templateSetting.setName(settingName.trim());
				
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
								, Notification.POSITION_CENTERED);
						return;
					} catch(MiddlewareQueryException ex){
						LOG.error("Error with saving template setting.", ex);
						MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
								, "Error with saving template setting.", Notification.POSITION_CENTERED);
					}
				} else{
					return;
				}
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
		boolean addSpaceBetweenPrefixAndCode = true;
		if(nameComponent.getAddSpaceOptionGroup().getValue().equals(messageSource.getMessage(Message.NO))){
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
		boolean useAMethodForAllCrosses = false;
		if(methodComponent.getCrossingMethodOptionGroup().getValue().equals(CrossingMethodOption.SAME_FOR_ALL_CROSSES)){
			isBasedOnStatusOfParentalLines = false;
			useAMethodForAllCrosses = true;
		}
		BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting(methodId, isBasedOnStatusOfParentalLines, useAMethodForAllCrosses);
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
}

