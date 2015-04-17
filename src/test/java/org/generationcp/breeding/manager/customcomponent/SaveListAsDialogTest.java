package org.generationcp.breeding.manager.customcomponent;

import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Locale;

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
	public void setUp(){
		createGermplasmList();
		source = Mockito.mock(SaveListAsDialogSource.class);
		dialog = new SaveListAsDialog(source, germplasmList);
	}
	
	private void createGermplasmList() {
		germplasmList = new GermplasmList();
		germplasmList.setName("Test List Name");
		germplasmList.setDescription("Test Description");
		germplasmList.setType("LST");
		germplasmList.setDate(Long.parseLong("20141105"));
		germplasmList.setNotes("Sample Notes");
	}

	@Test
	public void testGetCurrentParsedListDateForValidDateFormat(){
		Long expectedDate = 20111106L;
		Long parsedDate = dialog.getCurrentParsedListDate("Thu Nov 06 09:39:00 SGT 2011");
		
		Assert.assertEquals("Expected for input E MMM dd HH:mm:ss Z yyyy will return yyyymmdd but didn't.", expectedDate, parsedDate);
	}
	
	@Test
	public void testGetCurrentParsedListDateForInvalidDateFormat(){
		Calendar currentDate = DateUtil.getCalendarInstance();
		String currentDateString = String.valueOf(currentDate.get(Calendar.YEAR))
				+ appendZeroForSingleDigitMonthOrDay(currentDate.get(Calendar.MONTH) + 1) 
				+ appendZeroForSingleDigitMonthOrDay(currentDate.get(Calendar.DAY_OF_MONTH));
		Long expectedDate = Long.parseLong(currentDateString);
		
		//invalid date
		Long parsedDate = dialog.getCurrentParsedListDate("2014-22-22");
		Assert.assertEquals("Expected for invalid input return the current date in this format yyyymmdd but didn't.", expectedDate, parsedDate);
	}
	
	private String appendZeroForSingleDigitMonthOrDay(int digit){
		return (digit <= 9)? String.valueOf("0" + digit) : String.valueOf(digit);
	}
	
	@Test
	public void testIsSelectedListLockedReturnsTrueForAList(){
		germplasmList.setStatus(100);
		Assert.assertTrue("Expected to return true for a germplasm list with status >= 100 but didn't.", dialog.isSelectedListLocked());
	}
	
	@Test
	public void testIsSelectedListLockedReturnsFalseForAList(){
		germplasmList.setStatus(1);
		Assert.assertFalse("Expected to return false for a germplasm list with status < 100 but didn't.",dialog.isSelectedListLocked());
		
		//reset germplasm list instance
		createGermplasmList();
		
		germplasmList = null;
		Assert.assertFalse("Expected to return false for a germplasm list that is null but didn't.", dialog.isSelectedListLocked());
	}
	
	@Test
	public void testIsSelectedListAnExistingListReturnsTrueForAList(){
		germplasmList.setId(-1);
		germplasmList.setType("LST");
		dialog.setOriginalGermplasmList(null);
		
		Assert.assertTrue("Expected to return true for a germplasm list with LST type but didn't.", dialog.isSelectedListAnExistingList());
	}
	
	@Test
	public void testIsSelectedListAnExistingListReturnsFalseForAList(){
		//new list
		germplasmList.setId(null);
		germplasmList.setType("LST");
		dialog.setOriginalGermplasmList(null);
		
		Assert.assertFalse("Expected to return false for a germplasm list with id = null but didn't.", dialog.isSelectedListAnExistingList());
		
		//is a folder
		germplasmList.setId(-1);
		germplasmList.setType("FOLDER");
		dialog.setOriginalGermplasmList(null);
		Assert.assertFalse("Expected to return false when the item selected is a folder but didn't.", dialog.isSelectedListAnExistingList());
	}
	
	@Test
	public void testisSelectedListNotSameWithTheOriginalListReturnsTrueForAList(){
		germplasmList.setId(-1);
		dialog.setOriginalGermplasmList(germplasmList);
		
		Assert.assertFalse("Expecting the selected list is the original list in the save dialog but didn't.",dialog.isSelectedListNotSameWithTheOriginalList());
	}
	
	@Test
	public void testisSelectedListNotSameWithTheOriginalListReturnsFalseForAList(){
		germplasmList.setId(-1);
		originalGermplasmList = dialog.getOriginalGermplasmList();
		originalGermplasmList.setId(-2);
		
		Assert.assertFalse("Expecting the selected list is not the original list in the save dialog but didn't.",dialog.isSelectedListNotSameWithTheOriginalList());
	}
	
	@Test
	public void testIsSelectedListAnExistingListButNotItself() {
		SaveListAsDialog proxy = Mockito.spy(dialog);
		
		when(proxy.isSelectedListAnExistingList()).thenReturn(true);
		
		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertTrue("Given it is an existing list, the existing list will be overwritten",
				result);
	}
	
	@Test
	public void testIsSelectedListAnExistingListButNotItself_NoListToOverwrite() {
		SaveListAsDialog proxy = Mockito.spy(dialog);
		
		when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		GermplasmList listToOverWrite = null;
		proxy.setOriginalGermplasmList(listToOverWrite);
		when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(false);
		
		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertFalse("Given it is not an existing list " +
				"and the selected list to overwrite is null, " +
				"the list to save is a new record",
				result);
	}
	@Test
	public void testIsSelectedListAnExistingListButNotItself_AnExistingListWithListToOverwrite() {
		SaveListAsDialog proxy = Mockito.spy(dialog);
		
		when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		GermplasmList listToOverWrite = new GermplasmList(-1000);
		proxy.setOriginalGermplasmList(listToOverWrite);
		when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(false);
		
		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertFalse("Given it is not an existing list " +
				"and the selected list to overwrite is the same as the list to save, " +
				"the list to save is an existing record",
				result);
	}
	
	@Test
	public void testIsSelectedListAnExistingListButNotItself_ANewListWithListToOverwrite() {
		SaveListAsDialog proxy = Mockito.spy(dialog);
		
		when(proxy.isSelectedListAnExistingList()).thenReturn(false);
		GermplasmList listToOverWrite = new GermplasmList(-1000);
		proxy.setOriginalGermplasmList(listToOverWrite);
		when(proxy.isSelectedListNotSameWithTheOriginalList()).thenReturn(true);
		
		boolean result = proxy.isSelectedListAnExistingListButNotItself();
		Assert.assertTrue("Given it is not an existing list " +
				"and the selected list to overwrite is not the same as the list to save, " +
				"the selected list will be overwritten",
				result);
		
	}
}
