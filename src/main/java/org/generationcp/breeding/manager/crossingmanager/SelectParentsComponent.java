
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerTreeActionsListener;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.SaveTreeStateListener;
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
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SelectParentsComponent extends VerticalLayout implements BreedingManagerLayout, InitializingBean, InternationalizableComponent,
		CrossingManagerTreeActionsListener {

	private static final long serialVersionUID = -5109231715662648484L;

	public static final String TAB_DESCRIPTION_PREFIX = "List ID: ";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private CrossingManagerMakeCrossesComponent source;

	private CrossingManagerListTreeComponent listTreeComponent;
	private Button browseForListsButton;

	private Label selectParentsLabel;
	private Label instructionForSelectParents;
	private TabSheet listDetailsTabSheet;
	private Button closeAllTabsButton;

	private Button toggleTabsheetButton;

	public SelectParentsComponent(final CrossingManagerMakeCrossesComponent source) {
		super();
		this.source = source;
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.selectParentsLabel = new Label(this.messageSource.getMessage(Message.SELECT_PARENTS));
		this.selectParentsLabel.setDebugId("selectParentsLabel");
		this.selectParentsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.selectParentsLabel.addStyleName(AppConstants.CssStyles.BOLD);
		this.selectParentsLabel.setWidth("230px");

		this.toggleTabsheetButton = new Button();
		this.toggleTabsheetButton.setDebugId("selectParentsButton");
		this.toggleTabsheetButton.setImmediate(true);
		this.toggleTabsheetButton.setVisible(true);

		this.browseForListsButton = new Button(this.messageSource.getMessage(Message.BROWSE));
		this.browseForListsButton.setDebugId("browseForListsButton");
		this.browseForListsButton.setImmediate(true);
		this.browseForListsButton.setStyleName(Reindeer.BUTTON_LINK);

		this.listTreeComponent = new CrossingManagerListTreeComponent(this);
		this.listTreeComponent.setDebugId("listTreeComponent");

		this.instructionForSelectParents = new Label("for a list to work with.");
		this.instructionForSelectParents.setDebugId("instructionForSelectParents");

		this.listDetailsTabSheet = new TabSheet();
		this.listDetailsTabSheet.setDebugId("listDetailsTabSheet");
		this.listDetailsTabSheet.setHeight("365px");
		this.listDetailsTabSheet.setWidth("890px");
		hideListDetailsTabSheet();

		this.closeAllTabsButton = new Button(this.messageSource.getMessage(Message.CLOSE_ALL_TABS));
		this.closeAllTabsButton.setDebugId("closeAllTabsButton");
		this.closeAllTabsButton.setStyleName(BaseTheme.BUTTON_LINK);
		this.closeAllTabsButton.setVisible(false);

	}

	private void hideListDetailsTabSheet() {
		this.listDetailsTabSheet.addStyleName(AppConstants.CssStyles.NO_TAB);
		this.listDetailsTabSheet.setVisible(false);
	}

	private void showListDetailsTabSheet() {
		this.listDetailsTabSheet.removeStyleName(AppConstants.CssStyles.NO_TAB);
		this.showOrHideCloseAllTabsButton();
		this.browseForListsButton.setVisible(true);
		this.instructionForSelectParents.setVisible(true);
		this.listDetailsTabSheet.setVisible(true);
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {

		listDetailsTabSheet.setCloseHandler(new CloseHandler() {

			private static final long serialVersionUID = -7085023295466691749L;

			@Override
			public void onTabClose(final TabSheet tabsheet, final Component tabContent) {
				if (tabsheet.getComponentCount() > 1) {
					final String tabCaption = tabsheet.getTab(tabContent).getCaption();
					final Tab tab = Util.getTabToFocus(tabsheet, tabCaption);
					tabsheet.removeTab(tabsheet.getTab(tabContent));
					tabsheet.setSelectedTab(tab.getComponent());
				} else {
					tabsheet.removeTab(tabsheet.getTab(tabContent));
					hideDetailsTabsheet();
				}
			}
		});

		closeAllTabsButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -2946008623293356900L;

			@Override
			public void buttonClick(final ClickEvent event) {
				Util.closeAllTab(listDetailsTabSheet);
				hideListDetailsTabSheet();
				closeAllTabsButton.setVisible(false);
			}
		});

		browseForListsButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6385074843600086746L;

			@Override
			public void buttonClick(final ClickEvent event) {
				openBrowseForListDialog();
			}
		});

		toggleTabsheetButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6385074843600086746L;


			@Override
			public void buttonClick(final ClickEvent event) {
				if (!listDetailsTabSheet.isVisible()) {
					showListDetailsTabSheet();
				} else {
					hideDetailsTabsheet();
				}
			}
		});

	}

	protected void hideDetailsTabsheet() {
		closeAllTabsButton.setVisible(false);
		hideListDetailsTabSheet();
	}

	@Override
	public void layoutComponents() {
		setWidth("100%");
		setMargin(true);

		final HeaderLabelLayout selectParentsHeaderLayout =
				new HeaderLabelLayout(AppConstants.Icons.ICON_SELECT_PARENTS, selectParentsLabel, toggleTabsheetButton);
		selectParentsHeaderLayout.setDebugId("selectParentsHeaderLayout");

		final HorizontalLayout leftLayout = new HorizontalLayout();
		leftLayout.setDebugId("leftLayout");
		leftLayout.setSpacing(true);
		leftLayout.addComponent(browseForListsButton);
		leftLayout.addComponent(instructionForSelectParents);

		final HorizontalLayout instructionForSelectParentsLayout = new HorizontalLayout();
		instructionForSelectParentsLayout.setDebugId("instructionForSelectParentsLayout");
		instructionForSelectParentsLayout.setWidth("100%");
		instructionForSelectParentsLayout.addComponent(leftLayout);
		instructionForSelectParentsLayout.addComponent(closeAllTabsButton);
		instructionForSelectParentsLayout.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);
		instructionForSelectParentsLayout.setComponentAlignment(closeAllTabsButton, Alignment.MIDDLE_RIGHT);

		addComponent(selectParentsHeaderLayout);
		addComponent(instructionForSelectParentsLayout);
		addComponent(listDetailsTabSheet);
	}

	public void selectListInTree(final Integer id) {
		this.listTreeComponent.setListId(id);
		this.listTreeComponent.createTree();
		this.listTreeComponent.setSelectedListId(id);
	}

	@Override
	public void studyClicked(final GermplasmList list) {
		createListDetailsTab(list.getId(), list.getName());
	}

	public void openBrowseForListDialog() {
		final SaveTreeStateListener saveTreeStateListener = new SaveTreeStateListener(
				(TreeTable) listTreeComponent.getGermplasmListSource(), ListTreeState.GERMPLASM_LIST.name(), ListSelectorComponent.PROGRAM_LISTS);
		listTreeComponent.showAddRenameFolderSection(false);
		listTreeComponent.reinitializeTree(false);
		launchListSelectionWindow(getWindow(), listTreeComponent, messageSource.getMessage(Message.BROWSE_FOR_LISTS))
				.addListener(saveTreeStateListener);
	}

	private Window launchListSelectionWindow(final Window window, final Component content, final String caption) {

		final CssLayout layout = new CssLayout();
		layout.setDebugId("layout");
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

	public void createListDetailsTab(final Integer listId, final String listName) {

		if (listDetailsTabSheet.getComponentCount() == 0) {
			showListDetailsTabSheet();
		}

		if (Util.isTabExist(listDetailsTabSheet, listName)) {
			Tab tabToFocus = null;
			for (int ctr = 0; ctr < listDetailsTabSheet.getComponentCount(); ctr++) {
				final Tab tab = listDetailsTabSheet.getTab(ctr);
				if (tab != null && tab.getCaption().equals(listName)) {
					tabToFocus = tab;
				}
			}
			if (tabToFocus != null) {
				listDetailsTabSheet.setSelectedTab(tabToFocus);
			}
		} else {
			final Tab newTab = listDetailsTabSheet
					.addTab(new SelectParentsListDataComponent(listId, listName, source.getParentsComponent()), listName);
			newTab.setDescription(generateTabDescription(listId));
			newTab.setClosable(true);
			listDetailsTabSheet.setSelectedTab(newTab);
		}

		this.showOrHideCloseAllTabsButton();

	}

	private void showOrHideCloseAllTabsButton() {
		if (this.listDetailsTabSheet.getComponentCount() >= 2) {
			this.closeAllTabsButton.setVisible(true);
		} else {
			this.closeAllTabsButton.setVisible(false);
		}
	}

	public void updateUIForDeletedList(final GermplasmList list) {
		final String listName = list.getName();
		for (int ctr = 0; ctr < listDetailsTabSheet.getComponentCount(); ctr++) {
			final Tab tab = listDetailsTabSheet.getTab(ctr);
			if (tab != null && tab.getCaption().equals(listName)) {
				listDetailsTabSheet.removeTab(tab);
				return;
			}
		}
	}

	public void updateUIForRenamedList(final GermplasmList list, final String newName) {
		final Integer listId = list.getId();
		final String description = generateTabDescription(listId);
		for (int ctr = 0; ctr < listDetailsTabSheet.getComponentCount(); ctr++) {
			final Tab tab = listDetailsTabSheet.getTab(ctr);
			if (tab != null && tab.getDescription().equals(description)) {
				tab.setCaption(newName);
				return;
			}
		}
	}

	@Override
	public void folderClicked(final GermplasmList list) {
		// do nothing
	}

	// SETTERS AND GETTERS
	public TabSheet getListDetailsTabSheet() {
		return listDetailsTabSheet;
	}

	public CrossingManagerListTreeComponent getListTreeComponent() {
		return this.listTreeComponent;
	}

	public void setListTreeComponent(final CrossingManagerListTreeComponent listTreeComponent) {
		this.listTreeComponent = listTreeComponent;
	}

	@Override
	public void addListToFemaleList(final Integer germplasmListId) {
		source.getParentsComponent().addListToFemaleTable(germplasmListId);

	}

	@Override
	public void addListToMaleList(final Integer germplasmListId) {
		source.getParentsComponent().addListToMaleTable(germplasmListId);
	}

	public CrossingManagerMakeCrossesComponent getCrossingManagerMakeCrossesComponent() {
		return source;
	}

	public static String generateTabDescription(final Integer listId) {
		return TAB_DESCRIPTION_PREFIX + listId;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public Button getCloseAllTabsButton(){
		return this.closeAllTabsButton;
	}
	
	public Button getBrowseForListsButton() {
		return this.browseForListsButton;
	}
	
	public Button getToggleTabsheetButton() {
		return this.toggleTabsheetButton;
	}
}
