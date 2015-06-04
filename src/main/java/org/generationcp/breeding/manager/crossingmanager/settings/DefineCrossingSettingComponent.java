
package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
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

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class DefineCrossingSettingComponent extends CssLayout implements BreedingManagerLayout, InitializingBean,
		InternationalizableComponent {

	private static final long serialVersionUID = 8015092540102625727L;
	private static final Logger LOG = LoggerFactory.getLogger(DefineCrossingSettingComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private ContextUtil contextUtil;

	private final CrossingSettingsDetailComponent settingsParentComponent;

	private Label defineCrossingSettingsLabel;
	private Label crossingSettingsHelp;

	private CheckBox selectSetting;

	private ComboBox settingsComboBox;
	private Button deleteSettingButton;

	public DefineCrossingSettingComponent(CrossingSettingsDetailComponent settingsParentComponent) {
		this.settingsParentComponent = settingsParentComponent;
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.defineCrossingSettingsLabel.setValue(this.messageSource.getMessage(Message.DEFINE_CROSSING_SETTINGS).toUpperCase());
		this.crossingSettingsHelp.setValue(this.messageSource.getMessage(Message.CROSSING_SETTINGS_HELP));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {

		this.defineCrossingSettingsLabel = new Label(this.messageSource.getMessage(Message.DEFINE_CROSSING_SETTINGS).toUpperCase());
		this.defineCrossingSettingsLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		this.crossingSettingsHelp = new Label(this.messageSource.getMessage(Message.CROSSING_SETTINGS_HELP), Label.CONTENT_XHTML);
		this.crossingSettingsHelp.addStyleName("gcp-content-help-text");

		this.selectSetting = new CheckBox(this.messageSource.getMessage(Message.LOAD_PREVIOUSLY_SAVED_SETTING) + ":");
		this.selectSetting.setImmediate(true);

		this.settingsComboBox = new ComboBox();
		this.settingsComboBox.setImmediate(true);
		this.settingsComboBox.setNullSelectionAllowed(true);
		this.settingsComboBox.setTextInputAllowed(false);
		this.settingsComboBox.setVisible(false);

		this.deleteSettingButton =
				new Button("<span class='glyphicon glyphicon-trash' style='color: #7c7c7c;font-size: 16px; font-weight: bold;'></span>");
		this.deleteSettingButton.setHtmlContentAllowed(true);
		this.deleteSettingButton.setDescription("Delete Setting");
		this.deleteSettingButton.setStyleName(BaseTheme.BUTTON_LINK);
		this.deleteSettingButton.setVisible(false);
	}

	@Override
	public void initializeValues() {
		this.selectSetting.setValue(false);
		this.setSettingsComboBox(null);
	}

	@Override
	public void addListeners() {

		this.selectSetting.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = -1282191407425721085L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				final Boolean selectSetting = (Boolean) event.getProperty().getValue();

				DefineCrossingSettingComponent.this.showSettingSelection(selectSetting);

				if (selectSetting) {
					DefineCrossingSettingComponent.this.settingsComboBox.focus();
				} else {
					DefineCrossingSettingComponent.this.settingsComboBox.setValue(null);
				}
			}
		});

		this.settingsComboBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if (DefineCrossingSettingComponent.this.settingsComboBox.getValue() != null) {
					DefineCrossingSettingComponent.this.settingsParentComponent.setCurrentSetting(DefineCrossingSettingComponent.this
							.getSelectedTemplateSetting());
					DefineCrossingSettingComponent.this.settingsParentComponent.setManageCrossingSettingsFields();
				} else {
					DefineCrossingSettingComponent.this.revertScreenToDefaultValues();
				}
			}

		});

		this.deleteSettingButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -432280582291837428L;

			@Override
			public void buttonClick(ClickEvent event) {
				DefineCrossingSettingComponent.this.settingsParentComponent.doDeleteAction();
			}
		});
	}

	private void showSettingSelection(Boolean show) {
		this.settingsComboBox.setVisible(show);
		this.deleteSettingButton.setVisible(show);
	}

	@Override
	public void layoutComponents() {

		final HorizontalLayout crossingForm = new HorizontalLayout();

		crossingForm.addComponent(this.selectSetting);
		crossingForm.addComponent(this.settingsComboBox);
		crossingForm.addComponent(this.deleteSettingButton);

		this.selectSetting.addStyleName("cs-form-label");
		this.settingsComboBox.addStyleName("cs-form-input");
		this.deleteSettingButton.addStyleName("cs-inline-icon");

		this.addComponent(this.defineCrossingSettingsLabel);
		this.addComponent(this.crossingSettingsHelp);
		this.addComponent(crossingForm);

	}

	public void setSettingsComboBox(TemplateSetting currentSetting) {
		this.settingsComboBox.removeAllItems();
		try {
			Project project = this.contextUtil.getProjectInContext();

			Tool crossingManagerTool = this.workbenchDataManager.getToolWithName(CrossingManagerSetting.CROSSING_MANAGER_TOOL_NAME);

			TemplateSetting templateSettingFilter = new TemplateSetting();
			templateSettingFilter.setTool(crossingManagerTool);
			templateSettingFilter.setProjectId(project.getProjectId().intValue());

			List<TemplateSetting> templateSettings = this.workbenchDataManager.getTemplateSettings(templateSettingFilter);

			for (TemplateSetting ts : templateSettings) {
				this.settingsComboBox.addItem(ts);
				this.settingsComboBox.setItemCaption(ts, ts.getName());

				if (ts.getIsDefault() != null && ts.getIsDefault() == 1) {
					this.settingsComboBox.select(ts);
					this.settingsParentComponent.setDefaultSetting(ts);
				}

			}

			if (currentSetting != null) {
				this.settingsComboBox.select(currentSetting);
			}

		} catch (MiddlewareQueryException e) {
			// commenting out code for showing error notification because at this point this component is not yet attached to a window and
			// so getWindow() returns null
			DefineCrossingSettingComponent.LOG.error("Error with retrieving Workbench template settings for Crossing Manager tool.", e);
		}
	}

	public TemplateSetting getSelectedTemplateSetting() {
		return (TemplateSetting) this.settingsComboBox.getValue();
	}

	private void revertScreenToDefaultValues() {
		this.settingsParentComponent.setCurrentSetting(null);
		this.settingsParentComponent.setDefaultManageCrossingSettingsFields();
	}

}
