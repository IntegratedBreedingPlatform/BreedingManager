
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
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
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class GermplasmSearchResultsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmSearchResultsComponent.class);
	private static final long serialVersionUID = 5314653969843976836L;

	private Label totalMatchingGermplasmsLabel;
	private Label totalSelectedMatchingGermplasmsLabel;
	private Table matchingGermplasmsTable;

	private Button actionButton;
	private ContextMenu menu;
	private ContextMenuItem menuSelectAll;
	private ContextMenuItem menuAddNewEntry;

	private TableWithSelectAllLayout matchingGermplasmsTableWithSelectAll;

	public static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	public static final String NAMES = "NAMES";

	public static final String MATCHING_GEMRPLASMS_TABLE_DATA = "Matching Germplasms Table";

	static final Action ACTION_COPY_TO_NEW_LIST = new Action("Add Selected Entries to New List");
	static final Action ACTION_SELECT_ALL = new Action("Select All");
	static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] {GermplasmSearchResultsComponent.ACTION_COPY_TO_NEW_LIST,
			GermplasmSearchResultsComponent.ACTION_SELECT_ALL};

	private Action.Handler rightClickActionHandler;

	private final org.generationcp.breeding.manager.listmanager.ListManagerMain listManagerMain;

	private boolean viaToolUrl = true;

	private boolean showAddToList = true;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private LocationDataManager locationDataManager;
	
	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	public GermplasmSearchResultsComponent() {
		this.listManagerMain = null;
	}

	public GermplasmSearchResultsComponent(final ListManagerMain listManagerMain) {
		this.listManagerMain = listManagerMain;
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
		this.totalMatchingGermplasmsLabel.setWidth("120px");
		this.updateNoOfEntries(0);

		this.totalSelectedMatchingGermplasmsLabel = new Label("", Label.CONTENT_XHTML);
		this.totalSelectedMatchingGermplasmsLabel.setWidth("95px");
		this.updateNoOfSelectedEntries(0);

		this.actionButton = new ActionButton();

		this.menu = new ContextMenu();
		this.menu.setWidth("250px");
		this.menuAddNewEntry = this.menu.addItem(this.messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST));
		this.menuSelectAll = this.menu.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.updateActionMenuOptions(false);

		this.matchingGermplasmsTableWithSelectAll = this.getTableWithSelectAllLayout();
		this.matchingGermplasmsTableWithSelectAll.setHeight("500px");

		this.matchingGermplasmsTable = this.matchingGermplasmsTableWithSelectAll.getTable();
		this.matchingGermplasmsTable.setData(GermplasmSearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA);
		this.matchingGermplasmsTable.addContainerProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID, CheckBox.class, null);
		this.matchingGermplasmsTable.addContainerProperty(GermplasmSearchResultsComponent.NAMES, Button.class, null);
		this.matchingGermplasmsTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		this.matchingGermplasmsTable.addContainerProperty(ColumnLabels.AVAILABLE_INVENTORY.getName(), String.class, null);
		this.matchingGermplasmsTable.addContainerProperty(ColumnLabels.SEED_RESERVATION.getName(), String.class, null);
		this.matchingGermplasmsTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		this.matchingGermplasmsTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		this.matchingGermplasmsTable.addContainerProperty(ColumnLabels.GROUP_ID.getName(), String.class, null);
		this.matchingGermplasmsTable.addContainerProperty(ColumnLabels.GERMPLASM_LOCATION.getName(), String.class, null);
		this.matchingGermplasmsTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NAME.getName(), String.class, null);
		this.matchingGermplasmsTable.setWidth("100%");
		this.matchingGermplasmsTable.setMultiSelect(true);
		this.matchingGermplasmsTable.setSelectable(true);
		this.matchingGermplasmsTable.setImmediate(true);
		this.matchingGermplasmsTable.setDragMode(TableDragMode.ROW);
		this.matchingGermplasmsTable.setHeight("470px");

		this.messageSource.setColumnHeader(this.matchingGermplasmsTable, GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID,
				Message.CHECK_ICON);
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(),
				ColumnLabels.PARENTAGE.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName(),
				ColumnLabels.AVAILABLE_INVENTORY.getTermNameFromOntology(this.ontologyDataManager));
		this.matchingGermplasmsTable.setColumnHeader(ColumnLabels.SEED_RESERVATION.getName(),
				ColumnLabels.SEED_RESERVATION.getTermNameFromOntology(this.ontologyDataManager));
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
					GermplasmSearchResultsComponent.this.matchingGermplasmsTable
							.setValue(GermplasmSearchResultsComponent.this.matchingGermplasmsTable.getItemIds());
				}
			}
		};

	}

	protected TableWithSelectAllLayout getTableWithSelectAllLayout() {
		return new TableWithSelectAllLayout(10, GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID);
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
					GermplasmSearchResultsComponent.this.matchingGermplasmsTable
							.setValue(GermplasmSearchResultsComponent.this.matchingGermplasmsTable.getItemIds());
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
		leftHeaderLayout.setSpacing(true);
		leftHeaderLayout.addComponent(this.totalMatchingGermplasmsLabel);
		leftHeaderLayout.addComponent(this.totalSelectedMatchingGermplasmsLabel);
		leftHeaderLayout.setComponentAlignment(this.totalMatchingGermplasmsLabel, Alignment.MIDDLE_LEFT);
		leftHeaderLayout.setComponentAlignment(this.totalSelectedMatchingGermplasmsLabel, Alignment.MIDDLE_LEFT);

		final HorizontalLayout headerLayout = new HorizontalLayout();
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

	public void applyGermplasmResults(final List<Germplasm> germplasms) throws BreedingManagerSearchException {

		Map<Integer, String> locationsMap = new HashMap<>();
		Map<Integer, String> methodsMap = new HashMap<>();
		
		final Monitor monitor = MonitorFactory.start("GermplasmSearchResultsComponent.applyGermplasmResults()");
		
		this.updateNoOfEntries(germplasms.size());
		this.matchingGermplasmsTable.removeAllItems();
		for (final Germplasm germplasm : germplasms) {

			final GidLinkButtonClickListener listener =
					new GidLinkButtonClickListener(this.listManagerMain, germplasm.getGid().toString(), this.viaToolUrl, this.showAddToList);
			final Button gidButton = new Button(String.format("%s", germplasm.getGid().toString()), listener);
			gidButton.setStyleName(BaseTheme.BUTTON_LINK);

			final String germplasmFullName = this.getGermplasmNames(germplasm.getGid());
			final String shortenedNames = this.getShortenedNames(germplasmFullName);

			final Button namesButton = new Button(shortenedNames, listener);
			namesButton.setStyleName(BaseTheme.BUTTON_LINK);
			namesButton.setDescription(germplasmFullName);

			final String crossExpansion = this.pedigreeService.getCrossExpansion(germplasm.getGid(), this.crossExpansionProperties);

			final CheckBox itemCheckBox = new CheckBox();
			itemCheckBox.setData(germplasm.getGid());
			itemCheckBox.setImmediate(true);
			itemCheckBox.addListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
					final CheckBox itemCheckBox = (CheckBox) event.getButton();
					if (((Boolean) itemCheckBox.getValue()).equals(true)) {
						GermplasmSearchResultsComponent.this.matchingGermplasmsTable.select(itemCheckBox.getData());
					} else {
						GermplasmSearchResultsComponent.this.matchingGermplasmsTable.unselect(itemCheckBox.getData());
					}
				}

			});

			String methodName = this.retrieveMethodName(germplasm.getMethodId(), methodsMap);

			String locationName = this.retrieveLocationName(germplasm.getLocationId(), locationsMap);

			final GermplasmInventory inventoryInfo = germplasm.getInventoryInfo();
			final Label stockLabel = this.getStockIDs(inventoryInfo);
			final String availInv = this.getAvailableInventory(inventoryInfo);
			final String seedRes = this.getSeedReserved(inventoryInfo);
			final String groupID = germplasm.getMgid() == 0 ? "-" : germplasm.getMgid().toString();

			Item newItem = this.matchingGermplasmsTable.getContainerDataSource().addItem(germplasm.getGid());
			
			newItem.getItemProperty(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID).setValue(itemCheckBox);
			newItem.getItemProperty(GermplasmSearchResultsComponent.NAMES).setValue(namesButton);
			newItem.getItemProperty(ColumnLabels.PARENTAGE.getName()).setValue(crossExpansion);
			newItem.getItemProperty(ColumnLabels.AVAILABLE_INVENTORY.getName()).setValue(availInv);
			newItem.getItemProperty(ColumnLabels.SEED_RESERVATION.getName()).setValue(seedRes);
			newItem.getItemProperty(ColumnLabels.STOCKID.getName()).setValue(stockLabel);
			newItem.getItemProperty(ColumnLabels.GID.getName()).setValue(gidButton);
			newItem.getItemProperty(ColumnLabels.GROUP_ID.getName()).setValue(groupID);
			newItem.getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName()).setValue(locationName);
			newItem.getItemProperty(ColumnLabels.BREEDING_METHOD_NAME.getName()).setValue(methodName);
		}

		this.updateNoOfEntries();

		if (!this.matchingGermplasmsTable.getItemIds().isEmpty()) {
			this.updateActionMenuOptions(true);
		}
		GermplasmSearchResultsComponent.LOG.debug("" + monitor.stop());
	}

	private String retrieveMethodName(Integer methodId, Map<Integer, String> methodsMap) {
		String methodName = "-";
		if(methodsMap.get(methodId) == null){
			final Method germplasmMethod = this.germplasmDataManager.getMethodByID(methodId);
			if (germplasmMethod != null && germplasmMethod.getMname() != null) {
				methodName = germplasmMethod.getMname();
				methodsMap.put(methodId, methodName);
			}
		} else {
			methodName = methodsMap.get(methodId);
		}
		return methodName;
	}
	
	private String retrieveLocationName(Integer locId, Map<Integer, String> locationsMap) {
		String locationName = "-";
		if(locationsMap.get(locId) == null){
			final Location germplasmLocation = this.locationDataManager.getLocationByID(locId);
			if (germplasmLocation != null && germplasmLocation.getLname() != null) {
				locationName = germplasmLocation.getLname();
				locationsMap.put(locId, locationName);
			}
		} else {
			locationName = locationsMap.get(locId);
		}
		return locationName;
	}

	String getShortenedNames(final String germplasmFullName) {
		return germplasmFullName.length() > 20 ? germplasmFullName.substring(0, 20) + "..." : germplasmFullName;
	}

	private String getSeedReserved(final GermplasmInventory inventoryInfo) {
		String seedRes = "-";
		final Integer reservedLotCount = inventoryInfo.getReservedLotCount();
		if (reservedLotCount != null && reservedLotCount.intValue() != 0) {
			seedRes = reservedLotCount.toString();
		}
		return seedRes;
	}

	private String getAvailableInventory(final GermplasmInventory inventoryInfo) {
		String availInv = "-";
		final Integer actualInventoryLotCount = inventoryInfo.getActualInventoryLotCount();
		if (actualInventoryLotCount != null && actualInventoryLotCount.intValue() != 0) {
			availInv = actualInventoryLotCount.toString();
		}
		return availInv;
	}

	private Label getStockIDs(final GermplasmInventory inventoryInfo) {
		final String stockIDs = inventoryInfo.getStockIDs();
		final Label stockLabel = new Label(stockIDs);
		stockLabel.setDescription(stockIDs);
		return stockLabel;
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

	public TableWithSelectAllLayout getMatchingGermplasmsTableWithSelectAll() {
		return this.matchingGermplasmsTableWithSelectAll;
	}

	public Table getMatchingGermplasmsTable() {
		return this.matchingGermplasmsTable;
	}

	private void updateNoOfEntries(final long count) {
		this.totalMatchingGermplasmsLabel.setValue(this.messageSource.getMessage(Message.TOTAL_RESULTS) + ": " + "  <b>" + count + "</b>");
	}

	private void updateNoOfEntries() {
		int count = 0;
		count = this.matchingGermplasmsTable.getItemIds().size();
		this.updateNoOfEntries(count);
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
		final List<Integer> gids = new ArrayList<Integer>();
		gids.addAll((Collection<? extends Integer>) this.matchingGermplasmsTable.getValue());

		if (gids.isEmpty()) {
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.ERROR_GERMPLASM_MUST_BE_SELECTED));
		} else {
			for (final Integer gid : gids) {
				this.listManagerMain.addPlantToList(gid);
			}
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
