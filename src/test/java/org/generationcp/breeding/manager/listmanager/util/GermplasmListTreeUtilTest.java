
package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

public class GermplasmListTreeUtilTest {

	private static final String FOLDER = "FOLDER";

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeUtilTest.class);

	private static final String NEW_FOLDER_NAME = "NEW FOLDER " + (Math.random() * 100);
	private static final String ERROR_NO_SELECTION = "Please select a folder item";
	private static final String ERROR_ITEM_DOES_NOT_EXISTS = "Item does not exists";
	private static final String ERROR_UNABLE_TO_DELETE_LOCKED_LIST = "Unable to delete a locked list";

	private static final String ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER = "You cannot delete a list that you do not own";
	private static final String ERROR_HAS_CHILDREN = "Folder has child items";
	private static final Integer FOLDER_ID = (int) (Math.random() * 100);
	private static final Integer IBDB_USER_ID = (int) (Math.random() * 100);
	private static final Integer OTHER_IBDB_USER_ID = (int) (Math.random() * 100);
	private static final Integer GERMPLASM_LIST_ID = (int) (Math.random() * 100);
	private static final Integer GERMPLASM_LIST_ID_WITH_CHILDREN = (int) (Math.random() * 100);

	private static final String PROGRAM_UUID = "1234567";

	@Mock
	private GermplasmListManager germplasmListManager;
	
	@Mock
	private GermplasmDataManager germplasmDataManager;
	
	@Mock
	private WorkbenchDataManager workbenchDataManager;
	
	@Mock
	private UserDataManager userDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private ContextUtil contextUtil;
	
	@Mock
	private ListSelectorComponent source;
	
	@Mock
	private GermplasmListSource targetListSource;
	
	@Mock
	private Window window;
	
	@Mock
	private TextField folderTextfield;
	
	@Mock
	private ListTreeActionsListener treeListener;

	@InjectMocks
	private final GermplasmListTreeUtil util = new GermplasmListTreeUtil();

	private GermplasmList germplasmList;
	private GermplasmList testFolder;
	private Object[] cells = {NEW_FOLDER_NAME, FOLDER};

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.setUpMessageSource();
		this.setUpIBDBUserId(GermplasmListTreeUtilTest.IBDB_USER_ID);
		Mockito.doReturn(GermplasmListTreeUtilTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		Mockito.doReturn(this.window).when(this.source).getWindow();
		
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByFieldTableNameAndType(RowColumnType.LIST_TYPE.getFtable(),
				RowColumnType.LIST_TYPE.getFtype())).thenReturn(
				Arrays.asList(new UserDefinedField(1, "LISTNMS", "LISTTYPE", FOLDER, FOLDER, null, null, null, null, null, null)));
		Mockito.when(this.source.generateCellInfo(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString())).thenReturn(this.cells);
		Mockito.when(this.targetListSource.getItem(Matchers.anyInt())).thenReturn(Mockito.mock(Item.class));
		Mockito.when(this.targetListSource.getItem(Matchers.anyString())).thenReturn(Mockito.mock(Item.class));

		Mockito.when(this.folderTextfield.getValue()).thenReturn(NEW_FOLDER_NAME);
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
		this.util.setMessageSource(this.messageSource);
	}

	private void setUpIBDBUserId(Integer userId) {

		this.workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);

		Project dummyProject = new Project();
		dummyProject.setProjectId(5L);

		Mockito.when(this.workbenchDataManager.getLastOpenedProject(userId)).thenReturn(dummyProject);
		Mockito.when(this.workbenchDataManager.getLocalIbdbUserId(userId, dummyProject.getProjectId())).thenReturn(userId);

		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(userId);
	}

	private void setUpTestGermplasmList(Integer itemId, boolean isExistingList, boolean isAFolder) {
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
	
	private void setUpTestFolder(final Integer itemId) {
		this.testFolder = this.getSampleGermplasmList(itemId);
		this.testFolder.setType(FOLDER);

		try {
			Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(this.testFolder);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Failed to create a gerplasmList data.");
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
		this.setUpTestGermplasmList(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true, false);

		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.fail(
					"Expecting no exception is returned for validating item to delete using existing list, but the system returns an exception.");
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIDDoesNotExist() {
		this.setUpTestGermplasmList(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true, false);

		try {
			this.util.validateItemToDelete(null);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expecting an exception is returned for validating item to delete using a list ID that does not exist.",
					GermplasmListTreeUtilTest.ERROR_NO_SELECTION, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListDoesNotExist() {
		this.setUpTestGermplasmList(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, false, false);

		try {
			this.util.validateItemToDelete(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		} catch (InvalidValueException e) {
			Assert.assertEquals("Expecting an exception is returned for validating item to delete using a list that does not exist.",
					GermplasmListTreeUtilTest.ERROR_ITEM_DOES_NOT_EXISTS, e.getMessage());
		}
	}

	@Test
	public void testValidateItemToDeleteThrowsExceptionIfGermplasmListIsLocked() {
		this.setUpTestGermplasmList(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true, false);
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
		this.setUpTestGermplasmList(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, true, false);
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
		this.setUpTestGermplasmList(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, true, true);

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
	public void testIsSourceItemHasChildrenForItemWithChildren() {
		this.setupTreeItemWithChild();

		Assert.assertTrue("Expecting true is returned when checking an item with children but didn't.",
				this.util.isSourceItemHasChildren(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN));
	}

	@Test
	public void testIsSourceItemHasChildrenForItemWithNoChildren() {
		this.setupTreeItemWithNoChild();

		Assert.assertFalse("Expecting false is returned when checking an item with no children but didn't.",
				this.util.isSourceItemHasChildren(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID));
	}
	
	@Test
	public void testIsSourceItemHasChildrenWhenMiddlewareExceptionIsThrown() {
		Mockito.doThrow(new MiddlewareQueryException("Middleware error")).when(this.germplasmListManager)
				.getGermplasmListByParentFolderId(Matchers.anyInt(), Matchers.anyString());
		Assert.assertFalse(this.util.isSourceItemHasChildren(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID));
	}

	@Test
	public void testSetParentWhenItemToMoveIsRootProgramList() {
		final boolean isSourceMoved = this.util.setParent(ListSelectorComponent.PROGRAM_LISTS, GermplasmListTreeUtilTest.FOLDER_ID);
		Mockito.verify(this.germplasmListManager, Mockito.never()).getGermplasmListById(Matchers.anyInt());
		Mockito.verify(this.germplasmListManager, Mockito.never()).updateGermplasmList(Matchers.anyListOf(GermplasmList.class));
		Assert.assertFalse(isSourceMoved);
	}
	
	@Test
	public void testSetParentWhenItemToMoveIsRootCropList() {
		final boolean isSourceMoved = this.util.setParent(ListSelectorComponent.CROP_LISTS, GermplasmListTreeUtilTest.FOLDER_ID);
		Mockito.verify(this.germplasmListManager, Mockito.never()).getGermplasmListById(Matchers.anyInt());
		Mockito.verify(this.germplasmListManager, Mockito.never()).updateGermplasmList(Matchers.anyListOf(GermplasmList.class));
		Assert.assertFalse(isSourceMoved);
	}
	
	@Test
	public void testSetParentWhenItemToMoveHasChildren() {
		// Setup an item with child item - should not be allowed to update
		this.setupTreeItemWithChild();
		
		final boolean isSourceMoved = this.util.setParent(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, GermplasmListTreeUtilTest.FOLDER_ID);
		Mockito.verify(this.germplasmListManager, Mockito.never()).getGermplasmListById(Matchers.anyInt());
		Mockito.verify(this.germplasmListManager, Mockito.never()).updateGermplasmList(Matchers.anyListOf(GermplasmList.class));
		Assert.assertFalse(isSourceMoved);
	}

	@Test
	public void testSetParentWhenTargetItemIsNull() {
		this.setupTreeItemWithNoChild();
		final boolean isSourceMoved = this.util.setParent(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, null);
		Assert.assertTrue(isSourceMoved);
		
		// Verify Middleware Interactions
		Mockito.verify(this.germplasmListManager).getGermplasmListById(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		Assert.assertNull(this.germplasmList.getParent());
		Assert.assertEquals(GermplasmListTreeUtilTest.PROGRAM_UUID, this.germplasmList.getProgramUUID());
		Mockito.verify(this.germplasmListManager).updateGermplasmList(this.germplasmList);
		// Verify UI updates
		this.verifyUIChangesToTargetItem(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, null);
	}
	
	@Test
	public void testSetParentWhenTargetItemIsRootCropList() {
		this.setupTreeItemWithNoChild();
		final boolean isSourceMoved = this.util.setParent(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, ListSelectorComponent.CROP_LISTS);
		Assert.assertTrue(isSourceMoved);
		
		// Verify Middleware interactions
		Mockito.verify(this.germplasmListManager).getGermplasmListById(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		Assert.assertNull(this.germplasmList.getParent());
		Assert.assertNull(this.germplasmList.getProgramUUID());
		Assert.assertEquals(SaveListAsDialog.LIST_LOCKED_STATUS, this.germplasmList.getStatus());
		Mockito.verify(this.germplasmListManager).updateGermplasmList(this.germplasmList);
		// Verify UI updates
		this.verifyUIChangesToTargetItem(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, ListSelectorComponent.CROP_LISTS);
	}

	@Test
	public void testSetParentWhenTargetItemIsRootProgramList() {
		this.setupTreeItemWithNoChild();
		final boolean isSourceMoved = this.util.setParent(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, ListSelectorComponent.PROGRAM_LISTS);
		Assert.assertTrue(isSourceMoved);
		
		// Verify Middleware interactions
		Mockito.verify(this.germplasmListManager).getGermplasmListById(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		Assert.assertNull(this.germplasmList.getParent());
		Assert.assertEquals(GermplasmListTreeUtilTest.PROGRAM_UUID, this.germplasmList.getProgramUUID());
		Mockito.verify(this.germplasmListManager).updateGermplasmList(this.germplasmList);
		// Verify UI updates
		this.verifyUIChangesToTargetItem(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, ListSelectorComponent.PROGRAM_LISTS);
		// setChildrenAllowed(ListSelectorComponent.PROGRAM_LISTS
	}
	
	@Test
	public void testSetParentWhenTargetItemIsValidFolder() {
		this.setupTreeItemWithNoChild();
		this.setUpTestFolder(GermplasmListTreeUtilTest.FOLDER_ID);
		final boolean isSourceMoved = this.util.setParent(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, GermplasmListTreeUtilTest.FOLDER_ID);
		Assert.assertTrue(isSourceMoved);
		
		// Verify Middleware interactions
		Mockito.verify(this.germplasmListManager).getGermplasmListById(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListManager).getGermplasmListById(GermplasmListTreeUtilTest.FOLDER_ID);
		Assert.assertEquals(this.testFolder, this.germplasmList.getParent());
		Assert.assertEquals(GermplasmListTreeUtilTest.PROGRAM_UUID, this.germplasmList.getProgramUUID());
		Mockito.verify(this.germplasmListManager).updateGermplasmList(this.germplasmList);
		// Verify UI updates
		this.verifyUIChangesToTargetItem(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID,  GermplasmListTreeUtilTest.FOLDER_ID);
	}
	
	@Test
	public void testSetParentWhenTargetItemIsValidFolderButSourceListIsNull() {
		this.setupTreeItemWithNoChild();
		this.setUpTestFolder(GermplasmListTreeUtilTest.FOLDER_ID);
		Mockito.when(this.targetListSource.getItem(Matchers.anyInt())).thenReturn(null);
		
		final boolean isSourceMoved = this.util.setParent(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, GermplasmListTreeUtilTest.FOLDER_ID);
		Assert.assertTrue(isSourceMoved);
		
		// Verify Middleware interactions
		Mockito.verify(this.germplasmListManager).getGermplasmListById(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		Mockito.verify(this.germplasmListManager).getGermplasmListById(GermplasmListTreeUtilTest.FOLDER_ID);
		Assert.assertEquals(this.testFolder, this.germplasmList.getParent());
		Assert.assertEquals(GermplasmListTreeUtilTest.PROGRAM_UUID, this.germplasmList.getProgramUUID());
		Mockito.verify(this.germplasmListManager).updateGermplasmList(this.germplasmList);
		// Verify UI updates
		this.verifyUIChangesToTargetItem(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID,  null);
	}
	
	@Test
	public void testAddFolderToTreeWhenFolderIdIsNull() {
		this.util.addFolderToTree(FOLDER_ID, NEW_FOLDER_NAME, null, null, this.testFolder);
		Mockito.verifyZeroInteractions(this.germplasmDataManager);
		Mockito.verifyZeroInteractions(this.targetListSource);
		Mockito.verifyZeroInteractions(this.source);
	}
	
	@Test
	public void testAddFolderToTreeWhenTargetFolderIsRootProgramFolder() {
		this.setUpTestFolder(FOLDER_ID);
		Mockito.doReturn(true).when(this.source).isFolder(ListSelectorComponent.PROGRAM_LISTS);
		this.util.addFolderToTree(ListSelectorComponent.PROGRAM_LISTS, NEW_FOLDER_NAME, FOLDER_ID, this.testFolder, null);
		
		this.verifyNewFolderIsAddedToTree();
		
		Mockito.verify(this.targetListSource).setChildrenAllowed(ListSelectorComponent.PROGRAM_LISTS, true);
		Mockito.verify(this.targetListSource).setParent(FOLDER_ID, ListSelectorComponent.PROGRAM_LISTS);
		Mockito.verify(this.targetListSource).expandItem(ListSelectorComponent.PROGRAM_LISTS);
	
		Mockito.verify(this.targetListSource).select(FOLDER_ID);
		Mockito.verify(this.source).updateButtons(FOLDER_ID);
		Mockito.verify(this.source).showAddRenameFolderSection(false);
		Mockito.verify(this.source).refreshRemoteTree();
	}
	
	@Test
	public void testAddFolderToTreeWhenParentListIsNull() {
		this.setUpTestFolder(FOLDER_ID);
		this.util.addFolderToTree(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, NEW_FOLDER_NAME, FOLDER_ID, this.testFolder, null);
		
		this.verifyNewFolderIsAddedToTree();
		
		Mockito.verify(this.targetListSource).setChildrenAllowed(ListSelectorComponent.PROGRAM_LISTS, true);
		Mockito.verify(this.targetListSource).setParent(FOLDER_ID, ListSelectorComponent.PROGRAM_LISTS);
		Mockito.verify(this.targetListSource).expandItem(ListSelectorComponent.PROGRAM_LISTS);
	
		Mockito.verify(this.targetListSource).select(FOLDER_ID);
		Mockito.verify(this.source).updateButtons(FOLDER_ID);
		Mockito.verify(this.source).showAddRenameFolderSection(false);
		Mockito.verify(this.source).refreshRemoteTree();
	}
	
	@Test
	public void testAddFolderToTreeWhenParentListIsUnderRootProgramList() {
		this.setUpTestFolder(FOLDER_ID);
		this.setupTreeItemWithChild();
		Mockito.doReturn(false).when(this.source).isFolder(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN);
		this.util.addFolderToTree(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, NEW_FOLDER_NAME, FOLDER_ID, this.testFolder, this.germplasmList);
		
		this.verifyNewFolderIsAddedToTree();
		
		Mockito.verify(this.targetListSource).setChildrenAllowed(ListSelectorComponent.PROGRAM_LISTS, true);
		Mockito.verify(this.targetListSource).setParent(FOLDER_ID, ListSelectorComponent.PROGRAM_LISTS);
		Mockito.verify(this.targetListSource).expandItem(ListSelectorComponent.PROGRAM_LISTS);
	
		Mockito.verify(this.targetListSource).select(FOLDER_ID);
		Mockito.verify(this.source).updateButtons(FOLDER_ID);
		Mockito.verify(this.source).showAddRenameFolderSection(false);
		Mockito.verify(this.source).refreshRemoteTree();
	}
	
	@Test
	public void testAddFolderToTreeWhenParentListIsValidFolder() {
		this.setUpTestFolder(FOLDER_ID);
		this.setupTreeItemWithChild();
		this.testFolder.setParent(this.germplasmList);
		Mockito.doReturn(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN).when(this.targetListSource).getValue();
		Mockito.doReturn(true).when(this.source).isFolder(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN);
		this.util.addFolderToTree(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, NEW_FOLDER_NAME, FOLDER_ID, this.testFolder, this.germplasmList);
		
		this.verifyNewFolderIsAddedToTree();
		
		Mockito.verify(this.targetListSource).setChildrenAllowed(GERMPLASM_LIST_ID_WITH_CHILDREN, true);
		Mockito.verify(this.targetListSource).setParent(FOLDER_ID, GERMPLASM_LIST_ID_WITH_CHILDREN);
		Mockito.verify(this.targetListSource).expandItem(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN);
	
		Mockito.verify(this.targetListSource).select(FOLDER_ID);
		Mockito.verify(this.source).updateButtons(FOLDER_ID);
		Mockito.verify(this.source).showAddRenameFolderSection(false);
		Mockito.verify(this.source).refreshRemoteTree();
	}
	
	@Test
	public void testAddFolderToTreeWhenParentListIsUnderAnotherFolder() {
		this.setUpTestFolder(FOLDER_ID);
		this.setupTreeItemWithNoChild();
		this.testFolder.setParent(this.germplasmList);
		final int grandParentListId = 1000;
		this.germplasmList.setParent(this.getSampleGermplasmList(grandParentListId));
		Mockito.doReturn(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID).when(this.targetListSource).getValue();
		Mockito.doReturn(true).when(this.targetListSource).isExpanded(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		Mockito.doReturn(false).when(this.source).isFolder(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		this.util.addFolderToTree(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, NEW_FOLDER_NAME, FOLDER_ID, this.testFolder, this.germplasmList);
		
		this.verifyNewFolderIsAddedToTree();
		
		Mockito.verify(this.targetListSource).setChildrenAllowed(grandParentListId, true);
		Mockito.verify(this.targetListSource).setParent(FOLDER_ID, grandParentListId);
		Mockito.verify(this.targetListSource, Mockito.never()).expandItem(Matchers.any());
	
		Mockito.verify(this.targetListSource).select(FOLDER_ID);
		Mockito.verify(this.source).updateButtons(FOLDER_ID);
		Mockito.verify(this.source).showAddRenameFolderSection(false);
		Mockito.verify(this.source).refreshRemoteTree();
	}
	
	@Test
	public void testAddFolderWhenMiddlewareExceptionIsThrown() {
		Mockito.doThrow(new MiddlewareQueryException("Middleware Exception")).when(this.germplasmListManager).getGermplasmListById(Matchers.anyInt());
		this.util.addFolder(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, this.folderTextfield);
		Mockito.verify(this.germplasmListManager, Mockito.never()).addGermplasmList(Matchers.any(GermplasmList.class));
	}
	
	@Test
	public void testAddFolderWhenParentIsRootProgramList() {
		this.util.addFolder(ListSelectorComponent.PROGRAM_LISTS, this.folderTextfield);
		final ArgumentCaptor<GermplasmList> listCaptor = ArgumentCaptor.forClass(GermplasmList.class);
		Mockito.verify(this.germplasmListManager).addGermplasmList(listCaptor.capture());
		final GermplasmList list = listCaptor.getValue();
		Assert.assertEquals(NEW_FOLDER_NAME, list.getName());
		Assert.assertEquals(NEW_FOLDER_NAME, list.getDescription());
		Assert.assertEquals(GermplasmListTreeUtil.FOLDER_TYPE, list.getType());
		Assert.assertEquals(0, list.getStatus().intValue());
		Assert.assertEquals(PROGRAM_UUID, list.getProgramUUID());
		Assert.assertEquals(IBDB_USER_ID, list.getUserId());
		Assert.assertNull(list.getParent());
	}
	
	@Test
	public void testAddFolderWhenParentIsValidProgramFolder() {
		this.setupTreeItemWithChild();
		Mockito.doReturn(true).when(this.source).isFolder(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN);
		this.util.addFolder(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN, this.folderTextfield);
		
		final ArgumentCaptor<GermplasmList> listCaptor = ArgumentCaptor.forClass(GermplasmList.class);
		Mockito.verify(this.germplasmListManager).addGermplasmList(listCaptor.capture());
		final GermplasmList list = listCaptor.getValue();
		Assert.assertEquals(NEW_FOLDER_NAME, list.getName());
		Assert.assertEquals(NEW_FOLDER_NAME, list.getDescription());
		Assert.assertEquals(GermplasmListTreeUtil.FOLDER_TYPE, list.getType());
		Assert.assertEquals(0, list.getStatus().intValue());
		Assert.assertEquals(PROGRAM_UUID, list.getProgramUUID());
		Assert.assertEquals(IBDB_USER_ID, list.getUserId());
		Assert.assertEquals(this.germplasmList, list.getParent());
	}
	
	@Test
	public void testRenameFolderOrListWhenOldNameEqualsNewName() {
		Assert.assertEquals("", this.util.renameFolderOrList(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, this.treeListener, this.folderTextfield, NEW_FOLDER_NAME));
		Mockito.verify(this.germplasmListManager, Mockito.never()).getGermplasmListById(Matchers.anyInt());
		Mockito.verify(this.germplasmListManager, Mockito.never()).updateGermplasmList(Matchers.any(GermplasmList.class));
		Mockito.verify(this.source).showAddRenameFolderSection(false);
		Mockito.verifyNoMoreInteractions(this.source);
		Mockito.verifyZeroInteractions(this.targetListSource);
	}
	
	@Test
	public void testRenameFolderOrList() {
		final String oldName = "Old Name";
		this.setupTreeItemWithNoChild();
		this.germplasmList.setName(oldName);
		this.util.renameFolderOrList(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, this.treeListener, this.folderTextfield, oldName);
		
		Mockito.verify(this.germplasmListManager).getGermplasmListById(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		Assert.assertEquals(this.germplasmList.getName(), NEW_FOLDER_NAME);
		Mockito.verify(this.germplasmListManager).updateGermplasmList(this.germplasmList);
		// Verify UI updates
		Mockito.verify(this.targetListSource).setItemCaption(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID, NEW_FOLDER_NAME);
		Mockito.verify(this.targetListSource).select(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
		Mockito.verify(this.treeListener).updateUIForRenamedList(germplasmList, NEW_FOLDER_NAME);
		Mockito.verify(this.source).showAddRenameFolderSection(false);
		Mockito.verify(this.source).refreshRemoteTree();
		Mockito.verify(this.targetListSource).getItemCaption(GermplasmListTreeUtilTest.GERMPLASM_LIST_ID);
	}

	private void verifyNewFolderIsAddedToTree() {
		Mockito.verify(this.targetListSource).addItem(this.cells, FOLDER_ID);
		Mockito.verify(this.source).setNodeItemIcon(FOLDER_ID, true);
		Mockito.verify(this.targetListSource).setItemCaption(FOLDER_ID, NEW_FOLDER_NAME);
		Mockito.verify(this.targetListSource).setChildrenAllowed(FOLDER_ID, true);
		Mockito.verify(this.source).setSelectedListId(FOLDER_ID);
	}
	
	private void setupTreeItemWithChild() {
		Integer sourceId = GermplasmListTreeUtilTest.GERMPLASM_LIST_ID_WITH_CHILDREN;
		this.setUpTestGermplasmList(sourceId, true, false);

		GermplasmList parent = this.getSampleGermplasmList(sourceId);
		GermplasmList child1 = this.getSampleGermplasmList(2);
		child1.setParent(parent);
		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(sourceId, GermplasmListTreeUtilTest.PROGRAM_UUID))
				.thenReturn(Arrays.asList(child1));
	}
	
	private void setupTreeItemWithNoChild() {
		final Integer sourceId = GermplasmListTreeUtilTest.GERMPLASM_LIST_ID;
		this.setUpTestGermplasmList(sourceId, true, false);

		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderId(sourceId, GermplasmListTreeUtilTest.PROGRAM_UUID))
				.thenReturn(new ArrayList<GermplasmList>());
	}
	
	private void verifyUIChangesToTargetItem(final Object source, final Object target) {
		if (target != null) {
			Mockito.verify(this.targetListSource).setChildrenAllowed(target, true);
			Mockito.verify(this.targetListSource).setParent(source, target);
			Mockito.verify(this.targetListSource).expandItem(target);
		} else{
			Mockito.verify(this.targetListSource).setChildrenAllowed(source, true);
			Mockito.verify(this.targetListSource).setParent(source, ListSelectorComponent.PROGRAM_LISTS);
			Mockito.verify(this.targetListSource).expandItem(ListSelectorComponent.PROGRAM_LISTS);
		}
		Mockito.verify(this.source).setSelectedListId(source);
		Mockito.verify(this.targetListSource).select(source);
		Mockito.verify(this.targetListSource).setValue(source);
	}
	
	
}
