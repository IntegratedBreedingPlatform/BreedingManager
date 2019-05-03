
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.ListSelectionComponent;
import org.generationcp.breeding.manager.listmanager.ListSelectionLayout;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.UserDefinedFieldTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.junit.MockitoJUnitRunner;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import junit.framework.Assert;

@RunWith(value = MockitoJUnitRunner.class)
public class ListManagerCopyToListDialogTest {

	private static final String LST = "LST";

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private Component component;

	@Mock
	private Component componentParent;

	@Mock
	private Table table;

	@Mock
	private ListSelectionComponent listSectionComponent;

	@Mock
	private ListManagerTreeComponent listManagerTreeComponent;

	@Mock
	private ListManagerMain listManagerMain;

	@Mock
	private ListSelectionLayout listSectionLayout;

	private ListManagerCopyToListDialog listManagerCopyToListDialog;

	private ComboBox comboBox;

	private TextField txtDescription;

	private Select selectType;

	private static final String LIST_NAME_IS_TOO_LONG =
			"List Name is Too Looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong.";

	private static final String LIST_NAME = "List 1";

	@Before
	public void setUp() {
		this.initializeListManagerCopyToListDialog();
		this.setValues();
		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Arrays.asList(
				UserDefinedFieldTestDataInitializer.createUserDefinedField(ListManagerCopyToListDialogTest.LST, "GERMPLASMLISTS")));
		Mockito.when(this.table.getValue()).thenReturn(new ArrayList<>());
		Mockito.when(this.listManagerMain.getListSelectionComponent()).thenReturn(this.listSectionComponent);
		Mockito.when(this.listSectionComponent.getListTreeComponent()).thenReturn(this.listManagerTreeComponent);
		Mockito.doNothing().when(this.listManagerTreeComponent).createTree();
		Mockito.doNothing().when(this.listManagerTreeComponent).expandNode(ArgumentMatchers.anyString());
		Mockito.doNothing().when(this.listManagerTreeComponent).treeItemClickAction(ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean());
		Mockito.when(this.listSectionComponent.getListDetailsLayout()).thenReturn(this.listSectionLayout);
		Mockito.doNothing().when(this.listSectionLayout).removeTab(ArgumentMatchers.anyInt());
	}

	@Test
	public void testPopulateSelectType() {
		final Select selectType = new Select();
		this.listManagerCopyToListDialog.populateSelectType(selectType);
		Assert.assertEquals("The Select's value should be LST", ListManagerCopyToListDialogTest.LST, selectType.getValue());
	}

	@Test
	public void testPopulateComboBoxListName() {
		Mockito.when(this.germplasmListManager.getAllGermplasmListsByProgramUUID(ArgumentMatchers.<String>isNull()))
				.thenReturn(Arrays.asList(GermplasmListTestDataInitializer.createGermplasmList(1)));
		this.listManagerCopyToListDialog.populateComboBoxListName();
		Assert.assertEquals("The combo box's value should be an empty string", "", this.comboBox.getValue());
		Assert.assertNotNull("The combo box should not return a null value",
				this.comboBox.getItem(ListManagerCopyToListDialogTest.LIST_NAME));
	}

	@Test
	public void testPopulateComboBoxListNameGermplasmListTypeIsFolder() {
		Mockito.when(this.germplasmListManager.getAllGermplasmListsByProgramUUID(ArgumentMatchers.<String>isNull())).thenReturn(Arrays
				.asList(GermplasmListTestDataInitializer.createGermplasmListWithType(1, ListManagerCopyToListDialog.FOLDER_TYPE)));
		this.listManagerCopyToListDialog.populateComboBoxListName();
		Assert.assertEquals("The combo box's value should be an empty string", "", this.comboBox.getValue());
		Assert.assertNull("The combo box should return a null value", this.comboBox.getItem(ListManagerCopyToListDialogTest.LIST_NAME));
		Assert.assertTrue("The Local Folder Names should contain " + ListManagerCopyToListDialogTest.LIST_NAME,
				this.listManagerCopyToListDialog.getLocalFolderNames().contains(ListManagerCopyToListDialogTest.LIST_NAME));
	}

	@Test
	public void testSaveGermplasmListButtonClickActionProceedWithSaveIsFalse() {
		this.comboBox.addItem(ListManagerCopyToListDialogTest.LIST_NAME);
		this.comboBox.setValue(ListManagerCopyToListDialogTest.LIST_NAME);
		this.listManagerCopyToListDialog.setComboListName(this.comboBox);
		final Set<String> localFolderNames = new HashSet<String>();
		localFolderNames.add(ListManagerCopyToListDialogTest.LIST_NAME);
		this.listManagerCopyToListDialog.setLocalFolderNames(localFolderNames);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();

		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.ERROR_EXISTING_GERMPLASM_LIST_FOLDER_NAME);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is already an existing folder name.");
		}
	}

	@Test
	public void testSaveGermplasmListButtonClickActionListNameIsEmpty() {
		this.comboBox.addItem("");
		this.comboBox.setValue("");
		this.listManagerCopyToListDialog.setComboListName(this.comboBox);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();

		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.ERROR_SPECIFY_LIST_NAME);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is empty.");
		}
	}

	@Test
	public void testSaveGermplasmListButtonClickActionListNameIsTooLong() {
		this.comboBox.addItem(ListManagerCopyToListDialogTest.LIST_NAME_IS_TOO_LONG);
		this.comboBox.setValue(ListManagerCopyToListDialogTest.LIST_NAME_IS_TOO_LONG);
		this.listManagerCopyToListDialog.setComboListName(this.comboBox);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();

		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.ERROR_LIST_NAME_TOO_LONG);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is too long.");
		}
	}

	@Test
	public void testSaveGermplasmListButtonClickActionExistingListNotSelected() {
		this.comboBox.addItem(ListManagerCopyToListDialogTest.LIST_NAME);
		this.comboBox.setValue(ListManagerCopyToListDialogTest.LIST_NAME);
		this.listManagerCopyToListDialog.setComboListName(this.comboBox);
		this.listManagerCopyToListDialog.setExistingListSelected(false);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();
		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_SUCCESS);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is too long.");
		}
	}

	@Test
	public void testSaveGermplasmListButtonClickActionExistingListIsSelected() {
		final Map<String, Integer> mapExistingList = new HashMap<String, Integer>();
		mapExistingList.put(ListManagerCopyToListDialogTest.LIST_NAME, 1);
		this.listManagerCopyToListDialog.setMapExistingList(mapExistingList);
		this.comboBox.addItem(ListManagerCopyToListDialogTest.LIST_NAME);
		this.comboBox.setValue(ListManagerCopyToListDialogTest.LIST_NAME);
		this.listManagerCopyToListDialog.setComboListName(this.comboBox);
		this.listManagerCopyToListDialog.setExistingListSelected(true);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();
		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_EXISTING_LIST_SUCCESS, ListManagerCopyToListDialogTest.LIST_NAME);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is too long.");
		}
	}

	private void initializeListManagerCopyToListDialog() {
		final Window mainWindow = Mockito.mock(Window.class);
		final Window dialogWindow = Mockito.mock(Window.class);
		final String listName = "List Name";
		final int ibdbUserId = 1;
		final ListManagerMain listManagerMain = Mockito.mock(ListManagerMain.class);
		this.listManagerCopyToListDialog =
				new ListManagerCopyToListDialog(mainWindow, dialogWindow, listName, this.table, ibdbUserId, listManagerMain);

		final Window window = Mockito.mock(Window.class);
		Mockito.doReturn(window).when(this.component).getWindow();
		Mockito.doReturn(window).when(window).getParent();
		Mockito.doReturn(window).when(window).getWindow();
		this.listManagerCopyToListDialog.setParent(this.component);
	}

	private void setValues() {
		this.listManagerCopyToListDialog.setGermplasmListManager(this.germplasmListManager);
		this.listManagerCopyToListDialog.setGermplasmDataManager(this.germplasmDataManager);

		this.listManagerCopyToListDialog.setContextUtil(this.contextUtil);
		this.listManagerCopyToListDialog.setMessageSource(this.messageSource);

		this.comboBox = new ComboBox();
		this.comboBox.setNewItemsAllowed(true);
		this.comboBox.setNullSelectionAllowed(false);
		this.comboBox.setImmediate(true);
		this.listManagerCopyToListDialog.setComboListName(this.comboBox);

		this.txtDescription = new TextField("Description");
		this.listManagerCopyToListDialog.setTxtDescription(this.txtDescription);

		this.selectType = new Select();
		this.selectType.addItem(ListManagerCopyToListDialogTest.LST);
		this.selectType.setValue(ListManagerCopyToListDialogTest.LST);
		this.listManagerCopyToListDialog.setSelectType(this.selectType);

		this.listManagerCopyToListDialog.setListManagerMain(this.listManagerMain);
	}
}
