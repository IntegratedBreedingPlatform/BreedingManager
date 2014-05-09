package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class DefineCrossingSettingComponent extends CssLayout implements BreedingManagerLayout,
		InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 8015092540102625727L;
	private static final Logger LOG = LoggerFactory.getLogger(DefineCrossingSettingComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	private final CrossingSettingsDetailComponent settingsParentComponent;

	private Label defineCrossingSettingsLabel;
	private Label crossingSettingsHelp;
	private Label settingsComboLabel;

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
		defineCrossingSettingsLabel.setValue(messageSource.getMessage(Message.DEFINE_CROSSING_SETTINGS).toUpperCase());
		crossingSettingsHelp.setValue(messageSource.getMessage(Message.CROSSING_SETTINGS_HELP));
		settingsComboLabel.setValue(messageSource.getMessage(Message.LOAD_PREVIOUSLY_SAVED_SETTING));
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

		defineCrossingSettingsLabel =  new Label(messageSource.getMessage(Message.DEFINE_CROSSING_SETTINGS).toUpperCase());
		defineCrossingSettingsLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		crossingSettingsHelp =  new Label(messageSource.getMessage(Message.CROSSING_SETTINGS_HELP), Label.CONTENT_XHTML);
		crossingSettingsHelp.addStyleName("gcp-content-help-text");

		settingsComboLabel = new Label(messageSource.getMessage(Message.LOAD_PREVIOUSLY_SAVED_SETTING));
		settingsComboBox = new ComboBox();
		settingsComboBox.setImmediate(true);
		settingsComboBox.setNullSelectionAllowed(true);
		settingsComboBox.setTextInputAllowed(false);

		deleteSettingButton = new Button("<span class='glyphicon glyphicon-trash' style='color: #7c7c7c;font-size: 16px; font-weight: bold;'></span>");
		deleteSettingButton.setHtmlContentAllowed(true);
		deleteSettingButton.setDescription("Delete Setting");
		deleteSettingButton.setStyleName(Reindeer.BUTTON_LINK);
	}

	@Override
	public void initializeValues() {
		setSettingsComboBox(null);
	}

	@Override
	public void addListeners() {
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

		final HorizontalLayout crossingForm = new HorizontalLayout();

		crossingForm.addComponent(settingsComboLabel);
		crossingForm.addComponent(settingsComboBox);
		crossingForm.addComponent(deleteSettingButton);

		settingsComboLabel.addStyleName("cs-form-label");
		settingsComboBox.addStyleName("cs-form-input");
		deleteSettingButton.addStyleName("cs-inline-icon");

		addComponent(defineCrossingSettingsLabel);
		addComponent(crossingSettingsHelp);
		addComponent(crossingForm);
	}

	public void setSettingsComboBox(TemplateSetting currentSetting){
		settingsComboBox.removeAllItems();
		try {
			Tool crossingManagerTool = workbenchDataManager.getToolWithName(CrossingManagerSetting.CROSSING_MANAGER_TOOL_NAME);

			TemplateSetting templateSettingFilter = new TemplateSetting();
			templateSettingFilter.setTool(crossingManagerTool);

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
