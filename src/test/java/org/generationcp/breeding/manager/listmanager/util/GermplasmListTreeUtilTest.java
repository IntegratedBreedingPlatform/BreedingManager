
package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Validator.InvalidValueException;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListTreeUtilTest {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeUtilTest.class);

	private static final String ERROR_NO_SELECTION = "Please select a folder item";
	private static final String ERROR_ITEM_DOES_NOT_EXISTS = "Item does not exists";
	private static final String ERROR_UNABLE_TO_DELETE_LOCKED_LIST = "Unable to delete a locked list";
	private static final String ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER = "You cannot delete a list that you do not own";
	private static final String ERROR_HAS_CHILDREN = "Folder has child items";

	private GermplasmListManager germplasmListManager;
	private WorkbenchDataManager workbenchDataManager;

	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private final GermplasmListTreeUtil util = new GermplasmListTreeUtil();

	private static final Integer IBDB_USER_ID = (int) (Math.random() * 100);
	private static final Integer OTHER_IBDB_USER_ID = (int) (Math.random() * 100);
	private static final Integer GERMPLASM_LIST_ID = (int) (Math.random() * 100);
	private static final Integer GERMPLASM_LIST_ID_WITH_CHILDREN = (int) (Math.random() * 100);

	private static final String PROGRAM_UUID = "1234567";
	private GermplasmList germplasmList;

	@Before
	public void setUp() throws MiddlewareQueryException {
		this.setUpMessageSource();
		this.setUpIBDBUserId(GermplasmListTreeUtilTest.IBDB_USER_ID);
		Mockito.doReturn(GermplasmListTreeUtilTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
	}

	private void setUpMessageSource() {
		this.messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_NO_SELECTION)).thenReturn(GermplasmListTreeUtilTest.ERROR_NO_SELECTION);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_ITEM_DOES_NOT_EXISTS))
				.thenReturn(GermplasmListTreeUtilTest.ERROR_ITEM_DOES_NOT_EXISTS);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LOCKED_LIST))
				.thenReturn(GermplasmListTreeUtilTest.ERROR_UNABLE_TO_DELETE_LOCKED_LIST);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER))
				.thenReturn(GermplasmListTreeUtilTest.ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_HAS_CHILDREN)).thenReturn(GermplasmListTreeUtilTest.ERROR_HAS_CHILDREN);
		this.util.setMessageSource(this.messageSource);
	}

	private void setUpIBDBUserId(Integer userId) throws MiddlewareQueryException {
		this.workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);

		Project dummyProject = new Project();
		dummyProject.setProjectId(5L);

		Mockito.when(this.workbenchDataManager.getLastOpenedProject(userId)).thenReturn(dummyProject);
		Mockito.when(this.workbenchDataManager.getLocalIbdbUserId(userId, dummyProject.getProjectId())).thenReturn(userId);

		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(userId);
	}

	private void setUpGermplasmListDataManager(Integer itemId, boolean isExistingList) {
		this.setUpGermplasmListDataManager(itemId, isExistingList, false);
	}

	private void setUpGermplasmListDataManager(Integer itemId, boolean isExistingList, boolean isAFolder) {
		this.germplasmListManager = Mockito.mock(GermplasmListManager.class);
		this.util.setGermplasmListManager(this.germplasmListManager);
		this.germplasmList = this.getSampleGermplasmList(itemId);

		try {
			if (isExistingList) {
				Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(this.germplasmList);
			} else {
				Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(null);
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
					this.germplasmListManager.getGermplasmListByParentFolderId(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN,
							GermplasmListTreeUtilTest.PROGRAM_UUID))
					.thenReturn(dummyListOfGermplasmListWithEntries);
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
			Assert.fail(
					"Expecting no exception is returned for validating item to delete using existing list, but the system returns an exception.");
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIDDoesNotExist() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);

		try {
			this.util.validateItemToDelete(null);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expecting an exception is returned for validating item to delete using a list ID that does not exist.",
					GermplasmListTreeUtilTest.ERROR_NO_SELECTION, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListDoesNotExist() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, false);

		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expecting an exception is returned for validating item to delete using a list that does not exist.",
					GermplasmListTreeUtilTest.ERROR_ITEM_DOES_NOT_EXISTS, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIsLocked() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		this.germplasmList.setStatus(101);

		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expecting an exception is returned for validating item to delete using a list that is locked.",
					GermplasmListTreeUtilTest.ERROR_UNABLE_TO_DELETE_LOCKED_LIST, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIsNotOwnedByTheCurrentUser() throws MiddlewareQueryException {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);
		this.setUpIBDBUserId(GermplasmListTreeUtilTest.OTHER_IBDB_USER_ID);

		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals(
					"Expecting an exception is returned for validating item to delete using a list that is not owned by the current user.",
					GermplasmListTreeUtilTest.ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfItemHasContent() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, true, true);

		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expecting an exception is returned for validating item to delete which is a folder and has content.",
					GermplasmListTreeUtilTest.ERROR_HAS_CHILDREN, e.getMessage());
		}
	}

	private GermplasmList getSampleGermplasmList(Integer listId) {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(listId);
		germplasmList.setStatus(1);
		germplasmList.setUserId(GermplasmListTreeUtilTest.IBDB_USER_ID);
		germplasmList.setProgramUUID(GermplasmListTreeUtilTest.PROGRAM_UUID);
		return germplasmList;
	}

	@Test
	public void testIsSourceItemHasChildrenForItemWithChildren() throws MiddlewareQueryException {
		Integer sourceId = GermplasmListTreeUtilTest.GERMPLASM_LIST_ID;
		this.setUpGermplasmListDataManager(sourceId, true);

		GermplasmList parent = this.getSampleGermplasmList(sourceId);
		GermplasmList child1 = this.getSampleGermplasmList(2);
		child1.setParent(parent);

		List<GermplasmList> items = new ArrayList<GermplasmList>();
		items.add(child1);

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(sourceId, GermplasmListTreeUtilTest.PROGRAM_UUID))
				.thenReturn(items);

		Assert.assertTrue("Expecting true is returned when checking an item with children but didn't.",
				this.util.isSourceItemHasChildren(sourceId));
	}

	@Test
	public void testIsSourceItemHasChildrenForItemWithNoChildren() throws MiddlewareQueryException {
		Integer sourceId = GermplasmListTreeUtilTest.GERMPLASM_LIST_ID;
		this.setUpGermplasmListDataManager(sourceId, true);

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(sourceId, GermplasmListTreeUtilTest.PROGRAM_UUID))
				.thenReturn(new ArrayList<GermplasmList>());

		Assert.assertFalse("Expecting false is returned when checking an item with no children but didn't.",
				this.util.isSourceItemHasChildren(sourceId));
	}
}
