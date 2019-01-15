package org.generationcp.breeding.manager.customcomponent;

import java.util.Calendar;
import java.util.Date;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.customfields.ListDateField;
import org.generationcp.breeding.manager.customfields.ListDescriptionField;
import org.generationcp.breeding.manager.customfields.ListNameField;
import org.generationcp.breeding.manager.customfields.ListNotesField;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.customfields.ListTypeField;
import org.generationcp.breeding.manager.customfields.LocalListFoldersTreeComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class SaveListAsDialogTest {

	private static final String PROGRAM_UUID = "hdklashf-1837894-askdhasd";

	private SaveListAsDialog dialog;
	private GermplasmList germplasmList;

	@Mock
	private ListComponent source;

	@Mock
	private LocalListFoldersTreeComponent germplasmListTree;

	@Mock
	private BreedingManagerListDetailsComponent listDetailsComponent;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private Window window;

	@Mock
	private ListNameField listNameField;

	@Mock
	private ListDescriptionField listDescriptionField;

	@Mock
	private ListTypeField listTypeField;

	@Mock
	private ListDateField listDateField;

	@Mock
	private ListNotesField listNotesField;

	@Before
	public void setUp() {
		this.germplasmList = this.createGermplasmList();
		this.dialog = new SaveListAsDialog(this.source, this.germplasmList);
		this.dialog.setGermplasmListTree(this.germplasmListTree);
		this.dialog.setListDetailsComponent(this.listDetailsComponent);
		this.dialog.setGermplasmListManager(this.germplasmListManager);
		this.dialog.setMessageSource(this.messageSource);

		final Window window = new Window();
		Mockito.when(this.source.getWindow()).thenReturn(window);
		this.source.getWindow().addWindow(this.dialog);

		Mockito.when(this.listDetailsComponent.getListDateField()).thenReturn(this.listDateField);
		Mockito.doNothing().when(this.listDateField).validate();

		Mockito.when(this.listDetailsComponent.getListNameField()).thenReturn(this.listNameField);
		Mockito.when(this.listDetailsComponent.getListDescriptionField()).thenReturn(this.listDescriptionField);
		Mockito.when(this.listDetailsComponent.getListTypeField()).thenReturn(this.listTypeField);
		Mockito.when(this.listDetailsComponent.getListDateField()).thenReturn(this.listDateField);
		Mockito.when(this.listDetailsComponent.getListNotesField()).thenReturn(this.listNotesField);

	}

	private GermplasmList createGermplasmList() {
		final GermplasmList list = new GermplasmList();
		list.setName("Test List Name");
		list.setDescription("Test Description");
		list.setType("LST");
		list.setDate(Long.parseLong("20141105"));
		list.setNotes("Sample Notes");
		list.setProgramUUID(SaveListAsDialogTest.PROGRAM_UUID);
		return list;
	}

	/**
	 * The test is location dependant.
	 */
	@Ignore
	@Test
	public void testGetCurrentParsedListDateForValidDateFormat() {
		final Long expectedDate = 20111106L;
		final Long parsedDate = this.dialog.getCurrentParsedListDate("Thu Nov 06 09:39:00 SGT 2011");

		Assert.assertEquals("Expected for input E MMM dd HH:mm:ss Z yyyy will return yyyymmdd but didn't.",
				expectedDate, parsedDate);
	}

	@Test
	public void testGetCurrentParsedListDateForInvalidDateFormat() {
		final Calendar currentDate = DateUtil.getCalendarInstance();
		final String currentDateString = String.valueOf(currentDate.get(Calendar.YEAR))
				+ this.appendZeroForSingleDigitMonthOrDay(currentDate.get(Calendar.MONTH) + 1)
				+ this.appendZeroForSingleDigitMonthOrDay(currentDate.get(Calendar.DAY_OF_MONTH));
		final Long expectedDate = Long.parseLong(currentDateString);

		// invalid date
		final Long parsedDate = this.dialog.getCurrentParsedListDate("2014-22-22");
		Assert.assertEquals("Expected for invalid input return the current date in this format yyyymmdd but didn't.",
				expectedDate, parsedDate);
	}

	private String appendZeroForSingleDigitMonthOrDay(final int digit) {
		return digit <= 9 ? String.valueOf("0" + digit) : String.valueOf(digit);
	}

	@Test
	public void testIsSelectedListLockedReturnsTrueForAList() {
		this.germplasmList.setStatus(100);
		Assert.assertTrue("Expected to return true for a germplasm list with status >= 100 but didn't.",
				this.dialog.isSelectedListLocked());
	}

	@Test
	public void testIsSelectedListLockedReturnsFalseForAList() {
		this.germplasmList.setStatus(1);
		Assert.assertFalse("Expected to return false for a germplasm list with status < 100 but didn't.",
				this.dialog.isSelectedListLocked());

		// reset germplasm list instance
		this.createGermplasmList();

		this.germplasmList = null;
		Assert.assertFalse("Expected to return false for a germplasm list that is null but didn't.",
				this.dialog.isSelectedListLocked());
	}

	@Test
	public void testIsSelectedListAnExistingListReturnsTrueForAList() {
		this.germplasmList.setId(-1);
		this.germplasmList.setType("LST");
		this.dialog.setOriginalGermplasmList(null);

		Assert.assertTrue("Expected to return true for a germplasm list with LST type but didn't.",
				this.dialog.isSelectedListAnExistingList());
	}

	@Test
	public void testIsSelectedListAnExistingListReturnsFalseForAList() {
		// new list
		this.germplasmList.setId(null);
		this.germplasmList.setType("LST");
		this.dialog.setOriginalGermplasmList(null);

		Assert.assertFalse("Expected to return false for a germplasm list with id = null but didn't.",
				this.dialog.isSelectedListAnExistingList());

		// is a folder
		this.germplasmList.setId(-1);
		this.germplasmList.setType("FOLDER");
		this.dialog.setOriginalGermplasmList(null);
		Assert.assertFalse("Expected to return false when the item selected is a folder but didn't.",
				this.dialog.isSelectedListAnExistingList());
	}

	@Test
	public void testisSelectedListNotSameWithTheOriginalListReturnsTrueForAList() {
		this.germplasmList.setId(-1);
		this.dialog.setOriginalGermplasmList(this.germplasmList);

		Assert.assertFalse("Expecting the selected list is the original list in the save dialog but didn't.",
				this.dialog.isSelectedListNotSameWithTheOriginalList());
	}

	@Test
	public void testisSelectedListNotSameWithTheOriginalListReturnsFalseForAList() {
		this.germplasmList.setId(-1);
		final GermplasmList originalGermplasmList = this.dialog.getOriginalGermplasmList();
		originalGermplasmList.setId(-2);

		Assert.assertFalse("Expecting the selected list is not the original list in the save dialog but didn't.",
				this.dialog.isSelectedListNotSameWithTheOriginalList());
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItself() {
		final SaveListAsDialog proxy = Mockito.spy(this.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(true);

		final boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertTrue("Given it is an existing list, the existing list will be overwritten", result);
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItselfNoListToOverwrite() {
		final SaveListAsDialog proxy = Mockito.spy(this.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		final GermplasmList listToOverWrite = null;
		proxy.setOriginalGermplasmList(listToOverWrite);
		Mockito.when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(false);

		final boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertFalse("Given it is not an existing list " + "and the selected list to overwrite is null, "
				+ "the list to save is a new record", result);
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItselfAnExistingListWithListToOverwrite() {
		final SaveListAsDialog proxy = Mockito.spy(this.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		final GermplasmList listToOverWrite = new GermplasmList(-1000);
		proxy.setOriginalGermplasmList(listToOverWrite);
		Mockito.when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(false);

		final boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertFalse("Given it is not an existing list "
				+ "and the selected list to overwrite is the same as the list to save, "
				+ "the list to save is an existing record", result);
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItselfANewListWithListToOverwrite() {
		final SaveListAsDialog proxy = Mockito.spy(this.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		final GermplasmList listToOverWrite = new GermplasmList(-1000);
		proxy.setOriginalGermplasmList(listToOverWrite);
		Mockito.when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(true);

		final boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertTrue("Given it is not an existing list "
				+ "and the selected list to overwrite is not the same as the list to save, "
				+ "the selected list will be overwritten", result);

	}

	// Target list os locked
	@Test
	public void testDoSaveActionTargetListIsLocked() {

		final Integer selectedListId = 123;
		final GermplasmList germplasmList = this.createGermplasmList();
		germplasmList.setId(selectedListId);
		germplasmList.setStatus(101);

		Mockito.when(this.messageSource.getMessage(Message.ERROR)).thenReturn("Error");
		Mockito.when(this.messageSource.getMessage(Message.UNABLE_TO_EDIT_LOCKED_LIST))
				.thenReturn("Unable to edit list");
		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(this.germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(germplasmList);

		final Window parent = Mockito.mock(Window.class);
		Mockito.when(parent.getWindow()).thenReturn(this.window);
		this.dialog.getParent().removeWindow(this.dialog);
		this.dialog.setParent(parent);

		this.dialog.doSaveAction(Mockito.mock(Button.ClickEvent.class));

		final ArgumentCaptor<Window.Notification> captor = ArgumentCaptor.forClass(Window.Notification.class);
		Mockito.verify(this.window).showNotification(captor.capture());

		final Window.Notification notification = captor.getValue();

		Assert.assertEquals("Error", notification.getCaption());
		Assert.assertEquals("</br>Unable to edit list", notification.getDescription());

	}

	// If target list to be overwritten is not itself and is an existing list
	@Test
	public void testDoSaveActionTargetListIsNotItselfAndAnExistingList() {

		final Integer existingGermplasmListId = 1;
		final GermplasmList existingGermplasmList = this.createGermplasmList();
		existingGermplasmList.setId(existingGermplasmListId);
		existingGermplasmList.setStatus(1);
		this.dialog.setGermplasmList(existingGermplasmList);
		this.dialog.setOriginalGermplasmList(null);
		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(existingGermplasmListId);
		Mockito.when(this.germplasmListManager.getGermplasmListById(existingGermplasmListId))
				.thenReturn(existingGermplasmList);

		Mockito.when(this.listNameField.getValue()).thenReturn(existingGermplasmList.getName());
		Mockito.when(this.listDescriptionField.getValue()).thenReturn(existingGermplasmList.getDescription());
		Mockito.when(this.listTypeField.getValue()).thenReturn(existingGermplasmList.getType());
		Mockito.when(this.listDateField.getValue()).thenReturn(new Date(existingGermplasmList.getDate()));
		Mockito.when(this.listNotesField.getValue()).thenReturn(existingGermplasmList.getNotes());

		Mockito.when(this.messageSource.getMessage(Message.DO_YOU_WANT_TO_OVERWRITE_THIS_LIST))
				.thenReturn("Do you want to overwrite this list");
		Mockito.when(this.messageSource.getMessage(
				Message.LIST_DATA_WILL_BE_DELETED_AND_WILL_BE_REPLACED_WITH_THE_DATA_FROM_THE_LIST_THAT_YOU_JUST_CREATED))
				.thenReturn("List data willbe deleted");
		Mockito.when(this.messageSource.getMessage(Message.OK)).thenReturn("Ok");
		Mockito.when(this.messageSource.getMessage(Message.CANCEL)).thenReturn("Cancel");

		final Window parent = Mockito.mock(Window.class);
		Mockito.when(parent.getWindow()).thenReturn(this.window);
		this.dialog.getParent().removeWindow(this.dialog);
		this.dialog.setParent(parent);

		this.dialog.doSaveAction(Mockito.mock(Button.ClickEvent.class));

		final ArgumentCaptor<ConfirmDialog> captor = ArgumentCaptor.forClass(ConfirmDialog.class);
		Mockito.verify(this.window).addWindow(captor.capture());

		final ConfirmDialog dialog = captor.getValue();

		Assert.assertEquals("Do you want to overwrite this list?", dialog.getCaption());
		Assert.assertEquals("List data willbe deleted", dialog.getMessage());
		Assert.assertEquals("Ok", dialog.getOkButton().getCaption());
		Assert.assertEquals("Cancel", dialog.getCancelButton().getCaption());

	}

	@Test
	public void testDoSaveActionTargetListDefault() {

		this.dialog.setGermplasmList(null);
		this.dialog.setOriginalGermplasmList(null);

		final Integer selectedListId = 10;
		final GermplasmList selectedGermplasmList = this.createGermplasmList();
		selectedGermplasmList.setId(selectedListId);
		selectedGermplasmList.setType(SaveListAsDialog.FOLDER_TYPE);

		final GermplasmList newGermplasmList = this.createGermplasmList();

		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(this.germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(selectedGermplasmList);
		Mockito.when(this.listDetailsComponent.createGermplasmListFromListDetails(Matchers.anyBoolean()))
				.thenReturn(newGermplasmList);
		Mockito.when(this.listDetailsComponent.validate()).thenReturn(true);

		Mockito.when(this.listNameField.getValue()).thenReturn("Sample Name");
		Mockito.when(this.listDescriptionField.getValue()).thenReturn("Sample Description");
		Mockito.when(this.listTypeField.getValue()).thenReturn("Germplasm List");
		Mockito.when(this.listDateField.getValue()).thenReturn(new Date("01/01/2017"));
		Mockito.when(this.listNotesField.getValue()).thenReturn("Sample Notes");

		final Button.ClickEvent event = Mockito.mock(Button.ClickEvent.class);
		final Button button = Mockito.mock(Button.class);
		final Window buttonWindow = Mockito.mock(Window.class);
		final Window parentWindow = Mockito.mock(Window.class);
		Mockito.when(event.getButton()).thenReturn(button);
		Mockito.when(button.getWindow()).thenReturn(buttonWindow);
		Mockito.when(buttonWindow.getParent()).thenReturn(parentWindow);

		this.dialog.doSaveAction(event);

		Assert.assertEquals("Sample Name", newGermplasmList.getName());
		Assert.assertEquals("Sample Description", newGermplasmList.getDescription());
		Assert.assertEquals("Germplasm List", newGermplasmList.getType());
		Assert.assertEquals("20170101", newGermplasmList.getDate().toString());
		Assert.assertEquals("Sample Notes", newGermplasmList.getNotes());
		Assert.assertEquals(SaveListAsDialog.LIST_NAMES_STATUS, newGermplasmList.getStatus());

		Mockito.verify(this.source).saveList(newGermplasmList);
		Mockito.verify(parentWindow).removeWindow(Matchers.any(Window.class));

	}

	@Test
	public void testDoSaveActionTargetListGermplasmListIsSavedInCropListsFolder() {

		this.dialog.setGermplasmList(null);
		this.dialog.setOriginalGermplasmList(null);

		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(ListSelectorComponent.CROP_LISTS);

		final GermplasmList newGermplasmList = this.createGermplasmList();
		newGermplasmList.setProgramUUID(null);

		Mockito.when(this.listDetailsComponent.createGermplasmListFromListDetails(Matchers.anyBoolean()))
				.thenReturn(newGermplasmList);
		Mockito.when(this.listDetailsComponent.validate()).thenReturn(true);

		Mockito.when(this.listNameField.getValue()).thenReturn("Sample Name");
		Mockito.when(this.listDescriptionField.getValue()).thenReturn("Sample Description");
		Mockito.when(this.listTypeField.getValue()).thenReturn("Germplasm List");
		Mockito.when(this.listDateField.getValue()).thenReturn(new Date("01/01/2017"));
		Mockito.when(this.listNotesField.getValue()).thenReturn("Sample Notes");

		final Button.ClickEvent event = Mockito.mock(Button.ClickEvent.class);
		final Button button = Mockito.mock(Button.class);
		final Window buttonWindow = Mockito.mock(Window.class);
		final Window parentWindow = Mockito.mock(Window.class);
		Mockito.when(event.getButton()).thenReturn(button);
		Mockito.when(button.getWindow()).thenReturn(buttonWindow);
		Mockito.when(buttonWindow.getParent()).thenReturn(parentWindow);

		this.dialog.doSaveAction(event);

		Assert.assertEquals("Sample Name", newGermplasmList.getName());
		Assert.assertEquals("Sample Description", newGermplasmList.getDescription());
		Assert.assertEquals("Germplasm List", newGermplasmList.getType());
		Assert.assertEquals("20170101", newGermplasmList.getDate().toString());
		Assert.assertEquals("Sample Notes", newGermplasmList.getNotes());
		Assert.assertEquals(SaveListAsDialog.LIST_LOCKED_STATUS, newGermplasmList.getStatus());

		Mockito.verify(this.source).saveList(newGermplasmList);
		Mockito.verify(parentWindow).removeWindow(Matchers.any(Window.class));

	}

	@Test
	public void testGetGermplasmListToSaveSelectedItemIsRootFolder() {

		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(ListSelectorComponent.CROP_LISTS);
		Mockito.when(this.listDetailsComponent.createGermplasmListFromListDetails(Matchers.anyBoolean()))
				.thenReturn(this.germplasmList);

		final GermplasmList germplasmListToSave = this.dialog.getGermplasmListToSave();

		Assert.assertNull(germplasmListToSave.getParent());
		Assert.assertEquals(SaveListAsDialog.LIST_NAMES_STATUS, germplasmListToSave.getStatus());

	}

	@Test
	public void testGetGermplasmListToSaveSelectedItemIsAGermplasmList() {

		final Integer selectedListId = 10;
		final GermplasmList selectedGermplasmList = this.createGermplasmList();
		selectedGermplasmList.setId(selectedListId);

		final Integer selectedListParentId = 11;
		final GermplasmList selectedGermplasmListParent = this.createGermplasmList();
		selectedGermplasmListParent.setId(selectedListParentId);

		selectedGermplasmList.setParent(selectedGermplasmListParent);

		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(this.germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(selectedGermplasmList);
		Mockito.when(this.germplasmListManager.getGermplasmListById(selectedListParentId))
				.thenReturn(selectedGermplasmListParent);

		final GermplasmList germplasmListToSave = this.dialog.getGermplasmListToSave();

		Mockito.verify(this.source).setCurrentlySavedGermplasmList(germplasmListToSave);
		Assert.assertEquals(selectedGermplasmListParent, germplasmListToSave.getParent());

	}

	@Test
	public void testGetGermplasmListToSaveSelectedItemIsAFolder() {

		final Integer selectedListId = 10;
		final GermplasmList selectedGermplasmList = this.createGermplasmList();
		selectedGermplasmList.setId(selectedListId);
		selectedGermplasmList.setType(SaveListAsDialog.FOLDER_TYPE);

		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(this.germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(selectedGermplasmList);
		Mockito.when(this.listDetailsComponent.createGermplasmListFromListDetails(Matchers.anyBoolean()))
				.thenReturn(selectedGermplasmList);

		final GermplasmList germplasmListToSave = this.dialog.getGermplasmListToSave();

		Assert.assertEquals(selectedGermplasmList, germplasmListToSave.getParent());
		Assert.assertEquals(SaveListAsDialog.LIST_NAMES_STATUS, germplasmListToSave.getStatus());

	}

	@Test
	public void testGetSelectedListOnTree() {

		final Integer selectedListId = 123;
		final GermplasmList germplasmList = this.createGermplasmList();
		germplasmList.setId(selectedListId);
		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(this.germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(germplasmList);

		Assert.assertEquals(germplasmList, this.dialog.getSelectedListOnTree());

	}

	@Test
	public void testGetSelectedListOnTreeSelectedItemIsRoot() {

		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn(ListSelectorComponent.CROP_LISTS);

		Assert.assertNull(this.dialog.getSelectedListOnTree());

	}

	@Test
	public void testIsCropList() {

		// Selected item is Crop Lists folder
		Assert.assertTrue(this.dialog.isCropList(ListSelectorComponent.CROP_LISTS));

		// Selected item is Program Lists folder
		Assert.assertFalse(this.dialog.isCropList(ListSelectorComponent.PROGRAM_LISTS));

		final Integer selectedItemId = 1;

		// Parent of the selected list is Crop Lists folder
		Mockito.when(this.germplasmListTree.getParentOfListItem(selectedItemId))
				.thenReturn(ListSelectorComponent.CROP_LISTS);
		Assert.assertTrue(this.dialog.isCropList(selectedItemId));

		// Parent of the selected list is Program Lists folder
		Mockito.when(this.germplasmListTree.getParentOfListItem(selectedItemId))
				.thenReturn(ListSelectorComponent.PROGRAM_LISTS);
		Assert.assertFalse(this.dialog.isCropList(selectedItemId));

		// Parent of the selected list is a folder
		Mockito.when(this.germplasmListTree.getParentOfListItem(selectedItemId)).thenReturn(123);
		Assert.assertFalse(this.dialog.isCropList(selectedItemId));

	}

	@Test
	public void testCloseEvent() throws Exception {
		this.dialog.setCancelButton(new Button());
		this.dialog.setSaveButton(new Button());
		this.dialog.addListeners();
		this.dialog.getParent().removeWindow(this.dialog);
		Mockito.verify(this.source).updateListUI();
	}

	@Test
	public void testCancelButtonClick() throws Exception {
		this.dialog.setCancelButton(new Button());
		this.dialog.setSaveButton(new Button());
		this.dialog.addListeners();
		this.dialog.addComponent(this.dialog.getCancelButton());
		this.dialog.getCancelButton().click();
		Mockito.verify(this.source).updateListUI();
	}

	@Test
	public void testSaveButtonClickEvent() throws Exception {
		this.dialog.setCancelButton(new Button());
		this.dialog.setSaveButton(new Button());
		this.dialog.addListeners();
		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn("1");
		Mockito.when(this.listDetailsComponent.createGermplasmListFromListDetails(false))
				.thenReturn(this.germplasmList);
		this.dialog.getSaveButton().click();
		Mockito.verify(this.source).updateListUI();
		Mockito.verify(this.germplasmListTree, Mockito.times(2)).getSelectedListId();
		Mockito.verify(this.listDetailsComponent).createGermplasmListFromListDetails(false);
	}

}
