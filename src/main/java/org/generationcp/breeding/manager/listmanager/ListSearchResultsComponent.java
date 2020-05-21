
package org.generationcp.breeding.manager.listmanager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.util.ToolTipGenerator;
import org.generationcp.breeding.manager.listmanager.listeners.ListSearchResultsItemClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configurable
public class ListSearchResultsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final long serialVersionUID = 5314653969843976836L;
	private static final Logger LOG = LoggerFactory.getLogger(ListSearchResultsComponent.class);

	private final ListManagerMain source;

	private Label totalMatchingListsLabel;
	private Label totalSelectedMatchingListsLabel;

	private Button actionButton;
	private Table matchingListsTable;
	private TableWithSelectAllLayout matchingListsTableWithSelectAll;

	private static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	private static final String NAME_ID = "NAME";
	private static final String DESCRIPTION_ID = "DESCRIPTION";

	public static final String MATCHING_LISTS_TABLE_DATA = "Matching Lists Table";
	public static final String TOOLS_BUTTON_ID = "Actions";

	static final Action ACTION_SELECT_ALL = new Action("Select All");
	static final Action ACTION_ADD_TO_NEW_LIST = new Action("Add Selected List(s) to New List");
	static final Action[] LISTS_TABLE_CONTEXT_MENU = new Action[] {ListSearchResultsComponent.ACTION_SELECT_ALL,
			ListSearchResultsComponent.ACTION_ADD_TO_NEW_LIST};
	static final Action[] LISTS_TABLE_CONTEXT_MENU_LOCKED = new Action[] {ListSearchResultsComponent.ACTION_SELECT_ALL};

	private Action.Handler lockedRightClickActionHandler;
	private Action.Handler unlockedRightClickActionHandler;

	// Tools Button Context Menu
	private ContextMenu menu;
	private ContextMenuItem menuSelectAll;
	private ContextMenuItem menuAddToNewList;

	private final org.generationcp.breeding.manager.listmanager.ListSelectionLayout displayDetailsLayout;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	protected GermplasmListManager germplasmListManager;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private UserService userService;

	public ListSearchResultsComponent(ListManagerMain source, final ListSelectionLayout displayDetailsLayout) {
		this.source = source;
		this.displayDetailsLayout = displayDetailsLayout;
	}

	@Override
	public void updateLabels() {
		// not implemented
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

		this.totalMatchingListsLabel = new Label("", Label.CONTENT_XHTML);
		this.totalMatchingListsLabel.setDebugId("totalMatchingListsLabel");
		this.totalMatchingListsLabel.setWidth("120px");
		this.updateNoOfEntries(0);

		this.totalSelectedMatchingListsLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedMatchingListsLabel.setDebugId("totalSelectedMatchingListsLabel");
		this.totalSelectedMatchingListsLabel.setWidth("95px");
		this.updateNoOfSelectedEntries(0);

		this.actionButton = new ActionButton();
		this.actionButton.setDebugId("actionButton");
		this.actionButton.setData(ListSearchResultsComponent.TOOLS_BUTTON_ID);

		// Action Button ContextMenu
		this.menu = new ContextMenu();
		this.menu.setDebugId("menu");
		this.menu.setWidth("300px");

		this.menuAddToNewList = this.menu.addItem(this.messageSource.getMessage(Message.ADD_SELECTED_LIST_TO_NEW_LIST));
		this.menuSelectAll = this.menu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));

		this.updateActionMenuOptions(false);

		this.matchingListsTableWithSelectAll = new TableWithSelectAllLayout(5, ListSearchResultsComponent.CHECKBOX_COLUMN_ID);
		this.matchingListsTableWithSelectAll.setDebugId("matchingListsTableWithSelectAll");
		this.matchingListsTableWithSelectAll.setHeight("100%");
		this.matchingListsTable = this.matchingListsTableWithSelectAll.getTable();
		this.matchingListsTable.setData(ListSearchResultsComponent.MATCHING_LISTS_TABLE_DATA);
		this.matchingListsTable.addContainerProperty(ListSearchResultsComponent.CHECKBOX_COLUMN_ID, CheckBox.class, null);
		this.matchingListsTable.addContainerProperty(ListSearchResultsComponent.NAME_ID, String.class, null);
		this.matchingListsTable.addContainerProperty(ListSearchResultsComponent.DESCRIPTION_ID, String.class, null);
		this.matchingListsTable.setHeight("260px");
		this.matchingListsTable.setWidth("100%");
		this.matchingListsTable.setMultiSelect(true);
		this.matchingListsTable.setSelectable(true);
		this.matchingListsTable.setImmediate(true);
		this.matchingListsTable.setDragMode(TableDragMode.ROW);
		this.matchingListsTable.addListener(new ListSearchResultsItemClickListener(this.displayDetailsLayout));
		this.messageSource.setColumnHeader(this.matchingListsTable, ListSearchResultsComponent.CHECKBOX_COLUMN_ID, Message.CHECK_ICON);

		this.lockedRightClickActionHandler = new Action.Handler() {

			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (ListSearchResultsComponent.ACTION_SELECT_ALL == action) {
					ListSearchResultsComponent.this.matchingListsTable.setValue(ListSearchResultsComponent.this.matchingListsTable
							.getItemIds());
				}
			}

			@Override
			public Action[] getActions(Object target, Object sender) {
				return ListSearchResultsComponent.LISTS_TABLE_CONTEXT_MENU_LOCKED;
			}
		};

		this.unlockedRightClickActionHandler = new Action.Handler() {

			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(final Action action, final Object sender, final Object target) {
				final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						if (ListSearchResultsComponent.ACTION_SELECT_ALL == action) {
							ListSearchResultsComponent.this.matchingListsTable.setValue(ListSearchResultsComponent.this.matchingListsTable
									.getItemIds());
						} else if (ListSearchResultsComponent.ACTION_ADD_TO_NEW_LIST == action) {
							ListSearchResultsComponent.this.addSelectedListToNewList();
						}
					}
				});
			}

			@Override
			public Action[] getActions(Object target, Object sender) {
				return ListSearchResultsComponent.LISTS_TABLE_CONTEXT_MENU;
			}
		};

		this.addActionHandler();
	}

	private void updateActionMenuOptions(boolean status) {
		this.menuAddToNewList.setEnabled(status);
		this.menuSelectAll.setEnabled(status);
	}

	private void addActionHandler() {
		this.refreshActionHandler();
	}

	public void refreshActionHandler() {
		this.matchingListsTable.removeActionHandler(this.lockedRightClickActionHandler);
		this.matchingListsTable.removeActionHandler(this.unlockedRightClickActionHandler);

		if (this.source.listBuilderIsLocked()) {
			this.matchingListsTable.addActionHandler(this.lockedRightClickActionHandler);
			this.menuAddToNewList.setVisible(false);
		} else {
			this.matchingListsTable.addActionHandler(this.unlockedRightClickActionHandler);
			this.menuAddToNewList.setVisible(true);
		}
	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@Override
	public void addListeners() {

		this.menu.addListener(new ContextMenu.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void contextItemClick(final org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						ContextMenuItem clickedItem = event.getClickedItem();
						if (clickedItem.getName().equals(ListSearchResultsComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
							ListSearchResultsComponent.this.matchingListsTable.setValue(ListSearchResultsComponent.this.matchingListsTable
									.getItemIds());
						} else if (clickedItem.getName().equals(
								ListSearchResultsComponent.this.messageSource.getMessage(Message.ADD_SELECTED_LIST_TO_NEW_LIST))) {
							ListSearchResultsComponent.this.addSelectedListToNewList();
						}
					}
				});
			}
		});

		this.actionButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1345004576139547723L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				ListSearchResultsComponent.this.menu.show(event.getClientX(), event.getClientY());
			}
		});

		this.matchingListsTable.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				ListSearchResultsComponent.this.updateNoOfSelectedEntries();
			}
		});
	}

	@Override
	public void layoutComponents() {

		this.setWidth("100%");
		this.setSpacing(true);

		HorizontalLayout leftHeaderLayout = new HorizontalLayout();
		leftHeaderLayout.setDebugId("leftHeaderLayout");
		leftHeaderLayout.setSpacing(true);
		leftHeaderLayout.addComponent(this.totalMatchingListsLabel);
		leftHeaderLayout.addComponent(this.totalSelectedMatchingListsLabel);
		leftHeaderLayout.setComponentAlignment(this.totalMatchingListsLabel, Alignment.MIDDLE_LEFT);
		leftHeaderLayout.setComponentAlignment(this.totalSelectedMatchingListsLabel, Alignment.MIDDLE_LEFT);

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setDebugId("headerLayout");
		headerLayout.setWidth("100%");
		headerLayout.addComponent(leftHeaderLayout);
		headerLayout.addComponent(this.actionButton);
		headerLayout.setComponentAlignment(leftHeaderLayout, Alignment.BOTTOM_LEFT);
		headerLayout.setComponentAlignment(this.actionButton, Alignment.BOTTOM_RIGHT);

		this.addComponent(headerLayout);
		this.addComponent(this.matchingListsTableWithSelectAll);
		this.addComponent(this.menu);
	}

	public void applyGermplasmListResults(final List<GermplasmList> germplasmLists) {
		this.totalMatchingListsLabel.setValue(new Label(this.messageSource.getMessage(Message.TOTAL_RESULTS) + ": " + "  <b>"
				+ String.valueOf(germplasmLists.size()) + "</b>", Label.CONTENT_XHTML));
		this.matchingListsTable.removeAllItems();
		for (GermplasmList germplasmList : germplasmLists) {

			CheckBox itemCheckBox = new CheckBox();
			itemCheckBox.setDebugId("itemCheckBox");
			itemCheckBox.setData(germplasmList.getId());
			itemCheckBox.setImmediate(true);
			itemCheckBox.addListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
					CheckBox itemCheckBox = (CheckBox) event.getButton();
					if (((Boolean) itemCheckBox.getValue()).equals(true)) {
						ListSearchResultsComponent.this.matchingListsTable.select(itemCheckBox.getData());
					} else {
						ListSearchResultsComponent.this.matchingListsTable.unselect(itemCheckBox.getData());
					}
				}

			});

			Item newItem = this.matchingListsTable.getContainerDataSource().addItem(germplasmList.getId());
			newItem.getItemProperty(ListSearchResultsComponent.CHECKBOX_COLUMN_ID).setValue(itemCheckBox);
			newItem.getItemProperty(ListSearchResultsComponent.NAME_ID).setValue(germplasmList.getName());
			newItem.getItemProperty(ListSearchResultsComponent.DESCRIPTION_ID).setValue(germplasmList.getDescription());
		}
		
		this.matchingListsTable.setItemDescriptionGenerator(
				new ToolTipGenerator(this.userService.getAllUserIDFullNameMap(), germplasmListManager.getGermplasmListTypes())
						.getItemDescriptionGenerator(new HashSet<GermplasmList>(germplasmLists)));
	
		if (!this.matchingListsTable.getItemIds().isEmpty()) {
			this.updateActionMenuOptions(true);
		}
	}

	private void updateNoOfEntries(long count) {
		this.totalMatchingListsLabel.setValue(this.messageSource.getMessage(Message.TOTAL_RESULTS) + ": " + "  <b>" + count + "</b>");
	}

	private void updateNoOfSelectedEntries(int count) {
		this.totalSelectedMatchingListsLabel.setValue("<i>" + this.messageSource.getMessage(Message.SELECTED) + ": " + "  <b>" + count
				+ "</b></i>");
	}

	private void updateNoOfSelectedEntries() {
		int count = 0;

		Collection<?> selectedItems = (Collection<?>) this.matchingListsTable.getValue();
		count = selectedItems.size();

		this.updateNoOfSelectedEntries(count);
	}

	public Table getMatchingListsTable() {
		return this.matchingListsTable;
	}

	public ListSelectionLayout getListManagerDetailsLayout() {
		return this.displayDetailsLayout;
	}

	public void removeSearchResult(Object itemId) {
		this.matchingListsTable.removeItem(itemId);
	}

	@SuppressWarnings("unchecked")
	public void addSelectedListToNewList() {
		this.source.showListBuilder();

		Set<Integer> listIds = (Set<Integer>) this.matchingListsTable.getValue();
		this.source.getListBuilderComponent().addListsFromSearchResults(listIds);
	}
}
