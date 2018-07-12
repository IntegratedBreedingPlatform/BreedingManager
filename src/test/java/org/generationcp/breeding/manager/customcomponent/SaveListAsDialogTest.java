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
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class SaveListAsDialogTest {

	private static final String PROGRAM_UUID = "hdklashf-1837894-askdhasd";

	private static SaveListAsDialog dialog;
	private static GermplasmList germplasmList;
	private static GermplasmList originalGermplasmList;

	@Mock
	private SaveListAsDialogSource saveListAsDialogSource;

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
		SaveListAsDialogTest.germplasmList = this.createGermplasmList();
		SaveListAsDialogTest.dialog = new SaveListAsDialog(saveListAsDialogSource, SaveListAsDialogTest.germplasmList);
		dialog.setGermplasmListTree(germplasmListTree);
		dialog.setListDetailsComponent(listDetailsComponent);
		dialog.setGermplasmListManager(germplasmListManager);
		dialog.setMessageSource(messageSource);

		Window parent = Mockito.mock(Window.class);
		Mockito.when(parent.getWindow()).thenReturn(window);
		dialog.setParent(parent);

		Mockito.when(listDetailsComponent.getListDateField()).thenReturn(listDateField);
		Mockito.doNothing().when(listDateField).validate();

		Mockito.when(this.listDetailsComponent.getListNameField()).thenReturn(listNameField);
		Mockito.when(this.listDetailsComponent.getListDescriptionField()).thenReturn(listDescriptionField);
		Mockito.when(this.listDetailsComponent.getListTypeField()).thenReturn(listTypeField);
		Mockito.when(this.listDetailsComponent.getListDateField()).thenReturn(listDateField);
		Mockito.when(this.listDetailsComponent.getListNotesField()).thenReturn(listNotesField);

	}

	private GermplasmList createGermplasmList() {
		GermplasmList list = new GermplasmList();
		list.setName("Test List Name");
		list.setDescription("Test Description");
		list.setType("LST");
		list.setDate(Long.parseLong("20141105"));
		list.setNotes("Sample Notes");
		list.setProgramUUID(PROGRAM_UUID);
		return list;
	}

	/**
	 * The test is location dependant.
	 */
	@Ignore
	@Test
	public void testGetCurrentParsedListDateForValidDateFormat() {
		Long expectedDate = 20111106L;
		Long parsedDate = SaveListAsDialogTest.dialog.getCurrentParsedListDate("Thu Nov 06 09:39:00 SGT 2011");

		Assert.assertEquals("Expected for input E MMM dd HH:mm:ss Z yyyy will return yyyymmdd but didn't.", expectedDate, parsedDate);
	}

	@Test
	public void testGetCurrentParsedListDateForInvalidDateFormat() {
		Calendar currentDate = DateUtil.getCalendarInstance();
		String currentDateString = String.valueOf(currentDate.get(Calendar.YEAR)) + this
				.appendZeroForSingleDigitMonthOrDay(currentDate.get(Calendar.MONTH) + 1) + this
				.appendZeroForSingleDigitMonthOrDay(currentDate.get(Calendar.DAY_OF_MONTH));
		Long expectedDate = Long.parseLong(currentDateString);

		// invalid date
		Long parsedDate = SaveListAsDialogTest.dialog.getCurrentParsedListDate("2014-22-22");
		Assert.assertEquals("Expected for invalid input return the current date in this format yyyymmdd but didn't.", expectedDate,
				parsedDate);
	}

	private String appendZeroForSingleDigitMonthOrDay(int digit) {
		return digit <= 9 ? String.valueOf("0" + digit) : String.valueOf(digit);
	}

	@Test
	public void testIsSelectedListLockedReturnsTrueForAList() {
		SaveListAsDialogTest.germplasmList.setStatus(100);
		Assert.assertTrue("Expected to return true for a germplasm list with status >= 100 but didn't.",
				SaveListAsDialogTest.dialog.isSelectedListLocked());
	}

	@Test
	public void testIsSelectedListLockedReturnsFalseForAList() {
		SaveListAsDialogTest.germplasmList.setStatus(1);
		Assert.assertFalse("Expected to return false for a germplasm list with status < 100 but didn't.",
				SaveListAsDialogTest.dialog.isSelectedListLocked());

		// reset germplasm list instance
		this.createGermplasmList();

		SaveListAsDialogTest.germplasmList = null;
		Assert.assertFalse("Expected to return false for a germplasm list that is null but didn't.",
				SaveListAsDialogTest.dialog.isSelectedListLocked());
	}

	@Test
	public void testIsSelectedListAnExistingListReturnsTrueForAList() {
		SaveListAsDialogTest.germplasmList.setId(-1);
		SaveListAsDialogTest.germplasmList.setType("LST");
		SaveListAsDialogTest.dialog.setOriginalGermplasmList(null);

		Assert.assertTrue("Expected to return true for a germplasm list with LST type but didn't.",
				SaveListAsDialogTest.dialog.isSelectedListAnExistingList());
	}

	@Test
	public void testIsSelectedListAnExistingListReturnsFalseForAList() {
		// new list
		SaveListAsDialogTest.germplasmList.setId(null);
		SaveListAsDialogTest.germplasmList.setType("LST");
		SaveListAsDialogTest.dialog.setOriginalGermplasmList(null);

		Assert.assertFalse("Expected to return false for a germplasm list with id = null but didn't.",
				SaveListAsDialogTest.dialog.isSelectedListAnExistingList());

		// is a folder
		SaveListAsDialogTest.germplasmList.setId(-1);
		SaveListAsDialogTest.germplasmList.setType("FOLDER");
		SaveListAsDialogTest.dialog.setOriginalGermplasmList(null);
		Assert.assertFalse("Expected to return false when the item selected is a folder but didn't.",
				SaveListAsDialogTest.dialog.isSelectedListAnExistingList());
	}

	@Test
	public void testisSelectedListNotSameWithTheOriginalListReturnsTrueForAList() {
		SaveListAsDialogTest.germplasmList.setId(-1);
		SaveListAsDialogTest.dialog.setOriginalGermplasmList(SaveListAsDialogTest.germplasmList);

		Assert.assertFalse("Expecting the selected list is the original list in the save dialog but didn't.",
				SaveListAsDialogTest.dialog.isSelectedListNotSameWithTheOriginalList());
	}

	@Test
	public void testisSelectedListNotSameWithTheOriginalListReturnsFalseForAList() {
		SaveListAsDialogTest.germplasmList.setId(-1);
		SaveListAsDialogTest.originalGermplasmList = SaveListAsDialogTest.dialog.getOriginalGermplasmList();
		SaveListAsDialogTest.originalGermplasmList.setId(-2);

		Assert.assertFalse("Expecting the selected list is not the original list in the save dialog but didn't.",
				SaveListAsDialogTest.dialog.isSelectedListNotSameWithTheOriginalList());
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItself() {
		SaveListAsDialog proxy = Mockito.spy(SaveListAsDialogTest.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(true);

		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertTrue("Given it is an existing list, the existing list will be overwritten", result);
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItselfNoListToOverwrite() {
		SaveListAsDialog proxy = Mockito.spy(SaveListAsDialogTest.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		GermplasmList listToOverWrite = null;
		proxy.setOriginalGermplasmList(listToOverWrite);
		Mockito.when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(false);

		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertFalse(
				"Given it is not an existing list " + "and the selected list to overwrite is null, " + "the list to save is a new record",
				result);
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItselfAnExistingListWithListToOverwrite() {
		SaveListAsDialog proxy = Mockito.spy(SaveListAsDialogTest.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		GermplasmList listToOverWrite = new GermplasmList(-1000);
		proxy.setOriginalGermplasmList(listToOverWrite);
		Mockito.when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(false);

		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertFalse("Given it is not an existing list " + "and the selected list to overwrite is the same as the list to save, "
				+ "the list to save is an existing record", result);
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItselfANewListWithListToOverwrite() {
		SaveListAsDialog proxy = Mockito.spy(SaveListAsDialogTest.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		GermplasmList listToOverWrite = new GermplasmList(-1000);
		proxy.setOriginalGermplasmList(listToOverWrite);
		Mockito.when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(true);

		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertTrue("Given it is not an existing list " + "and the selected list to overwrite is not the same as the list to save, "
				+ "the selected list will be overwritten", result);

	}

	// Target list os locked
	@Test
	public void testDoSaveActionTargetListIsLocked() {

		final Integer selectedListId = 123;
		final GermplasmList germplasmList = createGermplasmList();
		germplasmList.setId(selectedListId);
		germplasmList.setStatus(101);

		Mockito.when(messageSource.getMessage(Message.ERROR)).thenReturn("Error");
		Mockito.when(messageSource.getMessage(Message.UNABLE_TO_EDIT_LOCKED_LIST)).thenReturn("Unable to edit list");
		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(germplasmList);

		dialog.doSaveAction(Mockito.mock(Button.ClickEvent.class));

		ArgumentCaptor<Window.Notification> captor = ArgumentCaptor.forClass(Window.Notification.class);
		Mockito.verify(window).showNotification(captor.capture());

		Window.Notification notification = captor.getValue();

		Assert.assertEquals("Error", notification.getCaption());
		Assert.assertEquals("</br>Unable to edit list", notification.getDescription());

	}

	// If target list to be overwritten is not itself and is an existing list
	@Test
	public void testDoSaveActionTargetListIsNotItselfAndAnExistingList() {

		final Integer existingGermplasmListId = 1;
		GermplasmList existingGermplasmList = createGermplasmList();
		existingGermplasmList.setId(existingGermplasmListId);
		existingGermplasmList.setStatus(1);
		dialog.setGermplasmList(existingGermplasmList);
		dialog.setOriginalGermplasmList(null);
		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(existingGermplasmListId);
		Mockito.when(germplasmListManager.getGermplasmListById(existingGermplasmListId)).thenReturn(existingGermplasmList);

		Mockito.when(listNameField.getValue()).thenReturn(existingGermplasmList.getName());
		Mockito.when(listDescriptionField.getValue()).thenReturn(existingGermplasmList.getDescription());
		Mockito.when(listTypeField.getValue()).thenReturn(existingGermplasmList.getType());
		Mockito.when(listDateField.getValue()).thenReturn(new Date(existingGermplasmList.getDate()));
		Mockito.when(listNotesField.getValue()).thenReturn(existingGermplasmList.getNotes());

		Mockito.when(messageSource.getMessage(Message.DO_YOU_WANT_TO_OVERWRITE_THIS_LIST)).thenReturn("Do you want to overwrite this list");
		Mockito.when(messageSource
				.getMessage(Message.LIST_DATA_WILL_BE_DELETED_AND_WILL_BE_REPLACED_WITH_THE_DATA_FROM_THE_LIST_THAT_YOU_JUST_CREATED))
				.thenReturn("List data willbe deleted");
		Mockito.when(messageSource.getMessage(Message.OK)).thenReturn("Ok");
		Mockito.when(messageSource.getMessage(Message.CANCEL)).thenReturn("Cancel");

		dialog.doSaveAction(Mockito.mock(Button.ClickEvent.class));

		ArgumentCaptor<ConfirmDialog> captor = ArgumentCaptor.forClass(ConfirmDialog.class);
		Mockito.verify(window).addWindow(captor.capture());

		ConfirmDialog dialog = captor.getValue();

		Assert.assertEquals("Do you want to overwrite this list?", dialog.getCaption());
		Assert.assertEquals("List data willbe deleted", dialog.getMessage());
		Assert.assertEquals("Ok", dialog.getOkButton().getCaption());
		Assert.assertEquals("Cancel", dialog.getCancelButton().getCaption());

	}

	@Test
	public void testDoSaveActionTargetListDefault() {

		dialog.setGermplasmList(null);
		dialog.setOriginalGermplasmList(null);

		final Integer selectedListId = 10;
		final GermplasmList selectedGermplasmList = createGermplasmList();
		selectedGermplasmList.setId(selectedListId);
		selectedGermplasmList.setType(SaveListAsDialog.FOLDER_TYPE);

		final GermplasmList newGermplasmList = createGermplasmList();

		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(selectedGermplasmList);
		Mockito.when(listDetailsComponent.createGermplasmListFromListDetails(Mockito.anyBoolean())).thenReturn(newGermplasmList);
		Mockito.when(listDetailsComponent.validate()).thenReturn(true);

		Mockito.when(listNameField.getValue()).thenReturn("Sample Name");
		Mockito.when(listDescriptionField.getValue()).thenReturn("Sample Description");
		Mockito.when(listTypeField.getValue()).thenReturn("Germplasm List");
		Mockito.when(listDateField.getValue()).thenReturn(new Date("01/01/2017"));
		Mockito.when(listNotesField.getValue()).thenReturn("Sample Notes");

		Button.ClickEvent event = Mockito.mock(Button.ClickEvent.class);
		Button button = Mockito.mock(Button.class);
		Window buttonWindow = Mockito.mock(Window.class);
		Window parentWindow = Mockito.mock(Window.class);
		Mockito.when(event.getButton()).thenReturn(button);
		Mockito.when(button.getWindow()).thenReturn(buttonWindow);
		Mockito.when(buttonWindow.getParent()).thenReturn(parentWindow);

		dialog.doSaveAction(event);

		Assert.assertEquals("Sample Name", newGermplasmList.getName());
		Assert.assertEquals("Sample Description", newGermplasmList.getDescription());
		Assert.assertEquals("Germplasm List", newGermplasmList.getType());
		Assert.assertEquals("20170101", newGermplasmList.getDate().toString());
		Assert.assertEquals("Sample Notes", newGermplasmList.getNotes());
		Assert.assertEquals(SaveListAsDialog.LIST_NAMES_STATUS, newGermplasmList.getStatus());

		Mockito.verify(saveListAsDialogSource).saveList(newGermplasmList);
		Mockito.verify(parentWindow).removeWindow(Mockito.any(Window.class));

	}

	@Test
	public void testDoSaveActionTargetListGermplasmListIsSavedInCropListsFolder() {

		dialog.setGermplasmList(null);
		dialog.setOriginalGermplasmList(null);

		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(ListSelectorComponent.CROP_LISTS);

		final GermplasmList newGermplasmList = createGermplasmList();
		newGermplasmList.setProgramUUID(null);

		Mockito.when(listDetailsComponent.createGermplasmListFromListDetails(Mockito.anyBoolean())).thenReturn(newGermplasmList);
		Mockito.when(listDetailsComponent.validate()).thenReturn(true);

		Mockito.when(listNameField.getValue()).thenReturn("Sample Name");
		Mockito.when(listDescriptionField.getValue()).thenReturn("Sample Description");
		Mockito.when(listTypeField.getValue()).thenReturn("Germplasm List");
		Mockito.when(listDateField.getValue()).thenReturn(new Date("01/01/2017"));
		Mockito.when(listNotesField.getValue()).thenReturn("Sample Notes");

		Button.ClickEvent event = Mockito.mock(Button.ClickEvent.class);
		Button button = Mockito.mock(Button.class);
		Window buttonWindow = Mockito.mock(Window.class);
		Window parentWindow = Mockito.mock(Window.class);
		Mockito.when(event.getButton()).thenReturn(button);
		Mockito.when(button.getWindow()).thenReturn(buttonWindow);
		Mockito.when(buttonWindow.getParent()).thenReturn(parentWindow);

		dialog.doSaveAction(event);

		Assert.assertEquals("Sample Name", newGermplasmList.getName());
		Assert.assertEquals("Sample Description", newGermplasmList.getDescription());
		Assert.assertEquals("Germplasm List", newGermplasmList.getType());
		Assert.assertEquals("20170101", newGermplasmList.getDate().toString());
		Assert.assertEquals("Sample Notes", newGermplasmList.getNotes());
		Assert.assertEquals(SaveListAsDialog.LIST_LOCKED_STATUS, newGermplasmList.getStatus());

		Mockito.verify(saveListAsDialogSource).saveList(newGermplasmList);
		Mockito.verify(parentWindow).removeWindow(Mockito.any(Window.class));

	}

	@Test
	public void testGetGermplasmListToSaveSelectedItemIsRootFolder() {

		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(ListSelectorComponent.CROP_LISTS);
		Mockito.when(listDetailsComponent.createGermplasmListFromListDetails(Mockito.anyBoolean())).thenReturn(germplasmList);

		GermplasmList germplasmListToSave = dialog.getGermplasmListToSave();

		Assert.assertNull(germplasmListToSave.getParent());
		Assert.assertEquals(SaveListAsDialog.LIST_NAMES_STATUS, germplasmListToSave.getStatus());

	}

	@Test
	public void testGetGermplasmListToSaveSelectedItemIsAGermplasmList() {

		final Integer selectedListId = 10;
		final GermplasmList selectedGermplasmList = createGermplasmList();
		selectedGermplasmList.setId(selectedListId);

		final Integer selectedListParentId = 11;
		final GermplasmList selectedGermplasmListParent = createGermplasmList();
		selectedGermplasmListParent.setId(selectedListParentId);

		selectedGermplasmList.setParent(selectedGermplasmListParent);

		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(selectedGermplasmList);
		Mockito.when(germplasmListManager.getGermplasmListById(selectedListParentId)).thenReturn(selectedGermplasmListParent);

		GermplasmList germplasmListToSave = dialog.getGermplasmListToSave();

		Mockito.verify(saveListAsDialogSource).setCurrentlySavedGermplasmList(germplasmListToSave);
		Assert.assertEquals(selectedGermplasmListParent, germplasmListToSave.getParent());

	}

	@Test
	public void testGetGermplasmListToSaveSelectedItemIsAFolder() {

		final Integer selectedListId = 10;
		final GermplasmList selectedGermplasmList = createGermplasmList();
		selectedGermplasmList.setId(selectedListId);
		selectedGermplasmList.setType(SaveListAsDialog.FOLDER_TYPE);

		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(selectedGermplasmList);
		Mockito.when(listDetailsComponent.createGermplasmListFromListDetails(Mockito.anyBoolean())).thenReturn(selectedGermplasmList);

		GermplasmList germplasmListToSave = dialog.getGermplasmListToSave();

		Assert.assertEquals(selectedGermplasmList, germplasmListToSave.getParent());
		Assert.assertEquals(SaveListAsDialog.LIST_NAMES_STATUS, germplasmListToSave.getStatus());

	}

	@Test
	public void testGetSelectedListOnTree() {

		final Integer selectedListId = 123;
		final GermplasmList germplasmList = createGermplasmList();
		germplasmList.setId(selectedListId);
		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(selectedListId);
		Mockito.when(germplasmListManager.getGermplasmListById(selectedListId)).thenReturn(germplasmList);

		Assert.assertEquals(germplasmList, dialog.getSelectedListOnTree());

	}

	@Test
	public void testGetSelectedListOnTreeSelectedItemIsRoot() {

		Mockito.when(germplasmListTree.getSelectedListId()).thenReturn(ListSelectorComponent.CROP_LISTS);

		Assert.assertNull(dialog.getSelectedListOnTree());

	}

	@Test
	public void testIsCropList() {

		// Selected item is Crop Lists folder
		Assert.assertTrue(dialog.isCropList(ListSelectorComponent.CROP_LISTS));

		// Selected item is Program Lists folder
		Assert.assertFalse(dialog.isCropList(ListSelectorComponent.PROGRAM_LISTS));

		final Integer selectedItemId = 1;

		// Parent of the selected list is Crop Lists folder
		Mockito.when(germplasmListTree.getParentOfListItem(selectedItemId)).thenReturn(ListSelectorComponent.CROP_LISTS);
		Assert.assertTrue(dialog.isCropList(selectedItemId));

		// Parent of the selected list is Program Lists folder
		Mockito.when(germplasmListTree.getParentOfListItem(selectedItemId)).thenReturn(ListSelectorComponent.PROGRAM_LISTS);
		Assert.assertFalse(dialog.isCropList(selectedItemId));

		// Parent of the selected list is a folder
		Mockito.when(germplasmListTree.getParentOfListItem(selectedItemId)).thenReturn(123);
		Assert.assertFalse(dialog.isCropList(selectedItemId));

	}
	
	@Test
	public void testCloseEvent() throws Exception {
		Window window = new Window();
		ListComponent component = Mockito.mock(ListComponent.class);
		Mockito.when(component.getWindow()).thenReturn(window);
		SaveListAsDialog saveListAsDialog = new SaveListAsDialog(component, GermplasmListTestDataInitializer.createGermplasmList(1));
		saveListAsDialog.setCancelButton(new Button());
		saveListAsDialog.setSaveButton(new Button());
		saveListAsDialog.addListeners();
		component.getWindow().addWindow(saveListAsDialog);
		saveListAsDialog.getParent().removeWindow(saveListAsDialog);
		Mockito.verify(component).updateListUI();
	}
	
	@Test
	public void testCancelButtonClick() throws Exception {
		Window window = new Window();
		ListComponent component = Mockito.mock(ListComponent.class);
		Mockito.when(component.getWindow()).thenReturn(window);
		SaveListAsDialog saveListAsDialog = new SaveListAsDialog(component, GermplasmListTestDataInitializer.createGermplasmList(1));
		saveListAsDialog.setCancelButton(new Button());
		saveListAsDialog.setSaveButton(new Button());
		saveListAsDialog.addListeners();
		component.getWindow().addWindow(saveListAsDialog);
		saveListAsDialog.addComponent(saveListAsDialog.getCancelButton());
		saveListAsDialog.getCancelButton().click();
		Mockito.verify(component).updateListUI();
	}
	
	@Test
	public void testSaveButtonClickEvent() throws Exception {
		dialog.setCancelButton(new Button());
		dialog.setSaveButton(new Button());
		dialog.addListeners();
		Mockito.when(this.germplasmListTree.getSelectedListId()).thenReturn("1");
		Mockito.when(this.listDetailsComponent.createGermplasmListFromListDetails(false)).thenReturn(this.germplasmList);
		dialog.getSaveButton().click();
		Mockito.verify(this.saveListAsDialogSource).updateListUI();
		Mockito.verify(this.germplasmListTree, Mockito.times(2)).getSelectedListId();
		Mockito.verify(this.listDetailsComponent).createGermplasmListFromListDetails(false);
	}

}
