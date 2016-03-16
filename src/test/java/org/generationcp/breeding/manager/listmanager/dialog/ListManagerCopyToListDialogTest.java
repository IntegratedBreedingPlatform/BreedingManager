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
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.runners.MockitoJUnitRunner;

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
	
	private UserDefinedFieldTestDataInitializer userDefinedFieldTestDataInitializer;
	
	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;
	
	private ComboBox comboBox;
	
	private TextField txtDescription;

	private Select selectType;
	
	private static final String LIST_NAME_IS_TOO_LONG = "List Name is Too Looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong.";

	private static final String LIST_NAME = "List 1";
	
	@Before
	public void setUp(){
		this.initializeListManagerCopyToListDialog();
		this.setValues();
		this.userDefinedFieldTestDataInitializer = new UserDefinedFieldTestDataInitializer();
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Arrays.asList(this.userDefinedFieldTestDataInitializer.createUserDefinedField(LST, "GERMPLASMLISTS")));
		Mockito.when(this.table.getValue()).thenReturn(new ArrayList<>());
		Mockito.when(this.listManagerMain.getListSelectionComponent()).thenReturn(listSectionComponent);
		Mockito.when(this.listSectionComponent.getListTreeComponent()).thenReturn(listManagerTreeComponent);
		Mockito.doNothing().when(listManagerTreeComponent).createTree();
		Mockito.doNothing().when(listManagerTreeComponent).expandNode(Matchers.anyString());
		Mockito.doNothing().when(listManagerTreeComponent).treeItemClickAction(Matchers.anyInt());
		Mockito.when(this.listSectionComponent.getListDetailsLayout()).thenReturn(listSectionLayout);
		Mockito.doNothing().when(listSectionLayout).removeTab(Matchers.anyInt());
	}
	
	@Test
	public void testPopulateSelectType(){
		Select selectType = new Select();
		this.listManagerCopyToListDialog.populateSelectType(selectType);
		Assert.assertEquals("The Select's value should be LST", LST, selectType.getValue());
	}
	
	@Test
	public void testPopulateComboBoxListName(){
		Mockito.when(this.germplasmListManager.getAllGermplasmListsByProgramUUID(Matchers.anyString())).thenReturn(Arrays.asList(this.germplasmListTestDataInitializer.createGermplasmList(1)));
		this.listManagerCopyToListDialog.populateComboBoxListName();
		Assert.assertEquals("The combo box's value should be an empty string", "", comboBox.getValue());
		Assert.assertNotNull("The combo box should not return a null value", comboBox.getItem(LIST_NAME));
	}
	
	@Test
	public void testPopulateComboBoxListNameGermplasmListTypeIsFolder(){
		Mockito.when(this.germplasmListManager.getAllGermplasmListsByProgramUUID(Matchers.anyString())).thenReturn(Arrays.asList(this.germplasmListTestDataInitializer.createGermplasmListWithType(1, ListManagerCopyToListDialog.FOLDER_TYPE)));
		this.listManagerCopyToListDialog.populateComboBoxListName();
		Assert.assertEquals("The combo box's value should be an empty string", "", comboBox.getValue());
		Assert.assertNull("The combo box should return a null value", comboBox.getItem(LIST_NAME));
		Assert.assertTrue("The Local Folder Names should contain " + LIST_NAME, this.listManagerCopyToListDialog.getLocalFolderNames().contains(LIST_NAME));
	}
	
	@Test
	public void testSaveGermplasmListButtonClickActionProceedWithSaveIsFalse() {
		this.comboBox.addItem(LIST_NAME);
		this.comboBox.setValue(LIST_NAME);
		this.listManagerCopyToListDialog.setComboListName(comboBox);
		Set<String> localFolderNames = new HashSet<String>();
		localFolderNames.add(LIST_NAME);
		this.listManagerCopyToListDialog.setLocalFolderNames(localFolderNames);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();
		
		try{
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.ERROR_EXISTING_GERMPLASM_LIST_FOLDER_NAME);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is already an existing folder name.");
		}
	}
	
	@Test
	public void testSaveGermplasmListButtonClickActionListNameIsEmpty() {
		this.comboBox.addItem("");
		this.comboBox.setValue("");
		this.listManagerCopyToListDialog.setComboListName(comboBox);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();
		
		try{
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.ERROR_SPECIFY_LIST_NAME);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is empty.");
		}
	}
	
	@Test
	public void testSaveGermplasmListButtonClickActionListNameIsTooLong() {
		this.comboBox.addItem(LIST_NAME_IS_TOO_LONG);
		this.comboBox.setValue(LIST_NAME_IS_TOO_LONG);
		this.listManagerCopyToListDialog.setComboListName(comboBox);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();
		
		try{
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.ERROR_LIST_NAME_TOO_LONG);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is too long.");
		}
	}
	
	@Test
	public void testSaveGermplasmListButtonClickActionExistingListNotSelected() {
		this.comboBox.addItem(LIST_NAME);
		this.comboBox.setValue(LIST_NAME);
		this.listManagerCopyToListDialog.setComboListName(comboBox);
		this.listManagerCopyToListDialog.setExistingListSelected(false);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();
		try{
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_SUCCESS);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is too long.");
		}
	}
	
	@Test
	public void testSaveGermplasmListButtonClickActionExistingListIsSelected() {
		Map<String, Integer> mapExistingList = new HashMap<String, Integer>();
		mapExistingList.put(LIST_NAME, 1);
		this.listManagerCopyToListDialog.setMapExistingList(mapExistingList);
		this.comboBox.addItem(LIST_NAME);
		this.comboBox.setValue(LIST_NAME);
		this.listManagerCopyToListDialog.setComboListName(comboBox);
		this.listManagerCopyToListDialog.setExistingListSelected(true);
		this.listManagerCopyToListDialog.saveGermplasmListButtonClickAction();
		try{
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.SAVE_GERMPLASMLIST_DATA_COPY_TO_NEW_LIST_SUCCESS);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting that the list name is too long.");
		}
	}
	
	private void initializeListManagerCopyToListDialog() {
		Window mainWindow = Mockito.mock(Window.class);
		Window dialogWindow = Mockito.mock(Window.class);
		String listName = "List Name";
		int ibdbUserId = 1;
		ListManagerMain listManagerMain = Mockito.mock(ListManagerMain.class);
		this.listManagerCopyToListDialog = new ListManagerCopyToListDialog(mainWindow, dialogWindow, listName, this.table, ibdbUserId, listManagerMain);
		
		final Window window = Mockito.mock(Window.class);
		Mockito.doReturn(window).when(this.component).getWindow();
		Mockito.doReturn(window).when(window).getParent();
		Mockito.doReturn(window).when(window).getWindow();
		this.listManagerCopyToListDialog.setParent(this.component);
	}
	
	private void setValues(){
		this.listManagerCopyToListDialog.setGermplasmListManager(this.germplasmListManager);
		this.listManagerCopyToListDialog.setContextUtil(this.contextUtil);
		this.listManagerCopyToListDialog.setMessageSource(this.messageSource);
		
		comboBox = new ComboBox();
		this.comboBox.setNewItemsAllowed(true);
		this.comboBox.setNullSelectionAllowed(false);
		this.comboBox.setImmediate(true);
		this.listManagerCopyToListDialog.setComboListName(comboBox);
		
		this.txtDescription = new TextField("Description");
		this.listManagerCopyToListDialog.setTxtDescription(this.txtDescription);
		
		this.selectType = new Select();
		this.selectType.addItem(LST);
		this.selectType.setValue(LST);
		this.listManagerCopyToListDialog.setSelectType(selectType);
		
		this.listManagerCopyToListDialog.setListManagerMain(listManagerMain);
	}
}
