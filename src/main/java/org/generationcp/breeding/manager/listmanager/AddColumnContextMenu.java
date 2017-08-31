
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.AddColumnMenuItemClickListener;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.gms.ListDataColumn;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

@Configurable
public class AddColumnContextMenu implements InternationalizableComponent {

	private SimpleResourceBundleMessageSource messageSource;

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

	private AddColumnSource addColumnSource;

	public static List<String> ADDABLE_PROPERTY_IDS;

	/**
	 * Add "Add column" context menu to a parent context menu item
	 * 
	 * @param addColumnSource - source component where AddColumn was called from
	 * @param sourceContextMenu - parent context menu object
	 * @param listEditingOption - parent context menu item to attach to
	 * @param messageSource - internationalized message resource bundle
	 */
	public AddColumnContextMenu(final AddColumnSource addColumnSource, final ContextMenu sourceContextMenu,
			final ContextMenuItem listEditingOption, final SimpleResourceBundleMessageSource messageSource) {
		this.addColumnSource = addColumnSource;
		this.sourceContextMenu = sourceContextMenu;
		// Adding new ContextMenuItem As ListEditingOption In which Add Column Will be Sub Menu
		this.listEditingOptions = listEditingOption;
		this.messageSource = messageSource;
		this.setupContextMenu();
	}

	private void setupContextMenu() {

		this.initializeAddableProperties();

		// Adding it to List Editing Option instead of main menu
		if (this.listEditingOptions != null) {
			this.addColumnItem = this.listEditingOptions.addItem(this.messageSource.getMessage(Message.ADD_COLUMN));
		} else {
			this.addColumnItem = this.sourceContextMenu.addItem(this.messageSource.getMessage(Message.ADD_COLUMN));
		}
		
		this.setupSubMenuItems();

		this.sourceContextMenu.addListener(new AddColumnMenuItemClickListener(this.addColumnSource));
	}

	void setupSubMenuItems() {
		final List<FillWithOption> columnsToExclude = this.addColumnSource.getColumnsToExclude();
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_PREFERRED_ID)) {
			this.menuFillWithPreferredId = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_PREFERRED_ID);
		}
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_PREFERRED_NAME)) {
			this.menuFillWithPreferredName = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_PREFERRED_NAME);
		}
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_GERMPLASM_DATE)) {
			this.menuFillWithGermplasmDate = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_GERMPLASM_DATE);
		}
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_LOCATION)) {
			this.menuFillWithLocations = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_LOCATION);
		}
		
		// Breeding method Info and its sub-options. Excluded sub-options will be visible but disabled
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO)) {
			this.menuFillWithMethodInfo = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO);

			final boolean doExcludeBreedingMethodName = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
			this.menuFillWithMethodName =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME, this.menuFillWithMethodInfo);
			this.menuFillWithMethodName.setEnabled(!doExcludeBreedingMethodName);

			final boolean doExcludeBreedingMethodAbbrev = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
			this.menuFillWithMethodAbbrev =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV, this.menuFillWithMethodInfo);
			this.menuFillWithMethodAbbrev.setEnabled(!doExcludeBreedingMethodAbbrev);

			final boolean doExcludeBreedingMethodNumber = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
			this.menuFillWithMethodNumber  =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER, this.menuFillWithMethodInfo);
			this.menuFillWithMethodNumber.setEnabled(!doExcludeBreedingMethodNumber);

			final boolean doExcludeBreedingMethodGroup = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
			this.menuFillWithMethodGroup  =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP, this.menuFillWithMethodInfo);
			this.menuFillWithMethodGroup.setEnabled(!doExcludeBreedingMethodGroup);
		}

		// Cross Female Info and its sub-options. Excluded sub-options will be visible but disabled
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO)) {
			this.menuFillWithCrossFemaleInfo = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO);

			final boolean doExcludeCrossFemaleGid = columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
			this.menuFillWithCrossFemaleGID =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_FEMALE_GID, this.menuFillWithCrossFemaleInfo);
			this.menuFillWithCrossFemaleGID.setEnabled(!doExcludeCrossFemaleGid);

			final boolean doExcludeCrossFemaleName = columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
			this.menuFillWithCrossFemalePrefName =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME, this.menuFillWithCrossFemaleInfo);
			this.menuFillWithCrossFemalePrefName.setEnabled(!doExcludeCrossFemaleName);
		}

		// Cross Male Info and its sub-options. Excluded sub-options will be visible but disabled
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_MALE_INFO)) {
			this.menuFillWithCrossMaleInfo = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_CROSS_MALE_INFO);

			final boolean doExcludeCrossMaleGid = columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_MALE_GID);
			this.menuFillWithCrossMaleGID =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_MALE_GID, this.menuFillWithCrossMaleInfo);
			this.menuFillWithCrossMaleGID.setEnabled(!doExcludeCrossMaleGid);

			final boolean doExcludeCrossMaleName = columnsToExclude.contains(FillWithOption.FILL_WITH_CROSS_MALE_NAME);
			this.menuFillWithCrossMalePrefName =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_CROSS_MALE_NAME, this.menuFillWithCrossMaleInfo);
			this.menuFillWithCrossMalePrefName.setEnabled(!doExcludeCrossMaleName);
		}

		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_ATTRIBUTE)) {
			this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_ATTRIBUTE);
		}
	}

	private ContextMenuItem addFillWIthOptionToMenu(final FillWithOption option) {
		return this.addColumnItem.addItem(this.messageSource.getMessage(option.getMessageKey()));
	}

	private ContextMenuItem addFillWithOptionToSubMenu(final FillWithOption option, final ContextMenuItem item) {
		return item.addItem(this.messageSource.getMessage(option.getMessageKey()));
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
				&& AddColumnContextMenu.propertyExists(ColumnLabels.BREEDING_METHOD_GROUP.getName(), table)) {
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
		if (menuItem != null) {
			if (AddColumnContextMenu.propertyExists(columnLabel.getName(), table)) {
				menuItem.setEnabled(false);
			} else {
				menuItem.setEnabled(true);
			}
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

	public static boolean sourceHadAddedColumn(final Object[] visibleColumns) {
		for (final Object column : visibleColumns) {
			if (AddColumnContextMenu.ADDABLE_PROPERTY_IDS.contains(column.toString())) {
				return true;
			}
		}
		return false;
	}

	
	public ContextMenuItem getAddColumnItem() {
		return addColumnItem;
	}

	
	public void setAddColumnItem(ContextMenuItem addColumnItem) {
		this.addColumnItem = addColumnItem;
	}

	
	public void setMenuFillWithPreferredId(ContextMenuItem menuFillWithPreferredId) {
		this.menuFillWithPreferredId = menuFillWithPreferredId;
	}

	
	public void setMenuFillWithPreferredName(ContextMenuItem menuFillWithPreferredName) {
		this.menuFillWithPreferredName = menuFillWithPreferredName;
	}

	
	public void setMenuFillWithGermplasmDate(ContextMenuItem menuFillWithGermplasmDate) {
		this.menuFillWithGermplasmDate = menuFillWithGermplasmDate;
	}

	
	public void setMenuFillWithLocations(ContextMenuItem menuFillWithLocations) {
		this.menuFillWithLocations = menuFillWithLocations;
	}

	
	public void setMenuFillWithMethodInfo(ContextMenuItem menuFillWithMethodInfo) {
		this.menuFillWithMethodInfo = menuFillWithMethodInfo;
	}

	
	public void setMenuFillWithMethodName(ContextMenuItem menuFillWithMethodName) {
		this.menuFillWithMethodName = menuFillWithMethodName;
	}

	
	public void setMenuFillWithMethodAbbrev(ContextMenuItem menuFillWithMethodAbbrev) {
		this.menuFillWithMethodAbbrev = menuFillWithMethodAbbrev;
	}

	
	public void setMenuFillWithMethodNumber(ContextMenuItem menuFillWithMethodNumber) {
		this.menuFillWithMethodNumber = menuFillWithMethodNumber;
	}

	
	public void setMenuFillWithMethodGroup(ContextMenuItem menuFillWithMethodGroup) {
		this.menuFillWithMethodGroup = menuFillWithMethodGroup;
	}

	
	public void setMenuFillWithCrossFemaleInfo(ContextMenuItem menuFillWithCrossFemaleInfo) {
		this.menuFillWithCrossFemaleInfo = menuFillWithCrossFemaleInfo;
	}

	
	public void setMenuFillWithCrossFemaleGID(ContextMenuItem menuFillWithCrossFemaleGID) {
		this.menuFillWithCrossFemaleGID = menuFillWithCrossFemaleGID;
	}

	
	public void setMenuFillWithCrossFemalePrefName(ContextMenuItem menuFillWithCrossFemalePrefName) {
		this.menuFillWithCrossFemalePrefName = menuFillWithCrossFemalePrefName;
	}

	
	public void setMenuFillWithCrossMaleInfo(ContextMenuItem menuFillWithCrossMaleInfo) {
		this.menuFillWithCrossMaleInfo = menuFillWithCrossMaleInfo;
	}

	
	public void setMenuFillWithCrossMaleGID(ContextMenuItem menuFillWithCrossMaleGID) {
		this.menuFillWithCrossMaleGID = menuFillWithCrossMaleGID;
	}

	
	public void setMenuFillWithCrossMalePrefName(ContextMenuItem menuFillWithCrossMalePrefName) {
		this.menuFillWithCrossMalePrefName = menuFillWithCrossMalePrefName;
	}
}
