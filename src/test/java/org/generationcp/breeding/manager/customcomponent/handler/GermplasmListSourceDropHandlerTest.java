package org.generationcp.breeding.manager.customcomponent.handler;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.GermplasmListTree;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Window;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListSourceDropHandlerTest {

	public static final String ERROR = "Error";
	public static final String CANNOT_MOVE_FOLDER_TO_CROP_LISTS_FOLDER = "CANNOT_MOVE_FOLDER_TO_CROP_LISTS_FOLDER";

	@Mock
	private GermplasmListTree targetListSource;

	@Mock
	private ListSelectorComponent listSelectorComponent;

	@Mock
	private GermplasmListTreeUtil germplasmListTreeUtil;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private Window window;


	private GermplasmListSourceDropHandler germplasmListSourceDropHandler;

	@Before
	public void init() {

		this.germplasmListSourceDropHandler =
				new GermplasmListSourceDropHandler(targetListSource, listSelectorComponent, germplasmListTreeUtil);

		this.germplasmListSourceDropHandler.setGermplasmListManager(germplasmListManager);
		this.germplasmListSourceDropHandler.setMessageSource(messageSource);

		Mockito.when(targetListSource.getWindow()).thenReturn(this.window);

		Mockito.when(messageSource.getMessage(Message.ERROR)).thenReturn(ERROR);
		Mockito.when(messageSource.getMessage(Message.CANNOT_MOVE_FOLDER_TO_CROP_LISTS_FOLDER)).thenReturn(
				CANNOT_MOVE_FOLDER_TO_CROP_LISTS_FOLDER);

	}

	@Test
	public void testDropFolderToCropListsFolder() {


		final Object targetItemId = ListSelectorComponent.CROP_LISTS;
		final Object sourceItemId = 1;

		final DragAndDropEvent event = Mockito.mock(DragAndDropEvent.class);
		final Transferable transferable = Mockito.mock(Transferable.class);
		final AbstractSelect.AbstractSelectTargetDetails target =  Mockito.mock(AbstractSelect.AbstractSelectTargetDetails.class);
		Mockito.when(event.getTransferable()).thenReturn(transferable);
		Mockito.when(event.getTargetDetails()).thenReturn(target);
		Mockito.when(target.getDropLocation()).thenReturn(VerticalDropLocation.MIDDLE);
		Mockito.when(target.getItemIdOver()).thenReturn(targetItemId);
		Mockito.when(transferable.getData("itemId")).thenReturn(sourceItemId);
		Mockito.when(transferable.getSourceComponent()).thenReturn(targetListSource);

		final GermplasmList sourceItem = new GermplasmList();
		sourceItem.setType(GermplasmList.FOLDER_TYPE);
		Mockito.when(germplasmListManager.getGermplasmListById((Integer) sourceItemId)).thenReturn(sourceItem);

		final ArgumentCaptor<Window.Notification> captor = ArgumentCaptor.forClass(Window.Notification.class);

		this.germplasmListSourceDropHandler.drop(event);

		Mockito.verify(this.window).showNotification(captor.capture());

		final Window.Notification notification = captor.getValue();

		Assert.assertEquals(ERROR, notification.getCaption());
		Assert.assertEquals("</br>CANNOT_MOVE_FOLDER_TO_CROP_LISTS_FOLDER", notification.getDescription());
		Mockito.verify(listSelectorComponent, Mockito.never()).refreshRemoteTree();


	}

	@Test
	public void testDropFolderToAFolder() {


		final Object targetItemId = 2;
		final Object sourceItemId = 1;

		final DragAndDropEvent event = Mockito.mock(DragAndDropEvent.class);
		final Transferable transferable = Mockito.mock(Transferable.class);
		final AbstractSelect.AbstractSelectTargetDetails target =  Mockito.mock(AbstractSelect.AbstractSelectTargetDetails.class);
		Mockito.when(event.getTransferable()).thenReturn(transferable);
		Mockito.when(event.getTargetDetails()).thenReturn(target);
		Mockito.when(target.getDropLocation()).thenReturn(VerticalDropLocation.MIDDLE);
		Mockito.when(target.getItemIdOver()).thenReturn(targetItemId);
		Mockito.when(transferable.getData("itemId")).thenReturn(sourceItemId);
		Mockito.when(transferable.getSourceComponent()).thenReturn(targetListSource);

		final GermplasmList sourceItem = new GermplasmList();
		sourceItem.setType(GermplasmList.FOLDER_TYPE);
		final GermplasmList targetItem = new GermplasmList();
		targetItem.setType(GermplasmList.FOLDER_TYPE);
		Mockito.when(germplasmListManager.getGermplasmListById((Integer) sourceItemId)).thenReturn(sourceItem);
		Mockito.when(germplasmListManager.getGermplasmListById((Integer) targetItemId)).thenReturn(targetItem);

		this.germplasmListSourceDropHandler.drop(event);

		Mockito.verify(germplasmListTreeUtil).setParent(sourceItemId, targetItemId);
		Mockito.verify(listSelectorComponent).refreshRemoteTree();
		Mockito.verify(this.window, Mockito.never()).showNotification(Mockito.any(Window.Notification.class));


	}

	@Test
	public void testDropListToAList() {

		final Object targetItemId = 2;
		final Object targetParent = 3;
		final Object sourceItemId = 1;

		final DragAndDropEvent event = Mockito.mock(DragAndDropEvent.class);
		final Transferable transferable = Mockito.mock(Transferable.class);
		final AbstractSelect.AbstractSelectTargetDetails target =  Mockito.mock(AbstractSelect.AbstractSelectTargetDetails.class);
		Mockito.when(event.getTransferable()).thenReturn(transferable);
		Mockito.when(event.getTargetDetails()).thenReturn(target);
		Mockito.when(target.getDropLocation()).thenReturn(VerticalDropLocation.MIDDLE);
		Mockito.when(target.getItemIdOver()).thenReturn(targetItemId);
		Mockito.when(transferable.getData("itemId")).thenReturn(sourceItemId);
		Mockito.when(transferable.getSourceComponent()).thenReturn(targetListSource);

		final GermplasmList sourceItem = new GermplasmList();
		sourceItem.setId((Integer) sourceItemId);
		final GermplasmList targetItem = new GermplasmList();
		targetItem.setId((Integer) targetItemId);
		targetItem.setParent(new GermplasmList((Integer) targetParent));
		Mockito.when(germplasmListManager.getGermplasmListById((Integer) sourceItemId)).thenReturn(sourceItem);
		Mockito.when(germplasmListManager.getGermplasmListById((Integer) targetItemId)).thenReturn(targetItem);

		this.germplasmListSourceDropHandler.drop(event);

		Mockito.verify(germplasmListTreeUtil, Mockito.never()).setParent(sourceItemId, targetParent);
		Mockito.verify(listSelectorComponent, Mockito.never()).refreshRemoteTree();
		Mockito.verify(this.window).showNotification(Mockito.any(Window.Notification.class));

	}
}
