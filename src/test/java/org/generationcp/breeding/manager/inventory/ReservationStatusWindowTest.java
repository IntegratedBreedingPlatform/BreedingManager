package org.generationcp.breeding.manager.inventory;

import java.util.Collection;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class ReservationStatusWindowTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	private ImportedGermplasmListDataInitializer importedGermplasmListDataInitializer;

	private ReservationStatusWindow seedInventoryImportStatusWindow;

	@Before
	public void setUp() {
		Mockito.when(messageSource.getMessage(Message.RESERVATION_STATUS)).thenReturn("Reservation Status");
		Mockito.when(messageSource.getMessage(Message.RESERVATION_STATUS_TABLE_CAPTION)).thenReturn("Reservation Status table caption");

		Mockito.when(messageSource.getMessage(Message.LOT_ID)).thenReturn("LOT ID");
		Mockito.when(messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER)).thenReturn("DESIGNATION");
		Mockito.when(messageSource.getMessage(Message.LOCATION_HEADER)).thenReturn("LOCATION");
		Mockito.when(messageSource.getMessage(Message.UNITS)).thenReturn("UNITS");
		Mockito.when(messageSource.getMessage(Message.AVAILABLE_BALANCE)).thenReturn("AVAILABLE BALANCE");
		Mockito.when(messageSource.getMessage(Message.AMOUNT_TO_RESERVE)).thenReturn("AMOUNT TO RESERVE");
		Mockito.when(messageSource.getMessage(Message.RESERVATION_STATUS)).thenReturn("RESERVATION STATUS");

		Mockito.when(messageSource.getMessage(Message.OK)).thenReturn("OK");

		importedGermplasmListDataInitializer = new ImportedGermplasmListDataInitializer();

		seedInventoryImportStatusWindow = new ReservationStatusWindow(Maps.<ListEntryLotDetails, Double>newHashMap());
		seedInventoryImportStatusWindow.setMessageSource(messageSource);

	}

	@Test
	public void testInstantiateComponents() {
		seedInventoryImportStatusWindow.instantiateComponents();

		Table statusTable = seedInventoryImportStatusWindow.getStatusTable();

		Collection<?> columnIds = statusTable.getContainerPropertyIds();

		Assert.assertTrue(columnIds.size() == 7);

		Assert.assertEquals("LOT ID", statusTable.getColumnHeader(this.messageSource.getMessage(Message.LOT_ID)));
		Assert.assertEquals("DESIGNATION", statusTable.getColumnHeader(this.messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER)));
		Assert.assertEquals("LOCATION", statusTable.getColumnHeader(this.messageSource.getMessage(Message.LOCATION_HEADER)));
		Assert.assertEquals("UNITS", statusTable.getColumnHeader(this.messageSource.getMessage(Message.UNITS)));
		Assert.assertEquals("AVAILABLE BALANCE", statusTable.getColumnHeader(this.messageSource.getMessage(Message.AVAILABLE_BALANCE)));
		Assert.assertEquals("AMOUNT TO RESERVE", statusTable.getColumnHeader(this.messageSource.getMessage(Message.AMOUNT_TO_RESERVE)));
		Assert.assertEquals("RESERVATION STATUS", statusTable.getColumnHeader(this.messageSource.getMessage(Message.RESERVATION_STATUS)));

	}

	@Test
	public void testInitializeValues() {
		Map<ListEntryLotDetails, Double> reservations = importedGermplasmListDataInitializer.createReservations(1);
		seedInventoryImportStatusWindow.setInvalidLotReservations(reservations);

		Name name = new Name();
		name.setNval("nVal");
		Mockito.when(this.germplasmDataManager.getPreferredNameByGID(Mockito.isA(Integer.class))).thenReturn(name);

		seedInventoryImportStatusWindow.setGermplasmDataManager(germplasmDataManager);

		seedInventoryImportStatusWindow.instantiateComponents();
		seedInventoryImportStatusWindow.initializeValues();

		Item item = seedInventoryImportStatusWindow.getStatusTable().getItem(seedInventoryImportStatusWindow.getStatusTable().lastItemId());

		Assert.assertEquals(1, item.getItemProperty(this.messageSource.getMessage(Message.LOT_ID)).getValue());
		Assert.assertEquals("nVal", item.getItemProperty(this.messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER)).getValue());
		Assert.assertEquals("Location1", item.getItemProperty(this.messageSource.getMessage(Message.LOCATION_HEADER)).getValue());
		Assert.assertEquals("Scale1", item.getItemProperty(this.messageSource.getMessage(Message.UNITS)).getValue());
		Assert.assertEquals(100.0, item.getItemProperty(this.messageSource.getMessage(Message.AVAILABLE_BALANCE)).getValue());
		Assert.assertEquals(0.0, item.getItemProperty(this.messageSource.getMessage(Message.AMOUNT_TO_RESERVE)).getValue());
		Assert.assertEquals("1", item.getItemProperty(this.messageSource.getMessage(Message.RESERVATION_STATUS)).getValue());

	}

	@Test
	public void testLayoutComponents() {
		seedInventoryImportStatusWindow.instantiateComponents();
		seedInventoryImportStatusWindow.layoutComponents();

		Assert.assertEquals(3, seedInventoryImportStatusWindow.getMainLayout().getComponentCount());
	}

}
