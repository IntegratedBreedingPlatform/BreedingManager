
package org.generationcp.breeding.manager.listmanager.listcomponent;

import junit.framework.Assert;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class GermplasmListTableContextMenuTest {

	private static final String HEADER_LABEL = "DUMMY HEADER";

	private GermplasmListTableContextMenu menu;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.menu = new GermplasmListTableContextMenu();
		this.menu.setMessageSource(this.messageSource);

		// set up dummy header returned for message source
		Mockito.doReturn(HEADER_LABEL).when(this.messageSource).getMessage(Mockito.any(Message.class));

		// initialize component
		this.menu.afterPropertiesSet();

	}

	@Test
	public void testUpdateGermplasmListTableContextMenu() {
		// Scenario 1: column is non editable, the list is locked in View list, the list is locked in Build New List, the source is
		// available
		boolean isNonEditableColumn = true;
		boolean isLockedList = true;
		boolean isListBuilderLocked = true;
		boolean isListComponentSourceAvailable = true;

		this.menu.updateGermplasmListTableContextMenu(isNonEditableColumn, isLockedList, isListBuilderLocked,
				isListComponentSourceAvailable);

		Assert.assertFalse("Edit Cell is not available if the cell is marked as non-editable or the list is locked", this.menu
				.getTableContextMenuEditCell().isVisible());
		Assert.assertFalse("Delete Entries is not avaiable if the list is locked", this.menu.getTableContextMenuDeleteEntries().isVisible());
		Assert.assertFalse(
				"Copy to New List is not available when the list builder is locked or if the list component source is available.",
				this.menu.getTableContextMenuCopyToNewList().isVisible());

		// Scenario 2: column is editable, the list is not locked in View list, the list is not locked in Build New List, the source is
		// available
		isNonEditableColumn = false;
		isLockedList = false;
		isListBuilderLocked = false;
		isListComponentSourceAvailable = true;

		this.menu.updateGermplasmListTableContextMenu(isNonEditableColumn, isLockedList, isListBuilderLocked,
				isListComponentSourceAvailable);

		Assert.assertTrue("Edit Cell is available if the cell is marked as editable or the list is not locked", this.menu
				.getTableContextMenuEditCell().isVisible());
		Assert.assertTrue("Delete Entries is avaiable if the list is not locked", this.menu.getTableContextMenuDeleteEntries().isVisible());
		Assert.assertTrue(
				"Copy to New List is available when the list builder is not locked or if the list component source is available.",
				this.menu.getTableContextMenuCopyToNewList().isVisible());
	}
}
