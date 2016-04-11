
package org.generationcp.breeding.manager.listmanager.listcomponent;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;

/**
 * This class is used as the Context Menu for Action Buttons in View List Germplasm List Data Table
 * 
 */
@Configurable
public class ListViewActionMenu extends ContextMenu implements InitializingBean, InternationalizableComponent {

	private static final String CONTEXT_MENU_WIDTH = "295px";

	private static final long serialVersionUID = 1L;
	private ContextMenuItem menuExportList;
	private ContextMenuItem menuCopyToList;
	private ContextMenuItem menuAddEntry;
	private ContextMenuItem menuSaveChanges;
	private ContextMenuItem menuDeleteEntries;
	private ContextMenuItem menuMarkLinesAsFixed;
	private ContextMenuItem menuEditList;
	private ContextMenuItem menuDeleteList;
	private ContextMenuItem menuInventoryView;
	private ContextMenuItem menuSelectAll;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public ListViewActionMenu() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setWidth(CONTEXT_MENU_WIDTH);

		// Generate main level items
		this.menuAddEntry = this.addItem(this.messageSource.getMessage(Message.ADD_ENTRIES));
		this.menuCopyToList = this.addItem(this.messageSource.getMessage(Message.COPY_TO_NEW_LIST));
		this.menuDeleteList = this.addItem(this.messageSource.getMessage(Message.DELETE_LIST));
		this.menuDeleteEntries = this.addItem(this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
		this.menuMarkLinesAsFixed = this.addItem(this.messageSource.getMessage(Message.MARK_LINES_AS_FIXED));
		this.menuEditList = this.addItem(this.messageSource.getMessage(Message.EDIT_LIST));
		this.menuExportList = this.addItem(this.messageSource.getMessage(Message.EXPORT_LIST));
		this.menuInventoryView = this.addItem(this.messageSource.getMessage(Message.INVENTORY_VIEW));
		this.menuSaveChanges = this.addItem(this.messageSource.getMessage(Message.SAVE_CHANGES));
		this.menuSelectAll = this.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public ContextMenuItem getMenuExportList() {
		return this.menuExportList;
	}

	public ContextMenuItem getMenuCopyToList() {
		return this.menuCopyToList;
	}

	public ContextMenuItem getMenuAddEntry() {
		return this.menuAddEntry;
	}

	public ContextMenuItem getMenuSaveChanges() {
		return this.menuSaveChanges;
	}

	public ContextMenuItem getMenuDeleteEntries() {
		return this.menuDeleteEntries;
	}

	public ContextMenuItem getMenuMarkLinesAsFixed() {
		return this.menuMarkLinesAsFixed;
	}

	public ContextMenuItem getMenuEditList() {
		return this.menuEditList;
	}

	public ContextMenuItem getMenuDeleteList() {
		return this.menuDeleteList;
	}

	public ContextMenuItem getMenuInventoryView() {
		return this.menuInventoryView;
	}

	public ContextMenuItem getMenuSelectAll() {
		return this.menuSelectAll;
	}

	/**
	 * When the Germplasm List is not locked, and when not accessed directly from URL or popup window
	 */
	public void setActionMenuWhenListIsLocked(final boolean isLocalUserListOwner) {
		this.menuEditList.setVisible(true);
		this.menuDeleteEntries.setVisible(true);
		// show only Delete List when user is owner
		this.menuDeleteList.setVisible(isLocalUserListOwner);
		this.menuMarkLinesAsFixed.setVisible(true);
		this.menuSaveChanges.setVisible(true);
		this.menuAddEntry.setVisible(true);
	}

	public void setActionMenuWhenListIsUnlocked() {
		this.menuEditList.setVisible(false);
		this.menuDeleteList.setVisible(false);
		this.menuDeleteEntries.setVisible(false);
		this.menuMarkLinesAsFixed.setVisible(false);
		this.menuSaveChanges.setVisible(false);
		this.menuAddEntry.setVisible(false);
	}

	/**
	 * Update the Action Menu in List View base on the following paramenters:
	 * 
	 * @param fromUrl - if it is loaded from the URL directly
	 * @param listBuilderIsLocked - if the list loaded in Build New List pane is locked
	 * @param hasSource - if the source, ListManagerMain.class the parent component, is not null
	 */
	public void updateListViewActionMenu(final boolean fromUrl, final boolean listBuilderIsLocked, final boolean hasSource) {
		if (fromUrl) {
			this.menuExportList.setVisible(false);
			this.menuCopyToList.setVisible(false);
		}

		if (hasSource) {
			this.menuCopyToList.setVisible(!listBuilderIsLocked);
		}
	}
}
