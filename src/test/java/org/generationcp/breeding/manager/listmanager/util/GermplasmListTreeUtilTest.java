package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vaadin.data.Validator.InvalidValueException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:testApplicationContext.xml" })
public class GermplasmListTreeUtilTest {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeUtilTest.class);

	private GermplasmListTreeUtil util;
	private GermplasmListManager germplasmListManager;
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private static Integer IBDB_USER_ID = (int) (Math.random() * 100);
	private static Integer OTHER_IBDB_USER_ID = (int) (Math.random() * 100);
	private static Integer GERMPLASM_LIST_ID = (int) (Math.random() * 100);
	private static Integer GERMPLASM_LIST_ID_WITH_CHILDREN = (int) (Math.random() * 100);
	private GermplasmList germplasmList;

	@Before
	public void setUp() {
		this.util = new GermplasmListTreeUtil();
		this.util.setMessageSource(this.messageSource);
		this.mockIBDBUserId(GermplasmListTreeUtilTest.IBDB_USER_ID);
	}

	private void mockIBDBUserId(Integer userId) {
		WorkbenchRuntimeData runtimeDate = new WorkbenchRuntimeData();
		runtimeDate.setUserId(new Integer(5));

		this.workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);
		this.util.setWorkbenchDataManager(this.workbenchDataManager);

		Project dummyProject = new Project();
		dummyProject.setProjectId(new Long(5));

		try {
			Mockito.when(this.workbenchDataManager.getWorkbenchRuntimeData()).thenReturn(
					runtimeDate);
			Mockito.when(this.workbenchDataManager.getLastOpenedProject(runtimeDate.getUserId()))
					.thenReturn(dummyProject);
			Mockito.when(
					this.workbenchDataManager.getLocalIbdbUserId(runtimeDate.getUserId(),
							dummyProject.getProjectId())).thenReturn(userId);

		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Failed to create an ibdbuser instance.");
		}

		try {
			Mockito.when(Util.getCurrentUserLocalId(this.workbenchDataManager)).thenReturn(userId);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Failed to create a ibdbuser data.");
		}
	}

	private void mockGermplasmListDataManager(Integer itemId, boolean isExistingList) {
		this.mockGermplasmListDataManager(itemId, isExistingList, false);
	}

	private void mockGermplasmListDataManager(Integer itemId, boolean isExistingList,
			boolean isAFolder) {
		this.germplasmListManager = Mockito.mock(GermplasmListManager.class);
		this.util.setGermplasmListManager(this.germplasmListManager);
		this.germplasmList = this.getSampleGermplasmList(itemId);

		try {
			if (isExistingList) {
				Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(
						this.germplasmList);
			} else {
				Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(
						null);
			}

		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Failed to create a gerplasmList data.");
		}

		if (isAFolder) {
			this.mockFolderItem();
		}
	}

	private void mockFolderItem() {
		List<GermplasmList> dummyListOfGermplasmListWithEntries = new ArrayList<GermplasmList>();
		dummyListOfGermplasmListWithEntries.add(this.germplasmList);

		try {
			Mockito.when(
					this.germplasmListManager.getGermplasmListByParentFolderId(
							GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, 0,
							Integer.MAX_VALUE)).thenReturn(dummyListOfGermplasmListWithEntries);
		} catch (MiddlewareQueryException e) {
			Assert.fail("Expecting no exception is returned for determining if the item is a folder and has content.");
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsNothingIfGermplasmListExist() {
		this.mockGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.fail("Expecting no exception is returned for validating item to delete using existing list, but the system returns an exception.");
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIDDoesNotExist() {
		this.mockGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		String expectedException = "Please select a folder item";
		try {
			this.util.validateItemToDelete(null);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list ID that does not exist.",
					expectedException, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListDoesNotExist() {
		this.mockGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, false);
		String expectedException = "Item does not exists";
		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list that does not exist.",
					expectedException, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIsLocked() {
		this.mockGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		this.germplasmList.setStatus(101);

		String expectedException = "Unable to delete a locked list";
		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list that is locked.",
					expectedException, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIsNotOwnedByTheCurrentUser() {
		this.mockGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		this.mockIBDBUserId(GermplasmListTreeUtilTest.OTHER_IBDB_USER_ID);

		String expectedException = "You cannot delete a list that you do not own";
		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list that is not owned by the current user.",
					expectedException, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfItemHasContent() {
		this.mockGermplasmListDataManager(
				GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, true, true);

		String expectedException = "Folder has child items";
		try {
			this.util
					.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete which is a folder and has content.",
					expectedException, e.getMessage());
		}
	}

	private GermplasmList getSampleGermplasmList(Integer listId) {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(listId);
		germplasmList.setStatus(1);
		germplasmList.setUserId(GermplasmListTreeUtilTest.IBDB_USER_ID);
		return germplasmList;
	}

}
