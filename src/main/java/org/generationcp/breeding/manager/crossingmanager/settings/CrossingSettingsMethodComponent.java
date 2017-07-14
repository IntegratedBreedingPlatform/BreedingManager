
package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.application.BreedingManagerWindowGenerator;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Method;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class CrossingSettingsMethodComponent extends VerticalLayout
		implements InternationalizableComponent, InitializingBean, BreedingManagerLayout {

	public static final String GENERATIVE_METHOD_TYPE = "GEN";
	private static final long serialVersionUID = 8287596386088188565L;
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsMethodComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private BreedingManagerWindowGenerator breedingManagerWindowGenerator;

	@Resource
	private ContextUtil contextUtil;

	private Label breedingMethodLabel;
	private Label breedingMethodDescLabel;

	private CheckBox selectMethod;

	private ComboBox breedingMethods;

	private CheckBox favoriteMethodsCheckbox;
	
	private Button manageFavoriteMethodsLink;

	private OptionGroup breedingMethodsRadioBtn;

	private PopupView methodPopupView;
	private Label breedingMethodsHelpPopup;

	private List<Method> methods;

	@Autowired
	private BreedingManagerService breedingManagerService;
	private String programUniqueId;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
		this.initializePopulateFavoriteMethod(this.programUniqueId);
	}

	public void initializePopulateFavoriteMethod(final String programUUID) {
		final List<Method> favoriteGenerativeMethods =
				this.germplasmDataManager.getFavoriteMethodsByMethodType(CrossingSettingsMethodComponent.GENERATIVE_METHOD_TYPE, programUUID);
		if (favoriteGenerativeMethods == null || favoriteGenerativeMethods.isEmpty()) {
			this.favoriteMethodsCheckbox.setValue(false);
			this.populateBreedingMethods(false, this.programUniqueId);
		} else {
			this.favoriteMethodsCheckbox.setValue(true);
			BreedingManagerUtil.populateMethodsComboBox(this.breedingMethods,
					CrossingSettingsMethodComponent.GENERATIVE_METHOD_TYPE, favoriteGenerativeMethods);
		}
	}

	private boolean isSelectAllMethods() {
		return ((String) this.breedingMethodsRadioBtn.getValue()).equals(this.messageSource.getMessage(Message.SHOW_ALL_METHODS));
	}

	@Override
	public void updateLabels() {
		this.breedingMethodLabel.setValue(this.messageSource.getMessage(Message.BREEDING_METHOD).toUpperCase());
		this.breedingMethodDescLabel.setValue(this.messageSource.getMessage(Message.BREEDING_METHOD_DESC));
	}

	@Override
	public void instantiateComponents() {

		this.breedingMethodLabel = new Label(this.messageSource.getMessage(Message.BREEDING_METHOD));
		this.breedingMethodLabel.setDebugId("breedingMethodLabel");
        this.breedingMethodLabel.setWidth("200px");
        this.breedingMethodLabel.setStyleName(Bootstrap.Typography.H4.styleName());
        this.breedingMethodLabel.addStyleName(AppConstants.CssStyles.BOLD);


		this.breedingMethodDescLabel = new Label(this.messageSource.getMessage(Message.BREEDING_METHOD_DESC));
		this.breedingMethodDescLabel.setDebugId("breedingMethodDescLabel");
		this.breedingMethodDescLabel.addStyleName("gcp-content-help-text");

		this.selectMethod = new CheckBox(this.messageSource.getMessage(Message.SELECT_A_METHOD_TO_USE_FOR_ALL_CROSSES) + ":");
		this.selectMethod.setDebugId("selectMethod");
		this.selectMethod.setImmediate(true);
		this.selectMethod.addStyleName(AppConstants.CssStyles.BOLD);

		this.breedingMethods = new ComboBox();
		this.breedingMethods.setDebugId("breedingMethods");
		this.breedingMethods.setImmediate(true);
		this.breedingMethods.setNullSelectionAllowed(false);

		this.breedingMethodsHelpPopup = new Label();
		this.breedingMethodsHelpPopup.setDebugId("breedingMethodsHelpPopup");
		this.breedingMethodsHelpPopup.setEnabled(false);
		this.breedingMethodsHelpPopup.setWidth("500px");

		this.methodPopupView = new PopupView("?", this.breedingMethodsHelpPopup);
		this.methodPopupView.setDebugId("methodPopupView");
		this.methodPopupView.addStyleName(AppConstants.CssStyles.POPUP_VIEW);

		this.favoriteMethodsCheckbox = new CheckBox(this.messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_METHODS));
		this.favoriteMethodsCheckbox.setDebugId("favoriteMethodsCheckbox");
		this.favoriteMethodsCheckbox.setImmediate(true);

		this.breedingMethodsRadioBtn = new OptionGroup();
		this.breedingMethodsRadioBtn.setDebugId("breedingMethodsRadioBtn");
		this.breedingMethodsRadioBtn.setMultiSelect(false);
		this.breedingMethodsRadioBtn.setImmediate(true);
		this.breedingMethodsRadioBtn.setStyleName("v-select-optiongroup-horizontal");
		this.breedingMethodsRadioBtn.addItem(this.messageSource.getMessage(Message.SHOW_ALL_METHODS));
		this.breedingMethodsRadioBtn.addItem(this.messageSource.getMessage(Message.SHOW_GENERATIVE_METHODS));
		this.breedingMethodsRadioBtn.select(this.messageSource.getMessage(Message.SHOW_GENERATIVE_METHODS));

		this.manageFavoriteMethodsLink = new Button();
		this.manageFavoriteMethodsLink.setDebugId("manageFavoriteMethodsLink");
		this.manageFavoriteMethodsLink.setStyleName(BaseTheme.BUTTON_LINK);
		this.manageFavoriteMethodsLink.setCaption(this.messageSource.getMessage(Message.MANAGE_METHODS));

		try {
			this.programUniqueId = this.breedingManagerService.getCurrentProject().getUniqueID();
		} catch (final MiddlewareQueryException e) {
			CrossingSettingsMethodComponent.LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void initializeValues() {
		this.selectMethod.setValue(false);
		this.breedingMethods.setValue(null);
		this.showBreedingMethodSelection(false);

		// Retrieve breeding methods
		try {
			this.methods = this.germplasmDataManager.getMethodsByType(CrossingSettingsMethodComponent.GENERATIVE_METHOD_TYPE,
					this.programUniqueId);
		} catch (final MiddlewareQueryException e) {
			CrossingSettingsMethodComponent.LOG.error(e.getMessage());
		}
	}

	@Override
	public void addListeners() {

		this.selectMethod.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = -1282191407425721085L;

			@Override
			public void valueChange(final ValueChangeEvent event) {

				final Boolean selectMethod = (Boolean) event.getProperty().getValue();

				CrossingSettingsMethodComponent.this.showBreedingMethodSelection(selectMethod);

				if (selectMethod) {
					CrossingSettingsMethodComponent.this.populateBreedingMethods(
							(Boolean) CrossingSettingsMethodComponent.this.favoriteMethodsCheckbox.getValue(),
							CrossingSettingsMethodComponent.this.programUniqueId);
					CrossingSettingsMethodComponent.this.breedingMethods.focus();
					CrossingSettingsMethodComponent.this.enableMethodHelp(false);
				} else {
					CrossingSettingsMethodComponent.this.breedingMethods.setValue(null);
				}
			}
		});

		this.breedingMethods.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 6294894800193942274L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final int size = CrossingSettingsMethodComponent.this.breedingMethods.size();
				if (size > 0) {
					CrossingSettingsMethodComponent.this.showMethodDescription((Integer) event.getProperty().getValue());
				}

				final Boolean methodSelected = CrossingSettingsMethodComponent.this.breedingMethods.getValue() != null;
				CrossingSettingsMethodComponent.this.enableMethodHelp(methodSelected);
			}
		});

		final Property.ValueChangeListener breedingMethodsListener = new Property.ValueChangeListener() {

			private static final long serialVersionUID = -4064520391948241747L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				CrossingSettingsMethodComponent.this.populateBreedingMethods(
						(Boolean) CrossingSettingsMethodComponent.this.favoriteMethodsCheckbox.getValue(),
						CrossingSettingsMethodComponent.this.programUniqueId);
			}

		};

		this.favoriteMethodsCheckbox.addListener(breedingMethodsListener);
		this.breedingMethodsRadioBtn.addListener(breedingMethodsListener);

		this.manageFavoriteMethodsLink.addListener(new ClickListener() {

			private static final long serialVersionUID = 1525347479193533974L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					final Project project = CrossingSettingsMethodComponent.this.contextUtil.getProjectInContext();
					final Window manageFavoriteMethodsWindow =
							breedingManagerWindowGenerator.openMethodManagerPopupWindow(project.getProjectId(),
									CrossingSettingsMethodComponent.this.getWindow(),
									CrossingSettingsMethodComponent.this.messageSource.getMessage(Message.MANAGE_METHODS));
					manageFavoriteMethodsWindow.addListener(new CloseListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void windowClose(final CloseEvent e) {
							final Object lastValue = CrossingSettingsMethodComponent.this.breedingMethods.getValue();
							CrossingSettingsMethodComponent.this.populateBreedingMethods(
									(Boolean) CrossingSettingsMethodComponent.this.favoriteMethodsCheckbox.getValue(),
									CrossingSettingsMethodComponent.this.programUniqueId);
							CrossingSettingsMethodComponent.this.breedingMethods.setValue(lastValue);
						}
					});
				} catch (final MiddlewareQueryException e) {
					CrossingSettingsMethodComponent.LOG.error(e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void layoutComponents() {

		this.setSpacing(true);

		this.breedingMethods.addStyleName("cs-form-input");
		this.methodPopupView.addStyleName("cs-inline-icon");

		final VerticalLayout methodSelectLayout = new VerticalLayout();
		methodSelectLayout.setDebugId("methodSelectLayout");

		final HorizontalLayout selectWithPopupLayout = new HorizontalLayout();
		selectWithPopupLayout.setDebugId("selectWithPopupLayout");
		selectWithPopupLayout.addComponent(this.breedingMethods);
		selectWithPopupLayout.addComponent(this.methodPopupView);

		methodSelectLayout.addComponent(selectWithPopupLayout);
		methodSelectLayout.addComponent(this.breedingMethodsRadioBtn);
		methodSelectLayout.addComponent(this.favoriteMethodsCheckbox);
		methodSelectLayout.addComponent(this.manageFavoriteMethodsLink);

		final VerticalLayout internalPanelLayout = new VerticalLayout();
		internalPanelLayout.setDebugId("internalPanelLayout");
		internalPanelLayout.setSpacing(true);
		internalPanelLayout.setMargin(true);
		internalPanelLayout.addComponent(this.breedingMethodDescLabel);
		internalPanelLayout.addComponent(this.selectMethod);
		internalPanelLayout.addComponent(methodSelectLayout);

		final Panel breedingMethodPanel = new Panel();
		breedingMethodPanel.setDebugId("breedingMethodPanel");
		breedingMethodPanel.setWidth("460px");
		breedingMethodPanel.setLayout(internalPanelLayout);
		breedingMethodPanel.addStyleName("section_panel_layout");

		final HeaderLabelLayout breedingMethodLayout = new HeaderLabelLayout(null, this.breedingMethodLabel);
		breedingMethodLayout.setDebugId("breedingMethodLayout");
		breedingMethodLayout.setDebugId("breedingMethodLayout");
		this.addComponent(breedingMethodLayout);
		this.addComponent(breedingMethodPanel);
	}

	public void setFields(final BreedingMethodSetting breedingMethodSetting) {
		if (breedingMethodSetting.isBasedOnStatusOfParentalLines()) {
			this.selectMethod.setValue(false);
		} else {
			this.selectMethod.setValue(true);
			final Integer methodId = breedingMethodSetting.getMethodId();

			if (!this.isMethodGenerative(methodId)) {
				this.favoriteMethodsCheckbox.setValue(true);
				this.populateBreedingMethods(true, this.programUniqueId);
			} else {
				this.favoriteMethodsCheckbox.setValue(false);
				this.populateBreedingMethods(false, this.programUniqueId);
			}
			this.breedingMethods.select(methodId);
			this.showMethodDescription(methodId);
		}
	}

	private boolean isMethodGenerative(final Integer methodId) {
		for (final Method method : this.methods) {
			if (method.getMid().equals(methodId)) {
				return true;
			}
		}
		return false;
	}

	public void setFieldsDefaultValue() {
		this.selectMethod.setValue(false);
		this.breedingMethods.setValue(null);
	}

	/**
	 * Whether or not the user is leaving the crossing method to be determined by the status of parental lines.
	 *
	 * @return true if the method is based on the status of parental lines, false if one method has been selected for all crosses
	 */
	public Boolean isBasedOnStatusOfParentalLines() {
		// The checkbox is actually the reverse of this - false indicates based on parental lines (as this is the default)
		return !this.selectMethod.booleanValue();
	}

	/**
	 * Get the ID of the currently selected breeding method.
	 *
	 * @return the ID of the currently selected breeding method, or null if none are selected.
	 */
	public Integer getSelectedBreedingMethodId() {
		return (Integer) this.breedingMethods.getValue();
	}

	private void showMethodDescription(final Integer methodId) {
		if (methodId != null) {
			try {
				final String methodDescription = this.germplasmDataManager.getMethodByID(methodId).getMdesc();
				this.breedingMethodsHelpPopup.setValue(methodDescription);
				this.breedingMethods.setDescription(methodDescription);

			} catch (final MiddlewareQueryException e) {
				CrossingSettingsMethodComponent.LOG.error(e.getMessage(), e);
			} catch (final ClassCastException e) {
				CrossingSettingsMethodComponent.LOG.error(e.getMessage(), e);
			}
		}
	}

	private void enableMethodHelp(final Boolean enable) {
		this.breedingMethodsHelpPopup.setEnabled(enable);
		this.methodPopupView.setEnabled(enable);
	}

	private void showBreedingMethodSelection(final Boolean show) {
		this.breedingMethods.setVisible(show);
		this.favoriteMethodsCheckbox.setVisible(show);
		this.breedingMethodsRadioBtn.setVisible(show);
		this.manageFavoriteMethodsLink.setVisible(show);
		this.breedingMethodsHelpPopup.setVisible(show);
		this.methodPopupView.setVisible(show);
	}

	private void populateBreedingMethods(final boolean showOnlyFavorites, final String programUUID) {
		this.breedingMethods.removeAllItems();

		if (showOnlyFavorites) {
			try {
				String mtype = null;

				if (!this.isSelectAllMethods()) {
					mtype = CrossingSettingsMethodComponent.GENERATIVE_METHOD_TYPE;
				}

				BreedingManagerUtil.populateWithFavoriteMethods(this.workbenchDataManager, this.germplasmDataManager, this.breedingMethods, mtype, programUUID);
			} catch (final MiddlewareQueryException e) {
				CrossingSettingsMethodComponent.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
						"Error getting favorite methods!");
			}

		} else {
			this.populateBreedingMethod(programUUID);
		}
	}

	private void populateBreedingMethod(final String programUUID) {

		try {
			if (this.isSelectAllMethods()) {
				this.methods = this.germplasmDataManager.getAllMethodsOrderByMname();
			} else {
				this.methods =
						this.germplasmDataManager.getMethodsByType(CrossingSettingsMethodComponent.GENERATIVE_METHOD_TYPE, programUUID);
			}
		} catch (final MiddlewareQueryException e) {
			CrossingSettingsMethodComponent.LOG.error(e.getMessage());
		}

		this.breedingMethods.removeAllItems();

		for (final Method m : this.methods) {
			final Integer methodId = m.getMid();
			this.breedingMethods.addItem(methodId);
			this.breedingMethods.setItemCaption(methodId, m.getMname());
		}
	}

	public boolean validateInputFields() {
		if ((Boolean) this.selectMethod.getValue() && this.breedingMethods.getValue() == null) {
			MessageNotifier.showRequiredFieldError(this.getWindow(),
					this.getMessageSource().getMessage(Message.PLEASE_CHOOSE_CROSSING_METHOD));
			return false;
		}
		return true;
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public GermplasmDataManager getGermplasmDataManager() {
		return this.germplasmDataManager;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public void setBreedingManagerService(final BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}

	public void registerBreedingMethodChangeListener(final Property.ValueChangeListener changeListener) {
		this.breedingMethods.addListener(changeListener);
	}

	public void setSelectMethod(final CheckBox selectMethodForAllCheckbox) {
		this.selectMethod = selectMethodForAllCheckbox;
	}

	public void setBreedingMethods(final ComboBox breedingMethodSelection) {
		this.breedingMethods = breedingMethodSelection;
	}
	
	public CheckBox getFavoriteMethodsCheckbox() {
		return favoriteMethodsCheckbox;
	}
}
