package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class DefineCrossingSettingComponent extends AbsoluteLayout implements BreedingManagerLayout,
		InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 8015092540102625727L;
	private static final Logger LOG = LoggerFactory.getLogger(DefineCrossingSettingComponent.class);
	
	public enum UsePreviousSettingOption {
		YES, NO
	}
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	private CrossingSettingsDetailComponent settingsParentComponent;
	
	private Label defineCrossingSettingsLabel;
	private Label mandatoryFieldLabel;
	private Label usePreviouslySavedSettingLabel;
	
	private OptionGroup usePreviousSettingOptionGroup;
	private ComboBox settingsComboBox;
	private Button deleteSettingButton;
	
	public DefineCrossingSettingComponent(CrossingSettingsDetailComponent settingsParentComponent){
		this.settingsParentComponent = settingsParentComponent;
	}
	
	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}
	
	@Override
	public void updateLabels() {
		usePreviouslySavedSettingLabel.setValue(messageSource.getMessage(Message.USE_PREVIOUSLY_SAVED_SETTING));
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
		defineCrossingSettingsLabel =  new Label(messageSource.getMessage(Message.DEFINE_CROSSING_SETTINGS));
		defineCrossingSettingsLabel.setWidth("250px");
		defineCrossingSettingsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		mandatoryFieldLabel =  new Label("<i>" +messageSource.getMessage(Message.INDICATES_A_MANDATORY_FIELD) 
				+ "</i>", Label.CONTENT_XHTML);
		
		usePreviouslySavedSettingLabel = new Label();
		usePreviouslySavedSettingLabel.addStyleName(AppConstants.CssStyles.BOLD);
		
		usePreviousSettingOptionGroup = new OptionGroup();
		usePreviousSettingOptionGroup.setImmediate(true);
		usePreviousSettingOptionGroup.addStyleName(AppConstants.CssStyles.HORIZONTAL_GROUP);
		
		settingsComboBox = new ComboBox();
		settingsComboBox.setWidth("260px");
		settingsComboBox.setImmediate(true);
		settingsComboBox.setNullSelectionAllowed(true);
		settingsComboBox.setTextInputAllowed(false);
			
		deleteSettingButton = new Button("<span class='glyphicon glyphicon-trash' style='left: 2px; color: #7c7c7c;font-size: 16px; font-weight: bold;'></span>");
		deleteSettingButton.setHtmlContentAllowed(true);
		deleteSettingButton.setDescription("Delete Setting");
		deleteSettingButton.setStyleName(Reindeer.BUTTON_LINK);
		deleteSettingButton.setWidth("25px");
	}

	@Override
	public void initializeValues() {
		usePreviousSettingOptionGroup.addItem(UsePreviousSettingOption.NO);
		usePreviousSettingOptionGroup.setItemCaption(UsePreviousSettingOption.NO, messageSource.getMessage(Message.NO));
		usePreviousSettingOptionGroup.addItem(UsePreviousSettingOption.YES);
		usePreviousSettingOptionGroup.setItemCaption(UsePreviousSettingOption.YES, messageSource.getMessage(Message.YES));
		usePreviousSettingOptionGroup.select(UsePreviousSettingOption.NO);
		
		settingsComboBox.setInputPrompt(messageSource.getMessage(Message.CHOOSE_SAVED_SETTINGS));
		setSettingsComboBox(null);
		toggleSettingsFields(false);
	}

	@Override
	public void addListeners() {
		// enable / disable settings combobox
		usePreviousSettingOptionGroup.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				boolean doUsePreviousSetting = UsePreviousSettingOption.YES.equals(
						usePreviousSettingOptionGroup.getValue());
				toggleSettingsFields(doUsePreviousSetting);
				if (!doUsePreviousSetting){
					revertScreenToDefaultValues();
				}
			}
		});
		
		settingsComboBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(settingsComboBox.getValue() != null){
					settingsParentComponent.setCurrentSetting(getSelectedTemplateSetting());
					settingsParentComponent.setManageCrossingSettingsFields();
				}
				else{
					revertScreenToDefaultValues();
				}
			}

		});
		
		deleteSettingButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -432280582291837428L;

			@Override
			public void buttonClick(ClickEvent event) {
				settingsParentComponent.doDeleteAction();
			}
		});
	}

	@Override
	public void layoutComponents() {
		HeaderLabelLayout manageGermplasmHeader = new HeaderLabelLayout(AppConstants.Icons.ICON_MANAGE_SETTINGS, defineCrossingSettingsLabel);
		addComponent(manageGermplasmHeader, "top:0px; left:0px");
		addComponent(mandatoryFieldLabel, "top:30px; left:0px");
		
		addComponent(usePreviouslySavedSettingLabel, "top:60px; left:0px");
		addComponent(usePreviousSettingOptionGroup, "top:60px; left:205px");
		addComponent(settingsComboBox, "top:60px; left:300px");
		addComponent(deleteSettingButton, "top:63px; left:570px");
	}
	
	public void setSettingsComboBox(TemplateSetting currentSetting){
		TemplateSetting templateSettingFilter = new TemplateSetting();
		settingsComboBox.removeAllItems();
		try {
			List<TemplateSetting> templateSettings = workbenchDataManager.getTemplateSettings(templateSettingFilter);
			
			for(TemplateSetting ts : templateSettings){
				settingsComboBox.addItem(ts);
				settingsComboBox.setItemCaption(ts, ts.getName());
			}
			
			if(currentSetting != null){
				usePreviousSettingOptionGroup.select(UsePreviousSettingOption.YES);
				settingsComboBox.select(currentSetting);
			}
			
		} catch (MiddlewareQueryException e) {
			//commenting out code for showing error notification because at this point this component is not yet attached to a window and so getWindow() returns null
			//MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR), "Error getting crossing templates!");
			LOG.error("Error with retrieving Workbench template settings for Crossing Manager tool.", e);
		}
	}
	
	public TemplateSetting getSelectedTemplateSetting(){
		return (TemplateSetting) settingsComboBox.getValue();
	}
	
	private void revertScreenToDefaultValues() {
		settingsParentComponent.setCurrentSetting(null);
		settingsParentComponent.setDefaultManageCrossingSettingsFields();
	}
	
	private void toggleSettingsFields(boolean enabled){
		settingsComboBox.setEnabled(enabled);
		deleteSettingButton.setEnabled(enabled);
	}

}
