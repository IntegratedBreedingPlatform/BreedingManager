
package org.generationcp.breeding.manager.listeners;

import org.generationcp.breeding.manager.inventory.InventoryViewComponent;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class InventoryLinkButtonClickListener implements Button.ClickListener {

	private static final long serialVersionUID = 8347640363159494223L;

	private Integer listId;
	private Integer recordId; // lrecId
	private final Integer gid;
	private final Component source;

	public InventoryLinkButtonClickListener(Component source, Integer listId, Integer recordId, Integer gid) {
		super();
		this.source = source;
		this.listId = listId;
		this.recordId = recordId;
		this.gid = gid;
	}

	public InventoryLinkButtonClickListener(Component source, Integer gid) {
		super();
		this.source = source;
		this.gid = gid;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		InventoryViewComponent listInventoryComponent = new InventoryViewComponent(this.listId, this.recordId, this.gid);
		listInventoryComponent.setDebugId("listInventoryComponent");

		Window inventoryWindow = new BaseSubWindow("Inventory Details");
		inventoryWindow.setModal(true);
		inventoryWindow.setWidth("810px");
		inventoryWindow.setHeight("355px");
		inventoryWindow.setResizable(false);
		inventoryWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		inventoryWindow.setContent(listInventoryComponent);

		this.source.getWindow().addWindow(inventoryWindow);
		inventoryWindow.center();
	}

}
