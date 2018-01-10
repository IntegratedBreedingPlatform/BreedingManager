package org.generationcp.breeding.manager.listmanager.dialog.layout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listeners.AssignCodesLevelOptionsCustomListener;
import org.generationcp.breeding.manager.listmanager.dialog.AssignCodesDialog;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.service.api.GermplasmNamingReferenceDataResolver;
import org.generationcp.middleware.service.api.GermplasmType;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class AssignCodeCustomLayout {
	public static final String SEQUENCE_PLACEHOLDER = "[SEQ]";
	public static final String SEQUENCE_LABEL = "SEQ";
	public static final String LST_SEQUENCE_LABEL_CLASS = "lst-sequence-label";

	private ComboBox programIdentifiersComboBox;
	private ComboBox germplasmTypeComboBoxLevel1;
	private ComboBox germplasmTypeComboBoxLevel2;
	private TextField yearSuffixLevel1;
	private TextField yearSuffixLevel2;
	private ComboBox locationIdentifierCombobox;

	private HorizontalLayout codeControlsLayoutLevel1;
	private HorizontalLayout codeControlsLayoutLevel2;
	private AbsoluteLayout codeControlsLayoutLevel3;

	//items we are getting from general layout
	private final GermplasmNamingReferenceDataResolver germplasmNamingReferenceDataResolver;
	private final ContextUtil contextUtil;
	private final SimpleResourceBundleMessageSource messageSource;
	private final AssignCodesNamingLayout assignCodesDefaultLayout;
	private final OptionGroup codingLevelOptions;
	private final VerticalLayout codesLayout;
	private final Label exampleText;
	private final VerticalLayout exampleLayout;

	public AssignCodeCustomLayout(final GermplasmNamingReferenceDataResolver germplasmNamingReferenceDataResolver,
			final ContextUtil contextUtil,
			final SimpleResourceBundleMessageSource messageSource,
			final AssignCodesNamingLayout assignCodesDefaultLayout, final OptionGroup codingLevelOptions,
			final VerticalLayout codesLayout, final Label exampleText, final VerticalLayout exampleLayout) {
		this.germplasmNamingReferenceDataResolver = germplasmNamingReferenceDataResolver;
		this.messageSource = messageSource;
		this.contextUtil = contextUtil;
		this.assignCodesDefaultLayout = assignCodesDefaultLayout;
		this.codingLevelOptions = codingLevelOptions;
		this.codesLayout = codesLayout;
		this.exampleText = exampleText;
		this.exampleLayout = exampleLayout;
	}

	public void instantiateComponents() {
		this.programIdentifiersComboBox = new ComboBox();
		this.programIdentifiersComboBox.setDebugId("programIdentifiersComboBox");
		this.germplasmTypeComboBoxLevel1 = new ComboBox();
		this.germplasmTypeComboBoxLevel1.setDebugId("germplasmTypeComboBoxLevel1");
		this.germplasmTypeComboBoxLevel2 = new ComboBox();
		this.germplasmTypeComboBoxLevel2.setDebugId("germplasmTypeComboBoxLevel2");
		this.yearSuffixLevel1 = new TextField();
		this.yearSuffixLevel1.setDebugId("yearSuffixLevel1");
		this.yearSuffixLevel2 = new TextField();
		this.yearSuffixLevel2.setDebugId("yearSuffixLevel2");
		this.locationIdentifierCombobox = new ComboBox();
		this.locationIdentifierCombobox.setDebugId("locationIdentifierCombobox");

		// set immediate to true for those fields we will listen to for the changes on the screen
		this.programIdentifiersComboBox.setImmediate(true);
		this.germplasmTypeComboBoxLevel1.setImmediate(true);
		this.germplasmTypeComboBoxLevel2.setImmediate(true);
		this.yearSuffixLevel1.setImmediate(true);
		this.yearSuffixLevel2.setImmediate(true);
		this.locationIdentifierCombobox.setImmediate(true);

		this.programIdentifiersComboBox.setCaption(this.messageSource.getMessage(Message.PROGRAM_IDENTIFIER_LABEL));
		this.germplasmTypeComboBoxLevel1.setCaption(this.messageSource.getMessage(Message.GERMPLASM_TYPE_LABEL));
		this.germplasmTypeComboBoxLevel2.setCaption(this.messageSource.getMessage(Message.GERMPLASM_TYPE_LABEL));
		this.yearSuffixLevel1.setCaption(this.messageSource.getMessage(Message.YEAR_LABEL));
		this.yearSuffixLevel2.setCaption(this.messageSource.getMessage(Message.YEAR_LABEL));
		this.locationIdentifierCombobox.setCaption(this.messageSource.getMessage(Message.LOCATION_IDENTIFIER_LABEL));

		// add validators
		this.yearSuffixLevel1.addValidator(new StringLengthValidator(this.messageSource.getMessage(Message.ERROR_YEAR_TOO_LONG), 0, 4,
				false));
		this.yearSuffixLevel2.addValidator(new StringLengthValidator(this.messageSource.getMessage(Message.ERROR_YEAR_TOO_LONG), 0, 4,
				false));
	}

	public void initializeValues() {
		final List<String> programIdentifiers = this.germplasmNamingReferenceDataResolver.getProgramIdentifiers(1, this.contextUtil.getCurrentProgramUUID
				());
		for (final String programIdentifier : programIdentifiers) {
			this.programIdentifiersComboBox.addItem(programIdentifier);
		}
		//the first value in the list is a default selection
		if (!programIdentifiers.isEmpty()) {
			this.programIdentifiersComboBox.setValue(programIdentifiers.get(0));
		}

		final Set<GermplasmType> germplasmTypes = this.germplasmNamingReferenceDataResolver.getGermplasmTypes();
		for (final GermplasmType germplasmType : germplasmTypes) {
			this.germplasmTypeComboBoxLevel1.addItem(germplasmType.name());
			this.germplasmTypeComboBoxLevel2.addItem(germplasmType.name());
		}
		//the first value in the list is a default selection
		if (!germplasmTypes.isEmpty()) {
			final GermplasmType germplasmType = (GermplasmType) germplasmTypes.toArray()[0];
			this.germplasmTypeComboBoxLevel1.setValue(germplasmType.name());
			this.germplasmTypeComboBoxLevel2.setValue(germplasmType.name());
		}

		// by default the current year in 2 digits format will be set to yearSuffix text field
		final Date today = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy");
		this.yearSuffixLevel1.setValue(simpleDateFormat.format(today));
		this.yearSuffixLevel2.setValue(simpleDateFormat.format(today));

		this.programIdentifiersComboBox.setNullSelectionAllowed(false);
		this.locationIdentifierCombobox.setNullSelectionAllowed(false);
		this.germplasmTypeComboBoxLevel1.setNullSelectionAllowed(false);
		this.germplasmTypeComboBoxLevel2.setNullSelectionAllowed(false);

		//update example text after setting defaults
		this.updateExampleValue();

		// setting up the possible values for location identifiers for level2
		final List<String> locationIdentifiers = this.germplasmNamingReferenceDataResolver
				.getProgramIdentifiers(2, this.contextUtil.getCurrentProgramUUID());
		for (final String locationIdentifier : locationIdentifiers) {
			this.locationIdentifierCombobox.addItem(locationIdentifier);
		}
		//the first value in the list is a default selection
		if (!locationIdentifiers.isEmpty()) {
			this.locationIdentifierCombobox.setValue(locationIdentifiers.get(0));
		}
	}

	public void addListeners(final OptionGroup codingLevelOptions) {

		final Property.ValueChangeListener codeOptionsListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(final Property.ValueChangeEvent event) {
				AssignCodeCustomLayout.this.updateExampleValue();
			}
		};
		final AssignCodesLevelOptionsCustomListener assignCodesLevelOptionsCustomListener = new AssignCodesLevelOptionsCustomListener(
				this.codingLevelOptions, this.codesLayout, this, this.exampleLayout);
		codingLevelOptions.addListener(assignCodesLevelOptionsCustomListener);

		this.programIdentifiersComboBox.addListener(codeOptionsListener);
		this.germplasmTypeComboBoxLevel1.addListener(codeOptionsListener);
		this.germplasmTypeComboBoxLevel2.addListener(codeOptionsListener);
		this.assignCodesDefaultLayout.getPrefixDefault().addListener(codeOptionsListener);
		this.yearSuffixLevel1.addListener(codeOptionsListener);
		this.yearSuffixLevel2.addListener(codeOptionsListener);
		this.locationIdentifierCombobox.addListener(codeOptionsListener);
	}

	private String getExampleValue(final int level) {
		// TODO this will be configurable (the order and fields)
		String exampleValue = "";
		switch (level) {
			case 1:
				exampleValue = this.programIdentifiersComboBox.getValue().toString() +
						this.germplasmTypeComboBoxLevel1.getValue().toString() +
						this.yearSuffixLevel1.getValue().toString() + SEQUENCE_PLACEHOLDER;
				break;
			case 2:
				exampleValue = this.locationIdentifierCombobox.getValue().toString() +
						this.germplasmTypeComboBoxLevel2.getValue().toString() +
						this.yearSuffixLevel2.getValue().toString() + SEQUENCE_PLACEHOLDER;
				break;
			case 3:
				exampleValue = this.assignCodesDefaultLayout.getPrefixDefault().getValue().toString() + SEQUENCE_PLACEHOLDER;
		}

		return exampleValue;
	}

	public void updateExampleValue() {
		this.exampleText.setValue(this.getExampleValue(this.getCodingLevel()));
	}

	private int getCodingLevel() {
		int level = 1;
		if (this.codingLevelOptions.getValue().equals(AssignCodesDialog.LEVEL1)) {
			level = 1;
		} else if (this.codingLevelOptions.getValue().equals(AssignCodesDialog.LEVEL2)) {
			level = 2;
		} else if (this.codingLevelOptions.getValue().equals(AssignCodesDialog.LEVEL3)) {
			level = 3;
		}
		return level;
	}

	public String getGroupNamePrefix() {
		String prefix = "";
		//TODO this should depend on configuration
		if (this.getCodingLevel() == 1) {
			prefix = this.programIdentifiersComboBox.getValue().toString() + this.germplasmTypeComboBoxLevel1.getValue().toString() + this
					.yearSuffixLevel1.getValue().toString();
		} else if (this.getCodingLevel() == 2) {
			prefix = this.locationIdentifierCombobox.getValue().toString() + this.germplasmTypeComboBoxLevel2.getValue().toString() + this
					.yearSuffixLevel2.getValue().toString();
		} else if (this.getCodingLevel() == 3) {
			prefix = this.assignCodesDefaultLayout.getPrefixDefault().getValue().toString();
		}
		return prefix;
	}

	public void layoutComponents() {
		//codes controls area
		//Level 1
		this.codeControlsLayoutLevel1 = new HorizontalLayout();
		this.codeControlsLayoutLevel1.setDebugId("codeControlsLayoutLevel1");
		this.codeControlsLayoutLevel1.setWidth("85%");
		this.codeControlsLayoutLevel1.setHeight("60px");
		this.codeControlsLayoutLevel1.setSpacing(false);
		this.codeControlsLayoutLevel1.setMargin(false);
		this.codeControlsLayoutLevel1.addStyleName("lst-margin-left");

		this.programIdentifiersComboBox.setWidth(8, Sizeable.UNITS_EM);
		this.programIdentifiersComboBox.addStyleName("lst-option-control");
		this.codeControlsLayoutLevel1.addComponent(this.programIdentifiersComboBox);
		this.codeControlsLayoutLevel1.setComponentAlignment(this.programIdentifiersComboBox, Alignment.MIDDLE_LEFT);

		this.germplasmTypeComboBoxLevel1.setWidth(8, Sizeable.UNITS_EM);
		this.codeControlsLayoutLevel1.addComponent(this.germplasmTypeComboBoxLevel1);
		this.codeControlsLayoutLevel1.setComponentAlignment(this.germplasmTypeComboBoxLevel1, Alignment.MIDDLE_LEFT);

		this.yearSuffixLevel1.setWidth(8, Sizeable.UNITS_EM);
		this.codeControlsLayoutLevel1.addComponent(this.yearSuffixLevel1);
		this.codeControlsLayoutLevel1.setComponentAlignment(this.yearSuffixLevel1, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel1 = new Label(SEQUENCE_LABEL);
		sequenceLabel1.setDebugId("sequenceLabel1");
		sequenceLabel1.addStyleName(LST_SEQUENCE_LABEL_CLASS);
		this.codeControlsLayoutLevel1.addComponent(sequenceLabel1);
		this.codeControlsLayoutLevel1.setComponentAlignment(sequenceLabel1, Alignment.MIDDLE_LEFT);

		//Level 2
		this.codeControlsLayoutLevel2 = new HorizontalLayout();
		this.codeControlsLayoutLevel2.setDebugId("codeControlsLayoutLevel2");
		this.codeControlsLayoutLevel2.setWidth("85%");
		this.codeControlsLayoutLevel2.setHeight("60px");
		this.codeControlsLayoutLevel2.setSpacing(false);
		this.codeControlsLayoutLevel2.setMargin(false);
		this.codeControlsLayoutLevel2.addStyleName("lst-margin-left");

		this.locationIdentifierCombobox.setWidth(8, Sizeable.UNITS_EM);
		this.locationIdentifierCombobox.addStyleName("lst-option-control");
		this.codeControlsLayoutLevel2.addComponent(this.locationIdentifierCombobox);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.locationIdentifierCombobox, Alignment.MIDDLE_LEFT);

		this.germplasmTypeComboBoxLevel2.setWidth(8, Sizeable.UNITS_EM);
		this.codeControlsLayoutLevel2.addComponent(this.germplasmTypeComboBoxLevel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.germplasmTypeComboBoxLevel2, Alignment.MIDDLE_LEFT);

		this.yearSuffixLevel2.setWidth(8, Sizeable.UNITS_EM);
		this.codeControlsLayoutLevel2.addComponent(this.yearSuffixLevel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.yearSuffixLevel2, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel2 = new Label(SEQUENCE_LABEL);
		sequenceLabel2.setDebugId("sequenceLabel2");
		sequenceLabel2.addStyleName(LST_SEQUENCE_LABEL_CLASS);
		this.codeControlsLayoutLevel2.addComponent(sequenceLabel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(sequenceLabel2, Alignment.MIDDLE_LEFT);

		//Level 3 layout is the same as the default layout
		this.codeControlsLayoutLevel3 = this.assignCodesDefaultLayout.constructDefaultCodeControlsLayout();

		// by default only level 1 panel is visible
		this.codesLayout.addComponent(this.codeControlsLayoutLevel1);
	}

	public HorizontalLayout getCodeControlsLayoutLevel1() {
		return this.codeControlsLayoutLevel1;
	}

	public HorizontalLayout getCodeControlsLayoutLevel2() {
		return this.codeControlsLayoutLevel2;
	}

	public AbsoluteLayout getCodeControlsLayoutLevel3() {
		return this.codeControlsLayoutLevel3;
	}

	public void validate() throws Validator.InvalidValueException {
		this.assignCodesDefaultLayout.validate();
		this.yearSuffixLevel1.validate();
		this.yearSuffixLevel2.validate();
	}
}
