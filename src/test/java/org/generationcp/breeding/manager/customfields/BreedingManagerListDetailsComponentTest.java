
package org.generationcp.breeding.manager.customfields;

import java.text.ParseException;
import java.util.Date;

import junit.framework.Assert;

import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BreedingManagerListDetailsComponentTest {

	public static final String PROGRAM_UUID = "8238423847-7hdksjhd-47328947";
	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerListDetailsComponentTest.class);
	private static final Integer OTHER_USER = new Integer(2);
	private static final String OTHER_USER_NAME = "Other User Name";
	private static final Integer CURRENT_USER_ID = 1;
	private static final String CURRENT_USER_NAME = "Current User Name";

	@Mock
	private BreedingManagerService breedingManagerService;

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

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private UserService userService;

	@InjectMocks
	private BreedingManagerListDetailsComponent listDetailsComponent = new BreedingManagerListDetailsComponent();

	private GermplasmList germplasmList;

	@Before
	public void setUp() {

		when(this.contextUtil.getCurrentWorkbenchUserId()).thenReturn(CURRENT_USER_ID);

		Mockito.when(userService.getPersonName(CURRENT_USER_ID)).thenReturn(
				BreedingManagerListDetailsComponentTest.CURRENT_USER_NAME);
		when(userService
						.getPersonName(BreedingManagerListDetailsComponentTest.OTHER_USER)).thenReturn(
				BreedingManagerListDetailsComponentTest.OTHER_USER_NAME);
		when(contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);

	}

	@Test
	public void testGetListOwnerValue() {
		this.initGermplasmList();

		Assert.assertEquals("Expecting current user name will be returned but didn't.",
				listDetailsComponent.getListOwnerValue(null),
				BreedingManagerListDetailsComponentTest.CURRENT_USER_NAME);

		Assert.assertEquals("Expecting other user name will be returned but didn't.",
				listDetailsComponent.getListOwnerValue(this.germplasmList),
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
			returnedDate = listDetailsComponent.getParsedDate(dateToParse);
		} catch (ParseException e) {
			Assert.fail("Expecting a valid date parsed but didn't.");
		}
		Assert.assertNotNull("Expecting the returned date is not null.", returnedDate);

		// Invalid Date
		dateToParse = "A0/144313";
		boolean returnedParseException = false;
		try {
			returnedDate = listDetailsComponent.getParsedDate(dateToParse);
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
				listDetailsComponent.getParsableDateString(dateToParse));
	}

	@Test
	public void testResetListNameFieldForExistingList() {
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

	@Test
	public void testCreateGermplasmListFromListDetailsForProgramLists() {

		final String sampleName = "Sample Name";
		final String sampleDescription = "Sample Dxzescription";
		final String sampleType = "Germplasm List";
		final String sampleNotes = "Sample Notes";

		when(listNameField.getValue()).thenReturn(sampleName);
		when(listDescriptionField.getValue()).thenReturn(sampleDescription);
		when(listTypeField.getValue()).thenReturn(sampleType);
		when(listDateField.getValue()).thenReturn(null);
		when(listNotesField.getValue()).thenReturn(sampleNotes);

		GermplasmList germplasmList = listDetailsComponent.createGermplasmListFromListDetails(false);

		Assert.assertEquals(sampleName, germplasmList.getName());
		Assert.assertEquals(sampleDescription, germplasmList.getDescription());
		Assert.assertEquals(sampleType, germplasmList.getType());
		Assert.assertNull(germplasmList.getDate());
		Assert.assertEquals(sampleNotes, germplasmList.getNotes());
		Assert.assertEquals(PROGRAM_UUID, germplasmList.getProgramUUID());
		Assert.assertEquals(1, germplasmList.getStatus().intValue());
		Assert.assertEquals(0, germplasmList.getUserId().intValue());


	}

	@Test
	public void testCreateGermplasmListFromListDetailsForCropLists() {

		final String sampleName = "Sample Name";
		final String sampleDescription = "Sample Dxzescription";
		final String sampleType = "Germplasm List";
		final String sampleNotes = "Sample Notes";

		when(listNameField.getValue()).thenReturn(sampleName);
		when(listDescriptionField.getValue()).thenReturn(sampleDescription);
		when(listTypeField.getValue()).thenReturn(sampleType);
		when(listDateField.getValue()).thenReturn(null);
		when(listNotesField.getValue()).thenReturn(sampleNotes);

		GermplasmList germplasmList = listDetailsComponent.createGermplasmListFromListDetails(true);

		Assert.assertEquals(sampleName, germplasmList.getName());
		Assert.assertEquals(sampleDescription, germplasmList.getDescription());
		Assert.assertEquals(sampleType, germplasmList.getType());
		Assert.assertEquals(null, germplasmList.getDate());
		Assert.assertEquals(sampleNotes, germplasmList.getNotes());
		Assert.assertNull("If the list is saved in Crop lists folder, the programUUID must be set to null", germplasmList.getProgramUUID());
		Assert.assertEquals(1, germplasmList.getStatus().intValue());
		Assert.assertEquals(0, germplasmList.getUserId().intValue());

	}
}
