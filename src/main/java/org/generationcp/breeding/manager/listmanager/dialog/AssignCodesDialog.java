
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodeCustomLayout;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodesDefaultLayout;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
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
	public static final String LEVEL1 = "Level1";
	public static final String LEVEL2 = "Level2";
	public static final String LEVEL3 = "Level3";

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

	private AssignCodesDefaultLayout assignCodesDefaultLayout;
	private AssignCodeCustomLayout assignCodesCustomLayout;

	private VerticalLayout codesLayout;
	private OptionGroup codingLevelOptions;
	private Label exampleText;
	private Button cancelButton;
	private Button continueButton;
	private Set<Integer> gidsToProcess = new HashSet<>();
	private final boolean isCustomLayout;
	private VerticalLayout exampleLayout;

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
		this.codingLevelOptions = new OptionGroup();
		this.exampleText = new Label();
		this.cancelButton = new Button();
		this.continueButton = new Button();
		this.continueButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.codesLayout = new VerticalLayout();
		this.exampleLayout = new VerticalLayout();

		// set immediate to true for those fields we will listen to for the changes on the screen
		this.codingLevelOptions.setImmediate(true);

		this.assignCodesDefaultLayout = new AssignCodesDefaultLayout(this.exampleText, this.codesLayout, this.messageSource);
		this.assignCodesDefaultLayout.instantiateComponents();

		if (this.isCustomLayout) {
			this.assignCodesCustomLayout = new AssignCodeCustomLayout(this.germplasmNamingReferenceDataResolver, this.contextUtil,
					this.messageSource,	this.assignCodesDefaultLayout, this.codingLevelOptions, this.codesLayout, this.exampleText, this
					.exampleLayout);
			this.assignCodesCustomLayout.instantiateComponents();
		}
	}

	@Override
	public void initializeValues() {
		//TODO There could be custom number of levels in the future
		this.codingLevelOptions.addItem(LEVEL1);
		this.codingLevelOptions.addItem(LEVEL2);
		this.codingLevelOptions.addItem(LEVEL3);
		this.codingLevelOptions.setItemCaption(LEVEL1, this.messageSource.getMessage(Message.LEVEL1));
		this.codingLevelOptions.setItemCaption(LEVEL2, this.messageSource.getMessage(Message.LEVEL2));
		this.codingLevelOptions.setItemCaption(LEVEL3, this.messageSource.getMessage(Message.LEVEL3));

		// by default the level 1 is selected
		this.codingLevelOptions.select(LEVEL1);

		if (this.isCustomLayout) {
			this.assignCodesCustomLayout.initializeValues();
		}
	}

	@Override
	public void addListeners() {
		if (this.isCustomLayout) {
			this.assignCodesCustomLayout.addListeners(this.codingLevelOptions);
		}
		this.assignCodesDefaultLayout.addListeners();

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
						AssignCodesDialog.this.assignCodesDefaultLayout.validate();
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
							AssignCodesDialog.this.germplasmNamingReferenceDataResolver.resolveNameType(AssignCodesDialog.this.getLevel());

					// TODO performance tuning when processing large number of list entries..
					for (final Integer gid : AssignCodesDialog.this.gidsToProcess) {
						// TODO pass user and location. Hardcoded to 0 = unknown for now.
						final String groupNamePrefix;
						if (AssignCodesDialog.this.isCustomLayout) {
							groupNamePrefix = AssignCodesDialog.this.assignCodesCustomLayout.getGroupNamePrefix();
						} else {
							groupNamePrefix = AssignCodesDialog.this.assignCodesDefaultLayout.getGroupNamePrefix();
						}
						final GermplasmGroupNamingResult result = AssignCodesDialog.this.germplasmNamingService.applyGroupName(gid,
								groupNamePrefix, nameType, 0, 0);
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

		// bordered area
		this.codesLayout.setWidth("97%");
		this.codesLayout.setHeight("160px");
		this.codesLayout.addStyleName("lst-border");

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
		this.exampleLayout.setWidth("100%");
		this.exampleLayout.setHeight("40px");
		this.exampleLayout.setSpacing(false);
		this.exampleLayout.setStyleName("lst-example-layout");
		final Label exampleLabel = new Label(this.messageSource.getMessage(Message.ASSIGN_CODES_EXAMPLE));
		exampleLabel.setStyleName("lst-margin-left");
		exampleLabel.setSizeUndefined();
		this.exampleLayout.addComponent(exampleLabel);
		this.exampleText.setStyleName("lst-example-text lst-margin-left");
		this.exampleLayout.addComponent(this.exampleText);

		this.exampleLayout.setComponentAlignment(exampleLabel, Alignment.TOP_LEFT);
		this.exampleLayout.setComponentAlignment(this.exampleText, Alignment.TOP_LEFT);

		if (this.isCustomLayout) {
			this.assignCodesCustomLayout.layoutComponents();
		} else {
			this.assignCodesDefaultLayout.layoutComponents();
		}

		this.codesLayout.addComponent(this.exampleLayout);
		this.codesLayout.setComponentAlignment(this.exampleLayout, Alignment.TOP_LEFT);

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
