
package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SaveGermplasmListActionTest {

	private SaveGermplasmListAction action;

	@Mock
	private UserDataManager userDataManager;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.action = new SaveGermplasmListAction();
		this.action.setUserDataManager(this.userDataManager);
	}

	@Test
	public void testGetCropPersonId_WithNullCropUserId() throws MiddlewareQueryException {
		Integer cropUserId = 0;
		Mockito.when(this.userDataManager.getUserById(cropUserId)).thenReturn(null);

		Assert.assertEquals("Expecting to return a blank for null userid but didn't.", this.action.getCropPersonId(cropUserId).intValue(),
				0);
	}

	@Test
	public void testGetCropPersonId_WithValidCropUserId() throws MiddlewareQueryException {
		Integer cropUserId = 1;
		Integer personUserId = 2;

		User user = new User();
		user.setUserid(cropUserId);
		user.setPersonid(personUserId);

		Mockito.when(this.userDataManager.getUserById(cropUserId)).thenReturn(user);

		Assert.assertEquals("Expecting to return a person id from the userid but didn't.", this.action.getCropPersonId(cropUserId),
				personUserId);
	}
}
