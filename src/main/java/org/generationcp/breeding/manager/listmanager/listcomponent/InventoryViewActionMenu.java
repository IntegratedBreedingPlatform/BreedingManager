
package org.generationcp.breeding.manager.listmanager.listcomponent;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;

/**
 * This class is used as the Context Menu for Action Buttons in Inventory View for View List Germplasm List Data Table window
 * 
 */
@Configurable
public class InventoryViewActionMenu extends ContextMenu implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private static final String CONTEXT_MENU_WIDTH = "295px";

	private ContextMenuItem menuCopyToNewListFromInventory;
	private ContextMenuItem menuInventorySaveChanges;
	private ContextMenuItem menuListView;
	private ContextMenuItem menuReserveInventory;
	private ContextMenuItem menuCancelReservation;
	private ContextMenuItem menuSelectAll;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public InventoryViewActionMenu() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {

		this.setWidth(CONTEXT_MENU_WIDTH);

		this.menuCancelReservation = this.addItem(this.messageSource.getMessage(Message.CANCEL_RESERVATIONS));
		this.menuCopyToNewListFromInventory = this.addItem(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST));
		this.menuReserveInventory = this.addItem(this.messageSource.getMessage(Message.RESERVE_INVENTORY));
		this.menuListView = this.addItem(this.messageSource.getMessage(Message.RETURN_TO_LIST_VIEW));
		this.menuInventorySaveChanges = this.addItem(this.messageSource.getMessage(Message.SAVE_RESERVATIONS));
		this.menuSelectAll = this.addItem(this.messageSource.getMessage(Message.SELECT_ALL));

		this.resetInventoryMenuOptions();
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public ContextMenuItem getMenuCopyToNewListFromInventory() {
		return this.menuCopyToNewListFromInventory;
	}

	public ContextMenuItem getMenuInventorySaveChanges() {
		return this.menuInventorySaveChanges;
	}

	public ContextMenuItem getMenuListView() {
		return this.menuListView;
	}

	public ContextMenuItem getMenuReserveInventory() {
		return this.menuReserveInventory;
	}

	public ContextMenuItem getMenuCancelReservation() {
		return this.menuCancelReservation;
	}

	public ContextMenuItem getMenuSelectAll() {
		return this.menuSelectAll;
	}

	public void resetInventoryMenuOptions() {
		// disable the save button at first since there are no reservations yet
		this.menuInventorySaveChanges.setEnabled(false);

		// Temporarily disable to Copy to New List in InventoryView
		// implement the function
		this.menuCopyToNewListFromInventory.setEnabled(false);
	}

	public void setMenuInventorySaveChanges() {
		this.menuInventorySaveChanges.setEnabled(true);
	}

	/**
	 * For Test Purposes
	 */
	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
