
package org.generationcp.breeding.manager.crossingmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.constants.CrossType;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.breeding.manager.customcomponent.BreedingManagerWizardDisplay.StepChangeListener;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesConfirmDialog;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesConfirmDialogSource;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingManagerMakeCrossesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout, StepChangeListener, UnsavedChangesConfirmDialogSource {

	private static final Logger LOG = LoggerFactory.getLogger(CrossingManagerMakeCrossesComponent.class);

	public static final String NEXT_BUTTON_ID = "next button";
	public static final String BACK_BUTTON_ID = "back button";

	private static final long serialVersionUID = 9097810121003895303L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private final ManageCrossingSettingsMain source;

	private Button backButton;
	private Button nextButton;

	private SelectParentsComponent selectParentsComponent;
	private MakeCrossesParentsComponent parentsComponent;
	private CrossingMethodComponent crossingMethodComponent;
	private MakeCrossesTableComponent crossesTableComponent;

	// Handles Universal Mode View for ListManagerMain
	private ModeView modeView;
	// marks if there are unsaved changes in List from ListSelectorComponent and ListBuilderComponent
	private boolean hasChanges;
	private UnsavedChangesConfirmDialog unsavedChangesDialog;
	private Link nurseryLink;

	public CrossingManagerMakeCrossesComponent(final ManageCrossingSettingsMain manageCrossingSettingsMain) {
		this.source = manageCrossingSettingsMain;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.backButton, Message.BACK);
		this.messageSource.setCaption(this.nextButton, Message.NEXT);
	}

	/*
	 * Action handler for Make Cross button
	 */
	public void makeCrossButtonAction(final List<GermplasmListEntry> femaleList, final List<GermplasmListEntry> maleList,
			final String listnameFemaleParent, final String listnameMaleParent, final CrossType type, final boolean makeReciprocalCrosses,
			final boolean excludeSelf) {

		if (!femaleList.isEmpty() && !maleList.isEmpty()) {
			try {
				final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(final TransactionStatus status) {
						// Female - Male Multiplication
						if (CrossType.MULTIPLY.equals(type)) {
							CrossingManagerMakeCrossesComponent.this.crossesTableComponent.multiplyParents(femaleList, maleList,
									listnameFemaleParent, listnameMaleParent, excludeSelf);
							if (makeReciprocalCrosses) {
								CrossingManagerMakeCrossesComponent.this.crossesTableComponent.multiplyParents(maleList, femaleList,
										listnameMaleParent, listnameFemaleParent, excludeSelf);
							}

							// Top to Bottom Crossing
						} else if (CrossType.TOP_TO_BOTTOM.equals(type)) {
							if (femaleList.size() == maleList.size()) {
								CrossingManagerMakeCrossesComponent.this.crossesTableComponent.makeTopToBottomCrosses(femaleList, maleList,
										listnameFemaleParent, listnameMaleParent, excludeSelf);
								if (makeReciprocalCrosses) {
									CrossingManagerMakeCrossesComponent.this.crossesTableComponent.makeTopToBottomCrosses(maleList,
											femaleList, listnameMaleParent, listnameFemaleParent, excludeSelf);
								}
							} else {
								MessageNotifier.showError(CrossingManagerMakeCrossesComponent.this.getWindow(),
										"Error with selecting parents.", CrossingManagerMakeCrossesComponent.this.messageSource
												.getMessage(Message.ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL));
							}
						}
					}
				});

			} catch (final Throwable e) {
				CrossingManagerMakeCrossesComponent.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(CrossingManagerMakeCrossesComponent.this.getWindow(),
						this.messageSource.getMessage(Message.ERROR),
						CrossingManagerMakeCrossesComponent.this.messageSource.getMessage(Message.ERROR_WITH_CROSSES_RETRIEVAL));
			}

		} else {
			MessageNotifier.showError(CrossingManagerMakeCrossesComponent.this.getWindow(), "Error with selecting parents.",
					CrossingManagerMakeCrossesComponent.this.messageSource
							.getMessage(Message.AT_LEAST_ONE_FEMALE_AND_ONE_MALE_PARENT_MUST_BE_SELECTED));
		}

		CrossingManagerMakeCrossesComponent.this
				.showNotificationAfterCrossing(CrossingManagerMakeCrossesComponent.this.crossesTableComponent.getTableCrossesMade().size());

	}

	void showNotificationAfterCrossing(final int noOfCrosses) {
		if (noOfCrosses == 0) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.NO_CROSSES_GENERATED));
		}
	}

	public void toggleNextButton() {
		this.nextButton.setEnabled(this.isAllListsSaved());
	}

	public void updateNurseryLink(final Integer id) {
		if( null == id) {
			return;
		}
		final String oldNurseryLinkUrl = ((ExternalResource)this.nurseryLink.getResource()).getURL();
		final ExternalResource urlToNursery = new ExternalResource(oldNurseryLinkUrl + "?" + BreedingManagerApplication
				.REQ_PARAM_CROSSES_LIST_ID + "=" + id);
		this.nurseryLink.setResource(urlToNursery);
	}

	private boolean isAllListsSaved() {
		return this.parentsComponent.getFemaleList() != null && this.parentsComponent.getMaleList() != null
				&& this.crossesTableComponent.getCrossList() != null;
	}

	public void nextButtonClickAction() {
		if (this.crossesTableComponent.getCrossList() != null) {
			this.source.viewGermplasmListCreated(this.crossesTableComponent.getCrossList(), this.parentsComponent.getFemaleList(),
					this.parentsComponent.getMaleList());
		}
	}

	public void backButtonClickAction() {
		if (this.crossesTableComponent.getCrossList() != null) {
			MessageNotifier.showWarning(this.getWindow(), "Invalid Action", "Cannot change settings once crosses have been saved");
			return;
		}

		if (this.source != null) {
			this.source.backStep();
		}
	}

	public void disableNextButton() {
		this.nextButton.setEnabled(false);
	}

	@Override
	public void instantiateComponents() {
		this.selectParentsComponent = new SelectParentsComponent(this);
		this.parentsComponent = new MakeCrossesParentsComponent(this);
		this.crossingMethodComponent = new CrossingMethodComponent(this);
		this.crossesTableComponent = new MakeCrossesTableComponent(this);

		this.backButton = new Button();
		this.backButton.setData(CrossingManagerMakeCrossesComponent.BACK_BUTTON_ID);
		this.backButton.setWidth("80px");

		this.nextButton = new Button();
		this.nextButton.setData(CrossingManagerMakeCrossesComponent.NEXT_BUTTON_ID);
		this.nextButton.setWidth("80px");
		this.nextButton.setEnabled(false);
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.modeView = ModeView.LIST_VIEW;
		this.hasChanges = false;
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		final CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
		this.backButton.addListener(listener);
		this.nextButton.addListener(listener);
	}

	@Override
	public void layoutComponents() {
		this.setWidth("950px");
		this.setMargin(true);
		this.setSpacing(true);

		final HorizontalLayout upperLayout = new HorizontalLayout();
		upperLayout.setSpacing(true);
		upperLayout.setHeight("535px");
		upperLayout.addComponent(this.selectParentsComponent);
		upperLayout.addComponent(this.parentsComponent);

		final HorizontalLayout lowerLayout = new HorizontalLayout();
		lowerLayout.setSpacing(true);
		lowerLayout.addComponent(this.crossingMethodComponent);
		lowerLayout.addComponent(this.crossesTableComponent);

		final HorizontalLayout layoutButtonArea = new HorizontalLayout();
		layoutButtonArea.setMargin(true, true, false, true);
		layoutButtonArea.setSpacing(true);
		layoutButtonArea.addComponent(this.backButton);
		layoutButtonArea.addComponent(this.nextButton);

		// show the link to navigate back to the Crossing Manager only if we came from the Nursery Manager previously
		final boolean isNavigatedFromNursery = BreedingManagerUtil.getApplicationRequest().getPathInfo().contains(BreedingManagerApplication
				.NAVIGATION_FROM_NURSERY_PREFIX);
		if (isNavigatedFromNursery) {
			this.nurseryLink = this.constructLinkToNursery(BreedingManagerUtil.getApplicationRequest());
			layoutButtonArea.addComponent(this.nurseryLink);
		}

		this.addComponent(upperLayout);
		this.addComponent(lowerLayout);
		this.addComponent(layoutButtonArea);

		this.setComponentAlignment(layoutButtonArea, Alignment.MIDDLE_CENTER);
		this.setStyleName("crosses-select-parents-tab");
	}

	protected Link constructLinkToNursery(final HttpServletRequest currentRequest) {
		final String nurseryId = currentRequest
				.getParameterValues(BreedingManagerApplication.REQ_PARAM_NURSERY_ID).length > 0 ?
				currentRequest.getParameterValues(BreedingManagerApplication.REQ_PARAM_NURSERY_ID)[0] : "";
		final ExternalResource urlToNursery;
		if (nurseryId.isEmpty() || !NumberUtils.isDigits(nurseryId)) {
			urlToNursery = new ExternalResource("http://" + currentRequest.getServerName() + ":" + currentRequest.getServerPort()
					+ BreedingManagerApplication.PATH_TO_NURSERY);
		} else {
			urlToNursery = new ExternalResource("http://" + currentRequest.getServerName() + ":" + currentRequest.getServerPort()
					+ BreedingManagerApplication.PATH_TO_EDIT_NURSERY + nurseryId);
		}
		final Link backToNurseryLink = new Link("", urlToNursery);
		backToNurseryLink.setData("nursery back button");
		this.messageSource.setCaption(backToNurseryLink, Message.BACK_TO_NURSERY);
		return backToNurseryLink;
	}

	public void updateCrossesSeedSource(final String femaleListName, final String maleListName) {
		this.crossesTableComponent.updateSeedSource(femaleListName, maleListName);
	}

	private boolean doUpdateTable() {
		return !this.getSeparatorString().equals(this.crossesTableComponent.getSeparator());
	}

	@Override
	public void updatePage() {
		// only make updates to the page if separator was changed
		if (this.doUpdateTable() && this.crossesTableComponent.getCrossList() == null) {
			this.crossesTableComponent.updateSeparatorForCrossesMade();
		}
	}

	// SETTERS AND GETTERS
	public String getSeparatorString() {
		final CrossNameSetting crossNameSetting = this.getCurrentCrossingSetting().getCrossNameSetting();
		return crossNameSetting.getSeparator();
	}

	public CrossingManagerSetting getCurrentCrossingSetting() {
		return this.source.getDetailComponent().getCurrentlyDefinedSetting();
	}

	public CrossesMadeContainer getCrossesMadeContainer() {
		return this.source;
	}

	public SelectParentsComponent getSelectParentsComponent() {
		return this.selectParentsComponent;
	}

	public void setSelectParentsComponent(SelectParentsComponent selectParentsComponent) {
		this.selectParentsComponent = selectParentsComponent;
	}

	public MakeCrossesParentsComponent getParentsComponent() {
		return this.parentsComponent;
	}

	public void setParentsComponent(final MakeCrossesParentsComponent parentsComponent) {
		this.parentsComponent = parentsComponent;
	}

	public CrossingMethodComponent getCrossingMethodComponent() {
		return this.crossingMethodComponent;
	}

	public MakeCrossesTableComponent getCrossesTableComponent() {
		return this.crossesTableComponent;
	}

	public void setCrossesTableComponent(final MakeCrossesTableComponent crossesTableComponent) {
		this.crossesTableComponent = crossesTableComponent;
	}

	public Component getSource() {
		return this.source;
	}

	public ModeView getModeView() {
		return this.modeView;
	}

	public void setModeViewOnly(final ModeView newModeView) {
		this.modeView = newModeView;
	}

	public void setModeView(final ModeView newModeView) {
		String message = "";

		if (this.modeView != newModeView) {
			if (this.hasChanges) {
				if (this.modeView.equals(ModeView.LIST_VIEW) && newModeView.equals(ModeView.INVENTORY_VIEW)) {
					message =
							"You have unsaved changes to a parent list you are creating."
									+ " Do you want to save them before changing views?";
				} else if (this.modeView.equals(ModeView.INVENTORY_VIEW) && newModeView.equals(ModeView.LIST_VIEW)) {
					message = "You have unsaved reservations to one or more lists. Do you want to save them before changing views?";
				}
				// both parents are not yet saved and has unsaved changes
				if (this.areBothParentsNewListWithUnsavedChanges()) {
					MessageNotifier.showError(this.getWindow(), "Unsaved Parent Lists",
							"Please save parent lists first before changing view.");
				} else {
					this.showUnsavedChangesConfirmDialog(message, newModeView);
				}
			} else {
				this.modeView = newModeView;
				this.updateView(this.modeView);
			}
		}

	}

	public boolean areBothParentsNewListWithUnsavedChanges() {
		// for female and male parent lists
		final ParentTabComponent femaleParentTab = this.parentsComponent.getFemaleParentTab();
		final ParentTabComponent maleParentTab = this.parentsComponent.getMaleParentTab();

		if (femaleParentTab.getGermplasmList() == null && femaleParentTab.hasUnsavedChanges() && maleParentTab.getGermplasmList() == null
				&& maleParentTab.hasUnsavedChanges()) {
			return true;
		}

		return false;
	}

	public void updateView(final ModeView modeView) {
		this.selectParentsComponent.updateViewForAllLists(modeView);
		this.parentsComponent.updateViewForAllParentLists(modeView);
	}

	public void showUnsavedChangesConfirmDialog(final String message, final ModeView newModeView) {
		this.modeView = newModeView;
		this.unsavedChangesDialog = new UnsavedChangesConfirmDialog(this, message);
		this.getWindow().addWindow(this.unsavedChangesDialog);
	}

	@Override
	public void saveAllListChangesAction() {

		if (this.selectParentsComponent.hasUnsavedChanges()) {
			final Map<SelectParentsListDataComponent, Boolean> listToUpdate = new HashMap<SelectParentsListDataComponent, Boolean>();
			listToUpdate.putAll(this.selectParentsComponent.getListStatusForChanges());

			for (final Map.Entry<SelectParentsListDataComponent, Boolean> list : listToUpdate.entrySet()) {
				final Boolean isListHasUnsavedChanges = list.getValue();
				if (isListHasUnsavedChanges) {
					final SelectParentsListDataComponent toSave = list.getKey();
					toSave.saveReservationChangesAction();
				}
			}
		}

		if (this.parentsComponent.hasUnsavedChanges()) {
			// for female and male parent lists
			final ParentTabComponent femaleParentTab = this.parentsComponent.getFemaleParentTab();
			final ParentTabComponent maleParentTab = this.parentsComponent.getMaleParentTab();

			ModeView prevModeView;
			if (this.modeView.equals(ModeView.LIST_VIEW)) {
				prevModeView = ModeView.INVENTORY_VIEW;
			} else {
				prevModeView = ModeView.LIST_VIEW;
			}

			if (femaleParentTab.hasUnsavedChanges() && !maleParentTab.hasUnsavedChanges()) {
				femaleParentTab.setPreviousModeView(prevModeView);
				femaleParentTab.doSaveActionFromMain();
			} else if (maleParentTab.hasUnsavedChanges() && !femaleParentTab.hasUnsavedChanges()) {
				maleParentTab.setPreviousModeView(prevModeView);
				maleParentTab.doSaveActionFromMain();
			} else {
				// keep track the unsaved changes due to reservation and dragging lots given that the parents have existing lists.
				if (femaleParentTab.getGermplasmList() != null && maleParentTab.getGermplasmList() != null) {

					femaleParentTab.setPreviousModeView(prevModeView);
					femaleParentTab.doSaveActionFromMain();

					maleParentTab.setPreviousModeView(prevModeView);
					maleParentTab.doSaveActionFromMain();
				}
			}
		} else {
			this.updateView(this.modeView);
		}

		this.resetUnsavedStatus();

		this.getWindow().removeWindow(this.unsavedChangesDialog);
	}

	private void resetUnsavedStatus() {
		this.selectParentsComponent.updateHasChangesForAllList(false);
		this.parentsComponent.updateHasChangesForAllParentList();
	}

	@Override
	public void discardAllListChangesAction() {
		// cancel all the unsaved changes
		if (this.modeView.equals(ModeView.INVENTORY_VIEW)) {
			this.selectParentsComponent.resetInventoryViewForCancelledChanges();
		}
		this.selectParentsComponent.updateViewForAllLists(this.modeView);

		// for female parent list
		final ParentTabComponent femaleTabComponent = this.parentsComponent.getFemaleParentTab();
		if (femaleTabComponent.getGermplasmList() != null) {
			if (this.modeView.equals(ModeView.INVENTORY_VIEW)) {
				femaleTabComponent.discardChangesInListView();
			} else if (this.modeView.equals(ModeView.LIST_VIEW)) {
				femaleTabComponent.discardChangesInInventoryView();
			}
		} else {
			// if no list save, just reset the list
			femaleTabComponent.resetList();
		}

		// for male parent list
		final ParentTabComponent maleTabComponent = this.parentsComponent.getMaleParentTab();
		if (maleTabComponent.getGermplasmList() != null) {
			if (this.modeView.equals(ModeView.INVENTORY_VIEW)) {
				maleTabComponent.discardChangesInListView();
			} else if (this.modeView.equals(ModeView.LIST_VIEW)) {
				maleTabComponent.discardChangesInInventoryView();
			}
		} else {
			// if no list save, just reset the list
			maleTabComponent.resetList();
		}

		this.resetUnsavedStatus();
		this.updateView(this.modeView);
		this.getWindow().removeWindow(this.unsavedChangesDialog);
		// end of discardAllListChangesAction()
	}

	@Override
	public void cancelAllListChangesAction() {

		// Return to Previous Mode View
		if (this.modeView.equals(ModeView.LIST_VIEW)) {
			this.setModeViewOnly(ModeView.INVENTORY_VIEW);
		} else if (this.modeView.equals(ModeView.INVENTORY_VIEW)) {
			this.setModeViewOnly(ModeView.LIST_VIEW);
		}

		this.getWindow().removeWindow(this.unsavedChangesDialog);
		// end of cancelAllListChangesAction()
	}

	public void setHasUnsavedChangesMain(final boolean hasChanges) {
		this.hasChanges = hasChanges;
	}

	public Boolean hasUnsavedChangesMain() {
		return this.hasChanges;
	}

	public void showNodeOnTree(final Integer listId) {
		final CrossingManagerListTreeComponent listTreeComponent = this.getSelectParentsComponent().getListTreeComponent();
		listTreeComponent.setListId(listId);
		listTreeComponent.createTree();
	}

	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
