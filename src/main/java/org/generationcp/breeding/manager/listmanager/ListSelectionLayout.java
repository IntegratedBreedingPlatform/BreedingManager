/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.UnsavedChangesSource;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.ListManagerDetailsTabCloseHandler;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author Mark Agarrado
 */
@Configurable
public class ListSelectionLayout extends VerticalLayout
		implements InternationalizableComponent, InitializingBean, BreedingManagerLayout, UnsavedChangesSource {

	private static final Logger LOG = LoggerFactory.getLogger(ListSelectionLayout.class);
	private static final long serialVersionUID = -6583178887344009055L;

	public static final String CLOSE_ALL_TABS_ID = "ListManagerDetailsLayout Close All Tabs ID";
	public static final String TAB_DESCRIPTION_PREFIX = "List ID: ";

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private UserDataManager userDataManager;

	private final ListManagerMain source;

	private Label headingLabel;
	private Label noListLabel;

	private Button btnCloseAllTabs;
	private Button browseForLists;
	private Button searchForLists;
	private Label or;
	private Label toWorkWith;

	private HorizontalLayout headerLayout;
	private HorizontalLayout listSelectionHeaderContainer;
	private HorizontalLayout searchOrBrowseContainer;

	private TabSheet detailsTabSheet;
	private Map<ListComponent, Boolean> listStatusForChanges;

	private final Integer listId;



	public ListSelectionLayout(final ListManagerMain source, final Integer listId) {
		super();
		this.source = source;
		this.listId = listId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.layoutComponents();
		this.addListeners();
		this.openRequestListIds();

		if (this.listId != null) {
			try {
				this.createListDetailsTab(this.listId);
			} catch (final MiddlewareQueryException ex) {
				ListSelectionLayout.LOG.error("Error with opening list details tab of list with id: " + this.listId,ex);
			}
		} else {
			this.displayDefault();
		}
	}

	@Override
	public void instantiateComponents() {

		this.noListLabel = new Label();
		this.noListLabel.setDebugId("noListLabel");
		this.noListLabel.setImmediate(true);

		this.headingLabel = new Label();
		this.headingLabel.setDebugId("headingLabel");
		this.headingLabel.setImmediate(true);
		this.headingLabel.setWidth("300px");
		this.headingLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.headingLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.headerLayout = new HorizontalLayout();
		this.headerLayout.setDebugId("headerLayout");

		this.detailsTabSheet = new TabSheet();
		this.detailsTabSheet.setDebugId("detailsTabSheet");
		this.detailsTabSheet.setWidth("100%");
		this.detailsTabSheet.addStyleName("listDetails");
		this.setDetailsTabSheetHeight();

		this.btnCloseAllTabs = new Button(this.messageSource.getMessage(Message.CLOSE_ALL_TABS));
		this.btnCloseAllTabs.setDebugId("btnCloseAllTabs");
		this.btnCloseAllTabs.setData(ListSelectionLayout.CLOSE_ALL_TABS_ID);
		this.btnCloseAllTabs.setImmediate(true);
		this.btnCloseAllTabs.setStyleName(BaseTheme.BUTTON_LINK);

		this.browseForLists = new Button();
		this.browseForLists.setDebugId("browseForLists");
		this.browseForLists.setImmediate(true);
		this.browseForLists.setStyleName(BaseTheme.BUTTON_LINK);

		this.searchForLists = new Button();
		this.searchForLists.setDebugId("searchForLists");
		this.searchForLists.setImmediate(true);
		this.searchForLists.setStyleName(BaseTheme.BUTTON_LINK);

		this.or = new Label();
		this.or.setDebugId("or");
		this.or.setImmediate(true);

		this.toWorkWith = new Label();
		this.toWorkWith.setDebugId("toWorkWith");
		this.toWorkWith.setImmediate(true);

		this.listStatusForChanges = new HashMap<>();
	}

	@Override
	public void initializeValues() {
		this.headingLabel.setValue(this.messageSource.getMessage(Message.LIST_DETAILS));
		this.browseForLists.setCaption(this.messageSource.getMessage(Message.BROWSE_FOR_A_LIST) + " ");
		this.searchForLists.setCaption(this.messageSource.getMessage(Message.SEARCH_FOR_A_LIST) + " ");
		this.or.setValue(this.messageSource.getMessage(Message.OR) + " ");
		this.toWorkWith.setValue(this.messageSource.getMessage(Message.A_LIST_TO_WORK_WITH) + ". ");
	}

	@Override
	public void layoutComponents() {
		this.setMargin(new MarginInfo(true, false, true, true));
		this.setWidth("100%");

		this.listSelectionHeaderContainer = new HorizontalLayout();
		this.listSelectionHeaderContainer.setDebugId("listSelectionHeaderContainer");
		this.listSelectionHeaderContainer.setHeight("26px");
		this.listSelectionHeaderContainer.setWidth("100%");

		final HeaderLabelLayout headerLbl = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_LIST_DETAILS,
				this.headingLabel);
		headerLbl.setDebugId("headerLbl");
		headerLbl.setDebugId("headerLbl");

		final HorizontalLayout searchOrBrowseLayout = new HorizontalLayout();
		searchOrBrowseLayout.setDebugId("searchOrBrowseLayout");

		this.searchOrBrowseContainer = new HorizontalLayout();
		this.searchOrBrowseContainer.setDebugId("searchOrBrowseContainer");
		this.searchOrBrowseContainer.setHeight("19px");
		this.searchOrBrowseContainer.setWidth("100%");

		// Ugh, bit of a hack - can't figure out how to space these nicely
		this.searchForLists.setWidth("43px");
		this.or.setWidth("16px");
		this.browseForLists.setWidth("48px");
		this.toWorkWith.setWidth("132px");

		searchOrBrowseLayout.addComponent(this.browseForLists);
		searchOrBrowseLayout.addComponent(this.or);
		searchOrBrowseLayout.addComponent(this.searchForLists);
		searchOrBrowseLayout.addComponent(this.toWorkWith);

		this.searchOrBrowseContainer.addComponent(searchOrBrowseLayout);
		this.searchOrBrowseContainer.addComponent(this.btnCloseAllTabs);
		this.searchOrBrowseContainer.setComponentAlignment(this.btnCloseAllTabs, Alignment.TOP_RIGHT);

		final VerticalLayout header = new VerticalLayout();
		header.setDebugId("header");
		header.setWidth("100%");
		header.addComponent(this.noListLabel);
		header.addComponent(headerLbl);

		final VerticalLayout headerBtnContainer = new VerticalLayout();
		headerBtnContainer.setDebugId("headerBtnContainer");
		headerBtnContainer.setSizeUndefined();
		headerBtnContainer.setSpacing(true);
		headerBtnContainer.addComponent(this.source.listBuilderToggleBtn1);

		this.listSelectionHeaderContainer.addComponent(header);
		this.listSelectionHeaderContainer.addComponent(headerBtnContainer);
		this.listSelectionHeaderContainer.setExpandRatio(header, 1.0F);
		this.listSelectionHeaderContainer.setComponentAlignment(headerBtnContainer, Alignment.TOP_RIGHT);

		this.hideDetailsTabsheet();
		this.addComponent(this.listSelectionHeaderContainer);
		this.addComponent(this.searchOrBrowseContainer);
		this.addComponent(this.detailsTabSheet);
		this.displayDefault();
	}

	/**
	 * Try to open list from url params
	 */
	private void openRequestListIds() {
		try {
			final String lists = BreedingManagerUtil.getApplicationRequest().getParameter("lists");
			String[] listArray = StringUtils.split(lists, ",");
			for (final String listId : listArray) {
				this.createListDetailsTab(Integer.valueOf(listId));
			}
		} catch (final Exception e) {
			LOG.error("Error opening lists: " + e.getMessage(),e);
		}
	}

	public void setDetailsTabSheetHeight() {
		this.detailsTabSheet.setHeight("647px");
	}

	public void displayDefault() {
		this.noListLabel.setVisible(false);
		this.headerLayout.setVisible(true);
		this.btnCloseAllTabs.setVisible(false);
	}

	@Override
	public void addListeners() {
		final ListManagerDetailsTabCloseHandler closeHandler = new ListManagerDetailsTabCloseHandler(this);
		this.btnCloseAllTabs.addListener(closeHandler);
		this.detailsTabSheet.setCloseHandler(closeHandler);
		this.detailsTabSheet.addListener(new TabSheet.SelectedTabChangeListener() {

			private static final long serialVersionUID = -7822326039221887888L;

			@Override
			public void selectedTabChange(final SelectedTabChangeEvent event) {
				if (ListSelectionLayout.this.detailsTabSheet.getComponentCount() <= 1) {
					ListSelectionLayout.this.btnCloseAllTabs.setVisible(false);
				} else {
					ListSelectionLayout.this.btnCloseAllTabs.setVisible(true);
				}
			}
		});

		this.browseForLists.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6385074843600086746L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ListSelectionLayout.this.source.getListSelectionComponent().openListBrowseDialog();
			}
		});

		this.searchForLists.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6385074843600086746L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ListSelectionLayout.this.source.getListSelectionComponent().openListSearchDialog();
			}
		});
	}

	@Override
	public void updateLabels() {
		this.headingLabel.setValue(this.messageSource.getMessage(Message.LIST_DETAILS));
		this.browseForLists.setCaption(this.messageSource.getMessage(Message.BROWSE_FOR_A_LIST) + " ");
		this.searchForLists.setCaption(this.messageSource.getMessage(Message.SEARCH_FOR_A_LIST) + " ");
		this.or.setValue(this.messageSource.getMessage(Message.OR) + " ");
		this.toWorkWith.setValue(this.messageSource.getMessage(Message.A_LIST_TO_WORK_WITH));
	}

	public void createListDetailsTab(final Integer listId) {
		final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(listId);
		if (germplasmList == null) {
			this.hideDetailsTabsheet();
			this.noListLabel.setCaption("There is no list in the database with id: " + listId);
			this.noListLabel.setVisible(true);
		} else {
			this.noListLabel.setVisible(false);
			final String tabName = germplasmList.getName();
			this.createTab(germplasmList, tabName);
			this.showDetailsTabsheet();
		}
	}

	private void createTab(final GermplasmList germplasmList, final String tabName) {

		final boolean tabExists = Util.isTabDescriptionExist(this.detailsTabSheet,
				this.generateTabDescription(germplasmList.getId()));

		if (!tabExists) {

			final Component tabContent = new ListTabComponent(this.source, this, germplasmList);
			final Tab tab = this.detailsTabSheet.addTab(tabContent, tabName, null);

			if (germplasmList != null) {
				tab.setDescription(this.generateTabDescription(germplasmList.getId()));
			}

			tab.setClosable(true);
			this.detailsTabSheet.setSelectedTab(tabContent);

		} else {
			final Tab tab = Util.getTabWithDescription(this.detailsTabSheet,
					this.generateTabDescription(germplasmList.getId()));

			if (tab != null) {
				this.detailsTabSheet.setSelectedTab(tab.getComponent());
			}
		}
	}

	private String generateTabDescription(final Integer listId) {
		return ListSelectionLayout.TAB_DESCRIPTION_PREFIX + listId;
	}

	public TabSheet getDetailsTabsheet() {
		return this.detailsTabSheet;
	}

	public void showDetailsTabsheet() {
		this.detailsTabSheet.removeStyleName(AppConstants.CssStyles.NO_TAB);
	}

	public void hideDetailsTabsheet() {
		this.btnCloseAllTabs.setVisible(false);
		this.detailsTabSheet.addStyleName(AppConstants.CssStyles.NO_TAB);
		this.source.setModeView(ModeView.LIST_VIEW);
	}

	public void repaintTabsheet() {
		if (this.detailsTabSheet.isVisible()) {
			this.removeAllComponents();
			this.addComponent(this.listSelectionHeaderContainer);
			this.addComponent(this.searchOrBrowseContainer);
			this.addComponent(this.detailsTabSheet);

			if (this.detailsTabSheet.getComponentCount() > 1) {
				this.btnCloseAllTabs.setVisible(true);
			}
			this.requestRepaint();
		}
	}

	public void renameTab(final Integer listId, final String newName) {

		final String tabDescription = this.generateTabDescription(listId);
		final Tab tab = Util.getTabWithDescription(this.detailsTabSheet, tabDescription);
		if (tab != null) {
			tab.setCaption(newName);
			final ListTabComponent listDetails = (ListTabComponent) tab.getComponent();
			listDetails.setListNameLabel(newName);

			if (tab.getComponent() instanceof ListTabComponent) {
				((ListTabComponent) tab.getComponent()).getGermplasmList().setName(newName);

				final GermplasmList germplasmList = ((ListTabComponent) tab.getComponent()).getListComponent()
						.getGermplasmList();
				germplasmList.setName(newName);
				((ListTabComponent) tab.getComponent()).getListComponent()
						.setViewListHeaderWindow(new ViewListHeaderWindow(germplasmList,
								BreedingManagerUtil.getAllNamesAsMap(this.userDataManager),
								this.germplasmListManager.getGermplasmListTypes()));
			}
		}
	}

	public void removeTab(final Integer listId) {
		final String tabDescription = this.generateTabDescription(listId);
		final Tab tab = Util.getTabWithDescription(this.detailsTabSheet, tabDescription);
		if (tab != null) {
			this.detailsTabSheet.removeTab(tab);
		}

		if (this.detailsTabSheet.getComponentCount() == 0) {
			this.hideDetailsTabsheet();
		}
	}

	@Override
	public void setHasUnsavedChangesMain(final boolean hasChanges) {
		this.source.setHasUnsavedChangesMain(hasChanges);
	}

	public Map<ListComponent, Boolean> getListStatusForChanges() {
		return this.listStatusForChanges;
	}

	public void addUpdateListStatusForChanges(final ListComponent listComponent, final Boolean status) {
		this.removeListStatusForChanges(listComponent);
		this.listStatusForChanges.put(listComponent, status);

		if (this.hasUnsavedChanges()) {
			this.setHasUnsavedChangesMain(true);
		} else {
			this.setHasUnsavedChangesMain(false);
		}
	}

	public boolean hasUnsavedChanges() {
		final List<Boolean> listOfStatus = new ArrayList<>();

		listOfStatus.addAll(this.listStatusForChanges.values());

		for (final Boolean status : listOfStatus) {
			if (status) {
				return true;
			}
		}

		return false;
	}

	public void removeListStatusForChanges(final ListComponent listComponent) {
		if (this.listStatusForChanges.containsKey(listComponent)) {
			this.listStatusForChanges.remove(listComponent);
		}
	}

	public void updateViewForAllLists(final ModeView modeView) {
		final List<ListComponent> listComponents = new ArrayList<>();
		listComponents.addAll(this.listStatusForChanges.keySet());

		if (modeView.equals(ModeView.LIST_VIEW)) {
			for (final ListComponent listComponent : listComponents) {
				listComponent.changeToListView();
			}
		} else if (modeView.equals(ModeView.INVENTORY_VIEW)) {
			for (final ListComponent listComponent : listComponents) {
				listComponent.viewInventoryActionConfirmed();
			}
		}
	}

	public void updateHasChangesForAllList(final Boolean hasChanges) {
		final List<ListComponent> listComponents = new ArrayList<>();
		listComponents.addAll(this.listStatusForChanges.keySet());

		for (final ListComponent listComponent : listComponents) {
			listComponent.setHasUnsavedChanges(hasChanges);
		}
	}

	public void resetListViewForCancelledChanges() {
		final List<ListComponent> listComponents = new ArrayList<>();
		listComponents.addAll(this.listStatusForChanges.keySet());

		for (final ListComponent listComponent : listComponents) {
			if (listComponent.hasUnsavedChanges()) {
				listComponent.resetListDataTableValues();
			}
		}
	}

	public void resetInventoryViewForCancelledChanges() {
		final List<ListComponent> listComponents = new ArrayList<>();
		listComponents.addAll(this.listStatusForChanges.keySet());

		for (final ListComponent listComponent : listComponents) {
			if (listComponent.hasUnsavedChanges()) {
				listComponent.resetListInventoryTableValues();
			}
		}
	}

}
