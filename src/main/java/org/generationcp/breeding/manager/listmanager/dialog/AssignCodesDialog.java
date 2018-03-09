package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.Property;
import com.vaadin.ui.GridLayout;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.MandatoryMarkLabel;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodesNamingLayout;
import org.generationcp.commons.service.GermplasmCodeGenerationService;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.NamingConfiguration;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.data.Validator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class AssignCodesDialog extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, Window.CloseListener {

	private static final Logger LOG = LoggerFactory.getLogger(AssignCodesDialog.class);

	private static final String CODE_NAME_WITH_SPACE_REGEX = "^CODE \\d$";
	private static final String CODE_NAME_REGEX = "^CODE\\d$";
	public static final String DEFAULT_DIALOG_WIDTH = "650px";
	public static final String DEFAULT_DIALOG_HEIGHT = "350px";
	public static final String DEFAULT_DIALOG_HEIGHT_FOR_MANUAL_NAMING = "600px";

	public static enum NAMING_OPTION {
		AUTOMATIC, MANUAL;
	}

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmCodeGenerationService germplasmCodeGenerationService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	private VerticalLayout manualCodeNamingLayout;
	private AssignCodesNamingLayout assignCodesNamingLayout;

	private MandatoryMarkLabel mandatoryLabel;

	private Label indicatesMandatoryLabel;

	private OptionGroup codingLevelOptions;
	private OptionGroup namingOptions;

	private Button cancelButton;
	private Button continueButton;
	private Set<Integer> gidsToProcess = new HashSet<>();

	public AssignCodesDialog(final Set<Integer> gidsToProcess) {
		this.gidsToProcess = gidsToProcess;
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
		this.mandatoryLabel = new MandatoryMarkLabel();
		this.mandatoryLabel.setDebugId("mandatoryLabel");
		this.indicatesMandatoryLabel = new Label(this.messageSource.getMessage(Message.INDICATES_A_MANDATORY_FIELD));
		this.indicatesMandatoryLabel.setDebugId("indicatesMandatoryLabel");
		this.indicatesMandatoryLabel.addStyleName("italic");

		this.codingLevelOptions = new OptionGroup();
		this.codingLevelOptions.setDebugId("codingLevelOptions");
		this.codingLevelOptions.addStyleName("lst-horizontal-options");
		this.namingOptions = new OptionGroup();
		this.namingOptions.setDebugId("namingOptions");
		this.namingOptions.addStyleName("lst-horizontal-options");

		this.instantiateButtons();

		// set immediate to true for those fields we will listen to for the changes on the screen
		this.codingLevelOptions.setImmediate(true);
		this.namingOptions.setImmediate(true);

		this.manualCodeNamingLayout = this.createManualCodeNamingLayout();

		this.assignCodesNamingLayout = new AssignCodesNamingLayout(manualCodeNamingLayout, this.continueButton);
		this.assignCodesNamingLayout.instantiateComponents();
		this.assignCodesNamingLayout.layoutComponents();

	}

	void instantiateButtons() {
		this.cancelButton = new Button();
		this.cancelButton.setDebugId("cancelButton");

		this.continueButton = new Button();
		this.continueButton.setDebugId("continueButton");
		this.continueButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {

		this.populateCodingNameTypes();
		this.namingOptions.addItem(NAMING_OPTION.AUTOMATIC);
		this.namingOptions.addItem(NAMING_OPTION.MANUAL);
		this.namingOptions.setValue(NAMING_OPTION.AUTOMATIC);


	}

	void populateCodingNameTypes() {
		final List<UserDefinedField> userDefinedFieldList = this.germplasmListManager.getGermplasmNameTypes();
		UserDefinedField firstId = null;
		for (final UserDefinedField userDefinedField : userDefinedFieldList) {
			if (this.isCodingNameType(userDefinedField.getFname())) {
				if (firstId == null) {
					firstId = userDefinedField;
				}
				this.codingLevelOptions.addItem(userDefinedField);
				this.codingLevelOptions.setItemCaption(userDefinedField, userDefinedField.getFcode());
			}
		}
		if (firstId != null) {
			this.codingLevelOptions.setValue(firstId);
		}
	}

	protected void toggleNamingLayout(final NAMING_OPTION namingOption) {
		if (namingOption == NAMING_OPTION.MANUAL) {
			AssignCodesDialog.this.manualCodeNamingLayout.setVisible(true);
			AssignCodesDialog.this.continueButton.setEnabled(false);
			AssignCodesDialog.this.setHeight(AssignCodesDialog.DEFAULT_DIALOG_HEIGHT_FOR_MANUAL_NAMING);
		} else {
			AssignCodesDialog.this.manualCodeNamingLayout.setVisible(false);
			AssignCodesDialog.this.continueButton.setEnabled(true);
			AssignCodesDialog.this.setHeight(AssignCodesDialog.DEFAULT_DIALOG_HEIGHT);
		}
	}

	@Override
	public void addListeners() {
		this.assignCodesNamingLayout.addListeners();

		this.namingOptions.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(final Property.ValueChangeEvent valueChangeEvent) {
				AssignCodesDialog.this.toggleNamingLayout((NAMING_OPTION) valueChangeEvent.getProperty().getValue());
			}
		});

		this.cancelButton.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				AssignCodesDialog.super.close();
			}
		});

		this.continueButton.addListener(new ContinueButtonClickListener());
	}

	protected void generateCodeNames() {

		final UserDefinedField nameType = (UserDefinedField) AssignCodesDialog.this.codingLevelOptions.getValue();

		Map<Integer, GermplasmGroupNamingResult> resultsMap = null;

		// TODO pass user and location. Hardcoded to 0 = unknown for now.
		if (AssignCodesDialog.this.namingOptions.getValue() == NAMING_OPTION.MANUAL) {
			resultsMap = AssignCodesDialog.this.germplasmCodeGenerationService.applyGroupNames(AssignCodesDialog.this.gidsToProcess,
					AssignCodesDialog.this.assignCodesNamingLayout.generateGermplasmNameSetting(), nameType, 0, 0);
		} else {
			try {
				final NamingConfiguration namingConfiguration = workbenchDataManager.getNamingConfigurationByName(nameType.getFname());
				resultsMap = germplasmCodeGenerationService.applyGroupNames(AssignCodesDialog.this.gidsToProcess, namingConfiguration, nameType);
			} catch (RuleException e) {
				LOG.error(e.getMessage(), e);
				MessageNotifier.showError(AssignCodesDialog.this.getParent(),
						AssignCodesDialog.this.messageSource.getMessage(Message.ASSIGN_CODES), e.getMessage());
			}
		}

		AssignCodesDialog.this.getParent().addWindow(new AssignCodesResultsDialog(resultsMap));
		AssignCodesDialog.this.closeWindow();

	}

	boolean isCodingNameType(final String nameType){
		return nameType.toUpperCase().matches(CODE_NAME_REGEX) || nameType.toUpperCase().matches(CODE_NAME_WITH_SPACE_REGEX);
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void layoutComponents() {
		this.setImmediate(true);
		this.setModal(true);
		this.setWidth(DEFAULT_DIALOG_WIDTH);
		this.setHeight(DEFAULT_DIALOG_HEIGHT);
		this.setResizable(false);
		this.addStyleName(Reindeer.WINDOW_LIGHT);

		this.center();

		final VerticalLayout dialogLayout = new VerticalLayout();
		dialogLayout.setDebugId("dialogLayout");
		dialogLayout.setHeight("100%");
		dialogLayout.setMargin(true);

		final HorizontalLayout mandatoryLabelLayout = new HorizontalLayout();
		mandatoryLabelLayout.setDebugId("mandatoryLabelLayout");
		mandatoryLabelLayout.setWidth("200px");
		mandatoryLabelLayout.setHeight("40px");
		this.mandatoryLabel.setWidth("10px");
		this.indicatesMandatoryLabel.setWidth("180px");
		mandatoryLabelLayout.addComponent(this.mandatoryLabel);
		mandatoryLabelLayout.addComponent(this.indicatesMandatoryLabel);

		final GridLayout namingAndCodeLevelsGridLayout = this.createNamingAndCodeLevelGridLayout(this.codingLevelOptions, this.namingOptions);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("60px");
		buttonLayout.setSpacing(true);

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.continueButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.continueButton, Alignment.BOTTOM_LEFT);

		dialogLayout.addComponent(mandatoryLabelLayout);
		dialogLayout.addComponent(namingAndCodeLevelsGridLayout);
		dialogLayout.addComponent(this.manualCodeNamingLayout);
		dialogLayout.addComponent(buttonLayout);

		dialogLayout.setComponentAlignment(mandatoryLabelLayout, Alignment.TOP_LEFT);
		dialogLayout.setComponentAlignment(namingAndCodeLevelsGridLayout, Alignment.TOP_LEFT);

		this.setContent(dialogLayout);
	}

	protected GridLayout createNamingAndCodeLevelGridLayout(final OptionGroup codingLevelOptions, final OptionGroup namingOptions) {

		final GridLayout namingAndCodeLevelOptionLayout = new GridLayout(2, 2);

		namingAndCodeLevelOptionLayout.addComponent(this.createCodingLevelOptionsLabelLayout(),0,0 );
		namingAndCodeLevelOptionLayout.addComponent(codingLevelOptions,1,0 );
		namingAndCodeLevelOptionLayout.addComponent(this.createNamingOptionsLabelLayout(),0,1 );
		namingAndCodeLevelOptionLayout.addComponent(namingOptions,1,1 );

		namingAndCodeLevelOptionLayout.setColumnExpandRatio(0, 0.3f);
		namingAndCodeLevelOptionLayout.setColumnExpandRatio(1, 0.7f);
		namingAndCodeLevelOptionLayout.setWidth("100%");

		return namingAndCodeLevelOptionLayout;
	}

	protected HorizontalLayout createCodingLevelOptionsLabelLayout() {

		final Label codingLevelLabel = new Label(this.messageSource.getMessage(Message.CODING_LEVEL));
		codingLevelLabel.setDebugId("codingLevelLabel");
		codingLevelLabel.addStyleName("bold");
		codingLevelLabel.setWidth("90px");
		codingLevelLabel.setHeight("45px");
		final MandatoryMarkLabel codingLevelMandatoryLabel = new MandatoryMarkLabel();
		codingLevelMandatoryLabel.setDebugId("codingLevelMandatoryLabel");

		final HorizontalLayout optionsLabelLayout = new HorizontalLayout();
		optionsLabelLayout.setDebugId("optionsLabelLayout");
		optionsLabelLayout.addComponent(codingLevelLabel);
		optionsLabelLayout.addComponent(codingLevelMandatoryLabel);

		return optionsLabelLayout;
	}

	protected HorizontalLayout createNamingOptionsLabelLayout() {

		final Label namingLabel = new Label(this.messageSource.getMessage(Message.NAMING));
		namingLabel.setDebugId("namingLabel");
		namingLabel.addStyleName("bold");
		namingLabel.setWidth("70px");
		namingLabel.setHeight("45px");
		final MandatoryMarkLabel namingMandatoryLabel = new MandatoryMarkLabel();
		namingMandatoryLabel.setDebugId("namingMandatoryLabel");

		final HorizontalLayout namingOptionsLabelLayout = new HorizontalLayout();
		namingOptionsLabelLayout.setDebugId("namingOptionsLabelLayout");
		namingOptionsLabelLayout.addComponent(namingLabel);
		namingOptionsLabelLayout.addComponent(namingMandatoryLabel);

		return namingOptionsLabelLayout;

	}

	protected VerticalLayout createManualCodeNamingLayout() {

		final VerticalLayout layout = new VerticalLayout();
		layout.setDebugId("codesLayout");
		layout.setWidth("100%");
		layout.setHeight("270px");
		layout.setImmediate(true);
		layout.setVisible(false);

		return layout;

	}

	@Override
	public void windowClose(final CloseEvent e) {
		super.close();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this, Message.ASSIGN_CODES_HEADER);
		this.messageSource.setCaption(this.continueButton, Message.APPLY_CODES);
		this.messageSource.setCaption(this.cancelButton, Message.CANCEL);
		this.namingOptions.setItemCaption(NAMING_OPTION.AUTOMATIC, this.messageSource.getMessage(Message.CODE_NAMING_OPTION_AUTOMATIC));
		this.namingOptions.setItemCaption(NAMING_OPTION.MANUAL, this.messageSource.getMessage(Message.CODE_NAMING_OPTION_MANUAL));
	}

	void setGidsToProcess(final Set<Integer> gidsToProcess) {
		this.gidsToProcess = gidsToProcess;
	}

	void setAssignCodesNamingLayout(final AssignCodesNamingLayout assignCodesNamingLayout) {
		this.assignCodesNamingLayout = assignCodesNamingLayout;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	
	public void setCodingLevelOptions(OptionGroup codingLevelOptions) {
		this.codingLevelOptions = codingLevelOptions;
	}
	
	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setManualCodeNamingLayout(final VerticalLayout manualCodeNamingLayout) {
		this.manualCodeNamingLayout = manualCodeNamingLayout;
	}

	public void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setNamingOptions(final OptionGroup namingOptions) {
		this.namingOptions = namingOptions;
	}

	protected Button getContinueButton() {
		return this.continueButton;
	}


	protected Button getCancelButton() {
		return cancelButton;
	}

	protected OptionGroup getNamingOptions() {
		return namingOptions;
	}

	protected OptionGroup getCodingLevelOptions() {
		return codingLevelOptions;
	}


	protected class ContinueButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {

			try {
				AssignCodesDialog.this.assignCodesNamingLayout.validate();
			} catch (final Validator.InvalidValueException ex) {
				LOG.error(ex.getMessage(), ex);
				MessageNotifier.showError(AssignCodesDialog.this.getWindow(),
						AssignCodesDialog.this.messageSource.getMessage(Message.ASSIGN_CODES), ex.getMessage());
				return;
			}

			/**
			 * This block of code is thread synchronized at the entire class level which means that the lock applies to all instances of
			 * AssignCodesDialog class that are invoking this operation. This is pessimistic locking based on the assumption that assigning code
			 * is not a massively parallel operation. It happens few times a year. It is OK for other users doing the same operation to wait
			 * while one user completes this operation.
			 */
			synchronized (AssignCodesDialog.class) {
				final TransactionTemplate transactionTemplate = new TransactionTemplate(AssignCodesDialog.this.transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(final TransactionStatus status) {

						AssignCodesDialog.this.generateCodeNames();

					}
				});
			}

		}
	}

	public void setGermplasmCodeGenerationService(final GermplasmCodeGenerationService germplasmCodeGenerationService) {
		this.germplasmCodeGenerationService = germplasmCodeGenerationService;
	}
}
