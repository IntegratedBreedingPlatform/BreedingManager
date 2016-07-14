
package org.generationcp.breeding.manager.listmanager.listcomponent;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class InventoryViewActionMenuTest {

	private static final String HEADER_LABEL = "DUMMY HEADER";

	private InventoryViewActionMenu menu;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.menu = new InventoryViewActionMenu();
		this.menu.setMessageSource(this.messageSource);

		// set up dummy header returned for message source
		Mockito.doReturn(HEADER_LABEL).when(this.messageSource).getMessage(Mockito.any(Message.class));

		// initialize component
		this.menu.afterPropertiesSet();

	}

	@Test
	public void testResetInventoryMenuOptions() {
		this.menu.resetInventoryMenuOptions();

		// verify if the following options are enabled in context menu
		Assert.assertFalse(this.menu.getMenuInventorySaveChanges().isEnabled());
		Assert.assertFalse(this.menu.getMenuCopyToNewListFromInventory().isEnabled());
	}

	@Test
	public void testSetMenuInventorySaveChanges() {
		this.menu.setMenuInventorySaveChanges();

		Assert.assertTrue("Save Reservation option is enabled.", this.menu.getMenuInventorySaveChanges().isEnabled());
	}

}
