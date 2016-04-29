
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.service.api.GermplasmGroup;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmGroupingComponent extends BaseSubWindow implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout, Window.CloseListener {

	private static final long serialVersionUID = -3348276076082552164L;
	private CheckBox preserveExistingGroupId;
	private CheckBox includeDescendants;

	private Button cancelButton;
	private Button continueButton;

	private VerticalLayout dialogLayout;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmGroupingService germplasmGroupingService;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private Set<Integer> gidsToProcess = new HashSet<>();

	public GermplasmGroupingComponent() {

	}

	public GermplasmGroupingComponent(final Set<Integer> gidsToProcess) {
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
		this.preserveExistingGroupId = new CheckBox();
		this.includeDescendants = new CheckBox();
		this.cancelButton = new Button();
		this.continueButton = new Button();
		this.continueButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		// Nothing to do.
	}

	@Override
	public void addListeners() {

		this.cancelButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				GermplasmGroupingComponent.super.close();
			}
		});

		this.continueButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				GermplasmGroupingComponent.this.groupGermplasm();
			}
		});
	}

	void groupGermplasm() {

		final boolean includeDescendantsChoice = this.includeDescendants.booleanValue();
		final boolean preserveExistingGroupChoice = this.preserveExistingGroupId.booleanValue();

		final Map<Integer, GermplasmGroup> allGroupingResults = new HashMap<>();
		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {

				for (final Integer gid : GermplasmGroupingComponent.this.gidsToProcess) {
					final Germplasm germplasm = GermplasmGroupingComponent.this.germplasmDataManager.getGermplasmByGID(gid);
					final GermplasmGroup report =
							GermplasmGroupingComponent.this.germplasmGroupingService.markFixed(germplasm, includeDescendantsChoice,
									preserveExistingGroupChoice);
					allGroupingResults.put(gid, report);
				}
			}
		});

		this.reportSuccessAndClose(allGroupingResults);
	}

	void reportSuccessAndClose(final Map<Integer, GermplasmGroup> groupingResults) {

		if (this.verifyIfAllEntriesHasAppliedMGIDSuccessfully(groupingResults)) {
			MessageNotifier.showMessage(this.getParent(), this.messageSource.getMessage(Message.MARK_LINES_AS_FIXED),
					this.messageSource.getMessage(Message.SUCCESS_MARK_LINES_AS_FIXED));
		} else {
			MessageNotifier.showWarning(this.getParent(), this.messageSource.getMessage(Message.MARK_LINES_AS_FIXED),
					this.messageSource.getMessage(Message.WARNING_MARK_LINES_AS_FIXED));
		}

		this.getParent().addWindow(new GermplasmGroupingResultsComponent(groupingResults));
		this.closeWindow();
	}

	/**
	 * Verify if all selected entries from list data table have its corresponding germplasm with non-generative method
	 * 
	 * @param groupingResults
	 * @return true if all selected germplasm entries have generative method, otherwise will return false
	 */
	boolean verifyIfAllEntriesHasAppliedMGIDSuccessfully(final Map<Integer, GermplasmGroup> groupingResults) {
		for (final Map.Entry<Integer, GermplasmGroup> mapEntry : groupingResults.entrySet()) {
			final GermplasmGroup groupingResult = mapEntry.getValue();
			if (groupingResult.getFounder().getMethod().isGenerative()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void layoutComponents() {
		this.setModal(true);
		this.setWidth("230px");
		this.setHeight("210px");
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

		this.dialogLayout.addComponent(this.preserveExistingGroupId);
		this.dialogLayout.addComponent(this.includeDescendants);

		this.dialogLayout.addComponent(buttonLayout);
		this.setContent(this.dialogLayout);

	}

	@Override
	public void windowClose(final CloseEvent e) {
		super.close();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this, Message.MARK_LINES_AS_FIXED);
		this.messageSource.setCaption(this.preserveExistingGroupId, Message.PRESERVE_EXISTING_GROUP);
		this.messageSource.setCaption(this.includeDescendants, Message.INCLUDE_DESCENDANTS);
		this.messageSource.setCaption(this.continueButton, Message.CONTINUE);
		this.messageSource.setCaption(this.cancelButton, Message.CANCEL);
	}

	// Setters for unit testing

	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	void setGermplasmGroupingService(final GermplasmGroupingService germplasmGroupingService) {
		this.germplasmGroupingService = germplasmGroupingService;
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	void setGidsToProcess(final Set<Integer> gidsToProcess) {
		this.gidsToProcess = gidsToProcess;
	}
}
