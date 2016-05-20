
package org.generationcp.breeding.manager.cross.study.adapted.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.breeding.manager.cross.study.adapted.main.QueryForAdaptedGermplasmMain;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

public class SaveToListDialogTest {

	@Mock
	private GermplasmListManager germplasmListManager;
	@Mock
	private QueryForAdaptedGermplasmMain queryFoAdaptedGermplasmMain;

	private SaveToListDialog saveToListDialog;
	private ComboBox combobox;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.saveToListDialog =
				Mockito.spy(new SaveToListDialog(this.queryFoAdaptedGermplasmMain, new AbsoluteLayout(), new Window(),
						new HashMap<Integer, String>()));
		this.saveToListDialog.setGermplasmListManager(this.germplasmListManager);

		this.combobox = new ComboBox();
		this.saveToListDialog.setComboboxListName(this.combobox);
	}

	@Test
	public void testListNameValues() throws MiddlewareQueryException {
		final Integer numberOfLists = 3;

		Mockito.when(this.germplasmListManager.countAllGermplasmLists()).thenReturn(Long.valueOf(numberOfLists));
		Mockito.when(this.germplasmListManager.getAllGermplasmLists(0, numberOfLists)).thenReturn(this.createDummyListsAndFolder());

		this.saveToListDialog.populateComboBoxListName();

		// there's an extra combobox item ("") for empty selection
		Assert.assertTrue("Three items available, consisting of two lists and one empty selection",
				numberOfLists.equals(this.combobox.size()));
		for (Object itemId : this.combobox.getItemIds()) {
			String listName = (String) itemId;
			if (!"".equals(listName)) {
				Assert.assertTrue("Expecting only lists are included", listName.contains("LIST"));
			}
		}

	}

	private List<GermplasmList> createDummyListsAndFolder() {
		List<GermplasmList> lists = new ArrayList<GermplasmList>();
		lists.add(new GermplasmList(-1, "LIST 1", new Long("20141215"), "LST", -1, "test", null, 1, null, null, null, null, null,
				"TEST NOTES", null));
		lists.add(new GermplasmList(-2, "LIST 2", new Long("20141216"), "LST", -1, "test", null, 1, null, null, null, null, null,
				"TEST NOTES", null));
		lists.add(new GermplasmList(-3, "FOLDER", new Long("20141217"), "FOLDER", -1, "test", null, 1, null, null, null, null, null,
				"TEST NOTES", null));

		return lists;
	}

	@Test
	public void testValidateListNameToSaveForExistingListNameInput() throws MiddlewareQueryException {
		String listName = "Existing List Name";

		Mockito.when(this.germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL)).thenReturn(1L);
		try {
			this.saveToListDialog.validateListNameToSave(listName);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expected to return an exception message.", "There is already an existing germplasm list with that name",
					e.getMessage());
		}
	}

	@Test
	public void testValidateListNameToSaveForEmptyStringInput() throws MiddlewareQueryException {
		String listName = "";

		Mockito.when(this.germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL)).thenReturn(0L);
		try {
			this.saveToListDialog.validateListNameToSave(listName);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expected to return an exception message.", "Please specify a List Name before saving", e.getMessage());
		}
	}

	@Test
	public void testValidateListNameToSaveForLongListNameInput() throws MiddlewareQueryException {
		String listName = "1234567890123456789012345678901234567890123456789012345";

		Mockito.when(this.germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL)).thenReturn(0L);
		try {
			this.saveToListDialog.validateListNameToSave(listName);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expected to return an exception message.",
					"Listname input is too large limit the name only up to 50 characters", e.getMessage());
		}
	}

}
