/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.crossingmanager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * This class contains the absolute layout of UI elements in Cross Name section in "Enter Additional Details..." tab in Crossing Manager
 * application
 *
 * @author Darla Ani
 *
 */
@Configurable
public class AdditionalDetailsCrossNameComponent extends AbsoluteLayout
		implements InitializingBean, InternationalizableComponent, CrossesMadeContainerUpdateListener {

	private final class OKButtonClickListener implements Button.ClickListener {

		private static final long serialVersionUID = -3519880320817778816L;

		@Override
		public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
			final boolean spaceBetweenPrefixAndCode = AdditionalDetailsCrossNameComponent.this.addSpaceCheckBox.booleanValue();
			final boolean spaceBetweenSuffixAndCode = AdditionalDetailsCrossNameComponent.this.addSpaceAfterSuffixCheckBox.booleanValue();

			String prefix = null;
			if (AdditionalDetailsCrossNameComponent.this.prefixTextField.getValue() == null
					|| AdditionalDetailsCrossNameComponent.this.prefixTextField.getValue().toString().length() == 0) {
				MessageNotifier.showRequiredFieldError(AdditionalDetailsCrossNameComponent.this.parentWindow,
						AdditionalDetailsCrossNameComponent.this.messageSource.getMessage(Message.PLEASE_SPECIFY_A_PREFIX));
				return;
			} else {
				prefix = AdditionalDetailsCrossNameComponent.this.prefixTextField.getValue().toString().trim();
			}

			String suffix = null;
			if (AdditionalDetailsCrossNameComponent.this.suffixTextField.getValue() != null) {
				suffix = AdditionalDetailsCrossNameComponent.this.suffixTextField.getValue().toString().trim();
			}

			int numOfAllowedDigits = 0;
			final boolean isNumOfZerosNeeded = AdditionalDetailsCrossNameComponent.this.sequenceNumCheckBox.booleanValue();
			if (isNumOfZerosNeeded) {
				numOfAllowedDigits = ((Integer) AdditionalDetailsCrossNameComponent.this.numOfAllowedDigitsSelect.getValue()).intValue();
			}

			final Object startNumberObj = AdditionalDetailsCrossNameComponent.this.startNumberTextField.getValue();
			if (startNumberObj == null || startNumberObj.toString().length() == 0) {
				MessageNotifier.showRequiredFieldError(AdditionalDetailsCrossNameComponent.this.parentWindow,
						AdditionalDetailsCrossNameComponent.this.messageSource.getMessage(Message.PLEASE_SPECIFY_A_STARTING_NUMBER));
				return;
			} else if (startNumberObj != null && numOfAllowedDigits > 0 && startNumberObj.toString().length() > numOfAllowedDigits) {
				MessageNotifier.showRequiredFieldError(AdditionalDetailsCrossNameComponent.this.parentWindow,
						AdditionalDetailsCrossNameComponent.this.messageSource
								.getMessage(Message.STARTING_NUMBER_IS_GREATER_THAN_THE_ALLOWED_NO_OF_DIGITS));
				return;
			} else if (startNumberObj.toString().length() > 9) {
				MessageNotifier.showRequiredFieldError(AdditionalDetailsCrossNameComponent.this.parentWindow,
						AdditionalDetailsCrossNameComponent.this.messageSource.getMessage(Message.STARTING_NUMBER_HAS_TOO_MANY_DIGITS));
				return;
			} else {
				try {
					Integer.parseInt(startNumberObj.toString());
				} catch (final NumberFormatException ex) {
					MessageNotifier.showRequiredFieldError(AdditionalDetailsCrossNameComponent.this.parentWindow,
							AdditionalDetailsCrossNameComponent.this.messageSource.getMessage(Message.PLEASE_ENTER_VALID_STARTING_NUMBER));
					return;
				}
			}

			final int startNumber = Integer.parseInt(startNumberObj.toString());

			final int numberOfEntries = AdditionalDetailsCrossNameComponent.this.fillWithSource.getNumberOfEntries();
			final StringBuilder builder = new StringBuilder();
			builder.append(prefix);
			if (spaceBetweenPrefixAndCode) {
				builder.append(" ");
			}

			if (numOfAllowedDigits > 0) {
				for (int i = 0; i < numOfAllowedDigits; i++) {
					builder.append("0");
				}
			}
			final int lastNumber = startNumber + numberOfEntries;
			builder.append(lastNumber);

			if (suffix != null && spaceBetweenSuffixAndCode) {
				builder.append(" ");
			}

			if (suffix != null) {
				builder.append(suffix);
			}

			if (AdditionalDetailsCrossNameComponent.this.propertyIdToFill.equals(ColumnLabels.SEED_SOURCE.getName())
					&& builder.toString().length() > 255) {
				MessageNotifier.showRequiredFieldError(AdditionalDetailsCrossNameComponent.this.parentWindow,
						AdditionalDetailsCrossNameComponent.this.messageSource.getMessage(Message.SEQUENCE_TOO_LONG_FOR_SEED_SOURCE));
				return;
			} else if (AdditionalDetailsCrossNameComponent.this.propertyIdToFill.equals(ColumnLabels.ENTRY_CODE.getName())
					&& builder.toString().length() > 47) {
				MessageNotifier.showRequiredFieldError(AdditionalDetailsCrossNameComponent.this.parentWindow,
						AdditionalDetailsCrossNameComponent.this.messageSource.getMessage(Message.SEQUENCE_TOO_LONG_FOR_ENTRY_CODE));
				return;
			}

			AdditionalDetailsCrossNameComponent.this.fillWithSource.fillWithSequence(
					AdditionalDetailsCrossNameComponent.this.propertyIdToFill, prefix, suffix, startNumber, numOfAllowedDigits,
					spaceBetweenPrefixAndCode, spaceBetweenSuffixAndCode);
			final Window parent = AdditionalDetailsCrossNameComponent.this.parentWindow.getParent();
			parent.removeWindow(AdditionalDetailsCrossNameComponent.this.parentWindow);
		}
	}

	public static final String GENERATE_BUTTON_ID = "Generate Next Name Id";

	private static final long serialVersionUID = -1197900610042529900L;
	private static final Logger LOG = LoggerFactory.getLogger(AdditionalDetailsCrossNameComponent.class);
	private static final Integer MAX_NUM_OF_ALLOWED_DIGITS = 9;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmManager;

	private Label specifyPrefixLabel;
	private Label specifySuffixLabel;
	private Label howManyDigitsLabel;
	private Label nextNameInSequenceLabel;
	private Label generatedNameLabel;
	private Label specifyStartNumberLabel;

	private TextField prefixTextField;
	private TextField suffixTextField;
	private TextField startNumberTextField;
	private CheckBox sequenceNumCheckBox;
	private CheckBox addSpaceCheckBox;
	private CheckBox addSpaceAfterSuffixCheckBox;
	private Select numOfAllowedDigitsSelect;
	private Button generateButton;
	private Button okButton;
	private Button cancelButton;

	private final AbstractComponent[] digitsToggableComponents = new AbstractComponent[2];
	private final AbstractComponent[] otherToggableComponents = new AbstractComponent[9];

	// store prefix used for MW method including zeros, if any
	private String lastPrefixUsed;
	private Integer nextNumberInSequence;

	private CrossesMadeContainer container;

	private FillWith fillWithSource;
	private String propertyIdToFill;
	private boolean forFillWith = false;
	private Window parentWindow;

	public AdditionalDetailsCrossNameComponent() {
		super();
		this.forFillWith = false;
	}

	public AdditionalDetailsCrossNameComponent(final FillWith fillWithSource, final String propertyIdToFill, final Window parentWindow) {
		super();
		this.forFillWith = true;
		this.fillWithSource = fillWithSource;
		this.propertyIdToFill = propertyIdToFill;
		this.parentWindow = parentWindow;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("200px");
		this.setWidth("700px");

		this.sequenceNumCheckBox = new CheckBox();
		this.sequenceNumCheckBox.setDebugId("sequenceNumCheckBox");
		this.sequenceNumCheckBox.setImmediate(true);
		this.sequenceNumCheckBox.addListener(new Property.ValueChangeListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = 547145467273073423L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				AdditionalDetailsCrossNameComponent.this.enableSpecifyNumOfAllowedDigitsComponents(
						AdditionalDetailsCrossNameComponent.this.sequenceNumCheckBox.booleanValue());
			}
		});

		this.addSpaceCheckBox = new CheckBox();
		this.addSpaceCheckBox.setDebugId("addSpaceCheckBox");
		this.addSpaceCheckBox.setImmediate(true);

		this.specifyPrefixLabel = new Label();
		this.specifyPrefixLabel.setDebugId("specifyPrefixLabel");
		this.prefixTextField = new TextField();
		this.prefixTextField.setDebugId("prefixTextField");
		this.prefixTextField.setWidth("300px");

		this.howManyDigitsLabel = new Label();
		this.howManyDigitsLabel.setDebugId("howManyDigitsLabel");
		this.numOfAllowedDigitsSelect = new Select();
		this.numOfAllowedDigitsSelect.setDebugId("numOfAllowedDigitsSelect");
		for (int i = 1; i <= AdditionalDetailsCrossNameComponent.MAX_NUM_OF_ALLOWED_DIGITS; i++) {
			this.numOfAllowedDigitsSelect.addItem(Integer.valueOf(i));
		}
		this.numOfAllowedDigitsSelect.setNullSelectionAllowed(false);
		this.numOfAllowedDigitsSelect.select(Integer.valueOf(1));
		this.numOfAllowedDigitsSelect.setWidth("50px");

		this.specifySuffixLabel = new Label();
		this.specifySuffixLabel.setDebugId("specifySuffixLabel");
		this.suffixTextField = new TextField();
		this.suffixTextField.setDebugId("suffixTextField");
		this.suffixTextField.setWidth("300px");

		this.nextNameInSequenceLabel = new Label();
		this.nextNameInSequenceLabel.setDebugId("nextNameInSequenceLabel");
		this.generatedNameLabel = new Label();
		this.generatedNameLabel.setDebugId("generatedNameLabel");

		this.generateButton = new Button();
		this.generateButton.setDebugId("generateButton");
		this.generateButton.setData(AdditionalDetailsCrossNameComponent.GENERATE_BUTTON_ID);
		this.generateButton.addListener(new CrossingManagerImportButtonClickListener(this));

		if (this.forFillWith) {
			this.setHeight("250px");
			this.setWidth("490px");
			this.specifyStartNumberLabel = new Label();
			this.specifyStartNumberLabel.setDebugId("specifyStartNumberLabel");

			this.startNumberTextField = new TextField();
			this.startNumberTextField.setDebugId("startNumberTextField");
			this.startNumberTextField.setWidth("90px");

			this.addSpaceAfterSuffixCheckBox = new CheckBox();
			this.addSpaceAfterSuffixCheckBox.setDebugId("addSpaceAfterSuffixCheckBox");
			this.addSpaceAfterSuffixCheckBox.setImmediate(true);

			this.cancelButton = new Button();
			this.cancelButton.setDebugId("cancelButton");
			this.cancelButton.addListener(new Button.ClickListener() {

				private static final long serialVersionUID = -3519880320817778816L;

				@Override
				public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
					final Window parent = AdditionalDetailsCrossNameComponent.this.parentWindow.getParent();
					parent.removeWindow(AdditionalDetailsCrossNameComponent.this.parentWindow);
				}
			});

			this.okButton = new Button();
			this.okButton.setDebugId("okButton");
			this.okButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
			this.okButton.addListener(new OKButtonClickListener());

		}

		this.layoutComponents();
		this.initializeToggableComponents();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.specifyPrefixLabel, Message.SPECIFY_PREFIX_REQUIRED);
		this.messageSource.setCaption(this.specifySuffixLabel, Message.SPECIFY_SUFFIX_OPTIONAL);
		this.messageSource.setCaption(this.sequenceNumCheckBox, Message.SEQUENCE_NUMBER_SHOULD_HAVE);
		this.messageSource.setCaption(this.addSpaceCheckBox, Message.ADD_SPACE_BETWEEN_PREFIX_AND_CODE);
		this.messageSource.setCaption(this.howManyDigitsLabel, Message.DIGITS);

		if (!this.forFillWith) {
			this.messageSource.setCaption(this.nextNameInSequenceLabel, Message.THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE);
			this.messageSource.setCaption(this.generateButton, Message.GENERATE);
		} else {
			this.messageSource.setCaption(this.addSpaceAfterSuffixCheckBox, Message.ADD_SPACE_BETWEEN_SUFFIX_AND_CODE);
			this.messageSource.setCaption(this.cancelButton, Message.CANCEL);
			this.messageSource.setCaption(this.okButton, Message.OK);
			this.messageSource.setCaption(this.specifyStartNumberLabel, Message.SPECIFY_START_NUMBER);
		}
	}

	private void layoutComponents() {
		if (!this.forFillWith) {
			this.addComponent(this.specifyPrefixLabel, "top:25px;left:0px");
			this.addComponent(this.prefixTextField, "top:6px;left:165px");
			this.addComponent(this.sequenceNumCheckBox, "top:37px;left:0px");
			this.addComponent(this.howManyDigitsLabel, "top:53px;left:289px");
			this.addComponent(this.numOfAllowedDigitsSelect, "top:35px;left:235px");
			this.addComponent(this.addSpaceCheckBox, "top:65px;left:0px");
			this.addComponent(this.specifySuffixLabel, "top:115px;left:0px");
			this.addComponent(this.suffixTextField, "top:95px;left:165px");
			this.addComponent(this.nextNameInSequenceLabel, "top:145px;left:0px");
			this.addComponent(this.generatedNameLabel, "top:145px;left:265px");
			this.addComponent(this.generateButton, "top:155px;left:0px");
		} else {
			this.addComponent(this.specifyPrefixLabel, "top:25px;left:10px");
			this.addComponent(this.prefixTextField, "top:6px;left:175px");
			this.addComponent(this.addSpaceCheckBox, "top:37px;left:10px");
			this.addComponent(this.specifyStartNumberLabel, "top:87px;left:10px");
			this.addComponent(this.startNumberTextField, "top:67px;left:175px");
			this.addComponent(this.sequenceNumCheckBox, "top:100px;left:10px");
			this.addComponent(this.numOfAllowedDigitsSelect, "top:98px;left:335px");
			this.addComponent(this.howManyDigitsLabel, "top:119px;left:389px");
			this.addComponent(this.specifySuffixLabel, "top:150px;left:10px");
			this.addComponent(this.suffixTextField, "top:130px;left:175px");
			this.addComponent(this.addSpaceAfterSuffixCheckBox, "top:162px;left:10px");

			final HorizontalLayout layoutButtonArea = new HorizontalLayout();
			layoutButtonArea.setDebugId("layoutButtonArea");
			layoutButtonArea.setSpacing(true);
			layoutButtonArea.addComponent(this.cancelButton);
			layoutButtonArea.addComponent(this.okButton);

			this.addComponent(layoutButtonArea, "top:205px; left:200px");
		}
	}

	private void initializeToggableComponents() {
		this.digitsToggableComponents[0] = this.howManyDigitsLabel;
		this.digitsToggableComponents[1] = this.numOfAllowedDigitsSelect;

		this.otherToggableComponents[0] = this.specifyPrefixLabel;
		this.otherToggableComponents[1] = this.specifySuffixLabel;
		this.otherToggableComponents[2] = this.nextNameInSequenceLabel;
		this.otherToggableComponents[3] = this.prefixTextField;
		this.otherToggableComponents[4] = this.suffixTextField;
		this.otherToggableComponents[5] = this.sequenceNumCheckBox;
		this.otherToggableComponents[6] = this.addSpaceCheckBox;
		this.otherToggableComponents[7] = this.generatedNameLabel;
		this.otherToggableComponents[8] = this.generateButton;

		this.enableSpecifyCrossNameComponents(true);
	}

	// Enables / disables UI elements for specifying Cross Name details
	private void enableSpecifyCrossNameComponents(final boolean enabled) {
		for (final AbstractComponent component : this.otherToggableComponents) {
			component.setEnabled(enabled);
		}
		this.enableSpecifyNumOfAllowedDigitsComponents(enabled && this.sequenceNumCheckBox.booleanValue());
	}

	private void enableSpecifyNumOfAllowedDigitsComponents(final boolean enabled) {
		for (final AbstractComponent component : this.digitsToggableComponents) {
			component.setEnabled(enabled);
		}
	}

	// Action handler for generating cross names
	public void generateNextNameButtonAction() {
		if (this.validateCrossNameFields()) {
			final String suffix = ((String) this.suffixTextField.getValue()).trim();

			try {
				this.lastPrefixUsed = this.buildPrefixString();
				final String nextSequenceNumberString = this.germplasmManager.getNextSequenceNumberForCrossName(this.lastPrefixUsed.trim(), suffix);

				this.nextNumberInSequence = Integer.parseInt(nextSequenceNumberString);
				this.generatedNameLabel.setCaption(this.buildNextNameInSequence(this.lastPrefixUsed, suffix, this.nextNumberInSequence));

			} catch (final MiddlewareQueryException e) {
				AdditionalDetailsCrossNameComponent.LOG.error(e.toString() + "\n" + e.getStackTrace(), e);
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE));
			}
		}

	}

	private boolean validateCrossNameFields() {
		final Window window = this.getWindow();
		final String prefix = ((String) this.prefixTextField.getValue()).trim();

		if (StringUtils.isEmpty(prefix)) {
			MessageNotifier.showError(window, this.messageSource.getMessage(Message.ERROR_WITH_CROSS_CODE),
					this.messageSource.getMessage(Message.ERROR_ENTER_PREFIX_FIRST));
			return false;
		}

		return true;
	}

	private boolean validateGeneratedName() {

		// if Generate button never pressed
		if (this.nextNumberInSequence == null) {
			MessageNotifier.showError(this.getWindow(), "Error with Cross Code",
					MessageFormat.format(this.messageSource.getMessage(Message.ERROR_NEXT_NAME_MUST_BE_GENERATED_FIRST), ""));
			return false;

			// if prefix specifications were changed and next name in sequence not generated first
		} else {
			final String currentPrefixString = this.buildPrefixString();
			if (!currentPrefixString.equals(this.lastPrefixUsed)) {
				MessageNotifier.showError(this.getWindow(), "Error with Cross Code", MessageFormat.format(
						this.messageSource.getMessage(Message.ERROR_NEXT_NAME_MUST_BE_GENERATED_FIRST), " (" + currentPrefixString + ")"));
				return false;
			}
		}

		return true;
	}

	private String buildPrefixString() {
		if (this.addSpaceCheckBox.booleanValue()) {
			return ((String) this.prefixTextField.getValue()).trim() + " ";
		}
		return ((String) this.prefixTextField.getValue()).trim();
	}

	private String buildNextNameInSequence(final String prefix, final String suffix, final Integer number) {
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(this.getNumberWithLeadingZeroesAsString(number));
		if (!StringUtils.isEmpty(suffix)) {
			sb.append(" ");
			sb.append(suffix);
		}
		return sb.toString();
	}

	private String getNumberWithLeadingZeroesAsString(final Integer number) {
		final StringBuilder sb = new StringBuilder();
		final String numberString = number.toString();
		if (this.sequenceNumCheckBox.booleanValue()) {
			final Integer numOfZeros = (Integer) this.numOfAllowedDigitsSelect.getValue();
			final int numOfZerosNeeded = numOfZeros - numberString.length();
			if (numOfZerosNeeded > 0) {
				for (int i = 0; i < numOfZerosNeeded; i++) {
					sb.append("0");
				}
			}
		}
		sb.append(number);
		return sb.toString();
	}

	@Override
	public boolean updateCrossesMadeContainer(final CrossesMadeContainer container) {

		if (this.container != null && this.container.getCrossesMade() != null && this.container.getCrossesMade().getCrossesMap() != null
				&& this.validateCrossNameFields() && this.validateGeneratedName()) {

			int ctr = this.nextNumberInSequence;
			final String suffix = (String) this.suffixTextField.getValue();

			final Map<Germplasm, Name> crossesMap = this.container.getCrossesMade().getCrossesMap();
			final List<GermplasmListEntry> oldCrossNames = new ArrayList<GermplasmListEntry>();

			// Store old cross name and generate new names based on prefix, suffix specifications
			for (final Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()) {
				final Name nameObject = entry.getValue();
				final String oldCrossName = nameObject.getNval();
				nameObject.setNval(this.buildNextNameInSequence(this.lastPrefixUsed, suffix, ctr++));

				final Germplasm germplasm = entry.getKey();
				final Integer tempGid = germplasm.getGid();
				final GermplasmListEntry oldNameEntry = new GermplasmListEntry(tempGid, tempGid, tempGid, oldCrossName);

				oldCrossNames.add(oldNameEntry);
			}
			// Only store the "original" cross names, would not store previous names on 2nd, 3rd, ... change
			if (this.container.getCrossesMade().getOldCrossNames() == null
					|| this.container.getCrossesMade().getOldCrossNames().isEmpty()) {
				this.container.getCrossesMade().setOldCrossNames(oldCrossNames);
			}

			return true;

		}

		return false;
	}

	Button getOkButton() {
		return this.okButton;
	}

	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	void setParent(final Window parent) {
		this.parentWindow = parent;
	}

	void setPrefixTextFieldValue(final String value) {
		this.prefixTextField.setValue(value);
	}

	void setSequenceNumCheckBoxValue(final boolean value) {
		this.sequenceNumCheckBox.setValue(value);
	}

	void setNumberOfAllowedDigitsSelectValue(final int value) {
		this.numOfAllowedDigitsSelect.setValue(value);
	}

	void setStartingNumberTextFieldValue(final String value) {
		this.startNumberTextField.setValue(value);
	}

	void setPropertyIdtoFillValue(final String value) {
		this.propertyIdToFill = value;
	}

	void setFillWithSource(final FillWith fillWith) {
		this.fillWithSource = fillWith;
	}

	public FillWith getFillWithSource() {
		return this.fillWithSource;
	}

	public String getPropertyIdToFill() {
		return this.propertyIdToFill;
	}
}
