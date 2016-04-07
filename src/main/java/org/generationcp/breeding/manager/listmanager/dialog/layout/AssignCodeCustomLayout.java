package org.generationcp.breeding.manager.listmanager.dialog.layout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.generationcp.breeding.manager.listeners.AssignCodesLevelOptionsCustomListener;
import org.generationcp.breeding.manager.listmanager.dialog.AssignCodesDialog;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.service.api.GermplasmNamingReferenceDataResolver;
import org.generationcp.middleware.service.api.GermplasmType;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;

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
	private HorizontalLayout codeControlsLayoutLevel3;

	//items we are getting from general layout
	private final GermplasmNamingReferenceDataResolver germplasmNamingReferenceDataResolver;
	private final ContextUtil contextUtil;
	private final AssignCodesDefaultLayout assignCodesDefaultLayout;
	private final OptionGroup codingLevelOptions;
	private final HorizontalLayout codesLayout;
	private final Label exampleText;

	public AssignCodeCustomLayout(final GermplasmNamingReferenceDataResolver germplasmNamingReferenceDataResolver,
			final ContextUtil contextUtil,
			final AssignCodesDefaultLayout assignCodesDefaultLayout, final OptionGroup codingLevelOptions,
			final HorizontalLayout codesLayout, final Label exampleText) {
		this.germplasmNamingReferenceDataResolver = germplasmNamingReferenceDataResolver;
		this.contextUtil = contextUtil;
		this.assignCodesDefaultLayout = assignCodesDefaultLayout;
		this.codingLevelOptions = codingLevelOptions;
		this.codesLayout = codesLayout;
		this.exampleText = exampleText;
	}

	public void instantiateComponents() {
		this.programIdentifiersComboBox = new ComboBox();
		this.germplasmTypeComboBoxLevel1 = new ComboBox();
		this.germplasmTypeComboBoxLevel2 = new ComboBox();
		this.yearSuffixLevel1 = new TextField();
		this.yearSuffixLevel2 = new TextField();
		this.locationIdentifierCombobox = new ComboBox();

		// set immediate to true for those fields we will listen to for the changes on the screen
		this.programIdentifiersComboBox.setImmediate(true);
		this.germplasmTypeComboBoxLevel1.setImmediate(true);
		this.germplasmTypeComboBoxLevel2.setImmediate(true);
		this.yearSuffixLevel1.setImmediate(true);
		this.yearSuffixLevel2.setImmediate(true);
		this.locationIdentifierCombobox.setImmediate(true);
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
		this.germplasmTypeComboBoxLevel1.setNullSelectionAllowed(false);
		this.germplasmTypeComboBoxLevel2.setNullSelectionAllowed(false);

		//update example text after setting defaults
		this.exampleText.setValue(this.programIdentifiersComboBox.getValue().toString() +
				this.germplasmTypeComboBoxLevel1.getValue().toString() + this.yearSuffixLevel1.getValue().toString() +
				AssignCodesDialog.SEQUENCE_PLACEHOLDER);

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
		final AssignCodesLevelOptionsCustomListener assignCodesLevelOptionsCustomListener = new AssignCodesLevelOptionsCustomListener(this
					.codingLevelOptions, this.codesLayout, this);
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
		this.codeControlsLayoutLevel1.setWidth("100%");
		this.codeControlsLayoutLevel1.setHeight("60px");

		this.programIdentifiersComboBox.setWidth(5, Sizeable.UNITS_EM);
		this.programIdentifiersComboBox.setStyleName("lst-option-control");
		this.codeControlsLayoutLevel1.addComponent(this.programIdentifiersComboBox);
		this.codeControlsLayoutLevel1.setComponentAlignment(this.programIdentifiersComboBox, Alignment.MIDDLE_LEFT);

		this.germplasmTypeComboBoxLevel1.setWidth(5, Sizeable.UNITS_EM);
		this.codeControlsLayoutLevel1.addComponent(this.germplasmTypeComboBoxLevel1);
		this.codeControlsLayoutLevel1.setComponentAlignment(this.germplasmTypeComboBoxLevel1, Alignment.MIDDLE_LEFT);

		this.yearSuffixLevel1.setWidth(5, Sizeable.UNITS_EM);
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

		this.locationIdentifierCombobox.setWidth(5, Sizeable.UNITS_EM);
		this.locationIdentifierCombobox.setStyleName("lst-option-control");
		this.codeControlsLayoutLevel2.addComponent(this.locationIdentifierCombobox);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.locationIdentifierCombobox, Alignment.MIDDLE_LEFT);

		this.germplasmTypeComboBoxLevel2.setWidth(5, Sizeable.UNITS_EM);
		this.codeControlsLayoutLevel2.addComponent(this.germplasmTypeComboBoxLevel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.germplasmTypeComboBoxLevel2, Alignment.MIDDLE_LEFT);

		this.yearSuffixLevel2.setWidth(5, Sizeable.UNITS_EM);
		this.codeControlsLayoutLevel2.addComponent(this.yearSuffixLevel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(this.yearSuffixLevel2, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel2 = new Label(SEQUENCE_LABEL);
		sequenceLabel2.setStyleName(LST_SEQUENCE_LABEL_CLASS);
		this.codeControlsLayoutLevel2.addComponent(sequenceLabel2);
		this.codeControlsLayoutLevel2.setComponentAlignment(sequenceLabel2, Alignment.MIDDLE_LEFT);

		// by default only level 1 panel is visible
		this.codeControlsLayoutLevel2.setVisible(false);

		//Level 3 layout is the same as the default layout
		this.codeControlsLayoutLevel3 = this.assignCodesDefaultLayout.constructDefaultCodeControlsLayout();

		// by default only level 1 panel is visible
		this.codeControlsLayoutLevel3.setVisible(false);

		this.codesLayout.addComponent(this.codeControlsLayoutLevel1);
		this.codesLayout.addComponent(this.codeControlsLayoutLevel2);
		this.codesLayout.addComponent(this.codeControlsLayoutLevel3);
		this.codesLayout.setComponentAlignment(this.codeControlsLayoutLevel1, Alignment.MIDDLE_LEFT);
		this.codesLayout.setComponentAlignment(this.codeControlsLayoutLevel2, Alignment.MIDDLE_LEFT);
		this.codesLayout.setComponentAlignment(this.codeControlsLayoutLevel3, Alignment.MIDDLE_LEFT);
		this.codesLayout.setExpandRatio(this.codeControlsLayoutLevel1, 2);
		this.codesLayout.setExpandRatio(this.codeControlsLayoutLevel2, 0);
		this.codesLayout.setExpandRatio(this.codeControlsLayoutLevel3, 0);
	}

	public HorizontalLayout getCodeControlsLayoutLevel1() {
		return this.codeControlsLayoutLevel1;
	}

	public HorizontalLayout getCodeControlsLayoutLevel2() {
		return this.codeControlsLayoutLevel2;
	}

	public HorizontalLayout getCodeControlsLayoutLevel3() {
		return this.codeControlsLayoutLevel3;
	}
}
