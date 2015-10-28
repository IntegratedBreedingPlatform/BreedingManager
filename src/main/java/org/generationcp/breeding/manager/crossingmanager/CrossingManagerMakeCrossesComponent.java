
package org.generationcp.breeding.manager.crossingmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingManagerMakeCrossesComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout, StepChangeListener, UnsavedChangesConfirmDialogSource {

	public static final String NEXT_BUTTON_ID = "next button";
	public static final String BACK_BUTTON_ID = "back button";

	private static final long serialVersionUID = 9097810121003895303L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

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

	public CrossingManagerMakeCrossesComponent(ManageCrossingSettingsMain manageCrossingSettingsMain) {
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
	public void makeCrossButtonAction(List<GermplasmListEntry> femaleList, List<GermplasmListEntry> maleList, String listnameFemaleParent,
			String listnameMaleParent, CrossType type, boolean makeReciprocalCrosses, boolean excludeSelf) {

		if (!femaleList.isEmpty() && !maleList.isEmpty()) {
			// Female - Male Multiplication
			if (CrossType.MULTIPLY.equals(type)) {
				this.crossesTableComponent.multiplyParents(femaleList, maleList, listnameFemaleParent, listnameMaleParent, excludeSelf);
				if (makeReciprocalCrosses) {
					this.crossesTableComponent.multiplyParents(maleList, femaleList, listnameMaleParent, listnameFemaleParent, excludeSelf);
				}

				// Top to Bottom Crossing
			} else if (CrossType.TOP_TO_BOTTOM.equals(type)) {
				if (femaleList.size() == maleList.size()) {
					this.crossesTableComponent.makeTopToBottomCrosses(femaleList, maleList, listnameFemaleParent, listnameMaleParent,
							excludeSelf);
					if (makeReciprocalCrosses) {
						this.crossesTableComponent.makeTopToBottomCrosses(maleList, femaleList, listnameMaleParent, listnameFemaleParent,
								excludeSelf);
					}
				} else {
					MessageNotifier.showError(this.getWindow(), "Error with selecting parents.",
							this.messageSource.getMessage(Message.ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL));
				}
			}
		} else {
			MessageNotifier.showError(this.getWindow(), "Error with selecting parents.",
					this.messageSource.getMessage(Message.AT_LEAST_ONE_FEMALE_AND_ONE_MALE_PARENT_MUST_BE_SELECTED));
		}

		this.showNotificationAfterCrossing(this.crossesTableComponent.getTableCrossesMade().size());

	}

	void showNotificationAfterCrossing(int noOfCrosses) {
		if (noOfCrosses == 0) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.NO_CROSSES_GENERATED));
		}
	}

	public void toggleNextButton() {
		this.nextButton.setEnabled(this.isAllListsSaved());
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
		CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
		this.backButton.addListener(listener);
		this.nextButton.addListener(listener);
	}

	@Override
	public void layoutComponents() {
		this.setWidth("950px");
		this.setMargin(true);
		this.setSpacing(true);

		HorizontalLayout upperLayout = new HorizontalLayout();
		upperLayout.setSpacing(true);
		upperLayout.setHeight("535px");
		upperLayout.addComponent(this.selectParentsComponent);
		upperLayout.addComponent(this.parentsComponent);

		HorizontalLayout lowerLayout = new HorizontalLayout();
		lowerLayout.setSpacing(true);
		lowerLayout.addComponent(this.crossingMethodComponent);
		lowerLayout.addComponent(this.crossesTableComponent);

		HorizontalLayout layoutButtonArea = new HorizontalLayout();
		layoutButtonArea.setMargin(true, true, false, true);
		layoutButtonArea.setSpacing(true);
		layoutButtonArea.addComponent(this.backButton);
		layoutButtonArea.addComponent(this.nextButton);

		this.addComponent(upperLayout);
		this.addComponent(lowerLayout);
		this.addComponent(layoutButtonArea);

		this.setComponentAlignment(layoutButtonArea, Alignment.MIDDLE_CENTER);
		this.setStyleName("crosses-select-parents-tab");
	}

	public void updateCrossesSeedSource(String femaleListName, String maleListName) {
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
		CrossNameSetting crossNameSetting = this.getCurrentCrossingSetting().getCrossNameSetting();
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

	public void setModeViewOnly(ModeView newModeView) {
		this.modeView = newModeView;
	}

	public void setModeView(ModeView newModeView) {
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
		ParentTabComponent femaleParentTab = this.parentsComponent.getFemaleParentTab();
		ParentTabComponent maleParentTab = this.parentsComponent.getMaleParentTab();

		if (femaleParentTab.getGermplasmList() == null && femaleParentTab.hasUnsavedChanges() && maleParentTab.getGermplasmList() == null
				&& maleParentTab.hasUnsavedChanges()) {
			return true;
		}

		return false;
	}

	public void updateView(ModeView modeView) {
		this.selectParentsComponent.updateViewForAllLists(modeView);
		this.parentsComponent.updateViewForAllParentLists(modeView);
	}

	public void showUnsavedChangesConfirmDialog(String message, ModeView newModeView) {
		this.modeView = newModeView;
		this.unsavedChangesDialog = new UnsavedChangesConfirmDialog(this, message);
		this.getWindow().addWindow(this.unsavedChangesDialog);
	}

	@Override
	public void saveAllListChangesAction() {

		if (this.selectParentsComponent.hasUnsavedChanges()) {
			Map<SelectParentsListDataComponent, Boolean> listToUpdate = new HashMap<SelectParentsListDataComponent, Boolean>();
			listToUpdate.putAll(this.selectParentsComponent.getListStatusForChanges());

			for (Map.Entry<SelectParentsListDataComponent, Boolean> list : listToUpdate.entrySet()) {
				Boolean isListHasUnsavedChanges = list.getValue();
				if (isListHasUnsavedChanges) {
					SelectParentsListDataComponent toSave = list.getKey();
					toSave.saveReservationChangesAction();
				}
			}
		}

		if (this.parentsComponent.hasUnsavedChanges()) {
			// for female and male parent lists
			ParentTabComponent femaleParentTab = this.parentsComponent.getFemaleParentTab();
			ParentTabComponent maleParentTab = this.parentsComponent.getMaleParentTab();

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
		ParentTabComponent femaleTabComponent = this.parentsComponent.getFemaleParentTab();
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
		ParentTabComponent maleTabComponent = this.parentsComponent.getMaleParentTab();
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

	public void setHasUnsavedChangesMain(boolean hasChanges) {
		this.hasChanges = hasChanges;
	}

	public Boolean hasUnsavedChangesMain() {
		return this.hasChanges;
	}

	public void showNodeOnTree(Integer listId) {
		CrossingManagerListTreeComponent listTreeComponent = this.getSelectParentsComponent().getListTreeComponent();
		listTreeComponent.setListId(listId);
		listTreeComponent.createTree();
	}

	void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
