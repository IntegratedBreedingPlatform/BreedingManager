
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.domain.gms.ListDataColumn;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
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
	private ContextMenuItem listEditingOptions;
	private static final String ADD_COLUMN_MENU = "Add column";
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

	private GermplasmColumnValuesGenerator valuesGenerator;
	private AddColumnSource addColumnSource;

	public static List<String> ADDABLE_PROPERTY_IDS;

	/**
	 * Add "Add column" context menu to a table
	 * 
	 * @param addColumnSource - source component where AddColumn was called from
	 * @param sourceContextMenu - util will attach event listener to this
	 */
	public AddColumnContextMenu(AddColumnSource addColumnSource, ContextMenu sourceContextMenu, ContextMenuItem listEditingOption) {
		this.addColumnSource = addColumnSource;
		this.valuesGenerator = new GermplasmColumnValuesGenerator(addColumnSource);
		this.sourceContextMenu = sourceContextMenu;
		this.listEditingOptions = listEditingOption;//Adding new ContextMenuItem As ListEditingOption In which Add Column Will be Sub Menu
		this.setupContextMenu();
	}

	private void setupContextMenu() {

		this.initializeAddableProperties();

		//Adding it to List Editing Option instead of main menu
		if(this.listEditingOptions != null){
			this.addColumnItem = this.listEditingOptions.addItem(AddColumnContextMenu.ADD_COLUMN_MENU);
		} else {
			this.addColumnItem = this.sourceContextMenu.addItem(AddColumnContextMenu.ADD_COLUMN_MENU);
		}
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

	public void refreshAddColumnMenu(final Table table) {
		// Disable menu items for columns already in the table
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.PREFERRED_ID, this.menuFillWithPreferredId);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.PREFERRED_NAME, this.menuFillWithPreferredName);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.GERMPLASM_DATE, this.menuFillWithGermplasmDate);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.GERMPLASM_LOCATION, this.menuFillWithLocations);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.BREEDING_METHOD_NAME, this.menuFillWithMethodName);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.BREEDING_METHOD_ABBREVIATION, this.menuFillWithMethodAbbrev);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.BREEDING_METHOD_NUMBER, this.menuFillWithMethodNumber);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.BREEDING_METHOD_GROUP, this.menuFillWithMethodGroup);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.CROSS_FEMALE_GID, this.menuFillWithCrossFemaleGID);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.CROSS_FEMALE_PREFERRED_NAME, this.menuFillWithCrossFemalePrefName);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.CROSS_MALE_GID, this.menuFillWithCrossMaleGID);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.CROSS_MALE_PREFERRED_NAME, this.menuFillWithCrossMalePrefName);

		// Disable main "Breeding Method Information" menu item if columns were added for all sub-menu items	
		if (AddColumnContextMenu.propertyExists(ColumnLabels.BREEDING_METHOD_NAME.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.BREEDING_METHOD_GROUP.getName(),table)) {
			this.menuFillWithMethodInfo.setEnabled(false);
		} else {
			this.menuFillWithMethodInfo.setEnabled(true);
		}

		// Disable main "Cross Female Information" menu item if columns were added for all sub-menu items	
		if (AddColumnContextMenu.propertyExists(ColumnLabels.CROSS_FEMALE_GID.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(), table)) {
			this.menuFillWithCrossFemaleInfo.setEnabled(false);
		} else {
			this.menuFillWithCrossFemaleInfo.setEnabled(true);
		}

		// Disable main "Cross Male Information" menu item if columns were added for all sub-menu items	
		if (AddColumnContextMenu.propertyExists(ColumnLabels.CROSS_MALE_GID.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), table)) {
			this.menuFillWithCrossMaleInfo.setEnabled(false);
		} else {
			this.menuFillWithCrossMaleInfo.setEnabled(true);
		}

		this.sourceContextMenu.requestRepaint();
	}

	private void disableMenuItemIfColumnAlreadyExists(final Table table, final ColumnLabels columnLabel, final ContextMenuItem menuItem) {
		if (AddColumnContextMenu.propertyExists(columnLabel.getName(), table)) {
			menuItem.setEnabled(false);
		} else {
			menuItem.setEnabled(true);
		}
	}

	private void addPreferredIdColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.PREFERRED_ID.getName())){
			this.addColumnSource.addColumn(ColumnLabels.PREFERRED_ID);
			this.valuesGenerator.setPreferredIdColumnValues();
		}
	}

	private void addPreferredNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.PREFERRED_NAME.getName())){
			this.addColumnSource.addColumn(ColumnLabels.PREFERRED_NAME);
			this.valuesGenerator.setPreferredNameColumnValues();
		}
	}

	private void addGermplasmDateColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GERMPLASM_DATE.getName())){
			this.addColumnSource.addColumn(ColumnLabels.GERMPLASM_DATE);
			this.valuesGenerator.setGermplasmDateColumnValues();
		}
	}

	private void addLocationColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GERMPLASM_LOCATION.getName())){
			this.addColumnSource.addColumn(ColumnLabels.GERMPLASM_LOCATION);
			this.valuesGenerator.setLocationNameColumnValues();
		}
	}
	
	private void addMethodNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_NAME.getName())){
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_NAME);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NAME.getName());
		}
	}

	private void addMethodAbbrevColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())){
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_ABBREVIATION);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
		}
	}

	private void addMethodNumberColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())){
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_NUMBER);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NUMBER.getName());
		}
	}

	private void addMethodGroupColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())){
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_GROUP);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_GROUP.getName());
		}
	}

	private void addCrossMaleGIDColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_MALE_GID.getName())){
			this.addColumnSource.addColumn(ColumnLabels.CROSS_MALE_GID);
			this.valuesGenerator.setCrossMaleGIDColumnValues();
		}
	}

	private void addCrossMalePrefNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
			this.addColumnSource.addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);
			this.valuesGenerator.setCrossMalePrefNameColumnValues();
		}
	}

	private void addCrossFemaleGidColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_FEMALE_GID.getName())){
			this.addColumnSource.addColumn(ColumnLabels.CROSS_FEMALE_GID);
			this.valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_GID.getName());
		}
	}

	private void addCrossFemalePrefNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())){
			this.addColumnSource.addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);
			this.valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
		}
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

	@Override
	public void updateLabels() {
		// do nothing
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
