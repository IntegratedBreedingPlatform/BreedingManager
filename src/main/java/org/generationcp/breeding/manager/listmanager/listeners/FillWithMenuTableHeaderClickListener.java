
package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.commons.constant.ColumnLabels;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Table.HeaderClickListener;

public class FillWithMenuTableHeaderClickListener implements HeaderClickListener {

	private static final long serialVersionUID = 4792602001489368804L;

	private final FillWith fillWith;
	private final ContextMenu fillWithMenu;
	private ContextMenuItem menuFillWithLocationName;
	private ContextMenuItem menuFillWithCrossExpansion;

	public FillWithMenuTableHeaderClickListener(final FillWith fillWith, final ContextMenu fillWithMenu,
			final ContextMenuItem menuFillWithLocationName, final ContextMenuItem menuFillWithCrossExpansion) {
		super();
		this.fillWith = fillWith;
		this.fillWithMenu = fillWithMenu;
		this.menuFillWithLocationName = menuFillWithLocationName;
		this.menuFillWithCrossExpansion = menuFillWithCrossExpansion;
	}

	@Override
	public void headerClick(final HeaderClickEvent event) {
		if (event.getButton() == com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT) {
			final String column = (String) event.getPropertyId();
			this.fillWithMenu.setData(column);
			if (column.equals(ColumnLabels.ENTRY_CODE.getName())) {
				this.menuFillWithLocationName.setVisible(false);
				this.menuFillWithCrossExpansion.setVisible(false);
				this.fillWith.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
				this.fillWithMenu.show(event.getClientX(), event.getClientY());
			} else if (column.equals(ColumnLabels.SEED_SOURCE.getName())) {
				this.menuFillWithLocationName.setVisible(true);
				this.menuFillWithCrossExpansion.setVisible(false);
				this.fillWith.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
				this.fillWithMenu.show(event.getClientX(), event.getClientY());
			} else if (column.equals(ColumnLabels.PARENTAGE.getName())) {
				this.fillWith.setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(false);
				this.menuFillWithLocationName.setVisible(false);
				this.menuFillWithCrossExpansion.setVisible(true);
				this.fillWithMenu.show(event.getClientX(), event.getClientY());
			}
		}
	}

	public void setMenuFillWithLocationName(final ContextMenuItem menuFillWithLocationName) {
		this.menuFillWithLocationName = menuFillWithLocationName;
	}

	public void setMenuFillWithCrossExpansion(final ContextMenuItem menuFillWithCrossExpansion) {
		this.menuFillWithCrossExpansion = menuFillWithCrossExpansion;
	}

}
