package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;

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
		defineCrossingSettingsLabel =  new Label("<b>" +messageSource.getMessage(Message.DEFINE_CROSSING_SETTINGS) 
				+ "</b>", Label.CONTENT_XHTML);
		defineCrossingSettingsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		mandatoryFieldLabel =  new Label("<i>" +messageSource.getMessage(Message.INDICATES_A_MANDATORY_FIELD) 
				+ "</i>", Label.CONTENT_XHTML);
		
		usePreviouslySavedSettingLabel = new Label();
		usePreviousSettingOptionGroup = new OptionGroup();
		usePreviousSettingOptionGroup.setImmediate(true);
		usePreviousSettingOptionGroup.addStyleName(AppConstants.CssStyles.HORIZONTAL_GROUP);
		
		settingsComboBox = new ComboBox();
		settingsComboBox.setWidth("250px");
		settingsComboBox.setImmediate(true);
		settingsComboBox.setNullSelectionAllowed(true);
		settingsComboBox.setTextInputAllowed(false);
	}

	@Override
	public void initializeValues() {
		usePreviousSettingOptionGroup.addItem(UsePreviousSettingOption.NO);
		usePreviousSettingOptionGroup.setItemCaption(UsePreviousSettingOption.NO, messageSource.getMessage(Message.NO));
		usePreviousSettingOptionGroup.addItem(UsePreviousSettingOption.YES);
		usePreviousSettingOptionGroup.setItemCaption(UsePreviousSettingOption.YES, messageSource.getMessage(Message.YES));
		usePreviousSettingOptionGroup.select(UsePreviousSettingOption.NO);
		
		settingsComboBox.setEnabled(false);
		settingsComboBox.setInputPrompt(messageSource.getMessage(Message.CHOOSE_SAVED_SETTINGS));
		setSettingsComboBox(null);
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
				settingsComboBox.setEnabled(doUsePreviousSetting);
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
	}

	@Override
	public void layoutComponents() {
		addComponent(defineCrossingSettingsLabel, "top:0px; left:0px");
		addComponent(mandatoryFieldLabel, "top:30px; left:0px");
		
		addComponent(usePreviouslySavedSettingLabel, "top:60px; left:0px");
		addComponent(usePreviousSettingOptionGroup, "top:60px; left:190px");
		addComponent(settingsComboBox, "top:60px; left:300px");
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

}
