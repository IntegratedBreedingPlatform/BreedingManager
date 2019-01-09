package org.generationcp.breeding.manager.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.ListInventoryDataInitializer;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReserveInventoryActionTest {

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private UserDataManager userDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private ReserveInventorySource reserveInventorySource;

	private ImportedGermplasmListDataInitializer importedGermplasmListInitializer;

	@InjectMocks
	private final ReserveInventoryAction reserveInventoryAction = new ReserveInventoryAction(reserveInventorySource);

	private static final int LIST_ID = 1;

	@Before
	public void testSetUp() {
		importedGermplasmListInitializer = new ImportedGermplasmListDataInitializer();

		final User user = new User();
		user.setUserid(12);
		user.setPersonid(123);

		Mockito.doReturn(user).when(this.userDataManager).getUserById(Matchers.anyInt());
		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(1);

	}

	@Test
	public void testSaveReserveTransactionsWithValidReservations() {
		Map<ListEntryLotDetails, Double> reservations = this.importedGermplasmListInitializer.createReservations(1);

		List<GermplasmListData> germplasmListData = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();

		Mockito.when(this.inventoryDataManager.getLotDetailsForList(Mockito.isA(Integer.class), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(germplasmListData);

		boolean status = reserveInventoryAction.saveReserveTransactions(reservations, LIST_ID);

		Assert.assertTrue(status);
		Mockito.verify(inventoryDataManager, Mockito.times(1)).addTransactions(ArgumentMatchers.<List<Transaction>>any());
	}

	@Test
	public void testSaveReserveTransactionsWithValidReservationsAndLotAlreadyReserved() {
		Map<ListEntryLotDetails, Double> reservations = this.importedGermplasmListInitializer.createReservations(1);

		List<GermplasmListData> germplasmListData = ListInventoryDataInitializer.createGermplasmListDataWithInventoryDetails();
		germplasmListData.get(0).getInventoryInfo().getLotRows().get(0).setWithdrawalStatus(ListDataInventory.RESERVED);

		Mockito.when(this.inventoryDataManager.getLotDetailsForList(Mockito.isA(Integer.class), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(germplasmListData);

		boolean status = reserveInventoryAction.saveReserveTransactions(reservations, LIST_ID);

		Assert.assertFalse(status);
	}


	@Test
	public void testSaveReserveTransactionsWithNoValidReservation() {
		boolean status = reserveInventoryAction.saveReserveTransactions(new HashMap<ListEntryLotDetails, Double>(), LIST_ID);

		Assert.assertTrue(status);
		Mockito.verify(inventoryDataManager).addTransactions(new ArrayList<Transaction>());
	}
}
