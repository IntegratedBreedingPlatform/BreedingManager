package org.generationcp.breeding.manager.listmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderComponent;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Window;

public class ListComponentTest {
	
	private static final String DUMMY_OPTION = "Dummy Option";
	private static final String UPDATED_GERMPLASM_LIST_NOTE = "UPDATED Germplasm List Note";
	private static final String UPDATED_GERMPLASM_LIST_NAME = "UPDATED Germplasm List Name";
	private static final String UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE = "UPDATED Germplasm List Description Value";
	private static final long UPDATED_GERMPLASM_LIST_DATE = (long) 20141205;
	private static final String UPDATED_GERMPLASM_LIST_TYPE = "F1 LST";
	private static final String GERMPLASM_LIST_NOTE = "Germplasm List Note";
	private static final String GERMPLASM_LIST_NAME = "Germplasm List Name";
	private static final String GERMPLASM_LIST_DESCRIPTION_VALUE = "Germplasm List Description Value";
	private static final long GERMPLASM_LIST_DATE = (long) 20141104;
	private static final String GERMPLASM_LIST_TYPE = "LST";

	private ListManagerMain source;
	
	@Mock
	private ListTabComponent parentListDetailsComponent;
	
	@Mock
	private GermplasmListManager germplasmListManager;
	
	@Mock
	private WorkbenchDataManager workbenchDataManager;
	
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	private ListComponent listComponent;
	
	private GermplasmList germplasmList;
	
	private Integer EXPECTED_USER_ID = 1;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		setUpWorkbench();
		
		source = spy(new ListManagerMain());
		source.setGermplasmListManager(germplasmListManager);
		source.setWorkbenchDataManager(workbenchDataManager);
		
		germplasmList = new GermplasmList();
		germplasmList.setId(1);
		germplasmList.setDescription(GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmList.setName(GERMPLASM_LIST_NAME);
		germplasmList.setNotes(GERMPLASM_LIST_NOTE);
		germplasmList.setDate(GERMPLASM_LIST_DATE);
		germplasmList.setType(GERMPLASM_LIST_TYPE);
		germplasmList.setStatus(1);
		
		
		List<GermplasmListData> listEntries = new ArrayList<GermplasmListData>();
		listEntries.add(mock(GermplasmListData.class));
		
		listComponent = spy(new ListComponent(source,  parentListDetailsComponent,  germplasmList));
		listComponent.setGermplasmListManager(germplasmListManager);
		listComponent.setMessageSource(messageSource);
		listComponent.setListEntries(listEntries);
	
		doReturn(mock(Window.class)).when(source).getWindow();
		doReturn(mock(ListSelectionComponent.class)).when(source).getListSelectionComponent();
		doNothing().when(listComponent).refreshTreeOnSave();
	}
	
	private void setUpWorkbench() {
		workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);
		
		WorkbenchRuntimeData runtimeDate = new WorkbenchRuntimeData();
		runtimeDate.setUserId(new Integer(5));

		Project dummyProject = new Project();
		dummyProject.setProjectId(new Long(5));

		try {
			Mockito.when(this.workbenchDataManager.getWorkbenchRuntimeData()).thenReturn(
					runtimeDate);
			Mockito.when(this.workbenchDataManager.getLastOpenedProject(runtimeDate.getUserId()))
					.thenReturn(dummyProject);
			Mockito.when(
					this.workbenchDataManager.getLocalIbdbUserId(runtimeDate.getUserId(),
							dummyProject.getProjectId())).thenReturn(EXPECTED_USER_ID);

		} catch (MiddlewareQueryException e) {
			Assert.fail("Failed to create an ibdbuser instance.");
		}
	}

	@Test
	public void testSaveList_OverwriteExistingGermplasmList(){
		
		GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(1);
		germplasmListToBeSaved.setDescription(UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(1);
		
		try{
			doReturn(germplasmList).when(germplasmListManager).getGermplasmListById(germplasmList.getId().intValue());
			
			listComponent.saveList(germplasmListToBeSaved);
			
			GermplasmList savedList = listComponent.getGermplasmList();
			
			assertEquals(savedList.getId(), germplasmListToBeSaved.getId());
			assertEquals(savedList.getDescription(), germplasmListToBeSaved.getDescription());
			assertEquals(savedList.getName(), germplasmListToBeSaved.getName());
			assertEquals(savedList.getNotes(), germplasmListToBeSaved.getNotes());
			assertEquals(savedList.getDate(), germplasmListToBeSaved.getDate());
			assertEquals(savedList.getType(), germplasmListToBeSaved.getType());
			assertEquals(savedList.getStatus(), germplasmListToBeSaved.getStatus());
			
			assertSame(savedList, germplasmList);
			
		}catch(Exception e){
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testSaveList_OverwriteExistingGermplasmListWithDifferentID(){
		
		GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(1000);
		germplasmListToBeSaved.setDescription(UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(1);	
		
		try{
			doNothing().when(source).closeList(germplasmListToBeSaved);
			doReturn(germplasmListToBeSaved).when(germplasmListManager).getGermplasmListById(anyInt());
			
			//this will overwrite the list entries of the current germplasm list. Germplasm List Details will not be updated.
			listComponent.saveList(germplasmListToBeSaved);
			
			GermplasmList savedList = listComponent.getGermplasmList();
			
			assertFalse("",savedList.getId().equals(germplasmListToBeSaved.getId()));
			assertFalse(savedList.getDescription().equals(germplasmListToBeSaved.getDescription()));
			assertFalse(savedList.getName().equals(germplasmListToBeSaved.getName()));
			assertFalse(savedList.getNotes().equals(germplasmListToBeSaved.getNotes()));
			assertFalse(savedList.getDate().equals(germplasmListToBeSaved.getDate()));
			assertFalse(savedList.getType().equals(germplasmListToBeSaved.getType()));
			
			assertSame(savedList, germplasmList);
			
		}catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testSaveList_OverwriteNonExistingGermplasmList(){
		
		GermplasmList germplasmListToBeSaved = new GermplasmList();
		germplasmListToBeSaved.setId(1);
		germplasmListToBeSaved.setDescription(UPDATED_GERMPLASM_LIST_DESCRIPTION_VALUE);
		germplasmListToBeSaved.setName(UPDATED_GERMPLASM_LIST_NAME);
		germplasmListToBeSaved.setNotes(UPDATED_GERMPLASM_LIST_NOTE);
		germplasmListToBeSaved.setDate(UPDATED_GERMPLASM_LIST_DATE);
		germplasmListToBeSaved.setType(UPDATED_GERMPLASM_LIST_TYPE);
		germplasmListToBeSaved.setStatus(1);
		
		try{
			doReturn(null).when(germplasmListManager).getGermplasmListById(germplasmList.getId().intValue());
			
			listComponent.saveList(germplasmListToBeSaved);
			
			GermplasmList savedList = listComponent.getGermplasmList();
			
			assertTrue(savedList.getId().equals(germplasmListToBeSaved.getId()));
			assertFalse(savedList.getDescription().equals(germplasmListToBeSaved.getDescription()));
			assertFalse(savedList.getName().equals(germplasmListToBeSaved.getName()));
			assertFalse(savedList.getNotes().equals(germplasmListToBeSaved.getNotes()));
			assertFalse(savedList.getDate().equals(germplasmListToBeSaved.getDate()));
			assertFalse(savedList.getType().equals(germplasmListToBeSaved.getType()));
			
			assertSame(savedList, germplasmList);
			
		}catch(Exception e){
			
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testLockGermplasmList(){
		//set up
		try {
			doReturn(germplasmList).when(germplasmListManager).getGermplasmListById(germplasmList.getId().intValue());
		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		}
		
		doNothing().when(listComponent).setLockedState(true);
		
		listComponent.lockGermplasmList(germplasmList.getId());
		
		Assert.assertEquals("Expecting the that the germplasmList status was changed to locked(101) but returned (" + germplasmList.getStatus() + ")", Integer.valueOf(101), germplasmList.getStatus());
	}
	
	
	@Test
	public void testAddListeners(){
		ViewListHeaderComponent viewListHeaderComponent = Mockito.mock(ViewListHeaderComponent.class);
		ViewListHeaderWindow viewListHeaderWindow = spy(new ViewListHeaderWindow(germplasmList));
		doReturn(viewListHeaderComponent).when(viewListHeaderWindow).getListHeaderComponent();
		doNothing().when(listComponent).instantiateViewListHeaderWindow();
		doNothing().when(listComponent).initializeListDataTable();
		doNothing().when(listComponent).updateListStatusWhenLaunchFromDashboard();
		doNothing().when(listComponent).addFillWithForUnLockedList();
		doNothing().when(listComponent).makeTableEditable();
		doNothing().when(listComponent).addListenerForUpdatingSelectedEntriesInListDataAndInventoryTable();
		
		Mockito.when(messageSource.getMessage(Message.ADD_ENTRIES)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.COPY_TO_NEW_LIST)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.DELETE_LIST)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.EDIT_LIST)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.EXPORT_LIST)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.INVENTORY_VIEW)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.SAVE_CHANGES)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.SELECT_ALL)).thenReturn(DUMMY_OPTION);
		
		Mockito.when(messageSource.getMessage(Message.CANCEL_RESERVATIONS)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.COPY_TO_NEW_LIST)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.RESERVE_INVENTORY)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.RETURN_TO_LIST_VIEW)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.SAVE_RESERVATIONS)).thenReturn(DUMMY_OPTION);
		
		Mockito.when(messageSource.getMessage(Message.EDIT_VALUE)).thenReturn(DUMMY_OPTION);
		Mockito.when(messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST)).thenReturn(DUMMY_OPTION);
		
		listComponent.setViewListHeaderWindow(viewListHeaderWindow);
		listComponent.instantiateComponents();
		Mockito.verify(listComponent, Mockito.times(1)).instantiateViewListHeaderWindow();
		Mockito.verify(listComponent, Mockito.times(1)).updateListStatusWhenLaunchFromDashboard();
		
		//Trigger the function to test
		listComponent.addListeners();
		
		Mockito.verify(listComponent, Mockito.times(1)).addFillWithForUnLockedList();
		Mockito.verify(listComponent, Mockito.times(1)).makeTableEditable();
		Mockito.verify(listComponent, Mockito.times(1)).addListenerToActionButton();
		Mockito.verify(listComponent, Mockito.times(1)).addListenerToActionMenuInListView();
		Mockito.verify(listComponent, Mockito.times(1)).addListenerToActionMenuInInventoryView();
		Mockito.verify(listComponent, Mockito.times(1)).addListenerToLockUnlockButton();
		Mockito.verify(listComponent, Mockito.times(1)).addListenerToListDataTableContextMenu();
		Mockito.verify(listComponent, Mockito.times(1)).addListenerForUpdatingSelectedEntriesInListDataAndInventoryTable();
	}
	
	@Test
	public void TestIsANonEditableColumnInListDataTable(){
		Assert.assertTrue("GID column must be a non editable column.",listComponent.isANonEditableColumnInListDataTable("gid"));
		Assert.assertTrue("ENTRY_ID column must be a non editable column.",listComponent.isANonEditableColumnInListDataTable("entryId"));
		Assert.assertTrue("DESIGNATION column must be a non editable column.",listComponent.isANonEditableColumnInListDataTable("desig"));
		Assert.assertFalse("PARENTAGE column must be an editable column.",listComponent.isANonEditableColumnInListDataTable("parentage"));
	}
	
	@Test
	public void TestIsAnInventoryRelatedColumn(){
		Assert.assertTrue("SEED_RESERVATION column is an inventory column.",listComponent.isAnInventoryRelatedColumn("seedRes"));
		Assert.assertTrue("AVAIL_INV column is an inventory column.",listComponent.isAnInventoryRelatedColumn("availInv"));
		Assert.assertFalse("PARENTAGE column is not an inventory column.",listComponent.isAnInventoryRelatedColumn("parentage"));
	}
}
