
package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customcomponent.GermplasmListTreeTable;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent.FolderSaveMode;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.data.Item;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import junit.framework.Assert;

/**
 * Created by EfficioDaniel on 9/29/2014.
 */
public class ListSelectorComponentTest {

	private static final String PROGRAM_UUID = "1234567";
	private SimpleResourceBundleMessageSource messageSource;
	private ListSelectorComponent listSelectorComponent;
	private GermplasmListManager germplasmListManager;
	private ContextUtil contextUtil;

	@Before
	public void setUp() {
		this.messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);
		this.listSelectorComponent = Mockito.mock(ListSelectorComponent.class);
		this.germplasmListManager = Mockito.mock(GermplasmListManager.class);
		this.contextUtil = Mockito.mock(ContextUtil.class);

		Mockito.when(this.messageSource.getMessage(Message.LISTS)).thenReturn("Lists");
		Mockito.doReturn(ListSelectorComponentTest.PROGRAM_UUID).when(this.listSelectorComponent).getCurrentProgramUUID();
		Mockito.doReturn(ListSelectorComponentTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
	}

	@Test
	public void testReturnTrueIfGermplasmListIdIsFolder() throws MiddlewareQueryException {
		Integer itemId = new Integer(5);
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

		GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		Assert.assertTrue("Expecting a true when the germplasm list that was retrieve using the item id is a folder ",
				listManagerTreeComponent.isFolder(itemId));
	}

	@Test
	public void testReturnFalseIfGermplasmListIdIsNotAFolder() throws MiddlewareQueryException {
		Integer itemId = new Integer(5);
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

		GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getType()).thenReturn("Not Folder");
		Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		Assert.assertFalse("Expecting a false when the germplasm list that was retrieve using the item id is not a folder ",
				listManagerTreeComponent.isFolder(itemId));
	}

	@Test
	public void testDoAddItemReturnTrueIfGermplasmListIsAFolderAndDoShowFoldersOnlyIsFalse() throws MiddlewareQueryException {
		Integer itemId = new Integer(5);
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

		GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		Assert.assertTrue(
				"Expecting a true when the germplasm list that was retrieve using the item id is a folder and the setting doShowFolderOnly is false",
				listManagerTreeComponent.doAddItem(germplasmList));
	}

	@Test
	public void testAddGermplasmListNodeUsingAParentGermplasmListId() throws MiddlewareQueryException {
		Integer parentGermplasmListId = new Integer(5);
		Integer childGermplasmListId = new Integer(20);
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setUtil(this.contextUtil);

		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		GermplasmList germplasmList = Mockito.mock(GermplasmList.class);

		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(germplasmList.getId()).thenReturn(childGermplasmListId);

		List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();
		germplasmListChildren.add(germplasmList);
		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
				ListSelectorComponentTest.PROGRAM_UUID, ListSelectorComponent.BATCH_SIZE)).thenReturn(germplasmListChildren);

		listManagerTreeComponent.instantiateGermplasmListSourceComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setUserDataManager(userDataManager);
		listManagerTreeComponent.addGermplasmListNode(parentGermplasmListId);

		Assert.assertNotNull("Returns same child germplasm list for the germplasm list that was added in the list source",
				listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId));

	}

	@Test
	public void testAddGermplasmListNodeWithEntries() throws MiddlewareQueryException {
		Integer parentGermplasmListId = new Integer(5);
		Integer childGermplasmListId = new Integer(50);
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setUtil(this.contextUtil);

		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		GermplasmList germplasmList = Mockito.mock(GermplasmList.class);

		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.LST);
		Mockito.when(germplasmList.getId()).thenReturn(childGermplasmListId);

		List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();
		germplasmListChildren.add(germplasmList);
		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
				ListSelectorComponentTest.PROGRAM_UUID, ListSelectorComponent.BATCH_SIZE)).thenReturn(germplasmListChildren);
		Long expectedNoOfEntries = 10L;
		Mockito.when(this.germplasmListManager.countGermplasmListDataByListId(childGermplasmListId)).thenReturn(expectedNoOfEntries);

		listManagerTreeComponent.instantiateGermplasmListSourceComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setUserDataManager(userDataManager);
		listManagerTreeComponent.addGermplasmListNode(parentGermplasmListId);
		Item item = listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId);
		Long actualNoOfEntries = Long.parseLong((String) item.getItemProperty(GermplasmListTreeTable.NUMBER_OF_ENTRIES_COL).getValue());
		Assert.assertEquals("The number of entries should be the same", expectedNoOfEntries, actualNoOfEntries);

	}

	@Test
	public void testRenameGermplasmListFolderIsSuccess() throws MiddlewareQueryException {
		String newFolderName = "New Folder Name";
		Integer germplasmListTreeId = new Integer(5);
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setUtil(this.contextUtil);

		TextField folderTextField = new TextField();
		folderTextField.setValue(newFolderName);
		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		GermplasmListTreeTable treeTable = Mockito.mock(GermplasmListTreeTable.class);

		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(this.germplasmListManager.getGermplasmListById(germplasmListTreeId)).thenReturn(germplasmList);

		listManagerTreeComponent.instantiateGermplasmListSourceComponent();
		listManagerTreeComponent.setGermplasmListSource(treeTable);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setMessageSource(this.messageSource);
		listManagerTreeComponent.setUserDataManager(userDataManager);

		listManagerTreeComponent.setFolderSaveMode(FolderSaveMode.RENAME);
		listManagerTreeComponent.setFolderTextField(folderTextField);

		listManagerTreeComponent.instantiateComponents();
		listManagerTreeComponent.getGermplasmListTreeUtil().setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.getGermplasmListTreeUtil().setMessageSource(this.messageSource);

		Object[] treeTableInfo = listManagerTreeComponent.generateCellInfo("Test", "Owner", "description", "listType", "25");
		listManagerTreeComponent.getGermplasmListSource().addItem(treeTableInfo, germplasmListTreeId);
		String newName =
				listManagerTreeComponent.getGermplasmListTreeUtil().renameFolderOrList(germplasmListTreeId,
						listManagerTreeComponent.getTreeActionsListener(), folderTextField, "Test");

		Assert.assertEquals("Returns correct folder name when the user is renaming a folder in the tree", " " + newFolderName, newName);
	}

	@Test
	public void testDeletionOfFolderNodeInTheTree() {
		Integer germplasmListId = new Integer(5);
		GermplasmList germplasmList = new GermplasmList();

		germplasmList.setId(germplasmListId);
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setMessageSource(this.messageSource);
		listManagerTreeComponent.setUtil(this.contextUtil);
		listManagerTreeComponent.instantiateComponents();
		listManagerTreeComponent.getGermplasmListTreeUtil().setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.getGermplasmListTreeUtil().setMessageSource(this.messageSource);

		Object[] info = listManagerTreeComponent.generateCellInfo("Test", "Owner", "description", "listType", "25");
		listManagerTreeComponent.getGermplasmListSource().addItem(info, germplasmListId);
		listManagerTreeComponent.removeListFromTree(germplasmList);
		Assert.assertNull("Should not return an object since the folder in the tree was deleted already", listManagerTreeComponent
				.getGermplasmListSource().getItem(germplasmListId));
	}

	@Test
	public void testMoveGermplasmListIfSourceIsAChildLocalNode() throws MiddlewareQueryException {
		// start: setup for the scenario
		GermplasmListTreeUtil treeUtil = new GermplasmListTreeUtil();

		Integer sourceItemId = new Integer(-11);
		Integer targetItemId = new Integer(-12);

		Mockito.when(this.listSelectorComponent.getWindow()).thenReturn(new Window());

		Mockito.when(this.germplasmListManager.getGermplasmListById(sourceItemId)).thenReturn(Mockito.mock(GermplasmList.class));
		Mockito.when(this.germplasmListManager.getGermplasmListById(targetItemId)).thenReturn(Mockito.mock(GermplasmList.class));

		treeUtil.setTargetListSource(Mockito.mock(GermplasmListSource.class));
		treeUtil.setGermplasmListManager(this.germplasmListManager);
		treeUtil.setSource(this.listSelectorComponent);
		treeUtil.setMessageSource(this.messageSource);
		treeUtil.setContextUtil(this.contextUtil);
		// end: setup for the scenario

		boolean result = treeUtil.setParent(sourceItemId, targetItemId);

		Assert.assertTrue("Should be able to move Child to any children local folder", result);
	}

	@Test
	public void testUpdateButtonsWhenTheSelectedItemisAListOrFolder() throws MiddlewareQueryException {
		Mockito.when(this.messageSource.getMessage(Message.ALL_LISTS)).thenReturn("All Lists");
		Mockito.when(this.germplasmListManager.getAllTopLevelListsBatched(PROGRAM_UUID, 10)).thenReturn(new ArrayList<GermplasmList>());

		ListManagerTreeComponent listManagerTreeComponent = Mockito.spy(new ListManagerTreeComponent());
		listManagerTreeComponent.setMessageSource(this.messageSource);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setUtil(this.contextUtil);
		Mockito.doReturn("All Lists").when(listManagerTreeComponent).getTreeHeading();
		listManagerTreeComponent.instantiateComponents();

		// Root Folder in Browse Lists
		listManagerTreeComponent.updateButtons("1");

		Assert.assertTrue("Add Item button must be enabled but didn't.", listManagerTreeComponent.getAddFolderBtn().isEnabled());
		Assert.assertTrue("Rename Item button must be enabled but didn't.", listManagerTreeComponent.getRenameFolderBtn().isEnabled());
		Assert.assertTrue("Delete Item button must be enabled but didn't.", listManagerTreeComponent.getDeleteFolderBtn().isEnabled());
	}

	@Test
	public void testUpdateButtonsWhenTheSelectedItemisARootFolder() throws MiddlewareQueryException {
		Mockito.when(this.messageSource.getMessage(Message.ALL_LISTS)).thenReturn("All Lists");
		Mockito.when(this.germplasmListManager.getAllTopLevelListsBatched(PROGRAM_UUID, 10)).thenReturn(new ArrayList<GermplasmList>());

		ListManagerTreeComponent listManagerTreeComponent = Mockito.spy(new ListManagerTreeComponent());
		listManagerTreeComponent.setMessageSource(this.messageSource);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setUtil(this.contextUtil);
		Mockito.doReturn("All Lists").when(listManagerTreeComponent).getTreeHeading();
		listManagerTreeComponent.instantiateComponents();

		// Root Folder in Browse Lists
		listManagerTreeComponent.updateButtons("Lists");

		Assert.assertTrue("Add Item button must be enabled but didn't.", listManagerTreeComponent.getAddFolderBtn().isEnabled());
		Assert.assertFalse("Rename Item button must be disabled but didn't.", listManagerTreeComponent.getRenameFolderBtn().isEnabled());
		Assert.assertFalse("Delete Item button must be disabled but didn't.", listManagerTreeComponent.getDeleteFolderBtn().isEnabled());
	}

	@Test
	public void testCreateGermplasmList_TestNoOfEntries() throws Exception {
		UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setUserDataManager(userDataManager);
		listManagerTreeComponent.setUtil(this.contextUtil);

		Integer childGermplasmListId = new Integer(50);
		Integer userId = 1;
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(AppConstants.DB.LST);
		germplasmList.setId(childGermplasmListId);
		germplasmList.setUserId(userId);
		List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();
		germplasmListChildren.add(germplasmList);

		Mockito.when(this.germplasmListManager.getAllTopLevelListsBatched(PROGRAM_UUID, ListSelectorComponent.BATCH_SIZE))
		.thenReturn(
				germplasmListChildren);
		Long expectedNoOfEntries = 10L;
		Mockito.when(this.germplasmListManager.countGermplasmListDataByListId(childGermplasmListId)).thenReturn(expectedNoOfEntries);
		Mockito.when(userDataManager.getUserById(userId)).thenReturn(null);
		listManagerTreeComponent.createGermplasmList();
		Item item = listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId);
		Long actualNoOfEntries = Long.parseLong((String) item.getItemProperty(GermplasmListTreeTable.NUMBER_OF_ENTRIES_COL).getValue());
		Assert.assertEquals("The number of entries should be the same", expectedNoOfEntries, actualNoOfEntries);
	}
}
