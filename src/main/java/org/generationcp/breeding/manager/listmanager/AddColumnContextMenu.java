
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.domain.gms.ListDataColumn;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Table;

@Configurable
public class AddColumnContextMenu implements InternationalizableComponent {

	private final class SourceContextMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = 1L;

		// Handle clicks on menu items
		@Override
		public void contextItemClick(ClickEvent event) {
			ContextMenuItem clickedItem = event.getClickedItem();
			if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_PREFERRED_ID)) {
				AddColumnContextMenu.this.addPreferredIdColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_PREFERRED_NAME)) {
				AddColumnContextMenu.this.addPreferredNameColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_GERMPLASM_DATE)) {
				AddColumnContextMenu.this.addGermplasmDateColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_LOCATION)) {
				AddColumnContextMenu.this.addLocationColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_METHOD_NAME)) {
				AddColumnContextMenu.this.addMethodNameColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_METHOD_ABBREV)) {
				AddColumnContextMenu.this.addMethodAbbrevColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_METHOD_NUMBER)) {
				AddColumnContextMenu.this.addMethodNumberColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_METHOD_GROUP)) {
				AddColumnContextMenu.this.addMethodGroupColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_CROSS_FEMALE_GID)) {
				AddColumnContextMenu.this.addCrossFemaleGidColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_CROSS_FEMALE_PREF_NAME)) {
				AddColumnContextMenu.this.addCrossFemalePrefNameColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_CROSS_MALE_GID)) {
				AddColumnContextMenu.this.addCrossMaleGIDColumn();
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_CROSS_MALE_PREF_NAME)) {
				AddColumnContextMenu.this.addCrossMalePrefNameColumn();
			}
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(AddColumnContextMenu.class);

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	private ListTabComponent listDetailsComponent = null;

	@SuppressWarnings("unused")
	private ComponentContainer cssLayoutSource;

	private final String gidPropertyId;
	private final Table targetTable;

	private ContextMenu sourceContextMenu;
	private ContextMenuItem addColumnItem;
	private ContextMenuItem menuFillWithPreferredId;
	private ContextMenuItem menuFillWithPreferredName;
	private ContextMenuItem menuFillWithGermplasmDate;
	private ContextMenuItem menuFillWithLocations;
	private ContextMenuItem menuFillWithMethodInfo;
	private ContextMenuItem menuFillWithMethodName;
	private ContextMenuItem menuFillWithMethodAbbrev;
	private ContextMenuItem menuFillWithMethodNumber;
	private ContextMenuItem menuFillWithMethodGroup;
	private ContextMenuItem menuFillWithCrossFemaleInfo;
	private ContextMenuItem menuFillWithCrossFemaleGID;
	private ContextMenuItem menuFillWithCrossFemalePrefName;
	private ContextMenuItem menuFillWithCrossMaleInfo;
	private ContextMenuItem menuFillWithCrossMaleGID;
	private ContextMenuItem menuFillWithCrossMalePrefName;

	private static final String ADD_COLUMN_MENU = "Add Column";
	private static final String FILL_WITH_PREFERRED_ID = "Fill with Preferred ID";
	private static final String FILL_WITH_PREFERRED_NAME = "Fill with Preferred Name";
	private static final String FILL_WITH_GERMPLASM_DATE = "Fill with Germplasm Dates";
	private static final String FILL_WITH_LOCATION = "Fill with Location";
	private static final String FILL_WITH_METHOD_INFO = "Fill with Breeding Method Information";
	private static final String FILL_WITH_METHOD_NAME = "Fill with Breeding Method Name";
	private static final String FILL_WITH_METHOD_ABBREV = "Fill with Breeding Method Abbreviation";
	private static final String FILL_WITH_METHOD_NUMBER = "Fill with Breeding Method Number";
	private static final String FILL_WITH_METHOD_GROUP = "Fill with Breeding Method Group";
	private static final String FILL_WITH_CROSS_FEMALE_INFO = "Fill with Cross-Female Information";
	private static final String FILL_WITH_CROSS_FEMALE_GID = "Fill with Cross-Female GID";
	private static final String FILL_WITH_CROSS_FEMALE_PREF_NAME = "Fill with Cross-Female Preferred Name";
	private static final String FILL_WITH_CROSS_MALE_INFO = "Fill with Cross-Male Information";
	private static final String FILL_WITH_CROSS_MALE_GID = "Fill with Cross-Male GID";
	private static final String FILL_WITH_CROSS_MALE_PREF_NAME = "Fill with Cross-Male Preferred Name";

	@SuppressWarnings("unused")
	private boolean fromBuildNewList;
	private ListBuilderComponent buildNewListComponent;

	public static List<String> ADDABLE_PROPERTY_IDS;

	/**
	 * Add "Add column" context menu to a table
	 * 
	 * @param listDetailsComponent - tab content from list manager details section.
	 * @param sourceContextMenu - util will attach event listener to this
	 * @param targetTable - table where data will be manipulated
	 * @param gid - property of GID (button with GID as caption) on that table
	 */
	public AddColumnContextMenu(ListTabComponent listDetailsComponent, ContextMenu sourceContextMenu, Table targetTable, String gid) {
		this.listDetailsComponent = listDetailsComponent;
		this.gidPropertyId = gid;
		this.targetTable = targetTable;
		this.sourceContextMenu = sourceContextMenu;

		this.setupContextMenu();
	}

	/**
	 * Add "Add column" context menu to a table
	 * 
	 * @param cssLayoutSource - context menu will attach to this
	 * @param sourceContextMenu - util will attach event listener to this
	 * @param targetTable - table where data will be manipulated
	 * @param gid - property of GID (button with GID as caption) on that table
	 */
	public AddColumnContextMenu(ComponentContainer cssLayoutSource, ContextMenu sourceContextMenu, Table targetTable, String gid,
			boolean fromBuildNewList) {
		this.gidPropertyId = gid;
		this.targetTable = targetTable;
		this.sourceContextMenu = sourceContextMenu;
		this.cssLayoutSource = cssLayoutSource;
		this.fromBuildNewList = fromBuildNewList;

		if (fromBuildNewList) {
			this.buildNewListComponent = (ListBuilderComponent) cssLayoutSource;
		}

		this.setupContextMenu();

	}

	/**
	 * Add "Add column" context menu to a table
	 * 
	 * @param addColumnButton - util will attach event listener to this
	 * @param targetTable - table where data will be manipulated
	 * @param gid - property of GID (button with GID as caption) on that table
	 */
	public AddColumnContextMenu(Table targetTable, String gid) {
		this.gidPropertyId = gid;
		this.targetTable = targetTable;

		this.setupContextMenu();
	}

	private void setupContextMenu() {

		this.initializeAddableProperties();

		this.addColumnItem = this.sourceContextMenu.addItem(AddColumnContextMenu.ADD_COLUMN_MENU);
		this.menuFillWithPreferredId = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_PREFERRED_ID);
		this.menuFillWithPreferredName = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_PREFERRED_NAME);
		this.menuFillWithGermplasmDate = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_GERMPLASM_DATE);
		this.menuFillWithLocations = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_LOCATION);
		this.menuFillWithMethodInfo = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_METHOD_INFO);
		this.menuFillWithCrossFemaleInfo = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_CROSS_FEMALE_INFO);
		this.menuFillWithCrossMaleInfo = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_CROSS_MALE_INFO);

		// breeding method sub-options
		this.menuFillWithMethodName = this.menuFillWithMethodInfo.addItem(AddColumnContextMenu.FILL_WITH_METHOD_NAME);
		this.menuFillWithMethodAbbrev = this.menuFillWithMethodInfo.addItem(AddColumnContextMenu.FILL_WITH_METHOD_ABBREV);
		this.menuFillWithMethodNumber = this.menuFillWithMethodInfo.addItem(AddColumnContextMenu.FILL_WITH_METHOD_NUMBER);
		this.menuFillWithMethodGroup = this.menuFillWithMethodInfo.addItem(AddColumnContextMenu.FILL_WITH_METHOD_GROUP);

		// cross female sub-options
		this.menuFillWithCrossFemaleGID = this.menuFillWithCrossFemaleInfo.addItem(AddColumnContextMenu.FILL_WITH_CROSS_FEMALE_GID);
		this.menuFillWithCrossFemalePrefName =
				this.menuFillWithCrossFemaleInfo.addItem(AddColumnContextMenu.FILL_WITH_CROSS_FEMALE_PREF_NAME);

		// cross-male info sub-options
		this.menuFillWithCrossMaleGID = this.menuFillWithCrossMaleInfo.addItem(AddColumnContextMenu.FILL_WITH_CROSS_MALE_GID);
		this.menuFillWithCrossMalePrefName = this.menuFillWithCrossMaleInfo.addItem(AddColumnContextMenu.FILL_WITH_CROSS_MALE_PREF_NAME);

		this.sourceContextMenu.addListener(new SourceContextMenuClickListener());
	}

	public void initializeAddableProperties() {

		AddColumnContextMenu.ADDABLE_PROPERTY_IDS = new ArrayList<String>();

		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.PREFERRED_ID.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.PREFERRED_NAME.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.GERMPLASM_DATE.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.GERMPLASM_LOCATION.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.BREEDING_METHOD_NAME.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.BREEDING_METHOD_NUMBER.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.BREEDING_METHOD_GROUP.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.CROSS_FEMALE_GID.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.CROSS_MALE_GID.getName());
		AddColumnContextMenu.ADDABLE_PROPERTY_IDS.add(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
	}

	public void refreshAddColumnMenu() {
		// Check if columns already exist in the table
		if (this.propertyExists(ColumnLabels.PREFERRED_ID.getName())) {
			this.menuFillWithPreferredId.setEnabled(false);
		} else {
			this.menuFillWithPreferredId.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.PREFERRED_NAME.getName())) {
			this.menuFillWithPreferredName.setEnabled(false);
		} else {
			this.menuFillWithPreferredName.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.GERMPLASM_DATE.getName())) {
			this.menuFillWithGermplasmDate.setEnabled(false);
		} else {
			this.menuFillWithGermplasmDate.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.GERMPLASM_LOCATION.getName())) {
			this.menuFillWithLocations.setEnabled(false);
		} else {
			this.menuFillWithLocations.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
			this.menuFillWithMethodName.setEnabled(false);
		} else {
			this.menuFillWithMethodName.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
			this.menuFillWithMethodAbbrev.setEnabled(false);
		} else {
			this.menuFillWithMethodAbbrev.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
			this.menuFillWithMethodNumber.setEnabled(false);
		} else {
			this.menuFillWithMethodNumber.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			this.menuFillWithMethodGroup.setEnabled(false);
		} else {
			this.menuFillWithMethodGroup.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.BREEDING_METHOD_NAME.getName())
				&& this.propertyExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())
				&& this.propertyExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())
				&& this.propertyExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			this.menuFillWithMethodInfo.setEnabled(false);
		} else {
			this.menuFillWithMethodInfo.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.CROSS_FEMALE_GID.getName())) {
			this.menuFillWithCrossFemaleGID.setEnabled(false);
		} else {
			this.menuFillWithCrossFemaleGID.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			this.menuFillWithCrossFemalePrefName.setEnabled(false);
		} else {
			this.menuFillWithCrossFemalePrefName.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.CROSS_FEMALE_GID.getName())
				&& this.propertyExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			this.menuFillWithCrossFemaleInfo.setEnabled(false);
		} else {
			this.menuFillWithCrossFemaleInfo.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.CROSS_MALE_GID.getName())) {
			this.menuFillWithCrossMaleGID.setEnabled(false);
		} else {
			this.menuFillWithCrossMaleGID.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			this.menuFillWithCrossMalePrefName.setEnabled(false);
		} else {
			this.menuFillWithCrossMalePrefName.setEnabled(true);
		}

		if (this.propertyExists(ColumnLabels.CROSS_MALE_GID.getName())
				&& this.propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			this.menuFillWithCrossMaleInfo.setEnabled(false);
		} else {
			this.menuFillWithCrossMaleInfo.setEnabled(true);
		}

		this.sourceContextMenu.requestRepaint();
	}

	private void doFixForTruncatedDataInEditableTable() {
		if (this.targetTable.isEditable()) {
			this.targetTable.setEditable(false);
			this.targetTable.setEditable(true);
		}
	}

	private void markHasChangesFlags(boolean fromAddColumn) {
		// mark flag that changes have been made in listDataTable
		if (this.listDetailsComponent != null && fromAddColumn) {
			this.listDetailsComponent.getListComponent().setHasUnsavedChanges(true);
		}

		// mark flag that changes have been made in buildNewListTable
		if (this.buildNewListComponent != null) {
			this.buildNewListComponent.setHasUnsavedChanges(true);
		}
	}

	private void addPreferredIdColumn() {
		if (!this.propertyExists(ColumnLabels.PREFERRED_ID.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.PREFERRED_ID.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.PREFERRED_ID.getName(),
					ColumnLabels.PREFERRED_ID.getTermNameFromOntology(this.ontologyDataManager));
			this.setPreferredIdColumnValues(true);
		}
	}

	public void setPreferredIdColumnValues(boolean fromAddColumn) {
		if (this.propertyExists(ColumnLabels.PREFERRED_ID.getName())) {
			try {
				List<Integer> itemIds = this.getItemIds(this.targetTable);
				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());
					String preferredID = "";
					Name name = this.germplasmDataManager.getPreferredIdByGID(gid);
					if (name != null && name.getNval() != null) {
						preferredID = name.getNval();
					}
					this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.PREFERRED_ID.getName()).setValue(preferredID);
				}

				// To trigger TableFieldFactory (fix for truncated data)
				this.doFixForTruncatedDataInEditableTable();

				this.markHasChangesFlags(fromAddColumn);
			} catch (MiddlewareQueryException e) {
				AddColumnContextMenu.LOG.error("Error in filling with preferred id values.", e);
			}
		}
	}

	private void addPreferredNameColumn() {
		if (!this.propertyExists(ColumnLabels.PREFERRED_NAME.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.PREFERRED_NAME.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.PREFERRED_NAME.getName(),
					ColumnLabels.PREFERRED_NAME.getTermNameFromOntology(this.ontologyDataManager));
			this.setPreferredNameColumnValues(true);
		}
	}

	public void setPreferredNameColumnValues(boolean fromAddColumn) {
		if (this.propertyExists(ColumnLabels.PREFERRED_NAME.getName())) {
			try {
				List<Integer> itemIds = this.getItemIds(this.targetTable);
				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());

					String preferredName = "";
					if (this.germplasmDataManager.getPreferredNameByGID(gid) != null
							&& this.germplasmDataManager.getPreferredNameByGID(gid).getNval() != null) {
						preferredName = this.germplasmDataManager.getPreferredNameByGID(gid).getNval();
					}
					this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.PREFERRED_NAME.getName()).setValue(preferredName);
				}

				// To trigger TableFieldFactory (fix for truncated data)
				this.doFixForTruncatedDataInEditableTable();

				this.markHasChangesFlags(fromAddColumn);

			} catch (MiddlewareQueryException e) {
				AddColumnContextMenu.LOG.error("Error in filling with preferred name values.", e);
			}
		}
	}

	private void addGermplasmDateColumn() {
		if (!this.propertyExists(ColumnLabels.GERMPLASM_DATE.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.GERMPLASM_DATE.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.GERMPLASM_DATE.getName(),
					ColumnLabels.GERMPLASM_DATE.getTermNameFromOntology(this.ontologyDataManager));
			// can create separate method for adding container property and the actual setting of column values,
			// so that the middleware call below can be called only once without having the gids become null
			this.setGermplasmDateColumnValues(true);
		}
	}

	public void setGermplasmDateColumnValues(boolean fromAddColumn) {
		if (this.propertyExists(ColumnLabels.GERMPLASM_DATE.getName())) {
			try {
				List<Integer> itemIds = this.getItemIds(this.targetTable);

				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());

					List<Integer> gids = new ArrayList<Integer>();
					gids.add(gid);

					// can make better use of the middleware method by just calling it once and not have it inside a loop
					Map<Integer, Integer> germplasmGidDateMap = this.germplasmDataManager.getGermplasmDatesByGids(gids);

					if (germplasmGidDateMap.get(gid) == null) {
						this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.GERMPLASM_DATE.getName()).setValue("");
					} else {
						this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.GERMPLASM_DATE.getName())
								.setValue(germplasmGidDateMap.get(gid));
					}
				}

				// To trigger TableFieldFactory (fix for truncated data)
				this.doFixForTruncatedDataInEditableTable();

				this.markHasChangesFlags(fromAddColumn);

			} catch (MiddlewareQueryException e) {
				AddColumnContextMenu.LOG.error("Error in filling with Germplasm Date values.", e);
			}
		}
	}

	private void addLocationColumn() {
		if (!this.propertyExists(ColumnLabels.GERMPLASM_LOCATION.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.GERMPLASM_LOCATION.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName(),
					ColumnLabels.GERMPLASM_LOCATION.getTermNameFromOntology(this.ontologyDataManager));
			this.setLocationColumnValues(true);
		}
	}

	public void setLocationColumnValues(boolean fromAddColumn) {
		if (this.propertyExists(ColumnLabels.GERMPLASM_LOCATION.getName())) {
			try {
				List<Integer> itemIds = this.getItemIds(this.targetTable);

				final Map<Integer, String> allLocationNamesMap = new HashMap<Integer, String>();

				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());

					List<Integer> gids = new ArrayList<Integer>();
					gids.add(gid);

					Map<Integer, String> locationNamesMap = this.germplasmDataManager.getLocationNamesByGids(gids);
					allLocationNamesMap.putAll(locationNamesMap);

					if (locationNamesMap.get(gid) == null) {
						this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName()).setValue("");
					} else {
						this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.GERMPLASM_LOCATION.getName())
								.setValue(locationNamesMap.get(gid));
					}
				}

				// To trigger TableFieldFactory (fix for truncated data)
				this.doFixForTruncatedDataInEditableTable();

				this.markHasChangesFlags(fromAddColumn);

			} catch (MiddlewareQueryException e) {
				AddColumnContextMenu.LOG.error("Error in filling with Location values.", e);
			}
		}
	}

	private void addMethodNameColumn() {
		if (!this.propertyExists(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NAME.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName(),
					ColumnLabels.BREEDING_METHOD_NAME.getTermNameFromOntology(this.ontologyDataManager));
			this.setMethodInfoColumnValues(true, ColumnLabels.BREEDING_METHOD_NAME.getName());
		}
	}

	private void addMethodAbbrevColumn() {
		if (!this.propertyExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
					ColumnLabels.BREEDING_METHOD_ABBREVIATION.getTermNameFromOntology(this.ontologyDataManager));
			this.setMethodInfoColumnValues(true, ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
		}
	}

	private void addMethodNumberColumn() {
		if (!this.propertyExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NUMBER.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_NUMBER.getName(),
					ColumnLabels.BREEDING_METHOD_NUMBER.getTermNameFromOntology(this.ontologyDataManager));
			this.setMethodInfoColumnValues(true, ColumnLabels.BREEDING_METHOD_NUMBER.getName());
		}
	}

	private void addMethodGroupColumn() {
		if (!this.propertyExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_GROUP.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_GROUP.getName(),
					ColumnLabels.BREEDING_METHOD_GROUP.getTermNameFromOntology(this.ontologyDataManager));
			this.setMethodInfoColumnValues(true, ColumnLabels.BREEDING_METHOD_GROUP.getName());
		}
	}

	public void setMethodInfoColumnValues(boolean fromAddColumn, String columnName) {
		if (this.propertyExists(columnName)) {
			try {
				List<Integer> itemIds = this.getItemIds(this.targetTable);

				final Map<Integer, Object> allMethodsMap = new HashMap<Integer, Object>();

				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());

					List<Integer> gids = new ArrayList<Integer>();
					gids.add(gid);

					Map<Integer, Object> methodsMap = this.germplasmDataManager.getMethodsByGids(gids);
					allMethodsMap.putAll(methodsMap);

					if (methodsMap.get(gid) == null) {
						this.targetTable.getItem(itemId).getItemProperty(columnName).setValue("");
					} else {
						String value = "";

						if (columnName.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
							value = ((Method) methodsMap.get(gid)).getMname();
						} else if (columnName.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
							value = ((Method) methodsMap.get(gid)).getMcode();
						} else if (columnName.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
							value = ((Method) methodsMap.get(gid)).getMid().toString();
						} else if (columnName.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
							value = ((Method) methodsMap.get(gid)).getMgrp();
						}

						this.targetTable.getItem(itemId).getItemProperty(columnName).setValue(value);
					}
				}

				// To trigger TableFieldFactory (fix for truncated data)
				this.doFixForTruncatedDataInEditableTable();

				this.markHasChangesFlags(fromAddColumn);

			} catch (MiddlewareQueryException e) {
				AddColumnContextMenu.LOG.error("Error in filling with Method Info values.", e);
			}
		}
	}

	private void addCrossMaleGIDColumn() {
		if (!this.propertyExists(ColumnLabels.CROSS_MALE_GID.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.CROSS_MALE_GID.getName(), String.class, "-");
			this.targetTable.setColumnHeader(ColumnLabels.CROSS_MALE_GID.getName(),
					ColumnLabels.CROSS_MALE_GID.getTermNameFromOntology(this.ontologyDataManager));
			this.setCrossMaleGIDColumnValues(true);
		}
	}

	public void setCrossMaleGIDColumnValues(boolean fromAddColumn) {
		if (this.propertyExists(ColumnLabels.CROSS_MALE_GID.getName())) {
			try {
				List<Integer> itemIds = this.getItemIds(this.targetTable);

				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());

					Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);

					if (germplasm != null) {
						if (germplasm.getGnpgs() >= 2) {
							if (germplasm.getGpid2() != null && germplasm.getGpid2() != 0) {
								this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_GID.getName())
										.setValue(germplasm.getGpid2().toString());
							} else {
								this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_GID.getName()).setValue("-");
							}
						} else {
							this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_GID.getName()).setValue("-");
						}
					} else {
						this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_GID.getName()).setValue("-");
					}
				}

				// To trigger TableFieldFactory (fix for truncated data)
				this.doFixForTruncatedDataInEditableTable();

				this.markHasChangesFlags(fromAddColumn);

			} catch (MiddlewareQueryException e) {
				AddColumnContextMenu.LOG.error("Error in filling with Cross-Male GID values.", e);
			}
		}
	}

	private void addCrossMalePrefNameColumn() {
		if (!this.propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), String.class, "-");
			this.targetTable.setColumnHeader(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(),
					ColumnLabels.CROSS_MALE_PREFERRED_NAME.getTermNameFromOntology(this.ontologyDataManager));
			this.setCrossMalePrefNameColumnValues(true);
		}
	}

	public void setCrossMalePrefNameColumnValues(boolean fromAddColumn) {
		if (this.propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			try {
				List<Integer> itemIds = this.getItemIds(this.targetTable);
				Map<Integer, List<Integer>> gidToItemIdMap = new HashMap<Integer, List<Integer>>();
				List<Integer> gidsToUseForQuery = new ArrayList<Integer>();

				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());

					Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);

					if (germplasm != null) {
						if (germplasm.getGnpgs() >= 2 && germplasm.getGpid2() != null && germplasm.getGpid2() != 0) {
							gidsToUseForQuery.add(germplasm.getGpid2());
							List<Integer> itemIdsInMap = gidToItemIdMap.get(germplasm.getGpid2());
							if (itemIdsInMap == null) {
								itemIdsInMap = new ArrayList<Integer>();
								itemIdsInMap.add(itemId);
								gidToItemIdMap.put(germplasm.getGpid2(), itemIdsInMap);
							} else {
								itemIdsInMap.add(itemId);
							}
						} else {
							this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())
									.setValue("-");
						}
					} else {
						this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).setValue("-");
					}
				}

				if (!gidsToUseForQuery.isEmpty()) {
					Map<Integer, String> gidToNameMap = this.germplasmDataManager.getPreferredNamesByGids(gidsToUseForQuery);

					for (Integer gid : gidToNameMap.keySet()) {
						String prefName = gidToNameMap.get(gid);
						List<Integer> itemIdsInMap = gidToItemIdMap.get(gid);
						for (Integer itemId : itemIdsInMap) {
							this.targetTable.getItem(itemId).getItemProperty(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())
									.setValue(prefName);
						}
					}
				}

				// To trigger TableFieldFactory (fix for truncated data)
				this.doFixForTruncatedDataInEditableTable();

				this.markHasChangesFlags(fromAddColumn);

			} catch (MiddlewareQueryException e) {
				AddColumnContextMenu.LOG.error("Error in filling with Cross-Male Preferred Name values.", e);
			}
		}
	}

	private void addCrossFemaleGidColumn() {
		if (!this.propertyExists(ColumnLabels.CROSS_FEMALE_GID.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.CROSS_FEMALE_GID.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.CROSS_FEMALE_GID.getName(),
					ColumnLabels.CROSS_FEMALE_GID.getTermNameFromOntology(this.ontologyDataManager));
			this.setCrossFemaleInfoColumnValues(true, ColumnLabels.CROSS_FEMALE_GID.getName());
		}
	}

	private void addCrossFemalePrefNameColumn() {
		if (!this.propertyExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			this.targetTable.addContainerProperty(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(), String.class, "");
			this.targetTable.setColumnHeader(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(),
					ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getTermNameFromOntology(this.ontologyDataManager));
			this.setCrossFemaleInfoColumnValues(true, ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
		}
	}

	public void setCrossFemaleInfoColumnValues(boolean fromAddColumn, String columnName) {
		if (this.propertyExists(columnName)) {
			try {
				List<Integer> itemIds = this.getItemIds(this.targetTable);

				for (Integer itemId : itemIds) {
					Integer gid =
							Integer.valueOf(((Button) this.targetTable.getItem(itemId).getItemProperty(this.gidPropertyId).getValue())
									.getCaption().toString());

					Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(gid);
					Germplasm femaleParent = null;
					// get female only if germplasm is created via generative process
					if (germplasm.getGnpgs() >= 2) {
						femaleParent = this.germplasmDataManager.getGermplasmByGID(germplasm.getGpid1());
					}

					if (femaleParent == null) {
						this.targetTable.getItem(itemId).getItemProperty(columnName).setValue("-");
					} else {
						String value = "-";
						if (columnName.equals(ColumnLabels.CROSS_FEMALE_GID.getName())) {
							value = femaleParent.getGid().toString();
						} else if (columnName.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
							Name prefName = this.germplasmDataManager.getPreferredNameByGID(femaleParent.getGid());
							if (prefName != null) {
								value = prefName.getNval();
							}
						}
						this.targetTable.getItem(itemId).getItemProperty(columnName).setValue(value);
					}
				}

				// To trigger TableFieldFactory (fix for truncated data)
				this.doFixForTruncatedDataInEditableTable();

				this.markHasChangesFlags(fromAddColumn);

			} catch (MiddlewareQueryException e) {
				AddColumnContextMenu.LOG.error("Error in filling with Cross Female Info values.", e);
			}
		}
	}

	public Boolean propertyExists(String propertyId) {
		List<String> propertyIds = AddColumnContextMenu.getTablePropertyIds(this.targetTable);
		return propertyIds.contains(propertyId);
	}

	public static Boolean propertyExists(String propertyId, Table table) {
		List<String> propertyIds = AddColumnContextMenu.getTablePropertyIds(table);
		return propertyIds.contains(propertyId);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getTablePropertyIds(Table table) {
		if (table != null) {
			List<String> propertyIds = new ArrayList<String>();
			propertyIds.addAll((Collection<? extends String>) table.getContainerPropertyIds());
			return propertyIds;
		} else {
			return new ArrayList<String>();
		}
	}

	public List<Integer> getGidsFromTable(Table table) {
		List<Integer> gids = new ArrayList<Integer>();
		List<Integer> listDataItemIds = this.getItemIds(table);
		for (Integer itemId : listDataItemIds) {
			gids.add(Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(this.gidPropertyId).getValue()).getCaption()
					.toString()));
		}
		return gids;
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getItemIds(Table table) {
		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
		return itemIds;
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	/**
	 * Save erases all values on the table, including the added columns, use this to re-populate it with data
	 */
	public void populateAddedColumns() {
		for (String propertyId : AddColumnContextMenu.ADDABLE_PROPERTY_IDS) {
			if (this.propertyExists(propertyId)) {
				if (propertyId.equals(ColumnLabels.PREFERRED_ID.getName())) {
					this.setPreferredIdColumnValues(false);
				} else if (propertyId.equals(ColumnLabels.PREFERRED_NAME.getName())) {
					this.setPreferredNameColumnValues(false);
				} else if (propertyId.equals(ColumnLabels.GERMPLASM_DATE.getName())) {
					this.setGermplasmDateColumnValues(false);
				} else if (propertyId.equals(ColumnLabels.GERMPLASM_LOCATION.getName())) {
					this.setLocationColumnValues(false);
				} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
					this.setMethodInfoColumnValues(false, ColumnLabels.BREEDING_METHOD_NAME.getName());
				} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
					this.setMethodInfoColumnValues(false, ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
				} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
					this.setMethodInfoColumnValues(false, ColumnLabels.BREEDING_METHOD_NUMBER.getName());
				} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
					this.setMethodInfoColumnValues(false, ColumnLabels.BREEDING_METHOD_GROUP.getName());
				} else if (propertyId.equals(ColumnLabels.CROSS_FEMALE_GID.getName())) {
					this.setCrossFemaleInfoColumnValues(false, ColumnLabels.CROSS_FEMALE_GID.getName());
				} else if (propertyId.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
					this.setCrossFemaleInfoColumnValues(false, ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
				} else if (propertyId.equals(ColumnLabels.CROSS_MALE_GID.getName())) {
					this.setCrossMaleGIDColumnValues(false);
				} else if (propertyId.equals(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
					this.setCrossMalePrefNameColumnValues(false);
				}

			}
		}
	}

	/**
	 * This has to be called after the list entries has been saved, because it'll need the germplasmListEntryId
	 * 
	 * @return
	 */
	public List<ListDataInfo> getListDataCollectionFromTable(Table table) {
		List<ListDataInfo> listDataCollection = new ArrayList<ListDataInfo>();
		List<String> propertyIds = AddColumnContextMenu.getTablePropertyIds(table);

		for (Object itemId : table.getItemIds()) {
			Item item = table.getItem(itemId);
			List<ListDataColumn> columns = new ArrayList<ListDataColumn>();
			for (String propertyId : propertyIds) {
				if (AddColumnContextMenu.ADDABLE_PROPERTY_IDS.contains(propertyId)) {
					if (item.getItemProperty(propertyId).getValue() != null) {
						columns.add(new ListDataColumn(propertyId, item.getItemProperty(propertyId).getValue().toString()));
					} else {
						columns.add(new ListDataColumn(propertyId, null));
					}
				}
			}
			listDataCollection.add(new ListDataInfo(Integer.valueOf(itemId.toString()), columns));
		}

		return listDataCollection;
	}

	/**
	 * This can be used to add columns given a property ID (should be one of the addable ID's)
	 */
	public void addColumn(String propertyId) {
		if (propertyId.equals(ColumnLabels.PREFERRED_ID.getName())) {
			this.addPreferredIdColumn();
		} else if (propertyId.equals(ColumnLabels.PREFERRED_NAME.getName())) {
			this.addPreferredNameColumn();
		} else if (propertyId.equals(ColumnLabels.GERMPLASM_DATE.getName())) {
			this.addGermplasmDateColumn();
		} else if (propertyId.equals(ColumnLabels.GERMPLASM_LOCATION.getName())) {
			this.addLocationColumn();
		} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_NAME.getName())) {
			this.addMethodNameColumn();
		} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())) {
			this.addMethodAbbrevColumn();
		} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_NUMBER.getName())) {
			this.addMethodNumberColumn();
		} else if (propertyId.equals(ColumnLabels.BREEDING_METHOD_GROUP.getName())) {
			this.addMethodGroupColumn();
		} else if (propertyId.equals(ColumnLabels.CROSS_FEMALE_GID.getName())) {
			this.addCrossFemaleGidColumn();
		} else if (propertyId.equals(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())) {
			this.addCrossFemalePrefNameColumn();
		} else if (propertyId.equals(ColumnLabels.CROSS_MALE_GID.getName())) {
			this.addCrossMaleGIDColumn();
		} else if (propertyId.equals(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())) {
			this.addCrossMalePrefNameColumn();
		}

	}

	public void showHideAddColumnMenu(boolean visible) {
		this.addColumnItem.setVisible(visible);
		this.sourceContextMenu.requestRepaint();
	}

	public void setEnabled(Boolean state) {
		this.addColumnItem.setEnabled(state);
	}

	public void setVisible(Boolean state) {
		this.addColumnItem.setVisible(state);
	}
}
