
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
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodesDefaultLayout;
import org.generationcp.breeding.manager.listmanager.dialog.layout.AssignCodeCustomLayout;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.generationcp.middleware.service.api.GermplasmNamingReferenceDataResolver;
import org.generationcp.middleware.service.api.GermplasmNamingService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class AssignCodesDialog extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, Window.CloseListener {

	public static final String SEQUENCE_PLACEHOLDER = "[SEQ]";
	public static final String LEVEL1 = "Level 1";
	public static final String LEVEL2 = "Level 2";
	public static final String LEVEL3 = "Level 3";
	public static final String LAYOUT_CUSTOM = "cymmit";
	public static final String LAYOUT_DEFAULT = "default";

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

	private HorizontalLayout codeControlsLayoutDefault;

	private HorizontalLayout codesLayout;
	private OptionGroup codingLevelOptions;
	private Label exampleText;
	private Button cancelButton;
	private Button continueButton;
	private Set<Integer> gidsToProcess = new HashSet<>();
	private final boolean isCustomLayout = true;

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
		this.cancelButton = new Button();
		this.continueButton = new Button();
		this.continueButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.codesLayout = new HorizontalLayout();

		// set immediate to true for those fields we will listen to for the changes on the screen
		this.codingLevelOptions.setImmediate(true);
		this.assignCodesDefaultLayout = new AssignCodesDefaultLayout(this.exampleText, this.codesLayout);
		this.assignCodesDefaultLayout.instantiateComponents();
		if (this.isCustomLayout) {
			this.assignCodesCustomLayout = new AssignCodeCustomLayout(this.germplasmNamingReferenceDataResolver, this.contextUtil,
					this.assignCodesDefaultLayout, this.codingLevelOptions, this.codesLayout, this.exampleText);
			this.assignCodesCustomLayout.instantiateComponents();
		}
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
					for (final Integer gid : AssignCodesDialog.this.gidsToProcess) {
						// TODO pass user and location. Hardcoded to 0 = unknown for now.
						String groupNamePrefix = "";
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
		this.codesLayout.setSpacing(true);
		this.codesLayout.setStyleName("lst-border");

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
		exampleLayout.setHeight("120px");
		exampleLayout.setSpacing(false);
		exampleLayout.setStyleName("lst-example-layout");
		final Label exampleLabel = new Label(this.messageSource.getMessage(Message.ASSIGN_CODES_EXAMPLE));
		exampleLabel.setStyleName("lst-margin-left");
		exampleLabel.setSizeUndefined();
		exampleLayout.addComponent(exampleLabel);
		this.exampleText.setStyleName("lst-example-text lst-margin-left");
		exampleLayout.addComponent(this.exampleText);
		//TODO Remove that temporary solution for the layout of the components with custom proper layout
		final Label emptyLabel = new Label("");
		exampleLayout.addComponent(emptyLabel);

		exampleLayout.setComponentAlignment(exampleLabel, Alignment.BOTTOM_LEFT);
		exampleLayout.setComponentAlignment(this.exampleText, Alignment.TOP_LEFT);

		this.codesLayout.addComponent(exampleLayout);
		this.codesLayout.setComponentAlignment(exampleLayout, Alignment.TOP_LEFT);
		this.codesLayout.setExpandRatio(exampleLayout, 1);

		if (this.isCustomLayout) {
			this.assignCodesCustomLayout.layoutComponents();
		} else {
			this.assignCodesDefaultLayout.layoutComponents();
		}

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
