package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.util.UserUtil;
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
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Validator.InvalidValueException;

public class GermplasmListTreeUtilTest {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeUtilTest.class);

	private static final String ERROR_NO_SELECTION = "Please select a folder item";
	private static final String ERROR_ITEM_DOES_NOT_EXISTS = "Item does not exists";
	private static final String ERROR_UNABLE_TO_DELETE_LOCKED_LIST = "Unable to delete a locked list";
	private static final String ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER = "You cannot delete a list that you do not own";
	private static final String ERROR_HAS_CHILDREN = "Folder has child items";

	private GermplasmListTreeUtil util;
	private GermplasmListManager germplasmListManager;
	private WorkbenchDataManager workbenchDataManager;

	private SimpleResourceBundleMessageSource messageSource;

	private static final Integer IBDB_USER_ID = (int) (Math.random() * 100);
	private static final Integer OTHER_IBDB_USER_ID = (int) (Math.random() * 100);
	private static final Integer GERMPLASM_LIST_ID = (int) (Math.random() * 100);
	private static final Integer GERMPLASM_LIST_ID_WITH_CHILDREN = (int) (Math.random() * 100);
	private GermplasmList germplasmList;

	@Before
	public void setUp() {
		this.util = new GermplasmListTreeUtil();
		this.setUpMessageSource();		
		this.setUpIBDBUserId(GermplasmListTreeUtilTest.IBDB_USER_ID);
	}

	private void setUpMessageSource() {
		messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);
		Mockito.when(messageSource.getMessage(Message.ERROR_NO_SELECTION)).thenReturn(ERROR_NO_SELECTION);
		Mockito.when(messageSource.getMessage(Message.ERROR_ITEM_DOES_NOT_EXISTS)).thenReturn(ERROR_ITEM_DOES_NOT_EXISTS);
		Mockito.when(messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LOCKED_LIST)).thenReturn(ERROR_UNABLE_TO_DELETE_LOCKED_LIST);
		Mockito.when(messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER)).thenReturn(ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER);
		Mockito.when(messageSource.getMessage(Message.ERROR_HAS_CHILDREN)).thenReturn(ERROR_HAS_CHILDREN);
		this.util.setMessageSource(messageSource);
	}

	private void setUpIBDBUserId(Integer userId) {
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
			Mockito.when(UserUtil.getCurrentUserLocalId(this.workbenchDataManager)).thenReturn(userId);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Failed to create a ibdbuser data.");
		}
	}

	private void setUpGermplasmListDataManager(Integer itemId, boolean isExistingList) {
		this.setUpGermplasmListDataManager(itemId, isExistingList, false);
	}

	private void setUpGermplasmListDataManager(Integer itemId, boolean isExistingList,
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
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		
		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.fail("Expecting no exception is returned for validating item to delete using existing list, but the system returns an exception.");
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIDDoesNotExist() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);

		try {
			this.util.validateItemToDelete(null);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list ID that does not exist.",
					ERROR_NO_SELECTION, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListDoesNotExist() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, false);
		
		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list that does not exist.",
					ERROR_ITEM_DOES_NOT_EXISTS, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIsLocked() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		this.germplasmList.setStatus(101);

		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list that is locked.",
					ERROR_UNABLE_TO_DELETE_LOCKED_LIST, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIsNotOwnedByTheCurrentUser() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		this.setUpIBDBUserId(GermplasmListTreeUtilTest.OTHER_IBDB_USER_ID);

		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list that is not owned by the current user.",
					ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfItemHasContent() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, true, true);
		
		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete which is a folder and has content.",
					ERROR_HAS_CHILDREN, e.getMessage());
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
