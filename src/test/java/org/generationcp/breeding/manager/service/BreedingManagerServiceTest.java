package org.generationcp.breeding.manager.service;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 9/26/2014
 * Time: 2:09 PM
 */

@RunWith(JUnit4.class)
public class BreedingManagerServiceTest {
	private BreedingManagerServiceImpl dut;

	private WorkbenchDataManager workbenchDataManager;
	private UserDataManager userDataManager;


	private static final Integer DUMMY_USER_ID = 1;
	private static final Integer DUMMY_PERSON_ID = 1;

	@Before
	public void setUp() {

		// instantiate all classes for the test

		dut = new BreedingManagerServiceImpl();
		workbenchDataManager = mock(WorkbenchDataManager.class);
		userDataManager = mock(UserDataManager.class);

		dut.setWorkbenchDataManager(workbenchDataManager);
		dut.setUserDataManager(userDataManager);

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
			String name = dut.getOwnerListName(DUMMY_USER_ID);

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
			String name = dut.getOwnerListName(DUMMY_USER_ID);

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
			String name = dut.getOwnerListName(DUMMY_USER_ID);

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
	public void testGetCurrentUserLocalId() throws MiddlewareQueryException {
		setUpGetCurrentUserLocalId();
		Integer currentLocalId = dut.getCurrentUserLocalId();

		assertEquals("Returned the wrong value for current user local id", DUMMY_USER_ID, currentLocalId);
	}


	protected Person createDummyPerson() {
		final String firstName = "FIRST NAME";
		final String middleName = "MIDDLE NAME";
		final String lastName = "LAST NAME";

		return new Person(firstName, middleName, lastName);
	}
}
