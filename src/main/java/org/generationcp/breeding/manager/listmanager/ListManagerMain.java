
package org.generationcp.breeding.manager.listmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesConfirmDialog;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesConfirmDialogSource;
import org.generationcp.breeding.manager.listmanager.util.BuildNewListDropHandler;
import org.generationcp.breeding.manager.listmanager.util.DropHandlerMethods.ListUpdatedEvent;
import org.generationcp.commons.help.document.HelpButton;
import org.generationcp.commons.help.document.HelpModule;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListManagerMain extends VerticalLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout,
		UnsavedChangesConfirmDialogSource {

	private static final long serialVersionUID = 5976245899964745758L;

	private static final Logger LOG = LoggerFactory.getLogger(ListManagerMain.class);

	private static final String VERSION_STRING = "<h2>1.0.0</h2>";

	private HorizontalLayout titleLayout;
	private Label toolTitle;
	public static final String BUILD_NEW_LIST_BUTTON_DATA = "Build new list";

	// Tabs
	private HorizontalLayout tabHeaderLayout;
	private Button listSelectionTabButton;
	private Button plantSelectionTabButton;

	// toggle on list
	protected Button listBuilderToggleBtn1;
	// toggle on germplasm search
	protected Button listBuilderToggleBtn2;

	// The tab content will be split between a plant finder component and a list builder component
	private HorizontalSplitPanel splitPanel;

	private AbsoluteLayout plantFinderContent;
	private ListBuilderComponent listBuilderComponent;

	// You can toggle the plant selection content to display a list view, or a germplasm view
	private ListSelectionComponent listSelectionComponent;
	private GermplasmSelectionComponent plantSelectionComponent;

	private final Integer selectedListId;

	// Handles Universal Mode View for ListManagerMain
	private ModeView modeView;
	// marks if there are unsaved changes in List from ListSelectorComponent and ListBuilderComponent
	private boolean hasChanges;
	private UnsavedChangesConfirmDialog unsavedChangesDialog;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Resource
	private ContextUtil contextUtil;

	private boolean isListBuilderShown = false;

	public ListManagerMain() {
		super();
		this.selectedListId = null;
	}

	public ListManagerMain(final Integer selectedListId) {
		super();
		this.selectedListId = selectedListId;
		this.setDebugId("ListManagerMain");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void updateLabels() {
		this.toolTitle.setValue(this.messageSource.getMessage(Message.LIST_MANAGER_SCREEN_LABEL) + "  " + ListManagerMain.VERSION_STRING);
	}

	@Override
	public void instantiateComponents() {
		this.listBuilderToggleBtn1 =
				new Button("<span class='bms-fa-chevron-left'" + "style='" + "position: relative;" + " bottom: 3px;" + "'></span>"
						+ "Show List Builder");
		this.listBuilderToggleBtn1.setDebugId("listBuilderToggleBtn1");
		
		this.listBuilderToggleBtn1.setHtmlContentAllowed(true);
		this.listBuilderToggleBtn1.setStyleName(Bootstrap.Buttons.BORDERED.styleName() + " lm-toggle");

		this.listBuilderToggleBtn2 =
				new Button("<span class='bms-fa-chevron-left'" + "style='" + "position: relative;" + " bottom: 3px;" + "'></span>"
						+ "Show List Builder");
		this.listBuilderToggleBtn2.setDebugId("listBuilderToggleBtn2");

		this.listBuilderToggleBtn2.setHtmlContentAllowed(true);
		this.listBuilderToggleBtn2.setStyleName(Bootstrap.Buttons.BORDERED.styleName() + " lm-toggle");

		this.modeView = ModeView.LIST_VIEW;
		this.hasChanges = false;

		this.setTitleContent();
		this.setTabHeader();
		this.setTabContent();
	}

	@Override
	public void initializeValues() {
		this.plantFinderContent.setWidth("100%");

		// By default, the list selection component will be opened first
		this.plantSelectionComponent.setVisible(false);
	}

	@Override
	public void addListeners() {

		this.listSelectionTabButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ListManagerMain.this.showListSelection();
				ListManagerMain.this.selectTab(ListManagerMain.this.listSelectionTabButton);
				ListManagerMain.this.deselectTab(ListManagerMain.this.plantSelectionTabButton);

			}

		});

		this.plantSelectionTabButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ListManagerMain.this.showPlantSelection();
				ListManagerMain.this.selectTab(ListManagerMain.this.plantSelectionTabButton);
				ListManagerMain.this.deselectTab(ListManagerMain.this.listSelectionTabButton);

			}
		});

		this.listBuilderToggleBtn1.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8178708255873293566L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ListManagerMain.this.toggleListBuilder();
			}
		});

		this.listBuilderToggleBtn2.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 4202348847712247508L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ListManagerMain.this.toggleListBuilder();
			}
		});
		this.listBuilderComponent.getBuildNewListDropHandler().addListener(new BuildNewListDropHandler.ListUpdatedListener() {

			@Override
			public void listUpdated(final ListUpdatedEvent event) {
				ListManagerMain.this.getPlantSelectionComponent().getSearchResultsComponent().getMatchingGermplasmTableWithSelectAll()
						.clearAllSelectedEntries();
			}
		});
	}

	@Override
	public void layoutComponents() {

		final VerticalLayout titleAndTabContainer = new VerticalLayout();
		titleAndTabContainer.setDebugId("titleAndTabContainer");
		titleAndTabContainer.setMargin(new MarginInfo(false, false, false, true));
		titleAndTabContainer.setSpacing(true);

		titleAndTabContainer.addComponent(this.titleLayout);
		titleAndTabContainer.addComponent(this.tabHeaderLayout);

		this.addComponent(titleAndTabContainer);

		final Panel splitPanelContainer = new Panel();
		splitPanelContainer.setDebugId("splitPanelContainer");
		splitPanelContainer.setScrollable(true);
		splitPanelContainer.setStyleName(Reindeer.PANEL_LIGHT + " lm-panel");

		splitPanelContainer.setContent(this.splitPanel);

		this.addComponent(splitPanelContainer);
		this.setExpandRatio(splitPanelContainer, 1.0F);

		this.setMargin(false);
		this.setSpacing(false);
	}

	/**
	 * Loads the specified list in the list builder. Ensures the list is not currently open anywhere else.
	 * 
	 * @param list the list to load for editing
	 */
	public void loadListForEditing(final GermplasmList list) {
		this.updateUIForEditingList(list);
		this.listSelectionComponent.getListDetailsLayout().repaintTabsheet();
		this.listBuilderComponent.editList(list);
		this.showListBuilder();
	}

	/**
	 * Closes the specified list from any open views.
	 * 
	 * @param list the list to close
	 */
	public void closeList(final GermplasmList list) {
		this.listSelectionComponent.getListDetailsLayout().removeTab(list.getId());
	}

	/**
	 * Add selected plants to the list open in the list builder.
	 * 
	 * @param sourceTable the table to retrieve the selected plants from
	 */
	public void addSelectedPlantsToList(final Table sourceTable) {
		this.listBuilderComponent.addFromListDataTable(sourceTable);
	}

	/**
	 * Add plants to the list open in the list builder.
	 * 
	 * @param gids list of IDs of the germplasm to add
	 */
	public void addPlantsToList(final List<Integer> gids) {
		this.listBuilderComponent.addGermplasm(gids);
	}

	public ListBuilderComponent getListBuilderComponent() {
		return this.listBuilderComponent;
	}

	public ListSelectionComponent getListSelectionComponent() {
		return this.listSelectionComponent;
	}

	public GermplasmSelectionComponent getPlantSelectionComponent() {
		return this.plantSelectionComponent;
	}

	protected void showPlantSelection() {

		this.plantFinderContent.setCaption("100%");

		this.listSelectionComponent.setVisible(false);
		this.plantSelectionComponent.setVisible(true);
		this.plantSelectionComponent.getSearchBarComponent().focusOnSearchField();

		this.plantFinderContent.requestRepaintAll();
	}

	protected void showListSelection() {

		this.plantFinderContent.setCaption("100%");

		this.listSelectionComponent.setVisible(true);
		this.plantSelectionComponent.setVisible(false);

		this.listSelectionComponent.requestRepaintAll();
	}

	private void setTitleContent() {
		this.titleLayout = new HorizontalLayout();
		this.titleLayout.setDebugId("titleLayout");
		this.titleLayout.setDebugId("titleLayout");
		this.titleLayout.setSpacing(true);
		this.titleLayout.setHeight("40px");

		this.toolTitle = new Label(this.messageSource.getMessage(Message.LIST_MANAGER_SCREEN_LABEL));
		this.toolTitle.setDebugId("toolTitle");
		this.toolTitle.setDebugId("toolTitle");

		this.toolTitle.setStyleName(Bootstrap.Typography.H1.styleName());
		this.toolTitle.setContentMode(Label.CONTENT_XHTML);
		this.toolTitle.setWidth("280px");

		this.titleLayout.addComponent(this.toolTitle);
		this.titleLayout.addComponent(new HelpButton(HelpModule.MANAGE_LIST, "View Manage Lists Tutorial"));
	}

	private void setTabHeader() {
		
		this.listSelectionTabButton = new Button(this.messageSource.getMessage(Message.VIEW_LISTS));
		this.listSelectionTabButton.setDebugId("listSelectionTabButton");
		this.listSelectionTabButton.setDebugId("listSelectionTabButton");
		
		this.plantSelectionTabButton = new Button(this.messageSource.getMessage(Message.VIEW_GERMPLASM));
		this.plantSelectionTabButton.setDebugId("plantSelectionTabButton");
		this.plantSelectionTabButton.setDebugId("plantSelectionTabButton");

		this.listSelectionTabButton.addStyleName("tabHeaderSelectedStyle");
		this.listSelectionTabButton.addStyleName("tabStyleButton");
		this.plantSelectionTabButton.addStyleName("tabStyleButton");
		this.listSelectionTabButton.setImmediate(true);
		this.plantSelectionTabButton.setImmediate(true);

		this.tabHeaderLayout = new HorizontalLayout();
		this.tabHeaderLayout.setDebugId("tabHeaderLayout");
		this.tabHeaderLayout.setDebugId("tabHeaderLayout");
		
		this.tabHeaderLayout.addStyleName("tabHeaderStyle");
		this.tabHeaderLayout.setSpacing(true);
		this.tabHeaderLayout.addComponent(this.listSelectionTabButton);
		this.tabHeaderLayout.addComponent(this.plantSelectionTabButton);
	}

	private void setTabContent() {
		this.splitPanel = new HorizontalSplitPanel();
		this.splitPanel.setDebugId("splitPanel");
		this.splitPanel.setDebugId("splitPanel");
		this.splitPanel.setMargin(false);
		this.splitPanel.setMaxSplitPosition(46.5f, Sizeable.UNITS_PERCENTAGE);
		this.splitPanel.setSplitPosition(0, Sizeable.UNITS_PERCENTAGE, true);

		this.splitPanel.setImmediate(true);
		this.splitPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
		this.splitPanel.addStyleName("tabContainerStyle");

		this.listSelectionComponent = new ListSelectionComponent(this, this.selectedListId);
		this.listSelectionComponent.setDebugId("listSelectionComponent");
		this.listSelectionComponent.setDebugId("listSelectionComponent");
		this.plantSelectionComponent = new GermplasmSelectionComponent(this);
		this.plantSelectionComponent.setDebugId("plantSelectionComponent");
		this.plantSelectionComponent.setDebugId("plantSelectionComponent");

		this.plantFinderContent = new AbsoluteLayout();
		this.plantFinderContent.setDebugId("plantFinderContent");
		this.plantFinderContent.setDebugId("plantFinderContent");
		this.plantFinderContent.addComponent(this.listSelectionComponent, "top:0px;left:0px");
		this.plantFinderContent.addComponent(this.plantSelectionComponent, "top:0px;left:0px");
		
		this.listBuilderComponent = new ListBuilderComponent(this);
		this.listBuilderComponent.setDebugId("listBuilderComponent");
		this.listBuilderComponent.setDebugId("listBuilderComponent");
		
		this.splitPanel.setFirstComponent(this.plantFinderContent);
		this.splitPanel.setSecondComponent(this.listBuilderComponent);

		this.splitPanel.setWidth("100%");
		this.splitPanel.setHeight("780px");

		this.addStyleName("lm-list-manager-main");
	}

	private void selectTab(final Button tabToSelect) {
		tabToSelect.removeStyleName("tabHeaderStyle");
		tabToSelect.addStyleName("tabHeaderSelectedStyle");
	}

	private void deselectTab(final Button tabToUnselect) {
		tabToUnselect.removeStyleName("tabHeaderSelectedStyle");
		tabToUnselect.addStyleName("tabHeaderStyle");
	}

	public void updateUIForEditingList(final GermplasmList list) {
		// Check if tab for deleted list is opened
		this.listSelectionComponent.getListDetailsLayout().removeTab(list.getId());
	}

	public void updateUIForDeletedList(final GermplasmList list) {
		SaveListAsDialog saveListAsDialog = null;

		// close the save dialog window in View list if the deleted list is the current selected list
		final ListTabComponent currentListTab =
				(ListTabComponent) this.listSelectionComponent.getListDetailsLayout().getDetailsTabsheet().getSelectedTab();
		if (currentListTab != null) {
			final ListComponent listComponent = currentListTab.getListComponent();
			saveListAsDialog = listComponent.getSaveListAsDialog();
			if (saveListAsDialog != null && listComponent.getCurrentListInSaveDialog().getName().equals(list.getName())) {
				listComponent.getWindow().removeWindow(saveListAsDialog);
				MessageNotifier
						.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS), "Germplasm List was deleted.");
			}
		}

		// Check if tab for deleted list is opened
		this.listSelectionComponent.getListDetailsLayout().removeTab(list.getId());

		// close the save dialog window in List Builder if the deleted list is the current selected list
		saveListAsDialog = this.getListBuilderComponent().getSaveListAsDialog();
		if (saveListAsDialog != null && saveListAsDialog.getGermplasmListToSave().getName().equals(list.getName())) {
			this.getListBuilderComponent().getWindow().removeWindow(saveListAsDialog);
		}

		if (this.getListBuilderComponent().getCurrentlySavedGermplasmList() != null && list != null
				&& this.getListBuilderComponent().getCurrentlySavedGermplasmList().getName().equals(list.getName())) {
			this.getListBuilderComponent().resetList();
			MessageNotifier.showMessage(this.getWindow(), this.messageSource.getMessage(Message.SUCCESS), "Germplasm List was deleted.");
		}

		// Check if deleted list is in the search results
		this.listSelectionComponent.getListSearchComponent().getSearchResultsComponent().removeSearchResult(list.getId());
	}

	public void setUIForLockedListBuilder() {
		this.plantSelectionComponent.getSearchResultsComponent().setRightClickActionHandlerEnabled(false);
		this.listSelectionComponent.getListSearchComponent().getSearchResultsComponent().refreshActionHandler();
	}

	public void setUIForUnlockedListBuilder() {
		this.plantSelectionComponent.getSearchResultsComponent().setRightClickActionHandlerEnabled(true);
		this.listSelectionComponent.getListSearchComponent().getSearchResultsComponent().refreshActionHandler();
	}

	public Boolean unlockGermplasmList(final GermplasmList germplasmList) {
		if (germplasmList.isLockedList()) {
			germplasmList.setStatus(germplasmList.getStatus() - 100);
			try {
				this.germplasmListManager.updateGermplasmList(germplasmList);

				this.contextUtil.logProgramActivity("Unlocked a germplasm list.", "Unlocked list " + germplasmList.getId() + " - "
						+ germplasmList.getName());

				return true;
			} catch (final MiddlewareQueryException e) {
				ListManagerMain.LOG.error("Error with unlocking list.", e);
				MessageNotifier.showError(this.getWindow(), "Database Error!",
						"Error with unlocking list. " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				return false;
			}
		}
		return false;
	}

	public void toggleListBuilder() {
		if (!this.isListBuilderShown) {
			this.showListBuilder();
		} else {
			this.hideListBuilder();
		}

		this.listSelectionComponent.getListDetailsLayout().repaintTabsheet();
	}

	public void showListBuilder() {
		this.splitPanel.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE, true);

		//TODO Localise button caption
		final String hideTxt =
				"<span class='bms-fa-chevron-right'" + "style='position: relative;" + " bottom: 3px;'" + "'></span>" + "Hide List Builder";

		this.listBuilderToggleBtn1.setCaption(hideTxt);
		this.listBuilderToggleBtn2.setCaption(hideTxt);

		this.isListBuilderShown = true;
	}

	public void hideListBuilder() {
		this.splitPanel.setSplitPosition(0, Sizeable.UNITS_PIXELS, true);

		//TODO Localise button caption
		final String showTxt =
				"<span class='bms-fa-chevron-left'" + "style='position: relative;" + " bottom: 3px;'" + "'></span>" + "Show List Builder";

		this.listBuilderToggleBtn1.setCaption(showTxt);
		this.listBuilderToggleBtn2.setCaption(showTxt);

		this.isListBuilderShown = false;
	}

	public Integer getListBuilderStatus() {
		if (this.listBuilderComponent != null && this.listBuilderComponent.getCurrentlySavedGermplasmList() != null) {
			return this.listBuilderComponent.getCurrentlySavedGermplasmList().getStatus();
		}
		return 0;
	}

	public Boolean listBuilderIsLocked() {
		return this.getListBuilderStatus() > 100;
	}

	public ModeView getModeView() {
		return this.modeView;
	}

	public void setModeView(final ModeView newModeView) {
		String message = "";

		if (this.modeView != newModeView) {
			if (this.hasChanges) {
				if (this.modeView.equals(ModeView.LIST_VIEW) && newModeView.equals(ModeView.INVENTORY_VIEW)) {
					message = "You have unsaved changes to one or more lists. Do you want to save them before changing views?";
					this.showUnsavedChangesConfirmDialog(message, newModeView);
				} else if (this.modeView.equals(ModeView.INVENTORY_VIEW) && newModeView.equals(ModeView.LIST_VIEW)) {
					message = "You have unsaved reservations to one or more lists. Do you want to save them before changing views?";
					this.showUnsavedChangesConfirmDialog(message, newModeView);
				}
			} else {
				this.modeView = newModeView;
				this.updateView(this.modeView);
			}
		}

	}

	public void showUnsavedChangesConfirmDialog(final String message, final ModeView newModeView) {
		this.modeView = newModeView;
		this.unsavedChangesDialog = new UnsavedChangesConfirmDialog(this, message);
		this.unsavedChangesDialog.setDebugId("unsavedChangesDialog");
		this.getWindow().addWindow(this.unsavedChangesDialog);
	}

	public void setModeViewOnly(final ModeView newModeView) {
		this.modeView = newModeView;
	}

	public void updateView(final ModeView modeView) {
		this.listSelectionComponent.getListDetailsLayout().updateViewForAllLists(modeView);

		if (modeView.equals(ModeView.INVENTORY_VIEW)) {
			this.listBuilderComponent.viewInventoryActionConfirmed();
		} else if (modeView.equals(ModeView.LIST_VIEW)) {
			this.listBuilderComponent.changeToListView();
		}

	}

	@Override
	public void saveAllListChangesAction() {

		if (this.getListSelectionComponent().getListDetailsLayout().hasUnsavedChanges()) {
			final Map<ListComponent, Boolean> listToUpdate = new HashMap<ListComponent, Boolean>();
			listToUpdate.putAll(this.listSelectionComponent.getListDetailsLayout().getListStatusForChanges());

			for (final Map.Entry<ListComponent, Boolean> list : listToUpdate.entrySet()) {
				final Boolean isListHasUnsavedChanges = list.getValue();
				if (isListHasUnsavedChanges) {
					final ListComponent toSave = list.getKey();
					// NOTE: the value of modeView here is the newModeView
					if (this.modeView.equals(ModeView.LIST_VIEW)) {
						toSave.saveReservationChangesAction(this.getWindow());
					} else if (this.modeView.equals(ModeView.INVENTORY_VIEW)) {
						toSave.saveChangesAction();
					}
				}
			}
		}

		if (this.listBuilderComponent.hasUnsavedChanges()) {
			// Save all changes in ListBuilder
			final GermplasmList currentlySavedGermplasmList = this.listBuilderComponent.getCurrentlySavedGermplasmList();
			if (currentlySavedGermplasmList == null) {
				this.listBuilderComponent.openSaveListAsDialog();
			} else {
				if (this.modeView.equals(ModeView.INVENTORY_VIEW)){
					this.listBuilderComponent.getSaveListButtonListener().doSaveAction();
					// Change ListBuilder View to List View
					this.listBuilderComponent.viewInventoryActionConfirmed();
				}
				else {
					this.listBuilderComponent.saveReservationsAction();
				}
			}
		}

		this.resetUnsavedStatus();
		this.updateView(this.modeView);

		this.getWindow().removeWindow(this.unsavedChangesDialog);
		// end of saveAllListChangesAction()
	}

	@Override
	public void discardAllListChangesAction() {
		// cancel all the unsaved changes
		if (this.modeView.equals(ModeView.LIST_VIEW)) {
			this.listSelectionComponent.getListDetailsLayout().resetInventoryViewForCancelledChanges();
		} else if (this.modeView.equals(ModeView.INVENTORY_VIEW)) {
			this.listSelectionComponent.getListDetailsLayout().resetListViewForCancelledChanges();
		}

		this.listSelectionComponent.getListDetailsLayout().updateViewForAllLists(this.modeView);

		if (this.listBuilderComponent.getCurrentlySavedGermplasmList() != null) {
			if (this.modeView.equals(ModeView.INVENTORY_VIEW)) {
				this.listBuilderComponent.discardChangesInListView();
			} else if (this.modeView.equals(ModeView.LIST_VIEW)) {
				this.listBuilderComponent.discardChangesInInventoryView();
			}
		} else {
			// if no list save, just reset the list
			this.listBuilderComponent.resetList();
		}

		this.resetUnsavedStatus();

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

	public void resetUnsavedStatus() {
		this.listSelectionComponent.getListDetailsLayout().updateHasChangesForAllList(false);
		this.listBuilderComponent.resetUnsavedChangesFlag();
	}

	public boolean hasUnsavedChanges() {
		return this.hasChanges;
	}

	public void setHasUnsavedChangesMain(final boolean hasChanges) {
		this.hasChanges = hasChanges;
	}
}
