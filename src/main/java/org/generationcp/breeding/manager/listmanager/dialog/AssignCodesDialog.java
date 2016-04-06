
package org.generationcp.breeding.manager.listmanager.dialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.generationcp.middleware.service.api.GermplasmNameTypeResolver;
import org.generationcp.middleware.service.api.GermplasmNamingService;
import org.generationcp.middleware.service.api.GermplasmType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class AssignCodesDialog extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, Window.CloseListener {

	public static final String SEQUENCE_PLACEHOLDER = "[SEQ]";
	public static final String SEQUENCE_LABEL = "SEQ";
	public static final String LST_SEQUENCE_LABEL_CLASS = "lst-sequence-label";
	public static final String LEVEL1 = "Level 1";
	public static final String LEVEL2 = "Level 2";
	public static final String LEVEL3 = "Level 3";
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmNamingService germplasmNamingService;

	@Autowired
	private GermplasmNameTypeResolver germplasmNameTypeResolver;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private OptionGroup codingLevelOptions;
	private Button cancelButton;
	private Button continueButton;
	private Set<Integer> gidsToProcess = new HashSet<>();
	private ComboBox programIdentifiersComboBox;
	private ComboBox germplasmTypeComboBoxLevel1;
	private ComboBox germplasmTypeComboBoxLevel2;
	private ComboBox germplasmTypeComboBoxLevel3;
	private TextField yearSuffixLevel1;
	private TextField yearSuffixLevel2;
	private Label exampleText;
	private HorizontalLayout codeControlsLayoutLevel1;
	private HorizontalLayout codeControlsLayoutLevel2;
	private HorizontalLayout codeControlsLayoutLevel3;
	private HorizontalLayout codesLayout;
	private ComboBox locationIdentifierCombobox;

	// used for unit tests
	AssignCodesDialog() {
	}

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
		this.codingLevelOptions = new OptionGroup();
		this.exampleText = new Label();
		this.programIdentifiersComboBox = new ComboBox();
		this.germplasmTypeComboBoxLevel1 = new ComboBox();
		this.germplasmTypeComboBoxLevel2 = new ComboBox();
		this.germplasmTypeComboBoxLevel3 = new ComboBox();
		this.yearSuffixLevel1 = new TextField();
		this.yearSuffixLevel2 = new TextField();
		this.locationIdentifierCombobox = new ComboBox();
		this.cancelButton = new Button();
		this.continueButton = new Button();
		this.continueButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		// set immediate to true for those fields we will listen to for the changes on the screen
		this.codingLevelOptions.setImmediate(true);
		this.programIdentifiersComboBox.setImmediate(true);
		this.germplasmTypeComboBoxLevel1.setImmediate(true);
		this.germplasmTypeComboBoxLevel2.setImmediate(true);
		this.germplasmTypeComboBoxLevel3.setImmediate(true);
		this.yearSuffixLevel1.setImmediate(true);
		this.yearSuffixLevel2.setImmediate(true);
		this.locationIdentifierCombobox.setImmediate(true);
	}

	@Override
	public void initializeValues() {
		//TODO Remove hardcoding of levels ??
		//TODO Localise these values
		this.codingLevelOptions.addItem(LEVEL1);
		this.codingLevelOptions.addItem(LEVEL2);
		this.codingLevelOptions.addItem(LEVEL3);

		// by default the level 1 is selected
		this.codingLevelOptions.select(LEVEL1);
		final List<String> programIdentifiers = this.germplasmNamingService.getProgramIdentifiers(1);
		for (final String programIdentifier : programIdentifiers) {
			this.programIdentifiersComboBox.addItem(programIdentifier);
		}
		//the first value in the list is a default selection
		if (!programIdentifiers.isEmpty()) {
			this.programIdentifiersComboBox.setValue(programIdentifiers.get(0));
		}

		final Set<GermplasmType> germplasmTypes = this.germplasmNamingService.getGermplasmTypes();
		for (final GermplasmType germplasmType : germplasmTypes) {
			this.germplasmTypeComboBoxLevel1.addItem(germplasmType.name());
			this.germplasmTypeComboBoxLevel2.addItem(germplasmType.name());
			this.germplasmTypeComboBoxLevel3.addItem(germplasmType.name());
		}
		//the first value in the list is a default selection
		if (!germplasmTypes.isEmpty()) {
			final GermplasmType germplasmType = (GermplasmType) germplasmTypes.toArray()[0];
			this.germplasmTypeComboBoxLevel1.setValue(germplasmType.name());
			this.germplasmTypeComboBoxLevel2.setValue(germplasmType.name());
			this.germplasmTypeComboBoxLevel3.setValue(germplasmType.name());
		}

		// by default the current year in 2 digits format will be set to yearSuffix text field
		final Date today = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy");
		this.yearSuffixLevel1.setValue(simpleDateFormat.format(today));
		this.yearSuffixLevel2.setValue(simpleDateFormat.format(today));

		this.programIdentifiersComboBox.setNullSelectionAllowed(false);
		this.germplasmTypeComboBoxLevel1.setNullSelectionAllowed(false);
		this.germplasmTypeComboBoxLevel2.setNullSelectionAllowed(false);
		this.germplasmTypeComboBoxLevel3.setNullSelectionAllowed(false);

		//update example text after setting defaults
		this.exampleText.setValue(this.programIdentifiersComboBox.getValue().toString() +
				this.germplasmTypeComboBoxLevel1.getValue().toString() + this.yearSuffixLevel1.getValue().toString() + SEQUENCE_PLACEHOLDER);

		// setting up the possible values for location identifiers for level2
		final List<String> locationIdentifiers = this.germplasmNamingService.getLocationIdentifiers();
		for (final String locationIdentifier : locationIdentifiers) {
			this.locationIdentifierCombobox.addItem(locationIdentifier);
		}
		//the first value in the list is a default selection
		if (!locationIdentifiers.isEmpty()) {
			this.locationIdentifierCombobox.setValue(locationIdentifiers.get(0));
		}
	}

	@Override
	public void addListeners() {

		final Property.ValueChangeListener codeOptionsListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(final Property.ValueChangeEvent event) {
				AssignCodesDialog.this.updateExampleValue();
			}
		};

		final Property.ValueChangeListener codingLevelsListener = new Property.ValueChangeListener() {

			@Override
			public void valueChange(final Property.ValueChangeEvent event) {
				//toggle codes controls panel
				if (AssignCodesDialog.this.codingLevelOptions.getValue().equals(LEVEL1)) {
					AssignCodesDialog.this.codeControlsLayoutLevel1.setVisible(true);
					AssignCodesDialog.this.codeControlsLayoutLevel2.setVisible(false);
					AssignCodesDialog.this.codeControlsLayoutLevel3.setVisible(false);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel2, 0);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel3, 0);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel1, 2);
				} else if (AssignCodesDialog.this.codingLevelOptions.getValue().equals(LEVEL2)) {
					AssignCodesDialog.this.codeControlsLayoutLevel1.setVisible(false);
					AssignCodesDialog.this.codeControlsLayoutLevel2.setVisible(true);
					AssignCodesDialog.this.codeControlsLayoutLevel3.setVisible(false);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel2, 2);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel1, 0);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel3, 0);
				} else if (AssignCodesDialog.this.codingLevelOptions.getValue().equals(LEVEL3)) {
					AssignCodesDialog.this.codeControlsLayoutLevel1.setVisible(false);
					AssignCodesDialog.this.codeControlsLayoutLevel2.setVisible(false);
					AssignCodesDialog.this.codeControlsLayoutLevel3.setVisible(true);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel2, 0);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel1, 0);
					AssignCodesDialog.this.codesLayout.setExpandRatio(AssignCodesDialog.this.codeControlsLayoutLevel3, 2);
				}
				AssignCodesDialog.this.updateExampleValue();
			}
		};

		this.programIdentifiersComboBox.addListener(codeOptionsListener);
		this.germplasmTypeComboBoxLevel1.addListener(codeOptionsListener);
		this.germplasmTypeComboBoxLevel2.addListener(codeOptionsListener);
		this.germplasmTypeComboBoxLevel3.addListener(codeOptionsListener);
		this.yearSuffixLevel1.addListener(codeOptionsListener);
		this.yearSuffixLevel2.addListener(codeOptionsListener);
		this.locationIdentifierCombobox.addListener(codeOptionsListener);
		this.codingLevelOptions.addListener(codingLevelsListener);


		this.cancelButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(final Button.ClickEvent event) {
				AssignCodesDialog.super.close();
			}
		});

		this.continueButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				AssignCodesDialog.this.assignCodes();
			}
		});
	}

	void assignCodes() {
		/**
		 * This block of code is thread synchronized at the entire class level wich means that the lock applies to all instances of
		 * AssignCodesDialog class that are invoking this operation. This is pessimistic locking based on the assumption that assigning code
		 * is not a massively parallel operation. It happens few times a year. It is OK for other users doing the same operation to wait
		 * while one user completes this operation.
		 */
		synchronized (AssignCodesDialog.class) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			final Map<Integer, GermplasmGroupNamingResult> assignCodesResultsMap = new HashMap<>();
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus status) {
					final UserDefinedField nameType =
							AssignCodesDialog.this.germplasmNameTypeResolver.resolve(AssignCodesDialog.this.getLevel());
					for (final Integer gid : AssignCodesDialog.this.gidsToProcess) {
						// TODO pass user and location. Hardcoded to 0 = unknown for now.
						final GermplasmGroupNamingResult result = AssignCodesDialog.this.germplasmNamingService.applyGroupName(gid,
								AssignCodesDialog.this.getGroupName(), nameType, 0, 0);
						assignCodesResultsMap.put(gid, result);
					}
				}
			});
			this.getParent().addWindow(new AssignCodesResultsDialog(assignCodesResultsMap));
			this.closeWindow();
		}
	}

	private int getLevel() {
		int level = 1;
		if (this.codingLevelOptions.getValue().equals(LEVEL1)) {
			level = 1;
		} else if (this.codingLevelOptions.getValue().equals(LEVEL2)) {
			level = 2;
		} else if (this.codingLevelOptions.getValue().equals(LEVEL3)) {
			level = 3;
		}
		return level;
	}

	private String getGroupName() {
		String prefix = "";
		//TODO this should depend on configuration
		if (this.codingLevelOptions.getValue().equals(LEVEL1)) {
			prefix = this.programIdentifiersComboBox.getValue().toString() + this.germplasmTypeComboBoxLevel1.getValue().toString() + this
					.yearSuffixLevel1.getValue().toString();
		} else if (this.codingLevelOptions.getValue().equals(LEVEL2)) {
			prefix = this.locationIdentifierCombobox.getValue().toString() + this.germplasmTypeComboBoxLevel2.getValue().toString() + this
					.yearSuffixLevel2.getValue().toString();
		} else if (this.codingLevelOptions.getValue().equals(LEVEL3)) {
			prefix = this.determineLevel3Prefix();
		}
		return prefix;
	}

	private String getExampleValue() {
		// TODO this will be configurable (the order and fields)
		String exampleValue = "";
		if (AssignCodesDialog.this.codingLevelOptions.getValue().equals(LEVEL1)) {
			exampleValue = AssignCodesDialog.this.programIdentifiersComboBox.getValue().toString() +
					AssignCodesDialog.this.germplasmTypeComboBoxLevel1.getValue().toString() +
					AssignCodesDialog.this.yearSuffixLevel1.getValue().toString() + SEQUENCE_PLACEHOLDER;
		} else if (AssignCodesDialog.this.codingLevelOptions.getValue().equals(LEVEL2)) {
			exampleValue = AssignCodesDialog.this.locationIdentifierCombobox.getValue().toString() +
					AssignCodesDialog.this.germplasmTypeComboBoxLevel2.getValue().toString() +
					AssignCodesDialog.this.yearSuffixLevel2.getValue().toString() + SEQUENCE_PLACEHOLDER;
		} else if (AssignCodesDialog.this.codingLevelOptions.getValue().equals(LEVEL3)) {
			exampleValue = this.determineLevel3Prefix() + SEQUENCE_PLACEHOLDER;
		}
		return exampleValue;
	}

	private void updateExampleValue() {
		this.exampleText.setValue(this.getExampleValue());
	}

	private String determineLevel3Prefix() {
		String prefix = "";
		if (this.germplasmTypeComboBoxLevel3.getValue().equals(GermplasmType.H.toString())) {
			prefix = "TBD";
		} else if (this.germplasmTypeComboBoxLevel3.getValue().equals(GermplasmType.L.toString())) {
			prefix = "CML";
		} else if (this.germplasmTypeComboBoxLevel3.getValue().equals(GermplasmType.P.toString())) {
			prefix = "ZM";
		}
		return prefix;
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void layoutComponents() {
		this.setModal(true);
		this.setWidth("550px");
		this.setHeight("380px");
		this.setResizable(false);
		this.addStyleName(Reindeer.WINDOW_LIGHT);

		this.center();
		final VerticalLayout dialogLayout = new VerticalLayout();
		dialogLayout.setMargin(true);
		dialogLayout.setSpacing(true);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("40px");
		buttonLayout.setSpacing(true);

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.continueButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.continueButton, Alignment.BOTTOM_LEFT);

		// area with level options
		final HorizontalLayout optionsLayout = new HorizontalLayout();
		optionsLayout.setWidth("100%");
		optionsLayout.setHeight("60px");
		optionsLayout.setSpacing(true);

		this.codingLevelOptions.addStyleName("lst-horizontal-options");
		optionsLayout.addComponent(this.codingLevelOptions);
		optionsLayout.setComponentAlignment(this.codingLevelOptions, Alignment.MIDDLE_LEFT);

		//example area
		final VerticalLayout exampleLayout = new VerticalLayout();
		exampleLayout.setWidth("100%");
		exampleLayout.setHeight("60px");
		exampleLayout.setSpacing(true);
		final Label exampleLabel = new Label(this.messageSource.getMessage(Message.ASSIGN_CODES_EXAMPLE));
		exampleLabel.setStyleName("lst-margin-left");
		exampleLayout.addComponent(exampleLabel);

		this.exampleText.setStyleName("lst-example-text lst-margin-left");
		exampleLayout.addComponent(this.exampleText);
		exampleLayout.setComponentAlignment(exampleLabel, Alignment.TOP_LEFT);
		exampleLayout.setComponentAlignment(this.exampleText, Alignment.MIDDLE_LEFT);

		//codes controls area
		//Level 1
		this.codeControlsLayoutLevel1 = new HorizontalLayout();
		this.codeControlsLayoutLevel1.setWidth("100%");
		this.codeControlsLayoutLevel1.setHeight("60px");

		this.programIdentifiersComboBox.setWidth(5, 3);
		this.programIdentifiersComboBox.setStyleName("lst-option-control");
		this.codeControlsLayoutLevel1.addComponent(this.programIdentifiersComboBox);
		this.codeControlsLayoutLevel1.setComponentAlignment(this.programIdentifiersComboBox, Alignment.MIDDLE_LEFT);

		this.germplasmTypeComboBoxLevel1.setWidth(5, 3);
		this.codeControlsLayoutLevel1.addComponent(this.germplasmTypeComboBoxLevel1);
		this.codeControlsLayoutLevel1.setComponentAlignment(this.germplasmTypeComboBoxLevel1, Alignment.MIDDLE_LEFT);

		this.yearSuffixLevel1.setWidth(5, 3);
		this.codeControlsLayoutLevel1.addComponent(this.yearSuffixLevel1);
		this.codeControlsLayoutLevel1.setComponentAlignment(this.yearSuffixLevel1, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel1 = new Label(SEQUENCE_LABEL);
		sequenceLabel1.setStyleName(LST_SEQUENCE_LABEL_CLASS);
		this.codeControlsLayoutLevel1.addComponent(sequenceLabel1);
		this.codeControlsLayoutLevel1.setComponentAlignment(sequenceLabel1, Alignment.MIDDLE_LEFT);

		//Level 2
		this.codeControlsLayoutLevel2 = new HorizontalLayout();
		this.codeControlsLayoutLevel2.setWidth("100%");
		this.codeControlsLayoutLevel2.setHeight("60px");

		this.locationIdentifierCombobox.setWidth(5, 3);
		this.locationIdentifierCombobox.setStyleName("lst-option-control");
		this.codeControlsLayoutLevel2.addComponent(this.locationIdentifierCombobox);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.locationIdentifierCombobox, Alignment.MIDDLE_LEFT);

		this.germplasmTypeComboBoxLevel2.setWidth(5, 3);
		this.codeControlsLayoutLevel2.addComponent(this.germplasmTypeComboBoxLevel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.germplasmTypeComboBoxLevel2, Alignment.MIDDLE_LEFT);

		this.yearSuffixLevel2.setWidth(5, 3);
		this.codeControlsLayoutLevel2.addComponent(this.yearSuffixLevel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.yearSuffixLevel2, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel2 = new Label(SEQUENCE_LABEL);
		sequenceLabel2.setStyleName(LST_SEQUENCE_LABEL_CLASS);
		this.codeControlsLayoutLevel2.addComponent(sequenceLabel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(sequenceLabel2, Alignment.MIDDLE_LEFT);

		// by default only level 1 panel is visible
		this.codeControlsLayoutLevel2.setVisible(false);

		//Level 3
		this.codeControlsLayoutLevel3 = new HorizontalLayout();
		this.codeControlsLayoutLevel3.setWidth("100%");
		this.codeControlsLayoutLevel3.setHeight("60px");

		this.germplasmTypeComboBoxLevel3.setWidth(5, 3);
		this.codeControlsLayoutLevel3.addComponent(this.germplasmTypeComboBoxLevel3);
		this.codeControlsLayoutLevel3.setComponentAlignment(this.germplasmTypeComboBoxLevel3, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel3 = new Label(SEQUENCE_LABEL);
		sequenceLabel3.setStyleName(LST_SEQUENCE_LABEL_CLASS);
		this.codeControlsLayoutLevel3.addComponent(sequenceLabel3);
		this.codeControlsLayoutLevel3.setComponentAlignment(sequenceLabel3, Alignment.MIDDLE_LEFT);

		// by default only level 1 panel is visible
		this.codeControlsLayoutLevel3.setVisible(false);

		// bordered area
		this.codesLayout = new HorizontalLayout();
		this.codesLayout.setWidth("97%");
		this.codesLayout.setHeight("160px");
		this.codesLayout.setSpacing(true);
		this.codesLayout.setStyleName("lst-border");

		this.codesLayout.addComponent(exampleLayout);
		this.codesLayout.addComponent(this.codeControlsLayoutLevel1);
		this.codesLayout.addComponent(this.codeControlsLayoutLevel2);
		this.codesLayout.addComponent(this.codeControlsLayoutLevel3);
		this.codesLayout.setComponentAlignment(exampleLayout, Alignment.MIDDLE_LEFT);
		this.codesLayout.setComponentAlignment(this.codeControlsLayoutLevel1, Alignment.MIDDLE_LEFT);
		this.codesLayout.setComponentAlignment(this.codeControlsLayoutLevel2, Alignment.MIDDLE_LEFT);
		this.codesLayout.setComponentAlignment(this.codeControlsLayoutLevel3, Alignment.MIDDLE_LEFT);
		this.codesLayout.setExpandRatio(exampleLayout, 1);
		this.codesLayout.setExpandRatio(this.codeControlsLayoutLevel1, 2);
		this.codesLayout.setExpandRatio(this.codeControlsLayoutLevel2, 0);
		this.codesLayout.setExpandRatio(this.codeControlsLayoutLevel3, 0);

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
		this.messageSource.setCaption(this.codingLevelOptions, Message.CODING_LEVEL);
		this.messageSource.setCaption(this.continueButton, Message.APPLY_CODES);
		this.messageSource.setCaption(this.cancelButton, Message.CANCEL);
	}
}
