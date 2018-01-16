package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.MandatoryMarkLabel;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodeCustomLayout;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodesNamingLayout;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.generationcp.middleware.service.api.GermplasmNamingReferenceDataResolver;
import org.generationcp.middleware.service.api.GermplasmNamingService;
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

	public static final String SEQUENCE_PLACEHOLDER = "[SEQ]";
	private static final String CODE_NAME_WITH_SPACE_REGEX = "^CODE \\d$";
	private static final String CODE_NAME_REGEX = "^CODE\\d$";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmNamingService germplasmNamingService;

	@Autowired
	private GermplasmNamingReferenceDataResolver germplasmNamingReferenceDataResolver;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private ContextUtil contextUtil;
	
	@Autowired
	private GermplasmListManager germplasmListManager;

	private AssignCodesNamingLayout assignCodesNamingLayout;
	private AssignCodeCustomLayout assignCodesCustomLayout;
	private HorizontalLayout codeControlsLayoutDefault;

	private MandatoryMarkLabel mandatoryLabel;
	private MandatoryMarkLabel codingLevelMandatoryLabel;
	private Label indicatesMandatoryLabel;
	private Label codingLevelLabel;
	private VerticalLayout codesLayout;
	private OptionGroup codingLevelOptions;
	private Button cancelButton;
	private Button continueButton;
	private Set<Integer> gidsToProcess = new HashSet<>();
	private final boolean isCustomLayout;

	// will be used for unit tests
	AssignCodesDialog(final boolean isCustomLayout) {
		this.isCustomLayout = isCustomLayout;
	}

	public AssignCodesDialog(final Set<Integer> gidsToProcess, final boolean isCustomLayout) {
		this.gidsToProcess = gidsToProcess;
		this.isCustomLayout = isCustomLayout;
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
		
		this.codingLevelLabel = new Label(this.messageSource.getMessage(Message.CODING_LEVEL));
		this.codingLevelLabel.setDebugId("codingLevelLabel");
		this.codingLevelLabel.addStyleName("bold");
		this.codingLevelMandatoryLabel = new MandatoryMarkLabel();
		this.codingLevelMandatoryLabel.setDebugId("codingLevelMandatoryLabel");
		
		this.cancelButton = new Button();
		this.cancelButton.setDebugId("cancelButton");
		
		this.continueButton = new Button();
		this.continueButton.setDebugId("continueButton");
		this.continueButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.continueButton.setEnabled(false);
		
		this.codesLayout = new VerticalLayout();
		this.codesLayout.setDebugId("codesLayout");

		// set immediate to true for those fields we will listen to for the changes on the screen
		this.codingLevelOptions.setImmediate(true);

		this.assignCodesNamingLayout = new AssignCodesNamingLayout(this.codesLayout, this.continueButton);
		this.assignCodesNamingLayout.instantiateComponents();

		if (this.isCustomLayout) {
			this.assignCodesCustomLayout =
					new AssignCodeCustomLayout(this.germplasmNamingReferenceDataResolver, this.contextUtil, this.messageSource,
							this.assignCodesNamingLayout, this.codingLevelOptions, this.codesLayout, null, null);
			this.assignCodesCustomLayout.instantiateComponents();
		}
	}

	@Override
	public void initializeValues() {
		this.populateCodingNameTypes();

		if (this.isCustomLayout) {
			this.assignCodesCustomLayout.initializeValues();
		}
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

	@Override
	public void addListeners() {
		if (this.isCustomLayout) {
			this.assignCodesCustomLayout.addListeners(this.codingLevelOptions);
		}
		this.assignCodesNamingLayout.addListeners();

		this.cancelButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				AssignCodesDialog.super.close();
			}
		});

		this.continueButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				if (AssignCodesDialog.this.isCustomLayout) {
					try {
						AssignCodesDialog.this.assignCodesCustomLayout.validate();
					} catch (final Validator.InvalidValueException ex) {
						MessageNotifier.showError(AssignCodesDialog.this.getWindow(),
								AssignCodesDialog.this.messageSource.getMessage(Message.ASSIGN_CODES), ex.getMessage());
						return;
					}
				} else {
					try {
						AssignCodesDialog.this.assignCodesNamingLayout.validate();
					} catch (final Validator.InvalidValueException ex) {
						MessageNotifier.showError(AssignCodesDialog.this.getWindow(),
								AssignCodesDialog.this.messageSource.getMessage(Message.ASSIGN_CODES), ex.getMessage());
						return;
					}
				}
				AssignCodesDialog.this.assignCodes();
			}
		});
	}

	void assignCodes() {
		/**
		 * This block of code is thread synchronized at the entire class level which means that the lock applies to all instances of
		 * AssignCodesDialog class that are invoking this operation. This is pessimistic locking based on the assumption that assigning code
		 * is not a massively parallel operation. It happens few times a year. It is OK for other users doing the same operation to wait
		 * while one user completes this operation.
		 */
		synchronized (AssignCodesDialog.class) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			final Map<Integer, GermplasmGroupNamingResult> assignCodesResultsMap = new LinkedHashMap<>();
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus status) {
					final UserDefinedField nameType = (UserDefinedField) AssignCodesDialog.this.codingLevelOptions.getValue();
						

					// TODO performance tuning when processing large number of list entries..
					for (final Integer gid : AssignCodesDialog.this.gidsToProcess) {
						// TODO pass user and location. Hardcoded to 0 = unknown for now.
						final GermplasmGroupNamingResult result =
								AssignCodesDialog.this.germplasmNamingService.applyGroupName(gid, AssignCodesDialog.this.assignCodesNamingLayout.generateGermplasmNameSetting(), nameType, 0, 0);
						assignCodesResultsMap.put(gid, result);
					}
				}
			});
			this.getParent().addWindow(new AssignCodesResultsDialog(assignCodesResultsMap));
			this.closeWindow();
		}
	}
	
	boolean isCodingNameType(final String nameType){
		return nameType.toUpperCase().matches(CODE_NAME_REGEX) || nameType.toUpperCase().matches(CODE_NAME_WITH_SPACE_REGEX);
	}

	String getGroupNamePrefix(final boolean isCustomLayout) {

		if (isCustomLayout) {
			return AssignCodesDialog.this.assignCodesCustomLayout.getGroupNamePrefix();
		} else {
			return AssignCodesDialog.this.assignCodesNamingLayout.getGroupNamePrefix();
		}

	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void layoutComponents() {
		this.setModal(true);
		this.setWidth("650px");
		this.setHeight("550px");
		this.setResizable(false);
		this.addStyleName(Reindeer.WINDOW_LIGHT);

		this.center();

		final VerticalLayout dialogLayout = new VerticalLayout();
		dialogLayout.setDebugId("dialogLayout");
		dialogLayout.setHeight("440px");
		dialogLayout.setMargin(true);

		final HorizontalLayout mandatoryLabelLayout = new HorizontalLayout();
		mandatoryLabelLayout.setDebugId("mandatoryLabelLayout");
		mandatoryLabelLayout.setWidth("250px");
		mandatoryLabelLayout.setHeight("45px");
		this.mandatoryLabel.setWidth("10px");
		this.indicatesMandatoryLabel.setWidth("210px");
		mandatoryLabelLayout.addComponent(this.mandatoryLabel);
		mandatoryLabelLayout.addComponent(this.indicatesMandatoryLabel);
		
		// Area with level options
		final HorizontalLayout optionsLabelLayout = new HorizontalLayout();
		optionsLabelLayout.setDebugId("optionsLabelLayout");
		optionsLabelLayout.setWidth("350px");
		this.codingLevelLabel.setWidth("90px");
		this.codingLevelMandatoryLabel.setWidth("160px");
		optionsLabelLayout.addComponent(this.codingLevelLabel);
		optionsLabelLayout.addComponent(this.codingLevelMandatoryLabel);
		final HorizontalLayout optionsLayout = new HorizontalLayout();
		optionsLayout.setDebugId("optionsLayout");
		optionsLayout.setWidth("450px");
		optionsLayout.setHeight("45px");
		this.codingLevelOptions.addStyleName("lst-horizontal-options");
		optionsLayout.addComponent(optionsLabelLayout);
		optionsLayout.addComponent(this.codingLevelOptions);
		optionsLayout.setComponentAlignment(this.codingLevelOptions, Alignment.TOP_RIGHT);

		this.codesLayout.setWidth("100%");
		this.codesLayout.setHeight("270px");
		if (this.isCustomLayout) {
			this.assignCodesCustomLayout.layoutComponents();
		} else {
			this.assignCodesNamingLayout.layoutComponents();
		}
		
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
		dialogLayout.addComponent(optionsLayout);
		dialogLayout.addComponent(this.codesLayout);
		dialogLayout.addComponent(buttonLayout);
		this.setContent(dialogLayout);
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
	}

	void setGidsToProcess(final Set<Integer> gidsToProcess) {
		this.gidsToProcess = gidsToProcess;
	}

	void setAssignCodesDefaultLayout(final AssignCodesNamingLayout assignCodesDefaultLayout) {
		this.assignCodesNamingLayout = assignCodesDefaultLayout;
	}

	void setAssignCodesCustomLayout(final AssignCodeCustomLayout assignCodesCustomLayout) {
		this.assignCodesCustomLayout = assignCodesCustomLayout;
	}
}
