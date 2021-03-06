
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.ListDataColumn;
import org.generationcp.middleware.domain.gms.ListDataInfo;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

@Configurable
public class AddColumnContextMenu implements InternationalizableComponent {

	private final SimpleResourceBundleMessageSource messageSource;

	private final ContextMenu sourceContextMenu;
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

	private ContextMenuItem menuFillWithGroupSourceInfo;
	private ContextMenuItem menuFillWithGroupSourceGID;
	private ContextMenuItem menuFillWithGroupSourcePreferredName;

	private ContextMenuItem menuFillWithImmediateSourceInfo;
	private ContextMenuItem menuFillWithImmediateSourceGID;
	private ContextMenuItem menuFillWithImmediateSourcePreferredName;

	private final ContextMenuItem listEditingOptions;
	private final AddColumnSource addColumnSource;

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
		// Adding it to List Editing Option instead of main menu
		if (this.listEditingOptions != null) {
			this.addColumnItem = this.listEditingOptions.addItem(this.messageSource.getMessage(Message.ADD_COLUMN));
		} else {
			this.addColumnItem = this.sourceContextMenu.addItem(this.messageSource.getMessage(Message.ADD_COLUMN));
		}

		this.setupSubMenuItems();
	}

	public void addListener(final ContextMenu.ClickListener listener) {
		this.sourceContextMenu.addListener(listener);
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
			this.menuFillWithMethodNumber =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER, this.menuFillWithMethodInfo);
			this.menuFillWithMethodNumber.setEnabled(!doExcludeBreedingMethodNumber);

			final boolean doExcludeBreedingMethodGroup = columnsToExclude.contains(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
			this.menuFillWithMethodGroup =
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

		// Group source information and its sub-options.
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_GROUP_SOURCE_INFO)) {
			this.menuFillWithGroupSourceInfo = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_GROUP_SOURCE_INFO);

			final boolean doExcludeGroupSourceGid = columnsToExclude.contains(FillWithOption.FILL_WITH_GROUP_SOURCE_GID);
			this.menuFillWithGroupSourceGID =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_GROUP_SOURCE_GID, this.menuFillWithGroupSourceInfo);
			this.menuFillWithGroupSourceGID.setEnabled(!doExcludeGroupSourceGid);

			final boolean doExcludeGroupSourcePreferredName =
					columnsToExclude.contains(FillWithOption.FILL_WITH_GROUP_SOURCE_PREFERRED_NAME);
			this.menuFillWithGroupSourcePreferredName =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_GROUP_SOURCE_PREFERRED_NAME, this.menuFillWithGroupSourceInfo);
			this.menuFillWithGroupSourcePreferredName.setEnabled(!doExcludeGroupSourcePreferredName);
		}

		// Immediate Group source information and its sub-options.
		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_INFO)) {
			this.menuFillWithImmediateSourceInfo = this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_INFO);

			final boolean doExcludeImmediateSourceGid = columnsToExclude.contains(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_GID);
			this.menuFillWithImmediateSourceGID =
					this.addFillWithOptionToSubMenu(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_GID, this.menuFillWithImmediateSourceInfo);
			this.menuFillWithImmediateSourceGID.setEnabled(!doExcludeImmediateSourceGid);

			final boolean doExcludeImmediateSourcePreferredName =
					columnsToExclude.contains(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_PREFERRED_NAME);
			this.menuFillWithImmediateSourcePreferredName = this.addFillWithOptionToSubMenu(
					FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_PREFERRED_NAME, this.menuFillWithImmediateSourceInfo);
			this.menuFillWithImmediateSourcePreferredName.setEnabled(!doExcludeImmediateSourcePreferredName);
			this.sourceContextMenu.setWidth("325px");
		}

		if (!columnsToExclude.contains(FillWithOption.FILL_WITH_GERMPLASM_NAME)) {
			this.addFillWIthOptionToMenu(FillWithOption.FILL_WITH_GERMPLASM_NAME);
		}
	}

	private ContextMenuItem addFillWIthOptionToMenu(final FillWithOption option) {
		return this.addColumnItem.addItem(this.messageSource.getMessage(option.getMessageKey()));
	}

	private ContextMenuItem addFillWithOptionToSubMenu(final FillWithOption option, final ContextMenuItem item) {
		return item.addItem(this.messageSource.getMessage(option.getMessageKey()));
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
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.FGID, this.menuFillWithCrossFemaleGID);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.CROSS_FEMALE_PREFERRED_NAME, this.menuFillWithCrossFemalePrefName);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.MGID, this.menuFillWithCrossMaleGID);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.CROSS_MALE_PREFERRED_NAME, this.menuFillWithCrossMalePrefName);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.GROUP_SOURCE_GID, this.menuFillWithGroupSourceGID);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.GROUP_SOURCE_PREFERRED_NAME,
				this.menuFillWithGroupSourcePreferredName);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.IMMEDIATE_SOURCE_GID, this.menuFillWithImmediateSourceGID);
		this.disableMenuItemIfColumnAlreadyExists(table, ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME,
				this.menuFillWithImmediateSourcePreferredName);

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
		if (AddColumnContextMenu.propertyExists(ColumnLabels.FGID.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(), table)) {
			this.menuFillWithCrossFemaleInfo.setEnabled(false);
		} else {
			this.menuFillWithCrossFemaleInfo.setEnabled(true);
		}

		// Disable main "Cross Male Information" menu item if columns were added for all sub-menu items
		if (AddColumnContextMenu.propertyExists(ColumnLabels.MGID.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName(), table)) {
			this.menuFillWithCrossMaleInfo.setEnabled(false);
		} else {
			this.menuFillWithCrossMaleInfo.setEnabled(true);
		}

		// Disable main "Group Source Information" menu item if columns were added for all sub-menu items
		if (AddColumnContextMenu.propertyExists(ColumnLabels.GROUP_SOURCE_GID.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName(), table)) {
			this.menuFillWithGroupSourceInfo.setEnabled(false);
		} else {
			this.menuFillWithGroupSourceInfo.setEnabled(true);
		}

		// Disable main "Immediate Source Information" menu item if columns were added for all sub-menu items
		if (AddColumnContextMenu.propertyExists(ColumnLabels.IMMEDIATE_SOURCE_GID.getName(), table)
				&& AddColumnContextMenu.propertyExists(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName(), table)) {
			this.menuFillWithImmediateSourceInfo.setEnabled(false);
		} else {
			this.menuFillWithImmediateSourceInfo.setEnabled(true);
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

	public static Boolean propertyExists(final String propertyId, final Table table) {
		final List<String> propertyIds = AddColumnContextMenu.getTablePropertyIds(table);
		return propertyIds.contains(propertyId);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getTablePropertyIds(final Table table) {
		if (table != null) {
			final List<String> propertyIds = new ArrayList<>();
			propertyIds.addAll((Collection<? extends String>) table.getContainerPropertyIds());
			return propertyIds;
		} else {
			return new ArrayList<>();
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
	public List<ListDataInfo> getListDataCollectionFromTable(final Table table, final List<String> attributeAndNameTypes) {
		final List<ListDataInfo> listDataCollection = new ArrayList<>();
		final List<String> propertyIds = AddColumnContextMenu.getTablePropertyIds(table);
		final List<String> addedColumns = this.getAddedColumns(table, attributeAndNameTypes);
		for (final Object itemId : table.getItemIds()) {
			final Item item = table.getItem(itemId);
			final List<ListDataColumn> columns = new ArrayList<>();
			for (final String propertyId : propertyIds) {
				if (addedColumns.contains(propertyId)) {
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

	public List<String> getAddedColumns(final Table table, final List<String> attributeAndNameTypes) {
		final List<String> propertyIds = AddColumnContextMenu.getTablePropertyIds(table);
		final List<String> addedColumns = new ArrayList<>();
		for (final String propertyId : propertyIds) {
			if (this.isAddedColumn(attributeAndNameTypes, propertyId)) {
				addedColumns.add(propertyId);
			}
		}
		return addedColumns;
	}

	public Boolean hasAddedColumn(final Table table, final List<String> attributeAndNameTypes) {
		if (!attributeAndNameTypes.isEmpty()) {
			return true;
		}
		final List<String> propertyIds = AddColumnContextMenu.getTablePropertyIds(table);
		for (final String propertyId : propertyIds) {
			if (ColumnLabels.getAddableGermplasmColumns().contains(propertyId)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAddedColumn(final List<String> attributeAndNameTypes, final String propertyId) {
		return ColumnLabels.getAddableGermplasmColumns().contains(propertyId) || attributeAndNameTypes.contains(propertyId);
	}

	public void showHideAddColumnMenu(final boolean visible) {
		this.addColumnItem.setVisible(visible);
		this.sourceContextMenu.requestRepaint();
	}

	public void setEnabled(final Boolean state) {
		this.addColumnItem.setEnabled(state);
	}

	public void setVisible(final Boolean state) {
		this.addColumnItem.setVisible(state);
	}

	public ContextMenuItem getAddColumnItem() {
		return this.addColumnItem;
	}

	public void setAddColumnItem(final ContextMenuItem addColumnItem) {
		this.addColumnItem = addColumnItem;
	}

	public void setMenuFillWithPreferredId(final ContextMenuItem menuFillWithPreferredId) {
		this.menuFillWithPreferredId = menuFillWithPreferredId;
	}

	public void setMenuFillWithPreferredName(final ContextMenuItem menuFillWithPreferredName) {
		this.menuFillWithPreferredName = menuFillWithPreferredName;
	}

	public void setMenuFillWithGermplasmDate(final ContextMenuItem menuFillWithGermplasmDate) {
		this.menuFillWithGermplasmDate = menuFillWithGermplasmDate;
	}

	public void setMenuFillWithLocations(final ContextMenuItem menuFillWithLocations) {
		this.menuFillWithLocations = menuFillWithLocations;
	}

	public void setMenuFillWithMethodInfo(final ContextMenuItem menuFillWithMethodInfo) {
		this.menuFillWithMethodInfo = menuFillWithMethodInfo;
	}

	public void setMenuFillWithMethodName(final ContextMenuItem menuFillWithMethodName) {
		this.menuFillWithMethodName = menuFillWithMethodName;
	}

	public void setMenuFillWithMethodAbbrev(final ContextMenuItem menuFillWithMethodAbbrev) {
		this.menuFillWithMethodAbbrev = menuFillWithMethodAbbrev;
	}

	public void setMenuFillWithMethodNumber(final ContextMenuItem menuFillWithMethodNumber) {
		this.menuFillWithMethodNumber = menuFillWithMethodNumber;
	}

	public void setMenuFillWithMethodGroup(final ContextMenuItem menuFillWithMethodGroup) {
		this.menuFillWithMethodGroup = menuFillWithMethodGroup;
	}

	public void setMenuFillWithCrossFemaleInfo(final ContextMenuItem menuFillWithCrossFemaleInfo) {
		this.menuFillWithCrossFemaleInfo = menuFillWithCrossFemaleInfo;
	}

	public void setMenuFillWithCrossFemaleGID(final ContextMenuItem menuFillWithCrossFemaleGID) {
		this.menuFillWithCrossFemaleGID = menuFillWithCrossFemaleGID;
	}

	public void setMenuFillWithCrossFemalePrefName(final ContextMenuItem menuFillWithCrossFemalePrefName) {
		this.menuFillWithCrossFemalePrefName = menuFillWithCrossFemalePrefName;
	}

	public void setMenuFillWithCrossMaleInfo(final ContextMenuItem menuFillWithCrossMaleInfo) {
		this.menuFillWithCrossMaleInfo = menuFillWithCrossMaleInfo;
	}

	public void setMenuFillWithCrossMaleGID(final ContextMenuItem menuFillWithCrossMaleGID) {
		this.menuFillWithCrossMaleGID = menuFillWithCrossMaleGID;
	}

	public void setMenuFillWithCrossMalePrefName(final ContextMenuItem menuFillWithCrossMalePrefName) {
		this.menuFillWithCrossMalePrefName = menuFillWithCrossMalePrefName;
	}

	public ContextMenuItem getMenuFillWithGroupSourceInfo() {
		return this.menuFillWithGroupSourceInfo;
	}

	public void setMenuFillWithGroupSourceInfo(final ContextMenuItem menuFillWithGroupSourceInfo) {
		this.menuFillWithGroupSourceInfo = menuFillWithGroupSourceInfo;
	}

	public ContextMenuItem getMenuFillWithGroupSourceGID() {
		return this.menuFillWithGroupSourceGID;
	}

	public void setMenuFillWithGroupSourceGID(final ContextMenuItem menuFillWithGroupSourceGID) {
		this.menuFillWithGroupSourceGID = menuFillWithGroupSourceGID;
	}

	public ContextMenuItem getMenuFillWithGroupSourcePreferredName() {
		return this.menuFillWithGroupSourcePreferredName;
	}

	public void setMenuFillWithGroupSourcePreferredName(final ContextMenuItem menuFillWithGroupSourcePreferredName) {
		this.menuFillWithGroupSourcePreferredName = menuFillWithGroupSourcePreferredName;
	}

	public ContextMenuItem getMenuFillWithImmediateSourceInfo() {
		return this.menuFillWithImmediateSourceInfo;
	}

	public void setMenuFillWithImmediateSourceInfo(final ContextMenuItem menuFillWithImmediateSourceInfo) {
		this.menuFillWithImmediateSourceInfo = menuFillWithImmediateSourceInfo;
	}

	public ContextMenuItem getMenuFillWithImmediateSourceGID() {
		return this.menuFillWithImmediateSourceGID;
	}

	public void setMenuFillWithImmediateSourceGID(final ContextMenuItem menuFillWithImmediateSourceGID) {
		this.menuFillWithImmediateSourceGID = menuFillWithImmediateSourceGID;
	}

	public ContextMenuItem getMenuFillWithImmediateSourcePreferredName() {
		return this.menuFillWithImmediateSourcePreferredName;
	}

	public void setMenuFillWithImmediateSourcePreferredName(final ContextMenuItem menuFillWithImmediateSourcePreferredName) {
		this.menuFillWithImmediateSourcePreferredName = menuFillWithImmediateSourcePreferredName;
	}

	public AddColumnSource getAddColumnSource() {
		return this.addColumnSource;
	}
}
