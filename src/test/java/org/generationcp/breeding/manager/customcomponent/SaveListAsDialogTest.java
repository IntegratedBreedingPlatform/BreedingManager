
package org.generationcp.breeding.manager.customcomponent;

import java.util.Calendar;

import junit.framework.Assert;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SaveListAsDialogTest {

	private static SaveListAsDialog dialog;
	private static GermplasmList germplasmList;
	private static GermplasmList originalGermplasmList;
	private static SaveListAsDialogSource source;

	@Before
	public void setUp() {
		this.createGermplasmList();
		SaveListAsDialogTest.source = Mockito.mock(SaveListAsDialogSource.class);
		SaveListAsDialogTest.dialog = new SaveListAsDialog(SaveListAsDialogTest.source, SaveListAsDialogTest.germplasmList);
	}

	private void createGermplasmList() {
		SaveListAsDialogTest.germplasmList = new GermplasmList();
		SaveListAsDialogTest.germplasmList.setName("Test List Name");
		SaveListAsDialogTest.germplasmList.setDescription("Test Description");
		SaveListAsDialogTest.germplasmList.setType("LST");
		SaveListAsDialogTest.germplasmList.setDate(Long.parseLong("20141105"));
		SaveListAsDialogTest.germplasmList.setNotes("Sample Notes");
	}

	@Test
	public void testGetCurrentParsedListDateForValidDateFormat() {
		Long expectedDate = 20111106L;
		Long parsedDate = SaveListAsDialogTest.dialog.getCurrentParsedListDate("Thu Nov 06 09:39:00 SGT 2011");

		Assert.assertEquals("Expected for input E MMM dd HH:mm:ss Z yyyy will return yyyymmdd but didn't.", expectedDate, parsedDate);
	}

	@Test
	public void testGetCurrentParsedListDateForInvalidDateFormat() {
		Calendar currentDate = DateUtil.getCalendarInstance();
		String currentDateString =
				String.valueOf(currentDate.get(Calendar.YEAR))
						+ this.appendZeroForSingleDigitMonthOrDay(currentDate.get(Calendar.MONTH) + 1)
						+ this.appendZeroForSingleDigitMonthOrDay(currentDate.get(Calendar.DAY_OF_MONTH));
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
	public void testIsSelectedListAnExistingListButNotItself_NoListToOverwrite() {
		SaveListAsDialog proxy = Mockito.spy(SaveListAsDialogTest.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		GermplasmList listToOverWrite = null;
		proxy.setOriginalGermplasmList(listToOverWrite);
		Mockito.when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(false);

		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertFalse("Given it is not an existing list " + "and the selected list to overwrite is null, "
				+ "the list to save is a new record", result);
	}

	@Test
	public void testIsSelectedListAnExistingListButNotItself_AnExistingListWithListToOverwrite() {
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
	public void testIsSelectedListAnExistingListButNotItself_ANewListWithListToOverwrite() {
		SaveListAsDialog proxy = Mockito.spy(SaveListAsDialogTest.dialog);

		Mockito.when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		GermplasmList listToOverWrite = new GermplasmList(-1000);
		proxy.setOriginalGermplasmList(listToOverWrite);
		Mockito.when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(true);

		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertTrue("Given it is not an existing list " + "and the selected list to overwrite is not the same as the list to save, "
				+ "the selected list will be overwritten", result);

	}
}
