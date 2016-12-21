package org.generationcp.breeding.manager.customcomponent.listinventory;

import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.beust.jcommander.internal.Lists;
import junit.framework.Assert;

public class CloseLotDiscardInventoryActionTest {

	@Mock
	private ListComponent source;

	@Mock
	private ListEntryLotDetails lotDetails;

	CloseLotDiscardInventoryAction closeLotDiscardInventoryAction = new CloseLotDiscardInventoryAction(this.source);

	@Before
	public void setUp() throws MiddlewareQueryException {
		MockitoAnnotations.initMocks(this);
		closeLotDiscardInventoryAction.setLotDetails(Lists.<ListEntryLotDetails>newArrayList(lotDetails));
		closeLotDiscardInventoryAction.getCloseLotListener().clear();
	}

	@Test
	public void testAddCloseLotListener() {
		CloseLotDiscardInventoryListener listener = Mockito.mock(CloseLotDiscardInventoryListener.class);
		closeLotDiscardInventoryAction.addCloseLotListener(listener);
		Assert.assertEquals("Should have one confirmation dialogue to be added", 1,
				closeLotDiscardInventoryAction.getCloseLotListener().size());
	}

	@Test
	public void testRemoveCurrentCloseLotListenerAndProcessNextItem() {
		CloseLotDiscardInventoryListener listener = Mockito.mock(CloseLotDiscardInventoryListener.class);
		this.closeLotDiscardInventoryAction.setCloseLotListener(Lists.newArrayList(listener));

		closeLotDiscardInventoryAction.removeCurrentCloseLotListenerAndProcessNextItem(listener);
		Assert.assertEquals("Should have zero confirmation dialogue to be added", 0,
				closeLotDiscardInventoryAction.getCloseLotListener().size());
	}

	@Test
	public void testCloseAllLotCloseListeners() {
		CloseLotDiscardInventoryListener listener1 = Mockito.mock(CloseLotDiscardInventoryListener.class);
		CloseLotDiscardInventoryListener listener2 = Mockito.mock(CloseLotDiscardInventoryListener.class);

		this.closeLotDiscardInventoryAction.setCloseLotListener(Lists.newArrayList(listener1, listener2));
		closeLotDiscardInventoryAction.closeAllLotCloseListeners();

		Assert.assertEquals("Should have zero confirmation dialogue to after closing all listeners", 0,
				closeLotDiscardInventoryAction.getCloseLotListener().size());
	}
}
