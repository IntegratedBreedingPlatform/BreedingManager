
package org.generationcp.breeding.manager.listmanager.listcomponent;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;

/**
 * This class is used as the Context Menu for Germplasm list table in "List View" for View List section in List Manager
 * 
 */
@Configurable
public class GermplasmListTableContextMenu extends ContextMenu implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private static final String CONTEXT_MENU_WIDTH = "295px";

	private ContextMenuItem tableContextMenuSelectAll;
	private ContextMenuItem tableContextMenuCopyToNewList;
	private ContextMenuItem tableContextMenuDeleteEntries;
	private ContextMenuItem tableContextMenuEditCell;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmListTableContextMenu() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setWidth(CONTEXT_MENU_WIDTH);

		this.tableContextMenuSelectAll = this.addItem(this.messageSource.getMessage(Message.SELECT_ALL));
		this.tableContextMenuDeleteEntries = this.addItem(this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES));
		this.tableContextMenuEditCell = this.addItem(this.messageSource.getMessage(Message.EDIT_VALUE));
		this.tableContextMenuCopyToNewList = this.addItem(this.messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST));
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public ContextMenuItem getTableContextMenuSelectAll() {
		return this.tableContextMenuSelectAll;
	}

	public ContextMenuItem getTableContextMenuCopyToNewList() {
		return this.tableContextMenuCopyToNewList;
	}

	public ContextMenuItem getTableContextMenuDeleteEntries() {
		return this.tableContextMenuDeleteEntries;
	}

	public ContextMenuItem getTableContextMenuEditCell() {
		return this.tableContextMenuEditCell;
	}

	public void updateGermplasmListTableContextMenu(final boolean isNonEditableColumn, final boolean isLockedList,
			final boolean isListBuilderLocked, final boolean isListComponentSourceAvailable) {
		// make the edit cell context menu available when selected column is editable and list is not locked
		this.tableContextMenuEditCell.setVisible(!isNonEditableColumn && !isLockedList);

		// delete entries context menu will be available when current germplasm list is not locked
		this.tableContextMenuDeleteEntries.setVisible(!isLockedList);

		// copy to new list context menu will be available if list builder is un-locked
		this.tableContextMenuCopyToNewList.setVisible(!isListBuilderLocked && isListComponentSourceAvailable);
	}

	/**
	 * For Test Purposes
	 */
	void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
