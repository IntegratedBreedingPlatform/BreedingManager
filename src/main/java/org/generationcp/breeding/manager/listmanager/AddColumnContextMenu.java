
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
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
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

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
			} else if (clickedItem.getName().equals(AddColumnContextMenu.FILL_WITH_ATTRIBUTE)) {
				AddColumnContextMenu.this.displayFillWithAttributeWindow();
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
	private static final String FILL_WITH_ATTRIBUTE = "Fill with Attribute";

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
		//Adding new ContextMenuItem As ListEditingOption In which Add Column Will be Sub Menu
		this.listEditingOptions = listEditingOption;
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
		final List<FillWithOption> columnsToExclude = this.addColumnSource.getColumnsToExclude();
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_PREFERRED_ID)) {
			this.menuFillWithPreferredId = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_PREFERRED_ID);
		}
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_PREFERRED_NAME)) {
			this.menuFillWithPreferredName = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_PREFERRED_NAME);
		}
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_GERMPLASM_DATE)) {
			this.menuFillWithGermplasmDate = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_GERMPLASM_DATE);
		}
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_LOCATION)) {
			this.menuFillWithLocations = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_LOCATION);
		}

		// Breeding method Info and its sub-options. Excluded sub-options will be visible but disabled
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO)) {
			this.menuFillWithMethodInfo = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_METHOD_INFO);
			
			final boolean doExcludeBreedingMethodName = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
			this.menuFillWithMethodName = this.menuFillWithMethodInfo.addItem(AddColumnContextMenu.FILL_WITH_METHOD_NAME);
			this.menuFillWithMethodName.setEnabled(!doExcludeBreedingMethodName);

			final boolean doExcludeBreedingMethodAbbrev = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
			this.menuFillWithMethodAbbrev = this.menuFillWithMethodInfo.addItem(AddColumnContextMenu.FILL_WITH_METHOD_ABBREV);
			this.menuFillWithMethodAbbrev.setEnabled(!doExcludeBreedingMethodAbbrev);

			final boolean doExcludeBreedingMethodNumber = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
			this.menuFillWithMethodNumber = this.menuFillWithMethodInfo.addItem(AddColumnContextMenu.FILL_WITH_METHOD_NUMBER);
			this.menuFillWithMethodNumber.setEnabled(!doExcludeBreedingMethodNumber);
			
			final boolean doExcludeBreedingMethodGroup = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
			this.menuFillWithMethodGroup = this.menuFillWithMethodInfo.addItem(AddColumnContextMenu.FILL_WITH_METHOD_GROUP);
			this.menuFillWithMethodGroup.setEnabled(!doExcludeBreedingMethodGroup);
		}
		
		// Cross Female Info and its sub-options. Excluded sub-options will be visible but disabled
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO)) {
			this.menuFillWithCrossFemaleInfo = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_CROSS_FEMALE_INFO);
			
			final boolean doExcludeCrossFemaleGid = columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
			this.menuFillWithCrossFemaleGID = this.menuFillWithCrossFemaleInfo.addItem(AddColumnContextMenu.FILL_WITH_CROSS_FEMALE_GID);
			this.menuFillWithCrossFemaleGID.setEnabled(!doExcludeCrossFemaleGid);
			
			final boolean doExcludeCrossFemaleName = columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
			this.menuFillWithCrossFemalePrefName =
					this.menuFillWithCrossFemaleInfo.addItem(AddColumnContextMenu.FILL_WITH_CROSS_FEMALE_PREF_NAME);
			this.menuFillWithCrossFemalePrefName.setEnabled(!doExcludeCrossFemaleName);
		}
			
		// Cross Male Info and its sub-options. Excluded sub-options will be visible but disabled
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_MALE_INFO)) {
			this.menuFillWithCrossMaleInfo = this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_CROSS_MALE_INFO);
			
			final boolean doExcludeCrossMaleGid = columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_MALE_GID);
			this.menuFillWithCrossMaleGID = this.menuFillWithCrossMaleInfo.addItem(AddColumnContextMenu.FILL_WITH_CROSS_MALE_GID);
			this.menuFillWithCrossMaleGID.setEnabled(!doExcludeCrossMaleGid);
			
			final boolean doExcludeCrossMaleName = columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_MALE_NAME);
			this.menuFillWithCrossMalePrefName = this.menuFillWithCrossMaleInfo.addItem(AddColumnContextMenu.FILL_WITH_CROSS_MALE_PREF_NAME);
			this.menuFillWithCrossMalePrefName.setEnabled(!doExcludeCrossMaleName);
		}
		
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_ATTRIBUTE)) {
			this.addColumnItem.addItem(AddColumnContextMenu.FILL_WITH_ATTRIBUTE);
		}

		this.sourceContextMenu.addListener(new SourceContextMenuClickListener());
	}

	public void initializeAddableProperties() {

		AddColumnContextMenu.ADDABLE_PROPERTY_IDS = new ArrayList<>();

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
			this.valuesGenerator.setPreferredIdColumnValues(ColumnLabels.PREFERRED_ID.getName());
		}
	}

	private void addPreferredNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.PREFERRED_NAME.getName())){
			this.addColumnSource.addColumn(ColumnLabels.PREFERRED_NAME);
			this.valuesGenerator.setPreferredNameColumnValues(ColumnLabels.PREFERRED_NAME.getName());
		}
	}

	private void addGermplasmDateColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GERMPLASM_DATE.getName())){
			this.addColumnSource.addColumn(ColumnLabels.GERMPLASM_DATE);
			this.valuesGenerator.setGermplasmDateColumnValues(ColumnLabels.GERMPLASM_DATE.getName());
		}
	}

	private void addLocationColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.GERMPLASM_LOCATION.getName())){
			this.addColumnSource.addColumn(ColumnLabels.GERMPLASM_LOCATION);
			this.valuesGenerator.setLocationNameColumnValues(ColumnLabels.GERMPLASM_LOCATION.getName());
		}
	}
	
	private void addMethodNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_NAME.getName())){
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_NAME);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NAME.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
		}
	}

	private void addMethodAbbrevColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName())){
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_ABBREVIATION);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
		}
	}

	private void addMethodNumberColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName())){
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_NUMBER);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NUMBER.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
		}
	}

	private void addMethodGroupColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.BREEDING_METHOD_GROUP.getName())){
			this.addColumnSource.addColumn(ColumnLabels.BREEDING_METHOD_GROUP);
			this.valuesGenerator.setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_GROUP.getName(),
					FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
		}
	}

	private void addCrossMaleGIDColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_MALE_GID.getName())){
			this.addColumnSource.addColumn(ColumnLabels.CROSS_MALE_GID);
			this.valuesGenerator.setCrossMaleGIDColumnValues(ColumnLabels.CROSS_MALE_GID.getName());
		}
	}

	private void addCrossMalePrefNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName())){
			this.addColumnSource.addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);
			this.valuesGenerator.setCrossMalePrefNameColumnValues(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
		}
	}

	private void addCrossFemaleGidColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_FEMALE_GID.getName())){
			this.addColumnSource.addColumn(ColumnLabels.CROSS_FEMALE_GID);
			this.valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_GID.getName(),
					FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
		}
	}

	private void addCrossFemalePrefNameColumn() {
		if (!this.addColumnSource.columnExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())){
			this.addColumnSource.addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);
			this.valuesGenerator.setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(),
					FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
		}
	}
	
	private void displayFillWithAttributeWindow() {
		final Window mainWindow = this.addColumnSource.getWindow();
		// 2nd parameter is null because user is yet to select the attribute type, which will become column name
		Window attributeWindow = new FillWithAttributeWindow(this.addColumnSource, null);
		attributeWindow.setStyleName(Reindeer.WINDOW_LIGHT);
		mainWindow.addWindow(attributeWindow);
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
	
	public static boolean sourceHadAddedColumn(final Object[] visibleColumns){
		for (final Object column : visibleColumns) {
			if (AddColumnContextMenu.ADDABLE_PROPERTY_IDS.contains(column.toString())) {
				return true;
			}
		}
		return false;
	}
}
