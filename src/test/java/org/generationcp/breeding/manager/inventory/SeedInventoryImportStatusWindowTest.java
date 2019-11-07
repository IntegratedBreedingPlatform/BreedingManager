package org.generationcp.breeding.manager.inventory;

import com.beust.jcommander.internal.Lists;
import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.ListComponent;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventory;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.InventoryDetailsTestDataInitializer;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(MockitoJUnitRunner.class)
public class SeedInventoryImportStatusWindowTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	protected OntologyDataManager ontologyDataManager;

	@Mock
	protected InventoryDataManager inventoryDataManager;

	@InjectMocks
	SeedInventoryImportStatusWindow seedInventoryImportStatusWindow = new SeedInventoryImportStatusWindow();

	@Before
	public void setUp() {
		Mockito.when(messageSource.getMessage(Message.TRANSACTION_ID)).thenReturn("TransactionId");
		Mockito.when(messageSource.getMessage(Message.WITHDRAWAL)).thenReturn(TransactionType.WITHDRAWAL.getValue());
		Mockito.when(messageSource.getMessage(Message.BALANCE)).thenReturn("Balance");
		Mockito.when(messageSource.getMessage(Message.IMPORT_PROCESSING_STATUS)).thenReturn("ProcessingStatus");
		Mockito.when(messageSource.getMessage(Message.HASHTAG)).thenReturn("#");
		Mockito.when(messageSource.getMessage(Message.CONTINUE)).thenReturn("Continue");
		Mockito.when(messageSource.getMessage(Message.CANCEL)).thenReturn("Cancel");

		Mockito.doReturn(new Term(TermId.DESIG.getId(), "Designation", "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.DESIGNATION.getTermId().getId());

		Mockito.doReturn(new Term(TermId.GID.getId(), "Gid", "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.GID.getTermId().getId());

		Mockito.doReturn(new Term(TermId.LOT_ID_INVENTORY.getId(), "LotID", "")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.LOT_ID.getTermId().getId());

		seedInventoryImportStatusWindow.instantiateComponents();

	}

	@Test
	public void testInstantiateComponents() {
		final Table statusTable = this.seedInventoryImportStatusWindow.getTable();

		Collection<?> columnIds = statusTable.getContainerPropertyIds();

		Assert.assertTrue(columnIds.size() == 8);

		Assert.assertEquals("#", statusTable.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("Designation", statusTable.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("Gid", statusTable.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("LotID", statusTable.getColumnHeader(ColumnLabels.LOT_ID.getName()));
		Assert.assertEquals("TransactionId", statusTable.getColumnHeader("TransactionId"));
		Assert.assertEquals(TransactionType.WITHDRAWAL.getValue(), statusTable.getColumnHeader(TransactionType.WITHDRAWAL.getValue()));
		Assert.assertEquals("Balance", statusTable.getColumnHeader("Balance"));
		Assert.assertEquals("ProcessingStatus", statusTable.getColumnHeader("ProcessingStatus"));
	}

	@Test
	public void testInitializeValues() {
		seedInventoryImportStatusWindow.setImportedSeedInventories(createImportedSeedInventoryList());
		Mockito.when(messageSource.getMessage(Message.SEED_IMPORT_PROCESSING_STATUS)).thenReturn("Processing");
		seedInventoryImportStatusWindow.initializeValues();

		Item item = seedInventoryImportStatusWindow.getTable().getItem(seedInventoryImportStatusWindow.getTable().lastItemId());

		Assert.assertEquals(5, seedInventoryImportStatusWindow.getImportStatusMessages().size());
		Assert.assertTrue(seedInventoryImportStatusWindow.getImportStatusMessages()
				.containsKey(Message.SEED_IMPORT_TRANSACTION_ALREADY_COMMITTED_ERROR.toString()));
		Assert.assertTrue(seedInventoryImportStatusWindow.getImportStatusMessages()
				.containsKey(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_RESERVATION_WARNING.toString()));
		Assert.assertTrue(seedInventoryImportStatusWindow.getImportStatusMessages()
				.containsKey(Message.SEED_IMPORT_WITHDRAWAL_GREATER_THAN_AVAILABLE_WARNING.toString()));
		Assert.assertTrue(
				seedInventoryImportStatusWindow.getImportStatusMessages().containsKey(Message.SEED_IMPORT_BALANCE_WARNING.toString()));
		Assert.assertTrue(seedInventoryImportStatusWindow.getImportStatusMessages().containsKey(Message.SEED_IMPORT_LOT_CLOSED.toString()));

		Assert.assertEquals(1, item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue());
		Assert.assertEquals("Des", item.getItemProperty(ColumnLabels.DESIGNATION.getName()).getValue());
		Assert.assertEquals(28, item.getItemProperty(ColumnLabels.GID.getName()).getValue());
		Assert.assertEquals(1, item.getItemProperty(ColumnLabels.LOT_ID.getName()).getValue());
		Assert.assertEquals("110", item.getItemProperty("TransactionId").getValue());
		Assert.assertEquals("2.0", item.getItemProperty(TransactionType.WITHDRAWAL.getValue()).getValue());
		Assert.assertNull(item.getItemProperty("Balance").getValue());
		Label label = (Label) item.getItemProperty("ProcessingStatus").getValue();
		Assert.assertEquals("Processing", label.getValue());
	}

	@Test
	public void testContinueClickFromListComponent() {
		seedInventoryImportStatusWindow.setProcessedTransactions(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		Component source = Mockito.mock(Component.class);
		seedInventoryImportStatusWindow.setSource(source);
		Window window = Mockito.mock(Window.class);

		Mockito.when(source.getWindow()).thenReturn(window);

		ListComponent listComponent = Mockito.mock(ListComponent.class);
		seedInventoryImportStatusWindow.setListComponent(listComponent);

		this.seedInventoryImportStatusWindow.addListeners();
		this.seedInventoryImportStatusWindow.getContinueButton().click();

		Mockito.verify(inventoryDataManager).addTransactions(InventoryDetailsTestDataInitializer.createValidReservedTransactions());
		Mockito.verify(this.messageSource).getMessage(Message.SEED_IMPORT_SUCCESS);
		Mockito.verify(source).getWindow();
		Mockito.verify(listComponent).resetListInventoryTableValues();
		Mockito.verify(listComponent).resetListDataTableValues();
	}

	@Test
	public void testContinueClickFromListBuilderComponent() {
		seedInventoryImportStatusWindow.setProcessedTransactions(InventoryDetailsTestDataInitializer.createValidReservedTransactions());

		Component source = Mockito.mock(Component.class);
		seedInventoryImportStatusWindow.setSource(source);
		Window window = Mockito.mock(Window.class);

		Mockito.when(source.getWindow()).thenReturn(window);

		ListBuilderComponent listComponent = Mockito.mock(ListBuilderComponent.class);
		seedInventoryImportStatusWindow.setListComponent(listComponent);

		this.seedInventoryImportStatusWindow.addListeners();
		this.seedInventoryImportStatusWindow.getContinueButton().click();

		Mockito.verify(inventoryDataManager).addTransactions(InventoryDetailsTestDataInitializer.createValidReservedTransactions());
		Mockito.verify(this.messageSource).getMessage(Message.SEED_IMPORT_SUCCESS);
		Mockito.verify(source).getWindow();
		Mockito.verify(listComponent).resetListInventoryTableValues();
		Mockito.verify(listComponent).resetListDataTableValues();
	}

	@Test
	public void testContinueClickWithMultipleThreads() {
		List<Transaction> validReservedTransactions = InventoryDetailsTestDataInitializer.createValidReservedTransactions();
		validReservedTransactions.get(0).setStatus(1);
		seedInventoryImportStatusWindow.setProcessedTransactions(validReservedTransactions);

		Component source = Mockito.mock(Component.class);
		seedInventoryImportStatusWindow.setSource(source);
		Window window = Mockito.mock(Window.class);

		Mockito.when(source.getWindow()).thenReturn(window);

		ListComponent listComponent = Mockito.mock(ListComponent.class);
		seedInventoryImportStatusWindow.setListComponent(listComponent);

		Mockito.when(inventoryDataManager.getTransactionsByIdList(Mockito.isA(List.class))).thenReturn(validReservedTransactions);
		this.seedInventoryImportStatusWindow.addListeners();

		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		Future<Void> threadOne = threadPool.submit(new Callable<Void>() {

			@Override
			public Void call() {
				seedInventoryImportStatusWindow.getContinueButton().click();
				return null;
			}
		});

		Future<Void> threadTwo = threadPool.submit(new Callable<Void>() {

			@Override
			public Void call() {
				seedInventoryImportStatusWindow.getContinueButton().click();
				return null;
			}
		});

		threadPool.shutdown();
		while (!threadPool.isTerminated()) {
		}

		Mockito.verify(inventoryDataManager, Mockito.never())
				.addTransactions(InventoryDetailsTestDataInitializer.createValidReservedTransactions());
		Mockito.verify(this.messageSource, Mockito.times(2)).getMessage(Message.SEED_IMPORT_TRANSACTION_ALREADY_COMMITTED_ERROR);
		Mockito.verify(source, Mockito.times(2)).getWindow();
		Mockito.verify(listComponent, Mockito.never()).refreshInventoryListDataTabel();
		Mockito.verify(listComponent, Mockito.never()).resetListDataTableValues();
	}

	@Test
	public void testCancelClickAction() {
		Component source = Mockito.mock(Component.class);
		seedInventoryImportStatusWindow.setSource(source);
		Window window = Mockito.mock(Window.class);

		Mockito.when(source.getWindow()).thenReturn(window);

		ListComponent listComponent = Mockito.mock(ListComponent.class);
		seedInventoryImportStatusWindow.setListComponent(listComponent);

		this.seedInventoryImportStatusWindow.addListeners();
		seedInventoryImportStatusWindow.getCancelButton().click();
		Mockito.verify(source).getWindow();
		Mockito.verify(this.messageSource).getMessage(Message.SEED_IMPORT_CANCEL);

	}

	private List<ImportedSeedInventory> createImportedSeedInventoryList() {
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
		return importedSeedInventories;
	}
}
