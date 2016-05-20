
package org.generationcp.breeding.manager.germplasmlist.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasmlist.ListManagerTreeComponent;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Tree;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListTreeUtilTest {

	private static final String INVALID_ITEM_NAME = "Invalid Item Name";

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeUtilTest.class);

	private static final String ERROR_NO_SELECTION = "Please select a folder item";
	private static final String ERROR_ITEM_DOES_NOT_EXISTS = "Item does not exists";
	private static final String ERROR_UNABLE_TO_DELETE_LOCKED_LIST = "Unable to delete a locked list";
	private static final String ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER = "You cannot delete a list that you do not own";
	private static final String ERROR_HAS_CHILDREN = "Folder has child items";

	@Spy
	private Tree targetTree;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private ListManagerTreeComponent source;

	@Mock
	private ContextUtil contextUtil;

	private GermplasmListTreeUtil util;

	private static final Integer IBDB_USER_ID = (int) (Math.random() * 100);
	private static final Integer OTHER_IBDB_USER_ID = (int) (Math.random() * 100);
	private static final Integer GERMPLASM_LIST_ID = (int) (Math.random() * 100);
	private static final Integer GERMPLASM_LIST_ID_WITH_CHILDREN = (int) (Math.random() * 100);

	private static final String PROGRAM_UUID = "1234567";
	private GermplasmList germplasmList;

	@Before
	public void setUp() throws MiddlewareQueryException {
		MockitoAnnotations.initMocks(this);

		this.setUpMessageSource();
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(GermplasmListTreeUtilTest.IBDB_USER_ID);
		Mockito.doReturn(GermplasmListTreeUtilTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();

		this.util =
				new GermplasmListTreeUtil(this.source, this.targetTree, this.germplasmListManager, this.messageSource, this.contextUtil);
	}

	private void setUpMessageSource() {
		Mockito.when(this.messageSource.getMessage(Message.ERROR_NO_SELECTION)).thenReturn(GermplasmListTreeUtilTest.ERROR_NO_SELECTION);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_ITEM_DOES_NOT_EXISTS))
		.thenReturn(GermplasmListTreeUtilTest.ERROR_ITEM_DOES_NOT_EXISTS);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LOCKED_LIST))
		.thenReturn(GermplasmListTreeUtilTest.ERROR_UNABLE_TO_DELETE_LOCKED_LIST);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER))
		.thenReturn(GermplasmListTreeUtilTest.ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER);
		Mockito.when(this.messageSource.getMessage(Message.ERROR_HAS_CHILDREN)).thenReturn(GermplasmListTreeUtilTest.ERROR_HAS_CHILDREN);
	}

	private void setUpGermplasmListDataManager(Integer itemId, boolean isExistingList) {
		this.setUpGermplasmListDataManager(itemId, isExistingList, false);
	}

	private void setUpGermplasmListDataManager(Integer itemId, boolean isExistingList, boolean isAFolder) {
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
							GermplasmListTreeUtilTest.PROGRAM_UUID, 0, Integer.MAX_VALUE))
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
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIsNotOwnedByTheCurrentUser() {
		this.setUpGermplasmListDataManager(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true);

		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(GermplasmListTreeUtilTest.OTHER_IBDB_USER_ID);

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

	@Test
	public void testValidateItemToAddReturnsFalseForNullInput() throws MiddlewareQueryException {
		Mockito.when(this.messageSource.getMessage(Message.ERROR_NO_SELECTION)).thenReturn(GermplasmListTreeUtilTest.INVALID_ITEM_NAME);
		try {
			this.util.validateItemToAddRename(null);
		} catch (InvalidValueException e) {
			Assert.assertEquals(GermplasmListTreeUtilTest.INVALID_ITEM_NAME, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToAddReturnsFalseForEmptyStringInput() throws MiddlewareQueryException {
		Mockito.when(this.messageSource.getMessage(Message.INVALID_LIST_FOLDER_NAME))
		.thenReturn(GermplasmListTreeUtilTest.INVALID_ITEM_NAME);
		try {
			this.util.validateItemToAddRename("");
		} catch (InvalidValueException e) {
			Assert.assertEquals(GermplasmListTreeUtilTest.INVALID_ITEM_NAME, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToAddReturnsFalseForItemNameSimilarToRootFolderInput() throws MiddlewareQueryException {
		Mockito.when(this.messageSource.getMessage(Message.INVALID_LIST_FOLDER_NAME))
		.thenReturn(GermplasmListTreeUtilTest.INVALID_ITEM_NAME);
		Mockito.when(this.messageSource.getMessage(Message.LISTS_LABEL)).thenReturn("Lists");

		try {
			this.util.validateItemToAddRename("Lists");
		} catch (InvalidValueException e) {
			Assert.assertEquals(GermplasmListTreeUtilTest.INVALID_ITEM_NAME, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToAddReturnsFalseForExistingItemNameInput() throws MiddlewareQueryException {
		String itemName = "Existing Item Name";
		List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		germplasmLists.add(this.getSampleGermplasmList(1));
		Mockito.when(this.messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE))
		.thenReturn(GermplasmListTreeUtilTest.INVALID_ITEM_NAME);
		Mockito.when(this.messageSource.getMessage(Message.LISTS_LABEL)).thenReturn("Lists");
		Mockito.when(
				this.germplasmListManager.getGermplasmListByName(itemName, GermplasmListTreeUtilTest.PROGRAM_UUID, 0, 1, Operation.EQUAL))
				.thenReturn(germplasmLists);

		try {
			this.util.validateItemToAddRename(itemName);
		} catch (InvalidValueException e) {
			Assert.assertEquals(GermplasmListTreeUtilTest.INVALID_ITEM_NAME, e.getMessage());
		}
	}

	@Test
	public void testGetParentItemReturnsNullForNullInput() throws MiddlewareQueryException {
		Assert.assertNull("Expected to return null for an input null.", this.util.getParentItem(null));
	}

	@Test
	public void testGetParentItemReturnsNullForStringInput() throws MiddlewareQueryException {
		Assert.assertNull("Expected to return null for an input with string type.", this.util.getParentItem("Lists"));
	}

	@Test
	public void testGetParentItemReturnsNullForItemWithNoParent() throws MiddlewareQueryException {
		Integer parentItemId = 1;
		Assert.assertNull("Expected to return null for an item with no parent.", this.util.getParentItem(parentItemId));
	}

	@Test
	public void testGetParentItemReturnsTheParentOfTheCurrentGermplasmItem() throws MiddlewareQueryException {
		Integer parentItemId = 1;
		Integer currentItemId = 2;
		this.targetTree.addItem(1);
		this.targetTree.addItem(2);

		GermplasmList parentGermplasmList = this.getSampleGermplasmList(parentItemId);
		GermplasmList currentGermplasmList = this.getSampleGermplasmList(currentItemId);
		currentGermplasmList.setParent(parentGermplasmList);

		Mockito.when(this.germplasmListManager.getGermplasmListById(currentItemId)).thenReturn(currentGermplasmList);
		Mockito.when(this.germplasmListManager.getGermplasmListById(parentItemId)).thenReturn(parentGermplasmList);
		Mockito.when(this.source.isFolder(currentItemId)).thenReturn(false);

		Assert.assertEquals("Expecting to return the parent folder of the germplasm list.", parentGermplasmList,
				this.util.getParentItem(currentItemId));
	}

	@Test
	public void testGetParentItemReturnsCurrentList() throws MiddlewareQueryException {
		Integer currentItemId = 1;
		this.targetTree.addItem(1);

		GermplasmList currentGermplasmList = this.getSampleGermplasmList(currentItemId);

		Mockito.when(this.germplasmListManager.getGermplasmListById(currentItemId)).thenReturn(currentGermplasmList);
		Mockito.when(this.source.isFolder(currentItemId)).thenReturn(true);

		Assert.assertEquals("Expecting to return the current germplasm list when it is not a folder item.", currentGermplasmList,
				this.util.getParentItem(currentItemId));
	}

	private GermplasmList getSampleGermplasmList(Integer listId) {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(listId);
		germplasmList.setStatus(1);
		germplasmList.setUserId(GermplasmListTreeUtilTest.IBDB_USER_ID);
		germplasmList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
		return germplasmList;
	}
}
