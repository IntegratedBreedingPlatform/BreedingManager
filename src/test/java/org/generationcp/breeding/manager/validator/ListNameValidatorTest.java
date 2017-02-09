
package org.generationcp.breeding.manager.validator;

import java.util.Collections;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import junit.framework.Assert;

public class ListNameValidatorTest {

	private static final String OLD_LIST_NAME = "Old List Name";

	private static final String NEW_LIST_NAME = "New List Name";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	private ListNameValidator listNameValidator;

	private final String DUMMY_ERROR_MESSAGE = "This is an error message.";

	private static final String PROGRAM_UUID = "1234567";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.messageSource.getMessage(Message.INVALID_ITEM_NAME)).thenReturn(this.DUMMY_ERROR_MESSAGE);
		Mockito.when(this.messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE)).thenReturn(this.DUMMY_ERROR_MESSAGE);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_VALIDATING_LIST)).thenReturn(this.DUMMY_ERROR_MESSAGE);

		this.listNameValidator = Mockito.spy(new ListNameValidator());
		this.listNameValidator.setMessageSource(this.messageSource);
		this.listNameValidator.setGermplasmListManager(this.germplasmListManager);

		Mockito.doReturn(ListNameValidatorTest.PROGRAM_UUID).when(this.listNameValidator).getCurrentProgramUUID();
	}

	@Test
	public void testValidateListNameForEmptyListName() {
		Assert.assertFalse("Expecting that the validator will return false for empty list name.",
				this.listNameValidator.validateListName(""));
	}
	
	@Test
	public void testValidateListNameForWhitespaceListName() {
		Assert.assertFalse("Expecting that the validator will return false for list name composing only of whitespaces.",
				this.listNameValidator.validateListName("   "));
	}

	@Test
	public void testValidateListNameForRootFolderLists() {
		Assert.assertFalse("Expecting that the validator will return false when the list name is the root folder name.",
				this.listNameValidator.validateListName(ListSelectorComponent.LISTS));
	}
	
	private void doReturnMatchingListFromMiddleware(String listName) {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(NEW_LIST_NAME);
		Mockito.when(this.germplasmListManager.getGermplasmListByName(listName, PROGRAM_UUID, 0, 1, Operation.EQUAL))
				.thenReturn(Collections.singletonList(germplasmList));
	}

	@Test
	public void testValidateListNameForNewListUsingUniqueName() throws MiddlewareQueryException {
		Assert.assertTrue("Expecting that the validator will return true when the list name is unique",
				this.listNameValidator.validateListName(NEW_LIST_NAME));
	}
	
	@Test
	public void testValidateListNameForNewListUsingNameThatExists() throws MiddlewareQueryException {
		String listName = NEW_LIST_NAME;

		// Return another list that has the same name
		this.doReturnMatchingListFromMiddleware(listName);

		boolean validateListName = this.listNameValidator.validateListName(listName);
		Assert.assertFalse("Expecting that the validator will return false because new list name is already taken.",
				validateListName);
		Mockito.verify(this.germplasmListManager, Mockito.times(1)).getGermplasmListByName(listName, PROGRAM_UUID, 0, 1, Operation.EQUAL);

	}
	

	@Test
	public void testValidateListNameForRenamingListsUsingNameThatExists() throws MiddlewareQueryException {
		// Setting a current list name means it is an existing list that will be renamed
		this.listNameValidator.setCurrentListName(OLD_LIST_NAME);
		String listName = NEW_LIST_NAME;

		// Return another list that has the same name
		this.doReturnMatchingListFromMiddleware(listName);

		boolean validateListName = this.listNameValidator.validateListName(listName);
		Assert.assertFalse("Expecting that the validator will return false because new list name is already taken.",
				validateListName);
		Mockito.verify(this.germplasmListManager, Mockito.times(1)).getGermplasmListByName(listName, PROGRAM_UUID, 0, 1, Operation.EQUAL);
	}


	
	@Test
	public void testValidateListNameForRenamingListsUsingUniqueName() throws MiddlewareQueryException {
		// Setting a current list name means it is an existing list that will be renamed
		this.listNameValidator.setCurrentListName(OLD_LIST_NAME);
		String listName = NEW_LIST_NAME;

		boolean isValidListName = this.listNameValidator.validateListName(listName);
		Assert.assertTrue("Expecting that the validator will return true because new list name is not yet existing.",
				isValidListName);
	}
	
	@Test
	public void testValidateListNameNewListNameSameAsCurrentName() throws MiddlewareQueryException {
		// Setting a current list name means it is an existing list that will be renamed
		this.listNameValidator.setCurrentListName(OLD_LIST_NAME);
		
		boolean isValidListName = this.listNameValidator.validateListName(OLD_LIST_NAME);
		Assert.assertTrue("Expecting that the validator will return true because new list name equals current list name.",
				isValidListName);
		// Check that Middleware call to retrieve matching lists by name is not called since no change to list name was made
		Mockito.verify(this.germplasmListManager, Mockito.times(0)).getGermplasmListByName(OLD_LIST_NAME, PROGRAM_UUID, 0, 1, Operation.EQUAL);
	}
	
	@Test
	public void testValidateListNameForExistingListsWithTrailingSpace() throws MiddlewareQueryException {
		String listName = NEW_LIST_NAME;
		this.listNameValidator.setCurrentListName(listName + "  ");

		// Return another list that has the same name
		this.doReturnMatchingListFromMiddleware(listName);

		Assert.assertTrue("Expecting that the validator will return true since only change to name is to remove trailing spaces.",
				this.listNameValidator.validateListName(listName));
		// Check that Middleware call to retrieve matching lists by name is not called since no change to list name was made
		Mockito.verify(this.germplasmListManager, Mockito.times(0)).getGermplasmListByName(listName, PROGRAM_UUID, 0, 1, Operation.EQUAL);
	}
	
	@Test
	public void testValidateListNameForRenamingListsAddTrailingSpace() throws MiddlewareQueryException {
		String listName = NEW_LIST_NAME;
		this.listNameValidator.setCurrentListName(listName);

		// Return another list that has the same name
		this.doReturnMatchingListFromMiddleware(listName);

		Assert.assertTrue("Expecting that the validator will return true since only change to name is to add trailing spaces.",
				this.listNameValidator.validateListName(listName + " "));
		// Check that Middleware call to retrieve matching lists by name is not called since no change to list name was made
		Mockito.verify(this.germplasmListManager, Mockito.times(0)).getGermplasmListByName(listName, PROGRAM_UUID, 0, 1, Operation.EQUAL);
	}

	@Test
	public void testValidateListNameForInvalidName() {
		final String rejectMessage = "Should reject invalid list name";

		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L?"));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L/"));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L\\"));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L:"));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L*"));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L|"));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L<"));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L>"));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L\""));
		Assert.assertFalse(rejectMessage, this.listNameValidator.validateListName("L."));

		Assert.assertTrue("Should accept valid list name", this.listNameValidator.validateListName("L"));

	}
}
