
package org.generationcp.breeding.manager.customfields;

import java.text.ParseException;
import java.util.Date;

import junit.framework.Assert;

import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BreedingManagerListDetailsComponentTest {

	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerListDetailsComponentTest.class);

	private static BreedingManagerListDetailsComponent listDetailsComponent;
	private static BreedingManagerService breedingManagerService;

	private static final Integer OTHER_USER = new Integer(2);
	private static final String OTHER_USER_NAME = "Other User Name";
	private static final String CURRENT_USER_NAME = "Current User Name";

	private GermplasmList germplasmList;

	@BeforeClass
	public static void setUp() {

		listDetailsComponent = Mockito.spy(new BreedingManagerListDetailsComponent());

		BreedingManagerListDetailsComponentTest.breedingManagerService = Mockito.mock(BreedingManagerService.class);
		listDetailsComponent.setBreedingManagerService(BreedingManagerListDetailsComponentTest.breedingManagerService);

		try {
			Mockito.when(BreedingManagerListDetailsComponentTest.breedingManagerService.getDefaultOwnerListName()).thenReturn(
					BreedingManagerListDetailsComponentTest.CURRENT_USER_NAME);
			Mockito.when(
					BreedingManagerListDetailsComponentTest.breedingManagerService
							.getOwnerListName(BreedingManagerListDetailsComponentTest.OTHER_USER)).thenReturn(
					BreedingManagerListDetailsComponentTest.OTHER_USER_NAME);
		} catch (MiddlewareQueryException e) {
			Assert.fail();
		}
	}

	@Test
	public void testGetListOwnerValue() {
		this.initGermplasmList();

		Assert.assertEquals("Expecting current user name will be returned but didn't.",
				BreedingManagerListDetailsComponentTest.listDetailsComponent.getListOwnerValue(null),
				BreedingManagerListDetailsComponentTest.CURRENT_USER_NAME);

		Assert.assertEquals("Expecting other user name will be returned but didn't.",
				BreedingManagerListDetailsComponentTest.listDetailsComponent.getListOwnerValue(this.germplasmList),
				BreedingManagerListDetailsComponentTest.OTHER_USER_NAME);

	}

	private void initGermplasmList() {
		this.germplasmList = new GermplasmList();
		this.germplasmList.setUserId(BreedingManagerListDetailsComponentTest.OTHER_USER);
	}

	@Test
	public void testGetParsedDate() {
		String dateToParse;
		Date returnedDate = null;

		// Valid Date
		dateToParse = "20141212";
		try {
			returnedDate = BreedingManagerListDetailsComponentTest.listDetailsComponent.getParsedDate(dateToParse);
		} catch (ParseException e) {
			Assert.fail("Expecting a valid date parsed but didn't.");
		}
		Assert.assertNotNull("Expecting the returned date is not null.", returnedDate);

		// Invalid Date
		dateToParse = "A0/144313";
		boolean returnedParseException = false;
		try {
			returnedDate = BreedingManagerListDetailsComponentTest.listDetailsComponent.getParsedDate(dateToParse);
			Assert.fail("Expecting an exception must be returned but didn't.");
		} catch (ParseException e) {
			BreedingManagerListDetailsComponentTest.LOG.error(e.getMessage(), e);
			returnedParseException = true;
		}
		Assert.assertTrue("Expecting an exception returned but didn't.", returnedParseException);
	}

	@Test
	public void testGetParsableDateString() {
		String dateToParse = "11212"; // 0001-12-12
		String expectedDateToParse = "00011212";

		Assert.assertEquals("Expecting a return of 8-character date string but didn't.", expectedDateToParse,
				BreedingManagerListDetailsComponentTest.listDetailsComponent.getParsableDateString(dateToParse));
	}

	@Test
	public void testResetListNameFieldForExistingList() {
		ListNameField listNameField = Mockito.mock(ListNameField.class);
		Mockito.doReturn(listNameField).when(listDetailsComponent).getListNameField();
		ListNameValidator listNameValidator = Mockito.mock(ListNameValidator.class);
		Mockito.doReturn(listNameValidator).when(listNameField).getListNameValidator();

		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		String listName = "Sample List";
		germplasmList.setName(listName);

		this.listDetailsComponent.resetListNameFieldForExistingList(germplasmList);

		try {
			Mockito.verify(listNameValidator, Mockito.times(1)).setCurrentListName(germplasmList.getName());
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting that the currentListName in listNameValidator has been set but didn't.");
		}
	}

	@Test
	public void testResetListNameFieldForNewList() {
		ListNameField listNameField = Mockito.mock(ListNameField.class);
		Mockito.doReturn(listNameField).when(listDetailsComponent).getListNameField();
		ListNameValidator listNameValidator = Mockito.mock(ListNameValidator.class);
		Mockito.doReturn(listNameValidator).when(listNameField).getListNameValidator();

		GermplasmList germplasmList = new GermplasmList();
		String listName = "Sample List";
		germplasmList.setName(listName);

		this.listDetailsComponent.resetListNameFieldForExistingList(germplasmList);

		try {
			Mockito.verify(listNameValidator, Mockito.times(0)).setCurrentListName(germplasmList.getName());
		} catch (NeverWantedButInvoked e) {
			Assert.fail("Expecting that the currentListName in listNameValidator has not been set but didn't.");
		}
	}
}
