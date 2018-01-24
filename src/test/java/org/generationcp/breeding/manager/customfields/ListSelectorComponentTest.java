package org.generationcp.breeding.manager.customfields;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.terminal.ThemeResource;
import junit.framework.Assert;
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
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListMetadata;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by EfficioDaniel on 9/29/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListSelectorComponentTest {

	private static final String PROGRAM_UUID = "1234567";
	private static final Integer TEST_USER_ID = 1;
	public static final String ALL_LISTS = "All Lists";

	@Mock
	private UserTreeStateService userTreeStateService;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private GermplasmListSource germplasmListSource;

	@Mock
	private UserDataManager userDataManager;

	@InjectMocks
	private final ListSelectorComponent listSelectorComponent = new ListManagerTreeComponent();

	@Before
	public void setUp() {
		Mockito.when(this.messageSource.getMessage(Message.LISTS)).thenReturn("Program lists");
		Mockito.doReturn(ListSelectorComponentTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();

		((ListManagerTreeComponent) listSelectorComponent).setFolderResource(Mockito.mock(ThemeResource.class));
		((ListManagerTreeComponent) listSelectorComponent).setLeafResource(Mockito.mock(ThemeResource.class));

		listSelectorComponent.setGermplasmListSource(germplasmListSource);
	}

	@Test
	public void testReturnTrueIfGermplasmListIdIsFolder() {
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
	public void testReturnFalseIfGermplasmListIdIsNotAFolder() {
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
	public void testDoAddItemReturnTrueIfGermplasmListIsAFolderAndDoShowFoldersOnlyIsFalse() {
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
	public void testAddGermplasmListNodeUsingAParentGermplasmListId() {
		final Integer parentGermplasmListId = 5;
		final Integer childGermplasmListId = 20;
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setUtil(this.contextUtil);

		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);

		Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
		Mockito.when(germplasmList.getId()).thenReturn(childGermplasmListId);

		final List<GermplasmList> germplasmListChildren = new ArrayList<>();
		germplasmListChildren.add(germplasmList);
		Mockito.when(this.germplasmListManager
				.getGermplasmListByParentFolderIdBatched(parentGermplasmListId, ListSelectorComponentTest.PROGRAM_UUID,
						ListSelectorComponent.BATCH_SIZE)).thenReturn(germplasmListChildren);

		listManagerTreeComponent.instantiateGermplasmListSourceComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setGermplasmDataManager(this.germplasmDataManager);
		listManagerTreeComponent.setUserDataManager(this.userDataManager);
		listManagerTreeComponent.addGermplasmListNode(parentGermplasmListId);

		Assert.assertNotNull("Returns same child germplasm list for the germplasm list that was added in the list source",
				listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId));

	}

	@Test
	public void testAddGermplasmListNodeWithEntries() {
		final Integer parentGermplasmListId = 5;
		final Integer childGermplasmListId = 50;
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setUtil(this.contextUtil);

		final GermplasmList germplasmList = new GermplasmList();

		germplasmList.setType(AppConstants.DB.LST);
		germplasmList.setId(childGermplasmListId);

		final List<GermplasmList> germplasmListChildren = new ArrayList<>();
		germplasmListChildren.add(germplasmList);
		Mockito.when(this.germplasmListManager
				.getGermplasmListByParentFolderIdBatched(parentGermplasmListId, ListSelectorComponentTest.PROGRAM_UUID,
						ListSelectorComponent.BATCH_SIZE)).thenReturn(germplasmListChildren);

		final Integer expectedNoOfEntries = 10;
		final Map<Integer, GermplasmListMetadata> allListMetaData = new HashMap<>();
		allListMetaData
				.put(childGermplasmListId, new GermplasmListMetadata(childGermplasmListId, expectedNoOfEntries, "Child List Owner Name"));

		Mockito.when(this.germplasmListManager.getGermplasmListMetadata(germplasmListChildren)).thenReturn(allListMetaData);

		listManagerTreeComponent.instantiateGermplasmListSourceComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setGermplasmDataManager(this.germplasmDataManager);
		listManagerTreeComponent.setUserDataManager(this.userDataManager);
		listManagerTreeComponent.addGermplasmListNode(parentGermplasmListId);
		final Item item = listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId);
		final Integer actualNoOfEntries =
				Integer.parseInt((String) item.getItemProperty(GermplasmListTreeTable.NUMBER_OF_ENTRIES_COL).getValue());
		Assert.assertEquals("The number of entries should be the same", expectedNoOfEntries, actualNoOfEntries);

	}

	@Test
	public void testRenameGermplasmListFolderIsSuccess() {
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
		listManagerTreeComponent.setGermplasmDataManager(this.germplasmDataManager);
		listManagerTreeComponent.setMessageSource(this.messageSource);

		listManagerTreeComponent.setFolderSaveMode(FolderSaveMode.RENAME);
		listManagerTreeComponent.setFolderTextField(folderTextField);

		listManagerTreeComponent.instantiateComponents();
		listManagerTreeComponent.getGermplasmListTreeUtil().setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.getGermplasmListTreeUtil().setMessageSource(this.messageSource);

		final Object[] treeTableInfo = listManagerTreeComponent.generateCellInfo("Test", "Owner", "description", "listType", "25");
		listManagerTreeComponent.getGermplasmListSource().addItem(treeTableInfo, germplasmListTreeId);
		final String newName = listManagerTreeComponent.getGermplasmListTreeUtil()
				.renameFolderOrList(germplasmListTreeId, listManagerTreeComponent.getTreeActionsListener(), folderTextField, "Test");

		Assert.assertEquals("Returns correct folder name when the user is renaming a folder in the tree", " " + newFolderName, newName);
	}

	@Test
	public void testDeletionOfFolderNodeInTheTree() {
		final Integer germplasmListId = 5;
		final GermplasmList germplasmList = new GermplasmList();

		germplasmList.setId(germplasmListId);
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setGermplasmDataManager(this.germplasmDataManager);
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
	public void testMoveGermplasmListIfSourceIsAChildLocalNode() {
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
	public void testUpdateButtonsWhenTheSelectedItemisAListOrFolder() {
		Mockito.when(this.messageSource.getMessage(Message.ALL_LISTS)).thenReturn(ALL_LISTS);
		Mockito.when(this.germplasmListManager.getAllTopLevelLists(ListSelectorComponentTest.PROGRAM_UUID))
				.thenReturn(new ArrayList<GermplasmList>());

		final ListManagerTreeComponent listManagerTreeComponent = Mockito.spy(new ListManagerTreeComponent());
		listManagerTreeComponent.setMessageSource(this.messageSource);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setGermplasmDataManager(this.germplasmDataManager);
		listManagerTreeComponent.setUtil(this.contextUtil);
		Mockito.doReturn(ALL_LISTS).when(listManagerTreeComponent).getTreeHeading();
		listManagerTreeComponent.instantiateComponents();

		// Root Folder in Browse Lists
		listManagerTreeComponent.updateButtons("1");

		Assert.assertTrue("Add Item button must be enabled but didn't.", listManagerTreeComponent.getAddFolderBtn().isEnabled());
		Assert.assertTrue("Rename Item button must be enabled but didn't.", listManagerTreeComponent.getRenameFolderBtn().isEnabled());
		Assert.assertTrue("Delete Item button must be enabled but didn't.", listManagerTreeComponent.getDeleteFolderBtn().isEnabled());
	}

	@Test
	public void testUpdateButtonsWhenTheSelectedItemisARootProgramListsFolder() {
		Mockito.when(this.messageSource.getMessage(Message.ALL_LISTS)).thenReturn(ALL_LISTS);
		Mockito.when(this.germplasmListManager.getAllTopLevelLists(ListSelectorComponentTest.PROGRAM_UUID))
				.thenReturn(new ArrayList<GermplasmList>());

		final ListManagerTreeComponent listManagerTreeComponent = Mockito.spy(new ListManagerTreeComponent());
		listManagerTreeComponent.setMessageSource(this.messageSource);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setGermplasmDataManager(this.germplasmDataManager);
		listManagerTreeComponent.setUtil(this.contextUtil);
		Mockito.doReturn(ALL_LISTS).when(listManagerTreeComponent).getTreeHeading();
		listManagerTreeComponent.instantiateComponents();

		// Root Folder in Browse Lists
		listManagerTreeComponent.updateButtons(ListSelectorComponent.PROGRAM_LISTS);

		Assert.assertTrue("Add Item button must be enabled but didn't.", listManagerTreeComponent.getAddFolderBtn().isEnabled());
		Assert.assertFalse("Rename Item button must be disabled but didn't.", listManagerTreeComponent.getRenameFolderBtn().isEnabled());
		Assert.assertFalse("Delete Item button must be disabled but didn't.", listManagerTreeComponent.getDeleteFolderBtn().isEnabled());
	}

	@Test
	public void testUpdateButtonsWhenTheSelectedItemisARootCropListsFolder() {
		Mockito.when(this.messageSource.getMessage(Message.ALL_LISTS)).thenReturn(ALL_LISTS);
		Mockito.when(this.germplasmListManager.getAllTopLevelLists(ListSelectorComponentTest.PROGRAM_UUID))
				.thenReturn(new ArrayList<GermplasmList>());

		final ListManagerTreeComponent listManagerTreeComponent = Mockito.spy(new ListManagerTreeComponent());
		listManagerTreeComponent.setMessageSource(this.messageSource);
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setGermplasmDataManager(this.germplasmDataManager);
		listManagerTreeComponent.setUtil(this.contextUtil);
		Mockito.doReturn(ALL_LISTS).when(listManagerTreeComponent).getTreeHeading();
		listManagerTreeComponent.instantiateComponents();

		// Root Folder in Browse Lists
		listManagerTreeComponent.updateButtons(ListSelectorComponent.CROP_LISTS);

		Assert.assertFalse("Add Item button must be disabled but didn't.", listManagerTreeComponent.getAddFolderBtn().isEnabled());
		Assert.assertFalse("Rename Item button must be disabled but didn't.", listManagerTreeComponent.getRenameFolderBtn().isEnabled());
		Assert.assertFalse("Delete Item button must be disabled but didn't.", listManagerTreeComponent.getDeleteFolderBtn().isEnabled());
	}

	@Test
	public void testCreateGermplasmListTestNoOfEntries() throws Exception {
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);
		final ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		listManagerTreeComponent.setGermplasmListManager(this.germplasmListManager);
		listManagerTreeComponent.setGermplasmDataManager(this.germplasmDataManager);
		listManagerTreeComponent.setUtil(this.contextUtil);

		final Integer childGermplasmListId = 50;
		final Integer userId = 1;
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(AppConstants.DB.LST);
		germplasmList.setId(childGermplasmListId);
		germplasmList.setUserId(userId);
		final List<GermplasmList> germplasmListChildren = new ArrayList<>();
		germplasmListChildren.add(germplasmList);

		Mockito.when(this.germplasmListManager.getAllTopLevelLists(ListSelectorComponentTest.PROGRAM_UUID))
				.thenReturn(germplasmListChildren);
		final Integer expectedNoOfEntries = 10;
		final Map<Integer, GermplasmListMetadata> allListMetaData = new HashMap<>();
		allListMetaData
				.put(childGermplasmListId, new GermplasmListMetadata(childGermplasmListId, expectedNoOfEntries, "Child List Owner Name"));
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
		final List<String> navigationState = Arrays.asList(ListSelectorComponent.PROGRAM_LISTS, "1", "2");
		Mockito.when(userTreeStateService
				.getUserProgramTreeStateByUserIdProgramUuidAndType(TEST_USER_ID, PROGRAM_UUID, ListTreeState.GERMPLASM_LIST.name()))
				.thenReturn(navigationState);
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(TEST_USER_ID);
		final GermplasmListSource source = Mockito.mock(GermplasmListSource.class);
		listSelectorComponent.setGermplasmListSource(source);

		listSelectorComponent.reinitializeTree(false);

		Mockito.verify(userTreeStateService)
				.getUserProgramTreeStateByUserIdProgramUuidAndType(TEST_USER_ID, PROGRAM_UUID, ListTreeState.GERMPLASM_LIST.name());
		Mockito.verify(source).expandItem(ListSelectorComponent.PROGRAM_LISTS);
		Mockito.verify(source).expandItem(Integer.parseInt(navigationState.get(1)));
		Mockito.verify(source).expandItem(Integer.parseInt(navigationState.get(2)));
		Mockito.verify(source).clearSelection();
	}

	@Test
	public void testTreeInitializationSaveDialog() {
		final List<String> saveHierarchy = Arrays.asList(ListSelectorComponent.PROGRAM_LISTS, "1", "2");
		Mockito.when(userTreeStateService.getUserProgramTreeStateForSaveList(TEST_USER_ID, PROGRAM_UUID)).thenReturn(saveHierarchy);
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(TEST_USER_ID);
		final GermplasmListSource source = Mockito.mock(GermplasmListSource.class);
		listSelectorComponent.setGermplasmListSource(source);

		listSelectorComponent.reinitializeTree(true);

		Mockito.verify(userTreeStateService).getUserProgramTreeStateForSaveList(TEST_USER_ID, PROGRAM_UUID);
		Mockito.verify(source).expandItem(ListSelectorComponent.PROGRAM_LISTS);
		Mockito.verify(source).expandItem(Integer.parseInt(saveHierarchy.get(1)));
		Mockito.verify(source).expandItem(Integer.parseInt(saveHierarchy.get(2)));

		// we need to ensure that the last item in the saveHierarchy is selected
		Mockito.verify(source).select(saveHierarchy.get(2));
	}

	@Test
	public void testAddGermplasmListsToTheTreeList() {

		final GermplasmList germplasmListForCrop = createGermplasmList(123, "Test Crop List", "Test Crop List Description");
		final List<GermplasmList> cropLists = new ArrayList<>();
		cropLists.add(germplasmListForCrop);

		final GermplasmList germplasmListForProgram = createGermplasmList(456, "Test Program List", "Test Program List Description");
		final List<GermplasmList> programLists = new ArrayList<>();
		programLists.add(germplasmListForProgram);

		Mockito.when(germplasmListManager.getAllTopLevelLists(null)).thenReturn(cropLists);
		Mockito.when(germplasmListManager.getAllTopLevelLists(PROGRAM_UUID)).thenReturn(programLists);
		Mockito.when(germplasmListManager.getGermplasmListMetadata(cropLists))
				.thenReturn(createGermplasmListMetaData(germplasmListForCrop, 100));
		Mockito.when(germplasmListManager.getGermplasmListMetadata(programLists))
				.thenReturn(createGermplasmListMetaData(germplasmListForProgram, 200));

		listSelectorComponent.addGermplasmListsToTheTreeList();

		// Verify root folders are created
		Mockito.verify(germplasmListSource)
				.addItem(listSelectorComponent.generateCellInfo(ListSelectorComponent.CROP_LISTS, "", "", "", ""),
						ListSelectorComponent.CROP_LISTS);
		Mockito.verify(germplasmListSource)
				.addItem(listSelectorComponent.generateCellInfo(ListSelectorComponent.PROGRAM_LISTS, "", "", "", ""),
						ListSelectorComponent.PROGRAM_LISTS);

		// Verify germplasm lists are added to their respective folder
		Mockito.verify(germplasmListSource).addItem(
				listSelectorComponent.generateCellInfo("Test Crop List", "", "Test Crop List Description", "Germplasm List", "100"), 123);
		Mockito.verify(germplasmListSource).setParent(Mockito.eq(123), Mockito.eq(ListSelectorComponent.CROP_LISTS));

		Mockito.verify(germplasmListSource).addItem(
				listSelectorComponent.generateCellInfo("Test Program List", "", "Test Program List Descripti...", "Germplasm List", "200"),
				456);
		Mockito.verify(germplasmListSource).setParent(Mockito.eq(456), Mockito.eq(ListSelectorComponent.PROGRAM_LISTS));

	}

	@Test
	public void testAddProgramLevelLists() {

		final Integer germplasmListId = 123;
		final String name = "Test Name";
		final String description = "Test Description";
		final Integer numberOfEntries = 999;
		final GermplasmList germplasmList = createGermplasmList(germplasmListId, name, description);
		final List<GermplasmList> germplasmLists = new ArrayList<>();
		germplasmLists.add(germplasmList);

		Mockito.when(germplasmListManager.getAllTopLevelLists(Mockito.anyString())).thenReturn(germplasmLists);
		Mockito.when(germplasmListManager.getGermplasmListMetadata(germplasmLists))
				.thenReturn(createGermplasmListMetaData(germplasmList, numberOfEntries));

		listSelectorComponent.addProgramLevelLists(germplasmListSource, new ArrayList<UserDefinedField>());

		Mockito.verify(germplasmListManager).getAllTopLevelLists(ListSelectorComponentTest.PROGRAM_UUID);

		// Verify root folder 'Program lists' is created
		Mockito.verify(germplasmListSource)
				.addItem(listSelectorComponent.generateCellInfo(ListSelectorComponent.PROGRAM_LISTS, "", "", "", ""),
						ListSelectorComponent.PROGRAM_LISTS);

		// Verify that the germplasm lists are added to the 'Program lists' folder
		Mockito.verify(germplasmListSource)
				.addItem(listSelectorComponent.generateCellInfo(name, "", description, "Germplasm List", numberOfEntries.toString()),
						germplasmList.getId());
		Mockito.verify(germplasmListSource).setParent(Mockito.eq(germplasmListId), Mockito.eq(ListSelectorComponent.PROGRAM_LISTS));

	}

	@Test
	public void testAddCropLevelLists() {

		final Integer germplasmListId = 123;
		final String name = "Test Name";
		final String description = "Test Description";
		final Integer numberOfEntries = 999;
		final GermplasmList germplasmList = createGermplasmList(germplasmListId, name, description);
		final List<GermplasmList> germplasmLists = new ArrayList<>();
		germplasmLists.add(germplasmList);

		Mockito.when(germplasmListManager.getAllTopLevelLists(Mockito.anyString())).thenReturn(germplasmLists);
		Mockito.when(germplasmListManager.getGermplasmListMetadata(germplasmLists))
				.thenReturn(createGermplasmListMetaData(germplasmList, numberOfEntries));

		listSelectorComponent.addCropLevelLists(germplasmListSource, new ArrayList<UserDefinedField>());

		Mockito.verify(germplasmListManager).getAllTopLevelLists(null);

		// Verify root folder 'Crop lists' is created
		Mockito.verify(germplasmListSource)
				.addItem(listSelectorComponent.generateCellInfo(ListSelectorComponent.CROP_LISTS, "", "", "", ""),
						ListSelectorComponent.CROP_LISTS);

		// Verify that the germplasm lists are added to the 'Crop lists' folder
		Mockito.verify(germplasmListSource)
				.addItem(listSelectorComponent.generateCellInfo(name, "", description, "Germplasm List", numberOfEntries.toString()),
						germplasmList.getId());
		Mockito.verify(germplasmListSource).setParent(Mockito.eq(germplasmListId), Mockito.eq(ListSelectorComponent.CROP_LISTS));

	}

	@Test
	public void testAddGermplasmList() {

		final Integer parentId = 111;
		final Integer germplasmListId = 123;
		final String name = "Test Name";
		final String description = "Test Description";
		final Integer numberOfEntries = 999;
		final GermplasmList germplasmList = createGermplasmList(germplasmListId, name, description);
		final Map<Integer, GermplasmListMetadata> metadataMap = createGermplasmListMetaData(germplasmList, numberOfEntries);

		listSelectorComponent
				.addGermplasmList(parentId, germplasmList, metadataMap, germplasmListSource, new ArrayList<UserDefinedField>());

		final ArgumentCaptor<Object[]> argumentCaptor = ArgumentCaptor.forClass(Object[].class);

		Mockito.verify(germplasmListSource).addItem(argumentCaptor.capture(), Mockito.eq(germplasmList.getId()));

		final Object[] cellInfo = argumentCaptor.getValue();

		Assert.assertEquals(" " + name, cellInfo[0]);
		Assert.assertEquals("", cellInfo[1]);
		Assert.assertEquals(description, cellInfo[2]);
		Assert.assertEquals("Germplasm List", cellInfo[3]);
		Assert.assertEquals(numberOfEntries.toString(), cellInfo[4]);

		Mockito.verify(germplasmListSource).setItemCaption(Mockito.eq(germplasmListId), Mockito.eq(name));
		Mockito.verify(germplasmListSource).setChildrenAllowed(Mockito.eq(germplasmListId), Mockito.eq(germplasmList.isFolder()));
		Mockito.verify(germplasmListSource).setParent(Mockito.eq(germplasmListId), Mockito.eq(parentId));

	}

	@Test
	public void testGermplasmListItemClickListenerItemIsList() {

		final Object itemId = 1;
		final ListSelectorComponent listSelectorComponent = Mockito.mock(ListSelectorComponent.class);
		final ListSelectorComponent.GermplasmListItemClickListener listener =
				listSelectorComponent.new GermplasmListItemClickListener(listSelectorComponent);

		final ItemClickEvent event = Mockito.mock(ItemClickEvent.class);
		Mockito.when(event.getButton()).thenReturn(MouseEvents.ClickEvent.BUTTON_LEFT);
		Mockito.when(event.getItemId()).thenReturn(itemId);

		listener.itemClick(event);

		Mockito.verify(listSelectorComponent).setSelectedListId(itemId);
		Mockito.verify(listSelectorComponent).updateButtons(itemId);
		Mockito.verify(listSelectorComponent).toggleFolderSectionForItemSelected();
		Mockito.verify(listSelectorComponent).treeItemClickAction(Integer.valueOf(itemId.toString()));

		Mockito.verify(listSelectorComponent, Mockito.times(0)).expandOrCollapseListTreeNode(itemId);
		Mockito.verify(listSelectorComponent, Mockito.times(0)).folderClickedAction(null);

	}

	@Test
	public void testGermplasmListItemClickListenerItemIsRootFolder() {

		final Object itemId = ListSelectorComponent.CROP_LISTS;
		final ListSelectorComponent listSelectorComponent = Mockito.mock(ListSelectorComponent.class);
		final ListSelectorComponent.GermplasmListItemClickListener listener =
				listSelectorComponent.new GermplasmListItemClickListener(listSelectorComponent);

		final ItemClickEvent event = Mockito.mock(ItemClickEvent.class);
		Mockito.when(event.getButton()).thenReturn(MouseEvents.ClickEvent.BUTTON_LEFT);
		Mockito.when(event.getItemId()).thenReturn(itemId);

		listener.itemClick(event);

		Mockito.verify(listSelectorComponent).setSelectedListId(itemId);
		Mockito.verify(listSelectorComponent).updateButtons(itemId);
		Mockito.verify(listSelectorComponent).toggleFolderSectionForItemSelected();
		Mockito.verify(listSelectorComponent).expandOrCollapseListTreeNode(itemId);
		Mockito.verify(listSelectorComponent).folderClickedAction(null);

		Mockito.verify(listSelectorComponent, Mockito.times(0)).treeItemClickAction(Mockito.anyInt());

	}

	@Test
	public void testGetParentOfListItem() {

		final Integer selectedItemId = 1;
		final Integer parentItemId = 2;

		Mockito.when(germplasmListSource.getParent(selectedItemId)).thenReturn(parentItemId);

		Assert.assertEquals(parentItemId, listSelectorComponent.getParentOfListItem(selectedItemId));


	}

	private GermplasmList createGermplasmList(final Integer id, final String name, final String description) {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(id);
		germplasmList.setName(name);
		germplasmList.setDescription(description);
		germplasmList.setType(GermplasmList.FOLDER_TYPE);
		return germplasmList;
	}

	private Map<Integer, GermplasmListMetadata> createGermplasmListMetaData(final GermplasmList germplasmList,
			final Integer numberOfEntries) {
		final Map<Integer, GermplasmListMetadata> metadataMap = new HashMap<>();
		final GermplasmListMetadata metadata = new GermplasmListMetadata();
		metadata.setNumberOfEntries(numberOfEntries);
		metadataMap.put(germplasmList.getId(), metadata);
		return metadataMap;
	}

}
