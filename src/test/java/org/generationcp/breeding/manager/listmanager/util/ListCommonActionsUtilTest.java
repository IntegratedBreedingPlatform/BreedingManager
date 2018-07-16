
package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.ListSelectionComponent;
import org.generationcp.breeding.manager.listmanager.ListSelectionLayout;
import org.generationcp.breeding.manager.listmanager.ListTabComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.InventoryDetailsTestDataInitializer;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class ListCommonActionsUtilTest {

	public static final String SUCCESS = "Success";
	private GermplasmList listToSave;
	private List<GermplasmListData> listEntries;
	private Boolean forceHasChanges;
	private List<GermplasmListData> newEntries;
	private List<GermplasmListData> entriesToUpdate;
	private List<GermplasmListData> entriesToDelete;

	@Mock
	private ListSelectionComponent listSelectionComponent;

	@Mock
	private ListBuilderComponent listBuilderComponent;

	@Mock
	private ListManagerMain listManagerMain;

	@Mock
	private GermplasmListManager dataManager;

	@Mock
	private Component source;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private Window window;

	@Test
	public void testGetNewEntriesToSaveUpdateDelete_ForNewEntries() throws MiddlewareQueryException {
		this.forceHasChanges = false;
		this.listToSave = GermplasmListTestDataInitializer.createGermplasmList(1);
		this.listEntries = GermplasmListTestDataInitializer.createGermplasmListData(this.listToSave, 5);
		this.newEntries = new ArrayList<GermplasmListData>();
		this.entriesToUpdate = new ArrayList<GermplasmListData>();
		this.entriesToDelete = new ArrayList<GermplasmListData>();

		Mockito.when(this.dataManager.countGermplasmListDataByListId(this.listToSave.getId())).thenReturn(5L);
		Mockito.when(this.dataManager.getGermplasmListDataByListId(this.listToSave.getId()))
				.thenReturn(this.listToSave.getListData());

		ListCommonActionsUtil.getNewEntriesToSaveUpdateDelete(this.listToSave, this.listEntries, this.forceHasChanges,
				this.newEntries, this.entriesToUpdate, this.entriesToDelete, this.dataManager, this.source,
				this.messageSource);

		Assert.assertTrue("Expecting that the newEntries has entries but didn't.", !this.newEntries.isEmpty());
	}

	@Test
	public void testSetDesignationOfMatchingSavedEntry_WhenDesignationIsNull() {
		final GermplasmListData entry = new GermplasmListData();
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setDesignationOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the designation value is \"-\" when the source designation is null.", "-",
				matchingSavedEntry.getDesignation());
	}

	@Test
	public void testSetDesignationOfMatchingSavedEntry_WhenDesignationIsNotNull() {
		final GermplasmListData entry = new GermplasmListData();
		final String expectedDesignation = "Expected Designation";
		entry.setDesignation(expectedDesignation);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setDesignationOfMatchingSavedEntry(entry, matchingSavedEntry);

		final String actualDesignation = matchingSavedEntry.getDesignation();
		Assert.assertEquals(
				"Expecting that the designation value is " + expectedDesignation + " but returned " + actualDesignation,
				expectedDesignation, actualDesignation);
	}

	@Test
	public void testSetEntryCodeOfMatchingSavedEntry_WhenEntryCodeIsNull() {
		final GermplasmListData entry = new GermplasmListData();
		entry.setEntryId(1);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setEntryCodeOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals(
				"Expecting that the entry code value is " + entry.getEntryId() + " when the source entry is null.",
				entry.getEntryId().toString(), matchingSavedEntry.getEntryCode());
	}

	@Test
	public void testSetEntryCodeOfMatchingSavedEntry_WhenEntryCodeIsNotNull() {
		final GermplasmListData entry = new GermplasmListData();
		final String expectedEntryCode = "Expected Entry Code";
		entry.setEntryCode(expectedEntryCode);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setEntryCodeOfMatchingSavedEntry(entry, matchingSavedEntry);

		final String actualEntryCode = matchingSavedEntry.getEntryCode();
		Assert.assertEquals(
				"Expecting that the entry code value is " + expectedEntryCode + " but returned " + actualEntryCode,
				expectedEntryCode, actualEntryCode);
	}

	@Test
	public void testSetSeedSourceOfMatchingSavedEntry_WhenSeedSourceIsNull() {
		final GermplasmListData entry = new GermplasmListData();
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setSeedSourceOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the seed source value is \"-\" when the source entry is null.", "-",
				matchingSavedEntry.getSeedSource());
	}

	@Test
	public void testSetSeedSourceOfMatchingSavedEntry_WhenSeedSourceIsNotNull() {
		final GermplasmListData entry = new GermplasmListData();
		final String expectedSeedSource = "Expected Seed Source";
		entry.setSeedSource(expectedSeedSource);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setSeedSourceOfMatchingSavedEntry(entry, matchingSavedEntry);

		final String actualSeedSource = matchingSavedEntry.getSeedSource();
		Assert.assertEquals(
				"Expecting that the seed source value is " + expectedSeedSource + " but returned " + actualSeedSource,
				expectedSeedSource, actualSeedSource);
	}

	@Test
	public void testSetGroupNameOfMatchingSavedEntry_WhenGroupNameIsNull() {
		final GermplasmListData entry = new GermplasmListData();
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setGroupNameOfMatchingSavedEntry(entry, matchingSavedEntry);

		Assert.assertEquals("Expecting that the groupname value is \"-\" when the source entry is null.", "-",
				matchingSavedEntry.getGroupName());
	}

	@Test
	public void testSetGroupNameOfMatchingSavedEntry_WhenGroupNameIsNotNull() {
		final GermplasmListData entry = new GermplasmListData();
		final String expectedGroupName = "Expected Group Name";
		entry.setGroupName(expectedGroupName);
		final GermplasmListData matchingSavedEntry = new GermplasmListData();

		ListCommonActionsUtil.setGroupNameOfMatchingSavedEntry(entry, matchingSavedEntry);

		final String actualGroupName = matchingSavedEntry.getGroupName();
		Assert.assertEquals(
				"Expecting that the group name value is " + expectedGroupName + " but returned " + actualGroupName,
				expectedGroupName, actualGroupName);
	}

	@Test
	public void testDeleteExistingListEntries() {
		this.listToSave = GermplasmListTestDataInitializer.createGermplasmList(1);
		final Integer listId = this.listToSave.getId();

		ListCommonActionsUtil.deleteExistingListEntries(listId, this.dataManager);

		Mockito.verify(this.dataManager, Mockito.times(1)).deleteGermplasmListDataByListId(listId);
	}

	@Test
	public void testHasReservationForAnyListEntriesReturnsTrue() {
		final List<GermplasmListData> germplasmListData = InventoryDetailsTestDataInitializer
				.createGermplasmListDataForReservedEntries();
		germplasmListData.get(0).getInventoryInfo().getLotRows().get(0).setWithdrawalStatus("Reserved");

		final boolean hasAnyReservation = ListCommonActionsUtil.hasReservationForAnyListEntries(germplasmListData);
		Assert.assertTrue(hasAnyReservation);
	}

	@Test
	public void testHasReservationForAnyListEntriesReturnsFalse() {
		final List<GermplasmListData> germplasmListData = InventoryDetailsTestDataInitializer
				.createGermplasmListDataForReservedEntries();
		germplasmListData.get(0).getInventoryInfo().getLotRows().get(0).setWithdrawalStatus("Committed");

		final boolean hasAnyReservation = ListCommonActionsUtil.hasReservationForAnyListEntries(germplasmListData);
		Assert.assertFalse(hasAnyReservation);
	}

	@Test
	public void testHandleCreateLabelsActionWithNoReservation() {
		final List<GermplasmListData> germplasmListData = InventoryDetailsTestDataInitializer
				.createGermplasmListDataForReservedEntries();
		germplasmListData.get(0).getInventoryInfo().getLotRows().get(0)
				.setWithdrawalStatus(GermplasmInventory.WITHDRAWN);

		Mockito.when(this.inventoryDataManager.getLotDetailsForList(Matchers.isA(Integer.class), Matchers.anyInt(),
				Matchers.anyInt())).thenReturn(germplasmListData);

		ListCommonActionsUtil.handleCreateLabelsAction(1, this.inventoryDataManager, this.messageSource, null, null,
				this.window);

		Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.PRINT_LABELS);
		Mockito.verify(this.messageSource, Mockito.times(1))
				.getMessage(Message.ERROR_COULD_NOT_CREATE_LABELS_WITHOUT_RESERVATION);
	}

	@Test
	public void testCreateListEntryLotDetailsMap() {
		final List<GermplasmListData> germplasmListData = InventoryDetailsTestDataInitializer
				.createGermplasmListDataForReservedEntries();

		final Map<Integer, ListEntryLotDetails> listEntryLotDetailsMap = ListCommonActionsUtil
				.createListEntryLotDetailsMap(germplasmListData);

		Assert.assertNotNull(listEntryLotDetailsMap);
		Assert.assertEquals(1, listEntryLotDetailsMap.size());
	}

	@Test
	public void testCreateLotDetailsMap() {
		final List<GermplasmListData> germplasmListData = InventoryDetailsTestDataInitializer
				.createGermplasmListDataForReservedEntries();

		final Map<Integer, LotDetails> lotDetailsMap = ListCommonActionsUtil.createLotDetailsMap(germplasmListData);

		Assert.assertNotNull(lotDetailsMap);
		Assert.assertEquals(1, lotDetailsMap.size());

	}

	@Test
	public void testUpdateGermplasmListStatusUI() {
		Mockito.when(this.listManagerMain.getListSelectionComponent()).thenReturn(this.listSelectionComponent);
		final ListSelectionLayout listSelectionLayout = Mockito.mock(ListSelectionLayout.class);
		Mockito.when(this.listSelectionComponent.getListDetailsLayout()).thenReturn(listSelectionLayout);
		final TabSheet tabSheet = Mockito.mock(TabSheet.class);
		Mockito.when(listSelectionLayout.getDetailsTabsheet()).thenReturn(tabSheet);
		Mockito.when(tabSheet.getComponentCount()).thenReturn(1);
		final Tab tab = Mockito.mock(Tab.class);
		Mockito.when(tabSheet.getTab(0)).thenReturn(tab);
		final ListTabComponent listTabComponent = Mockito.mock(ListTabComponent.class);
		Mockito.when(tab.getComponent()).thenReturn(listTabComponent);
		final ListComponent listComponent = Mockito.mock(ListComponent.class);
		Mockito.when(listTabComponent.getListComponent()).thenReturn(listComponent);
		ListCommonActionsUtil.updateGermplasmListStatusUI(this.listManagerMain);
		Mockito.verify(listComponent).updateGermplasmListStatus();
	}

	@Test
	public void testgetLotCountButton() {
		final Button lotButton = ListCommonActionsUtil.getLotCountButton(2, 2, "Germplasm", this.source, 2);
		Assert.assertEquals("Expecting lot count value as 2", 2, Integer.parseInt(lotButton.getCaption()));

	}

	@Test
	public void testOverwriteListSourceIsListBuilderComponent() {

		final Integer listId = 1;

		final GermplasmList germplasmListToSave = GermplasmListTestDataInitializer.createGermplasmList(listId);
		final GermplasmList germplasmListFromDatabase = new GermplasmList();

		Mockito.when(this.dataManager.getGermplasmListById(listId)).thenReturn(germplasmListFromDatabase);
		Mockito.when(this.dataManager.updateGermplasmList(germplasmListFromDatabase)).thenReturn(listId);
		Mockito.when(this.listBuilderComponent.getSource()).thenReturn(this.listManagerMain);
		Mockito.when(this.listManagerMain.getListSelectionComponent()).thenReturn(this.listSelectionComponent);

		ListCommonActionsUtil.overwriteList(germplasmListToSave, this.dataManager, this.listBuilderComponent,
				this.messageSource, true);

		Assert.assertEquals(germplasmListToSave.getName(), germplasmListFromDatabase.getName());
		Assert.assertEquals(germplasmListToSave.getDescription(), germplasmListFromDatabase.getDescription());
		Assert.assertEquals(germplasmListToSave.getDate(), germplasmListFromDatabase.getDate());
		Assert.assertEquals(germplasmListToSave.getType(), germplasmListFromDatabase.getType());
		Assert.assertEquals(germplasmListToSave.getNotes(), germplasmListFromDatabase.getNotes());
		Assert.assertEquals(germplasmListToSave.getProgramUUID(), germplasmListFromDatabase.getProgramUUID());

		Mockito.verify(this.dataManager).updateGermplasmList(germplasmListFromDatabase);
		Mockito.verify(this.listBuilderComponent).setCurrentlySavedGermplasmList(germplasmListFromDatabase);
		Mockito.verify(this.listBuilderComponent).setHasUnsavedChanges(false);
		Mockito.verify(this.listSelectionComponent).showNodeOnTree(listId);

	}

	@Test
	public void testOverwriteListSourceIsListManagerMain() {

		final Integer listId = 1;

		final GermplasmList germplasmListToSave = GermplasmListTestDataInitializer.createGermplasmList(listId);
		final GermplasmList germplasmListFromDatabase = new GermplasmList();
		germplasmListFromDatabase.setId(listId);

		Mockito.when(this.dataManager.getGermplasmListById(listId)).thenReturn(germplasmListFromDatabase);
		Mockito.when(this.dataManager.updateGermplasmList(germplasmListFromDatabase)).thenReturn(listId);
		Mockito.when(this.listBuilderComponent.getSource()).thenReturn(this.listManagerMain);
		Mockito.when(this.listManagerMain.getListSelectionComponent()).thenReturn(this.listSelectionComponent);
		Mockito.when(this.listManagerMain.getWindow()).thenReturn(this.window);
		Mockito.when(this.messageSource.getMessage(Message.SUCCESS)).thenReturn(ListCommonActionsUtilTest.SUCCESS);

		ListCommonActionsUtil.overwriteList(germplasmListToSave, this.dataManager, this.listManagerMain,
				this.messageSource, true);

		Assert.assertEquals(germplasmListToSave.getName(), germplasmListFromDatabase.getName());
		Assert.assertEquals(germplasmListToSave.getDescription(), germplasmListFromDatabase.getDescription());
		Assert.assertEquals(germplasmListToSave.getDate(), germplasmListFromDatabase.getDate());
		Assert.assertEquals(germplasmListToSave.getType(), germplasmListFromDatabase.getType());
		Assert.assertEquals(germplasmListToSave.getNotes(), germplasmListFromDatabase.getNotes());
		Assert.assertEquals(germplasmListToSave.getProgramUUID(), germplasmListFromDatabase.getProgramUUID());		

		Mockito.verify(this.dataManager).updateGermplasmList(germplasmListFromDatabase);
		Mockito.verify(this.listSelectionComponent).updateUIForRenamedList(germplasmListToSave,
				germplasmListToSave.getName());
		Mockito.verify(this.listSelectionComponent).showNodeOnTree(listId);

		final ArgumentCaptor<Window.Notification> captor = ArgumentCaptor.forClass(Window.Notification.class);

		Mockito.verify(this.window).showNotification(captor.capture());

		final Window.Notification notification = captor.getValue();

		Assert.assertEquals(ListCommonActionsUtilTest.SUCCESS, notification.getCaption());
		Assert.assertEquals("</br>Changes to list header were saved.", notification.getDescription());

	}
	
	@Test
	public void testOverwriteListSourceIsListManagerMainListMovedToCropLists() {

		final Integer listId = 1;

		final GermplasmList germplasmListToSave = GermplasmListTestDataInitializer.createGermplasmList(listId);
		germplasmListToSave.setProgramUUID(null);
		final GermplasmList germplasmListFromDatabase = new GermplasmList();
		germplasmListFromDatabase.setId(listId);

		Mockito.when(this.dataManager.getGermplasmListById(listId)).thenReturn(germplasmListFromDatabase);
		Mockito.when(this.dataManager.updateGermplasmList(germplasmListFromDatabase)).thenReturn(listId);
		Mockito.when(this.listBuilderComponent.getSource()).thenReturn(this.listManagerMain);
		Mockito.when(this.listManagerMain.getListSelectionComponent()).thenReturn(this.listSelectionComponent);
		Mockito.when(this.listManagerMain.getWindow()).thenReturn(this.window);
		Mockito.when(this.messageSource.getMessage(Message.SUCCESS)).thenReturn(ListCommonActionsUtilTest.SUCCESS);

		ListCommonActionsUtil.overwriteList(germplasmListToSave, this.dataManager, this.listManagerMain,
				this.messageSource, true);

		Assert.assertEquals(germplasmListToSave.getName(), germplasmListFromDatabase.getName());
		Assert.assertEquals(germplasmListToSave.getDescription(), germplasmListFromDatabase.getDescription());
		Assert.assertEquals(germplasmListToSave.getDate(), germplasmListFromDatabase.getDate());
		Assert.assertEquals(germplasmListToSave.getType(), germplasmListFromDatabase.getType());
		Assert.assertEquals(germplasmListToSave.getNotes(), germplasmListFromDatabase.getNotes());
		Assert.assertEquals(germplasmListToSave.getProgramUUID(), germplasmListFromDatabase.getProgramUUID());
		Assert.assertEquals(SaveListAsDialog.LIST_LOCKED_STATUS, germplasmListFromDatabase.getStatus());

		Mockito.verify(this.dataManager).updateGermplasmList(germplasmListFromDatabase);
		Mockito.verify(this.listSelectionComponent).updateUIForRenamedList(germplasmListToSave,
				germplasmListToSave.getName());
		Mockito.verify(this.listSelectionComponent).showNodeOnTree(listId);

		final ArgumentCaptor<Window.Notification> captor = ArgumentCaptor.forClass(Window.Notification.class);

		Mockito.verify(this.window).showNotification(captor.capture());

		final Window.Notification notification = captor.getValue();

		Assert.assertEquals(ListCommonActionsUtilTest.SUCCESS, notification.getCaption());
		Assert.assertEquals("</br>Changes to list header were saved.", notification.getDescription());

	}
}
