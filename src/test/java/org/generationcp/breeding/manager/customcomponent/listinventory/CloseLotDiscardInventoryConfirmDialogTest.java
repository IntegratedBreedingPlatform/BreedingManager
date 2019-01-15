package org.generationcp.breeding.manager.customcomponent.listinventory;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.inventory.exception.CloseLotException;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;

import com.beust.jcommander.internal.Lists;
import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class CloseLotDiscardInventoryConfirmDialogTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private ListComponent source;

	@Mock
	private CloseLotDiscardInventoryAction closeLotDiscardInventoryAction;

	@Mock
	private ListEntryLotDetails listEntryLotDetails;

	@Mock
	private Window window;

	CloseLotDiscardInventoryConfirmDialog closeLotDiscardInventoryConfirmDialog = null;

	@Before
	public void setUp() {

		closeLotDiscardInventoryConfirmDialog =
				new CloseLotDiscardInventoryConfirmDialog(this.source, this.closeLotDiscardInventoryAction, this.listEntryLotDetails);
		closeLotDiscardInventoryConfirmDialog.setMessageSource(messageSource);

		Mockito.when(listEntryLotDetails.getLotId()).thenReturn(1);
		Mockito.when(listEntryLotDetails.getId()).thenReturn(1);
		Mockito.when(messageSource.getMessage(Message.YES)).thenReturn((Message.YES.name()));
		Mockito.when(messageSource.getMessage(Message.NO)).thenReturn(Message.NO.name());
		Mockito.when(messageSource.getMessage(Message.APPLY_TO_ALL)).thenReturn(Message.APPLY_TO_ALL.name());
		Mockito.when(messageSource.getMessage(Message.LOTS_HAVE_AVAILABLE_BALANCE_NO_UNCOMMITTED_RESERVATION_ERROR, 1))
				.thenReturn(Message.LOTS_HAVE_AVAILABLE_BALANCE_NO_UNCOMMITTED_RESERVATION_ERROR.name() + "1");
		Mockito.when(messageSource.getMessage(Message.LOTS_CLOSED_SUCCESSFULLY)).thenReturn((Message.LOTS_CLOSED_SUCCESSFULLY.name()));
		Mockito.when(messageSource.getMessage(Message.SUCCESS)).thenReturn((Message.SUCCESS.name()));
		Mockito.when(messageSource.getMessage(Message.LOT_ALREADY_CLOSED_ERROR.name()))
				.thenReturn((Message.LOT_ALREADY_CLOSED_ERROR.name()));
		Mockito.when(messageSource.getMessage(Message.ERROR)).thenReturn((Message.ERROR.name()));
		Mockito.when(this.source.getWindow()).thenReturn(window);

		closeLotDiscardInventoryConfirmDialog.instantiateComponents();
		closeLotDiscardInventoryConfirmDialog.addListeners();

	}

	@Test
	public void testInstantiateComponents() {

		Assert.assertEquals(Message.LOTS_HAVE_AVAILABLE_BALANCE_NO_UNCOMMITTED_RESERVATION_ERROR.name() + "1",
				closeLotDiscardInventoryConfirmDialog.getConfirmLabel().getValue());

		Assert.assertTrue(closeLotDiscardInventoryConfirmDialog.getApplyAllCheckBox().getValue().equals(Boolean.FALSE));
		Assert.assertEquals(Message.YES.name(), closeLotDiscardInventoryConfirmDialog.getYesButton().getCaption());
		Assert.assertEquals(Message.NO.name(), closeLotDiscardInventoryConfirmDialog.getNoButton().getCaption());

	}

	@Test
	public void testAddListeners() {
		Assert.assertEquals(1, closeLotDiscardInventoryConfirmDialog.getYesButton().getListeners(Button.ClickEvent.class).size());
		Assert.assertEquals(1, closeLotDiscardInventoryConfirmDialog.getNoButton().getListeners(Button.ClickEvent.class).size());
	}

	@Test
	public void testLayoutComponents() {
		closeLotDiscardInventoryConfirmDialog.layoutComponents();

		Assert.assertEquals(3, closeLotDiscardInventoryConfirmDialog.getMainLayout().getComponentCount());
	}

	@Test
	public void testYesActionListenerWithApplyToAllFalse() throws Exception {
		closeLotDiscardInventoryConfirmDialog.getYesButton().click();

		Mockito.verify(this.source).processCloseLots(Lists.newArrayList(this.listEntryLotDetails));
		Mockito.verify(this.closeLotDiscardInventoryAction)
				.removeCurrentCloseLotListenerAndProcessNextItem(closeLotDiscardInventoryConfirmDialog);
		Mockito.verify(this.source).resetListInventoryTableValues();
		Mockito.verify(this.source).resetListDataTableValues();
		Mockito.verify(this.window).removeWindow(closeLotDiscardInventoryConfirmDialog);
		Mockito.verify(window).showNotification(Mockito.any(Window.Notification.class));
	}

	@Test
	public void testYesActionListenerWithApplyToAllFalseThrowException() throws Exception {
		Mockito.doThrow(new CloseLotException(Message.LOT_ALREADY_CLOSED_ERROR.name())).when(this.source)
				.processCloseLots(Lists.<ListEntryLotDetails>newArrayList(this.listEntryLotDetails));

		closeLotDiscardInventoryConfirmDialog.getYesButton().click();
		Mockito.verify(this.closeLotDiscardInventoryAction).closeAllLotCloseListeners();
		Mockito.verify(window).showNotification(Mockito.any(Window.Notification.class));
	}

	@Test
	public void testYesActionListenerWithApplyToAllTrue() throws Exception {
		closeLotDiscardInventoryConfirmDialog.getApplyAllCheckBox().setValue(Boolean.TRUE);

		CloseLotDiscardInventoryListener listener = Mockito.mock(CloseLotDiscardInventoryListener.class);
		ListEntryLotDetails listEntryLotDetails = Mockito.mock(ListEntryLotDetails.class);

		Mockito.when(listener.getEntryLotDetails()).thenReturn(listEntryLotDetails);

		Mockito.when(this.closeLotDiscardInventoryAction.getCloseLotListener())
				.thenReturn(Lists.<CloseLotDiscardInventoryListener>newArrayList(listener));

		closeLotDiscardInventoryConfirmDialog.getYesButton().click();

		Mockito.verify(this.source).processCloseLots(Lists.newArrayList(listEntryLotDetails));
		Mockito.verify(this.closeLotDiscardInventoryAction).closeAllLotCloseListeners();
		Mockito.verify(this.source).resetListInventoryTableValues();
		Mockito.verify(this.source).resetListDataTableValues();
		Mockito.verify(this.window).removeWindow(closeLotDiscardInventoryConfirmDialog);
		Mockito.verify(window).showNotification(Mockito.any(Window.Notification.class));
	}

	@Test
	public void testYesActionListenerWithApplyToAllTrueThrowException() throws Exception {
		closeLotDiscardInventoryConfirmDialog.getApplyAllCheckBox().setValue(Boolean.TRUE);

		CloseLotDiscardInventoryListener listener = Mockito.mock(CloseLotDiscardInventoryListener.class);
		ListEntryLotDetails listEntryLotDetails = Mockito.mock(ListEntryLotDetails.class);

		Mockito.when(listener.getEntryLotDetails()).thenReturn(listEntryLotDetails);

		Mockito.when(this.closeLotDiscardInventoryAction.getCloseLotListener())
				.thenReturn(Lists.<CloseLotDiscardInventoryListener>newArrayList(listener));

		Mockito.doThrow(new CloseLotException(Message.LOT_ALREADY_CLOSED_ERROR.name())).when(this.source)
				.processCloseLots(Lists.<ListEntryLotDetails>newArrayList(listEntryLotDetails));

		closeLotDiscardInventoryConfirmDialog.getYesButton().click();
		Mockito.verify(this.closeLotDiscardInventoryAction).closeAllLotCloseListeners();
		Mockito.verify(window).showNotification(Mockito.any(Window.Notification.class));

	}

	@Test
	public void testNoActionListenerWithApplyToAllTrue() {
		closeLotDiscardInventoryConfirmDialog.getApplyAllCheckBox().setValue(Boolean.TRUE);

		closeLotDiscardInventoryConfirmDialog.getNoButton().click();

		Mockito.verify(this.closeLotDiscardInventoryAction).closeAllLotCloseListeners();
		Mockito.verify(this.window).removeWindow(closeLotDiscardInventoryConfirmDialog);
	}

}
