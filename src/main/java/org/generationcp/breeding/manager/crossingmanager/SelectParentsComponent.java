
package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerTreeActionsListener;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesSource;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SelectParentsComponent extends VerticalLayout implements BreedingManagerLayout, InitializingBean,
		InternationalizableComponent, CrossingManagerTreeActionsListener, UnsavedChangesSource {

	private static final long serialVersionUID = -5109231715662648484L;

	public static final String TAB_DESCRIPTION_PREFIX = "List ID: ";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private final CrossingManagerMakeCrossesComponent source;

	private CrossingManagerListTreeComponent listTreeComponent;
	private Button browseForListsButton;

	private Label selectParentsLabel;
	private Label instructionForSelectParents;
	private TabSheet listDetailsTabSheet;
	private Button closeAllTabsButton;

	private Map<SelectParentsListDataComponent, Boolean> listStatusForChanges;

	public SelectParentsComponent(CrossingManagerMakeCrossesComponent source) {
		super();
		this.source = source;
	}

	@Override
	public void updateLabels() {
		// do nothing
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
		this.selectParentsLabel = new Label(this.messageSource.getMessage(Message.SELECT_PARENTS));
		this.selectParentsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.selectParentsLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.browseForListsButton = new Button(this.messageSource.getMessage(Message.BROWSE));
		this.browseForListsButton.setImmediate(true);
		this.browseForListsButton.setStyleName(BaseTheme.BUTTON_LINK);

		this.listTreeComponent = new CrossingManagerListTreeComponent(this, this.source);

		this.instructionForSelectParents = new Label("for a list to work with.");

		this.listDetailsTabSheet = new TabSheet();
		this.listDetailsTabSheet.setWidth("460px");
		this.listDetailsTabSheet.setHeight("465px");
		this.hideListDetailsTabSheet();

		this.closeAllTabsButton = new Button(this.messageSource.getMessage(Message.CLOSE_ALL_TABS));
		this.closeAllTabsButton.setStyleName(BaseTheme.BUTTON_LINK);
		this.closeAllTabsButton.setVisible(false);

		this.listStatusForChanges = new HashMap<SelectParentsListDataComponent, Boolean>();
	}

	private void hideListDetailsTabSheet() {
		this.listDetailsTabSheet.addStyleName(AppConstants.CssStyles.NO_TAB);
	}

	private void showListDetailsTabSheet() {
		this.listDetailsTabSheet.removeStyleName(AppConstants.CssStyles.NO_TAB);
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {

		this.listDetailsTabSheet.setCloseHandler(new CloseHandler() {

			private static final long serialVersionUID = -7085023295466691749L;

			@Override
			public void onTabClose(TabSheet tabsheet, Component tabContent) {
				if (tabsheet.getComponentCount() > 1) {
					String tabCaption = tabsheet.getTab(tabContent).getCaption();
					Tab tab = Util.getTabToFocus(tabsheet, tabCaption);
					tabsheet.removeTab(tabsheet.getTab(tabContent));
					tabsheet.setSelectedTab(tab.getComponent());
				} else {
					tabsheet.removeTab(tabsheet.getTab(tabContent));
					SelectParentsComponent.this.hideDetailsTabsheet();
				}
			}
		});

		this.closeAllTabsButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -2946008623293356900L;

			@Override
			public void buttonClick(ClickEvent event) {
				Util.closeAllTab(SelectParentsComponent.this.listDetailsTabSheet);
				SelectParentsComponent.this.hideListDetailsTabSheet();
				SelectParentsComponent.this.closeAllTabsButton.setVisible(false);
			}
		});

		this.browseForListsButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6385074843600086746L;

			@Override
			public void buttonClick(final ClickEvent event) {
				SelectParentsComponent.this.openBrowseForListDialog();
			}
		});
	}

	protected void hideDetailsTabsheet() {
		this.closeAllTabsButton.setVisible(false);
		this.hideListDetailsTabSheet();
	}

	@Override
	public void layoutComponents() {

		this.setSpacing(true);
		this.setWidth("460px");

		HeaderLabelLayout selectParentsHeaderLayout =
				new HeaderLabelLayout(AppConstants.Icons.ICON_SELECT_PARENTS, this.selectParentsLabel);

		HorizontalLayout leftLayout = new HorizontalLayout();
		leftLayout.setSpacing(true);
		leftLayout.addComponent(this.browseForListsButton);
		leftLayout.addComponent(this.instructionForSelectParents);

		HorizontalLayout instructionForSelectParentsLayout = new HorizontalLayout();
		instructionForSelectParentsLayout.setWidth("100%");
		instructionForSelectParentsLayout.addComponent(leftLayout);
		instructionForSelectParentsLayout.addComponent(this.closeAllTabsButton);
		instructionForSelectParentsLayout.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);
		instructionForSelectParentsLayout.setComponentAlignment(this.closeAllTabsButton, Alignment.MIDDLE_RIGHT);

		this.addComponent(selectParentsHeaderLayout);
		this.addComponent(instructionForSelectParentsLayout);
		this.addComponent(this.listDetailsTabSheet);
	}

	public void selectListInTree(Integer id) {
		this.listTreeComponent.setListId(id);
		this.listTreeComponent.createTree();
		this.listTreeComponent.setSelectedListId(id);
	}

	@Override
	public void studyClicked(GermplasmList list) {
		this.createListDetailsTab(list.getId(), list.getName());
	}

	public void openBrowseForListDialog() {
		this.listTreeComponent.showAddRenameFolderSection(false);
		this.launchListSelectionWindow(this.getWindow(), this.listTreeComponent, this.messageSource.getMessage(Message.BROWSE_FOR_LISTS));
	}

	private Window launchListSelectionWindow(final Window window, final Component content, final String caption) {

		final CssLayout layout = new CssLayout();
		layout.setMargin(true);
		layout.setWidth("100%");
		layout.setHeight("515px");

		layout.addComponent(content);

		final Window popupWindow = new BaseSubWindow();
		popupWindow.setWidth("900px");
		popupWindow.setHeight("575px");
		popupWindow.setModal(true);
		popupWindow.setResizable(false);
		popupWindow.center();
		popupWindow.setCaption(caption);
		popupWindow.setContent(layout);
		popupWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		popupWindow.addStyleName("lm-list-manager-popup");
		popupWindow.setCloseShortcut(KeyCode.ESCAPE, null);
		popupWindow.setScrollable(false);

		window.addWindow(popupWindow);

		return popupWindow;
	}

	public void createListDetailsTab(Integer listId, String listName) {

		if (this.listDetailsTabSheet.getComponentCount() == 0) {
			this.showListDetailsTabSheet();
		}

		if (Util.isTabExist(this.listDetailsTabSheet, listName)) {
			Tab tabToFocus = null;
			for (int ctr = 0; ctr < this.listDetailsTabSheet.getComponentCount(); ctr++) {
				Tab tab = this.listDetailsTabSheet.getTab(ctr);
				if (tab != null && tab.getCaption().equals(listName)) {
					tabToFocus = tab;
				}
			}
			if (tabToFocus != null) {
				this.listDetailsTabSheet.setSelectedTab(tabToFocus);
			}
		} else {
			Tab newTab =
					this.listDetailsTabSheet.addTab(
							new SelectParentsListDataComponent(listId, listName, this.source.getParentsComponent()), listName);
			newTab.setDescription(SelectParentsComponent.generateTabDescription(listId));
			newTab.setClosable(true);
			this.listDetailsTabSheet.setSelectedTab(newTab);
		}

		if (this.listDetailsTabSheet.getComponentCount() >= 2) {
			this.closeAllTabsButton.setVisible(true);
		} else {
			this.closeAllTabsButton.setVisible(false);
		}

	}

	public void updateUIForDeletedList(GermplasmList list) {
		String listName = list.getName();
		for (int ctr = 0; ctr < this.listDetailsTabSheet.getComponentCount(); ctr++) {
			Tab tab = this.listDetailsTabSheet.getTab(ctr);
			if (tab != null && tab.getCaption().equals(listName)) {
				this.listDetailsTabSheet.removeTab(tab);
				return;
			}
		}
	}

	@Override
	public void updateUIForRenamedList(GermplasmList list, String newName) {
		Integer listId = list.getId();
		String description = SelectParentsComponent.generateTabDescription(listId);
		for (int ctr = 0; ctr < this.listDetailsTabSheet.getComponentCount(); ctr++) {
			Tab tab = this.listDetailsTabSheet.getTab(ctr);
			if (tab != null && tab.getDescription().equals(description)) {
				tab.setCaption(newName);
				return;
			}
		}
	}

	@Override
	public void folderClicked(GermplasmList list) {
		// do nothing
	}

	// SETTERS AND GETTERS
	public TabSheet getListDetailsTabSheet() {
		return this.listDetailsTabSheet;
	}

	public CrossingManagerListTreeComponent getListTreeComponent() {
		return this.listTreeComponent;
	}

	@Override
	public void addListToFemaleList(Integer germplasmListId) {
		this.source.getParentsComponent().addListToFemaleTable(germplasmListId);

	}

	@Override
	public void addListToMaleList(Integer germplasmListId) {
		this.source.getParentsComponent().addListToMaleTable(germplasmListId);
	}

	public void updateViewForAllLists(ModeView modeView) {
		List<SelectParentsListDataComponent> selectParentComponents = new ArrayList<SelectParentsListDataComponent>();
		selectParentComponents.addAll(this.listStatusForChanges.keySet());

		if (modeView.equals(ModeView.LIST_VIEW)) {
			for (SelectParentsListDataComponent selectParentComponent : selectParentComponents) {
				selectParentComponent.changeToListView();
			}
		} else if (modeView.equals(ModeView.INVENTORY_VIEW)) {
			for (SelectParentsListDataComponent selectParentComponent : selectParentComponents) {
				selectParentComponent.viewInventoryActionConfirmed();
			}
		}
	}

	public Map<SelectParentsListDataComponent, Boolean> getListStatusForChanges() {
		return this.listStatusForChanges;
	}

	public void addUpdateListStatusForChanges(SelectParentsListDataComponent selectParentsListDataComponent, boolean hasChanges) {
		this.removeListStatusForChanges(selectParentsListDataComponent);
		this.listStatusForChanges.put(selectParentsListDataComponent, hasChanges);

		if (this.hasUnsavedChanges()) {
			this.setHasUnsavedChangesMain(true);
		} else {
			this.setHasUnsavedChangesMain(false);
		}
	}

	public boolean hasUnsavedChanges() {
		List<Boolean> listOfStatus = new ArrayList<Boolean>();

		listOfStatus.addAll(this.listStatusForChanges.values());

		for (Boolean status : listOfStatus) {
			if (status) {
				return true;
			}
		}

		return false;
	}

	public void removeListStatusForChanges(SelectParentsListDataComponent selectParentsListDataComponent) {
		if (this.listStatusForChanges.containsKey(selectParentsListDataComponent)) {
			this.listStatusForChanges.remove(selectParentsListDataComponent);
		}
	}

	@Override
	public void setHasUnsavedChangesMain(boolean hasChanges) {
		this.source.setHasUnsavedChangesMain(hasChanges);
	}

	public void updateHasChangesForAllList(boolean hasChanges) {
		List<SelectParentsListDataComponent> selectParentComponents = new ArrayList<SelectParentsListDataComponent>();
		selectParentComponents.addAll(this.listStatusForChanges.keySet());

		for (SelectParentsListDataComponent selectParentComponent : selectParentComponents) {
			selectParentComponent.setHasUnsavedChanges(hasChanges);
		}
	}

	public CrossingManagerMakeCrossesComponent getCrossingManagerMakeCrossesComponent() {
		return this.source;
	}

	public void resetInventoryViewForCancelledChanges() {
		List<SelectParentsListDataComponent> listDataComponents = new ArrayList<SelectParentsListDataComponent>();
		listDataComponents.addAll(this.listStatusForChanges.keySet());

		for (SelectParentsListDataComponent listDataComponent : listDataComponents) {
			if (listDataComponent.hasUnsavedChanges()) {
				listDataComponent.resetListInventoryTableValues();
			}
		}
	}

	public static String generateTabDescription(Integer listId) {
		return SelectParentsComponent.TAB_DESCRIPTION_PREFIX + listId;
	}
}
