
package org.generationcp.breeding.manager.service;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 9/26/2014 Time: 2:09 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class BreedingManagerServiceTest {

	private static final Integer DUMMY_USER_ID = 1;
	private static final Integer DUMMY_PERSON_ID = 1;
	private static final String DUMMY_PROGRAM_UUID = "a7433c01-4f46-4bc8-ae3a-678f0b62ac23";
	private static final String SAMPLE_SEARCH_STRING = "a sample search string";
	private static final String CONTAINS_SEARCH_STRING = "%a_s_%string";
	private static final Operation CONTAINS_MATCH = Operation.LIKE;
	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private UserDataManager userDataManager;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private BreedingManagerServiceImpl breedingManagerService;

	@Before
	public void setUp() {
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(DUMMY_PROGRAM_UUID);
	}

	@Test
	public void testGetOwnerListNamePositiveScenario() {

		final User sampleUser = Mockito.mock(User.class);
		final Person p = this.createDummyPerson();

		try {

			// the following is code used to set up the positive scenario
			Mockito.when(this.userDataManager.getUserById(BreedingManagerServiceTest.DUMMY_USER_ID)).thenReturn(sampleUser);
			Mockito.when(sampleUser.getPersonid()).thenReturn(BreedingManagerServiceTest.DUMMY_PERSON_ID);

			// we set up the test so that the dummy person object we created will be the one used by the service
			Mockito.when(this.userDataManager.getPersonById(BreedingManagerServiceTest.DUMMY_PERSON_ID)).thenReturn(p);

			// actual verification portion
			final String name = this.breedingManagerService.getOwnerListName(BreedingManagerServiceTest.DUMMY_USER_ID);

			Assert.assertEquals("Generated owner name is not correct", p.getFirstName() + " " + p.getMiddleName() + " " + p.getLastName(),
					name);

		} catch (final MiddlewareQueryException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetOwnerListNameNoPerson() {
		final User sampleUser = Mockito.mock(User.class);
		final String dummyUserName = "USER NAME";
		try {

			// the following is code used to set up the positive scenario
			Mockito.when(this.userDataManager.getUserById(BreedingManagerServiceTest.DUMMY_USER_ID)).thenReturn(sampleUser);
			Mockito.when(sampleUser.getPersonid()).thenReturn(BreedingManagerServiceTest.DUMMY_PERSON_ID);

			// we set up the test so that the dummy person object we created will be the one used by the service
			Mockito.when(this.userDataManager.getPersonById(BreedingManagerServiceTest.DUMMY_PERSON_ID)).thenReturn(null);
			Mockito.when(sampleUser.getName()).thenReturn(dummyUserName);

			// actual verification portion
			final String name = this.breedingManagerService.getOwnerListName(BreedingManagerServiceTest.DUMMY_USER_ID);

			Assert.assertEquals("Generated owner name is not correct", dummyUserName, name);

		} catch (final MiddlewareQueryException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetDefaultOwnerList() {
		final User sampleUser = Mockito.mock(User.class);
		final Person p = this.createDummyPerson();

		try {

			// the following is code used to set up the positive scenario
			this.setUpGetCurrentUserLocalId();
			Mockito.when(this.userDataManager.getUserById(BreedingManagerServiceTest.DUMMY_USER_ID)).thenReturn(sampleUser);
			Mockito.when(sampleUser.getPersonid()).thenReturn(BreedingManagerServiceTest.DUMMY_PERSON_ID);

			// we set up the test so that the dummy person object we created will be the one used by the service
			Mockito.when(this.userDataManager.getPersonById(BreedingManagerServiceTest.DUMMY_PERSON_ID)).thenReturn(p);

			// actual verification portion
			final String name = this.breedingManagerService.getOwnerListName(BreedingManagerServiceTest.DUMMY_USER_ID);

			Assert.assertEquals("Generated owner name is not correct", p.getFirstName() + " " + p.getMiddleName() + " " + p.getLastName(),
					name);

		} catch (final MiddlewareQueryException e) {
			Assert.fail(e.getMessage());
		}
	}

	protected void setUpGetCurrentUserLocalId() {
		final Long dummyProjectId = (long) 1;
		final Project project = Mockito.mock(Project.class);

		Mockito.when(this.workbenchDataManager.getLastOpenedProject(BreedingManagerServiceTest.DUMMY_USER_ID)).thenReturn(project);
		Mockito.when(project.getProjectId()).thenReturn(dummyProjectId);

		Mockito.when(this.workbenchDataManager.getLocalIbdbUserId(BreedingManagerServiceTest.DUMMY_USER_ID, dummyProjectId)).thenReturn(
				BreedingManagerServiceTest.DUMMY_USER_ID);
	}

	@Test
	public void testDoGermplasmListSearchStartsWith() throws Exception {
		final List<GermplasmList> expectedResult = new ArrayList<>();
		expectedResult.add(new GermplasmList());

		Mockito.when(
				this.germplasmListManager.searchForGermplasmList(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
						BreedingManagerServiceTest.DUMMY_PROGRAM_UUID, BreedingManagerServiceTest.CONTAINS_MATCH)).thenReturn(
				expectedResult);

		// assume we have a search result
		final List<GermplasmList> result =
				this.breedingManagerService.doGermplasmListSearch(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
						BreedingManagerServiceTest.CONTAINS_MATCH);

		Mockito.verify(this.germplasmListManager).searchForGermplasmList(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
				BreedingManagerServiceTest.DUMMY_PROGRAM_UUID, BreedingManagerServiceTest.CONTAINS_MATCH);

		Assert.assertTrue("expects the result size is equal to the expectedResult size", result.size() == expectedResult.size());
	}

	@Test(expected = BreedingManagerSearchException.class)
	public void testDoGermplasmListSearchContainsUnderADifferentProgram() throws Exception {
		final List<GermplasmList> expectedResult = new ArrayList<>();
		expectedResult.add(new GermplasmList());

		Mockito.when(
				this.germplasmListManager.searchForGermplasmList(BreedingManagerServiceTest.CONTAINS_SEARCH_STRING, "other program uuid",
						BreedingManagerServiceTest.CONTAINS_MATCH)).thenReturn(expectedResult);

		this.breedingManagerService.doGermplasmListSearch(BreedingManagerServiceTest.CONTAINS_SEARCH_STRING,
				BreedingManagerServiceTest.CONTAINS_MATCH);
	}

	@Test
	public void testDoGermplasmListSearchEqual() throws Exception {
		final List<GermplasmList> expectedResult = new ArrayList<>();
		expectedResult.add(new GermplasmList());

		Mockito.when(
				this.germplasmListManager.searchForGermplasmList(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
						BreedingManagerServiceTest.DUMMY_PROGRAM_UUID, Operation.LIKE)).thenReturn(expectedResult);

		// assume we have a search result
		final List<GermplasmList> result =
				this.breedingManagerService.doGermplasmListSearch(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING, Operation.LIKE);

		Mockito.verify(this.germplasmListManager).searchForGermplasmList(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
				BreedingManagerServiceTest.DUMMY_PROGRAM_UUID, Operation.LIKE);

		Assert.assertTrue("expects the result size is equal to the expectedResult size", result.size() == expectedResult.size());
	}

	@Test(expected = BreedingManagerSearchException.class)
	public void testDoGermplasmListSearchEqualUnderADifferentProgram() throws Exception {
		final List<GermplasmList> expectedResult = new ArrayList<>();
		expectedResult.add(new GermplasmList());

		Mockito.when(
				this.germplasmListManager.searchForGermplasmList(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING, "other program uuid",
						BreedingManagerServiceTest.CONTAINS_MATCH)).thenReturn(expectedResult);

		this.breedingManagerService.doGermplasmListSearch(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
				BreedingManagerServiceTest.CONTAINS_MATCH);
	}

	@Test
	public void testDoGermplasmListSearchContains() throws Exception {
		final List<GermplasmList> expectedResult = new ArrayList<>();
		expectedResult.add(new GermplasmList());

		Mockito.when(
				this.germplasmListManager.searchForGermplasmList(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
						BreedingManagerServiceTest.DUMMY_PROGRAM_UUID, BreedingManagerServiceTest.CONTAINS_MATCH)).thenReturn(
				expectedResult);

		// assume we have a search result
		final List<GermplasmList> result =
				this.breedingManagerService.doGermplasmListSearch(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
						BreedingManagerServiceTest.CONTAINS_MATCH);

		Mockito.verify(this.germplasmListManager).searchForGermplasmList(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
				BreedingManagerServiceTest.DUMMY_PROGRAM_UUID, BreedingManagerServiceTest.CONTAINS_MATCH);

		Assert.assertTrue("expects the result size is equal to the expectedResult size", result.size() == expectedResult.size());
	}

	@Test(expected = BreedingManagerSearchException.class)
	public void testDoGermplasmListSearchStartsWithUnderADifferentProgram() throws Exception {
		final List<GermplasmList> expectedResult = new ArrayList<>();
		expectedResult.add(new GermplasmList());

		Mockito.when(
				this.germplasmListManager.searchForGermplasmList(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING, "other program uuid",
						BreedingManagerServiceTest.CONTAINS_MATCH)).thenReturn(expectedResult);

		this.breedingManagerService.doGermplasmListSearch(BreedingManagerServiceTest.SAMPLE_SEARCH_STRING,
				BreedingManagerServiceTest.CONTAINS_MATCH);
	}

	@Test
	public void testDoGermplasmListSearchEmptyString() throws Exception {

		try {
			this.breedingManagerService.doGermplasmListSearch("", BreedingManagerServiceTest.CONTAINS_MATCH);
			Assert.fail("expects an error since germplasm search string is empty");
		} catch (final BreedingManagerSearchException e) {
			Assert.assertEquals("Should throw a BreedingManagerSearchException with SEARCH_QUERY_CANNOT_BE_EMPTY message",
					e.getErrorMessage(), Message.SEARCH_QUERY_CANNOT_BE_EMPTY);
			Mockito.verifyZeroInteractions(this.germplasmListManager); // germplasmListManager should not be called
		}
	}

	@Test
	public void testValidateEmptySearchString() throws Exception {
		try {
			this.breedingManagerService.validateEmptySearchString("");
			Assert.fail("expects a BreedingManagerSearchException to be thrown");
		} catch (final BreedingManagerSearchException e) {
			Assert.assertEquals("Should throw a BreedingManagerSearchException with SEARCH_QUERY_CANNOT_BE_EMPTY message",
					e.getErrorMessage(), Message.SEARCH_QUERY_CANNOT_BE_EMPTY);
		}
	}

	protected Person createDummyPerson() {
		final String firstName = "FIRST NAME";
		final String middleName = "MIDDLE NAME";
		final String lastName = "LAST NAME";

		return new Person(firstName, middleName, lastName);
	}

}
