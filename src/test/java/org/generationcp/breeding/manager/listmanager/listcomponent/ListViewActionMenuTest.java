package org.generationcp.breeding.manager.listmanager.listcomponent;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.vaadin.peter.contextmenu.ContextMenu;

public class ListViewActionMenuTest {

	private static final String HEADER_LABEL = "DUMMY HEADER";

	private ListViewActionMenu menu;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		this.menu = new ListViewActionMenu();
		this.menu.setMessageSource(this.messageSource);

		// set up dummy header returned for message source
		Mockito.doReturn(HEADER_LABEL).when(this.messageSource).getMessage(Mockito.any(Message.class));

		// initialize component
		this.menu.afterPropertiesSet();

	}

	@Test
	public void testSetActionMenuWhenListIsUnlockedWhenLocalUserIsTheListOwner() {
		this.menu.setActionMenuWhenListIsUnlocked(true);

		// The following options from the context menu must be visible
		Assert.assertTrue(this.menu.getMenuEditList().isVisible());
		Assert.assertTrue(this.menu.getMenuDeleteEntries().isVisible());
		Assert.assertTrue(this.menu.getMenuDeleteList().isVisible());
		Assert.assertTrue(this.menu.getMenuGroupLines().isVisible());
		Assert.assertTrue(this.menu.getMenuSaveChanges().isVisible());
		Assert.assertTrue(this.menu.getMenuAddEntry().isVisible());
		Assert.assertTrue(this.menu.getMenuAssignCodes().isVisible());
		Assert.assertTrue(this.menu.getRemoveSelectedGermplasm().isVisible());
	}

	@Test
	public void testSetActionMenuWhenListIsUnlockedWhenLocalUserIsNotTheListOwner() {
		this.menu.setActionMenuWhenListIsUnlocked(false);

		// verify if the menu option are properly displayed in the context menu
		Assert.assertTrue(this.menu.getMenuEditList().isVisible());
		Assert.assertTrue(this.menu.getMenuDeleteEntries().isVisible());
		// User can't delete a list which he doesn't owned so the option for delete list will be hidden
		Assert.assertFalse(this.menu.getMenuDeleteList().isVisible());
		Assert.assertTrue(this.menu.getMenuGroupLines().isVisible());
		Assert.assertTrue(this.menu.getMenuSaveChanges().isVisible());
		Assert.assertTrue(this.menu.getMenuAddEntry().isVisible());
		Assert.assertTrue(this.menu.getMenuAssignCodes().isVisible());
		Assert.assertTrue(this.menu.getRemoveSelectedGermplasm().isVisible());
	}

	@Test
	public void testSetActionMenuWhenListIsLocked() {
		this.menu.setActionMenuWhenListIsLocked();

		// The following options from the context menu must be invisible
		Assert.assertFalse(this.menu.getMenuEditList().isVisible());
		Assert.assertFalse(this.menu.getMenuDeleteEntries().isVisible());
		Assert.assertFalse(this.menu.getMenuDeleteList().isVisible());
		Assert.assertFalse(this.menu.getMenuGroupLines().isVisible());
		Assert.assertFalse(this.menu.getMenuSaveChanges().isVisible());
		Assert.assertFalse(this.menu.getMenuAddEntry().isVisible());
		Assert.assertFalse(this.menu.getMenuAssignCodes().isVisible());
		Assert.assertFalse(this.menu.getRemoveSelectedGermplasm().isVisible());
	}

	@Test
	public void testUpdateListViewActionMenuWhenTheListManagerIsLoadedFromTheURLListBuilderIsLockedAndHasSource() {
		final boolean fromUrl = true;
		final boolean listBuilderIsLocked = true;
		final boolean hasSource = true;

		this.menu.updateListViewActionMenu(fromUrl, listBuilderIsLocked, hasSource);

		Assert.assertFalse("Export List option must be invisible.", this.menu.getMenuExportList().isVisible());
		Assert.assertFalse("When the List Builder Section is locked, Copy to New List option must be invisible",
				this.menu.getMenuCopyToList().isVisible());
	}

	@Test
	public void testUpdateListViewActionMenuWhenTheListManagerIsNotLoadedFromTheURLListBuilderIsLockedAndHasSource() {
		final boolean fromUrl = false;
		final boolean listBuilderIsLocked = true;
		final boolean hasSource = true;

		this.menu.updateListViewActionMenu(fromUrl, listBuilderIsLocked, hasSource);

		Assert.assertTrue("Export List option must be visible.", this.menu.getMenuExportList().isVisible());
		Assert.assertFalse("When the List Builder Section is locked, Copy to New List option must be invisible",
				this.menu.getMenuCopyToList().isVisible());
	}

	@Test
	public void testUpdateListViewActionMenuWhenTheListManagerIsNotLoadedFromTheURLListBuilderIsNotLockedAndHasSource() {
		final boolean fromUrl = false;
		final boolean listBuilderIsLocked = false;
		final boolean hasSource = true;

		this.menu.updateListViewActionMenu(fromUrl, listBuilderIsLocked, hasSource);

		Assert.assertTrue("Export List option must be visible.", this.menu.getMenuExportList().isVisible());
		Assert.assertTrue("When the List Builder Section is locked, Copy to New List option must be visible",
				this.menu.getMenuCopyToList().isVisible());
	}

	@Test
	public void testLayoutAdminLink() {

		final String removeSelectedGermplasmMessage = "removeSelectedGermplasm";
	  final String ungroupMessage = "removeSelectedGermplasm";

		Mockito.when(this.messageSource.getMessage(Message.REMOVE_SELECTED_GERMPLASM)).thenReturn(removeSelectedGermplasmMessage);
	  	Mockito.when(this.messageSource.getMessage(Message.UNGROUP)).thenReturn(ungroupMessage);

		final ContextMenu.ContextMenuItem removeSelectedGermplasmContextMenuItem = Mockito.mock(ContextMenu.ContextMenuItem.class);
		final ContextMenu.ContextMenuItem listEditiongOptionsContextMenuItem = Mockito.mock(ContextMenu.ContextMenuItem.class);
		Mockito.when(listEditiongOptionsContextMenuItem.addItem(removeSelectedGermplasmMessage)).thenReturn(removeSelectedGermplasmContextMenuItem);
		this.menu.setListEditingOptions(listEditiongOptionsContextMenuItem);

	  	final ContextMenu.ContextMenuItem codingAndGroupingOptions = Mockito.mock(ContextMenu.ContextMenuItem.class);
		this.menu.setCodingAndGroupingOptions(codingAndGroupingOptions);
		this.menu.layoutAdminLink();

		// Verify that removeSelectedGermplasmContextMenuItem is added inside listEditiongOptionsContextMenuItem
		Mockito.verify(listEditiongOptionsContextMenuItem).addItem(removeSelectedGermplasmMessage);
		Assert.assertSame(removeSelectedGermplasmContextMenuItem, this.menu.getRemoveSelectedGermplasm());
	  	Mockito.verify(codingAndGroupingOptions).addItem(ungroupMessage);
	}

}
