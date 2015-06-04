
package org.generationcp.breeding.manager.validator;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.breeding.manager.application.Message;
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

public class ListNameValidatorTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	private ListNameValidator listNameValidator;

	private final String DUMMY_ERROR_MESSAGE = "This is an error message.";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.messageSource.getMessage(Message.INVALID_ITEM_NAME)).thenReturn(this.DUMMY_ERROR_MESSAGE);
		Mockito.when(this.messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE)).thenReturn(this.DUMMY_ERROR_MESSAGE);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_VALIDATING_LIST)).thenReturn(this.DUMMY_ERROR_MESSAGE);

		this.listNameValidator = Mockito.spy(new ListNameValidator());
		this.listNameValidator.setMessageSource(this.messageSource);
		this.listNameValidator.setGermplasmListManager(this.germplasmListManager);
	}

	@Test
	public void testValidateListNameForEmptyListName() {
		Assert.assertFalse("Expecting that the validator will return false for empty list name.",
				this.listNameValidator.validateListName(""));
	}

	@Test
	public void testValidateListNameForRootFolderLists() {
		Assert.assertFalse("Expecting that the validator will return false when the list name is the root folder name.",
				this.listNameValidator.validateListName("Lists"));
	}

	@Test
	public void testValidateListNameForExistingLists() throws MiddlewareQueryException {

		String listName = "Sample List 1";
		List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Samplem List 1");
		germplasmLists.add(germplasmList);

		Mockito.when(this.germplasmListManager.getGermplasmListByName(listName, 0, 5, Operation.EQUAL)).thenReturn(germplasmLists);

		Assert.assertFalse("Expecting that the validator will return false when the list name is similar to an existing list.",
				this.listNameValidator.validateListName(listName));
	}
}
