package org.generationcp.breeding.manager.service;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 9/26/2014
 * Time: 2:09 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class BreedingManagerServiceTest {

	private static final Integer DUMMY_USER_ID = 1;
	private static final Integer DUMMY_PERSON_ID = 1;

    private static final String SAMPLE_SEARCH_STRING = "a sample search string";
    private static final Operation CONTAINS_MATCH = Operation.LIKE;
    private static final Boolean EXCLUDE_PARENT = false;
    private static final Boolean SEARCH_PUBLIC_DATA = true;
    private static final Boolean NO_PUBLIC_DATA = false;

    @Mock
    private GermplasmDataManager germplasmDataManager;

    @Mock
    private GermplasmListManager germplasmListManager;

    @Mock
    private WorkbenchDataManager workbenchDataManager;

    @Mock
    private UserDataManager userDataManager;

    @InjectMocks
    private BreedingManagerServiceImpl breedingManagerService;

    @Before
	public void setUp() {
    }

	@Test
	public void testGetOwnerListNamePositiveScenario() {

		User sampleUser = mock(User.class);
		Person p = createDummyPerson();

		try {

			// the following is code used to set up the positive scenario
			when(userDataManager.getUserById(DUMMY_USER_ID)).thenReturn(sampleUser);
			when(sampleUser.getPersonid()).thenReturn(DUMMY_PERSON_ID);

			// we set up the test so that the dummy person object we created will be the one used by the service
			when(userDataManager.getPersonById(DUMMY_PERSON_ID)).thenReturn(p);


			// actual verification portion
			String name = breedingManagerService.getOwnerListName(DUMMY_USER_ID);

			assertEquals("Generated owner name is not correct", p.getFirstName() + " " + p.getMiddleName() + " " + p.getLastName(), name);


		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetOwnerListNameNoPerson() {
		User sampleUser = mock(User.class);
		final String dummyUserName = "USER NAME";
		try {

			// the following is code used to set up the positive scenario
			when(userDataManager.getUserById(DUMMY_USER_ID)).thenReturn(sampleUser);
			when(sampleUser.getPersonid()).thenReturn(DUMMY_PERSON_ID);

			// we set up the test so that the dummy person object we created will be the one used by the service
			when(userDataManager.getPersonById(DUMMY_PERSON_ID)).thenReturn(null);
			when(sampleUser.getName()).thenReturn(dummyUserName);

			// actual verification portion
			String name = breedingManagerService.getOwnerListName(DUMMY_USER_ID);

			assertEquals("Generated owner name is not correct",
					dummyUserName, name);

		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetDefaultOwnerList() {
		User sampleUser = mock(User.class);
		Person p = createDummyPerson();

		try {

			// the following is code used to set up the positive scenario
			setUpGetCurrentUserLocalId();
			when(userDataManager.getUserById(DUMMY_USER_ID)).thenReturn(sampleUser);
			when(sampleUser.getPersonid()).thenReturn(DUMMY_PERSON_ID);

			// we set up the test so that the dummy person object we created will be the one used by the service
			when(userDataManager.getPersonById(DUMMY_PERSON_ID)).thenReturn(p);


			// actual verification portion
			String name = breedingManagerService.getOwnerListName(DUMMY_USER_ID);

			assertEquals("Generated owner name is not correct",
					p.getFirstName() + " " + p.getMiddleName() + " " + p.getLastName(), name);

		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		}
	}

	protected void setUpGetCurrentUserLocalId() throws MiddlewareQueryException{
		final Long dummyProjectId = (long)1;
		Project project = mock(Project.class);
		WorkbenchRuntimeData runtimeData = mock(WorkbenchRuntimeData.class);

		when(workbenchDataManager.getWorkbenchRuntimeData()).thenReturn(runtimeData);
		when(runtimeData.getUserId()).thenReturn(DUMMY_USER_ID);

		when(workbenchDataManager.getLastOpenedProject(DUMMY_USER_ID)).thenReturn(project);
		when(project.getProjectId()).thenReturn(dummyProjectId);

		when(workbenchDataManager.getLocalIbdbUserId(DUMMY_USER_ID, dummyProjectId)).thenReturn(DUMMY_USER_ID);
	}

    @Test
    public void testDoGermplasmSearch() throws Exception {
        List<Germplasm> expectedResult = mock(List.class);
        expectedResult.add(mock(Germplasm.class));

        when(germplasmDataManager.searchForGermplasm(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, NO_PUBLIC_DATA)).thenReturn(expectedResult);

        // assume we have a search result
        List<Germplasm> result = breedingManagerService.doGermplasmSearch(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, NO_PUBLIC_DATA);

        verify(germplasmDataManager).searchForGermplasm(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, NO_PUBLIC_DATA);

        assertTrue("expects the result size is equal to the expectedResult size",result.size() == expectedResult.size());

    }

    @Test
    public void testDoGermplasmSearchEmptyString() throws Exception {

        try {
            breedingManagerService.doGermplasmSearch("", CONTAINS_MATCH, EXCLUDE_PARENT, NO_PUBLIC_DATA);
            fail("expects an error since germplasm search string is empty");
        } catch (BreedingManagerSearchException e) {
            assertEquals("Should throw a BreedingManagerSearchException with SEARCH_QUERY_CANNOT_BE_EMPTY message",e.getErrorMessage(), Message.SEARCH_QUERY_CANNOT_BE_EMPTY);
            verifyZeroInteractions(germplasmDataManager); // germplasmListManager should not be called
        }
    }

    @Test
    public void testDoGermplasmSearchEmptyResultsUncheckedSearchPublicData() throws Exception {
        when(germplasmDataManager.searchForGermplasm(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, NO_PUBLIC_DATA)).thenReturn(null);

        try {
            breedingManagerService.doGermplasmSearch(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, NO_PUBLIC_DATA);
            fail("expects a BreedingManagerSearchException to be thrown");
        } catch (BreedingManagerSearchException e) {
            assertEquals("Should throw a BreedingManagerSearchException with NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA message",e.getErrorMessage(), Message.NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA);
            verify(germplasmDataManager).searchForGermplasm(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, NO_PUBLIC_DATA);
        }

    }

    @Test
    public void testDoGermplasmSearchEmptyResultsIncludingPublicData() throws Exception {
        when(germplasmDataManager.searchForGermplasm(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, SEARCH_PUBLIC_DATA)).thenReturn(null);

        try {
            breedingManagerService.doGermplasmSearch(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, SEARCH_PUBLIC_DATA);
            fail("expects a BreedingManagerSearchException to be thrown");
        } catch (BreedingManagerSearchException e) {
            assertEquals("Should throw a BreedingManagerSearchException with NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA message",e.getErrorMessage(), Message.NO_SEARCH_RESULTS);
            verify(germplasmDataManager).searchForGermplasm(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, EXCLUDE_PARENT, SEARCH_PUBLIC_DATA);
        }
    }

    @Test
    public void testDoGermplasmListSearch() throws Exception {
        List<GermplasmList> expectedResult = mock(List.class);
        expectedResult.add(mock(GermplasmList.class));

        when(germplasmListManager.searchForGermplasmList(SAMPLE_SEARCH_STRING,CONTAINS_MATCH,NO_PUBLIC_DATA)).thenReturn(expectedResult);

        // assume we have a search result
        List<GermplasmList> result = breedingManagerService.doGermplasmListSearch(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, NO_PUBLIC_DATA);

        verify(germplasmListManager).searchForGermplasmList(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, NO_PUBLIC_DATA);

        assertTrue("expects the result size is equal to the expectedResult size",result.size() == expectedResult.size());

    }

    @Test
    public void testDoGermplasmListSearchEmptyString() throws Exception {

        try {
            breedingManagerService.doGermplasmListSearch("", CONTAINS_MATCH, NO_PUBLIC_DATA);
            fail("expects an error since germplasm search string is empty");
        } catch (BreedingManagerSearchException e) {
            assertEquals("Should throw a BreedingManagerSearchException with SEARCH_QUERY_CANNOT_BE_EMPTY message",e.getErrorMessage(), Message.SEARCH_QUERY_CANNOT_BE_EMPTY);
            verifyZeroInteractions(germplasmListManager); // germplasmListManager should not be called
        }
    }

    @Test
    public void testDoGermplasmListSearchEmptyResultsUncheckedSearchPublicData() throws Exception {
        when(germplasmListManager.searchForGermplasmList(SAMPLE_SEARCH_STRING,CONTAINS_MATCH,NO_PUBLIC_DATA)).thenReturn(null);

        try {
            breedingManagerService.doGermplasmListSearch(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, NO_PUBLIC_DATA);
            fail("expects a BreedingManagerSearchException to be thrown");
        } catch (BreedingManagerSearchException e) {
            assertEquals("Should throw a BreedingManagerSearchException with NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA message",e.getErrorMessage(), Message.NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA);
            verify(germplasmListManager).searchForGermplasmList(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, NO_PUBLIC_DATA);
        }

    }

    @Test
    public void testDoGermplasmListSearchEmptyResultsIncludingPublicData() throws Exception {
        when(germplasmListManager.searchForGermplasmList(SAMPLE_SEARCH_STRING,CONTAINS_MATCH,SEARCH_PUBLIC_DATA)).thenReturn(null);

        try {
            breedingManagerService.doGermplasmListSearch(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, SEARCH_PUBLIC_DATA);
            fail("expects a BreedingManagerSearchException to be thrown");
        } catch (BreedingManagerSearchException e) {
            assertEquals("Should throw a BreedingManagerSearchException with NO_SEARCH_RESULTS_UNCHECKED_PUBLIC_DATA message",e.getErrorMessage(), Message.NO_SEARCH_RESULTS);
            verify(germplasmListManager).searchForGermplasmList(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, SEARCH_PUBLIC_DATA);
        }
    }

    @Test
    public void testValidateEmptySearchString() throws Exception {
         try {
             breedingManagerService.validateEmptySearchString("");
             fail("expects a BreedingManagerSearchException to be thrown");
         } catch (BreedingManagerSearchException e) {
             assertEquals("Should throw a BreedingManagerSearchException with SEARCH_QUERY_CANNOT_BE_EMPTY message",e.getErrorMessage(), Message.SEARCH_QUERY_CANNOT_BE_EMPTY);
         }
    }

    protected Person createDummyPerson() {
        final String firstName = "FIRST NAME";
        final String middleName = "MIDDLE NAME";
        final String lastName = "LAST NAME";

        return new Person(firstName, middleName, lastName);
    }




}
