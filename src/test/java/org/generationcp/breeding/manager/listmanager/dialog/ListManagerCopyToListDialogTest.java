package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.Arrays;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.UserDefinedFieldTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

import junit.framework.Assert;

@RunWith(value = MockitoJUnitRunner.class)
public class ListManagerCopyToListDialogTest {
	private static final String LIST_NAME = "List 1";

		@Mock
	private GermplasmListManager germplasmListManager;
	
	@Mock
	private ContextUtil contextUtil;
	
	private ListManagerCopyToListDialog listManagerCopyToListDialog;
	
	private UserDefinedFieldTestDataInitializer userDefinedFieldTestDataInitializer;
	
	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;
	
	ComboBox comboBox;
	
	@Before
	public void setUp(){
		this.initializeListManagerCopyToListDialog();
		this.setValues();
		this.userDefinedFieldTestDataInitializer = new UserDefinedFieldTestDataInitializer();
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Arrays.asList(this.userDefinedFieldTestDataInitializer.createUserDefinedField("LST", "GERMPLASMLISTS")));
	}
	
	@Test
	public void testPopulateSelectType(){
		Select selectType = new Select();
		this.listManagerCopyToListDialog.populateSelectType(selectType);
		Assert.assertEquals("The Select's value should be LST", "LST", selectType.getValue());
		
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
	
	private void initializeListManagerCopyToListDialog() {
		Window mainWindow = Mockito.mock(Window.class);
		Window dialogWindow = Mockito.mock(Window.class);
		String listName = "List Name";
		Table listEntriesTable = Mockito.mock(Table.class);
		int ibdbUserId = 1;
		ListManagerMain listManagerMain = Mockito.mock(ListManagerMain.class);
		this.listManagerCopyToListDialog = new ListManagerCopyToListDialog(mainWindow, dialogWindow, listName, listEntriesTable, ibdbUserId, listManagerMain);
	}
	
	private void setValues(){
		this.listManagerCopyToListDialog.setGermplasmListManager(this.germplasmListManager);
		this.listManagerCopyToListDialog.setContextUtil(this.contextUtil);
		
		comboBox = new ComboBox();
		this.listManagerCopyToListDialog.setComboListName(comboBox);
	}
}
