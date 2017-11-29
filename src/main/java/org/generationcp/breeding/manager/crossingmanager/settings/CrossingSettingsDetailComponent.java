
package org.generationcp.breeding.manager.crossingmanager.settings;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.spring.util.ContextUtil;
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

	public static final String CS_PANEL_SECTION = "cs-panel-section";
	private static final long serialVersionUID = -7733004867121978697L;
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsDetailComponent.class);
	private static final int SETTING_NAME_MAX_LENGTH = 64;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	WorkbenchDataManager workbenchDataManager;

	@Resource
	private ContextUtil contextUtil;

	private DefineCrossingSettingComponent defineSettingComponent;
	private CrossingSettingsOtherDetailsComponent additionalDetailsComponent;
	private Button nextButton;
	private Button cancelButton;
	private TemplateSetting currentSetting;
	private Panel sectionPanel;
	private Project project;
	private Tool crossingManagerTool;
	private TemplateSetting defaultSetting;

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.nextButton, Message.NEXT);
		this.messageSource.setCaption(this.cancelButton, Message.RESET);
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

		this.defineSettingComponent = new DefineCrossingSettingComponent(this);
		this.defineSettingComponent.setDebugId("defineSettingComponent");

		this.additionalDetailsComponent = new CrossingSettingsOtherDetailsComponent();
		this.additionalDetailsComponent.setDebugId("additionalDetailsComponent");

		this.nextButton = new Button();
		this.nextButton.setDebugId("nextButton");
		this.nextButton.setData(Actions.SAVE);
		this.nextButton.setWidth("80px");
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.cancelButton = new Button();
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setData(Actions.CANCEL);
		this.cancelButton.setWidth("80px");
		this.cancelButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());
	}

	@Override
	public void initializeValues() {
		this.currentSetting = null;
		this.project = null;
		this.crossingManagerTool = null;

		if (this.defineSettingComponent.getSelectedTemplateSetting() != null) {
			this.setCurrentSetting(this.defineSettingComponent.getSelectedTemplateSetting());
			this.setManageCrossingSettingsFields();
		}
	}

	@Override
	public void addListeners() {
		this.nextButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -432280582291837428L;

			@Override
			public void buttonClick(final ClickEvent event) {
				CrossingSettingsDetailComponent.this.doNextAction();
			}
		});

		this.cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				CrossingSettingsDetailComponent.this.doResetAction();
			}

		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		this.setWidth("900px");
		this.setHeight("900px");

		this.sectionPanel = new Panel();
		this.sectionPanel.setDebugId("sectionPanel");
		this.sectionPanel.setWidth("100%");
		this.sectionPanel.setHeight("850px");
		this.sectionPanel.addStyleName(AppConstants.CssStyles.PANEL_GRAY_BACKGROUND);

		final CssLayout sectionLayout = new CssLayout();
		sectionLayout.setDebugId("sectionLayout");
		sectionLayout.setMargin(false, true, true, true);

		// cs is our crossing settings namespace
		sectionLayout.addStyleName("cs");
		this.defineSettingComponent.addStyleName(CrossingSettingsDetailComponent.CS_PANEL_SECTION);

		sectionLayout.addComponent(this.defineSettingComponent);
		sectionLayout.addComponent(this.additionalDetailsComponent);

		this.sectionPanel.setLayout(sectionLayout);

		// 3
		final HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setDebugId("buttonBar");
		buttonBar.setSpacing(true);
		buttonBar.setMargin(true);
		buttonBar.addComponent(this.cancelButton);
		buttonBar.addComponent(this.nextButton);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(buttonBar);
		buttonLayout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);

		this.addComponent(this.sectionPanel);
		this.addComponent(buttonLayout);
	}

	public void doResetAction() {

		final String message;
		if (this.currentSetting != null) {
			message = "Are you sure you want to reset the current setting for '" + this.currentSetting.getName() + "'?";
		} else {
			message = "Are you sure you want to reset the current setting ?";
		}

		ConfirmDialog.show(this.getWindow(), this.messageSource.getMessage(Message.CONFIRM_RESET_CROSSING_MANAGER_SETTINGS_TITLE), message,
				"Yes", "No", new ConfirmDialog.Listener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClose(final ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							if (CrossingSettingsDetailComponent.this.currentSetting != null) {
								CrossingSettingsDetailComponent.this.setManageCrossingSettingsFields();
							} else {
								CrossingSettingsDetailComponent.this.setDefaultManageCrossingSettingsFields();
							}
							MessageNotifier.showMessage(CrossingSettingsDetailComponent.this.getWindow(),
									CrossingSettingsDetailComponent.this.messageSource.getMessage(Message.SUCCESS),
									"Crossing Manager Setting has been reset.");
						}
					}
				});
	}

	public void setManageCrossingSettingsFields() {
		if (this.currentSetting != null) {
			final CrossingManagerSetting templateSetting;
			try {
				templateSetting = this.readXmlStringForSetting(this.currentSetting.getConfiguration());

				this.additionalDetailsComponent.setFields(templateSetting.getAdditionalDetailsSetting(), templateSetting.getName(),
						this.currentSetting.isDefault());

			} catch (final JAXBException e) {
				CrossingSettingsDetailComponent.LOG.error("Error with retrieving template setting.", e);

			}
		}
	}

	public void doDeleteAction() {
		if (this.currentSetting != null) {
			final String message = "Are you sure you want to delete '" + this.currentSetting.getName() + "'?";
			ConfirmDialog.show(this.getWindow(), "Delete Crossing Manage Setting", message, "Yes", "No", new ConfirmDialog.Listener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClose(final ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						try {
							CrossingSettingsDetailComponent.this.workbenchDataManager
									.deleteTemplateSetting(CrossingSettingsDetailComponent.this.currentSetting);
							CrossingSettingsDetailComponent.this.defineSettingComponent.setSettingsComboBox(null);
							CrossingSettingsDetailComponent.this.setDefaultManageCrossingSettingsFields();

							MessageNotifier.showMessage(CrossingSettingsDetailComponent.this.getWindow(),
									CrossingSettingsDetailComponent.this.messageSource.getMessage(Message.SUCCESS),
									"Crossing Manager Setting has been deleted.");
						} catch (final MiddlewareQueryException e) {
							CrossingSettingsDetailComponent.LOG.error("Error with deleting the manage crossing template setting", e);

						}
					}
				}
			});
		} else {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					"There is no selected crossing manager setting to delete.");
		}

	}

	private void doNextAction() {
		if (this.additionalDetailsComponent.validateInputFields() && this.additionalDetailsComponent.settingsFileNameProvided()) {
			if (this.defaultSetting != null && !this.defaultSetting.equals(this.currentSetting)
					&& (Boolean) this.additionalDetailsComponent.getSetAsDefaultSettingCheckbox().getValue()) {
				ConfirmDialog.show(this.getWindow(), "Save Crossing Setting",
						"There is already an existing default setting. Do you want to replace the default setting?", "Yes", "No",
						new ConfirmDialog.Listener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClose(final ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									CrossingSettingsDetailComponent.this.saveSetting();
								} else {
									CrossingSettingsDetailComponent.this.additionalDetailsComponent.setSetAsDefaultSettingCheckbox(false);
									CrossingSettingsDetailComponent.this.saveSetting();
								}
							}
						});

			} else {
				this.saveSetting();
			}
		}
	}

	public void updateTemplateSettingVariables() {
		try {
			this.project = this.contextUtil.getProjectInContext();
			this.crossingManagerTool = this.workbenchDataManager.getToolWithName(CrossingManagerSetting.CROSSING_MANAGER_TOOL_NAME);
		} catch (final MiddlewareQueryException ex) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					"Error with retrieving currently opened Workbench Program and Crossing Manager Tool record.");
			CrossingSettingsDetailComponent.LOG
					.error("Error with retrieving currently opened Workbench Program and Crossing Manager Tool record.", ex);
		}
	}

	private void saveSetting() {

		this.updateTemplateSettingVariables();

		// TODO clarify the saving of setting operation now that all settings are not on the same page
		final CrossingManagerSetting currentlyDefinedSettingsInUi = this.getPartialCurrentSetting();

		if (this.currentSetting == null) {
			final TemplateSetting templateSetting = new TemplateSetting();
			final String settingName = this.getCurrentSettingNameinUI();
			templateSetting.setName(settingName);
			if (!this.doesSettingNameExist(settingName, this.project.getProjectId().intValue(), this.crossingManagerTool)) {
				templateSetting.setIsDefault(this.additionalDetailsComponent.getSetAsDefaultSettingCheckbox().booleanValue());
				templateSetting.setProjectId(this.project.getProjectId().intValue());
				templateSetting.setTool(this.crossingManagerTool);
				templateSetting.setTemplateSettingId(null);

				try {
					final String configuration = this.getXmlStringForSetting(currentlyDefinedSettingsInUi);
					templateSetting.setConfiguration(configuration);
				} catch (final JAXBException ex) {
					MessageNotifier.showError(this.getWindow(), "XML Writing Error",
							"There was an error with writing the XML for the setting.");
					CrossingSettingsDetailComponent.LOG.error("Error with writing XML String.", ex);
					return;
				}

				try {
					final Integer templateSettingId = this.workbenchDataManager.addTemplateSetting(templateSetting);
					final List<TemplateSetting> results = this.workbenchDataManager
							.getTemplateSettings(new TemplateSetting(templateSettingId, null, null, null, null, null));
					if (!results.isEmpty()) {
						this.currentSetting = results.get(0);
						this.defineSettingComponent.setSettingsComboBox(this.currentSetting);

					} else {
						templateSetting.setTemplateSettingId(templateSettingId);
						this.currentSetting = templateSetting;
					}

					MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
							"Crossing Manager Settings have been saved.");
				} catch (final MiddlewareQueryException ex) {
					CrossingSettingsDetailComponent.LOG.error("Error with saving template setting.", ex);
					MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
							"Error with saving template setting.");
				}
			} else {
				this.confirmCrossingSettingOverwrite();
				return;
			}
		} else {
			boolean thereIsAChange = false;
			final String currentSettingNameInUi = this.getCurrentSettingNameinUI();

			if (!this.currentSetting.getName().equals(currentSettingNameInUi)) {
				if (!this.doesSettingNameExist(currentSettingNameInUi, this.project.getProjectId().intValue(), this.crossingManagerTool)) {
					this.currentSetting.setName(currentSettingNameInUi);
					thereIsAChange = true;
				} else {
					this.confirmCrossingSettingOverwrite();
					return;
				}
			}

			this.updateSetting(currentlyDefinedSettingsInUi, thereIsAChange);
		}
	}

	public void confirmCrossingSettingOverwrite() {
		ConfirmDialog.show(this.getWindow(), "Save Crossing Setting",
				"There is an existing setting with the same name you have specified." + " Do you want to overwrite the existing setting?",
				"Yes", "No", new ConfirmDialog.Listener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClose(final ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							CrossingSettingsDetailComponent.this.overwriteSetting();
						} else {
							CrossingSettingsDetailComponent.this.additionalDetailsComponent.getSettingsNameTextfield().focus();
						}
					}
				});
	}

	public void overwriteSetting() {
		// TODO clarify the saving of setting operation now that all settings are not on the same page
		final CrossingManagerSetting currentlyDefinedSettingsInUi = this.getPartialCurrentSetting();

		// get the existing setting
		final TemplateSetting templateSettingToOverwrite =
				this.getExistingTemplateSetting(this.project.getProjectId().intValue(), this.crossingManagerTool);
		this.currentSetting = templateSettingToOverwrite;

		this.updateSetting(currentlyDefinedSettingsInUi, false);

	}

	public void updateSetting(final CrossingManagerSetting currentlyDefinedSettingsInUi, final boolean thereIsAChange) {
		boolean settingChangeResult = thereIsAChange;
		try {
			final CrossingManagerSetting savedSetting = this.readXmlStringForSetting(this.currentSetting.getConfiguration());
			if (!currentlyDefinedSettingsInUi.equals(savedSetting)) {
				try {
					final String configuration = this.getXmlStringForSetting(currentlyDefinedSettingsInUi);
					this.currentSetting.setConfiguration(configuration);
					settingChangeResult = true;
				} catch (final JAXBException ex) {
					MessageNotifier.showError(this.getWindow(), "XML Writing Error",
							"There was an error with writing the XML for the setting.");
					CrossingSettingsDetailComponent.LOG.error("Error with writing XML String.", ex);
					return;
				}
			}
		} catch (final JAXBException ex) {
			CrossingSettingsDetailComponent.LOG.error("Error with parsing crossing manager XML string.", ex);
			MessageNotifier.showError(this.getWindow(), "XML Parsing Error", "Error with parsing XML string for Crossing Manager setting.");
			return;
		}

		if (!this.currentSetting.isDefault().equals(this.additionalDetailsComponent.getSetAsDefaultSettingCheckbox().booleanValue())) {
			this.currentSetting.setIsDefault(this.additionalDetailsComponent.getSetAsDefaultSettingCheckbox().booleanValue());
			settingChangeResult = true;
		}

		try {
			if (settingChangeResult) {
				this.workbenchDataManager.updateTemplateSetting(this.currentSetting);
				// must reload settings combobox to solve out of sync when going back to this screen
				this.defineSettingComponent.setSettingsComboBox(this.currentSetting);
				MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
						"Crossing Manager Setting has been updated.");
			}
		} catch (final MiddlewareQueryException ex) {
			CrossingSettingsDetailComponent.LOG.error("Error with updating template setting record.", ex);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					"Error with updating Crossing Manager Setting.");
		}
	}

	public TemplateSetting getExistingTemplateSetting(final Integer projectId, final Tool tool) {

		final String name = this.getCurrentSettingNameinUI();
		final TemplateSetting existingTemplateSetting;

		final TemplateSetting filter = new TemplateSetting();
		filter.setName(name);
		filter.setConfiguration(null);
		filter.setIsDefaultToNull();
		filter.setProjectId(projectId);
		filter.setTemplateSettingId(null);
		filter.setTool(tool);
		try {
			final List<TemplateSetting> results = this.workbenchDataManager.getTemplateSettings(filter);
			if (!results.isEmpty()) {
				existingTemplateSetting = results.get(0);

				return existingTemplateSetting;
			}
		} catch (final MiddlewareQueryException ex) {
			CrossingSettingsDetailComponent.LOG
					.error("Error getting template settings for project:" + projectId + "and crossing manager tool.", ex);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					"Error with checking for uniqueness of settings name.");
		}
		return null;
	}

	public String getCurrentSettingNameinUI() {
		String currentSettingNameInUi = (String) this.additionalDetailsComponent.getSettingsNameTextfield().getValue();
		currentSettingNameInUi = currentSettingNameInUi.toString();
		currentSettingNameInUi = currentSettingNameInUi.trim();
		currentSettingNameInUi = currentSettingNameInUi.substring(0,
				Math.min(currentSettingNameInUi.length(), CrossingSettingsDetailComponent.SETTING_NAME_MAX_LENGTH));

		return currentSettingNameInUi;
	}

	private boolean doesSettingNameExist(final String name, final Integer projectId, final Tool tool) {
		final TemplateSetting filter = new TemplateSetting();
		filter.setName(name);
		filter.setConfiguration(null);
		filter.setIsDefaultToNull();
		filter.setProjectId(projectId);
		filter.setTemplateSettingId(null);
		filter.setTool(tool);
		try {
			final List<TemplateSetting> settings = this.workbenchDataManager.getTemplateSettings(filter);
			if (!settings.isEmpty()) {
				return true;
			}
		} catch (final MiddlewareQueryException ex) {
			CrossingSettingsDetailComponent.LOG
					.error("Error getting template settings for project:" + projectId + "and crossing manager tool.", ex);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					"Error with checking for uniqueness of settings name.");
			return true;
		}
		return false;
	}

	/**
	 * Make sure to validate the input fields first before calling this method.
	 *
	 * The method is named getPartialCurrentSetting because it does not include the breeding method setting
	 *
	 * @return
	 */
	public CrossingManagerSetting getPartialCurrentSetting() {
		final CrossingManagerSetting toreturn = new CrossingManagerSetting();

		final CrossNameSetting crossNameSettingPojo = new CrossNameSetting();
		toreturn.setCrossNameSetting(crossNameSettingPojo);

		final Integer locId = (Integer) this.additionalDetailsComponent.getHarvestLocComboBox().getValue();
		final String harvestDate = this.additionalDetailsComponent.getHarvestDtDateField().getValue();
		final AdditionalDetailsSetting additionalDetails = new AdditionalDetailsSetting(locId, harvestDate);
		toreturn.setAdditionalDetailsSetting(additionalDetails);

		String settingName = (String) this.additionalDetailsComponent.getSettingsNameTextfield().getValue();
		settingName = settingName.trim();
		toreturn.setName(settingName);

		return toreturn;
	}

	private String getXmlStringForSetting(final CrossingManagerSetting setting) throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(CrossingManagerSetting.class);
		final Marshaller marshaller = context.createMarshaller();
		final StringWriter writer = new StringWriter();
		marshaller.marshal(setting, writer);
		return writer.toString();
	}

	private CrossingManagerSetting readXmlStringForSetting(final String xmlString) throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(CrossingManagerSetting.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		return (CrossingManagerSetting) unmarshaller.unmarshal(new StringReader(xmlString));
	}

	public TemplateSetting getCurrentSetting() {
		return this.currentSetting;
	}

	public void setCurrentSetting(final TemplateSetting currentSetting) {
		this.currentSetting = currentSetting;
	}

	public void setDefaultManageCrossingSettingsFields() {
		this.additionalDetailsComponent.setFieldsDefaultValue();
	}

	public void setDefaultSetting(final TemplateSetting defaultSetting) {
		this.defaultSetting = defaultSetting;
	}

	public enum Actions {
		SAVE, CANCEL, DELETE
	}

}
