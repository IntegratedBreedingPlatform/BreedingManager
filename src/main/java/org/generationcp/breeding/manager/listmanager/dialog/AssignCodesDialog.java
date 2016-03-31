
package org.generationcp.breeding.manager.listmanager.dialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
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
		for (final String programIdentifier : this.germplasmNamingService.getProgramIdentifiers(1)) {
			this.programIdentifiersComboBox.addItem(programIdentifier);
		}
		for (final GermplasmType germplasmType : this.germplasmNamingService.getGermplasmTypes()) {
			this.germplasmTypeComboBox.addItem(germplasmType.name());
		}

		// by default the current year in 2 digits format will be set to yearSuffix text field
		final Date today = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy");
		this.yearSuffix.setValue(simpleDateFormat.format(today));

		this.programIdentifiersComboBox.setNullSelectionAllowed(false);
		this.germplasmTypeComboBox.setNullSelectionAllowed(false);
	}

	@Override
	public void addListeners() {
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
		final StringBuffer resultMessages = new StringBuffer();
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				// TODO hard coded level = 1, derive from UI choice..
				UserDefinedField nameType = AssignCodesDialog.this.germplasmNameTypeResolver.resolve(1);
				for (final Integer gid : AssignCodesDialog.this.gidsToProcess) {
					final GermplasmGroupNamingResult result =
							AssignCodesDialog.this.germplasmNamingService.applyGroupName(gid, "AB-H-15-01", nameType, 1, 1);
					resultMessages.append(StringUtils.join(result.getMessages(), "<br/>"));
				}
				resultMessages.append("<br/>");
			}
		});
		// TODO replace with proper dialog window..
		MessageNotifier.showMessage(this.getWindow(), AssignCodesDialog.this.messageSource.getMessage(Message.ASSIGN_CODES),
				resultMessages.toString());
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
		optionsLayout.setComponentAlignment(this.codingLevelOptions, Alignment.MIDDLE_CENTER);

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
		final Label exampleText = new Label("ABH051a");
		exampleText.setStyleName("lst-example-text lst-margin-left");
		exampleLayout.addComponent(exampleText);
		exampleLayout.setComponentAlignment(exampleLabel, Alignment.MIDDLE_LEFT);
		exampleLayout.setComponentAlignment(exampleText, Alignment.MIDDLE_LEFT);

		//codes controls area
		final HorizontalLayout codesControlsLayout = new HorizontalLayout();
		codesControlsLayout.setWidth("100%");
		codesControlsLayout.setHeight("55px");
		codesControlsLayout.setSpacing(true);

		this.programIdentifiersComboBox.setWidth(5, 3);
		codesControlsLayout.addComponent(this.programIdentifiersComboBox);
		codesControlsLayout.setComponentAlignment(this.programIdentifiersComboBox, Alignment.MIDDLE_LEFT);

		this.germplasmTypeComboBox.setWidth(5, 3);
		codesControlsLayout.addComponent(this.germplasmTypeComboBox);
		codesControlsLayout.setComponentAlignment(this.germplasmTypeComboBox, Alignment.MIDDLE_LEFT);

		this.yearSuffix.setWidth(5, 3);
		codesControlsLayout.addComponent(this.yearSuffix);
		codesControlsLayout.setComponentAlignment(this.yearSuffix, Alignment.MIDDLE_LEFT);

		final Label sequnceSuffix = new Label("SEQ");
		codesControlsLayout.addComponent(sequnceSuffix);
		codesControlsLayout.setComponentAlignment(sequnceSuffix, Alignment.MIDDLE_LEFT);

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
