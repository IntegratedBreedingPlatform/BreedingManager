
package org.generationcp.breeding.manager.listmanager.dialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
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
	private VerticalLayout dialogLayout;
	private Set<Integer> gidsToProcess = new HashSet<>();
	private ComboBox programIdentifiersComboBox;
	private ComboBox germplasmTypeComboBox;
	private TextField yearSuffix;
	private Label exampleText;

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
		this.germplasmTypeComboBox = new ComboBox();
		this.yearSuffix = new TextField();
		this.cancelButton = new Button();
		this.continueButton = new Button();
		this.continueButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		this.codingLevelOptions.addItem("Level 1");
		this.codingLevelOptions.addItem("Level 2");
		this.codingLevelOptions.addItem("Level 3");

		// by default the level 1 is selected
		this.codingLevelOptions.select("Level 1");
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
			this.germplasmTypeComboBox.addItem(germplasmType.name());
		}
		//the first value in the list is a default selection
		if (!germplasmTypes.isEmpty()) {
			final GermplasmType germplasmType = (GermplasmType) germplasmTypes.toArray()[0];
			this.germplasmTypeComboBox.setValue(germplasmType.name());
		}

		// by default the current year in 2 digits format will be set to yearSuffix text field
		final Date today = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy");
		this.yearSuffix.setValue(simpleDateFormat.format(today));

		this.programIdentifiersComboBox.setNullSelectionAllowed(false);
		this.germplasmTypeComboBox.setNullSelectionAllowed(false);

		//update example text after setting defaults
		this.exampleText.setValue(this.programIdentifiersComboBox.getValue().toString() +
				this.germplasmTypeComboBox.getValue().toString() + this.yearSuffix.getValue().toString() + SEQUENCE_PLACEHOLDER);
	}

	@Override
	public void addListeners() {

		final Property.ValueChangeListener codeOptionsListener = new Property.ValueChangeListener() {

			@Override
			public void valueChange(final Property.ValueChangeEvent event) {
				// TODO this will be different for each level and configurable
				AssignCodesDialog.this.exampleText.setValue(
						AssignCodesDialog.this.programIdentifiersComboBox.getValue().toString() +
								AssignCodesDialog.this.germplasmTypeComboBox.getValue().toString() +
								AssignCodesDialog.this.yearSuffix.getValue().toString() + SEQUENCE_PLACEHOLDER);

			}
		};
		this.programIdentifiersComboBox.addListener(codeOptionsListener);
		this.germplasmTypeComboBox.addListener(codeOptionsListener);
		this.yearSuffix.addListener(codeOptionsListener);


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
		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		final Map<Integer, GermplasmGroupNamingResult> assignCodesResultsMap = new HashMap<>();
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				// TODO hard coded level = 1, derive from UI choice..
				UserDefinedField nameType = AssignCodesDialog.this.germplasmNameTypeResolver.resolve(1);
				for (final Integer gid : AssignCodesDialog.this.gidsToProcess) {
					final GermplasmGroupNamingResult result =
							AssignCodesDialog.this.germplasmNamingService.applyGroupName(gid, "AB-H-15-01", nameType, 1, 1);
					assignCodesResultsMap.put(gid, result);
				}
			}
		});
		this.getParent().addWindow(new AssignCodesResultsDialog(assignCodesResultsMap));
		this.closeWindow();
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
		this.dialogLayout = new VerticalLayout();
		this.dialogLayout.setMargin(true);
		this.dialogLayout.setSpacing(true);

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

		// bordered area
		final HorizontalLayout codesLayout = new HorizontalLayout();
		codesLayout.setWidth("97%");
		codesLayout.setHeight("160px");
		codesLayout.setSpacing(true);
		codesLayout.setStyleName("lst-border");

		//example area
		final VerticalLayout exampleLayout = new VerticalLayout();
		exampleLayout.setWidth("100%");
		exampleLayout.setHeight("60px");
		exampleLayout.setSpacing(true);
		final Label exampleLabel = new Label("Example:");
		exampleLabel.setStyleName("lst-margin-left");
		exampleLayout.addComponent(exampleLabel);

		this.exampleText.setStyleName("lst-example-text lst-margin-left");
		exampleLayout.addComponent(this.exampleText);
		exampleLayout.setComponentAlignment(exampleLabel, Alignment.TOP_LEFT);
		exampleLayout.setComponentAlignment(this.exampleText, Alignment.MIDDLE_LEFT);

		//codes controls area
		final HorizontalLayout codesControlsLayout = new HorizontalLayout();
		codesControlsLayout.setWidth("100%");
		codesControlsLayout.setHeight("60px");

		this.programIdentifiersComboBox.setWidth(5, 3);
		this.programIdentifiersComboBox.setStyleName("lst-option-control");
		codesControlsLayout.addComponent(this.programIdentifiersComboBox);
		codesControlsLayout.setComponentAlignment(this.programIdentifiersComboBox, Alignment.MIDDLE_LEFT);

		this.germplasmTypeComboBox.setWidth(5, 3);
		codesControlsLayout.addComponent(this.germplasmTypeComboBox);
		codesControlsLayout.setComponentAlignment(this.germplasmTypeComboBox, Alignment.MIDDLE_LEFT);

		this.yearSuffix.setWidth(5, 3);
		codesControlsLayout.addComponent(this.yearSuffix);
		codesControlsLayout.setComponentAlignment(this.yearSuffix, Alignment.MIDDLE_LEFT);

		final Label sequenceLabel = new Label("SEQ");
		sequenceLabel.setStyleName("lst-sequence-label");
		codesControlsLayout.addComponent(sequenceLabel);
		codesControlsLayout.setComponentAlignment(sequenceLabel, Alignment.MIDDLE_LEFT);

		codesLayout.addComponent(exampleLayout);
		codesLayout.addComponent(codesControlsLayout);
		codesLayout.setComponentAlignment(exampleLayout, Alignment.MIDDLE_LEFT);
		codesLayout.setComponentAlignment(codesControlsLayout, Alignment.MIDDLE_LEFT);
		codesLayout.setExpandRatio(exampleLayout, 1);
		codesLayout.setExpandRatio(codesControlsLayout, 2);

		this.dialogLayout.addComponent(optionsLayout);
		this.dialogLayout.addComponent(codesLayout);
		this.dialogLayout.addComponent(buttonLayout);
		this.setContent(this.dialogLayout);

	}

	@Override
	public void windowClose(final CloseEvent e) {
		super.close();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this, Message.ASSIGN_CODES_HEADER);
		this.messageSource.setCaption(this.codingLevelOptions, Message.CODING_LEVEL);
		this.messageSource.setCaption(this.continueButton, Message.CONTINUE);
		this.messageSource.setCaption(this.cancelButton, Message.CANCEL);
	}
}
