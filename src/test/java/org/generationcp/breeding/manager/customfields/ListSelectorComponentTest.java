
package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customcomponent.GermplasmListTreeTable;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent.FolderSaveMode;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.fields.SanitizedTextField;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.data.Item;

import junit.framework.Assert;

/**
 * Created by EfficioDaniel on 9/29/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListSelectorComponentTest {

	private static final String PROGRAM_UUID = "1234567";
	private static final Integer TEST_USER_ID = 1;

	@Mock
	private UserTreeStateService userTreeStateService;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private UserDataManager userDataManager;
	
	@InjectMocks
	private final ListSelectorComponent listSelectorComponent = new ListManagerTreeComponent();

	@Before
	public void setUp() {
		Mockito.when(this.messageSource.getMessage(Message.LISTS)).thenReturn("Lists");

		Mockito.doReturn(ListSelectorComponentTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
	}

	@Test
	public void testReturnTrueIfGermplasmListIdIsFolder() throws MiddlewareQueryException {
		final Integer itemId = 5;
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		Assert.assertTrue("Expecting a true when the germplasm list that was retrieve using the item id is a folder ",
				listManagerTreeComponent.isFolder(itemId));
	}

	@Test
	public void testReturnFalseIfGermplasmListIdIsNotAFolder() throws MiddlewareQueryException {
		final Integer itemId = 5;
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getType()).thenReturn("Not Folder");
		Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		Assert.assertFalse("Expecting a false when the germplasm list that was retrieve using the item id is not a folder ",
				listManagerTreeComponent.isFolder(itemId));
	}

	@Test
	public void testDoAddItemReturnTrueIfGermplasmListIsAFolderAndDoShowFoldersOnlyIsFalse() throws MiddlewareQueryException {
		final Integer itemId = 5;
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(this.germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		Assert.assertTrue(
				"Expecting a true when the germplasm list that was retrieve using the item id is a folder and the setting doShowFolderOnly is false",
				listManagerTreeComponent.doAddItem(germplasmList));
	}

	@Test
	public void testAddGermplasmListNodeUsingAParentGermplasmListId() throws MiddlewareQueryException {
		final Integer parentGermplasmListId = 5;
		final Integer childGermplasmListId = 20;
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setUtil(this.contextUtil);

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);

		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(germplasmList.getId()).thenReturn(childGermplasmListId);

		final List<GermplasmList> germplasmListChildren = new ArrayList<>();
		germplasmListChildren.add(germplasmList);
		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
				ListSelectorComponentTest.PROGRAM_UUID, ListSelectorComponent.BATCH_SIZE)).thenReturn(germplasmListChildren);

		listManagerTreeComponent.instantiateGermplasmListSourceComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setUserDataManager(this.userDataManager);
		listManagerTreeComponent.addGermplasmListNode(parentGermplasmListId);

		Assert.assertNotNull("Returns same child germplasm list for the germplasm list that was added in the list source",
				listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId));

	}

	@Test
	public void testAddGermplasmListNodeWithEntries() throws MiddlewareQueryException {
		final Integer parentGermplasmListId = 5;
		final Integer childGermplasmListId = 50;
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setUtil(this.contextUtil);

		final GermplasmList germplasmList = new GermplasmList();

		germplasmList.setType(AppConstants.DB.LST);
		germplasmList.setId(childGermplasmListId);

		final List<GermplasmList> germplasmListChildren = new ArrayList<>();
		germplasmListChildren.add(germplasmList);
		Mockito.when(this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
				ListSelectorComponentTest.PROGRAM_UUID, ListSelectorComponent.BATCH_SIZE)).thenReturn(germplasmListChildren);

		final Integer expectedNoOfEntries = 10;
		final Map<Integer, GermplasmListMetadata> allListMetaData = new HashMap<>();
		allListMetaData.put(childGermplasmListId, new GermplasmListMetadata(childGermplasmListId,
				expectedNoOfEntries, "Child List Owner Name"));

		Mockito.when(this.germplasmListManager.getGermplasmListMetadata(germplasmListChildren)).thenReturn(allListMetaData);

		listManagerTreeComponent.instantiateGermplasmListSourceComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setUserDataManager(this.userDataManager);
		listManagerTreeComponent.addGermplasmListNode(parentGermplasmListId);
		final Item item = listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId);
		final Integer actualNoOfEntries =
				Integer.parseInt((String) item.getItemProperty(GermplasmListTreeTable.NUMBER_OF_ENTRIES_COL).getValue());
		Assert.assertEquals("The number of entries should be the same", expectedNoOfEntries, actualNoOfEntries);

	}

	@Test
	public void testRenameGermplasmListFolderIsSuccess() throws MiddlewareQueryException {
		final String newFolderName = "New Folder Name";
		final Integer germplasmListTreeId = 5;
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setUtil(this.contextUtil);

		final SanitizedTextField folderTextField = new SanitizedTextField();
		folderTextField.setValue(newFolderName);
		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		final GermplasmListTreeTable treeTable = Mockito.mock(GermplasmListTreeTable.class);

		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(this.germplasmListManager.getGermplasmListById(germplasmListTreeId)).thenReturn(germplasmList);

		listManagerTreeComponent.instantiateGermplasmListSourceComponent();
		listManagerTreeComponent.setGermplasmListSource(treeTable);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setMessageSource(this.messageSource);

		listManagerTreeComponent.setFolderSaveMode(FolderSaveMode.RENAME);
		listManagerTreeComponent.setFolderTextField(folderTextField);

		listManagerTreeComponent.instantiateComponents();
		listManagerTreeComponent.getGermplasmListTreeUtil().setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.getGermplasmListTreeUtil().setMessageSource(this.messageSource);

		final Object[] treeTableInfo = listManagerTreeComponent.generateCellInfo("Test", "Owner", "description", "listType", "25");
		listManagerTreeComponent.getGermplasmListSource().addItem(treeTableInfo, germplasmListTreeId);
		final String newName = listManagerTreeComponent.getGermplasmListTreeUtil().renameFolderOrList(germplasmListTreeId,
				listManagerTreeComponent.getTreeActionsListener(), folderTextField, "Test");

		Assert.assertEquals("Returns correct folder name when the user is renaming a folder in the tree", " " + newFolderName, newName);
	}

	@Test
	public void testDeletionOfFolderNodeInTheTree() {
		final Integer germplasmListId = 5;
		final GermplasmList germplasmList = new GermplasmList();

		germplasmList.setId(germplasmListId);
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setMessageSource(this.messageSource);
		listManagerTreeComponent.setUtil(this.contextUtil);
		listManagerTreeComponent.instantiateComponents();
		listManagerTreeComponent.getGermplasmListTreeUtil().setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.getGermplasmListTreeUtil().setMessageSource(this.messageSource);

		final Object[] info = listManagerTreeComponent.generateCellInfo("Test", "Owner", "description", "listType", "25");
		listManagerTreeComponent.getGermplasmListSource().addItem(info, germplasmListId);
		listManagerTreeComponent.removeListFromTree(germplasmList);
		Assert.assertNull("Should not return an object since the folder in the tree was deleted already",
				listManagerTreeComponent.getGermplasmListSource().getItem(germplasmListId));
	}

	@Test
	public void testMoveGermplasmListIfSourceIsAChildLocalNode() throws MiddlewareQueryException {
		// start: setup for the scenario
		final GermplasmListTreeUtil treeUtil = new GermplasmListTreeUtil();

		final Integer sourceItemId = -11;
		final Integer targetItemId = -12;

		Mockito.when(this.germplasmListManager.getGermplasmListById(sourceItemId)).thenReturn(Mockito.mock(GermplasmList.class));
		Mockito.when(this.germplasmListManager.getGermplasmListById(targetItemId)).thenReturn(Mockito.mock(GermplasmList.class));

		final GermplasmListSource source = Mockito.mock(GermplasmListSource.class);

		treeUtil.setTargetListSource(source);
		this.listSelectorComponent.setGermplasmListSource(source);
		treeUtil.setGermplasmListManager(this.germplasmListManager);
		treeUtil.setSource(this.listSelectorComponent);
		treeUtil.setMessageSource(this.messageSource);
		treeUtil.setContextUtil(this.contextUtil);
		// end: setup for the scenario

		final boolean result = treeUtil.setParent(sourceItemId, targetItemId);

		Assert.assertTrue("Should be able to move Child to any children local folder", result);
	}

	@Test
	public void testUpdateButtonsWhenTheSelectedItemisAListOrFolder() throws MiddlewareQueryException {
		Mockito.when(this.messageSource.getMessage(Message.ALL_LISTS)).thenReturn("All Lists");
		Mockito.when(this.germplasmListManager.getAllTopLevelLists(ListSelectorComponentTest.PROGRAM_UUID))
				.thenReturn(new ArrayList<GermplasmList>());

		final ListManagerTreeComponent listManagerTreeComponent = Mockito.spy(new ListManagerTreeComponent());
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
		Mockito.when(this.germplasmListManager.getAllTopLevelLists(ListSelectorComponentTest.PROGRAM_UUID))
				.thenReturn(new ArrayList<GermplasmList>());

		final ListManagerTreeComponent listManagerTreeComponent = Mockito.spy(new ListManagerTreeComponent());
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
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setUtil(this.contextUtil);

		final Integer childGermplasmListId = 50;
		final Integer userId = 1;
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(AppConstants.DB.LST);
		germplasmList.setId(childGermplasmListId);
		germplasmList.setUserId(userId);
		final List<GermplasmList> germplasmListChildren = new ArrayList<>();
		germplasmListChildren.add(germplasmList);

		Mockito.when(this.germplasmListManager.getAllTopLevelLists(ListSelectorComponentTest.PROGRAM_UUID)).thenReturn(germplasmListChildren);
		final Integer expectedNoOfEntries = 10;
		final Map<Integer, GermplasmListMetadata> allListMetaData = new HashMap<>();
		allListMetaData.put(childGermplasmListId, new GermplasmListMetadata(childGermplasmListId,
				expectedNoOfEntries, "Child List Owner Name"));
		Mockito.when(this.germplasmListManager.getGermplasmListMetadata(germplasmListChildren)).thenReturn(allListMetaData);

		Mockito.when(userDataManager.getUserById(userId)).thenReturn(null);
		listManagerTreeComponent.createGermplasmList();
		final Item item = listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId);
		final Integer actualNoOfEntries =
				Integer.parseInt((String) item.getItemProperty(GermplasmListTreeTable.NUMBER_OF_ENTRIES_COL).getValue());
		Assert.assertEquals("The number of entries should be the same", expectedNoOfEntries, actualNoOfEntries);
	}

	@Test
	public void testTreeInitializationNotSaveListDialog() {
		final List<String> navigationState = Arrays.asList(ListSelectorComponent.LISTS, "1", "2");
		Mockito.when(
				userTreeStateService.getUserProgramTreeStateByUserIdProgramUuidAndType(TEST_USER_ID, PROGRAM_UUID,
						ListTreeState.GERMPLASM_LIST.name())).thenReturn(navigationState);
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(TEST_USER_ID);
		final GermplasmListSource source = Mockito.mock(GermplasmListSource.class);
		listSelectorComponent.setGermplasmListSource(source);

		listSelectorComponent.reinitializeTree(false);

		Mockito.verify(userTreeStateService).getUserProgramTreeStateByUserIdProgramUuidAndType(TEST_USER_ID, PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());
		Mockito.verify(source).expandItem(ListSelectorComponent.LISTS);
		Mockito.verify(source).expandItem(Integer.parseInt(navigationState.get(1)));
		Mockito.verify(source).expandItem(Integer.parseInt(navigationState.get(2)));
		Mockito.verify(source).clearSelection();
	}

	@Test
	public void testTreeInitializationSaveDialog() {
		final List<String> saveHierarchy = Arrays.asList(ListSelectorComponent.LISTS, "1", "2");
		Mockito.when(userTreeStateService.getUserProgramTreeStateForSaveList(TEST_USER_ID, PROGRAM_UUID)).thenReturn(saveHierarchy);
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(TEST_USER_ID);
		final GermplasmListSource source = Mockito.mock(GermplasmListSource.class);
		listSelectorComponent.setGermplasmListSource(source);

		listSelectorComponent.reinitializeTree(true);

		Mockito.verify(userTreeStateService).getUserProgramTreeStateForSaveList(TEST_USER_ID, PROGRAM_UUID);
		Mockito.verify(source).expandItem(ListSelectorComponent.LISTS);
		Mockito.verify(source).expandItem(Integer.parseInt(saveHierarchy.get(1)));
		Mockito.verify(source).expandItem(Integer.parseInt(saveHierarchy.get(2)));

        // we need to ensure that the last item in the saveHierarchy is selected
        Mockito.verify(source).select(saveHierarchy.get(2));
	}

}
