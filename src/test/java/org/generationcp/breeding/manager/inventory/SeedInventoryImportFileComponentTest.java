package org.generationcp.breeding.manager.inventory;

import com.beust.jcommander.internal.Lists;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.inventory.exception.SeedInventoryImportException;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventory;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventoryList;
import org.generationcp.commons.parsing.InvalidFileDataException;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.InventoryDetailsTestDataInitializer;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class SeedInventoryImportFileComponentTest {

	@Mock
	protected InventoryDataManager inventoryDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	private GermplasmList germplasmList;

	@InjectMocks
	SeedInventoryImportFileComponent seedInventoryImportFileComponent = new SeedInventoryImportFileComponent();

	@Before
	public void setUp(){
		this.germplasmList =
				GermplasmListTestDataInitializer.createGermplasmListWithListData(1, 1);

		this.seedInventoryImportFileComponent.setSelectedGermplsmList(this.germplasmList);
		Mockito.when(this.inventoryDataManager.getReservedLotDetailsForExportList(Mockito.anyInt())).thenReturn(InventoryDetailsTestDataInitializer.createGermplasmListDataForReservedEntries());

	}

	@Test
	public void testInitializeValuesTest() throws Exception {
		seedInventoryImportFileComponent.initializeValues();

		Set<String> extensionSet = seedInventoryImportFileComponent.getExtensionSet();
		Assert.assertEquals("Extension list should have two extensions", 2, extensionSet.size());

		List<GermplasmListData> selectedListReservedInventoryDetails =
				seedInventoryImportFileComponent.getSelectedListReservedInventoryDetails();

		Assert.assertEquals("Observation sheet should have only one reservation", 1, selectedListReservedInventoryDetails.size());

		GermplasmListData germplasmListData = selectedListReservedInventoryDetails.get(0);
		Assert.assertEquals("1", germplasmListData.getEntryId().toString());
		Assert.assertEquals("Des", germplasmListData.getDesignation());
		Assert.assertEquals("28", germplasmListData.getGid().toString());
		Assert.assertEquals("GroupName", germplasmListData.getGroupName());
		Assert.assertEquals("SeedSource", germplasmListData.getSeedSource());

		List<? extends LotDetails> lotRows = germplasmListData.getInventoryInfo().getLotRows();
		Assert.assertEquals("Should have one lot details rows", 1, lotRows.size());

		ListEntryLotDetails lotDetails = (ListEntryLotDetails) lotRows.get(0);
		Assert.assertEquals("1", lotDetails.getLotId().toString());
		Assert.assertEquals("stockIds", lotDetails.getStockIds());
		Assert.assertEquals("2.0", new Double(lotDetails.getReservedTotalForEntry()).toString());
	}

	@Test
	public void testValidateImportedSeedInventoryListSuccess() throws SeedInventoryImportException, InvalidFileDataException {
		this.seedInventoryImportFileComponent.setSelectedListReservedInventoryDetails(InventoryDetailsTestDataInitializer.createGermplasmListDataForReservedEntries());

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(createValidImportedInventoryList());

		this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();

		// No assertion to put as if we this test case without exception means data is valid
	}

	@Test
	public void testInvalidValidateImportedSeedInventoryListWithListNameDoesNotMatch() throws SeedInventoryImportException, InvalidFileDataException {
		this.seedInventoryImportFileComponent.setSelectedListReservedInventoryDetails(InventoryDetailsTestDataInitializer.createGermplasmListDataForReservedEntries());

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		ImportedSeedInventoryList inValidImportedInventoryList = createValidImportedInventoryList();
		inValidImportedInventoryList.setListName("incorrectListName");

		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(inValidImportedInventoryList);

		try{
			this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		}
		catch (InvalidFileDataException e){
			Assert.assertEquals(e.getMessage() , Message.SEED_IMPORT_LIST_NAME_MISMATCH_ERROR.toString());
		}
	}

	@Test
	public void testInvalidValidateImportedSeedInventoryListWithGIDDoesNotMatch() throws SeedInventoryImportException, InvalidFileDataException {
		this.seedInventoryImportFileComponent.setSelectedListReservedInventoryDetails(InventoryDetailsTestDataInitializer.createGermplasmListDataForReservedEntries());

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		ImportedSeedInventoryList inValidImportedInventoryList = createValidImportedInventoryList();
		inValidImportedInventoryList.getImportedSeedInventoryList().get(0).setGid(0);

		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(inValidImportedInventoryList);

		try{
			this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		}
		catch (InvalidFileDataException e){
			Assert.assertEquals(e.getMessage() , Message.SEED_IMPORT_GID_MATCH_ERROR.toString());
		}
	}

	@Test
	public void testInvalidValidateImportedSeedInventoryListWithDesignationDoesNotMatch() throws SeedInventoryImportException, InvalidFileDataException {
		this.seedInventoryImportFileComponent.setSelectedListReservedInventoryDetails(InventoryDetailsTestDataInitializer.createGermplasmListDataForReservedEntries());

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		ImportedSeedInventoryList inValidImportedInventoryList = createValidImportedInventoryList();
		inValidImportedInventoryList.getImportedSeedInventoryList().get(0).setDesignation("updatedDesignation");

		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(inValidImportedInventoryList);

		try{
			this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		}
		catch (InvalidFileDataException e){
			Assert.assertEquals(e.getMessage() , Message.SEED_IMPORT_DESIGNATION_MATCH_ERROR.toString());
		}
	}

	@Test
	public void testInvalidValidateImportedSeedInventoryListWithWithdrawalAndBalanceBothGiven() throws SeedInventoryImportException, InvalidFileDataException {
		this.seedInventoryImportFileComponent.setSelectedListReservedInventoryDetails(InventoryDetailsTestDataInitializer.createGermplasmListDataForReservedEntries());

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		ImportedSeedInventoryList inValidImportedInventoryList = createValidImportedInventoryList();
		inValidImportedInventoryList.getImportedSeedInventoryList().get(0).setBalanceAmount(12.0);

		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(inValidImportedInventoryList);

		try{
			this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		}
		catch (InvalidFileDataException e){
			Assert.assertEquals(e.getMessage() , Message.SEED_IMPORT_WITHDRAWAL_BALANCE_BOTH_ERROR.toString());
		}
	}

	@Test
	public void testProcessImportedInventoryTransactionsSuccessWithProcessingState()
			throws SeedInventoryImportException, InvalidFileDataException {

		this.seedInventoryImportFileComponent.initializeValues();

		ImportedSeedInventoryList validImportedInventoryList = createValidImportedInventoryList();
		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(validImportedInventoryList);

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		this.seedInventoryImportFileComponent.processImportedInventoryTransactions();

		Assert.assertEquals(1, this.seedInventoryImportFileComponent.getProcessedTransactions().size());

		Transaction transaction = this.seedInventoryImportFileComponent.getProcessedTransactions().get(0);
		Assert.assertEquals(new Double("-2.0"), transaction.getQuantity());
		Assert.assertEquals(1, transaction.getStatus().intValue());
		Assert.assertEquals("comments", transaction.getComments());
		Assert.assertNull(validImportedInventoryList.getImportedSeedInventoryList().get(0).getTransactionProcessingStatus());

	}

	@Test
	public void testProcessImportedInventoryTransactionsSuccessWithWithdrawalGreaterThanReservationAndProcessingState()
			throws SeedInventoryImportException, InvalidFileDataException {

		this.seedInventoryImportFileComponent.initializeValues();

		ImportedSeedInventoryList validImportedInventoryList = createValidImportedInventoryList();
		validImportedInventoryList.getImportedSeedInventoryList().get(0).setWithdrawalAmount(1.0);

		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(validImportedInventoryList);

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		this.seedInventoryImportFileComponent.processImportedInventoryTransactions();

		Assert.assertEquals(1, this.seedInventoryImportFileComponent.getProcessedTransactions().size());

		Transaction transaction = this.seedInventoryImportFileComponent.getProcessedTransactions().get(0);
		Assert.assertEquals(new Double("-1.0"), transaction.getQuantity());
		Assert.assertEquals(1, transaction.getStatus().intValue());
		Assert.assertEquals("comments", transaction.getComments());
		Assert.assertNull(validImportedInventoryList.getImportedSeedInventoryList().get(0).getTransactionProcessingStatus());

	}

	@Test
	public void testProcessImportedInventoryTransactionsSuccessWithWithdrawalGreaterThanReservationAndUnAvailable()
			throws SeedInventoryImportException, InvalidFileDataException {

		this.seedInventoryImportFileComponent.initializeValues();

		ImportedSeedInventoryList validImportedInventoryList = createValidImportedInventoryList();
		validImportedInventoryList.getImportedSeedInventoryList().get(0).setWithdrawalAmount(10.0);

		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(validImportedInventoryList);

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		this.seedInventoryImportFileComponent.processImportedInventoryTransactions();
		Assert.assertEquals(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_AVAILABLE_WARNING.toString(), validImportedInventoryList.getImportedSeedInventoryList().get(0).getTransactionProcessingStatus());
	}

	@Test
	public void testProcessImportedInventoryTransactionsSuccessWithWithdrawalGreaterThanReservationAndAvailable()
			throws SeedInventoryImportException, InvalidFileDataException {

		this.seedInventoryImportFileComponent.initializeValues();

		ImportedSeedInventoryList validImportedInventoryList = createValidImportedInventoryList();
		validImportedInventoryList.getImportedSeedInventoryList().get(0).setWithdrawalAmount(3.0);

		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(validImportedInventoryList);

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		this.seedInventoryImportFileComponent.processImportedInventoryTransactions();

		Assert.assertEquals(1, this.seedInventoryImportFileComponent.getProcessedTransactions().size());

		Transaction transaction = this.seedInventoryImportFileComponent.getProcessedTransactions().get(0);
		Assert.assertEquals(new Double("-3.0"), transaction.getQuantity());
		Assert.assertEquals(1, transaction.getStatus().intValue());
		Assert.assertEquals("comments", transaction.getComments());

		Assert.assertEquals(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_RESERVATION_WARNING.toString(), validImportedInventoryList.getImportedSeedInventoryList().get(0).getTransactionProcessingStatus());
	}


	@Test
	public void testProcessImportedInventoryTransactionsSuccessWithValidBalance()
			throws SeedInventoryImportException, InvalidFileDataException {

		this.seedInventoryImportFileComponent.initializeValues();

		ImportedSeedInventoryList validImportedInventoryList = createValidImportedInventoryList();
		validImportedInventoryList.getImportedSeedInventoryList().get(0).setWithdrawalAmount(null);
		validImportedInventoryList.getImportedSeedInventoryList().get(0).setBalanceAmount(2.0);
		this.seedInventoryImportFileComponent.setImportedSeedInventoryList(validImportedInventoryList);

		Mockito.when(this.inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		Mockito.when(this.messageSource.getMessage(Matchers.any(Message.class))).thenReturn("StockTakingAdjustment");
		this.seedInventoryImportFileComponent.validateImportedSeedInventoryList();
		this.seedInventoryImportFileComponent.processImportedInventoryTransactions();

		Assert.assertEquals(1, this.seedInventoryImportFileComponent.getProcessedTransactions().size());

		Transaction transaction = this.seedInventoryImportFileComponent.getProcessedTransactions().get(0);
		Assert.assertEquals(new Double("-3.0"), transaction.getQuantity());
		Assert.assertEquals(1, transaction.getStatus().intValue());
		Assert.assertEquals("StockTakingAdjustment", transaction.getComments());

	}

	private ImportedSeedInventoryList createValidImportedInventoryList(){
		ImportedSeedInventoryList importedSeedInventoryList = new ImportedSeedInventoryList("ListName-Seed Prep.xls");
		importedSeedInventoryList.setListName("List 1");
		List<ImportedSeedInventory> importedSeedInventories = Lists.newArrayList();

		ImportedSeedInventory importedSeedInventory = new ImportedSeedInventory();
		importedSeedInventory.setEntry(1);
		importedSeedInventory.setDesignation("Des");
		importedSeedInventory.setGid(28);
		importedSeedInventory.setLotID(1);
		importedSeedInventory.setTransactionId(110);
		importedSeedInventory.setReservationAmount(2.0);
		importedSeedInventory.setWithdrawalAmount(2.0);
		importedSeedInventory.setComments("comments");

		importedSeedInventories.add(importedSeedInventory);

		importedSeedInventoryList.setImportedSeedInventoryList(importedSeedInventories);

		return importedSeedInventoryList;
	}


}
