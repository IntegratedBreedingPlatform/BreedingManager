
package org.generationcp.breeding.manager.customcomponent.listinventory;

import java.util.List;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.InventoryTableDropHandler;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.themes.BaseTheme;

/**
 * This table is used for displaying lots in Inventory view specifically for List Manager. It basically handles the drop handling method in
 * inventory view.
 */
public class ListManagerInventoryTable extends ListInventoryTable {

	private static final Logger LOG = LoggerFactory.getLogger(ListManagerInventoryTable.class);

	private static final long serialVersionUID = 7827387488704418083L;
	public static final String INVENTORY_TABLE_DATA = "BuildNewListInventoryTableData";

	private final ListManagerMain listManagerMain;
	private InventoryTableDropHandler inventoryTableDropHandler;
	private final Boolean enableDragSource;
	private final Boolean enableDropHandler;

	public ListManagerInventoryTable(final ListManagerMain listManagerMain, final Integer listId, final Boolean enableDragSource,
			final Boolean enableDropHandler) {
		super(listId);
		this.listManagerMain = listManagerMain;
		this.enableDragSource = enableDragSource;
		this.enableDropHandler = enableDropHandler;
	}

	@Override
	public void instantiateComponents() {
		super.instantiateComponents();

		this.listInventoryTable.setData(ListManagerInventoryTable.INVENTORY_TABLE_DATA);
		this.setDragSource();
		this.setDropHandler();
	}

	public void setDropHandler() {
		this.inventoryTableDropHandler =
				new InventoryTableDropHandler(this.listManagerMain, this.germplasmDataManager, this.germplasmListManager,
						this.inventoryDataManager, this.pedigreeService, this.crossExpansionProperties, this.listInventoryTable);
		if (this.enableDropHandler) {
			this.listInventoryTable.setDropHandler(this.inventoryTableDropHandler);
		}
	}

	public void setDragSource() {
		if (this.enableDragSource) {
			this.listInventoryTable.setDragMode(TableDragMode.ROW);
		}
	}

	public InventoryTableDropHandler getInventoryTableDropHandler() {
		return this.inventoryTableDropHandler;
	}

}
