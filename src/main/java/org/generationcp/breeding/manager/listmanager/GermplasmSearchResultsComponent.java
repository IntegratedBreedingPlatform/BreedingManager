
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.containers.GermplasmQueryFactory;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.PagedTableWithSelectAllLayout;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmSearchResultsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmSearchResultsComponent.class);
	private static final long serialVersionUID = 5314653969843976836L;

	private Label totalMatchingGermplasmsLabel;
	private Label totalSelectedMatchingGermplasmsLabel;
	private PagedBreedingManagerTable matchingGermplasmsTable;

	private Button actionButton;
	private ContextMenu menu;
	private ContextMenuItem menuSelectAll;
	private ContextMenuItem menuAddNewEntry;

	private PagedTableWithSelectAllLayout matchingGermplasmsTableWithSelectAll;

	public static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	public static final String NAMES = "NAMES";

	public static final String MATCHING_GEMRPLASMS_TABLE_DATA = "Matching Germplasms Table";

	static final Action ACTION_COPY_TO_NEW_LIST = new Action("Add Selected Entries to New List");
	static final Action ACTION_SELECT_ALL = new Action("Select All");
	static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] {GermplasmSearchResultsComponent.ACTION_COPY_TO_NEW_LIST,
			GermplasmSearchResultsComponent.ACTION_SELECT_ALL};

	private Action.Handler rightClickActionHandler;

	private final org.generationcp.breeding.manager.listmanager.ListManagerMain listManagerMain;

	private final LazyQueryDefinition definition = new LazyQueryDefinition(true, 20);

	private boolean viaToolUrl = true;

	private boolean showAddToList = true;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmSearchResultsComponent() {
		this(null);
	}

	public GermplasmSearchResultsComponent(final ListManagerMain listManagerMain) {
		this.listManagerMain = listManagerMain;

		this.definition.addProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID, CheckBox.class, null, false, false);
		this.definition.addProperty(GermplasmSearchResultsComponent.NAMES, Button.class, null, false, false);
		this.definition.addProperty(ColumnLabels.PARENTAGE.getName(), String.class, null, false, false);
		this.definition.addProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), Button.class, null, false, false);
		this.definition.addProperty(ColumnLabels.TOTAL.getName(), Button.class, null, false, false);
		this.definition.addProperty(ColumnLabels.STOCKID.getName(), Label.class, null, false, true);
		this.definition.addProperty(ColumnLabels.GID.getName(), Button.class, null, false, false);
		this.definition.addProperty(ColumnLabels.GROUP_ID.getName(), String.class, null, false, true);
		this.definition.addProperty(ColumnLabels.GERMPLASM_LOCATION.getName(), String.class, null, false, false);
		this.definition.addProperty(ColumnLabels.BREEDING_METHOD_NAME.getName(), String.class, null, false, false);
		this.definition.addProperty(ColumnLabels.GID.getName() + "_REF", Integer.class, null, false, false);

	}

	public GermplasmSearchResultsComponent(final ListManagerMain listManagerMain, final boolean viaToolUrl, final boolean showAddToList) {
		this(listManagerMain);

		this.viaToolUrl = viaToolUrl;
		this.showAddToList = showAddToList;
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

		this.setWidth("100%");

		this.totalMatchingGermplasmsLabel = new Label("", Label.CONTENT_XHTML);
		this.totalMatchingGermplasmsLabel.setDebugId("totalMatchingGermplasmsLabel");
		this.totalMatchingGermplasmsLabel.setWidth("120px");
		this.updateNoOfEntries(0);

		this.totalSelectedMatchingGermplasmsLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedMatchingGermplasmsLabel.setDebugId("totalSelectedMatchingGermplasmsLabel");
		this.totalSelectedMatchingGermplasmsLabel.setWidth("95px");
		this.updateNoOfSelectedEntries(0);

		this.actionButton = new ActionButton();
		this.actionButton.setDebugId("actionButton");

		this.menu = new ContextMenu();
		this.menu.setDebugId("menu");
		this.menu.setWidth("250px");
		this.menuAddNewEntry = this.menu.addItem(this.messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST));
		this.menuSelectAll = this.menu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.updateActionMenuOptions(false);

		this.initMatchingGermplasmTable();

		this.rightClickActionHandler = new Action.Handler() {

			private static final long serialVersionUID = -897257270314381555L;

			@Override
			public Action[] getActions(final Object target, final Object sender) {
				return GermplasmSearchResultsComponent.GERMPLASMS_TABLE_CONTEXT_MENU;
			}

			@Override
			public void handleAction(final Action action, final Object sender, final Object target) {
				if (GermplasmSearchResultsComponent.ACTION_COPY_TO_NEW_LIST == action) {
					GermplasmSearchResultsComponent.this.addSelectedEntriesToNewList();
				} else if (GermplasmSearchResultsComponent.ACTION_SELECT_ALL == action) {
					GermplasmSearchResultsComponent.this.matchingGermplasmsTableWithSelectAll.selectAllEntriesOnCurrentPage();
				}
			}
		};

	}

	private void initMatchingGermplasmTable() {
		this.matchingGermplasmsTableWithSelectAll = this.getTableWithSelectAllLayout();
		this.matchingGermplasmsTableWithSelectAll.setHeight("530px");

		this.matchingGermplasmsTable = this.matchingGermplasmsTableWithSelectAll.getTable();
		this.matchingGermplasmsTable.setData(GermplasmSearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA);
		this.matchingGermplasmsTable.setWidth("100%");
		this.matchingGermplasmsTable.setMultiSelect(true);
		this.matchingGermplasmsTable.setSelectable(true);
		this.matchingGermplasmsTable.setImmediate(true);
		this.matchingGermplasmsTable.setDragMode(TableDragMode.ROW);
		this.matchingGermplasmsTable.setHeight("440px");

		this.messageSource.setColumnHeader(this.matchingGermplasmsTable, GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID,
				Message.CHECK_ICON);
		this.messageSource.setColumnHeader(this.matchingGermplasmsTable, GermplasmSearchResultsComponent.NAMES, Message.NAMES_LABEL);
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(),
				ColumnLabels.PARENTAGE.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(),
				ColumnLabels.AVAILABLE_INVENTORY.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.TOTAL.getName(),
				ColumnLabels.TOTAL.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.STOCKID.getName(),
				ColumnLabels.STOCKID.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.GID.getName(),
				ColumnLabels.GID.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.GROUP_ID.getName(),
				ColumnLabels.GROUP_ID.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName(),
				ColumnLabels.GERMPLASM_LOCATION.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName(),
				ColumnLabels.BREEDING_METHOD_NAME.getTermNameFromOntology(this.ontologyDataManager));

		this.matchingGermplasmsTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = 1L;

			@Override
			public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
				// set the default value to empty string instead of null for germplasm without name
				String germplasmNames = "";
				if (propertyId == GermplasmSearchResultsComponent.NAMES) {
					final Item item = GermplasmSearchResultsComponent.this.matchingGermplasmsTable.getItem(itemId);
					final Integer gid =
							Integer.valueOf(((Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue()).getCaption());

					germplasmNames = GermplasmSearchResultsComponent.this.getGermplasmNames(gid);
				}
				return germplasmNames;
			}
		});

		// init container
		this.matchingGermplasmsTable.setContainerDataSource(this.createInitialContainer());

		// hide the internal GID reference ID
		this.matchingGermplasmsTable.setVisibleColumns(new ArrayList<>(this.definition.getPropertyIds()).subList(0,this.definition.getPropertyIds().size()-1).toArray());
	}

	/**
	 * This will just create a container with the table properties so we can have headers when we load the table initially
	 * @return
	 */
	private Container createInitialContainer() {
		Container container = new IndexedContainer();
		for (Object propertyId : definition.getPropertyIds()) {
			container.addContainerProperty(propertyId, definition.getPropertyType(propertyId),
					definition.getPropertyDefaultValue(propertyId));
		}

		return container;
	}

	private GermplasmQueryFactory createGermplasmQueryFactory(final GermplasmSearchParameter searchParameter) {
		// set the start and no of entries to retrieve at initial loading
		searchParameter.setStartingRow(0);
		searchParameter.setNumberOfEntries(this.matchingGermplasmsTable.getPageLength());

		return new GermplasmQueryFactory(this.listManagerMain, this.viaToolUrl, this.showAddToList, searchParameter,
				this.matchingGermplasmsTable, definition);
	}

	private LazyQueryContainer createContainer(final GermplasmQueryFactory factory) {
		// update the definitions batch size to match current state of table's page entry length
		this.definition.setBatchSize(this.matchingGermplasmsTable.getPageLength());

		final LazyQueryContainer container = new LazyQueryContainer(this.definition,factory);
		return container;
	}

	protected PagedTableWithSelectAllLayout getTableWithSelectAllLayout() {
		return new PagedTableWithSelectAllLayout(20, GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID);
	}

	private void updateActionMenuOptions(final boolean status) {
		this.menuAddNewEntry.setEnabled(status);
		this.menuSelectAll.setEnabled(status);
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {

		this.actionButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				GermplasmSearchResultsComponent.this.menu.show(event.getClientX(), event.getClientY());
			}

		});

		this.menu.addListener(new ContextMenu.ClickListener() {

			private static final long serialVersionUID = -2343109406180457070L;

			@Override
			public void contextItemClick(final org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				final ContextMenuItem clickedItem = event.getClickedItem();

				if (clickedItem.getName().equals(
						GermplasmSearchResultsComponent.this.messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST))) {
					GermplasmSearchResultsComponent.this.addSelectedEntriesToNewList();
				} else if (clickedItem.getName().equals(GermplasmSearchResultsComponent.this.messageSource.getMessage(Message.SELECT_ALL))) {
					GermplasmSearchResultsComponent.this.matchingGermplasmsTableWithSelectAll.selectAllEntriesOnCurrentPage();
				}

			}
		});

		this.matchingGermplasmsTable.addActionHandler(this.rightClickActionHandler);

		this.matchingGermplasmsTable.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				GermplasmSearchResultsComponent.this.updateNoOfSelectedEntries();
			}
		});
	}

	public void setRightClickActionHandlerEnabled(final Boolean isEnabled) {
		this.matchingGermplasmsTable.removeActionHandler(this.rightClickActionHandler);
		if (isEnabled) {
			this.matchingGermplasmsTable.addActionHandler(this.rightClickActionHandler);
		}
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		final HorizontalLayout leftHeaderLayout = new HorizontalLayout();
		leftHeaderLayout.setDebugId("leftHeaderLayout");
		leftHeaderLayout.setSpacing(true);
		leftHeaderLayout.addComponent(this.totalMatchingGermplasmsLabel);
		leftHeaderLayout.addComponent(this.totalSelectedMatchingGermplasmsLabel);
		leftHeaderLayout.setComponentAlignment(this.totalMatchingGermplasmsLabel, Alignment.MIDDLE_LEFT);
		leftHeaderLayout.setComponentAlignment(this.totalSelectedMatchingGermplasmsLabel, Alignment.MIDDLE_LEFT);

		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setDebugId("headerLayout");
		headerLayout.setWidth("100%");
		headerLayout.setSpacing(true);
		headerLayout.addComponent(leftHeaderLayout);
		headerLayout.addComponent(this.actionButton);
		headerLayout.setComponentAlignment(leftHeaderLayout, Alignment.BOTTOM_LEFT);
		headerLayout.setComponentAlignment(this.actionButton, Alignment.BOTTOM_RIGHT);

		this.addComponent(this.menu);
		this.addComponent(headerLayout);
		this.addComponent(this.matchingGermplasmsTableWithSelectAll);
	}

	public void applyGermplasmResults(final GermplasmSearchParameter searchParameter) throws BreedingManagerSearchException {

		final Monitor monitor = MonitorFactory.start("GermplasmSearchResultsComponent.applyGermplasmResults()");

		final GermplasmQueryFactory factory = this.createGermplasmQueryFactory(searchParameter);
		final LazyQueryContainer container = this.createContainer(factory);

		// set the current page to first page before updating the entries with the new search results
		this.matchingGermplasmsTable.setCurrentPage(1);

		this.matchingGermplasmsTable.setContainerDataSource(container);
		this.matchingGermplasmsTable.setImmediate(true);

		// hide the internal GID reference ID
		this.matchingGermplasmsTable.setVisibleColumns(new ArrayList<>(this.definition.getPropertyIds()).subList(0,this.definition.getPropertyIds().size()-1).toArray());

		this.updateNoOfEntries(factory.getNumberOfItems());

		if (!this.matchingGermplasmsTable.getItemIds().isEmpty()) {
			this.updateActionMenuOptions(true);
		}

		if (this.matchingGermplasmsTable.getContainerDataSource().getItemIds().isEmpty()) {
			throw new BreedingManagerSearchException(Message.NO_SEARCH_RESULTS);
		}

		GermplasmSearchResultsComponent.LOG.debug("" + monitor.stop());

		// update controls
		this.matchingGermplasmsTableWithSelectAll.refreshTablePagingControls();
	}

	String getShortenedNames(final String germplasmFullName) {
		return germplasmFullName.length() > 20 ? germplasmFullName.substring(0, 20) + "..." : germplasmFullName;
	}

	private String getGermplasmNames(final int gid) {
		final StringBuilder germplasmNames = new StringBuilder("");

		final List<Name> names = this.germplasmDataManager.getNamesByGID(new Integer(gid), null, null);

		int i = 0;
		for (final Name n : names) {
			if (i < names.size() - 1) {
				germplasmNames.append(n.getNval() + ", ");
			} else {
				germplasmNames.append(n.getNval());
			}
			i++;
		}

		return germplasmNames.toString();
	}

	public PagedTableWithSelectAllLayout getMatchingGermplasmsTableWithSelectAll() {
		return this.matchingGermplasmsTableWithSelectAll;
	}

	public Table getMatchingGermplasmsTable() {
		return this.matchingGermplasmsTable;
	}

	private void updateNoOfEntries(final long count) {
		this.totalMatchingGermplasmsLabel.setValue(this.messageSource.getMessage(Message.TOTAL_RESULTS) + ": " + "  <b>" + count + "</b>");
	}

	private void updateNoOfSelectedEntries(final int count) {
		this.totalSelectedMatchingGermplasmsLabel.setValue("<i>" + this.messageSource.getMessage(Message.SELECTED) + ": " + "  <b>" + count
				+ "</b></i>");
	}

	private void updateNoOfSelectedEntries() {
		int count = 0;

		final Collection<?> selectedItems = (Collection<?>) this.matchingGermplasmsTable.getValue();
		count = selectedItems.size();

		this.updateNoOfSelectedEntries(count);
	}

	@SuppressWarnings("unchecked")
	public void addSelectedEntriesToNewList() {
		final List<Integer> gids = new ArrayList<>();
		final List<Integer> itemIds = new ArrayList<>();
		itemIds.addAll((Collection<? extends Integer>) this.matchingGermplasmsTable.getValue());

		if (itemIds.isEmpty()) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.ERROR_GERMPLASM_MUST_BE_SELECTED));
		} else {
			for (final Integer id : itemIds) {
				// retrieve the actual GID from the itemId
				final Integer gid =
						(Integer) this.matchingGermplasmsTable.getItem(id).getItemProperty(ColumnLabels.GID + "_REF").getValue();
				gids.add(gid);
			}
			this.listManagerMain.addPlantsToList(gids);
		}
	}

	public boolean isViaToolUrl() {
		return this.viaToolUrl;
	}

	public void setViaToolUrl(final boolean viaToolUrl) {
		this.viaToolUrl = viaToolUrl;
	}

	public boolean isShowAddToList() {
		return this.showAddToList;
	}

	public void setShowAddToList(final boolean showAddToList) {
		this.showAddToList = showAddToList;
	}
}
