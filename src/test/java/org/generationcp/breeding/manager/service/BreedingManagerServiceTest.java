package org.generationcp.breeding.manager.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

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
    private static final Boolean INCLUDE_PARENT = true;
    private static final Boolean WITH_INVENTORY_ONLY = true;

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

        when(germplasmDataManager.searchForGermplasm(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, 
        		INCLUDE_PARENT, WITH_INVENTORY_ONLY)).thenReturn(expectedResult);

        // assume we have a search result
        List<Germplasm> result = breedingManagerService.doGermplasmSearch(SAMPLE_SEARCH_STRING, 
        		CONTAINS_MATCH, INCLUDE_PARENT, WITH_INVENTORY_ONLY);

        verify(germplasmDataManager).searchForGermplasm(SAMPLE_SEARCH_STRING, CONTAINS_MATCH, 
        		INCLUDE_PARENT, WITH_INVENTORY_ONLY);

        assertTrue("expects the result size is equal to the expectedResult size",result.size() == expectedResult.size());

    }

    @Test
    public void testDoGermplasmSearchEmptyString() throws Exception {

        try {
            breedingManagerService.doGermplasmSearch("", CONTAINS_MATCH, INCLUDE_PARENT, WITH_INVENTORY_ONLY);
            fail("expects an error since germplasm search string is empty");
        } catch (BreedingManagerSearchException e) {
            assertEquals("Should throw a BreedingManagerSearchException with SEARCH_QUERY_CANNOT_BE_EMPTY message",e.getErrorMessage(), Message.SEARCH_QUERY_CANNOT_BE_EMPTY);
            verifyZeroInteractions(germplasmDataManager); // germplasmListManager should not be called
        }
    }

    @Test
    public void testDoGermplasmListSearch() throws Exception {
        List<GermplasmList> expectedResult = mock(List.class);
        expectedResult.add(mock(GermplasmList.class));

        when(germplasmListManager.searchForGermplasmList(SAMPLE_SEARCH_STRING,CONTAINS_MATCH)).thenReturn(expectedResult);

        // assume we have a search result
        List<GermplasmList> result = breedingManagerService.doGermplasmListSearch(SAMPLE_SEARCH_STRING, CONTAINS_MATCH);

        verify(germplasmListManager).searchForGermplasmList(SAMPLE_SEARCH_STRING, CONTAINS_MATCH);

        assertTrue("expects the result size is equal to the expectedResult size",result.size() == expectedResult.size());
    }

    @Test
    public void testDoGermplasmListSearchEmptyString() throws Exception {

        try {
            breedingManagerService.doGermplasmListSearch("", CONTAINS_MATCH);
            fail("expects an error since germplasm search string is empty");
        } catch (BreedingManagerSearchException e) {
            assertEquals("Should throw a BreedingManagerSearchException with SEARCH_QUERY_CANNOT_BE_EMPTY message",e.getErrorMessage(), Message.SEARCH_QUERY_CANNOT_BE_EMPTY);
            verifyZeroInteractions(germplasmListManager); // germplasmListManager should not be called
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
